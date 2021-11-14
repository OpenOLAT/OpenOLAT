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
package org.olat.modules.appointments.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FindingConfirmationController extends FormBasicController {
	
	private static final String usageIdentifyer = UserRestrictionTableModel.class.getCanonicalName();

	private UserRestrictionTableModel usersTableModel;
	private FlexiTableElement usersTableEl;
	
	private final Appointment appointment;
	private final Topic topic;
	private final RepositoryEntry entry;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private List<Participation> participations;
	private List<Long> participationIdentityKeys;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserManager userManager;

	public FindingConfirmationController(UserRequest ureq, WindowControl wControl, Appointment appointment) {
		super(ureq, wControl, "finding_confirmation");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.appointment = appointment;
		this.topic = appointment.getTopic();
		this.entry = topic.getEntry();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
	
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("finding.confirmation.info");
		
		if (appointment.getBBBMeeting() != null) {
			flc.contextPut("meeting", Boolean.TRUE);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = UserRestrictionTableModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		usersTableModel = new UserRestrictionTableModel(columnsModel, getLocale()); 
		usersTableEl = uifactory.addTableElement(getWindowControl(), "users", usersTableModel, 20, false, getTranslator(), formLayout);
		usersTableEl.setAndLoadPersistedPreferences(ureq, "finding.confirmation.v2");
		usersTableEl.setEmptyTableMessageKey("finding.confirmation.empty.table");
		usersTableEl.setSelectAllEnable(true);
		usersTableEl.setMultiSelect(true);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	private void loadModel() {
		boolean groupRestrictions = appointmentsService.hasGroupRestrictions(topic);
		
		List<Identity> restrictedIdentities = groupRestrictions
				? appointmentsService.getRestrictionMembers(topic)
				: repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
				
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(appointment);
		params.setFetchIdentities(true);
		params.setFetchUser(true);
		participations = appointmentsService.getParticipations(params);
		
		List<Identity> participationIdentities = new ArrayList<>(participations.size());
		participationIdentityKeys = new ArrayList<>(participations.size());
		for (Participation participation : participations) {
			Identity identity = participation.getIdentity();
			participationIdentities.add(identity);
			participationIdentityKeys.add(identity.getKey());
		}
		
		Set<Identity> allIdentities = new HashSet<>();
		allIdentities.addAll(participationIdentities);
		allIdentities.addAll(restrictedIdentities);
		
		List<UserPropertiesRow> rows = new ArrayList<>(allIdentities.size());
		for (Identity identity : allIdentities) {
			rows.add(new UserPropertiesRow(identity, userPropertyHandlers, getLocale()));
		}
		
		usersTableModel.setObjects(rows);
		usersTableEl.reset(true, true, true);
	
		Set<Integer> selectedRows = new HashSet<>();
		for(int i=usersTableModel.getRowCount(); i--> 0; ) {
			UserPropertiesRow row = usersTableModel.getObject(i);
			if(participationIdentityKeys.contains(row.getIdentityKey())) {
				selectedRows.add(Integer.valueOf(i));
			}
		}
		usersTableEl.setMultiSelectedIndex(selectedRows);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		usersTableEl.clearError();
		if (appointment.getBBBMeeting() != null) {
			AppointmentSearchParams params = new AppointmentSearchParams();
			params.setAppointment(appointment);
			params.setFetchMeetings(true);
			List<Appointment> appointments = appointmentsService.getAppointments(params);
			if (!appointments.isEmpty()) {
				Integer maxMeetingParticipants = appointments.get(0).getBBBMeeting().getTemplate().getMaxParticipants();
				if (maxMeetingParticipants.intValue() < usersTableEl.getMultiSelectedIndex().size()) {
					usersTableEl.setErrorKey("error.selected.identities.greater.room", new String[] {maxMeetingParticipants.toString()});
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> index = usersTableEl.getMultiSelectedIndex();
		List<Long> selectedIdentityKeys = new ArrayList<>(index.size());
		for (Integer i : index) {
			UserPropertiesRow row = usersTableModel.getObject(i.intValue());
			selectedIdentityKeys.add(row.getIdentityKey());
		}
		
		Collection<Long> addedIdentityKeys = new ArrayList<>();
		for (Long selectedKey : selectedIdentityKeys) {
			if (!participationIdentityKeys.contains(selectedKey)) {
				addedIdentityKeys.add(selectedKey);
			}
		}
		Collection<Identity> identities = securityManager.loadIdentityByKeys(addedIdentityKeys);
		appointmentsService.createParticipations(appointment, identities, getIdentity(), topic.isMultiParticipation(),
				topic.isAutoConfirmation(), false);
		
		List<Participation> unselectedParticipations = new ArrayList<>();
		for (Participation participation : participations) {
			Long identityKey = participation.getIdentity().getKey();
			if (!selectedIdentityKeys.contains(identityKey)) {
				unselectedParticipations.add(participation);
			}
		}
		appointmentsService.deleteParticipations(unselectedParticipations, false);
		
		appointmentsService.confirmAppointment(appointment);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
