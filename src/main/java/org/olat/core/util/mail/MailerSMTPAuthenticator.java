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
package org.olat.core.util.mail;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/**
 * <h3>Description:</h3>
 * SMTP authenticator based on username / pwd used to send mails to a SMTP
 * server that uses username/password authentication
 * <p>
 * Initial Date: 02.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class MailerSMTPAuthenticator extends Authenticator {
	private final String smtpUser;

	private final String smtpPwd;

	/**
	 * Constructor
	 * 
	 * @param smtpUser
	 * @param smtpPwd
	 */
	public MailerSMTPAuthenticator(String smtpUser, String smtpPwd) {
		this.smtpUser = smtpUser;
		this.smtpPwd = smtpPwd;
	}

	/**
	 * @see javax.mail.Authenticator#getPasswordAuthentication()
	 */
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.smtpUser, this.smtpPwd);
	}
}
