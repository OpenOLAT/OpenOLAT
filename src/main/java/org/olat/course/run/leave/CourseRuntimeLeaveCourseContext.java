/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.course.run.leave;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.run.leave.LeaveCourseParticipation.Origin;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Initial date: 16.03.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CourseRuntimeLeaveCourseContext implements LeaveCourseContext {

	private final RepositoryEntry repositoryEntry;
	private final UserCourseEnvironment userCourseEnv;
	private final boolean assessmentLock;
	private final boolean guestOnly;
	private final boolean curriculumParticipant;

	private List<LeaveCourseParticipation> participations;
	private Date now;

	public CourseRuntimeLeaveCourseContext(UserCourseEnvironment userCourseEnv,
			boolean assessmentLock, boolean isGuestOnly, boolean isCurriculumParticipant) {
		this.repositoryEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		this.userCourseEnv = userCourseEnv;
		this.assessmentLock = assessmentLock;
		this.guestOnly = isGuestOnly;
		this.curriculumParticipant = isCurriculumParticipant;
	}

	@Override
	public RepositoryEntryRuntimeType getRuntimeType() {
		return repositoryEntry.getRuntimeType();
	}

	@Override
	public RepositoryEntryAllowToLeaveOptions getAllowToLeave() {
		return repositoryEntry.getAllowToLeaveOption();
	}

	@Override
	public boolean isGuest() {
		return guestOnly;
	}

	@Override
	public boolean isAssessmentMode() {
		return assessmentLock;
	}

	@Override
	public List<LeaveCourseParticipation> getParticipations() {
		if (participations == null) {
			participations = buildParticipations();
		}
		return participations;
	}

	private List<LeaveCourseParticipation> buildParticipations() {
		List<LeaveCourseParticipation> result = new ArrayList<>();

		if (userCourseEnv.isMemberParticipant()) {
			result.add(new LeaveCourseParticipation(Origin.DIRECT, true, 1, true));
		}

		List<BusinessGroup> groups = userCourseEnv.getParticipatingGroups();
		if (!groups.isEmpty()) {
			var rootNode = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
			var courseResource = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseResource();

			Map<Long, Boolean> enrollmentDelistingByGroupKey = new HashMap<>();

			new TreeVisitor(node -> {
				if (node instanceof ENCourseNode enNode) {
					try {
						for (BusinessGroup group : groups) {
							if (!enrollmentDelistingByGroupKey.containsKey(group.getKey())
									&& enNode.isUsedForEnrollment(List.of(group), courseResource)) {
								enrollmentDelistingByGroupKey.put(group.getKey(),
										enNode.getModuleConfiguration().getBooleanSafe(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED));
							}
						}
					} catch (Exception e) {
						// ignore
					}
				}
			}, rootNode, true).visitAll();

			BusinessGroupRelationDAO businessGroupRelationDAO = CoreSpringFactory.getImpl(BusinessGroupRelationDAO.class);
			BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
			var identity = userCourseEnv.getIdentityEnvironment().getIdentity();
			for (BusinessGroup group : groups) {
				boolean groupLeavingAllowed = businessGroupService.isAllowToLeaveBusinessGroup(identity, group).isAllowToLeave();
				int linkedCourseCount = businessGroupRelationDAO.countResources(group);
				boolean isEnrollment = enrollmentDelistingByGroupKey.containsKey(group.getKey());
				boolean delisting = enrollmentDelistingByGroupKey.getOrDefault(group.getKey(), false);
				boolean enrollmentDelistingPermitted = !isEnrollment || delisting;
				result.add(new LeaveCourseParticipation(Origin.GROUP, groupLeavingAllowed, linkedCourseCount, enrollmentDelistingPermitted));
			}
		}

		if (curriculumParticipant) {
			result.add(new LeaveCourseParticipation(Origin.CPL, true, 1, true));
		}

		return result;
	}

	@Override
	public RepositoryEntryStatusEnum getEntryStatus() {
		return repositoryEntry.getEntryStatus();
	}

	@Override
	public Date getLifecycleEndDate() {
		return repositoryEntry.getLifecycle() != null ? repositoryEntry.getLifecycle().getValidTo() : null;
	}

	@Override
	public Date getNow() {
		if (now == null) {
			now = new Date();
		}
		return now;
	}
}
