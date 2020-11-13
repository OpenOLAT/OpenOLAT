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
package org.olat.course.nodes.co;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 12 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseMailTemplate extends MailTemplate {
	
	private static final String COURSE_URL = "courseUrl";
	private static final String COURSE_NAME = "courseName";
	private static final String COURSE_DESCRIPTION = "courseDescription";
	private static final String USERNAME = "username";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String FULL_NAME = "fullName";
	private static final String EMAIL = "email";
	private static final Collection<String> VARIABLE_NAMES =
			List.of(COURSE_URL, COURSE_NAME, COURSE_DESCRIPTION, USERNAME, FIRST_NAME, LAST_NAME, FULL_NAME, EMAIL);

	private final Locale locale;
	private final Identity sender;
	private final RepositoryEntry entry;
	
	public CourseMailTemplate(RepositoryEntry entry, Identity sender, Locale locale) {
		super(null, null, null);
		this.entry = entry;
		this.sender = sender;
		this.locale = locale;
	}
	
	public static Collection<String> variableNames() {
		return VARIABLE_NAMES;
	}
	
	@Override
	public Collection<String> getVariableNames() {
		List<String> variableNames = new ArrayList<>();
		
		if (entry != null) {
			variableNames.add(COURSE_URL);
			variableNames.add(COURSE_NAME);
			variableNames.add(COURSE_DESCRIPTION);
		}
		if (sender != null) {
			variableNames.add(USERNAME);
			variableNames.add(FIRST_NAME);
			variableNames.add(LAST_NAME);
			variableNames.add(FULL_NAME);
			variableNames.add(EMAIL);
		}
		
		return variableNames;
	}

	@Override
	public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
		if(entry != null) {
			String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
			vContext.put(COURSE_URL, url);
			vContext.put("courseurl", url);
			vContext.put(COURSE_NAME, entry.getDisplayname());
			vContext.put("coursename", entry.getDisplayname());
			vContext.put(COURSE_DESCRIPTION, entry.getDescription());
			vContext.put("coursedescription", entry.getDescription());
		}
		if(sender != null) {
			User user = sender.getUser();
			UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
			BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
			
			vContext.put(FIRST_NAME, user.getProperty(UserConstants.FIRSTNAME, null));
			vContext.put("firstname", user.getProperty(UserConstants.FIRSTNAME, null));
			vContext.put(LAST_NAME, user.getProperty(UserConstants.LASTNAME, null));
			vContext.put("lastname", user.getProperty(UserConstants.LASTNAME, null));
			String fullName = userManager.getUserDisplayName(sender);
			vContext.put(FULL_NAME, fullName);
			vContext.put("fullname", fullName); 
			vContext.put("mail", userManager.getUserDisplayEmail(user, locale));
			vContext.put(EMAIL, userManager.getUserDisplayEmail(user, locale));
			String loginName = securityManager.findAuthenticationName(sender);
			if(!StringHelper.containsNonWhitespace(loginName)) {
				loginName = sender.getName();
			}
			vContext.put(USERNAME, loginName);
		}
	}
}
