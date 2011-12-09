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
package org.olat.course.config.ui.courselayout.attribs;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
/**
 * 
 * Description:<br>
 * attribute: preview, gets the config of all other attributes from AbstractLayoutElement, and shows it as preview
 * 
 * <P>
 * Initial Date:  08.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class PreviewLA extends AbstractLayoutAttribute {

	public static final String IDENTIFIER = "preview";
	
	public PreviewLA() {
		setAttributeKey("");
		String[] availKeys = new String[] 	{ "" };
		setAvailKeys(availKeys);
		String[] availValues = new String[] 	{ "" };
		setAvailValues(availValues);
		String[] availCSS = new String[] 	{ "" };
		setAvailCSS(availCSS);
	}

	@Override
	public String getLayoutAttributeTypeName() {
		return IDENTIFIER;
	}

	/**
	 * @see org.olat.course.config.ui.courselayout.attribs.AbstractLayoutAttribute#getFormItem(java.lang.String, org.olat.core.gui.components.form.flexible.FormItemContainer)
	 */
	@Override
	public FormItem getFormItem(String compName, FormItemContainer formLayout) {
		FormUIFactory uifact = FormUIFactory.getInstance();

		String textEl = new String("<span style=\"" + getAttributeValue() + "\">"+ formLayout.getTranslator().translate("preview.sample") + "</span>");
		StaticTextElement fi = uifact.addStaticTextElement(compName, null, textEl, formLayout);
		return fi;
	}


	/**
	 * @see org.olat.course.config.ui.courselayout.attribs.AbstractLayoutAttribute#getRelativeValue(java.lang.String, int)
	 */
	@Override
	public String getRelativeValue(String value, int rel) {
		return "";
	}

}
