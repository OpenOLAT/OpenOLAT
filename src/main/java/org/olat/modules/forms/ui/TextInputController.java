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
package org.olat.modules.forms.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.model.xml.TextInput;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputController extends FormBasicController {
	
	private FormLink saveButton;
	private TextAreaElement textEl;
	
	private final TextInput textInput;
	
	public TextInputController(UserRequest ureq, WindowControl wControl, TextInput textInput) {
		super(ureq, wControl, "textinput");
		this.textInput = textInput;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int rows = 12;
		if(textInput.getRows() > 0) {
			rows = textInput.getRows();
		}
		
		long postfix = CodeHelper.getRAMUniqueID();
		textEl = uifactory.addTextAreaElement("textinput_" + postfix, rows, 72, "", formLayout);
		saveButton = uifactory.addFormLink("save_" + postfix, "save", null, formLayout, Link.BUTTON);
		saveButton.setEnabled(false);
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.getFormItemComponent().contextPut("postfix", Long.toString(postfix));
		}
	}
	
	public void update() {
		int rows = 12;
		if(textInput.getRows() > 0) {
			rows = textInput.getRows();
		}
		textEl.setRows(rows);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
