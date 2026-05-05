/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.category;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.ui.category.CategoryDataModel.CategoryCols;

/**
 * 
 * Initial date: 17 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CategoryAdminController extends FormBasicController {
	
	private FormLink addCategoryButton;
	private FlexiTableElement tableEl;
	private CategoryDataModel tableModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteBox;
	private EditCategoryController editCategoryCtrl;
	
	@Autowired
	private TaggingService taggingService;
	
	public CategoryAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "categories");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addCategoryButton = uifactory.addFormLink("add.category", formLayout, Link.BUTTON);
		addCategoryButton.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CategoryCols.category));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CategoryCols.color, new CategoryColorCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		tableModel = new CategoryDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "categories", tableModel, 40, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("category.table.empty")
				.build());
	}
	
	private void loadModel() {
		List<Category> categories = taggingService.getSystemCategories();
		tableModel.setObjects(categories);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				Category category = (Category)confirmDeleteBox.getUserObject();
				doDelete(category);
				loadModel();
			}
		} else if(editCategoryCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCategoryCtrl);
		removeAsListenerAndDispose(cmc);
		editCategoryCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addCategoryButton == source) {
			doNewCategory(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					Category row = tableModel.getObject(se.getIndex());
					doConfirmDelete(ureq, row);
				} else if("edit".equals(se.getCommand())) {
					Category row = tableModel.getObject(se.getIndex());
					doEditCategory(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doNewCategory(UserRequest ureq) {
		if(guardModalController(editCategoryCtrl)) return;
		
		editCategoryCtrl = new EditCategoryController(ureq, getWindowControl());
		listenTo(editCategoryCtrl);
		
		String title = translate("add.category");
		cmc = new CloseableModalController(getWindowControl(), "c", editCategoryCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditCategory(UserRequest ureq, Category category) {
		if(guardModalController(editCategoryCtrl)) return;
		
		editCategoryCtrl = new EditCategoryController(ureq, getWindowControl(), category);
		listenTo(editCategoryCtrl);
		
		String title = translate("edit.category", new String[] { category.getName() });
		cmc = new CloseableModalController(getWindowControl(), "c", editCategoryCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, Category category) {
		int usedBy = taggingService.countApplications(category);
		String title = translate("confirm.delete.category.title");
		String text = translate("confirm.delete.category", new String[]{ category.getName(), Integer.toString(usedBy) });
		confirmDeleteBox = activateYesNoDialog(ureq, title, text, confirmDeleteBox);
		confirmDeleteBox.setUserObject(category);
	}
	
	private void doDelete(Category category) {
		taggingService.deleteCategory(category);
	}
}
