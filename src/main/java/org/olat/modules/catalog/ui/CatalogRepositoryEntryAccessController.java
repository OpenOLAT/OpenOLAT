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
package org.olat.modules.catalog.ui;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.list.RepositoryEntryDetailsHeaderController;
import org.olat.repository.ui.list.RepositoryEntryDetailsLinkController;
import org.olat.repository.ui.list.RepositoryEntryDetailsMetadataController;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.AccessListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryAccessController extends BasicController {
	
	private final RepositoryEntryDetailsHeaderController headerCtrl;
	private final RepositoryEntryDetailsMetadataController metadataCtrl;
	private final RepositoryEntryDetailsLinkController linkCtrl;
	private AccessListController accessCtrl;
	
	private final RepositoryEntry entry;
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ACService acService;
	public CatalogRepositoryEntryAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		List<String> memberRoles = repositoryService.getRoles(getIdentity(), entry);
		boolean isOwner = memberRoles.contains(GroupRoles.owner.name());
		boolean isParticipant = memberRoles.contains(GroupRoles.participant.name());
		boolean isMember = isOwner || isParticipant || memberRoles.contains(GroupRoles.coach.name());
		
		VelocityContainer mainVC = createVelocityContainer("access");
		
		headerCtrl = new RepositoryEntryDetailsHeaderController(ureq, wControl, entry, isMember, false);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		metadataCtrl = new RepositoryEntryDetailsMetadataController(ureq, wControl, entry, isMember, isParticipant, headerCtrl.getTypes(), ureq.getUserSession().getRoles().isGuestOnly());
		listenTo(metadataCtrl);
		mainVC.put("metadata", metadataCtrl.getInitialComponent());
		
		linkCtrl = new RepositoryEntryDetailsLinkController(ureq, wControl, entry);
		listenTo(linkCtrl);
		mainVC.put("link", linkCtrl.getInitialComponent());
		
		if (entry.getEducationalType() != null) {
			mainVC.contextPut("educationalTypeClass", entry.getEducationalType().getCssClass());	
		}
		
		AccessResult acResult = acService.isAccessible(entry, getIdentity(), null, ureq.getUserSession().getRoles().isGuestOnly(), false);
		if (acResult.isAccessible() || acService.tryAutoBooking(getIdentity(), entry, acResult)) {
			fireEvent(ureq, new BookedEvent(entry));
		} else {
			accessCtrl = new AccessListController(ureq, getWindowControl(), acResult.getAvailableMethods(), false);
			listenTo(accessCtrl);
			mainVC.put("access", accessCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessCtrl) {
			if (event == AccessEvent.ACCESS_OK_EVENT) {
				fireEvent(ureq, new BookedEvent(entry));
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
