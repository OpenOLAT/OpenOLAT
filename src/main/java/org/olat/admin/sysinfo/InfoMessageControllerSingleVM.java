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
package org.olat.admin.sysinfo;

import org.olat.admin.AdminModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.util.GlobalStickyMessage;
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
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 * Description:<br>
 * edit sysinfo messages which are displayed systemwide on every page or on the login page
 * 
 * <P>
 * Initial Date:  16.12.2008 <br>
 * @author guido
 */
public class InfoMessageControllerSingleVM extends BasicController {
	
	private Link infomsgEditButton,infomsgClearButton, maintenancemsgEditButton,maintenancemsgClearButton;
	private VelocityContainer infoMsgView, infoMsgEdit;
	private InfoMsgForm infoMsgForm, maintenanceMsgForm;
	private Panel container;
	
	/**
	 * 
	 * @param ureq
	 * @param control
	 */
	public InfoMessageControllerSingleVM(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		container = new Panel("container");
		infoMsgView = createVelocityContainer("infomsg");
		infoMsgEdit = createVelocityContainer("infomsgEdit");
		infoMsgView.contextPut("cluster", Boolean.FALSE);
		infoMsgEdit.contextPut("cluster", Boolean.FALSE);
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, AdminModule.PROPERTY_MAINTENANCE_MESSAGE);
		String adminToken = (p == null ? "" : p.getStringValue());
		infoMsgView.contextPut("admintoken", adminToken);
		
		infomsgEditButton = LinkFactory.createButton("infomsgEdit", infoMsgView, this);
		infomsgClearButton = LinkFactory.createButton("infomsgClear", infoMsgView, this);
		maintenancemsgEditButton = LinkFactory.createButton("maintenancemsgEdit", infoMsgView, this);
		maintenancemsgClearButton = LinkFactory.createButton("maintenancemsgClear", infoMsgView, this);
		
		//info message stuff
		InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
		String infoMsg = mrg.getInfoMessage();
		if (infoMsg != null && infoMsg.length() > 0) {
			infoMsgView.contextPut("infomsg", infoMsg);
		}
		infoMsgForm = new InfoMsgForm(ureq, control, infoMsg);
		listenTo(infoMsgForm);
		
		infoMsgEdit.put("infoMsgForm", infoMsgForm.getInitialComponent());
		
		//maintenance message stuff
		String maintenanceMsg = GlobalStickyMessage.getGlobalStickyMessage(true);
		if (maintenanceMsg != null && maintenanceMsg.length() > 0) {
			infoMsgView.contextPut("maintenanceMsgAllNodes", maintenanceMsg);
		}
		maintenanceMsgForm = new InfoMsgForm(ureq, control, maintenanceMsg);
		listenTo(maintenanceMsgForm);
		infoMsgEdit.put("maintenanceMsgForm", maintenanceMsgForm.getInitialComponent());
		
		container.setContent(infoMsgView);

		putInitialPanel(container);
	}

	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == infomsgEditButton){
			infoMsgEdit.contextPut("infoEdit", Boolean.TRUE);
			infoMsgEdit.contextPut("cluster", Boolean.FALSE);
			container.pushContent(infoMsgEdit);
		}
		else if (source == maintenancemsgEditButton){
			infoMsgEdit.contextPut("infoEdit", Boolean.FALSE);
			infoMsgEdit.contextPut("cluster", Boolean.FALSE);
			container.pushContent(infoMsgEdit);
		}
		
		// clear buttons
		else if (source == maintenancemsgClearButton){
			GlobalStickyMessage.setGlobalStickyMessage("", true);
			infoMsgView.contextRemove("maintenanceMsgAllNodes");
			maintenanceMsgForm.reset();
		}
		else if (source == infomsgClearButton){
			InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
			mrg.setInfoMessage("");
			infoMsgView.contextRemove("infomsg");
			infoMsgForm.reset();
		}
	}
		
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == infoMsgForm && event == Event.DONE_EVENT) {
				String infoMsg = infoMsgForm.getInfoMsg();
				InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
				mrg.setInfoMessage(infoMsg);
				if (infoMsg != null && infoMsg.length() > 0) {
					infoMsgView.contextPut("infomsg", infoMsg);
					getWindowControl().setInfo("New info message activated.");
				} else {
					infoMsgView.contextRemove("infomsg");
				}
				container.popContent();
		} else if (source == maintenanceMsgForm && event == Event.DONE_EVENT) {
				String maintenanceMsg = maintenanceMsgForm.getInfoMsg();
				GlobalStickyMessage.setGlobalStickyMessage(maintenanceMsg, true);
				if (maintenanceMsg != null && maintenanceMsg.length() > 0) {
					infoMsgView.contextPut("maintenanceMsgAllNodes", maintenanceMsg);
					getWindowControl().setInfo("New maintenance message activated.");
				} else {
					infoMsgView.contextRemove("maintenanceMsgAllNodes");
				}
				container.popContent();
		}
		
		if (event == Event.CANCELLED_EVENT && (source == infoMsgForm || source == maintenanceMsgForm)) {
			container.popContent();
		}
		
	}
	
	protected VelocityContainer getViewContainer() {
		return infoMsgView;
	}
	protected VelocityContainer getEditContainer() {
		return infoMsgEdit;
	}
	protected Panel getMainContainer() {
		return container;
	}
	protected InfoMsgForm getMaintenanceMsgForm() {
		return maintenanceMsgForm;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}




}
