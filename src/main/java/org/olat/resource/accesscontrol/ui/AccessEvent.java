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

package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.control.Event;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  15 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AccessEvent extends Event {
	
	private static final long serialVersionUID = -9220571332758113408L;
	public static final String ACCESS_FAILED = "access-failed";
	public static final String ACCESS_OK = "access-ok";
	
	public static final AccessEvent ACCESS_FAILED_EVENT = new AccessEvent();
	public static final AccessEvent ACCESS_OK_EVENT = new AccessEvent(ACCESS_OK);
	
	private final String message;
	
	public AccessEvent() {
		this(ACCESS_FAILED, null);
	}
	
	public AccessEvent(String cmd) {
		this(cmd, null);
	}
	
	public AccessEvent(String cmd, String message) {
		super(cmd);
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public int hashCode() {
		return getCommand().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if( obj instanceof AccessEvent) {
			AccessEvent e = (AccessEvent)obj;
			return getCommand() != null && getCommand().equals(e.getCommand());
		}
		return false;
	}
}