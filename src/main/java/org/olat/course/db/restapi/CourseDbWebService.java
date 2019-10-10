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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.course.db.restapi;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.db.CourseDBEntry;
import org.olat.course.db.CourseDBManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.vo.KeyValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Description:<br>
 * Access the custom dbs of a course
 * 
 * <P>
 * Initial Date:	 *7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses/{courseId}/db/{category}")
public class CourseDbWebService {
	
	private static final String VERSION	= "1.0";
	
	@Autowired
	private RepositoryManager repositoryManager;
	
	/**
	 * Retrieves the version of the Course DB Web Service.
	 * @response.representation.200.mediaType text/plain
	 * @response.representation.200.doc The version of this specific Web Service
	 * @response.representation.200.example 1.0
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "Retrieve version",
	description = "Retrieves the version of the Course DB Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Retrieve all values of the authenticated user
	 * @response.representation.200.qname {http://www.example.com}keyValuePair
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc All the values in the course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_KEYVALUEVOes}
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @param request The HTTP request
	 * @return
	 */
	@GET
	@Path("values")
	@Operation(summary = "Retrieve all values of the authenticated user",
	description = "Retrieve all values of the authenticated user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All the values in the course",
					content = {
							@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class))),
							@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class)))
						} 
			)}
		)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getValues(@PathParam("courseId") Long courseId, @PathParam("category") String category, @Context HttpServletRequest request) {
		ICourse course = loadCourse(courseId);
		UserRequest ureq = RestSecurityHelper.getUserRequest(request);
		List<CourseDBEntry> entries = CoreSpringFactory.getImpl(CourseDBManager.class)
				.getValues(course, ureq.getIdentity(), category, null);

		KeyValuePair[] pairs = new KeyValuePair[entries.size()];
		int count=0;
		for(CourseDBEntry entry:entries) {
			Object value = entry.getValue();
			pairs[count++] = new KeyValuePair(entry.getName(), value == null ? "" : value.toString());
		}
		return Response.ok(pairs).build();
	}
	
	/**
	 * Put a new value for an authenticated user.
	 * @response.representation.qname {http://www.example.com}keyValuePair
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc the key value pair is saved on the db
	 * @response.representation.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_KEYVALUEVOes}
	 * @response.representation.200.doc the key value pair is saved on the db
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @param pair The key value pair
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("values")
	@Operation(summary = "Put a new value for an authenticated user",
	description = "Put a new value for an authenticated user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "the key value pair is saved on the db",
					content = {
							@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class))),
							@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class)))
						} 
			)}
		)
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putValues(@PathParam("courseId") Long courseId, @PathParam("category") String category, KeyValuePair pair, @Context HttpServletRequest request) {
		return internPutValues(courseId, category, pair, request);
	}
	
	/**
	 * Update a value for an authenticated user.
	 * @response.representation.qname {http://www.example.com}keyValuePair
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc the key value pair is saved on the db
	 * @response.representation.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_KEYVALUEVOes}
	 * @response.representation.200.doc the key value pair is saved on the db
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @param pair The key value pair
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("values")
	@Operation(summary = "Update a value for an authenticated user",
	description = "Update a value for an authenticated user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "the key value pair is saved on the db",
					content = {
							@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class))),
							@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class)))
						} 
			)}
		)
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postValues(@PathParam("courseId") Long courseId, @PathParam("category") String category, KeyValuePair pair, @Context HttpServletRequest request) {
		return internPutValues(courseId, category, pair, request);
	}

	/**
	 * Retrieve a value of an authenticated user.
	 * @response.representation.200.qname {http://www.example.com}keyValuePair
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The value in the course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_KEYVALUEVO}
	 * @response.representation.404.doc The entry cannot be found
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @parma name The name of the key value pair
	 * @param request The HTTP request
	 * @return
	 */
	@GET
	@Path("values/{name}")
	@Operation(summary = "Retrieve a value of an authenticated user",
	description = "Retrieve a value of an authenticated user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The value in the course",
					content = {
							@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class))),
							@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class)))
						} 
			),
			@ApiResponse(responseCode = "204", description = "The entry cannot be found"
					
			)
			}
		)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getValue(@PathParam("courseId") Long courseId, @PathParam("category") String category, @PathParam("name") String name, @Context HttpServletRequest request) {
		ICourse course = loadCourse(courseId);
		UserRequest ureq = RestSecurityHelper.getUserRequest(request);
		CourseDBEntry entry = CoreSpringFactory.getImpl(CourseDBManager.class)
				.getValue(course, ureq.getIdentity(), category, name);
		if(entry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Object value = entry.getValue();
		KeyValuePair pair = new KeyValuePair(name, value == null ? "" : value.toString());
		return Response.ok(pair).build();
	}

	/**
	 * Retrieve a value of an authenticated user.
	 * @response.representation.200.qname {http://www.example.com}keyValuePair
	 * @response.representation.200.mediaType text/plain, text/html
	 * @response.representation.200.doc A value of the course
	 * @response.representation.200.example Green
	 * @response.representation.404.doc The entry cannot be found
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @param name The name of the key value pair
	 * @param request The HTTP request
	 * @return
	 */
	@GET
	@Path("values/{name}")
	@Operation(summary = "Retrieve a value of an authenticated user",
	description = "Retrieve a value of an authenticated user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The value in the course",
					content = {
							@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class))),
							@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = KeyValuePair.class)))
						} 
			),
			@ApiResponse(responseCode = "404", description = "The entry cannot be found"
					
			)
			}
		)
	@Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
	public Response getValuePlain(@PathParam("courseId") Long courseId, @PathParam("category") String category, @PathParam("name") String name,
			@Context HttpServletRequest request) {
		ICourse course = loadCourse(courseId);
		UserRequest ureq = RestSecurityHelper.getUserRequest(request);
		CourseDBEntry entry = CoreSpringFactory.getImpl(CourseDBManager.class)
				.getValue(course, ureq.getIdentity(), category, name);
		if(entry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Object value = entry.getValue();
		String val = value == null ? "" : value.toString();
		return Response.ok(val).build();
	}

	/**
	 * Put a new value for an authenticated user.
	 * @response.representation.200.doc The value is saved in the course
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @param name The name of the key value pair
	 * @param value The value of the key value pair
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("values/{name}")
	@Operation(summary = "Put a new value for an authenticated user",
	description = "Put a new value for an authenticated user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The value is saved in the course")
			}
		)
	public Response putValue(@PathParam("courseId") Long courseId, @PathParam("category") String category, @PathParam("name") String name,
			@QueryParam("value")  @Parameter(description = "The value of the key value pair") String value, @Context HttpServletRequest request) {
		return internPutValue(courseId, category, name, value, request);
	}

	/**
	 * Update a value for an authenticated user.
	 * @response.representation.200.doc The value is saved in the course
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @param name The name of the key value pair
	 * @param val The value of the key value pair
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("values/{name}")
	@Operation(summary = "Update a value for an authenticated user",
	description = "Update a value for an authenticated user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The value is saved in the course")
			}
		)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response formValue(@PathParam("courseId") Long courseId, @PathParam("category") String category, @PathParam("name") String name,
			@FormParam("val") String value, @Context HttpServletRequest request){
		return internPutValue(courseId, category, name, value, request);
	}
	
	/**
	 * Delete a value for an authenticated user.
	 * @response.representation.200.doc the key value pair is remove from the db
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The entry cannot be found
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @param name The name of the key value pair
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("values/{name}")
	@Operation(summary = "Delete a value for an authenticated user",
	description = "Delete a value for an authenticated user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "the key value pair is remove from the db"

			),
			@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient"
					
			),
			@ApiResponse(responseCode = "404", description = "The entry cannot be found"
			
					)
			}
		)
	public Response deleteValue(@PathParam("courseId") Long courseId, @PathParam("category") String category, 
			@PathParam("name") String name, @Context HttpServletRequest request) {
		UserRequest ureq = RestSecurityHelper.getUserRequest(request);
		ICourse course = loadCourse(courseId);
		if(isManager(course, ureq)) {
			boolean ok = CoreSpringFactory.getImpl(CourseDBManager.class)
					.deleteValue(course, ureq.getIdentity(), category, name);
			if(ok) {
				return Response.ok().build();
			}
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.serverError().status(Status.UNAUTHORIZED).build();
	}
	
	/**
	 * Fallback method for the browsers
	 * 
	 * @response.representation.200.doc the key value pair is remove from the db
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The entry cannot be found
	 * @param courseId The course resourceable's id
	 * @param category The name of the database
	 * @param name The name of the key value pair
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("values/{name}/delete")
	@Operation(summary = "Fallback method for the browsers",
	description = "Fallbakc method for the browsers")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "the key value pair is remove from the db"

			),
			@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient"
					
			),
			@ApiResponse(responseCode = "404", description = "The entry cannot be found"
			
					)
			}
		)
	public Response deleteValuePost(@PathParam("courseId") Long courseId, @PathParam("category") String category,
			@PathParam("name") String name, @Context HttpServletRequest request) {
		return deleteValue(courseId, category, name, request);
	}
	
	private Response internPutValues(Long courseId, String category, KeyValuePair pair, HttpServletRequest request) {
		return internPutValue(courseId, category, pair.getKey(), pair.getValue(), request);
	}
	
	private Response internPutValue(Long courseId, String category, String name, Object value, HttpServletRequest request) {
		ICourse course = loadCourse(courseId);
		UserRequest ureq = RestSecurityHelper.getUserRequest(request);
		CourseDBEntry entry = CoreSpringFactory.getImpl(CourseDBManager.class)
				.setValue(course, ureq.getIdentity(), category, name, value);
		if(entry == null) {
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.ok().build();
	}
	
	private ICourse loadCourse(Long potentialCourseId) {
		Long courseId = CoreSpringFactory.getImpl(CourseDBManager.class).getCourseId(potentialCourseId);
		return CourseFactory.loadCourse(courseId);
	}
	
	private boolean isManager(ICourse course, UserRequest ureq) {
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity identity = ureq.getIdentity();
		Roles roles = ureq.getUserSession().getRoles();
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(identity, roles, re);
		return reSecurity.isEntryAdmin();
	}
}
