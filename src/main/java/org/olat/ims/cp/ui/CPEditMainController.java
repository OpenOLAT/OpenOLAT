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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.ims.cp.ui;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeModifiedEvent;
import org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ContentPackage;
import org.olat.modules.cp.CPUIFactory;

/**
 * The content packaging main edit controller.
 */
public class CPEditMainController extends MainLayoutBasicController {

	private LayoutMain3ColsController columnLayoutCtr;
	private CPContentController contentCtr;
	private CPTreeController treeCtr;
	private final ContentPackage cp;
	private LockResult lock;

	public CPEditMainController(UserRequest ureq, WindowControl wControl, VFSContainer cpContainer, OLATResourceable ores) {
		super(ureq, wControl);

		// acquire lock for resource
		lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, ureq.getIdentity(), null);

		CPManager cpMgm = CPManager.getInstance();

		this.cp = cpMgm.load(cpContainer, ores);

		String errorString = cp.getLastError();
		if (errorString == null) {
			if (lock.isSuccess()) {
				initDefaultView(ureq, wControl);
			} else {
				showInfo("contentcontroller.no.lock");
				displayCP(ureq, wControl, cpContainer);
			}
		} else {
			initErrorView(ureq, wControl, errorString);
			this.showError("maincontroller.loaderror", errorString);
		}
		logAudit("cp editor started. oresId: " + ores.getResourceableId(), null);
	}

	/**
	 * Displays the cp without being able to modify it.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param root
	 */
	private void displayCP(UserRequest ureq, WindowControl wControl, VFSContainer root) {
		MainLayout3ColumnsController cpCtr = CPUIFactory.getInstance().createMainLayoutController(ureq, wControl, root, true);
		this.putInitialPanel(cpCtr.getInitialComponent());
	}

	/**
	 * initializes default controllers
	 * 
	 * @param ureq
	 * @param wControl
	 * @param cp
	 */
	private void initDefaultView(UserRequest ureq, WindowControl wControl) {
		treeCtr = new CPTreeController(ureq, wControl, cp);
		listenTo(treeCtr);

		contentCtr = new CPContentController(ureq, wControl, cp);
		listenTo(contentCtr);
		contentCtr.init(ureq);

		// Make tree controller aware of contentCtr in order to display pages after
		// import.
		treeCtr.setContentController(contentCtr);

		columnLayoutCtr = new LayoutMain3ColsController(ureq, wControl, treeCtr.getInitialComponent(), null, contentCtr.getInitialComponent(),
				"cptestmain");
		columnLayoutCtr.addCssClassToMain("b_menu_toolbar");
		listenTo(columnLayoutCtr); // auto dispose

		this.putInitialPanel(columnLayoutCtr.getInitialComponent());

		if (!cp.isOLATContentPackage()) {
			showWarning("maincontroller.cp.created.with.third.party.editor");
		}
	}

	/**
	 * initializes a special view, where the user is informed about errors. (while
	 * loading cp)
	 * 
	 * @param ureq
	 * @param wControl
	 * @param cp
	 */
	private void initErrorView(UserRequest ureq, WindowControl wControl, String errorString) {
		Panel p = new Panel("errorPanel");
		columnLayoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, p, "cptestmain");
		this.putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		Long oresId = cp.getResourcable().getResourceableId();
		logAudit("cp editor closing. oresId: " + oresId, null);
		if (lock.isSuccess() && contentCtr != null) {
			// Save CP to zip
			CPManager.getInstance().writeToZip(cp);
		}
		// In any case, release the lock
		CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		logAudit("finished editing cp. ores-id: " + oresId, null);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// nothing
	}

	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == treeCtr) {
			// event from TreeController
			if (event instanceof TreeNodeClickedEvent) {
				TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
				String nodeId = clickedEvent.getNodeId();
				contentCtr.displayPage(ureq, nodeId);

			} else if (event instanceof TreeNodeModifiedEvent) {
				TreeNodeModifiedEvent nodeEvent = (TreeNodeModifiedEvent) event;
				String nodeId = nodeEvent.getNodeId();
				String newItemTitle = nodeEvent.getModifiedValue();
				treeCtr.updateNode(nodeId, newItemTitle);

			} else if (event.getCommand().equals("New Page")) {
				String newIdentifier = treeCtr.addNewHTMLPage();
				contentCtr.displayPage(ureq, newIdentifier);

			} else if (event instanceof NewCPPageEvent) {
				contentCtr.displayPage(ureq, ((NewCPPageEvent) event).getCPPage().getIdentifier());
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
