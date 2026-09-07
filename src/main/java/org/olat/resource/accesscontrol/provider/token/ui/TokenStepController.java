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
package org.olat.resource.accesscontrol.provider.token.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.ui.wizard.BookingContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Same validation as {@link TokenSubmitDetailsController}: the code must have
 * at least two characters and must match the offer's access code. Only the
 * checked code is kept in the {@link BookingContext}; the booking itself
 * happens in the wizard's finish callback.
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TokenStepController extends StepFormBasicController {

	private TextElement tokenEl;

	private final BookingContext bookingContext;

	@Autowired
	private AccessControlModule acModule;

	public TokenStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			BookingContext bookingContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.bookingContext = bookingContext;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_method_token");

		String token = bookingContext.getArgument() instanceof String s ? s : "";
		tokenEl = uifactory.addTextElement("token", "accesscontrol.token", 255, token, formLayout);
		tokenEl.setElementCssClass("o_sel_accesscontrol_token_entry");
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		String token = tokenEl.getValue();
		tokenEl.clearError();
		if (token == null || token.length() < 2) {
			tokenEl.setErrorKey("invalid.token.format");
			allOk = false;
		} else {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(bookingContext.getOfferAccess().getMethod().getType());
			if (!handler.checkArgument(bookingContext.getOfferAccess(), token)) {
				tokenEl.setErrorKey("invalid.token");
				allOk = false;
			}
		}

		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	public void back() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		bookingContext.setArgument(tokenEl.getValue());
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
