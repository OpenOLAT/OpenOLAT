/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.folder.ui;

import org.olat.core.commons.services.folder.ui.event.FileBrowserSearchEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 29 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserSearchController extends FormBasicController {
	
	private TextElement quickSearchEl;
	private FormLink quickSearchButton;

	protected FileBrowserSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "browser_search");
		initForm(ureq);
		setVisible(false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.setAriaLabel("enter.search.term");
		
		quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setDomReplacementWrapperRequired(false);
		quickSearchButton.setTitle(translate("search"));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (quickSearchEl == source) {
			doQuickSearch(ureq);
		} else if (quickSearchButton == source) {
			doQuickSearch(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doQuickSearch(UserRequest ureq) {
		formOK(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new FileBrowserSearchEvent(quickSearchEl.getValue()));
	}

	public void setVisible(boolean isVisible) {
		quickSearchEl.setVisible(isVisible);
		quickSearchButton.setVisible(isVisible);
		flc.setDirty(true);
	}

	public void enable(String placeholder) {
		quickSearchEl.setPlaceholderText(placeholder);
		quickSearchEl.setAriaLabel(placeholder);
		quickSearchEl.setEnabled(true);
		quickSearchButton.setEnabled(true);
	}

	public void disable() {
		quickSearchEl.setPlaceholderText(translate("search.not.available"));
		quickSearchEl.setAriaLabel(translate("search.not.available"));
		quickSearchEl.setEnabled(false);
		quickSearchButton.setEnabled(false);
	}

}
