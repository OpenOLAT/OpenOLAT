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

package org.olat.commons.info.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  29 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Tag(name = "Infomessages")
@Component
@Path("infomessages")
public class InfoMessagesWebService {
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InfoMessageFrontendManager messageManager;
	
	/**
	 * The version of the Info messages Web Service
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the Info messages Web Service",
	description = "The version of the Info messages Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")		
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Creates a new info message
	 * 
	 * @param resName The OLAT Resourceable name
	 * @param resId The OLAT Resourceable id
	 * @param resSubPath The resource sub path (optional)
	 * @param businessPath The business path
	 * @param authorKey The identity key of the author
	 * @param title The title
	 * @param message The message
	 * @param request The HTTP request
	 * @return It returns the id of the newly info message
	 */
	@PUT
	@Operation(summary = "Creates a new info message", description = "Creates a new info message")
	@ApiResponse(responseCode = "200", description = "The info message",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = InfoMessageVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = InfoMessageVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not Found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createEmptyCourse(final @QueryParam("resName") @Parameter(description = "The OLAT Resourceable name") String resName,
			final @QueryParam("resId") @Parameter(description = "The OLAT Resourceable id") Long resId, @QueryParam("resSubPath") @Parameter(description = "The resource sub path (optional)") String resSubPath,
			@QueryParam("businessPath") @Parameter(description = "The business path") String businessPath, @QueryParam("authorKey") @Parameter(description = "The identity key of the author") Long authorKey,
			@QueryParam("title") @Parameter(description = "The title") String title, @QueryParam("message") @Parameter(description = "The message") String message,
			@Context HttpServletRequest request) {
		
		if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity author;
		UserRequest ureq = getUserRequest(request);
		if(authorKey == null) {
			author = ureq.getIdentity();
		} else {
			author = securityManager.loadIdentityByKey(authorKey, false);
			if(author == null) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
		InfoMessage msg = messageManager.createInfoMessage(ores, resSubPath, businessPath, author);
		msg.setTitle(title);
		msg.setMessage(message);
		messageManager.sendInfoMessage(msg, null, ureq.getLocale(), ureq.getIdentity(), Collections.<Identity>emptyList());
		InfoMessageVO infoVO = new InfoMessageVO(msg);
		return Response.ok(infoVO).build();
	}
	
	@Path("{infoMessageKey}")
	public InfoMessageWebService getInfoMessageWebservice(@PathParam("infoMessageKey") Long infoMessageKey) {
		InfoMessage msg = messageManager.loadInfoMessage(infoMessageKey);
		return new InfoMessageWebService(msg);
	}
	
	private boolean isAuthor(HttpServletRequest request) {
		try {
			Roles roles = getRoles(request);
			return (roles.isAuthor() || roles.isAdministrator() || roles.isLearnResourceManager());
		} catch (Exception e) {
			return false;
		}
	}
}
