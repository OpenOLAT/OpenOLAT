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

package org.olat.admin.search;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.SearchServiceImpl;

/**
*  Description:<br>
*  is the controller for
*
* @author Felix Jost
*/
public class SearchAdminController extends BasicController {

	VelocityContainer myContent;
	private SearchAdminForm searchAdminForm;
	
	Panel main;
	private Link stopIndexingButton;
	private Link startIndexingButton;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public SearchAdminController(UserRequest ureq, WindowControl wControl) { 
		super(ureq,wControl);	
		
		main = new Panel("searchmain");
		myContent = createVelocityContainer("index");
		startIndexingButton = LinkFactory.createButtonSmall("button.startindexing", myContent, this);
		stopIndexingButton = LinkFactory.createButtonSmall("button.stopindexing", myContent, this);
		myContent.contextPut("searchstatus", SearchServiceFactory.getService().getStatus());
    
		searchAdminForm = new SearchAdminForm(ureq, wControl);
		listenTo(searchAdminForm);
		
		searchAdminForm.setIndexInterval(
				SearchServiceFactory.getService().getIndexInterval()
		);
	
		myContent.put("searchAdminForm", searchAdminForm.getInitialComponent());
		main.setContent(myContent);
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startIndexingButton) {
			SearchServiceFactory.getService().startIndexing();
			logInfo("Indexing started via Admin", SearchAdminController.class.getName());
			myContent.setDirty(true);
		} else if (source == stopIndexingButton) {
			SearchServiceFactory.getService().stopIndexing();
			logInfo("Indexing stopped via Admin", SearchAdminController.class.getName());
			myContent.setDirty(true);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchAdminForm) {
			if (event == Event.DONE_EVENT) {
				SearchServiceFactory.getService().setIndexInterval(searchAdminForm.getIndexInterval());
				return;
			}
		}
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {		
		//
	}
}
