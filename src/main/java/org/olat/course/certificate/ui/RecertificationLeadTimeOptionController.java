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
package org.olat.course.certificate.ui;

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.CertificationTimeUnit;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;

/**
 * 
 * Initial date: 21 avr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecertificationLeadTimeOptionController extends FormBasicController {

	private TextElement reCertificationTimelapseEl;
	
	private RepositoryEntryCertificateConfiguration certificateConfig;
	
	public RecertificationLeadTimeOptionController(UserRequest ureq, WindowControl wControl, RepositoryEntryCertificateConfiguration certificateConfig) {
		super(ureq, wControl);
		this.certificateConfig = certificateConfig;
		
		initForm(ureq);
	}
	
	public int getLeadTimeInDays() {
		if(StringHelper.isLong(reCertificationTimelapseEl.getValue())) {
			return Math.abs(Integer.parseInt(reCertificationTimelapseEl.getValue()));
		}
		return 0;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		reCertificationTimelapseEl = uifactory.addTextElement("recertification.after.days", 6, "90", formLayout);
		reCertificationTimelapseEl.setElementCssClass("form-inline");
		reCertificationTimelapseEl.setTextAddOn("recertification.after.days.addon");
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("confirm.activate.recertification", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		reCertificationTimelapseEl.clearError();
		if(StringHelper.containsNonWhitespace(reCertificationTimelapseEl.getValue())) {
			try {
				Integer days = Integer.parseInt(reCertificationTimelapseEl.getValue());
				if(days.intValue() < 0) {
					reCertificationTimelapseEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				} else {
					int time = certificateConfig.getValidityTimelapse();
					CertificationTimeUnit timeUnit = certificateConfig.getValidityTimelapseUnit();
					if(timeUnit != null) {
						Date nextRecertification = timeUnit.toDate(ureq.getRequestTimestamp(), time);
						nextRecertification = CalendarUtils.endOfDay(nextRecertification);
						long nextRecertificationInDays = DateUtils.countDays(ureq.getRequestTimestamp(), nextRecertification);
						if(days >= nextRecertificationInDays) {
							reCertificationTimelapseEl.setErrorKey("error.recertication.time");
							allOk &= false;
						}
					}
				}
			} catch (NumberFormatException e) {
				logDebug("Wrong format: " + reCertificationTimelapseEl.getValue());
				reCertificationTimelapseEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}

		return allOk;
	}


	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
