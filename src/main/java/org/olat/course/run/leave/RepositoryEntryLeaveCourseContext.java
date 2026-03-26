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
import org.olat.core.id.Identity;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.run.leave.LeaveCourseParticipation.Origin;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;

/**
 * Initial date: 16.03.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class RepositoryEntryLeaveCourseContext implements LeaveCourseContext {

	private final RepositoryEntry repositoryEntry;
	private final Identity identity;

	private List<LeaveCourseParticipation> participations;
	private Date now;

	public RepositoryEntryLeaveCourseContext(RepositoryEntry repositoryEntry, Identity identity) {
		this.repositoryEntry = repositoryEntry;
		this.identity = identity;
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
		return false;
	}

	@Override
	public boolean isAssessmentMode() {
		return false;
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

		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		if (repositoryService.hasRole(identity, repositoryEntry, "participant")) {
			result.add(new LeaveCourseParticipation(Origin.DIRECT, true, 1, true));
		}

		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(
				repositoryEntry, identity, List.of(CurriculumRoles.participant));
		if (!curriculumElements.isEmpty()) {
			result.add(new LeaveCourseParticipation(Origin.CPL, true, 1, true));
		}

		boolean isCourse = CourseModule.ORES_TYPE_COURSE.equals(repositoryEntry.getOlatResource().getResourceableTypeName());

		BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, false, true);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, repositoryEntry, 0, -1);
		if (!groups.isEmpty()) {
			Map<Long, Boolean> enrollmentDelistingByGroupKey = new HashMap<>(1);

			if (isCourse) {
				var course = CourseFactory.loadCourse(repositoryEntry);
				var rootNode = course.getRunStructure().getRootNode();
				var courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();

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
			}

			BusinessGroupRelationDAO businessGroupRelationDAO = CoreSpringFactory.getImpl(BusinessGroupRelationDAO.class);
			for (BusinessGroup group : groups) {
				boolean groupLeavingAllowed = businessGroupService.isAllowToLeaveBusinessGroup(identity, group).isAllowToLeave();
				int linkedCourseCount = businessGroupRelationDAO.countResources(group);
				boolean isEnrollment = isCourse && enrollmentDelistingByGroupKey.containsKey(group.getKey());
				boolean delisting = isCourse && enrollmentDelistingByGroupKey.getOrDefault(group.getKey(), false);
				boolean enrollmentDelistingPermitted = !isEnrollment || delisting;
				result.add(new LeaveCourseParticipation(Origin.GROUP, groupLeavingAllowed, linkedCourseCount, enrollmentDelistingPermitted));
			}
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
