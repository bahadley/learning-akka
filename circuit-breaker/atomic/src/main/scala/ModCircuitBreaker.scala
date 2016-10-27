package basic

import akka.AkkaException
import akka.actor.Scheduler
import akka.util.Unsafe

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicInteger, AtomicLong, AtomicReference }

import scala.concurrent.{ ExecutionContext, Future, Promise, TimeoutException }
import scala.concurrent.duration._
import scala.util.control.{ NonFatal, NoStackTrace }
import scala.util.Success


class ModCircuitBreaker(
  scheduler:                Scheduler,
  maxFailures:              Int,
  callTimeout:              FiniteDuration,
  resetTimeout:             FiniteDuration)(implicit executor: ExecutionContext) {

  private var _currentState: AtomicReference[State] = new AtomicReference(Closed)

  /**
   * Wraps invocations of asynchronous calls that need to be protected
   *
   * @param body Call needing protected
   * @return [[scala.concurrent.Future]] containing the call result or a
   *   `scala.concurrent.TimeoutException` if the call timed out
   *
   */
  def guard[T](body: ⇒ Future[T]): Future[T] = _currentState.get().invoke(body)

  /**
   * Adds a callback to execute when circuit breaker opens
   *
   * The callback is run in the [[scala.concurrent.ExecutionContext]] supplied in the constructor.
   *
   * @param callback Handler to be invoked on state change
   * @return ModCircuitBreaker for fluent usage
   */
  def onOpen(callback: ⇒ Unit): ModCircuitBreaker = onOpen(new Runnable { def run = callback })

  /**
   * Java API for onOpen
   *
   * @param callback Handler to be invoked on state change
   * @return ModCircuitBreaker for fluent usage
   */
  def onOpen(callback: Runnable): ModCircuitBreaker = {
    Open addListener callback
    this
  }

  /**
   * Adds a callback to execute when circuit breaker transitions to half-open
   * The callback is run in the [[scala.concurrent.ExecutionContext]] supplied in the constructor.
   *
   * @param callback Handler to be invoked on state change
   * @return ModCircuitBreaker for fluent usage
   */
  def onHalfOpen(callback: ⇒ Unit): ModCircuitBreaker = onHalfOpen(new Runnable { def run = callback })

  /**
   * Adds a callback to execute when circuit breaker state closes
   *
   * The callback is run in the [[scala.concurrent.ExecutionContext]] supplied in the constructor.
   *
   * @param callback Handler to be invoked on state change
   * @return ModCircuitBreaker for fluent usage
   */
  def onClose(callback: ⇒ Unit): ModCircuitBreaker = onClose(new Runnable { def run = callback })

  /**
   * Helper method for access to underlying state via Unsafe
   *
   * @param oldState Previous state on transition
   * @param newState Next state on transition
   * @return Whether the previous state matched correctly
   */
  private def swapState(oldState: State, newState: State): Boolean =
    _currentState.compareAndSet(oldState, newState)

  /**
   * Implements consistent transition between states. Throws IllegalStateException if an invalid transition is attempted.
   *
   * @param fromState State being transitioning from
   * @param toState State being transitioning from
   */
  private def transition(fromState: State, toState: State): Unit = {
    if (swapState(fromState, toState))
      toState.enter()
    // else some other thread already swapped state
  }

  /**
   * Trips breaker to an open state.  This is valid from Closed or Half-Open states.
   *
   * @param fromState State we're coming from (Closed or Half-Open)
   */
  private def tripBreaker(fromState: State): Unit = transition(fromState, Open)

  /**
   * Resets breaker to a closed state.  This is valid from an Half-Open state only.
   *
   */
  private def resetBreaker(): Unit = transition(HalfOpen, Closed)

  /**
   * Attempts to reset breaker by transitioning to a half-open state.  This is valid from an Open state only.
   *
   */
  private def attemptReset(): Unit = transition(Open, HalfOpen)


  /**
   * Internal state abstraction
   */
  private sealed trait State {
    private val timeoutFuture = Future.failed(new TimeoutException("Circuit Breaker Timed out.") with NoStackTrace)

    private val listeners = new CopyOnWriteArrayList[Runnable]

    /**
     * Add a listener function which is invoked on state entry
     *
     * @param listener listener implementation
     */
    def addListener(listener: Runnable): Unit = listeners add listener

    /**
     * Test for whether listeners exist
     *
     * @return whether listeners exist
     */
    private def hasListeners: Boolean = !listeners.isEmpty

    /**
     * Notifies the listeners of the transition event via a Future executed in implicit parameter ExecutionContext
     *
     * @return Promise which executes listener in supplied [[scala.concurrent.ExecutionContext]]
     */
    protected def notifyTransitionListeners() {
      if (hasListeners) {
        val iterator = listeners.iterator
        while (iterator.hasNext) {
          val listener = iterator.next
          executor.execute(listener)
        }
      }
    }

    /**
     * Shared implementation of call across all states.  Thrown exception or execution of the call beyond the allowed
     * call timeout is counted as a failed call, otherwise a successful call
     *
     * @param body Implementation of the call
     * @return Future containing the result of the call
     */
    def callThrough[T](body: ⇒ Future[T]): Future[T] = {

      def materialize[U](value: ⇒ Future[U]): Future[U] = try value catch { case NonFatal(t) ⇒ Future.failed(t) }

      if (callTimeout == Duration.Zero) {
        materialize(body)
      } else {
        val p = Promise[T]()

        //implicit val ec = sameThreadExecutionContext
        p.future.onComplete {
          case s: Success[_] ⇒ callSucceeds()
          case _             ⇒ callFails()
        }

        val timeout = scheduler.scheduleOnce(callTimeout) {
          p tryCompleteWith timeoutFuture
        }

        materialize(body).onComplete { result ⇒
          p tryComplete result
          timeout.cancel
        }
        p.future
      }
    }

    /**
     * Abstract entry point for all states
     *
     * @param body Implementation of the call that needs protected
     * @return Future containing result of protected call
     */
    def invoke[T](body: ⇒ Future[T]): Future[T]

    /**
     * Invoked when call succeeds
     *
     */
    def callSucceeds(): Unit

    /**
     * Invoked when call fails
     *
     */
    def callFails(): Unit

    /**
     * Invoked on the transitioned-to state during transition.  Notifies listeners after invoking subclass template
     * method _enter
     *
     */
    final def enter(): Unit = {
      _enter()
      notifyTransitionListeners()
    }

    /**
     * Template method for concrete traits
     *
     */
    def _enter(): Unit
  }

  /**
   * Concrete implementation of Closed state
   */
  private object Closed extends AtomicInteger(0) with State {

    /**
     * Implementation of invoke, which simply attempts the call
     *
     * @param body Implementation of the call that needs protected
     * @return Future containing result of protected call
     */
    override def invoke[T](body: ⇒ Future[T]): Future[T] = callThrough(body)

    /**
     * On successful call, the failure count is reset to 0
     *
     * @return
     */
    override def callSucceeds(): Unit = set(0)

    /**
     * On failed call, the failure count is incremented.  The count is checked against the configured maxFailures, and
     * the breaker is tripped if we have reached maxFailures.
     *
     * @return
     */
    override def callFails(): Unit = if (incrementAndGet() == maxFailures) tripBreaker(Closed)

    /**
     * On entry of this state, failure count and resetTimeout is reset.
     *
     * @return
     */
    override def _enter(): Unit = {
      set(0)
      //swapResetTimeout(currentResetTimeout, resetTimeout)
    }
  }

  /**
   * Concrete implementation of half-open state
   */
  private object HalfOpen extends AtomicBoolean(true) with State {

    /**
     * Allows a single call through, during which all other callers fail-fast.  If the call fails, the breaker reopens.
     * If the call succeeds the breaker closes.
     *
     * @param body Implementation of the call that needs protected
     * @return Future containing result of protected call
     */
    override def invoke[T](body: ⇒ Future[T]): Future[T] =
      if (compareAndSet(true, false)) callThrough(body) else Promise.failed[T](new ModCircuitBreakerOpenException()).future

    /**
     * Reset breaker on successful call.
     *
     * @return
     */
    override def callSucceeds(): Unit = resetBreaker()

    /**
     * Reopen breaker on failed call.
     *
     * @return
     */
    override def callFails(): Unit = tripBreaker(HalfOpen)

    /**
     * On entry, guard should be reset for that first call to get in
     *
     * @return
     */
    override def _enter(): Unit = set(true)
  }

  /**
   * Concrete implementation of Open state
   */
  private object Open extends AtomicLong with State {

    /**
     * Fail-fast on any invocation
     *
     * @param body Implementation of the call that needs protected
     * @return Future containing result of protected call
     */
    override def invoke[T](body: ⇒ Future[T]): Future[T] =
      Promise.failed[T](new ModCircuitBreakerOpenException()).future

    /**
     * No-op for open, calls are never executed so cannot succeed or fail
     *
     * @return
     */
    override def callSucceeds(): Unit = ()

    /**
     * No-op for open, calls are never executed so cannot succeed or fail
     *
     * @return
     */
    override def callFails(): Unit = ()

    /**
     * On entering this state, schedule an attempted reset via [[akka.actor.Scheduler]] and store the entry time to
     * calculate remaining time before attempted reset.
     *
     * @return
     */
    override def _enter(): Unit = {
      set(System.nanoTime())
      scheduler.scheduleOnce(resetTimeout) {
        attemptReset()
      }
    }
  }

}

/**
 * Exception thrown when Circuit Breaker is open.
 *
 * @param message Defaults to "Circuit Breaker is open; calls are failing fast"
 */
class ModCircuitBreakerOpenException(
  message: String = "Circuit Breaker is open; calls are failing fast") extends AkkaException(message) with NoStackTrace
