
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

package org.olat.commons.info.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Tag(name = "Infomessages")
@Component
@Path("infomessages")
public class InfoMessagesWebService {
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
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
	@ApiResponse(responseCode = "400", description = "The parameters resName or resId are missing")
	@ApiResponse(responseCode = "403", description = "The authenticated user cannot manage the info messages of the resource")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createEmptyCourse(final @QueryParam("resName") @Parameter(description = "The OLAT Resourceable name") String resName,
			final @QueryParam("resId") @Parameter(description = "The OLAT Resourceable id") Long resId, @QueryParam("resSubPath") @Parameter(description = "The resource sub path (optional)") String resSubPath,
			@QueryParam("businessPath") @Parameter(description = "The business path") String businessPath, @QueryParam("authorKey") @Parameter(description = "The identity key of the author") Long authorKey,
			@QueryParam("title") @Parameter(description = "The title") String title, @QueryParam("message") @Parameter(description = "The message") String message,
			@Context HttpServletRequest request) {
		if(resName == null || resId == null) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
		if(!isManager(ores, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Identity author;
		Roles roles = getRoles(request);
		UserRequest ureq = getUserRequest(request);
		if(authorKey == null || !roles.isAdministrator()) {
			author = ureq.getIdentity();
		} else {
			author = securityManager.loadIdentityByKey(authorKey, false);
			if(author == null) {
				return Response.serverError().status(Status.FORBIDDEN).build();
			}
		}

		InfoMessage msg = messageManager.createInfoMessage(ores, resSubPath, businessPath, author);
		msg.setTitle(title);
		msg.setMessage(message);
		messageManager.sendInfoMessage(msg, null, ureq.getLocale(), ureq.getIdentity(), Collections.emptySet());
		InfoMessageVO infoVO = new InfoMessageVO(msg);
		return Response.ok(infoVO).build();
	}
	
	@Path("{infoMessageKey}")
	public InfoMessageWebService getInfoMessageWebservice(@PathParam("infoMessageKey") Long infoMessageKey, @Context HttpServletRequest request) {
		InfoMessage msg = messageManager.loadInfoMessage(infoMessageKey);
		if(msg == null || msg.getResName() == null ) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(msg.getResName(), msg.getResId());
		if(isManager(ores, request)
				|| ((msg.getPublishDate() == null || msg.getPublishDate().before(new Date())) && isUser(ores, msg.getResSubPath(), request))) {
			return new InfoMessageWebService(msg);
		}
		throw new WebApplicationException(Status.FORBIDDEN);
	}
	
	private boolean isManager(OLATResourceable ores, HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		if("CourseModule".equals(ores.getResourceableTypeName())) {
			Identity identity = ureq.getIdentity();
			RepositoryEntry entry = repositoryService.loadByResourceId(ores.getResourceableTypeName(), ores.getResourceableId());
			return repositoryService.hasRoleExpanded(identity, entry,
					OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
					GroupRoles.owner.name());
		}
		if("BusinessGroup".equals(ores.getResourceableTypeName())) {
			Roles roles = getRoles(request);
			if(roles.isAdministrator()) {
				return true;
			}
			Identity identity = ureq.getIdentity();
			List<String> groupRoles = businessGroupService.getIdentityRolesInBusinessGroup(identity, new BusinessGroupRefImpl(ores.getResourceableId()));
			return groupRoles != null && groupRoles.contains(GroupRoles.coach.name());
		}
		return false;
	}
	
	private boolean isUser(OLATResourceable ores, String ident, HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		if("CourseModule".equals(ores.getResourceableTypeName())) {
			RepositoryEntry entry = repositoryService.loadByResourceId(ores.getResourceableTypeName(), ores.getResourceableId());
			if(entry == null) {
				return false;
			}
			// canLaunch checks the status of the course too
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq.getIdentity(), ureq.getUserSession().getRoles(), entry);
			if(reSecurity.canLaunch() && (reSecurity.isParticipant() || reSecurity.isCoach())) {
				if(ident == null) {
					return true;
				}
				ICourse course = CourseFactory.loadCourse(entry);
				CourseNode node = course.getRunStructure().getNode(ident);
				if(node == null) {
					return false;
				}
				return (new CourseTreeVisitor(course, ureq.getUserSession().getIdentityEnvironment())).isAccessible(node);
			}
		}
		if("BusinessGroup".equals(ores.getResourceableTypeName())) {
			Identity identity = ureq.getIdentity();
			List<String> groupRoles = businessGroupService.getIdentityRolesInBusinessGroup(identity, new BusinessGroupRefImpl(ores.getResourceableId()));
			return groupRoles != null && groupRoles.contains(GroupRoles.participant.name());
		}
		return false;
	}
}
