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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterTextController extends FormBasicController {

	private TextElement textEl;
	private FormLink clearButton;
	private FormLink updateButton;
	
	private final FlexiTableTextFilter filter;
	
	public FlexiFilterTextController(UserRequest ureq, WindowControl wControl, FlexiTableTextFilter filter) {
		super(ureq, wControl, "field_text");
		setTranslator(Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale(), getTranslator()));
		this.filter = filter;
		initForm(ureq);
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String searchString = filter.getValue();
		textEl = uifactory.addTextElement("text", null, 255, searchString, formLayout);
		textEl.setDomReplacementWrapperRequired(false);
		if(!StringHelper.containsNonWhitespace(searchString)) {
			textEl.setFocus(true);
		}

		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON_SMALL);
		clearButton = uifactory.addFormLink("clear", formLayout, Link.LINK);
		clearButton.setElementCssClass("o_filter_clear");
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(updateButton == source && validateFormLogic(ureq)) {
			doUpdate(ureq);
		} else if(clearButton == source) {
			doClear(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doUpdate(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doUpdate(UserRequest ureq) {
		filter.setValue(textEl.getValue());
		fireEvent(ureq, new ChangeFilterEvent(filter));
	}
	
	private void doClear(UserRequest ureq) {
		textEl.setValue("");
		filter.setValue(null);
		fireEvent(ureq, new ChangeFilterEvent(filter));
	}
}
