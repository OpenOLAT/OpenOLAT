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
package org.olat.restapi.repository.course;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.support.ObjectFactory.get;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.vo.CourseNodeVO;

public abstract class AbstractCourseNodeWebService {
	
	private static final Logger log = Tracing.createLoggerFor(AbstractCourseNodeWebService.class);
	
	private static final String CONDITION_ID_ACCESS = "accessability";
	private static final String CONDITION_ID_VISIBILITY = "visibility";
	
	private CourseEditSession openEditSession(ICourse course, Identity identity) {
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(course, identity, CourseFactory.COURSE_EDITOR_LOCK, null);
		if(lock.isSuccess()) {
			course = CourseFactory.openCourseEditSession(course.getResourceableId());
		}
		return new CourseEditSession(course, lock);
	}
	
	protected Response update(Long courseId, String nodeId, String shortTitle, String longTitle, String objectives,
			String visibilityExpertRules, String accessExpertRules, CustomConfigDelegate config, HttpServletRequest request) {
		return attach(courseId, null, nodeId, null, null, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	protected Response attach(Long courseId, String parentNodeId, String type, Integer position, String shortTitle, String longTitle, 
			String objectives, String visibilityExpertRules, String accessExpertRules, CustomConfigDelegate config, HttpServletRequest request) {
		return attach(courseId, parentNodeId, null, type, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	protected Response attach(Long courseId, String parentNodeId, String nodeId, String type, Integer position, String shortTitle, String longTitle, 
			String objectives, String visibilityExpertRules, String accessExpertRules, CustomConfigDelegate config, HttpServletRequest request) {
		if(config != null && !config.isValid()) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!isAuthorEditor(course, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CourseEditSession editSession = null;
		try {
			editSession = openEditSession(course, getIdentity(request));
			if(!editSession.canEdit()) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			CourseNodeVO node;
			if(nodeId != null) {
				node = updateCourseNode(nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, editSession);
			} else {
				CourseEditorTreeNode parentNode = getParentNode(course, parentNodeId);
				if(parentNode == null) {
					return Response.serverError().status(Status.NOT_FOUND).build();
				}
				node = createCourseNode(type, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, editSession, parentNode, position);
			}
			return Response.ok(node).build();
		} catch(Exception ex) {
			log.error("Error while adding an enrolment building block", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			saveAndCloseCourse(editSession);
		}
	}
	
	protected Response attachNodeConfig(Long courseId, String nodeId, FullConfigDelegate config, HttpServletRequest request) {
		if(config == null || !config.isValid()) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!isAuthorEditor(course, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		CourseEditorTreeNode editorCourseNode = getParentNode(course, nodeId);
		if(editorCourseNode == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		CourseNode courseNode = editorCourseNode.getCourseNode();
		if(courseNode == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!config.isApplicable(course, courseNode)) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CourseEditSession editSession = null;
		try {
			editSession = openEditSession(course, getIdentity(request));
			if(!editSession.canEdit()) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
			config.configure(course, courseNode, moduleConfig);
			course.getEditorTreeModel().nodeConfigChanged(editorCourseNode);
			return Response.ok().build();
		} catch(Exception ex) {
			log.error("Error while adding an enrolment building block", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			saveAndCloseCourse(editSession);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private CourseNodeVO createCourseNode(String type, String shortTitle, String longTitle, String learningObjectives,
			String visibilityExpertRules, String accessExpertRules, CustomConfigDelegate delegateConfig,
			CourseEditSession editSession, CourseEditorTreeNode parentNode, Integer position) {

		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(type);
		CourseNode insertedNode = newNodeConfig.getInstance(parentNode);
		insertedNode.setShortTitle(shortTitle);
		insertedNode.setLongTitle(longTitle);
		insertedNode.setLearningObjectives(learningObjectives);
		insertedNode.setNoAccessExplanation("You don't have access");
		
		if(StringHelper.containsNonWhitespace(visibilityExpertRules)) {
			Condition cond = this.createExpertCondition(CONDITION_ID_VISIBILITY, visibilityExpertRules);
			insertedNode.setPreConditionVisibility(cond);
		}
		
		if(StringHelper.containsNonWhitespace(accessExpertRules) && insertedNode instanceof AbstractAccessableCourseNode) {
			Condition cond = createExpertCondition(CONDITION_ID_ACCESS, accessExpertRules);
			((AbstractAccessableCourseNode)insertedNode).setPreConditionAccess(cond);
		}
		
		ICourse course = editSession.getCourse();
		if(delegateConfig != null) {
			ModuleConfiguration moduleConfig = insertedNode.getModuleConfiguration();
			delegateConfig.configure(course, insertedNode, moduleConfig);
		}

		if (position == null || position.intValue() < 0) {
			course.getEditorTreeModel().addCourseNode(insertedNode, parentNode.getCourseNode());
		} else {
			course.getEditorTreeModel().insertCourseNodeAt(insertedNode, parentNode.getCourseNode(), position);
		}
		
		CourseEditorTreeNode editorNode = course.getEditorTreeModel().getCourseEditorNodeContaining(insertedNode);
		CourseNodeVO vo = get(insertedNode);
		vo.setParentId(editorNode.getParent() == null ? null: editorNode.getParent().getIdent());
		return vo;
	}
	
	private CourseNodeVO updateCourseNode(String nodeId, String shortTitle, String longTitle, String learningObjectives,
			String visibilityExpertRules, String accessExpertRules, CustomConfigDelegate delegateConfig, CourseEditSession editSession) {

		ICourse course = editSession.getCourse();
		
		TreeNode updateEditorNode = course.getEditorTreeModel().getNodeById(nodeId);
		CourseNode updatedNode = course.getEditorTreeModel().getCourseNode(nodeId);
		if(StringHelper.containsNonWhitespace(shortTitle)) {
			updatedNode.setShortTitle(shortTitle);
		}
		if(StringHelper.containsNonWhitespace(longTitle)) {
			updatedNode.setLongTitle(longTitle);
		}
		if(StringHelper.containsNonWhitespace(learningObjectives)) {
			updatedNode.setLearningObjectives(learningObjectives);
		}
		
		if(visibilityExpertRules != null) {
			Condition cond = createExpertCondition(CONDITION_ID_VISIBILITY, visibilityExpertRules);
			updatedNode.setPreConditionVisibility(cond);
		}
		
		if(StringHelper.containsNonWhitespace(accessExpertRules) && updatedNode instanceof AbstractAccessableCourseNode) {
			Condition cond = createExpertCondition(CONDITION_ID_ACCESS, accessExpertRules);
			((AbstractAccessableCourseNode)updatedNode).setPreConditionAccess(cond);
		}
		
		if(delegateConfig != null) {
			ModuleConfiguration moduleConfig = updatedNode.getModuleConfiguration();
			delegateConfig.configure(course, updatedNode, moduleConfig);
		}
		course.getEditorTreeModel().nodeConfigChanged(updateEditorNode);
		CourseEditorTreeNode editorNode = course.getEditorTreeModel().getCourseEditorNodeContaining(updatedNode);
		CourseNodeVO vo = get(updatedNode);
		vo.setParentId(editorNode.getParent() == null ? null: editorNode.getParent().getIdent());
		return vo;
	}
	
	protected Condition createExpertCondition(String conditionId, String expertRules) {
		Condition cond = new Condition();
		cond.setConditionExpression(expertRules);
		cond.setExpertMode(true);
		cond.setConditionId(conditionId);
		return cond;
	}
	
	protected CourseEditorTreeNode getParentNode(ICourse course, String parentNodeId) {
		if (parentNodeId == null) {
			return (CourseEditorTreeNode)course.getEditorTreeModel().getRootNode();
		}
		TreeNode treeNode = course.getEditorTreeModel().getNodeById(parentNodeId);
		if (treeNode instanceof CourseEditorTreeNode) {
			return (CourseEditorTreeNode) treeNode;
		}
		return null;
	}
	
	private void saveAndCloseCourse(CourseEditSession editSession) {
		if(editSession == null || !editSession.canEdit()) return;

		CourseFactory.saveCourseEditorTreeModel(editSession.getCourseId());
		CourseFactory.fireModifyCourseEvent(editSession.getCourseId());//close the edit session too
		CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(editSession.getLock());
	}
	
	protected boolean isAuthorEditor(ICourse course, HttpServletRequest request) {
		try {
			Identity identity = getUserRequest(request).getIdentity();
			CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			return repositoryService.hasRoleExpanded(identity, cgm.getCourseEntry(),
					OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
					GroupRoles.owner.name()) || cgm.hasRight(identity, CourseRights.RIGHT_COURSEEDITOR, null);
		} catch (Exception e) {
			return false;
		}
	}
	
	public interface CustomConfigDelegate {
		
		public boolean isValid();
		
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig);
	}
	
	public interface FullConfigDelegate extends CustomConfigDelegate {
		public boolean isApplicable(ICourse course, CourseNode courseNode);
	}
	
	private class CourseEditSession {
		private final ICourse course;
		private final LockResult entry;
		
		public CourseEditSession(ICourse course, LockResult entry) {
			this.course = course;
			this.entry = entry;
		}
		
		public Long getCourseId() {
			return course.getResourceableId();
		}
		
		public ICourse getCourse() {
			return course;
		}
		
		public LockResult getLock() {
			return entry;
		}
		
		public boolean canEdit() {
			return course != null && entry.isSuccess();
		}
	}
}
