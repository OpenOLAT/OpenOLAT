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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.events.DecisionEvent;
import org.olat.modules.selectus.ui.events.FinalDecisionChangeEvent;
import org.olat.modules.selectus.ui.model.ApplicationLightRow;

/**
 * 
 * Initial date: 20 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BatchDecisionController extends FormBasicController {
	
	private SingleSelection decisionEl;
	
	private final Position position;
	private final List<? extends ApplicationLightRow> applications;
	
	@Autowired
	private AuditService auditService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService erFrontendManager;
	
	public BatchDecisionController(UserRequest ureq, WindowControl wControl,
			List<? extends ApplicationLightRow> applications, Position position) {
		super(ureq, wControl);
		this.position = position;
		this.applications = applications;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		StringBuilder applicationList = new StringBuilder();
		for(ApplicationLightRow application:applications) {
			if(applicationList.length() > 0) applicationList.append(", ");
			Person person = application.getApplication().getPerson();
			String fullname = salutationGenerator.getFullname(person, getLocale());
			applicationList.append(fullname);
		}

		formLayout.setElementCssClass("o_sel_batch_decision_form");
		setFormInfo("batch.decision.msg", new String[] { applicationList.toString() });
		
		String[] decisionKeys = new String[]{ "0", "3", "2", "1" };
		String[] decisionValues = new String[] {
				translate("decision.0"), translate("decision.3"),
				translate("decision.2"), translate("decision.1")	
		};
		decisionEl = uifactory.addDropdownSingleselect("edit.decision", "edit.decision.label", formLayout, decisionKeys, decisionValues, null);
		decisionEl.setElementCssClass("o_sel_committee_decision");
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(!decisionEl.isOneSelected()) {
			decisionEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
		if(decisionEl.isOneSelected()) {
			String key = decisionEl.getSelectedKey();
			try {
				int decision = Integer.parseInt(key);
				for(ApplicationLightRow application:applications) {
					Long applicationKey = application.getApplication().getKey();
					Application app = erFrontendManager.getApplicationByKey(applicationKey);
					if(app != null) {
						Integer before = app.getDecision();
						erFrontendManager.setDecision(app, decision);
						
						String messageI18n = "audit.log.application.update.decision";
						String[] args = new String[] { salutationGenerator.getTitleFullname(app, getLocale()), app.getId().toString() };
						auditService.auditApplicationDecisionLog(Action.update, ActionTarget.decision, before, decision, messageI18n, args, getTranslator(), position, app, getIdentity());
						
						FinalDecisionChangeEvent fde = new FinalDecisionChangeEvent(app.getKey(), decision, getIdentity().getKey());
						CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(fde, position);
					}
				}
				fireEvent(ureq, new DecisionEvent());
			} catch (NumberFormatException e) {
				logError("", e);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}