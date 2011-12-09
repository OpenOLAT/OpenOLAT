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
package org.olat.test.guidemo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.textboxlist.ResultMapProvider;
import org.olat.core.gui.components.textboxlist.TextBoxListComponent;
import org.olat.core.gui.components.textboxlist.TextBoxListEvent;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Description:<br>
 * A simple Demo for the TextBoxList Component
 * 
 * <P>
 * Initial Date: 23.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class GuiDemoTextBoxListController extends BasicController {

	private TextBoxListComponent tblC;

	public GuiDemoTextBoxListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		
		
		Map<String, String> initialItems = new TreeMap<String, String>();
		initialItems.put("123 Demo", "demo");
		initialItems.put("try to delete me", "delete");
		tblC = new TextBoxListComponent("testTextbox", "textboxlist.hint", initialItems, getTranslator());
		ResultMapProvider provider = new ResultMapProvider() {
			@Override
			public void getAutoCompleteContent(String searchValue, Map<String, String> resMap) {
				// put some dummy values as result. For real-world do your search-magic here!
				resMap.put("Hausvermietung" + searchValue, "10");
				resMap.put("Clown" + searchValue, "4");
				resMap.put("Suche nach: " + searchValue, "3");
			}
		};
		tblC.setMapperProvider(provider);
		// if no provider is needed (maybe only a small autocomplete-map) you could provide them directly
//		Map<String, Integer> autoCompleteContent = new HashMap<String, Integer>();
//		autoCompleteContent.put("Hausvermietung", 10);
//		autoCompleteContent.put("Clown", 4);
//		autoCompleteContent.put("Versicherung", 3);
//		tblC.setAutoCompleteContent(autoCompleteContent);

//		tblC.setEnabled(false);
		tblC.addListener(this);

		putInitialPanel(tblC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof TextBoxListEvent) {
			TextBoxListEvent tblEv = (TextBoxListEvent) event;
			List<String> all = tblEv.getAllItems();
			List<String> newItems = tblEv.getNewOnly();
			getWindowControl().setInfo(
					"the following items were submitted: " + all.toString() + "    some where even added: " + newItems.toString());
		}

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

}
