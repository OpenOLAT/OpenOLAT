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
import org.olat.core.gui.components.form.flexible.elements.SearchElement;
import org.olat.core.gui.components.form.flexible.elements.SearchVariant;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.SearchFormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 29 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserSearchController extends FormBasicController {
	
	private SearchElement searchEl;

	protected FileBrowserSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "browser_search");
		initForm(ureq);
		setVisible(false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchEl = uifactory.addSearchElement("quicksearch", SearchVariant.DEFAULT, formLayout);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (searchEl == source && event instanceof SearchFormEvent) {
			doQuickSearch(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doQuickSearch(UserRequest ureq) {
		formOK(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new FileBrowserSearchEvent(searchEl.getValue()));
	}

	public void setVisible(boolean isVisible) {
		searchEl.setVisible(isVisible);
		flc.setDirty(true);
	}

	public void enable(String placeholder) {
		searchEl.setPlaceholderText(placeholder);
		searchEl.setAriaLabel(placeholder);
		searchEl.setEnabled(true);
	}

	public void disable() {
		searchEl.setPlaceholderText(translate("search.not.available"));
		searchEl.setAriaLabel(translate("search.not.available"));
		searchEl.setEnabled(false);
	}

}
