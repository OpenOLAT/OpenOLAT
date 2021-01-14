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

import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicEditController extends AbstractTopicController {
	
	private Topic topic;

	public TopicEditController(UserRequest ureq, WindowControl wControl, Topic topic) {
		super(ureq, wControl, topic);
		this.topic = topic;
		init(ureq);
	}

	@Override
	protected RepositoryEntryRef getRepositoryEntry() {
		return topic.getEntry();
	}

	@Override
	protected List<Organizer> getCurrentOrganizers() {
		return appointmentsService.getOrganizers(topic);
	}

	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSaveTopic();
		doSaveOrgianzers();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doSaveTopic() {
		updatedAttributes(topic);
		topic = appointmentsService.updateTopic(topic);
	}
	
	private void doSaveOrgianzers() {
		appointmentsService.updateOrganizers(topic, getOrganizers());
	}
	
	@Override
	protected boolean isConfigChanged() {
		Type type = typeEl.isOneSelected() ? Type.valueOf(typeEl.getSelectedKey()) : Type.enrollment;
		Collection<String> configKeys = configurationEl.getSelectedKeys();
		boolean multiParticipation = configKeys.contains(KEY_MULTI_PARTICIPATION);
		boolean autoConfirmation = Type.finding == type
				? false
				: !configKeys.contains(KEY_COACH_CONFIRMATION);
		
		return type != topic.getType()
				|| multiParticipation != topic.isMultiParticipation()
				|| autoConfirmation != topic.isAutoConfirmation();
	}

	@Override
	protected boolean isConfigChangeable() {
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setTopic(topic);
		Long participationCount = appointmentsService.getParticipationCount(params);
		return participationCount.longValue() == 0;
	}

}
