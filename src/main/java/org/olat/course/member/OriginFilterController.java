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
package org.olat.course.member;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OriginFilterController extends FormBasicController {
	

	private String[] originKeys = new String[]{"all", "repo", "group"};
	
	private SingleSelection originEl;
	
	public OriginFilterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] openValues = new String[originKeys.length];
		for(int i=originKeys.length; i-->0; ) {
			openValues[i] = translate("search." + originKeys[i]);
		}
		originEl = uifactory.addRadiosHorizontal("openBg", "search.origin.alt", formLayout, originKeys, openValues);
		originEl.select("all", true);
		originEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(originEl == source) {
			SearchOriginParams e = doSearch();
			fireEvent(ureq, e);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private SearchOriginParams doSearch() {
		SearchOriginParams params = new SearchOriginParams();
		//origin
		if(!originEl.isOneSelected() || originEl.isSelected(0)) {
			params.setRepoOrigin(true);
			params.setGroupOrigin(true);
		} else if(originEl.isSelected(1)) {
			params.setRepoOrigin(true);
			params.setGroupOrigin(false);
		} else if(originEl.isSelected(2)) {
			params.setRepoOrigin(false);
			params.setGroupOrigin(true);
		}
		return params;
	}
}
