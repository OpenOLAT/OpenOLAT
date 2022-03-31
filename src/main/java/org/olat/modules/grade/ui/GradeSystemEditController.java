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
package org.olat.modules.grade.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.modules.grade.ui.GradeUIFactory.THREE_DIGITS;
import static org.olat.modules.grade.ui.GradeUIFactory.translateResolution;
import static org.olat.modules.grade.ui.GradeUIFactory.translateRounding;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.Rounding;
import org.olat.modules.grade.ui.PerformanceClassDataModel.PerformanceClassCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeSystemEditController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	private static final String CMD_TRANSLATE_NAME = "translate";
	private static final String CMD_DELETE = "delete";
	
	private StaticTextElement systemNameEl;
	private FormLink systemNameLink;
	private MultipleSelectionElement enabledEl;
	private SingleSelection typeEl;
	private SingleSelection resolutionEl;
	private SingleSelection roundingEl;
	private TextElement bestGradeEl;
	private TextElement lowestGradeEl;
	private TextElement cutValueEl;
	private FormLayoutContainer performanceClassCont;
	private FormLink addButton;
	private PerformanceClassDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController systemTranslatorCtrl;
	private SingleKeyTranslatorController translatorCtrl;
	
	private GradeSystem gradeSystem;
	private boolean hasScale;
	private final boolean predefined;
	private List<PerformanceClassRow> performanceClassRows;
	
	@Autowired
	private GradeService gradeService;

	public GradeSystemEditController(UserRequest ureq, WindowControl wControl, GradeSystem gradeSystem) {
		super(ureq, wControl);
		this.gradeSystem = gradeSystem;
		hasScale = gradeService.hasGradeScale(gradeSystem);
		predefined = gradeSystem.isPredefined();
		
		initForm(ureq);
		loadPerformanceClasses();
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("grade.system.identifier", gradeSystem.getIdentifier(), formLayout);
		
		FormLayoutContainer nameCont = FormLayoutContainer.createButtonLayout("nameCont", getTranslator());
		nameCont.setLabel("grade.system.name", null);
		nameCont.setElementCssClass("o_inline_cont");
		nameCont.setRootForm(mainForm);
		formLayout.add(nameCont);
		
		String translateGradeSystem = GradeUIFactory.translateGradeSystem(getTranslator(), gradeSystem);
		systemNameEl = uifactory.addStaticTextElement("grade.system.name", null, translateGradeSystem, nameCont);
		
		if (!predefined) {
			systemNameLink = uifactory.addFormLink("grade.system.name.edit", nameCont);
		}
		
		String[] onValues = new String[]{ translate("on") };
		enabledEl = uifactory.addCheckboxesHorizontal("grade.system.enabled", formLayout, onKeys, onValues);
		enabledEl.select(onKeys[0], gradeSystem == null || gradeSystem.isEnabled());
		
		SelectionValues typeSV = new SelectionValues();
		typeSV.add(entry(GradeSystemType.numeric.name(), translate("grade.system.type.numeric")));
		typeSV.add(entry(GradeSystemType.text.name(), translate("grade.system.type.text")));
		typeEl = uifactory.addDropdownSingleselect("grade.system.type", formLayout, typeSV.keys(), typeSV.values());
		typeEl.setMandatory(true);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String typeKey = gradeSystem != null && gradeSystem.getType() != null
				? gradeSystem.getType().name()
				: GradeSystemType.numeric.name();
		typeEl.select(typeKey, true);
		typeEl.setEnabled(!hasScale && !predefined);
		
		SelectionValues resolutionSV = new SelectionValues();
		resolutionSV.add(entry(NumericResolution.whole.name(), translateResolution(getTranslator(), NumericResolution.whole)));
		resolutionSV.add(entry(NumericResolution.half.name(), translateResolution(getTranslator(), NumericResolution.half)));
		resolutionSV.add(entry(NumericResolution.quarter.name(), translateResolution(getTranslator(), NumericResolution.quarter)));
		resolutionSV.add(entry(NumericResolution.tenth.name(), translateResolution(getTranslator(), NumericResolution.tenth)));
		resolutionEl = uifactory.addDropdownSingleselect("grade.system.resolution", formLayout, resolutionSV.keys(), resolutionSV.values());
		resolutionEl.setMandatory(true);
		String resolutionKey = gradeSystem != null && gradeSystem.getResolution() != null
				? gradeSystem.getResolution().name()
				: NumericResolution.half.name();
		resolutionEl.select(resolutionKey, true);
		resolutionEl.setEnabled(!hasScale && !predefined);
		
		SelectionValues roundingSV = new SelectionValues();
		roundingSV.add(entry(Rounding.nearest.name(), translateRounding(getTranslator(), Rounding.nearest)));
		roundingSV.add(entry(Rounding.up.name(), translateRounding(getTranslator(), Rounding.up)));
		roundingSV.add(entry(Rounding.down.name(), translateRounding(getTranslator(), Rounding.down)));
		roundingEl = uifactory.addDropdownSingleselect("grade.system.rounding", formLayout, roundingSV.keys(), roundingSV.values());
		roundingEl.setMandatory(true);
		String roundingKey = gradeSystem != null && gradeSystem.getResolution() != null
				? gradeSystem.getRounding().name()
				: Rounding.nearest.name();
		roundingEl.select(roundingKey, true);
		roundingEl.setEnabled(!hasScale && !predefined);
		
		String bestGrade = gradeSystem != null && gradeSystem.getBestGrade() != null? THREE_DIGITS.format(gradeSystem.getBestGrade()): null;
		bestGradeEl = uifactory.addTextElement("grade.system.best.grade", 10, bestGrade, formLayout);
		bestGradeEl.setMandatory(true);
		
		String lowestGrade = gradeSystem != null && gradeSystem.getLowestGrade() != null? THREE_DIGITS.format(gradeSystem.getLowestGrade()): null;
		lowestGradeEl = uifactory.addTextElement("grade.system.lowest.grade", 10, lowestGrade, formLayout);
		lowestGradeEl.setMandatory(true);
		
		String cutValue = gradeSystem != null && gradeSystem.getCutValue() != null? THREE_DIGITS.format(gradeSystem.getCutValue()): null;
		cutValueEl = uifactory.addTextElement("grade.system.cut.value", 10, cutValue, formLayout);
		
		performanceClassCont = FormLayoutContainer.createCustomFormLayout("pcCont", getTranslator(), velocity_root + "/performance_classes.html");
		performanceClassCont.setLabel("performance.classes", null);
		performanceClassCont.setRootForm(mainForm);
		formLayout.add(performanceClassCont);
		
		addButton = uifactory.addFormLink("performance.class.add", performanceClassCont, Link.BUTTON);
		addButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassCols.position));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassCols.markPassed));
		if (!hasScale && !predefined) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CMD_DELETE, -1, CMD_DELETE,
				new StaticFlexiCellRenderer(translate("performance.class.delete"), CMD_DELETE, "", "", null)));
		}
		
		dataModel = new PerformanceClassDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), performanceClassCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void updateUI() {
		boolean numeric = GradeSystemType.numeric == GradeSystemType.valueOf(typeEl.getSelectedKey());
		resolutionEl.setEnabled(!hasScale && !predefined);
		resolutionEl.setVisible(numeric);
		roundingEl.setEnabled(!hasScale && !predefined);
		roundingEl.setVisible(numeric);
		bestGradeEl.setEnabled(!hasScale && !predefined);
		bestGradeEl.setVisible(numeric);
		lowestGradeEl.setEnabled(!hasScale && !predefined);
		lowestGradeEl.setVisible(numeric);
		cutValueEl.setEnabled(!hasScale && !predefined);
		cutValueEl.setVisible(numeric);
		
		performanceClassCont.setVisible(!numeric);
		addButton.setVisible(numeric && !predefined);
		for (PerformanceClassRow row : performanceClassRows) {
			row.getMarkPassedEl().setEnabled(!hasScale && !predefined);
			row.getNameLink().setEnabled(!predefined);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == systemTranslatorCtrl) {
			systemNameEl.setValue(GradeUIFactory.translateGradeSystem(getTranslator(), gradeSystem));
			cmc.deactivate();
			cleanUp();
		} else if (source == translatorCtrl) {
			updateNameLink((PerformanceClassRow)translatorCtrl.getUserObject());
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(systemTranslatorCtrl);
		removeAsListenerAndDispose(translatorCtrl);
		removeAsListenerAndDispose(cmc);
		systemTranslatorCtrl = null;
		translatorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == typeEl) {
			updateUI();
		} else if (source == systemNameLink) {
			doTranslateGradeSystem(ureq);
		} else if (source == addButton) {
			doAddPerformanceClass();
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_TRANSLATE_NAME.equals(cmd)) {
				PerformanceClassRow performanceClassRow = (PerformanceClassRow)link.getUserObject();
				doTranslatePerformanceClass(ureq, performanceClassRow);
			} 
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if (CMD_DELETE.equals(cmd)) {
					PerformanceClassRow performanceClassRow = dataModel.getObject(se.getIndex());
					doDelete(performanceClassRow);
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= GradeUIFactory.validateInteger(bestGradeEl);
		allOk &= GradeUIFactory.validateInteger(lowestGradeEl);
		
		cutValueEl.clearError();
		if (GradeSystemType.numeric == GradeSystemType.valueOf(typeEl.getSelectedKey())) {
			NumericResolution resolution = NumericResolution.valueOf(resolutionEl.getSelectedKey());
			allOk &= GradeUIFactory.validateCutValue(cutValueEl, resolution);
			
		}
		
		tableEl.clearError();
		if (performanceClassCont.isVisible() && performanceClassRows.isEmpty()) {
			tableEl.setErrorKey("error.performance.class.madatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		hasScale = gradeService.hasGradeScale(gradeSystem);
		
		gradeSystem.setEnabled(enabledEl.isAtLeastSelected(1));
		
		if (!hasScale && !predefined) {
			GradeSystemType gradeSystemType = GradeSystemType.valueOf(typeEl.getSelectedKey());
			gradeSystem.setType(gradeSystemType);
			boolean numeric = GradeSystemType.numeric == gradeSystemType;
			
			NumericResolution resolution = numeric? NumericResolution.valueOf(resolutionEl.getSelectedKey()): null;
			gradeSystem.setResolution(resolution);
			
			Rounding rounding = numeric? Rounding.valueOf(roundingEl.getSelectedKey()): null;
			gradeSystem.setRounding(rounding);
			
			Integer bestGrade = numeric? Integer.valueOf(bestGradeEl.getValue()): null;
			gradeSystem.setBestGrade(bestGrade);
			
			Integer lowestGrade = numeric? Integer.valueOf(lowestGradeEl.getValue()): null;
			gradeSystem.setLowestGrade(lowestGrade);
			
			BigDecimal cutValue = numeric && StringHelper.containsNonWhitespace(cutValueEl.getValue())
					? new BigDecimal(cutValueEl.getValue())
					: null;
			gradeSystem.setCutValue(cutValue);
			
			if (numeric) {
				gradeService.deletePerformanceClasses(gradeSystem);
			} else {
				savePerformanceClasses();
			}
		}
		
		gradeSystem = gradeService.updateGradeSystem(gradeSystem);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doTranslateGradeSystem(UserRequest ureq) {
		if (guardModalController(systemTranslatorCtrl))
			return;

		String i18nKey = GradeUIFactory.getGradeSystemI18nKey(gradeSystem);

		systemTranslatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), i18nKey,
				GradeSystemEditController.class);
		listenTo(systemTranslatorCtrl);

		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), "close", systemTranslatorCtrl.getInitialComponent(), true,
				translate("grade.system.name"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void loadPerformanceClasses() {
		List<PerformanceClass> performanceClasses = gradeService.getPerformanceClasses(gradeSystem);
		Collections.sort(performanceClasses);
		performanceClassRows = new ArrayList<>(performanceClasses.size());
		for (PerformanceClass performanceClass : performanceClasses) {
			PerformanceClassRow row = createRow(performanceClass, performanceClass.getBestToLowest(),
					performanceClass.isPassed());
			performanceClassRows.add(row);
		}
		if (performanceClasses.isEmpty()) {
			performanceClassRows = new ArrayList<>(6);
			performanceClassRows.add(createRow(null, Integer.valueOf(1), false));
		}
		
		dataModel.setObjects(performanceClassRows);
		tableEl.reset();
	}

	private void doAddPerformanceClass() {
		Integer position = performanceClassRows.isEmpty()
				? Integer.valueOf(0)
				: Integer.valueOf(performanceClassRows.get(performanceClassRows.size()-1).getPosition().intValue() + 1);
		PerformanceClassRow row = createRow(null, position, false);
		performanceClassRows.add(row);
		
		dataModel.setObjects(performanceClassRows);
		tableEl.reset();
	}
	
	private PerformanceClassRow createRow(PerformanceClass performanceClass, Integer position, boolean passed) {
		PerformanceClassRow row = new PerformanceClassRow(performanceClass);
		row.setPosition(position);
		
		String identifier = performanceClass != null
				? performanceClass.getIdentifier()
				: UUID.randomUUID().toString().toLowerCase().replace("-", "");
		row.setIdentifier(identifier);
		
		FormLink nameLink = uifactory.addFormLink("name_" + identifier, CMD_TRANSLATE_NAME, "", null, null, Link.NONTRANSLATED);
		nameLink.setUserObject(row);
		row.setNameLink(nameLink);
		updateNameLink(row);
		
		MultipleSelectionElement markPassedEl = uifactory.addCheckboxesHorizontal(
				"passed_" + row.getPosition(), null, performanceClassCont, onKeys, onValues);
		markPassedEl.setDomReplacementWrapperRequired(false);
		markPassedEl.select(onKeys[0], passed);
		row.setMarkPassedEl(markPassedEl);
		
		return row;
	}

	private void savePerformanceClasses() {
		List<PerformanceClass> performanceClassToDelete = gradeService.getPerformanceClasses(gradeSystem);
		for (PerformanceClassRow row : performanceClassRows) {
			PerformanceClass performanceClass = row.getPerformanceClass();
			if (performanceClass == null) {
				performanceClass = gradeService.createPerformanceClass(gradeSystem, row.getIdentifier());
			} else {
				performanceClassToDelete.remove(performanceClass);
			}
			
			performanceClass.setBestToLowest(row.getPosition());
			performanceClass.setPassed(row.getMarkPassedEl().isAtLeastSelected(1));
			gradeService.updatePerformanceClass(performanceClass);
		}
		
		performanceClassToDelete.forEach(gradeService::deletePerformanceClass);
	}

	private void doTranslatePerformanceClass(UserRequest ureq, PerformanceClassRow performanceClassRow) {
		if (guardModalController(translatorCtrl))
			return;

		String i18nKey = GradeUIFactory.getPerformanceClassI18nKey(performanceClassRow.getIdentifier());

		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), i18nKey,
				GradeSystemEditController.class);
		listenTo(translatorCtrl);
		translatorCtrl.setUserObject(performanceClassRow);
		translatorCtrl.setEnabled(!hasScale);

		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), "close", translatorCtrl.getInitialComponent(), true,
				translate("performance.class.name"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void updateNameLink(PerformanceClassRow row) {
		String translatedName = row.getPerformanceClass() != null
				? GradeUIFactory.translatePerformanceClass(getTranslator(), row.getPerformanceClass())
				: GradeUIFactory.translatePerformanceClass(getTranslator(), row.getIdentifier(), row.getPosition().toString());
		row.getNameLink().setI18nKey(translatedName);
	}
	
	private void doDelete(PerformanceClassRow performanceClassRow) {
		performanceClassRows.remove(performanceClassRow);
		dataModel.setObjects(performanceClassRows);
		tableEl.reset();
	}

}
