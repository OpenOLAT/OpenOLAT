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
package org.olat.course.nodes.st.assessment;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.editor.NodeEditController;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.learningpath.obligation.ExceptionalObligationController;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.learningpath.ui.ExceptionalObligationDataModel;
import org.olat.course.learningpath.ui.ExceptionalObligationDataModel.ExceptionalObligationCols;
import org.olat.course.learningpath.ui.ExceptionalObligationRow;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLearningPathConfigController extends FormBasicController {
	
	private static final String CMD_ADD_EXEPTIONAL_OBLIGATION = "add.exeptional.obligation";
	
	private static final String[] EXCEPTIONAL_OBLIGATION_KEYS = { "on" };
	private static final String[] EXCEPTIONAL_OBLIGATION_VALUES = { "" };
	private static final String EVALUATED_PREFIX = "eo_evaluated_";
	private static final String EXCLUDED_PREFIX = "eo_excluded_";
	private static final String CMD_DELETE = "delete";

	private SingleSelection sequenceEl;
	private SingleSelection obligationEl;
	private FormLayoutContainer obligationCont;
	private ExceptionalObligationDataModel dataModel;
	private FlexiTableElement tableEl;
	private FormLink showExceptionalObligationLink;
	private FormLink hideExceptionalObligationLink;
	
	private CloseableModalController cmc;
	private ExceptionalObligationController exceptionalObligationCreateCtrl;
	
	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;
	private final LearningPathConfigs learningPathConfigs;
	private final ModuleConfiguration moduleConfig;
	private AssessmentObligation selectedObligation;
	private List<ExceptionalObligationRow> allRows;
	
	@Autowired
	private LearningPathService learningPathService;

	public STLearningPathConfigController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, CourseNode courseNode) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(STCourseNodeEditController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.moduleConfig = courseNode.getModuleConfiguration();
		CourseEditorTreeNode editorTreeNode = CourseFactory.loadCourse(courseEntry).getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
		this.learningPathConfigs = learningPathService.getConfigs(courseNode, editorTreeNode.getParent());
		this.selectedObligation = learningPathConfigs.getObligation() != null
				? learningPathConfigs.getObligation()
				: AssessmentObligation.evaluated;
		
		initForm(ureq);
		loadExceptionalObligations();
		updateExceptionalObligationsUI(allRows.size() > 1); // One for the default obligation
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.title");
		setFormContextHelp("Learning Path");
		formLayout.setElementCssClass("o_lp_config_edit");
		
		SelectionValues sequenceKV = new SelectionValues();
		sequenceKV.add(SelectionValues.entry(STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL, translate("config.sequence.sequential")));
		sequenceKV.add(SelectionValues.entry(STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT, translate("config.sequence.without")));
		sequenceEl = uifactory.addRadiosHorizontal("config.sequence", formLayout, sequenceKV.keys(), sequenceKV.values());
		sequenceEl.addActionListener(FormEvent.ONCHANGE);
		String sequenceKey = moduleConfig.getStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY, STLearningPathConfigs.CONFIG_LP_SEQUENCE_DEFAULT);
		sequenceEl.select(sequenceKey, true);
		
		String page = Util.getPackageVelocityRoot(LearningPathNodeConfigController.class) + "/config_obligation.html";
		obligationCont = FormLayoutContainer.createCustomFormLayout("obligationCont", getTranslator(), page);
		obligationCont.setLabel("config.obligation", null);
		obligationCont.setRootForm(mainForm);
		formLayout.add(obligationCont);
		
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(AssessmentObligation.evaluated.name(), translate("config.obligation.evaluated")));
		obligationKV.add(entry(AssessmentObligation.excluded.name(), translate("config.obligation.excluded")));
		obligationEl = uifactory.addRadiosHorizontal("config.obligation", obligationCont, obligationKV.keys(), obligationKV.values());
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		String obligationKey = selectedObligation.name();
		if (Arrays.asList(obligationEl.getKeys()).contains(obligationKey)) {
			obligationEl.select(obligationKey, true);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.evaluated));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.excluded));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExceptionalObligationCols.delete));
		
		dataModel = new ExceptionalObligationDataModel(columnsModel);
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
		
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	private void addHandlerToDropdown(DropdownItem dropdown, ExceptionalObligationHandler handler) {
		if (handler.isShowAdd(courseEntry)) {
			FormLink link = uifactory.addFormLink(handler.getType(), CMD_ADD_EXEPTIONAL_OBLIGATION, handler.getAddI18nKey(), null, obligationCont, Link.LINK);
			link.setUserObject(handler);
			dropdown.addElement(link);
		}
	}
	
	private void updateExceptionalObligationsUI(boolean showExceptional) {
		obligationEl.select(selectedObligation.name(), true);
		obligationEl.setVisible(!showExceptional);
		flc.setDirty(true);
		
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
		SingleSelection evaluatedEl = uifactory.addRadiosHorizontal(EVALUATED_PREFIX + "default", null,
				obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
		evaluatedEl.setAllowNoSelection(true);
		evaluatedEl.addActionListener(FormEvent.ONCHANGE);
		evaluatedEl.setUserObject(row);
		row.setEvaluatedEl(evaluatedEl);
		
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
					
					SingleSelection evaluatedEl = uifactory.addRadiosHorizontal(EVALUATED_PREFIX + CodeHelper.getRAMUniqueID(),
							obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
					evaluatedEl.setAllowNoSelection(true);
					evaluatedEl.setUserObject(row);
					row.setEvaluatedEl(evaluatedEl);
					
					SingleSelection excludedEl = uifactory.addRadiosHorizontal(EXCLUDED_PREFIX + CodeHelper.getRAMUniqueID(),
							obligationCont, EXCEPTIONAL_OBLIGATION_KEYS, EXCEPTIONAL_OBLIGATION_VALUES);
					excludedEl.setAllowNoSelection(true);
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
					: AssessmentObligation.evaluated;
			updateExceptionalObligations();
			markDirty();
		} else if (source == showExceptionalObligationLink) {
			updateExceptionalObligationsUI(true);
			markDirty();
		} else if (source == hideExceptionalObligationLink) {
			updateExceptionalObligationsUI(false);
			markDirty();
		} else if (source instanceof SingleSelection) {
			if (source.getName().startsWith("eo_")) {
				ExceptionalObligationRow row = (ExceptionalObligationRow)source.getUserObject();
				doUpdatedExceptionalObligation(row, source);
			}
			// Sequence and exeptional obligations
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
	protected void formOK(UserRequest ureq) {
		String sequenceKey = sequenceEl.getSelectedKey();
		moduleConfig.setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY, sequenceKey);
		
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
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
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
		AssessmentObligation defaultObligation = AssessmentObligation.evaluated == selectedObligation
				? AssessmentObligation.excluded
				: AssessmentObligation.evaluated;
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
			if (source.getName().startsWith(EVALUATED_PREFIX)) {
				selectedObligation =  AssessmentObligation.evaluated;
			} else if (source.getName().startsWith(EXCLUDED_PREFIX)) {
				selectedObligation =  AssessmentObligation.excluded;
			}
			updateExceptionalObligations();
		}
		updateExceptionalObligationRadioSelection(row);
	}
	
	private void updateExceptionalObligationRadioSelection(ExceptionalObligationRow row) {
		AssessmentObligation obligation = row.isExceptionalObligation()
				? row.getObligation()
				: selectedObligation;
		row.getEvaluatedEl().select(row.getEvaluatedEl().getKey(0), AssessmentObligation.evaluated == obligation);
		row.getExcludedEl().select(row.getExcludedEl().getKey(0), AssessmentObligation.excluded == obligation);
	}
	
	private void updateExceptionalObligationRadioVisiblity(ExceptionalObligationRow row) {
		if (row.isDefaultObligation()) return;
		
		row.getEvaluatedEl().setVisible(AssessmentObligation.evaluated != selectedObligation);
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
