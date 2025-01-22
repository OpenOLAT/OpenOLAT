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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.modules.catalog.ui.BookEvent;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.ui.author.MediaContainerFilter;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.OffersController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfosController extends BasicController implements Controller {
	
	private final VelocityContainer mainVC;
	private CurriculumElementInfosHeaderController headerCtrl;
	private CurriculumElementInfosOutlineController outlineCtrl;
	private OffersController offersCtrl;
	private CurriculumElementInfosOverviewController overviewCtrl;

	private final CurriculumElement element;
	private VFSContainer mediaContainer;
	private final String baseUrl;
	private Boolean descriptionOpen = Boolean.TRUE;
	private Boolean objectivesOpen = Boolean.TRUE;
	private Boolean outlineOpen = Boolean.TRUE;
	private Boolean offersOpen = Boolean.TRUE;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private ACService acService;

	public CurriculumElementInfosController(UserRequest ureq, WindowControl wControl, CurriculumElement element, boolean scrollToOffers) {
		super(ureq, wControl);
		this.element = element;
		mainVC = createVelocityContainer("curriculum_element_infos");
		putInitialPanel(mainVC);
		
		Boolean webPublish = Boolean.TRUE;
		Boolean isMember = Boolean.FALSE;
		if (getIdentity() != null) {
			webPublish = null;
			isMember = !curriculumService.getCurriculumElementMemberships(List.of(element), List.of(getIdentity())).isEmpty();
		}
		
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setLectureConfiguredRepositoryEntry(false);
		searchParams.setCurriculumElementPath(element.getMaterializedPathKeys());
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, -1, Boolean.TRUE);
		
		
		// Header
		headerCtrl = new CurriculumElementInfosHeaderController(ureq, getWindowControl(), element, isMember);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		// Description, objectives
		mediaContainer = curriculumService.getMediaContainer(element);
		if (mediaContainer != null && mediaContainer.getName().equals("media")) {
			mediaContainer = mediaContainer.getParentContainer();
			mediaContainer.setDefaultItemFilter(new MediaContainerFilter(mediaContainer));
		}
		baseUrl = mediaContainer != null
				? registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()))
						: null;
		
		String description = getFormattedText(element.getDescription());
		if (StringHelper.containsNonWhitespace(description)) {
			mainVC.contextPut("description", element.getDescription());
			mainVC.contextPut("descriptionOpen",descriptionOpen);
		}
		
		mainVC.contextPut("objectives", element.getObjectives());
		mainVC.contextPut("objectivesOpen", objectivesOpen);
		
		// Outline
		if (element.isShowOutline()) {
			outlineCtrl = new CurriculumElementInfosOutlineController(ureq, getWindowControl(), element, lectureBlocks);
			listenTo(outlineCtrl);
			mainVC.put("outline", outlineCtrl.getInitialComponent());
			mainVC.contextPut("outlineOpen", outlineOpen);
		}
		
		// Offers
		AccessResult acResult = acService.isAccessible(element, getIdentity(), isMember, false, webPublish, false);
		if (acResult.isAccessible()) {
			fireEvent(ureq, new BookedEvent(element));
		} else if (!acResult.getAvailableMethods().isEmpty()) {
			if (acResult.getAvailableMethods().size() > 1 || !acResult.getAvailableMethods().get(0).getOffer().isAutoBooking()) {
				boolean webCatalog = webPublish != null? webPublish.booleanValue(): false;
				offersCtrl = new OffersController(ureq, getWindowControl(), acResult.getAvailableMethods(), false, webCatalog);
				listenTo(offersCtrl);
				mainVC.put("offers", offersCtrl.getInitialComponent());
				mainVC.contextPut("offersOpen", offersOpen);
			}
		}
		
		// Overview and lecture blocks	
		overviewCtrl = new CurriculumElementInfosOverviewController(ureq, getWindowControl(), element, lectureBlocks.size());
		listenTo(overviewCtrl);
		mainVC.put("overview", overviewCtrl.getInitialComponent());
		
		if (scrollToOffers) {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.scrollToElemId("#offers"));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.scrollToElemId("#o_navbar_wrapper"));
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
			if (event instanceof BookEvent) {
				fireEvent(ureq, event);
			}
		} else if (source == offersCtrl) {
			if (event == AccessEvent.ACCESS_OK_EVENT) {
				fireEvent(ureq, new BookedEvent(element));
			} else if (event == OffersController.LOGIN_EVENT) {
				fireEvent(ureq, new BookEvent(element.getResource().getKey()));
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
			String outlineOpenVal = ureq.getParameter("outlineOpen");
			if (StringHelper.containsNonWhitespace(outlineOpenVal)) {
				outlineOpen = Boolean.valueOf(outlineOpenVal);
				mainVC.contextPut("outlineOpen", outlineOpen);
			}
			String offersOpenVal = ureq.getParameter("offersOpen");
			if (StringHelper.containsNonWhitespace(offersOpenVal)) {
				offersOpen = Boolean.valueOf(offersOpenVal);
				mainVC.contextPut("offersOpen", offersOpen);
			}
		}
	}

}
