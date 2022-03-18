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

import java.io.Serializable;
import java.util.Date;

import org.olat.core.util.StringHelper;

/**
 * The SysInfoMessage contains either an info or maintenance message depeding on
 * the type. It can have optional dates to define the publication time frame of
 * the message
 * 
 * Initial date: 17 March. 2022<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */

public class SysInfoMessage implements Serializable {
	private static final long serialVersionUID = -5726906655347623869L;
	private static final String EMPTY_MESSAGE = "";

	private String type;
	private String message;
	private Date start;
	private Date end;

	/**
	 * A SysInfoMessage that represents an info or maintenance message
	 * 
	 * @param type    InfoMessageManager.INFO_MSG or
	 *                InfoMessageManager.MAINTENANCE_MSG
	 * @param message The message. An emtpy message, message containing only
	 *                whitespace or NULL is treated as "no message"
	 * @param start Optional publication start date or NULL
	 * @param end Optional publication end date or NULL
	 */
	public SysInfoMessage(final String type, final String message, final Date start, final Date end) {
		super();
		this.type = type;
		this.message = (StringHelper.containsNonWhitespace(message) ? message : EMPTY_MESSAGE);
		this.start = start;
		this.end = end;
	}
	
	/**
	 * @return The message type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Get the raw message, regardless of current time limitations. Returns an empty
	 * string if no message is set, never NULL
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Get the message only if within the optional start-end time frame. Returns an
	 * empty string if no message is set for the current time, never NULL
	 * 
	 * @return
	 */
	public String getTimedMessage() {		
		Date now = new Date();
		if ( (start != null && now.before(start)) || (end != null && now.after(end)) ) {
			return EMPTY_MESSAGE;
		}		
		return message;
	}
	
	/**
	 * @return An optional publication start date or NULL
	 */
	public Date getStart() {
		return start;
	}
	
	/**
	 * @return An optional publication end date or NULL
	 */
	public Date getEnd() {
		return end;
	}
	
	/**
	 * @return true: there is a message regardless of any optional start / end
	 *         dates; false: no message set
	 */
	public boolean hasMessage() {
		return (!EMPTY_MESSAGE.equals(message));
	}
}
