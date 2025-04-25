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
package org.olat.modules.curriculum.ui;


import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.course.CorruptedCourseException;
import org.olat.modules.catalog.ui.BookEvent;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureBlocksTimelineController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.MediaContainerFilter;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.repository.ui.list.RepositoryEntryDetailsHeaderController;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.OffersController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfosController extends BasicController {
	
	private final VelocityContainer mainVC;
	private CurriculumElementInfosHeaderController headerCtrl;
	private CurriculumElementInfosOutlineController outlineCtrl;
	private CurriculumElementInfoTaughtByController taughtByCtrl;
	private CurriculumElementInfosOverviewController overviewCtrl;
	private LectureBlocksTimelineController lectureBlocksCtrl;

	private final CurriculumElement element;
	private final RepositoryEntry entry;
	private VFSContainer mediaContainer;
	private final String baseUrl;
	private boolean isMember;
	private Boolean descriptionOpen = Boolean.TRUE;
	private Boolean objectivesOpen = Boolean.TRUE;
	private Boolean requirementsOpen = Boolean.TRUE;
	private Boolean creditsOpen = Boolean.TRUE;
	private Boolean outlineOpen = Boolean.TRUE;
	private Boolean taughtbyOpen = Boolean.TRUE;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;

	public CurriculumElementInfosController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			RepositoryEntry entry, Identity bookedIdentity, boolean preview) {
		super(ureq, wControl);
		this.element = element;
		this.entry = entry;
		mainVC = createVelocityContainer("curriculum_element_infos");
		putInitialPanel(mainVC);
		
		bookedIdentity = bookedIdentity != null ? bookedIdentity : getIdentity();
		if (bookedIdentity != null) {
			isMember = !curriculumService.getCurriculumElementMemberships(List.of(element), List.of(bookedIdentity)).isEmpty();
		} else {
			isMember = false;
		}
		
		List<LectureBlock> lectureBlocks = List.of();
		if (lectureModule.isEnabled()) {
			LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
			searchParams.setLectureConfiguredRepositoryEntry(false);
			searchParams.setCurriculumElementPath(element.getMaterializedPathKeys());
			lectureBlocks = lectureService.getLectureBlocks(searchParams, -1, Boolean.TRUE);
		}
		
		
		// Header
		headerCtrl = new CurriculumElementInfosHeaderController(ureq, getWindowControl(), element, entry, bookedIdentity, isMember, preview);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		// Description, objectives
		mediaContainer = curriculumService.getMediaContainer(element);
		if (mediaContainer != null && mediaContainer.getName().equals("media")) {
			mediaContainer = mediaContainer.getParentContainer();
			mediaContainer.setDefaultItemFilter(new MediaContainerFilter(mediaContainer));
		}
		baseUrl = mediaContainer != null
				? registerMapper(ureq, new VFSContainerMapper(mediaContainer))
						: null;
		
		String description = getFormattedText(element.getDescription());
		if (StringHelper.containsNonWhitespace(description)) {
			mainVC.contextPut("description", description);
			mainVC.contextPut("descriptionOpen",descriptionOpen);
		}
		String objectives = getFormattedText(element.getObjectives());
		if (StringHelper.containsNonWhitespace(objectives)) {
			mainVC.contextPut("objectives", objectives);
			mainVC.contextPut("objectivesOpen",objectivesOpen);
		}
		String requirements = getFormattedText(element.getRequirements());
		if (StringHelper.containsNonWhitespace(requirements)) {
			mainVC.contextPut("requirements", requirements);
			mainVC.contextPut("requirementsOpen",requirementsOpen);
		}
		String credits = getFormattedText(element.getCredits());
		if (StringHelper.containsNonWhitespace(credits)) {
			mainVC.contextPut("credits", credits);
			mainVC.contextPut("creditsOpen",creditsOpen);
		}
		
		
		// Outline
		if (element.isShowOutline()) {
			outlineCtrl = new CurriculumElementInfosOutlineController(ureq, getWindowControl(), element, lectureBlocks);
			listenTo(outlineCtrl);
			if (!outlineCtrl.isEmpty()) {
				mainVC.put("outline", outlineCtrl.getInitialComponent());
				mainVC.contextPut("outlineOpen", outlineOpen);
			}
		}
		
		// Taught by
		if (!element.getTaughtBys().isEmpty()) {
			taughtByCtrl = new CurriculumElementInfoTaughtByController(ureq, getWindowControl(), element, lectureBlocks);
			listenTo(taughtByCtrl);
			if (!taughtByCtrl.isEmpty()) {
				mainVC.put("taughtby", taughtByCtrl.getInitialComponent());
				mainVC.contextPut("taughtbyOpen", taughtbyOpen);
			}
		}
		
		// Overview and lecture blocks	
		overviewCtrl = new CurriculumElementInfosOverviewController(ureq, getWindowControl(), element, lectureBlocks.size());
		listenTo(overviewCtrl);
		mainVC.put("overview", overviewCtrl.getInitialComponent());
		
		if (element.isShowLectures() && !lectureBlocks.isEmpty()) {
			lectureBlocksCtrl = new LectureBlocksTimelineController(ureq, getWindowControl(), lectureBlocks, true);
			listenTo(lectureBlocksCtrl);
			mainVC.put("lectures", lectureBlocksCtrl.getInitialComponent());
		}
	}
	
	private String getFormattedText(final String text) {
		if (!StringHelper.containsNonWhitespace(text)) return null;
		
		String formattedTtext = StringHelper.xssScan(text);
		if (baseUrl != null) {
			formattedTtext = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUrl).filter(formattedTtext);
		}
		return Formatter.formatLatexFormulas(formattedTtext);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == headerCtrl) {
			if (event == RepositoryEntryDetailsHeaderController.START_EVENT) {
				doStart(ureq);
			} else if (event instanceof BookEvent) {
				fireEvent(ureq, event);
			} else if (event == AccessEvent.ACCESS_OK_EVENT) {
				fireEvent(ureq, new BookedEvent(element));
			} else if (event == OffersController.LOGIN_EVENT) {
				fireEvent(ureq, new BookEvent(element.getResource().getKey()));
			} else if (event instanceof LeavingEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String descriptionOpenVal = ureq.getParameter("descriptionOpen");
			if (StringHelper.containsNonWhitespace(descriptionOpenVal)) {
				descriptionOpen = Boolean.valueOf(descriptionOpenVal);
				mainVC.contextPut("descriptionOpen", descriptionOpen);
			}
			String objectivesOpenVal = ureq.getParameter("objectivesOpen");
			if (StringHelper.containsNonWhitespace(objectivesOpenVal)) {
				objectivesOpen = Boolean.valueOf(objectivesOpenVal);
				mainVC.contextPut("objectivesOpen", objectivesOpen);
			}
			String requirementsOpenVal = ureq.getParameter("requirementsOpen");
			if (StringHelper.containsNonWhitespace(requirementsOpenVal)) {
				requirementsOpen = Boolean.valueOf(requirementsOpenVal);
				mainVC.contextPut("requirementsOpen", requirementsOpen);
			}
			String creditsOpenVal = ureq.getParameter("creditsOpen");
			if (StringHelper.containsNonWhitespace(creditsOpenVal)) {
				creditsOpen = Boolean.valueOf(creditsOpenVal);
				mainVC.contextPut("creditsOpen", creditsOpen);
			}
			String outlineOpenVal = ureq.getParameter("outlineOpen");
			if (StringHelper.containsNonWhitespace(outlineOpenVal)) {
				outlineOpen = Boolean.valueOf(outlineOpenVal);
				mainVC.contextPut("outlineOpen", outlineOpen);
			}
			String taughtbyOpenVal = ureq.getParameter("taughtbyOpen");
			if (StringHelper.containsNonWhitespace(taughtbyOpenVal)) {
				taughtbyOpen = Boolean.valueOf(taughtbyOpenVal);
				mainVC.contextPut("taughtbyOpen", taughtbyOpen);
			}
		}
	}
	
	protected void doStart(UserRequest ureq) {
		if(isMember && entry != null) {
			try {
				String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (CorruptedCourseException e) {
				logError("Course corrupted: " + entry.getKey() + " (" + entry.getOlatResource().getResourceableId() + ")", e);
				showError("cif.error.corrupted");
			}
		} else {
			String businessPath = "[MyCoursesSite:0][CurriculumElement:" + element.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}
}
