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
package org.olat.course.nodes.practice.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.course.assessment.AssessmentEvents;
import org.olat.course.nodes.CourseNodeSegmentPrefs;
import org.olat.course.nodes.CourseNodeSegmentPrefs.CourseNodeSegment;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.course.nodes.practice.ui.events.ComposeSerieEvent;
import org.olat.course.nodes.practice.ui.events.NextSerieEvent;
import org.olat.course.nodes.practice.ui.events.OverviewEvent;
import org.olat.course.nodes.practice.ui.events.StartPracticeEvent;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeRunController extends BasicController {
	
	private Link coachLink;
	private Link previewLink;
	private final CourseNodeSegmentPrefs segmentPrefs;
	private SegmentViewComponent segmentView;
	private BreadcrumbedStackedPanel composerPanel;
	private final VelocityContainer mainVC;
	private TooledStackedPanel coachPanel;

	private final List<PracticeResource> resources;
	
	private final boolean authorMode;
	private PracticeCourseNode courseNode;
	private RepositoryEntry courseEntry;
	private final UserCourseEnvironment userCourseEnv;
	
	private PracticeController serieCtrl;
	private PracticeCoachController coachCtrl;
	private PracticeParticipantController participantCtrl;
	private PracticeComposeSerieController composeSerieCtrl;
	
	@Autowired
	private PracticeService practiceService;
	
	public PracticeRunController(UserRequest ureq, WindowControl wControl,
			PracticeCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		mainVC = createVelocityContainer("practice_run");
		segmentPrefs = new CourseNodeSegmentPrefs(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		authorMode = userCourseEnv.isCoach() || userCourseEnv.isAdmin();

		// remove resources which have deleted/trash status
		resources = practiceService.getResources(courseEntry, courseNode.getIdent());
		List<PracticeResource> resourcesToRemove = new ArrayList<>();

		resources.forEach(r -> {
			RepositoryEntry testEntry = r.getTestEntry();
			if (testEntry != null
					&& (RepositoryEntryStatusEnum.deleted == testEntry.getEntryStatus()
					|| RepositoryEntryStatusEnum.trash == testEntry.getEntryStatus())) {
				resourcesToRemove.add(r);
			}
		});

		if (!resourcesToRemove.isEmpty()) {
			resources.removeAll(resourcesToRemove);
		}

		// if all resources are deleted, return emptyStateCmp
		if (resources.isEmpty() & !authorMode) {
			EmptyStateConfig emptyState = EmptyStateConfig.builder()
					.withIconCss("o_practice_icon")
					.withIndicatorIconCss("o_icon_deleted")
					.withMessageI18nKey("warning.practice.all.entries.del")
					.build();
			EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
			emptyStateCmp.setTranslator(getTranslator());
			putInitialPanel(emptyStateCmp);
			return;
		}

		if(authorMode) {
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			coachLink = LinkFactory.createLink("run.coach.all", mainVC, this);
			coachLink.setElementCssClass("o_sel_practice_coaching");
			segmentView.addSegment(coachLink, true);
			
			previewLink = LinkFactory.createLink("run.preview", mainVC, this);
			previewLink.setElementCssClass("o_sel_practice_preview");
			segmentView.addSegment(previewLink, false);
			
			//Participants
			coachPanel = new TooledStackedPanel("coachPanel", getTranslator(), this);
			coachPanel.setToolbarAutoEnabled(true);
			coachPanel.setToolbarEnabled(false);
			coachPanel.setShowCloseLink(true, false);
			coachPanel.setCssClass("o_segment_toolbar o_block_top");
			
			doOpenPreferredSegment(ureq);
		} else {
			doOpenParticipant(ureq, false);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == coachLink) {
					doOpenCoach(ureq, true);
				} else if (clickedLink == previewLink) {
					doOpenParticipant(ureq, true);
				}
			}
		} else if(composerPanel == source) {
			cleanUpComposer();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(participantCtrl == source || composeSerieCtrl == source) {
			if(event instanceof StartPracticeEvent spe) {
				doStartPractice(ureq, spe.getPlayMode(), spe.getItems());
			} else if(event instanceof ComposeSerieEvent) {
				doComposeSerie(ureq);
			}
		} else if(serieCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT || event instanceof OverviewEvent) {
				cleanUpSerie();
				participantCtrl.reload();
			} else if(event instanceof NextSerieEvent nse) {
				doNextSerie(ureq, nse.getPlayMode());
			} else if(event == AssessmentEvents.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUpComposer() {
		removeAsListenerAndDispose(composeSerieCtrl);
		composeSerieCtrl = null;
		composerPanel.removeListener(this);
		composerPanel = null;
		
		cleanUpSerie();
	}
	
	private void cleanUpSerie() {
		mainVC.remove("practiceCmp");
		
		removeAsListenerAndDispose(composeSerieCtrl);
		removeAsListenerAndDispose(serieCtrl);
		composeSerieCtrl = null;
		serieCtrl = null;
	}
	
	private void doOpenPreferredSegment(UserRequest ureq) {
		CourseNodeSegment segment = segmentPrefs.getSegment(ureq);
		if (CourseNodeSegment.participants == segment && coachLink != null) {
			doOpenCoach(ureq, false);
		} else if (CourseNodeSegment.preview == segment && previewLink != null) {
			doOpenParticipant(ureq, false);
		} else {
			doOpenCoach(ureq, false);
		}
	}
	
	private Activateable2 doOpenCoach(UserRequest ureq, boolean saveSegmentPref) {
		if(coachCtrl == null) {
			coachCtrl = new PracticeCoachController(ureq, getWindowControl(), coachPanel,
					courseEntry, courseNode, userCourseEnv);
			listenTo(coachCtrl);
			coachPanel.pushController(translate("segment.participants"), coachCtrl);
		}
		addToHistory(ureq, coachCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", coachPanel);
			segmentView.select(coachLink);
			segmentPrefs.setSegment(ureq, CourseNodeSegment.participants, segmentView, saveSegmentPref);
		}
		return coachCtrl;
	}

	private void doOpenParticipant(UserRequest ureq, boolean saveSegmentPref) {
		// if all resources are deleted, return emptyStateCmp
		if (resources.isEmpty()) {
			EmptyStateConfig emptyState = EmptyStateConfig.builder()
					.withIconCss("o_practice_icon")
					.withIndicatorIconCss("o_icon_deleted")
					.withMessageI18nKey("warning.practice.all.entries.del")
					.build();
			EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
			emptyStateCmp.setTranslator(getTranslator());
			if(segmentView != null) {
				mainVC.put("segmentCmp", emptyStateCmp);
				segmentView.select(previewLink);
				segmentPrefs.setSegment(ureq, CourseNodeSegment.preview, segmentView, saveSegmentPref);
			} else {
				mainVC.put("participantCmp", emptyStateCmp);
			}
		} else {
			participantCtrl = new PracticeParticipantController(ureq, getWindowControl(),
					courseEntry, courseNode, userCourseEnv, getIdentity(), null, null);
			listenTo(participantCtrl);
			addToHistory(ureq, participantCtrl);
			if(segmentView != null) {
				mainVC.put("segmentCmp", participantCtrl.getInitialComponent());
				segmentView.select(previewLink);
				segmentPrefs.setSegment(ureq, CourseNodeSegment.preview, segmentView, saveSegmentPref);
			} else {
				mainVC.put("participantCmp", participantCtrl.getInitialComponent());
			}
		}
	}
	
	private void doNextSerie(UserRequest ureq, PlayMode playMode) {
		if(playMode != PlayMode.freeShuffle && playMode != PlayMode.incorrectQuestions && playMode != PlayMode.newQuestions) {
			return;
		}
		
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(getIdentity(), courseEntry, courseNode);
		searchParams.setPlayMode(playMode);

		int questionPerSeries = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_QUESTIONS_PER_SERIE, 10);
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			doStartPractice(ureq, playMode, items);
		}
	}
	
	private void doStartPractice(UserRequest ureq, PlayMode playMode, List<PracticeItem> items) {
		cleanUpSerie();
		
		boolean countSerieInChallenge = playMode == PlayMode.freeShuffle;
		serieCtrl = new PracticeController(ureq, getWindowControl(),
				courseEntry, courseNode, items, playMode, userCourseEnv, countSerieInChallenge, authorMode);
		listenTo(serieCtrl);
		mainVC.put("practiceCmp", serieCtrl.getInitialComponent());
	}

	private void doComposeSerie(UserRequest ureq) {
		cleanUpSerie();

		composeSerieCtrl = new PracticeComposeSerieController(ureq, getWindowControl(), courseEntry, courseNode);
		listenTo(composeSerieCtrl);
		
		composerPanel = new BreadcrumbedStackedPanel("composer", getTranslator(), this);
		composerPanel.setInvisibleCrumb(0);
		composerPanel.pushController(translate("crumb.overview"), this);
		composerPanel.pushController(translate("crumb.compose"), composeSerieCtrl);
		mainVC.put("practiceCmp", composerPanel);
	}

}
