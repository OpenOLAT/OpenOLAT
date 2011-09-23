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
 * <p>
 */

package org.olat.test.guidemo.cssjs;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.dev.controller.SourceViewController;

public class GUIDemoCSSJSController extends BasicController{

	private VelocityContainer mainVc;
	private VelocityContainer modalVc1, jsVc1;
	private VelocityContainer modalVc2, jsVc2;

	private Link jscssremove;
	private Link jscss, linkjs1, linkjs2;
	
	private Panel jsTestP;
	
	
	public GUIDemoCSSJSController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVc = createVelocityContainer("cssjsdemo");
		
		// the first demo has a css (which must be removed if not used) and a js lib
		modalVc1 = createVelocityContainer("modal1");
		JSAndCSSComponent jscss1 = new JSAndCSSComponent("jsAndCssForDemo", this.getClass(), new String[]{"js1.js","js1b.js"}, "style1.css", true);
		modalVc1.put("jsAndCssForDemo", jscss1); // we attach it to the modalComponent, so that it is activated when this component shows up on screen.			

		// the second demo has a css (which is not removed even if not used anymore) and a js lib
		modalVc2 = createVelocityContainer("modal2");
		JSAndCSSComponent jscss2 = new JSAndCSSComponent("jsAndCssForDemo", this.getClass(), new String[]{"js2.js","js2b.js"}, "style2.css", false);
		modalVc2.put("jsAndCssForDemo", jscss2); // we attach it to the modalComponent, so that it is activated when this component shows up on screen.			
		
		// js functions override test
		linkjs1 = LinkFactory.createButtonSmall("link.js1", mainVc, this);
		jsVc1 = createVelocityContainer("jstest1");
		JSAndCSSComponent jstest1 = new JSAndCSSComponent("jstest1includes", this.getClass(), new String[]{"jsfuncdef1.js"}, null, false);
		jsVc1.put("jstest1includes", jstest1);
		
		linkjs2 = LinkFactory.createButtonSmall("link.js2", mainVc, this);
		jsVc2 = createVelocityContainer("jstest2");
		JSAndCSSComponent jstest2 = new JSAndCSSComponent("jstest1includes", this.getClass(), new String[]{"jsfuncdef2.js"}, null, false);
		jsVc2.put("jstest2includes", jstest2);
		
		jsTestP = new Panel("jstestP");
		mainVc.put("jstestpanel", jsTestP);
		
		jscssremove = LinkFactory.createButtonXSmall("link.jscssremove", mainVc, this);
		jscss = LinkFactory.createButtonXSmall("link.jscss", mainVc, this);
		
		//add source view control
    Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), mainVc);
    mainVc.put("sourceview", sourceview.getInitialComponent());
		
		// let the scripts (.js files) and css files be included when this controller's main component is rendered
		putInitialPanel(mainVc);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == jscssremove){
			CloseableModalController cmc = new CloseableModalController(getWindowControl(), "closing removes the css again, js -libs- are never removed by design, but you can include &lt;script&gt; tags in your velocity pages", modalVc1);
			cmc.activate();
		} else if (source == jscss){
			CloseableModalController cmc2 = new CloseableModalController(getWindowControl(), "closing does not remove the css again, js -libs- are never removed by design, but you can include &lt;script&gt; tags in your velocity pages", modalVc2);
			cmc2.activate();
		} else if (source == linkjs1) {
			jsTestP.setContent(jsVc1);
		} else if (source == linkjs2) {
			jsTestP.setContent(jsVc2);
		}
	}

	protected void doDispose() {
		//
	}

}
