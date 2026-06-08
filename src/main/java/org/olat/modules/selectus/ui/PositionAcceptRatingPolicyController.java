/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.AcceptPolicyEnum;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionAcceptRatingPolicyController extends FormBasicController implements Activateable2 {

	private final Position position;
	private final ApplicationRef preselectedAppplication;
	private final AcceptPolicyEnum policyToAccept;
	private final List<AcceptPolicyEnum> policyToAcceptList;
	private final List<ContextEntry> activation;
	
	private FormBasicController ratingPolicyController;
	
	private MultipleSelectionElement dontShowElement;
	
	private static final String[] dontShowKeys = new String[] { "dont" };
	private final String[] dontShowValues = new String[1];
	
	@Autowired
	private RecruitingService recruitingService;

	public PositionAcceptRatingPolicyController(UserRequest ureq, WindowControl wControl,
			Position position, ApplicationRef preselectedAppplication,
			List<AcceptPolicyEnum> policyToAcceptList, AcceptPolicyEnum policyToAccept
			, List<ContextEntry> activation) {
		super(ureq, wControl, "accept_ratings_policy");
		
		this.position = position;
		this.policyToAccept = policyToAccept;
		this.policyToAcceptList = policyToAcceptList;
		this.preselectedAppplication = preselectedAppplication;
		this.activation = activation;
		
		dontShowValues[0] = translate("edit.rating.policy.dontShow");
		
		if(policyToAccept == AcceptPolicyEnum.ratingPolicy) {
			ratingPolicyController = new PositionRatingPolicyController(ureq, wControl, mainForm, position);
		} else {
			ratingPolicyController = new MessageToCommitteeController(ureq, wControl, mainForm, position);
		}
		listenTo(ratingPolicyController);
		initForm(ureq);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public ApplicationRef getApplicationToSelect() {
		return preselectedAppplication;
	}
	
	public List<ContextEntry> getActivation() {
		return activation;
	}
	
	public AcceptPolicyEnum getPolicyToAccept() {
		return policyToAccept;
	}
	
	public List<AcceptPolicyEnum> getPolicyToAcceptList() {
		return policyToAcceptList;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("policy_item", ratingPolicyController.getInitialFormItem());
		
		final FormLayoutContainer checkLayout = FormLayoutContainer.createDefaultFormLayout("check_layout", getTranslator());
		formLayout.add(checkLayout);
		checkLayout.setRootForm(mainForm);
		dontShowElement = uifactory.addCheckboxesHorizontal("dontshow", null, checkLayout, dontShowKeys, dontShowValues);
		dontShowElement.setLabel(null, null);

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		FormSubmit submit = uifactory.addFormSubmitButton("ok", buttonLayout);
		submit.setLabel("ok", null);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Boolean dontShow = Boolean.valueOf(dontShowElement.isAtLeastSelected(1));
		recruitingService.acceptPositionPolicy(position, getIdentity(), policyToAccept, dontShow);
		policyToAcceptList.remove(policyToAccept);
		ureq.getUserSession().putEntryInNonClearedStore(RecruitingHelper.sessionKeyAcceptedPolicy(position, policyToAccept), Boolean.TRUE);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	public static class MessageToCommitteeController extends FormBasicController {
		
		private final String message;
		
		public MessageToCommitteeController(UserRequest ureq, WindowControl wControl, Form rootForm, Position position) {
			super(ureq, wControl, "message_to_committee");
			message = position.getMessageToCommitte();
			if(rootForm != null) {
				mainForm = rootForm;
				flc.setRootForm(rootForm);
				mainForm.addSubFormListener(this);
			}
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if(formLayout instanceof FormLayoutContainer) {
				FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
				layoutCont.contextPut("message", message);
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}