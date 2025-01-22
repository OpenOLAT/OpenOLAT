/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.run.InfoCourse;
import org.olat.modules.catalog.ui.BookEvent;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.OffersController;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 * 
 */
public abstract class RepositoryEntryDetailsController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryDetailsController.class);
	
	private RepositoryEntryDetailsHeaderController headerCtrl;
	private RepositoryEntryResourceInfoDetailsHeaderController resourceInfoHeaderCtrl;
	private final RepositoryEntryDetailsDescriptionController descriptionCtrl;
	private final RepositoryEntryDetailsMetadataController metadataCtrl;
	private OffersController offersCtrl;
	private final RepositoryEntryDetailsLinkController linkCtrl;
	private RepositoryEntryDetailsTechnicalController technicalDetailsCtrl;

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final RepositoryEntry entry;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryModule repositoryModule;

	@Autowired
	private ACService acService;
	@Autowired
	private CourseModule courseModule;

	public RepositoryEntryDetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean isResourceInfoView, boolean closeTabOnLeave, boolean webCatalog) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		List<String> memberRoles = getIdentity() != null? repositoryService.getRoles(getIdentity(), entry): List.of();
		boolean isOwner = memberRoles.contains(GroupRoles.owner.name());
		boolean isParticipant = memberRoles.contains(GroupRoles.participant.name());
		boolean isMember = isOwner || isParticipant || memberRoles.contains(GroupRoles.coach.name());
		
		velocity_root = Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class);
		VelocityContainer mainVC = createVelocityContainer("details");

		if (isResourceInfoView) {
			resourceInfoHeaderCtrl = new RepositoryEntryResourceInfoDetailsHeaderController(ureq, wControl, entry);
			listenTo(resourceInfoHeaderCtrl);
			mainVC.put("header", resourceInfoHeaderCtrl.getInitialComponent());
			metadataCtrl = new RepositoryEntryDetailsMetadataController(ureq, wControl, entry, isMember, true);
		} else {
			headerCtrl = new RepositoryEntryDetailsHeaderController(ureq, wControl, entry, isMember, closeTabOnLeave);
			listenTo(headerCtrl);
			mainVC.put("header", headerCtrl.getInitialComponent());
			boolean guestOnly = ureq.getUserSession().getRoles() == null || ureq.getUserSession().getRoles().isGuestOnly();
			metadataCtrl = new RepositoryEntryDetailsMetadataController(ureq, wControl, entry, isMember, guestOnly);
		}
		
		listenTo(metadataCtrl);
		mainVC.put("metadata", metadataCtrl.getInitialComponent());
		
		descriptionCtrl = new RepositoryEntryDetailsDescriptionController(ureq, wControl, entry);
		listenTo(descriptionCtrl);
		mainVC.put("description", descriptionCtrl.getInitialComponent());
		mainVC.contextPut("hasDescription", Boolean.valueOf(descriptionCtrl.hasDescription()));
		
		if (!isMember) {
			AccessResult acResult = ureq.getUserSession().getRoles() == null
					? acService.isAccessible(entry, null, Boolean.FALSE, false, Boolean.TRUE, false)
					: acService.isAccessible(entry, getIdentity(), null, ureq.getUserSession().getRoles().isGuestOnly(), null, false);
			if (acResult.isAccessible()) {
				fireEvent(ureq, new BookedEvent(entry));
			} else if (!acResult.getAvailableMethods().isEmpty()) {
				if (acResult.getAvailableMethods().size() > 1 || !acResult.getAvailableMethods().get(0).getOffer().isAutoBooking()) {
					offersCtrl = new OffersController(ureq, getWindowControl(), acResult.getAvailableMethods(), false, webCatalog);
					listenTo(offersCtrl);
					mainVC.put("offers", offersCtrl.getInitialComponent());
				}
			}
		}
		
		linkCtrl = new RepositoryEntryDetailsLinkController(ureq, wControl, entry);
		listenTo(linkCtrl);
		mainVC.put("link", linkCtrl.getInitialComponent());

		if (repositoryModule.isCommentEnabled()) {
			UserCommentsAndRatingsController userCommentsCtrl = initCommentsCtrl(ureq);
			mainVC.put("comments", userCommentsCtrl.getInitialComponent());
		}

		// show technical data only for administrative users or owners, hide from normal users
		if (ureq.getUserSession().getRoles() != null) {
			Roles roles = ureq.getUserSession().getRoles();
			if (isOwner || roles.isAdministrator() || roles.isManager()) {
				technicalDetailsCtrl = new RepositoryEntryDetailsTechnicalController(ureq, wControl, entry, isOwner);
				listenTo(technicalDetailsCtrl);
				mainVC.put("technical", technicalDetailsCtrl.getInitialComponent());
			}
		}
		
		if (entry.getEducationalType() != null) {
			mainVC.contextPut("educationalTypeClass", entry.getEducationalType().getCssClass());	
		}
		
		if (courseModule.isInfoDetailsEnabled()) {
			String oInfoCourse = null;
			try {
				InfoCourse infoCourse = InfoCourse.of(entry);
				if (infoCourse != null) {
					oInfoCourse = objectMapper.writeValueAsString(infoCourse);
				}
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
			mainVC.contextPut("oInfoCourse", oInfoCourse);
		}
		
		putInitialPanel(mainVC);
	}

	private UserCommentsAndRatingsController initCommentsCtrl(UserRequest ureq) {
		boolean anonym = ureq.getUserSession().getRoles() == null || ureq.getUserSession().getRoles().isGuestOnly();
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, anonym);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", entry.getKey());
		UserCommentsAndRatingsController commentsCtrl = new UserCommentsAndRatingsController(ureq, getWindowControl(), ores, null, secCallback, null, secCallback.canViewComments(), false, true);

		listenTo(commentsCtrl);
		return commentsCtrl;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == headerCtrl) {
			if (event == RepositoryEntryDetailsHeaderController.START_EVENT) {
				doStart(ureq);
			} else if (event instanceof BookEvent) {
				fireEvent(ureq, event);
			} else if (event instanceof LeavingEvent) {
				fireEvent(ureq, event);
			}
		} else if (source == resourceInfoHeaderCtrl) {
			if (event == RepositoryEntryResourceInfoDetailsHeaderController.START_EVENT) {
				doStart(ureq);
			}
		} else if (source == offersCtrl) {
			if (event instanceof AccessEvent aeEvent) {
				if (event.equals(AccessEvent.ACCESS_OK_EVENT)) {
					doStart(ureq);
					fireEvent(ureq, new BookedEvent(entry));
				}
			} else if (event == OffersController.LOGIN_EVENT) {
				fireEvent(ureq, new BookEvent(entry.getOlatResource().getKey()));
			}
		}
		super.event(ureq, source, event);
	}
	
	protected RepositoryEntry getEntry() {
		return entry;
	}

	protected abstract void doStart(UserRequest ureq);
	
}
