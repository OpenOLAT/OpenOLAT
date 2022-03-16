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
package org.olat.modules.message.ui;

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.message.AssessmentMessage;
import org.olat.modules.message.AssessmentMessagePublicationEnum;
import org.olat.modules.message.AssessmentMessageService;
import org.olat.modules.message.AssessmentMessageStatusEnum;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageEditController extends FormBasicController {
	
	private TextElement messageEl;
	private DateChooser publicationDateEl;
	private DateChooser expirationDateEl;
	private SingleSelection publicationEl;
	
	private AssessmentMessage message;
	private final RepositoryEntry entry;
	private final String resSubPath;
	private AssessmentMessageStatusEnum status;
	
	@Autowired
	private AssessmentMessageService assessmentMessageService;
	
	public AssessmentMessageEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String resSubPath) {
		super(ureq, wControl);
		this.entry = entry;
		this.resSubPath = resSubPath;
		status = AssessmentMessageStatusEnum.planned;
		initForm(ureq);
		updateUI();
	}
	
	public AssessmentMessageEditController(UserRequest ureq, WindowControl wControl, AssessmentMessage message) {
		super(ureq, wControl);
		this.message = message;
		status = AssessmentMessageStatusEnum.valueOf(message, ureq.getRequestTimestamp());
		entry = message.getEntry();
		resSubPath = message.getResSubPath();
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String text = message == null ? null : message.getMessage();
		messageEl = uifactory.addTextAreaElement("message.text", "message.text", 2000, 10, 60, false, false, false, text, formLayout);
		messageEl.setMandatory(true);
		messageEl.setEnabled(status == AssessmentMessageStatusEnum.planned);
		
		SelectionValues publicationKeys = new SelectionValues();
		publicationKeys.add(SelectionValues.entry(AssessmentMessagePublicationEnum.asap.name(), translate("publication.type.asap")));
		publicationKeys.add(SelectionValues.entry(AssessmentMessagePublicationEnum.scheduled.name(), translate("publication.type.scheduled")));
		publicationEl = uifactory.addRadiosVertical("publication.type", "publication.type", formLayout, publicationKeys.keys(), publicationKeys.values());
		publicationEl.setEnabled(status == AssessmentMessageStatusEnum.planned);
		publicationEl.addActionListener(FormEvent.ONCHANGE);
		if(message == null || message.getPublicationType() == AssessmentMessagePublicationEnum.asap) {
			publicationEl.select(AssessmentMessagePublicationEnum.asap.name(), true);
		} else {
			publicationEl.select(AssessmentMessagePublicationEnum.scheduled.name(), true);
		}
		
		Date publicationDate = message == null ? CalendarUtils.startOfDay(ureq.getRequestTimestamp()) : message.getPublicationDate();
		publicationDateEl = uifactory.addDateChooser("publication.date", "publication.date", publicationDate, formLayout);
		publicationDateEl.setEnabled(status == AssessmentMessageStatusEnum.planned);
		publicationDateEl.setDateChooserTimeEnabled(true);
		publicationDateEl.setMandatory(true);
		Date expirationDate = message == null ? CalendarUtils.endOfDay(ureq.getRequestTimestamp()) : message.getExpirationDate();
		expirationDateEl = uifactory.addDateChooser("expiration.date", "expiration.date", expirationDate, formLayout);
		expirationDateEl.setEnabled(status == AssessmentMessageStatusEnum.planned || status == AssessmentMessageStatusEnum.published);
		expirationDateEl.setDateChooserTimeEnabled(true);
		expirationDateEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		if(status == AssessmentMessageStatusEnum.planned || status == AssessmentMessageStatusEnum.published) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}
	
	private void updateUI() {
		boolean scheduled = publicationEl.isOneSelected()
				&& AssessmentMessagePublicationEnum.scheduled.name().equals(publicationEl.getSelectedKey());
		publicationDateEl.setVisible(scheduled);
		expirationDateEl.setVisible(scheduled);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		publicationEl.clearError();
		if(!publicationEl.isOneSelected()) {
			publicationEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		messageEl.clearError();
		if(!StringHelper.containsNonWhitespace(messageEl.getValue())) {
			messageEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(messageEl.getValue().length() > 500) {
			messageEl.setErrorKey("form.error.toolong", new String[] { Integer.toString(500) });
			allOk &= false;
		}
		
		publicationDateEl.clearError();
		expirationDateEl.clearError();
		if(publicationDateEl.isVisible() && expirationDateEl.isVisible()) {
			if(publicationDateEl.getDate() == null) {
				publicationDateEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			if(expirationDateEl.getDate() == null) {
				expirationDateEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			if(publicationDateEl.getDate() != null && expirationDateEl.getDate() != null
					&& publicationDateEl.getDate().after(expirationDateEl.getDate())) {
				expirationDateEl.setErrorKey("error.publication.date.after", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(publicationEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AssessmentMessagePublicationEnum publicationType = AssessmentMessagePublicationEnum.valueOf(publicationEl.getSelectedKey());
		if(message == null) {
			Date publicationDate;
			Date expirationDate;
			if(publicationType == AssessmentMessagePublicationEnum.asap) {
				publicationDate = CalendarUtils.removeSeconds(ureq.getRequestTimestamp());
				expirationDate = CalendarUtils.endOfDay(publicationDate);
			} else {
				publicationDate = publicationDateEl.getDate();
				expirationDate = expirationDateEl.getDate();
			}
			
			message = assessmentMessageService.createAndPersistMessage(messageEl.getValue(),
					publicationDate, expirationDate, publicationType, entry, resSubPath, getIdentity());
		} else {
			if(publicationType == AssessmentMessagePublicationEnum.scheduled) {
				message.setPublicationDate(publicationDateEl.getDate());
				message.setExpirationDate(expirationDateEl.getDate());
			}
			message.setMessage(messageEl.getValue());
			message.setPublicationType(publicationType);
			message = assessmentMessageService.updateMessage(message, getIdentity());
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
