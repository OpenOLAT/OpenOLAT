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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.course.nodes.tu.TUConfigForm;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * Wrapper controller to wrap a tunnel component
 * <P>
 * Initial Date:  Dec 15, 2004
 *
 * @author gnaegi 
 */
public class TunnelController extends DefaultController {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(TunnelController.class);

	private TunnelComponent tuc;
	private VelocityContainer main;
	private CloseableHttpClient httpClientInstance;
	
	@Autowired
	private HttpClientService httpClientService;
	
	public TunnelController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(wControl);
		Translator trans = Util.createPackageTranslator(TunnelController.class, ureq.getLocale());
		main = new VelocityContainer("tucMain", VELOCITY_ROOT + "/index.html", trans, null);

		String user = (String)config.get(TUConfigForm.CONFIGKEY_USER);
		String pass = (String)config.get(TUConfigForm.CONFIGKEY_PASS);
		String host = (String)config.get(TUConfigForm.CONFIGKEY_HOST);
		Integer port = (Integer)config.get(TUConfigForm.CONFIGKEY_PORT);
		httpClientInstance = httpClientService.createThreadSafeHttpClient(host, port.intValue(), user, pass, true);

		tuc = new TunnelComponent("tuc", config, httpClientInstance, ureq);
		main.put("tuc", tuc);
		setInitialComponent(main);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void doDispose() {
		IOUtils.closeQuietly(httpClientInstance);
		tuc = null;
        super.doDispose();
	}

}
