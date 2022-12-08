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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.helpTooltip.HelpTooltip;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.dev.controller.SourceViewController;


/**
 * Initial Date:  19.06.2007 <br>
 * @author guido
 */
public class GuiDemoTooltipsController extends BasicController {
	
	private VelocityContainer content = createVelocityContainer("tooltips");
	private VelocityContainer tooltipContent = createVelocityContainer("tooltipContent");
	private Link link4;
	
	public GuiDemoTooltipsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		Link button = LinkFactory.createButton("button", content, this);
		button.setTooltip("tooltip.content");
		Link linkT = LinkFactory.createLink("linkT", content, this);
		linkT.setTooltip("tooltip.content");
		Link link1 = LinkFactory.createLink("link1", content, this);
		link1.setTooltip("tooltip.content");
		Link link2 = LinkFactory.createLink("link2", content, this);
		link2.setTooltip("tooltip.content");
		
		content.put("tooltipContent", tooltipContent);
		link4 = LinkFactory.createLink("link4", tooltipContent, this);

		/* help text component with link to manual */
		HelpTooltip helpText1 = new HelpTooltip("helpText1", "This is a little help, just for the sake of beeing!");
		content.put("helpText1", helpText1);
		HelpTooltip helpText2 = new HelpTooltip("helpText2", "Boy, boy, check out this cool introduction in the manual!", "Introduction", getLocale());
		content.put("helpText2", helpText2);
		
		
		//add source view control
    Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), content);
    content.put("sourceview", sourceview.getInitialComponent());
		
		putInitialPanel(content);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == link4) {
			getWindowControl().setInfo("You clicked a link rendered in a tooltip!");
			link4.setDirty(true);
		}
	}

}
