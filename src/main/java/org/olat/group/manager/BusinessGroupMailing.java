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

import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.MembershipModification;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.main.MemberPermissionChangeEvent;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http:/www.frentix.com
 */
public class BusinessGroupMailing {

	public static MailType getDefaultTemplateType(MemberPermissionChangeEvent event) {
		if(event != null && event.size() == 1) {
			List<BusinessGroupMembershipChange> changes = event.getGroupChanges();
			if(changes.size() == 1) {
				BusinessGroupMembershipChange change = changes.get(0);
				if(change.getTutor() != null) {
					return MailType.addCoach;
				} else if(change.getParticipant() != null) {
					return MailType.addParticipant;
				} else if (change.getWaitingList() != null) {
					return MailType.addToWaitingList;
				}
			}
		}
		return null;
	}
	
	
	public static MailType getDefaultTemplateType(MembershipModification mod) {
		int total = mod.getAddOwners().size() + mod.getAddParticipants().size()
				+ mod.getAddToWaitingList().size() + mod.getRemovedIdentities().size();

		if(total == 1) {
			if(mod.getAddOwners().size() == 1) {
				return MailType.addCoach;
			} else if(mod.getAddParticipants().size() == 1) {
				return MailType.addParticipant;
			} else if(mod.getAddToWaitingList().size() == 1) {
				return MailType.addToWaitingList;
			}
		}
		return null;
	}
	
	public static MailTemplate getDefaultTemplate(MailType type, BusinessGroupShort group, Identity ureqIdentity) {
		if(type == null) return null;
		
		switch(type) {
			case addParticipant:
				return BGMailHelper.createAddParticipantMailTemplate(group, ureqIdentity);
			case removeParticipant:
				return BGMailHelper.createRemoveParticipantMailTemplate(group, ureqIdentity);
			case addCoach:
				return BGMailHelper.createAddParticipantMailTemplate(group, ureqIdentity);
			case removeCoach:
				return BGMailHelper.createRemoveParticipantMailTemplate(group, ureqIdentity);
			case addToWaitingList:
				return BGMailHelper.createAddWaitinglistMailTemplate(group, ureqIdentity);
			case removeToWaitingList:
				return BGMailHelper.createRemoveWaitinglistMailTemplate(group, ureqIdentity);
			case graduateFromWaitingListToParticpant:
				return BGMailHelper.createWaitinglistTransferMailTemplate(group, ureqIdentity);
		}
		return null;
	}
		
	protected static void sendEmail(Identity ureqIdentity, Identity identity, BusinessGroupShort group,
			MailType type, MailPackage mailing, MailerWithTemplate mailer) {
		
		if(mailing != null && !mailing.isSendEmail()) {
			return;
		}
		
		if(mailing == null) {
			BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
			BusinessGroupModule groupModule = CoreSpringFactory.getImpl(BusinessGroupModule.class);
			Roles ureqRoles = securityManager.getRoles(ureqIdentity);
			if(!groupModule.isMandatoryEnrolmentEmail(ureqRoles)) {
				return;
			}
		}

		MailTemplate template = mailing == null ? null : mailing.getTemplate();
		if(mailing == null || mailing.getTemplate() == null) {
			template = getDefaultTemplate(type, group, ureqIdentity);
		}
		
		MailContext context = mailing == null ? null : mailing.getContext();
		if(context == null) {
			context = new MailContextImpl(null, null, "[BusinessGroup:" + group.getKey() + "]");
		}
		
		String metaId = mailing != null ? mailing.getUuid() : null;
		MailerResult result = mailer.sendMailAsSeparateMails(context, Collections.singletonList(identity), null, template, ureqIdentity, metaId);
		if(mailing != null) {
			mailing.appendResult(result);
		}
	}

	public enum MailType {
		addParticipant,
		removeParticipant,
		addCoach,
		removeCoach,
		addToWaitingList,
		removeToWaitingList,
		graduateFromWaitingListToParticpant,
	}
}
