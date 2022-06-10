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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.PieChartElement;
import org.olat.core.gui.components.chart.PiePoint;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.manager.SearchPracticeItemHelper;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.course.nodes.practice.ui.PracticeParticipantTaxonomyStatisticsTableModel.TaxonomyStatisticsCols;
import org.olat.course.nodes.practice.ui.events.ComposeSerieEvent;
import org.olat.course.nodes.practice.ui.events.StartPracticeEvent;
import org.olat.course.nodes.practice.ui.renders.LevelBarsCellRenderer;
import org.olat.course.nodes.practice.ui.renders.PracticeTaxonomyCellRenderer;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
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
	private FormLink globalLevelsLink;
	private FlexiTableElement taxonomyTableEl;
	private PracticeParticipantTaxonomyStatisticsTableModel taxonomyTableModel;
	
	private final boolean rankList;
	
	private int counter = 0;
	private final int numOfLevels;
	private final int questionPerSeries;
	private final int seriesPerChallenge;
	private final int challengesToComplete;
	private final boolean includeWithoutTaxonomyLevels;

	private final List<PracticeItem> allItems;
	private List<AssessmentTestSession> series;
	private final List<PracticeResource> resources;
	private final UserCourseEnvironment userCourseEnv;
	private final boolean playable;
	private final Identity practicingIdentity;
	
	private RepositoryEntry courseEntry;
	private PracticeCourseNode courseNode;
	
	private PracticeRankListController rankListCtrl;
	private PracticeLevelsCalloutController levelsCtrl; 
	private CloseableCalloutWindowController levelsCalloutCtrl;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private PracticeService practiceService;
	
	public PracticeParticipantController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			PracticeCourseNode courseNode, UserCourseEnvironment userCourseEnv, Identity practicingIdentity,
			List<PracticeResource> cachedResources, List<PracticeItem> cachedItems) {
		super(ureq, wControl, "practice_participant");
		
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		this.practicingIdentity = practicingIdentity;
		playable = getIdentity().equals(practicingIdentity);
		rankList = courseNode.getModuleConfiguration().getBooleanSafe(PracticeEditController.CONFIG_KEY_RANK_LIST, false);
		questionPerSeries = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_QUESTIONS_PER_SERIE, 10);
		seriesPerChallenge = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_SERIE_PER_CHALLENGE, 2);
		challengesToComplete = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_CHALLENGES_FOR_COMPLETION, 2);
		numOfLevels = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_LEVELS, 3);
		includeWithoutTaxonomyLevels = courseNode.getModuleConfiguration().getBooleanSafe(PracticeEditController.CONFIG_KEY_FILTER_INCLUDE_WO_TAXONOMY_LEVELS, false);
		
		series = practiceService.getSeries(practicingIdentity, courseEntry, courseNode.getIdent());
		resources = cachedResources == null ? practiceService.getResources(courseEntry, courseNode.getIdent()) : new ArrayList<>(cachedResources);
		
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(practicingIdentity, courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.all);
		allItems = cachedItems == null ? practiceService.generateItems(resources, searchParams, -1, getLocale()) : new ArrayList<>(cachedItems);
		
		initForm(ureq);
		load(series);
	}
	
	public List<PracticeResource> getResources() {
		return resources;
		
	}
	public List<PracticeItem> getPracticeItems() {
		return allItems;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String playShuffleDesc = translate("play.shuffle.desc", Integer.toString(questionPerSeries));
			layoutCont.contextPut("playShuffleDesc", playShuffleDesc);
			layoutCont.contextPut("playable", Boolean.valueOf(playable));
		}
		
		if(rankList && userCourseEnv != null) {
			rankListCtrl = new PracticeRankListController(ureq, getWindowControl(), mainForm,
					courseEntry, courseNode, userCourseEnv);
			listenTo(rankListCtrl);
			formLayout.add("rankList", rankListCtrl.getInitialFormItem());
		}
		
		globalLevelsLink = uifactory.addFormLink("global.levels", "0", null, flc, Link.LINK | Link.NONTRANSLATED);
		
		initPractiseStarters(formLayout);
		initSubjectStatistics(formLayout);
	}
	
	private void initPractiseStarters(FormItemContainer formLayout) {
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
	
	private void initSubjectStatistics(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyStatisticsCols.taxonomyLevel,
				new PracticeTaxonomyCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyStatisticsCols.bars,
				new LevelBarsCellRenderer()));
		
		DefaultFlexiColumnModel numbersCol = new DefaultFlexiColumnModel(TaxonomyStatisticsCols.numbers);
		numbersCol.setHeaderLabel("&nbsp;");
		numbersCol.setHeaderTooltip(translate(TaxonomyStatisticsCols.numbers.i18nHeaderKey()));
		columnsModel.addFlexiColumnModel(numbersCol);
		
		if(practicingIdentity.equals(getIdentity())) {
			DefaultFlexiColumnModel playCol = new DefaultFlexiColumnModel("play", -1, "play",
					new StaticFlexiCellRenderer("", "play", "o_practice_play", "o_icon o_icon_start", null));
			columnsModel.addFlexiColumnModel(playCol);
		}
		
		taxonomyTableModel = new PracticeParticipantTaxonomyStatisticsTableModel(columnsModel, getLocale());
		taxonomyTableEl = uifactory.addTableElement(getWindowControl(), "taxonomy.table", taxonomyTableModel, 20, false, getTranslator(), formLayout);
		taxonomyTableEl.setNumOfRowsEnabled(false);
		taxonomyTableEl.setCustomizeColumns(false);
	}
	
	protected void reload() {
		series = practiceService.getSeries(practicingIdentity, courseEntry, courseNode.getIdent());
		load(series);
	}
	
	protected void load(List<AssessmentTestSession> seriesList) {
		this.series = seriesList;
		loadChallengeStatistics();
		loadItemStatistics(allItems);
	}
	
	private void loadChallengeStatistics() {
		// Block to counter if the max. number of series is completed
		final int completedSeries = PracticeHelper.completedSeries(series);
		final int currentNumOfSeries = completedSeries % seriesPerChallenge;
		flc.contextPut("series", Integer.valueOf(completedSeries));

		// Challenges
		final long completedChallenges = PracticeHelper.completedChalllenges(completedSeries, seriesPerChallenge);
		String challengeProgress = translate("challenge.progress",
				Long.toString(completedChallenges), Integer.toString(challengesToComplete));
		flc.contextPut("challengeProgress", challengeProgress);
		flc.contextPut("challenges", Long.valueOf(completedChallenges));

		// Series
		String currentSeriesI18n = seriesPerChallenge > 1 ? "current.series.plural" : "current.series.singular";
		String currentSeriesStr;
		// check if the user completed the challenges
		boolean ended = currentNumOfSeries == 0 && completedSeries >= (seriesPerChallenge * challengesToComplete);
		if(ended) {
			currentSeriesStr = Integer.toString(seriesPerChallenge);
		} else {
			currentSeriesStr = Integer.toString(currentNumOfSeries);
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
		
		long duration = 0l;
		long numOfDuration = 0l;
		for(AssessmentTestSession serie:series) {
			if(serie.getDuration() != null) {
				duration += serie.getDuration().longValue();
				numOfDuration++;
			}
		}
		
		double averageDuration = 0.0d;
		if(numOfDuration > 0) {
			averageDuration = duration / (double)numOfDuration;
		}
		String formattedAverageDuration = Formatter.formatDuration(Math.round(averageDuration));
		flc.contextPut("averageDuration", formattedAverageDuration);
	}
	
	private void loadItemStatistics(List<PracticeItem> items) {
		
		flc.contextPut("numOfLevels", numOfLevels);
		flc.contextPut("numOfQuestions", items.size());
		
		List<PracticeAssessmentItemGlobalRef> globalRefs = practiceService
				.getPracticeAssessmentItemGlobalRefs(items, practicingIdentity);
		Map<String,PracticeAssessmentItemGlobalRef> globalRefsMap = globalRefs.stream()
				.collect(Collectors.toMap(PracticeAssessmentItemGlobalRef::getIdentifier, ref -> ref, (u, v) -> u));
		
		long numOfLastAttempt = 0l;
		long correctLastAttempt = 0l;
		final Levels globalLevels = new Levels(numOfLevels);
		
		List<Long> selectedLevels = courseNode.getModuleConfiguration().getList(PracticeEditController.CONFIG_KEY_FILTER_TAXONOMY_LEVELS, Long.class);
		final Map<String, PracticeParticipantTaxonomyStatisticsRow> levelMaps = new HashMap<>();
		boolean withSpecifiedTaxonomy = selectedLevels != null && !selectedLevels.isEmpty();
		if(withSpecifiedTaxonomy) {
			List<TaxonomyLevel> levels = taxonomyService.getTaxonomyLevelsByKeys(selectedLevels);
			for(TaxonomyLevel level:levels) {
				putTaxonomyLevelInMap(level, levelMaps);
			}
		}
		flc.contextPut("withLevels", !selectedLevels.isEmpty());

		PracticeParticipantTaxonomyStatisticsRow withoutTaxonomyLevelRow
			= new PracticeParticipantTaxonomyStatisticsRow(translate("wo.taxonomy.level.label"), numOfLevels);

		Set<String> duplicates = new HashSet<>();
		for(PracticeItem item:items) {
			String identifier = item.getIdentifier();
			if(identifier == null || duplicates.contains(identifier)) {
				continue;
			}
			
			PracticeAssessmentItemGlobalRef globalRef = globalRefsMap.get(identifier);
			globalLevels.append(globalRef);
			duplicates.add(identifier);
			
			String taxonomyLevel = SearchPracticeItemHelper.buildKeyOfTaxonomicPath(item.getTaxonomyLevelName(), item.getTaxonomicPath());
			if(taxonomyLevel != null) {
				PracticeParticipantTaxonomyStatisticsRow row = levelMaps.get(taxonomyLevel);
				if(row != null) {
					row.getLevels().append(globalRef);
				} /*else if(!withSpecifiedTaxonomy && item.getTaxonomyLevelName() != null) {
					// putTaxonomyLevelInMap(row.getTaxonomyLevel(), levelMaps).getLevels().append(globalRef);
				} */ else {
					withoutTaxonomyLevelRow.getLevels().append(globalRef);
				}
			} else {
				withoutTaxonomyLevelRow.getLevels().append(globalRef);
			}
			
			if(globalRef != null && globalRef.getLastAttempts() != null) {
				numOfLastAttempt++;
				if(globalRef.getLastAttemptsPassed() != null && globalRef.getLastAttemptsPassed().booleanValue()) {
					correctLastAttempt++;
				}
			}
		}

		
		flc.contextPut("globalLevels", globalLevels);
		globalLevelsLink.setI18nKey(Integer.toString(globalLevels.getTotal()));
		globalLevelsLink.setUserObject(globalLevels);
		flc.add("global.levels", globalLevelsLink);
		
		PieChartElement chartEl = new PieChartElement("levels.chart");
		chartEl.setElementCssClass("o_practice_piechart");
		chartEl.setLayer(20);
		chartEl.setTitle(Integer.toString(globalLevels.getTotal()));
		chartEl.setSubTitle(translate("chart.title"));
		for(int i=1; i<=numOfLevels; i++) {
			chartEl.addPoints(new PiePoint(globalLevels.getLevelPercent(i), globalLevels.getCssClass(i)));
		}
		chartEl.addPoints(new PiePoint(globalLevels.getNotPercent(), globalLevels.getCssClass(0)));
		flc.add("levels.chart", chartEl);
		flc.contextPut("progressI18n", getIdentity().equals(practicingIdentity) ? "progress.chart.title.your" : "progress.chart.title");
		
		double correctPercent = 0.0d;
		if(numOfLastAttempt > 0) {
			correctPercent = (correctLastAttempt / (double)numOfLastAttempt) * 100.0d;
		}
		flc.contextPut("correctPercent", Long.toString(Math.round(correctPercent)));
		
		List<PracticeParticipantTaxonomyStatisticsRow> levelRows = new ArrayList<>(levelMaps.values());
		aggregate(levelRows);
		if(includeWithoutTaxonomyLevels) {
			levelRows.add(withoutTaxonomyLevelRow);
		}
		addCalloutLevelsLinks(levelRows);
		

		taxonomyTableModel.setObjects(levelRows);
		taxonomyTableEl.sort(TaxonomyStatisticsCols.taxonomyLevel.name(), true);
		taxonomyTableEl.reset();
	}
	
	private PracticeParticipantTaxonomyStatisticsRow putTaxonomyLevelInMap(TaxonomyLevel level,
			Map<String, PracticeParticipantTaxonomyStatisticsRow> levelMaps) {
		List<String> keys = SearchPracticeItemHelper.buildKeyOfTaxonomicPath(level);
		PracticeParticipantTaxonomyStatisticsRow row = new PracticeParticipantTaxonomyStatisticsRow(level, numOfLevels);
		for(String key:keys) {
			levelMaps.put(key, row);
		}
		return row;
	}
	
	private void addCalloutLevelsLinks(List<PracticeParticipantTaxonomyStatisticsRow> levelRows) {
		for(PracticeParticipantTaxonomyStatisticsRow levelRow:levelRows) {
			Levels levels = levelRow.getLevels();
			FormLink calloutLink = uifactory.addFormLink("levels_callout_" + (counter++), Integer.toString(levels.getTotal()),
					null, flc, Link.LINK | Link.NONTRANSLATED);
			levelRow.setLevelsLink(calloutLink);
			calloutLink.setUserObject(levels);
		}
	}
	
	private void aggregate(List<PracticeParticipantTaxonomyStatisticsRow> levelRows) {
		List<PracticeParticipantTaxonomyStatisticsRow> parentCopyLevelRows = new ArrayList<>(levelRows);
		Collections.sort(levelRows, new TaxonomyPathComparator());
		for(Iterator<PracticeParticipantTaxonomyStatisticsRow> itRows=levelRows.iterator(); itRows.hasNext(); ) {
			PracticeParticipantTaxonomyStatisticsRow row = itRows.next();
			if(row.getAggregatedLevels() != null && !row.getAggregatedLevels().isEmpty()) {
				continue;
			}
			
			int numOfQuestions = row.getLevels().getTotal();
			if(numOfQuestions < questionPerSeries) {
				PracticeParticipantTaxonomyStatisticsRow parent = getParentRow(row.getTaxonomyLevel(), parentCopyLevelRows);
				if(parent != null) {
					parent.appendRow(row);
					itRows.remove();
				}	
			}
		}
		
		for(Iterator<PracticeParticipantTaxonomyStatisticsRow> itRows=levelRows.iterator(); itRows.hasNext(); ) {
			if(itRows.next().isEmpty()) {
				itRows.remove();
			}
		}
	}
	
	private PracticeParticipantTaxonomyStatisticsRow getParentRow(TaxonomyLevel taxonomyLevel, List<PracticeParticipantTaxonomyStatisticsRow> levelRows) {
		TaxonomyLevel parentLevel = taxonomyLevel.getParent();
		for(PracticeParticipantTaxonomyStatisticsRow levelRow:levelRows) {
			if(levelRow.getTaxonomyLevel().equals(parentLevel)) {
				return levelRow;
			}
		}
		return null;
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
		} else if(taxonomyTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("play".equals(se.getCommand())) {
					PracticeParticipantTaxonomyStatisticsRow statisticsRow = taxonomyTableModel.getObject(se.getIndex());
					doStartTaxonomyLevelMode(ureq, statisticsRow);
				}
			}
		} else if(globalLevelsLink == source) {
			doOpenLevelsCallout(ureq, globalLevelsLink.getFormDispatchId(), (Levels)globalLevelsLink.getUserObject());
		} else if(source instanceof FormLink && ((FormLink)source).getUserObject() instanceof Levels) {
			FormLink calloutLink = (FormLink)source;
			doOpenLevelsCallout(ureq, calloutLink.getFormDispatchId(), (Levels)calloutLink.getUserObject());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doPlayShuffled(UserRequest ureq) {
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(practicingIdentity, courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.freeShuffle);
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			fireEvent(ureq, new StartPracticeEvent(PlayMode.freeShuffle, items));
		}
	}

	private void doNewQuestions(UserRequest ureq) {
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(practicingIdentity, courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.newQuestions);
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			fireEvent(ureq, new StartPracticeEvent(PlayMode.newQuestions, items));
		}
	}
	
	private void doErrorQuestions(UserRequest ureq) {
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(practicingIdentity, courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.incorrectQuestions);
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			fireEvent(ureq, new StartPracticeEvent(PlayMode.incorrectQuestions, items));
		}
	}
	
	private void doStartTaxonomyLevelMode(UserRequest ureq, PracticeParticipantTaxonomyStatisticsRow statisticsRow) {
		SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(practicingIdentity, courseEntry, courseNode);
		searchParams.setPlayMode(PlayMode.all);
		// Override standard taxonomy settings
		List<TaxonomyLevel> taxonomyLevels = new ArrayList<>();
		if(statisticsRow.getAggregatedLevels() != null) {
			taxonomyLevels.addAll(statisticsRow.getAggregatedLevels());
		}
		if(statisticsRow.getTaxonomyLevel() != null) {
			taxonomyLevels.add(statisticsRow.getTaxonomyLevel());
		}
		searchParams.setExactTaxonomyLevels(taxonomyLevels);
		searchParams.setIncludeWithoutTaxonomyLevel(false);
		searchParams.setDescendantsLevels(null);
		
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, questionPerSeries, getLocale());
		if(items.isEmpty()) {
			showWarning("warning.no.items.found");
		} else {
			fireEvent(ureq, new StartPracticeEvent(PlayMode.all, items));
		}
	}
	
	private void doOpenLevelsCallout(UserRequest ureq, String elementId, Levels levels) {
		levelsCtrl = new PracticeLevelsCalloutController(ureq, getWindowControl(), levels);
		listenTo(levelsCtrl);

		levelsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				levelsCtrl.getInitialComponent(), elementId, "", true, "");
		listenTo(levelsCalloutCtrl);
		levelsCalloutCtrl.activate();
	}
	
	private class TaxonomyPathComparator implements Comparator<PracticeParticipantTaxonomyStatisticsRow> {

		@Override
		public int compare(PracticeParticipantTaxonomyStatisticsRow o1, PracticeParticipantTaxonomyStatisticsRow o2) {
			int p1 = length(o1);
			int p2 = length(o2);
			return Integer.compare(p2, p1);
		}
		
		private int length(PracticeParticipantTaxonomyStatisticsRow o) {
			return o == null || o.getTaxonomyPath() == null ? 0 : o.getTaxonomyPath().size();
		}
	}
}
