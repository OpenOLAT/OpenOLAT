/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;

/**
 * Chooser for the available groups.
 * 
 * Initial date: 06.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupChooserController extends BasicController {
	
	private BusinessGroup selectGroup;
	
	public BusinessGroupChooserController(UserRequest ureq, WindowControl wControl,
			List<BusinessGroup> myGroups) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("groups_chooser");
		
		List<String> links = new ArrayList<>();
		for(BusinessGroup myGroup:myGroups) {
			String name = "gp-" + myGroup.getKey();
			Link link = LinkFactory.createLink(name, myGroup.getName(), getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
			link.setCustomDisplayText(StringHelper.escapeHtml(myGroup.getName()));
			link.setUserObject(myGroup);
			mainVC.put(name, link);
			links.add(name);
		}
		mainVC.contextPut("links", links);
		putInitialPanel(mainVC);
	}

	public BusinessGroup getSelectGroup() {
		return selectGroup;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link select = (Link)source;
			selectGroup = (BusinessGroup)select.getUserObject();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
