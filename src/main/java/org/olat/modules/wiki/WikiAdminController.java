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
package org.olat.modules.wiki;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };

	private MultipleSelectionElement wikiEl;
	private MultipleSelectionElement xssSanEl;
	
	@Autowired
	private WikiModule wikiModule;
	
	public WikiAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		
		String[] onValues = new String[] { translate("enable") };
		wikiEl = uifactory.addCheckboxesHorizontal("wiki.enable", "wiki.enable", formLayout, onKeys, onValues);
		wikiEl.addActionListener(FormEvent.ONCHANGE);
		if(wikiModule.isWikiEnabled()) {
			wikiEl.select("on", true);
		}
		
		xssSanEl = uifactory.addCheckboxesHorizontal("wiki.xss.scan", "wiki.xss.scan", formLayout, onKeys, onValues);
		xssSanEl.addActionListener(FormEvent.ONCHANGE);
		if(wikiModule.isXSScanEnabled()) {
			xssSanEl.select("on", true);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(wikiEl == source) {
			wikiModule.setWikiEnabled(wikiEl.isAtLeastSelected(1));
		} else if(xssSanEl == source) {
			wikiModule.setXSSScanEnabled(xssSanEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}
}