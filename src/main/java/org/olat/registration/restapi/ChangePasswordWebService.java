/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.registration.restapi;

import static org.olat.restapi.security.RestSecurityHelper.isUserManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.user.UserModule;
import org.springframework.stereotype.Component;


/**
 * Webservice to create a temporary key to change the password
 * 
 * Initial date: 15.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Component
@Path("pwchange")
public class ChangePasswordWebService {
	
/**
 * 
 * @param identityKey
 * @param request
 * @return
 */
	@PUT
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response register(@QueryParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		if(!isUserManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(!CoreSpringFactory.getImpl(UserModule.class).isPwdChangeAllowed(identity)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		RegistrationManager rm = CoreSpringFactory.getImpl(RegistrationManager.class);
		String emailAdress = identity.getUser().getProperty(UserConstants.EMAIL, null); 
		String ip = request.getRemoteAddr();
		TemporaryKey tk = rm.createAndDeleteOldTemporaryKey(identity.getKey(), emailAdress, ip, RegistrationManager.PW_CHANGE);

		return Response.ok(new TemporaryKeyVO(tk)).build();
	}
}
