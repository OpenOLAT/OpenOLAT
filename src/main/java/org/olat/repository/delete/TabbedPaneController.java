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

package org.olat.repository.delete;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.UserManager;

/** 
 * Learning-resource deletion tabbed pane controller.
 *  
 * @author Christian Guretzki
 */
public class TabbedPaneController extends BasicController implements ControllerEventListener {
	// NLS support
	
	private static final String NLS_ERROR_NOACCESS_TO_USER = "error.noaccess.to.user";
	
	private VelocityContainer myContent;

	// controllers used in tabbed pane
	private TabbedPane repositoryDeleteTabP;
	private SelectionController selectionCtr;
	private StatusController    deleteStatusCtr;
	private ReadyToDeleteController readyToDeleteCtr;

	private LockResult lock;

	/**
	 * Constructor that creates a back - link as default
	 * @param ureq
	 * @param wControl
	 * @param identity
	 */
	public TabbedPaneController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	
		if ( ureq.getUserSession().getRoles().isOLATAdmin() ) {
			// Acquire lock for hole delete-group workflow
			OLATResourceable lockResourceable = OresHelper.createOLATResourceableTypeWithoutCheck(this.getClass().getName());
			lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockResourceable, ureq.getIdentity(), "deleteGroup");
			if (!lock.isSuccess()) {
				String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(lock.getOwner());
				String text = getTranslator().translate("error.deleteworkflow.locked.by", new String[]{ fullName });
				Controller uiInfoMsgCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, null, text);
				listenTo(uiInfoMsgCtrl);//register to let dispose on dispose of this controller
				putInitialPanel(uiInfoMsgCtrl.getInitialComponent());
				return;
			}

			myContent = createVelocityContainer("deleteTabbedPane");
			initTabbedPane(ureq);
			putInitialPanel(myContent);
		} else {
			String supportAddr = WebappHelper.getMailConfig("mailSupport");
			showWarning(getTranslator().translate(NLS_ERROR_NOACCESS_TO_USER, new String[]{supportAddr}), null);
			putInitialPanel(new Panel("empty"));
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (event.getCommand().equals(TabbedPaneChangedEvent.TAB_CHANGED)) {
			selectionCtr.updateRepositoryEntryList();
			deleteStatusCtr.updateRepositoryEntryList();
			readyToDeleteCtr.updateRepositoryEntryList();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		//
	}

	
	/**
	 * Initialize the tabbed pane according to the users rights and the system
	 * configuration
	 * @param identity
	 * @param ureq
	 */
	private void initTabbedPane(UserRequest ureq) {
		repositoryDeleteTabP = new TabbedPane("repositoryDeleteTabP", ureq.getLocale());
		repositoryDeleteTabP.addListener(this);
		
		selectionCtr = new SelectionController(ureq, getWindowControl());
		listenTo(selectionCtr);
		repositoryDeleteTabP.addTab(translate("delete.workflow.tab.start.process"), selectionCtr.getInitialComponent());

		deleteStatusCtr = new StatusController(ureq, getWindowControl());
		listenTo(deleteStatusCtr);
		repositoryDeleteTabP.addTab(translate("delete.workflow.tab.status.email"), deleteStatusCtr.getInitialComponent());

		readyToDeleteCtr = new ReadyToDeleteController(ureq, getWindowControl());
		listenTo(readyToDeleteCtr);
		repositoryDeleteTabP.addTab(translate("delete.workflow.tab.select.delete"), readyToDeleteCtr.getInitialComponent());
		
		myContent.put("repositoryDeleteTabP", repositoryDeleteTabP);
	}

	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//childcontrollers are registered with listenTo(..) and are disposed by basiccontroller!
		releaseLock();
	}

	/**
	 * Releases the lock for this page if set
	 */
	private void releaseLock() {
		if (lock != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
			lock = null;
		}
	}

}