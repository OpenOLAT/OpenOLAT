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
package org.olat.admin.user.groups;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGMailHelper;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * Functions to add an identity to multiple groups at once.
 * 
 * <P>
 * Initial Date:  09.05.2011 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GroupAddManager extends BasicManager {

	private static GroupAddManager INSTANCE = new GroupAddManager();

	public static final GroupAddManager getInstance() {
		return INSTANCE;
	}
	
	private GroupAddManager(){
		//
	}
	
	/**
	 * add identity to given groups as owner and/or participant
	 * @param ownGroups
	 * @param partGroups
	 * @param mailGroups
	 * @param ident
	 * @param addingIdentity
	 * @return
	 */
	public String[] addIdentityToGroups(List<Long> ownGroups, List<Long> partGroups, List<Long> mailGroups, final Identity ident, final Identity addingIdentity){
		AddToGroupsEvent groupsEv = new AddToGroupsEvent(ownGroups, partGroups, mailGroups);
		return addIdentityToGroups(groupsEv, ident, addingIdentity);
	}
	
	/**
	 * add identity to given groups as owner and/or participant
	 * @param groupsEv
	 * @param ident the identity to be added
	 * @param addingIdentity the identity who does this action
	 * @return
	 */
	public String[] addIdentityToGroups(AddToGroupsEvent groupsEv, final Identity ident, final Identity addingIdentity){
		final BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
		String[] resultTextArgs = new String[2];
		boolean addToAnyGroup = false;

		// notify user about add for following groups:
		List<Long> notifyAboutAdd = new ArrayList<Long>();
		List<Long> mailKeys = groupsEv.getMailForGroupsList();
		
		// add to owner groups
		List<Long> ownerKeys = groupsEv.getOwnerGroupKeys();
		String ownerGroupnames = "";
		for (Long groupKey : ownerKeys) {
			BusinessGroup group = bgm.loadBusinessGroup(groupKey, false);	
			if (group != null && !securityManager.isIdentityInSecurityGroup(ident, group.getOwnerGroup())){
//				seems not to work, but would be the way to go!
//				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(group));
				bgm.addOwnerAndFireEvent(addingIdentity, ident, group, flags, false);
				ownerGroupnames += group.getName() + ", ";
				addToAnyGroup = true;
				if (!notifyAboutAdd.contains(groupKey) && mailKeys.contains(groupKey)) notifyAboutAdd.add(groupKey);
			}
		}
		resultTextArgs[0] = ownerGroupnames.substring(0, ownerGroupnames.length() > 0 ? ownerGroupnames.length() - 2 : 0);

		// add to participant groups
		List<Long> participantKeys = groupsEv.getParticipantGroupKeys();
		String participantGroupnames = "";
		for (Long groupKey : participantKeys) {
			BusinessGroup group = bgm.loadBusinessGroup(groupKey, false);
			if (group != null && !securityManager.isIdentityInSecurityGroup(ident, group.getPartipiciantGroup())) {
				final BusinessGroup toAddGroup = group;
//				seems not to work, but would be the way to go!
//				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(group));
				CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
					public void execute() {
						bgm.addParticipantAndFireEvent(addingIdentity, ident, toAddGroup, flags, false);
					}});
				participantGroupnames += group.getName() + ", ";
				addToAnyGroup = true;
				if (!notifyAboutAdd.contains(groupKey) && mailKeys.contains(groupKey)) notifyAboutAdd.add(groupKey);
			}			
		}
		resultTextArgs[1] = participantGroupnames.substring(0, participantGroupnames.length() > 0 ? participantGroupnames.length() - 2 : 0);
		
		// send notification mails
		for (Long notifKey : notifyAboutAdd) {
			BusinessGroup group = bgm.loadBusinessGroup(notifKey, false);
			MailTemplate mailTemplate = BGMailHelper.createAddParticipantMailTemplate(group, addingIdentity);
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			MailerResult mailerResult = mailer.sendMail(null, ident, null, null, mailTemplate, null);
			if (mailerResult.getReturnCode() != MailerResult.OK){
				logDebug("Problems sending Group invitation mail for identity: " + ident.getName() + " and group: " 
						+ group.getName() + " key: " + group.getKey() + " mailerresult: " + mailerResult.getReturnCode(), null);
			}
		}		
		
		if (addToAnyGroup) {
			return resultTextArgs;
		} else {
			return null;
		}
	}	
}
