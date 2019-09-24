/**

 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi.repository.course;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.ICourse;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.course.nodes.ta.TaskController;
import org.olat.course.nodes.tu.TUConfigForm;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.repository.course.config.CustomConfigFactory;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.CourseNodeVO;
import org.olat.restapi.support.vo.elements.SurveyConfigVO;
import org.olat.restapi.support.vo.elements.TaskConfigVO;
import org.olat.restapi.support.vo.elements.TestConfigVO;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * This interface provides course building capabilities from our REST API.
 * <p>
 * Initial Date: Feb 8, 2010 Time: 3:45:50 PM<br>
 * 
 * @author cbuckley, srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses/{courseId}/elements")
public class CourseElementWebService extends AbstractCourseNodeWebService {
	private static final Logger log = Tracing.createLoggerFor(CourseElementWebService.class);
	private static final String VERSION = "0.1";
	
	/**
	 * The version of the Course Elements Web Service
	 * @response.representation.200.mediaType text/plain
	 * @response.representation.200.doc The version of this specific Web Service
	 * @response.representation.200.example 1.0
	 * @return
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	
	/**
	 * Retrieves metadata of the course node
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id
	 * @param request The HTTP request
	 * @return The persisted structure element (fully populated)
	 */
	@GET
	@Path("{nodeId}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseNode(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@Context HttpServletRequest request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthorEditor(course, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CourseNode courseNode = getParentNode(course, nodeId);
		if(courseNode == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		CourseNodeVO vo = ObjectFactory.get(courseNode);
		return Response.ok(vo).build();
	}
	
	/**
	 * This updates a Structure Element onto a given course.
	 * @response.representation.mediaType application/x-www-form-urlencoded, multipart/form-data
	 * @response.representation.doc The course node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id of this structure
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param displayType The type of display (file, toc...)
	 * @param filename The name of the file to be attached
	 * @param file The file to be attached
	 * @param request The HTTP request
	 * @return The persisted structure element (fully populated)
	 * @return The persisted structure element (fully populated)
	 */
	@POST
	@Path("structure/{nodeId}")
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateStructure(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@Context HttpServletRequest request) {
		
		InputStream in = null;
		MultipartReader reader = null;
		try {
			reader = new MultipartReader(request);
			String shortTitle = reader.getValue("shortTitle");
			String longTitle = reader.getValue("longTitle");
			String objectives = reader.getValue("objectives");
			String visibilityExpertRules = reader.getValue("visibilityExpertRules");
			String accessExpertRules = reader.getValue("accessExpertRules");
			String displayType = reader.getValue("displayType", STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
			String filename = reader.getValue("filename", "attachment");
			if(reader.getFile() != null) {
				in = new FileInputStream(reader.getFile());
			}
			CustomConfigDelegate config = new StructureFullConfig(displayType, in, filename);
			return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
		} catch (Exception e) {
			log.error("", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			MultipartReader.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}
	}
	
	/**
	 * This attaches a Structure Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The course node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this
	 *          structure
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return The persisted structure element (fully populated)
	 */
	@POST
	@Path("structure")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachStructurePostMultiparts(@PathParam("courseId") Long courseId, 
			@Context HttpServletRequest request) {

		InputStream in = null;
		MultipartReader reader = null;
		try {
			reader = new MultipartReader(request);
			String parentNodeId = reader.getValue("parentNodeId");
			Integer position = reader.getIntegerValue("position");
			String shortTitle = reader.getValue("shortTitle");
			String longTitle = reader.getValue("longTitle");
			String objectives = reader.getValue("objectives");
			String visibilityExpertRules = reader.getValue("visibilityExpertRules");
			String displayType = reader.getValue("displayType", STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
			String filename = reader.getValue("filename", "attachment");
			String accessExpertRules = reader.getValue("accessExpertRules");
			if(reader.getFile() != null) {
				in = new FileInputStream(reader.getFile());
			}
			CustomConfigDelegate config = new StructureFullConfig(displayType, in, filename);
			return attach(courseId, parentNodeId, "st", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
		} catch (Exception e) {
			log.error("", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			MultipartReader.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}
	}
	
	/**
	 * This attaches a Structure Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The course node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this
	 *          structure
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return The persisted structure element (fully populated)
	 */
	@PUT
	@Path("structure")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachStructure(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("displayType") @DefaultValue(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC) String displayType,
			@Context HttpServletRequest request) {
		//fxdiff FXOLAT-122: course management
		CustomConfigDelegate config = new StructureFullConfig(displayType, null, null);
		return attach(courseId, parentNodeId, "st", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This updates a Single Page Element onto a given course.
	 * @response.representation.mediaType multipart/form-data
	 * @response.representation.doc The content of the single page
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc the course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id of this single page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param filename The single page file name
	 * @param file The file input stream
	 * @param request The HTTP request
	 * @return The persisted Single Page Element(fully populated)
	 */
	@POST
	@Path("singlepage/{nodeId}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateSinglePage(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@Context HttpServletRequest request) {

		InputStream in = null;
		MultipartReader reader = null;
		try {
			reader = new MultipartReader(request);
			String shortTitle = reader.getValue("shortTitle");
			String longTitle = reader.getValue("longTitle");
			String objectives = reader.getValue("objectives");
			String visibilityExpertRules = reader.getValue("visibilityExpertRules");
			String filename = reader.getValue("filename", "attachment");
			String accessExpertRules = reader.getValue("accessExpertRules");
			in = new FileInputStream(reader.getFile());
			SinglePageCustomConfig config = new SinglePageCustomConfig(in, filename);
			return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
		} catch (Exception e) {
			log.error("", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			MultipartReader.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}
	}
	
	/**
	 * This attaches a Single Page Element onto a given course. The element will
	 * be inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType multipart/form-data
	 * @response.representation.doc The content of the single page
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable id
	 * @param parentNodeId The node's id which will be the parent of this single
	 *          page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param filename The single page file name
	 * @param file The file input stream
	 * @param request The HTTP request
	 * @return The persisted Single Page Element(fully populated)
	 */
	@POST
	@Path("singlepage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachSinglePagePost(@PathParam("courseId") Long courseId, @Context HttpServletRequest request) {
		return attachSinglePage(courseId, request);
	}

	/**
	 * This attaches a Single Page Element onto a given course. The element will
	 * be inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType multipart/form-data
	 * @response.representation.doc The content of the single page
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc the course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this single
	 *          page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param filename The single page file name
	 * @param file The file input stream
	 * @param request The HTTP request
	 * @return The persisted Single Page Element(fully populated)
	 */
	@PUT
	@Path("singlepage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachSinglePage(@PathParam("courseId") Long courseId, @Context HttpServletRequest request) {
		InputStream in = null;
		MultipartReader reader = null;
		try {
			reader = new MultipartReader(request);
			String parentNodeId = reader.getValue("parentNodeId");
			Integer position = reader.getIntegerValue("position");
			String shortTitle = reader.getValue("shortTitle");
			String longTitle = reader.getValue("longTitle");
			String objectives = reader.getValue("objectives");
			String visibilityExpertRules = reader.getValue("visibilityExpertRules");
			String accessExpertRules = reader.getValue("accessExpertRules");
			String filename = reader.getValue("filename", "attachment");
			in = new FileInputStream(reader.getFile());
			SinglePageCustomConfig config = new SinglePageCustomConfig(in, filename);
			return attach(courseId, parentNodeId, "sp", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
		} catch (Exception e) {
			log.error("", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			MultipartReader.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}
	}
	
	/**
	 * This attaches a Single Page Element onto a given course. The element will
	 * be inserted underneath the supplied parentNodeId. The page is found in the
	 * resource folder of the course.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The content of the single page
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc the course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this single
	 *          page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param filename The single page file name
	 * @param path The path of the file
	 * @param request The HTTP request
	 * @return The persisted Single Page Element(fully populated)
	 */
	@POST
	@Path("singlepage")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachSinglePagePost(@PathParam("courseId") Long courseId, @FormParam("parentNodeId") String parentNodeId,
			@FormParam("position") Integer position, @FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("filename") String filename, @FormParam("path") String path, @Context HttpServletRequest request) {
		return attachSinglePage(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, filename, path, request);
	}
	
	/**
	 * This attaches a Single Page Element onto a given course. The element will
	 * be inserted underneath the supplied parentNodeId. The page is found in the
	 * resource folder of the course.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The content of the single page
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc the course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this single
	 *          page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param filename The single page file name
	 * @param path The path of the file
	 * @param request The HTTP request
	 * @return The persisted Single Page Element(fully populated)
	 */
	@PUT
	@Path("singlepage")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachSinglePage(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("filename") String filename, @QueryParam("path") String path, @Context HttpServletRequest request) {	
		SinglePageCustomConfig config = new SinglePageCustomConfig(path, filename);
		return attach(courseId, parentNodeId, "sp", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This updates a Task Element onto a given course.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The task node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable id
	 * @param nodeId The node's id of this task
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param text The task node text
	 * @param points The task node's possible points
	 * @param request The HTTP request
	 * @return The persisted task element (fully populated)
	 */
	@POST
	@Path("task/{nodeId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	//fxdiff FXOLAT-122: course management
	public Response updateTask(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle, 
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("text") String text, @FormParam("points") Float points,
			@Context HttpServletRequest request) {
		TaskCustomConfig config = new TaskCustomConfig(points, text);
		return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This attaches a Task Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The task node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable id
	 * @param parentNodeId The node's id which will be the parent of this task
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param text The task node text
	 * @param points The task node's possible points
	 * @param request The HTTP request
	 * @return The persisted task element (fully populated)
	 */
	@POST
	@Path("task")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachTaskPost(@PathParam("courseId") Long courseId, @FormParam("parentNodeId") String parentNodeId,
			@FormParam("position") Integer position, @FormParam("shortTitle") @DefaultValue("undefined") String shortTitle, 
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("text") String text, @FormParam("points") Float points,
			@Context HttpServletRequest request) {
		return attachTask(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, text, points, request);
	}

	/**
	 * This attaches a Task Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The task node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable id
	 * @param parentNodeId The node's id which will be the parent of this task
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param text The task node text
	 * @param points The task node's possible points
	 * @param request The HTTP request
	 * @return The persisted task element (fully populated)
	 */
	@PUT
	@Path("task")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachTask(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("text") String text, @QueryParam("points") Float points, 
			@Context HttpServletRequest request) {
		TaskCustomConfig config = new TaskCustomConfig(points, text);
		return attach(courseId, parentNodeId, "ta", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This updates a Test Element onto a given course.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The course node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The test node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course, parentNode or test not found
	 * @param courseId The course resourceable id
	 * @param nodeId The node's id of this test
	 * @param testResourceableId The test node's id which is retorned in the
	 *          response of the import test resource
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param testResourceableId The repository entry key of the test
	 * @param request The HTTP request
	 * @return The persisted test element (fully populated)
	 */
	@POST
	@Path("test/{nodeId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	//fxdiff FXOLAT-122: course management
	public Response updateTest(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle, 
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("testResourceableId") Long testResourceableId,  @Context HttpServletRequest request) {
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry testRepoEntry = rm.lookupRepositoryEntry(testResourceableId);
		if(testRepoEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		CustomConfigDelegate config = CustomConfigFactory.getTestCustomConfig(testRepoEntry);
		return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This attaches a Test Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The course node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The test node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course, parentNode or test not found
	 * @param courseId The course resourceable id
	 * @param parentNodeId The node's id which will be the parent of this test
	 * @param testResourceableId The test node's id which is retorned in the
	 *          response of the import test resource
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return The persisted test element (fully populated)
	 */
	@POST
	@Path("test")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachTestPost(@PathParam("courseId") Long courseId, @FormParam("parentNodeId") String parentNodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle, @FormParam("position") Integer position,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("testResourceableId") Long testResourceableId,  @Context HttpServletRequest request) {
		return attachTest(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, testResourceableId, request);
	}

	/**
	 * This attaches a Test Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc the course node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc the test node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc course, parentNode or test not found
	 * @param courseId The course resourceable id
	 * @param parentNodeId The node's id which will be the parent of this test
	 * @param testResourceableId The test node's id which is retorned in the
	 *          response of the import test resource
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return The persisted test element (fully populated)
	 */
	@PUT
	@Path("test")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachTest(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle, 
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("testResourceableId") Long testResourceableId, @Context HttpServletRequest request) {
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry testRepoEntry = rm.lookupRepositoryEntry(testResourceableId);
		if(testRepoEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		CustomConfigDelegate config = CustomConfigFactory.getTestCustomConfig(testRepoEntry);
		return attach(courseId, parentNodeId, "iqtest", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Updates an assessment building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id of this assessment
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("assessment/{nodeId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	//fxdiff FXOLAT-122: course management
	public Response updateAssessment(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@Context HttpServletRequest request) {
		AssessmentCustomConfig config = new AssessmentCustomConfig();
		return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Attaches an assessment building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("assessment")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachAssessmentPost(@PathParam("courseId") Long courseId, @FormParam("parentNodeId") String parentNodeId,
			@FormParam("position") Integer position, @FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@Context HttpServletRequest request) {
		return attachAssessment(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, request);
	}
	
	/**
	 * Attaches an assessment building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("assessment")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachAssessment(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@Context HttpServletRequest request) {
		AssessmentCustomConfig config = new AssessmentCustomConfig();
		return attach(courseId, parentNodeId, "ms", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Attaches an wiki building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id which of this wiki
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param wikiResourceableId The repository entry key of the wiki
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("wiki/{nodeId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	//fxdiff FXOLAT-122: course management
	public Response updateWiki(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("wikiResourceableId") Long wikiResourceableId, @Context HttpServletRequest request) {
		RepositoryEntry wikiRepoEntry = null;
		if(wikiResourceableId != null) {
			RepositoryManager rm = RepositoryManager.getInstance();
			wikiRepoEntry = rm.lookupRepositoryEntry(wikiResourceableId);
			if(wikiRepoEntry == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
		}
		WikiCustomConfig config = new WikiCustomConfig(wikiRepoEntry);
		return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Attaches an wiki building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("wiki")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachWikiPost(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("wikiResourceableId") Long wikiResourceableId, @Context HttpServletRequest request) {
		return attachWiki(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, wikiResourceableId, request);
	}
	
	/**
	 * Attaches an wiki building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("wiki")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachWiki(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("wikiResourceableId") Long wikiResourceableId, @Context HttpServletRequest request) {
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry wikiRepoEntry = rm.lookupRepositoryEntry(wikiResourceableId);
		if(wikiRepoEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		WikiCustomConfig config = new WikiCustomConfig(wikiRepoEntry);
		return attach(courseId, parentNodeId, "wiki", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Update an blog building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id of this blog
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param blogResourceableId The softkey of the blog resourceable (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("blog/{nodeId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	//fxdiff FXOLAT-122: course management
	public Response updateBlog(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("repoEntry") Long blogResourceableId, @Context HttpServletRequest request) {
		RepositoryEntry blogRepoEntry = null;
		if(blogResourceableId != null) {
			RepositoryManager rm = RepositoryManager.getInstance();
			blogRepoEntry = rm.lookupRepositoryEntry(blogResourceableId);
			if(blogRepoEntry == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
		}
		BlogCustomConfig config = new BlogCustomConfig(blogRepoEntry);
		return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Attaches an blog building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param blogResourceableId The softkey of the blog resourceable (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("blog")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachBlogPost(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("repoEntry") Long blogResourceableId, @Context HttpServletRequest request) {
		return attachBlog(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, blogResourceableId, request);
	}
	
	/**
	 * Attaches an blog building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param blogResourceableId The softkey of the blog resourceable (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("blog")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachBlog(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("repoEntry") Long blogResourceableId, @Context HttpServletRequest request) {
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry blogRepoEntry = rm.lookupRepositoryEntry(blogResourceableId);
		if(blogRepoEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		BlogCustomConfig config = new BlogCustomConfig(blogRepoEntry);
		
		return attach(courseId, parentNodeId, "blog", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Attaches an survey building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("survey/{nodeId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachSurveyPost(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("surveyResourceableId") Long surveyResourceableId, @Context HttpServletRequest request) {
		
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry surveyRepoEntry = rm.lookupRepositoryEntry(surveyResourceableId);
		if(surveyRepoEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		CustomConfigDelegate config = CustomConfigFactory.getSurveyCustomConfig(surveyRepoEntry);
		return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Attaches an survey building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("survey")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachSurveyPost(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("surveyResourceableId") Long surveyResourceableId, @Context HttpServletRequest request) {
		return attachSurvey(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, surveyResourceableId, request);
	}
	
	/**
	 * Attaches an survey building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The assessment node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("survey")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachSurvey(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("surveyResourceableId") Long surveyResourceableId, @Context HttpServletRequest request) {
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry surveyRepoEntry = rm.lookupRepositoryEntry(surveyResourceableId);
		if(surveyRepoEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		CustomConfigDelegate config = CustomConfigFactory.getSurveyCustomConfig(surveyRepoEntry);
		
		return attach(courseId, parentNodeId, "iqsurv", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Update an external page building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The external page node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id of this external page
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param url The URL of the external page
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("externalpage/{nodeId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	//fxdiff FXOLAT-122: course management
	public Response updateExternalPage(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("url") String url, @Context HttpServletRequest request) {
		URL externalUrl = null;
		if(url != null) {
			try {
				externalUrl = new URL(url);
			} catch (MalformedURLException e) {
				return Response.serverError().status(Status.CONFLICT).build();
			}
		}
		ExternalPageCustomConfig config = new ExternalPageCustomConfig(externalUrl);
		return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Attaches an external page building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The external page node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param url The URL of the external page
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("externalpage")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachExternalPagePost(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("url") String url, @Context HttpServletRequest request) {
		return attachExternalPage(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, url, request);
	}
	
	/**
	 * Attaches an external page building block.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The external page node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @response.representation.409.doc The given URL is not valid
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this assessment
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param url The URL of the external page
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("externalpage")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachExternalPage(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("url") String url, @Context HttpServletRequest request) {
		URL externalUrl = null;
		try {
			externalUrl = new URL(url);
		} catch (MalformedURLException e) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		ExternalPageCustomConfig config = new ExternalPageCustomConfig(externalUrl);
		
		return attach(courseId, parentNodeId, "tu", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This attaches a Task file onto a given task element.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The task node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable id
	 * @param nodeId The node's id which will be the parent of this task file
	 * @param request The HTTP request
	 * @return The persisted task element (fully populated)
	 */
	@POST
	@Path("task/{nodeId}/file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachTaskFilePost(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@Context HttpServletRequest request) {
		return attachTaskFile(courseId, nodeId, request);
	}

	/**
	 * This attaches a Task file onto a given task element.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The task node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @response.representation.406.doc The course node is not of type task
	 * @param courseId The course resourceable id
	 * @param nodeId The node's id which will be the parent of this task file
	 * @param request The HTTP request
	 * @return The persisted task element (fully populated)
	 */
	@PUT
	@Path("task/{nodeId}/file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachTaskFile(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@Context HttpServletRequest request) {
			ICourse course = CoursesWebService.loadCourse(courseId);
	CourseNode node = getParentNode(course, nodeId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(node == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(!(node instanceof TACourseNode)) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		if (!isAuthorEditor(course, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		InputStream in = null;
		MultipartReader reader = null;
		try {
			reader = new MultipartReader(request);
			String filename = reader.getValue("filename", "task");
			String taskFolderPath = TACourseNode.getTaskFolderPathRelToFolderRoot(course, node);
			VFSContainer taskFolder = VFSManager.olatRootContainer(taskFolderPath, null);
			VFSLeaf singleFile = (VFSLeaf) taskFolder.resolve("/" + filename);
			if (singleFile == null) {
				singleFile = taskFolder.createChildLeaf("/" + filename);
			}
			File file = reader.getFile();
			if(file != null) {
				in = new FileInputStream(file);
				OutputStream out = singleFile.getOutputStream(false);
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(out);
			} else {
				return Response.status(Status.NOT_ACCEPTABLE).build();
			}
		} catch (Exception e) {
			log.error("", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			MultipartReader.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}
		
		return Response.ok().build();
	}
	
	/**
	 * This attaches the run-time configuration onto a given task element.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The task node configuration
	 * @response.representation.200.qname {http://www.example.com}surveyConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The task node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or task node not found
	 * @response.representation.406.doc The call is not applicable to task course node
	 * @response.representation.409.doc The configuration is not valid 
	 * @param courseId
	 * @param nodeId
	 * @param enableAssignment
	 * @param taskAssignmentType
	 * @param taskAssignmentText
	 * @param enableTaskPreview
	 * @param enableTaskDeselect
	 * @param onlyOneUserPerTask
	 * @param enableDropbox
	 * @param enableDropboxConfirmationMail
	 * @param dropboxConfirmationText
	 * @param enableReturnbox
	 * @param enableScoring
	 * @param grantScoring
	 * @param scoreMin
	 * @param scoreMax
	 * @param grantPassing
	 * @param scorePassingThreshold
	 * @param enableCommentField
	 * @param commentForUser
	 * @param commentForCoaches
	 * @param enableSolution
	 * @param accessExpertRuleTask
	 * @param accessExpertRuleDropbox
	 * @param accessExpertRuleReturnbox
	 * @param accessExpertRuleScoring
	 * @param accessExpertRuleSolution
	 * @param request
	 * @return
	 */
	@POST
	@Path("task/{nodeId}/configuration")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addTaskConfigurationPost(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@QueryParam("enableAssignment") Boolean enableAssignment,
			@QueryParam("taskAssignmentType") String taskAssignmentType,
			@QueryParam("taskAssignmentText") String taskAssignmentText,
			@QueryParam("enableTaskPreview") Boolean enableTaskPreview,
			@QueryParam("enableTaskDeselect") Boolean enableTaskDeselect,
			@QueryParam("onlyOneUserPerTask") Boolean onyOneUserPerTask,
			@QueryParam("enableDropbox") Boolean enableDropbox,
			@QueryParam("enableDropboxConfirmationMail") Boolean enableDropboxConfirmationMail,
			@QueryParam("dropboxConfirmationText") String dropboxConfirmationText,
			@QueryParam("enableReturnbox") Boolean enableReturnbox,
			@QueryParam("enableScoring") Boolean enableScoring,
			@QueryParam("grantScoring") Boolean grantScoring,
			@QueryParam("scoreMin") Float scoreMin,
			@QueryParam("scoreMax") Float scoreMax,
			@QueryParam("grantPassing") Boolean grantPassing,
			@QueryParam("scorePassingThreshold") Float scorePassingThreshold,
			@QueryParam("enableCommentField") Boolean enableCommentField,
			@QueryParam("commentForUser") String commentForUser,
			@QueryParam("commentForCoaches") String commentForCoaches,
			@QueryParam("enableSolution") Boolean enableSolution,
			@QueryParam("accessExpertRuleTask") String accessExpertRuleTask,
			@QueryParam("accessExpertRuleDropbox") String accessExpertRuleDropbox,
			@QueryParam("accessExpertRuleReturnbox") String accessExpertRuleReturnbox,
			@QueryParam("accessExpertRuleScoring") String accessExpertRuleScoring,
			@QueryParam("accessExpertRuleSolution") String accessExpertRuleSolution,
			@Context HttpServletRequest request) {
		
		return addTaskConfiguration(courseId, nodeId, enableAssignment, taskAssignmentType, taskAssignmentText,
				enableTaskPreview, enableTaskDeselect, onyOneUserPerTask, enableDropbox, enableDropboxConfirmationMail,
				dropboxConfirmationText, enableReturnbox, enableScoring, grantScoring, scoreMin, scoreMax, grantPassing,
				scorePassingThreshold, enableCommentField, commentForUser, commentForCoaches, enableSolution, accessExpertRuleTask,
				accessExpertRuleDropbox, accessExpertRuleReturnbox, accessExpertRuleScoring, accessExpertRuleSolution, request);
	}
	
	/**
	 * This attaches the run-time configuration onto a given task element.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The task node configuration
	 * @response.representation.200.qname {http://www.example.com}surveyConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The task node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or task node not found
	 * @response.representation.406.doc The call is not applicable to task course node
	 * @response.representation.409.doc The configuration is not valid 
	 * @param courseId
	 * @param nodeId
	 * @param enableAssignment
	 * @param taskAssignmentType
	 * @param taskAssignmentText
	 * @param enableTaskPreview
	 * @param enableTaskDeselect
	 * @param onlyOneUserPerTask
	 * @param enableDropbox
	 * @param enableDropboxConfirmationMail
	 * @param dropboxConfirmationText
	 * @param enableReturnbox
	 * @param enableScoring
	 * @param grantScoring
	 * @param scoreMin
	 * @param scoreMax
	 * @param grantPassing
	 * @param scorePassingThreshold
	 * @param enableCommentField
	 * @param commentForUser
	 * @param commentForCoaches
	 * @param enableSolution
	 * @param accessExpertRuleTask
	 * @param accessExpertRuleDropbox
	 * @param accessExpertRuleReturnbox
	 * @param accessExpertRuleScoring
	 * @param accessExpertRuleSolution
	 * @param request
	 * @return
	 */
	@PUT
	@Path("task/{nodeId}/configuration")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addTaskConfiguration(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@QueryParam("enableAssignment") Boolean enableAssignment,
			@QueryParam("taskAssignmentType") String taskAssignmentType,
			@QueryParam("taskAssignmentText") String taskAssignmentText,
			@QueryParam("enableTaskPreview") Boolean enableTaskPreview,
			@QueryParam("enableTaskDeselect") Boolean enableTaskDeselect,
			@QueryParam("onlyOneUserPerTask") Boolean onlyOneUserPerTask,
			@QueryParam("enableDropbox") Boolean enableDropbox,
			@QueryParam("enableDropboxConfirmationMail") Boolean enableDropboxConfirmationMail,
			@QueryParam("dropboxConfirmationText") String dropboxConfirmationText,
			@QueryParam("enableReturnbox") Boolean enableReturnbox,
			@QueryParam("enableScoring") Boolean enableScoring,
			@QueryParam("grantScoring") Boolean grantScoring,
			@QueryParam("scoreMin") Float scoreMin,
			@QueryParam("scoreMax") Float scoreMax,
			@QueryParam("grantPassing") Boolean grantPassing,
			@QueryParam("scorePassingThreshold") Float scorePassingThreshold,
			@QueryParam("enableCommentField") Boolean enableCommentField,
			@QueryParam("commentForUser") String commentForUser,
			@QueryParam("commentForCoaches") String commentForCoaches,
			@QueryParam("enableSolution") Boolean enableSolution,
			@QueryParam("accessExpertRuleTask") String accessExpertRuleTask,
			@QueryParam("accessExpertRuleDropbox") String accessExpertRuleDropbox,
			@QueryParam("accessExpertRuleReturnbox") String accessExpertRuleReturnbox,
			@QueryParam("accessExpertRuleScoring") String accessExpertRuleScoring,
			@QueryParam("accessExpertRuleSolution") String accessExpertRuleSolution,
			@Context HttpServletRequest request) {
		
		TaskFullConfig config = new TaskFullConfig(enableAssignment, taskAssignmentType, taskAssignmentText, enableTaskPreview,
				enableTaskDeselect, onlyOneUserPerTask, enableDropbox, enableDropboxConfirmationMail, dropboxConfirmationText, enableReturnbox,
				enableScoring, grantScoring, scoreMin, scoreMax, grantPassing, scorePassingThreshold, enableCommentField, commentForUser,
				commentForCoaches, enableSolution, accessExpertRuleTask, accessExpertRuleDropbox, accessExpertRuleReturnbox,
				accessExpertRuleScoring, accessExpertRuleSolution);
		
		return attachNodeConfig(courseId, nodeId, config, request);
	}
	
	/**
	 * Retrieves configuration of the task course node
	 * @response.representation.200.qname {http://www.example.com}surveyConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or task node not found
	 * @param courseId
	 * @param nodeId
	 * @return the task course node configuration
	 */
	@GET
	@Path("task/{nodeId}/configuration")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTaskConfiguration(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@Context HttpServletRequest httpRequest) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthorEditor(course, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		TaskConfigVO config = new TaskConfigVO();
		CourseNode courseNode = getParentNode(course, nodeId);
		ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
		//build configuration with fallback to default values
		Boolean isAssignmentEnabled = (Boolean)moduleConfig.get(TACourseNode.CONF_TASK_ENABLED);
		config.setIsAssignmentEnabled(isAssignmentEnabled == null ? Boolean.TRUE : isAssignmentEnabled);
		String taskAssignmentType = moduleConfig.getStringValue(TACourseNode.CONF_TASK_TYPE);
		config.setTaskAssignmentType(taskAssignmentType == null ? TaskController.TYPE_MANUAL : taskAssignmentType);
		String taskAssignmentText = moduleConfig.getStringValue(TACourseNode.CONF_TASK_TEXT);
		config.setTaskAssignmentText(taskAssignmentText == null ? "" : taskAssignmentText);
		Boolean isTaskPreviewEnabled = moduleConfig.get(TACourseNode.CONF_TASK_PREVIEW) == null ? Boolean.FALSE : moduleConfig.getBooleanEntry(TACourseNode.CONF_TASK_PREVIEW);
		config.setIsTaskPreviewEnabled(isTaskPreviewEnabled);
		Boolean isTaskDeselectEnabled = moduleConfig.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT);
		config.setIsTaskDeselectEnabled(isTaskDeselectEnabled == null ? Boolean.FALSE : isTaskDeselectEnabled);
		Boolean onlyOneUserPerTask = (Boolean)moduleConfig.get(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT);
		config.setOnlyOneUserPerTask(onlyOneUserPerTask == null ? Boolean.TRUE : onlyOneUserPerTask);
		Boolean isDropboxEnabled = (Boolean)moduleConfig.get(TACourseNode.CONF_DROPBOX_ENABLED);
		config.setIsDropboxEnabled(isDropboxEnabled == null ? Boolean.TRUE : isDropboxEnabled);
		Boolean isDropboxConfirmationMailEnabled = (Boolean)moduleConfig.get(TACourseNode.CONF_DROPBOX_ENABLEMAIL);
		config.setIsDropboxConfirmationMailEnabled(isDropboxConfirmationMailEnabled == null ? Boolean.FALSE : isDropboxConfirmationMailEnabled);
		String dropboxConfirmationText = moduleConfig.getStringValue(TACourseNode.CONF_DROPBOX_CONFIRMATION);
		config.setDropboxConfirmationText(dropboxConfirmationText == null ? "" : dropboxConfirmationText);
		Boolean isReturnboxEnabled = (Boolean)moduleConfig.get(TACourseNode.CONF_RETURNBOX_ENABLED);
		config.setIsReturnboxEnabled(isReturnboxEnabled == null ? Boolean.TRUE : isReturnboxEnabled);
		Boolean isScoringEnabled = (Boolean)moduleConfig.get(TACourseNode.CONF_SCORING_ENABLED);
		config.setIsScoringEnabled(isScoringEnabled == null ? Boolean.TRUE : isScoringEnabled);
		Boolean isScoringGranted = (Boolean)moduleConfig.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		config.setIsScoringGranted(isScoringGranted == null ? Boolean.FALSE : isScoringGranted);
		Float minScore = (Float)moduleConfig.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		config.setMinScore(minScore);
		Float maxScore = (Float)moduleConfig.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		config.setMaxScore(maxScore);
		Boolean isPassingGranted = (Boolean)moduleConfig.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		config.setIsPassingGranted(isPassingGranted == null ? Boolean.FALSE : isPassingGranted);
		Float passingScoreThreshold = (Float)moduleConfig.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		config.setPassingScoreThreshold(passingScoreThreshold);
		Boolean hasCommentField = (Boolean)moduleConfig.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
		config.setHasCommentField(hasCommentField == null ? Boolean.FALSE : hasCommentField);
		String commentForUser = moduleConfig.getStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		config.setCommentForUser(commentForUser == null ? "" : commentForUser);
		String commentForCoaches = moduleConfig.getStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		config.setCommentForCoaches(commentForCoaches == null ? "" : commentForCoaches);
		Boolean isSolutionEnabled = (Boolean)moduleConfig.get(TACourseNode.CONF_SOLUTION_ENABLED);
		config.setIsSolutionEnabled(isSolutionEnabled == null ? Boolean.TRUE : isSolutionEnabled);
		//get the conditions
		List<ConditionExpression> lstConditions = courseNode.getConditionExpressions();
		for(ConditionExpression cond : lstConditions) {
			String id = cond.getId();
			String expression = cond.getExptressionString();
			if(id.equals(TACourseNode.ACCESS_TASK))
				config.setConditionTask(expression);
			else if(id.equals("drop")) //TACourseNode uses "drop" instead the static ACCESS_DROPBOX, very bad!
				config.setConditionDropbox(expression);
			else if(id.equals(TACourseNode.ACCESS_RETURNBOX))
				config.setConditionReturnbox(expression);
			else if(id.equals(TACourseNode.ACCESS_SCORING))
				config.setConditionScoring(expression);
			else if(id.equals(TACourseNode.ACCESS_SOLUTION))
				config.setConditionSolution(expression);
		}
		
		return Response.ok(config).build();
	}
	
	/**
	 * This attaches the run-time configuration onto a given survey element.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The test node configuration
	 * @response.representation.200.qname {http://www.example.com}surveyConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The survey node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or survey node not found
	 * @response.representation.406.doc The call is not applicable to survey course node
	 * @response.representation.409.doc The configuration is not valid 
	 * @param courseId
	 * @param nodeId
	 * @param allowCancel
	 * @param allowNavigation
	 * @param allowSuspend
	 * @param sequencePresentation
	 * @param showNavigation
	 * @param showQuestionTitle
	 * @param showSectionsOnly
	 * @param request
	 * @return
	 */
	@POST
	@Path("survey/{nodeId}/configuration")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addSurveyConfigurationPost(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@QueryParam("allowCancel") @DefaultValue("false") Boolean allowCancel,
			@QueryParam("allowNavigation") @DefaultValue("false") Boolean allowNavigation,
			@QueryParam("allowSuspend") @DefaultValue("false") Boolean allowSuspend,
			@QueryParam("sequencePresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM) String sequencePresentation,
			@QueryParam("showNavigation") @DefaultValue("true") Boolean showNavigation,
			@QueryParam("showQuestionTitle") @DefaultValue("true") Boolean showQuestionTitle,
			@QueryParam("showSectionsOnly") @DefaultValue("false") Boolean showSectionsOnly,
			@Context HttpServletRequest request) {
		
		return addSurveyConfiguration(courseId, nodeId, allowCancel, allowNavigation, allowSuspend, sequencePresentation, showNavigation, showQuestionTitle, showSectionsOnly, request);
	}
	
	/**
	 * This attaches the run-time configuration onto a given survey element.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The test node configuration
	 * @response.representation.200.qname {http://www.example.com}surveyConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The survey node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or survey node not found
	 * @response.representation.406.doc The call is not applicable to survey course node
	 * @response.representation.409.doc The configuration is not valid 
	 * @param courseId
	 * @param nodeId
	 * @param allowCancel
	 * @param allowNavigation
	 * @param allowSuspend
	 * @param sequencePresentation
	 * @param showNavigation
	 * @param showQuestionTitle
	 * @param showSectionsOnly
	 * @param request
	 * @return
	 */
	@PUT
	@Path("survey/{nodeId}/configuration")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addSurveyConfiguration(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@QueryParam("allowCancel") @DefaultValue("false") Boolean allowCancel,
			@QueryParam("allowNavigation") @DefaultValue("false") Boolean allowNavigation,
			@QueryParam("allowSuspend") @DefaultValue("false") Boolean allowSuspend,
			@QueryParam("sequencePresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM) String sequencePresentation,
			@QueryParam("showNavigation") @DefaultValue("true") Boolean showNavigation,
			@QueryParam("showQuestionTitle") @DefaultValue("true") Boolean showQuestionTitle,
			@QueryParam("showSectionsOnly") @DefaultValue("false") Boolean showSectionsOnly,
			@Context HttpServletRequest request) {
		
		SurveyFullConfig config = new SurveyFullConfig(allowCancel, allowNavigation, allowSuspend, sequencePresentation, showNavigation,
				showQuestionTitle, showSectionsOnly);
		
		return attachNodeConfig(courseId, nodeId, config, request);
	}
	
	/**
	 * Retrieves configuration of the survey course node
	 * @response.representation.200.qname {http://www.example.com}surveyConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or survey node not found
	 * @param courseId
	 * @param nodeId
	 * @return survey course node configuration
	 */
	@GET
	@Path("survey/{nodeId}/configuration")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getSurveyConfiguration(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@Context HttpServletRequest httpRequest) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthorEditor(course, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		SurveyConfigVO config = new SurveyConfigVO();
		CourseNode courseNode = getParentNode(course, nodeId);
		ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
		//build configuration with fallback to default values
		Boolean allowCancel = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_ENABLECANCEL);
		config.setAllowCancel(allowCancel == null ? false : allowCancel);
		Boolean allowNavi = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_ENABLEMENU);
		config.setAllowNavigation(allowNavi == null ? false : allowNavi);
		Boolean allowSuspend = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_ENABLESUSPEND);
		config.setAllowSuspend(allowSuspend == null ? false : allowSuspend);
		config.setSequencePresentation(moduleConfig.getStringValue(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM));
		Boolean showNavi = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_DISPLAYMENU);
		config.setShowNavigation(showNavi == null ? true : showNavi);
		Boolean showQuestionTitle = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_QUESTIONTITLE);
		config.setShowQuestionTitle(showQuestionTitle == null ? true : showQuestionTitle);
		Boolean showSectionsOnly = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_RENDERMENUOPTION);
		config.setShowSectionsOnly(showSectionsOnly == null ? false : showSectionsOnly);
		
		return Response.ok(config).build();
	}
	
	/**
	 * This attaches the run-time configuration onto a given test element.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The test node configuration
	 * @response.representation.200.qname {http://www.example.com}testConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The test node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or test node not found
	 * @response.representation.406.doc The call is not applicable to test course node
	 * @response.representation.409.doc The configuration is not valid 
	 * @param courseId
	 * @param nodeId
	 * @param allowCancel
	 * @param allowNavigation
	 * @param allowSuspend
	 * @param numAttempts
	 * @param sequencePresentation
	 * @param showNavigation
	 * @param showQuestionTitle
	 * @param showResultsAfterFinish
	 * @param showResultsDependendOnDate
	 * @param showResultsOnHomepage
	 * @param showScoreInfo
	 * @param showQuestionProgress
	 * @param showScoreProgress
	 * @param showSectionsOnly
	 * @param summaryPresentation
	 * @param startDate
	 * @param endDate
	 * @param request
	 * @return
	 */
	@POST
	@Path("test/{nodeId}/configuration")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addTestConfigurationPost(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@QueryParam("allowCancel") @DefaultValue("false") Boolean allowCancel,
			@QueryParam("allowNavigation") @DefaultValue("false") Boolean allowNavigation,
			@QueryParam("allowSuspend") @DefaultValue("false") Boolean allowSuspend,
			@QueryParam("numAttempts") @DefaultValue("0") int numAttempts,
			@QueryParam("sequencePresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM) String sequencePresentation,
			@QueryParam("showNavigation") @DefaultValue("true") Boolean showNavigation,
			@QueryParam("showQuestionTitle") @DefaultValue("true") Boolean showQuestionTitle,
			@QueryParam("showResultsAfterFinish") @DefaultValue("true") Boolean showResultsAfterFinish,
			@QueryParam("showResultsDependendOnDate") @DefaultValue("false") Boolean showResultsDependendOnDate,
			@QueryParam("showResultsOnHomepage") @DefaultValue("false") Boolean showResultsOnHomepage,
			@QueryParam("showScoreInfo") @DefaultValue("true") Boolean showScoreInfo,
			@QueryParam("showQuestionProgress") @DefaultValue("true") Boolean showQuestionProgress,
			@QueryParam("showScoreProgress") @DefaultValue("true") Boolean showScoreProgress,
			@QueryParam("showSectionsOnly") @DefaultValue("false") Boolean showSectionsOnly,
			@QueryParam("summaryPresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT) String summaryPresentation,
			@QueryParam("startDate") Long startDate, @QueryParam("endDate") Long endDate,
			@Context HttpServletRequest request) {
		
		return addTestConfiguration(courseId, nodeId, allowCancel, allowNavigation, allowSuspend, numAttempts, sequencePresentation, showNavigation, showQuestionTitle, showResultsAfterFinish, showResultsDependendOnDate, showResultsOnHomepage, showScoreInfo, showQuestionProgress, showScoreProgress, showSectionsOnly, summaryPresentation, startDate, endDate, request);
	}
	
	/**
	 * This attaches the run-time configuration onto a given test element.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.doc The test node configuration
	 * @response.representation.200.qname {http://www.example.com}testConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The test node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or test node not found
	 * @response.representation.406.doc The call is not applicable to test course node
	 * @response.representation.409.doc The configuration is not valid 
	 * @param courseId
	 * @param nodeId
	 * @param allowCancel
	 * @param allowNavigation
	 * @param allowSuspend
	 * @param numAttempts
	 * @param sequencePresentation
	 * @param showNavigation
	 * @param showQuestionTitle
	 * @param showResultsAfterFinish
	 * @param showResultsDependendOnDate
	 * @param showResultsOnHomepage
	 * @param showScoreInfo
	 * @param showQuestionProgress
	 * @param showScoreProgress
	 * @param showSectionsOnly
	 * @param summaryPresentation
	 * @param startDate
	 * @param endDate
	 * @param request
	 * @return
	 */
	@PUT
	@Path("test/{nodeId}/configuration")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addTestConfiguration(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@QueryParam("allowCancel") @DefaultValue("false") Boolean allowCancel,
			@QueryParam("allowNavigation") @DefaultValue("false") Boolean allowNavigation,
			@QueryParam("allowSuspend") @DefaultValue("false") Boolean allowSuspend,
			@QueryParam("numAttempts") @DefaultValue("0") int numAttempts,
			@QueryParam("sequencePresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM) String sequencePresentation,
			@QueryParam("showNavigation") @DefaultValue("true") Boolean showNavigation,
			@QueryParam("showQuestionTitle") @DefaultValue("true") Boolean showQuestionTitle,
			@QueryParam("showResultsAfterFinish") @DefaultValue("true") Boolean showResultsAfterFinish,
			@QueryParam("showResultsDependendOnDate") @DefaultValue("false") Boolean showResultsDependendOnDate,
			@QueryParam("showResultsOnHomepage") @DefaultValue("false") Boolean showResultsOnHomepage,
			@QueryParam("showScoreInfo") @DefaultValue("true") Boolean showScoreInfo,
			@QueryParam("showQuestionProgress") @DefaultValue("true") Boolean showQuestionProgress,
			@QueryParam("showScoreProgress") @DefaultValue("true") Boolean showScoreProgress,
			@QueryParam("showSectionsOnly") @DefaultValue("false") Boolean showSectionsOnly,
			@QueryParam("summaryPresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT) String summaryPresentation,
			@QueryParam("startDate") Long startDate, @QueryParam("endDate") Long endDate,
			@Context HttpServletRequest request) {
		
		TestFullConfig config = new TestFullConfig(allowCancel, allowNavigation, allowSuspend, numAttempts, sequencePresentation,
				showNavigation, showQuestionTitle, showResultsAfterFinish, showResultsDependendOnDate, showResultsOnHomepage, showScoreInfo,
				showQuestionProgress, showScoreProgress, showSectionsOnly, summaryPresentation, startDate, endDate);
		
		return attachNodeConfig(courseId, nodeId, config, request);
	}
	
	/**
	 * Retrieves configuration of the test course node
	 * @response.representation.200.qname {http://www.example.com}testConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node configuration
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or test node not found
	 * @param courseId
	 * @param nodeId
	 * @return test course node configuration
	 */
	@GET
	@Path("test/{nodeId}/configuration")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTestConfiguration(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId) {
		
		TestConfigVO config = new TestConfigVO();
		ICourse course = CoursesWebService.loadCourse(courseId);
		CourseNode courseNode = getParentNode(course, nodeId);
		//build configuration with fallback to default values
		ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
		Boolean allowCancel = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_ENABLECANCEL);
		config.setAllowCancel(allowCancel == null ? false : allowCancel);
		Boolean allowNavi = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_ENABLEMENU);
		config.setAllowNavigation(allowNavi == null ? false : allowNavi);
		Boolean allowSuspend = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_ENABLESUSPEND);
		config.setAllowSuspend(allowSuspend == null ? false : allowSuspend);
		config.setNumAttempts(moduleConfig.getIntegerSafe(IQEditController.CONFIG_KEY_ATTEMPTS, 0));
		config.setSequencePresentation(moduleConfig.getStringValue(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM));
		Boolean showNavi = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_DISPLAYMENU);
		config.setShowNavigation(showNavi == null ? true : showNavi);
		Boolean showQuestionTitle = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_QUESTIONTITLE);
		config.setShowQuestionTitle(showQuestionTitle == null ? true : showQuestionTitle);
		Boolean showResFinish = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_FINISH);
		config.setShowResultsAfterFinish(showResFinish == null ? true : showResFinish);
		Boolean showResDate = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
		config.setShowResultsDependendOnDate(showResDate == null ? false : showResDate);
		config.setShowResultsStartDate((Date) moduleConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE));
		config.setShowResultsEndDate((Date) moduleConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE));
		Boolean showResHomepage = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
		config.setShowResultsOnHomepage(showResHomepage == null ? false : showResHomepage);
		Boolean showScoreInfo = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
		config.setShowScoreInfo(showScoreInfo == null ? true : showScoreInfo);
		Boolean showQuestionProgress = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_QUESTIONPROGRESS);
		config.setShowQuestionProgress(showQuestionProgress == null ? true : showQuestionProgress);
		Boolean showScoreProgress = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_SCOREPROGRESS);
		config.setShowScoreProgress(showScoreProgress == null ? true : showScoreProgress);
		Boolean showSectionsOnly = (Boolean)moduleConfig.get(IQEditController.CONFIG_KEY_RENDERMENUOPTION);
		config.setShowSectionsOnly(showSectionsOnly == null ? false : showSectionsOnly);
		config.setSummeryPresentation(moduleConfig.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT));
		
		return Response.ok(config).build();
	}
	
	public class ExternalPageCustomConfig implements CustomConfigDelegate {
		private URL url;
		
		public ExternalPageCustomConfig(URL url) {
			this.url = url;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			moduleConfig.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.FALSE.booleanValue());
			    //fxdiff FXOLAT-122: course management
			if(url != null) {
				moduleConfig.setConfigurationVersion(2);
				moduleConfig.set(TUConfigForm.CONFIGKEY_PROTO, url.getProtocol());
				moduleConfig.set(TUConfigForm.CONFIGKEY_HOST, url.getHost());
				moduleConfig.set(TUConfigForm.CONFIGKEY_URI, url.getPath());
				moduleConfig.set(TUConfigForm.CONFIGKEY_QUERY, url.getQuery());
				int port = url.getPort();
				moduleConfig.set(TUConfigForm.CONFIGKEY_PORT, new Integer(port != -1 ? port : url.getDefaultPort()));
				moduleConfig.setBooleanEntry(TUConfigForm.CONFIG_IFRAME, true);
			}
		}
		
	}
	
	public class WikiCustomConfig implements CustomConfigDelegate {
		private RepositoryEntry wikiRepoEntry;
		
		public WikiCustomConfig(RepositoryEntry wikiRepoEntry) {
			this.wikiRepoEntry = wikiRepoEntry;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			if(wikiRepoEntry != null) {
				moduleConfig.set("reporef", wikiRepoEntry.getSoftkey());
			}
			moduleConfig.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			moduleConfig.setConfigurationVersion(1);
		}
	}
	
	public class BlogCustomConfig implements CustomConfigDelegate {
		private RepositoryEntry blogRepoEntry;
		
		public BlogCustomConfig(RepositoryEntry blogRepoEntry) {
			this.blogRepoEntry = blogRepoEntry;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			if(blogRepoEntry != null) {
				moduleConfig.set(AbstractFeedCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY, blogRepoEntry.getSoftkey());
			}
		}
	}
	
	public class TaskFullConfig implements FullConfigDelegate {

		private final Boolean enableAssignment;
		private final String taskAssignmentText;
		private final String taskAssignmentType;
		private final Boolean enableTaskDeselect;
		private final Boolean enableTaskPreview;
		private final Boolean onlyOneUserPerTask;
		private final Boolean enableDropbox;
		private final String dropboxConfirmationText;
		private final Boolean enableDropboxConfirmationMail;
		private final Boolean enableReturnbox;
		private final Boolean grantPassing;
		private final Boolean grantScoring;
		private final Boolean enableScoring;
		private final Float scoreMin;
		private final Float scoreMax;
		private final Boolean enableCommentField;
		private final Float scorePassingThreshold;
		private final String commentForUser;
		private final String commentForCoaches;
		private final String accessExpertRuleTask;
		private final Boolean enableSolution;
		private final String accessExpertRuleSolution;
		private final String accessExpertRuleReturnbox;
		private final String accessExpertRuleDropbox;
		private final String accessExpertRuleScoring;

		public TaskFullConfig(Boolean enableAssignment,
				String taskAssignmentType, String taskAssignmentText,
				Boolean enableTaskPreview, Boolean enableTaskDeselect,
				Boolean onlyOneUserPerTask, Boolean enableDropbox,
				Boolean enableDropboxConfirmationMail, String dropboxConfirmationText,
				Boolean enableReturnbox, Boolean enableScoring,
				Boolean grantScoring, Float scoreMin,
				Float scoreMax, Boolean grantPassing,
				Float scorePassingThreshold, Boolean enableCommentField,
				String commentForUser, String commentForCoaches,
				Boolean enableSolution, String accessExpertRuleTask,
				String accessExpertRuleDropbox, String accessExpertRuleReturnbox,
				String accessExpertRuleScoring, String accessExpertRuleSolution) {
			this.enableAssignment = enableAssignment;
			this.taskAssignmentType = taskAssignmentType;
			this.taskAssignmentText = taskAssignmentText;
			this.enableTaskPreview = enableTaskPreview;
			this.enableTaskDeselect = enableTaskDeselect;
			this.onlyOneUserPerTask = onlyOneUserPerTask;
			this.enableDropbox = enableDropbox;
			this.enableDropboxConfirmationMail = enableDropboxConfirmationMail;
			this.dropboxConfirmationText = dropboxConfirmationText;
			this.enableReturnbox = enableReturnbox;
			this.enableScoring = enableScoring;
			this.grantScoring = grantScoring;
			this.scoreMin = scoreMin;
			this.scoreMax = scoreMax;
			this.grantPassing = grantPassing;
			this.scorePassingThreshold = scorePassingThreshold;
			this.enableCommentField = enableCommentField;
			this.commentForUser = commentForUser;
			this.commentForCoaches = commentForCoaches;
			this.enableSolution = enableSolution;
			this.accessExpertRuleTask = accessExpertRuleTask;
			this.accessExpertRuleDropbox = accessExpertRuleDropbox;
			this.accessExpertRuleReturnbox = accessExpertRuleReturnbox;
			this.accessExpertRuleScoring = accessExpertRuleScoring;
			this.accessExpertRuleSolution = accessExpertRuleSolution;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public boolean isApplicable(ICourse course, CourseNode courseNode) {
			return courseNode instanceof TACourseNode;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			//module configuration
			if(enableAssignment != null) moduleConfig.set(TACourseNode.CONF_TASK_ENABLED, enableAssignment);
			if(taskAssignmentType != null) moduleConfig.setStringValue(TACourseNode.CONF_TASK_TYPE, taskAssignmentType);
			if(taskAssignmentText != null) moduleConfig.setStringValue(TACourseNode.CONF_TASK_TEXT, taskAssignmentText);
			if(enableTaskPreview != null) moduleConfig.setBooleanEntry(TACourseNode.CONF_TASK_PREVIEW, enableTaskPreview);
			if(enableTaskDeselect != null) moduleConfig.setBooleanEntry(TACourseNode.CONF_TASK_DESELECT, enableTaskDeselect);
			if(onlyOneUserPerTask != null) moduleConfig.set(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT, onlyOneUserPerTask);
			if(enableDropbox != null) moduleConfig.set(TACourseNode.CONF_DROPBOX_ENABLED, enableDropbox);
			if(enableDropboxConfirmationMail != null) moduleConfig.set(TACourseNode.CONF_DROPBOX_ENABLEMAIL, enableDropboxConfirmationMail);
			if(dropboxConfirmationText != null) moduleConfig.setStringValue(TACourseNode.CONF_DROPBOX_CONFIRMATION, dropboxConfirmationText);
			if(enableReturnbox != null) moduleConfig.set(TACourseNode.CONF_RETURNBOX_ENABLED, enableReturnbox);
			if(enableScoring != null) moduleConfig.set(TACourseNode.CONF_SCORING_ENABLED, enableScoring);
			if(grantScoring != null) moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, grantScoring);
			if(scoreMin != null) moduleConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, scoreMin);
			if(scoreMax != null) moduleConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, scoreMax);
			if(grantPassing != null) moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, grantPassing);
			if(scorePassingThreshold != null) moduleConfig.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, scorePassingThreshold);
			if(enableCommentField != null) moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, enableCommentField);
			if(commentForUser != null) moduleConfig.setStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, commentForUser);
			if(commentForCoaches != null) moduleConfig.setStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, commentForCoaches);
			if(enableSolution != null) moduleConfig.set(TACourseNode.CONF_SOLUTION_ENABLED, enableSolution);
			//conditions
			TACourseNode courseNode = (TACourseNode)newNode;
			createExpertCondition("drop", accessExpertRuleDropbox);
			if(accessExpertRuleTask != null)
				courseNode.setConditionTask(createExpertCondition(TACourseNode.ACCESS_TASK, accessExpertRuleTask));
			if(accessExpertRuleDropbox != null)
				courseNode.setConditionDrop(createExpertCondition(TACourseNode.ACCESS_DROPBOX, accessExpertRuleDropbox));
			if(accessExpertRuleReturnbox != null)
				courseNode.setConditionReturnbox(createExpertCondition(TACourseNode.ACCESS_RETURNBOX, accessExpertRuleReturnbox));
			if(accessExpertRuleScoring != null)
				courseNode.setConditionScoring(createExpertCondition(TACourseNode.ACCESS_SCORING, accessExpertRuleScoring));
			if(accessExpertRuleSolution != null)
				courseNode.setConditionSolution(createExpertCondition(TACourseNode.ACCESS_SOLUTION, accessExpertRuleSolution));
		}
	}
	
	public class SurveyFullConfig implements FullConfigDelegate {
		
		private final Boolean allowSuspend;
		private final Boolean allowNavigation;
		private final Boolean allowCancel;
		private final Boolean showSectionsOnly;
		private final Boolean showQuestionTitle;
		private final Boolean showNavigation;
		private final String sequencePresentation;

		public SurveyFullConfig(Boolean allowCancel, Boolean allowNavigation, Boolean allowSuspend,
				String sequencePresentation, Boolean showNavigation, Boolean showQuestionTitle, Boolean showSectionsOnly) {
					this.allowCancel = allowCancel;
					this.allowNavigation = allowNavigation;
					this.allowSuspend = allowSuspend;
					this.sequencePresentation = sequencePresentation;
					this.showNavigation = showNavigation;
					this.showQuestionTitle = showQuestionTitle;
					this.showSectionsOnly = showSectionsOnly;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public boolean isApplicable(ICourse course, CourseNode courseNode) {
			return courseNode instanceof IQSURVCourseNode;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			if(allowCancel != null) moduleConfig.set(IQEditController.CONFIG_KEY_ENABLECANCEL, allowCancel);
			if(allowNavigation != null) moduleConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, allowNavigation);
			if(allowSuspend != null) moduleConfig.set(IQEditController.CONFIG_KEY_ENABLESUSPEND, allowSuspend);
			if(sequencePresentation != null) moduleConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, sequencePresentation);
			if(showNavigation != null) moduleConfig.set(IQEditController.CONFIG_KEY_DISPLAYMENU, showNavigation);
			if(showQuestionTitle != null) moduleConfig.set(IQEditController.CONFIG_KEY_QUESTIONTITLE, showQuestionTitle);
			if(showSectionsOnly != null) moduleConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, showSectionsOnly);
		}
	}
	
	//fxdiff FXOLAT-122: course management
	public class StructureFullConfig implements FullConfigDelegate {
		
		private final String displayType;
		private final InputStream in;
		private final String filename;
		
		public StructureFullConfig(String displayType, InputStream in, String filename) {
			this.displayType = displayType;
			this.in = in;
			this.filename = filename;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			
			if(displayType != null && (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC.equals(displayType)
					|| STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(displayType)
					|| STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(displayType))) {
				moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, displayType);
			}
			
			if(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(moduleConfig.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE))) {
				if(in != null && StringHelper.containsNonWhitespace(filename)) {
					VFSContainer rootContainer = course.getCourseFolderContainer();
					VFSLeaf singleFile = (VFSLeaf) rootContainer.resolve("/" + filename);
					if (singleFile == null) {
						singleFile = rootContainer.createChildLeaf("/" + filename);
					}
	
					moduleConfig.set(STCourseNodeEditController.CONFIG_KEY_FILE, "/" + filename);
					OutputStream out = singleFile.getOutputStream(false);
					FileUtils.copy(in, out);
					FileUtils.closeSafely(out);
					FileUtils.closeSafely(in);
				} else if (StringHelper.containsNonWhitespace(filename)) {
					VFSContainer rootContainer = course.getCourseFolderContainer();
					VFSLeaf singleFile = (VFSLeaf) rootContainer.resolve("/" + filename);
					if(singleFile != null) {
						moduleConfig.set(STCourseNodeEditController.CONFIG_KEY_FILE, "/" + filename);
					}
				}
			}
		}

		@Override
		public boolean isApplicable(ICourse course, CourseNode courseNode) {
			return courseNode instanceof STCourseNode;
		}
	}
	
	public class TestFullConfig implements FullConfigDelegate {
		
		private final Boolean allowCancel;
		private final Boolean allowSuspend;
		private final Boolean allowNavigation;
		private final Boolean showResultsDependendOnDate;
		private final Boolean showNavigation;
		private final String sequencePresentation;
		private final Boolean showQuestionTitle;
		private final Integer numAttempts;
		private final Boolean showScoreInfo;
		private final Boolean showResultsOnHomepage;
		private final Boolean showResultsAfterFinish;
		private final Boolean showScoreProgress;
		private final Boolean showQuestionProgress;
		private final Boolean showSectionsOnly;
		private final Long startDate;
		private final String summaryPresentation;
		private final Long endDate;

		public TestFullConfig(Boolean allowCancel, Boolean allowNavigation, Boolean allowSuspend,
			int numAttempts, final String sequencePresentation, Boolean showNavigation, Boolean showQuestionTitle,
			Boolean showResultsAfterFinish, Boolean showResultsDependendOnDate, Boolean showResultsOnHomepage,
			Boolean showScoreInfo, Boolean showQuestionProgress, Boolean showScoreProgress,
			Boolean showSectionsOnly, String summaryPresentation, Long startDate, Long endDate) {
				this.allowCancel = allowCancel;
				this.allowNavigation = allowNavigation;
				this.allowSuspend = allowSuspend;
				this.numAttempts = numAttempts;
				this.sequencePresentation = sequencePresentation;
				this.showNavigation = showNavigation;
				this.showQuestionTitle = showQuestionTitle;
				this.showResultsAfterFinish = showResultsAfterFinish;
				this.showResultsDependendOnDate = showResultsDependendOnDate;
				this.showResultsOnHomepage = showResultsOnHomepage;
				this.showScoreInfo = showScoreInfo;
				this.showQuestionProgress = showQuestionProgress;
				this.showScoreProgress = showScoreProgress;
				this.showSectionsOnly = showSectionsOnly;
				this.summaryPresentation = summaryPresentation;
				this.startDate = startDate;
				this.endDate = endDate;
		}

		@Override
		public boolean isValid() {
			return true;
		}
		
		@Override
		public boolean isApplicable(ICourse course, CourseNode courseNode) {
			return courseNode instanceof IQTESTCourseNode;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			if(allowCancel != null) moduleConfig.set(IQEditController.CONFIG_KEY_ENABLECANCEL, allowCancel);
			if(allowNavigation != null) moduleConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, allowNavigation);
			if(allowSuspend != null) moduleConfig.set(IQEditController.CONFIG_KEY_ENABLESUSPEND, allowSuspend);
			if(numAttempts != null) moduleConfig.set(IQEditController.CONFIG_KEY_ATTEMPTS, numAttempts);
			if(sequencePresentation != null) moduleConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, sequencePresentation);
			if(showNavigation != null) moduleConfig.set(IQEditController.CONFIG_KEY_DISPLAYMENU, showNavigation);
			if(showQuestionTitle != null) moduleConfig.set(IQEditController.CONFIG_KEY_QUESTIONTITLE, showQuestionTitle);
			if(showResultsAfterFinish != null) moduleConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_FINISH, showResultsAfterFinish);
			if(showResultsDependendOnDate != null) moduleConfig.set(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, showResultsDependendOnDate);
			if(startDate != null && endDate != null) {
				moduleConfig.set(IQEditController.CONFIG_KEY_RESULTS_START_DATE, new Date(startDate));
				moduleConfig.set(IQEditController.CONFIG_KEY_RESULTS_END_DATE, new Date(endDate));
			}
			if(showResultsOnHomepage != null) moduleConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, showResultsOnHomepage);
			if(showScoreInfo != null) moduleConfig.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, showScoreInfo);
			if(showQuestionProgress != null) moduleConfig.set(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, showQuestionProgress);
			if(showScoreProgress != null) moduleConfig.set(IQEditController.CONFIG_KEY_SCOREPROGRESS, showScoreProgress);
			if(showSectionsOnly != null) moduleConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, showSectionsOnly);
			if(summaryPresentation != null) moduleConfig.set(IQEditController.CONFIG_KEY_SUMMARY, summaryPresentation);
		}
	}
	
	public class SinglePageCustomConfig implements CustomConfigDelegate {
		private InputStream in;
		private String filename;
		private String path;
		
		public SinglePageCustomConfig(InputStream in, String filename) {
			this.in = in;
			this.filename = filename;
		}
		
		public SinglePageCustomConfig(String path, String filename) {
			this.path = path;
			this.filename = filename;
		}
		
		@Override
		public boolean isValid() {
			return in != null || StringHelper.containsNonWhitespace(path);
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			newNode.setDisplayOption(CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT);
			VFSContainer rootContainer = course.getCourseFolderContainer();
			VFSLeaf singleFile = (VFSLeaf) rootContainer.resolve("/" + filename);
			if (singleFile == null) {
				singleFile = rootContainer.createChildLeaf("/" + filename);
			}

			if(in != null) {
				moduleConfig.set(SPEditController.CONFIG_KEY_FILE, "/" + filename);
				
				OutputStream out = singleFile.getOutputStream(false);
				FileUtils.copy(in, out);
				FileUtils.closeSafely(out);
				FileUtils.closeSafely(in);
			} else {
				if(StringHelper.containsNonWhitespace(path)) {
					if(!path.startsWith("/")) {
						path = "/" + path;
					}
					if(!path.endsWith("/")) {
						path += "/";
					}
				} else {
					path = "/";
				}
				moduleConfig.set(SPEditController.CONFIG_KEY_FILE, path + filename);
			}
			//saved node configuration
		}
	}
	
	public class TaskCustomConfig implements CustomConfigDelegate {
		private final Float points;
		private final String text;
		
		public TaskCustomConfig(Float points, String text) {
			this.points = points;
			this.text = text;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			newNode.updateModuleConfigDefaults(true);
			//fxdiff FXOLAT-122: course management
			if(text != null) {
				moduleConfig.set(TACourseNode.CONF_TASK_TEXT, text);
			}
			if (points != null) {
				moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, true);
				moduleConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, new Float(0));
				moduleConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, points);
			}
			TACourseNode taskNode = (TACourseNode) newNode;
			taskNode.getConditionExpressions();
		}
	}
	
	public class AssessmentCustomConfig implements CustomConfigDelegate {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			// Use default min and max scores and default cut value
			/*
			 * //score granted (default is FALSE) Boolean scoreField = Boolean.FALSE;
			 * modConfig.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, scoreField);
			 * //if score granted == TRUE we can set these values if (scoreField) {
			 * modConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, 5.0f);
			 * modConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, 10.0f); }
			 * 
			 * 
			 * //display passed / failed (note that TRUE means automatic and FALSE
			 * means manually)... //default is TRUE Boolean displayPassed =
			 * Boolean.TRUE; modConfig.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD,
			 * displayPassed); //display set to false -> we can set these values
			 * manually if (!displayPassed.booleanValue()) { //passed set to when
			 * score higher than cut value
			 * modConfig.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, 5.0f); }
			 */

			// comment
			moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, Boolean.TRUE);
			// info coach
			moduleConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, "Info coach");
			// info user
			moduleConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, "Info user");
		}
	}
	
}
