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
package org.olat.admin.user.bulkChange;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.olat.admin.user.SystemRolesAndRightsController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.login.auth.OLATAuthManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.GenderPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * this is a helper class which can be used in bulkChange-Steps and also the UsermanagerUserSearchController
 * 
 * <P>
 * Initial Date: 07.03.2008 <br>
 * 
 * @author rhaag
 */
@Service
public class UserBulkChangeManager implements InitializingBean {
	
	private static VelocityEngine velocityEngine;
	private static final OLog log = Tracing.createLoggerFor(UserBulkChangeManager.class);

	protected static final String CRED_IDENTIFYER = "password";
	protected static final String LANG_IDENTIFYER = "language";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;

	@Override
	public void afterPropertiesSet() throws Exception {
		// init velocity engine
		Properties p = new Properties();
		try {
			velocityEngine = new VelocityEngine();
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("config error " + p);
		}
	}


	public void changeSelectedIdentities(List<Identity> selIdentities, UserBulkChanges userBulkChanges,
			List<String> notUpdatedIdentities, boolean isAdministrativeUser,
			Translator trans, Identity actingIdentity) {

		Translator transWithFallback = userManager.getPropertyHandlerTranslator(trans);
		String usageIdentifyer = UserBulkChangeStep00.class.getCanonicalName();

		notUpdatedIdentities.clear();
		List<Identity> changedIdentities = new ArrayList<>();
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		OrganisationRoles[] organisationRoles = {
				OrganisationRoles.usermanager, OrganisationRoles.groupmanager,
				OrganisationRoles.author, OrganisationRoles.administrator,
				OrganisationRoles.poolmanager, OrganisationRoles.learnresourcemanager
			};
		
		Map<String,String> attributeChangeMap = userBulkChanges.getAttributeChangeMap();
		// loop over users to be edited:
		for (Identity identity : selIdentities) {
			//reload identity from cache, to prevent stale object
			identity = securityManager.loadIdentityByKey(identity.getKey());
			User user = identity.getUser();
			String oldEmail = user.getEmail();
			String errorDesc = "";
			boolean updateError = false;
			// change pwd
			if (attributeChangeMap.containsKey(CRED_IDENTIFYER)) {
				String newPwd = attributeChangeMap.get(CRED_IDENTIFYER);
				if (StringHelper.containsNonWhitespace(newPwd)) {
					if (!userManager.syntaxCheckOlatPassword(newPwd)) {
						errorDesc = transWithFallback.translate("error.password");
						updateError = true;
					}
				} else {
					newPwd = null;
				}
				olatAuthManager.changePasswordAsAdmin(identity, newPwd);
			}

			// set language
			String userLanguage = user.getPreferences().getLanguage();
			if (attributeChangeMap.containsKey(LANG_IDENTIFYER)) {
				String inputLanguage = attributeChangeMap.get(LANG_IDENTIFYER);
				if (!userLanguage.equals(inputLanguage)) {
					Preferences preferences = user.getPreferences();
					preferences.setLanguage(inputLanguage);
					user.setPreferences(preferences);
				}
			}

			Context vcContext = new VelocityContext();
			// set all properties as context
			setUserContext(identity, vcContext);
			// loop for each property configured in
			// src/serviceconfig/org/olat/_spring/olat_userconfig.xml -> Key:
			// org.olat.admin.user.bulkChange.UserBulkChangeStep00
			for (int k = 0; k < userPropertyHandlers.size(); k++) {
				UserPropertyHandler propHandler = userPropertyHandlers.get(k);
				String propertyName = propHandler.getName();
				String userValue = identity.getUser().getProperty(propertyName, null);
				String inputFieldValue = "";
				if (attributeChangeMap.containsKey(propertyName)) {
					inputFieldValue = attributeChangeMap.get(propertyName);
					inputFieldValue = inputFieldValue.replace("$", "$!");
					String evaluatedInputFieldValue = evaluateValueWithUserContext(inputFieldValue, vcContext);	
					
					// validate evaluated property-value
					ValidationError validationError = new ValidationError();
					// do validation checks with users current locale!
					Locale locale = transWithFallback.getLocale();
					if (!propHandler.isValidValue(identity.getUser(), evaluatedInputFieldValue, validationError, locale)) {
						errorDesc = transWithFallback.translate(validationError.getErrorKey(), validationError.getArgs()) + " (" + evaluatedInputFieldValue + ")";
						updateError = true;
						break;
					}

					if (!evaluatedInputFieldValue.equals(userValue)) {
						String stringValue = propHandler.getStringValue(evaluatedInputFieldValue, locale);
							propHandler.setUserProperty(user, stringValue);
					}
				}

			} // for (propertyHandlers)

			// set roles for identity
			// loop over securityGroups defined above
			Map<OrganisationRoles,String> roleChangeMap = userBulkChanges.getRoleChangeMap();
			for (OrganisationRoles organisationRole : organisationRoles) {
				boolean isInGroup = organisationService.hasRole(identity, organisationRole);
				String thisRoleAction = "";
				if (roleChangeMap.containsKey(organisationRole)) {
					thisRoleAction = roleChangeMap.get(organisationRole);
					// user not anymore in security group, remove him
					if (isInGroup && thisRoleAction.equals("remove")) {
						organisationService.removeMember(identity, organisationRole);
						log.audit("User::" + actingIdentity.getKey() + " removed system role::" + organisationRole + " from user::" + identity.getKey(), null);
					}
					// user not yet in security group, add him
					if (!isInGroup && thisRoleAction.equals("add")) {
						organisationService.addMember(identity, organisationRole);
						log.audit("User::" + actingIdentity.getKey() + " added system role::" + organisationRole + " to user::" + identity.getKey(), null);
					}
				}
			}
			

			// set status
			if (userBulkChanges.getStatus() != null) {
				Integer status = userBulkChanges.getStatus();	
				String newStatusText = getStatusText(status);
				Integer oldStatus = identity.getStatus();
				String oldStatusText = getStatusText(oldStatus);
				if(!oldStatus.equals(status) && Identity.STATUS_LOGIN_DENIED.equals(status) && userBulkChanges.isSendLoginDeniedEmail()) {
					sendLoginDeniedEmail(identity);
				}
				identity = securityManager.saveIdentityStatus(identity, status, actingIdentity);
				log.audit("User::" + actingIdentity.getKey() + " changed account status for user::" + identity.getKey() + " from::" + oldStatusText + " to::" + newStatusText, null);
			}

			// persist changes:
			if (updateError) {
				String errorOutput = identity.getKey() + ": " + errorDesc;
				log.debug("error during bulkChange of users, following user could not be updated: " + errorOutput);
				notUpdatedIdentities.add(errorOutput); 
			} else {
				userManager.updateUserFromIdentity(identity);
				securityManager.deleteInvalidAuthenticationsByEmail(oldEmail);
				changedIdentities.add(identity);
				log.audit("User::" + actingIdentity.getKey() + " successfully changed account data for user::" + identity.getKey() + " in bulk change", null);
			}

			// commit changes for this user
			dbInstance.commit();
		} // for identities

		// Add identity to new groups:
		List<Long> ownGroups = userBulkChanges.getOwnerGroups();
		List<Long> partGroups = userBulkChanges.getParticipantGroups();
		if (!ownGroups.isEmpty() || !partGroups.isEmpty()) {
			List<BusinessGroupMembershipChange> changes = new ArrayList<>();
			for(Identity selIdentity:selIdentities) {
				if(ownGroups != null && !ownGroups.isEmpty()) {
					for(Long tutorGroupKey:ownGroups) {
						BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(selIdentity, tutorGroupKey);
						change.setTutor(Boolean.TRUE);
						changes.add(change);
					}
				}
				if(partGroups != null && !partGroups.isEmpty()) {
					for(Long partGroupKey:partGroups) {
						BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(selIdentity, partGroupKey);
						change.setParticipant(Boolean.TRUE);
						changes.add(change);
					}
				}
			}

			MailPackage mailing = new MailPackage();
			businessGroupService.updateMemberships(actingIdentity, changes, mailing);
			dbInstance.commit();
		}
	}
	
	public String getStatusText(Integer status) {
		String text;
		if(Identity.STATUS_PERMANENT.equals(status)) {
			text = "permanent";
		} else if(Identity.STATUS_ACTIV.equals(status)) {
			text = "active";
		} else if(Identity.STATUS_LOGIN_DENIED.equals(status)) {
			text = "login_denied";
		} else if(Identity.STATUS_DELETED.equals(status)) {
			text = "deleted";
		} else if(Identity.STATUS_PENDING.equals(status)) {
			text = "pending";
		} else {
			text = "unknown";
		}
		return text;
	}
	
	public void sendLoginDeniedEmail(Identity identity) {
		String lang = identity.getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(lang);
		Translator translator = Util.createPackageTranslator(SystemRolesAndRightsController.class, locale);

		String gender = "";
		UserPropertyHandler handler = userManager.getUserPropertiesConfig().getPropertyHandler(UserConstants.GENDER);
		if(handler instanceof GenderPropertyHandler) {
			String internalGender = ((GenderPropertyHandler)handler).getInternalValue(identity.getUser());
			if(StringHelper.containsNonWhitespace(internalGender)) {
				Translator userPropTrans = userManager.getUserPropertiesConfig().getTranslator(translator);
				gender = userPropTrans.translate("form.name.gender.salutation." + internalGender);
			}
		}
		
		String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
			email = StringHelper.containsNonWhitespace(email)? email: "-";

		String[] args = new String[] {
				identity.getName(),//0: changed users username
				email,// 1: changed users email address
				userManager.getUserDisplayName(identity.getUser()),// 2: Name (first and last name) of user who changed the password
				WebappHelper.getMailConfig("mailSupport"), //3: configured support email address
				identity.getUser().getProperty(UserConstants.LASTNAME, null), //4 last name
				getServerURI(), //5 url system
				gender //6 Mr. Mrs.
		};
		
		MailBundle bundle = new MailBundle();
		bundle.setToId(identity);
		bundle.setContent(translator.translate("mailtemplate.login.denied.subject", args),
			translator.translate("mailtemplate.login.denied.body", args));
		mailManager.sendExternMessage(bundle, null, false);
	}
	
	private String getServerURI() {
		String uri = Settings.getSecureServerContextPathURI();
		if(StringHelper.containsNonWhitespace(uri)) {
			return uri;
		}
		return Settings.getInsecureServerContextPathURI();
	}

	public String evaluateValueWithUserContext(String valToEval, Context vcContext) {
		StringWriter evaluatedUserValue = new StringWriter();
		// evaluate inputFieldValue to get a concatenated string
		try {
			velocityEngine.evaluate(vcContext, evaluatedUserValue, "vcUservalue", valToEval);
		} catch (Exception e) {
			log.error("evaluating of values in BulkChange Field not possible!");
			return "ERROR";
		}
		return evaluatedUserValue.toString();
	}

	/**
	 * 
	 * @param identity
	 * @param vcContext
	 * @param isAdministrativeUser
	 */
	public void setUserContext(Identity identity, Context vcContext) {
		List<UserPropertyHandler> userPropertyHandlers2 = userManager.getAllUserPropertyHandlers();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers2) {
			String propertyName = userPropertyHandler.getName();
			String userValue = identity.getUser().getProperty(propertyName, null);
			vcContext.put(propertyName, userValue);
		}
	}

	public Context getDemoContext(Locale locale) {
		Translator propertyTrans = Util.createPackageTranslator(UserPropertyHandler.class, locale);
		return getDemoContext(propertyTrans);
	}
	
	public Context getDemoContext(Translator propertyTrans) {
		Context vcContext = new VelocityContext();
		List<UserPropertyHandler> userPropertyHandlers2 = userManager.getAllUserPropertyHandlers();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers2) {
			String propertyName = userPropertyHandler.getName();
			String userValue = propertyTrans.translate("import.example." + userPropertyHandler.getName());
			vcContext.put(propertyName, userValue);
		}
		return vcContext;
	}
}