/**
* OLAT - Online Learning and Training<br />
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br />
* you may not use this file except in compliance with the License.<br />
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br />
* software distributed under the License is distributed on an "AS IS" BASIS, <br />
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
* See the License for the specific language governing permissions and <br />
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.instantMessaging;
/**
 * Description:<br />
 * Evaluates the log level to be relevant for sending to the IM users.
 * Set return event.getLevel().isGreaterOrEqual(Level.INFO); to the level you wish.
 * Initial Date:  13.04.2005 <br />
 *
 * @author guido
 */
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;


/**
 * <code>DefaultEvaluator</code> implements the single method
 * <code>TriggeringEventEvaluator</code> interface. This class
 * allows <code>IMAppender</code> to decide when to perform the
 * IM message delivery.
 *
 * @author Rafael Luque & Ruth Zamorano
 */

public class IMEvaluator implements TriggeringEventEvaluator {
	public IMEvaluator(){
		//
	}

    /**
     * Is this <code>event</code> the e-mail triggering event?
     *
     * <p>This method returns <code>true</code> if the event level
     * has ERROR level or higher. Otherwise it returns
     * <code>false</code>. 
     */
    public boolean isTriggeringEvent(LoggingEvent event) {
        return event.getLevel().isGreaterOrEqual(Level.WARN);
    }
}
