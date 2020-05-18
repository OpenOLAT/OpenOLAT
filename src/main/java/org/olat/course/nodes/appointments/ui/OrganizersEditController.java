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
package org.olat.course.nodes.appointments.ui;

import static org.olat.core.gui.components.util.KeyValues.VALUE_ASC;
import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Organizer;
import org.olat.course.nodes.appointments.Topic;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganizersEditController extends FormBasicController {

	private MultipleSelectionElement organizerEl;

	private final Topic topic;
	private List<Organizer> organizers;
	private List<Identity> coaches;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserManager userManager;

	public OrganizersEditController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl);
		this.topic = topic;
		organizers = appointmentsService.getOrganizers(topic);
		coaches = repositoryService.getMembers(topic.getEntry(), RepositoryEntryRelationType.all,
				GroupRoles.coach.name());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		KeyValues coachesKV = new KeyValues();
		for (Identity coach : coaches) {
			coachesKV.add(entry(coach.getKey().toString(), userManager.getUserDisplayName(coach.getKey())));
		}
		coachesKV.sort(VALUE_ASC);
		organizerEl = uifactory.addCheckboxesVertical("coaches", formLayout, coachesKV.keys(), coachesKV.values(), 1);
		for (Organizer organizer : organizers) {
			Long organizerKey = organizer.getIdentity().getKey();
			if (coaches.stream().anyMatch(coach -> organizerKey.equals(coach.getKey()))) {
				organizerEl.select(organizerKey.toString(), true);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> selectedOrganizerKeys = organizerEl.getSelectedKeys();
		
		// delete unselected
		List<Organizer> organizersToDelete = organizers.stream()
				.filter(organizer -> !selectedOrganizerKeys.contains(organizer.getIdentity().getKey().toString()))
				.collect(Collectors.toList());
		appointmentsService.deleteOrganizers(topic, organizersToDelete);
		
		// create newly selected
		Set<String> currentOrganizerKeys = organizers.stream()
				.map(o -> o.getIdentity().getKey().toString())
				.collect(Collectors.toSet());
		selectedOrganizerKeys.removeAll(currentOrganizerKeys);
		coaches.stream()
				.filter(coach -> selectedOrganizerKeys.contains(coach.getKey().toString()))
				.forEach(coach -> appointmentsService.createOrganizer(topic, coach));
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
