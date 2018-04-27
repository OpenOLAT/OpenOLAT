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
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.ui.model.CompareResponse;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceCompareController extends FormBasicController {
	

	private final MultipleChoice multipleChoice;
	private final List<CompareResponse> compareResponses;

	public MultipleChoiceCompareController(UserRequest ureq, WindowControl wControl, MultipleChoice multipleChoice,
			List<CompareResponse> compareResponses) {
		super(ureq, wControl, "multiple_choice_compare");
		this.multipleChoice = multipleChoice;
		this.compareResponses = compareResponses;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("wrappers", createWrappers());
	}

	private List<MultipleChoiceCompareWrapper> createWrappers() {
		List<MultipleChoiceCompareWrapper> wrappers = new ArrayList<>();
		for (CompareResponse compareResponse: compareResponses) {
			if (isValid(compareResponse)) {
				MultipleChoiceCompareWrapper wrapper = createWrapper(compareResponse);
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
		return true;
	}

	private MultipleChoiceCompareWrapper createWrapper(CompareResponse compareResponse) {
		String choice = concatResponses(compareResponse);
		return new MultipleChoiceCompareWrapper(compareResponse.getLegendName(), compareResponse.getColor(), choice);
	}

	private String concatResponses(CompareResponse compareResponse) {
		StringBuilder choices = new StringBuilder();
		for (EvaluationFormResponse response: compareResponse.getResponses()) {
			String choiceKey = response.getStringuifiedResponse();
			String choice = getChoice(choiceKey);
			if (choice == null && multipleChoice.isWithOthers()) {
				choice = choiceKey;
			}
			if (choice != null) {
				choices.append(choice).append("<br/>");
			}
		}
		return choices.toString();
	}

	private String getChoice(String choiceKey) {
		for (Choice choice: multipleChoice.getChoices().asList()) {
			if (choiceKey.equals(choice.getId())) {
				return choice.getValue();
			}
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public final static class MultipleChoiceCompareWrapper {
		
		private final String name;
		private final String color;
		private final String choice;
		
		public MultipleChoiceCompareWrapper(String name, String color, String choice) {
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
