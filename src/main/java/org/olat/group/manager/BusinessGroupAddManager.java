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
package org.olat.group.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.groups.AddToGroupsEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGMailHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * Functions to add an identity to multiple groups at once.
 * 
 * <P>
 * Initial Date:  09.05.2011 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
@Service("groupAddManager")
public class BusinessGroupAddManager extends BasicManager {
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	
	/**
	 * add identity to given groups as owner and/or participant
	 * @param ownGroups
	 * @param partGroups
	 * @param mailGroups
	 * @param ident
	 * @param addingIdentity
	 * @return
	 */
	public String[] addIdentityToGroups(List<Long> ownGroups, List<Long> partGroups, List<Long> mailGroups, final Identity ident, final Identity addingIdentity) {
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
	public String[] addIdentityToGroups(AddToGroupsEvent groupsEv, final Identity ident, final Identity addingIdentity) {
		final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
		String[] resultTextArgs = new String[2];
		boolean addToAnyGroup = false;

		// notify user about add for following groups:
		List<Long> notifyAboutAdd = new ArrayList<Long>();
		List<Long> mailKeys = groupsEv.getMailForGroupsList();
		
		// add to owner groups
		List<Long> ownerKeys = groupsEv.getOwnerGroupKeys();
		String ownerGroupnames = "";

		List<BusinessGroup> groups = businessGroupService.loadBusinessGroups(ownerKeys);	
		for (BusinessGroup group : groups) {
			if (group != null && !securityManager.isIdentityInSecurityGroup(ident, group.getOwnerGroup())){
//				seems not to work, but would be the way to go!
//				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(group));
				businessGroupService.addOwner(addingIdentity, ident, group, flags);
				ownerGroupnames += group.getName() + ", ";
				addToAnyGroup = true;
				if (!notifyAboutAdd.contains(group.getKey()) && mailKeys.contains(group.getKey())) notifyAboutAdd.add(group.getKey());
			}
		}
		resultTextArgs[0] = ownerGroupnames.substring(0, ownerGroupnames.length() > 0 ? ownerGroupnames.length() - 2 : 0);

		// add to participant groups
		List<Long> participantKeys = groupsEv.getParticipantGroupKeys();
		String participantGroupnames = "";
		List<BusinessGroup> participantGroups = businessGroupService.loadBusinessGroups(participantKeys);	
		for (BusinessGroup group : participantGroups) {
			if (group != null && !securityManager.isIdentityInSecurityGroup(ident, group.getPartipiciantGroup())) {
				final BusinessGroup toAddGroup = group;
//				seems not to work, but would be the way to go!
//				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(group));
				CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
					public void execute() {
						businessGroupService.addParticipant(addingIdentity, ident, toAddGroup, flags);
					}});
				participantGroupnames += group.getName() + ", ";
				addToAnyGroup = true;
				if (!notifyAboutAdd.contains(group.getKey()) && mailKeys.contains(group.getKey())) notifyAboutAdd.add(group.getKey());
			}			
		}
		resultTextArgs[1] = participantGroupnames.substring(0, participantGroupnames.length() > 0 ? participantGroupnames.length() - 2 : 0);
		
		// send notification mails

		List<BusinessGroup> notifGroups = businessGroupService.loadBusinessGroups(notifyAboutAdd);
		for (BusinessGroup group : notifGroups) {
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
