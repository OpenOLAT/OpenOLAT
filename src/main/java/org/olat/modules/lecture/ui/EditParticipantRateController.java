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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureParticipantSummary;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditParticipantRateController extends FormBasicController {
	
	private TextElement rateEl;
	private FormLink removeCustomRateButton;
	
	private double defaultRate;
	private LectureParticipantSummary participantSummary;
	
	@Autowired
	private LectureService lectureService;

	public EditParticipantRateController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Identity identity, double defaultRate) {
		super(ureq, wControl);
		this.defaultRate = defaultRate;
		participantSummary = lectureService.getOrCreateParticipantSummary(entry, identity);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		long rate = Math.round(defaultRate * 100.0d);
		uifactory.addStaticTextElement("entry.rate", Long.toString(rate) + "%", formLayout);
		
		String customRate = "";
		if(participantSummary.getRequiredAttendanceRate() != null) {
			long cRate = Math.round(participantSummary.getRequiredAttendanceRate().doubleValue() * 100.0d);
			customRate = Long.toString(cRate);
		}
		rateEl = uifactory.addTextElement("participant.rate", "participant.rate", 4, customRate, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		removeCustomRateButton = uifactory.addFormLink("remove.custom.rate", "remove.custom.rate", null, buttonsCont, Link.BUTTON);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		rateEl.clearError();
		if(StringHelper.containsNonWhitespace(rateEl.getValue())) {
			try {
				int val = Integer.parseInt(rateEl.getValue());
				if(val < 0 && val > 100) {
					rateEl.setErrorKey("error.integer.between", new String[] {"0", "100"});
				}
			} catch (NumberFormatException e) {
				rateEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(removeCustomRateButton == source) {
			doRemoveCustomRate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String customRate = rateEl.getValue();
		if(StringHelper.containsNonWhitespace(customRate)) {
			double val = Long.parseLong(customRate) / 100.0d;
			participantSummary.setRequiredAttendanceRate(val);
		} else {
			participantSummary.setRequiredAttendanceRate(null);
		}
		participantSummary = lectureService.saveParticipantSummary(participantSummary);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doRemoveCustomRate(UserRequest ureq) {
		participantSummary.setRequiredAttendanceRate(null);
		participantSummary = lectureService.saveParticipantSummary(participantSummary);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
