/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.dispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.servlets.RequestAbortedException;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.dispatcher.AuthenticatedDispatcher;
import org.olat.dispatcher.DMZDispatcher;
import org.olat.login.LoginAuthprovidersController;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Person;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * Initial date: 24 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("refereedashboarddispatcher")
public class RefereeDashboardDispatcher implements Dispatcher {
	
	public static final String REFEREE_DASHBOARD_SOURCE = "refereedashboard";
	public static final String REFEREE_DASHBOARD_APP = "refereedashboard-app";

	private static final Logger log = Tracing.createLoggerFor(RefereeDashboardDispatcher.class);
	
	public static final String DISPATCHER_SOURCE = "rt-dispatcher";
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private OrganisationService organisationService;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		//
		// create a ContextEntries String which can be used to create a BusinessControl -> move to 
		//
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String restPart = origUri.substring(uriPrefix.length());
		try {
			restPart = URLDecoder.decode(restPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}

		// create the olat ureq and get an associated main window to spawn the "tab"
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		if(usess != null) {
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
			usess.putEntryInNonClearedStore(DISPATCHER_SOURCE, "refereedashboard");
		}
		UserRequest ureq = null;
		try {
			//upon creation URL is checked for 
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException | RequestAbortedException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(log.isDebugEnabled()){
				log.debug("Bad Request {}", request.getPathInfo());
			}
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}

		String appUuid = getApplicationUUID(ureq);
		Application app = CoreSpringFactory.getImpl(RecruitingService.class).getApplicationByApplicantKey(appUuid);
		if(app != null) {
			Authentication authentication = CoreSpringFactory.getImpl(BaseSecurity.class)
				.findAuthenticationByAuthusername(app.getPerson().getMail(), "OLAT", BaseSecurity.DEFAULT_ISSUER);
			if(authentication == null && app.getIdentity() == null) {
				app = createIdentity(app);
			} else if(authentication != null && app.getIdentity() == null) {
				app = activateApplication(app, authentication);
			}
			redirectDashboard(ureq, app, response);
		} else {
			DispatcherModule.redirectToDefaultDispatcher(response);
		}
	}
	
	private void redirectDashboard(UserRequest ureq, Application app, HttpServletResponse response) {
		String dmzBusinessPath = "[RefereeDashboard:" + app.getKey() + "]";
		ureq.getUserSession().putEntryInNonClearedStore(DMZDispatcher.DMZDISPATCHER_BUSINESSPATH, dmzBusinessPath);
		String businessPath = "[Positions:0][MyApplication:" + app.getKey() + "]";
		ureq.getUserSession().putEntryInNonClearedStore(AuthenticatedDispatcher.AUTHDISPATCHER_BUSINESSPATH, businessPath);
		ureq.getUserSession().putEntryInNonClearedStore(LoginAuthprovidersController.ATTR_LOGIN_PROVIDER, "OLAT");
		ureq.getUserSession().putEntryInNonClearedStore(REFEREE_DASHBOARD_APP, app);
		DispatcherModule.redirectToDefaultDispatcher(response);
	}
	
	private Application activateApplication(Application app, Authentication authentication) {
		app.setIdentity(authentication.getIdentity());
		app = recruitingService.saveApplication(app);
		return app;
	}
	
	private Application createIdentity(Application app) {
		// Create a new user
		Person person = app.getPerson();
		User newUser = userManager.createUser(person.getFirstName(), person.getLastName(), person.getMail());
		Identity identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(null, person.getMail(), null, newUser, null, null, null, null, null, null, null, null);
		app.setIdentity(identity);
		organisationService.addMember(identity, OrganisationRoles.invitee, identity);
		return recruitingService.saveApplication(app);
	}
	
	private String getApplicationUUID(UserRequest ureq) {
		String requestUri = ureq.getHttpReq().getRequestURI();
		String uriPrefix = ureq.getUriPrefix();
		if(uriPrefix.length() < requestUri.length()) {
			requestUri = requestUri.substring(uriPrefix.length());
		}
		
		if(requestUri.startsWith("/")) {
			requestUri = requestUri.substring(1, requestUri.length());
		}
		
		int slashFromRestUrlIndex = requestUri.indexOf('/');
		if(slashFromRestUrlIndex > 0 && slashFromRestUrlIndex + 1 < requestUri.length()) {
			requestUri = requestUri.substring(slashFromRestUrlIndex + 1, requestUri.length());
		}
		return requestUri;
	}
}