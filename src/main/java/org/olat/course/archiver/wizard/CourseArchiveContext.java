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
package org.olat.course.archiver.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 16 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveContext {
	
	private final RepositoryEntry courseEntry;
	
	private final CourseArchiveOptions archiveOptions;
	
	private List<CourseNode> courseNodes;
	private boolean allowedLogAuthors;
	private boolean allowedLogUsers;
	private boolean allowedLogStatistics;
	private boolean administrator;

	public CourseArchiveContext(CourseArchiveOptions archiveOptions, RepositoryEntry courseEntry) {
		this.archiveOptions = archiveOptions;
		this.courseEntry = courseEntry;
	}
	
	public static final CourseArchiveContext defaultValues(RepositoryEntry courseEntry, Identity identity, Roles roles, RepositoryService repositoryService) {
		CourseArchiveOptions options = new CourseArchiveOptions();
		options.setArchiveType(ArchiveType.COMPLETE);
		options.setLogSettings(LogSettings.ANONYMOUS);
		
		options.setCustomize(false);
		options.setItemColumns(true);
		options.setPointColumn(true);
		options.setTimeColumns(true);
		options.setCommentColumn(true);
		options.setResultsWithPDFs(false);
		
		options.setLogFiles(true);
		options.setCourseResults(true);
		options.setCourseChat(true);

		CourseArchiveContext context = new CourseArchiveContext(options, courseEntry);
		
		boolean isAdministrator = roles.isAdministrator()
				&& repositoryService.hasRoleExpanded(identity, courseEntry, OrganisationRoles.administrator.name());
		boolean isOresOwner = repositoryService.hasRole(identity, courseEntry, GroupRoles.owner.name());
		boolean isOresInstitutionalManager = roles.isLearnResourceManager()
				&& repositoryService.hasRoleExpanded(identity, courseEntry, OrganisationRoles.learnresourcemanager.name());
		context.administrator = isAdministrator;
		context.allowedLogAuthors = isOresOwner || isOresInstitutionalManager || isAdministrator;
		context.allowedLogUsers = isOresInstitutionalManager || isAdministrator;
		context.allowedLogStatistics = isOresOwner || isOresInstitutionalManager || isAdministrator;
		
		options.setLogFilesAuthors(context.allowedLogAuthors);
		options.setLogFilesUsers(context.allowedLogUsers);
		options.setLogFilesStatistics(!context.allowedLogUsers);
		
		return context;
	}
	
	public RepositoryEntry getCourseEntry() {
		return courseEntry;
	}
	
	public CourseArchiveOptions getArchiveOptions() {
		return archiveOptions;
	}
	
	public boolean isAdministrator() {
		return administrator;
	}

	public boolean isAllowedLogAuthors() {
		return allowedLogAuthors;
	}

	public boolean isAllowedLogUsers() {
		return allowedLogUsers;
	}

	public boolean isAllowedLogStatistics() {
		return allowedLogStatistics;
	}

	public List<CourseNode> getCourseNodes() {
		return courseNodes;
	}

	public void setCourseNodes(List<CourseNode> courseNodes) {
		this.courseNodes = courseNodes;
		List<String> idents = courseNodes.stream()
				.map(CourseNode::getIdent)
				.collect(Collectors.toList());
		archiveOptions.setCourseNodesIdents(idents);
	}
	
	public boolean hasCustomization() {
		List<CourseNode> nodes;
		if(getArchiveOptions().getArchiveType() == ArchiveType.COMPLETE) {
			CourseNode rootNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getRootNode();
			nodes = new ArrayList<>();
			new TreeVisitor(node -> {
				if(node instanceof CourseNode cNode && acceptCourseElement(cNode)) {
					nodes.add(cNode);
				}
			}, rootNode, true).visitAll();
		} else {
			nodes = getCourseNodes();
		}
		
		if(nodes == null || nodes.isEmpty()) {
			return false;
		}
		
		Optional<CourseNode> node = nodes.stream()
				.filter(IQTESTCourseNode.class::isInstance)
				.findFirst();
		return node.isPresent();
	}

	public static boolean acceptCourseElement(CourseNode courseNode) {
		return courseNode instanceof ScormCourseNode
				|| courseNode instanceof GTACourseNode
				|| courseNode instanceof VideoTaskCourseNode
				|| courseNode instanceof IQTESTCourseNode
				|| courseNode instanceof CheckListCourseNode
				|| courseNode instanceof FormCourseNode
				|| courseNode instanceof SurveyCourseNode
				|| courseNode instanceof WikiCourseNode
				|| courseNode instanceof FOCourseNode
				|| courseNode instanceof DialogCourseNode
				|| courseNode instanceof PFCourseNode
				|| courseNode instanceof ProjectBrokerCourseNode;
	}
	
	public enum LogSettings {
		ANONYMOUS,
		PERSONALISED
	}
}
