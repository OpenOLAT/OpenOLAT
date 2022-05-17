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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.course.nodes.practice.ui.events.ComposeSerieEvent;
import org.olat.course.nodes.practice.ui.events.StartPracticeEvent;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeParticipantController extends FormBasicController {
	
	private FormLink playButton;
	private FormLink newQuestionsButton;
	private FormLink errorsButton;
	private FormLink customButton;
	
	private final boolean rankList;
	private final int questionPerSeries;
	private final int seriesPerChallenge;
	private final int challengesToComplete;
	private int numOfErrors = 2;
	private List<AssessmentTestSession> series;
	private final List<PracticeResource> resources;
	private final UserCourseEnvironment userCourseEnv;
	
	private RepositoryEntry courseEntry;
	private PracticeCourseNode courseNode;
	
	private PracticeRankListController rankListCtrl;
	private PracticeParticipantStatisticsController statisticsCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PracticeService practiceService;
	
	public PracticeParticipantController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, PracticeCourseNode courseNode,
			UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl, "practice_participant");
		
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		rankList = courseNode.getModuleConfiguration().getBooleanSafe(PracticeEditController.CONFIG_KEY_RANK_LIST, false);
		questionPerSeries = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_QUESTIONS_PER_SERIE, 20);
		seriesPerChallenge = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_SERIE_PER_CHALLENGE, 1);
		challengesToComplete = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_CHALLENGES_FOR_COMPLETION, 1);
		
		series = practiceService.getSeries(getIdentity(), courseEntry, courseNode.getIdent());
		resources = practiceService.getResources(courseEntry, courseNode.getIdent());
		
		initForm(ureq);
		load(series);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			String welcomeTitle;
			if(ureq.getUserSession().getRoles().isGuestOnly()) {
				welcomeTitle = translate("welcome.title.guest");
			} else {
				String[] args = new String[] {
					getIdentity().getUser().getFirstName(), getIdentity().getUser().getLastName(),
					userManager.getUserDisplayName(getIdentity())
				};
				welcomeTitle = translate("welcome.title", args);
			}
			layoutCont.contextPut("welcomeTitle", welcomeTitle);
			
			String playShuffleDesc = translate("play.shuffle.desc", Integer.toString(questionPerSeries));
			layoutCont.contextPut("playShuffleDesc", playShuffleDesc);
		}
		
		if(rankList) {
			rankListCtrl = new PracticeRankListController(ureq, getWindowControl(), mainForm,
					courseEntry, courseNode, userCourseEnv);
			listenTo(rankListCtrl);
			formLayout.add("rankList", rankListCtrl.getInitialFormItem());
		}
		
		statisticsCtrl = new PracticeParticipantStatisticsController(ureq, getWindowControl(),
				courseEntry, courseNode, resources, series, mainForm);
		listenTo(statisticsCtrl);
		formLayout.add("statistics", statisticsCtrl.getInitialFormItem());
		
		playButton = uifactory.addFormLink("play", formLayout, Link.BUTTON);
		playButton.setElementCssClass("btn btn-primary");
		playButton.setIconRightCSS("o_icon o_icon_start");
		newQuestionsButton = uifactory.addFormLink("play.new.questions", formLayout, Link.BUTTON);
		newQuestionsButton.setIconRightCSS("o_icon o_icon_start");
		errorsButton = uifactory.addFormLink("play.errors", formLayout, Link.BUTTON);
		errorsButton.setIconRightCSS("o_icon o_icon_start");
		customButton = uifactory.addFormLink("play.custom", formLayout, Link.BUTTON);
		customButton.setIconRightCSS("o_icon o_icon_start");
	}
	
	protected void reload() {
		series = practiceService.getSeries(getIdentity(), courseEntry, courseNode.getIdent());
		load(series);
	}
	
	protected void load(List<AssessmentTestSession> seriesList) {
		this.series = seriesList;
		loadStatistics();
		if(statisticsCtrl != null) {
			statisticsCtrl.loadStatistics(seriesList);
		}
	}
	
	protected void loadStatistics() {
		// Errors
		String errorsI18nKey = numOfErrors > 1 ? "play.errors.desc.plural" : "play.errors.desc.singular";
		String errorsDesc = translate(errorsI18nKey, Integer.toString(numOfErrors));
		flc.contextPut("errorsDesc", errorsDesc);

		// Block to counter if the max. number of series is completed
		int completedSeries = Math.min(series.size(), (seriesPerChallenge * challengesToComplete));
		int currentNumOfSeries = completedSeries % seriesPerChallenge;

		// Challenges
		int completedChallenges = (completedSeries - currentNumOfSeries) / seriesPerChallenge;
		completedChallenges = Math.min(completedChallenges, challengesToComplete);
		String challengeProgress = translate("challenge.progress",
				Integer.toString(completedChallenges), Integer.toString(challengesToComplete));
		flc.contextPut("challengeProgress", challengeProgress);
		
		// Series
		String currentSeriesI18n = seriesPerChallenge > 1 ? "current.series.plural" : "current.series.singular";
		String currentSeriesStr = Integer.toString(currentNumOfSeries);
		// check if the user completed the challenges
		boolean ended = currentNumOfSeries == 0 && completedSeries >= (seriesPerChallenge * challengesToComplete);
		if(ended) {
			currentSeriesStr = Integer.toString(seriesPerChallenge);
		}
		String currentSeries = translate(currentSeriesI18n, currentSeriesStr, Integer.toString(seriesPerChallenge));
		flc.contextPut("currentSeries", currentSeries);
		
		double currentSeriesProgress = 0.0d;
		if(ended) {
			currentSeriesProgress = 100.0d;
		} else if(currentNumOfSeries > 0) {
			currentSeriesProgress = (currentNumOfSeries / (double)seriesPerChallenge) * 100.0d;
		}
		
		flc.contextPut("currentSeriesProgress", Double.valueOf(currentSeriesProgress));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(statisticsCtrl == source) {
			if(event instanceof StartPracticeEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(playButton == source) {
			doPlayShuffled(ureq);
		} else if(newQuestionsButton == source) {
			doNewQuestions(ureq);
		} else if(errorsButton == source) {
			doErrorQuestions(ureq);
		} else if(customButton == source) {
			fireEvent(ureq, new ComposeSerieEvent());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doPlayShuffled(UserRequest ureq) {
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(getIdentity(), courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.freeShuffle);
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			fireEvent(ureq, new StartPracticeEvent(PlayMode.freeShuffle, items));
		}
	}

	private void doNewQuestions(UserRequest ureq) {
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(getIdentity(), courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.newQuestions);
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			fireEvent(ureq, new StartPracticeEvent(PlayMode.newQuestions, items));
		}
	}
	
	private void doErrorQuestions(UserRequest ureq) {
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(getIdentity(), courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.incorrectQuestions);
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			fireEvent(ureq, new StartPracticeEvent(PlayMode.incorrectQuestions, items));
		}
	}
}
