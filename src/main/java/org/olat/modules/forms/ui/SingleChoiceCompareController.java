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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.ui.model.CompareResponse;

/**
 * 
 * Initial date: 20.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceCompareController extends FormBasicController {

	private final SingleChoice singleChoice;
	private final List<CompareResponse> compareResponses;

	public SingleChoiceCompareController(UserRequest ureq, WindowControl wControl, SingleChoice singleChoice,
			List<CompareResponse> compareResponses) {
		super(ureq, wControl, "single_choice_compare");
		this.singleChoice = singleChoice;
		this.compareResponses = compareResponses;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("wrappers", createWrappers());
	}

	private List<SingleChoiceCompareWrapper> createWrappers() {
		List<SingleChoiceCompareWrapper> wrappers = new ArrayList<>();
		for (CompareResponse compareResponse: compareResponses) {
			if (isValid(compareResponse)) {
				SingleChoiceCompareWrapper wrapper = createWrapper(compareResponse);
				wrappers.add(wrapper);
			}	
		}
		return wrappers;
	}
	
	private boolean isValid(CompareResponse compareResponse) {
		List<EvaluationFormResponse> responses = compareResponse.getResponses();
		if (responses == null)
			return false;
		if (responses.isEmpty())
			return false;
		EvaluationFormResponse response = responses.get(0);
		if (response.getResponseIdentifier() == null)
			return false;
		if (!response.getResponseIdentifier().equals(singleChoice.getId()))
			return false;
		if (!StringHelper.containsNonWhitespace(response.getStringuifiedResponse()))
			return false;
		return true;
	}

	private SingleChoiceCompareWrapper createWrapper(CompareResponse compareResponse) {
		EvaluationFormResponse response = compareResponse.getResponses().get(0);
		String choiceKey = response.getStringuifiedResponse();
		String choice = getChoice(choiceKey);
		return new SingleChoiceCompareWrapper(compareResponse.getLegendName(), compareResponse.getColor(), choice);
	}

	private String getChoice(String choiceKey) {
		for (Choice choice: singleChoice.getChoices().asList()) {
			if (choiceKey.equals(choice.getId())) {
				return choice.getValue();
			}
		}
		return "";
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public final static class SingleChoiceCompareWrapper {
		
		private final String name;
		private final String color;
		private final String choice;
		
		public SingleChoiceCompareWrapper(String name, String color, String choice) {
			this.name = name;
			this.color = color;
			this.choice = choice;
		}
		
		public String getName() {
			return name;
		}

		public String getColor() {
			return color;
		}

		public String getChoice() {
			return choice;
		}
	}

}
