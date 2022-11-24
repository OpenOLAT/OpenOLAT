/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util;

/**
 * Description:<br>
 * This class can return true or false depending on the initial value. If
 * initialized with 'true', the return value will be 'true' the first time. All
 * following requests of will return 'false'
 * 
 * <P>
 * Initial Date: 08.02.2008 <br>
 * 
 * @author Florian Gnägi, frentix GmbH
 */
public class ConsumableBoolean {
	boolean isTrue;
	
	/**
	 * Constructor
	 * @param initialValue
	 */
	public ConsumableBoolean(boolean initialValue) {
		isTrue = initialValue;
	}
	
	
	/**
	 * @return true if initialized with a true value and the call of the message
	 *         is the first call. false for all subsequent requests and if
	 *         initialized with a false value
	 */
	public synchronized boolean isTrue() {
		if(isTrue) {
			isTrue = false;
			return true;
		}
		return false;			
	}


	/**
	 * Reset the value of the consumable boolean. 
	 * @param value
	 */
	public void setTrue(boolean value) {
		isTrue = value;		
	}
}
