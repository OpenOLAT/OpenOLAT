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
package org.olat.repository.ui.list;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.run.InfoCourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * 
 */
public abstract class RepositoryEntryDetailsController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryDetailsController.class);
	
	private final RepositoryEntryDetailsHeaderController headerCtrl;
	private final RepositoryEntryDetailsDescriptionController accessListCtrl;
	private final RepositoryEntryDetailsMetadataController metadataCtrl;
	private final RepositoryEntryDetailsLinkController linkCtrl;
	private final RepositoryEntryDetailsTechnicalController technicalDetailsCtrl;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final RepositoryEntry entry;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CourseModule courseModule;

	public RepositoryEntryDetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		List<String> memberRoles = repositoryService.getRoles(getIdentity(), entry);
		boolean isOwner = memberRoles.contains(GroupRoles.owner.name());
		boolean isParticipant = memberRoles.contains(GroupRoles.participant.name());
		boolean isMember = isOwner || isParticipant || memberRoles.contains(GroupRoles.coach.name());
		
		velocity_root = Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class);
		VelocityContainer mainVC = createVelocityContainer("details");
		
		OLATResourceable ores = OresHelper.createOLATResourceableType("MyCoursesSite");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		
		headerCtrl = new RepositoryEntryDetailsHeaderController(ureq, wControl, entry, isMember, true);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		accessListCtrl = new RepositoryEntryDetailsDescriptionController(ureq, wControl, entry);
		listenTo(accessListCtrl);
		mainVC.put("description", accessListCtrl.getInitialComponent());
		mainVC.contextPut("hasNoDescription", Boolean.valueOf(!accessListCtrl.hasDescription()));
		
		metadataCtrl = new RepositoryEntryDetailsMetadataController(ureq, wControl, entry, isMember, isParticipant, headerCtrl.getTypes());
		listenTo(metadataCtrl);
		mainVC.put("metadata", metadataCtrl.getInitialComponent());
		
		linkCtrl = new RepositoryEntryDetailsLinkController(ureq, wControl, entry);
		listenTo(linkCtrl);
		mainVC.put("link", linkCtrl.getInitialComponent());
		
		technicalDetailsCtrl = new RepositoryEntryDetailsTechnicalController(ureq, wControl, entry, isOwner);
		listenTo(technicalDetailsCtrl);
		mainVC.put("technical", technicalDetailsCtrl.getInitialComponent());
		
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

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == headerCtrl) {
			if (event == RepositoryEntryDetailsHeaderController.START_EVENT) {
				doStart(ureq);
			} else if (event == RepositoryEntryDetailsHeaderController.BOOK_EVENT) {
				doBook(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	protected RepositoryEntry getEntry() {
		return entry;
	}

	protected abstract void doStart(UserRequest ureq);
	
	protected abstract void doBook(UserRequest ureq);
	
}
