/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.commons.persistence.async;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.DBRuntimeException;


/**
 * @author Christian Guretzki
 */
public abstract class AbstractBackgroundTask implements BackgroundTask {
	private static Logger log = Logger.getLogger(AbstractBackgroundTask.class.getName());
	
	private int maxRetry = 1000;// 0 = endless retry

	private boolean taskDone = false;
	
	public AbstractBackgroundTask() {
	}
	
	public void execute() {
		boolean finished = false;
		int retry = 0;
		while(!finished ) {
			try {
				executeTask();
				DBFactory.getInstance().commitAndCloseSession();
				finished = true;
				// catch all exception because other cluster nodes can write data concurrently
			} catch (DBRuntimeException re) {
				log.debug("DBRuntimeException in executeTask retry=" + retry + " Ex=" + re);
				retry++;
				finished = handleError( retry);
			} catch (HibernateException re) {
				log.debug("RuntimeException in executeTask retry=" + retry);
				retry++;
				finished = handleError( retry);
			} catch (Error re) {
				log.debug("Error in executeTask retry=" + retry);
				retry++;
				finished = handleError( retry);
			} catch(Throwable th) {
				log.debug("Error in executeTask retry=" + retry);
				retry++;
				finished = handleError( retry);
			}
			if (!finished) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// empty is ok
				}
			}
		}
		if (retry > 0) {
			if (retry >= maxRetry) {
				log.error("Too many retries, check background-task , retry=" + retry );
			} else {
				log.debug("retry=" + retry );
			}
		}
		log.debug("set taskDone=true this=" + this);
		taskDone  = true;
	}
	
	
	private boolean handleError( int retry) {
	    boolean finished = false;
		if ( (maxRetry != 0 ) && (retry >= maxRetry) ){
	    	// finished because to many retries
	    	finished = true;
	    }
    try {
    	DBFactory.getInstance().rollbackAndCloseSession();
    	log.debug("handleError DB-Session rolled back and closed");
    } catch (DBRuntimeException re) {
    	log.debug("###DBRuntimeException2, Could not rollbackAndCloseSession properly after Exception in BackgroundTask.execute");
    } catch (HibernateException re2) {
    	log.debug("###HibernateException2, Could not rollbackAndCloseSession properly after Exception in BackgroundTask.execute");
    } catch (Error re) {
    	log.debug("###Error2, Could not rollbackAndCloseSession properly after Exception in BackgroundTask.execute");
    } catch (Throwable th2) {
    	log.debug("###Could not rollbackAndCloseSession properly after Exception in BackgroundTask.execute" );
    }
	    return finished;
    }

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public abstract void executeTask();

	public boolean isTaskDone() {
    	return taskDone;
    }

	public void waitForDone() {
		log.debug("waitForDone start");
		// Wait until background task is done or reach timeout
		int counter = 0;
		int COUNTER_LIMIT = 10;
		while (!this.isTaskDone() && counter < COUNTER_LIMIT) {
			try {
				log.debug("waitForDone: this.isTaskDone()=" + this.isTaskDone() + "   counter=" + counter + "  this=" + this);
	            Thread.currentThread().sleep(200);
	            counter++;
            } catch (InterruptedException e) {
            	// no log
            }
		}
		if (counter >= COUNTER_LIMIT) {
			log.error("waitForDone: Could not finish BackgroundTask");
		}
    }

}
