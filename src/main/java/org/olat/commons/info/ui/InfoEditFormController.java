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

package org.olat.commons.info.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  26 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoEditFormController extends FormBasicController {

	private TextElement title;
	private RichTextElement message;
	private final boolean showTitle;
	
	public InfoEditFormController(UserRequest ureq, WindowControl wControl, Form mainForm, boolean showTitle) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		this.showTitle = showTitle;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_info_form");
		if(showTitle) {
			setFormTitle("edit.title");
		}
		
		title = uifactory.addTextElement("info_title", "edit.info_title", 512, "", formLayout);
		title.setElementCssClass("o_sel_info_title");
		title.setMandatory(true);
		
		message = uifactory.addRichTextElementForStringDataMinimalistic("edit.info_message", "edit.info_message", "", 6, 80,
				formLayout, getWindowControl());
		message.getEditorConfiguration().setRelativeUrls(false);
		message.getEditorConfiguration().setRemoveScriptHost(false);
		message.setMandatory(true);
		message.setMaxLength(2000);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public String getTitle() {
		return title.getValue();
	}
	
	public void setTitle(String titleStr) {
		title.setValue(titleStr);
	}
	
	public String getMessage() {
		return message.getValue();
	}
	
	public void setMessage(String messageStr) {
		message.setValue(messageStr);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		title.clearError();
		message.clearError();
		boolean allOk = true;
		
		String t = title.getValue();
		if(!StringHelper.containsNonWhitespace(t)) {
			title.setErrorKey("form.legende.mandatory", new String[] {});
			allOk = false;
		} else if (t.length() > 500) {
			title.setErrorKey("input.toolong", new String[] {"500", Integer.toString(t.length())});
			allOk = false;
		}
		
		String m = message.getValue();
		if(!StringHelper.containsNonWhitespace(m)) {
			message.setErrorKey("form.legende.mandatory", new String[] {});
			allOk = false;
		} else if (m.length() > 2000) {
			message.setErrorKey("input.toolong", new String[] {"2000", Integer.toString(m.length())});
			allOk = false;
		}
		
		return allOk && super.validateFormLogic(ureq);
	}

	public FormLayoutContainer getInitialFormItem() {
		return flc;
	}
}
