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
package org.olat.shibboleth;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShibbolethAdminController extends FormBasicController {
	
	private static final String[] keys = new String[]{ "x" };
	
	private MultipleSelectionElement attributeEl;
	private TextElement attribute1El, values1El;
	private TextElement attribute2El, values2El;
	
	@Autowired
	private ShibbolethModule shibbolethModule;
	
	public ShibbolethAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormDescription("admin.description");
		
		uifactory.addStaticTextElement("admin.ac.url", Settings.getServerContextPathURI() + "/shib/", formLayout);
		
		boolean enabled = shibbolethModule.isAccessControlByAttributes();
		String[] values = new String[]{ translate("enabled") };
		attributeEl = uifactory.addCheckboxesHorizontal("admin.ac.attribute", formLayout, keys, values);
		attributeEl.addActionListener(FormEvent.ONCHANGE);
		attributeEl.select(keys[0], enabled);
		
		String attribute1 = shibbolethModule.getAttribute1();
		attribute1El = uifactory.addTextElement("attr-1", "admin.ac.attribute.1", 255, attribute1, formLayout);
		attribute1El.setVisible(enabled);
		String value1 = shibbolethModule.getAttribute1Values();
		values1El = uifactory.addTextAreaElement("admin.ac.value.1", 6, 60, value1, formLayout);
		values1El.setVisible(enabled);
		
		String attribute2 = shibbolethModule.getAttribute2();
		attribute2El = uifactory.addTextElement("attr-2", "admin.ac.attribute.2", 255, attribute2, formLayout);
		attribute2El.setVisible(enabled);
		String value2 = shibbolethModule.getAttribute2Values();
		values2El = uifactory.addTextAreaElement("admin.ac.value.2", 6, 60, value2, formLayout);
		values2El.setVisible(enabled);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(attributeEl == source) {
			boolean enabled = attributeEl.isAtLeastSelected(1);
			values1El.setVisible(enabled);
			values2El.setVisible(enabled);
			attribute1El.setVisible(enabled);
			attribute2El.setVisible(enabled);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(attributeEl.isAtLeastSelected(1)) {
			shibbolethModule.setAccessControlByAttributes(true);
			shibbolethModule.setAttribute1(attribute1El.getValue());
			shibbolethModule.setAttribute2(attribute2El.getValue());
			shibbolethModule.setAttribute1Values(values1El.getValue());
			shibbolethModule.setAttribute2Values(values2El.getValue());
		} else {
			shibbolethModule.setAccessControlByAttributes(false);
		}
	}
}