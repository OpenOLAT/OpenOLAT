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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.appointments.OrganizerCandidateSupplier;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight;
import org.olat.modules.appointments.ui.DuplicateTopicCallback.DuplicationContext;

/**
 * 
 * Initial date: 19 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class DuplicateTopic1StepController extends StepFormBasicController {

	private final DuplicateTopicEditController editCtrl;

	private final DuplicationContext context;

	public DuplicateTopic1StepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, Topic sourceTopic, OrganizerCandidateSupplier organizerCandidateSupplier) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		
		context = DuplicateTopicCallback.getDuplicationContext(runContext);
		context.setEntry(sourceTopic.getEntry());
		context.setSubIdent(sourceTopic.getSubIdent());
		
		TopicLight topic = DuplicateTopicCallback.toTransientTopic(sourceTopic);
		context.setTopic(topic);
		
		editCtrl = new DuplicateTopicEditController(ureq, wControl, rootForm, topic, sourceTopic, organizerCandidateSupplier);
		listenTo(editCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("edit", editCtrl.getInitialFormItem());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		editCtrl.updatedAttributes(context.getTopic());
		context.setOrganizers(editCtrl.getOrganizers());
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
