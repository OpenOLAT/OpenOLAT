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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.Category;

/**
 * Reusable controller to show the list of categories / tags and edit them with
 * a small edit button. When edited, an Event.CHANGED is fired
 * 
 * Initial date: 28.07.2017<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class CategoriesEditController extends FormBasicController {
	
	private TextBoxListElement categoriesEl;
	private FormLink editLink;
	private FormSubmit saveButton;

	List<Category> categories;
	
	private List<TextBoxItem> categoriesNames = new ArrayList<>();
	private Map<String,Category> categoriesMap = new HashMap<>();
	
	public CategoriesEditController(UserRequest ureq, WindowControl wControl, List<Category> categories) {
		super(ureq, wControl, "categories_edit");
		this.categories = categories;
		for(Category category:categories) {
			categoriesNames.add(new TextBoxItemImpl(category.getName(), category.getName()));
			categoriesMap.put(category.getName(), category);
		}
		initForm(ureq);
		/* we add domID to categories_edit.html to reduce DIV count */
		flc.getFormItemComponent().setDomReplacementWrapperRequired(false);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_entry_tags_form");
		
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categoriesNames, formLayout, getTranslator());
		categoriesEl.setHelpText(translate("categories.hint"));
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		categoriesEl.setAllowDuplicates(false);
		categoriesEl.setElementCssClass("o_block_inline");
		categoriesEl.getComponent().setSpanAsDomReplaceable(true);
		categoriesEl.setIcon("o_icon_tags");
		categoriesEl.setShowSaveButton(true);
		categoriesEl.setLabel("categories", null);
		
		editLink = uifactory.addFormLink("edit", "edit", "edit", null, formLayout, Link.LINK);
		editLink.setCustomEnabledLinkCSS("o_button_textstyle");
		
		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		saveButton.setVisible(false);
		saveButton.setElementCssClass("btn-xs");
		
		// on init set to read-only
		initFormEditableState(false);
	}

	/**
	 * Internal helper to hide and disable elements of the form depending on the
	 * editable state of the form
	 * 
	 * @param editable
	 */
	private void initFormEditableState(boolean editable) {
		categoriesEl.setEnabled(editable);
		editLink.setVisible(!editable);
		saveButton.setVisible(editable);
		// Special label when no categories are there
		if (categoriesEl.getValueList().isEmpty()) {
			editLink.setI18nKey("add");			
		} else {
			editLink.setI18nKey("edit");
		}
		
		flc.setDirty(true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == editLink) {
			initFormEditableState(true);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
		initFormEditableState(false);
	}

	/**
	 * @return The list of categories as visually configured in the box
	 */
	public List<String> getUpdatedCategories() {
		return categoriesEl.getValueList();
	}
	
	@Override
	protected void doDispose() {
		//
	}
}