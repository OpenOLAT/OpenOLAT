/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Initial Date:  26.09.2009 <br>
 * @author patrickb
 */
public class GuiDemoInlineEditingBasedOnFlexiForm extends FormBasicController {

	private TextElement[] elements = new TextElement[5];
	private TextElement inlineLabel;

	public GuiDemoInlineEditingBasedOnFlexiForm(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		initForm(this.flc, this, ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String msg = "";
		for (int i = 0; i < elements.length; i++) {
			msg += elements[i].getValue()+" | ";
		}
		msg +="CustomLabel is named: "+inlineLabel.getValue();
		showInfo("noTransOnlyParam",msg);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("inline.editing.flexiform");
		setFormDescription("inline.editing.flexiform.rem");
		FormUIFactory formUIf = FormUIFactory.getInstance();
		int i=0;
		for (; i < elements.length; i++) {
			elements[i] = formUIf.addInlineTextElement("inline.label.text"+i, "some", formLayout, this);
			elements[i].setLabel("inline.label.text", null);
			elements[i].setNotLongerThanCheck(5, "text.element.error.notlongerthan");
			if(i%2 == 0){
				elements[i].setEnabled(false);
			}
		}

		
		// test for inline editable label field
		// the inlineLable is used as Label for the addStaticTextElement
		// Avoid translation error by setting i18nLabel key null first and then set the LabelComponent, and also you need to call showLabel(true)
		inlineLabel = formUIf.addInlineTextElement("inline.label.int"+i+1, "mytext"+i+1, formLayout, null);
		formUIf.addStaticTextElement("inline.label.text"+i,  null,"my bony", formLayout);
	}
}
