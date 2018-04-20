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
*/

package org.olat.admin.user.delete.service;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.TemporalType;

import org.olat.admin.user.delete.SelectionController;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.manager.AuthenticationHistoryDAO;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.repository.RepositoryDeletionModule;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Manager for user-deletion.
 *
 * @author Christian Guretzki
 */
@Service("userDeletionManager")
public class UserDeletionManager {
	
	private static final OLog log = Tracing.createLoggerFor(UserDeletionManager.class);

	public static final String DELETED_USER_DELIMITER = "_bkp_";
	/** Default value for last-login duration in month. */
	private static final int DEFAULT_LAST_LOGIN_DURATION = 24;
	/** Default value for delete-email duration in days. */
	private static final int DEFAULT_DELETE_EMAIL_DURATION = 30;
	private static final String LAST_LOGIN_DURATION_PROPERTY_NAME = "LastLoginDuration";
	private static final String DELETE_EMAIL_DURATION_PROPERTY_NAME = "DeleteEmailDuration";
	private static final String PROPERTY_CATEGORY = "UserDeletion";

	private static UserDeletionManager INSTANCE;
	public static final String SEND_DELETE_EMAIL_ACTION = "sendDeleteEmail";
	private static final String USER_ARCHIVE_DIR = "archive_deleted_users";
	private static final String USER_DELETED_ACTION = "userdeleted";
	private static boolean keepUserLoginAfterDeletion;
	private static boolean keepUserEmailAfterDeletion;


	// Flag used in user-delete to indicate that all deletable managers are initialized
	@Autowired
	private RepositoryDeletionModule deletionModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private AuthenticationHistoryDAO suthenticationHistoryDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private DB dbInstance;

	/**
	 * [used by spring]
	 */
	private UserDeletionManager() {
		INSTANCE = this;
	}

	/**
	 * @return Singleton.
	 */
	public static UserDeletionManager getInstance() { return INSTANCE; }

	/**
	 * Send 'delete'- emails to a list of identities. The delete email is an announcement for the user-deletion.
	 *
	 * @param selectedIdentities
	 * @return String with warning message (e.g. email-address not valid, could not send email).
	 *         If there is no warning, the return String is empty ("").
	 */
	public String sendUserDeleteEmailTo(List<Identity> selectedIdentities, MailTemplate template,
			boolean isTemplateChanged, String keyEmailSubject, String keyEmailBody, Identity sender, Translator pT ) {
		StringBuilder buf = new StringBuilder();
		if (template != null) {
			template.addToContext("responseTo", deletionModule.getEmailResponseTo());
			for (Iterator<Identity> iter = selectedIdentities.iterator(); iter.hasNext();) {
				Identity identity = iter.next();
				if (!isTemplateChanged) {
					// Email template has NOT changed => take translated version of subject and body text
					Translator identityTranslator = Util.createPackageTranslator(SelectionController.class, I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage()));
					template.setSubjectTemplate(identityTranslator.translate(keyEmailSubject));
					template.setBodyTemplate(identityTranslator.translate(keyEmailBody));
				}
				template.putVariablesInMailContext(template.getContext(), identity);
				log.debug(" Try to send Delete-email to identity=" + identity.getName() + " with email=" + identity.getUser().getProperty(UserConstants.EMAIL, null));

				MailerResult result = new MailerResult();
				MailBundle bundle = mailManager.makeMailBundle(null, identity, template, null, null, result);
				if(bundle != null) {
					mailManager.sendMessage(bundle);
				}
				if(template.getCpfrom()) {
					MailBundle ccBundle = mailManager.makeMailBundle(null, sender, template, sender, null, result);
					if(ccBundle != null) {
						mailManager.sendMessage(ccBundle);
					}
				}

				if (result.getReturnCode() != MailerResult.OK) {
					buf.append(pT.translate("email.error.send.failed", new String[] {identity.getUser().getProperty(UserConstants.EMAIL, null), identity.getName()} )).append("\n");
				}
				log.audit("User-Deletion: Delete-email send to identity=" + identity.getName() + " with email=" + identity.getUser().getProperty(UserConstants.EMAIL, null));
				markSendEmailEvent(identity);
			}
		} else {
			// no template => User decides to sending no delete-email, mark only in lifecycle table 'sendEmail'
			for (Iterator<Identity> iter = selectedIdentities.iterator(); iter.hasNext();) {
				Identity identity = iter.next();
				log.audit("User-Deletion: Move in 'Email sent' section without sending email, identity=" + identity.getName());
				markSendEmailEvent(identity);
			}
		}
		return buf.toString();
	}

	private void markSendEmailEvent(Identity identity) {
		LifeCycleManager.createInstanceFor(identity).markTimestampFor(SEND_DELETE_EMAIL_ACTION);
	}

	/**
	 * Return list of identities which have last-login older than 'lastLoginDuration' parameter.
	 * This user are ready to start with user-deletion process.
	 * @param lastLoginDuration  last-login duration in month
	 * @return List of Identity objects
	 */
	public List<Identity> getDeletableIdentities(int lastLoginDuration) {
		Calendar lastLoginLimit = Calendar.getInstance();
		lastLoginLimit.add(Calendar.MONTH, - lastLoginDuration);
		log.debug("lastLoginLimit=" + lastLoginLimit);
		// 1. get all 'active' identities with lastlogin > x
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user as user")
		  .append(" where ident.status=").append(Identity.STATUS_ACTIV).append(" and (ident.lastLogin = null or ident.lastLogin < :lastLogin)");
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("lastLogin", lastLoginLimit.getTime(), TemporalType.TIMESTAMP)
				.getResultList();

		// 2. get all 'active' identities in deletion process
		String queryStr = "select ident from org.olat.core.id.Identity as ident"
			+ " , org.olat.commons.lifecycle.LifeCycleEntry as le"
			+ " where ident.key = le.persistentRef "
			+ " and le.persistentTypeName ='" + IdentityImpl.class.getName() + "'"
			+ " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' ";
		List<Identity> identitiesInProcess = dbInstance.getCurrentEntityManager()
				.createQuery(queryStr, Identity.class)
				.getResultList();
		// 3. Remove all identities in deletion-process from all inactive-identities
		identities.removeAll(identitiesInProcess);
		return identities;
	}

	/**
	 * Return list of identities which are in user-deletion-process.
	 * user-deletion-process means delete-announcement.email send, duration of waiting for response is not expired.
	 * @param deleteEmailDuration  Duration of user-deletion-process in days
	 * @return List of Identity objects
	 */
	public List<Identity> getIdentitiesInDeletionProcess(int deleteEmailDuration) {
		Calendar deleteEmailLimit = Calendar.getInstance();
		deleteEmailLimit.add(Calendar.DAY_OF_MONTH, - (deleteEmailDuration-1));
		log.debug("deleteEmailLimit=" + deleteEmailLimit);
		String queryStr = "select ident from org.olat.core.id.Identity as ident"
			+ " , org.olat.commons.lifecycle.LifeCycleEntry as le"
			+ " where ident.key = le.persistentRef "
			+ " and ident.status = "	+ Identity.STATUS_ACTIV
			+ " and le.persistentTypeName ='" + IdentityImpl.class.getName() + "'"
			+ " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp >= :deleteEmailDate ";
		return dbInstance.getCurrentEntityManager()
				.createQuery(queryStr, Identity.class)
				.setParameter("deleteEmailDate", deleteEmailLimit.getTime(), TemporalType.TIMESTAMP)
				.getResultList();
	}

	/**
	 * Return list of identities which are ready-to-delete in user-deletion-process.
	 * (delete-announcement.email send, duration of waiting for response is expired).
	 * @param deleteEmailDuration  Duration of user-deletion-process in days
	 * @return List of Identity objects
	 */
	public List<Identity> getIdentitiesReadyToDelete(int deleteEmailDuration) {
		Calendar deleteEmailLimit = Calendar.getInstance();
		deleteEmailLimit.add(Calendar.DAY_OF_MONTH, - (deleteEmailDuration - 1));
		log.debug("deleteEmailLimit=" + deleteEmailLimit);
		String queryStr = "select ident from org.olat.core.id.Identity as ident"
			+ " , org.olat.commons.lifecycle.LifeCycleEntry as le"
			+ " where ident.key = le.persistentRef "
			+ " and ident.status = "	+ Identity.STATUS_ACTIV
			+ " and le.persistentTypeName ='" + IdentityImpl.class.getName() + "'"
			+ " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp < :deleteEmailDate ";
		return dbInstance.getCurrentEntityManager()
				.createQuery(queryStr, Identity.class)
				.setParameter("deleteEmailDate", deleteEmailLimit.getTime(), TemporalType.TIMESTAMP)
				.getResultList();
	}

	/**
	 * Delete all user-data in registered deleteable resources.
	 * @param identity
	 * @return true
	 */
	public void deleteIdentity(Identity identity) {
		log.info("Start deleteIdentity for identity=" + identity);

		String newName = getBackupStringWithDate(identity.getName());

		log.info("Start Deleting user=" + identity);
		File archiveFilePath = getArchivFilePath(identity);
		Map<String,UserDataDeletable> userDataDeletableResourcesMap = CoreSpringFactory.getBeansOfType(UserDataDeletable.class);
		List<UserDataDeletable> userDataDeletableResources = new ArrayList<>(userDataDeletableResourcesMap.values());
		Collections.sort(userDataDeletableResources, new UserDataDeletableComparator());
		for (UserDataDeletable element : userDataDeletableResources) {
			log.info("UserDataDeletable-Loop element=" + element);
			element.deleteUserData(identity, newName, archiveFilePath);
		}

		// Delete all authentications for certain identity
		List<Authentication> authentications = securityManager.getAuthentications(identity);
		for (Authentication auth:authentications) {
			log.info("deleteAuthentication auth=" + auth);
			securityManager.deleteAuthentication(auth);
			log.debug("Delete auth=" + auth + "  of identity="  + identity);
		}
		// delete the authentication history
		suthenticationHistoryDao.deleteAuthenticationHistory(identity);

		//remove identity from its security groups
		List<SecurityGroup> securityGroups = securityGroupDao.getSecurityGroupsForIdentity(identity);
		for (SecurityGroup secGroup : securityGroups) {
			securityGroupDao.removeIdentityFromSecurityGroup(identity, secGroup);
			log.info("Removing user=" + identity + " from security group="  + secGroup.toString());
		}
		//remove identity from groups
		groupDao.removeMemberships(identity);

		String key = identity.getUser().getProperty("emchangeKey", null);
		TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
		if (tempKey != null) {
			registrationManager.deleteTemporaryKey(tempKey);
		}


		identity = securityManager.loadIdentityByKey(identity.getKey());
		//keep login-name only -> change email

		User persistedUser = identity.getUser();
		List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getAllUserPropertyHandlers();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			String actualProperty = userPropertyHandler.getName();
			if (userPropertyHandler.isDeletable()
					&& !(keepUserEmailAfterDeletion && UserConstants.EMAIL.equals(actualProperty))) {
				persistedUser.setProperty(actualProperty, null);
			}

			if((!keepUserEmailAfterDeletion && UserConstants.EMAIL.equals(actualProperty))) {
				String oldEmail = userPropertyHandler.getUserProperty(persistedUser, null);
				String newEmail = "";
				if (StringHelper.containsNonWhitespace(oldEmail)){
					newEmail = getBackupStringWithDate(oldEmail);
				}
				log.info("Update user-property user=" + persistedUser);
				userPropertyHandler.setUserProperty(persistedUser, newEmail);
			}
		}
		UserManager.getInstance().updateUserFromIdentity(identity);

		log.info("deleteUserProperties user=" + persistedUser);
		dbInstance.commit();
		identity = securityManager.loadIdentityByKey(identity.getKey());
		//keep email only -> change login-name
		if (!keepUserEmailAfterDeletion) {
			identity = securityManager.saveIdentityName(identity, newName, null);
		}

		//keep everything, change identity.status to deleted
		log.info("Change stater identity=" + identity);
		identity = securityManager.saveIdentityStatus(identity, Identity.STATUS_DELETED);

		LifeCycleManager.createInstanceFor(identity).deleteTimestampFor(SEND_DELETE_EMAIL_ACTION);
		LifeCycleManager.createInstanceFor(identity).markTimestampFor(USER_DELETED_ACTION, createLifeCycleLogDataFor(identity));

		log.audit("User-Deletion: Delete all userdata for identity=" + identity);
	}

	public String getBackupStringWithDate(String original){
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		String dateStamp = dateFormat.format(new Date());
		return dateStamp + DELETED_USER_DELIMITER + original;
	}

	private String createLifeCycleLogDataFor(Identity identity) {
		StringBuilder buf = new StringBuilder();
		buf.append("<identity>");
		buf.append("<username>").append(identity.getName()).append("</username>");
		buf.append("<lastname>").append(identity.getName()).append("</lastname>");
		buf.append("<firstname>").append(identity.getName()).append("</firstname>");
		buf.append("<email>").append(identity.getName()).append("</email>");
		buf.append("</identity>");
		return buf.toString();
	}

	/**
	 * Re-activate an identity, lastLogin = now, reset deleteemaildate = null.
	 * @param identity
	 */
	public Identity setIdentityAsActiv(final Identity identity) {
		securityManager.setIdentityLastLogin(identity);
		LifeCycleManager lifeCycleManagerForIdenitiy = LifeCycleManager.createInstanceFor(identity);
		if (lifeCycleManagerForIdenitiy.hasLifeCycleEntry(SEND_DELETE_EMAIL_ACTION)) {
			log.audit("User-Deletion: Remove from delete-list identity=" + identity);
			lifeCycleManagerForIdenitiy.deleteTimestampFor(SEND_DELETE_EMAIL_ACTION);
		}
		return identity;
	}

	/**
	 * @return  Return duration in days for waiting for reaction on delete-email.
	 */
	public int getDeleteEmailDuration() {
		return getPropertyByName(DELETE_EMAIL_DURATION_PROPERTY_NAME, DEFAULT_DELETE_EMAIL_DURATION);
	}

	/**
	 * @return  Return last-login duration in month for user on delete-selection list.
	 */
	public int getLastLoginDuration() {
		return getPropertyByName(LAST_LOGIN_DURATION_PROPERTY_NAME, DEFAULT_LAST_LOGIN_DURATION);
	}

	private int getPropertyByName(String name, int defaultValue) {
		List<Property> properties = PropertyManager.getInstance().findProperties(null, null, null, PROPERTY_CATEGORY, name);
		if (properties.size() == 0) {
			return defaultValue;
		} else {
			return properties.get(0).getLongValue().intValue();
		}
	}

	public void setLastLoginDuration(int lastLoginDuration) {
		setProperty(LAST_LOGIN_DURATION_PROPERTY_NAME, lastLoginDuration);
	}

	public void setDeleteEmailDuration(int deleteEmailDuration) {
		setProperty(DELETE_EMAIL_DURATION_PROPERTY_NAME, deleteEmailDuration);
	}

	private void setProperty(String propertyName, int value) {
		List<Property> properties = PropertyManager.getInstance().findProperties(null, null, null, PROPERTY_CATEGORY, propertyName);
		Property property = null;
		if (properties.size() == 0) {
			property = PropertyManager.getInstance().createPropertyInstance(null, null, null, PROPERTY_CATEGORY, propertyName, null,  new Long(value), null, null);
		} else {
			property = properties.get(0);
			property.setLongValue( new Long(value) );
		}
		PropertyManager.getInstance().saveProperty(property);
	}

	private File getArchivFilePath(Identity identity) {
		String archiveFilePath = deletionModule.getArchiveRootPath() + File.separator + USER_ARCHIVE_DIR + File.separator + RepositoryDeletionModule.getArchiveDatePath()
		     + File.separator + "del_identity_" + identity.getName();
		File archiveIdentityRootDir = new File(archiveFilePath);
		if (!archiveIdentityRootDir.exists()) {
			archiveIdentityRootDir.mkdirs();
		}
		return archiveIdentityRootDir;
	}

	/**
	 * Setter method used by spring
	 * @param keepUserLoginAfterDeletion The keepUserLoginAfterDeletion to set.
	 */
	public void setKeepUserLoginAfterDeletion(boolean keepUserLoginAfterDeletion) {
		UserDeletionManager.keepUserLoginAfterDeletion = keepUserLoginAfterDeletion;
	}

	/**
	 * Setter method used by spring
	 * @param keepUserEmailAfterDeletion The keepUserEmailAfterDeletion to set.
	 */
	public void setKeepUserEmailAfterDeletion(boolean keepUserEmailAfterDeletion) {
		UserDeletionManager.keepUserEmailAfterDeletion = keepUserEmailAfterDeletion;
	}

	public static boolean isKeepUserLoginAfterDeletion() {
		return keepUserLoginAfterDeletion;
	}

	public static class UserDataDeletableComparator implements Comparator<UserDataDeletable> {
		@Override
		public int compare(UserDataDeletable o1, UserDataDeletable o2) {
			int p1 = o1.deleteUserDataPriority();
			int p2 = o2.deleteUserDataPriority();
			return -Integer.compare(p1, p2);
		}
	}
}
