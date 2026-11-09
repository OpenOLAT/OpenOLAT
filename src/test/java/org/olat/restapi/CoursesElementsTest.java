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
* <p>
*/

package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.httpclient.ConnectionUtilities.NameValuePair;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.TUCourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.support.vo.CourseNodeVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.restapi.support.vo.elements.TaskConfigVO;
import org.olat.restapi.support.vo.elements.TestConfigVO;
import org.olat.test.OlatRestTestCase;

public class CoursesElementsTest extends OlatRestTestCase {
	
	@Test
	public void testCreateCoursePost() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", "course3").queryParam("title", "course3 long name").build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		CourseVO course = conn.parse(response, CourseVO.class);
		assertNotNull(course);
		assertNotNull(course.getKey());
		assertNotNull(course.getEditorRootNodeId());
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure").build();
		List<NameValuePair> newStructureParameters = List.of(
				new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("position", "0"),
				new NameValuePair("shortTitle", "Structure-0"),
				new NameValuePair("longTitle", "Structure-long-0"),
				new NameValuePair("description", "Structure-description-0"),
				new NameValuePair("objectives", "Structure-objectives-0"),
				new NameValuePair("instruction", "Structure-instruction-0"),
				new NameValuePair("instructionalDesign", "Structure-instructionalDesign-0"));
		HttpRequest newStructureMethod = conn.createPost(newStructureUri, null, null, newStructureParameters, MediaType.APPLICATION_JSON);
		

		HttpResponse<InputStream> newStructureResponse = conn.execute(newStructureMethod);
		int newStructureCode = newStructureResponse.statusCode();
		assertTrue(newStructureCode == 200 || newStructureCode == 201);
		CourseNodeVO structureNode = conn.parse(newStructureResponse, CourseNodeVO.class);
		assertNotNull(structureNode);
		assertNotNull(structureNode.getId());
		assertEquals(structureNode.getShortTitle(), "Structure-0");
		assertEquals(structureNode.getLongTitle(), "Structure-long-0");
		assertEquals(structureNode.getDescription(), "Structure-description-0");
		assertEquals(structureNode.getObjectives(), "Structure-objectives-0");
		assertEquals(structureNode.getInstruction(), "Structure-instruction-0");
		assertEquals(structureNode.getInstructionalDesign(), "Structure-instructionalDesign-0");
		assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());
		
		//create single page
		URL pageUrl = CoursesElementsTest.class.getResource("singlepage.html");
		assertNotNull(pageUrl);
		File page = new File(pageUrl.toURI());
		
		URI newPageUri = getElementsUri(course).path("singlepage").build();
		List<NameValuePair> formPageParameters = List.of(
				new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("position", "1"),
				new NameValuePair("shortTitle","Single-Page-0"),
				new NameValuePair("longTitle", "Single-Page-long-0"),
				new NameValuePair("description", "Single-Page-description-0"),
				new NameValuePair("objectives", "Single-Page-objectives-0"),
				new NameValuePair("instruction", "Single-Page-instruction-0"),
				new NameValuePair("instructionalDesign", "Single-Page-instructionalDesign-0"));
		HttpRequest newPageMethod = conn.createPost(newPageUri, page, page.getName(), formPageParameters, MediaType.APPLICATION_JSON);
		
		HttpResponse<InputStream> newPageCode = conn.execute(newPageMethod);
		assertTrue(newPageCode.statusCode() == 200 || newPageCode.statusCode() == 201);
		CourseNodeVO pageNode = conn.parse(newPageCode, CourseNodeVO.class);
		assertNotNull(pageNode);
		assertNotNull(pageNode.getId());
		assertEquals(pageNode.getShortTitle(), "Single-Page-0");
		assertEquals(pageNode.getLongTitle(), "Single-Page-long-0");
		assertEquals(pageNode.getDescription(), "Single-Page-description-0");
		assertEquals(pageNode.getObjectives(), "Single-Page-objectives-0");
		assertEquals(pageNode.getInstruction(), "Single-Page-instruction-0");
		assertEquals(pageNode.getInstructionalDesign(), "Single-Page-instructionalDesign-0");
		assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());
		
		//create a folder node
		URI newFolderUri = getElementsUri(course).path("folder").build();
		String rule = "hasLanguage(\"de\")";
		List<NameValuePair> formParameters = List.of(new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("position", "2"),
				new NameValuePair("shortTitle", "Folder-0"),
				new NameValuePair("longTitle", "Folder-long-0"),
				new NameValuePair("description", "Folder-description-0"),
				new NameValuePair("objectives", "Folder-objectives-0"),
				new NameValuePair("instruction", "Folder-instruction-0"),
				new NameValuePair("instructionalDesign", "Folder-instructionalDesign-0"),
				new NameValuePair("visibilityExpertRules", rule),
				new NameValuePair("downloadExpertRules", rule),
				new NameValuePair("uploadExpertRules", rule));
		HttpRequest newFolderMethod = conn.createPost(newFolderUri, formParameters, MediaType.APPLICATION_JSON);

		HttpResponse<InputStream> newFolderCode = conn.execute(newFolderMethod);
		assertTrue(newFolderCode.statusCode() == 200 || newFolderCode.statusCode() == 201);
		CourseNodeVO folderNode = conn.parse(newFolderCode, CourseNodeVO.class);
		assertNotNull(folderNode);
		assertNotNull(folderNode.getId());
		assertEquals(folderNode.getShortTitle(), "Folder-0");
		assertEquals(folderNode.getLongTitle(), "Folder-long-0");
		assertEquals(folderNode.getDescription(), "Folder-description-0");
		assertEquals(folderNode.getObjectives(), "Folder-objectives-0");
		assertEquals(folderNode.getInstruction(), "Folder-instruction-0");
		assertEquals(folderNode.getInstructionalDesign(), "Folder-instructionalDesign-0");
		assertEquals(folderNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a forum node
		URI newForumUri = getElementsUri(course).path("forum").build();
		List<NameValuePair> forumParmeters = List.of(new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("position", "3"),
				new NameValuePair("shortTitle", "Forum-0"),
				new NameValuePair("longTitle", "Forum-long-0"),
				new NameValuePair("description", "Forum-description-0"),
				new NameValuePair("objectives", "Forum-objectives-0"),
				new NameValuePair("instruction", "Forum-instruction-0"),
				new NameValuePair("instructionalDesign", "Forum-instructionalDesign-0"));
		HttpRequest newForumMethod = conn.createPost(newForumUri, forumParmeters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newForumCode = conn.execute(newForumMethod);
		
		assertTrue(newForumCode.statusCode() == 200 || newForumCode.statusCode() == 201);
		CourseNodeVO forumNode = conn.parse(newForumCode, CourseNodeVO.class);
		assertNotNull(forumNode);
		assertNotNull(forumNode.getId());
		assertEquals(forumNode.getShortTitle(), "Forum-0");
		assertEquals(forumNode.getLongTitle(), "Forum-long-0");
		assertEquals(forumNode.getDescription(), "Forum-description-0");
		assertEquals(forumNode.getObjectives(), "Forum-objectives-0");
		assertEquals(forumNode.getInstruction(), "Forum-instruction-0");
		assertEquals(forumNode.getInstructionalDesign(), "Forum-instructionalDesign-0");
		assertEquals(forumNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a task node
		URI newTaskUri = getElementsUri(course).path("task").build();
		List<NameValuePair> formTaskParmaters = List.of(new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("position", "4"),
				new NameValuePair("shortTitle", "Task-0"),
				new NameValuePair("longTitle", "Task-long-0"),
				new NameValuePair("description", "Task-description-0"),
				new NameValuePair("objectives", "Task-objectives-0"),
				new NameValuePair("instruction", "Task-instruction-0"),
				new NameValuePair("instructionalDesign", "Task-instructionalDesign-0"),
				new NameValuePair("points", "25"),
				new NameValuePair("text", "A very difficult test"));
		HttpRequest newTaskMethod = conn.createPost(newTaskUri, formTaskParmaters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newTaskCode = conn.execute(newTaskMethod);
		assertTrue(newTaskCode.statusCode() == 200 || newTaskCode.statusCode() == 201);
		CourseNodeVO taskNode = conn.parse(newTaskCode, CourseNodeVO.class);
		assertNotNull(taskNode);
		assertNotNull(taskNode.getId());
		assertEquals(taskNode.getShortTitle(), "Task-0");
		assertEquals(taskNode.getLongTitle(), "Task-long-0");
		assertEquals(taskNode.getDescription(), "Task-description-0");
		assertEquals(taskNode.getObjectives(), "Task-objectives-0");
		assertEquals(taskNode.getInstruction(), "Task-instruction-0");
		assertEquals(taskNode.getInstructionalDesign(), "Task-instructionalDesign-0");
		assertEquals(taskNode.getParentId(), course.getEditorRootNodeId());
		

		//create a test node
		URI newTestUri = getElementsUri(course).path("test").build();
		List<NameValuePair> formTestParmaters = List.of(new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("testResourceableId", course.getEditorRootNodeId()),
				new NameValuePair("position", "5"),
				new NameValuePair("shortTitle", "Test-0"),
				new NameValuePair("longTitle", "Test-long-0"),
				new NameValuePair("description", "Test-description-0"),
				new NameValuePair("objectives", "Test-objectives-0"),
				new NameValuePair("instruction", "Test-instruction-0"),
				new NameValuePair("instructionalDesign", "Test-instructionalDesign-0"));
		HttpRequest newTestMethod = conn.createPost(newTestUri, formTestParmaters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newTestCode = conn.execute(newTestMethod);
		assertTrue(newTestCode.statusCode() == 404);//must bind a real test
		RestConnection.consume(newTestCode);
		
		/*
		assertTrue(newTestCode == 200 || newTestCode == 201);
		String newTestBody = newTestMethod.getResponseBodyAsString();
		CourseNodeVO testNode = parse(newTestBody, CourseNodeVO.class);
		assertNotNull(testNode);
		assertNotNull(testNode.getId());
		assertEquals(testNode.getShortTitle(), "Test-0");
		assertEquals(testNode.getParentId(), course.getEditorRootNodeId());
		*/
		
		//create an assessment node
		URI newAssessmentUri = getElementsUri(course).path("assessment").build();
		List<NameValuePair> formAssessmentParmeters = List.of(new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("position", "5"),
				new NameValuePair("shortTitle", "Assessment-0"),
				new NameValuePair("longTitle", "Assessment-long-0"),
				new NameValuePair("description", "Assessment-description-0"),
				new NameValuePair("objectives", "Assessment-objectives-0"),
				new NameValuePair("instruction", "Assessment-instruction-0"),
				new NameValuePair("instructionalDesign", "Assessment-instructionalDesign-0"));
		HttpRequest newAssessmentMethod = conn.createPost(newAssessmentUri, formAssessmentParmeters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newAssessmentCode = conn.execute(newAssessmentMethod);
		assertTrue(newAssessmentCode.statusCode() == 200 || newAssessmentCode.statusCode() == 201);
		CourseNodeVO assessmentNode = conn.parse(newAssessmentCode, CourseNodeVO.class);
		assertNotNull(assessmentNode);
		assertNotNull(assessmentNode.getId());
		assertEquals(assessmentNode.getShortTitle(), "Assessment-0");
		assertEquals(assessmentNode.getLongTitle(), "Assessment-long-0");
		assertEquals(assessmentNode.getDescription(), "Assessment-description-0");
		assertEquals(assessmentNode.getObjectives(), "Assessment-objectives-0");
		assertEquals(assessmentNode.getInstruction(), "Assessment-instruction-0");
		assertEquals(assessmentNode.getInstructionalDesign(), "Assessment-instructionalDesign-0");
		assertEquals(assessmentNode.getParentId(), course.getEditorRootNodeId());
		
		//create an contact node
		URI newContactUri = getElementsUri(course).path("contact").build();
		List<NameValuePair> formContactParmeters = List.of(new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("position", "6"),
				new NameValuePair("shortTitle", "Contact-0"),
				new NameValuePair("longTitle", "Contact-long-0"),
				new NameValuePair("description", "Contact-description-0"),
				new NameValuePair("objectives", "Contact-objectives-0"),
				new NameValuePair("instruction", "Contact-instruction-0"),
				new NameValuePair("instructionalDesign", "Contact-instructionalDesign-0"));
		HttpRequest newContactMethod = conn.createPost(newContactUri, formContactParmeters, MediaType.APPLICATION_JSON);

		HttpResponse<InputStream> newContactCode = conn.execute(newContactMethod);
		assertEquals(200, newContactCode.statusCode());
		CourseNodeVO contactNode = conn.parse(newContactCode, CourseNodeVO.class);
		assertNotNull(contactNode);
		assertNotNull(contactNode.getId());
		assertEquals(contactNode.getShortTitle(), "Contact-0");
		assertEquals(contactNode.getLongTitle(), "Contact-long-0");
		assertEquals(contactNode.getDescription(), "Contact-description-0");
		assertEquals(contactNode.getObjectives(), "Contact-objectives-0");
		assertEquals(contactNode.getInstruction(), "Contact-instruction-0");
		assertEquals(contactNode.getInstructionalDesign(), "Contact-instructionalDesign-0");
		assertEquals(contactNode.getParentId(), course.getEditorRootNodeId());
		conn.close();
	}
	
	@Test
	public void testCreateCoursePut() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", "course3").queryParam("title", "course3 long name").build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		CourseVO course = conn.parse(response, CourseVO.class);
		assertNotNull(course);
		assertNotNull(course.getKey());
		assertNotNull(course.getEditorRootNodeId());
		
		try {
			ICourse savedCourse = CourseFactory.loadCourse(course.getKey());
			assertNotNull(savedCourse);
		} catch (Exception e) {
			assertTrue(false);
		}
		
		//create a learning group
		GroupVO groupVo = new GroupVO();
		groupVo.setName("hello");
		groupVo.setDescription("hello description");
		groupVo.setMinParticipants(Integer.valueOf(-1));
		groupVo.setMaxParticipants(Integer.valueOf(-1));
		
		URI newGroupUri = getCoursesUri().path(course.getKey().toString()).path("groups").build();
		HttpRequest newGrpMethod = conn.createPut(newGroupUri, groupVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newGrpCode = conn.execute(newGrpMethod);
		assertEquals(200, newGrpCode.statusCode());
		GroupVO group = conn.parse(newGrpCode, GroupVO.class);
		assertNotNull(group);
		assertNotNull(group.getKey());
		
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "0").queryParam("shortTitle", "Structure-0")
			.queryParam("longTitle", "Structure-long-0")
			.queryParam("description", "Structure-description-0")
			.queryParam("objectives", "Structure-objectives-0")
			.queryParam("instruction", "Structure-instruction-0")
			.queryParam("instructionalDesign", "Structure-instructionalDesign-0")
			.build();
		HttpRequest newStructureMethod = conn.createPut(newStructureUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newStructureCode = conn.execute(newStructureMethod);
		assertTrue(newStructureCode.statusCode() == 200 || newStructureCode.statusCode() == 201);
		CourseNodeVO structureNode = conn.parse(newStructureCode, CourseNodeVO.class);
		assertNotNull(structureNode);
		assertNotNull(structureNode.getId());
		assertEquals(structureNode.getShortTitle(), "Structure-0");
		assertEquals(structureNode.getLongTitle(), "Structure-long-0");
		assertEquals(structureNode.getDescription(), "Structure-description-0");
		assertEquals(structureNode.getObjectives(), "Structure-objectives-0");
		assertEquals(structureNode.getInstruction(), "Structure-instruction-0");
		assertEquals(structureNode.getInstructionalDesign(), "Structure-instructionalDesign-0");
		assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create single page
		URL pageUrl = CoursesElementsTest.class.getResource("singlepage.html");
		assertNotNull(pageUrl);
		File page = new File(pageUrl.toURI());
		
		URI newPageUri = getElementsUri(course).path("singlepage").build();
		List<NameValuePair> formPageParameters = List.of(
				new NameValuePair("filename", page.getName()),
				new NameValuePair("parentNodeId",course.getEditorRootNodeId()),
				new NameValuePair("position", "1"),
				new NameValuePair("shortTitle", "Single-Page-0"),
				new NameValuePair("longTitle", "Single-Page-long-0"),
				new NameValuePair("description", "Single-Page-description-0"),
				new NameValuePair("objectives", "Single-Page-objectives-0"),
				new NameValuePair("instruction", "Single-Page-instruction-0"),
				new NameValuePair("instructionalDesign", "Single-Page-instructionalDesign-0"));
		HttpRequest newPageMethod = conn.createPut(newPageUri, page, page.getName(), formPageParameters, MediaType.APPLICATION_JSON);

		
		HttpResponse<InputStream> newPageCode = conn.execute(newPageMethod);
		assertTrue(newPageCode.statusCode() == 200 || newPageCode.statusCode() == 201);
		CourseNodeVO pageNode = conn.parse(newPageCode, CourseNodeVO.class);
		assertNotNull(pageNode);
		assertNotNull(pageNode.getId());
		assertEquals(pageNode.getShortTitle(), "Single-Page-0");
		assertEquals(pageNode.getLongTitle(), "Single-Page-long-0");
		assertEquals(pageNode.getObjectives(), "Single-Page-objectives-0");
		assertEquals(pageNode.getDescription(), "Single-Page-description-0");
		assertEquals(pageNode.getObjectives(), "Single-Page-objectives-0");
		assertEquals(pageNode.getInstruction(), "Single-Page-instruction-0");
		assertEquals(pageNode.getInstructionalDesign(), "Single-Page-instructionalDesign-0");
		assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a folder node
		URI newFolderUri = getElementsUri(course).path("folder")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "2").queryParam("shortTitle", "Folder-0")
			.queryParam("longTitle", "Folder-long-0")
			.queryParam("objectives", "Folder-objectives-0")
			.queryParam("description", "Folder-description-0")
			.queryParam("objectives", "Folder-objectives-0")
			.queryParam("instruction", "Folder-instruction-0")
			.queryParam("instructionalDesign", "Folder-instructionalDesign-0")
			.build();
		HttpRequest newFolderMethod = conn.createPut(newFolderUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newFolderCode = conn.execute(newFolderMethod);
		assertTrue(newFolderCode.statusCode() == 200 || newFolderCode.statusCode() == 201);
		CourseNodeVO folderNode = conn.parse(newFolderCode, CourseNodeVO.class);
		assertNotNull(folderNode);
		assertNotNull(folderNode.getId());
		assertEquals(folderNode.getShortTitle(), "Folder-0");
		assertEquals(folderNode.getLongTitle(), "Folder-long-0");
		assertEquals(folderNode.getObjectives(), "Folder-objectives-0");
		assertEquals(folderNode.getDescription(), "Folder-description-0");
		assertEquals(folderNode.getObjectives(), "Folder-objectives-0");
		assertEquals(folderNode.getInstruction(), "Folder-instruction-0");
		assertEquals(folderNode.getInstructionalDesign(), "Folder-instructionalDesign-0");
		assertEquals(folderNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a forum node
		URI newForumUri = getElementsUri(course).path("forum")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "3").queryParam("shortTitle", "Forum-0")
			.queryParam("longTitle", "Forum-long-0")
			.queryParam("description", "Forum-description-0")
			.queryParam("objectives", "Forum-objectives-0")
			.queryParam("instruction", "Forum-instruction-0")
			.queryParam("instructionalDesign", "Forum-instructionalDesign-0")
			.build();
		HttpRequest newForumMethod = conn.createPut(newForumUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newForumCode = conn.execute(newForumMethod);
		assertTrue(newForumCode.statusCode() == 200 || newForumCode.statusCode() == 201);
		CourseNodeVO forumNode = conn.parse(newForumCode, CourseNodeVO.class);
		assertNotNull(forumNode);
		assertNotNull(forumNode.getId());
		assertEquals(forumNode.getShortTitle(), "Forum-0");
		assertEquals(forumNode.getLongTitle(), "Forum-long-0");
		assertEquals(forumNode.getObjectives(), "Forum-objectives-0");
		assertEquals(forumNode.getDescription(), "Forum-description-0");
		assertEquals(forumNode.getObjectives(), "Forum-objectives-0");
		assertEquals(forumNode.getInstruction(), "Forum-instruction-0");
		assertEquals(forumNode.getInstructionalDesign(), "Forum-instructionalDesign-0");
		assertEquals(forumNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a task node
		URI newTaskUri = getElementsUri(course).path("task")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "4").queryParam("shortTitle", "Task-0")
			.queryParam("longTitle", "Task-long-0")
			.queryParam("description", "Task-description-0")
			.queryParam("objectives", "Task-objectives-0")
			.queryParam("instruction", "Task-instruction-0")
			.queryParam("instructionalDesign", "Task-instructionalDesign-0")
			.queryParam("points", "25").queryParam("text", "A very difficult test").build();
		HttpRequest newTaskMethod = conn.createPut(newTaskUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newTaskCode = conn.execute(newTaskMethod);
		assertTrue(newTaskCode.statusCode() == 200 || newTaskCode.statusCode() == 201);
		CourseNodeVO taskNode = conn.parse(newTaskCode, CourseNodeVO.class);
		assertNotNull(taskNode);
		assertNotNull(taskNode.getId());
		assertEquals(taskNode.getShortTitle(), "Task-0");
		assertEquals(taskNode.getLongTitle(), "Task-long-0");
		assertEquals(taskNode.getObjectives(), "Task-objectives-0");
		assertEquals(taskNode.getDescription(), "Task-description-0");
		assertEquals(taskNode.getObjectives(), "Task-objectives-0");
		assertEquals(taskNode.getInstruction(), "Task-instruction-0");
		assertEquals(taskNode.getInstructionalDesign(), "Task-instructionalDesign-0");
		assertEquals(taskNode.getParentId(), course.getEditorRootNodeId());
		
		//add task configuration
		URI taskConfigUri = getElementsUri(course).path("task/"+taskNode.getId()+"/configuration")
		.queryParam("enableAssignment", Boolean.FALSE)
		.queryParam("enableScoring", Boolean.TRUE)
		.queryParam("grantScoring", Boolean.TRUE)
		.queryParam("scoreMin", Float.valueOf(1.5f))
		.queryParam("scoreMax", 10)
		.build();
		HttpRequest taskConfigMethod = conn.createPut(taskConfigUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> taskConfigCode = conn.execute(taskConfigMethod);
		assertTrue(taskConfigCode.statusCode() == 200 || taskConfigCode.statusCode() == 201);
		RestConnection.consume(taskConfigCode);

		HttpRequest getTaskConfig = conn.createGet(taskConfigUri, MediaType.APPLICATION_JSON);
		taskConfigCode = conn.execute(getTaskConfig);
		assertTrue(taskConfigCode.statusCode() == 200 || taskConfigCode.statusCode() == 201);
		TaskConfigVO taskConfig = conn.parse(taskConfigCode, TaskConfigVO.class);
		assertNotNull(taskConfig);
		assertTrue(!taskConfig.getIsAssignmentEnabled());//default is true
		assertTrue(taskConfig.getIsScoringEnabled() & taskConfig.getIsScoringGranted());
		assertTrue(taskConfig.getMinScore().floatValue() == 1.5);
		assertTrue(taskConfig.getMaxScore().floatValue() == 10);
		
		//create an assessment node
		URI newAssessmentUri = getElementsUri(course).path("assessment")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "5").queryParam("shortTitle", "Assessment-0")
			.queryParam("longTitle", "Assessment-long-0")
			.queryParam("description", "Assessment-description-0")
			.queryParam("objectives", "Assessment-objectives-0")
			.queryParam("instruction", "Assessment-instruction-0")
			.queryParam("instructionalDesign", "Assessment-instructionalDesign-0")
			.build();
		HttpRequest newAssessmentMethod = conn.createPut(newAssessmentUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newAssessmentCode = conn.execute(newAssessmentMethod);
		assertTrue(newAssessmentCode.statusCode() == 200 || newAssessmentCode.statusCode() == 201);
		CourseNodeVO assessmentNode = conn.parse(newAssessmentCode, CourseNodeVO.class);
		assertNotNull(assessmentNode);
		assertNotNull(assessmentNode.getId());
		assertEquals(assessmentNode.getShortTitle(), "Assessment-0");
		assertEquals(assessmentNode.getLongTitle(), "Assessment-long-0");
		assertEquals(assessmentNode.getDescription(), "Assessment-description-0");
		assertEquals(assessmentNode.getObjectives(), "Assessment-objectives-0");
		assertEquals(assessmentNode.getInstruction(), "Assessment-instruction-0");
		assertEquals(assessmentNode.getInstructionalDesign(), "Assessment-instructionalDesign-0");
		assertEquals(assessmentNode.getParentId(), course.getEditorRootNodeId());
		
		//create an contact node
		URI newContactUri = getElementsUri(course).path("contact")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "6").queryParam("shortTitle", "Contact-0")
			.queryParam("longTitle", "Contact-long-0")
			.queryParam("description", "Contact-description-0")
			.queryParam("objectives", "Contact-objectives-0")
			.queryParam("instruction", "Contact-instruction-0")
			.queryParam("instructionalDesign", "Contact-instructionalDesign-0")
			.build();
		HttpRequest newContactMethod = conn.createPut(newContactUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newContactCode = conn.execute(newContactMethod);
		assertEquals(200, newContactCode.statusCode());
		CourseNodeVO contactNode = conn.parse(newContactCode, CourseNodeVO.class);
		assertNotNull(contactNode);
		assertNotNull(contactNode.getId());
		assertEquals(contactNode.getShortTitle(), "Contact-0");
		assertEquals(contactNode.getLongTitle(), "Contact-long-0");
		assertEquals(contactNode.getDescription(), "Contact-description-0");
		assertEquals(contactNode.getObjectives(), "Contact-objectives-0");
		assertEquals(contactNode.getInstruction(), "Contact-instruction-0");
		assertEquals(contactNode.getInstructionalDesign(), "Contact-instructionalDesign-0");
		assertEquals(contactNode.getParentId(), course.getEditorRootNodeId());
		
		//try to create an invalid enrollment node
		URI newENUri = getElementsUri(course).path("enrollment")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "7").queryParam("shortTitle", "Enrollment-0")
			.queryParam("longTitle", "Enrollment-long-0")
			.queryParam("objectives", "Enrollment-objectives-0").build();
		HttpRequest newENMethod = conn.createPut(newENUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newENCode = conn.execute(newENMethod);
		assertEquals(406, newENCode.statusCode());
		RestConnection.consume(newENCode);

		//create an enrollment node
		newENUri = getElementsUri(course).path("enrollment")
				.queryParam("parentNodeId", course.getEditorRootNodeId())
				.queryParam("position", "7")
				.queryParam("shortTitle", "Enrollment-0")
				.queryParam("longTitle", "Enrollment-long-0")
				.queryParam("description", "Enrollment-description-0")
				.queryParam("objectives", "Enrollment-objectives-0")
				.queryParam("instruction", "Enrollment-instruction-0")
				.queryParam("instructionalDesign", "Enrollment-instructionalDesign-0")
				.queryParam("groups",group.getKey().toString())
				.queryParam("cancelEnabled","true").build();
		newENMethod = conn.createPut(newENUri, MediaType.APPLICATION_JSON);
		
		newENCode = conn.execute(newENMethod);
		assertEquals(200, newENCode.statusCode());
		CourseNodeVO enNode = conn.parse(newENCode, CourseNodeVO.class);
		assertNotNull(enNode);
		assertNotNull(enNode.getId());
		assertEquals(enNode.getShortTitle(), "Enrollment-0");
		assertEquals(enNode.getLongTitle(), "Enrollment-long-0");
		assertEquals(enNode.getDescription(), "Enrollment-description-0");
		assertEquals(enNode.getObjectives(), "Enrollment-objectives-0");
		assertEquals(enNode.getInstruction(), "Enrollment-instruction-0");
		assertEquals(enNode.getInstructionalDesign(), "Enrollment-instructionalDesign-0");
		assertEquals(enNode.getParentId(), course.getEditorRootNodeId());
		
		//create a test node
		URL qtiDemoUrl = CoursesElementsTest.class.getResource("qti21-demo.zip");
		assertNotNull(qtiDemoUrl);
		File qtiFile = new File(qtiDemoUrl.toURI());
		Assert.assertEquals(4071, qtiFile.length());

		URI repoEntriesUri = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		List<NameValuePair> formTestParameters = List.of(
				new NameValuePair("resourcename", "QTI demo"),
				new NameValuePair("displayname", "QTI demo"));
		HttpRequest qtiRepoMethod = conn.createPut(repoEntriesUri, qtiFile, qtiFile.getName(), formTestParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> qtiRepoCode = conn.execute(qtiRepoMethod);
		int qtiHttpCode = qtiRepoCode.statusCode();
		assertTrue(qtiHttpCode == 200 || qtiHttpCode == 201);
		RepositoryEntryVO newTestVO = conn.parse(qtiRepoCode, RepositoryEntryVO.class);
		assertNotNull(newTestVO);
		
		Long key = newTestVO.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("QTI demo", re.getDisplayname());
		
		URI newTestUri = getElementsUri(course).path("test")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "8").queryParam("shortTitle", "Test-0")
			.queryParam("longTitle", "Test-long-0")
			.queryParam("description", "Test-description-0")
			.queryParam("objectives", "Test-objectives-0")
			.queryParam("instruction", "Test-instruction-0")
			.queryParam("instructionalDesign", "Test-instructionalDesign-0")
			.queryParam("testResourceableId", key).build();
		HttpRequest newTestMethod = conn.createPut(newTestUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> newTestCode = conn.execute(newTestMethod);
		assertTrue(newTestCode.statusCode() == 200 || newTestCode.statusCode() == 201);
		CourseNodeVO testNode = conn.parse(newTestCode, CourseNodeVO.class);
		assertNotNull(testNode);
		assertNotNull(testNode.getId());
		assertEquals(testNode.getShortTitle(), "Test-0");
		assertEquals(testNode.getParentId(), course.getEditorRootNodeId());
		
		// configure test node
		URI testConfigUri = getElementsUri(course).path("test/"+testNode.getId()+"/configuration")
			.queryParam("allowCancel", Boolean.TRUE)
			.queryParam("allowNavigation", Boolean.TRUE)
			.queryParam("allowSuspend", Boolean.TRUE)
			.queryParam("numAttempts", 10)
			.queryParam("sequencePresentation", QTI21Constants.QMD_ENTRY_SEQUENCE_ITEM)
			.queryParam("showNavigation", Boolean.TRUE)
			.queryParam("showQuestionTitle", Boolean.TRUE)
			.queryParam("showResultsAfterFinish", Boolean.TRUE)
			.queryParam("showResultsDependendOnDate", Boolean.TRUE)
			.queryParam("showResultsOnHomepage", key)
			.queryParam("showScoreInfo", Boolean.TRUE)
			.queryParam("showScoreProgress", Boolean.TRUE)
			.queryParam("showSectionsOnly", Boolean.TRUE)
			.queryParam("summaryPresentation", QTI21Constants.QMD_ENTRY_SUMMARY_DETAILED)
			.queryParam("startDate", Long.valueOf(1280444400))//new Date(1280444400))
			.queryParam("endDate", Long.valueOf(1293667200))//new Date(1293667200))
			.build();
		HttpRequest testConfigMethod = conn.createPut(testConfigUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> testConfigCode = conn.execute(testConfigMethod);
		assertTrue(testConfigCode.statusCode() == 200 || testConfigCode.statusCode() == 201);
		RestConnection.consume(testConfigCode);
		
		HttpRequest getTestConfig = conn.createGet(testConfigUri, MediaType.APPLICATION_JSON);
		testConfigCode = conn.execute(getTestConfig);
		assertTrue(testConfigCode.statusCode() == 200 || testConfigCode.statusCode() == 201);
		TestConfigVO testConfig = conn.parse(testConfigCode, TestConfigVO.class);
		Assert.assertEquals(Integer.valueOf(10), testConfig.getNumAttempts());
		assertTrue(testConfig.getAllowCancel());
		assertTrue(testConfig.getSummeryPresentation().equals(QTI21Constants.QMD_ENTRY_SUMMARY_DETAILED));
		
		//create an external page node
		URI newTUUri = getElementsUri(course).path("externalpage")
				.queryParam("parentNodeId", course.getEditorRootNodeId())
				.queryParam("position", "10")
				.queryParam("shortTitle", "ExternalPage-0")
				.queryParam("longTitle", "ExternalPage-long-0")
				.queryParam("description", "ExternalPage-description-0")
				.queryParam("objectives", "ExternalPage-objectives-0")
				.queryParam("instruction", "ExternalPage-instruction-0")
				.queryParam("instructionalDesign", "ExternalPage-instructionalDesign-0")
				.queryParam("url","http://www.olat.org").build();
		HttpRequest newTUMethod = conn.createPut(newTUUri, MediaType.APPLICATION_JSON);

		HttpResponse<InputStream> newTUCode = conn.execute(newTUMethod);
		assertEquals(200, newTUCode.statusCode());
		CourseNodeVO tuNode = conn.parse(newTUCode, CourseNodeVO.class);
		assertNotNull(tuNode);
		assertNotNull(tuNode.getId());
		assertEquals(tuNode.getShortTitle(), "ExternalPage-0");
		assertEquals(tuNode.getLongTitle(), "ExternalPage-long-0");
		assertEquals(tuNode.getDescription(), "ExternalPage-description-0");
		assertEquals(tuNode.getObjectives(), "ExternalPage-objectives-0");
		assertEquals(tuNode.getInstruction(), "ExternalPage-instruction-0");
		assertEquals(tuNode.getInstructionalDesign(), "ExternalPage-instructionalDesign-0");
		assertEquals(tuNode.getParentId(), course.getEditorRootNodeId());
		
		//summary check
		ICourse realCourse = CourseFactory.loadCourse(course.getKey());
		TreeNode realRoot = realCourse.getEditorTreeModel().getRootNode();
		Assert.assertEquals(10, realRoot.getChildCount());
		
		//structure
		CourseEditorTreeNode child = (CourseEditorTreeNode)realRoot.getChildAt(0);
		CourseNode childNode = child.getCourseNode();
		assertTrue(childNode instanceof STCourseNode);
		
		//single page
		child = (CourseEditorTreeNode)realRoot.getChildAt(1);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof SPCourseNode);
		
		//folder
		child = (CourseEditorTreeNode)realRoot.getChildAt(2);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof BCCourseNode);
		
		//forum
		child = (CourseEditorTreeNode)realRoot.getChildAt(3);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof FOCourseNode);
		
		//task
		child = (CourseEditorTreeNode)realRoot.getChildAt(4);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof TACourseNode);
		
		//assessment
		child = (CourseEditorTreeNode)realRoot.getChildAt(5);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof MSCourseNode);
		
		//contact
		child = (CourseEditorTreeNode)realRoot.getChildAt(6);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof COCourseNode);
		
		//enrollment
		child = (CourseEditorTreeNode)realRoot.getChildAt(7);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof ENCourseNode);
		
		//test
		child = (CourseEditorTreeNode) realRoot.getChildAt(8);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof IQTESTCourseNode);
		
		//external page
		child = (CourseEditorTreeNode) realRoot.getChildAt(9);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof TUCourseNode);
		
		//attach file to task
		child = (CourseEditorTreeNode)realRoot.getChildAt(4);
		childNode = child.getCourseNode();
		URI attachTaskFileUri = getElementsUri(course).path("task").path(childNode.getIdent()).path("file")
			.queryParam("filename", "singlepage.html").build();
		HttpRequest taskFileMethod = conn.createPut(attachTaskFileUri, page, page.getName(), List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> taskFileCode = conn.execute(taskFileMethod);
		assertEquals(200, taskFileCode.statusCode());
		String taskFolderPath = TACourseNode.getTaskFolderPathRelToFolderRoot(realCourse, childNode);
		VFSContainer taskFolder = VFSManager.olatRootContainer(taskFolderPath, null);
		VFSLeaf singleFile = (VFSLeaf) taskFolder.resolve("/" + "singlepage.html");
		assertNotNull(singleFile);
		conn.close();

	}
	
	@Test
	public void testUpdateRootNodeCoursePost() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", "course4").queryParam("title", "course4 long name").build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		CourseVO course = conn.parse(response, CourseVO.class);
		assertNotNull(course);
		assertNotNull(course.getKey());
		assertNotNull(course.getEditorRootNodeId());
		
		//update the root node
		URI rootUri = getElementsUri(course).path("structure").path(course.getEditorRootNodeId()).build();
		
		List<NameValuePair> formParameters = List.of(new NameValuePair("shortTitle", "Structure-0b"),
				new NameValuePair("longTitle", "Structure-long-0b"),
				new NameValuePair("objectives", "Structure-objectives-0b"));
		HttpRequest updateMethod = conn.createPost(rootUri, null, null, formParameters, MediaType.APPLICATION_JSON);
		
		HttpResponse<InputStream> newStructureResponse = conn.execute(updateMethod);
		int newStructureCode = newStructureResponse.statusCode();
		assertTrue(newStructureCode == 200 || newStructureCode == 201);
		//check the response
		CourseNodeVO structureNode = conn.parse(newStructureResponse, CourseNodeVO.class);
		assertNotNull(structureNode);
		assertNotNull(structureNode.getId());
		assertEquals(structureNode.getShortTitle(), "Structure-0b");
		assertEquals(structureNode.getLongTitle(), "Structure-long-0b");
		assertEquals(structureNode.getObjectives(), "Structure-objectives-0b");
		assertEquals(structureNode.getId(), course.getEditorRootNodeId());
		
		//check the real node
		ICourse realCourse = CourseFactory.loadCourse(course.getKey());
		CourseEditorTreeModel editorTreeModel = realCourse.getEditorTreeModel();
		CourseEditorTreeNode rootNode = (CourseEditorTreeNode)editorTreeModel.getRootNode();
		assertNotNull(rootNode);
		assertNotNull(rootNode.getIdent());
		assertNotNull(rootNode.getCourseNode());
		assertEquals(rootNode.getCourseNode().getShortTitle(), "Structure-0b");
		assertEquals(rootNode.getCourseNode().getLongTitle(), "Structure-long-0b");
		assertEquals(rootNode.getCourseNode().getObjectives(), "Structure-objectives-0b");
		conn.close();

	}
	
	@Test
	public void testUpdateRootNodeCoursePostWithFile() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", "course5").queryParam("title", "course5 long name").build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());

		CourseVO course = conn.parse(response, CourseVO.class);
		assertNotNull(course);
		assertNotNull(course.getKey());
		assertNotNull(course.getEditorRootNodeId());
		
		//the page
		URL pageUrl = CoursesElementsTest.class.getResource("singlepage.html");
		assertNotNull(pageUrl);
		File page = new File(pageUrl.toURI());
		
		//update the root node
		URI rootUri = getElementsUri(course).path("structure").path(course.getEditorRootNodeId()).build();
		List<NameValuePair> formPageParameters = List.of(
				new NameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new NameValuePair("position", "1"),
				new NameValuePair("shortTitle", "Structure-0-with-file"),
				new NameValuePair("longTitle", "Structure-long-0-with-file"),
				new NameValuePair("objectives", "Structure-objectives-0-with-file"),
				new NameValuePair("displayType", "file"));
		HttpRequest newStructureMethod = conn.createPost(rootUri, page, page.getName(), formPageParameters, MediaType.APPLICATION_JSON);
		
		HttpResponse<InputStream> newStructureCode = conn.execute(newStructureMethod);
		assertTrue(newStructureCode.statusCode() == 200 || newStructureCode.statusCode() == 201);
		//check the response
		CourseNodeVO structureNode = conn.parse(newStructureCode, CourseNodeVO.class);
		assertNotNull(structureNode);
		assertNotNull(structureNode.getId());
		assertEquals(structureNode.getShortTitle(), "Structure-0-with-file");
		assertEquals(structureNode.getLongTitle(), "Structure-long-0-with-file");
		assertEquals(structureNode.getObjectives(), "Structure-objectives-0-with-file");
		assertEquals(structureNode.getId(), course.getEditorRootNodeId());
		
		//check the real node
		ICourse realCourse = CourseFactory.loadCourse(course.getKey());
		CourseEditorTreeModel editorTreeModel = realCourse.getEditorTreeModel();
		CourseEditorTreeNode rootNode = (CourseEditorTreeNode)editorTreeModel.getRootNode();
		assertNotNull(rootNode);
		assertNotNull(rootNode.getIdent());
		assertNotNull(rootNode.getCourseNode());
		assertEquals(rootNode.getCourseNode().getShortTitle(), "Structure-0-with-file");
		assertEquals(rootNode.getCourseNode().getLongTitle(), "Structure-long-0-with-file");
		assertEquals(rootNode.getCourseNode().getObjectives(), "Structure-objectives-0-with-file");
		conn.close();

	}
	
	private UriBuilder getCoursesUri() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses");
	}
	
	private UriBuilder getElementsUri(CourseVO course) {
		return getCoursesUri().path(course.getKey().toString()).path("elements");
	}
}
