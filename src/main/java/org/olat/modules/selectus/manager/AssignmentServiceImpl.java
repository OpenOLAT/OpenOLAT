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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.AssignmentService;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAssignment;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.assignment.AssignmentKey;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.committee.assignment.AssignmentsData.Spreading;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssignmentServiceImpl implements AssignmentService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private SelectusAssignmentDAO assignmentDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	@Override
	public void assignments(PositionRef position, List<ApplicationLight> applications, List<Identity> assignees, Identity doer, Translator translator) {
		List<ApplicationAssignmentLight> currentAssignments = assignmentDao.getAssignmentPosition(position);
		Set<AssignmentKey> currentAssignmentsSet = currentAssignments.stream()
				.map(assignment -> new AssignmentKey(assignment.getAssigneeKey(), assignment.getApplicationKey()))
				.collect(Collectors.toSet());
		
		for(ApplicationLight app:applications) {
			Application application = applicationDao.loadApplicationByKey(app.getKey());
			for(Identity assignee:assignees) {
				AssignmentKey key = new AssignmentKey(assignee.getKey(), application.getKey());
				if(!currentAssignmentsSet.contains(key)) {
					ApplicationAssignment assignment = assignmentDao.createAssignment(assignee, application);
					
					String messageI18n = "audit.log.assignment.add";
					String[] args = getLogArguments(application, assignee, translator.getLocale());
					String after = auditService.toAuditXml(assignment);
					auditService.auditAssignmentLog(Action.add, null, after, messageI18n, args, translator, position, application, assignee, doer);
				}
			}
		}
		dbInstance.commit();
	}
	
	@Override
	public void assignments(PositionRef position, List<ApplicationLight> applications, List<Identity> assignees,
			Integer additionalAssignments, Integer maximumAssignments, Spreading spreading, Identity doer, Translator translator) {
		
		List<ApplicationAssignmentLight> allCurrentAssignments = assignmentDao.getAssignmentPosition(position);
		Map<Long, List<ApplicationAssignmentLight>> applicationToAssignments = allCurrentAssignments.stream()
		           .collect(Collectors.groupingBy(ApplicationAssignmentLight::getApplicationKey, Collectors.toList()));
		
		AssigneeSpreading assigneeList;
		if(Spreading.additional == spreading) {
			assigneeList = new AssignmentAdditionalSpreading(assignees);
		} else {
			Map<Long, Long> assigneeToAssignments = allCurrentAssignments.stream()
			           .collect(Collectors.groupingBy(ApplicationAssignmentLight::getAssigneeKey, Collectors.counting()));
			assigneeList = new AssignmentTotalSpreading(assignees, assigneeToAssignments);
		}

		for(ApplicationLight app:applications) {
			Application application = applicationDao.loadApplicationByKey(app.getKey());
			List<ApplicationAssignmentLight> currentApplicationAssignments = applicationToAssignments
					.computeIfAbsent(app.getKey(),  appKey -> new ArrayList<>());

			int assignmentsToAdd = 0;
			if(additionalAssignments != null && maximumAssignments != null) {
				assignmentsToAdd = additionalAssignments.intValue();
				int finalNum = currentApplicationAssignments.size() + assignmentsToAdd;
				if(finalNum > maximumAssignments.intValue()) {
					assignmentsToAdd -= (finalNum - maximumAssignments.intValue());
				}
			} else if(maximumAssignments != null) {
				assignmentsToAdd = maximumAssignments - currentApplicationAssignments.size();
			} else if(additionalAssignments != null) {
				assignmentsToAdd = additionalAssignments.intValue();
			}
			if(assignmentsToAdd > 0) {
				completeAssignmentsList(position, application, currentApplicationAssignments, assignmentsToAdd, assigneeList, doer, translator);
			}
		}
	}

	private void completeAssignmentsList(PositionRef position, Application application, List<ApplicationAssignmentLight> currentApplicationAssignments,
			int numOfAssignmentsToAdd, AssigneeSpreading assigneeList, Identity doer, Translator translator) {
		Set<Long> currentAssignees = currentApplicationAssignments.stream()
				.map(ApplicationAssignmentLight::getAssigneeKey)
				.collect(Collectors.toSet());
		
		for(int i=0; i<numOfAssignmentsToAdd; i++) {
			Identity assignee = assigneeList.getNextAssignee(currentAssignees);
			if(assignee != null) {
				ApplicationAssignment assignment = assignmentDao.createAssignment(assignee, application);
				String messageI18n = "audit.log.assignment.add";
				String[] args = getLogArguments(application, assignee, translator.getLocale());
				String after = auditService.toAuditXml(assignment);
				auditService.auditAssignmentLog(Action.add, null, after, messageI18n, args, translator, position, application, assignee, doer);
			}
		}
	}
	
	@Override
	public void removeAssignments(PositionRef position, List<ApplicationLight> applications,
			List<Identity> assigneeList, Identity doer, Translator translator) {
		
		Set<Long> applicationKeys = applications.stream()
				.map(ApplicationLight::getKey).collect(Collectors.toSet());
		Map<Long,Identity> assigneeSet = assigneeList.stream()
				.collect(Collectors.toMap(Identity::getKey, ident -> ident, (u, v) -> u));
		
		List<ApplicationAssignmentLight> assignments = assignmentDao.getAssignmentPosition(position);
		for(ApplicationAssignmentLight assignment:assignments) {
			
			if(applicationKeys.contains(assignment.getApplicationKey()) && assigneeSet.containsKey(assignment.getAssigneeKey())) {
				Application application = applicationDao.loadApplicationByKey(assignment.getApplicationKey());
				Identity assignee = assigneeSet.get(assignment.getAssigneeKey());
				String before = auditService.toAuditXml(assignment);
				
				assignmentDao.removeAssignment(assignment.getKey());

				String messageI18n = "audit.log.assignment.remove";
				String[] args = getLogArguments(application, assignee, translator.getLocale());
				auditService.auditAssignmentLog(Action.add, before, null, messageI18n, args, translator, position, application, assignee, doer);
			}
		}
	}

	/**
	 * <ul>
	 * 	<li>0: applicant full name
	 *  <li>1: application id
	 *  <li>2: name of the committee member
	 * </ul>
	 * @return an array
	 */
	private String[] getLogArguments(Application application, Identity member, Locale locale) {
		// 0 application
		// 1 application id
		// 2 committee member name
		String appName = salutationGenerator.getTitleFullname(application, locale);
		String appId = application.getId() == null ? "" : application.getId().toString();
		String memberName = RecruitingHelper.formatFullNameWithTitle(member, locale);
		return new String[] { appName, appId, memberName };
	}

	@Override
	public List<ApplicationAssignmentLight> getAssignments(PositionRef position) {
		return assignmentDao.getAssignmentPosition(position);
	}

	@Override
	public List<Identity> getAssignees(ApplicationRef application) {
		return assignmentDao.getAssignees(application);
	}
	
	private interface AssigneeSpreading {
		
		public Identity getNextAssignee(Set<Long> excludedAssignes);
		
	}
	
	private static class AssignmentAdditionalSpreading implements AssigneeSpreading {
		
		private final List<Assignee> poolOfAssignees;

		public AssignmentAdditionalSpreading(List<Identity> poolOfAssignees) {
			this.poolOfAssignees = poolOfAssignees.stream()
					.map(Assignee::new)
					.collect(Collectors.toList());
		}
		
		@Override
		public Identity getNextAssignee(Set<Long> excludedAssignes) {
			Collections.sort(poolOfAssignees);
			
			Assignee newAssignee = null;
			for(Assignee fromPool:poolOfAssignees) {
				if(!excludedAssignes.contains(fromPool.getIdentity().getKey())) {
					newAssignee = fromPool;
					break;
				}
			}
			
			Identity identity = null;
			if(newAssignee != null) {
				identity = newAssignee.getIdentity();
				newAssignee.incrementNumOfAssignments();
				excludedAssignes.add(identity.getKey());
			}
			return identity;
		}
	}
	
	private static class AssignmentTotalSpreading implements AssigneeSpreading {
		
		private final List<Assignee> poolOfAssignees;
		
		public AssignmentTotalSpreading(List<Identity> poolOfAssignees, Map<Long,Long> numOfAssignmentsMap) {
			this.poolOfAssignees = poolOfAssignees.stream()
					.map(id -> {
						Long numOfAssignments = numOfAssignmentsMap.get(id.getKey());
						int numberOf = numOfAssignments == null ? 0 : numOfAssignments.intValue();
						return new Assignee(id, numberOf);
					})
					.collect(Collectors.toList());
		}
		
		@Override
		public Identity getNextAssignee(Set<Long> excludedAssignes) {
			Collections.sort(poolOfAssignees);
			
			Assignee newAssignee = null;
			for(Assignee fromPool:poolOfAssignees) {
				if(!excludedAssignes.contains(fromPool.getIdentity().getKey())) {
					newAssignee = fromPool;
					break;
				}
			}
			
			Identity identity = null;
			if(newAssignee != null) {
				identity = newAssignee.getIdentity();
				newAssignee.incrementNumOfAssignments();
				excludedAssignes.add(identity.getKey());
			}
			return identity;
		}
	}
	
	private static class Assignee implements Comparable<Assignee> {
		
		private final Identity identity;
		private int numOfAssignments;
		
		public Assignee(Identity identity) {
			this.identity = identity;
			numOfAssignments = 0;
		}
		
		public Assignee(Identity identity, int numOfAssignments) {
			this.identity = identity;
			this.numOfAssignments = numOfAssignments;
		}
		
		public Identity getIdentity() {
			return identity;
		}
		
		public void incrementNumOfAssignments() {
			numOfAssignments++;
		}

		@Override
		public int compareTo(Assignee o) {
			return Integer.compare(numOfAssignments, o.numOfAssignments);
		}
	}
}
