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

import static org.olat.restapi.security.RestSecurityHelper.isGroupManager;
import static org.olat.restapi.support.ObjectFactory.getInformation;

import java.util.Collections;
import java.util.Date;
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

import org.olat.admin.quota.QuotaConstants;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.restapi.VFSWebServiceSecurityCallback;
import org.olat.core.commons.services.vfs.restapi.VFSWebservice;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
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
import org.springframework.stereotype.Component;


/**
 * Description:<br>
 * This handles the learning groups.
 * 
 * <P>
 * Initial Date:  23 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Component
@Path("groups")
public class LearningGroupWebService {
	
	private OLog log = Tracing.createLoggerFor(LearningGroupWebService.class);
	
	private static final String VERSION = "1.0";
	
	private static CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	/**
	 * Retrieves the version of the Group Web Service.
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
	 * Return the list of all groups if you have group manager permission, or all
	 * learning group that you particip with or owne.
	 * @response.representation.200.qname {http://www.example.com}groupVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc This is the list of all groups in OLAT system
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVOes}
	 * @param externalId Search with an external ID
	 * @param managed (true / false) Search only managed / not managed groups 
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getGroupList(@QueryParam("externalId") String externalId, @QueryParam("managed") Boolean managed,
			@Context HttpServletRequest request) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
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
	 * @response.representation.200.qname {http://www.example.com}groupVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc A business group in the OLAT system
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVO}
	 * @param groupKey The key of the group
	 * @param request The REST request
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Path("{groupKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response findById(@PathParam("groupKey") Long groupKey, @Context Request request,
			@Context HttpServletRequest httpRequest) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(groupKey);
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
	 * @response.representation.qname {http://www.example.com}groupVO
   * @response.representation.mediaType application/xml, application/json
   * @response.representation.doc A business group in the OLAT system
   * @response.representation.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVO}
	 * @response.representation.200.qname {http://www.example.com}groupVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The saved business group
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param group The group
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createGroup(final GroupVO group, @Context HttpServletRequest request) {
		Identity identity = RestSecurityHelper.getIdentity(request);
		if(identity == null || !isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		final BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		if(group.getKey() != null && group.getKey().longValue() > 0) {
			return postGroup(group.getKey(), group, request);
		}
		
		if(!StringHelper.containsNonWhitespace(group.getName())) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}


		Integer minPart = normalize(group.getMinParticipants());
		Integer maxPart = normalize(group.getMaxParticipants());
		BusinessGroup newBG = bgs.createBusinessGroup(identity, group.getName(), group.getDescription(),
				group.getExternalId(), group.getManagedFlags(), minPart, maxPart, false, false, null);
		GroupVO savedVO = ObjectFactory.get(newBG);
		return Response.ok(savedVO).build();
	}
	
	/**
	 * Updates a group.
	 * @response.representation.qname {http://www.example.com}groupVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc A business group in the OLAT system
	 * @response.representation.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVO}
	 * @response.representation.200.qname {http://www.example.com}groupVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The saved business group
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param group The group
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postGroup(@PathParam("groupKey") Long groupKey, final GroupVO group, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		final BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
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
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group cannot be found or the news tool is not enabled
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/news")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getNews(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}

		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
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
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group cannot be found or the news tool is not enabled
	 * @param groupKey The key of the group
	 * @param news The news
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}/news")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response postNews(@PathParam("groupKey") Long groupKey, @FormParam("news") String news, @Context HttpServletRequest request) {
		BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(tools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			tools.saveNews(news);
			return Response.ok(news).build();
		} else {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
	}
	
	/**
	 * Deletes the news of the group if the news tool is enabled.
	 * @response.representation.200.doc The news are deleted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group cannot be found or hte news tool is not enabled
	 * @param groupKey The key of the group
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}/news")
	public Response deleteNews(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(tools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			tools.saveNews(null);
			return Response.ok().build();
		} else {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
	}
	
	@POST
	@Path("{groupKey}/configuration")
	public Response postGroupConfiguration(@PathParam("groupKey") Long groupKey, final GroupConfigurationVO group, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		final BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String[] selectedTools = group.getTools();
		if(selectedTools == null) {
			selectedTools = new String[0];
		}
		String[] availableTools = CollaborationToolsFactory.getInstance().getAvailableTools().clone();
		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
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
		bg = bgs.updateDisplayMembers(bg,
				ownersIntern, participantsIntern, waitingListIntern,
				ownersPublic, participantsPublic, waitingListPublic,
				bg.isDownloadMembersLists());

		return Response.ok().build();
	}
	
	/**
	 * Deletes the business group specified by the groupKey.
	 * @response.representation.200.doc The business group is deleted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}")
	public Response deleteGroup(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BusinessGroup bg = bgs.loadBusinessGroup(groupKey);
		if(bg == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		bgs.deleteBusinessGroup(bg);
		return Response.ok().build();
	}
	
	/**
	 * Returns the informations of the group specified by the groupKey.
	 * @response.representation.200.qname {http://www.example.com}groupInfoVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc Participants of the business group
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_GROUPINFOVO}
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/infos")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getInformations(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
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
	public ForumWebService getForum(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(groupKey);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
			Forum forum = collabTools.getForum();
			ForumWebService ws = new ForumWebService(forum);
			CoreSpringFactory.autowireObject(ws);
			return ws;
		}
		return null;
	}
	
	@Path("{groupKey}/folder")
	public VFSWebservice getFolder(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
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
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(!collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			return null;
		}
		
		String relPath = collabTools.getFolderRelPath();
		QuotaManager qm = CoreSpringFactory.getImpl(QuotaManager.class);
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
	public GroupWikiWebService getWiki(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
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
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI)) {
			return new GroupWikiWebService(bg);
		}
		return null;
	}
	
	/**
	 * Returns the list of owners of the group specified by the groupKey.
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc Owners of the business group
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/owners")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTutors(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
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
		
		List<Identity> coaches = CoreSpringFactory.getImpl(BusinessGroupService.class)
				.getMembers(bg, GroupRoles.coach.name());
		return getIdentityInGroup(coaches);
	}
	
	/**
	 * Returns the list of participants of the group specified by the groupKey.
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc Participants of the business group
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.404.doc The business group cannot be found
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@GET
	@Path("{groupKey}/participants")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getParticipants(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
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

		List<Identity> participants = CoreSpringFactory.getImpl(BusinessGroupService.class)
				.getMembers(bg, GroupRoles.participant.name());
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
	 * @response.representation.200.doc The user is added as owner of the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group 
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("{groupKey}/owners/{identityKey}")
	public Response addTutor(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			final BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			final BusinessGroup group = bgs.loadBusinessGroup(groupKey);
			final Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
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
	 * @response.representation.200.doc The user is removed as owner from the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}/owners/{identityKey}")
	public Response removeTutor(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			final BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			final BusinessGroup group = bgs.loadBusinessGroup(groupKey);
			final Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
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
	 * @response.representation.200.doc The user is added as participant of the group
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The user's id
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("{groupKey}/participants/{identityKey}")
	public Response addParticipant(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			final BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			final BusinessGroup group = bgs.loadBusinessGroup(groupKey);
			final Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
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
	 * @response.representation.200.doc The user is remove from the group as participant
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The business group or the user cannot be found
	 * @param groupKey The key of the group
	 * @param identityKey The id of the user
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}/participants/{identityKey}")
	public Response removeParticipant(@PathParam("groupKey") Long groupKey, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		try {
			if(!isGroupManager(request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			
			final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
			final BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			final BusinessGroup group = bgs.loadBusinessGroup(groupKey);
			final Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
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
