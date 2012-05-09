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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.TUCourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.support.vo.CourseNodeVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.restapi.support.vo.elements.SurveyConfigVO;
import org.olat.restapi.support.vo.elements.TaskConfigVO;
import org.olat.restapi.support.vo.elements.TestConfigVO;
import org.olat.test.OlatJerseyTestCase;

public class CoursesElementsTest extends OlatJerseyTestCase {

	private RestConnection conn;
	
	@Before
	public void startup() {
		conn = new RestConnection();
	}
	
  @After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
		} catch (Exception e) {
      e.printStackTrace();
      throw e;
		}
	}
	
	@Test
	public void testCreateCoursePost() throws IOException, URISyntaxException{
		assertTrue(conn.login("administrator", "openolat"));
		
		
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", "course3").queryParam("title", "course3 long name").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		
		CourseVO course = parse(body, CourseVO.class);
		assertNotNull(course);
		assertNotNull(course.getKey());
		assertNotNull(course.getEditorRootNodeId());
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure").build();
		HttpPost newStructureMethod = conn.createPost(newStructureUri, MediaType.APPLICATION_JSON, true);
		conn.addEntity(newStructureMethod, new BasicNameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new BasicNameValuePair("position", "0"),
				new BasicNameValuePair("shortTitle", "Structure-0"),
				new BasicNameValuePair("longTitle", "Structure-long-0"),
				new BasicNameValuePair("objectives", "Structure-objectives-0"));

		HttpResponse newStructureCode = conn.execute(newStructureMethod);
		assertTrue(newStructureCode.getStatusLine().getStatusCode() == 200 || newStructureCode.getStatusLine().getStatusCode() == 201);
		String newStructureBody = EntityUtils.toString(newStructureCode.getEntity());
		CourseNodeVO structureNode = parse(newStructureBody, CourseNodeVO.class);
		assertNotNull(structureNode);
		assertNotNull(structureNode.getId());
		assertEquals(structureNode.getShortTitle(), "Structure-0");
		assertEquals(structureNode.getLongTitle(), "Structure-long-0");
		assertEquals(structureNode.getLearningObjectives(), "Structure-objectives-0");
		assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create single page
		URL pageUrl = CoursesElementsTest.class.getResource("singlepage.html");
		assertNotNull(pageUrl);
		File page = new File(pageUrl.toURI());
		
		URI newPageUri = getElementsUri(course).path("singlepage").build();
		HttpPost newPageMethod = conn.createPost(newPageUri, MediaType.APPLICATION_JSON, true);
		newPageMethod.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(page));
		entity.addPart("filename", new StringBody(page.getName()));
		entity.addPart("parentNodeId", new StringBody(course.getEditorRootNodeId()));
		entity.addPart("position", new StringBody("1"));
		entity.addPart("shortTitle", new StringBody("Single-Page-0"));
		entity.addPart("longTitle", new StringBody("Single-Page-long-0"));
		entity.addPart("objectives", new StringBody("Single-Page-objectives-0"));
		newPageMethod.setEntity(entity);
		
		HttpResponse newPageCode = conn.execute(newPageMethod);
		assertTrue(newPageCode.getStatusLine().getStatusCode() == 200 || newPageCode.getStatusLine().getStatusCode() == 201);
		String newPageBody = EntityUtils.toString(newPageCode.getEntity());
		CourseNodeVO pageNode = parse(newPageBody, CourseNodeVO.class);
		assertNotNull(pageNode);
		assertNotNull(pageNode.getId());
		assertEquals(pageNode.getShortTitle(), "Single-Page-0");
		assertEquals(pageNode.getLongTitle(), "Single-Page-long-0");
		assertEquals(pageNode.getLearningObjectives(), "Single-Page-objectives-0");
		assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a folder node
		URI newFolderUri = getElementsUri(course).path("folder").build();
		HttpPost newFolderMethod = conn.createPost(newFolderUri, MediaType.APPLICATION_JSON, true);
		
		String rule = "hasLanguage(\"de\")";
		conn.addEntity(newFolderMethod, new BasicNameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new BasicNameValuePair("position", "2"),
				new BasicNameValuePair("shortTitle", "Folder-0"),
				new BasicNameValuePair("longTitle", "Folder-long-0"),
				new BasicNameValuePair("objectives", "Folder-objectives-0"),
				new BasicNameValuePair("visibilityExpertRules", rule),
				new BasicNameValuePair("downloadExpertRules", rule),
				new BasicNameValuePair("uploadExpertRules", rule));

		HttpResponse newFolderCode = conn.execute(newFolderMethod);
		assertTrue(newFolderCode.getStatusLine().getStatusCode() == 200 || newFolderCode.getStatusLine().getStatusCode() == 201);
		String newFolderBody = EntityUtils.toString(newFolderCode.getEntity());
		CourseNodeVO folderNode = parse(newFolderBody, CourseNodeVO.class);
		assertNotNull(folderNode);
		assertNotNull(folderNode.getId());
		assertEquals(folderNode.getShortTitle(), "Folder-0");
		assertEquals(folderNode.getLongTitle(), "Folder-long-0");
		assertEquals(folderNode.getLearningObjectives(), "Folder-objectives-0");
		assertEquals(folderNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a forum node
		URI newForumUri = getElementsUri(course).path("forum").build();
		HttpPost newForumMethod = conn.createPost(newForumUri, MediaType.APPLICATION_JSON, true);
		conn.addEntity(newForumMethod, new BasicNameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new BasicNameValuePair("position", "3"),
				new BasicNameValuePair("shortTitle", "Forum-0"),
				new BasicNameValuePair("longTitle", "Forum-long-0"),
				new BasicNameValuePair("objectives", "Forum-objectives-0"));
		HttpResponse newForumCode = conn.execute(newForumMethod);
		assertTrue(newForumCode.getStatusLine().getStatusCode() == 200 || newForumCode.getStatusLine().getStatusCode() == 201);
		String newForumBody = EntityUtils.toString(newForumCode.getEntity());
		CourseNodeVO forumNode = parse(newForumBody, CourseNodeVO.class);
		assertNotNull(forumNode);
		assertNotNull(forumNode.getId());
		assertEquals(forumNode.getShortTitle(), "Forum-0");
		assertEquals(forumNode.getLongTitle(), "Forum-long-0");
		assertEquals(forumNode.getLearningObjectives(), "Forum-objectives-0");
		assertEquals(forumNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a task node
		URI newTaskUri = getElementsUri(course).path("task").build();
		HttpPost newTaskMethod = conn.createPost(newTaskUri, MediaType.APPLICATION_JSON, true);
		conn.addEntity(newTaskMethod, new BasicNameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new BasicNameValuePair("position", "4"),
				new BasicNameValuePair("shortTitle", "Task-0"),
				new BasicNameValuePair("longTitle", "Task-long-0"),
				new BasicNameValuePair("objectives", "Task-objectives-0"),
				new BasicNameValuePair("points", "25"),
				new BasicNameValuePair("text", "A very difficult test"));
		HttpResponse newTaskCode = conn.execute(newTaskMethod);
		assertTrue(newTaskCode.getStatusLine().getStatusCode() == 200 || newTaskCode.getStatusLine().getStatusCode() == 201);
		String newTaskBody = EntityUtils.toString(newTaskCode.getEntity());
		CourseNodeVO taskNode = parse(newTaskBody, CourseNodeVO.class);
		assertNotNull(taskNode);
		assertNotNull(taskNode.getId());
		assertEquals(taskNode.getShortTitle(), "Task-0");
		assertEquals(taskNode.getLongTitle(), "Task-long-0");
		assertEquals(taskNode.getLearningObjectives(), "Task-objectives-0");
		assertEquals(taskNode.getParentId(), course.getEditorRootNodeId());
		

		//create a test node
		URI newTestUri = getElementsUri(course).path("test").build();
		HttpPost newTestMethod = conn.createPost(newTestUri, MediaType.APPLICATION_JSON, true);
		conn.addEntity(newTestMethod, new BasicNameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new BasicNameValuePair("testResourceableId", course.getEditorRootNodeId()),
				new BasicNameValuePair("position", "5"),
				new BasicNameValuePair("shortTitle", "Test-0"),
				new BasicNameValuePair("longTitle", "Test-long-0"),
				new BasicNameValuePair("objectives", "Test-objectives-0"));
		HttpResponse newTestCode = conn.execute(newTestMethod);
		assertTrue(newTestCode.getStatusLine().getStatusCode() == 404);//must bind a real test
		EntityUtils.consume(newTestCode.getEntity());
		
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
		HttpPost newAssessmentMethod = conn.createPost(newAssessmentUri, MediaType.APPLICATION_JSON, true);
		conn.addEntity(newAssessmentMethod, new BasicNameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new BasicNameValuePair("position", "5"),
				new BasicNameValuePair("shortTitle", "Assessment-0"),
				new BasicNameValuePair("longTitle", "Assessment-long-0"),
				new BasicNameValuePair("objectives", "Assessment-objectives-0"));
		HttpResponse newAssessmentCode = conn.execute(newAssessmentMethod);
		assertTrue(newAssessmentCode.getStatusLine().getStatusCode() == 200 || newAssessmentCode.getStatusLine().getStatusCode() == 201);
		String newAssessmentBody = EntityUtils.toString(newAssessmentCode.getEntity());
		CourseNodeVO assessmentNode = parse(newAssessmentBody, CourseNodeVO.class);
		assertNotNull(assessmentNode);
		assertNotNull(assessmentNode.getId());
		assertEquals(assessmentNode.getShortTitle(), "Assessment-0");
		assertEquals(assessmentNode.getLongTitle(), "Assessment-long-0");
		assertEquals(assessmentNode.getLearningObjectives(), "Assessment-objectives-0");
		assertEquals(assessmentNode.getParentId(), course.getEditorRootNodeId());
		
		//create an contact node
		URI newContactUri = getElementsUri(course).path("contact").build();
		HttpPost newContactMethod = conn.createPost(newContactUri, MediaType.APPLICATION_JSON, true);
		conn.addEntity(newContactMethod, new BasicNameValuePair("parentNodeId", course.getEditorRootNodeId()),
				new BasicNameValuePair("position", "6"),
				new BasicNameValuePair("shortTitle", "Contact-0"),
				new BasicNameValuePair("longTitle", "Contact-long-0"),
				new BasicNameValuePair("objectives", "Contact-objectives-0"));

		HttpResponse newContactCode = conn.execute(newContactMethod);
		assertEquals(200, newContactCode.getStatusLine().getStatusCode());
		String newContactBody = EntityUtils.toString(newContactCode.getEntity());
		CourseNodeVO contactNode = parse(newContactBody, CourseNodeVO.class);
		assertNotNull(contactNode);
		assertNotNull(contactNode.getId());
		assertEquals(contactNode.getShortTitle(), "Contact-0");
		assertEquals(contactNode.getLongTitle(), "Contact-long-0");
		assertEquals(contactNode.getLearningObjectives(), "Contact-objectives-0");
		assertEquals(contactNode.getParentId(), course.getEditorRootNodeId());
	}
	
	@Test
	public void testCreateCoursePut() throws IOException, URISyntaxException{
		assertTrue(conn.login("administrator", "openolat"));
		
		
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", "course3").queryParam("title", "course3 long name").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		
		CourseVO course = parse(body, CourseVO.class);
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
		groupVo.setMinParticipants(new Integer(-1));
		groupVo.setMaxParticipants(new Integer(-1));
		
		URI newGroupUri = getCoursesUri().path(course.getKey().toString()).path("groups").build();
		HttpPut newGrpMethod = conn.createPut(newGroupUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(newGrpMethod, groupVo);
		
		HttpResponse newGrpCode = conn.execute(newGrpMethod);
		assertEquals(200, newGrpCode.getStatusLine().getStatusCode());
		InputStream newGrpBody = newGrpCode.getEntity().getContent();

		GroupVO group = parse(newGrpBody, GroupVO.class);
		assertNotNull(group);
		assertNotNull(group.getKey());
		
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "0").queryParam("shortTitle", "Structure-0")
			.queryParam("longTitle", "Structure-long-0")
			.queryParam("objectives", "Structure-objectives-0").build();
		HttpPut newStructureMethod = conn.createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newStructureCode = conn.execute(newStructureMethod);
		assertTrue(newStructureCode.getStatusLine().getStatusCode() == 200 || newStructureCode.getStatusLine().getStatusCode() == 201);
		String newStructureBody = EntityUtils.toString(newStructureCode.getEntity());
		CourseNodeVO structureNode = parse(newStructureBody, CourseNodeVO.class);
		assertNotNull(structureNode);
		assertNotNull(structureNode.getId());
		assertEquals(structureNode.getShortTitle(), "Structure-0");
		assertEquals(structureNode.getLongTitle(), "Structure-long-0");
		assertEquals(structureNode.getLearningObjectives(), "Structure-objectives-0");
		assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create single page
		URL pageUrl = RepositoryEntriesTest.class.getResource("singlepage.html");
		assertNotNull(pageUrl);
		File page = new File(pageUrl.toURI());
		
		URI newPageUri = getElementsUri(course).path("singlepage")
			.queryParam("parentNodeId",course.getEditorRootNodeId())
			.queryParam("position","1").queryParam("shortTitle", "Single-Page-0")
			.queryParam("longTitle", "Single-Page-long-0")
			.queryParam("objectives", "Single-Page-objectives-0").build();
		HttpPut newPageMethod = conn.createPut(newPageUri, MediaType.APPLICATION_JSON, true);
		newPageMethod.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		conn.addMultipart(newPageMethod, page.getName(), page);
		
		HttpResponse newPageCode = conn.execute(newPageMethod);
		assertTrue(newPageCode.getStatusLine().getStatusCode() == 200 || newPageCode.getStatusLine().getStatusCode() == 201);
		String newPageBody = EntityUtils.toString(newPageCode.getEntity());
		CourseNodeVO pageNode = parse(newPageBody, CourseNodeVO.class);
		assertNotNull(pageNode);
		assertNotNull(pageNode.getId());
		assertEquals(pageNode.getShortTitle(), "Single-Page-0");
		assertEquals(pageNode.getLongTitle(), "Single-Page-long-0");
		assertEquals(pageNode.getLearningObjectives(), "Single-Page-objectives-0");
		assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a folder node
		URI newFolderUri = getElementsUri(course).path("folder")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "2").queryParam("shortTitle", "Folder-0")
			.queryParam("longTitle", "Folder-long-0")
			.queryParam("objectives", "Folder-objectives-0").build();
		HttpPut newFolderMethod = conn.createPut(newFolderUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newFolderCode = conn.execute(newFolderMethod);
		assertTrue(newFolderCode.getStatusLine().getStatusCode() == 200 || newFolderCode.getStatusLine().getStatusCode() == 201);
		String newFolderBody = EntityUtils.toString(newFolderCode.getEntity());
		CourseNodeVO folderNode = parse(newFolderBody, CourseNodeVO.class);
		assertNotNull(folderNode);
		assertNotNull(folderNode.getId());
		assertEquals(folderNode.getShortTitle(), "Folder-0");
		assertEquals(folderNode.getLongTitle(), "Folder-long-0");
		assertEquals(folderNode.getLearningObjectives(), "Folder-objectives-0");
		assertEquals(folderNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a forum node
		URI newForumUri = getElementsUri(course).path("forum")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "3").queryParam("shortTitle", "Forum-0")
			.queryParam("longTitle", "Forum-long-0")
			.queryParam("objectives", "Forum-objectives-0").build();
		HttpPut newForumMethod = conn.createPut(newForumUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newForumCode = conn.execute(newForumMethod);
		assertTrue(newForumCode.getStatusLine().getStatusCode() == 200 || newForumCode.getStatusLine().getStatusCode() == 201);
		String newForumBody = EntityUtils.toString(newForumCode.getEntity());
		CourseNodeVO forumNode = parse(newForumBody, CourseNodeVO.class);
		assertNotNull(forumNode);
		assertNotNull(forumNode.getId());
		assertEquals(forumNode.getShortTitle(), "Forum-0");
		assertEquals(forumNode.getLongTitle(), "Forum-long-0");
		assertEquals(forumNode.getLearningObjectives(), "Forum-objectives-0");
		assertEquals(forumNode.getParentId(), course.getEditorRootNodeId());
		
		
		//create a task node
		URI newTaskUri = getElementsUri(course).path("task")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "4").queryParam("shortTitle", "Task-0")
			.queryParam("longTitle", "Task-long-0")
			.queryParam("objectives", "Task-objectives-0")
			.queryParam("points", "25").queryParam("text", "A very difficult test").build();
		HttpPut newTaskMethod = conn.createPut(newTaskUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newTaskCode = conn.execute(newTaskMethod);
		assertTrue(newTaskCode.getStatusLine().getStatusCode() == 200 || newTaskCode.getStatusLine().getStatusCode() == 201);
		String newTaskBody = EntityUtils.toString(newTaskCode.getEntity());
		CourseNodeVO taskNode = parse(newTaskBody, CourseNodeVO.class);
		assertNotNull(taskNode);
		assertNotNull(taskNode.getId());
		assertEquals(taskNode.getShortTitle(), "Task-0");
		assertEquals(taskNode.getLongTitle(), "Task-long-0");
		assertEquals(taskNode.getLearningObjectives(), "Task-objectives-0");
		assertEquals(taskNode.getParentId(), course.getEditorRootNodeId());
		
		//add task configuration
		URI taskConfigUri = getElementsUri(course).path("task/"+taskNode.getId()+"/configuration")
		.queryParam("enableAssignment", Boolean.FALSE)
		.queryParam("enableScoring", Boolean.TRUE)
		.queryParam("grantScoring", Boolean.TRUE)
		.queryParam("scoreMin", new Float(1.5))
		.queryParam("scoreMax", 10)
		.build();
		HttpPut taskConfigMethod = conn.createPut(taskConfigUri, MediaType.APPLICATION_JSON, true);
		HttpResponse taskConfigCode = conn.execute(taskConfigMethod);
		assertTrue(taskConfigCode.getStatusLine().getStatusCode() == 200 || taskConfigCode.getStatusLine().getStatusCode() == 201);
		EntityUtils.consume(taskConfigCode.getEntity());

		HttpGet getTaskConfig = conn.createGet(taskConfigUri, MediaType.APPLICATION_JSON, true);
		taskConfigCode = conn.execute(getTaskConfig);
		assertTrue(taskConfigCode.getStatusLine().getStatusCode() == 200 || taskConfigCode.getStatusLine().getStatusCode() == 201);
		String taskConfigBody = EntityUtils.toString(taskConfigCode.getEntity());
		TaskConfigVO taskConfig = parse(taskConfigBody, TaskConfigVO.class);
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
			.queryParam("objectives", "Assessment-objectives-0").build();
		HttpPut newAssessmentMethod = conn.createPut(newAssessmentUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newAssessmentCode = conn.execute(newAssessmentMethod);
		assertTrue(newAssessmentCode.getStatusLine().getStatusCode() == 200 || newAssessmentCode.getStatusLine().getStatusCode() == 201);
		String newAssessmentBody = EntityUtils.toString(newAssessmentCode.getEntity());
		CourseNodeVO assessmentNode = parse(newAssessmentBody, CourseNodeVO.class);
		assertNotNull(assessmentNode);
		assertNotNull(assessmentNode.getId());
		assertEquals(assessmentNode.getShortTitle(), "Assessment-0");
		assertEquals(assessmentNode.getLongTitle(), "Assessment-long-0");
		assertEquals(assessmentNode.getLearningObjectives(), "Assessment-objectives-0");
		assertEquals(assessmentNode.getParentId(), course.getEditorRootNodeId());
		
		//create an contact node
		URI newContactUri = getElementsUri(course).path("contact")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "6").queryParam("shortTitle", "Contact-0")
			.queryParam("longTitle", "Contact-long-0")
			.queryParam("objectives", "Contact-objectives-0").build();
		HttpPut newContactMethod = conn.createPut(newContactUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newContactCode = conn.execute(newContactMethod);
		assertEquals(200, newContactCode.getStatusLine().getStatusCode());
		String newContactBody = EntityUtils.toString(newContactCode.getEntity());
		CourseNodeVO contactNode = parse(newContactBody, CourseNodeVO.class);
		assertNotNull(contactNode);
		assertNotNull(contactNode.getId());
		assertEquals(contactNode.getShortTitle(), "Contact-0");
		assertEquals(contactNode.getLongTitle(), "Contact-long-0");
		assertEquals(contactNode.getLearningObjectives(), "Contact-objectives-0");
		assertEquals(contactNode.getParentId(), course.getEditorRootNodeId());
		
		//try to create an invalid enrollment node
		URI newENUri = getElementsUri(course).path("enrollment")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "7").queryParam("shortTitle", "Enrollment-0")
			.queryParam("longTitle", "Enrollment-long-0")
			.queryParam("objectives", "Enrollment-objectives-0").build();
		HttpPut newENMethod = conn.createPut(newENUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newENCode = conn.execute(newENMethod);
		assertEquals(406, newENCode.getStatusLine().getStatusCode());
		EntityUtils.consume(newENCode.getEntity());

		//create an enrollment node
		newENUri = getElementsUri(course).path("enrollment")
				.queryParam("parentNodeId", course.getEditorRootNodeId())
				.queryParam("position", "7")
				.queryParam("shortTitle", "Enrollment-0")
				.queryParam("longTitle", "Enrollment-long-0")
				.queryParam("objectives", "Enrollment-objectives-0")
				.queryParam("groups",group.getKey().toString())
				.queryParam("cancelEnabled","true").build();
		newENMethod = conn.createPut(newENUri, MediaType.APPLICATION_JSON, true);
		
		newENCode = conn.execute(newENMethod);
		assertEquals(200, newENCode.getStatusLine().getStatusCode());
		
		String newENBody = EntityUtils.toString(newENCode.getEntity());
		CourseNodeVO enNode = parse(newENBody, CourseNodeVO.class);
		assertNotNull(enNode);
		assertNotNull(enNode.getId());
		assertEquals(enNode.getShortTitle(), "Enrollment-0");
		assertEquals(enNode.getLongTitle(), "Enrollment-long-0");
		assertEquals(enNode.getLearningObjectives(), "Enrollment-objectives-0");
		assertEquals(enNode.getParentId(), course.getEditorRootNodeId());
		
		//create a test node
		URL cpUrl = CoursesElementsTest.class.getResource("qti-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		URI repoEntriesUri = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut qtiRepoMethod = conn.createPut(repoEntriesUri, MediaType.APPLICATION_JSON, true);
		qtiRepoMethod.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(cp));
		entity.addPart("filename", new StringBody("qti-demo.zip"));
		entity.addPart("resourcename", new StringBody("QTI demo"));
		entity.addPart("displayname", new StringBody("QTI demo"));
		qtiRepoMethod.setEntity(entity);
		
		HttpResponse qtiRepoCode = conn.execute(qtiRepoMethod);
		assertTrue(qtiRepoCode.getStatusLine().getStatusCode() == 200 || qtiRepoCode.getStatusLine().getStatusCode() == 201);
		
		String qtiRepoBody = EntityUtils.toString(qtiRepoCode.getEntity());
		RepositoryEntryVO newTestVO = parse(qtiRepoBody, RepositoryEntryVO.class);
		assertNotNull(newTestVO);
		
		Long key = newTestVO.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOwnerGroup());
		assertNotNull(re.getOlatResource());
		assertEquals("QTI demo", re.getDisplayname());
		
		URI newTestUri = getElementsUri(course).path("test")
			.queryParam("parentNodeId", course.getEditorRootNodeId())
			.queryParam("position", "8").queryParam("shortTitle", "Test-0")
			.queryParam("longTitle", "Test-long-0")
			.queryParam("objectives", "Test-objectives-0")
			.queryParam("testResourceableId", key).build();
		HttpPut newTestMethod = conn.createPut(newTestUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newTestCode = conn.execute(newTestMethod);
		assertTrue(newTestCode.getStatusLine().getStatusCode() == 200 || newTestCode.getStatusLine().getStatusCode() == 201);
		String newTestBody = EntityUtils.toString(newTestCode.getEntity());
		CourseNodeVO testNode = parse(newTestBody, CourseNodeVO.class);
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
			.queryParam("sequencePresentation", AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM)
			.queryParam("showNavigation", Boolean.TRUE)
			.queryParam("showQuestionTitle", Boolean.TRUE)
			.queryParam("showResultsAfterFinish", Boolean.TRUE)
			.queryParam("showResultsDependendOnDate", Boolean.TRUE)
			.queryParam("showResultsOnHomepage", key)
			.queryParam("showScoreInfo", Boolean.TRUE)
			.queryParam("showScoreProgress", Boolean.TRUE)
			.queryParam("showSectionsOnly", Boolean.TRUE)
			.queryParam("summaryPresentation", AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED)
			.queryParam("startDate", new Long(1280444400))//new Date(1280444400))
			.queryParam("endDate", new Long(1293667200))//new Date(1293667200))
			.build();
		HttpPut testConfigMethod = conn.createPut(testConfigUri, MediaType.APPLICATION_JSON, true);
		HttpResponse testConfigCode = conn.execute(testConfigMethod);
		assertTrue(testConfigCode.getStatusLine().getStatusCode() == 200 || testConfigCode.getStatusLine().getStatusCode() == 201);
		EntityUtils.consume(testConfigCode.getEntity());
		
		HttpGet getTestConfig = conn.createGet(testConfigUri, MediaType.APPLICATION_JSON, true);
		testConfigCode = conn.execute(getTestConfig);
		assertTrue(testConfigCode.getStatusLine().getStatusCode() == 200 || testConfigCode.getStatusLine().getStatusCode() == 201);
		String testConfigBody = EntityUtils.toString(testConfigCode.getEntity());
		TestConfigVO testConfig = parse(testConfigBody, TestConfigVO.class);
		assertTrue(testConfig.getNumAttempts() == 10);
		assertTrue(testConfig.getAllowCancel());
		assertTrue(testConfig.getSummeryPresentation().equals(AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED));
		
		//create a survey node
		URL newSurveyUrl = CoursesElementsTest.class.getResource("questionnaire-demo.zip");
		assertNotNull(newSurveyUrl);
		File surveyFile = new File(newSurveyUrl.toURI());

		URI repoEntriesUri2 = UriBuilder.fromUri(getContextURI()).path("repo").path("entries").build();
		HttpPut surveyRepoMethod = conn.createPut(repoEntriesUri2, MediaType.APPLICATION_JSON, true);
		surveyRepoMethod.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity surveyEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		surveyEntity.addPart("file", new FileBody(surveyFile));
		surveyEntity.addPart("filename", new StringBody("questionnaire-demo.zip"));
		surveyEntity.addPart("resourcename", new StringBody("Questionnaire demo"));
		surveyEntity.addPart("displayname", new StringBody("Questionnaire demo"));
		surveyRepoMethod.setEntity(surveyEntity);
		HttpResponse surveyRepoCode = conn.execute(surveyRepoMethod);
		assertTrue(surveyRepoCode.getStatusLine().getStatusCode() == 200 || surveyRepoCode.getStatusLine().getStatusCode() == 201);
		
		String surveyRepoBody = EntityUtils.toString(surveyRepoCode.getEntity());
		RepositoryEntryVO newSurveyVO = parse(surveyRepoBody, RepositoryEntryVO.class);
		assertNotNull(newSurveyVO);
		
		Long surveyKey = newSurveyVO.getKey();
		RepositoryEntry surveyRE = RepositoryManager.getInstance().lookupRepositoryEntry(surveyKey);
		assertNotNull(surveyRE);
		assertNotNull(surveyRE.getOwnerGroup());
		assertNotNull(surveyRE.getOlatResource());
		assertEquals("Questionnaire demo", surveyRE.getDisplayname());
		
		URI newSurveyUri = getElementsUri(course).path("survey")
		.queryParam("parentNodeId", course.getEditorRootNodeId())
		.queryParam("position", "9").queryParam("shortTitle", "Survey-0")
		.queryParam("longTitle", "Survey-long-0")
		.queryParam("objectives", "Survey-objectives-0")
		.queryParam("surveyResourceableId", surveyKey).build();
		HttpPut newSurveyMethod = conn.createPut(newSurveyUri, MediaType.APPLICATION_JSON, true);
		HttpResponse newSurveyCode = conn.execute(newSurveyMethod);
		assertTrue(newSurveyCode.getStatusLine().getStatusCode() == 200 || newSurveyCode.getStatusLine().getStatusCode() == 201);
		String newSurveyBody = EntityUtils.toString(newSurveyCode.getEntity());
		CourseNodeVO surveyNode = parse(newSurveyBody, CourseNodeVO.class);
		assertNotNull(surveyNode);
		assertNotNull(surveyNode.getId());
		assertEquals(surveyNode.getShortTitle(), "Survey-0");
		assertEquals(surveyNode.getParentId(), course.getEditorRootNodeId());
		
		//configure survey node
		URI surveykConfigUri = getElementsUri(course).path("survey/"+surveyNode.getId()+"/configuration")
		.queryParam("allowCancel", Boolean.TRUE)
		.queryParam("allowNavigation", Boolean.TRUE)
		.queryParam("allowSuspend", Boolean.TRUE)
		.queryParam("sequencePresentation", AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM)
		.queryParam("showNavigation", Boolean.TRUE)
		.queryParam("showQuestionTitle", Boolean.TRUE)
		.queryParam("showSectionsOnly", Boolean.TRUE)
		.build();
		HttpPut surveyConfigMethod = conn.createPut(surveykConfigUri, MediaType.APPLICATION_JSON, true);
		HttpResponse surveyConfigCode = conn.execute(surveyConfigMethod);
		assertTrue(surveyConfigCode.getStatusLine().getStatusCode() == 200 || surveyConfigCode.getStatusLine().getStatusCode() == 201);
		EntityUtils.consume(surveyConfigCode.getEntity());

		HttpGet getSurveyConfig = conn.createGet(surveykConfigUri, MediaType.APPLICATION_JSON, true);
		surveyConfigCode = conn.execute(getSurveyConfig);
		assertTrue(surveyConfigCode.getStatusLine().getStatusCode() == 200 || surveyConfigCode.getStatusLine().getStatusCode() == 201);
		String surveyConfigBody =EntityUtils.toString(surveyConfigCode.getEntity());
		SurveyConfigVO surveyConfig = parse(surveyConfigBody, SurveyConfigVO.class);
		assertNotNull(surveyConfig);
		assertTrue(surveyConfig.getAllowCancel());
		
		//create an external page node
		URI newTUUri = getElementsUri(course).path("externalpage")
				.queryParam("parentNodeId", course.getEditorRootNodeId())
				.queryParam("position", "10")
				.queryParam("shortTitle", "ExternalPage-0")
				.queryParam("longTitle", "ExternalPage-long-0")
				.queryParam("objectives", "ExternalPage-objectives-0")
				.queryParam("url","http://www.olat.org").build();
		HttpPut newTUMethod = conn.createPut(newTUUri, MediaType.APPLICATION_JSON, true);

		HttpResponse newTUCode = conn.execute(newTUMethod);
		assertEquals(200, newTUCode.getStatusLine().getStatusCode());
		
		String newTUBody = EntityUtils.toString(newTUCode.getEntity());
		CourseNodeVO tuNode = parse(newTUBody, CourseNodeVO.class);
		assertNotNull(tuNode);
		assertNotNull(tuNode.getId());
		assertEquals(tuNode.getShortTitle(), "ExternalPage-0");
		assertEquals(tuNode.getLongTitle(), "ExternalPage-long-0");
		assertEquals(tuNode.getLearningObjectives(), "ExternalPage-objectives-0");
		assertEquals(tuNode.getParentId(), course.getEditorRootNodeId());
		
		//summary check
		ICourse realCourse = CourseFactory.loadCourse(course.getKey());
		TreeNode realRoot = realCourse.getEditorTreeModel().getRootNode();
		assertEquals(11, realRoot.getChildCount());
		
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
		
		//survey
		child = (CourseEditorTreeNode) realRoot.getChildAt(9);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof IQSURVCourseNode);
		
		//external page
		child = (CourseEditorTreeNode) realRoot.getChildAt(10);
		childNode = child.getCourseNode();
		assertTrue(childNode instanceof TUCourseNode);
		
		//attach file to task
		child = (CourseEditorTreeNode)realRoot.getChildAt(4);
		childNode = child.getCourseNode();
		URI attachTaskFileUri = getElementsUri(course).path("task").path(childNode.getIdent()).path("file")
			.queryParam("filename", "singlepage.html").build();
		HttpPut taskFileMethod = conn.createPut(attachTaskFileUri, MediaType.APPLICATION_JSON, true);
		taskFileMethod.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		conn.addMultipart(taskFileMethod, page.getName(), page);
		
		HttpResponse taskFileCode = conn.execute(taskFileMethod);
		assertEquals(200, taskFileCode.getStatusLine().getStatusCode());
		String taskFolderPath = TACourseNode.getTaskFolderPathRelToFolderRoot(realCourse, childNode);
		OlatRootFolderImpl taskFolder = new OlatRootFolderImpl(taskFolderPath, null);
		VFSLeaf singleFile = (VFSLeaf) taskFolder.resolve("/" + "singlepage.html");
		assertNotNull(singleFile);
	}
	
	@Test
	//fxdiff FXOLAT-122: course management
	public void testUpdateRootNodeCoursePost() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", "course4").queryParam("title", "course4 long name").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		
		CourseVO course = parse(body, CourseVO.class);
		assertNotNull(course);
		assertNotNull(course.getKey());
		assertNotNull(course.getEditorRootNodeId());
		
		//update the root node
		URI rootUri = getElementsUri(course).path("structure").path(course.getEditorRootNodeId()).build();
		HttpPost updateMethod = conn.createPost(rootUri, MediaType.APPLICATION_JSON, true);
		updateMethod.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("shortTitle", new StringBody("Structure-0b"));
		entity.addPart("longTitle", new StringBody("Structure-long-0b"));
		entity.addPart("objectives", new StringBody("Structure-objectives-0b"));
		updateMethod.setEntity(entity);
		
		HttpResponse newStructureCode = conn.execute(updateMethod);
		assertTrue(newStructureCode.getStatusLine().getStatusCode() == 200 || newStructureCode.getStatusLine().getStatusCode() == 201);
		//check the response
		String newStructureBody = EntityUtils.toString(newStructureCode.getEntity());
		CourseNodeVO structureNode = parse(newStructureBody, CourseNodeVO.class);
		assertNotNull(structureNode);
		assertNotNull(structureNode.getId());
		assertEquals(structureNode.getShortTitle(), "Structure-0b");
		assertEquals(structureNode.getLongTitle(), "Structure-long-0b");
		assertEquals(structureNode.getLearningObjectives(), "Structure-objectives-0b");
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
		assertEquals(rootNode.getCourseNode().getLearningObjectives(), "Structure-objectives-0b");
	}
	
	@Test
	//fxdiff FXOLAT-122: course management
	public void testUpdateRootNodeCoursePostWithFile() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an empty course
		URI uri = getCoursesUri().queryParam("shortTitle", "course5").queryParam("title", "course5 long name").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		
		CourseVO course = parse(body, CourseVO.class);
		assertNotNull(course);
		assertNotNull(course.getKey());
		assertNotNull(course.getEditorRootNodeId());
		
		//the page
		URL pageUrl = RepositoryEntriesTest.class.getResource("singlepage.html");
		assertNotNull(pageUrl);
		File page = new File(pageUrl.toURI());
		
		//update the root node
		URI rootUri = getElementsUri(course).path("structure").path(course.getEditorRootNodeId()).build();
		HttpPost newStructureMethod = conn.createPost(rootUri, MediaType.APPLICATION_JSON, true);
		newStructureMethod.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(page));
		entity.addPart("filename", new StringBody(page.getName()));
		entity.addPart("parentNodeId", new StringBody(course.getEditorRootNodeId()));
		entity.addPart("position", new StringBody("1"));
		entity.addPart("shortTitle", new StringBody("Structure-0-with-file"));
		entity.addPart("longTitle", new StringBody("Structure-long-0-with-file"));
		entity.addPart("objectives", new StringBody("Structure-objectives-0-with-file"));
		entity.addPart("displayType", new StringBody("file"));
		newStructureMethod.setEntity(entity);
		
		
		HttpResponse newStructureCode = conn.execute(newStructureMethod);
		assertTrue(newStructureCode.getStatusLine().getStatusCode() == 200 || newStructureCode.getStatusLine().getStatusCode() == 201);
		//check the response
		String newStructureBody = EntityUtils.toString(newStructureCode.getEntity());
		CourseNodeVO structureNode = parse(newStructureBody, CourseNodeVO.class);
		assertNotNull(structureNode);
		assertNotNull(structureNode.getId());
		assertEquals(structureNode.getShortTitle(), "Structure-0-with-file");
		assertEquals(structureNode.getLongTitle(), "Structure-long-0-with-file");
		assertEquals(structureNode.getLearningObjectives(), "Structure-objectives-0-with-file");
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
		assertEquals(rootNode.getCourseNode().getLearningObjectives(), "Structure-objectives-0-with-file");
	}
	
	private UriBuilder getCoursesUri() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses");
	}
	
	private UriBuilder getElementsUri(CourseVO course) {
		return getCoursesUri().path(course.getKey().toString()).path("elements");
	}
}
