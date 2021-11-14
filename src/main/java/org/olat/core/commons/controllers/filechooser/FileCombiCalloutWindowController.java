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
package org.olat.core.commons.controllers.filechooser;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Description:
 * <p>This controller provides a view with three link options:
 * to browse for a file, to create a new file or to upload a file. 
 * </p>
 * 
 * Initial date: 18.12.2014<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class FileCombiCalloutWindowController extends BasicController {
	private final Link chooseLink;
	private final Link createLink;
	private final Link uploadLink;

	
	protected FileCombiCalloutWindowController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		final VelocityContainer contentVC = createVelocityContainer("combiLinks");
		
		chooseLink = LinkFactory.createLink("chooseLink", contentVC, this);
		chooseLink.setIconLeftCSS("o_icon o_icon_browse o_icon-fw");
		chooseLink.setElementCssClass("o_sel_filechooser_choose");

		createLink = LinkFactory.createLink("createLink", contentVC, this);
		createLink.setIconLeftCSS("o_icon o_icon_new_document o_icon-fw");
		createLink.setElementCssClass("o_sel_filechooser_create");

		uploadLink = LinkFactory.createLink("uploadLink", contentVC, this);
		uploadLink.setIconLeftCSS("o_icon o_icon_upload o_icon-fw");
		uploadLink.setElementCssClass("o_sel_filechooser_upload");

		putInitialPanel(contentVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(chooseLink == source){
			this.fireEvent(ureq, new Event("chooseLink"));
		}else if(source == createLink){
			this.fireEvent(ureq, new Event("createLink"));
		}else if(source == uploadLink){
			this.fireEvent(ureq, new Event("uploadLink"));
		}

	}
}
