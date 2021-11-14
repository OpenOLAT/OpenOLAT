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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.TextBoxListElementImpl;
import org.olat.core.gui.components.textboxlist.ResultMapProvider;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * A simple Demo for the TextBoxList Component
 * 
 * <P>
 * Initial Date: 23.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class GuiDemoTextBoxListController extends FormBasicController {

	public GuiDemoTextBoxListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "guidemo-textboxlist");
		initForm(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof TextBoxListElementImpl) {
			List<String> all = ((TextBoxListElement) source).getValueList();
			getWindowControl().setInfo("the following items were submitted: " + all.toString());
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// the first showcase
		List<TextBoxItem> initialItems = new ArrayList<>();
		initialItems.add(new TextBoxItemImpl("Demo", "demo"));
		TextBoxListElement textBoxListEl = uifactory.addTextBoxListElement("testTextbox", null, "textboxlist.hint", initialItems, formLayout, getTranslator());
		
		textBoxListEl.setAllowDuplicates(false);
		textBoxListEl.setAllowNewValues(false);
		
		ResultMapProvider provider = (searchValue, resMap) -> {
			resMap.put("Haus", "haus");
			resMap.put("Clown", "clown");
			resMap.put("Dog", "dog");
		};
		textBoxListEl.setMapperProvider(provider, ureq);

		// the second showcase
		TextBoxListElement textBoxListEl2 = uifactory.addTextBoxListElement("testTextbox2", null, "textboxlist.hint", null, formLayout, getTranslator());
		textBoxListEl2.setAllowDuplicates(true);
		textBoxListEl2.setAllowNewValues(true);

		// the third showcase
		TextBoxListElement textBoxListEl3 = uifactory.addTextBoxListElement("testTextbox3", null, "textboxlist.hint", null, formLayout, getTranslator());
		textBoxListEl3.setAllowDuplicates(false);
		textBoxListEl3.setAllowNewValues(true);

		// the fourth showcase
		TextBoxListElement textBoxListEl4 = uifactory.addTextBoxListElement("testTextbox4", null, "textboxlist.hint", null, formLayout, getTranslator());
		textBoxListEl4.setAllowDuplicates(true);
		textBoxListEl4.setAllowNewValues(true);
		
		List<TextBoxItem> provider4 = new ArrayList<>();
		provider4.add(new TextBoxItemImpl("haus", "Haus"));
		provider4.add(new TextBoxItemImpl("clown", "Clown"));
		provider4.add(new TextBoxItemImpl("dog", "Dog"));
		textBoxListEl4.setAutoCompleteContent(provider4);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do here
	}

}
