/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.category;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Category;

/**
 * 
 * Initial date: 12 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteCategoryController extends FormBasicController {
	
	private FormLink deleteButton;
	private FormLink replaceDeleteButton;
	private SingleSelection replacementCategoryEl;
	
	private Category category;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaggingService taggingService;
	
	public ConfirmDeleteCategoryController(UserRequest ureq, WindowControl wControl, Category category) {
		super(ureq, wControl, "delete_category");
		this.category = category;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int usedBy = taggingService.countApplications(category);
		String text = translate("confirm.delete.category", new String[]{ category.getName(), Integer.toString(usedBy) });
		formLayout.contextPut("msg", text);
		
		FormLayoutContainer replacementCont = FormLayoutContainer.createDefaultFormLayout("replacement", getTranslator());
		formLayout.add(replacementCont);
		replacementCont.setFormDescription(translate("replacement.description"));
		
		SelectionValues catKeyValues = new SelectionValues();
		catKeyValues.add(SelectionValues.entry("-", translate("replacement.no")));
		List<Category> replacementCategories;
		if(category.getPosition() == null) {
			replacementCategories = taggingService.getSystemCategories();
		} else {
			replacementCategories = taggingService.getAvailableCategoriesFor(category.getPosition());
		}
		for(Category replacementCategory:replacementCategories) {
			if(category.equals(replacementCategory)) {
				continue;
			}
			catKeyValues.add(SelectionValues.entry(replacementCategory.getKey().toString(), replacementCategory.getName()));	
		}

		replacementCategoryEl = uifactory.addDropdownSingleselect("replacement.categories", replacementCont, catKeyValues.keys(), catKeyValues.values(), null);
		replacementCategoryEl.setAllowNoSelection(true);

		replaceDeleteButton = uifactory.addFormLink("replace.delete", formLayout, Link.BUTTON);
		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		replacementCategoryEl.clearError();
		if(!replacementCategoryEl.isOneSelected() || "-".equals(replacementCategoryEl.getSelectedKey())) {
			replacementCategoryEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(replaceDeleteButton == source) {
			if(validateFormLogic(ureq)) {
				doReplaceDelete(ureq);
			}
		} else if(deleteButton == source) {
			doDelete(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doDelete(UserRequest ureq) {
		taggingService.deleteCategory(category);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doReplaceDelete(UserRequest ureq) {
		Long replacementCategoryKey = Long.parseLong(replacementCategoryEl.getSelectedKey());
		Category replacementCategory = taggingService.getCategoryByKey(replacementCategoryKey);
		taggingService.deleteCategory(category, replacementCategory);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
