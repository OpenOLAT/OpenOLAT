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

package org.olat.group.delete.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupArchiver;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.delete.service.DeletionModule;


/**
 * Manager for group deletion. Handle deletion-email and db-access for group-deletion lists. 
 * @author Chreistian Guretzki
 */
public class GroupDeletionManager extends BasicManager {

	private static final String GROUP_ARCHIVE_DIR = "archive_deleted_groups";

	private static final String PROPERTY_CATEGORY = "GroupDeletion";
	private static final String LAST_USAGE_DURATION_PROPERTY_NAME = "LastUsageDuration";
	private static final int DEFAULT_LAST_USAGE_DURATION = 24;
	private static final String DELETE_EMAIL_DURATION_PROPERTY_NAME = "DeleteEmailDuration";
	private static final int DEFAULT_DELETE_EMAIL_DURATION = 30;
	
	private static final String  GROUPEXPORT_XML  = "groupexport.xml";
	private static final String  GROUPARCHIVE_XLS = "grouparchive.xls";
	
	private static GroupDeletionManager INSTANCE;

	public static final String SEND_DELETE_EMAIL_ACTION = "sendDeleteEmail";
	private static final String GROUP_DELETED_ACTION = "groupDeleted";
	private DeletionModule module;
	private BusinessGroupService businessGroupService;

	/**
	 * [used by spring]
	 * @param deletionModule
	 */
	private GroupDeletionManager(DeletionModule deletionModule) {
		this.module = deletionModule;
		INSTANCE = this;
	}

	/**
	 * [used by Spring]
	 */
	public void setBusinessGroupService(BusinessGroupService businessGroupService) {
		this.businessGroupService = businessGroupService;
	}

	/**
	 * @return Singleton.
	 */
	public static GroupDeletionManager getInstance() { 
		return INSTANCE; 
	}

	public void setLastUsageDuration(int lastUsageDuration) {
		setProperty(LAST_USAGE_DURATION_PROPERTY_NAME, lastUsageDuration);
	}

	public void setDeleteEmailDuration(int deleteEmailDuration) {
		setProperty(DELETE_EMAIL_DURATION_PROPERTY_NAME, deleteEmailDuration);
	}

	public int getLastUsageDuration() {
		return getPropertyByName(LAST_USAGE_DURATION_PROPERTY_NAME, DEFAULT_LAST_USAGE_DURATION);
	}

	public int getDeleteEmailDuration() {
		return getPropertyByName(DELETE_EMAIL_DURATION_PROPERTY_NAME, DEFAULT_DELETE_EMAIL_DURATION);
	}

	public String sendDeleteEmailTo(List selectedGroups, MailTemplate mailTemplate, boolean isTemplateChanged, String keyEmailSubject, 
			String keyEmailBody, Identity sender, PackageTranslator pT) {
		StringBuffer warningMessage = new StringBuffer();
		if (mailTemplate != null) {
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			HashMap identityGroupList = new HashMap();
			for (Iterator iter = selectedGroups.iterator(); iter.hasNext();) {
				BusinessGroup group = (BusinessGroup)iter.next();
			
				// Build owner group, list of identities
				SecurityGroup ownerGroup = group.getOwnerGroup();
				List ownerIdentities = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(ownerGroup);
				// loop over this list and send email
				for (Iterator iterator = ownerIdentities.iterator(); iterator.hasNext();) {
					Identity identity = (Identity) iterator.next();
					if (identityGroupList.containsKey(identity) ) {
						List groupsOfIdentity = (List)identityGroupList.get(identity);
						groupsOfIdentity.add(group);
					} else {
						List groupsOfIdentity = new ArrayList();
						groupsOfIdentity.add(group);
						identityGroupList.put(identity, groupsOfIdentity);
					}				
				}
			}
	    //	 loop over identity list and send email
			for (Iterator iterator = identityGroupList.keySet().iterator(); iterator.hasNext();) {
				Identity identity = (Identity) iterator.next(); 
						
				mailTemplate.addToContext("responseTo", module.getEmailResponseTo());
				if (!isTemplateChanged) {
					// Email template has NOT changed => take translated version of subject and body text
					Translator identityTranslator = Util.createPackageTranslator(this.getClass(), I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage()));
					mailTemplate.setSubjectTemplate(identityTranslator.translate(keyEmailSubject));
					mailTemplate.setBodyTemplate(identityTranslator.translate(keyEmailBody));
				} 
				// loop over all repositoriesOfIdentity to build email message
				StringBuilder buf = new StringBuilder();
				for (Iterator groupIterator = ((List)identityGroupList.get(identity)).iterator(); groupIterator.hasNext();) {
					BusinessGroup group = (BusinessGroup) groupIterator.next();
					buf.append("\n  ").append( group.getName() ).append(" / ").append(FilterFactory.getHtmlTagsFilter().filter(group.getDescription()));
				}
				mailTemplate.addToContext("groupList", buf.toString());
				mailTemplate.putVariablesInMailContext(mailTemplate.getContext(), identity);
				logDebug(" Try to send Delete-email to identity=" + identity.getName() + " with email=" + identity.getUser().getProperty(UserConstants.EMAIL, null));
				List<Identity> ccIdentities = new ArrayList<Identity>();
				if(mailTemplate.getCpfrom()) {
					ccIdentities.add(sender);
				} else {
					ccIdentities = null;	
				}
				MailerResult mailerResult = mailer.sendMailUsingTemplateContext(identity, ccIdentities, null, mailTemplate, sender);
				if (mailerResult.getReturnCode() == MailerResult.OK) {
					// Email sended ok => set deleteEmailDate
					for (Iterator groupIterator = ((List)identityGroupList.get(identity)).iterator(); groupIterator.hasNext();) {
						BusinessGroup group = (BusinessGroup) groupIterator.next();
						logAudit("Group-Deletion: Delete-email send to identity=" + identity.getName() + " with email=" + identity.getUser().getProperty(UserConstants.EMAIL, null) + " for group=" + group);
						markSendEmailEvent(group);
					}
				} else {
					warningMessage.append( pT.translate("email.error.send.failed", new String[] {identity.getUser().getProperty(UserConstants.EMAIL, null), identity.getName()} ) ).append("\n");
				}
			}
		} else {
			// no template => User decides to sending no delete-email, mark only in lifecycle table 'sendEmail'
			for (Iterator iter = selectedGroups.iterator(); iter.hasNext();) {
				BusinessGroup group = (BusinessGroup)iter.next();
				logAudit("Group-Deletion: Move in 'Email sent' section without sending email, group=" + group);
				markSendEmailEvent(group);
			}
		}		
		return warningMessage.toString();		
	}


	private void markSendEmailEvent(BusinessGroup group) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		group = bgs.loadBusinessGroup(group);
		LifeCycleManager.createInstanceFor(group).markTimestampFor(SEND_DELETE_EMAIL_ACTION);
		group = bgs.mergeBusinessGroup(group);
	}

	public List getDeletableGroups(int lastLoginDuration) {
		Calendar lastUsageLimit = Calendar.getInstance();
		lastUsageLimit.add(Calendar.MONTH, - lastLoginDuration);
		logDebug("lastLoginLimit=" + lastUsageLimit);
		// 1. get all businness-groups with lastusage > x
		String query = "select gr from org.olat.group.BusinessGroupImpl as gr "
				+ " where (gr.lastUsage = null or gr.lastUsage < :lastUsage)"
				+ " and gr.type = :type ";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		dbq.setDate("lastUsage", lastUsageLimit.getTime());
		dbq.setString("type", BusinessGroup.TYPE_BUDDYGROUP);
		List groups = dbq.list();
		// 2. get all businness-groups in deletion-process (email send)
		query = "select gr from org.olat.group.BusinessGroupImpl as gr"
			+ " , org.olat.commons.lifecycle.LifeCycleEntry as le"
			+ " where gr.key = le.persistentRef "
			+ " and le.persistentTypeName ='org.olat.group.BusinessGroupImpl'" 
			+ " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' ";
		dbq = DBFactory.getInstance().createQuery(query);
		List groupsInProcess = dbq.list();
		// 3. Remove all groups in deletion-process from all unused-groups
		groups.removeAll(groupsInProcess);
		return groups;
	}

	public List getGroupsInDeletionProcess(int deleteEmailDuration) {
		Calendar deleteEmailLimit = Calendar.getInstance();
		deleteEmailLimit.add(Calendar.DAY_OF_MONTH, - (deleteEmailDuration - 1));
		logDebug("deleteEmailLimit=" + deleteEmailLimit);
		String queryStr = "select gr from org.olat.group.BusinessGroupImpl as gr"
				+ " , org.olat.commons.lifecycle.LifeCycleEntry as le"
				+ " where gr.key = le.persistentRef "
				+ " and le.persistentTypeName ='org.olat.group.BusinessGroupImpl'" 
				+ " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp >= :deleteEmailDate "
				+ " and gr.type = :type ";
		DBQuery dbq = DBFactory.getInstance().createQuery(queryStr);
		dbq.setDate("deleteEmailDate", deleteEmailLimit.getTime());
		dbq.setString("type", BusinessGroup.TYPE_BUDDYGROUP);
		return dbq.list();
	}

	public List getGroupsReadyToDelete(int deleteEmailDuration) {
		Calendar deleteEmailLimit = Calendar.getInstance();
		deleteEmailLimit.add(Calendar.DAY_OF_MONTH, - (deleteEmailDuration - 1));
		logDebug("deleteEmailLimit=" + deleteEmailLimit);
		String queryStr = "select gr from org.olat.group.BusinessGroupImpl as gr"
			+ " , org.olat.commons.lifecycle.LifeCycleEntry as le"
			+ " where gr.key = le.persistentRef "
			+ " and le.persistentTypeName ='org.olat.group.BusinessGroupImpl'" 
			+ " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp < :deleteEmailDate "
			+ " and gr.type = :type ";
		DBQuery dbq = DBFactory.getInstance().createQuery(queryStr);
		dbq.setDate("deleteEmailDate", deleteEmailLimit.getTime());
		dbq.setString("type", BusinessGroup.TYPE_BUDDYGROUP);
		return dbq.list();
	}

	public void deleteGroups(List objects) {
		for (Iterator iter = objects.iterator(); iter.hasNext();) {
			BusinessGroup businessGroup = (BusinessGroup) iter.next();
			String archiveFileName = archive(getArchivFilePath(businessGroup), businessGroup);
			logAudit("Group-Deletion: archived businessGroup=" + businessGroup + " , archive-file-name=" + archiveFileName);
			CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup).deleteTools(businessGroup);
			BusinessGroupManagerImpl.getInstance().deleteBusinessGroup(businessGroup);
			LifeCycleManager.createInstanceFor(businessGroup).deleteTimestampFor(SEND_DELETE_EMAIL_ACTION);
			LifeCycleManager.createInstanceFor(businessGroup).markTimestampFor(GROUP_DELETED_ACTION, createLifeCycleLogDataFor(businessGroup));
			logAudit("Group-Deletion: deleted businessGroup=" + businessGroup);
		}
	}

	private String createLifeCycleLogDataFor(BusinessGroup businessGroup) {
		StringBuilder buf = new StringBuilder();
		buf.append("<businessgroup>");
		buf.append("<name>").append(businessGroup.getName()).append("</name>");
		String desc = FilterFactory.getHtmlTagsFilter().filter(businessGroup.getDescription());
		buf.append("<description>").append(trimDescription(desc, 60)).append("</description>");
		buf.append("<resid>").append(businessGroup.getResourceableId()).append("</resid>");
		buf.append("</businessgroup>");
		return buf.toString();
	}
	
	private String trimDescription(String description, int maxlength) {
		if (description.length() > (maxlength) ) {
			return description.substring(0,maxlength-3) + "...";
		} 
		return description;
	}

	/**
	 * Archive group runtime-data in xls file and export group as xml file
	 * @param archiveFilePath
	 * @param businessGroup
	 * @return
	 */
	private String archive(String archiveFilePath, BusinessGroup businessGroup) {
		File exportRootDir = new File(archiveFilePath);
		if (!exportRootDir.exists()) {
			exportRootDir.mkdirs();
		}
		businessGroupService.archiveGroups(Collections.singletonList(businessGroup), new File(archiveFilePath, GROUPARCHIVE_XLS));
		File exportFile = new File(archiveFilePath, GROUPEXPORT_XML);
		businessGroupService.exportGroups(Collections.singletonList(businessGroup), exportFile);			

		return GROUPEXPORT_XML;
	}

	
	private String getArchivFilePath(BusinessGroup businessGroup) {
		return module.getArchiveRootPath() + File.separator + GROUP_ARCHIVE_DIR + File.separator + DeletionModule.getArchiveDatePath() 
		     + File.separator + "del_group_" + businessGroup.getResourceableId();
	}
	

	//////////////////
	// Private Methods
	//////////////////
	private int getPropertyByName(String name, int defaultValue) {
		List properties = PropertyManager.getInstance().findProperties(null, null, null, PROPERTY_CATEGORY, name);
		if (properties.size() == 0) {
			return defaultValue;
		} else {
			return ((Property)properties.get(0)).getLongValue().intValue();
		}
	}

	private void setProperty(String propertyName, int value) {
		List properties = PropertyManager.getInstance().findProperties(null, null, null, PROPERTY_CATEGORY, propertyName);
		Property property = null;
		if (properties.size() == 0) {
			property = PropertyManager.getInstance().createPropertyInstance(null, null, null, PROPERTY_CATEGORY, propertyName, null,  new Long(value), null, null);
		} else {
			property = (Property)properties.get(0);
			property.setLongValue( new Long(value) );
		}
		PropertyManager.getInstance().saveProperty(property);
	}


	public void setLastUsageNowFor(final BusinessGroup group) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<BusinessGroup>() {
			public BusinessGroup execute() {
				BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
				BusinessGroup bg = bgs.loadBusinessGroup(group);
				bg.setLastUsage(new Date());
				LifeCycleManager lcManager = LifeCycleManager.createInstanceFor(bg);
				if (lcManager.lookupLifeCycleEntry(SEND_DELETE_EMAIL_ACTION) != null) {
					logAudit("Group-Deletion: Remove from delete-list group=" + bg);
					LifeCycleManager.createInstanceFor(bg).deleteTimestampFor(SEND_DELETE_EMAIL_ACTION);
				}
				return bgs.mergeBusinessGroup(bg);	
			}
		});
	}
}