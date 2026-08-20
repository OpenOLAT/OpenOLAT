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
import org.olat.core.util.Util;
import org.olat.course.CorruptedCourseException;
import org.olat.modules.catalog.ui.BookEvent;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureBlocksTimelineController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.list.AbstractInfoPageGetStartedController;
import org.olat.repository.ui.list.CurriculumElementInfoPageSectionsController;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.repository.ui.list.InfoPageBenefitsController;
import org.olat.repository.ui.list.InfoPageData;
import org.olat.repository.ui.list.InfoPageFactsController;
import org.olat.repository.ui.list.InfoPageHeaderController;
import org.olat.repository.ui.list.InfoPageTeaserImageController;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
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
	private final InfoPageHeaderController headerCtrl;
	private final InfoPageTeaserImageController teaserImageCtrl;
	private final CurriculumElementInfoPageGetStartedController getStartedCtrl;
	private final InfoPageBenefitsController benefitsCtrl;
	private final InfoPageFactsController factsCtrl;
	private final CurriculumElementInfoPageSectionsController sectionsCtrl;
	private LectureBlocksTimelineController lectureBlocksCtrl;

	private final CurriculumElement element;
	private final RepositoryEntry entry;
	private final Identity bookedIdentity;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;

	public CurriculumElementInfosController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			RepositoryEntry entry, DetailsHeaderConfig headerConfig) {
		this(ureq, wControl, element, entry, headerConfig, false);
	}

	public CurriculumElementInfosController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			RepositoryEntry entry, DetailsHeaderConfig headerConfig, boolean webPublish) {
		super(ureq, wControl, Util.createPackageTranslator(CurriculumElementInfosController.class, ureq.getLocale()));
		// The translator is explicitly set so that it is also available in the subclasses.
		this.element = element;
		this.entry = entry;
		bookedIdentity = headerConfig.getBookedIdentity();

		// Reset the velocity root, so that the children find the template
		setVelocityRoot(Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class));
		mainVC = createVelocityContainer("info_page");
		putInitialPanel(mainVC);

		List<LectureBlock> lectureBlocks = List.of();
		if (lectureModule.isEnabled()) {
			LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
			searchParams.setLectureConfiguredRepositoryEntry(false);
			searchParams.setCurriculumElementPath(element.getMaterializedPathKeys());
			lectureBlocks = lectureService.getLectureBlocks(searchParams, -1, Boolean.TRUE);
		}

		String shareUrl = headerConfig instanceof PreviewCurriculumElementHeaderConfig
				? null
				: CatalogBCFactory.get(webPublish).getOfferUrl(element.getResource());
		InfoPageData data = new CurriculumElementInfoPageData(element);
		headerCtrl = new InfoPageHeaderController(ureq, wControl, data, shareUrl);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());

		teaserImageCtrl = new InfoPageTeaserImageController(ureq, wControl, data);
		listenTo(teaserImageCtrl);
		if (teaserImageCtrl.hasContent()) {
			mainVC.put("thumbnail", teaserImageCtrl.getInitialComponent());
		}

		getStartedCtrl = new CurriculumElementInfoPageGetStartedController(ureq, wControl, headerConfig, element);
		listenTo(getStartedCtrl);
		if (getStartedCtrl.hasContent()) {
			mainVC.put("getStarted", getStartedCtrl.getInitialComponent());
		}

		benefitsCtrl = new InfoPageBenefitsController(ureq, wControl, element);
		listenTo(benefitsCtrl);
		if (benefitsCtrl.hasContent()) {
			mainVC.put("benefits", benefitsCtrl.getInitialComponent());
		}

		factsCtrl = new InfoPageFactsController(ureq, wControl, element, lectureBlocks.size());
		listenTo(factsCtrl);
		if (factsCtrl.hasContent()) {
			mainVC.put("facts", factsCtrl.getInitialComponent());
		}

		sectionsCtrl = new CurriculumElementInfoPageSectionsController(ureq, wControl, element, lectureBlocks);
		listenTo(sectionsCtrl);
		if (sectionsCtrl.hasContent()) {
			mainVC.put("sections", sectionsCtrl.getInitialComponent());
		}

		if (element.isShowLectures() && !lectureBlocks.isEmpty()) {
			lectureBlocksCtrl = new LectureBlocksTimelineController(ureq, getWindowControl(), lectureBlocks, true);
			listenTo(lectureBlocksCtrl);
			mainVC.put("events", lectureBlocksCtrl.getInitialComponent());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == getStartedCtrl) {
			if (event == AbstractInfoPageGetStartedController.START_EVENT) {
				doStart(ureq);
			} else if (event == AbstractInfoPageGetStartedController.RESERVATION_CONFIRMATION_EVENT) {
				doReservationConfirmed(ureq);
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
		//
	}

	protected void doStart(UserRequest ureq) {
		// Reload membership, maybe auto-booked
		if (bookedIdentity == null && !curriculumService.getCurriculumElementMemberships(List.of(element), List.of(bookedIdentity)).isEmpty()) {
			return;
		}
		if(entry != null) {
			try {
				String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (CorruptedCourseException e) {
				logError("Course corrupted: " + entry.getKey() + " (" + entry.getOlatResource().getResourceableId() + ")", e);
				showError("cif.error.corrupted");
			}
		} else if(element != null) {
			String businessPath = "[MyCoursesSite:0][Implementation:" + element.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}

	protected void doReservationConfirmed(UserRequest ureq) {
		fireEvent(ureq, new BookedEvent(entry));
	}

}
