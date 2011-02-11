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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.dispatcher;

import java.util.Map;

/**
 * Holds the configured dispatchers from the
 * <code>_spring/defaultconfig.xml</code> and
 * <code>_spring/extconfig.xml</code>
 * <P>
 * Initial Date: 20.06.2006 <br>
 * 
 * @author patrickb
 */
public class DispatcherConfig {
	private Map dispatchers;
	private Map extconfigdispatchers;

	/**
	 * @return Returns the extconfigdispatchers.
	 */
	public Map getExtconfigdispatchers() {
		return extconfigdispatchers;
	}

	/**
	 * @param extconfigdispatchers The extconfigdispatchers to set.
	 */
	public void setExtconfigdispatchers(Map extconfigdispatchers) {
		this.extconfigdispatchers = extconfigdispatchers;
	}

	/**
	 * [key, value] pairs<br>
	 * <ul>
	 * <li>key is a String with the dispatcher path, e.g. /dmz/ or /auth/ or /webstat.html</li>
	 * <li>value is of type <code>Dispatcher</code></li>
	 * </ul>
	 * @return Returns the dispatchers.
	 */
	public Map getDispatchers() {
		return dispatchers;
	}

	/**
	 * called only by spring
	 * @param dispatchers The dispatchers to set.
	 */
	public void setDispatchers(Map dispatchers) {
		this.dispatchers = dispatchers;
	}
}
