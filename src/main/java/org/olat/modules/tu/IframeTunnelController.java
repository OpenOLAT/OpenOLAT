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

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
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
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.course.nodes.tu.TUConfigForm;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	private CloseableHttpClient httpClientInstance; // package local for performance only
	private ModuleConfiguration config;
	
	@Autowired
	private HttpClientService httpClientService;

	public IframeTunnelController(UserRequest ureq, WindowControl wControl, final ModuleConfiguration config) {
		super(ureq, wControl);
		// use iframe translator for generic iframe title text
		setTranslator(Util.createPackageTranslator(IFrameDisplayController.class, ureq.getLocale()));
		this.config = config;
		
		// configuration....
		int configVersion = config.getConfigurationVersion();
		// since config version 1
		final String proto = (String) config.get(TUConfigForm.CONFIGKEY_PROTO);
		final String host = (String) config.get(TUConfigForm.CONFIGKEY_HOST);
		final Integer port = (Integer) config.get(TUConfigForm.CONFIGKEY_PORT);
		final String user = (String) config.get(TUConfigForm.CONFIGKEY_USER);
		final String startUri = (String) config.get(TUConfigForm.CONFIGKEY_URI);
		String pass = (String) config.get(TUConfigForm.CONFIGKEY_PASS);
		String firstQueryString = null;
		if (configVersion == 2) {
			// query string is available since config version 2
			firstQueryString = (String) config.get(TUConfigForm.CONFIGKEY_QUERY);
		}
		String ref = config.getStringValue(TUConfigForm.CONFIGKEY_REF);

		boolean usetunnel= config.getBooleanSafe(TUConfigForm.CONFIG_TUNNEL);
		myContent = createVelocityContainer("iframe_index");			
		if (!usetunnel) { // display content directly
			String rawurl = TUConfigForm.getFullURL(proto, host, port, startUri, firstQueryString, ref).toString();
			myContent.contextPut("url", rawurl);
		} else { // tunnel
			Identity ident = ureq.getIdentity();
			String ipAddress = ureq.getUserSession().getSessionInfo().getFromIP();
			httpClientInstance = httpClientService.createThreadSafeHttpClient(host, port.intValue(), user, pass, true);
			Mapper mapper = new TunnelMapper(proto, host, port, startUri, ipAddress, ident, httpClientInstance);
			String amapPath = registerMapper(ureq, mapper);
			String alluri = amapPath + startUri;
			if (firstQueryString != null) {
				alluri += "?" + firstQueryString;
			}
			if (StringHelper.containsNonWhitespace(ref)) {
				alluri += "#" + ref;
			}
			myContent.contextPut("url", alluri);
		}
		
		String frameId = "ifdc" + hashCode(); // for e.g. js use
		myContent.contextPut("frameId", frameId);
		putInitialPanel(myContent);		
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void doDispose() {
		IOUtils.closeQuietly(httpClientInstance);
	}

	@Override
	public Controller cloneController(UserRequest ureq, WindowControl control) {
		return new IframeTunnelController(ureq, control, config);
	}
	
}
