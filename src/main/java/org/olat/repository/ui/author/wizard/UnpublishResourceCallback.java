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
package org.olat.repository.ui.author.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;

/**
 * 
 * Initial date: 29.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UnpublishResourceCallback implements StepRunnerCallback {
	
	private static final OLog log = Tracing.createLoggerFor(UnpublishResourceCallback.class);
	
	private RepositoryEntry repositoryEntry;
	
	private final MailManager mailManager;
	private final RepositoryService repositoryService;
	private final BusinessGroupService businessGroupService;
	
	public UnpublishResourceCallback(RepositoryEntry entry) {
		repositoryEntry = entry;
		mailManager = CoreSpringFactory.getImpl(MailManager.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		
		MailTemplate mailTemplate = (MailTemplate)runContext.get("mailTemplate");
		
		if (mailTemplate != null) {
			List<Identity> ownerList = new ArrayList<>();
			// owners
			if (repositoryService.hasRole(ureq.getIdentity(), repositoryEntry, GroupRoles.owner.name())) {
				ownerList = repositoryService.getMembers(repositoryEntry, GroupRoles.owner.name());
			}

			String businessPath = wControl.getBusinessControl().getAsString();
			MailContext context = new MailContextImpl(businessPath);
			String metaId = UUID.randomUUID().toString().replace("-", "");
			MailerResult result = new MailerResult();
			MailBundle[] bundles = mailManager.makeMailBundles(context, ownerList, mailTemplate, ureq.getIdentity(), metaId, result);
			result.append(mailManager.sendMessage(bundles));
			if (mailTemplate.getCpfrom()) {
				MailBundle ccBundle = mailManager.makeMailBundle(context, ureq.getIdentity(), mailTemplate, ureq.getIdentity(), metaId, result);
				result.append(mailManager.sendMessage(ccBundle));
			}
			
			StringBuilder errorMessage = new StringBuilder(1024);
			StringBuilder warningMessage = new StringBuilder(1024);
			Roles roles = ureq.getUserSession().getRoles();
			boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
			MailHelper.appendErrorsAndWarnings(result, errorMessage, warningMessage, detailedErrorOutput, ureq.getLocale());
			if (warningMessage.length() > 0) {
				wControl.setWarning(warningMessage.toString());
			}
			if (errorMessage.length() > 0) {
				wControl.setError(errorMessage.toString());
			}
			ownerList.clear();
		}

		//update status
		repositoryEntry = repositoryService.loadByKey(repositoryEntry.getKey());
		repositoryEntry.setStatusCode(RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED);
		repositoryEntry = DBFactory.getInstance().getCurrentEntityManager().merge(repositoryEntry);

		// clean catalog
		Object cleanCatalog = runContext.get("cleanCatalog");
		if(cleanCatalog != null && Boolean.TRUE.equals(cleanCatalog)) {
			CoreSpringFactory.getImpl(CatalogManager.class).resourceableDeleted(repositoryEntry);
		}
		// clean groups
		Object cleanGroups = runContext.get("cleanGroups");
		if(cleanGroups != null && Boolean.TRUE.equals(cleanGroups)) {
			doCleanGroups(ureq.getIdentity());
		}
		

		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_CLOSE, getClass());
		log.audit("Repository entry " + repositoryEntry.getDisplayname() + " ( " + repositoryEntry.getOlatResource() + " ) closed");
		
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	/**
	 * do unsubscribe all group members from this course
	 */
	private void doCleanGroups(Identity identity) {
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		if(course != null) {
			// LearningGroups
			List<BusinessGroup> allGroups = course.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups();
			for (BusinessGroup bGroup : allGroups) {
				List<BusinessGroup> bGroupList = Collections.singletonList(bGroup);
				List<RepositoryEntry> entries = businessGroupService.findRepositoryEntries(bGroupList, 0, -1);
				if(entries.contains(repositoryEntry) && entries.size() == 1) {
					List<Identity> owners = businessGroupService.getMembers(bGroup, GroupRoles.coach.name());
					businessGroupService.removeOwners(identity, owners, bGroup);
	
					List<Identity> participants = businessGroupService.getMembers(bGroup, GroupRoles.participant.name());
					businessGroupService.removeParticipants(identity, participants, bGroup, null);
					
					List<Identity> waitingList = businessGroupService.getMembers(bGroup, GroupRoles.waiting.name());
					businessGroupService.removeFromWaitingList(identity, waitingList, bGroup, null);
				} else {
					businessGroupService.removeResourceFrom(bGroupList, repositoryEntry);
				}
			}
			repositoryService.removeMembers(repositoryEntry, GroupRoles.coach.name(), GroupRoles.participant.name());
		}
	}
}