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
package org.olat.core.commons.services.analytics.ui;

import org.olat.core.commons.services.analytics.spi.GoogleAnalyticsSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin controller to configure the google analytics service. Basically asks for the Tracker-ID
 * 
 * Initial date: 15 feb. 2018<br>
 * @author Florian Gnägi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class GoogleAnalyticsConfigFormController extends FormBasicController {
	private TextElement analyticsTrackingIdEl;
		
	@Autowired
	private GoogleAnalyticsSPI googleAnalyticsSPI;


	/**
	 * Standard constructor
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public GoogleAnalyticsConfigFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("analytics.google.title");		
		setFormDescription("analytics.google.desc");

		// Tracker-ID is a text. We do not validate as we do not know what could
		// be possible values. We only check for empty values
		String analyticsTrackingId = googleAnalyticsSPI.getAnalyticsTrackingId();
		analyticsTrackingIdEl = uifactory.addTextElement("analytics.google.tracking.id", "analytics.google.tracking.id", 32, analyticsTrackingId, formLayout);
		analyticsTrackingIdEl.setPlaceholderText("UA-123456-1"); // example
		analyticsTrackingIdEl.setMandatory(true);
		
		// show error when not configured as the tracking ID is mandatory
		if(!googleAnalyticsSPI.isValid()) {
			analyticsTrackingIdEl.setErrorKey("form.legende.mandatory", null);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormResetButton("reset", "reset", buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);		
		analyticsTrackingIdEl.clearError();
		if(!StringHelper.containsNonWhitespace(analyticsTrackingIdEl.getValue())) {
			analyticsTrackingIdEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// just save to the service/module. No check for validity other than whitespace check
		String trackingId = analyticsTrackingIdEl.getValue().trim();
		googleAnalyticsSPI.setAnalyticsTrackingId(trackingId);
		logAudit("Google Analytics Tracking-ID changed", trackingId);										
		fireEvent(ureq, Event.DONE_EVENT);		
	}
}
