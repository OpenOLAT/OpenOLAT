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
package org.olat.admin.sysinfo;

import org.olat.admin.quota.QuotaController;
import org.olat.core.commons.services.vfs.ui.management.VFSOverviewController;
import org.olat.core.commons.services.vfs.ui.version.VFSTrashController;
import org.olat.core.commons.services.vfs.ui.version.AdminConfigurationController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;


/**
 * 
 * Initial date: 13.12.2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class FilesAndFoldersController extends BasicController{
	private final Link fileStatsLink;
	private final Link configurationLink;
	private final Link quotaLink;
	private final Link trashLink;
	private final Link overviewLink;
	
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private AdminConfigurationController configurationCtrl;
	private LargeFilesController fileStats;
	private QuotaController quotas;
	private VFSTrashController trash;
	private VFSOverviewController vfsOverviewCtrl;
	
	public FilesAndFoldersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		overviewLink = LinkFactory.createLink("filesfolders.menu.overview", mainVC, this);
		segmentView.addSegment(overviewLink, true);
		
		configurationLink = LinkFactory.createLink("configuration", mainVC, this);
		segmentView.addSegment(configurationLink, false);
		
		quotaLink = LinkFactory.createLink("filesfolders.menu.quota", mainVC, this);
		segmentView.addSegment(quotaLink, false);
		
		fileStatsLink = LinkFactory.createLink("filesfolders.menu.largefiles", mainVC, this);
		segmentView.addSegment(fileStatsLink, false);
		
		trashLink = LinkFactory.createLink("filesfolders.menu.deletedFiles", mainVC, this);
		segmentView.addSegment(trashLink, false);
		
		
		mainVC.put("segments", segmentView);
		doOverview(ureq);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if(clickedLink == configurationLink) {
					doConfiguration(ureq);
				} else if(clickedLink == fileStatsLink) {
					doFileStats(ureq);
				} else if(clickedLink == quotaLink) {
					doQuota(ureq);
				} else if(clickedLink == trashLink) {
					doTrash(ureq);
				} else if(clickedLink == overviewLink) {
					doOverview(ureq);
				} 
			}
		}
	}
	
	
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == vfsOverviewCtrl) {
			if (event == VFSOverviewController.OPEN_LARGE_FILES_EVENT) {
				doFileStats(ureq);
			} else if (event == VFSOverviewController.OPEN_TRASH_EVENT) {
				doTrash(ureq);
			} else if (event == VFSOverviewController.OPEN_VERSIONS_EVENT) {
				doConfiguration(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			configurationCtrl = new AdminConfigurationController(ureq, getWindowControl());
			listenTo(configurationCtrl);
		}
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
		segmentView.select(configurationLink);
	}
	
	private void doFileStats(UserRequest ureq) {
		if(fileStats == null) {
			fileStats = new LargeFilesController(ureq, getWindowControl());
			listenTo(fileStats);
		}
		mainVC.put("segmentCmp", fileStats.getInitialComponent());
		segmentView.select(fileStatsLink);
	}
	
	private void doQuota(UserRequest ureq) {
		if(quotas == null) {
			quotas = new QuotaController(ureq, getWindowControl());
			listenTo(quotas);
		}
		mainVC.put("segmentCmp", quotas.getInitialComponent());
		segmentView.select(quotaLink);
	}
	
	private void doOverview(UserRequest ureq) {
		if (vfsOverviewCtrl == null) {
			vfsOverviewCtrl = new VFSOverviewController(ureq, getWindowControl());
			listenTo(vfsOverviewCtrl);
		}
		mainVC.put("segmentCmp", vfsOverviewCtrl.getInitialComponent());
		segmentView.select(overviewLink);
	}
	
	private void doTrash(UserRequest ureq) {
		if (trash == null) {
			trash = new VFSTrashController(ureq, getWindowControl());
			listenTo(trash);
		}
		mainVC.put("segmentCmp", trash.getInitialComponent());
		segmentView.select(trashLink);
	}
	
	
}
