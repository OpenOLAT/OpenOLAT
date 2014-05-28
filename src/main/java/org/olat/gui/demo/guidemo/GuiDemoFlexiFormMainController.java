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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.spacesaver.ShrinkController;
import org.olat.core.gui.dev.controller.SourceViewController;

/**
 * Description:<br>
 * Manages the sequence of flexi form demos, and provides a flexi form demo
 * navigation.
 * <P>
 * Initial Date: 10.09.2007 <br>
 * 
 * @author patrickb
 */
public class GuiDemoFlexiFormMainController extends BasicController {

	private VelocityContainer mainVC;
	private Map<String, ControllerCreator> demos = new HashMap<String, ControllerCreator>();
	List<String> demolinknames;
	private Controller demoController;
	private StackedPanel contentP;
	private VelocityContainer content_sourceVC;
	private Panel sourceP;
	{
		// create the demos
		// could also be injected with spring
		//
		// for the order
		demolinknames = new ArrayList<String>();
		//
		demolinknames.add("guidemo_flexi_form_simpleform");
		demos.put("guidemo_flexi_form_simpleform", new ControllerCreator() {
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new GuiDemoFlexiForm(ureq, wControl, null);
			}
		});
		demolinknames.add("guidemo_flexi_form_withchooser");
		demos.put("guidemo_flexi_form_withchooser", new ControllerCreator() {
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new GuiDemoFlexiFormSubworkflow(ureq, wControl, null);
			}
		});
		demolinknames.add("guidemo_flexi_form_customlayout");
		demos.put("guidemo_flexi_form_customlayout", new ControllerCreator() {
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new GuiDemoFlexiFormCustomlayout(ureq, wControl, null);
			}
		});
		demolinknames.add("guidemo_flexi_form_hideunhide");
		demos.put("guidemo_flexi_form_hideunhide", new ControllerCreator() {
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new GuiDemoFlexiFormHideUnhide(ureq, wControl, null);
			}
		});
		demolinknames.add("guidemo_flexi_form_inline");
		demos.put("guidemo_flexi_form_inline", new ControllerCreator() {
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new GuiDemoInlineEditingBasedOnFlexiForm(ureq, wControl);
			}
		});
		demolinknames.add("guidemo_flexi_form_advanced");
		demos.put("guidemo_flexi_form_advanced", new ControllerCreator() {
			public Controller createController(UserRequest ureq, WindowControl wControl) {
				return new GuiDemoFlexiFormAdvancedController(ureq, wControl);
			}
		});
	}

	public GuiDemoFlexiFormMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("flexiformdemos");

		for (String linkName : demolinknames) {
			Link tmpLink = LinkFactory.createLink(linkName, mainVC, this);
			tmpLink.setUserObject(linkName);
		}

		mainVC.contextPut("demolinknames", demolinknames);

		// all democontroller content goes in this panel
		contentP = new SimpleStackedPanel("content");
		content_sourceVC = createVelocityContainer("content_source");
		mainVC.put("democontent", contentP);
		//
		String firstDemo = demolinknames.iterator().next();
		ControllerCreator cc = demos.get(firstDemo);
		demoController = cc.createController(ureq, getWindowControl());
		contentP.setContent(demoController.getInitialComponent());
		
		sourceP = new Panel("sourceP");
		VelocityContainer sourceVC = createVelocityContainer(firstDemo);
		ShrinkController sc = new ShrinkController(false, sourceVC, "toggle source");
		sourceP.setContent(sc.getInitialComponent());

		content_sourceVC.put("content", mainVC);
		content_sourceVC.put("source", sourceP);
		//add source view control
    Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), content_sourceVC);
    mainVC.put("sourceview", sourceview.getInitialComponent());
		
		putInitialPanel(content_sourceVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		if (demoController != null) demoController.dispose();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//there are only events of type link from the demos navigation
		if (source instanceof Link) {
			Link sl = (Link) source;
			//userobject tells which demo to activate
			String uob = (String) sl.getUserObject();
			if (uob != null) {
				ControllerCreator cc = demos.get(uob);
				//update source
				VelocityContainer sourceVC = createVelocityContainer(uob);
				ShrinkController sc = new ShrinkController(false, sourceVC, "toggle source");
				sourceP.setContent(sc.getInitialComponent());
				
				//cleanup former democontroller
				if (demoController != null) demoController.dispose();
				contentP.popContent();
				//create new demo controller
				demoController = cc.createController(ureq, getWindowControl());
				contentP.pushContent(demoController.getInitialComponent());
			}
		}

	}
	
}
