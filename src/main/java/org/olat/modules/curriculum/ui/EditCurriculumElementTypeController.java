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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.gui.components.date.RelativeDateElement;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumAutomationService;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.curriculum.ui.component.AutomationContextCellRenderer;
import org.olat.modules.curriculum.ui.component.AutomationTargetStatusCellRenderer;
import org.olat.modules.curriculum.ui.component.CurriculumElementTypeComparator;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementTypeController extends FormBasicController {
	
	private static final String LECTURES = "lectures";
	private static final String CALENDAR = "calendar";
	private static final String LEARNING_PROGRESS = "learningprogress";
	public static final String FOR_USE_AS_IMPL = "implementation";
	private static final String FOR_USE_AS_IMPL_OR_ELEM = "implementationOrElement";
	public static final String FOR_USE_AS_ELEM = "element";
	private static final String SUB_ELEMENTS_YES = "subElementsYes";
	private static final String SUB_ELEMENTS_NO = "subElementsNo";
	private static final String CONTENT_NO_CONTENT = "noContent";
	private static final String CONTENT_SINGLE_COURSE = "singleCourse";
	private static final String CONTENT_COURSE_BUNDLE = "courseBundle";

	private static final String FILTER_CONTEXT = "Context";
	private static final String FILTER_STATUS = "Status";
	private static final String TAB_ALL = "All";
	private static final String TAB_RELEVANT = "Relevant";
	private static final String TAB_IMPLEMENTATION = "Implementation";
	private static final String TAB_CONTENT = "Content";
	
	private TextElement cssClassEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private RichTextElement descriptionEl;
	private MultipleSelectionElement featuresEnabledEl;

	private SingleSelection subElementsEl;
	private SingleSelection contentSubelementsYesEl;
	private SingleSelection contentSubelementsNoEl;
	private SpacerElement dividerEl;
	private MultipleSelectionElement parentTypesEl;
	private MultipleSelectionElement childTypesEl;
	
	private FormToggle automationEnabledEl;
	private FlexiTableElement automationTable;
	private AutomationRuleTableModel automationTableModel;
	private FlexiFiltersTab relevantTab;

	private CloseableModalController cmc;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private AutomationToolsController toolsCtrl;
	private EditCurriculumElementTypeAutomationController editRuleCtrl;

	private int automationRowCount = 0;

	private CurriculumElementType curriculumElementType;
	private final String preselectedForUseAs;
	private CurriculumAutomationConfig automationConfig;

	@Autowired
	private CurriculumAutomationService automationService;
	@Autowired
	private CurriculumService curriculumService;
	
	public EditCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, CurriculumElementType curriculumElementType) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.curriculumElementType = curriculumElementType;
		this.preselectedForUseAs = null;
		initForm(ureq);
		updateUI();
	}

	public EditCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, String preselectedForUseAs) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.curriculumElementType = null;
		this.preselectedForUseAs = preselectedForUseAs;
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer commonContainer = FormLayoutContainer.createDefaultFormLayout("common", getTranslator());
		commonContainer.setRootForm(mainForm);
		formLayout.add(commonContainer);

		String displayName = curriculumElementType == null ? "" : curriculumElementType.getDisplayName();
		displayNameEl = uifactory.addTextElement("type.displayname", "type.displayname", 255, displayName, commonContainer);
		displayNameEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.displayName));
		displayNameEl.setMandatory(true);
		if(displayNameEl.isEnabled() && !StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}
		
		String identifier = curriculumElementType == null ? "" : curriculumElementType.getIdentifier();
		identifierEl = uifactory.addTextElement("type.identifier", "type.identifier", 255, identifier, commonContainer);
		identifierEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.identifier));
		identifierEl.setMandatory(true);

		String cssClass = curriculumElementType == null ? "" : curriculumElementType.getCssClass();
		cssClassEl = uifactory.addTextElement("type.cssClass", "type.cssClass", 255, cssClass, commonContainer);
		cssClassEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.cssClass));
		
		String description = curriculumElementType == null ? "" : curriculumElementType.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("type.description", "type.description", 
				description, 10, -1, false, null, null,
				commonContainer, ureq.getUserSession(), getWindowControl());
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		descriptionEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.description));

		SelectionValues featuresPK = new SelectionValues();
		featuresPK.add(SelectionValues.entry(LECTURES, translate("type.lectures.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.lectures)));
		featuresPK.add(SelectionValues.entry(CALENDAR, translate("type.calendars.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars)));
		featuresPK.add(SelectionValues.entry(LEARNING_PROGRESS, translate("type.learning.progress.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.learningProgress)));

		featuresEnabledEl = uifactory.addCheckboxesVertical("type.features.enabled", commonContainer, featuresPK.keys(), featuresPK.values(), 1);
		featuresEnabledEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars));
		CurriculumLectures lecturesEnabled =  curriculumElementType == null ? null : curriculumElementType.getLectures();
		featuresEnabledEl.select(LECTURES, lecturesEnabled == CurriculumLectures.enabled);
		CurriculumCalendars calendarsEnabled =  curriculumElementType == null ? null : curriculumElementType.getCalendars();
		featuresEnabledEl.select(CALENDAR, calendarsEnabled == CurriculumCalendars.enabled);
		CurriculumLearningProgress learningProgressEnabled =  curriculumElementType == null ? null : curriculumElementType.getLearningProgress();
		featuresEnabledEl.select(LEARNING_PROGRESS, learningProgressEnabled == CurriculumLearningProgress.enabled);
		
		FormLayoutContainer configurationContainer = FormLayoutContainer.createDefaultFormLayout("configuration", getTranslator());
		configurationContainer.setRootForm(mainForm);
		formLayout.add(configurationContainer);
		configurationContainer.setFormTitle(translate("configuration"));
		
		String forUseAs = getForUseAs();
		String forUseAsLabel = getForUseAsLabel(forUseAs);
		uifactory.addStaticTextElement("type.for.use.as", "type.for.use.as", forUseAsLabel, configurationContainer);

		SelectionValues subElementsKV = new SelectionValues();
		subElementsKV.add(SelectionValues.entry(SUB_ELEMENTS_NO,
				translate("no"),
				translate("table.type.subelements.no.desc"),
				"o_icon o_icon_single_element", null, true));
		subElementsKV.add(SelectionValues.entry(SUB_ELEMENTS_YES,
				translate("yes"),
				translate("table.type.subelements.yes.desc"),
				"o_icon o_icon_sitemap", null, true));
		subElementsEl = uifactory.addCardSingleSelectHorizontal("subelements", 
				"table.type.header.type.subelements", configurationContainer, subElementsKV);
		subElementsEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, 
				CurriculumElementTypeManagedFlag.composite));
		subElementsEl.addActionListener(FormEvent.ONCHANGE);
		if(curriculumElementType != null && !curriculumElementType.isSingleElement()) {
			subElementsEl.select(SUB_ELEMENTS_YES, true);
		} else {
			subElementsEl.select(SUB_ELEMENTS_NO, true);
		}

		boolean contentManaged = CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, 
				CurriculumElementTypeManagedFlag.maxEntryRelations);
		String initialContent;
		if(curriculumElementType != null && curriculumElementType.getMaxRepositoryEntryRelations() == -1) {
			initialContent = CONTENT_COURSE_BUNDLE;
		} else if(curriculumElementType != null && curriculumElementType.getMaxRepositoryEntryRelations() == 0) {
			initialContent = CONTENT_NO_CONTENT;
		} else {
			initialContent = CONTENT_SINGLE_COURSE;
		}

		SelectionValues contentSubelementsYesKV = new SelectionValues();
		contentSubelementsYesKV.add(SelectionValues.entry(CONTENT_NO_CONTENT,
				translate("table.type.content.no.content"),
				translate("table.type.content.no.content.desc"),
				"o_icon o_icon_ban", null, true));
		contentSubelementsYesKV.add(SelectionValues.entry(CONTENT_SINGLE_COURSE,
				translate("table.type.content.single.course"),
				translate("table.type.content.single.course.desc"),
				"o_icon o_icon_courserun", null, true));
		contentSubelementsYesKV.add(SelectionValues.entry(CONTENT_COURSE_BUNDLE,
				translate("table.type.content.course.bundle"),
				translate("table.type.content.course.bundle.desc"),
				"o_icon o_icon_course_bundle", null, true));
		contentSubelementsYesEl = uifactory.addCardSingleSelectHorizontal("type.content.structural",
				"table.type.header.type.content", configurationContainer, contentSubelementsYesKV);
		contentSubelementsYesEl.setEnabled(!contentManaged);
		contentSubelementsYesEl.select(initialContent, true);
		contentSubelementsYesEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues contentSubelementsNoKV = new SelectionValues();
		contentSubelementsNoKV.add(SelectionValues.entry(CONTENT_SINGLE_COURSE,
				translate("table.type.content.single.course"),
				translate("table.type.content.single.course.desc"),
				"o_icon o_icon_courserun", null, true));
		contentSubelementsNoKV.add(SelectionValues.entry(CONTENT_COURSE_BUNDLE,
				translate("table.type.content.course.bundle"),
				translate("table.type.content.course.bundle.desc"),
				"o_icon o_icon_course_bundle", null, true));
		contentSubelementsNoEl = uifactory.addCardSingleSelectHorizontal("type.content.single",
				"table.type.header.type.content", configurationContainer, contentSubelementsNoKV);
		contentSubelementsNoEl.setEnabled(!contentManaged);
		// Override the illegal state "no subelements" and "no content":
		contentSubelementsNoEl.select(CONTENT_NO_CONTENT.equals(initialContent) ? CONTENT_SINGLE_COURSE : initialContent, true);
		contentSubelementsNoEl.addActionListener(FormEvent.ONCHANGE);

		dividerEl = uifactory.addSpacerElement("divider", configurationContainer, false);

		List<CurriculumElementType> elementTypes = curriculumService.getCurriculumElementTypes();
		elementTypes.sort(new CurriculumElementTypeComparator(getLocale()));
		elementTypes.remove(curriculumElementType);

		SelectionValues parentTypesKV = new SelectionValues();
		for(CurriculumElementType type:elementTypes) {
			if(!type.isSingleElement()) {
				String label = StringHelper.escapeHtml(type.getDisplayName())
						+ "<span class=\"text-muted o_small\"> · " + StringHelper.escapeHtml(type.getIdentifier())
						+ "</span>";
				parentTypesKV.add(SelectionValues.entry(type.getKey().toString(), label));
			}
		}

		parentTypesEl = uifactory.addCheckboxesVertical("type.parent.types", configurationContainer,
				parentTypesKV.keys(), parentTypesKV.values(), 2);
		parentTypesEl.setEscapeHtml(false);

		List<CurriculumElementTypeToType> allRelations = curriculumElementType != null
				? curriculumService.getAllCurriculumElementTypeRelations()
				: List.of();
		Set<Long> currentParentTypeKeys = allRelations.stream()
				.filter(r -> r.getAllowedSubType().getKey().equals(curriculumElementType.getKey()))
				.map(r -> r.getType().getKey())
				.collect(Collectors.toSet());
		for(Long parentKey : currentParentTypeKeys) {
			parentTypesEl.select(parentKey.toString(), true);
		}

		SelectionValues childTypesKV = new SelectionValues();
		for(CurriculumElementType type:elementTypes) {
			if(!type.isImplOnly()) {
				String label = StringHelper.escapeHtml(type.getDisplayName())
						+ "<span class=\"text-muted o_small\"> · " + StringHelper.escapeHtml(type.getIdentifier())
						+ "</span>";
				childTypesKV.add(SelectionValues.entry(type.getKey().toString(), label));
			}
		}

		childTypesEl = uifactory.addCheckboxesVertical("type.allowed.sub.types", configurationContainer,
				childTypesKV.keys(), childTypesKV.values(), 2);
		childTypesEl.setEscapeHtml(false);
		childTypesEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.subTypes));

		if(curriculumElementType != null) {
			Set<CurriculumElementTypeToType> typeToTypes = curriculumElementType.getAllowedSubTypes();
			for(CurriculumElementTypeToType typeToType:typeToTypes) {
				String subTypeKey = typeToType.getAllowedSubType().getKey().toString();
				childTypesEl.select(subTypeKey, true);
			}
		}

		FormLayoutContainer automationCont = uifactory.addVerticalFormLayout("automation", null, formLayout);
		automationCont.setFormTitle(translate("automation.title"));
		boolean isImplOrElem = FOR_USE_AS_IMPL_OR_ELEM.equals(getForUseAs());
		if (isImplOrElem) {
			automationCont.setFormInfo(translate("automation.info.impl.or.elem"));
		} else {
			String automationInfoKey = FOR_USE_AS_ELEM.equals(getForUseAs()) ? "automation.info.element" : "automation.info";
			automationCont.setFormInfo(translate(automationInfoKey));
		}

		FormLayoutContainer automationEnableContainer = FormLayoutContainer.createDefaultFormLayout("automationEnableWrapper", getTranslator());
		automationEnableContainer.setRootForm(mainForm);
		automationCont.add(automationEnableContainer);
		automationEnabledEl = uifactory.addToggleButton("automation.enable", "automation.enable", null, null, automationEnableContainer);
		automationEnabledEl.addActionListener(FormEvent.ONCHANGE);
		automationEnabledEl.setVisible(!isImplOrElem);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.context,
				new AutomationContextCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.automationType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.targetStatus,
				new AutomationTargetStatusCellRenderer(getTranslator(), true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.condition));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.statusIs));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AutomationCols.rule));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(AutomationCols.tools));

		FormLayoutContainer automationTableCont = FormLayoutContainer.createBareBoneFormLayout("automationTableWrapper", getTranslator());
		automationTableCont.setRootForm(mainForm);
		automationCont.add(automationTableCont);

		automationTableModel = new AutomationRuleTableModel(columnsModel, getTranslator());
		automationTable = uifactory.addTableElement(getWindowControl(), "automationRules",
				automationTableModel, getTranslator(), automationTableCont);
		automationTable.setElementCssClass("o_block_large_bottom");
		automationTable.setExportEnabled(true);

		if (!isImplOrElem) {
			automationConfig = curriculumElementType != null
					? curriculumElementType.getAutomationConfig()
					: defaultAutomationConfig();
			automationEnabledEl.toggle(automationConfig != null);
			initAutomationFilters();
			initAutomationFilterTabs();
			loadAutomationTable();
			automationTable.setSelectedFilterTab(ureq, relevantTab);
		}
		automationTable.setVisible(!isImplOrElem && automationEnabledEl.isOn());

		FormLayoutContainer buttonWrapperContainer = FormLayoutContainer.createDefaultFormLayout("buttonsWrapper", getTranslator());
		buttonWrapperContainer.setRootForm(mainForm);
		formLayout.add(buttonWrapperContainer);
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		buttonWrapperContainer.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void initAutomationFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues contextValues = new SelectionValues();
		for (AutomationContext context : AutomationContext.values()) {
			contextValues.add(SelectionValues.entry(context.name(), translate("automation.context." + context.name().toLowerCase())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("automation.filter.context"), FILTER_CONTEXT, contextValues, true));

		SelectionValues statusValues = new SelectionValues();
		for (CurriculumElementStatus status : CurriculumElementStatus.selectableAdmin()) {
			statusValues.add(SelectionValues.entry(status.name(), translate("status." + status.name())));
		}
		Translator repoTranslator = Util.createPackageTranslator(RepositoryEntryStatusEnum.class, getLocale(), getTranslator());
		for (RepositoryEntryStatusEnum status : RepositoryEntryStatusEnum.preparationToClosed()) {
			statusValues.add(SelectionValues.entry(status.name(), repoTranslator.translate(status.i18nKey())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("automation.filter.status"), FILTER_STATUS, statusValues, true));

		automationTable.setFilters(true, filters, true, false);
	}

	private void initAutomationFilterTabs() {
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tab(TAB_ALL, translate("automation.filter.all"), TabSelectionBehavior.nothing);
		relevantTab = FlexiFiltersTabFactory.tab(TAB_RELEVANT, translate("automation.filter.relevant"), TabSelectionBehavior.nothing);
		boolean isElemType = FOR_USE_AS_ELEM.equals(getForUseAs());
		String implTabLabel = isElemType ? translate("automation.filter.element") : translate("automation.filter.implementation");
		AutomationContext implTabContext = isElemType ? AutomationContext.ELEMENT : AutomationContext.IMPLEMENTATION;
		FlexiFiltersTab implementationTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_IMPLEMENTATION,
				implTabLabel, TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_CONTEXT, implTabContext.name())));
		FlexiFiltersTab contentTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_CONTENT,
				translate("automation.filter.content"), TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_CONTEXT, AutomationContext.CONTENT.name())));
		automationTable.setFilterTabs(true, List.of(allTab, relevantTab, implementationTab, contentTab));
	}

	private void loadAutomationTable() {
		List<AutomationRuleRow> rows = new ArrayList<>();
		if (automationConfig != null && automationConfig.getRules() != null) {
			for (CurriculumAutomationRule rule : automationConfig.getRules()) {
				rows.add(forgeAutomationRow(rule));
			}
		}
		automationTableModel.setObjects(rows);
		automationTable.reset(true, true, true);
	}

	private AutomationRuleRow forgeAutomationRow(CurriculumAutomationRule rule) {
		AutomationRuleRow row = new AutomationRuleRow(rule);
		FormToggle ruleEl = uifactory.addToggleButton("rule_" + (++automationRowCount), null,
				translate("on"), translate("off"), null);
		ruleEl.toggle(rule.isEnabled());
		ruleEl.addActionListener(FormEvent.ONCHANGE);
		ruleEl.setUserObject(row);
		row.setRuleEnabledEl(ruleEl);
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		return row;
	}

	private CurriculumAutomationConfig defaultAutomationConfig() {
		boolean subYes = subElementsEl.isOneSelected() && SUB_ELEMENTS_YES.equals(subElementsEl.getSelectedKey());
		SingleSelection contentEl = subYes ? contentSubelementsYesEl : contentSubelementsNoEl;
		String content = contentEl.isOneSelected() ? contentEl.getSelectedKey() : CONTENT_SINGLE_COURSE;
		int maxRelations = CONTENT_COURSE_BUNDLE.equals(content) ? -1 : (CONTENT_NO_CONTENT.equals(content) ? 0 : 1);
		return automationService.getDefaultConfig(FOR_USE_AS_IMPL.equals(getForUseAs()), maxRelations);
	}

	private void updateAutomationDefaults() {
		if (curriculumElementType == null && automationEnabledEl.isOn()) {
			automationConfig = defaultAutomationConfig();
			loadAutomationTable();
		}
	}

	private void doOpenAutomationTools(UserRequest ureq, AutomationRuleRow row, FormLink link) {
		toolsCtrl = new AutomationToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doEditRule(UserRequest ureq, CurriculumAutomationRule rule) {
		editRuleCtrl = new EditCurriculumElementTypeAutomationController(ureq, getWindowControl(), rule,
				FOR_USE_AS_IMPL.equals(getForUseAs()));
		listenTo(editRuleCtrl);
		String title = translate("automation.rule.edit.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editRuleCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editRuleCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		editRuleCtrl = null;
		toolsCtrl = null;
		toolsCalloutCtrl = null;
		cmc = null;
	}
	
	private String getForUseAs() {
		if(preselectedForUseAs != null) {
			return preselectedForUseAs;
		}
		if (curriculumElementType == null) {
			return null;
		}
		if(curriculumElementType.isImplOnly()) {
			return FOR_USE_AS_IMPL;
		} else if(!curriculumElementType.isAllowedAsRootElement()) {
			return FOR_USE_AS_ELEM;
		} else {
			return FOR_USE_AS_IMPL_OR_ELEM;
		}
	}
	
	private String getForUseAsLabel(String value) {
		if(FOR_USE_AS_IMPL.equals(value)) {
			return translate("table.type.for.use.as.implementation");
		} else if(FOR_USE_AS_ELEM.equals(value)) {
			return translate("table.type.for.use.as.element");
		} else if(FOR_USE_AS_IMPL_OR_ELEM.equals(value)) {
			return translate("table.type.for.use.as.implementation.or.element");
		}

		return null;
	}

	private void updateUI() {
		boolean subElementsYes = subElementsEl.isOneSelected() && SUB_ELEMENTS_YES.equals(subElementsEl.getSelectedKey());

		if(!subElementsYes) {
			// If subelements==no, copy the content state of the other element,
			// but override the illegal state "subelements==no" and "no content". 
			// Change the value to "single course" in that case.
			String content = contentSubelementsYesEl.isOneSelected() ? contentSubelementsYesEl.getSelectedKey() : CONTENT_SINGLE_COURSE;
			contentSubelementsNoEl.select(CONTENT_NO_CONTENT.equals(content) ? CONTENT_SINGLE_COURSE : content, true);
		}
		contentSubelementsYesEl.setVisible(subElementsYes);
		contentSubelementsNoEl.setVisible(!subElementsYes);

		boolean showParentElements = !FOR_USE_AS_IMPL.equals(getForUseAs());
		parentTypesEl.setVisible(showParentElements);
		childTypesEl.setVisible(subElementsYes);
		dividerEl.setVisible(showParentElements || subElementsYes);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayNameEl.clearError();
		if(!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
			displayNameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		identifierEl.clearError();
		if(!StringHelper.containsNonWhitespace(identifierEl.getValue())) {
			identifierEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editRuleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadAutomationTable();
			}
			cmc.deactivate();
			cleanUp();
		} else if (toolsCtrl == source || toolsCalloutCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(subElementsEl == source) {
			if(SUB_ELEMENTS_YES.equals(subElementsEl.getSelectedKey())) {
				contentSubelementsYesEl.select(CONTENT_NO_CONTENT, true);
			}
			updateUI();
			updateAutomationDefaults();
		} else if (contentSubelementsYesEl == source || contentSubelementsNoEl == source) {
			updateAutomationDefaults();
		} else if (automationEnabledEl == source) {
			if (automationEnabledEl.isOn() && automationConfig == null) {
				automationConfig = defaultAutomationConfig();
				loadAutomationTable();
			}
			automationTable.setVisible(automationEnabledEl.isOn());
		} else if (automationTable == source) {
			if (event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				automationTableModel.filter(automationTable.getQuickSearchString(), automationTable.getFilters());
				automationTable.reset(true, true, false);
			}
		} else if (source instanceof FormToggle tg && tg.getUserObject() instanceof AutomationRuleRow r) {
			r.getRule().setEnabled(tg.isOn());
		} else if (source instanceof FormLink link && "tools".equals(link.getCmd())
				&& link.getUserObject() instanceof AutomationRuleRow r) {
			doOpenAutomationTools(ureq, r, link);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (fiSrc instanceof FormLink || fiSrc == automationTable) {
			return;
		}
		super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(curriculumElementType == null) {
			curriculumElementType = curriculumService.createCurriculumElementType(identifierEl.getValue(), displayNameEl.getValue(),
					descriptionEl.getValue(), null);
		} else {
			curriculumElementType = curriculumService.getCurriculumElementType(curriculumElementType);
			curriculumElementType.setIdentifier(identifierEl.getValue());
			curriculumElementType.setDisplayName(displayNameEl.getValue());
			curriculumElementType.setDescription(descriptionEl.getValue());
		}
		curriculumElementType.setCssClass(cssClassEl.getValue());
		Collection<String> selectedFeatures = featuresEnabledEl.getSelectedKeys();
		if(selectedFeatures.contains(CALENDAR)) {
			curriculumElementType.setCalendars(CurriculumCalendars.enabled);
		} else {
			curriculumElementType.setCalendars(CurriculumCalendars.disabled);
		}
		if(selectedFeatures.contains(LECTURES)) {
			curriculumElementType.setLectures(CurriculumLectures.enabled);
		} else {
			curriculumElementType.setLectures(CurriculumLectures.disabled);
		}
		if(selectedFeatures.contains(LEARNING_PROGRESS)) {
			curriculumElementType.setLearningProgress(CurriculumLearningProgress.enabled);
		} else {
			curriculumElementType.setLearningProgress(CurriculumLearningProgress.disabled);
		}

		String forUseAs = getForUseAs();
		if(FOR_USE_AS_IMPL.equals(forUseAs)) {
			curriculumElementType.setImplOnly(true);
			curriculumElementType.setAllowedAsRootElement(true);
		} else if(FOR_USE_AS_ELEM.equals(forUseAs)) {
			curriculumElementType.setImplOnly(false);
			curriculumElementType.setAllowedAsRootElement(false);
		} else {
			curriculumElementType.setImplOnly(false);
			curriculumElementType.setAllowedAsRootElement(true);
		}

		if(subElementsEl.isOneSelected()) {
			curriculumElementType.setSingleElement(SUB_ELEMENTS_NO.equals(subElementsEl.getSelectedKey()));
		}
		SingleSelection contentEl = contentSubelementsYesEl.isVisible() ? contentSubelementsYesEl : contentSubelementsNoEl;
		if(contentEl.isOneSelected()) {
			String content = contentEl.getSelectedKey();
			if(CONTENT_COURSE_BUNDLE.equals(content)) {
				curriculumElementType.setMaxRepositoryEntryRelations(-1);
			} else if(CONTENT_NO_CONTENT.equals(content)) {
				curriculumElementType.setMaxRepositoryEntryRelations(0);
			} else {
				curriculumElementType.setMaxRepositoryEntryRelations(1);
			}
		}

		Collection<String> selectedParentKeys = parentTypesEl.isVisible()
				? parentTypesEl.getSelectedKeys()
				: Set.of();
		List<CurriculumElementType> allTypes = curriculumService.getCurriculumElementTypes();
		allTypes.remove(curriculumElementType);
		for(CurriculumElementType parentCandidate : allTypes) {
			if(selectedParentKeys.contains(parentCandidate.getKey().toString())) {
				curriculumService.allowCurriculumElementSubType(parentCandidate, curriculumElementType);
			} else {
				curriculumService.disallowCurriculumElementSubType(parentCandidate, curriculumElementType);
			}
		}

		Collection<String> selectedAllowedSubTypeKeys = childTypesEl.isVisible()
				? childTypesEl.getSelectedKeys()
				: List.of();
		List<CurriculumElementType> allowedSubTypes = new ArrayList<>();
		for(String selectedAllowedSubTypeKey:selectedAllowedSubTypeKeys) {
			allowedSubTypes.add(curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(Long.valueOf(selectedAllowedSubTypeKey))));
		}
		curriculumElementType.setAutomationConfig(automationEnabledEl.isOn() ? automationConfig : null);
		curriculumElementType = curriculumService.updateCurriculumElementType(curriculumElementType, allowedSubTypes);

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private class AutomationRuleTableModel extends DefaultFlexiTableDataModel<AutomationRuleRow>
			implements FilterableFlexiTableModel {

		private static final AutomationCols[] COLS = AutomationCols.values();

		private final Translator translator;
		private List<AutomationRuleRow> backupRows = List.of();

		public AutomationRuleTableModel(FlexiTableColumnModel columnsModel,
				org.olat.core.gui.translator.Translator translator) {
			super(columnsModel);
			Translator withDate = Util.createPackageTranslator(RelativeDateElement.class,
					translator.getLocale(), translator);
			this.translator = Util.createPackageTranslator(RepositoryEntryStatusEnum.class,
					translator.getLocale(), withDate);
		}

		@Override
		public void setObjects(List<AutomationRuleRow> objects) {
			backupRows = objects;
			super.setObjects(objects);
		}

		@Override
		public void filter(String searchString, List<FlexiTableFilter> filters) {
			List<AutomationRuleRow> rows = backupRows;

			FlexiTableFilter contextFilter = FlexiTableFilter.getFilter(filters, FILTER_CONTEXT);
			if (contextFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> values = extendedFilter.getValues();
				if (values != null && !values.isEmpty()) {
					rows = rows.stream()
							.filter(r -> values.contains(r.getContext().name()))
							.toList();
				}
			}

			FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, FILTER_STATUS);
			if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> values = extendedFilter.getValues();
				if (values != null && !values.isEmpty()) {
					rows = rows.stream()
							.filter(r -> {
								Object ts = r.getTargetStatus();
								if (ts instanceof CurriculumElementStatus ces) {
									return values.contains(ces.name());
								} else if (ts instanceof RepositoryEntryStatusEnum res) {
									return values.contains(res.name());
								} else if (ts instanceof String s) {
									return values.contains(s);
								}
								return false;
							})
							.toList();
				}
			}

			if (relevantTab != null && relevantTab.equals(automationTable.getSelectedFilterTab())) {
				rows = rows.stream().filter(AutomationRuleRow::isEnabled).toList();
			}

			super.setObjects(rows);
		}

		@Override
		public Object getValueAt(int row, int col) {
			AutomationRuleRow ruleRow = getObject(row);
			return switch (COLS[col]) {
				case context -> ruleRow.getContext();
				case automationType -> translator.translate("automation.type." + ruleRow.getAutomationType().name().toLowerCase());
				case targetStatus -> ruleRow.getTargetStatus();
				case condition -> conditionText(ruleRow.getRule());
				case statusIs -> joinStatuses(ruleRow.getRule().getOnlyWhenStatus());
				case rule -> ruleRow.getRuleEnabledEl();
				case tools -> ruleRow.getToolsLink();
			};
		}

		private String conditionText(CurriculumAutomationRule rule) {
			if (rule.getDependingOn() == AutomationDependingOn.STATUS) {
				return statusCondition(rule.getDependingOnStatus());
			}
			boolean after = rule.getDirection() == OffsetDirection.AFTER;
			boolean endRef = CurriculumAutomationRule.REFERENCE_END.equals(rule.getReference())
					|| (rule.getReference() == null && after);
			String anchor = translator.translate(endRef
					? "automation.condition.anchor.end" : "automation.condition.anchor.begin");
			if (rule.getUnit() == null || rule.getUnit() == AutomationUnit.SAME_DAY) {
				return translator.translate("relative.date.display.same.day", new String[] { anchor });
			}
			if (rule.getValue() == null) {
				return "-";
			}
			String base = "relative.date.unit." + rule.getUnit().name().toLowerCase().replaceAll("s$", "");
			String unit = translator.translate(rule.getValue() == 1 ? base : base + "s");
			String key = after ? "relative.date.display.after" : "relative.date.display.before";
			return translator.translate(key, new String[] { String.valueOf(rule.getValue()), unit, anchor });
		}

		private String statusCondition(Set<String> statuses) {
			if (statuses == null || statuses.isEmpty()) {
				return "-";
			}
			String sep = " " + translator.translate("automation.condition.status.or") + " ";
			String joined = statuses.stream()
					.map(s -> "\"" + CurriculumUIFactory.translateAutomationStatus(getTranslator(), s) + "\"")
					.collect(Collectors.joining(sep));
			return translator.translate("automation.condition.status", new String[] { joined });
		}

		private String joinStatuses(Set<String> statuses) {
			if (statuses == null || statuses.isEmpty()) {
				return "-";
			}
			return statuses.stream()
					.map(s -> CurriculumUIFactory.translateAutomationStatus(getTranslator(), s))
					.collect(Collectors.joining(", "));
		}
	}

	private enum AutomationCols implements FlexiColumnDef {
		context("automation.col.context"),
		automationType("automation.col.automation"),
		targetStatus("automation.col.target.status"),
		condition("automation.col.condition"),
		statusIs("automation.col.status.is"),
		rule("automation.col.rule"),
		tools("action");

		private final String i18nKey;

		AutomationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

	private class AutomationToolsController extends BasicController {

		private final AutomationRuleRow row;
		private final Link editLink;

		public AutomationToolsController(UserRequest ureq, WindowControl wControl, AutomationRuleRow row) {
			super(ureq, wControl);
			this.row = row;
			VelocityContainer mainVC = createVelocityContainer("tool_automation_rule");
			editLink = LinkFactory.createLink("automation.rule.edit", "automation.rule.edit",
					getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (editLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doEditRule(ureq, row.getRule());
			}
		}
	}
}
