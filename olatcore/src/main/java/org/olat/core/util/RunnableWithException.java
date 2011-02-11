package org.olat.core.util;

/**
 * Generic interface similar to Runnable except that it allows
 * an Exception to be thrown from run() and not only an Error
 * or a RuntimeException.
 * <p>
 * Initial Date:  22.10.2009 <br>
 * @author Stefan
 */
public interface RunnableWithException {

	/**
	 * Similar to Runnable.run - i.e. executes this RunnableWithException's
	 * action - plus allowing an Exception to be thrown
	 * @throws Exception when something went wrong
	 */
	public void run() throws Exception;
	
}
