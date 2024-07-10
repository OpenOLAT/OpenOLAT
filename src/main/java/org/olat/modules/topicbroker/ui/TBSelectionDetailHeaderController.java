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

import static org.olat.modules.topicbroker.ui.TBTopicDetailHeaderController.IMAGE_HEIGHT;
import static org.olat.modules.topicbroker.ui.TBTopicDetailHeaderController.IMAGE_WIDTH;

import java.util.List;
import java.util.Optional;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBSelectionStatus;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBSelectionDetailHeaderController extends FormBasicController {
	
	private static final String CMD_SELECT = "select";
	private static final String CMD_UNSELECT = "unselect";
	private static final String CMD_SELECT_FIRST = "select_first";
	private static final String CMD_SELECT_LAST = "select_last";
	private static final String CMD_SELECT_POS = "select_pos_";

	private FormLink selectButton;
	private FormLink unselectButton;
	private DropdownItem selectDropdown;
	
	private TBBroker broker;
	private final TBTopic topic;
	private final Long topicKey;
	private final TBParticipant participant;
	private final TBPeriodEvaluator periodEvaluator;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	public TBSelectionDetailHeaderController(UserRequest ureq, WindowControl wControl, TBBroker broker, TBParticipant participant, TBTopic topic) {
		super(ureq, wControl, "selection_detail_header");
		this.broker = broker;
		this.topic = topic;
		this.participant = participant;
		topicKey = topic.getKey();
		periodEvaluator = new TBPeriodEvaluator(broker);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		VFSLeaf image = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TOPIC_TEASER_IMAGE);
		VFSLeaf video = topicBrokerService.getTopicLeaf(topic, TopicBrokerService.TOPIC_TEASER_VIDEO);
		boolean hasThumbnail = image != null || video != null;
		if (hasThumbnail) {
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
		
		selectButton = uifactory.addFormLink("select", CMD_SELECT, "select", null, flc, Link.BUTTON);
		selectButton.setPrimary(true);
		selectButton.setVisible(false);
		
		unselectButton = uifactory.addFormLink("unselect", CMD_UNSELECT, "withdraw", null, flc, Link.BUTTON);
		unselectButton.setVisible(false);
		
		selectDropdown = uifactory.addDropdownMenu("selectDropdown", null, null, formLayout, getTranslator());
		selectDropdown.setOrientation(hasThumbnail? DropdownOrientation.right: DropdownOrientation.normal);
		selectDropdown.setPrimary(true);
		selectDropdown.setVisible(false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void updateUI() {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setIdentity(participant.getIdentity());
		List<TBSelection> selections = topicBrokerService.getSelections(searchParams);
		Optional<TBSelection> topicSelection = selections.stream().filter(selection -> topicKey.equals(selection.getTopic().getKey())).findFirst();
		if (topicSelection.isPresent()) {
			TBSelection selection = topicSelection.get();
			int numEnrollments = (int)selections.stream().filter(TBSelection::isEnrolled).count();
			int requiredEnrollments = TBUIFactory.getRequiredEnrollments(broker, participant);
			TBSelectionStatus status = TBUIFactory.getSelectionStatus(broker, requiredEnrollments, numEnrollments, true,
					selection.isEnrolled(), selection.getSortOrder());
			
			flc.contextPut("status", TBUIFactory.getTranslatedStatus(getTranslator(), status));
			flc.contextPut("statusIcon", TBUIFactory.getStatusIconCss(status));
			flc.contextPut("statusCss", TBUIFactory.getLabelLightCss(status));
			
			flc.contextPut("priority", TBUIFactory.getPriorityLabel(getTranslator(), status, selection.getSortOrder()));
			
			selectButton.setVisible(false);
			selectDropdown.setVisible(false);
			unselectButton.setVisible(periodEvaluator.isSelectionPeriod() || periodEvaluator.isWithdrawPeriod());
		} else {
			flc.contextRemove("status");
			flc.contextRemove("priority");
			if (periodEvaluator.isSelectionPeriod()) {
				selectDropdown.removeAllFormItems();
				
				FormLink selectFirstLink = uifactory.addFormLink("selectf", CMD_SELECT_FIRST, "", null, flc, Link.NONTRANSLATED);
				selectFirstLink.setI18nKey(translate("select.pos.first"));
				selectFirstLink.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_select_first");
				selectDropdown.addElement(selectFirstLink);
				
				FormLink selectLastLink = uifactory.addFormLink("selectl", CMD_SELECT_LAST, "", null, flc, Link.NONTRANSLATED);
				selectLastLink.setI18nKey(translate("select.pos.last"));
				selectLastLink.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_select_last");
				selectDropdown.addElement(selectLastLink);
				
				selectDropdown.addElement(new SpacerItem("space"));
				
				for (int i = 1; i <= selections.size() + 1; i++) {
					String cmd = CMD_SELECT_POS + i;
					FormLink selectPosLink = uifactory.addFormLink("selectp_" + i, cmd, "", null, flc, Link.NONTRANSLATED);
					selectPosLink.setI18nKey(translate("select.pos", String.valueOf(i)));
					selectDropdown.addElement(selectPosLink);
				}
			}
				
			selectButton.setVisible(periodEvaluator.isSelectionPeriod());
			selectDropdown.setVisible(periodEvaluator.isSelectionPeriod());
			unselectButton.setVisible(false);
		}
		
	}	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (CMD_SELECT.equals(cmd)) {
				doSelectTopic(null);
			} else if (CMD_SELECT_FIRST.equals(cmd)) {
				doSelectTopic(Integer.valueOf(1));
			} else if (CMD_SELECT_LAST.equals(cmd)) {
				doSelectTopic(null);
			} else if (cmd.startsWith(CMD_SELECT_POS)) {
				int sortOrder = Integer.valueOf(cmd.substring(CMD_SELECT_POS.length()));
				doSelectTopic(sortOrder);
			} else if (CMD_UNSELECT.equals(cmd)) {
				doUnselectTopic();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doUnselectTopic() {
		periodEvaluator.refresh();
		if (periodEvaluator.isSelectionPeriod()) {
			topicBrokerService.unselect(getIdentity(), getIdentity(), topic);
		} else if (periodEvaluator.isWithdrawPeriod()) {
			topicBrokerService.withdraw(getIdentity(), getIdentity(), topic, false);
		}
		
		updateUI();
	}
	
	private void doSelectTopic(Integer sortOrder) {
		periodEvaluator.refresh();
		if (periodEvaluator.isSelectionPeriod()) {
			topicBrokerService.select(getIdentity(), getIdentity(), topic, sortOrder);
		}
		
		updateUI();
	}

	public void setBroker(TBBroker broker) {
		this.broker = broker;
		this.periodEvaluator.setBroker(broker);
		periodEvaluator.refresh();
		updateUI();
		flc.setDirty(true);
	}

}
