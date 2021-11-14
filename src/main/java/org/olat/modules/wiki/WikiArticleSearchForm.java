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
package org.olat.modules.wiki;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * Provides a search text input field
 * 
 * <P>
 * Initial Date:  07.02.2008 <br>
 * @author guido
 */
public class WikiArticleSearchForm extends FormBasicController {

	private TextElement searchQuery;

	public WikiArticleSearchForm(UserRequest ureq, WindowControl control) {
		super(ureq, control, "articleSearch");
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchQuery = uifactory.addTextElement("search", null, 250, null, formLayout);
		searchQuery.setDisplaySize(40);
		
		uifactory.addFormSubmitButton("subm", "navigation.create.article", formLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String val = searchQuery.getValue();
		searchQuery.clearError();
		if(!StringHelper.containsNonWhitespace(val)) {
			searchQuery.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(StringHelper.xssScanForErrors(val)) {
			searchQuery.setErrorKey("form.legende.mandatory", null);
			searchQuery.setValue("");
			allOk &= false;
		}

		return allOk;
	}

	public String getQuery() {
		return searchQuery.getValue();
	}
}