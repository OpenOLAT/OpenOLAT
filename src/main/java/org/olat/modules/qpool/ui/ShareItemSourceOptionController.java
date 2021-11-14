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
package org.olat.modules.qpool.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.ui.events.QPoolEvent;

/**
 * 
 * Initial date: 30.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShareItemSourceOptionController extends FormBasicController {

	private final String[] keys = {"yes","no"};
	private SingleSelection editableEl;
	
	private final List<QuestionItem> items;
	private final QuestionItemsSource source;
	
	public ShareItemSourceOptionController(UserRequest ureq, WindowControl wControl,
			List<QuestionItem> items, QuestionItemsSource source) {	
		super(ureq, wControl, "share_options");
		
		this.items = items;
		this.source = source;
		
		initForm(ureq);
	}
	
	public List<QuestionItem> getItems() {
		return items;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer mailCont = FormLayoutContainer.createDefaultFormLayout("editable", getTranslator());
		formLayout.add(mailCont);
		String[] values = new String[]{
				translate("yes"),
				translate("no")
		};
		editableEl = uifactory.addRadiosVertical("share.editable", "share.editable", mailCont, keys, values);
		editableEl.select("no", true);
		
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("shares", "");
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean editable = editableEl.isOneSelected() && editableEl.isSelected(0);
		int count = source.postImport(items, editable);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_SHARED));
		if(count == 0) {
			showWarning("import.failed");
		} else {
			showInfo("import.success", Integer.toString(count));
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
