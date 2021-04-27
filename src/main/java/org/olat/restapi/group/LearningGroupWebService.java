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
package org.olat.restapi.group;

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.isGroupManager;
import static org.olat.restapi.support.ObjectFactory.getInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.admin.quota.QuotaConstants;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.collaboration.CollaborationManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.restapi.CalWebService;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.restapi.VFSWebServiceSecurityCallback;
import org.olat.core.commons.services.vfs.restapi.VFSWebservice;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.restapi.ForumWebService;
import org.olat.modules.wiki.restapi.GroupWikiWebService;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.GroupConfigurationVO;
import org.olat.restapi.support.vo.GroupInfoVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Description:<br>
 * This handles the learning groups.
 * 
 * <P>
 * Initial Date:  23 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Groups")
@Component
@Path("groups")
public class LearningGroupWebService {
	
	private static final Logger log = Tracing.createLoggerFor(LearningGroupWebService.class);
	
	private static final String VERSION = "1.0";
	
	private static CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	@Autowired
	private BusinessGroupService bgs;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private QuotaManager qm;
	@Autowired
	private CollaborationManager collaborationManager;
	@Autowired
	private CollaborationToolsFactory collaborationToolsFactory;
	
	/**
	 * Retrieves the version of the Group Web Service.
	 *
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "Retrieves the version of the Group Web Service", description = "Retrieves the version of the Group Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Return the list of all groups if you have group manager permission, or all
	 * learning group that you particip with or owne.
	 * 
	 * @param externalId Search with an external ID
	 * @param managed (true / false) Search only managed / not managed groups 
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Operation(summary = "Return the list of all groups ", description = "Return the list of all groups if you have group manager permission, or all\n" + 
			" learning group that you particip with or owne")
	@ApiResponse(responseCode = "200", description = "This is the list of all groups in OLAT system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))) })
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getGroupList(@QueryParam("externalId") @Parameter(description = "Search with an external ID") String externalId, @QueryParam("managed") @Parameter(description = "(true / false) Search only managed / not managed groups") Boolean managed,
			@Context HttpServletRequest request) {
		List<BusinessGroup> groups;
		SearchBusinessGroupParams params;
		if(isGroupManager(request)) {
			params = new SearchBusinessGroupParams();
		} else {
			Identity identity = RestSecurityHelper.getIdentity(request);
			params = new SearchBusinessGroupParams(identity, true, true);
		}
		if(StringHelper.containsNonWhitespace(externalId)) {
			params.setExternalId(externalId);
		}
		params.setManaged(managed);
		groups = bgs.findBusinessGroups(params, null, 0, -1);
		
		int count = 0;
		GroupVO[] groupVOs = new GroupVO[groups.size()];
		for(BusinessGroup bg:groups) {
			groupVOs[count++] = ObjectFactory.get(bg);
		}
		return Response.ok(groupVOs).build();
	}
	
	/**
	 * Return the group specified by the key of the group.
	 * 
	 * @param groupKey The key of the group
	 * @param request The REST request
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Path("{groupKey}")
	@Operation(summary = "Return the group specified by the key of the group", description = "RReturn the group specified by the key of the group")
	@ApiResponse(responseCode = "200", description = "A business group in the OLAT system", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class)) })
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response findById(@PathParam("groupKey") Long groupKey, @Context Request request,
			@Context HttpServletRequest httpRequest) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Identity identity = RestSecurityHelper.getIdentity(httpRequest);
		if(!isGroupManager(httpRequest) && !bgs.isIdentityInBusinessGroup(identity, bg)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Date lastModified = bg.getLastModified();
		Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
		if(response == null) {
			GroupVO vo = ObjectFactory.get(bg);
			response = Response.ok(vo);
		}
		return response.build();
	}
	
	/**
	 * Create a group.
	 * 
	 * @param groupKey The key of the group
	 * @param group The group
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Operation(summary = "Create a group", description = "Create a group")
	@ApiResponse(responseCode = "200", description = "The saved business group", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createGroup(final GroupVO group, @Context HttpServletRequest request) {
		Identity identity = RestSecurityHelper.getIdentity(request);
		if(identity == null || !isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		if(group.getKey() != null && group.getKey().longValue() > 0) {
			return postGroup(group.getKey(), group, request);
		}
		
		if(!StringHelper.containsNonWhitespace(group.getName())) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}


		Integer minPart = normalize(group.getMinParticipants());
		Integer maxPart = normalize(group.getMaxParticipants());
		BusinessGroup newBG = bgs.createBusinessGroup(identity, group.getName(), group.getDescription(), BusinessGroup.BUSINESS_TYPE,
				group.getExternalId(), group.getManagedFlags(), minPart, maxPart, false, false, null);
		GroupVO savedVO = ObjectFactory.get(newBG);
		return Response.ok(savedVO).build();
	}
	
	/**
	 * Updates a group.
	 * 
	 * @param groupKey The key of the group
	 * @param group The group
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}")
	@Operation(summary = "Update a group", description = "Update a group")
	@ApiResponse(responseCode = "200", description = "The saved business group", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postGroup(@PathParam("groupKey") Long groupKey, final GroupVO group, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		final BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!StringHelper.containsNonWhitespace(group.getName())) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		Identity identity = RestSecurityHelper.getIdentity(request);
		BusinessGroup mergedBg = bgs.updateBusinessGroup(identity, bg, group.getName(), group.getDescription(),
				group.getExternalId(), group.getManagedFlags(), normalize(group.getMinParticipants()), normalize(group.getMaxParticipants()));
		//save the updated group
		GroupVO savedVO = ObjectFactory.get(mergedBg);
		return Response.ok(savedVO).build();
	}
	
	/**
	 * Returns the  news.
	 * 
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/news")
	@Operation(summary = "Returns the news", description = "Returns the news")
	@ApiResponse(responseCode = "200", description = "The news", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = String.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = String.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found or the news tool is not enabled")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getNews(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}

		CollaborationTools tools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		if(tools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			String news = tools.lookupNews();
			if(news == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			return Response.ok(news).build();
		} else {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
	}
	
	/**
	 * Update the news.
	 * 
	 * @param groupKey The key of the group
	 * @param news The news
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}/news")
	@Operation(summary = "Update the news", description = "Update the news")
	@ApiResponse(responseCode = "200", description = "The updated news", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = String.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = String.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found or the news tool is not enabled")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response postNews(@PathParam("groupKey") Long groupKey, @FormParam("news") String news, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CollaborationTools tools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		if(tools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			tools.saveNews(news);
			return Response.ok(news).build();
		} else {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
	}
	
	/**
	 * Deletes the news of the group if the news tool is enabled.
	 * 
	 * @param groupKey The key of the group
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}/news")
	@Operation(summary = "Deletes the news of the group if the news tool is enabled", description = "Deletes the news of the group if the news tool is enabled")
	@ApiResponse(responseCode = "200", description = "The new are deleted")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found or the news tool is not enabled")
	public Response deleteNews(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CollaborationTools tools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		if(tools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			tools.saveNews(null);
			return Response.ok().build();
		} else {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
	}
	
	@GET
	@Path("{groupKey}/configuration")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Operation(summary = "Get configuration", description = "Get configuration")
	@ApiResponse(responseCode = "200", description = "Configuration")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Group not found")
	public Response getGroupConfiguration(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		GroupConfigurationVO configuration = new GroupConfigurationVO();
		configuration.setOwnersPublic(bg.isOwnersVisiblePublic());
		configuration.setOwnersVisible(bg.isOwnersVisibleIntern());
		configuration.setParticipantsPublic(bg.isParticipantsVisiblePublic());
		configuration.setParticipantsVisible(bg.isParticipantsVisibleIntern());
		configuration.setWaitingListPublic(bg.isWaitingListVisiblePublic());
		configuration.setWaitingListVisible(bg.isWaitingListVisibleIntern());
		
		String[] availableTools = collaborationToolsFactory.getAvailableTools().clone();
		CollaborationTools tools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		
		String news = tools.lookupNews();
		configuration.setNews(news);
		
		List<String> toolsList = new ArrayList<>();
		Map<String, Integer> toolsAccess = new HashMap<>();
		for (int i=availableTools.length; i-->0; ) {
			String tool = availableTools[i];
			if(tools.isToolEnabled(tool)) {
				toolsList.add(tool);
				Long access = tools.getToolAccess(tool);
				if(access != null) {
					toolsAccess.put(tool, Integer.valueOf(access.intValue()));
				}
			}
		}
		configuration.setTools(toolsList.toArray(new String[toolsList.size()]));
		configuration.setToolsAccess(toolsAccess);
		
		return Response.ok(configuration).build();
	}
	
	@POST
	@Path("{groupKey}/configuration")
	@Operation(summary = "Post configuration", description = "Post configuration")
	@ApiResponse(responseCode = "200", description = "Configuration posted")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Group not found")
	public Response postGroupConfiguration(@PathParam("groupKey") Long groupKey, final GroupConfigurationVO group, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String[] selectedTools = group.getTools();
		if(selectedTools == null) {
			selectedTools = new String[0];
		}
		String[] availableTools = collaborationToolsFactory.getAvailableTools().clone();
		CollaborationTools tools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		for (int i=availableTools.length; i-->0; ) {
			boolean enable = false;
			String tool = availableTools[i];
			for(String selectedTool:selectedTools) {
				if(tool.equals(selectedTool)) {
					enable = true;
				}
			}
			tools.setToolEnabled(tool, enable);
		}
		
		Map<String,Integer> toolsAccess = group.getToolsAccess();
		if (toolsAccess != null) {
			// ignore null for backward compatibility, don't change current configuration
			for (String tool : toolsAccess.keySet()) {
				tools.setToolAccess(tool, toolsAccess.get(tool));
			}
		}
		
		if(StringHelper.containsNonWhitespace(group.getNews())) {
			tools.saveNews(group.getNews());
		}
		
		boolean ownersIntern = bg.isOwnersVisibleIntern();
		if(group.getOwnersVisible() != null) {
			ownersIntern = group.getOwnersVisible().booleanValue();
		}
		boolean participantsIntern = bg.isParticipantsVisibleIntern();
		if(group.getParticipantsVisible() != null) {
			participantsIntern = group.getParticipantsVisible().booleanValue();
		}
		boolean waitingListIntern = bg.isWaitingListVisibleIntern();
		if(group.getWaitingListVisible() != null) {
			waitingListIntern = group.getWaitingListVisible().booleanValue();
		}
		boolean ownersPublic = bg.isOwnersVisiblePublic();
		if(group.getOwnersPublic() != null) {
			ownersPublic = group.getOwnersPublic().booleanValue();
		}
		boolean participantsPublic = bg.isParticipantsVisiblePublic();
		if(group.getParticipantsPublic() != null) {
			participantsPublic = group.getParticipantsPublic().booleanValue();
		}
		boolean waitingListPublic = bg.isWaitingListVisiblePublic();
		if(group.getWaitingListPublic() != null) {
			waitingListPublic = group.getWaitingListPublic().booleanValue();
		}
		bgs.updateDisplayMembers(bg,
				ownersIntern, participantsIntern, waitingListIntern,
				ownersPublic, participantsPublic, waitingListPublic,
				bg.isDownloadMembersLists());
		return Response.ok().build();
	}
	
	/**
	 * Deletes the business group specified by the groupKey.
	 * 
	 * @param groupKey The key of the group
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}")
	@Operation(summary = "Deletes the business group specified by the groupKey", description = "Deletes the business group specified by the groupKey")
	@ApiResponse(responseCode = "200", description = "The business group is deleted")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found or the news tool is not enabled")
	public Response deleteGroup(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		bgs.deleteBusinessGroup(bg);
		return Response.ok().build();
	}
	
	/**
	 * Returns the informations of the group specified by the groupKey.
	 * 
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/infos")
	@Operation(summary = " Returns the informations of the group specified by the groupKey", description = " Returns the informations of the group specified by the groupKey")
	@ApiResponse(responseCode = "200", description = "The updated news", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GroupInfoVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupInfoVO.class)) })
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getInformations(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		Identity identity = RestSecurityHelper.getIdentity(request);
		if(!isGroupManager(request)) {
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}
		
		GroupInfoVO info = getInformation(identity, bg);
		return Response.ok(info).build();
	}
	
	/**
	 * Return the Forum web service
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@Path("{groupKey}/forum")
	@Operation(summary = "Return the Forum web service", description = "Return the Forum web service")
	public ForumWebService getForum(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
			Forum forum = collabTools.getForum();
			ForumWebService ws = new ForumWebService(forum);
			CoreSpringFactory.autowireObject(ws);
			return ws;
		}
		return null;
	}
	
	@Path("{groupKey}/folder")
	@Operation(summary = "Return the folder", description = "Return the folder")
	public VFSWebservice getFolder(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		if(!collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			return null;
		}
		
		String relPath = collabTools.getFolderRelPath();
		Quota folderQuota = qm.getCustomQuota(relPath);
		if (folderQuota == null) {
			Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
			folderQuota = qm.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		SubscriptionContext subsContext = null;
		VFSSecurityCallback secCallback = new VFSWebServiceSecurityCallback(true, true, true, folderQuota, subsContext);
		LocalFolderImpl rootContainer = VFSManager.olatRootContainer(relPath, null);
		rootContainer.setLocalSecurityCallback(secCallback);
		return new VFSWebservice(rootContainer);
	}
	
	
	/**
	 * Return the Forum web service
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@Path("{groupKey}/wiki")
	@Operation(summary = "Return the Forum web service", description = "Return the Forum web service")
	public GroupWikiWebService getWiki(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI)) {
			return new GroupWikiWebService(bg);
		}
		return null;
	}
	
	/**
	 * Return the callendar web service
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@Path("{groupKey}/calendar")
	@Operation(summary = "Return the calendar web service", description = "Return the calendar web service")
	public CalWebService getCalendarWebService(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(!calendarModule.isEnabled() || !calendarModule.isEnableGroupCalendar()) {
			return null;
		}
		
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = collaborationToolsFactory.getOrCreateCollaborationTools(bg);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) {
			UserRequest ureq = getUserRequest(request);
			KalendarRenderWrapper calendar = collaborationManager.getCalendar(bg, ureq, true);
			return new CalWebService(calendar);
		}
		return null;
	}

	
	/**
	 * Returns the list of owners of the group specified by the groupKey.
	 * 
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/owners")
	@Operation(summary = "Returns the list of owners of the group specified by the groupKey", description = "Returns the list of owners of the group specified by the groupKey")
	@ApiResponse(responseCode = "200", description = "Owners of the business group", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTutors(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			if(!bg.isOwnersVisibleIntern()) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}
		
		List<Identity> coaches = bgs.getMembers(bg, GroupRoles.coach.name());
		return getIdentityInGroup(coaches);
	}
	
	/**
	 * Returns the list of participants of the group specified by the groupKey.
	 * 
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/participants")
	@Operation(summary = "Returns the list of participants of the group specified by the groupKey", description = "Returns the list of participants of the group specified by the groupKey")
	@ApiResponse(responseCode = "200", description = "Participants of the business group", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getParticipants(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			if(!bg.isParticipantsVisibleIntern()) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}

		List<Identity> participants = bgs.getMembers(bg, GroupRoles.participant.name());
		return getIdentityInGroup(participants);
	}
	
	private Response getIdentityInGroup(List<Identity> identities) {
		int count = 0;
		UserVO[] ownerVOs = new UserVO[identities.size()];
		for(int i=0; i<identities.size(); i++) {
			ownerVOs[count++] = UserVOFactory.get(identities.get(i));
		}
		return Response.ok(ownerVOs).build();
	}
	
	/**
	 * Adds an owner to the group.
	 * 
	 * @param groupKey The key of the group 
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("{groupKey}/owners/{identityKey}")
	@Operation(summary = "Add an owner to the group", description = "Adds an owner to the group")
	@ApiResponse(responseCode = "200", description = "The user is added as owner of the group")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	public Response addTutor(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			final BusinessGroup group = bgs.loadBusinessGroup(groupKey);
			final Identity identity = securityManager.loadIdentityByKey(identityKey, false);
			if(identity == null || group == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}

			bgs.addOwners(ureq.getIdentity(), ureq.getUserSession().getRoles(), Collections.singletonList(identity), group, null);
			return Response.ok().build();
		} catch (Exception e) {
			log.error("Trying to add an owner to a group", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Removes the owner from the group.
	 * 
	 * @param groupKey The key of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}/owners/{identityKey}")
	@Operation(summary = "Removes the owner from the group", description = "Removes the owner from the group")
	@ApiResponse(responseCode = "200", description = "The user is removed as owner from the group")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group or the user cannot be found")
	public Response removeTutor(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			final BusinessGroup group = bgs.loadBusinessGroup(groupKey);
			final Identity identity = securityManager.loadIdentityByKey(identityKey, false);
			if(identity == null || group == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			bgs.removeOwners(ureq.getIdentity(), Collections.singletonList(identity), group);
			return Response.ok().build();
		} catch (Exception e) {
			log.error("Trying to remove an owner to a group", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Adds a participant to the group.
	 * 
	 * @param groupKey The key of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("{groupKey}/participants/{identityKey}")
	@Operation(summary = "Adds a participant to the group", description = "Adds a participant to the group")
	@ApiResponse(responseCode = "200", description = "The user is added as participant of the group")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group or the user cannot be found")
	public Response addParticipant(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			final BusinessGroup group = bgs.loadBusinessGroup(groupKey);
			final Identity identity = securityManager.loadIdentityByKey(identityKey, false);
			if(identity == null || group == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}

			BusinessGroupAddResponse state = bgs.addParticipants(ureq.getIdentity(), ureq.getUserSession().getRoles(), Collections.singletonList(identity), group, null);
			if(state.getAddedIdentities().contains(identity)) {
				return Response.ok().build();
			} else if(state.getIdentitiesAlreadyInGroup().contains(identity)) {
				return Response.ok().status(Status.NOT_MODIFIED).build();
			}
			return Response.serverError().status(Status.PRECONDITION_FAILED).build();
		} catch (Exception e) {
			log.error("Trying to add a participant to a group", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Removes a participant from the group.
	 * 
	 * @param groupKey The key of the group
	 * @param identityKey The id of the user
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}/participants/{identityKey}")
	@Operation(summary = "Removes a participant from the group", description = "Removes a participant from the group")
	@ApiResponse(responseCode = "200", description = "The user is remove from the group as participant")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group or the user cannot be found")
	public Response removeParticipant(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			final BusinessGroup group = bgs.loadBusinessGroup(groupKey);
			final Identity identity = securityManager.loadIdentityByKey(identityKey, false);
			if(identity == null || group == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			bgs.removeParticipants(ureq.getIdentity(), Collections.singletonList(identity), group, null);

			return Response.ok().build();
		} catch (Exception e) {
			log.error("Trying to remove a participant to a group", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * @param integer
	 * @return value bigger or equal than 0
	 */
	private static final Integer normalize(Integer integer) {
		if(integer == null) return null;
		if(integer.intValue() <= 0) return null;
		return integer;
	}
}
