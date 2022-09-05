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
package org.olat.repository.manager;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.fullWebApp.NotificationEvent;
import org.olat.core.commons.fullWebApp.NotificationsCenter;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryMembershipModifiedEvent;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Process the removed membership of repository entries.
 * 
 * Initial date: 09.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryMembershipProcessor implements InitializingBean, GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryMembershipProcessor.class);
	
	@Autowired
	private ACService acService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(RepositoryEntry.class));
	}

	@Override
	public void event(Event event) {
		if(event instanceof RepositoryEntryMembershipModifiedEvent) {
			RepositoryEntryMembershipModifiedEvent e = (RepositoryEntryMembershipModifiedEvent)event;
			if(RepositoryEntryMembershipModifiedEvent.IDENTITY_REMOVED.equals(e.getCommand())) {
				processIdentityRemoved(e.getRepositoryEntryKey(), e.getIdentityKey());
			} else if(RepositoryEntryMembershipModifiedEvent.ROLE_PARTICIPANT_ADD_PENDING.equals(e.getCommand())) {
				sendNotificationsToIdentities(e.getIdentityKey());
			} else if (RepositoryEntryMembershipModifiedEvent.ROLE_PARTICIPANT_ADDED.equals(e.getCommand())) {
				processIdentityAddedToRepositoryEntry(e.getIdentityKey(), e.getRepositoryEntryKey());
			}
		}
	}
	
	private void sendNotificationsToIdentities(Long identityKey) {
		OLATResourceable target = OresHelper.createOLATResourceableInstance(NotificationsCenter.class, identityKey);
		if (identityKey != null && !acService.getReservations(new IdentityRefImpl(identityKey)).isEmpty()) {
			String businessPath = "[GroupsSite:0][AllGroups:0]";
			List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
			String url = BusinessControlFactory.getInstance().getAsAuthURIString(ces, true);
			String[] args = new String[] {url};
			NotificationEvent event = new NotificationEvent(RepositoryManager.class, "pending.enrolments.info", args);
			coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, target);
		}
	}
	
	private void processIdentityRemoved(Long repoKey, Long identityKey) {
		IdentityRef identity = new IdentityRefImpl(identityKey);
		RepositoryEntryRef re = new RepositoryEntryRefImpl(repoKey);
		
		List<String> remainingRoles = repositoryEntryRelationDao.getRoles(identity, re);
		if(remainingRoles.isEmpty()) {
			OLATResource resource = repositoryManager.lookupRepositoryEntryResource(repoKey);
			notificationsManager.unsubscribeAllForIdentityAndResId(identity, resource.getResourceableId());
			
			// Inactivate the invitations
			invitationService.inactivateInvitations(re, identity);
		}
	}
	
	private void processIdentityAddedToRepositoryEntry(Long identityKey, Long courseEntryKey) {
		try {
			log.debug("Process Identity {} added to RepositoryEntry {}", identityKey, courseEntryKey);
			RepositoryEntry entry = repositoryService.loadByKey(courseEntryKey);
			if (entry != null && (ImsQTI21Resource.TYPE_NAME.equals(entry.getOlatResource().getResourceableTypeName())
							|| BinderTemplateResource.TYPE_NAME.equals(entry.getOlatResource().getResourceableTypeName()))) {
				Identity identity = securityManager.loadIdentityByKey(identityKey);
				assessmentService.getOrCreateAssessmentEntry(identity, null, entry, null, Boolean.TRUE, entry);
			}
		} catch (Exception e) {
			log.warn("Error when processing Identity {} added to RepositoryEntry {}",
					identityKey, courseEntryKey);
		}
	}
}