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
package org.olat.ims.qti21.model;

import org.olat.core.util.mail.MailBundle;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 17 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DigitalSignatureOptions {
	
	private final boolean digitalSignature;
	private final boolean sendMail;
	private MailBundle mailBundle;
	
	private String subIdentName;
	private final RepositoryEntry entry;
	private final RepositoryEntry testEntry;
	
	public DigitalSignatureOptions(boolean digitalSignature, boolean sendMail, RepositoryEntry entry, RepositoryEntry testEntry) {
		this.digitalSignature = digitalSignature;
		this.sendMail = sendMail;
		this.entry = entry;
		this.testEntry = testEntry;
	}

	public boolean isDigitalSignature() {
		return digitalSignature;
	}

	public boolean isSendMail() {
		return sendMail;
	}

	public String getSubIdentName() {
		return subIdentName;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public RepositoryEntry getTestEntry() {
		return testEntry;
	}

	public void setSubIdentName(String subIdentName) {
		this.subIdentName = subIdentName;
	}

	public MailBundle getMailBundle() {
		return mailBundle;
	}
	
	public void setMailBundle(MailBundle mailBundle) {
		this.mailBundle = mailBundle;
	}
}
