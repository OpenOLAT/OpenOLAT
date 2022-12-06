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

	private final VelocityContainer mainVC;
	private final Map<String, ControllerCreator> demos = new HashMap<>();
	private final List<String> demolinknames = new ArrayList<>();
	private Controller demoController;
	private StackedPanel contentP;
	private VelocityContainer contentSourceVC;
	private Panel sourceP;

	public GuiDemoFlexiFormMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("flexiformdemos");
		initControllers();

		for (String linkName : demolinknames) {
			Link tmpLink = LinkFactory.createLink(linkName, mainVC, this);
			tmpLink.setUserObject(linkName);
		}

		mainVC.contextPut("demolinknames", demolinknames);

		// all demo controllers content goes in this panel
		contentP = new SimpleStackedPanel("content");
		contentSourceVC = createVelocityContainer("content_source");
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

		contentSourceVC.put("content", mainVC);
		contentSourceVC.put("source", sourceP);
		//add source view control
		Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), contentSourceVC);
		mainVC.put("sourceview", sourceview.getInitialComponent());
		
		putInitialPanel(contentSourceVC);
	}
	
	private void initControllers() {
		demolinknames.add("guidemo_flexi_form_simpleform");
		demos.put("guidemo_flexi_form_simpleform", (ureq, wControl) -> new GuiDemoFlexiForm(ureq, wControl, null));
		
		demolinknames.add("guidemo_flexi_form_simpleform_validating");
		demos.put("guidemo_flexi_form_simpleform_validating", GuiDemoFlexiFormValidating::new);
		
		demolinknames.add("guidemo_flexi_form_inlineform_validating");
		demos.put("guidemo_flexi_form_inlineform_validating", GuiDemoFlexiInlineFormValidating::new);

		demolinknames.add("guidemo_flexi_form_withchooser");
		demos.put("guidemo_flexi_form_withchooser", (ureq, wControl) ->  new GuiDemoFlexiFormSubworkflow(ureq, wControl, null));
		
		demolinknames.add("guidemo_flexi_form_customlayout");
		demos.put("guidemo_flexi_form_customlayout", (ureq, wControl) -> new GuiDemoFlexiFormCustomlayout(ureq, wControl, null));
		
		demolinknames.add("guidemo_flexi_form_hideunhide");
		demos.put("guidemo_flexi_form_hideunhide", (ureq, wControl) ->  new GuiDemoFlexiFormHideUnhide(ureq, wControl, null));
		
		demolinknames.add("guidemo_flexi_form_inline");
		demos.put("guidemo_flexi_form_inline", (ureq, wControl) -> new GuiDemoInlineEditingBasedOnFlexiForm(ureq, wControl));
		
		demolinknames.add("guidemo_flexi_form_advanced");
		demos.put("guidemo_flexi_form_advanced", (ureq, wControl) -> new GuiDemoFlexiFormAdvancedController(ureq, wControl));
	}

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
				removeAsListenerAndDispose(demoController);
				contentP.popContent();
				//create new demo controller
				demoController = cc.createController(ureq, getWindowControl());
				listenTo(demoController);
				contentP.pushContent(demoController.getInitialComponent());
			}
		}
	}
}
