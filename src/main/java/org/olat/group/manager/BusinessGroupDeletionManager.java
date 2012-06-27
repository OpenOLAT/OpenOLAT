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

package org.olat.group.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.delete.service.DeletionModule;


/**
 * Manager for group deletion. Handle deletion-email and db-access for group-deletion lists. 
 * @author Chreistian Guretzki
 */
public class BusinessGroupDeletionManager extends BasicManager {

	private static final String GROUP_ARCHIVE_DIR = "archive_deleted_groups";
	private static final String  GROUPEXPORT_XML  = "groupexport.xml";
	private static final String  GROUPARCHIVE_XLS = "grouparchive.xls";

	private static final String GROUP_DELETED_ACTION = "groupDeleted";
	private DeletionModule module;
	private BusinessGroupService businessGroupService;

	/**
	 * [used by spring]
	 * @param deletionModule
	 */
	private BusinessGroupDeletionManager(DeletionModule deletionModule) {
		this.module = deletionModule;
	}

	/**
	 * [used by Spring]
	 */
	public void setBusinessGroupService(BusinessGroupService businessGroupService) {
		this.businessGroupService = businessGroupService;
	}



	public String sendDeleteEmailTo(List<BusinessGroup> selectedGroups, MailTemplate mailTemplate, boolean isTemplateChanged, String keyEmailSubject, 
			String keyEmailBody, Identity sender, Translator pT) {
		StringBuffer warningMessage = new StringBuffer();
		if (mailTemplate != null) {
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			Map<Identity,List<BusinessGroup>> identityGroupList = new HashMap<Identity,List<BusinessGroup>>();
			for (BusinessGroup group: selectedGroups) {
				// Build owner group, list of identities
				SecurityGroup ownerGroup = group.getOwnerGroup();
				List<Identity> ownerIdentities = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(ownerGroup);
				// loop over this list and send email
				for (Identity identity : ownerIdentities) {
					if (identityGroupList.containsKey(identity) ) {
						List<BusinessGroup> groupsOfIdentity = identityGroupList.get(identity);
						groupsOfIdentity.add(group);
					} else {
						List<BusinessGroup> groupsOfIdentity = new ArrayList<BusinessGroup>();
						groupsOfIdentity.add(group);
						identityGroupList.put(identity, groupsOfIdentity);
					}				
				}
			}
	    //	 loop over identity list and send email
			for (Identity identity : identityGroupList.keySet()) {		
				mailTemplate.addToContext("responseTo", module.getEmailResponseTo());
				if (!isTemplateChanged) {
					// Email template has NOT changed => take translated version of subject and body text
					String language = identity.getUser().getPreferences().getLanguage();
					Translator identityTranslator = Util.createPackageTranslator(BusinessGroupService.class, I18nManager.getInstance().getLocaleOrDefault(language));
					mailTemplate.setSubjectTemplate(identityTranslator.translate(keyEmailSubject));
					mailTemplate.setBodyTemplate(identityTranslator.translate(keyEmailBody));
				} 
				
				// loop over all repositoriesOfIdentity to build email message
				StringBuilder buf = new StringBuilder();
				for (BusinessGroup group : identityGroupList.get(identity)) {
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
					for (BusinessGroup group : identityGroupList.get(identity)) {
						logAudit("Group-Deletion: Delete-email send to identity=" + identity.getName() + " with email=" + identity.getUser().getProperty(UserConstants.EMAIL, null) + " for group=" + group);
						markSendEmailEvent(group);
					}
				} else {
					warningMessage.append( pT.translate("email.error.send.failed", new String[] {identity.getUser().getProperty(UserConstants.EMAIL, null), identity.getName()} ) ).append("\n");
				}
			}
		} else {
			// no template => User decides to sending no delete-email, mark only in lifecycle table 'sendEmail'
			for (BusinessGroup group : selectedGroups) {
				logAudit("Group-Deletion: Move in 'Email sent' section without sending email, group=" + group);
				markSendEmailEvent(group);
			}
		}		
		return warningMessage.toString();		
	}


	private void markSendEmailEvent(BusinessGroup group) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		group = bgs.loadBusinessGroup(group);
		LifeCycleManager.createInstanceFor(group).markTimestampFor(BusinessGroupService.SEND_DELETE_EMAIL_ACTION);
		//group = bgs.updateBusinessGroup(group);
	}

	protected void deleteGroups(List<BusinessGroup> groups) {
		for (BusinessGroup businessGroup : groups) {
			String archiveFileName = archive(getArchivFilePath(businessGroup), businessGroup);
			logAudit("Group-Deletion: archived businessGroup=" + businessGroup + " , archive-file-name=" + archiveFileName);

			businessGroupService.deleteBusinessGroup(businessGroup);
			LifeCycleManager.createInstanceFor(businessGroup).deleteTimestampFor(BusinessGroupService.SEND_DELETE_EMAIL_ACTION);
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
}