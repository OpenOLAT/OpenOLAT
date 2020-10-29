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
package org.olat.modules.bigbluebutton.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 29 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Virtual class rooms")
@Component
@Path("bigbluebutton/servers")
public class BigBlueButtonServerWebService {
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	/**
	 * 
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Operation(summary = "Get the version of the BigBlueButton web service",
		description = "Get the version of the BigBlueButton web service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Return the curriculums a manager user is allowed to see.
	 * 
	 * @param httpRequest  The HTTP request
	 * @return An array of curriculums
	 */
	@GET
	@Operation(summary = "Return the list of BigBlueButton servers",
		description = "Return the list of BigBlueButton servers")
	@ApiResponse(responseCode = "200", description = "An array of BigBlueButton servers",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BigBlueButtonServerVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = BigBlueButtonServerVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getServers(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<BigBlueButtonServer> servers = bigBlueButtonManager.getServers();
		List<BigBlueButtonServerVO> voes = new ArrayList<>(servers.size());
		for(BigBlueButtonServer server:servers) {
			voes.add(BigBlueButtonServerVO.valueOf(server));
		}
		return Response.ok(voes.toArray(new BigBlueButtonServerVO[voes.size()])).build();
	}
	
	/**
	 * Get a specific curriculum.
	 * 
	 * @param curriculumKey The curriculum primary key
	 * @param httpRequest The HTTP request
	 * @return The curriculum
	 */
	@GET
	@Path("{serverKey}")
	@Operation(summary = "Get a specific BigBlueButton server",
		description = "Get a specific BigBlueButton server")
	@ApiResponse(responseCode = "200", description = "The BigBlueButton server",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = BigBlueButtonServerVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = BigBlueButtonServerVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getServer(@PathParam("serverKey") Long serverKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		BigBlueButtonServer server = bigBlueButtonManager.getServer(serverKey);
		if(server == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(BigBlueButtonServerVO.valueOf(server)).build();
	}
	
	/**
	 * Create or update a BigBlueButton server entity.
	 * 
	 * @param curriculum The server to merge
	 * @param request The HTTP request
	 * @return The merged server
	 */
	@PUT
	@Operation(summary = "Creates or update a BigBlueButton server",
		description = "Creates or update a BigBlueButton server")
	@ApiResponse(responseCode = "200", description = "The server to update",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = BigBlueButtonServerVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = BigBlueButtonServerVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putServer(BigBlueButtonServerVO server, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		BigBlueButtonServer savedServer = saveServer(server);
		return Response.ok(BigBlueButtonServerVO.valueOf(savedServer)).build();
	}
	
	/**
	 * Create or update a BigBlueButton server entity.
	 * 
	 * @param curriculum The server to merge
	 * @param request The HTTP request
	 * @return The merged server
	 */
	@POST
	@Operation(summary = "Creates or update a BigBlueButton server",
		description = "Creates or update a BigBlueButton server")
	@ApiResponse(responseCode = "200", description = "The server to update",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = BigBlueButtonServerVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = BigBlueButtonServerVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postServer(BigBlueButtonServerVO server, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		BigBlueButtonServer savedServer = saveServer(server);
		return Response.ok(BigBlueButtonServerVO.valueOf(savedServer)).build();
	}

	
	private BigBlueButtonServer saveServer(BigBlueButtonServerVO serverVo) {
		BigBlueButtonServer server;
		if(serverVo.getKey() == null) {
			server = bigBlueButtonManager.createServer(serverVo.getUrl(), serverVo.getRecordingUrl(), serverVo.getSharedSecret());
		} else {
			server = bigBlueButtonManager.getServer(serverVo.getKey());
		}
		
		if(StringHelper.containsNonWhitespace(serverVo.getName())) {
			server.setName(serverVo.getName());
		}
		if(StringHelper.containsNonWhitespace(serverVo.getRecordingUrl())) {
			server.setRecordingUrl(serverVo.getRecordingUrl());
		}
		if(StringHelper.containsNonWhitespace(serverVo.getSharedSecret())) {
			server.setSharedSecret(serverVo.getSharedSecret());
		}
		if(StringHelper.containsNonWhitespace(serverVo.getUrl())) {
			server.setUrl(serverVo.getUrl());
		}
		if(serverVo.getCapacityFactory() != null) {
			server.setCapacityFactory(serverVo.getCapacityFactory());
		}
		if(serverVo.getEnabled() != null) {
			server.setEnabled(serverVo.getEnabled().booleanValue());
		}
		return bigBlueButtonManager.updateServer(server);
	}

}
