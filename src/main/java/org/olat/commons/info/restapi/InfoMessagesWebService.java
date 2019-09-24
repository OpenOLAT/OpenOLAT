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

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
	 * Creates a new info message
	 * @response.representation.200.qname {http://www.example.com}infoMessageVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The info message
	 * @response.representation.200.example {@link org.olat.commons.info.restapi.Examples#SAMPLE_INFOMESSAGEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
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
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createEmptyCourse(final @QueryParam("resName") String resName,
			final @QueryParam("resId") Long resId, @QueryParam("resSubPath") String resSubPath,
			@QueryParam("businessPath") String businessPath, @QueryParam("authorKey") Long authorKey,
			@QueryParam("title") String title, @QueryParam("message") String message,
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
