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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.course.assessment.AssessmentEvents;
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
	private SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	private TooledStackedPanel coachPanel;
	
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
		authorMode = userCourseEnv.isCoach() || userCourseEnv.isAdmin();
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
			
			doOpenCoach(ureq);
		} else {
			doOpenParticipant(ureq);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == coachLink) {
					doOpenCoach(ureq);
				} else if (clickedLink == previewLink) {
					doOpenParticipant(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(participantCtrl == source || composeSerieCtrl == source) {
			if(event instanceof StartPracticeEvent) {
				StartPracticeEvent spe = (StartPracticeEvent)event;
				doStartPractice(ureq, spe.getPlayMode(), spe.getItems());
			} else if(event instanceof ComposeSerieEvent) {
				doComposeSerie(ureq);
			}
		} else if(serieCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				cleanUpSerie();
			} else if(event instanceof OverviewEvent) {
				cleanUpSerie();
				participantCtrl.reload();
			} else if(event instanceof NextSerieEvent) {
				doNextSerie(ureq, ((NextSerieEvent)event).getPlayMode());
			} else if(event == AssessmentEvents.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUpSerie() {
		mainVC.remove("practiceCmp");
		
		removeAsListenerAndDispose(composeSerieCtrl);
		removeAsListenerAndDispose(serieCtrl);
		composeSerieCtrl = null;
		serieCtrl = null;
	}
	
	private Activateable2 doOpenCoach(UserRequest ureq) {
		if(coachCtrl == null) {
			coachCtrl = new PracticeCoachController(ureq, getWindowControl(), coachPanel,
					courseEntry, courseNode, userCourseEnv);
			listenTo(coachCtrl);
			coachPanel.pushController(translate("segment.participants"), coachCtrl);
		} else {
			//TODO coachCtrl.reload(ureq);
		}
		addToHistory(ureq, coachCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", coachPanel);
		}
		return coachCtrl;
	}
	
	private void doOpenParticipant(UserRequest ureq) {
		participantCtrl = new PracticeParticipantController(ureq, getWindowControl(),
				courseEntry, courseNode, userCourseEnv);
		listenTo(participantCtrl);

		addToHistory(ureq, participantCtrl);
		if(segmentView != null) {
			mainVC.put("segmentCmp", participantCtrl.getInitialComponent());
		} else {
			mainVC.put("participantCmp", participantCtrl.getInitialComponent());
		}
	}
	
	private void doNextSerie(UserRequest ureq, PlayMode playMode) {
		if(playMode != PlayMode.freeShuffle && playMode != PlayMode.incorrectQuestions && playMode != PlayMode.newQuestions) {
			return;
		}
		
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(getIdentity(), courseEntry, courseNode);
		searchParams.setPlayMode(playMode);
		
		List<PracticeResource> resources = practiceService.getResources(courseEntry, courseNode.getIdent());
		int questionPerSeries = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_QUESTIONS_PER_SERIE, 20);
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			doStartPractice(ureq, playMode, items);
		}
	}
	
	private void doStartPractice(UserRequest ureq, PlayMode playMode, List<PracticeItem> items) {
		cleanUpSerie();
		
		serieCtrl = new PracticeController(ureq, getWindowControl(),
				courseEntry, courseNode, items, playMode, userCourseEnv, authorMode);
		listenTo(serieCtrl);
		mainVC.put("practiceCmp", serieCtrl.getInitialComponent());
	}

	private void doComposeSerie(UserRequest ureq) {
		cleanUpSerie();

		composeSerieCtrl = new PracticeComposeSerieController(ureq, getWindowControl(), courseEntry, courseNode);
		listenTo(composeSerieCtrl);
		mainVC.put("practiceCmp", composeSerieCtrl.getInitialComponent());
	}

}
