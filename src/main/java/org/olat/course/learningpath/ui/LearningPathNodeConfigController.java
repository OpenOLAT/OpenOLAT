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
package org.olat.course.learningpath.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CompletionType;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathEditConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.LearningPathTranslations;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.learningpath.obligation.ExceptionalObligationController;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.learningpath.ui.ExceptionalObligationDataModel.ExceptionalObligationCols;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNodeConfigController extends FormBasicController {	

	private static final String CMD_ADD_EXEPTIONAL_OBLIGATION = "add.exeptional.obligation";
	
	public static final String CONFIG_VALUE_TRIGGER_NODE_VISITED = FullyAssessedTrigger.nodeVisited.name();
	public static final String CONFIG_VALUE_TRIGGER_CONFIRMED = FullyAssessedTrigger.confirmed.name();
	public static final String CONFIG_VALUE_TRIGGER_STATUS_DONE = FullyAssessedTrigger.statusDone.name();
	public static final String CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW = FullyAssessedTrigger.statusInReview.name();
	public static final String CONFIG_VALUE_TRIGGER_SCORE = FullyAssessedTrigger.score.name();
	public static final String CONFIG_VALUE_TRIGGER_PASSED = FullyAssessedTrigger.passed.name();

	private static final String[] EXCEPTIONAL_OBLIGATION_KEYS = { "on" };
	private static final String[] EXCEPTIONAL_OBLIGATION_VALUES = { "" };
	private static final String MANDATORY_PREFIX = "mandatory_";
	private static final String EXCLUDED_PREFIX = "excluded_";
	private static final String OPTIONAL_PREFIX = "optional_";
	private static final String CMD_DELETE = "delete";
	
	private SingleSelection obligationEl;
	private FormLayoutContainer obligationCont;
	private ExceptionalObligationDataModel dataModel;
	private FlexiTableElement tableEl;
	private FormLink showExceptionalObligationLink;
	private FormLink hideExceptionalObligationLink;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private TextElement durationEl;
	private SingleSelection triggerEl;
	private TextElement scoreCutEl;
	
	private CloseableModalController cmc;
	private ExceptionalObligationController exceptionalObligationCreateCtrl;

	private final RepositoryEntry courseEntry;
	private final ICourse course;
	private final CourseNode courseNode;
	private final LearningPathConfigs learningPathConfigs;
	private final LearningPathEditConfigs editConfigs;
	private AssessmentObligation selectedObligation;
	private List<ExceptionalObligationRow> allRows;
	
	@Autowired
	private LearningPathService learningPathService;

	public LearningPathNodeConfigController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, LearningPathEditConfigs editConfigs) {
		super(ureq, wControl);
		this.courseEntry = courseEntry;
		this.course = CourseFactory.loadCourse(courseEntry);
		this.courseNode = courseNode;
		CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
		this.learningPathConfigs = learningPathService.getConfigs(courseNode, editorTreeNode.getParent());
		this.editConfigs = editConfigs;
		this.selectedObligation = learningPathConfigs.getObligation() != null
				? learningPathConfigs.getObligation()
				: LearningPathConfigs.OBLIGATION_DEFAULT;
		
		initForm(ureq);
		updateUI();
		loadExceptionalObligations();
		updateExceptionalObligationsUI(allRows.size() > 1); // One for the default obligation
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.title");
		setFormContextHelp("Learning path element");
		formLayout.setElementCssClass("o_lp_config_edit");
		
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory")));
		obligationKV.add(entry(AssessmentObligation.optional.name(), translate("config.obligation.optional")));
		obligationKV.add(entry(AssessmentObligation.excluded.name(), translate("config.obligation.excluded")));
		obligationEl = uifactory.addRadiosHorizontal("config.obligation", formLayout, obligationKV.keys(), obligationKV.values());
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		String obligationKey = selectedObligation.name();
		if (Arrays.asList(obligationEl.getKeys()).contains(obligationKey)) {
			obligationEl.select(obligationKey, true);
		}
		
		obligationCont = FormLayoutContainer.createCustomFormLayout("obligationCont", getTranslator(), velocity_root + "/config_obligation.html");
		obligationCont.setRootForm(mainForm);
		formLayout.add(obligationCont);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.mandatory));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.optional));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.excluded));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.delete));
		
		dataModel = new ExceptionalObligationDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), obligationCont);
		tableEl.setElementCssClass("o_lp_exobli_table");
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		DropdownItem addExceptionalObligationDropdown = uifactory.addDropdownMenu("config.exceptional.obligation.add",
				"config.exceptional.obligation.add", null, obligationCont, getTranslator());
		addExceptionalObligationDropdown.setOrientation(DropdownOrientation.normal);
		addExceptionalObligationDropdown.setExpandContentHeight(true);
		
		learningPathService.getExceptionalObligationHandlers().stream()
				.sorted((h1, h2) -> Integer.compare(h1.getSortValue(), h2.getSortValue()))
				.forEach(handler -> addHandlerToDropdown(addExceptionalObligationDropdown, handler));
		
		showExceptionalObligationLink = uifactory.addFormLink("show.exceptional.obligation", "off", "off", obligationCont, Link.LINK);
		showExceptionalObligationLink.setCustomEnabledLinkCSS("o_button_toggle");
		showExceptionalObligationLink.setIconLeftCSS("o_icon o_icon_toggle");
		
		hideExceptionalObligationLink = uifactory.addFormLink("hide.exceptional.obligation", "on", "on", obligationCont, Link.LINK);
		hideExceptionalObligationLink.setCustomEnabledLinkCSS("o_button_toggle o_on");
		hideExceptionalObligationLink.setIconRightCSS("o_icon o_icon_toggle");
		
		Date startDate = learningPathConfigs.getStartDate();
		startDateEl = uifactory.addDateChooser("config.start.date", startDate, formLayout);
		startDateEl.setDateChooserTimeEnabled(true);
		startDateEl.setHelpTextKey("config.start.date.help", null);
		
		Date endDate = learningPathConfigs.getEndDate();
		endDateEl = uifactory.addDateChooser("config.end.date", endDate, formLayout);
		endDateEl.setDateChooserTimeEnabled(true);
		endDateEl.setHelpTextKey("config.end.date.help", null);
		
		String duration = learningPathConfigs.getDuration() != null? learningPathConfigs.getDuration().toString(): null;
		durationEl = uifactory.addTextElement("config.duration", 128, duration , formLayout);
		durationEl.setHelpTextKey("config.duration.help", null);
		
		SelectionValues triggerKV = getTriggerKV();
		triggerEl = uifactory.addRadiosVertical("config.trigger", formLayout,
				triggerKV.keys(), triggerKV.values());
		triggerEl.addActionListener(FormEvent.ONCHANGE);
		FullyAssessedTrigger trigger = learningPathConfigs.getFullyAssessedTrigger() != null
				? learningPathConfigs.getFullyAssessedTrigger()
				: LearningPathConfigs.LEGACY_TRIGGER_DEFAULT;
		String triggerKey = trigger.name();
		if (Arrays.asList(triggerEl.getKeys()).contains(triggerKey)) {
			triggerEl.select(triggerKey, true);
		}
		
		String score = learningPathConfigs.getScoreTriggerValue() != null
				? learningPathConfigs.getScoreTriggerValue().toString()
				: null;
		scoreCutEl = uifactory.addTextElement("config.score.cut", 100, score, formLayout);
		scoreCutEl.setMandatory(true);
		
		uifactory.addFormSubmitButton("save", formLayout);
	}

	private void addHandlerToDropdown(DropdownItem dropdown, ExceptionalObligationHandler handler) {
		if (handler.isShowAdd(courseEntry)) {
			FormLink link = uifactory.addFormLink(handler.getType(), CMD_ADD_EXEPTIONAL_OBLIGATION, handler.getAddI18nKey(), null, obligationCont, Link.LINK);
			link.setUserObject(handler);
			dropdown.addElement(link);
		}
	}

	private SelectionValues getTriggerKV() {
		SelectionValues triggerKV = new SelectionValues();
		if (editConfigs.isTriggerNodeVisited()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_NODE_VISITED, translate("config.trigger.visited")));
		}
		if (editConfigs.isTriggerConfirmed()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_CONFIRMED, translate("config.trigger.confirmed")));
		}
		if (editConfigs.isTriggerScore()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_SCORE, translate("config.trigger.score")));
		}
		if (editConfigs.isTriggerPassed()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_PASSED, translate("config.trigger.passed")));
		}
		
		LearningPathTranslations translations = editConfigs.getTranslations();
		if (editConfigs.isTriggerStatusInReview()) {
			String translation = translations.getTriggerStatusInReview(getLocale()) != null
					? translations.getTriggerStatusInReview(getLocale())
					: translate("config.trigger.status.in.review");
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW, translation));
		}
		
		if (editConfigs.isTriggerStatusDone()) {
			String translation = translations.getTriggerStatusDone(getLocale()) != null
					? translations.getTriggerStatusDone(getLocale())
					: translate("config.trigger.status.done");
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_STATUS_DONE, translation));
		}
		return triggerKV;
	}
	
	private void updateUI() {
		boolean obligationMandatory = hasMandatoryObligation();
		endDateEl.setVisible(obligationMandatory);
				
		durationEl.setMandatory(isDurationMandatory());
		
		boolean triggerScore = triggerEl.isOneSelected() && triggerEl.getSelectedKey().equals(CONFIG_VALUE_TRIGGER_SCORE);
		scoreCutEl.setVisible(triggerScore);
	}
	
	private void updateExceptionalObligationsUI(boolean showExceptional) {
		obligationEl.select(selectedObligation.name(), true);
		obligationEl.setVisible(!showExceptional);
		flc.setDirty(true);
		
		if (showExceptional) {
			obligationCont.setLabel("config.obligation", null);
		} else {
			obligationCont.setLabel(null, null);
		}
		obligationCont.contextPut("exceptional", Boolean.valueOf(showExceptional));
	}

	private void loadExceptionalObligations() {
		List<ExceptionalObligation> exceptionalObligations = new ArrayList<>(learningPathConfigs.getExceptionalObligations());
		allRows = new ArrayList<>(exceptionalObligations.size());
		addDefaultObligationRow(allRows);
		addExceptionalObligationRows(exceptionalObligations);
	}

	private void addDefaultObligationRow(List<ExceptionalObligationRow> rows) {
		ExceptionalObligationRow row = new ExceptionalObligationRow(null);
		row.setName(translate("exceptional.obligation.default.type"));
		row.setType(null);
		SingleSelection mandatoryEl = uifactory.addRadiosHorizontal(MANDATORY_PREFIX + "default", null,
				obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
		mandatoryEl.setAllowNoSelection(true);
		mandatoryEl.addActionListener(FormEvent.ONCHANGE);
		mandatoryEl.setUserObject(row);
		row.setMandatoryEl(mandatoryEl);
		
		SingleSelection optionalEl = uifactory.addRadiosHorizontal(OPTIONAL_PREFIX + "default", null,
				obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
		optionalEl.setAllowNoSelection(true);
		optionalEl.addActionListener(FormEvent.ONCHANGE);
		optionalEl.setUserObject(row);
		row.setOptionalEl(optionalEl);
		
		SingleSelection excludedEl = uifactory.addRadiosHorizontal(EXCLUDED_PREFIX + "default", null,
				obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
		excludedEl.setAllowNoSelection(true);
		excludedEl.addActionListener(FormEvent.ONCHANGE);
		excludedEl.setUserObject(row);
		row.setExcludedEl(excludedEl);
		
		updateExceptionalObligationRadioSelection(row);
		
		rows.add(row);
	}

	private void addExceptionalObligationRows(List<ExceptionalObligation> exceptionalObligations) {
		for (ExceptionalObligation exceptionalObligation : exceptionalObligations) {
			ExceptionalObligationRow row = new ExceptionalObligationRow(exceptionalObligation);
			
			// The exceptional obligation is obsolete if it has the same obligation as the default obligation
			if (selectedObligation != exceptionalObligation.getObligation()) {
				appendTypeName(row, exceptionalObligation);
				
				// If row has no name the exceptional obligation is obsolete, e.g. the user was deleted.
				if (StringHelper.containsNonWhitespace(row.getName())) {
					row.setObligation(exceptionalObligation.getObligation());
					
					SingleSelection mandatoryEl = uifactory.addRadiosHorizontal(MANDATORY_PREFIX + CodeHelper.getRAMUniqueID(),
							obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
					mandatoryEl.setAllowNoSelection(true);
					mandatoryEl.addActionListener(FormEvent.ONCHANGE);
					mandatoryEl.setUserObject(row);
					row.setMandatoryEl(mandatoryEl);
					
					SingleSelection optionalEl = uifactory.addRadiosHorizontal(OPTIONAL_PREFIX + CodeHelper.getRAMUniqueID(),
							obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
					optionalEl.setAllowNoSelection(true);
					optionalEl.addActionListener(FormEvent.ONCHANGE);
					optionalEl.setUserObject(row);
					row.setOptionalEl(optionalEl);
					
					SingleSelection excludedEl = uifactory.addRadiosHorizontal(EXCLUDED_PREFIX + CodeHelper.getRAMUniqueID(),
							obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
					excludedEl.setAllowNoSelection(true);
					excludedEl.addActionListener(FormEvent.ONCHANGE);
					excludedEl.setUserObject(row);
					row.setExcludedEl(excludedEl);
					
					updateExceptionalObligationRadioSelection(row);
					updateExceptionalObligationRadioVisiblity(row);
					
					String linkName = CMD_DELETE + CodeHelper.getRAMUniqueID();
					FormLink deleteLink = uifactory.addFormLink(linkName, CMD_DELETE, "delete", null, obligationCont, Link.LINK);
					deleteLink.setUserObject(row);
					row.setDeleteLink(deleteLink);
					
					allRows.add(row);
				}
			}
		}
		
		resetTable();
	}

	private void appendTypeName(ExceptionalObligationRow row, ExceptionalObligation exceptionalObligation) {
		ExceptionalObligationHandler exceptionalObligationHandler = learningPathService.getExceptionalObligationHandler(exceptionalObligation.getType());
		if (exceptionalObligationHandler != null) {
			row.setName(exceptionalObligationHandler.getDisplayName(getTranslator(), exceptionalObligation, courseEntry));
			row.setType(exceptionalObligationHandler.getDisplayType(getTranslator(), exceptionalObligation));
		}
	}
	
	private void resetTable() {
		List<ExceptionalObligationRow> rows = allRows.stream()
				.filter(this::isRowVisible)
				.collect(Collectors.toList());
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	private boolean isRowVisible(ExceptionalObligationRow row) {
		if (row.isDeleted()) return false;
		
		return row.isDefaultObligation() || row.getObligation() != selectedObligation;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == obligationEl) {
			selectedObligation = obligationEl.isOneSelected()
					? AssessmentObligation.valueOf(obligationEl.getSelectedKey())
					: LearningPathConfigs.OBLIGATION_DEFAULT;
			updateExceptionalObligations();
			updateUI();
			markDirty();
		} else if (source == showExceptionalObligationLink) {
			updateExceptionalObligationsUI(true);
		} else if (source == hideExceptionalObligationLink) {
			updateExceptionalObligationsUI(false);
		} else if (source == triggerEl) {
			updateUI();
			markDirty();
		} else if (source instanceof SingleSelection) {
			ExceptionalObligationRow row = (ExceptionalObligationRow)source.getUserObject();
			doUpdatedExceptionalObligation(row, source);
			markDirty();
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(CMD_DELETE.equals(cmd)) {
				ExceptionalObligationRow row = (ExceptionalObligationRow)source.getUserObject();
				doDeleteExceptionalObligation(row.getExceptionalObligation());
				markDirty();
			} else if (CMD_ADD_EXEPTIONAL_OBLIGATION.equals(cmd)){
				ExceptionalObligationHandler handler = (ExceptionalObligationHandler)link.getUserObject();
				doAddExceptionalObligations(ureq, handler);
				markDirty();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void markDirty() {
		String dirtyOnLoad = FormJSHelper.setFlexiFormDirtyOnLoad(flc.getRootForm());
		getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand(dirtyOnLoad));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (exceptionalObligationCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				List<ExceptionalObligation> exceptionalObligations = exceptionalObligationCreateCtrl.getExceptionalObligations();
				if (!exceptionalObligations.isEmpty()) {
					doInitExceptionalObligations(exceptionalObligations);
					doAddExceptionalObligations(exceptionalObligations);
					markDirty();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(exceptionalObligationCreateCtrl);
		removeAsListenerAndDispose(cmc);
		exceptionalObligationCreateCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateInteger(durationEl, 1, 10000, isDurationMandatory(), "error.positiv.int");
		allOk &= validateInteger(scoreCutEl, 0, 10000, true, "error.positiv.int");
		
		if (startDateEl.getDate() != null && endDateEl.getDate() != null) {
			Date start = startDateEl.getDate();
			Date end = endDateEl.getDate();
			if(end.before(start)) {
				endDateEl.setErrorKey("error.start.after.end", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	public static boolean validateInteger(TextElement el, int min, int max, boolean mandatory, String i18nKey) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					int value = Integer.parseInt(val);
					if(min > value) {
						allOk = false;
					} else if(max < value) {
						allOk = false;
					}
				} catch (NumberFormatException e) {
					allOk = false;
				}
			} else if (mandatory) {
				allOk = false;
			}
		}
		if (!allOk) {
			el.setErrorKey(i18nKey, null);
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		learningPathConfigs.setObligation(selectedObligation);
		
		List<ExceptionalObligation> exeptionalObligations = null;
		if (!obligationEl.isVisible()) {
			List<ExceptionalObligationRow> rows = dataModel.getObjects();
			exeptionalObligations = new ArrayList<>(rows.size());
			for (ExceptionalObligationRow row : rows) {
				if (row.isExceptionalObligation()) {
					ExceptionalObligation exceptionalObligation = row.getExceptionalObligation();
					exceptionalObligation.setObligation(row.getObligation());
					exeptionalObligations.add(exceptionalObligation);
				}
			}
		}
		learningPathConfigs.setExceptionalObligations(exeptionalObligations);
		loadExceptionalObligations();
		updateExceptionalObligationsUI((Boolean)obligationCont.contextGet("exceptional"));
		
		Date startDate = startDateEl.getDate();
		learningPathConfigs.setStartDate(startDate);
		
		if (!endDateEl.isVisible()) {
			endDateEl.setValue(null);
		}
		Date endDate = endDateEl.getDate();
		learningPathConfigs.setEndDate(endDate);
		
		Integer duration = StringHelper.containsNonWhitespace(durationEl.getValue())
				? Integer.valueOf(durationEl.getValue())
				: null;
		learningPathConfigs.setDuration(duration);
		
		FullyAssessedTrigger trigger = triggerEl.isOneSelected()
				? FullyAssessedTrigger.valueOf(triggerEl.getSelectedKey())
				: LearningPathConfigs.LEGACY_TRIGGER_DEFAULT;
		learningPathConfigs.setFullyAssessedTrigger(trigger);
		
		Integer score = scoreCutEl.isVisible()? Integer.valueOf(scoreCutEl.getValue()): null;
		learningPathConfigs.setScoreTriggerValue(score);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private boolean isDurationMandatory() {
		return CompletionType.duration.equals(course.getCourseConfig().getCompletionType())
				&& hasMandatoryObligation();
	}

	private boolean hasMandatoryObligation() {
		if (AssessmentObligation.mandatory == selectedObligation) return true;
		
		if (!obligationEl.isVisible()) {
			for (ExceptionalObligationRow row : dataModel.getObjects()) {
				if (AssessmentObligation.mandatory == row.getObligation()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void doAddExceptionalObligations(UserRequest ureq, ExceptionalObligationHandler handler) {
		guardModalController(exceptionalObligationCreateCtrl);
		
		exceptionalObligationCreateCtrl = handler.createCreationController(ureq, getWindowControl(), courseEntry, courseNode);
		listenTo(exceptionalObligationCreateCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", exceptionalObligationCreateCtrl.getInitialComponent(), true,
				translate("config.exceptional.obligation.add"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doInitExceptionalObligations(List<ExceptionalObligation> exceptionalObligations) {
		AssessmentObligation defaultObligation = AssessmentObligation.mandatory;
		if (AssessmentObligation.mandatory == selectedObligation) {
			defaultObligation = AssessmentObligation.optional;
		}
		for (ExceptionalObligation exceptionalObligation : exceptionalObligations) {
			exceptionalObligation.setIdentifier(UUID.randomUUID().toString());
			exceptionalObligation.setObligation(defaultObligation);
		}
	}

	private void doAddExceptionalObligations(List<ExceptionalObligation> exceptionalObligations) {
		addExceptionalObligationRows(exceptionalObligations);
	}

	private void updateExceptionalObligations() {
		for (ExceptionalObligationRow row : allRows) {
			updateExceptionalObligationRadioSelection(row);
			updateExceptionalObligationRadioVisiblity(row);
		}
		
		resetTable();
	}

	private void doUpdatedExceptionalObligation(ExceptionalObligationRow row, FormItem source) {
		if (row.isDefaultObligation()) {
			if (source.getName().startsWith(MANDATORY_PREFIX)) {
				selectedObligation =  AssessmentObligation.mandatory;
			} else if (source.getName().startsWith(OPTIONAL_PREFIX)) {
				selectedObligation =  AssessmentObligation.optional;
			} else if (source.getName().startsWith(EXCLUDED_PREFIX)) {
				selectedObligation =  AssessmentObligation.excluded;
			}
			updateExceptionalObligations();
			updateUI();
		} else {
			if (source.getName().startsWith(MANDATORY_PREFIX)) {
				row.setObligation(AssessmentObligation.mandatory);
			} else if (source.getName().startsWith(OPTIONAL_PREFIX)) {
				row.setObligation(AssessmentObligation.optional);
			} else if (source.getName().startsWith(EXCLUDED_PREFIX)) {
				row.setObligation(AssessmentObligation.excluded);
			}
		}
		updateExceptionalObligationRadioSelection(row);
	}
	
	private void updateExceptionalObligationRadioSelection(ExceptionalObligationRow row) {
		AssessmentObligation obligation = row.isExceptionalObligation()
				? row.getObligation()
				: selectedObligation;
		row.getMandatoryEl().select(row.getMandatoryEl().getKey(0), AssessmentObligation.mandatory == obligation);
		row.getOptionalEl().select(row.getOptionalEl().getKey(0), AssessmentObligation.optional == obligation);
		row.getExcludedEl().select(row.getExcludedEl().getKey(0), AssessmentObligation.excluded == obligation);
	}
	
	private void updateExceptionalObligationRadioVisiblity(ExceptionalObligationRow row) {
		if (row.isDefaultObligation()) return;
		
		row.getMandatoryEl().setVisible(AssessmentObligation.mandatory != selectedObligation);
		row.getOptionalEl().setVisible(AssessmentObligation.optional != selectedObligation);
		row.getExcludedEl().setVisible(AssessmentObligation.excluded != selectedObligation);
	}
	
	private void doDeleteExceptionalObligation(ExceptionalObligation exceptionalObligation) {
		for (ExceptionalObligationRow row : allRows) {
			if (hasSameIdentifer(row, exceptionalObligation)) {
				row.setDeleted(true);
			}
		}
		resetTable();
		updateExceptionalObligationsUI(true);
	}

	private boolean hasSameIdentifer(ExceptionalObligationRow row, ExceptionalObligation exceptionalObligation) {
		return row.isExceptionalObligation()
				&& row.getExceptionalObligation().getIdentifier() != null
				&& row.getExceptionalObligation().getIdentifier().equals(exceptionalObligation.getIdentifier());
	}
	
}

