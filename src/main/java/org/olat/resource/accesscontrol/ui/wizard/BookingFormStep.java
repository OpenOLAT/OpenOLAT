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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.OfferToSurvey;

/**
 * One step per form attached to the booked offer. The step title is the
 * form's step name, {@link org.olat.modules.forms.EvaluationFormSurvey#getDisplayName()}.
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BookingFormStep extends BasicStep {

	private final BookingContext bookingContext;
	private final OfferToSurvey offerToSurvey;

	/**
	 * @param remainingForms Consumed: the first form is used for this step, the
	 *            rest is passed on to the next step.
	 */
	public BookingFormStep(UserRequest ureq, BookingContext bookingContext, List<OfferToSurvey> remainingForms) {
		this(ureq, bookingContext, remainingForms, null);
	}

	/**
	 * @param remainingForms Consumed: the first form is used for this step, the
	 *            rest is passed on to the next step.
	 * @param tailStep Step to continue with once every form has its own step,
	 *            instead of the default end of the wizard.
	 */
	public BookingFormStep(UserRequest ureq, BookingContext bookingContext, List<OfferToSurvey> remainingForms, Step tailStep) {
		super(ureq);
		this.bookingContext = bookingContext;
		this.offerToSurvey = remainingForms.remove(0);

		setStepCollection(null);
		if (!remainingForms.isEmpty()) {
			setNextStep(new BookingFormStep(ureq, bookingContext, remainingForms, tailStep));
		} else if (tailStep != null) {
			setNextStep(tailStep);
		}
	}

	@Override
	public FormItem getStepTitle() {
		String stepName = StringHelper.escapeHtml(offerToSurvey.getSurvey().getDisplayName());
		FormLink stepTitle = new FormLinkImpl("booking.form.step." + offerToSurvey.getKey(), null, stepName,
				Link.FLEXIBLEFORMLNK + Link.NONTRANSLATED);
		stepTitle.setTranslator(getTranslator());
		return stepTitle;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext runContext, Form form) {
		form.setMultipartEnabled(true);
		return new BookingFormStepController(ureq, windowControl, form, runContext, bookingContext, offerToSurvey);
	}

}
