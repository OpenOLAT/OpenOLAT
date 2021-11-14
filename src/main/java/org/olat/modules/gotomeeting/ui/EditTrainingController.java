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
package org.olat.modules.gotomeeting.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.model.GoToError;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTrainingController extends FormBasicController {
	
	private SingleSelection organizersEl;
	private TextElement nameEl, descriptionEl;
	private DateChooser startDateEl, endDateEl;
	private FormLink addMeAsOrganizerButton;
	
	private List<GoToOrganizer> availablesOrganizers;
	
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	private final GoToMeeting meeting;
	
	private CloseableModalController cmc;
	private LoginOrganizerController addOrganizerController;

	@Autowired
	private GoToMeetingManager meetingManager;
	
	public EditTrainingController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		super(ureq, wControl);

		this.entry = entry;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		this.meeting = null;
		availablesOrganizers = meetingManager.getOrganizersFor(getIdentity());
		
		initForm(ureq);
	}
	
	public EditTrainingController(UserRequest ureq, WindowControl wControl, GoToMeeting meeting) {
		super(ureq, wControl);

		this.entry = null;
		this.subIdent = null;
		this.businessGroup = null;
		this.meeting = meeting;
		availablesOrganizers = meetingManager.getOrganizersFor(getIdentity());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean isOrganizer = false;
		String[] organizerKeys = new String[availablesOrganizers.size()];
		String[] organizerValues = new String[availablesOrganizers.size()];
		for(int i=availablesOrganizers.size(); i-->0; ) {
			GoToOrganizer organizer = availablesOrganizers.get(i);
			organizerKeys[i] = Integer.toString(i);
			String label = organizer.getName();
			if(!StringHelper.containsNonWhitespace(label)) {
				label = organizer.getUsername();
			}
			organizerValues[i] = label;
			if(organizer.getOwner() != null) {
				isOrganizer = true;
			}
		}
		organizersEl = uifactory.addDropdownSingleselect("organizers", "organizers", formLayout, organizerKeys, organizerValues, null);
		organizersEl.setMandatory(true);
		if(meeting != null && meeting.getOrganizer() != null) {
			organizersEl.setVisible(false);
			isOrganizer = true;
		}
		
		if(!isOrganizer) {
			addMeAsOrganizerButton = uifactory.addFormLink("add.my.account", formLayout, Link.BUTTON);
		}
		
		String name = meeting == null ? "" : meeting.getName();
		nameEl = uifactory.addTextElement("training.name", "training.name", 128, name, formLayout);
		nameEl.setMandatory(true);
		String description = meeting == null ? "" : meeting.getDescription();
		descriptionEl = uifactory.addTextAreaElement("training.description", "training.description", 2000, 8, 72, false, false, description, formLayout);

		Date startDate = meeting == null ? null : meeting.getStartDate();
		startDateEl = uifactory.addDateChooser("training.start", "training.start", startDate, formLayout);
		startDateEl.setMandatory(true);
		startDateEl.setDateChooserTimeEnabled(true);
		Date endDate = meeting == null ? null : meeting.getEndDate();
		endDateEl = uifactory.addDateChooser("training.end", "training.end", endDate, formLayout);
		endDateEl.setMandatory(true);
		endDateEl.setDateChooserTimeEnabled(true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("ok", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(meeting == null || meeting.getOrganizer() == null) {
			organizersEl.clearError();
			if(!organizersEl.isOneSelected()) {
				organizersEl.setErrorKey("form.mandatory.hover", null);
				allOk &= false;
			} else {
				Date start = startDateEl.getDate();
				Date end = endDateEl.getDate();
				
				String selectedKeyStr = organizersEl.getSelectedKey();
				int selectedIndex = Integer.parseInt(selectedKeyStr);
				GoToOrganizer organizer = availablesOrganizers.get(selectedIndex);
				if(start != null && end != null && organizer != null) {
					if(!meetingManager.checkOrganizerAvailability(organizer, start, end)) {
						organizersEl.setErrorKey("error.organizer.overbooked", null);
						allOk &= false;
					}
				}
			}
		}
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		startDateEl.clearError();
		if(startDateEl.getDate() == null) {
			startDateEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		endDateEl.clearError();
		if(endDateEl.getDate() == null) {
			endDateEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		if(startDateEl.getDate() != null && endDateEl.getDate() != null) {
			Date start = startDateEl.getDate();
			Date end = endDateEl.getDate();
			if(start.compareTo(end) >= 0) {
				startDateEl.setErrorKey("error.start.after.end", null);
			}
		}
		
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addOrganizerController == source) {
			if(event == Event.DONE_EVENT) {
				doSetPersonalAccount();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addOrganizerController);
		removeAsListenerAndDispose(cmc);
		addOrganizerController = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMeAsOrganizerButton == source) {
			doAddPersonalAccount(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = nameEl.getValue();
		String description = descriptionEl.getValue();
		Date start = startDateEl.getDate();
		Date end = endDateEl.getDate();

		GoToError error = new GoToError();
		if(meeting == null) {
			String selectedKeyStr = organizersEl.getSelectedKey();
			int selectedIndex = Integer.parseInt(selectedKeyStr);
			GoToOrganizer organizer = availablesOrganizers.get(selectedIndex);
			meetingManager.scheduleTraining(organizer, name, null, description, start, end, entry, subIdent, businessGroup, error);
		} else {
			meetingManager.updateTraining(meeting, name, description, start, end, error);
		}
		if(error.hasError()) {
			fireEvent(ureq, new GoToErrorEvent(error));
		} else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doAddPersonalAccount(UserRequest ureq) {
		if(guardModalController(addOrganizerController)) return;
		
		addOrganizerController = new LoginOrganizerController(ureq, getWindowControl(), getIdentity());
		listenTo(addOrganizerController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addOrganizerController.getInitialComponent(),
				true, translate("add.organizer"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doSetPersonalAccount() {
		availablesOrganizers = meetingManager.getOrganizersFor(getIdentity());
		
		String selectedKey = null;
		String[] organizerKeys = new String[availablesOrganizers.size()];
		String[] organizerValues = new String[availablesOrganizers.size()];
		for(int i=availablesOrganizers.size(); i-->0; ) {
			GoToOrganizer organizer = availablesOrganizers.get(i);
			organizerKeys[i] = Integer.toString(i);
			organizerValues[i] = organizer.getUsername();
			if(organizer.getOwner() != null) {
				selectedKey = organizerKeys[i];
			}
		}
		organizersEl.setKeysAndValues(organizerKeys, organizerValues, null);
		if(selectedKey != null) {
			organizersEl.select(selectedKey, true);
			addMeAsOrganizerButton.setVisible(false);
		}
	}
}
