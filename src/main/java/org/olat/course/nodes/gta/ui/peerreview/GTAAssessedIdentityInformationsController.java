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
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.List;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.ui.GTAParticipantController;
import org.olat.course.nodes.gta.ui.component.TaskReviewAssignmentStatusCellRenderer;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAAssessedIdentityInformationsController extends FormBasicController {
	
	private final Identity user;
	private final boolean anonym;
	private final GTACourseNode gtaNode;
	private final String placeholderName;
	private final MapperKey avatarMapperKey;
	private final TaskReviewAssignmentStatus assignmentStatus;
	
	@Autowired
	private UserManager userManager;
	
	GTAAssessedIdentityInformationsController(UserRequest ureq, WindowControl wControl, MapperKey avatarMapperKey,
			TaskReviewAssignmentStatus assignmentStatus, GTACourseNode gtaNode, Identity user, String placeholderName, boolean anonym) {
		super(ureq, wControl, "assessed_identity_informations", Util.createPackageTranslator(GTAParticipantController.class, ureq.getLocale()));
		this.user = user;
		this.anonym = anonym;
		this.gtaNode = gtaNode;
		this.placeholderName = placeholderName;
		this.avatarMapperKey = avatarMapperKey;
		this.assignmentStatus = assignmentStatus;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String fullName = anonym ? placeholderName : userManager.getUserDisplayName(user);	
			layoutCont.contextPut("fullName", fullName);
			
			// Status
			String status = new TaskReviewAssignmentStatusCellRenderer(getLocale(), false).render(assignmentStatus);
			layoutCont.contextPut("status", status);
			
			// Portrait
			Identity userToPortrait = anonym ? new TransientIdentity() : user;
			List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(List.of(userToPortrait));
			UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_id", flc.getFormItemComponent(), null, avatarMapperKey);
			usersPortraitCmp.setAriaLabel(translate("member.list.aria"));
			usersPortraitCmp.setSize(PortraitSize.large);
			usersPortraitCmp.setMaxUsersVisible(5);
			usersPortraitCmp.setUsers(portraitUsers);
			layoutCont.put("portraits", usersPortraitCmp);
			
			// Form of the review
			String formReview = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW,
					GTACourseNode.GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW);
			if(GTACourseNode.GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW.equals(formReview)) {
				layoutCont.contextPut("reviewForm", translate("review.form.desc.double.blinded"));
			} else if(GTACourseNode.GTASK_PEER_REVIEW_SINGLE_BLINDED_REVIEW.equals(formReview)) {
				layoutCont.contextPut("reviewForm", translate("review.form.desc.single.blinded"));
			} else if(GTACourseNode.GTASK_PEER_REVIEW_OPEN_REVIEW.equals(formReview)) {
				layoutCont.contextPut("reviewForm", translate("review.form.desc.open"));
			}

			// Points for the reviewer
			String pointsProReview = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_PRO_REVIEW);
			if(StringHelper.containsNonWhitespace(pointsProReview)) {
				layoutCont.contextPut("reviewPoint", translate("review.form.desc.point"));
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
}
