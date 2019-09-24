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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.group.model.EnrollState;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.BGMailHelper;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
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
public class EnrollmentManager {
	
	private static final Logger log = Tracing.createLoggerFor(EnrollmentManager.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;


	public EnrollStatus doEnroll(Identity identity, Roles roles, BusinessGroup group, ENCourseNode enNode, CoursePropertyManager coursePropertyManager,
			WindowControl wControl, Translator trans, List<Long> groupKeys, List<Long> areaKeys, CourseGroupManager cgm) {
		
		final EnrollStatus enrollStatus = new EnrollStatus();
		if (log.isDebugEnabled()) log.debug("doEnroll");
		// check if the user is able to be enrolled
		int groupsEnrolledCount = getBusinessGroupsWhereEnrolled(identity, groupKeys, areaKeys, cgm.getCourseEntry()).size();
		int waitingListCount = getBusinessGroupsWhereInWaitingList(identity, groupKeys, areaKeys).size();
		int enrollCountConfig = enNode.getModuleConfiguration().getIntegerSafe(ENCourseNode.CONFIG_ALLOW_MULTIPLE_ENROLL_COUNT, 1);
		if ( (groupsEnrolledCount + waitingListCount) < enrollCountConfig ) {
			if (log.isDebugEnabled()) log.debug("Identity is not enrolled identity=" + identity.getKey() + "  group=" + group.getName());
			// 1. Check if group has max size defined. If so check if group is full
			// o_clusterREVIEW cg please review it - also where does the group.getMaxParticipants().equals("") come from??
			// and: why can't we just have a group here and a max participants count and an identity to enrol?
			// the group was chosen, so why do we need the groupNames and areaNames here???

			MailPackage doNotSendmailPackage = new MailPackage(false);
			EnrollState state = businessGroupService.enroll(identity, roles, identity, group, doNotSendmailPackage);
			if(state.isFailed()) {
				enrollStatus.setErrorMessage(trans.translate(state.getI18nErrorMessage()));
			} else {
				if(state.getEnrolled() == GroupRoles.participant) {
					addUserToParticipantList(identity, group, enNode, coursePropertyManager, wControl, trans);
					enrollStatus.setIsEnrolled(true);
				} else if(state.getEnrolled() == GroupRoles.waiting) {
					addUserToWaitingList(identity, group, enNode, coursePropertyManager, wControl, trans);
					enrollStatus.setIsInWaitingList(true);
				}
			}
		} else {
			enrollStatus.setErrorMessage(trans.translate("error.group.already.enrolled"));
		}
		if (log.isDebugEnabled()) log.debug("doEnroll finished");
		return enrollStatus;
	}

	public void doCancelEnrollment(final Identity identity, final BusinessGroup enrolledGroup, final ENCourseNode enNode,
			final CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		if (log.isDebugEnabled()) log.debug("doCancelEnrollment");
		// 1. Remove group membership, fire events, do loggin etc.
		// Remove participant. This will also check if a waiting-list with auto-close-ranks is configurated
		// and move the users accordingly
		MailPackage doNotSendmailPackage = new MailPackage(false);
		businessGroupService.removeParticipants(identity, Collections.singletonList(identity), enrolledGroup, doNotSendmailPackage);
		log.info(identity.getKey() + " doCancelEnrollment in group " + enrolledGroup);

		log.info(identity.getKey() + " doCancelEnrollment in group " + enrolledGroup);
		// 2. Remove enrollmentdate property
		// only remove last time date, not firsttime
		Property lastTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_ENROLLMENT_DATE);
		if (lastTime != null) {
			coursePropertyManager.deleteProperty(lastTime);
		}

		// 3. Send notification mail
		MailTemplate mailTemplate = BGMailHelper.createRemoveMyselfMailTemplate(enrolledGroup, identity);
		//fxdiff VCRP-16: intern mail system
		MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, identity, mailTemplate, null, null, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
		}
		MailHelper.printErrorsAndWarnings(result, wControl, false, trans.getLocale());
	}

	public void doCancelEnrollmentInWaitingList(final Identity identity, final BusinessGroup enrolledWaitingListGroup, final ENCourseNode enNode,
			final CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		// 1. Remove group membership, fire events, do loggin etc.
		businessGroupService.removeFromWaitingList(identity, Collections.singletonList(identity), enrolledWaitingListGroup, null);
		
		// 2. Remove enrollmentdate property
		// only remove last time date, not firsttime
		Property lastTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_WAITINGLIST_DATE);
		if (lastTime != null) {
			coursePropertyManager.deleteProperty(lastTime);
		}

		// 3. Send notification mail
		MailTemplate mailTemplate = BGMailHelper.createRemoveWaitinglistMailTemplate(enrolledWaitingListGroup, identity);
		//fxdiff VCRP-16: intern mail system
		MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, identity, mailTemplate, null, null, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
		}
		MailHelper.printErrorsAndWarnings(result, wControl, false, trans.getLocale());
	}

	// Helper Methods
	// ////////////////
	/**
	 * @param identity
	 * @param List<Long> groupKeys which are in the list
	 * @param List<Long> areaKeys which are in the list
	 * @return List<BusinessGroup> in which the identity is enrolled
	 */
	protected List<BusinessGroup> getBusinessGroupsWhereEnrolled(Identity identity, List<Long> groupKeys, List<Long> areaKeys, RepositoryEntry courseResource) {
		List<BusinessGroup> groups = new ArrayList<>();
		//search in the enrollable bg keys for the groups where identity is attendee
		if(groupKeys != null && !groupKeys.isEmpty()) {
			SearchBusinessGroupParams params = new SearchBusinessGroupParams();
			params.setAttendee(true);
			params.setIdentity(identity);
			params.setGroupKeys(groupKeys);
			groups.addAll(businessGroupService.findBusinessGroups(params, courseResource, 0, 0));
		}
		//search in the enrollable area keys for the groups where identity is attendee
		if(areaKeys != null && !areaKeys.isEmpty()) {
			groups.addAll(areaManager.findBusinessGroupsOfAreaAttendedBy(identity, areaKeys, courseResource.getOlatResource()));
		}
		return groups; 
	}

	/**
	 * @param identity
	 * @param groupNames
	 * @return true if this identity is any waiting-list group in this course that
	 *         has a name that is in the group names list
	 */
	protected List<BusinessGroup> getBusinessGroupsWhereInWaitingList(Identity identity, List<Long> groupKeys, List<Long> areaKeys) {
		List<BusinessGroup> groups = loadGroupsFromNames(groupKeys, areaKeys);
		List<BusinessGroup> waitingInTheseGroups = new ArrayList<> ();
		// loop over all business-groups
		for (BusinessGroup businessGroup:groups) {
			if (businessGroupService.hasRoles(identity, businessGroup, GroupRoles.waiting.name())) { 
				waitingInTheseGroups.add(businessGroup);
			}
		}
		return waitingInTheseGroups;
	}

	/**
	 * @param groupNames
	 * @return a list of business groups from any of the courses group contexts
	 *         that match the names from the groupNames list. If a groupname is
	 *         not found it won't be in the list. So groupNames.size() can very
	 *         well by different than loadGroupsFromNames().size()
	 */
	protected List<BusinessGroup> loadGroupsFromNames(List<Long> groupKeys, List<Long> areaKeys) {
		List<BusinessGroup> groups = new ArrayList<>(businessGroupService.loadBusinessGroups(groupKeys));
		List<BusinessGroup> areaGroups = areaManager.findBusinessGroupsOfAreaKeys(areaKeys);
		// add groups from areas
		for (BusinessGroup areaGroup:areaGroups) {
			if (!groups.contains(areaGroup)) {
				groups.add(areaGroup);
			}
		}
		return groups;
	}
	
	public List<Long> getBusinessGroupKeys(List<Long> groupKeys, List<Long> areaKeys) {
		List<Long> allKeys = new ArrayList<>();
		if(groupKeys != null && !groupKeys.isEmpty()) {
			allKeys.addAll(groupKeys);
		}
		if(areaKeys != null && !areaKeys.isEmpty()) {
			List<Long> areaGroupKeys = areaManager.findBusinessGroupKeysOfAreaKeys(areaKeys);
			allKeys.addAll(areaGroupKeys);
		}
		return allKeys;
	}
	
	protected List<EnrollmentRow> getEnrollments(IdentityRef identity, List<Long> groupKeys, List<Long> areaKeys,
			int descriptionMaxSize) {
		List<Long> allGroupKeys = getBusinessGroupKeys(groupKeys, areaKeys);
		if(allGroupKeys.isEmpty()) return Collections.emptyList();
		
		// groupKey, name, description, maxParticipants, waitingListEnabled;
		// numInWaitingList, numOfParticipants, participant, waiting;

		StringBuilder sb = new StringBuilder();
		sb.append("select grp.key, grp.name, grp.description, grp.maxParticipants, grp.waitingListEnabled, ")
		  //num of participant
		  .append(" (select count(participants.key) from bgroupmember participants ")
		  .append("  where participants.group=baseGroup and participants.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  //num of pending
		  .append(" (select count(reservations.key) from resourcereservation reservations ")
		  .append("  where reservations.resource.key=grp.resource.key")
		  .append(" ) as numOfReservationss,")
		  //length of the waiting list
		  .append(" (select count(waiters.key) from bgroupmember waiters ")
		  .append("  where grp.waitingListEnabled=true and waiters.group=baseGroup and waiters.role='").append(GroupRoles.waiting.name()).append("'")
		  .append(" ) as numOfWaiters,")
		  //participant?
		  .append(" (select count(meParticipant.key) from bgroupmember meParticipant ")
		  .append("  where meParticipant.group=baseGroup and meParticipant.role='").append(GroupRoles.participant.name()).append("'")
		  .append("  and meParticipant.identity.key=:identityKey")
		  .append(" ) as numOfMeParticipant,")
		  //waiting?
		  .append(" (select count(meWaiting.key) from bgroupmember meWaiting ")
		  .append("  where grp.waitingListEnabled=true and meWaiting.group=baseGroup and meWaiting.role='").append(GroupRoles.waiting.name()).append("'")
		  .append("  and meWaiting.identity.key=:identityKey")
		  .append(" ) as numOfMeWaiting")
		  
		  .append(" from businessgroup grp ")
		  .append(" inner join grp.baseGroup as baseGroup ")
		  .append(" where grp.key in (:groupKeys)");
		
		List<Object[]> rows = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("groupKeys", allGroupKeys)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		
		List<EnrollmentRow> enrollments = new ArrayList<>(rows.size());
		for(Object[] row:rows) {
			Long key = ((Number)row[0]).longValue();
			String name = (String)row[1];
			String desc = (String)row[2];
			if(StringHelper.containsNonWhitespace(desc) && descriptionMaxSize > 0) {
				String asciiDesc = FilterFactory.getHtmlTagsFilter().filter(desc);
				if(asciiDesc.length() > descriptionMaxSize) {
					desc = Formatter.truncate(asciiDesc, descriptionMaxSize);
				}
			}

			int maxParticipants = row[3] == null ? -1 : ((Number)row[3]).intValue();
			
			Object enabled = row[4];
			boolean waitingListEnabled;
			if(enabled == null) {
				waitingListEnabled = false;
			} else if(enabled instanceof Boolean) {
				waitingListEnabled = ((Boolean)enabled).booleanValue();
			} else if(enabled instanceof Number) {
				int val = ((Number)enabled).intValue();
				waitingListEnabled = val == 1;
			} else {
				waitingListEnabled = false;
			}
			
			int numOfParticipants = row[5] == null ? 0 : ((Number)row[5]).intValue();
			int numOfReservations = row[6] == null ? 0 : ((Number)row[6]).intValue();
			int numOfWaiters = row[7] == null ? 0 : ((Number)row[7]).intValue();
			boolean participant = row[8] == null ? false : ((Number)row[8]).intValue() > 0;
			boolean waiting = row[9] == null ? false : ((Number)row[9]).intValue() > 0;
			
			EnrollmentRow enrollment = new EnrollmentRow(key, name, desc,
					maxParticipants, waitingListEnabled);
			enrollment.setNumOfParticipants(numOfParticipants);
			enrollment.setNumOfReservations(numOfReservations);
			enrollment.setNumInWaitingList(numOfWaiters);
			enrollment.setParticipant(participant);
			enrollment.setWaiting(waiting);
			
			if(waitingListEnabled && waiting) {
				int pos = businessGroupService.getPositionInWaitingListFor(identity, new BusinessGroupRefImpl(key));
				enrollment.setPositionInWaitingList(pos);
			} else {
				enrollment.setPositionInWaitingList(-1);
			}
			
			enrollments.add(enrollment);
		}
		
		return enrollments;
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
		MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, identity, mailTemplate, null, null, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
		}
		MailHelper.printErrorsAndWarnings(result, wControl, false, trans.getLocale());
		return true;
	}

	private boolean addUserToWaitingList(Identity identity, BusinessGroup group, ENCourseNode enNode,
			CoursePropertyManager coursePropertyManager, WindowControl wControl, Translator trans) {
		// <- moved to bgs 1. Add user to group, fire events, do loggin etc.
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
		//fxdiff VCRP-16: intern mail system
		MailContext context = new MailContextImpl(wControl.getBusinessControl().getAsString());
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, identity, mailTemplate, null, null, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
		}
		MailHelper.printErrorsAndWarnings(result, wControl, false, trans.getLocale());
		return true;
	}

}