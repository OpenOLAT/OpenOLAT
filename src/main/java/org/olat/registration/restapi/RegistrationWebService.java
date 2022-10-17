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

import static org.olat.restapi.security.RestSecurityHelper.getLocale;

import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.registration.RegistrationController;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.registration.TemporaryKey;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


/**
 * 
 * Description:<br>
 * Web service to trigger the registration process
 * 
 * <P>
 * Initial Date:  14 juil. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Component
@Path("registration")
public class RegistrationWebService {
	
	private static final Logger log = Tracing.createLoggerFor(RegistrationWebService.class);

	private static final String SEPARATOR = "____________________________________________________________________\n";
	
	/**
	 * Register with the specified email
	 * 
   * @param email The email address
   * @param request The HTTP Request
	 * @return
	 */
	@POST
	@Operation(summary = "Register with the specified email", description = "Register with the specified email")
	@ApiResponse(responseCode = "200", description = "Registration successful")
	@ApiResponse(responseCode = "304", description = "Already registered, HTTP-Header location set to redirect")
	public Response registerPost(@FormParam("email") String email, @Context HttpServletRequest request) {
		return register(email, request);
	}
	
	/**
	 * Register with the specified email
	 * 
   * @param email The email address
   * @param request The HTTP Request
	 * @return
	 */
	@PUT
	@Operation(summary = "Register with the specified email", description = "Register with the specified email")
	@ApiResponse(responseCode = "200", description = "Registration successful")
	@ApiResponse(responseCode = "304", description = "Already registered, HTTP-Header location set to redirect")
	public Response register(@QueryParam("email") @Parameter(description = "The email address") String email, @Context HttpServletRequest request) {
		if (!CoreSpringFactory.getImpl(RegistrationModule.class).isSelfRegistrationEnabled()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		ResponseBuilder response;
		Locale locale = getLocale(request);
		Translator translator = getTranslator(locale);
		
		MailManager mailM = CoreSpringFactory.getImpl(MailManager.class);
		UserManager userManager = UserManager.getInstance();
		RegistrationManager rm = CoreSpringFactory.getImpl(RegistrationManager.class);
		RegistrationModule rModule = CoreSpringFactory.getImpl(RegistrationModule.class);
		
		boolean foundUser = userManager.findUniqueIdentityByEmail(email) != null;
		boolean noNewUserWithEmail = !userManager.isEmailAllowed(email);

		String serverpath = Settings.getServerContextPathURI();
		if (foundUser && noNewUserWithEmail) {
			//redirect
			URI redirectUri = UriBuilder.fromUri(Settings.getServerContextPathURI()).build();
			response = Response.ok().status(Status.NOT_MODIFIED).location(redirectUri);
		} else if (userManager.isEmailAllowed(email)) {
			String ip = request.getRemoteAddr();
			TemporaryKey tk = null;
			UserModule userModule = CoreSpringFactory.getImpl(UserModule.class);
			if (userModule.isEmailUnique()) {
				tk = rm.loadTemporaryKeyByEmail(email);
			}
			if (tk == null) {
				tk = rm.loadOrCreateTemporaryKeyByEmail(email, ip, RegistrationManager.REGISTRATION, rModule.getValidUntilHoursRest());
			}
			String today = DateFormat.getDateInstance(DateFormat.LONG, locale).format(new Date());
			String[] bodyAttrs = new String[] {
					serverpath,
					tk.getRegistrationKey(),
					CoreSpringFactory.getImpl(I18nModule.class).getLocaleKey(locale)
			};
			String[] whereFromAttrs = new String [] { serverpath, today, ip };
			String body = translator.translate("reg.body", bodyAttrs) + SEPARATOR + translator.translate("reg.wherefrom", whereFromAttrs);
			try {
				MailBundle bundle = new MailBundle();
				bundle.setTo(email);
				bundle.setContent(translator.translate("reg.subject"), body);
				MailerResult result = mailM.sendExternMessage(bundle, null, true);
				if (result.isSuccessful()) {
					response = Response.ok();
				} else {
					response = Response.serverError().status(Status.INTERNAL_SERVER_ERROR);
				}
			} catch (Exception e) {
				response = Response.serverError().status(Status.INTERNAL_SERVER_ERROR);
				log.error("", e);
			}
		} else {
			response = Response.serverError().status(Status.BAD_REQUEST);
		}
		
		return response.build();
	}
	
	private Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(RegistrationController.class, locale);
	}

}
