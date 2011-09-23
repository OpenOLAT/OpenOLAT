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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.course.nodes.en;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGMailHelper;
import org.olat.properties.Property;
import org.olat.testutils.codepoints.server.Codepoint;

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
public class EnrollmentManager  extends BasicManager {
	// TODO with spring
	static final EnrollmentManager enrollmentManager = new EnrollmentManager();
	// Managers TODO inject with spring
	private BaseSecurity securityManager;
	private BusinessGroupManager businessGroupManager;

	/**
	 * @param moduleConfiguration
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param enNode
	 */
	private EnrollmentManager() {
		this.securityManager = BaseSecurityManager.getInstance();
		this.businessGroupManager = BusinessGroupManagerImpl.getInstance();
	}

	public static EnrollmentManager getInstance() {
		return enrollmentManager;
	}

	public EnrollStatus doEnroll(final Identity identity, final BusinessGroup group, final ENCourseNode enNode, final CoursePropertyManager coursePropertyManager,
			final WindowControl wControl, final Translator trans, List groupNames, List areaNames, CourseGroupManager cgm) {
		final EnrollStatus enrollStatus = new EnrollStatus();
		if (Tracing.isDebugEnabled(this.getClass())) Tracing.logDebug("doEnroll", this.getClass());
		// check if the user is already enrolled (user can be enrooled only in one group)
		if ( ( getBusinessGroupWhereEnrolled( identity, groupNames, areaNames, cgm) == null)
			  && ( getBusinessGroupWhereInWaitingList( identity, groupNames, areaNames, cgm) == null) ) {
			if (Tracing.isDebugEnabled(this.getClass())) Tracing.logDebug("Identity is not enrolled identity=" + identity.getName() + "  group=" + group.getName() , this.getClass());
			// 1. Check if group has max size defined. If so check if group is full
			// o_clusterREVIEW cg please review it - also where does the group.getMaxParticipants().equals("") come from??
			// and: why can't we just have a group here and a max participants count and an identity to enrol?
			// the group was chosen, so why do we need the groupNames and areaNames here???

			Codepoint.codepoint(EnrollmentManager.class, "beforeDoInSync");
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
				public void execute() {
					Tracing.logInfo("doEnroll start: group="+OresHelper.createStringRepresenting(group), identity.getName(), EnrollmentManager.class);
					Codepoint.codepoint(EnrollmentManager.class, "doInSync1");
					// 6_1_0-RC15: reload group object here another node might have changed this in the meantime
					BusinessGroup reloadedGroup = (BusinessGroup) DBFactory.getInstance().loadObject(group, true);					
					if (reloadedGroup.getMaxParticipants() != null && !reloadedGroup.getMaxParticipants().equals("")) {
						int participantsCounter = securityManager.countIdentitiesOfSecurityGroup(reloadedGroup.getPartipiciantGroup());
						
						Tracing.logInfo("doEnroll - participantsCounter: " + participantsCounter + ", maxParticipants: " + reloadedGroup.getMaxParticipants().intValue(), identity.getName(), EnrollmentManager.class);
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
							Tracing.logInfo("doEnroll - setIsEnrolled ", identity.getName(), EnrollmentManager.class);
						}
					} else {
						if (Tracing.isDebugEnabled(this.getClass())) Tracing.logDebug("doEnroll beginTransaction", this.getClass());
						boolean done = addUserToParticipantList(identity, reloadedGroup, enNode, coursePropertyManager, wControl, trans);
						enrollStatus.setIsEnrolled(done);						
						if (Tracing.isDebugEnabled(this.getClass())) Tracing.logDebug("doEnroll committed", this.getClass());
					}
					Tracing.logInfo("doEnroll end", identity.getName(), EnrollmentManager.class);
				}				
			});// end of doInSync
			Codepoint.codepoint(EnrollmentManager.class, "afterDoInSync");
		} else {
			enrollStatus.setErrorMessage(trans.translate("error.group.already.enrolled"));
		}
		if (Tracing.isDebugEnabled(this.getClass())) Tracing.logDebug("doEnroll finished", this.getClass());
		return enrollStatus;
	}

	public void doCancelEnrollment(final Identity identity, final BusinessGroup enrolledGroup, final ENCourseNode enNode,
			final CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		if (Tracing.isDebugEnabled(this.getClass())) Tracing.logDebug("doCancelEnrollment", this.getClass());
		// 1. Remove group membership, fire events, do loggin etc.
		final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(enrolledGroup, new SyncerExecutor(){
			public void execute() {
				// Remove participant. This will also check if a waiting-list with auto-close-ranks is configurated
				// and move the users accordingly
				businessGroupManager.removeParticipantAndFireEvent(identity, identity, enrolledGroup, flags, false);
				Tracing.logInfo("doCancelEnrollment in group " + enrolledGroup, identity.getName() , EnrollmentManager.class);
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
		MailerResult mailerResult = mailer.sendMail(identity, null, null, mailTemplate, null);
		MailHelper.printErrorsAndWarnings(mailerResult, wControl, trans.getLocale());
	}

	public void doCancelEnrollmentInWaitingList(final Identity identity, final BusinessGroup enrolledWaitingListGroup, final ENCourseNode enNode,
			final CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		// 1. Remove group membership, fire events, do loggin etc.
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(enrolledWaitingListGroup, new SyncerExecutor(){
			public void execute() {
				businessGroupManager.removeFromWaitingListAndFireEvent(identity, identity, enrolledWaitingListGroup, false);
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
		MailerResult mailerResult = mailer.sendMail(identity, null, null, mailTemplate, null);
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
	protected BusinessGroup getBusinessGroupWhereEnrolled(Identity identity, List groupNames, List areaNames, CourseGroupManager cgm) {
		Iterator iterator = groupNames.iterator();
		// 1. check in groups
		while (iterator.hasNext()) {
			String groupName = (String) iterator.next();
			List groups = cgm.getParticipatingLearningGroupsFromAllContexts(identity, groupName);
			if (groups.size() > 0) {
				// Usually it is only possible to be in one group. However,
				// theoretically the
				// admin can put the user in a second enrollment group or the user could
				// theoretically be in a second group context. For now, we only look for
				// the first
				// group. All groups found after the first one are disgarded.
				return (BusinessGroup) groups.get(0);
			}
		}
		// 2. check in areas
		iterator = areaNames.iterator();
		while (iterator.hasNext()) {
			String areaName = (String) iterator.next();
			List groups = cgm.getParticipatingLearningGroupsInAreaFromAllContexts(identity, areaName);
			if (groups.size() > 0) {
				// Usually it is only possible to be in one group. However,
				// theoretically the
				// admin can put the user in a second enrollment group or the user could
				// theoretically be in a second group context. For now, we only look for
				// the first
				// group. All groups found after the first one are disgarded.
				return (BusinessGroup) groups.get(0);
			}
		}
		return null; // 
	}

	/**
	 * @param identity
	 * @param groupNames
	 * @return true if this identity is any waiting-list group in this course that
	 *         has a name that is in the group names list
	 */
	protected BusinessGroup getBusinessGroupWhereInWaitingList(Identity identity, List groupNames, List areaNames, CourseGroupManager cgm) {
		List groups = loadGroupsFromNames(groupNames, areaNames, cgm);
		BusinessGroup businessGroup;
		// loop over all business-groups
		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			businessGroup = (BusinessGroup) iter.next();
			if (securityManager.isIdentityInSecurityGroup(identity, businessGroup.getWaitingGroup())) { return businessGroup; }
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
	protected List loadGroupsFromNames(List groupNames, List areaNames, CourseGroupManager cgm) {
		List groups = new ArrayList();
		// 1. add groups
		Iterator iterator = groupNames.iterator();
		while (iterator.hasNext()) {
			String groupName = (String) iterator.next();
			List mygroups = cgm.getLearningGroupsFromAllContexts(groupName);
			for (Iterator it = mygroups.iterator(); it.hasNext();) {
				BusinessGroup bg = (BusinessGroup) it.next();
				if (!groups.contains(bg)) groups.add(bg);
			}
		}
		// add groups from areas
		iterator = areaNames.iterator();
		while (iterator.hasNext()) {
			String areaName = (String) iterator.next();
			List mygroups = cgm.getLearningGroupsInAreaFromAllContexts(areaName);
			for (Iterator it = mygroups.iterator(); it.hasNext();) {
				BusinessGroup bg = (BusinessGroup) it.next();
				if (!groups.contains(bg)) groups.add(bg);
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
	protected boolean hasAnyWaitingList(List groups) {
		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			BusinessGroup businessGroup = (BusinessGroup) iter.next();
			if (businessGroup.getWaitingListEnabled().booleanValue()) { return true; }
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
		businessGroupManager.addParticipantAndFireEvent(identity, identity, group, flags, false);
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
		MailerResult mailerResult = mailer.sendMail(identity, null, null, mailTemplate, null);
		MailHelper.printErrorsAndWarnings(mailerResult, wControl, trans.getLocale());


		return true;
	}

	private boolean addUserToWaitingList(Identity identity, BusinessGroup group, ENCourseNode enNode,
			CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
		// 1. Add user to group, fire events, do loggin etc.
		businessGroupManager.addToWaitingListAndFireEvent(identity, identity, group, false);
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
		MailerResult mailerResult = mailer.sendMail(identity, null, null, mailTemplate, null);
		MailHelper.printErrorsAndWarnings(mailerResult, wControl, trans.getLocale());

		return true;
	}

}