/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.admin.sysinfo;

import org.olat.core.util.event.MultiUserEvent;

/**
 * This event is fired when any change for SysImfoMessages happen. The message
 * of the event can be "deleted" or "modified"
 * 
 * Initial date: 17 March. 2022<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class SysInfoMessageChangedEvent extends MultiUserEvent {
	public static final String CMD_DELETED = "deleted";
	public static final String CMD_MODIFIED = "modified";

	private static final long serialVersionUID = 3199767160246830180L;

	private final SysInfoMessage sysInfoMessage;

	/**
	 * @param sysInfoMessage The changed SysInfoMessage
	 */
	public SysInfoMessageChangedEvent(SysInfoMessage sysInfoMessage) {
		super(sysInfoMessage.hasMessage() ? CMD_MODIFIED : CMD_DELETED);
		this.sysInfoMessage = sysInfoMessage;
	}

	/**
	 * @return The changed SysInfoMessage
	 */
	public SysInfoMessage getSysInfoMessage() {
		return sysInfoMessage;
	}
}
