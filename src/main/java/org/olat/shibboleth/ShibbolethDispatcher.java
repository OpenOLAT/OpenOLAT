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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.shibboleth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.restapi.security.RestSecurityBean;
import org.olat.shibboleth.util.ShibbolethAttribute;
import org.olat.shibboleth.util.ShibbolethHelper;
import org.olat.user.UserManager;

/**
 * Initial Date:  17.07.2004
 *
 * @author Mike Stock
 */
public class ShibbolethDispatcher implements Dispatcher{
	
	private static final OLog log = Tracing.createLoggerFor(ShibbolethDispatcher.class);

	/** Provider identifier */
	public static final String PROVIDER_SHIB = "Shib";
	/** Identifies requests for the ShibbolethDispatcher */
	public static final String PATH_SHIBBOLETH = "/shib/";
	
	private Translator translator;
	private boolean mobile = false;
	private BaseSecurity securityManager;
	private ShibbolethModule shibbolethModule;
	private RestSecurityBean restSecurityBean;
	private UserDeletionManager userDeletionManager;
	
	
	/**
	 * [used by Spring]
	 * @param mobile
	 */
	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}
	
	/**
	 * [used by Spring]
	 * @param shibbolethModule
	 */
	public void setShibbolethModule(ShibbolethModule shibbolethModule) {
		this.shibbolethModule = shibbolethModule;
	}
	
	/**
	 * [used by Spring]
	 * @param restSecurityBean
	 */
	public void setRestSecurityBean(RestSecurityBean restSecurityBean) {
		this.restSecurityBean = restSecurityBean;
	}
	
	/**
	 * [used by Spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}

	/**
	 * [used by Spring]
	 * @param userDeletionManager
	 */
	public void setUserDeletionManager(UserDeletionManager userDeletionManager) {
		this.userDeletionManager = userDeletionManager;
	}

	/**
	 * Main method called by OpenOLATServlet.
	 * This processess all shibboleth requests.
	 * 
	 * @param req
	 * @param resp
	 * @param uriPrefix
	 */
	@Override
	public void execute(HttpServletRequest req,	HttpServletResponse resp) {
		if(translator==null) {
			translator = Util.createPackageTranslator(ShibbolethDispatcher.class, I18nModule.getDefaultLocale());
		}
		String uri = req.getRequestURI();
		if (!shibbolethModule.isEnableShibbolethLogins()){
			throw new OLATSecurityException("Got shibboleth request but shibboleth is not enabled: " + uri);
		}
		try {	uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertException("UTF-8 encoding not supported!!!!");
		}
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(req);
		uri = uri.substring(uriPrefix.length()); // guaranteed to exist by DispatcherAction	
			
		Map<String, String> attributesMap = getShibbolethAttributesFromRequest(req);
		String uniqueID = getUniqueIdentifierFromRequest(req, resp, attributesMap);
		if(uniqueID == null) {
			return;
		}
		
		if(!authorization(req, resp, attributesMap)) {
			return;
		}

		UserRequest ureq = null;
		try{
			//upon creation URL is checked for 
			ureq = new UserRequestImpl(uriPrefix, req, resp);
		} catch(NumberFormatException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(log.isDebug()){
				log.debug("Bad Request "+req.getPathInfo());
			}
			DispatcherModule.sendBadRequest(req.getPathInfo(), resp);
			return;
		}
		
		Authentication auth = securityManager.findAuthenticationByAuthusername(uniqueID, PROVIDER_SHIB);
		if (auth == null) { // no matching authentication...
			ShibbolethRegistrationController.putShibAttributes(req, attributesMap);
			ShibbolethRegistrationController.putShibUniqueID(req, uniqueID);
			redirectToShibbolethRegistration(resp);
			return;
		}
		if(ureq.getUserSession() != null) {
			//re-init the activity logger
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(req);
		}
		int loginStatus = AuthHelper.doLogin(auth.getIdentity(), ShibbolethDispatcher.PROVIDER_SHIB, ureq);
		if (loginStatus != AuthHelper.LOGIN_OK) {
			if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
				DispatcherModule.redirectToServiceNotAvailable(resp);
			} else {
				DispatcherModule.redirectToDefaultDispatcher(resp); // error, redirect to login screen
			}
			return;
		}
		
		// successfull login
		userDeletionManager.setIdentityAsActiv(ureq.getIdentity());
		ureq.getUserSession().getIdentityEnvironment().addAttributes(
				shibbolethModule.getAttributeTranslator().translateAttributesMap(attributesMap));

		// update user attributes
		Identity authenticationedIdentity = ureq.getIdentity();
		User user = authenticationedIdentity.getUser();
		String s = attributesMap.get(shibbolethModule.getFirstName());
		if (s != null) user.setProperty(UserConstants.FIRSTNAME, s);
		s = attributesMap.get(shibbolethModule.getLastName());
		if (s != null) user.setProperty(UserConstants.LASTNAME, s);
		s = attributesMap.get(shibbolethModule.getInstitutionalName());
		if (s != null) user.setProperty(UserConstants.INSTITUTIONALNAME, s);		
		s = ShibbolethHelper.getFirstValueOf(shibbolethModule.getInstitutionalEMail(), attributesMap);
		if (s != null) user.setProperty(UserConstants.INSTITUTIONALEMAIL, s);
		s = attributesMap.get(shibbolethModule.getInstitutionalUserIdentifier());
		if (s != null) user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, s);
		// Optional organization unit property
		String orgUnitIdent = shibbolethModule.getOrgUnit();
		if(orgUnitIdent != null) {
			s = ShibbolethHelper.getFirstValueOf(orgUnitIdent, attributesMap);
			if (s != null) user.setProperty(UserConstants.ORGUNIT, s);
		}
		UserManager.getInstance().updateUser(user);

		
		if(mobile) {
			String token = restSecurityBean.generateToken(ureq.getIdentity(), ureq.getHttpReq().getSession(true));
			
			try {
				resp.sendRedirect(WebappHelper.getServletContextPath() + "/mobile?x-olat-token=" + token + "&username=" + ureq.getIdentity().getName());
			} catch (IOException e) {
				log.error("Redirect to mobile app.", e);
			}
		} else {
			MediaResource mr = ureq.getDispatchResult().getResultingMediaResource();
			if (mr instanceof RedirectMediaResource) {
				RedirectMediaResource rmr = (RedirectMediaResource)mr;
				rmr.prepare(resp);
			} else {
				DispatcherModule.redirectToDefaultDispatcher(resp); // error, redirect to login screen
			}
		}
	}

	private String getUniqueIdentifierFromRequest(HttpServletRequest req, HttpServletResponse resp, Map<String, String> attributesMap) {

		String uniqueID = attributesMap.get(shibbolethModule.getDefaultUIDAttribute());				
		if (uniqueID == null) {				
			handleException(new ShibbolethException(ShibbolethException.UNIQUE_ID_NOT_FOUND,"Unable to get unique identifier for subject. Make sure you are listed in the metadata.xml file and your resources your are trying to access are available and your are allowed to see them. (Resourceregistry). "), 
					req, resp, translator);
			return null;
		} else if (!checkAttributes(attributesMap)) {
			handleException(new ShibbolethException(ShibbolethException.INSUFFICIENT_ATTRIBUTES,"Insufficient shibboleth attributes!"), 
					req, resp, translator);
			return null;
		}
		return uniqueID;
	}

	private Map<String, String> getShibbolethAttributesFromRequest(HttpServletRequest req) {
		Set<String> translateableAttributes = shibbolethModule.getAttributeTranslator().getTranslateableAttributes();
		Map<String, String> attributesMap = new HashMap<String, String>();
		Enumeration<String> headerEnum = req.getHeaderNames();
		while(headerEnum.hasMoreElements()) {
			String attribute = headerEnum.nextElement();
			String attributeValue = req.getHeader(attribute);
			
			ShibbolethAttribute shibbolethAttribute = ShibbolethAttribute.createFromUserRequestValue(attribute, attributeValue);
			
			boolean validAndTranslateableAttribute = shibbolethAttribute.isValid() && translateableAttributes.contains(shibbolethAttribute.getName()); 
			if(validAndTranslateableAttribute){
				attributesMap.put(shibbolethAttribute.getName(),shibbolethAttribute.getValueString());
			}
		}
		
		if(log.isDebug()){
			log.debug("Shib attribute Map: \n\n"+attributesMap.toString()+"\n\n");
		}
		
		return attributesMap;
	}
	
	/**
	 * Check if all required attributes are here.
	 * @param attributesMap
	 * @return true if all required attributes are present, false otherwise.
	 */
	private boolean checkAttributes(Map<String, String>  attributesMap) {
		if(attributesMap.keySet().size()==1) {
			return false;
		}
		try {
			String lastname = attributesMap.get(shibbolethModule.getLastName());
			String firstname = attributesMap.get(shibbolethModule.getFirstName());
			String email = ShibbolethHelper.getFirstValueOf(shibbolethModule.getEMail(), attributesMap);
			String institutionalEMail = ShibbolethHelper.getFirstValueOf(shibbolethModule.getInstitutionalEMail(), attributesMap);
			String institutionalName = attributesMap.get(shibbolethModule.getInstitutionalName());
			//String institutionalUserIdentifier = userMapping.getInstitutionalUserIdentifier();
			if(lastname!=null && !lastname.equals("") && firstname!=null && !firstname.equals("") && email!=null && !email.equals("") &&
					institutionalEMail!=null && !institutionalEMail.equals("") && institutionalName!=null && !institutionalName.equals("")) {
				return true;
			}
		} catch (IllegalArgumentException e) {
			log.error("Error when reading Shib attributes. Either home org not allowed to connect to this OO instance or user has missing attributes.");
		}
		return false;
	}

	private final void redirectToShibbolethRegistration(HttpServletResponse response) {
		try {
			response.sendRedirect(WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault() + ShibbolethModule.PATH_REGISTER_SHIBBOLETH + "/");
		} catch (IOException e) {
			log.error("Redirect failed: url=" + WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault(),e);
		}
	}
	
	private boolean authorization(HttpServletRequest req, HttpServletResponse resp, Map<String,String> attributesMap) {
		boolean authorized = false;
		if(shibbolethModule.isAccessControlByAttributes()) {
			if(StringHelper.containsNonWhitespace(shibbolethModule.getAttribute1()) && StringHelper.containsNonWhitespace(shibbolethModule.getAttribute1Values())) {
				authorized |= authorization(shibbolethModule.getAttribute1(), shibbolethModule.getAttribute1Values(), attributesMap);
			}
			if(StringHelper.containsNonWhitespace(shibbolethModule.getAttribute2()) && StringHelper.containsNonWhitespace(shibbolethModule.getAttribute2Values())) {
				authorized |= authorization(shibbolethModule.getAttribute2(), shibbolethModule.getAttribute2Values(), attributesMap);
			}
		} else {
			authorized = true;
		}
		
		if(!authorized) {
			UserRequest ureq = new UserRequestImpl(ShibbolethDispatcher.PATH_SHIBBOLETH, req, resp);
			String userMsg = translator.translate("error.shibboleth.not.authorized"); 
			ChiefController msgcc = MessageWindowController.createMessageChiefController(ureq, null, userMsg, null);
			msgcc.getWindow().dispatchRequest(ureq, true);
		}
		return authorized;
	}
	
	private boolean authorization(String attribute, String allowedValues, Map<String,String> attributesMap) {
		String val = attributesMap.get(attribute);
		if(StringHelper.containsNonWhitespace(val)) {
			val = val.trim();
			for(StringTokenizer tokenizer = new StringTokenizer(allowedValues, "\n\r,;", false); tokenizer.hasMoreTokens(); ) {
				String allowedValue = tokenizer.nextToken().trim();
				if(val.equalsIgnoreCase(allowedValue)) {
					return true;
				}	
				// Could be multi-field attribute. Check for semi-colon delimited encodings
				String[] multiValues = val.split(";");
				for (String singleValue : multiValues) {
					singleValue = singleValue.trim();
					if(singleValue.equalsIgnoreCase(allowedValue)) {
						return true;
					}	
				}
			}	
		}
		return false;
	}

	/**
	 * It first tries to catch the frequent SAMLExceptions and to ask the user to login again.
	 * It basically lets the user to login again without getting a RedScreen if one of the most 
	 * frequent shibboleth error occurs. Else a RedScreen is the last option.
	 * @param e
	 * @param req
	 * @param resp
	 */
	private void handleException(Throwable e, HttpServletRequest req, HttpServletResponse resp, Translator translator) {
		UserRequest ureq = new UserRequestImpl(ShibbolethDispatcher.PATH_SHIBBOLETH, req, resp);
		if(e instanceof ShibbolethException) {			
			String userMsg = "";
			int errorCode = ((ShibbolethException)e).getErrorCode();
			switch (errorCode) {
				case ShibbolethException.GENERAL_SAML_ERROR: userMsg = translator.translate("error.shibboleth.generic"); break;	
				case ShibbolethException.UNIQUE_ID_NOT_FOUND: userMsg = translator.translate("error.unqueid.notfound"); break;
				case ShibbolethException.INSUFFICIENT_ATTRIBUTES: userMsg = translator.translate("error.insufficieant.attributes"); break;
				default: userMsg = translator.translate("error.shibboleth.generic"); break;
			}			
			showMessage(ureq,"org.opensaml.SAMLException: " + e.getMessage(), e, userMsg, ((ShibbolethException)e).getContactPersonEmail());
			return;					
		} else {
		  try {			  
			  ChiefController msgcc = MsgFactory.createMessageChiefController(ureq,
					new OLATRuntimeException("Error processing Shibboleth request: " + e.getMessage(), e), false);
			  msgcc.getWindow().dispatchRequest(ureq, true);
		  } catch (Throwable t) {
			  log.error("We're fucked up....",t);
		  }
	  }
	}
	
	/**
	 * 
	 * @param ureq
	 * @param exceptionLogMessage will be recorded into the log file
	 * @param cause
	 * @param userMessage gets shown to the user 
	 * @param supportEmail if any available, else null
	 */
	private void showMessage(UserRequest ureq, String exceptionLogMessage, Throwable cause, String userMessage, String supportEmail) {
		ChiefController msgcc = MessageWindowController.createMessageChiefController(ureq,	new OLATRuntimeException(exceptionLogMessage, cause), userMessage, supportEmail);
		msgcc.getWindow().dispatchRequest(ureq, true);
	}

}
