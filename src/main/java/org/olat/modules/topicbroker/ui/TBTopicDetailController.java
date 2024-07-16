/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroupShort;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBSecurityCallback;
import org.olat.modules.topicbroker.TBTopic;

/**
 * 
 * Initial date: 11 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicDetailController extends FormBasicController {

	private TBTopicDetailHeaderController headerCtrl;
	private TBTopicDescriptionController descriptionCtrl;

	private final TBTopic topic;
	private final List<BusinessGroupShort> groupRestrictions;
	private final List<TBCustomField> customFields;
	private final TBSecurityCallback secCallback;
	private final int numEnrollments;
	private final int waitingList;

	public TBTopicDetailController(UserRequest ureq, WindowControl wControl, Form mainForm, TBTopic topic,
			List<BusinessGroupShort> groupRestrictions, List<TBCustomField> customFields, TBSecurityCallback secCallback,
			int numEnrollments, int waitingList) {
		super(ureq, wControl, LAYOUT_CUSTOM, "topic_details", mainForm);
		this.topic = topic;
		this.groupRestrictions = groupRestrictions;
		this.customFields = customFields;
		this.secCallback = secCallback;
		this.numEnrollments = numEnrollments;
		this.waitingList = waitingList;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		headerCtrl = new TBTopicDetailHeaderController(ureq, getWindowControl(), mainForm, topic, secCallback, numEnrollments, waitingList);
		listenTo(headerCtrl);
		formLayout.add(headerCtrl.getInitialFormItem());
		flc.put("header", headerCtrl.getInitialComponent());
		
		descriptionCtrl = new TBTopicDescriptionController(ureq, getWindowControl(), topic, groupRestrictions, customFields);
		listenTo(descriptionCtrl);
		flc.put("description", descriptionCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == headerCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
