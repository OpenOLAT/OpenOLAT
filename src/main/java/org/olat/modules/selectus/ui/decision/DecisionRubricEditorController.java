/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.decision;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
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
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.DecisionRubricSPI;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.decision.DecisionRubricEditorDataModel.RubricDefCols;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionRubricEditorController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[] { "" };
	
	private final String[] typeKeys;
	private final String[] typeValues;
	private final String[] weightKeys;
	private final String[] weightValues;
	
	private FormLink addButton;
	private FlexiTableElement tableEl;
	private DecisionRubricEditorDataModel dataModel;
	
	private int count = 0;
	private Position position;
	private final List<DecisionRubricSPI> decisionRubricSpies;
	private final List<RubricDefinitionRow> wrapperToDelete = new ArrayList<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingFrontendManager;
	
	public DecisionRubricEditorController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, "decision_editor");
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale()));
		this.position = position;
		decisionRubricSpies = recruitingModule.getDecisionRubricSpies();
		
		int numOfDecisionRubricSpies = decisionRubricSpies.size();
		typeKeys = new String[numOfDecisionRubricSpies];
		typeValues = new String[numOfDecisionRubricSpies];
		for(int i=0; i<numOfDecisionRubricSpies; i++) {
			DecisionRubricSPI rubricSpi = decisionRubricSpies.get(i);
			typeKeys[i] = rubricSpi.getKey();
			typeValues[i] = rubricSpi.getName();
		}
		
		weightKeys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
		weightValues = weightKeys;

		initForm(ureq);
		loadModel();
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricDefCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricDefCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricDefCols.sum));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricDefCols.weight));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricDefCols.up));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricDefCols.down));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricDefCols.delete));

		dataModel = new DecisionRubricEditorDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_decision_definition_table");
		
		FormSubmit saveButton = uifactory.addFormSubmitButton("save.and.close", formLayout);
		saveButton.setElementCssClass("o_sel_decision_definition_save");
		addButton = uifactory.addFormLink("rubric.add", formLayout, Link.BUTTON);
		addButton.setElementCssClass("o_sel_decision_add_rubric");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void loadModel() {
		List<RubricDefinitionRow> wrappers = new ArrayList<>();
		List<DecisionRubricDefinition> rubricDefs = recruitingFrontendManager.getDecisionRubricDefinition(position);
		for(DecisionRubricDefinition rubricDef:rubricDefs) {
			wrappers.add(forgeRubricRow(rubricDef));
		}
		dataModel.setObjects(wrappers);
		tableEl.reset();
		tableEl.reloadData();
		updateUpDownButtons();
	}
	
	private RubricDefinitionRow forgeRubricRow(DecisionRubricDefinition rubric) {
		String name = rubric == null ? "" : rubric.getRubric();
		TextElement nameEl = uifactory.addTextElement("name_" + (++count), "rubric.displayname", 128, name, flc);
		
		SingleSelection typeEl = uifactory.addDropdownSingleselect("type_" + (++count), "rubric.type", flc, typeKeys, typeValues, null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		
		boolean typed = false;
		if(StringHelper.containsNonWhitespace(rubric.getType())) {
			for(String typeKey:typeKeys) {
				if(typeKey.equals(rubric.getType())) {
					typeEl.select(typeKey, true);
					typed = true;
				}
			}
		}
		if(!typed) {
			typeEl.select(typeKeys[0], true);
		}
		
		MultipleSelectionElement sumEl = uifactory.addCheckboxesHorizontal("sum_" + (++count), "rubric.sum", flc, onKeys, onValues);
		sumEl.addActionListener(FormEvent.ONCHANGE);
		if(rubric.isSum()) {
			sumEl.select(onKeys[0], true);
		}
		
		SingleSelection weightEl = uifactory.addDropdownSingleselect("weight_" + (++count), "rubric.weight", flc, weightKeys, weightValues, null);
		weightEl.addActionListener(FormEvent.ONCHANGE);
		boolean weighted = false;
		if(rubric.getWeight() > 0) {
			String selectedWeight = Integer.toString(rubric.getWeight());
			for(String weightKey:weightKeys) {
				if(weightKey.equals(selectedWeight)) {
					weightEl.select(weightKey, true);
					weighted = true;
				}
			}
		}
		if(!weighted) {
			weightEl.select(weightKeys[0], true);
		}
		
		FormLink removeButton = uifactory.addFormLink("del_" + (count++), "delete", "table.header.rubric.delete", null,
				flc, Link.NONTRANSLATED);
		removeButton.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
		removeButton.setI18nKey("");
		FormLink upButton = uifactory.addFormLink("up_" + (count++), "up", "table.header.rubric.up", null,
				flc, Link.NONTRANSLATED);
		upButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		upButton.setI18nKey("");
		FormLink downButton = uifactory.addFormLink("down_" + (count++), "down", "table.header.rubric.down", null,
				flc, Link.NONTRANSLATED);
		downButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		downButton.setI18nKey("");
		
		RubricDefinitionRow row = new RubricDefinitionRow(rubric, nameEl, typeEl, sumEl, weightEl,
				removeButton, upButton, downButton);
		typeEl.setUserObject(row);
		removeButton.setUserObject(row);
		upButton.setUserObject(row);
		downButton.setUserObject(row);
		updateRow(row);
		return row;
	}
	
	private void updateRow(RubricDefinitionRow row) {
		SingleSelection selectionEl = row.getTypeEl();
		boolean enabled = !selectionEl.getSelectedKey().equals("text");
		row.getSumEl().setEnabled(enabled);
		row.getWeightEl().setEnabled(enabled);
		if(!enabled) {
			row.getSumEl().uncheckAll();
			row.getWeightEl().select(weightKeys[0], true);
		}
	}
	
	private void updateUpDownButtons() {
		List<RubricDefinitionRow> rows = dataModel.getObjects();
		for(int i=0; i<rows.size(); i++) {
			rows.get(i).getUpButton().setVisible(i > 0);
			rows.get(i).getDownButton().setVisible(i < rows.size() - 1);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addButton == source) {
			doAddRubric(dataModel.getRowCount());
		} else if(source instanceof SingleSelection) {
			SingleSelection selectionEl = (SingleSelection)source;
			if(selectionEl.getName().startsWith("type_")) {
				updateRow((RubricDefinitionRow)selectionEl.getUserObject());
			}
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			if("add".equals(button.getCmd())) {
				int index = dataModel.getObjects().indexOf(button.getUserObject());
				doAddRubric(index);
			} else if("delete".equals(button.getCmd())) {
				int index = dataModel.getObjects().indexOf(button.getUserObject());
				doDeleteRubric(index);
			} else if("up".equals(button.getCmd())) {
				int index = dataModel.getObjects().indexOf(button.getUserObject());
				doMoveRubricUp(index);
			} else if("down".equals(button.getCmd())) {
				int index = dataModel.getObjects().indexOf(button.getUserObject());
				doMoveRubricDown(index);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(RubricDefinitionRow wrapper:dataModel.getObjects()) {
			wrapper.getNameEl().clearError();
			if(!StringHelper.containsNonWhitespace(wrapper.getNameEl().getValue())) {
				wrapper.getNameEl().setErrorKey("form.general.error");
				allOk &= false;
			}
			
			wrapper.getTypeEl().clearError();
			if(!wrapper.getTypeEl().isOneSelected()) {
				wrapper.getTypeEl().setErrorKey("form.general.error");
				allOk &= false;
			}
			
			wrapper.getWeightEl().clearError();
			if(!wrapper.getWeightEl().isOneSelected()) {
				wrapper.getWeightEl().setErrorKey("form.general.error");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		position = recruitingFrontendManager.getPosition(position.getKey());
		
		List<RubricDefinitionRow> wrappers = dataModel.getObjects();
		for(int i=0; i<wrappers.size(); i++) {
			RubricDefinitionRow wrapper = wrappers.get(i);
			DecisionRubricDefinition def = wrapper.commit();
			def.setPos(i);
			if(def.getKey() == null) {
				logAudit("Rubric definition added: " + def, null);
			}
			recruitingFrontendManager.saveDecisionRubricDefinition(def, position);
		}
		for(RubricDefinitionRow wrapper:wrapperToDelete) {
			if(wrapper.getDefinition() == null) {
				logAudit("Rubric definition deleted: " + wrapper.getDefinition(), null);
			}
			recruitingFrontendManager.deleteDecisionRubricDefinition(wrapper.getDefinition());
		}
		
		position = recruitingFrontendManager.savePosition(position);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doAddRubric(int row) {
		DecisionRubricDefinition rubricDef = recruitingFrontendManager.createDecisionRubricDefinition();
		RubricDefinitionRow wrapper = forgeRubricRow(rubricDef);
		List<RubricDefinitionRow> wrappers = dataModel.getObjects();
		if(row + 1 < wrappers.size()) {
			wrappers.add(row + 1, wrapper);
		} else {
			wrappers.add(wrapper);
		}
		dataModel.setObjects(wrappers);
		updateUpDownButtons();
		tableEl.reset();
		tableEl.reloadData();
	}
	
	private void doDeleteRubric(int index) {
		if(index < 0 || index >= dataModel.getRowCount()) return;
		
		List<RubricDefinitionRow> wrappers = dataModel.getObjects();
		RubricDefinitionRow row = wrappers.remove(index);
		dataModel.setObjects(wrappers);
		updateUpDownButtons();
		tableEl.reset();
		tableEl.reloadData();

		if(row.getDefinition().getKey() != null) {
			wrapperToDelete.add(row);
		}
	}
	
	private void doMoveRubricUp(int index) {
		List<RubricDefinitionRow> rows = dataModel.getObjects();
		if(index > 0 && index < rows.size()) {
			RubricDefinitionRow row = rows.remove(index);
			rows.add(index -1, row);
			dataModel.setObjects(rows);
			tableEl.reset();
			tableEl.reloadData();
			updateUpDownButtons();
		}
	}
	
	private void doMoveRubricDown(int index) {
		List<RubricDefinitionRow> rows = dataModel.getObjects();
		if(index >= 0 && index < rows.size() - 1) {
			RubricDefinitionRow row = rows.remove(index);
			rows.add(index + 1, row);
			dataModel.setObjects(rows);
			tableEl.reset();
			tableEl.reloadData();
			updateUpDownButtons();
		}
	}

	public class RubricDefinitionRow {
		
		private TextElement nameEl;
		private SingleSelection typeEl;
		private MultipleSelectionElement sumEl;
		private SingleSelection weightEl;
		private FormLink upButton;
		private FormLink downButton;
		private FormLink removeButton;
		
		private DecisionRubricDefinition definition;
		
		public RubricDefinitionRow(DecisionRubricDefinition definition,
				TextElement nameEl, SingleSelection typeEl,
				MultipleSelectionElement sumEl, SingleSelection weightEl,
				FormLink removeButton, FormLink upButton, FormLink downButton) {
			this.definition = definition;
			this.nameEl = nameEl;
			this.typeEl = typeEl;
			this.sumEl = sumEl;
			this.weightEl = weightEl;
			this.upButton = upButton;
			this.downButton = downButton;
			this.removeButton = removeButton;
		}

		public TextElement getNameEl() {
			return nameEl;
		}

		public void setNameEl(TextElement nameEl) {
			this.nameEl = nameEl;
		}

		public SingleSelection getTypeEl() {
			return typeEl;
		}

		public void setTypeEl(SingleSelection typeEl) {
			this.typeEl = typeEl;
		}

		public MultipleSelectionElement getSumEl() {
			return sumEl;
		}

		public void setSumEl(MultipleSelectionElement sumEl) {
			this.sumEl = sumEl;
		}

		public SingleSelection getWeightEl() {
			return weightEl;
		}

		public void setWeightEl(SingleSelection weightEl) {
			this.weightEl = weightEl;
		}
		
		public FormLink getRemoveButton() {
			return removeButton;
		}
		
		public FormLink getUpButton() {
			return upButton;
		}
		
		public FormLink getDownButton() {
			return downButton;
		}

		public DecisionRubricDefinition getDefinition() {
			return definition;
		}

		public void setDefinition(DecisionRubricDefinition definition) {
			this.definition = definition;
		}
		
		public DecisionRubricDefinition commit() {
			definition.setRubric(nameEl.getValue());
			definition.setSum(sumEl.isAtLeastSelected(1));
			
			boolean weighted = false;
			if(weightEl.isOneSelected()) {
				String selectedWeight = weightEl.getSelectedKey();
				definition.setWeight(Integer.parseInt(selectedWeight));
				weighted = true;
			}
			if(!weighted && definition.getWeight() < 1) {
				definition.setWeight(10);
			}
			
			if(typeEl.isOneSelected()) {
				definition.setType(typeEl.getSelectedKey());
			} else if(!StringHelper.containsNonWhitespace(definition.getType()))  {
				definition.setType(typeKeys[0]);
			}
			
			return definition;
		}
	}
}
