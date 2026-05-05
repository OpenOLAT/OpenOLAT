/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.comparator.TextBoxItemComparator;

/**
 * 
 * Initial date: 13 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCategoriesController extends FormBasicController {
	
	private FormLink editCategoriesButton;
	private FormSubmit saveCategoriesButton;
	private TextBoxListElement categoriesEl;
	
	private Position position;
	private Application application;
	private final boolean canEdit;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private TaggingService taggingService;
	
	public ApplicationCategoriesController(UserRequest ureq, WindowControl wControl, Position position, Application application,
			 RecruitingPositionSecurityCallback secCallback, Form rootForm) {
		super(ureq, wControl, "categories", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;
		canEdit = secCallback.canEditApplicationCategories() || secCallback.canEditApplicationAdministrativeCategories();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<TextBoxItem> categoriesItems = getCategories();
		Collections.sort(categoriesItems, new TextBoxItemComparator());
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", null, categoriesItems, formLayout, getTranslator());
		categoriesEl.getComponent().setSpanAsDomReplaceable(true);
		categoriesEl.setEnabled(false);
		categoriesEl.setAllowDuplicates(false);
		categoriesEl.setAllowNewValues(secCallback.canEditPositionCategories());
		if(canEdit) {
			String hint = translate("edit.categories.details.hint");
			hint += "<br>" + translate("add.categories.details.hint");
			if(secCallback.canEditPositionCategories()) {
				hint += "<br>" + translate("add.categories.plural.new.hint");
			}
			if(secCallback.canEditApplicationAdministrativeCategories()) {
				hint += "<br>" + translate("add.categories.plural.administrative.hint");
			}
			hint += "<br>" + translate("remove.categories.details.hint");
			categoriesEl.setHelpText(hint);
			formLayout.contextPut("hint", hint);
		}
		List<TextBoxItem> allCategoriesItems = getAllCategories();
		categoriesEl.setAutoCompleteContent(allCategoriesItems);

		editCategoriesButton = uifactory.addFormLink("edit.categories", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
		editCategoriesButton.setDomReplacementWrapperRequired(false);
		editCategoriesButton.setIconLeftCSS("o_icon o_icon_edit");
		editCategoriesButton.setAriaLabel(translate("aria.edit.categories"));
		editCategoriesButton.setVisible(canEdit);
		
		saveCategoriesButton = uifactory.addFormSubmitButton("save.categories", "save.categories", formLayout);
		saveCategoriesButton.setElementCssClass("o_sel_save_categories");
		saveCategoriesButton.setVisible(false);
	}
	
	private List<TextBoxItem> getCategories() {
		List<ApplicationCategoryInfos> currentCategories = taggingService.getApplicationCategories(position, application,
				secCallback.canSeeApplicationAdministrativeCategories());
		boolean canEditAdministrative = secCallback.canEditApplicationAdministrativeCategories();
		return currentCategories.stream()
				.map(cat -> new TextBoxItemImpl(cat.tagName(), cat.tagName(), cat.getCategory().getColor(),
						canEditAdministrative || !cat.isAdministrative(),
						cat))
				.collect(Collectors.toList());
	}
	
	private List<TextBoxItem> getAllCategories() {
		List<TextBoxItem> items = new ArrayList<>();
		if(canEdit) {
			List<Category> allCategories = taggingService.getAvailableCategoriesFor(position);
			if(secCallback.canEditApplicationAdministrativeCategories()) {
				for(Category category:allCategories) {
					String name = "a:".concat(category.getName());
					items.add(new TextBoxItemImpl(name, name, category.getColor(), true, category));
				}
			}
			for(Category category:allCategories) {
				items.add(new TextBoxItemImpl(category.getName(), category.getName(), category.getColor(), true, category));
			}
			Collections.sort(items, new TextBoxItemComparator());
		}
		return items;
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//super.propagateDirtinessToContainer(fiSrc, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSaveCategories(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editCategoriesButton == source) {
			doEditCategories();
		} else if(saveCategoriesButton == source) {
			doSaveCategories(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doEditCategories() {
		if(!canEdit) return;
		
		categoriesEl.setEnabled(true);
		editCategoriesButton.setVisible(false);
		saveCategoriesButton.setVisible(true);
	}
	
	private void doSaveCategories(UserRequest ureq) {
		if(!canEdit) return;
		
		List<String> selectedCategories = categoriesEl.getValueList();
		taggingService.setCategories(application, selectedCategories, secCallback.canEditApplicationAdministrativeCategories(),
				position, getIdentity(), getLocale());
		
		categoriesEl.setEnabled(false);
		editCategoriesButton.setVisible(true);
		saveCategoriesButton.setVisible(false);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
