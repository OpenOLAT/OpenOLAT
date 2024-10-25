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

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.ui.GTAParticipantController;
import org.olat.user.UserAvatarMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Only show the identity header with an empty documents folder.
 * 
 * Initial date: 25 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAEvaluationFormInProgressController extends BasicController {
	
	private Link backLink;
	private final VelocityContainer mainVC;
	
	private Task task;
	private final MapperKey avatarMapperKey;

	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private MapperService mapperService;
	
	public GTAEvaluationFormInProgressController(UserRequest ureq, WindowControl wControl, TaskReviewAssignment assignment,
			GTACourseNode gtaNode, GTAEvaluationFormExecutionOptions options) {
		super(ureq, wControl, Util.createPackageTranslator(GTAParticipantController.class, ureq.getLocale()));
		avatarMapperKey =  mapperService.register(ureq.getUserSession(), new UserAvatarMapper(true));

		task = gtaManager.getTask(assignment.getTask());

		mainVC = createVelocityContainer("evaluation_form_review");	
		if(options.withBackButton()) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
		}

		initNoDocuments(ureq, options.getPlaceHolderName(), options.isAnonym());

		if(options.withAssessedIdentityHeader()) {
			initAssessedIdentityHeader(ureq, assignment, gtaNode, options.getPlaceHolderName(), options.isAnonym());
		}
		if(options.withReviewerHeader()) {
			initReviewerHeader(ureq, assignment, options.getPlaceHolderName(), options.isAnonym());
		}
		putInitialPanel(mainVC);
	}
	
	private void initAssessedIdentityHeader(UserRequest ureq, TaskReviewAssignment assignment,
			GTACourseNode gtaNode, String placeholderName, boolean anonym) {
		Identity user = anonym ? null : task.getIdentity();
		GTAAssessedIdentityInformationsController userInfosCtrl = new GTAAssessedIdentityInformationsController(ureq, getWindowControl(),
				avatarMapperKey, assignment.getStatus(), gtaNode, user, placeholderName, anonym);
		listenTo(userInfosCtrl);
		mainVC.put("assessed.identity.infos", userInfosCtrl.getInitialComponent());
	}
	
	private void initReviewerHeader(UserRequest ureq, TaskReviewAssignment assignment, String placeholderName, boolean anonym) {
		Identity user = anonym ? null : assignment.getAssignee();
		GTAReviewerIdentityInformationsController userInfosCtrl = new GTAReviewerIdentityInformationsController(ureq, getWindowControl(),
				avatarMapperKey, user, placeholderName, anonym);
		listenTo(userInfosCtrl);
		mainVC.put("reviewer.infos", userInfosCtrl.getInitialComponent());
	}
	
	private void initNoDocuments(UserRequest ureq, String fullName, boolean anonym) {
		VFSContainer submitContainer = new VirtualContainer("Hello");
		GTADocumentsController documentsCtrl = new GTADocumentsController(ureq, getWindowControl(),
				avatarMapperKey, submitContainer, fullName, anonym);
		documentsCtrl.setEmptyMessage("empty.evaluation.docs.not.submitted");
		listenTo(documentsCtrl);
		mainVC.put("documents", documentsCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
}
