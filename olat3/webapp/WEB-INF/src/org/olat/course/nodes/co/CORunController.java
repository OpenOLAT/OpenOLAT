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
* <p>
*/ 

package org.olat.course.nodes.co;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
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
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
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
	private UserCourseEnvironment userCourseEnv;

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
		this.userCourseEnv = userCourseEnv;
		//set translator with fall back translator.
		Translator fallback = Util.createPackageTranslator(ContactFormController.class, ureq.getLocale());
		setTranslator(Util.createPackageTranslator(CORunController.class, ureq.getLocale(), fallback));
		
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

		boolean valid = false; // true if at least one email adress
		Stack<ContactList> contactLists = new Stack<ContactList>();
		Boolean partipsConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS);
		if (partipsConfigured != null && partipsConfigured.booleanValue()) {
			ContactList participantsEmailList = retrieveParticipants();
			if (participantsEmailList != null && participantsEmailList.getEmailsAsStrings().size()>0) {
				contactLists.push(participantsEmailList);
				valid = true;
			}
		}
		Boolean coachesConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES);
		if (coachesConfigured!=null && coachesConfigured.booleanValue()) {
			ContactList coachesEmailList = retrieveCoaches();
			if (coachesEmailList != null && coachesEmailList.getEmailsAsStrings().size()>0){
				contactLists.push(coachesEmailList);
				valid = true;
			}
		}
		String groups = (String)moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
		if (groups != null && !groups.equals("")) {
			ContactList[] groupsCL = retrieveGroups(groups);
			if (groupsCL.length > 0){
				for (int i = 0; i < groupsCL.length; i++) {
					if(groupsCL[i].getEmailsAsStrings().size() > 0){
						contactLists.push(groupsCL[i]);
						valid=true;
					}
				}
			}
		}
		String areas = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
		if (areas != null && !areas.equals("")) {
			ContactList[] areasCL = retrieveAreas(areas);
			if (areasCL.length > 0){
				for (int i = 0; i < areasCL.length; i++) {
					if(areasCL[i].getEmailsAsStrings().size() > 0){
						contactLists.push(areasCL[i]);
						valid = true;
					}
				}
			}
		}

		// Adding contact form
		if (emailListConfig != null) {
			ContactList emailList = new ContactList(translate("recipients"));
			for (Iterator<String> iter = emailListConfig.iterator(); iter.hasNext();) {
				String email = iter.next();
				emailList.add(email);
			}
			contactLists.push(emailList);
			valid = true;
		}

		if (valid){ // at least one email adress
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			
			while (!contactLists.empty()) {
				ContactList cl = contactLists.pop();
				cmsg.addEmailTo(cl);
			}
			cmsg.setBodyText(mBody);
			cmsg.setSubject(mSubject);
			coFoCtr = new ContactFormController(ureq, getWindowControl(), true,false,false,false, cmsg);
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

	private ContactList retrieveCoaches() {
		CourseGroupManager cgm = this.userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		List<Identity> coaches = cgm.getCoachesFromLearningGroup(null);
		Set<Identity> coachesWithoutDuplicates = new HashSet<Identity>(coaches);
		coaches = new ArrayList<Identity>(coachesWithoutDuplicates);
		ContactList cl = new ContactList(translate("form.message.chckbx.coaches"));
		cl.addAllIdentites(coaches);
		return cl;
	}

	private ContactList retrieveParticipants() {
		CourseGroupManager cgm = this.userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		List<Identity> participiants = cgm.getParticipantsFromLearningGroup(null);
		//FIXME:pb:c docu getParticipantsFromLearningGroup: really duplicates?
		Set<Identity> participantsWithoutDuplicates = new HashSet<Identity>(participiants);
		
		participiants = new ArrayList<Identity>(participantsWithoutDuplicates);
		ContactList cl = new ContactList(translate("form.message.chckbx.partips"));
		cl.addAllIdentites(participiants);
		return cl;

	}

	private ContactList[] retrieveGroups(String csvGroups) {
		CourseGroupManager cgm = this.userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		List<String> groupNames = splitNames(csvGroups);
		List<ContactList> groupsCL = new ArrayList<ContactList>();
		/*
		 * for each group name in all the course's group contexts get the
		 * participants groups. From the resulting groups take all participants and
		 * add the identities to the ContactList named like the group.
		 */
		Iterator<String> iterator = groupNames.iterator();
		while (iterator.hasNext()) {
			// fetch all participants and owners by getting all participants and
			// owners of all groups
			String groupName = iterator.next();
			List<BusinessGroup> mygroups = cgm.getLearningGroupsFromAllContexts(groupName);
			// create a ContactList with the name of the group
			ContactList tmp = new ContactList(groupName);
			for (int i = 0; i < mygroups.size(); i++) {
				BusinessGroup bg = mygroups.get(i);
				List<Identity> ids = secManager.getIdentitiesOfSecurityGroup(bg.getPartipiciantGroup());
				ids.addAll(secManager.getIdentitiesOfSecurityGroup(bg.getOwnerGroup()));
				// add all identities to the ContactList
				tmp.addAllIdentites(ids);
			}
			// add the ContactList
			groupsCL.add(tmp);
		}
		// remove duplicates and convert List -> to Array.
		Set<ContactList> groupsCLWithouthDups = new HashSet<ContactList>(groupsCL);
		ContactList[] retVal = new ContactList[groupsCLWithouthDups.size()];
		retVal = groupsCLWithouthDups.toArray(retVal);
		return retVal;
	}

	private ContactList[] retrieveAreas(String csvAreas) {
		CourseGroupManager cgm = this.userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		List<String> areaNames = splitNames(csvAreas);
		List<ContactList> groupsCL = new ArrayList<ContactList>();
		/*
		 * for each area name in all the course's group contexts get the
		 * participants groups. From the resulting groups take all participants and
		 * add the identities to the ContactList named like the group.
		 */
		Iterator<String> iterator = areaNames.iterator();
		while (iterator.hasNext()) {
			// fetch all participants and owners by getting all participants and
			// owners of all groups
			String areaName = iterator.next();
			List<BusinessGroup> mygroups = cgm.getLearningGroupsInAreaFromAllContexts(areaName);
			// create a ContactList with the name of the group
			ContactList tmp = new ContactList(areaName);
			for (int i = 0; i < mygroups.size(); i++) {
				BusinessGroup bg = mygroups.get(i);
				List<Identity> ids = secManager.getIdentitiesOfSecurityGroup(bg.getPartipiciantGroup());
				ids.addAll(secManager.getIdentitiesOfSecurityGroup(bg.getOwnerGroup()));
				// add all identities to the ContactList
				tmp.addAllIdentites(ids);
			}
			// add the ContactList
			groupsCL.add(tmp);
		}
		// remove duplicates and convert List -> to Array.
		Set<ContactList> groupsCLWithouthDups = new HashSet<ContactList>(groupsCL);
		ContactList[] retVal = new ContactList[groupsCLWithouthDups.size()];
		retVal = groupsCLWithouthDups.toArray(retVal);
		return retVal;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 * @see org.olat.core.gui.components.Component, @see org.olat.core.gui.control.Event)
	 */
	@SuppressWarnings("unused")
	public void event(UserRequest ureq, Component source, Event event) {
	// no components to listen to
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

	private List<String> splitNames(String namesList) {
		List<String> names = new ArrayList<String>();
		if (namesList != null) {
			String[] name = namesList.split(",");
			for (int i = 0; i < name.length; i++) {
				names.add(name[i].trim());
			}
		}
		return names;
	}

}
