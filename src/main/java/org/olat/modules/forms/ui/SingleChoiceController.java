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
package org.olat.modules.forms.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.SingleChoice;

/**
 * 
 * Initial date: 10.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceController extends FormBasicController  {

	private SingleSelection singleChoiceEl;
	
	private final SingleChoice singleChoice;
	
	public SingleChoiceController(UserRequest ureq, WindowControl wControl, SingleChoice singleChoice) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.singleChoice = singleChoice;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateForm();
	}

	void updateForm() {
		if (singleChoiceEl != null) {
			flc.remove(singleChoiceEl);
		}
		String name = "sc_" + CodeHelper.getRAMUniqueID();
		List<Choice> choices = singleChoice.getChoices().asList();
		String[] keys = new String[choices.size()];
		String[] values = new String[choices.size()];
		for (int i = 0; i<choices.size(); i++) {
			Choice coice = choices.get(i);
			keys[i] = coice.getId();
			values[i] = coice.getValue();
		}
		switch (singleChoice.getPresentation()) {
			case HORIZONTAL:
				singleChoiceEl = uifactory.addRadiosHorizontal(name, null, flc, keys, values);
				break;
			case VERTICAL:
				singleChoiceEl = uifactory.addRadiosVertical(name, null, flc, keys, values);
				break;
			default:
				singleChoiceEl = uifactory.addDropdownSingleselect(name, null, flc, keys, values);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}
