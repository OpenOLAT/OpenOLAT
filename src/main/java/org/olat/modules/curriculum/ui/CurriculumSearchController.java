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
package org.olat.modules.curriculum.ui;

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExtendedFlexiTableSearchController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.ui.event.CurriculumSearchEvent;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumSearchController extends FormBasicController implements ExtendedFlexiTableSearchController {
	
	private FormLink searchButton;
	private TextElement elementIdEl;
	private TextElement elementTextEl;
	private TextElement entryIdEl;
	private TextElement entryTextEl;
	private DateChooser elementDatesEl;
	
	private boolean enabled = true;
	
	public CurriculumSearchController(UserRequest ureq, WindowControl wControl, Form form) {
		super(ureq, wControl, LAYOUT_CUSTOM, "search", form);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);

		elementIdEl = uifactory.addTextElement("element.id", "search.element.id", 255, "", leftContainer);
		elementIdEl.setElementCssClass("o_sel_cur_search_id");
		elementIdEl.setFocus(true);
		
		elementTextEl = uifactory.addTextElement("element.text", "search.element.text", 255, "", leftContainer);
		elementTextEl.setElementCssClass("o_sel_cur_search_text");
		elementTextEl.setFocus(true);

		elementDatesEl = uifactory.addDateChooser("search.element.begin", null, leftContainer);
		elementDatesEl.setSecondDate(true);
		elementDatesEl.setSeparator("search.element.end");

		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		rightContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);
		
		entryIdEl = uifactory.addTextElement("entry.id", "search.entry.id", 255, "", rightContainer);
		entryIdEl.setElementCssClass("o_sel_cur_search_id");
		entryIdEl.setFocus(true);
		
		entryTextEl = uifactory.addTextElement("entry.text", "search.entry.text", 255, "", rightContainer);
		entryTextEl.setElementCssClass("o_sel_cur_search_text");
		entryTextEl.setFocus(true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("quick.search", buttonLayout, ureq, getWindowControl());
		searchButton = uifactory.addFormLink("search", buttonLayout, Link.BUTTON);
		searchButton.setElementCssClass("o_sel_repo_search_button");
		searchButton.setCustomEnabledLinkCSS("btn btn-primary");
	}

	@Override
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(enabled) {
			fireSearchEvent(ureq);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabled && searchButton == source) {
			fireSearchEvent(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void fireSearchEvent(UserRequest ureq) {
		CurriculumSearchEvent e = new CurriculumSearchEvent();
		e.setElementId(elementIdEl.getValue());
		e.setElementText(elementTextEl.getValue());
		e.setElementBegin(elementDatesEl.getDate());
		if(elementDatesEl.getSecondDate() != null) {
			Date endDate = CalendarUtils.endOfDay(elementDatesEl.getSecondDate());
			e.setElementEnd(endDate);
		}
		e.setEntryId(entryIdEl.getValue());
		e.setEntryText(entryTextEl.getValue());
		fireEvent(ureq, e);
	}
}
