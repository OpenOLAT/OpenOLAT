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
package org.olat.course.editor.overview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.duedate.ui.DueDateConfigCellRenderer;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.SelectEvent;
import org.olat.course.editor.overview.OverviewDataModel.OverviewCols;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.LearningPathTranslations;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewListController extends FormBasicController implements FlexiTableCssDelegate {
	
	private static final String CMD_OPEN = "open";
	private static final String ALL_TAB_ID = "All";
	private static final String ASSESSABLE_TAB_ID = "Assessable";
	private static final String FILTER_ASSESSABLE = "assessable";
	private static final String FILTER_INCLUDE_IN_COURSE_ASSESSMENT = "in-course-assessment";
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab assessableTab;
	private FlexiTableElement tableEl;
	private OverviewDataModel dataModel;
	private FlexiTableOneClickSelectionFilter assessableFilter;
	private FlexiTableOneClickSelectionFilter includeInCourseAssessmentFilter;
	private FormLink bulkLink;

	private CloseableModalController cmc;
	private BulkChangeController bulkChangeCtrl;
	private EditScoreScalingController editScoreScalingctrl;
	private CloseableCalloutWindowController scoreScalingCalloutCtrl;
	
	private int counter = 0;
	private ICourse course;
	private final Model usedModel;
	private final OverviewListOptions listOptions;
	private final boolean learningPath;
	private final boolean scoreScalingEnabled;
	private final boolean ignoreInCourseAssessmentAvailable;
	
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private NodeAccessService nodeAccessService;

	public OverviewListController(UserRequest ureq, WindowControl wControl, ICourse course, Model usedModel, OverviewListOptions listOptions) {
		super(ureq, wControl, "overview_list");
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		this.course = course;
		this.usedModel = usedModel;
		this.listOptions = listOptions;
		this.learningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType());
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(NodeAccessType.of(course));
		scoreScalingEnabled = ScoreScalingHelper.isEnabled(course);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTable(ureq, formLayout);
		initButtons(formLayout);
	}

	private void initTable(UserRequest ureq, FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer, CMD_OPEN);
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(OverviewCols.node, CMD_OPEN, nodeRenderer);
		nodeModel.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nodeModel);
		DefaultFlexiColumnModel hintsModel = new DefaultFlexiColumnModel(OverviewCols.hints);
		hintsModel.setCellRenderer(new HintsCellRenderer());
		hintsModel.setExportable(false);
		columnsModel.addFlexiColumnModel(hintsModel);
		DefaultFlexiColumnModel dirtyModel = new DefaultFlexiColumnModel(OverviewCols.dirty);
		dirtyModel.setCellRenderer(new YesNoCellRenderer());
		dirtyModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(dirtyModel);
		DefaultFlexiColumnModel newModel = new DefaultFlexiColumnModel(OverviewCols.newNode);
		newModel.setCellRenderer(new YesNoCellRenderer());
		newModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(newModel);
		DefaultFlexiColumnModel deletedModel = new DefaultFlexiColumnModel(OverviewCols.deleted);
		deletedModel.setCellRenderer(new YesNoCellRenderer());
		deletedModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(deletedModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OverviewCols.longTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(listOptions.isShowShortTitleDefault(), OverviewCols.shortTitle));
		DefaultFlexiColumnModel descriptionModel = new DefaultFlexiColumnModel(OverviewCols.description);
		descriptionModel.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.none));
		descriptionModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(descriptionModel);
		DefaultFlexiColumnModel objectivesModel = new DefaultFlexiColumnModel(OverviewCols.objectives);
		objectivesModel.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.none));
		objectivesModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(objectivesModel);
		DefaultFlexiColumnModel instructionModel = new DefaultFlexiColumnModel(OverviewCols.instruction);
		instructionModel.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.none));
		instructionModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(instructionModel);
		DefaultFlexiColumnModel instructionalDesignModel = new DefaultFlexiColumnModel(OverviewCols.instructionalDesign);
		instructionalDesignModel.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.none));
		instructionalDesignModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(instructionalDesignModel);
		DefaultFlexiColumnModel displayModel = new DefaultFlexiColumnModel(OverviewCols.display);
		displayModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(displayModel);
		
		if (learningPath) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(listOptions.isShowLearningTimeDefault(), OverviewCols.duration));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.start, new DueDateConfigCellRenderer(getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.end, new DueDateConfigCellRenderer(getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.obligation));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.trigger));
		}
		
		// Assessment configuration
		DefaultFlexiColumnModel scoreModel = new DefaultFlexiColumnModel(OverviewCols.score);
		scoreModel.setCellRenderer(new OnOffCellRenderer(getTranslator()));
		scoreModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(scoreModel);
		DefaultFlexiColumnModel scoreMinModel = new DefaultFlexiColumnModel(OverviewCols.scoreMin);
		scoreMinModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(scoreMinModel);
		DefaultFlexiColumnModel scoreMaxModel = new DefaultFlexiColumnModel(OverviewCols.scoreMax);
		scoreMaxModel.setDefaultVisible(listOptions.isShowMaxScoreDefault());
		columnsModel.addFlexiColumnModel(scoreMaxModel);
		DefaultFlexiColumnModel passedModel = new DefaultFlexiColumnModel(OverviewCols.passed);
		passedModel.setCellRenderer(new OnOffCellRenderer(getTranslator()));
		passedModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(passedModel);
		DefaultFlexiColumnModel passedCutModel = new DefaultFlexiColumnModel(OverviewCols.passesCut);
		passedCutModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(passedCutModel);
		if (ignoreInCourseAssessmentAvailable ) {
			DefaultFlexiColumnModel ignoreInCourseAssessmentModel = new DefaultFlexiColumnModel(OverviewCols.incorporateInCourseAssessment);
			if(usedModel == Model.RUN) {
				ignoreInCourseAssessmentModel.setCellRenderer(new OnOffCellRenderer(getTranslator()));
			}
			columnsModel.addFlexiColumnModel(ignoreInCourseAssessmentModel);
		}
		if (scoreScalingEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.scoreScaling));
		}
		
		DefaultFlexiColumnModel commentModel = new DefaultFlexiColumnModel(OverviewCols.comment);
		commentModel.setCellRenderer(new OnOffCellRenderer(getTranslator()));
		commentModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(commentModel);
		DefaultFlexiColumnModel individualDocumentsModel = new DefaultFlexiColumnModel(OverviewCols.individualAsssessmentDocuments);
		individualDocumentsModel.setCellRenderer(new OnOffCellRenderer(getTranslator()));
		individualDocumentsModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(individualDocumentsModel);
		
		dataModel = new OverviewDataModel(columnsModel, usedModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_course_edit_overview_table o_course_editor_legend");
		if (learningPath) {
			tableEl.setElementCssClass(tableEl.getElementCssClass() + " o_lp_edit");
		}
		tableEl.setCssDelegate(this);
		tableEl.setEmptyTableMessageKey("table.empty");
		tableEl.setExportEnabled(true);
		boolean batchAction = usedModel == Model.EDITOR;
		tableEl.setMultiSelect(batchAction);
		tableEl.setSelectAllEnable(batchAction);
		
		initFilters();
		initFiltersPresets();
		tableEl.setSelectedFilterTab(ureq, allTab);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-editor-overview");
		
		// Set some default values for the filters before loading the model
		if(listOptions.isDefaultValueAssessableFilter()) {
			tableEl.setFilterValue(assessableFilter, FILTER_ASSESSABLE);
		}
		if(listOptions.isDefaultValueIncludeInCourseAssessmentFilter()) {
			tableEl.setFilterValue(includeInCourseAssessmentFilter, FILTER_INCLUDE_IN_COURSE_ASSESSMENT);
		}

		loadModel();
	}
	
	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues assessableValues = new SelectionValues();
		assessableValues.add(SelectionValues.entry(FILTER_ASSESSABLE, translate("filter.assessable")));
		assessableFilter = new FlexiTableOneClickSelectionFilter(translate("filter.assessable"),
				FILTER_ASSESSABLE, assessableValues, true);
		filters.add(assessableFilter);
		
		SelectionValues includeValues = new SelectionValues();
		includeValues.add(SelectionValues.entry(FILTER_INCLUDE_IN_COURSE_ASSESSMENT, translate("filter.in.course.assessment")));
		includeInCourseAssessmentFilter = new FlexiTableOneClickSelectionFilter(translate("filter.in.course.assessment"),
				FILTER_INCLUDE_IN_COURSE_ASSESSMENT, includeValues, true);
		filters.add(includeInCourseAssessmentFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_elements_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		assessableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ASSESSABLE_TAB_ID, translate("filter.assessable"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ASSESSABLE, List.of(FILTER_ASSESSABLE))));
		assessableTab.setFiltersExpanded(true);
		tabs.add(assessableTab);
		
		tableEl.setFilterTabs(true, tabs);
	}

	private void loadModel() {
		INode rootNode;
		if(usedModel == Model.EDITOR) {
			rootNode = course.getEditorTreeModel().getRootNode();
		} else {
			rootNode = course.getRunStructure().getRootNode();
		}	
		List<OverviewRow> rows = new ArrayList<>();
		forgeRows(rows, rootNode, 0, null);
		filterModel(rows);
		dataModel.setObjects(rows);
		tableEl.reset(true, false, true);
	}
	
	private void filterModel(List<OverviewRow> rows) {
		boolean filterAssessable = isFilterSelected(FILTER_ASSESSABLE);
		boolean filterIncludeInCourseAssessment = isFilterSelected(FILTER_INCLUDE_IN_COURSE_ASSESSMENT);
		
		if(filterAssessable || filterIncludeInCourseAssessment) {
			Set<FlexiTreeTableNode> toRetains = new HashSet<>();
			
			for(OverviewRow row:rows) {
				if((!filterAssessable || row.getAssessmentConfig().isAssessable())
						&& (!filterIncludeInCourseAssessment || acceptIncludeInCourseAssessment(row))) {
					for(FlexiTreeTableNode aRow=row; aRow != null; aRow = aRow.getParent()) {
						toRetains.add(aRow);
					}
				}
			}
			
			rows.retainAll(toRetains);
		}
	}
	
	private boolean acceptIncludeInCourseAssessment(OverviewRow row) {
		Object include = dataModel.getIncorporateInCourseAssessment(row);
		if(include instanceof FormToggle fToggle) {
			return fToggle.isOn();
		}
		if(include instanceof Boolean bInclude) {
			return bInclude.booleanValue();
		}
		return false;
	}
	
	private boolean isFilterSelected(String id) {
		FlexiTableFilter assessableFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), id);
		if (assessableFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)assessableFilter).getValues();
			if(filterValues != null && filterValues.contains(id)) {
				return true;
			}
		}
		return false;
	}

	private void forgeRows(List<OverviewRow> rows, INode node, int recursionLevel, OverviewRow parent) {
		OverviewRow row;
		if (node instanceof CourseEditorTreeNode editorNode) {
			row = forgeRow(editorNode.getCourseNode(), editorNode, recursionLevel, parent);
		} else if(node instanceof CourseNode courseNode) {
			CourseEditorTreeNode editorNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
			row = forgeRow(courseNode, editorNode, recursionLevel, parent);
		} else {
			return;
		}
		
		rows.add(row);
		
		int childCount = node.getChildCount();
		for (int i = 0; i < childCount; i++) {
			INode child = node.getChildAt(i);
			forgeRows(rows, child, ++recursionLevel, row);
		}
	}

	private OverviewRow forgeRow(CourseNode courseNode, CourseEditorTreeNode editorNode, int recursionLevel, OverviewRow parent) {
		OverviewRow row = new OverviewRow(editorNode, recursionLevel);
		row.setParent(parent);
		row.setTranslatedDisplayOption(getTranslatedDisplayOption(courseNode));
		if (learningPath) {
			CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
			LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode, editorTreeNode.getParent());
			row.setDuration(learningPathConfigs.getDuration());
			row.setTranslatedObligation(getTranslatedObligation(learningPathConfigs));
			row.setStart(learningPathConfigs.getStartDateConfig());
			row.setEnd(learningPathConfigs.getEndDateConfig());
			row.setTranslatedTrigger(getTranslatedTrigger(courseNode, learningPathConfigs));
		}
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(course), courseNode);
		row.setAssessmentConfig(assessmentConfig);
		
		if(usedModel == Model.EDITOR && assessmentConfig.isAssessable() && !(courseNode instanceof STCourseNode)) {
			FormToggle incorporateInCourseAssessmentEl = uifactory.addToggleButton("inc_assessment_" + (++counter), null,
					translate("on"), translate("off"), flc);
			row.setIncorporateInCourseAssessmentEl(incorporateInCourseAssessmentEl);
			incorporateInCourseAssessmentEl.setUserObject(row);
			if(assessmentConfig.ignoreInCourseAssessment()) {
				incorporateInCourseAssessmentEl.toggleOff();
			} else {
				incorporateInCourseAssessmentEl.toggleOn();
			}
			
			String scoreScaling = getFormattedScoreScale(editorNode);
			FormLink scoreScalingEl = // uifactory.addFormLink("scal_" + (++counter), "scaling", scoreScaling, null, flc, Link.LINK | Link.NONTRANSLATED);
					uifactory.addFormLink("scal_" + (++counter), "scaling", scoreScaling, tableEl, Link.LINK | Link.NONTRANSLATED);
			scoreScalingEl.setIconRightCSS("o_icon o_icon_correction");
			row.setScoreScalingEl(scoreScalingEl);
			scoreScalingEl.setUserObject(row);
		}
		return row;
	}
	
	private String getFormattedScoreScale(CourseEditorTreeNode editorNode) {
		String scoreScaling = editorNode.getCourseNode().getModuleConfiguration()
				.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		if(scoreScaling.indexOf('/') < 0) {
			scoreScaling = translate("score.scaling.decorator", scoreScaling);
		}
		return scoreScaling;
	}

	private String getTranslatedDisplayOption(CourseNode courseNode) {
		String displayOption = courseNode.getDisplayOption();
		if (displayOption == null) return null;
		
		switch(displayOption) {
		case CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT: return translate("nodeConfigForm.title_desc_content");
		case CourseNode.DISPLAY_OPTS_TITLE_CONTENT: return translate("nodeConfigForm.title_content");
		case CourseNode.DISPLAY_OPTS_DESCRIPTION_CONTENT: return translate("nodeConfigForm.desc_content");
		case CourseNode.DISPLAY_OPTS_CONTENT: return translate("nodeConfigForm.content_only");
		default:
			// nothing
		}
		return null;
	}

	private String getTranslatedObligation(LearningPathConfigs learningPathConfigs) {
		AssessmentObligation obligation = learningPathConfigs.getObligation();
		if (obligation == null) return null;
		
		switch (obligation) {
		case mandatory: return translate("config.obligation.mandatory");
		case optional: return translate("config.obligation.optional");
		default:
			// nothing
		}
		return null;
	}

	private String getTranslatedTrigger(CourseNode courseNode, LearningPathConfigs learningPathConfigs) {
		FullyAssessedTrigger trigger = learningPathConfigs.getFullyAssessedTrigger();
		if (trigger == null) return null;
		
		switch (trigger) {
		case nodeVisited: return translate("config.trigger.visited");
		case confirmed: return translate("config.trigger.confirmed");
		case score: {
			Integer scoreTriggerValue = learningPathConfigs.getScoreTriggerValue();
			return translate("config.trigger.score.value", scoreTriggerValue.toString());
		}
		case passed: return translate("config.trigger.passed");
		case statusInReview: {
			LearningPathTranslations translations = learningPathService.getEditConfigs(courseNode).getTranslations();
			return translations.getTriggerStatusInReview(getLocale()) != null
					? translations.getTriggerStatusInReview(getLocale())
					: translate("config.trigger.status.in.review");
		}
		case statusDone: {
			LearningPathTranslations translations = learningPathService.getEditConfigs(courseNode).getTranslations();
			return translations.getTriggerStatusDone(getLocale()) != null
					? translations.getTriggerStatusDone(getLocale())
					: translate("config.trigger.status.done");
		}
		case nodeCompleted: {
			LearningPathTranslations translations = learningPathService.getEditConfigs(courseNode).getTranslations();
			return translations.getTriggerNodeCompleted(getLocale()) != null
					? translations.getTriggerNodeCompleted(getLocale())
					: translate("config.trigger.node.completed");
		}
		default:
			// nothing
		}
		return null;
	}

	private void initButtons(FormItemContainer formLayout) {
		bulkLink = uifactory.addFormLink("command.bulk", formLayout, Link.BUTTON);
		tableEl.addBatchButton(bulkLink);
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		return dataModel.getObject(pos).getEditorNode().getCssClass();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == bulkChangeCtrl) {
			if (event == Event.DONE_EVENT) {
				doFinishbulk(ureq, bulkChangeCtrl.getCourseNodes());
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(editScoreScalingctrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doSetScoreScaling(ureq, editScoreScalingctrl.getRow(), editScoreScalingctrl.getScale());
			}
			scoreScalingCalloutCtrl.deactivate();
			cleanUp();
		} else if(scoreScalingCalloutCtrl == source) {
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(scoreScalingCalloutCtrl);
		removeAsListenerAndDispose(editScoreScalingctrl);
		removeAsListenerAndDispose(bulkChangeCtrl);
		removeAsListenerAndDispose(cmc);
		scoreScalingCalloutCtrl = null;
		editScoreScalingctrl = null;
		bulkChangeCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		boolean scalingCallout = fiSrc instanceof FormLink link && "scaling".equals(link.getCmd());
		if(!scalingCallout) {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				OverviewRow row = dataModel.getObject(se.getIndex());
				if (CMD_OPEN.equals(cmd)) {
					fireEvent(ureq, new SelectEvent(row.getCourseNode()));
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		} else if(source instanceof FormToggle toggle && toggle.getUserObject() instanceof OverviewRow row) {
			doIgnoreInCourseAssessment(ureq, row, !toggle.isOn());
		} else if(source instanceof FormLink link && "scaling".equals(link.getCmd())
				&& link.getUserObject() instanceof OverviewRow row) {
			doEditScoreScaling(ureq, row);
		} else if (source == bulkLink) {
			doBulk(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doBulk(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex.isEmpty()) {
			showWarning("error.select.one.course.node");
			return;
		}
		
		List<CourseNode> selectedCourseNodes = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(OverviewRow::getCourseNode)
				.collect(Collectors.toList());
		
		removeAsListenerAndDispose(bulkChangeCtrl);
		bulkChangeCtrl = new BulkChangeController(ureq, getWindowControl(), course, selectedCourseNodes);
		listenTo(bulkChangeCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkChangeCtrl.getInitialComponent(),
				true, translate("command.bulk"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doFinishbulk(UserRequest ureq, List<CourseNode> selectedCourseNodes) {
		List<CourseEditorTreeNode> nodes = new ArrayList<>();
		for(CourseNode selectedCourseNode:selectedCourseNodes) {
			CourseEditorTreeNode treeNode = course.getEditorTreeModel().getCourseEditorNodeById(selectedCourseNode.getIdent());
			if(treeNode != null) {
				nodes.add(treeNode);
			}
		}
		fireEvent(ureq, new OverviewNodesChangedEvent(nodes));
	}
	
	private void doSetScoreScaling(UserRequest ureq, OverviewRow row, String scale) {
		if(CourseFactory.isCourseEditSessionOpen(course.getResourceableId())) {
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(course), row.getCourseNode());
			assessmentConfig.setScoreScale(scale);
			List<CourseEditorTreeNode> nodes = List.of(row.getEditorNode());
			fireEvent(ureq, new OverviewNodesChangedEvent(nodes));
			// Only update the link
			String formattedScale = getFormattedScoreScale(row.getEditorNode());
			row.getScoreScalingEl().setI18nKey(formattedScale);
		}
	}
	
	private void doIgnoreInCourseAssessment(UserRequest ureq, OverviewRow row, boolean ignoreInCourseAssessment) {
		if(CourseFactory.isCourseEditSessionOpen(course.getResourceableId())) {
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(course), row.getCourseNode());
			assessmentConfig.setIgnoreInCourseAssessment(ignoreInCourseAssessment);
			List<CourseEditorTreeNode> nodes = List.of(row.getEditorNode());
			fireEvent(ureq, new OverviewNodesChangedEvent(nodes));
			
			// Only update the link
			String formattedScale = getFormattedScoreScale(row.getEditorNode());
			row.getScoreScalingEl().setI18nKey(formattedScale);
		}
	}
	
	private void doEditScoreScaling(UserRequest ureq, OverviewRow row) {
		editScoreScalingctrl = new EditScoreScalingController(ureq, getWindowControl(), row);
		listenTo(editScoreScalingctrl);

		scoreScalingCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				editScoreScalingctrl.getInitialComponent(), row.getScoreScalingEl().getFormDispatchId(),
				"", true, "", new CalloutSettings());
		listenTo(scoreScalingCalloutCtrl);
		scoreScalingCalloutCtrl.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public enum Model {
		RUN,
		EDITOR
	}
	
	public static class OverviewListOptions {
		
		private final boolean showMaxScoreDefault;
		private final boolean showShortTitleDefault;
		private final boolean showLearningTimeDefault;
		private final boolean defaultValueAssessableFilter;
		private final boolean defaultValueIncludeInCourseAssessmentFilter;
		
		public OverviewListOptions(boolean showMaxScoreDefault, boolean showShortTitleDefault, boolean showLearningTimeDefault,
				boolean defaultValueAssessableFilter, boolean defaultValueIncludeInCourseAssessmentFilter) {
			this.showMaxScoreDefault = showMaxScoreDefault;
			this.showShortTitleDefault = showShortTitleDefault;
			this.showLearningTimeDefault = showLearningTimeDefault;
			this.defaultValueAssessableFilter = defaultValueAssessableFilter;
			this.defaultValueIncludeInCourseAssessmentFilter = defaultValueIncludeInCourseAssessmentFilter;
		}
		
		public static OverviewListOptions defaultOptions() {
			return new OverviewListOptions(false, true, true, false, false);
		}

		public boolean isShowMaxScoreDefault() {
			return showMaxScoreDefault;
		}

		public boolean isShowShortTitleDefault() {
			return showShortTitleDefault;
		}

		public boolean isShowLearningTimeDefault() {
			return showLearningTimeDefault;
		}

		public boolean isDefaultValueAssessableFilter() {
			return defaultValueAssessableFilter;
		}

		public boolean isDefaultValueIncludeInCourseAssessmentFilter() {
			return defaultValueIncludeInCourseAssessmentFilter;
		}
	}
}
