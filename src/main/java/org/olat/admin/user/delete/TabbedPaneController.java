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

package org.olat.admin.user.delete;

import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

/** 
 * User deletion tabbed pane controller.
 *  
 * @author Christian Guretzki
 */
public class TabbedPaneController extends DefaultController implements ControllerEventListener, Activateable2 {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(TabbedPaneController.class);
	
	private static final String NLS_ERROR_NOACCESS_TO_USER = "error.noaccess.to.user";
	
	private VelocityContainer myContent;
	
	private Translator translator;

	// controllers used in tabbed pane
	private TabbedPane userDeleteTabP;
	private SelectionController userSelectionCtr;
	private StatusController userDeleteStatusCtr;
	private ReadyToDeleteController readyToDeleteCtr;


	/**
	 * Constructor for user delete tabbed pane.
	 * @param ureq
	 * @param wControl
	 * @param identity
	 */
	public TabbedPaneController(UserRequest ureq, WindowControl wControl) {
		super(wControl);
	
		translator = Util.createPackageTranslator(TabbedPaneController.class, ureq.getLocale());

		Boolean canDelete = BaseSecurityModule.USERMANAGER_CAN_DELETE_USER;
		if ( canDelete.booleanValue() || ureq.getUserSession().getRoles().isOLATAdmin() ) {
			myContent = new VelocityContainer("deleteTabbedPane", VELOCITY_ROOT + "/deleteTabbedPane.html", translator, this);
			initTabbedPane(ureq);
			setInitialComponent(myContent);
		} else {
			String supportAddr = WebappHelper.getMailConfig("mailSupport");
			getWindowControl().setWarning(translator.translate(NLS_ERROR_NOACCESS_TO_USER, new String[]{supportAddr}));
			setInitialComponent(new Panel("empty"));
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (event.getCommand().equals(TabbedPaneChangedEvent.TAB_CHANGED)) {
			userSelectionCtr.updateUserList();
			userDeleteStatusCtr.updateUserList();
			readyToDeleteCtr.updateUserList();
			//fxdiff BAKS-7 Resume function
			userDeleteTabP.addToHistory(ureq, getWindowControl());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
	}

	
	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && entries.isEmpty()) return;
		userDeleteTabP.activate(ureq, entries, state);
	}

	/**
	 * Initialize the tabbed pane according to the users rights and the system
	 * configuration
	 * @param identity
	 * @param ureq
	 */
	private void initTabbedPane(UserRequest ureq) {
		userDeleteTabP = new TabbedPane("userDeleteTabP", ureq.getLocale());
		userDeleteTabP.addListener(this);
		
		userSelectionCtr = new SelectionController(ureq, getWindowControl());
		userSelectionCtr.addControllerListener(this);
		userDeleteTabP.addTab(translator.translate("delete.workflow.tab.start.process"), userSelectionCtr.getInitialComponent());

		userDeleteStatusCtr = new StatusController(ureq, getWindowControl());
		userDeleteStatusCtr.addControllerListener(this);
		userDeleteTabP.addTab(translator.translate("delete.workflow.tab.status.email"), userDeleteStatusCtr.getInitialComponent());

		readyToDeleteCtr = new ReadyToDeleteController(ureq, getWindowControl());
		readyToDeleteCtr.addControllerListener(this);
		userDeleteTabP.addTab(translator.translate("delete.workflow.tab.select.delete"), readyToDeleteCtr.getInitialComponent());
				
		myContent.put("userDeleteTabP", userDeleteTabP);
	}

	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (userSelectionCtr != null) {
			userSelectionCtr.dispose();
			userSelectionCtr = null;
		}
		if (userDeleteStatusCtr != null) {
			userDeleteStatusCtr.dispose();
			userDeleteStatusCtr = null;
		}
		if (readyToDeleteCtr != null) {
			readyToDeleteCtr.dispose();
			readyToDeleteCtr = null;
		}
	}

}