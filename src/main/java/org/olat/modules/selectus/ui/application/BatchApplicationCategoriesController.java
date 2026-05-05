/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.comparator.ApplicationCategoryInfosComparator;
import org.olat.modules.selectus.ui.comparator.TextBoxItemComparator;
import org.olat.modules.selectus.ui.model.ApplicationLightRow;

/**
 * 
 * Initial date: 30 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BatchApplicationCategoriesController extends FormBasicController {

	private TextBoxListElement addCategoriesEl;
	private MultipleSelectionElement removeCategoriesEl;

	private final Position position;
	private final List<? extends ApplicationLightRow> applications;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingService recruitingService;
	
	public BatchApplicationCategoriesController(UserRequest ureq, WindowControl wControl,
			List<? extends ApplicationLightRow> applications, Position position, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.applications = applications;
		this.secCallback = secCallback;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("batch.categories.descr");
		
		// add categories
		addCategoriesEl = uifactory.addTextBoxListElement("add.categories", "add.categories", null, new ArrayList<>(), formLayout, getTranslator());
		addCategoriesEl.setAllowDuplicates(false);
		addCategoriesEl.setAllowNewValues(secCallback.canEditPositionCategories());
		String hint = translate("add.categories.plural.hint");
		if(secCallback.canEditPositionCategories()) {
			hint += "<br>" + translate("add.categories.plural.new.hint");
		}
		if(secCallback.canEditApplicationAdministrativeCategories()) {
			hint += "<br>" + translate("add.categories.plural.administrative.hint");
		}
		addCategoriesEl.setHelpText(hint);

		List<TextBoxItem> allCategoryItems = getAllCategories();
		addCategoriesEl.setAutoCompleteContent(allCategoryItems);
		
		SelectionValues removeKeyValues = getRemovableCategoriesKeyValues();
		removeCategoriesEl = uifactory.addCheckboxesVertical("remove.categories", "remove.categories", formLayout,
				removeKeyValues.keys(), removeKeyValues.values(), 1);
		removeCategoriesEl.setHelpText(translate("remove.categories.plural.hint"));
		removeCategoriesEl.setEscapeHtml(false);

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private SelectionValues getRemovableCategoriesKeyValues() {
		// remove categories
		List<ApplicationRef> apps = applications.stream()
				.map(ApplicationLightRow::getApplication).collect(Collectors.toList());
		List<ApplicationCategoryInfos> infos = taggingService.getApplicationCategories(position, apps,
				secCallback.canEditApplicationAdministrativeCategories());
		Collections.sort(infos, new ApplicationCategoryInfosComparator());
		
		Set<String> tagNames = new HashSet<>();
		SelectionValues removeKeyValues = new SelectionValues();
		for(ApplicationCategoryInfos info:infos) {
			String tagName = info.tagName();
			if(tagNames.contains(tagName)) {
				continue;
			}
			tagNames.add(tagName);
			String label = RecruitingHelper.getLabel(info.getCategory().getName(), info.getCategory().getColor(), info.isAdministrative());
			removeKeyValues.add(SelectionValues.entry(info.tagName(), label));
		}
		return removeKeyValues;
	}
	
	private List<TextBoxItem> getAllCategories() {
		List<TextBoxItem> items = new ArrayList<>();
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
		return items;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// add categories
		List<TextBoxItem> addedCategories = addCategoriesEl.getValueItems();
		List<String> addedCategoriesNames = null;
		if(addedCategories != null && !addedCategories.isEmpty()) {
			addedCategoriesNames = addedCategories.stream()
					.map(TextBoxItem::getValue)
					.collect(Collectors.toList());
			for(ApplicationLightRow application:applications) {
				Long applicationKey = application.getApplication().getKey();
				Application app = recruitingService.getApplicationByKey(applicationKey);
				taggingService.addCategories(app, addedCategoriesNames, secCallback.canEditApplicationAdministrativeCategories(),
						position, getIdentity(), getLocale());
			}
		}
		
		// remove categories
		Collection<String> removedCategories = removeCategoriesEl.getSelectedKeys();
		if(addedCategoriesNames != null && !addedCategoriesNames.isEmpty()) {
			removedCategories.removeAll(addedCategoriesNames);
		}
		if(!removedCategories.isEmpty()) {
			for(ApplicationLightRow application:applications) {
				Long applicationKey = application.getApplication().getKey();
				Application app = recruitingService.getApplicationByKey(applicationKey);
				taggingService.removeCategories(app, removedCategories, getIdentity(), getLocale());
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
