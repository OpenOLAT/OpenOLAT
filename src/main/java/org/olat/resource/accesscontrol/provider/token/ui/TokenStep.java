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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.resource.accesscontrol.OfferToSurvey;
import org.olat.resource.accesscontrol.ui.wizard.BookingContext;
import org.olat.resource.accesscontrol.ui.wizard.BookingFormStep;

/**
 * First step of the booking wizard for an "access code" offer: enter and
 * check the code, before the forms attached to the offer.
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TokenStep extends BasicStep {

	private final BookingContext bookingContext;

	public TokenStep(UserRequest ureq, BookingContext bookingContext, List<OfferToSurvey> offerToSurveys) {
		super(ureq);
		this.bookingContext = bookingContext;

		setI18nTitleAndDescr("token.details.title", null);
		setNextStep(new BookingFormStep(ureq, bookingContext, new ArrayList<>(offerToSurveys)));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext runContext, Form form) {
		return new TokenStepController(ureq, windowControl, form, runContext, bookingContext);
	}

}
