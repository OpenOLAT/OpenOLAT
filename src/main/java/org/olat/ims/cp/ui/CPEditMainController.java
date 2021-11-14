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

package org.olat.ims.cp.ui;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ContentPackage;
import org.olat.modules.cp.CPAssessmentProvider;
import org.olat.modules.cp.CPUIFactory;
import org.olat.modules.cp.PersistingAssessmentProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntryRuntimeController.ToolbarAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The content packaging main edit controller.
 */
public class CPEditMainController extends BasicController implements ToolbarAware {

	private TooledStackedPanel stackPanel;
	private CPContentController contentCtr;
	private CPTreeController treeCtr;
	private final ContentPackage cp;
	private LockResult lock;
	private DeliveryOptions deliveryOptions;
	
	@Autowired
	private CPManager cpManager;

	public CPEditMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar,
			VFSContainer cpContainer, RepositoryEntry cpEntry) {
		super(ureq, wControl);
		this.stackPanel = toolbar;
		OLATResourceable ores = cpEntry.getOlatResource();

		// acquire lock for resource
		lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, ureq.getIdentity(), null, getWindow());
		cp = cpManager.load(cpContainer, ores);
		
		CPPackageConfig packageConfig = cpManager.getCPPackageConfig(ores);
		if(packageConfig != null) {
			deliveryOptions = packageConfig.getDeliveryOptions();
		}
		
		String errorString = cp.getLastError();
		if (errorString == null) {
			if (lock.isSuccess()) {
				treeCtr = new CPTreeController(ureq, wControl, cp);
				listenTo(treeCtr);

				contentCtr = new CPContentController(ureq, wControl, cp);
				listenTo(contentCtr);
				contentCtr.init(ureq);

				// Make tree controller aware of contentCtr in order to display pages after import.
				treeCtr.setContentController(contentCtr);

				Controller columnLayoutCtr = new LayoutMain3ColsController(ureq, wControl, treeCtr.getInitialComponent(), contentCtr.getInitialComponent(), "cptestmain");
				listenTo(columnLayoutCtr);
				
				StackedPanel initPanel = new SimpleStackedPanel("cpeditpanel", "o_edit_mode");
				initPanel.setContent(columnLayoutCtr.getInitialComponent());
				putInitialPanel(initPanel);

				if (!cp.isOLATContentPackage()) {
					showWarning("maincontroller.cp.created.with.third.party.editor");
				}
			} else {
				if(lock.isDifferentWindows()) {
					showWarning("contentcontroller.no.lock.same.user");
				} else {
					showWarning("contentcontroller.no.lock");
				}
				
				CPAssessmentProvider cpAssessmentProvider = PersistingAssessmentProvider.create(cpEntry, getIdentity(), false);
				Controller cpCtr = CPUIFactory.getInstance()
						.createMainLayoutController(ureq, wControl, cpContainer, true, deliveryOptions, cpAssessmentProvider);
				putInitialPanel(cpCtr.getInitialComponent());
			}
		} else {
			showError("maincontroller.loaderror", errorString);
			Controller columnLayoutCtr = new LayoutMain3ColsController(ureq, wControl, null, new Panel("errorPanel"), "cptestmain");
			putInitialPanel(columnLayoutCtr.getInitialComponent());
		}
		logAudit("cp editor started. oresId: " + ores.getResourceableId());
	}

	@Override
	public void initToolbar() {
		if(stackPanel != null && treeCtr != null) {
			treeCtr.initToolbar(stackPanel);
		}
		if(stackPanel != null && contentCtr != null) {
			contentCtr.initToolbar(stackPanel);
		}
	}

	@Override
	protected void doDispose() {
		Long oresId = cp.getResourcable().getResourceableId();
		// In any case, release the lock
		CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		logAudit("finished editing cp. ores-id: " + oresId);
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// nothing
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == treeCtr) {
			if (event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				String nodeId = te.getNodeId();
				contentCtr.displayPage(ureq, nodeId);
			} else if (event.getCommand().equals("New Page")) {
				String newIdentifier = treeCtr.addNewHTMLPage();
				contentCtr.displayPageWithMetadataEditor(ureq, newIdentifier);
			} else if (event instanceof NewCPPageEvent) {
				contentCtr.displayPageWithMetadataEditor(ureq, ((NewCPPageEvent) event).getCPPage().getIdentifier());
			}
		} else if (source == contentCtr) {
			// event from contentController
			if (event instanceof NewCPPageEvent) {
				NewCPPageEvent ncpEvent = (NewCPPageEvent) event;
				CPPage page = ncpEvent.getCPPage();
				if (event.getCommand().equals("New Page Saved")) {
					String newNodeID = treeCtr.addPage(page);
					contentCtr.newPageAdded(newNodeID);
					treeCtr.updatePage(page);

				} else if (event.getCommand().equals("Page Saved")) {
					treeCtr.updatePage(page);
					// Title could have changed -> dirty view
					treeCtr.getInitialComponent().setDirty(true);
				}

			} else if (event.getCommand().equals("Page loaded")) {
				CPPage page = contentCtr.getCurrentPage();
				if (page != null) {
					treeCtr.selectTreeNodeByCPPage(page);
				}
			}
		}
	}
}
