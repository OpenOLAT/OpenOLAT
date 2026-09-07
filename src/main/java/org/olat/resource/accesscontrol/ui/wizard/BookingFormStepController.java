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
package org.olat.resource.accesscontrol.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.CachedRunContextController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.forms.CoachCandidates;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.resource.accesscontrol.OfferToSurvey;

/**
 * Wizard step wrapping one {@link EvaluationFormExecutionController}, kept
 * alive across back and forward navigation via {@link CachedRunContextController}.
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BookingFormStepController extends StepFormBasicController {

	private final CachedRunContextController<EvaluationFormExecutionController> executionCtrl;

	public BookingFormStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			BookingContext bookingContext, OfferToSurvey offerToSurvey) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		executionCtrl = CachedRunContextController.of(runContext, "form." + offerToSurvey.getSurvey().getKey(),
				() -> new EvaluationFormExecutionController(ureq, wControl, rootForm,
						bookingContext.reload(offerToSurvey.getSurvey()), CoachCandidates.NONE, null),
				this);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}

	@Override
	public FormItem getStepFormItem() {
		return executionCtrl.getStepFormItem();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		List<ValidationMessage> messages = new ArrayList<>(1);
		boolean allOk = executionCtrl.get().validate(ureq, messages);
		if (!allOk) {
			for (ValidationMessage message : messages) {
				getWindowControl().setWarning(message.getMessage());
			}
		}
		return allOk;
	}

	@Override
	public void back() {
		// Nothing to commit: the answers stay in executionCtrl, which is kept
		// alive across navigation, see CachedRunContextController.
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		executionCtrl.release(this);
		super.doDispose();
	}

}
