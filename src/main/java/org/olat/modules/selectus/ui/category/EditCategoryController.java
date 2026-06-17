/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.category;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 17 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCategoryController extends FormBasicController {
	
	private TextElement nameEl;
	private TextElement colorEl;
	
	private Category category;
	private Position position;
	
	@Autowired
	private TaggingService taggingService;
	
	public EditCategoryController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}
	
	public EditCategoryController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl);
		this.position = position;
		initForm(ureq);
	}
	
	public EditCategoryController(UserRequest ureq, WindowControl wControl, Category category) {
		super(ureq, wControl);
		this.category = category;
		this.position = category.getPosition();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = category == null ? "" : category.getName();
		nameEl = uifactory.addTextElement("category.name", "category.name", 32, name, formLayout);
		nameEl.setDisplaySize(32);
		
		String color = category == null ? "" : category.getColor();
		colorEl = uifactory.addTextElement("category.color", "category.color", 16, color, formLayout);
		colorEl.setDisplaySize(16);
		colorEl.setExampleKey("category.color.example", null);
		colorEl.setHelpText(translate("category.color.hint"));
		colorEl.setHelpUrl("https://color.adobe.com/create/color-wheel/");
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(nameEl.getValue().length() > nameEl.getMaxLength()) {
			nameEl.setErrorKey("error.category.too.long", new String[] { Integer.toString(nameEl.getMaxLength()) });
			allOk &= false;
		} else {
			List<Category> categories = taggingService.getCategoriesByName(nameEl.getValue(), position);
			if(categories != null && !categories.isEmpty() && !categories.contains(category)) {
				nameEl.setErrorKey("error.category.already.exists");
				allOk &= false;
			}
		}
		
		colorEl.clearError();
		if(StringHelper.containsNonWhitespace(colorEl.getValue())) {
			String color = colorEl.getValue();
			if(color.length() > colorEl.getMaxLength()) {
				colorEl.setErrorKey("error.color.too.long", new String[] { Integer.toString(colorEl.getMaxLength()) });
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(category == null) {
			category = taggingService.createCategory(nameEl.getValue(), colorEl.getValue(), position);
		} else {
			category.setName(nameEl.getValue());
			category.setColor(colorEl.getValue());
			category = taggingService.updateCategory(category);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
