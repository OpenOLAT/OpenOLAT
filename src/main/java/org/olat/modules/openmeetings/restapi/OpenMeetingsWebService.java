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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.user.DisplayPortraitManager;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 13.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Hidden
@Component
@Path("openmeetings")
public class OpenMeetingsWebService {

	private static final CacheControl cc = new CacheControl();
	
	/**
	 * Retrieves the portrait of an user
	 * 
	 * @param identityToken The identity key of the user being searched
	 * @param request The REST request
	 * @return The image
	 */
	@GET
	@Path("{identityToken}/portrait")
	@Operation(summary = "Retrieve the portrait of an user", description = "Retrieves the portrait of an user")
	@ApiResponse(responseCode = "200", description = "The portrait as image")
	@ApiResponse(responseCode = "404", description = "The identity or the portrait not found")
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
		Identity identity = CoreSpringFactory.getImpl(BaseSecurity.class).loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		File portrait = CoreSpringFactory.getImpl(DisplayPortraitManager.class).getBigPortrait(identity);
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
