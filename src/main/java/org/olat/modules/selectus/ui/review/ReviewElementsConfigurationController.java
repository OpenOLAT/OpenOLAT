/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.review;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.components.ReviewElementTypeCellRenderer;
import org.olat.modules.selectus.ui.review.ReviewElementsConfigurationDataModel.ElementCols;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewElementsConfigurationController extends FormBasicController {
	
	private FormLink addSliderEl;
	private FormLink addTextEl;
	private FormLink addTitleEl;
	private FlexiTableElement tableEl;
	private ReviewElementsConfigurationDataModel tableModel;
	
	private DialogBoxController confirmDeleteCtrl;
	
	private int count = 0;
	private final boolean readOnly;
	private PositionReviewDefinition reviewDefinition;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private SelectusReviewService reviewService;
	
	public ReviewElementsConfigurationController(UserRequest ureq, WindowControl wControl, PositionReviewDefinition reviewDefinition, boolean readOnly) {
		super(ureq, wControl, "config_elements");
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.reviewDefinition = reviewDefinition;
		this.readOnly = readOnly;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.label));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.type, new ReviewElementTypeCellRenderer(getTranslator())));
		
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.up));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.down));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.delete));
		}
		
		tableModel = new ReviewElementsConfigurationDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 250, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		addSliderEl = uifactory.addFormLink("add.slider", "add.slider", null, formLayout, Link.BUTTON);
		addSliderEl.setVisible(!readOnly);
		addTextEl = uifactory.addFormLink("add.text", "add.text", null, formLayout, Link.BUTTON);
		addTextEl.setVisible(!readOnly);
		addTitleEl = uifactory.addFormLink("add.title", "add.title", null, formLayout, Link.BUTTON);
		addTitleEl.setVisible(!readOnly);
	}
	
	private void loadModel() {
		List<ReviewElementDefinition> elements = reviewDefinition.getElements();
		List<ElementDefinitionRow> rows = new ArrayList<>();
		for(ReviewElementDefinition element:elements) {
			if(element != null) {
				rows.add(forgeRow(element));
			}
		}
		
		if(!rows.isEmpty()) {
			rows.get(0).getUpLink().setVisible(false);
			rows.get(rows.size() - 1).getDownLink().setVisible(false);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private ElementDefinitionRow forgeRow(ReviewElementDefinition element) {
		String label = element.getLabel(); 
		TextElement labelEl = uifactory.addTextElement("label_" + (++count), "table.header.label", 250, label, flc);
		labelEl.addActionListener(FormEvent.ONCHANGE);
		labelEl.setEnabled(!readOnly);
		
		FormLink deleteButton = uifactory.addFormLink("del_" + (count++), "delete", "table.header.delete", null,
				flc, Link.NONTRANSLATED);
		deleteButton.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
		deleteButton.setI18nKey("");
		deleteButton.setEnabled(!readOnly);
		FormLink upButton = uifactory.addFormLink("up_" + (count++), "up", "table.header.up", null,
				flc, Link.NONTRANSLATED);
		upButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		upButton.setI18nKey("");
		upButton.setEnabled(!readOnly);
		FormLink downButton = uifactory.addFormLink("down_" + (count++), "down", "table.header.down", null,
				flc, Link.NONTRANSLATED);
		downButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		downButton.setI18nKey("");
		downButton.setEnabled(!readOnly);
		
		ElementDefinitionRow row = new ElementDefinitionRow(element, labelEl, upButton, downButton, deleteButton);
		deleteButton.setUserObject(row);
		upButton.setUserObject(row);
		downButton.setUserObject(row);
		labelEl.setUserObject(row);
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doDelete((ElementDefinitionRow)confirmDeleteCtrl.getUserObject());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addSliderEl == source) {
			doAddElement(ureq, ReviewElementType.slider);
		} else if(addTextEl == source) {
			doAddElement(ureq, ReviewElementType.text);
		} else if(addTitleEl == source) {
			doAddElement(ureq, ReviewElementType.title);
		} else if(source instanceof TextElement) {
			updateLabel((TextElement)source);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("up".equals(link.getCmd())) {
				doMoveUp((ElementDefinitionRow)link.getUserObject());
			} else if("down".equals(link.getCmd())) {
				doMoveDown((ElementDefinitionRow)link.getUserObject());
			} else if("delete".equals(link.getCmd())) {
				doConfirmDelete(ureq, (ElementDefinitionRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddElement(UserRequest ureq, ReviewElementType type) {
		reviewDefinition = reviewService.getReviewDefinition(reviewDefinition);
		reviewService.createReviewElement(reviewDefinition, type);
		dbInstance.commit();
		reviewDefinition = reviewService.getReviewDefinition(reviewDefinition);
		loadModel();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doMoveUp(ElementDefinitionRow row) {
		ReviewElementDefinition element = row.getElement();
		reviewDefinition = reviewService.getReviewDefinition(reviewDefinition);
		List<ReviewElementDefinition> currentElements = reviewDefinition.getElements();

		int index = currentElements.indexOf(element) ;
		if(index > 0 && index < currentElements.size()) {
			ReviewElementDefinition el = currentElements.remove(index);
			currentElements.add(index - 1, el);
			reviewDefinition = reviewService.saveReviewDefinition(reviewDefinition);
			dbInstance.commit();
			loadModel();
		}
	}
	
	private void doMoveDown(ElementDefinitionRow row) {
		ReviewElementDefinition element = row.getElement();
		reviewDefinition = reviewService.getReviewDefinition(reviewDefinition);
		List<ReviewElementDefinition> currentElements = reviewDefinition.getElements();

		int index = currentElements.indexOf(element) ;
		if(index >= 0 && index + 1 < currentElements.size()) {
			ReviewElementDefinition el = currentElements.remove(index);
			currentElements.add(index + 1, el);
			reviewDefinition = reviewService.saveReviewDefinition(reviewDefinition);
			dbInstance.commit();
			loadModel();
		}
	}
	
	private void updateLabel(TextElement el) {
		ElementDefinitionRow row = (ElementDefinitionRow)el.getUserObject();
		ReviewElementDefinition element = row.getElement();
		element.setLabel(el.getValue());
		ReviewElementDefinition mergedElement = reviewService.saveReviewElement(element);
		row.setElement(mergedElement);
	}
	
	private void doConfirmDelete(UserRequest ureq, ElementDefinitionRow row) {
		String title = translate("delete.element");
		String text = translate("delete.element.text", new String[] { row.getLabel()});
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(row);
	}
	
	private void doDelete(ElementDefinitionRow row) {
		ReviewElementDefinition element = row.getElement();
		reviewDefinition = reviewService.getReviewDefinition(reviewDefinition);
		reviewDefinition = reviewService.deleteReviewElement(reviewDefinition, element);
		dbInstance.commit();
		loadModel();	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	protected void updateReviewDefinition(PositionReviewDefinition positionReviewDefinition) {
		reviewDefinition = positionReviewDefinition;
	}
}
