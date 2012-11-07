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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.mail;

/**
 * A mail package is the sum of the template, the context and it's result.
 * All or part of thesse can be null.
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailPackage {
	
	private final boolean sendEmail;
	private final MailTemplate template;
	private final MailContext context;
	private final MailerResult result;
	
	public MailPackage() {
		this.sendEmail = true;
		this.template = null;
		this.context = null;
		this.result = new MailerResult();
	}
	
	public MailPackage(boolean sendMail) {
		this.sendEmail = sendMail;
		this.template = null;
		this.context = null;
		this.result = new MailerResult();
	}
	
	public MailPackage(MailTemplate template, MailContext context) {
		this.sendEmail = true;
		this.template = template;
		this.context = context;
		this.result = new MailerResult();
	}
	
	public MailPackage(MailTemplate template, String businessPath, boolean sendMail) {
		this.sendEmail = true;
		this.template = template;
		this.context = new MailContextImpl(null, null, businessPath);
		this.result = new MailerResult();
	}
	
	public MailPackage(MailerResult result, String businessPath, boolean sendMail) {
		this.sendEmail = true;
		this.template = null;
		this.context = new MailContextImpl(null, null, businessPath);
		this.result = result;
	}
	
	public MailPackage(MailTemplate template, MailerResult result, String businessPath, boolean sendMail) {
		this.sendEmail = true;
		this.template = template;
		this.context = new MailContextImpl(null, null, businessPath);
		this.result = result;
	}


	/**
	 * Default is true, you want to send mails. But in rare case, this flag
	 * give you the possibility to skip the mails.
	 * @return
	 */
	public boolean isSendEmail() {
		return sendEmail;
	}

	public MailTemplate getTemplate() {
		return template;
	}
	
	public MailContext getContext() {
		return context;
	}
	
	public MailerResult getResult() {
		return result;
	}

	
	public void appendResult(MailerResult result) {
		
	}
}
