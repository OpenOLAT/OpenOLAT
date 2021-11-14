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
package org.olat.course.nodes.tu;

import java.net.MalformedURLException;
import java.net.URL;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.ModuleConfiguration;

public class TUStartController extends BasicController {
	
	private VelocityContainer runVC;
	
	public TUStartController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		
		runVC = createVelocityContainer("run");
		
		URL url = null;
		try {
			url = new URL((String)config.get(TUConfigForm.CONFIGKEY_PROTO), 
							(String)config.get(TUConfigForm.CONFIGKEY_HOST),
							((Integer)config.get(TUConfigForm.CONFIGKEY_PORT)).intValue(),
							(String) config.get(TUConfigForm.CONFIGKEY_URI));
		} catch (MalformedURLException e) {
			// this should not happen since the url was already validated in edit mode
			runVC.contextPut("url", "");
		}
		if (url != null) {
			StringBuilder sb = new StringBuilder(128);
			sb.append(url.toString());
			// since the url only includes the path, but not the query (?...), append it here, if any
			String query = (String)config.get(TUConfigForm.CONFIGKEY_QUERY);
			if (query != null) {
				sb.append("?").append(query);
			}
			String ref = (String)config.get(TUConfigForm.CONFIGKEY_REF);
			if (StringHelper.containsNonWhitespace(ref)) {
				sb.append("#").append(ref);
			}
			runVC.contextPut("url", sb.toString());
		}
		
		putInitialPanel(runVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
