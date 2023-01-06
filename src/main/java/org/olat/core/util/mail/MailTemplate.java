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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.util.mail;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.i18n.I18nManager;
import org.olat.user.UserManager;

/**
 * Description:<br>
 * The MailTemplate holds a mail subject/body template and the according methods
 * to populate the velocity contexts with the user values
 * <P>
 * Usage:<br>
 * See MailTest.testMailTemplate() to learn how you can use this abstract class
 * and how you have to implement the putVariablesInMailContext() method.
 * <p>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public abstract class MailTemplate {

	private static final String USER_NAME = "userName";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String EMAIL = "email";
	private static final Collection<String> STANDART_IDENTITIY_VARIABLE_NAMES =
			List.of(USER_NAME, FIRST_NAME, LAST_NAME, EMAIL);

	private String templateName;
	private String subjectTemplate;
	private String bodyTemplate;
	private File[] attachments;
	private File attachmentsTmpDir;
	private VelocityContext context;
	private Boolean cpfrom;
	
	/**
	 * Constructor for a mail using a template
	 * 
	 * @param subjectTemplate Template for mail subject. Must not be NULL
	 * @param bodyTemplate Template for mail body. Must not be NULL
	 * @param attachments File array for mail attachments. Can be NULL
	 */
	public MailTemplate(String subjectTemplate, String bodyTemplate, File[] attachments) {
		this.subjectTemplate = subjectTemplate;
		this.bodyTemplate = bodyTemplate;
		this.attachments = attachments;
		this.context = new VelocityContext();
		this.cpfrom = true;
	}
	
	/**
	 * @return A name for UI
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * Only used for UI
	 * 
	 * @param templateName The name of the template
	 */
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public Boolean getCpfrom() {
		return cpfrom;
	}

	public void setCpfrom(Boolean cpfrom) {
		this.cpfrom = cpfrom;
	}

	/**
	 * @return mail subject template as string
	 */
	public String getSubjectTemplate() {
		return subjectTemplate;
	}

	/**
	 * @return mail body template as string
	 */
	public String getBodyTemplate() {
		return bodyTemplate;
	}

	/**
	 * @return attachments as File array
	 */
	public File[] getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments set file attachments
	 */
	public void setAttachments(File[] attachments) {
		this.attachments = attachments;
	}
	
	/**
	 * @return The directory where the attachments are saved temporarily.
	 */
	public File getAttachmentsTmpDir() {
		return attachmentsTmpDir;
	}

	/**
	 * 
	 * @param attachmentsTmpDir
	 */
	public void setAttachmentsTmpDir(File attachmentsTmpDir) {
		this.attachmentsTmpDir = attachmentsTmpDir;
	}

	/**
	 * @param bodyTemplate Set body template
	 */
	public void setBodyTemplate(String bodyTemplate) {
		this.bodyTemplate = bodyTemplate;
	}

	/**
	 * @param subjectTemplate Set subject template
	 */
	public void setSubjectTemplate(String subjectTemplate) {
		this.subjectTemplate = subjectTemplate;
	}
	
	/**
	 * A collection of variable name which can be used in
	 * {@link #putVariablesInMailContext(VelocityContext,Identity)}. The variable
	 * names are primarily used to show in the context help. The variable names do
	 * not have to contain the character "$".
	 *
	 * @return the variable names
	 */
	public Collection<String> getVariableNames() {
		return Collections.emptyList();
	}

	/**
	 * Method that puts all necessary variables for those templates into the give
	 * velocity context. This method must match all variables used in the subject
	 * and body template.
	 * 
	 * @param context The context where to put the variables
	 * @param recipient The current identity which will get the email
	 */
	public abstract void putVariablesInMailContext(VelocityContext vContext, Identity recipient);
	
	public void addToContext(String name, String value) {
		context.put(name, value);
	}
	
	public VelocityContext getContext() {
		return context;
	}
	
	protected static final Collection<String> getStandardIdentityVariableNames() {
		return STANDART_IDENTITIY_VARIABLE_NAMES;
	}
	
	protected static void fillContextWithStandardIdentityValues(VelocityContext vContext, Identity identity, Locale locale) {
		if(identity == null) return;
		
		User user = identity.getUser();
		if(locale == null) {
			locale = I18nManager.getInstance().getLocaleOrDefault(user.getPreferences().getLanguage());
		}
		
		vContext.put("login", user.getProperty(UserConstants.NICKNAME, locale));
		vContext.put("username", user.getProperty(UserConstants.NICKNAME, locale));
		vContext.put(USER_NAME, user.getProperty(UserConstants.NICKNAME, locale));
		vContext.put("first", user.getProperty(UserConstants.FIRSTNAME, locale));
		vContext.put("firstname", user.getProperty(UserConstants.FIRSTNAME, locale));
		vContext.put(FIRST_NAME, user.getProperty(UserConstants.FIRSTNAME, locale));
		vContext.put("last", user.getProperty(UserConstants.LASTNAME, locale));
		vContext.put("lastname", user.getProperty(UserConstants.LASTNAME, locale));
		vContext.put(LAST_NAME, user.getProperty(UserConstants.LASTNAME, locale));
		vContext.put(EMAIL, UserManager.getInstance().getUserDisplayEmail(identity, locale));
	}
	
	protected static void putVariablesInMailContext(VelocityContext vContext, String key, String value) {
		vContext.put(key, value);
		vContext.put(key.toLowerCase(), value);
	}
}
