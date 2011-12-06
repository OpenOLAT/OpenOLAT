/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.modules.fo.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.isAdmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Subscriber;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.SearchBusinessGroupParams;
import org.olat.modules.fo.Forum;
import org.olat.restapi.support.MediaTypeVariants;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Path("users/{identityKey}/forums")
public class MyForumsWebService {

	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getForums(@PathParam("identityKey") Long identityKey,  @QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		
		Identity retrievedUser = getIdentity(httpRequest);
		if(retrievedUser == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if(!identityKey.equals(retrievedUser.getKey())) {
			if(isAdmin(httpRequest)) {
				retrievedUser = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey);
			} else {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}
		
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			SearchBusinessGroupParams params = new SearchBusinessGroupParams();
			params.addTypes(BusinessGroup.TYPE_BUDDYGROUP, BusinessGroup.TYPE_LEARNINGROUP);
			params.addTools(CollaborationTools.TOOL_FORUM);

			int totalCount = bgm.countBusinessGroups(params, retrievedUser, true, true, null);
			Set<Long> subscriptions = new HashSet<Long>();
			if(totalCount > 0) {
				NotificationsManager man = NotificationsManager.getInstance();
				List<String> notiTypes = Collections.singletonList("Forum");
				List<Subscriber> subs = man.getSubscribers(retrievedUser, notiTypes);
				for(Subscriber sub:subs) {
					if("BusinessGroup".equals(sub.getPublisher().getResName())) {
						subscriptions.add(sub.getPublisher().getResId());
					}
				}
			}
			
			List<ForumVO> forumVOs = new ArrayList<ForumVO>();
			List<BusinessGroup> groups = bgm.findBusinessGroups(params, retrievedUser, true, true, null, start, limit);
			for(BusinessGroup group:groups) {
				CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
				if(collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
					Forum forum = collabTools.getForum();
					ForumVO forumVo = new ForumVO(forum);
					forumVo.setName(group.getName());
					forumVo.setGroupKey(group.getKey());
					forumVo.setSubscribed(subscriptions.contains(group.getKey()));
					forumVOs.add(forumVo);
				}
			}
			
			ForumVOes voes = new ForumVOes();
			voes.setForums(forumVOs.toArray(new ForumVO[forumVOs.size()]));
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
	}

}
