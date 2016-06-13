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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderMetadataEditController extends FormBasicController {
	
	private TextElement titleEl, summaryEl;
	private TextBoxListElement categoriesEl;
	
	private Binder binder;
	private Map<String,String> categories = new HashMap<>();
	private Map<String,Category> categoriesMap = new HashMap<>();
	
	@Autowired
	private PortfolioService portfolioService;

	public BinderMetadataEditController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, (Binder)null);
		initForm(ureq);
	}
	
	public BinderMetadataEditController(UserRequest ureq, WindowControl wControl, Binder binder) {
		super(ureq, wControl);
		this.binder = binder;
		if(binder != null) {
			List<Category> tags = portfolioService.getCategories(binder);
			for(Category tag:tags) {
				categories.put(tag.getName(), tag.getName());
				categoriesMap.put(tag.getName(), tag);
			}
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = binder == null ? null : binder.getTitle();
		titleEl = uifactory.addTextElement("title", "title", 255, title, formLayout);
		titleEl.setMandatory(true);
		
		String summary = binder == null ? null : binder.getSummary();
		summaryEl = uifactory.addTextAreaElement("summary", "summary", 4096, 4, 60, false, summary, formLayout);
		summaryEl.setPlaceholderKey("summary.placeholder", null);
		
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categories, formLayout, getTranslator());
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		//Map<String, String> allUsersTags = ePFMgr.getUsersMostUsedTags(getIdentity(), 50);
		//categoriesEl.setAutoCompleteContent(allUsersTags);
		categoriesEl.setAllowDuplicates(false);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("create.binder", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public Binder getBinder() {
		return binder;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(binder == null) {
			String title = titleEl.getValue();
			String summary = summaryEl.getValue();
			binder = portfolioService.createNewBinder(title, summary, getIdentity());
			
			List<String> updatedCategories = categoriesEl.getValueList();
			portfolioService.updateCategories(binder, updatedCategories);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
