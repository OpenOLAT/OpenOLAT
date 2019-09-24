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
* <p>
*/ 
package org.olat.core.gui.components.util;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Initial Date: 10.03.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ComponentUtil {
	public static final Event VALIDATE_EVENT = new Event("validatecomponent");
	
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ComponentUtil.class);

	public static void registerForValidateEvents(VelocityContainer parentContainer, Controller listeningController) {
		ValidateForwardComponent vfc = new ValidateForwardComponent("validatecomp");
		vfc.addListener(listeningController);
		parentContainer.put("o_validate"+vfc.getDispatchID(), vfc);
	}

	/**
	 * convenience method to easily add a title to a component
	 * @param titleKey the key for the title 
	 * @param args the arguments for the translator, when you need to pass arguments to the translation. may be null
	 * @param trans the translator to translate the given key
	 * @param content the component which is to appear after the title
	 * @return a component which first renders the title with h1 tags, and then the content
	 */
	public static Component createTitledComponent(String titleKey, String[] args, Translator trans, Component content) {
		VelocityContainer vc = new VelocityContainer("titlewrapper", VELOCITY_ROOT + "/title.html", trans, null);
		vc.contextPut("title", trans.translate(titleKey, args));
		vc.put("content", content);
		return vc;
	}

}
