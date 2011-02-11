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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.util.mail;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;

/**
 * Description:<br>
 * Result object when sending mail using the MailerWithTemplate
 * <P>
 * Initial Date: 22.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class MailerResult {
	public static final int OK = 0; // default return code
	public static final int MAILHOST_UNDEFINED = 1;
	public static final int SEND_GENERAL_ERROR = 2;
	public static final int SENDER_ADDRESS_ERROR = 3;
	public static final int RECIPIENT_ADDRESS_ERROR = 4;
	public static final int TEMPLATE_PARSE_ERROR = 5;
	public static final int TEMPLATE_GENERAL_ERROR = 6;
	public static final int ATTACHMENT_INVALID = 7;

	private List<Identity> failedIdentites = new ArrayList<Identity>();
	private int returnCode = OK;

	/**
	 * @return list of identities to which the mail could not be send, e.g.
	 *         because of an invalid mail address
	 */
	public List<Identity> getFailedIdentites() {
		return failedIdentites;
	}

	/**
	 * @return return code. If nothing else set return will be OK
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/**
	 * Package helper to add an identity to which for whatever reason the mail
	 * could not be sent
	 * 
	 * @param failedIdentity
	 */
	void addFailedIdentites(Identity failedIdentity) {
		this.failedIdentites.add(failedIdentity);
	}

	/**
	 * Package helper to set the return code.
	 * 
	 * @param returnCode
	 */
	void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

}
