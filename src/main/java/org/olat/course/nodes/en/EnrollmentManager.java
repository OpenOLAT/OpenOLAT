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

package org.olat.course.nodes.en;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGMailHelper;
import org.olat.properties.Property;
import org.olat.testutils.codepoints.server.Codepoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<BR>
 * Business-logic of enrollemnt
 * <p>
 * Handle enroll and cancel-enrollment
 * <P>
 * Initial Date: Nov 11, 2006
 * 
 * @author Christian Guretzki
 */
@Service("enrollmentManager")
public class EnrollmentManager extends BasicManager {

	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;


	public EnrollStatus doEnroll(final Identity identity, final BusinessGroup group, final ENCourseNode enNode, final CoursePropertyManager coursePropertyManager,
			final WindowControl wControl, final Translator trans, List<Long> groupKeys, List<Long> areaKeys, CourseGroupManager cgm) {
		
		final EnrollStatus enrollStatus = new EnrollStatus();
		if (isLogDebugEnabled()) logDebug("doEnroll");
		// check if the user is already enrolled (user can be enrooled only in one group)
		if ( ( getBusinessGroupWhereEnrolled( identity, groupKeys, areaKeys, cgm) == null)
			  && ( getBusinessGroupWhereInWaitingList( identity, groupKeys, areaKeys, cgm) == null) ) {
			if (isLogDebugEnabled()) logDebug("Identity is not enrolled identity=" + identity.getName() + "  group=" + group.getName());
			// 1. Check if group has max size defined. If so check if group is full
			// o_clusterREVIEW cg please review it - also where does the group.getMaxParticipants().equals("") come from??
			// and: why can't we just have a group here and a max participants count and an identity to enrol?
			// the group was chosen, so why do we need the groupNames and areaNames here???

			Codepoint.codepoint(EnrollmentManager.class, "beforeDoInSync");
		//TODO gsync
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
				public void execute() {
					logInfo("doEnroll start: group="+OresHelper.createStringRepresenting(group), identity.getName());
					Codepoint.codepoint(EnrollmentManager.class, "doInSync1");
					// 6_1_0-RC15: reload group object here another node might have changed this in the meantime
					BusinessGroup reloadedGroup = businessGroupService.loadBusinessGroup(group);					
					if (reloadedGroup.getMaxParticipants() != null && !reloadedGroup.getMaxParticipants().equals("")) {
						int participantsCounter = securityManager.countIdentitiesOfSecurityGroup(reloadedGroup.getPartipiciantGroup());
						
						logInfo("doEnroll - participantsCounter: " + participantsCounter + ", maxParticipants: " + reloadedGroup.getMaxParticipants().intValue(), identity.getName());
						if (participantsCounter >= reloadedGroup.getMaxParticipants().intValue()) {
							// already full, show error and updated choose page again
							if (!reloadedGroup.getWaitingListEnabled().booleanValue()) {
								// No Waiting List => List is full
								enrollStatus.setErrorMessage(trans.translate("error.group.full"));
							} else {
								boolean done = addUserToWaitingList(identity, reloadedGroup, enNode, coursePropertyManager, wControl, trans);
								enrollStatus.setIsInWaitingList(done);
							}
						} else {
							boolean done = addUserToParticipantList(identity, reloadedGroup, enNode, coursePropertyManager, wControl, trans);
							Codepoint.codepoint(EnrollmentManager.class, "doInSync2");
							enrollStatus.setIsEnrolled(done);
							logInfo("doEnroll - setIsEnrolled ", identity.getName());
						}
					} else {
						if (isLogDebugEnabled()) logDebug("doEnroll beginTransaction");
						boolean done = addUserToParticipantList(identity, reloadedGroup, enNode, coursePropertyManager, wControl, trans);
						enrollStatus.setIsEnrolled(done);						
						if (isLogDebugEnabled()) logDebug("doEnroll committed");
					}
					logInfo("doEnroll end", identity.getName());
				}				
			});// end of doInSync
			Codepoint.codepoint(EnrollmentManager.class, "afterDoInSync");
		} else {
			enrollStatus.setErrorMessage(trans.translate("error.group.already.enrolled"));
		}
		if (isLogDebugEnabled()) logDebug("doEnroll finished");
		return enrollStatus;
	}

	public void doCancelEnrollment(final Identity identity, final BusinessGroup enrolledGroup, final ENCourseNode enNode,
			final CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		if (isLogDebugEnabled()) logDebug("doCancelEnrollment");
		// 1. Remove group membership, fire events, do loggin etc.
		final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
	//TODO gsync
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(enrolledGroup, new SyncerExecutor(){
			public void execute() {
				// Remove participant. This will also check if a waiting-list with auto-close-ranks is configurated
				// and move the users accordingly
				businessGroupService.removeParticipant(identity, identity, enrolledGroup, flags);
				logInfo("doCancelEnrollment in group " + enrolledGroup, identity.getName());
				// 2. Remove enrollmentdate property
				// only remove last time date, not firsttime
				Property lastTime = coursePropertyManager
				.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_ENROLLMENT_DATE);
				if (lastTime != null) {
					coursePropertyManager.deleteProperty(lastTime);
				}
			}});
		

		// 3. Send notification mail
		MailTemplate mailTemplate = BGMailHelper.createRemoveMyselfMailTemplate(enrolledGroup, identity);
		MailerWithTemplate mailer = MailerWithTemplate.getInstance();
		//fxdiff VCRP-16: intern mail system
		MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
		MailerResult mailerResult = mailer.sendMail(context, identity, null, null, mailTemplate, null);
		MailHelper.printErrorsAndWarnings(mailerResult, wControl, trans.getLocale());
	}

	public void doCancelEnrollmentInWaitingList(final Identity identity, final BusinessGroup enrolledWaitingListGroup, final ENCourseNode enNode,
			final CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		// 1. Remove group membership, fire events, do loggin etc.
		//TODO gsync
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(enrolledWaitingListGroup, new SyncerExecutor(){
			public void execute() {
				businessGroupService.removeFromWaitingList(identity, identity, enrolledWaitingListGroup);
				// 2. Remove enrollmentdate property
				// only remove last time date, not firsttime
				Property lastTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null,
						ENCourseNode.PROPERTY_RECENT_WAITINGLIST_DATE);
				if (lastTime != null) {
					coursePropertyManager.deleteProperty(lastTime);
				}
			}});
		

		// 3. Send notification mail
		MailTemplate mailTemplate = BGMailHelper.createRemoveWaitinglistMailTemplate(enrolledWaitingListGroup, identity);
		MailerWithTemplate mailer = MailerWithTemplate.getInstance();
		//fxdiff VCRP-16: intern mail system
		MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
		MailerResult mailerResult = mailer.sendMail(context, identity, null, null, mailTemplate, null);
		MailHelper.printErrorsAndWarnings(mailerResult, wControl, trans.getLocale());
	}

	// Helper Methods
	// ////////////////
	/**
	 * @param identity
	 * @param groupNames
	 * @return BusinessGroup in which the identity is enrolled, null if identity
	 *         is nowhere enrolled.
	 */
	protected BusinessGroup getBusinessGroupWhereEnrolled(Identity identity, List<Long> groupKeys, List<Long> areaKeys, CourseGroupManager cgm) {
		// 1. check in groups
		if(groupKeys != null && !groupKeys.isEmpty()) {
			SearchBusinessGroupParams params = new SearchBusinessGroupParams();
			params.setAttendee(true);
			params.setIdentity(identity);
			params.setKeys(groupKeys);
			List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, cgm.getCourseResource(), 0, 1);
			if (groups.size() > 0) {
					// Usually it is only possible to be in one group. However,
					// theoretically the
					// admin can put the user in a second enrollment group or the user could
					// theoretically be in a second group context. For now, we only look for
					// the first
					// group. All groups found after the first one are discarded.
				return groups.get(0);
			}
		}
		// 2. check in areas
		if(areaKeys != null && !areaKeys.isEmpty()) {
			for (Long areaKey:areaKeys) {
				String areaName = areaKey.toString();//TODO gm
				List<BusinessGroup> groups = cgm.getParticipatingLearningGroupsInAreaFromAllContexts(identity, areaName);
				if (groups.size() > 0) {
					// Usually it is only possible to be in one group. However,
					// theoretically the
					// admin can put the user in a second enrollment group or the user could
					// theoretically be in a second group context. For now, we only look for
					// the first
					// group. All groups found after the first one are discarded.
					return groups.get(0);
				}
			}
		}
		return null; 
	}

	/**
	 * @param identity
	 * @param groupNames
	 * @return true if this identity is any waiting-list group in this course that
	 *         has a name that is in the group names list
	 */
	protected BusinessGroup getBusinessGroupWhereInWaitingList(Identity identity, List<Long> groupKeys, List<Long> areaKeys, CourseGroupManager cgm) {
		List<BusinessGroup> groups = loadGroupsFromNames(groupKeys, areaKeys, cgm);
		// loop over all business-groups
		for (BusinessGroup businessGroup:groups) {
			if (securityManager.isIdentityInSecurityGroup(identity, businessGroup.getWaitingGroup())) { 
				return businessGroup;
			}
		}
		return null;
	}

	/**
	 * @param groupNames
	 * @return a list of business groups from any of the courses group contexts
	 *         that match the names from the groupNames list. If a groupname is
	 *         not found it won't be in the list. So groupNames.size() can very
	 *         well by different than loadGroupsFromNames().size()
	 */
	protected List<BusinessGroup> loadGroupsFromNames(List<Long> groupKeys, List<Long> areaKeys, CourseGroupManager cgm) {
		List<BusinessGroup> groups = businessGroupService.loadBusinessGroups(groupKeys);
		List<BusinessGroup> areaGroups = areaManager.findBusinessGroupsOfAreaKeys(areaKeys);
		// add groups from areas
		for (BusinessGroup areaGroup:areaGroups) {
			if (!groups.contains(areaGroup)) {
				groups.add(areaGroup);
			}
		}
		return groups;
	}

	/**
	 * Check if in any business-group a waiting-list is configured.
	 * 
	 * @param groups
	 * @return true : YES, there are waiting-list<br>
	 *         false: NO, no waiting-list
	 */
	protected boolean hasAnyWaitingList(List<BusinessGroup> groups) {
		for (BusinessGroup businessGroup :groups) {
			if (businessGroup.getWaitingListEnabled().booleanValue()) {
				return true;
			}
		}
		return false;
	}

	// /////////////////
	// Private Methods
	// /////////////////
	private boolean addUserToParticipantList(Identity identity, BusinessGroup group, ENCourseNode enNode,
			CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
		// 1. Add user to group, fire events, do loggin etc.
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		businessGroupService.addParticipant(identity, identity, group, flags);
		// 2. Set first enrollment date
		String nowString = Long.toString(System.currentTimeMillis());
		Property firstTime = coursePropertyManager
				.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_INITIAL_ENROLLMENT_DATE);
		if (firstTime == null) {
			// create firsttime
			firstTime = coursePropertyManager.createCourseNodePropertyInstance(enNode, identity, null,
					ENCourseNode.PROPERTY_INITIAL_ENROLLMENT_DATE, null, null, nowString, null);
			coursePropertyManager.saveProperty(firstTime);
		}
		// 3. Set enrollmentdate property
		Property thisTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_ENROLLMENT_DATE);
		if (thisTime == null) {
			// create firsttime
			thisTime = coursePropertyManager.createCourseNodePropertyInstance(enNode, identity, null,
					ENCourseNode.PROPERTY_RECENT_ENROLLMENT_DATE, null, null, nowString, null);
			coursePropertyManager.saveProperty(thisTime);
		} else {
			thisTime.setStringValue(nowString);
			coursePropertyManager.updateProperty(thisTime);
		}
		// 4. Send notification mail
		MailTemplate mailTemplate = BGMailHelper.createAddMyselfMailTemplate(group, identity);
		MailerWithTemplate mailer = MailerWithTemplate.getInstance();
		//fxdiff VCRP-16: intern mail system
		MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
		MailerResult mailerResult = mailer.sendMail(context, identity, null, null, mailTemplate, null);
		MailHelper.printErrorsAndWarnings(mailerResult, wControl, trans.getLocale());


		return true;
	}

	private boolean addUserToWaitingList(Identity identity, BusinessGroup group, ENCourseNode enNode,
			CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
		// 1. Add user to group, fire events, do loggin etc.
		businessGroupService.addToWaitingList(identity, identity, group);
		// 2. Set first waiting-list date
		String nowString = Long.toString(System.currentTimeMillis());
		Property firstTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null,
				ENCourseNode.PROPERTY_INITIAL_WAITINGLIST_DATE);
		if (firstTime == null) {
			// create firsttime
			firstTime = coursePropertyManager.createCourseNodePropertyInstance(enNode, identity, null,
					ENCourseNode.PROPERTY_INITIAL_WAITINGLIST_DATE, null, null, nowString, null);
			coursePropertyManager.saveProperty(firstTime);
		}
		// 3. Set waiting-list date property
		Property thisTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_WAITINGLIST_DATE);
		if (thisTime == null) {
			// create firsttime
			thisTime = coursePropertyManager.createCourseNodePropertyInstance(enNode, identity, null,
					ENCourseNode.PROPERTY_RECENT_WAITINGLIST_DATE, null, null, nowString, null);
			coursePropertyManager.saveProperty(thisTime);
		} else {
			thisTime.setStringValue(nowString);
			coursePropertyManager.updateProperty(thisTime);
		}		
		// 4. Send notification mail
		MailTemplate mailTemplate = BGMailHelper.createAddWaitinglistMailTemplate(group, identity);
		MailerWithTemplate mailer = MailerWithTemplate.getInstance();
		//fxdiff VCRP-16: intern mail system
		MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
		MailerResult mailerResult = mailer.sendMail(context, identity, null, null, mailTemplate, null);
		MailHelper.printErrorsAndWarnings(mailerResult, wControl, trans.getLocale());

		return true;
	}

}