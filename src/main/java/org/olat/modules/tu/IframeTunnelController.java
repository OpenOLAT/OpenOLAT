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

package org.olat.modules.tu;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.clone.CloneableController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.media.HttpRequestMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.httpclient.HttpClientFactory;
import org.olat.course.nodes.tu.TUConfigForm;
import org.olat.modules.ModuleConfiguration;

/**
 *   Initial Date: 09.01.2006
 *  
 *   @author Felix Jost<br>
 *  
 *   Description:
 *   this controller serves an external content/web page in a iframe. the content is all redirected through olat in order 
 *   to be able to supply the olat-specific parameters and/or password for basic authentication to the request
 *   
 * </pre>
 */
public class IframeTunnelController extends BasicController implements CloneableController {

	private VelocityContainer myContent;
	
	HttpClient httpClientInstance = null; // package local for performance only
	private ModuleConfiguration config;

	/**
	 * Constructor for a tunnel component wrapper controller
	 * 
	 * @param ureq the userrequest
	 * @param wControl the windowcontrol
	 * @param config the module configuration
	 */
	public IframeTunnelController(UserRequest ureq, WindowControl wControl, final ModuleConfiguration config) {
		super(ureq, wControl);
		// use iframe translator for generic iframe title text
		setTranslator(Util.createPackageTranslator(IFrameDisplayController.class, ureq.getLocale()));
		this.config = config;
		
		// configuration....
		int configVersion = config.getConfigurationVersion();
		// since config version 1
		String proto = (String) config.get(TUConfigForm.CONFIGKEY_PROTO);
		String host = (String) config.get(TUConfigForm.CONFIGKEY_HOST);
		Integer port = (Integer) config.get(TUConfigForm.CONFIGKEY_PORT);
		final String user = (String) config.get(TUConfigForm.CONFIGKEY_USER);
		final String startUri = (String) config.get(TUConfigForm.CONFIGKEY_URI);
		String pass = (String) config.get(TUConfigForm.CONFIGKEY_PASS);
		String firstQueryString = null;
		if (configVersion == 2) {
			// query string is available since config version 2
			firstQueryString = (String) config.get(TUConfigForm.CONFIGKEY_QUERY);
		}

		boolean usetunnel= config.getBooleanSafe(TUConfigForm.CONFIG_TUNNEL);
		myContent = createVelocityContainer("iframe_index");			
		if (!usetunnel) { // display content directly
			String rawurl = TUConfigForm.getFullURL(proto, host, port, startUri, firstQueryString).toString();
			myContent.contextPut("url", rawurl);
		} else { // tunnel

			final Locale loc = ureq.getLocale();
			final Identity ident = ureq.getIdentity();
			final String ipAddress = ureq.getUserSession().getSessionInfo().getFromIP();
	
			if (user != null && user.length() > 0) {
				httpClientInstance = HttpClientFactory.getHttpClientInstance(host, port.intValue(), proto, user, pass);
			} else {
				httpClientInstance = HttpClientFactory.getHttpClientInstance(host, port.intValue(), proto, null, null);				
			}
			
			Mapper mapper = new Mapper() {
				public MediaResource handle(String relPath, HttpServletRequest hreq) {
					MediaResource mr = null;
					String method = hreq.getMethod();
					String uri = relPath;
					HttpMethod meth = null;
	
					if (uri == null) uri = (startUri == null) ? "" : startUri;
					if (uri.length() > 0 && uri.charAt(0) != '/') uri = "/" + uri;
					
					//String contentType = hreq.getContentType();
	
					// if (allowedToSendPersonalHeaders) {
					String userName = ident.getName();
					User u = ident.getUser();
					String lastName = u.getProperty(UserConstants.LASTNAME, loc);
					String firstName = u.getProperty(UserConstants.FIRSTNAME, loc);
					String email = u.getProperty(UserConstants.EMAIL, loc);
	
					if (method.equals("GET")) {
						GetMethod cmeth = new GetMethod(uri);
						String queryString = hreq.getQueryString();
						if (queryString != null) cmeth.setQueryString(queryString);
						meth = cmeth;
						// if response is a redirect, follow it
						meth.setFollowRedirects(true);
						
					} else if (method.equals("POST")) {
						//if (contentType == null || contentType.equals("application/x-www-form-urlencoded")) {
							// regular post, no file upload
						//}
						Map<String,String[]> params = hreq.getParameterMap();
						PostMethod pmeth = new PostMethod(uri);
						Set<String> postKeys = params.keySet();
						for (Iterator<String> iter = postKeys.iterator(); iter.hasNext();) {
							String key = iter.next();
							String vals[] = params.get(key);
							for (int i = 0; i < vals.length; i++) {
								pmeth.addParameter(key, vals[i]);
							}
							meth = pmeth;
						}
						if (meth == null) return null;
						// Redirects are not supported when using POST method!
						// See RFC 2616, section 10.3.3, page 62
					}
					
					// Add olat specific headers to the request, can be used by external
					// applications to identify user and to get other params
					// test page e.g. http://cgi.algonet.se/htbin/cgiwrap/ug/test.py
					if("enabled".equals(CoreSpringFactory.getImpl(BaseSecurityModule.class).getUserInfosTunnelCourseBuildingBlock())) {
						meth.addRequestHeader("X-OLAT-USERNAME", userName);
						meth.addRequestHeader("X-OLAT-LASTNAME", lastName);
						meth.addRequestHeader("X-OLAT-FIRSTNAME", firstName);
						meth.addRequestHeader("X-OLAT-EMAIL", email);
						meth.addRequestHeader("X-OLAT-USERIP", ipAddress);
					}
	
					boolean ok = false;
					try {
						httpClientInstance.executeMethod(meth);
						ok = true;
					} catch (Exception e) {
						// handle error later
					}
	
					if (!ok) {
						// error
						meth.releaseConnection();
						return new NotFoundMediaResource(relPath);
					}
	
					// get or post successfully
					Header responseHeader = meth.getResponseHeader("Content-Type");
					if (responseHeader == null) {
						// error
						return new NotFoundMediaResource(relPath);
					}
					mr = new HttpRequestMediaResource(meth);
					return mr;
				}
			};
	
			String amapPath = registerMapper(ureq, mapper);
			String alluri = amapPath + startUri;
			if (firstQueryString != null) {
				alluri+="?"+firstQueryString;
			}
			myContent.contextPut("url", alluri);
		}
		
		String frameId = "ifdc" + hashCode(); // for e.g. js use
		myContent.contextPut("frameId", frameId);

		putInitialPanel(myContent);		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// mapper autodisposed by basic controller
	}

	/**
	 * @see org.olat.core.gui.control.generic.clone.CloneableController#cloneController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller cloneController(UserRequest ureq, WindowControl control) {
		return new IframeTunnelController(ureq, control, config);
	}
	
}
