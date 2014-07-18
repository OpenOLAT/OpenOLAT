/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.co;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.co.ContactFormController;

/**
 * Description:<BR/> Run controller for the contact form building block <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author gnaegi
 */
public class CORunController extends BasicController {

	private VelocityContainer myContent;
	private ContactFormController coFoCtr;
	private final CourseGroupManager cgm;
	
	private final BGAreaManager areaManager;
	private final BusinessGroupService businessGroupService;

	/**
	 * Constructor for the contact form run controller
	 * 
	 * @param moduleConfiguration
	 * @param ureq
	 * @param wControl
	 * @param coCourseNode
	 */
	public CORunController(ModuleConfiguration moduleConfiguration, UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, COCourseNode coCourseNode) {
		super(ureq, wControl);
		cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		//set translator with fall back translator.
		Translator fallback = Util.createPackageTranslator(ContactFormController.class, ureq.getLocale());
		setTranslator(Util.createPackageTranslator(CORunController.class, ureq.getLocale(), fallback));
		
		@SuppressWarnings("unchecked")
		List<String> emailListConfig = (List<String>) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
		String mSubject = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT);
		String mBody = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_MBODY_DEFAULT);

		myContent = createVelocityContainer("run");

		myContent.contextPut("menuTitle", coCourseNode.getShortTitle());
		myContent.contextPut("displayTitle", coCourseNode.getLongTitle());

		// Adding learning objectives using a consumable panel. Will only be
		// displayed on the first page
		String learningObj = coCourseNode.getLearningObjectives();
		Panel panel = new Panel("panel");
		myContent.put("learningObjectives", panel);
		if (learningObj != null) {
			Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
			panel.setContent(learningObjectives);
		}

		Boolean partipsConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS);
		Boolean coachesConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES);

		Stack<ContactList> contactLists = new Stack<ContactList>();

		String grpNames = (String)moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
		@SuppressWarnings("unchecked")
		List<Long> groupKeys = (List<Long>) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS);
		if(groupKeys == null && StringHelper.containsNonWhitespace(grpNames)) {
			groupKeys = businessGroupService.toGroupKeys(grpNames, cgm.getCourseEntry());
		}
		if (coachesConfigured != null && coachesConfigured.booleanValue()) {
			ContactList cl = retrieveCoachesFromGroups(groupKeys);
			contactLists.push(cl);
		}
		if (partipsConfigured != null && partipsConfigured.booleanValue()) {
			ContactList cl = retrieveParticipantsFromGroups(groupKeys);
			contactLists.push(cl);
		} 

		String areaNames = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOAREA_IDS);
		if(areaKeys == null && StringHelper.containsNonWhitespace(areaNames)) {
			areaKeys = areaManager.toAreaKeys(areaNames, cgm.getCourseResource());
		}
		if (coachesConfigured != null && coachesConfigured.booleanValue()) {
			ContactList cl = retrieveCoachesFromAreas(areaKeys);
			contactLists.push(cl);
		}
		if (partipsConfigured != null && partipsConfigured.booleanValue()) {
			ContactList cl = retrieveParticipantsFromAreas(areaKeys);
			contactLists.push(cl);
		}

		// Adding contact form
		if (emailListConfig != null) {
			ContactList emailList = new ContactList(translate("recipients"));
			for (Iterator<String> iter = emailListConfig.iterator(); iter.hasNext();) {
				String email = iter.next();
				emailList.add(email);
			}
			contactLists.push(emailList);
		}

		if (contactLists.size() > 0) { 
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			
			while (!contactLists.empty()) {
				ContactList cl = contactLists.pop();
				cmsg.addEmailTo(cl);	
			}
			
			cmsg.setBodyText(mBody);
			cmsg.setSubject(mSubject);
			coFoCtr = new ContactFormController(ureq, getWindowControl(), false, false, false, cmsg);
			listenTo(coFoCtr);//dispose as this controller is disposed
			myContent.put("myCoForm", coFoCtr.getInitialComponent());
			putInitialPanel(myContent);
		} else { // no email adresses at all
			String message = translate("error.msg.send.no.rcps");
			Controller mCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, message);
			listenTo(mCtr);//to be disposed as this controller gets disposed
			putInitialPanel(mCtr.getInitialComponent());
		}
	}
	
	private ContactList retrieveCoachesFromGroups(List<Long> groupKeys) {
		List<Identity> coaches = cgm.getCoachesFromBusinessGroups(groupKeys);
		Set<Identity> coachesWithoutDuplicates = new HashSet<Identity>(coaches);
		coaches = new ArrayList<Identity>(coachesWithoutDuplicates);
		ContactList cl = new ContactList(translate("form.message.chckbx.coaches"));
		cl.addAllIdentites(coaches);
		return cl;
	}
	
	private ContactList retrieveCoachesFromAreas(List<Long> areaKeys) {
		List<Identity> coaches = cgm.getCoachesFromAreas(areaKeys);
		Set<Identity> coachesWithoutDuplicates = new HashSet<Identity>(coaches);
		coaches = new ArrayList<Identity>(coachesWithoutDuplicates);
		ContactList cl = new ContactList(translate("form.message.chckbx.coaches"));
		cl.addAllIdentites(coaches);
		return cl;
	}

	private ContactList retrieveParticipantsFromGroups(List<Long> groupKeys) {
		List<Identity> participiants = cgm.getParticipantsFromBusinessGroups(groupKeys);
		ContactList cl = new ContactList(translate("form.message.chckbx.partips"));
		cl.addAllIdentites(participiants);
		return cl;
	}
	
	private ContactList retrieveParticipantsFromAreas(List<Long> areaKeys) {
		List<Identity> participiants = cgm.getParticipantsFromAreas(areaKeys);
		ContactList cl = new ContactList(translate("form.message.chckbx.partips"));
		cl.addAllIdentites(participiants);
		return cl;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 * @see org.olat.core.gui.components.Component, @see org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	// no components to listen to
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}