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

package org.olat.course.nodes.info;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.basesecurity.GroupRoles;
import org.olat.commons.info.InfoSubscriptionManager;
import org.olat.commons.info.manager.MailFormatter;
import org.olat.commons.info.ui.InfoDisplayController;
import org.olat.commons.info.ui.InfoSecurityCallback;
import org.olat.commons.info.ui.SendInfoMailFormatter;
import org.olat.commons.info.ui.SendMailCurriculumOption;
import org.olat.commons.info.ui.SendMailGroupOption;
import org.olat.commons.info.ui.SendSubscriberMailOption;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.nodes.InfoCourseNode;
import org.olat.course.run.GoToEvent;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Container for a InfodisplayController and the SubscriptionController
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoRunController extends BasicController {
	
	private InfoDisplayController infoDisplayController;
	private ContextualSubscriptionController subscriptionController;
	
	@Autowired
	private NodeRightService nodeRightService;
	@Autowired
	private InfoSubscriptionManager subscriptionManager;
	@Autowired
	private CurriculumService curriculumService;

	public InfoRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne, InfoCourseNode courseNode) {
		super(ureq, wControl);
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		String resSubPath = courseNode.getIdent();

		boolean canAdd;
		boolean canAdmin;
		if(userCourseEnv.isCourseReadOnly()) {
			canAdd = false;
			canAdmin = false;
		} else {
			canAdd = canEdit(courseNode, userCourseEnv, ne);
			canAdmin = canAdmin(courseNode, userCourseEnv, ne);
		}
		InfoSecurityCallback secCallback = new InfoCourseSecurityCallback(getIdentity(), canAdd, canAdmin);

		boolean autoSubscribe = InfoCourseNodeEditController.getAutoSubscribe(config);
		int maxResults = getConfigValue(config, InfoCourseNodeConfiguration.CONFIG_LENGTH, 10);
		int duration = getConfigValue(config, InfoCourseNodeConfiguration.CONFIG_DURATION, 90);
		
		initVC(ureq, userCourseEnv, resSubPath, secCallback, autoSubscribe, maxResults, duration);
	}
	
	private boolean canEdit(InfoCourseNode courseNode, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (userCourseEnv.isAdmin()) {
			return true;
		}
		
		if (courseNode.hasCustomPreConditions()) {
			return ne.isCapabilityAccessible(InfoCourseNode.EDIT_CONDITION_ID);
		}
		
		return nodeRightService.isGranted(courseNode.getModuleConfiguration(), userCourseEnv, InfoCourseNode.EDIT);
	}
	
	private boolean canAdmin(InfoCourseNode courseNode, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (userCourseEnv.isAdmin()) {
			return true;
		}
		
		if (courseNode.hasCustomPreConditions()) {
			return ne.isCapabilityAccessible(InfoCourseNode.ADMIN_CONDITION_ID);
		}
		
		return nodeRightService.isGranted(courseNode.getModuleConfiguration(), userCourseEnv, InfoCourseNode.ADMIN);
	}
	
	public InfoRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			String resSubPath, InfoSecurityCallback secCallback, boolean autoSubscribe) {
		super(ureq, wControl);
		initVC(ureq, userCourseEnv, resSubPath, secCallback, autoSubscribe, -1, -1);
	}

	private void initVC(UserRequest ureq, UserCourseEnvironment userCourseEnv,
			String resSubPath, InfoSecurityCallback secCallback, boolean autoSubscribe, int maxResults, int duration) {
		Long resId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		OLATResourceable infoResourceable = new InfoOLATResourceable(resId);
		String businessPath = normalizeBusinessPath(getWindowControl().getBusinessControl().getAsString());
		ICourse course = CourseFactory.loadCourse(resId);
		
		CourseGroupManager cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		RepositoryEntry courseEntry = cgm.getCourseEntry();
		String infoMailTitle = course.getCourseTitle();
		
		//manage opt-out subscription
		UserSession usess = ureq.getUserSession();
		if(!usess.getRoles().isGuestOnly()) {
			SubscriptionContext subContext = subscriptionManager.getInfoSubscriptionContext(infoResourceable, resSubPath);
			PublisherData pdata = subscriptionManager.getInfoPublisherData(infoResourceable, businessPath);
			subscriptionController = new ContextualSubscriptionController(ureq, getWindowControl(), subContext, pdata, autoSubscribe);
			listenTo(subscriptionController);
		}	
		
		infoDisplayController = new InfoDisplayController(ureq, getWindowControl(), maxResults, duration, secCallback, infoResourceable, resSubPath, businessPath);
		infoDisplayController.addSendMailOptions(new SendSubscriberMailOption(infoResourceable, resSubPath, getLocale()));
		infoDisplayController.addSendMailOptions(new SendMembersMailOption(courseEntry, GroupRoles.owner, translate("wizard.step1.send_option.owner")));
		infoDisplayController.addSendMailOptions(new SendMembersMailOption(courseEntry, GroupRoles.coach, translate("wizard.step1.send_option.coach")));
		infoDisplayController.addSendMailOptions(new SendMembersMailOption(courseEntry, GroupRoles.participant, translate("wizard.step1.send_option.participant")));
		
		List<GroupRoles> groupRolesToSend = new ArrayList<>();
		groupRolesToSend.add(GroupRoles.participant);
		groupRolesToSend.add(GroupRoles.coach);
		groupRolesToSend.add(GroupRoles.owner);
		
		for (BusinessGroup group : cgm.getAllBusinessGroups()) {
			infoDisplayController.addGroupMailOption(new SendMailGroupOption(group, groupRolesToSend));
		}
		
		List<CurriculumRoles> curriculumRolesToSend = new ArrayList<>();
		curriculumRolesToSend.add(CurriculumRoles.participant);
		curriculumRolesToSend.add(CurriculumRoles.coach);
		curriculumRolesToSend.add(CurriculumRoles.owner);
		
		for (CurriculumElement curriculumElement : curriculumService.getCurriculumElements(courseEntry)) {
			infoDisplayController.addCurriuclaMailOptions(new SendMailCurriculumOption(curriculumElement, curriculumRolesToSend));
		}

		MailFormatter mailFormatter = new SendInfoMailFormatter(infoMailTitle, businessPath, getTranslator());
		infoDisplayController.setSendMailFormatter(mailFormatter);
		listenTo(infoDisplayController);

		VelocityContainer runVc = createVelocityContainer("run");
		if(subscriptionController != null) {
			runVc.put("infoSubscription", subscriptionController.getInitialComponent());
		}
		runVc.put("displayInfos", infoDisplayController.getInitialComponent());
		
		putInitialPanel(runVc);
	}
	
	private int getConfigValue(ModuleConfiguration config, String key, int def) {
		String durationStr = (String)config.get(key);
		if("\u221E".equals(durationStr)) {
			return -1;
		} else if(StringHelper.containsNonWhitespace(durationStr)) {
			try {
				return Integer.parseInt(durationStr);
			} catch(NumberFormatException e) { /* fallback to default */ }
		}
		return def;
	}
	
	/**
	 * Remove ROOT, remove identity context entry or duplicate, 
	 * @param url
	 * @return
	 */
	private String normalizeBusinessPath(String url) {
		if (url == null) return null;
		if (url.startsWith("ROOT")) {
			url = url.substring(4, url.length());
		}
		List<String> tokens = new ArrayList<>();
		for(StringTokenizer tokenizer = new StringTokenizer(url, "[]"); tokenizer.hasMoreTokens(); ) {
			String token = tokenizer.nextToken();
			if(token.startsWith("Identity")) {
				//The portlet "My courses" add an Identity context entry to the business path
				//ignore it
				continue;
			}
			if(!tokens.contains(token)) {
				tokens.add(token);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(String token:tokens) {
			sb.append('[').append(token).append(']');
		}
		return sb.toString();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == infoDisplayController) {
			if(event instanceof GoToEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	private class InfoOLATResourceable implements OLATResourceable {
		private final Long resId;
		
		public InfoOLATResourceable(Long resId) {
			this.resId = resId;
		}
		
		@Override
		public String getResourceableTypeName() {
			return OresHelper.calculateTypeName(CourseModule.class);
		}

		@Override
		public Long getResourceableId() {
			return resId;
		}
	}
}
