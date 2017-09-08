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

package org.olat.ldap;

import java.util.Date;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Description:<br>
 * MultiUserEvent
 * 
 * <P>
 * Initial Date:  2 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LDAPEvent extends MultiUserEvent {

	private static final long serialVersionUID = -3733295775896629486L;
	
	public static final String SYNCHING = "synching";
	public static final String DO_SYNCHING = "do_synching";
	public static final String SYNCHING_ENDED = "synching_ended";
	
	private boolean success;
	private Date timestamp;
	private LDAPError errors;
	
	public LDAPEvent(String command) {
		super(command);
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public LDAPError getErrors() {
		return errors;
	}

	public void setErrors(LDAPError errors) {
		this.errors = errors;
	}
}
