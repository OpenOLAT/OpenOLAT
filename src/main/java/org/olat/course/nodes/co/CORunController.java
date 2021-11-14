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

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.core.util.mail.MailContent;
import org.olat.core.util.mail.MailManager;
import org.olat.course.assessment.AssessmentEvents;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.members.ui.group.MembersSelectorFormFragment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR/> Run controller for the contact form building block <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author gnaegi
 * @author Dirk Furrer
 */
public class CORunController extends BasicController {

	private ContactFormController coFoCtr;

	private final COCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;

	private final CourseGroupManager cgm;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	/**
	 * Constructor for the contact form run controller
	 * 
	 * @param moduleConfiguration
	 * @param ureq
	 * @param wControl
	 * @param coCourseNode
	 */
	public CORunController(COCourseNode courseNode, UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		
		ModuleConfiguration moduleConfiguration = courseNode.getModuleConfiguration();
		

		//set translator with fall back translator.
		Translator fallback = Util.createPackageTranslator(ContactFormController.class, ureq.getLocale());
		setTranslator(Util.createPackageTranslator(CORunController.class, ureq.getLocale(), fallback));
		setTranslator(Util.createPackageTranslator(MembersSelectorFormFragment.class, ureq.getLocale(), getTranslator()));
		
		List<String> emailListConfig = moduleConfiguration.getList(COEditController.CONFIG_KEY_EMAILTOADRESSES, String.class);
		String mSubject = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT);
		String mBody = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_MBODY_DEFAULT);

		// Adding learning objectives using a consumable panel. Will only be
		// displayed on the first page
		Boolean participantsCourseConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE);
		Boolean participantsAllConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_ALL);
		Boolean coachesCourseConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE);
		Boolean coachesAssignedConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_ASSIGNED);
		Boolean coachesAllConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL);
		Boolean ownersConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOOWNERS);
		
		Stack<ContactList> contactLists = new Stack<>();
		String participantGroupNames = (String)moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP);
		List<Long> participantGroupKeys = moduleConfiguration.getList(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID, Long.class);
		if((participantGroupKeys == null || participantGroupKeys.isEmpty())  && StringHelper.containsNonWhitespace(participantGroupNames)) {
			participantGroupKeys = businessGroupService.toGroupKeys(participantGroupNames, cgm.getCourseEntry());
		}
		
		String participantAreaNames = (String)moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA);
		List<Long> participantAreaKeys = moduleConfiguration.getList(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA_ID, Long.class);
		if((participantAreaKeys == null || participantAreaKeys.isEmpty()) && StringHelper.containsNonWhitespace(participantAreaNames)) {
			participantAreaKeys = businessGroupService.toGroupKeys(participantAreaNames, cgm.getCourseEntry());
		}
		
		String coachGroupNames = (String)moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP);
		List<Long> coachGroupKeys = moduleConfiguration.getList(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID, Long.class);
		if((coachGroupKeys == null || coachGroupKeys.isEmpty()) && StringHelper.containsNonWhitespace(coachGroupNames)) {
			coachGroupKeys = businessGroupService.toGroupKeys(coachGroupNames, cgm.getCourseEntry());
		}
		
		String coachAreaNames = (String)moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA);
		List<Long> coachAreaKeys = moduleConfiguration.getList(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS, Long.class);
		if((coachAreaKeys == null || coachAreaKeys.isEmpty()) && StringHelper.containsNonWhitespace(coachAreaNames)) {
			coachAreaKeys = businessGroupService.toGroupKeys(coachAreaNames, cgm.getCourseEntry());
		}
		
		
		if(coachAreaNames != null){
			ContactList cl = retrieveCoachesFromAreas(coachAreaKeys);
			contactLists.push(cl);
		}
		if(coachGroupNames != null){
			ContactList cl = retrieveCoachesFromGroups(coachGroupKeys);
			contactLists.push(cl);
		}
		if(participantGroupNames != null){
			ContactList cl = retrieveParticipantsFromGroups(participantGroupKeys);
			contactLists.push(cl);
		}
		if(participantAreaNames != null){
			ContactList cl = retrieveParticipantsFromAreas(participantAreaKeys);
			contactLists.push(cl);
		}
		
		if (coachesAllConfigured != null && coachesAllConfigured.booleanValue()) {
			ContactList cl = retrieveCoachesFromCourse();
			contactLists.push(cl);
			List<BusinessGroup> groups = cgm.getAllBusinessGroups();
			List<Long> groupKeys = new ArrayList<>();
			for(BusinessGroup group:groups){
				groupKeys.add(group.getKey());
			}
			cl = retrieveCoachesFromGroups(groupKeys);
			contactLists.push(cl);
			cl = retrieveCoachesFromAreas(groupKeys);
			contactLists.push(cl);
		} else if (coachesCourseConfigured != null && coachesCourseConfigured.booleanValue()){
			ContactList cl = retrieveCoachesFromCourse();
			contactLists.push(cl);
		} else if (coachesAssignedConfigured != null && coachesAssignedConfigured.booleanValue()){
			ContactList cl = retrieveAssignedCoaches();
			contactLists.push(cl);
		}
		
		if (participantsAllConfigured != null && participantsAllConfigured.booleanValue()) {
			ContactList cl = retrieveParticipantsFromCourse();
			contactLists.push(cl);
			List<BusinessGroup> groups = cgm.getAllBusinessGroups();
			List<Long> groupKeys = new ArrayList<>();
			for(BusinessGroup group:groups){
				groupKeys.add(group.getKey());
			}
			cl = retrieveParticipantsFromGroups(groupKeys);
			contactLists.push(cl);
			cl = retrieveParticipantsFromAreas(groupKeys);
			contactLists.push(cl);
		} else if (participantsCourseConfigured != null && participantsCourseConfigured.booleanValue()){
			ContactList cl = retrieveParticipantsFromCourse();
			contactLists.push(cl);
		}
		
		if (ownersConfigured != null && ownersConfigured){
			ContactList cl = retrieveOwnersFromCourse();
			contactLists.push(cl);
		}

		String areaNames = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA);
		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS);
		if(areaKeys == null && StringHelper.containsNonWhitespace(areaNames)) {
			areaKeys = areaManager.toAreaKeys(areaNames, cgm.getCourseResource());
		}
		if (coachesAllConfigured != null && coachesAllConfigured.booleanValue()) {
			ContactList cl = retrieveCoachesFromAreas(areaKeys);
			contactLists.push(cl);
		}
		if (participantsAllConfigured != null && participantsAllConfigured.booleanValue()) {
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

		if (!contactLists.isEmpty()) { 
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			while (!contactLists.empty()) {
				ContactList cl = contactLists.pop();
				cmsg.addEmailTo(cl);	
			}
			
			RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			CourseMailTemplate template = new CourseMailTemplate(entry, getIdentity(), getLocale());
			template.setBodyTemplate(mBody);
			template.setSubjectTemplate(mSubject);
			MailContent content = mailManager.evaluateTemplate(template);
			template.setSubjectTemplate(content.getSubject());
			template.setBodyTemplate(content.getBody());
			coFoCtr = new ContactFormController(ureq, getWindowControl(), false, false, false, cmsg, template);
			listenTo(coFoCtr);//dispose as this controller is disposed
			putInitialPanel(coFoCtr.getInitialComponent());
		} else { // no email adresses at all
			String message = translate("error.msg.send.no.rcps");
			Controller mCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, message);
			listenTo(mCtr);//to be disposed as this controller gets disposed
			putInitialPanel(mCtr.getInitialComponent());
		}
	}
	
	
	private ContactList retrieveCoachesFromGroups(List<Long> groupKeys) {
		List<Identity> coaches = new ArrayList<>(new HashSet<>(cgm.getCoachesFromBusinessGroups(groupKeys)));
		ContactList cl = new ContactList(translate("form.message.chckbx.coaches"));
		cl.addAllIdentites(coaches);
		return cl;
	}
	
	private ContactList retrieveCoachesFromAreas(List<Long> areaKeys) {
		List<Identity> coaches = cgm.getCoachesFromAreas(areaKeys);
		Set<Identity> coachesWithoutDuplicates = new HashSet<>(coaches);
		coaches = new ArrayList<>(coachesWithoutDuplicates);
		ContactList cl = new ContactList(translate("form.message.chckbx.coaches"));
		cl.addAllIdentites(coaches);
		return cl;
	}
	
	private ContactList retrieveCoachesFromCourse() {
		List<Identity> coaches = cgm.getCoaches();
		List<Identity> curriculumCoaches = cgm.getCoachesFromCurriculumElements();
		ContactList cl = new ContactList(translate("form.message.chckbx.coaches"));
		cl.addAllIdentites(coaches);
		cl.addAllIdentites(curriculumCoaches);
		return cl;
	}

	private ContactList retrieveAssignedCoaches() {
		List<Identity> coaches = repositoryService.getAssignedCoaches(getIdentity(), cgm.getCourseEntry());
		
		// Fallback: Course coaches
		if (coaches.isEmpty()) {
			coaches = repositoryService.getMembers(cgm.getCourseEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		}
		// Fallback: Course owners
		if (coaches.isEmpty()) {
			coaches = repositoryService.getMembers(cgm.getCourseEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		}
		
		ContactList cl = new ContactList(translate("form.message.coaches.assigned"));
		cl.addAllIdentites(coaches);
		return cl;
	}

	private ContactList retrieveParticipantsFromGroups(List<Long> groupKeys) {
		List<Identity> participiants = cgm.getParticipantsFromBusinessGroups(groupKeys);
		ContactList cl = new ContactList(translate("form.message.chckbx.partips"));
		cl.addAllIdentites(participiants);
		return cl;
	}
	
	private ContactList retrieveParticipantsFromCourse() {
		List<Identity> participiants = cgm.getParticipants();
		List<Identity> curriculumParticipants = cgm.getParticipantsFromCurriculumElements();
		ContactList cl = new ContactList(translate("form.message.chckbx.partips"));
		cl.addAllIdentites(participiants);
		cl.addAllIdentites(curriculumParticipants);
		return cl;
	}
	
	private ContactList retrieveParticipantsFromAreas(List<Long> areaKeys) {
		List<Identity> participiants = cgm.getParticipantsFromAreas(areaKeys);
		ContactList cl = new ContactList(translate("form.message.chckbx.partips"));
		cl.addAllIdentites(participiants);
		return cl;
	}
	
	private ContactList retrieveOwnersFromCourse(){
		List<Identity> ownerList = repositoryService.getMembers(cgm.getCourseEntry(), RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());
		ContactList cl = new ContactList(translate("form.message.chckbx.owners"));
		cl.addAllIdentites(ownerList);
		return cl;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == coFoCtr) {
			doUpdateAssessmentStatus(ureq);
		}
		super.event(ureq, source, event);
	}


	private void doUpdateAssessmentStatus(UserRequest ureq) {
		if (!userCourseEnv.isCourseReadOnly() && userCourseEnv.isParticipant()) {
			AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
			if (!AssessmentEntryStatus.done.equals(assessmentEvaluation.getAssessmentStatus())) {
				ScoreEvaluation scoreEval = new ScoreEvaluation(null, null, AssessmentEntryStatus.done, null, null, null, null, null);
				courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, userCourseEnv, null, false, Role.user);
				fireEvent(ureq, AssessmentEvents.CHANGED_EVENT);
			}
		}
	}


	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
}