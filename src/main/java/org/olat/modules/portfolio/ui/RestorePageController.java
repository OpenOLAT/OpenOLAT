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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.assessment.Role;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.SectionKeyRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RestorePageController extends FormBasicController {

	private TextElement titleEl;
	private SingleSelection bindersEl, sectionsEl;
	
	private Page page;
	private Binder currentBinder;

	@Autowired
	private PortfolioService portfolioService;
	
	public RestorePageController(UserRequest ureq, WindowControl wControl, Page page) {
		super(ureq, wControl);
		this.page = page;

		initForm(ureq);
	}
	
	public Page getPage() {
		return page;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_entry_form");
		
		String title = page == null ? null : page.getTitle();
		titleEl = uifactory.addTextElement("title", "page.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_edit_entry_title");
		titleEl.setEnabled(false);
		titleEl.setMandatory(true);
		
		List<Binder> binders = portfolioService.getOwnedBinders(getIdentity());

		String[] theKeys = new String[binders.size()+1];
		String[] theValues = new String[binders.size()+1];
		theKeys[0] = "none";
		theValues[0] = translate("binder.none");
		for (int i = 0; i < binders.size(); ++i) {
			Binder binder = binders.get(i);
			theKeys[i+1] = binder.getKey().toString();
			theValues[i+1] = StringHelper.escapeHtml(binder.getTitle());
		} 
	
		bindersEl = uifactory.addDropdownSingleselect("binders", "page.binders", formLayout, theKeys, theValues, null);
		bindersEl.addActionListener(FormEvent.ONCHANGE);
		bindersEl.select(theKeys[0], true);
		
		sectionsEl = uifactory.addDropdownSingleselect("sections", "page.sections", formLayout, new String[] { "" }, new String[] { "" }, null);
		sectionsEl.setVisible(false);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("restore.page", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	protected void updateSections() {
		String selectedBinderKey =  bindersEl.isOneSelected() ? bindersEl.getSelectedKey() : null;
		if(selectedBinderKey == null || "none".equals(selectedBinderKey)) {
			sectionsEl.setKeysAndValues(new String[] { "" }, new String[] { "" }, null);
			sectionsEl.reset();
			sectionsEl.setVisible(false);
		} else {
			List<Section> sections = portfolioService.getSections(currentBinder);
			if(sections.isEmpty()) {
				sectionsEl.setKeysAndValues(new String[] { "" }, new String[] { "" }, null);
				sectionsEl.reset();
				sectionsEl.setVisible(false);
			} else {
				int numOfSections = sections.size();
				String[] theKeys = new String[numOfSections];
				String[] theValues = new String[numOfSections];
				for (int i = 0; i < numOfSections; i++) {
					Long sectionKey = sections.get(i).getKey();
					theKeys[i] = sectionKey.toString();
					theValues[i] = sections.get(i).getTitle();
				}
				
				sectionsEl.setKeysAndValues(theKeys, theValues, null);
				sectionsEl.reset();
				sectionsEl.setEnabled(true);
				sectionsEl.setVisible(true);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(sectionsEl != null && sectionsEl.isEnabled() && sectionsEl.isVisible()) {
			sectionsEl.clearError();
			if(!sectionsEl.isOneSelected() || !StringHelper.containsNonWhitespace(sectionsEl.getSelectedKey())) {
				sectionsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.draft, getIdentity(), Role.user);

		SectionRef selectSection = getSelectedSection();
		if((page.getSection() == null && selectSection != null) ||
				(page.getSection() != null && selectSection != null && !page.getSection().getKey().equals(selectSection.getKey()))) {
			page = portfolioService.updatePage(page, selectSection);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private SectionRef getSelectedSection() {
		SectionRef selectSection = null;
		if (sectionsEl != null && sectionsEl.isOneSelected() && sectionsEl.isEnabled() && sectionsEl.isVisible()) {
			String selectedKey = sectionsEl.getSelectedKey();
			selectSection = new SectionKeyRef(Long.valueOf(selectedKey));
		}
		return selectSection;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (bindersEl == source) {
			if (bindersEl.getSelectedKey().equals("none")) {
				sectionsEl.setVisible(false);
				currentBinder = null;
			} else {
				try {
					String selectedKey = bindersEl.getSelectedKey();
					currentBinder = portfolioService.getBinderByKey(Long.valueOf(selectedKey));
					sectionsEl.setVisible(true);
					updateSections();
				} catch(NumberFormatException e) {
					logError("", e);
				}
			}
		}
	}
}