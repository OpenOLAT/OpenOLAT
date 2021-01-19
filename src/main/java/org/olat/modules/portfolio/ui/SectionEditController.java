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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SectionEditController extends FormBasicController {

	private TextElement titleEl;
	private RichTextElement descriptionEl;
	private DateChooser beginDateEl, endDateEl;
	
	private BinderRef binder;
	private Section section;
	private Object userObject;
	private BinderSecurityCallback secCallback;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public SectionEditController(UserRequest ureq, WindowControl wControl,
			BinderRef binder, BinderSecurityCallback secCallback) {
		super(ureq, wControl);
		this.binder = binder;
		this.secCallback = secCallback;
		initForm(ureq);
	}
	
	public SectionEditController(UserRequest ureq, WindowControl wControl,
			Section section, BinderSecurityCallback secCallback) {
		super(ureq, wControl);
		this.section = section;
		this.secCallback = secCallback;
		initForm(ureq);
	}
	
	public Section getSection() {
		return section;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		formLayout.setElementCssClass("o_sel_pf_edit_section_form");
		
		String title = section == null ? null : section.getTitle();
		titleEl = uifactory.addTextElement("title", "title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_edit_section_title");
		titleEl.setMandatory(true);
		
		String description = section == null ? null : section.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("summary", "page.summary", description, 8, 60, formLayout, getWindowControl());
		descriptionEl.setPlaceholderKey("summary.placeholder", null);
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		
		Date begin = section == null ? null : section.getBeginDate();
		beginDateEl = uifactory.addDateChooser("begin.date", "begin.date", begin, formLayout);
		beginDateEl.setVisible(secCallback.canSectionBeginAndEnd());
		
		Date end = section == null ? null : section.getEndDate();
		endDateEl = uifactory.addDateChooser("end.date", "end.date", end, formLayout);
		endDateEl.setVisible(secCallback.canSectionBeginAndEnd());

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		if(section != null && section.getKey() != null) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		} else {
			uifactory.addFormSubmitButton("create.section", buttonsCont);
		}
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(secCallback.canSectionBeginAndEnd()) {
			Date begin = beginDateEl.getDate();
			Date end = endDateEl.getDate();
			if(begin != null && end != null && end.before(begin)) {
				endDateEl.setErrorKey("error.begin.after.end", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(section == null) {
			String title = titleEl.getValue();
			String description = descriptionEl.getValue();
			SectionRef sectionRef = portfolioService.appendNewSection(title, description, beginDateEl.getDate(), endDateEl.getDate(), binder);
			section = portfolioService.getSection(sectionRef);
		} else {
			Section reloadedSection = portfolioService.getSection(section);
			reloadedSection.setTitle(titleEl.getValue());
			reloadedSection.setDescription(descriptionEl.getValue());
			reloadedSection.setBeginDate(beginDateEl.getDate());
			reloadedSection.setEndDate(endDateEl.getDate());
			section = portfolioService.updateSection(reloadedSection);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
