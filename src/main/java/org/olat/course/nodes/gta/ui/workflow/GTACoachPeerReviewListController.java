/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.workflow;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.ui.peerreview.GTACoachPeerReviewAwardedListController;
import org.olat.course.nodes.gta.ui.peerreview.GTACoachPeerReviewReceivedListController;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 28 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachPeerReviewListController extends AbstractWorkflowListController {
	
	private final BreadcrumbPanel stackPanel;

	private Boolean awardedOpen = Boolean.TRUE;
	private Boolean receivedOpen = Boolean.TRUE;
	
	private GTACoachPeerReviewAwardedListController peerReviewAwardedListCtrl;
	private GTACoachPeerReviewReceivedListController peerReviewReceivedListCtrl;
	
	public GTACoachPeerReviewListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			UserCourseEnvironment coachCourseEnv, List<Identity> identities, GTACourseNode gtaNode) {
		super(ureq, wControl, "peerreview_list", coachCourseEnv, identities, gtaNode);
		this.stackPanel = stackPanel;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		InfoPanelItem panel = uifactory.addInfoPanel("configuration", null, formLayout);
		panel.setTitle(translate("workflow.infos.configuration"));
		initConfigurationInfos(panel);
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("awardedOpen", awardedOpen);
			layoutCont.contextPut("receivedOpen", receivedOpen);
		}
		
		TaskList taskList = gtaManager.getTaskList(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode);
		peerReviewReceivedListCtrl = new GTACoachPeerReviewReceivedListController(ureq, getWindowControl(),
				stackPanel, taskList, assessedIdentities,
				courseEnv, gtaNode, mainForm);
		listenTo(peerReviewReceivedListCtrl);
		formLayout.add("received", peerReviewReceivedListCtrl.getInitialFormItem());
		
		peerReviewAwardedListCtrl = new GTACoachPeerReviewAwardedListController(ureq, getWindowControl(),
				taskList, assessedIdentities, courseEnv, gtaNode, mainForm);
		listenTo(peerReviewAwardedListCtrl);
		formLayout.add("awarded", peerReviewAwardedListCtrl.getInitialFormItem());
	}
	
	protected void initConfigurationInfos(InfoPanelItem panel) {
		StringBuilder infos = new StringBuilder();
		
		DueDateConfig dueDateConfig = gtaNode.getDueDateConfig(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE);
		if(dueDateConfig != DueDateConfig.noDueDateConfig()) {
			String dueDateVal = dueDateConfigToString(dueDateConfig);
			if(StringHelper.containsNonWhitespace(dueDateVal)) {
				String deadlineInfos = translate("workflow.deadline.peerreview.period", dueDateVal);
				infos.append("<p><i class='o_icon o_icon-fw o_icon_timelimit'> </i> ").append(deadlineInfos).append("</p>");
			}
		}
		
		String numOfReviews = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS_DEFAULT);
		String numOfReviewsInfos = translate("workflow.infos.num.reviews", numOfReviews);
		infos.append("<p><i class='o_icon o_icon-fw o_icon_group'> </i> ").append(numOfReviewsInfos).append("</p>");
		
		String assignmentInfos = "";
		String assignment = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT);
		if(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_SAME_TASK.equals(assignment)) {
			assignmentInfos = translate("peer.review.assignment.same.task");
		} else if(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_OTHER_TASK.equals(assignment)) {
			assignmentInfos = translate("peer.review.assignment.other.task");
		} else if(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_RANDOM.equals(assignment)) {
			assignmentInfos = translate("peer.review.assignment.random");
		}
		
		String formReviewInfos = "";
		String formReview = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW);
		if(GTACourseNode.GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW.equals(formReview)) {
			formReviewInfos = translate("peer.review.double.blinded.review");
		} else if(GTACourseNode.GTASK_PEER_REVIEW_SINGLE_BLINDED_REVIEW.equals(formReview)) {
			formReviewInfos = translate("peer.review.single.blinded.review");
		} else if(GTACourseNode.GTASK_PEER_REVIEW_OPEN_REVIEW.equals(formReview)) {
			formReviewInfos = translate("peer.review.open.review");
		}
		if(StringHelper.containsNonWhitespace(assignmentInfos) && StringHelper.containsNonWhitespace(formReviewInfos)) {
			String text = translate("workflow.infos.asssignment.type", assignmentInfos, formReviewInfos);
			infos.append("<p><i class='o_icon o_icon-fw o_icon_info_list'> </i> ").append(text).append("</p>");
		}
		
		panel.setInformations(infos.toString());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String receivedOpenVal = ureq.getParameter("receivedOpen");
			if (StringHelper.containsNonWhitespace(receivedOpenVal)) {
				receivedOpen = Boolean.valueOf(receivedOpenVal);
				flc.contextPut("receivedOpen", receivedOpen);
			}
			String awardedOpenVal = ureq.getParameter("awardedOpen");
			if (StringHelper.containsNonWhitespace(awardedOpenVal)) {
				awardedOpen = Boolean.valueOf(awardedOpenVal);
				flc.contextPut("awardedOpen", awardedOpen);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
