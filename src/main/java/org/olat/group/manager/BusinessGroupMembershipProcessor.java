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

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.commons.fullWebApp.NotificationEvent;
import org.olat.core.commons.fullWebApp.NotificationsCenter;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.BusinessGroupDeletedEvent;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.group.model.BusinessGroupRelationModified;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.group.ui.main.BusinessGroupListController;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * process event related to membership removed from groups.
 * 
 * 
 * Initial date: 09.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BusinessGroupMembershipProcessor implements InitializingBean, GenericEventListener {
	
	@Autowired
	private ACService acService;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private InfoMessageFrontendManager infoMessageManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		coordinator.getCoordinator().getEventBus()
			.registerFor(this, null, OresHelper.lookupType(BusinessGroup.class));
	}

	@Override
	public void event(Event event) {
		if(event instanceof BusinessGroupModifiedEvent) {
			BusinessGroupModifiedEvent e = (BusinessGroupModifiedEvent)event;
			if(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT.equals(e.getCommand()) && e.getAffectedRepositoryEntryKey() == null) {
				processIdentityRemoved(e.getModifiedGroupKey(), e.getAffectedIdentityKey());
			} else if(BusinessGroupModifiedEvent.IDENTITY_ADD_PENDING_EVENT.equals(e.getCommand()) && e.getAffectedRepositoryEntryKey() == null) {
				sendNotificationsToIdentities(e.getAffectedIdentityKey());
			}
		} else if(event instanceof BusinessGroupRelationModified) {
			BusinessGroupRelationModified e = (BusinessGroupRelationModified)event;
			if(BusinessGroupRelationModified.RESOURCE_REMOVED_EVENT.equals(e.getCommand())) {
				processResourceRemoved(e.getGroupKey(), e.getRepositoryEntryKey());
			}
		} else if(event instanceof BusinessGroupDeletedEvent) {
			BusinessGroupDeletedEvent e = (BusinessGroupDeletedEvent)event;
			if(BusinessGroupDeletedEvent.RESOURCE_DELETED_EVENT.equals(e.getCommand())) {
				processBusinessGroupDeleted(e.getMemberKeys(), e.getRepositoryEntryKeys());
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
			NotificationEvent event = new NotificationEvent(BusinessGroupListController.class, "pending.enrolments.info", args);
			coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, target);
		}
	}
	
	private void processBusinessGroupDeleted(List<Long> memberKeys, List<Long> repoKeys) {
		for(Long repoKey:repoKeys) {
			RepositoryEntryRef entryRef = new RepositoryEntryRefImpl(repoKey);
			OLATResource resource = repositoryManager.lookupRepositoryEntryResource(entryRef.getKey());
			for(Long memberKey:memberKeys) {
				IdentityRef member = new IdentityRefImpl(memberKey);
				List<String> remaingRoles = repositoryEntryRelationDao.getRoles(member, entryRef);
				if(remaingRoles.isEmpty()) {
					notificationsManager.unsubscribeAllForIdentityAndResId(member, resource.getResourceableId());
				}
			}
		}
	}
	
	private void processResourceRemoved(Long groupKey, Long repoKey) {
		BusinessGroupRef groupRef = new BusinessGroupRefImpl(groupKey);
		RepositoryEntryRef entryRef = new RepositoryEntryRefImpl(repoKey);
		OLATResource resource = repositoryManager.lookupRepositoryEntryResource(entryRef.getKey());

		List<Long> memberKeys = businessGroupRelationDao
			.getMemberKeys(Collections.singletonList(groupRef), GroupRoles.coach.name(), GroupRoles.participant.name());
		for(Long memberKey:memberKeys) {
			IdentityRef member = new IdentityRefImpl(memberKey);
			List<String> remaingRoles = repositoryEntryRelationDao.getRoles(member, entryRef);
			if(remaingRoles.isEmpty()) {
				notificationsManager.unsubscribeAllForIdentityAndResId(member, resource.getResourceableId());
			}
		}
	}
	
	private void processIdentityRemoved(Long groupKey, Long identityKey) {
		IdentityRef identityRef = new IdentityRefImpl(identityKey);
		BusinessGroupRef groupRef = new BusinessGroupRefImpl(groupKey);
		
		if(!businessGroupRelationDao.hasAnyRole(identityRef, groupRef)) {
			infoMessageManager.updateInfoMessagesOfIdentity(groupRef, identityRef);
			notificationsManager.unsubscribeAllForIdentityAndResId(identityRef, groupRef.getKey());
			
			List<BGRepositoryEntryRelation> relations = businessGroupRelationDao
					.findRelationToRepositoryEntries(Collections.singletonList(groupKey), 0, -1);
			for(BGRepositoryEntryRelation relation:relations) {
				Long repositoryEntryKey = relation.getRepositoryEntryKey();
				RepositoryEntryRef entryRef = new RepositoryEntryRefImpl(repositoryEntryKey);
				List<String> remaingRoles = repositoryEntryRelationDao.getRoles(identityRef, entryRef);
				if(remaingRoles.isEmpty()) {
					OLATResource resource = repositoryManager.lookupRepositoryEntryResource(entryRef.getKey());
					notificationsManager.unsubscribeAllForIdentityAndResId(identityRef, resource.getResourceableId());
				}
			}
		}
	}
}
