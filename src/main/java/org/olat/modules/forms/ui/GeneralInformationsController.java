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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.model.xml.GeneralInformation;
import org.olat.modules.forms.model.xml.GeneralInformation.Type;
import org.olat.modules.forms.model.xml.GeneralInformations;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneralInformationsController extends FormBasicController implements EvaluationFormResponseController {

	private List<GeneralInformationWrapper> generalInformationWrappers = new ArrayList<>();
	private FormLink fillInButton;
	
	private final GeneralInformations generalInformations;
	private List<EvaluationFormResponse> responses;

	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public GeneralInformationsController(UserRequest ureq, WindowControl wControl,
			GeneralInformations generalInformations) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.generalInformations = generalInformations;
		initForm(ureq);
	}

	public GeneralInformationsController(UserRequest ureq, WindowControl wControl,
			GeneralInformations generalInformations, Form rootForm) {
		super(ureq, wControl, LAYOUT_HORIZONTAL, null, rootForm);
		this.generalInformations = generalInformations;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		update();
	}

	public void update() {
		for (GeneralInformationWrapper wrapper: generalInformationWrappers) {
			flc.remove(wrapper.getName());
		}
		if (fillInButton != null) {
			flc.remove(fillInButton);
		}
		
		generalInformationWrappers = new ArrayList<>();
		for (GeneralInformation generalInformation: generalInformations.asOrderedList()) {
			GeneralInformationWrapper wrapper = createWrapper(generalInformation);
			generalInformationWrappers.add(wrapper);
		}
		
		fillInButton = uifactory.addFormLink("gi_" + CodeHelper.getRAMUniqueID(), "general.informations.fill.in",
				"general.informations.fill.in.label", flc, Link.BUTTON);
		fillInButton.addActionListener(FormEvent.ONCLICK);
	}

	private GeneralInformationWrapper createWrapper(GeneralInformation generalInformation) {
		String name = "gi_" + CodeHelper.getRAMUniqueID();
		TextElement informationEl = uifactory.addTextElement(name, name, "general.informations.label", 400, null, flc);
		String label = GeneralInformationsUIFactory.getTranslatedType(generalInformation.getType(), getLocale());
		informationEl.setLabel("general.informations.label", new String[] { label });
		String value = getValue(generalInformation);
		informationEl.setValue(value);
		return new GeneralInformationWrapper(generalInformation, name, informationEl);
	}

	private String getValue(GeneralInformation generalInformation) {
		if (responses != null) {
			EvaluationFormResponse response = getResponse(generalInformation);
			if (response != null) {
				return response.getStringuifiedResponse();
			}
		}
		return null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fillInButton) {
			doFillIn();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

	private void doFillIn() {
		for (GeneralInformationWrapper wrapper: generalInformationWrappers) {
			GeneralInformation generalInformation = wrapper.getGeneralInformation();
			String initialValue = getInitialValue(generalInformation);
			TextElement informationEl = wrapper.getInformationEl();
			informationEl.setValue(initialValue);
		}
	}

	private String getInitialValue(GeneralInformation generalInformation) {
		Type type = generalInformation.getType();
		if (type.name().startsWith("USER_")) {
			return getUserProperty(type);
		}
		return null;
	}

	private String getUserProperty(Type type) {
		String propertyName = GeneralInformationsUIFactory.getUserProperty(type);
		return getIdentity().getUser().getProperty(propertyName, getLocale());
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		for (GeneralInformationWrapper wrapper: generalInformationWrappers) {
			wrapper.getInformationEl().setEnabled(!readOnly);
		}
		fillInButton.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		return responses != null;
	}

	@Override
	public void loadResponse(EvaluationFormSessionRef session) {
		List<EvaluationFormResponse> loadedResponses = new ArrayList<>();
		for (GeneralInformationWrapper wrapper: generalInformationWrappers) {
			EvaluationFormResponse response = evaluationFormManager.loadResponse(wrapper.getGeneralInformation().getId(), session);
			if (response != null) {
				loadedResponses.add(response);
				String value = response.getStringuifiedResponse();
				wrapper.getInformationEl().setValue(value);
			}
		}
		responses = loadedResponses;
	}

	@Override
	public void saveResponse(EvaluationFormSession session) {
		List<EvaluationFormResponse> savedResponses = new ArrayList<>();
		for (GeneralInformationWrapper wrapper: generalInformationWrappers) {
			GeneralInformation generalInformation = wrapper.getGeneralInformation();
			String value = wrapper.getValue();
			EvaluationFormResponse response = getResponse(generalInformation);
			if (response == null) {
				response = evaluationFormManager.createStringResponse(generalInformation.getId(), session, value);
			} else {
				response = evaluationFormManager.updateStringResponse(response, value);
			}
			savedResponses.add(response);
		}
		responses = savedResponses;
	}
	
	private EvaluationFormResponse getResponse(GeneralInformation generalInformation) {
		if (responses != null) {
			for (EvaluationFormResponse response: responses) {
				if (generalInformation.getId().equals(response.getResponseIdentifier())) {
					return response;
				}
			}
		}
		return null;
	}
	
	public static final class GeneralInformationWrapper {
		
		private final GeneralInformation generalInformation;
		private final String name;
		private final TextElement informationEl;
		
		public GeneralInformationWrapper(GeneralInformation generalInformation, String name, TextElement informationEl) {
			super();
			this.generalInformation = generalInformation;
			this.name = name;
			this.informationEl = informationEl;
		}

		public GeneralInformation getGeneralInformation() {
			return generalInformation;
		}

		public String getName() {
			return name;
		}

		public TextElement getInformationEl() {
			return informationEl;
		}
		
		public String getValue() {
			return informationEl.getValue();
		}
		
	}

}
