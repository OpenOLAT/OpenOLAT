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
package org.olat.core.gui.control.generic.confirmation;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.StaticListElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 14 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BulkDeleteConfirmationController extends ConfirmationController {
	
	private final String label;
	private final List<String> values;
	private final String showAllI18nKey;

	public BulkDeleteConfirmationController(UserRequest ureq, WindowControl wControl, String message,
			String confirmation, String confirmButton, String label, List<String> values, String showAllI18nKey) {
		super(ureq, wControl, message, confirmation, confirmButton, true, false);
		this.label = label;
		this.values = values;
		this.showAllI18nKey = showAllI18nKey;
		
		initForm(ureq);
	}

	@Override
	protected void initFormElements(FormLayoutContainer confirmCont) {
		StaticListElement deleteValuesEl = uifactory.addStaticListElement("delete.values", null, values, confirmCont);
		deleteValuesEl.setLabel(label, null, false);
		deleteValuesEl.setShowAllI18nKey(showAllI18nKey);
	}
	
	@Override
	protected FormLayoutContainer createConfirmContainer() {
		return FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
	}
	
}
