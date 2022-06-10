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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl.SelectionMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PracticeFilterRule;
import org.olat.course.nodes.practice.PracticeFilterRule.Operator;
import org.olat.course.nodes.practice.PracticeFilterRule.Type;
import org.olat.course.nodes.practice.manager.SearchPracticeItemHelper;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.PracticeResourceInfos;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.course.nodes.practice.ui.PracticeResourceTableModel.PracticeResourceCols;
import org.olat.course.nodes.practice.ui.PracticeResourceTaxonomyTableModel.PracticeTaxonomyCols;
import org.olat.course.nodes.practice.ui.renders.PracticeResourceIconFlexiCellRenderer;
import org.olat.course.nodes.practice.ui.renders.PracticeTaxonomyCellRenderer;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.ui.QuestionListController;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory.KeyValues;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.AuthorListConfiguration;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringEntryRow;
import org.olat.repository.ui.author.AuthoringEntryRowSelectionEvent;
import org.olat.repository.ui.author.AuthoringEntryRowsListSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeConfigurationController extends FormBasicController {
	
	private FormLink addTestButton;
	private FormLink addPoolButton;
	private MultipleSelectionElement taxonomyEl;
	private MultipleSelectionElement withoutTaxonomyEl;
	private SingleSelection levelEl;
	private TextElement challengeToCompleteEl;
	private SingleSelection questionPerSerieEl;
	private TextElement seriePerChallengeEl;
	private MultipleSelectionElement rankListEl;

	private FlexiTableElement resourcesTableEl;
	private PracticeResourceTableModel resourcesModel;
	private FlexiTableElement taxonomyStatisticsEl;
	private PracticeResourceTaxonomyTableModel taxonomyStatisticsModel;

	private SingleSelection addRuleEl;
	private final List<RuleElement> ruleEls = new ArrayList<>();
	private FormLayoutContainer criteriaCont;
	
	private FormLayoutContainer statisticsKeywordsCont;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmRemoveDialog;
	private AuthorListController testResourcesListCtrl;
	private SharedResourceChooserController sharesChooserCtrl;
	
	private int counter = 0;
	
	private PracticeCourseNode courseNode;
	private final RepositoryEntry courseEntry;
	private final ModuleConfiguration config;

	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private PracticeService practiceResourceService;
	
	public PracticeConfigurationController(UserRequest ureq, WindowControl wControl, ICourse course, PracticeCourseNode courseNode) {
		super(ureq, wControl, "practice_edit", Util.createPackageTranslator(QuestionListController.class, ureq.getLocale()));
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		initForm(ureq);
		loadModel();
		loadRules();
		loadStatistics();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		String resourcesListPage = velocity_root + "/resources_list.html";
		FormLayoutContainer resourcesListCont = FormLayoutContainer.createCustomFormLayout("resourcesList", getTranslator(), resourcesListPage);
		formLayout.add(resourcesListCont);
		initResourcesListForm(resourcesListCont);

		String criteriaPage = velocity_root + "/criteria_list.html";
		criteriaCont = FormLayoutContainer.createCustomFormLayout("criteria", getTranslator(), criteriaPage);
		formLayout.add(criteriaCont);
		initCriteriaForm(criteriaCont);
		
		FormLayoutContainer configurationCont = FormLayoutContainer.createDefaultFormLayout("config", getTranslator());
		formLayout.add(configurationCont);
		initConfiguration(configurationCont);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		configurationCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		
		String statsPage = velocity_root + "/statistics_keywords.html";
		statisticsKeywordsCont = FormLayoutContainer.createCustomFormLayout("stats", getTranslator(), statsPage);
		formLayout.add(statisticsKeywordsCont);
		initStatisticsForm(statisticsKeywordsCont);
	}
	
	private void initResourcesListForm(FormItemContainer formLayout) {
		addTestButton = uifactory.addFormLink("add.resource.test", formLayout, Link.BUTTON);
		addTestButton.setIconLeftCSS("o_icon o_icon_add");
		addPoolButton = uifactory.addFormLink("add.resource.pool", formLayout, Link.BUTTON);
		addPoolButton.setIconLeftCSS("o_icon o_icon_add");
		
		// Table list
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PracticeResourceCols.id));
		DefaultFlexiColumnModel iconCol = new DefaultFlexiColumnModel(PracticeResourceCols.icon,
				new PracticeResourceIconFlexiCellRenderer());
		iconCol.setHeaderLabel("&nbsp;");
		iconCol.setHeaderTooltip(translate(PracticeResourceCols.icon.i18nHeaderKey()));
		columnsModel.addFlexiColumnModel(iconCol);
		DefaultFlexiColumnModel titleCol = new DefaultFlexiColumnModel(PracticeResourceCols.title);
		columnsModel.addFlexiColumnModel(titleCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PracticeResourceCols.numOfQuestions));
		DefaultFlexiColumnModel removeCol = new DefaultFlexiColumnModel("remove", "", "remove", "o_icon o_icon_delete_item");
		removeCol.setIconHeader("o_icon o_icon_delete_item");
		removeCol.setHeaderTooltip(translate("remove"));
		columnsModel.addFlexiColumnModel(removeCol);
		
		resourcesModel = new PracticeResourceTableModel(columnsModel, getLocale());
		resourcesTableEl = uifactory.addTableElement(getWindowControl(), "resources.list", resourcesModel, 12, false, getTranslator(), formLayout);
		resourcesTableEl.setCustomizeColumns(false);
		resourcesTableEl.setNumOfRowsEnabled(false);
	}
	
	private void initCriteriaForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("criteria"));
		
		Taxonomy taxonomy = qpoolService.getQPoolTaxonomy();
		SelectionValues levelKeys;
		if(taxonomy != null) {
			List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomy);
			levelKeys = RepositoyUIFactory.createTaxonomyLevelKV(allTaxonomyLevels);
		} else {
			levelKeys = new SelectionValues();
		}
		taxonomyEl = uifactory.addCheckboxesDropdown("taxonomy.levels", "taxonomy.levels", formLayout,
				levelKeys.keys(), levelKeys.values());
		
		SelectionValues woKeys = new SelectionValues();
		woKeys.add(SelectionValues.entry("wo", translate("wo.taxonomy.levels")));
		withoutTaxonomyEl = uifactory.addCheckboxesHorizontal("wo.taxonomy.levels", "wo.taxonomy.levels", formLayout,
				woKeys.keys(), woKeys.values());
		boolean includeWoLevels = config.getBooleanSafe(PracticeEditController.CONFIG_KEY_FILTER_INCLUDE_WO_TAXONOMY_LEVELS, false);
		if(includeWoLevels) {
			withoutTaxonomyEl.select("wo", true);
		}
		
		List<Long> selectedLevels = config.getList(PracticeEditController.CONFIG_KEY_FILTER_TAXONOMY_LEVELS, Long.class);
		if(selectedLevels != null) {
			for(Long selectedLevel:selectedLevels) {
				String selectedLevelKey = selectedLevel.toString();
				if(levelKeys.containsKey(selectedLevelKey)) {
					taxonomyEl.select(selectedLevelKey, true);
				}
			}
		}

		SelectionValues additionalTypeKeys = getRuleTypes();
		addRuleEl = uifactory.addDropdownSingleselect("add.rule.type", formLayout,
				additionalTypeKeys.keys(), additionalTypeKeys.values());
		addRuleEl.enableNoneSelection(translate("add.rule.select"));
		addRuleEl.addActionListener(FormEvent.ONCHANGE);
		
		formLayout.contextPut("rules", ruleEls);
	}
	
	private SelectionValues getRuleTypes() {
		SelectionValues additionalTypeKeys = new SelectionValues();
		additionalTypeKeys.add(SelectionValues.entry(PracticeFilterRule.Type.assessmentType.name(), translate("crit.assessment.type")));
		additionalTypeKeys.add(SelectionValues.entry(PracticeFilterRule.Type.keyword.name(), translate("crit.keyword")));
		additionalTypeKeys.add(SelectionValues.entry(PracticeFilterRule.Type.language.name(), translate("crit.language")));
		additionalTypeKeys.add(SelectionValues.entry(PracticeFilterRule.Type.educationalContextLevel.name(), translate("crit.educational.context.level")));
		return additionalTypeKeys;
	}
	
	private RuleElement initRuleForm(FormLayoutContainer rulesCont, PracticeFilterRule rule) {
		String partId = Integer.toString(++counter);
		SelectionValues ruleKeys = getRuleTypes();
		SingleSelection typeEl = uifactory.addDropdownSingleselect("rule.type.".concat(partId), null, rulesCont, ruleKeys.keys(), ruleKeys.values(), null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String type = rule.getType().name();
		for(String typeKey : typeEl.getKeys()) {
			if(type.equals(typeKey)) {
				typeEl.select(typeKey, true);
			}
		}
		
		SelectionValues operatorKeys = new SelectionValues();
		operatorKeys.add(SelectionValues.entry(PracticeFilterRule.Operator.equals.name(), translate("operator.equal")));
		operatorKeys.add(SelectionValues.entry(PracticeFilterRule.Operator.notEquals.name(), translate("operator.not.equal")));
		SingleSelection operatorEl = uifactory.addDropdownSingleselect("rule.operator.".concat(partId), null, rulesCont, operatorKeys.keys(), operatorKeys.values(), null);
		operatorEl.select(rule.getOperator().name(), true);
		
		FormLink deleteRuleButton = uifactory.addFormLink("delete.rule.".concat(partId), "delete", "", null, rulesCont, Link.NONTRANSLATED + Link.BUTTON);
		deleteRuleButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		deleteRuleButton.setElementCssClass("o_sel_course_delete_rule");

		RuleElement ruleEl = new RuleElement(typeEl, operatorEl, deleteRuleButton);
		typeEl.setUserObject(ruleEl);
		deleteRuleButton.setUserObject(ruleEl);
		
		initRuleValueForm(rulesCont, rule, ruleEl);
		
		return ruleEl;
	}
	
	private void initRuleValueForm(FormLayoutContainer rulesCont, PracticeFilterRule rule, RuleElement ruleEl) {
		if(rule.getType() == PracticeFilterRule.Type.assessmentType) {
			initAssessmentTypeRuleForm(rulesCont, rule, ruleEl);
		} else if(rule.getType() == PracticeFilterRule.Type.keyword || rule.getType() == PracticeFilterRule.Type.language) {
			initTextFeldRuleForm(rulesCont, rule, ruleEl);
		} else if(rule.getType() == PracticeFilterRule.Type.educationalContextLevel) {
			initEducationalContextLevelForm(rulesCont, rule, ruleEl);
		}
	}
	
	private void initEducationalContextLevelForm(FormLayoutContainer rulesCont, PracticeFilterRule rule, RuleElement ruleEl) {
		KeyValues contextsKeys = MetaUIFactory.getContextKeyValues(getTranslator(), qpoolService);
		
		SingleSelection taxonomyLevelEl = uifactory.addDropdownSingleselect("rule.tax.lev." + (++counter), null, rulesCont,
				contextsKeys.getKeys(), contextsKeys.getValues(), null);
		taxonomyLevelEl.addActionListener(FormEvent.ONCHANGE);
		String val = rule.getValue();
		for(String typeKey : taxonomyLevelEl.getKeys()) {
			if(typeKey.equals(val)) {
				taxonomyLevelEl.select(typeKey, true);
			}
		}
		ruleEl.setValueItem(taxonomyLevelEl);
	}
	
	private void initAssessmentTypeRuleForm(FormLayoutContainer rulesCont, PracticeFilterRule rule, RuleElement ruleEl) {
		KeyValues assessmentTypeKeys = MetaUIFactory.getAssessmentTypes(getTranslator());

		SingleSelection assessmentTypesEl = uifactory.addDropdownSingleselect("rule.assessment.type." + (++counter), null, rulesCont,
				assessmentTypeKeys.getKeys(), assessmentTypeKeys.getValues(), null);
		assessmentTypesEl.addActionListener(FormEvent.ONCHANGE);
		String val = rule.getValue();
		for(String typeKey : assessmentTypesEl.getKeys()) {
			if(typeKey.equals(val)) {
				assessmentTypesEl.select(typeKey, true);
			}
		}
		ruleEl.setValueItem(assessmentTypesEl);
	}
	
	private void initTextFeldRuleForm(FormLayoutContainer rulesCont, PracticeFilterRule rule, RuleElement ruleEl) {
		String value = rule.getValue();
		TextElement textEl = uifactory.addTextElement("rule.text." + (++counter), null, 255, value, rulesCont);
		ruleEl.setValueItem(textEl);
	}
	
	private void initConfiguration(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("configuration.title"));
		
		SelectionValues levels = new SelectionValues();
		for(int i=1; i<=5; i++) {
			String level = Integer.toString(i);
			levels.add(SelectionValues.entry(level, level));
		}
		levelEl = uifactory.addDropdownSingleselect("levels", "levels", formLayout,
				levels.keys(), levels.values());
		levelEl.setMandatory(true);
		String level = config.getStringValue(PracticeEditController.CONFIG_KEY_NUM_LEVELS, "3");
		if(StringHelper.containsNonWhitespace(level) && levels.containsKey(level)) {
			levelEl.select(level, true);
		}
		
		SelectionValues questionsPerSerie = new SelectionValues();
		questionsPerSerie.add(SelectionValues.entry("10", "10"));
		questionsPerSerie.add(SelectionValues.entry("20", "20"));
		questionsPerSerie.add(SelectionValues.entry("50", "50"));
		questionPerSerieEl = uifactory.addDropdownSingleselect("questions.serie", "questions.serie", formLayout,
				questionsPerSerie.keys(), questionsPerSerie.values());
		questionPerSerieEl.setMandatory(true);
		String questionPerSerie = config.getStringValue(PracticeEditController.CONFIG_KEY_QUESTIONS_PER_SERIE, "10");
		if(StringHelper.containsNonWhitespace(questionPerSerie) && questionsPerSerie.containsKey(questionPerSerie)) {
			questionPerSerieEl.select(questionPerSerie, true);
		}
		
		String seriePerChallenge = config.getStringValue(PracticeEditController.CONFIG_KEY_SERIE_PER_CHALLENGE, "2");
		seriePerChallengeEl = uifactory.addTextElement("series.challenge", 4, seriePerChallenge, formLayout);
		seriePerChallengeEl.setMandatory(true);
		
		uifactory.addSpacerElement("challenges-space", formLayout, false);

		String challengeToComplete = config.getStringValue(PracticeEditController.CONFIG_KEY_NUM_CHALLENGES_FOR_COMPLETION, "2");
		challengeToCompleteEl = uifactory.addTextElement("num.challenges", 4, challengeToComplete, formLayout);
		challengeToCompleteEl.setMandatory(true);
		
		SelectionValues rankKeys = new SelectionValues();
		rankKeys.add(SelectionValues.entry("on", ""));
		rankListEl = uifactory.addCheckboxesHorizontal("rank.list", "rank.list", formLayout, rankKeys.keys(), rankKeys.values());
		boolean rankList = config.getBooleanSafe(PracticeEditController.CONFIG_KEY_RANK_LIST, false);
		rankListEl.select("on", rankList);
	}
	
	private void initStatisticsForm(FormLayoutContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PracticeTaxonomyCols.taxonomyLevel,
				new PracticeTaxonomyCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PracticeTaxonomyCols.numOfQuestions));

		taxonomyStatisticsModel = new PracticeResourceTaxonomyTableModel(columnsModel, getLocale());
		taxonomyStatisticsEl = uifactory.addTableElement(getWindowControl(), "statistics.taxonomy.level", taxonomyStatisticsModel, 20, false, getTranslator(), formLayout);
		taxonomyStatisticsEl.setNumOfRowsEnabled(false);
		taxonomyStatisticsEl.setCustomizeColumns(false);
	}
	
	private void loadModel() {
		List<PracticeResourceInfos> resourceInfos = practiceResourceService.getResourcesInfos(null, courseEntry, courseNode.getIdent());
		resourcesModel.setObjects(resourceInfos);
		resourcesTableEl.reset(true, true, true);
	}
	
	private void loadStatistics() {
		SearchPracticeItemParameters searchParams = new SearchPracticeItemParameters();
		searchParams.setRules(getRules());
		
		List<Long> taxonomyLevelKeys = getSelectedTaxonomyLevels();
		Map<String,PracticeResourceTaxonomyRow> taxonomyLevelsMap = new HashMap<>();
		boolean withSpecifiedTaxonomy = !taxonomyLevelKeys.isEmpty();
		if(withSpecifiedTaxonomy) {
			List<TaxonomyLevel> levels = taxonomyService.getTaxonomyLevelsByKeys(getSelectedTaxonomyLevels());
			searchParams.setDescendantsLevels(levels);
			for(TaxonomyLevel level:levels) {
				putTaxonomyLevelInMap(level, taxonomyLevelsMap);
			}
		}

		searchParams.setIncludeWithoutTaxonomyLevel(withoutTaxonomyEl.isAtLeastSelected(1));
		
		List<PracticeResource> resources = getSelectedResources();
		List<PracticeItem> items = practiceResourceService.generateItems(resources, searchParams, -1, getLocale());
		
		int withoutTaxonomyLevels = 0;
		
		// Collect taxonomy levels
		for(PracticeItem item:items) {
			QuestionItem qItem = item.getItem();
			if(qItem != null) {
				String levelName = qItem.getTaxonomyLevelName();
				if(StringHelper.containsNonWhitespace(levelName)) {
					List<String> parentLine = SearchPracticeItemHelper.cleanTaxonomicParentLine(levelName, qItem.getTaxonomicPath());
					String key = SearchPracticeItemHelper.buildKeyOfTaxonomicPath(levelName, parentLine);
					PracticeResourceTaxonomyRow row = taxonomyLevelsMap.get(key);
					if(row != null) {
						row.incrementNumOfQuestions();
					} else if(!withSpecifiedTaxonomy && qItem.getTaxonomyLevel() != null) {
						putTaxonomyLevelInMap(qItem.getTaxonomyLevel(), taxonomyLevelsMap)
							.incrementNumOfQuestions();
					} else {
						withoutTaxonomyLevels++;
					}
				} else {
					withoutTaxonomyLevels++;
				}
			}
		}

		// Some statistics
		int total = resourcesModel.getTotalNumOfItems();
		String questionsMsg = translate("stats.num.questions", Integer.toString(items.size()), Integer.toString(total));
		statisticsKeywordsCont.contextPut("numOfQuestions", questionsMsg);
		
		List<PracticeResourceTaxonomyRow> taxonomyLevelsStats = taxonomyLevelsMap.values().stream()
				.filter(levelRow -> !levelRow.isEmpty())
				.collect(Collectors.toList());
		if(withoutTaxonomyEl.isAtLeastSelected(1)) {
			taxonomyLevelsStats.add(new PracticeResourceTaxonomyRow(translate("wo.taxonomy.level.label"), withoutTaxonomyLevels));
		}
		taxonomyStatisticsModel.setObjects(taxonomyLevelsStats);
		taxonomyStatisticsEl.reset(true, true, true);
		taxonomyStatisticsEl.sort(PracticeTaxonomyCols.taxonomyLevel.name(), true);
	}
	
	private PracticeResourceTaxonomyRow putTaxonomyLevelInMap(TaxonomyLevel level, Map<String,PracticeResourceTaxonomyRow> taxonomyLevelsMap) {
		List<String> keys = SearchPracticeItemHelper.buildKeyOfTaxonomicPath(level);
		PracticeResourceTaxonomyRow row = new PracticeResourceTaxonomyRow(level);
		for(String key:keys) {
			taxonomyLevelsMap.put(key, row);
		}
		return row;
	}
	
	private void loadRules() {
		List<PracticeFilterRule> rules = config.getList(PracticeEditController.CONFIG_KEY_FILTER_RULES, PracticeFilterRule.class);
		
		ruleEls.clear();
		for(PracticeFilterRule rule:rules) {
			RuleElement ruleEl = initRuleForm(criteriaCont, rule);
			ruleEls.add(ruleEl);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(testResourcesListCtrl == source) {
			if(event instanceof AuthoringEntryRowSelectionEvent) {
				AuthoringEntryRowSelectionEvent se = (AuthoringEntryRowSelectionEvent)event;
				doAddTest(se.getRow());
			} else if(event instanceof AuthoringEntryRowsListSelectionEvent) {
				AuthoringEntryRowsListSelectionEvent se = (AuthoringEntryRowsListSelectionEvent)event;
				doAddTest(se.getRows());
			}
			cmc.deactivate();
			cleanUp();
		} else if(sharesChooserCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				loadStatistics();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmRemoveDialog == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				PracticeResourceInfos resourceRow = (PracticeResourceInfos)confirmRemoveDialog.getUserObject();
				doRemoveResource(resourceRow);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(testResourcesListCtrl);
		removeAsListenerAndDispose(confirmRemoveDialog);
		removeAsListenerAndDispose(sharesChooserCtrl);
		removeAsListenerAndDispose(cmc);
		testResourcesListCtrl = null;
		confirmRemoveDialog = null;
		sharesChooserCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTestButton == source) {
			doAddTest(ureq);
		} else if(addPoolButton == source) {
			doAddPool(ureq);
		} else if(addRuleEl == source) {
			doAddRule(addRuleEl.getSelectedKey());
			addRuleEl.select(SingleSelection.NO_SELECTION_KEY, true);
		} else if(resourcesTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("remove".equals(se.getCommand())) {
					doConfirmRemoveResource(ureq, resourcesModel.getObject(se.getIndex()));
				}
			}
		} else if(source instanceof FormLink && ((FormLink)source).getUserObject() instanceof RuleElement) {
			doRemoveRule((RuleElement)((FormLink)source).getUserObject());
		} else if(source instanceof SingleSelection && ((SingleSelection)source).getUserObject() instanceof RuleElement) {
			doChangeRuleType((RuleElement)((SingleSelection)source).getUserObject());
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateFormLogic(levelEl);
		allOk &= validateFormLogic(questionPerSerieEl);
		allOk &= validateInteger(seriePerChallengeEl);
		allOk &= validateInteger(challengeToCompleteEl);

		return allOk;
	}
	
	private boolean validateFormLogic(SingleSelection el) {
		boolean allOk = true;
		
		el.clearError();
		if(!el.isOneSelected()) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateInteger(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(!StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!StringHelper.isLong(el.getValue())) {
			el.setErrorKey("form.error.nointeger", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
		String numOfLevels = levelEl.getSelectedKey();
		config.setStringValue(PracticeEditController.CONFIG_KEY_NUM_LEVELS, numOfLevels);
		String challengeToComplete = challengeToCompleteEl.getValue();
		config.setStringValue(PracticeEditController.CONFIG_KEY_NUM_CHALLENGES_FOR_COMPLETION, challengeToComplete);
		String questionsPerSerie = questionPerSerieEl.getSelectedKey();
		config.setStringValue(PracticeEditController.CONFIG_KEY_QUESTIONS_PER_SERIE, questionsPerSerie);
		String seriesPerChallenge = seriePerChallengeEl.getValue();
		config.setStringValue(PracticeEditController.CONFIG_KEY_SERIE_PER_CHALLENGE, seriesPerChallenge);
		List<PracticeFilterRule> rules = getRules();
		config.setList(PracticeEditController.CONFIG_KEY_FILTER_RULES, rules);
		List<Long> selectedLevelKeys = getSelectedTaxonomyLevels();
		config.setList(PracticeEditController.CONFIG_KEY_FILTER_TAXONOMY_LEVELS, selectedLevelKeys);
		boolean includeWoLevels = withoutTaxonomyEl.isAtLeastSelected(1);
		config.setBooleanEntry(PracticeEditController.CONFIG_KEY_FILTER_INCLUDE_WO_TAXONOMY_LEVELS, includeWoLevels);
		boolean rankList = rankListEl.isAtLeastSelected(1);
		config.setBooleanEntry(PracticeEditController.CONFIG_KEY_RANK_LIST, rankList);
		
		fireEvent(ureq, Event.DONE_EVENT);
		
		loadStatistics();
	}
	
	private List<PracticeFilterRule> getRules() {
		List<PracticeFilterRule> rules = new ArrayList<>();
		for(RuleElement ruleEl:ruleEls) {
			String type = ruleEl.getTypeEl().getSelectedKey();
			PracticeFilterRule.Type typed = PracticeFilterRule.Type.valueOf(type);
			String value = ruleEl.getValue();
			Operator operator = ruleEl.getOperator();
			rules.add(new PracticeFilterRule(typed, operator, value));
		}
		return rules;
	}
	
	private List<Long> getSelectedTaxonomyLevels() {
		List<Long> selectedLevelKeys = new ArrayList<>();
		Collection<String> selectedTaxonomyLevelKeys = taxonomyEl.getSelectedKeys();
		for(String key:selectedTaxonomyLevelKeys) {
			if(StringHelper.containsNonWhitespace(key)) {
				selectedLevelKeys.add(Long.valueOf(key));
			}
		}
		return selectedLevelKeys;
	}
	
	private List<PracticeResource> getSelectedResources() {
		return resourcesModel.getObjects().stream()
				.map(PracticeResourceInfos::getResource)
				.collect(Collectors.toList());
	}
	
	private void doAddTest(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		AuthorListConfiguration tableConfig = AuthorListConfiguration.selectRessource("practice-qti21-test-v1", ImsQTI21Resource.TYPE_NAME);
		tableConfig.setSelectRepositoryEntry(SelectionMode.multi);
		tableConfig.setImportRessources(false);
		tableConfig.setCreateRessources(false);
		
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.setCanReference(true);
		searchParams.addResourceTypes(ImsQTI21Resource.TYPE_NAME);
		List<Long> excludeEntries = resourcesModel.getObjects().stream()
				.filter(infos -> infos.getResource().getTestEntry() != null)
				.map(resource -> resource.getResource().getTestEntry().getKey())
				.collect(Collectors.toList());
		searchParams.setExcludeEntryKeys(excludeEntries);
		
		testResourcesListCtrl = new AuthorListController(ureq, getWindowControl(), searchParams, tableConfig);
		listenTo(testResourcesListCtrl);
		testResourcesListCtrl.selectFilterTab(ureq, testResourcesListCtrl.getMyTab());
		
		String title = translate("add.resource.test.title");
		cmc = new CloseableModalController(getWindowControl(), "close", testResourcesListCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddTest(AuthoringEntryRow entryRow) {
		RepositoryEntry testEntry = repositoryService.loadByKey(entryRow.getKey());
		practiceResourceService.createResource(courseEntry, courseNode.getIdent(), testEntry);
		loadModel();
		loadStatistics();
	}
	
	private void doAddTest(List<AuthoringEntryRow> entryRows) {
		for(AuthoringEntryRow entryRow:entryRows) {
			RepositoryEntry testEntry = repositoryService.loadByKey(entryRow.getKey());
			practiceResourceService.createResource(courseEntry, courseNode.getIdent(), testEntry);
		}
		loadModel();
		loadStatistics();
	}
	
	private void doAddPool(UserRequest ureq) {
		List<PracticeResourceInfos> resources = resourcesModel.getObjects();
		sharesChooserCtrl = new SharedResourceChooserController(ureq, getWindowControl(), courseEntry, courseNode, resources);
		listenTo(sharesChooserCtrl);
		
		String title = translate("add.resource.pool.title");
		cmc = new CloseableModalController(getWindowControl(), "close", sharesChooserCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmRemoveResource(UserRequest ureq, PracticeResourceInfos row) {
		if(guardModalController(confirmRemoveDialog)) return;
		
		boolean testEntry = row.isTestEntry();
		String titleI18n = testEntry ? "confirm.remove.entry.title" : "confirm.remove.pool.title";
		String textI18n = testEntry ? "confirm.remove.entry.text" : "confirm.remove.pool.text";
		String[] args = new String[] { row.getName() };
		String title = translate(titleI18n, args);
		String text = translate(textI18n, args);

		confirmRemoveDialog = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), title, text);
		listenTo(confirmRemoveDialog);
		confirmRemoveDialog.setUserObject(row);
		confirmRemoveDialog.activate();
	}

	private void doRemoveResource(PracticeResourceInfos row) {
		practiceResourceService.deleteResource(row.getResource());
		loadModel();
		loadStatistics();
	}
	
	private void doAddRule(String rule) {
		PracticeFilterRule filter = new PracticeFilterRule(PracticeFilterRule.Type.valueOf(rule), Operator.equals, null);
		RuleElement ruleEl = initRuleForm(criteriaCont, filter);
		ruleEls.add(ruleEl);
		criteriaCont.setDirty(true);
	}
	
	private void doRemoveRule(RuleElement ruleEl) {
		ruleEls.remove(ruleEl);
		criteriaCont.setDirty(true);
	}
	
	private void doChangeRuleType(RuleElement ruleEl) {
		PracticeFilterRule rule = new PracticeFilterRule(ruleEl.getType(), ruleEl.getOperator(), ruleEl.getValue());
		initRuleValueForm(criteriaCont, rule, ruleEl);
		criteriaCont.setDirty(true);
	}
	
	public static class RuleElement {
		
		private final SingleSelection typeEl;
		private final SingleSelection operatorEl;
		private final FormLink deleteButton;
		private FormItem valueItem;
		
		public RuleElement(SingleSelection typeEl, SingleSelection operatorEl, FormLink deleteButton) {
			this.typeEl = typeEl;
			this.operatorEl = operatorEl;
			this.deleteButton = deleteButton;
		}
		
		public PracticeFilterRule.Type getType() {
			return Type.valueOf(typeEl.getSelectedKey());
		}
		
		public SingleSelection getOperatorEl() {
			return operatorEl;
		}
		
		public SingleSelection getTypeEl() {
			return typeEl;
		}
		
		public FormLink getDeleteButton() {
			return deleteButton;
		}
		
		public FormItem getValueItem() {
			return valueItem;
		}
		
		public void setValueItem(FormItem valueItem) {
			this.valueItem = valueItem;
		}
		
		public String getValue() {
			if(valueItem instanceof TextElement) {
				return ((TextElement)valueItem).getValue();
			}
			if(valueItem instanceof SingleSelection && ((SingleSelection)valueItem).isOneSelected()) {
				return ((SingleSelection)valueItem).getSelectedKey();
			}
			return null;	
		}
		
		public Operator getOperator() {
			if(operatorEl.isOneSelected()) {
				return Operator.valueOf(operatorEl.getSelectedKey());
			}
			return Operator.equals;
		}
	}
}
