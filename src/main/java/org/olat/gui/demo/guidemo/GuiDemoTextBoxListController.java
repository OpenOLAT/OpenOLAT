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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.TextBoxListElementImpl;
import org.olat.core.gui.components.textboxlist.ResultMapProvider;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
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

	private TextBoxListElement textBoxListEl;
	private TextBoxListElement textBoxListEl2;
	private TextBoxListElement textBoxListEl3;
	private TextBoxListElement textBoxListEl4;

	public GuiDemoTextBoxListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "guidemo-textboxlist");
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof TextBoxListElementImpl) {
			List<String> all = ((TextBoxListElement) source).getValueList();
			getWindowControl().setInfo("the following items were submitted: " + all.toString());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// the first showcase
		Map<String, String> initialItems = new TreeMap<>();
		initialItems.put("Demo", "demo");
		initialItems.put("You can delete me!", "delete");
		textBoxListEl = uifactory.addTextBoxListElement("testTextbox", null, "textboxlist.hint", initialItems, formLayout, getTranslator());
		ResultMapProvider provider = new ResultMapProvider() {
			@Override
			public void getAutoCompleteContent(String searchValue, Map<String, String> resMap) {
				// put some dummy values as result. For real-world do your
				// search-magic here!
				resMap.put("Haus", "haus");
				resMap.put("Clown", "clown");
				resMap.put("Dog", "dog");
			}
		};
		textBoxListEl.setMapperProvider(provider);
		textBoxListEl.setAllowDuplicates(false);
		textBoxListEl.setAllowNewValues(false);

		// the second showcase
		textBoxListEl2 = uifactory.addTextBoxListElement("testTextbox2", null, "textboxlist.hint", null, formLayout, getTranslator());
		textBoxListEl2.setAllowDuplicates(true);
		textBoxListEl2.setAllowNewValues(true);

		// the third showcase
		textBoxListEl3 = uifactory.addTextBoxListElement("testTextbox3", null, "textboxlist.hint", null, formLayout, getTranslator());
		textBoxListEl3.setAllowDuplicates(false);
		textBoxListEl3.setAllowNewValues(true);

		// the fourth showcase
		textBoxListEl4 = uifactory.addTextBoxListElement("testTextbox4", null, "textboxlist.hint", null, formLayout, getTranslator());
		textBoxListEl4.setAllowDuplicates(true);
		textBoxListEl4.setAllowNewValues(true);
		textBoxListEl4.setMapperProvider(provider);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do here
	}

}
