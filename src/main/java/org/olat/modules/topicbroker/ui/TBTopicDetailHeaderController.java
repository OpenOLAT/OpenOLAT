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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.topicbroker.TBSecurityCallback;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.events.TBTopicEditEnrollmentsEvent;
import org.olat.modules.topicbroker.ui.events.TBTopicEditEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicDetailHeaderController extends FormBasicController {
	
	public static final int IMAGE_WIDTH = 300;
	public static final int IMAGE_HEIGHT = (IMAGE_WIDTH / 3) * 2;

	private FormLink editEnrollmentsLink;
	private DropdownItem editDropdown;
	private FormLink editLink;

	private final TBTopic topic;
	private final TBSecurityCallback secCallback;
	private final int numEnrollments;
	private final int waitingList;

	@Autowired
	private TopicBrokerService topicBrokerService;
	
	protected TBTopicDetailHeaderController(UserRequest ureq, WindowControl wControl, Form mainForm, TBTopic topic,
			TBSecurityCallback secCallback, int numEnrollments, int waitingList) {
		super(ureq, wControl, LAYOUT_CUSTOM, "topic_detail_header", mainForm);
		this.topic = topic;
		this.secCallback = secCallback;
		this.numEnrollments = numEnrollments;
		this.waitingList = waitingList;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		VFSLeaf image = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TOPIC_TEASER_IMAGE);
		VFSLeaf video = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TOPIC_TEASER_VIDEO);
		if (image != null || video != null) {
			ImageComponent ic = new ImageComponent(ureq.getUserSession(), "thumbnail");
			if (video != null) {
				ic.setMedia(video);
				ic.setMaxWithAndHeightToFitWithin(IMAGE_WIDTH, IMAGE_HEIGHT);
				if (image != null) {
					ic.setPoster(image);
				}
			} else {
				ic.setMedia(image);
				ic.setMaxWithAndHeightToFitWithin(IMAGE_WIDTH, IMAGE_HEIGHT);
			}
			flc.put("thumbnail", ic);
		} else {
			flc.contextPut("titleAbbr", TBUIFactory.getTitleAbbr(topic.getTitle()));
		}
		
		flc.contextPut("title", topic.getTitle());
		flc.contextPut("numEnrollments", numEnrollments);
		flc.contextPut("waitingList", waitingList);
		
		if (secCallback.canEditSelections()) {
			editEnrollmentsLink = uifactory.addFormLink("enrollments.edit", formLayout, Link.BUTTON);
			editEnrollmentsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_tb_edit_enrollments");
		}
		if (secCallback.canEditTopics()) {
			if (editEnrollmentsLink != null) {
				editDropdown = uifactory.addDropdownMenu("editDropdown", null, null, formLayout, getTranslator());
				editDropdown.setOrientation(DropdownOrientation.right);
				
				editLink = uifactory.addFormLink("topic.edit", formLayout, Link.LINK);
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
				editDropdown.addElement(editLink);
			} else {
				editLink = uifactory.addFormLink("topic.edit", formLayout, Link.BUTTON);
				editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editLink) {
			fireEvent(ureq, new TBTopicEditEvent(topic));
		} else if (source == editEnrollmentsLink) {
			fireEvent(ureq, new TBTopicEditEnrollmentsEvent(topic));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
