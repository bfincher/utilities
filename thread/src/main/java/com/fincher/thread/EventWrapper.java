package com.fincher.thread;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/** Wraps an event to be executed in the future
 * 
 * @author Brian Fincher
 *
 */
class EventWrapper implements MyRunnableScheduledFuture<Boolean>, RunnableWithIdIfc {
	
	private static Logger logger = Logger.getLogger(EventWrapper.class);	
	
	/** The body of this event */
	private final RunnableWithIdIfc task;		
	
	/** The time (in millis since 1070) at which this event should execute */
	protected long nextExecutionTime;		
	
	/** If executing, the thread that is executing */
	private Thread currentThread = null;
	
	/** The time at which this event began execution */
	private long beginExecutionTime;	
	
	/** The state of this event */
	protected enum StateEnum {
		PENDING,
		RUNNING,
		CANCELLED,
		COMPLETED
	}
	
	private StateEnum state = StateEnum.PENDING;
	
	protected final Object stateEnumSynchronizer = new Object();
	
	/** Creates a new non repeating EventWrapper
	 * 
	 * @param task The body of the event
	 * @param nextExecutionTime The time at which this event should execute (in millis since 1970)
	 */
	public EventWrapper(RunnableWithIdIfc task, long nextExecutionTime) {
		this.task = task;
		this.nextExecutionTime = nextExecutionTime;
	}		
	
	@Override
	public boolean isPeriodic() {
		return false;
	}	
	
	protected void setState(StateEnum newState) {
		synchronized (stateEnumSynchronizer) {
			this.state = newState;
			stateEnumSynchronizer.notifyAll();
		}
	}		
	
	@Override
	public void run() {				
		synchronized (stateEnumSynchronizer) {
			if (state == StateEnum.CANCELLED)
				return;
			
			setState(StateEnum.RUNNING);
		}
		
		currentThread = Thread.currentThread();
		beginExecutionTime = System.currentTimeMillis();
		try {			
			task.run();
			currentThread = null;
		}
		catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
		
		postExecute();				
	}
	
	@Override
	public long getDelay(TimeUnit unit) {
		long delayInMillis = nextExecutionTime - System.currentTimeMillis();
		return delayInMillis;		
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		
		boolean result = true;
		
		synchronized (stateEnumSynchronizer) {
			switch (state) {
			case CANCELLED:
				result = true;
				break;
				
			case COMPLETED:
				result = false;;
				break;
				
			case PENDING:				
				result = true;
				break;
				
			case RUNNING:
				if (mayInterruptIfRunning) {
					if (currentThread != null) {
						currentThread.interrupt();
						result = true;
					}
					else
						result = false;
				}
				else {
					result = false;
				}
			}
		}
		
		if (result)
			setState(StateEnum.CANCELLED);
		
		if (logger.isTraceEnabled()) {
			logger.trace(getId() +" cancel:  mayInterruptIfRunning = " + mayInterruptIfRunning + ".  Result = " + result);
		}
		
		return result;		
	}
	
	@Override
	public boolean isCancelled() {
		return state == StateEnum.CANCELLED;
	}
	
	@Override
	public boolean isDone() {
		switch (state) {
		case CANCELLED:
		case COMPLETED:
			return true;
			
		default:
			return false;
		}
	}
	
	@Override
	public Boolean get() throws InterruptedException, CancellationException {
		if (isCancelled())
			throw new CancellationException();
		
		synchronized (stateEnumSynchronizer) {
			if (isDone())
				return Boolean.TRUE;
			else {
				stateEnumSynchronizer.wait();
				if (isCancelled())
					throw new CancellationException();
				
				return Boolean.TRUE;
			}
		}
	}
	 
	@Override
	public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, CancellationException {
		if (isCancelled())
			throw new CancellationException();
		
		synchronized (stateEnumSynchronizer) {
			if (isDone())
				return Boolean.TRUE;
			else {
				stateEnumSynchronizer.wait(unit.toMillis(timeout));
				
				if (isCancelled())
					throw new CancellationException();
				
				if (isDone())
					return Boolean.TRUE;
				else
					return false;
			}
		}
	}
	
	@Override
	public int compareTo(Delayed delayed) {
		long thisDelayMillis = getDelay(TimeUnit.MILLISECONDS);
		long delayedMillis = delayed.getDelay(TimeUnit.MILLISECONDS);
		
		if (thisDelayMillis < delayedMillis)
			return -1;
		else if (thisDelayMillis == delayedMillis)
			return 0;
		else
			return 1;
	}
	
	@Override
	public String getId() {
		return task.getId() + "EventWrapper";
	}
	
	/** Is called after the event is executed */
	protected void postExecute() {
		setState(StateEnum.COMPLETED);
	}
	
	/** Get the time at which this event began execution
	 * 
	 * @return the time at which this event began execution (in millis since 1970)
	 */
	protected long getBeginExecutionTime() {
		return beginExecutionTime;
	}
	
	@Override
	public RunnableWithIdIfc getTask() {
		return task;
	}

}
