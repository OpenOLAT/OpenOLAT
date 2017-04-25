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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.MembershipModification;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.repository.RepositoryEntryShort;

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
			MailType type, MailPackage mailing) {
		
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
		} else if(group != null && template.getContext() != null && needTemplateEnhancement(template)) {
			BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
			List<RepositoryEntryShort> repoEntries = businessGroupService.findShortRepositoryEntries(Collections.singletonList(group), 0, -1);
			template = new MailTemplateDelegate(template, group, repoEntries);
		}
		
		MailContext context = mailing == null ? null : mailing.getContext();
		if(context == null) {
			context = new MailContextImpl(null, null, "[BusinessGroup:" + group.getKey() + "]");
		}
		
		MailerResult result = new MailerResult();
		String metaId = mailing != null ? mailing.getUuid() : null;
		MailManager mailService = CoreSpringFactory.getImpl(MailManager.class);
		MailBundle bundle = mailService.makeMailBundle(context, identity, template, ureqIdentity, metaId, result);
		if(bundle != null) {
			mailService.sendMessage(bundle);
		}
		if(mailing != null) {
			mailing.appendResult(result);
		}
	}
	
	private static boolean needTemplateEnhancement(MailTemplate template) {
		String body = template.getBodyTemplate();
		if(body.contains("groupname") || body.contains("groupdescription") || body.contains("courselist")) {
			if(!StringHelper.containsNonWhitespace((String)template.getContext().get("groupname"))) {
				return true;
			}
		}
		return false;
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
	
	public static class MailTemplateDelegate extends MailTemplate {
		
		private final MailTemplate delegate;
		private final BusinessGroupShort group;
		private final List<RepositoryEntryShort> entries;
		
		public MailTemplateDelegate(MailTemplate delegate, BusinessGroupShort group, List<RepositoryEntryShort> entries) {
			super(null, null, null);
			this.delegate = delegate;
			this.group = group;
			this.entries = entries;
		}

		@Override
		public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
			delegate.putVariablesInMailContext(vContext, recipient);
			
			StringBuilder learningResources = new StringBuilder();
			if(entries != null && entries.size() > 0) {
				for (RepositoryEntryShort entry: entries) {
					String title = entry.getDisplayname();
					String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString("[RepositoryEntry:" + entry.getKey() + "]");
					learningResources.append(title);
					learningResources.append(" (");
					learningResources.append(url);
					learningResources.append(")\n");
				}
			}
			vContext.put("courselist", learningResources.toString());
			
			if(group != null) {
				vContext.put("groupname", group.getName());
				
				String description;
				if(group instanceof BusinessGroup) {
					description = ((BusinessGroup)group).getDescription(); 
				} else {
					description = CoreSpringFactory.getImpl(BusinessGroupDAO.class).loadDescription(group.getKey());
				}
				description = FilterFactory.getHtmlTagAndDescapingFilter().filter(description); 
				vContext.put("groupdescription", description);
			}
		}

		@Override
		public Boolean getCpfrom() {
			return delegate.getCpfrom();
		}

		@Override
		public void setCpfrom(Boolean cpfrom) {
			delegate.setCpfrom(cpfrom);
		}

		@Override
		public String getSubjectTemplate() {
			return delegate.getSubjectTemplate();
		}

		@Override
		public void setSubjectTemplate(String subjectTemplate) {
			delegate.setSubjectTemplate(subjectTemplate);
		}
		
		@Override
		public String getBodyTemplate() {
			return delegate.getBodyTemplate();
		}

		@Override
		public void setBodyTemplate(String bodyTemplate) {
			delegate.setBodyTemplate(bodyTemplate);
		}

		@Override
		public File[] getAttachments() {
			return delegate.getAttachments();
		}

		@Override
		public void setAttachments(File[] attachments) {
			delegate.setAttachments(attachments);
		}

		@Override
		public void addToContext(String name, String value) {
			delegate.addToContext(name, value);
		}

		@Override
		public VelocityContext getContext() {
			return delegate.getContext();
		}
	}
}