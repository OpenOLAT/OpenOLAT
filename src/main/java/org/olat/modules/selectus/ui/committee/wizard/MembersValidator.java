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
package org.olat.modules.selectus.ui.committee.wizard;

import java.util.List;
import java.util.Locale;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.UsernameValidationRulesFactory;
import org.olat.login.validation.ValidationResult;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;

/**
 * 
 * Initial date: 8 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersValidator {

	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final SyntaxValidator usernameSyntaxValidator;

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UsernameValidationRulesFactory usernameRulesFactory;
	
	public MembersValidator(Locale locale) {
		CoreSpringFactory.autowireObject(this);
		this.locale = locale;
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(PositionCommitteeController.formIdentifyer, true);
		usernameSyntaxValidator = new SyntaxValidator(usernameRulesFactory.createRules(false), false);
	}
	
	public MembersValidator(List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		CoreSpringFactory.autowireObject(this);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
		usernameSyntaxValidator = new SyntaxValidator(usernameRulesFactory.createRules(false), false);
	}
	
	public CommitteeMemberStatus valid(CommitteeMember member, List<CommitteeMember> otherMembers) {
		if(member.getStatus() == CommitteeMemberStatus.skipped) {
			return CommitteeMemberStatus.skipped;
		}

		// validate if username does match the syntactical login requirements
		if(member.getIdentity() instanceof TransientIdentity) {
			String loginName = member.getIdentity().getName();
			if(!StringHelper.containsNonWhitespace(loginName)) {
				return CommitteeMemberStatus.notValid;
			}
			
			ValidationResult validationResult = usernameSyntaxValidator.validate(loginName, member.getIdentity());
			if (!StringHelper.containsNonWhitespace(loginName) || !validationResult.isValid()) {			
				return CommitteeMemberStatus.notValid;
			}
			for(CommitteeMember otherMember:otherMembers) {
				if(otherMember != member && otherMember.getIdentity().getName() != null
						&& otherMember.getIdentity().getName().equals(member.getIdentity().getName())) {
					return CommitteeMemberStatus.notValid;
				}
			}
			
			// Check if login is still available
			Identity identity = securityManager.findIdentityByName(loginName);
			if ((identity != null && member.getIdentity() == null) ||
					(identity != null && member.getIdentity() != null && !identity.equals(member.getIdentity()))) {			
				return CommitteeMemberStatus.notValid;
			}
			
			// validate special rules for each user property
			if(!validateUserProperties(member)) {
				return CommitteeMemberStatus.notValid;
			}
	
			// check email
			String email = member.getIdentity().getUser().getProperty(UserConstants.EMAIL, locale);
			if(!StringHelper.containsNonWhitespace(email)) {
				return CommitteeMemberStatus.notValid;
			}
			if(MailHelper.isValidEmailAddress(email)) {
				// Check if email is not already taken
				boolean allowed = userManager.isEmailAllowed(email, member.getIdentity().getUser());
				if (!allowed) {
					return CommitteeMemberStatus.notValid;
				}
			} else {
				return CommitteeMemberStatus.notValid;
			}
		} else {
			if(!validateUserProperties(member)) {
				return CommitteeMemberStatus.notValid;
			}
		}

		return CommitteeMemberStatus.ok;
	}
	
	private boolean validateUserProperties(CommitteeMember member) {
		for (UserPropertyHandler userPropertyHandler:userPropertyHandlers) {		
			ValidationError error = new ValidationError();
			String value = member.getIdentity().getUser().getProperty(userPropertyHandler.getName());
			if("_".equals(value) && dbInstance.isOracle()) {
				value = null;
			}
			userPropertyHandler.isValidValue(member.getIdentity().getUser(), value, error, locale);
			boolean mandatory = userManager.isMandatoryUserProperty(MembersController.formIdentifyer, userPropertyHandler);
			if((mandatory && (!StringHelper.containsNonWhitespace(value) || "-".equals(value))) || error.getErrorKey() != null) {
				return false;
			}
		}
		return true;
	}
}
