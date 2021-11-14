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

package org.olat.admin.search;

import org.apache.logging.log4j.Level;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.LogRealTimeViewerController;
import org.olat.search.SearchModule;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.indexer.IndexCronGenerator;

/**
*  Description:<br>
*  is the controller for
*
* @author Felix Jost
*/
public class SearchAdminController extends BasicController {

	private final Link stopIndexingButton;
	private final Link startIndexingButton;
	private final Link refreshIndexingButton;
	private final VelocityContainer myContent;
	
	private SearchAdminForm searchAdminForm;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public SearchAdminController(UserRequest ureq, WindowControl wControl) { 
		super(ureq,wControl);	
		
		myContent = createVelocityContainer("index");
		startIndexingButton = LinkFactory.createButtonSmall("button.startindexing", myContent, this);
		stopIndexingButton = LinkFactory.createButtonSmall("button.stopindexing", myContent, this);
		refreshIndexingButton = LinkFactory.createButtonSmall("button.refreshindexing", myContent, this);
		
		myContent.contextPut("searchstatus", SearchServiceFactory.getService().getStatus());
		
		IndexCronGenerator generator = (IndexCronGenerator)CoreSpringFactory.getBean("&searchIndexCronGenerator");
		if(generator.isCronEnabled()) {
			myContent.contextPut("cronExpression", generator.getCron());
		} else {
			myContent.contextPut("cronExpression", Boolean.FALSE);
		}
    
		searchAdminForm = new SearchAdminForm(ureq, wControl);
		listenTo(searchAdminForm);
		
		searchAdminForm.setIndexInterval(SearchServiceFactory.getService().getIndexInterval());
		
		SearchModule searchModule = (SearchModule)CoreSpringFactory.getBean("searchModule");
		searchAdminForm.setFileBlackList(searchModule.getCustomFileBlackList());
		searchAdminForm.setExcelFileEnabled(searchModule.getExcelFileEnabled());
		searchAdminForm.setPptFileEnabled(searchModule.getPptFileEnabled());
		searchAdminForm.setPdfFileEnabled(searchModule.getPdfFileEnabled());
	
		myContent.put("searchAdminForm", searchAdminForm.getInitialComponent());
		
		LogRealTimeViewerController logViewController = new LogRealTimeViewerController(ureq, wControl, "org.olat.search", Level.DEBUG, false);
		listenTo(logViewController);
		myContent.put("logViewController", logViewController.getInitialComponent());

		putInitialPanel(myContent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startIndexingButton) {
			doStartIndexer();
			myContent.setDirty(true);
		} else if (source == stopIndexingButton) {
			doStopIndexer();
			myContent.setDirty(true);
		} else if(source == refreshIndexingButton) {
			doRefresh();
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchAdminForm) {
			if (event == Event.DONE_EVENT) {
				SearchServiceFactory.getService().setIndexInterval(searchAdminForm.getIndexInterval());
				SearchModule searchModule = (SearchModule)CoreSpringFactory.getBean("searchModule");
				searchModule.setCustomFileBlackList(searchAdminForm.getFileBlackList());
				searchModule.setExcelFileEnabled(searchAdminForm.isExcelFileEnabled());
				searchModule.setPptFileEnabled(searchAdminForm.isPptFileEnabled());
				searchModule.setPdfFileEnabled(searchAdminForm.isPdfFileEnabled());
			}
		}
	}
	
	/**
	 * Refresh the Lucene's readers
	 */
	private void doRefresh() {
		if(SearchServiceFactory.getService().refresh()) {
			showInfo("refreshed");
		} else {
			showWarning("refresh.error");
		}
	}
	
	private void doStartIndexer() {
		SearchServiceFactory.getService().startIndexing();
		logInfo("Indexing started via Admin");
	}
	
	private void doStopIndexer() {
		SearchServiceFactory.getService().stopIndexing();
		logInfo("Indexing started via Admin");
	}
}
