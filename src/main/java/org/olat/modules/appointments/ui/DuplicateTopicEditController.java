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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.OrganizerCandidateSupplier;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 19 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DuplicateTopicEditController extends AbstractTopicController {

	private final Topic sourceTopic;

	public DuplicateTopicEditController(UserRequest ureq, WindowControl wControl, Form rootForm, TopicLight topic,
			Topic sourceTopic, OrganizerCandidateSupplier organizerCandidateSupplier) {
		super(ureq, wControl, rootForm, topic, organizerCandidateSupplier);
		this.sourceTopic = sourceTopic;
		init(ureq);
	}

	@Override
	protected RepositoryEntryRef getRepositoryEntry() {
		return sourceTopic.getEntry();
	}

	@Override
	protected List<Organizer> getCurrentOrganizers() {
		return appointmentsService.getOrganizers(sourceTopic);
	}

	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		//
	}

	@Override
	protected boolean isConfigChangeable() {
		return true;
	}

	@Override
	protected boolean isConfigChanged() {
		return false;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
