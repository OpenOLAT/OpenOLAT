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
	
	private static final String COURSEURL = "courseurl";
	private static final String COURSENAME = "coursename";
	private static final String COURSEDESCRIPTION = "coursedescription";
	private static final String USERNAME = "username";
	private static final String FIRSTNAME = "firstname";
	private static final String LASTNAME = "lastname";
	private static final String FULLNAME = "fullname";
	private static final String EMAIL = "email";

	private final Locale locale;
	private final Identity sender;
	private final RepositoryEntry entry;
	
	public CourseMailTemplate(RepositoryEntry entry, Identity sender, Locale locale) {
		super(null, null, null);
		this.entry = entry;
		this.sender = sender;
		this.locale = locale;
	}
	
	@Override
	public Collection<String> getVariableNames() {
		List<String> variableNames = new ArrayList<>();
		
		if (entry != null) {
			variableNames.add(COURSEURL);
			variableNames.add(COURSENAME);
			variableNames.add(COURSEDESCRIPTION);
		}
		if (sender != null) {
			variableNames.add(USERNAME);
			variableNames.add(FIRSTNAME);
			variableNames.add(LASTNAME);
			variableNames.add(FULLNAME);
			variableNames.add(EMAIL);
		}
		
		return variableNames;
	}

	@Override
	public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
		if(entry != null) {
			String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
			vContext.put(COURSEURL, url);
			vContext.put(COURSENAME, entry.getDisplayname());
			vContext.put(COURSEDESCRIPTION, entry.getDescription());
		}
		if(sender != null) {
			User user = sender.getUser();
			UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
			BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
			
			vContext.put(FIRSTNAME, user.getProperty(UserConstants.FIRSTNAME, null));
			vContext.put(UserConstants.FIRSTNAME, user.getProperty(UserConstants.FIRSTNAME, null));
			vContext.put(LASTNAME, user.getProperty(UserConstants.LASTNAME, null));
			vContext.put(UserConstants.LASTNAME, user.getProperty(UserConstants.LASTNAME, null));
			String fullName = userManager.getUserDisplayName(sender);
			vContext.put(FULLNAME, fullName);
			vContext.put("fullName", fullName); 
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
