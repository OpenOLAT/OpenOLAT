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
import java.util.HashMap;
import java.util.HashSet;
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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.PracticeFilterRule;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.course.nodes.practice.ui.PracticeParticipantTaxonomyStatisticsTableModel.TaxonomyStatisticsCols;
import org.olat.course.nodes.practice.ui.events.StartPracticeEvent;
import org.olat.course.nodes.practice.ui.renders.LevelBarsCellRenderer;
import org.olat.course.nodes.practice.ui.renders.LevelNumbersCellRenderer;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeParticipantStatisticsController extends FormBasicController {
	
	private PracticeCourseNode courseNode;
	private List<AssessmentTestSession> series;
	private final List<PracticeResource> resources;
	private final List<PracticeItem> allItems;
	private final Identity practicingIdentity;
	private final RepositoryEntry courseEntry;
	private final int numOfLevels;
	
	private FlexiTableElement taxonomyTableEl;
	private PracticeParticipantTaxonomyStatisticsTableModel taxonomyTableModel;
	
	@Autowired
	private PracticeService practiceService;
	
	public PracticeParticipantStatisticsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, PracticeCourseNode courseNode, Identity practicingIdentity,
			List<PracticeResource> cachedResources, List<PracticeItem> cachedItems) {
		super(ureq, wControl, "practice_participant_statistics");
		
		this.courseNode = courseNode;
		this.courseEntry = courseEntry;
		this.practicingIdentity = practicingIdentity;
		if(cachedResources == null) {
			resources = practiceService.getResources(courseEntry, courseNode.getIdent());
		} else {
			resources = List.copyOf(cachedResources);
		}
		numOfLevels = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_LEVELS, 3);
		
		initForm(ureq);
		
		List<AssessmentTestSession> seriesList = practiceService.getSeries(practicingIdentity, courseEntry, courseNode.getIdent());
		loadStatistics(seriesList);
		
		if(cachedItems == null) {
			SearchPracticeItemParameters searchParams = new SearchPracticeItemParameters();
			allItems = practiceService.generateItems(resources, searchParams, -1, getLocale());
		} else {
			allItems = List.copyOf(cachedItems);
		}
		loadItemStatistics(allItems);
	}
	
	public PracticeParticipantStatisticsController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			PracticeCourseNode courseNode, List<PracticeResource> resources, List<AssessmentTestSession> series,
			Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "practice_participant_statistics", rootForm);
		this.courseNode = courseNode;
		this.resources = resources;
		this.courseEntry = courseEntry;
		practicingIdentity = getIdentity();
		numOfLevels = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_LEVELS, 3);

		initForm(ureq);
		loadStatistics(series);
		
		SearchPracticeItemParameters searchParams = new SearchPracticeItemParameters();
		allItems = practiceService.generateItems(resources, searchParams, -1, getLocale());
		loadItemStatistics(allItems);
	}
	
	public List<PracticeResource> getResources() {
		return resources;
	}
	
	public List<PracticeItem> getPracticeItems() {
		return allItems;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyStatisticsCols.taxonomyLevel));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyStatisticsCols.bars,
				new LevelBarsCellRenderer()));
		
		DefaultFlexiColumnModel numbersCol = new DefaultFlexiColumnModel(TaxonomyStatisticsCols.numbers,
				new LevelNumbersCellRenderer());
		numbersCol.setHeaderLabel("&nbsp;");
		numbersCol.setHeaderTooltip(translate(TaxonomyStatisticsCols.numbers.i18nHeaderKey()));
		columnsModel.addFlexiColumnModel(numbersCol);
		
		if(practicingIdentity.equals(getIdentity())) {
			DefaultFlexiColumnModel playCol = new DefaultFlexiColumnModel("play", -1, "play",
					new StaticFlexiCellRenderer("", "play", "o_practice_play", "o_icon o_icon_start", null));
			columnsModel.addFlexiColumnModel(playCol);
		}
		
		taxonomyTableModel = new PracticeParticipantTaxonomyStatisticsTableModel(columnsModel);
		taxonomyTableEl = uifactory.addTableElement(getWindowControl(), "taxonomy.table", taxonomyTableModel, 20, false, getTranslator(), formLayout);
		taxonomyTableEl.setNumOfRowsEnabled(false);
		taxonomyTableEl.setCustomizeColumns(false);
	}
	
	protected void loadStatistics(List<AssessmentTestSession> seriesList) {
		series = List.copyOf(seriesList);

		int seriesPerChallenge = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_SERIE_PER_CHALLENGE, 2);
		int challengesToComplete = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_CHALLENGES_FOR_COMPLETION, 2);
		
		int numOfSeries = series.size();
		flc.contextPut("series", Integer.valueOf(numOfSeries));
		
		int currentNumOfSeries = series.size() % seriesPerChallenge;
		int completedChallenges = Math.min(challengesToComplete, (numOfSeries - currentNumOfSeries) / seriesPerChallenge);
		flc.contextPut("challenges", Integer.valueOf(completedChallenges));
		
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
	
	protected void loadItemStatistics(List<PracticeItem> items) {
		
		flc.contextPut("numOfLevels", numOfLevels);
		
		List<PracticeAssessmentItemGlobalRef> globalRefs = practiceService.getPracticeAssessmentItemGlobalRefs(items, practicingIdentity);
		Map<String,PracticeAssessmentItemGlobalRef> globalRefsMap = globalRefs.stream()
				.collect(Collectors.toMap(PracticeAssessmentItemGlobalRef::getIdentifier, ref -> ref, (u, v) -> u));
		
		final Levels globalLevels = new Levels(numOfLevels);
		final Map<TaxonomyLevel, PracticeParticipantTaxonomyStatisticsRow> levelMaps = new HashMap<>();
		
		long numOfLastAttempt = 0l;
		long correctLastAttempt = 0l;
		
		Set<String> duplicates = new HashSet<>();
		for(PracticeItem item:items) {
			String identifier = item.getIdentifier();
			if(identifier == null || duplicates.contains(identifier)) {
				continue;
			}
			
			PracticeAssessmentItemGlobalRef globalRef = globalRefsMap.get(identifier);
			globalLevels.append(globalRef);
			duplicates.add(identifier);
			
			TaxonomyLevel taxonomyLevel = item.getTaxonomyLevel();
			if(taxonomyLevel != null) {
				PracticeParticipantTaxonomyStatisticsRow row = levelMaps
						.computeIfAbsent(taxonomyLevel, level -> new PracticeParticipantTaxonomyStatisticsRow(level, numOfLevels));
				row.getLevels().append(globalRef);
			}
			
			if(globalRef != null && globalRef.getLastAttempts() != null) {
				numOfLastAttempt++;
				if(globalRef.getLastAttemptsPassed() != null && globalRef.getLastAttemptsPassed().booleanValue()) {
					correctLastAttempt++;
				}
			}
		}

		flc.contextPut("globalLevels", globalLevels);
		
		PieChartElement chartEl = new PieChartElement("levels.chart");
		chartEl.setLayer(20);
		chartEl.addPoints(new PiePoint(globalLevels.getNotPercent(), globalLevels.getColor(0)));
		for(int i=1; i<=numOfLevels; i++) {
			chartEl.addPoints(new PiePoint(globalLevels.getLevelPercent(i), globalLevels.getColor(i)));
		}
		flc.add("levels.chart", chartEl);
		
		double correctPercent = 0.0d;
		if(numOfLastAttempt > 0) {
			correctPercent = (correctLastAttempt / (double)numOfLastAttempt) * 100.0d;
		}
		flc.contextPut("correctPercent", Long.toString(Math.round(correctPercent)));
		
		List<PracticeParticipantTaxonomyStatisticsRow> levelRows = new ArrayList<>(levelMaps.values());
		taxonomyTableModel.setObjects(levelRows);
		taxonomyTableEl.reset();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(taxonomyTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("play".equals(se.getCommand())) {
					PracticeParticipantTaxonomyStatisticsRow statisticsRow = taxonomyTableModel.getObject(se.getIndex());
					doStartTaxonomyLevelMode(ureq, statisticsRow);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doStartTaxonomyLevelMode(UserRequest ureq, PracticeParticipantTaxonomyStatisticsRow statisticsRow) {
		SearchPracticeItemParameters searchParams = getSearchParams();
		searchParams.setPlayMode(PlayMode.all);
		searchParams.setExactTaxonomyLevelKey(statisticsRow.getTaxonomyLevel().getKey());
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, -1, getLocale());
		fireEvent(ureq, new StartPracticeEvent(PlayMode.all, items));
	}
	
	private SearchPracticeItemParameters getSearchParams() {
		SearchPracticeItemParameters searchParams = new SearchPracticeItemParameters();
		List<PracticeFilterRule> rules = courseNode.getModuleConfiguration()
				.getList(PracticeEditController.CONFIG_KEY_FILTER_RULES, PracticeFilterRule.class);
		searchParams.setRules(rules);
		searchParams.setIdentity(getIdentity());
		searchParams.setCourseEntry(courseEntry);
		searchParams.setSubIdent(courseNode.getIdent());
		searchParams.setExactTaxonomyLevelKey(null);
		
		return searchParams;
	}
}
