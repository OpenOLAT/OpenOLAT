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

package org.olat.modules.fo.restapi;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.manager.ForumManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Description:<br>
 * Web service to manage forums.
 * 
 * <P>
 * Initial Date:  26 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Tag(name = "Repo")
@Component

@Path("repo/forums")
public class ForumImportWebService {
	
	private static final String VERSION  = "1.0";
	
	@Autowired
	private ForumManager forumManager;
	
	/**
	 * The version of the Forum Web Service
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the Forum Web Service", description = "The version of the Forum Web Service")
	@ApiResponse(responseCode = "200", description = "The version of the Forum Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Web service to manage a forum
	 * @param forumKey The key of the forum
	 * @return
	 */
	@Path("{forumKey}")
	@Operation(summary = "Web service to manage a forum", description = "Web service to manage a forum")
	public ForumWebService getForumWebservice(@PathParam("forumKey") Long forumKey) {
		Forum forum = forumManager.loadForum(forumKey);
		ForumWebService ws = new ForumWebService(forum);
		CoreSpringFactory.autowireObject(ws);
		return ws;
	}
}
