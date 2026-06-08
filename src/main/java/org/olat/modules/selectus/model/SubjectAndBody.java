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
package org.olat.modules.selectus.model;

import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.mail.MailAttachment;

/**
 * 
 * Initial date: 12.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SubjectAndBody {
	
	private final String subject;
	private final String body;
	private final MailAttachment attachment;
	
	public SubjectAndBody(String subject, String body) {
		this.subject = subject;
		this.body = body;
		this.attachment = null;
	}
	
	public SubjectAndBody(String subject, String body, MailAttachment attachment) {
		this.subject = subject;
		this.body = body;
		this.attachment = attachment;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}
	
	public MailAttachment getLetter() {
		return attachment;
	}
	
	public boolean isHtml() {
		return StringHelper.isHtml(body);
	}
}
