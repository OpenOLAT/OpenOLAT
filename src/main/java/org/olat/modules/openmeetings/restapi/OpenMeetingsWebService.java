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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.restapi;

import java.io.File;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserManager;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 13.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Component
@Path("openmeetings")
public class OpenMeetingsWebService {

	private static final CacheControl cc = new CacheControl();
	
	/**
	 * Retrieves the portrait of an user
	 * @response.representation.200.mediaType application/octet-stream
	 * @response.representation.200.doc The portrait as image
   * @response.representation.404.doc The identity or the portrait not found
	 * @param identityToken The identity key of the user being searched
	 * @param request The REST request
	 * @return The image
	 */
	@GET
	@Path("{identityToken}/portrait")
	@Produces({"image/jpeg","image/jpg",MediaType.APPLICATION_OCTET_STREAM})
	public Response getPortrait(@PathParam("identityToken") String identityToken, @Context Request request) {
		OpenMeetingsModule module = CoreSpringFactory.getImpl(OpenMeetingsModule.class);
		if(!module.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		OpenMeetingsManager omm = CoreSpringFactory.getImpl(OpenMeetingsManager.class);
		Long identityKey = omm.getIdentityKey(identityToken);
		if(identityKey == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		String username = CoreSpringFactory.getImpl(UserManager.class).getUsername(identityKey);
		if(username == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		File portrait = CoreSpringFactory.getImpl(DisplayPortraitManager.class).getBigPortrait(username);
		if(portrait == null || !portrait.exists()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		Date lastModified = new Date(portrait.lastModified());
		Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
		if(response == null) {
			response = Response.ok(portrait).lastModified(lastModified).cacheControl(cc);
		}
		return response.build();
	}
}
