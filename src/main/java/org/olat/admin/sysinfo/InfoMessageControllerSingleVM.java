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

import java.util.Date;

import org.olat.admin.AdminModule;
import org.olat.admin.sysinfo.manager.CustomStaticFolderManager;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * edit sysinfo messages which are displayed systemwide on every page or on the login page
 * 
 * <P>
 * Initial Date:  16.12.2008 <br>
 * @author guido
 */
public class InfoMessageControllerSingleVM extends BasicController {
	
	private final Link infomsgEditButton;
	private final Link infomsgClearButton;
	private final Link maintenancemsgEditButton;
	private final Link maintenancemsgClearButton;
	private final VelocityContainer infoMsgView;
	private final VelocityContainer infoMsgEdit;
	private final InfoMsgForm infoMsgForm;
	private final InfoMsgForm maintenanceMsgForm;
	private final FolderRunController staticFolderCtrl;
	private final StackedPanel container;
	
	@Autowired
	private PropertyManager pm;
	@Autowired
	private InfoMessageManager mrg;
	@Autowired
	private CustomStaticFolderManager staticFolderMgr;
	
	public InfoMessageControllerSingleVM(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		infoMsgView = createVelocityContainer("infomsg");
		infoMsgEdit = createVelocityContainer("infomsgEdit");
		infoMsgView.contextPut("cluster", Boolean.FALSE);
		infoMsgEdit.contextPut("cluster", Boolean.FALSE);

		Property p = pm.findProperty(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, AdminModule.PROPERTY_MAINTENANCE_MESSAGE);
		String adminToken = (p == null ? "" : p.getStringValue());
		infoMsgView.contextPut("admintoken", adminToken);
		String protocol = Settings.getURIScheme().substring(0, Settings.getURIScheme().length()-1);
		String changeInfoUrl = Settings.getServerContextPathURI() + "/admin.html?token=TOKEN&cmd=setinfomessage&msg=Lorem Ipsum";
		String changeMaintenanceUrl = Settings.getServerContextPathURI() + "/admin.html?token=TOKEN&cmd=setmaintenancemessage&msg=Lorem Ipsum";
		String getMessagesUrl = Settings.getServerContextPathURI() + "/admin.html?token=TOKEN&cmd=getmessages";
		infoMsgView.contextPut("admintokenusage", translate("infomsg.token.usage", new String[] { protocol }));
		infoMsgView.contextPut("changeInfoUrl", changeInfoUrl);
		infoMsgView.contextPut("changeMaintenanceUrl", changeMaintenanceUrl);
		infoMsgView.contextPut("getMessagesUrl", getMessagesUrl);

		
		infomsgEditButton = LinkFactory.createButton("infomsgEdit", infoMsgView, this);
		infomsgEditButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		infomsgClearButton = LinkFactory.createButton("infomsgClear", infoMsgView, this);
		infomsgClearButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
		maintenancemsgEditButton = LinkFactory.createButton("maintenancemsgEdit", infoMsgView, this);
		maintenancemsgEditButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		maintenancemsgEditButton.setElementCssClass("o_sel_maintenance_msg_edit");
		maintenancemsgClearButton = LinkFactory.createButton("maintenancemsgClear", infoMsgView, this);
		maintenancemsgClearButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
		maintenancemsgClearButton.setElementCssClass("o_sel_maintenance_msg_clear");
		
		// Info message stuff
		SysInfoMessage sysInfoMsg = mrg.getInfoMessage();
		infoMsgView.contextPut("infomsg", sysInfoMsg);
		infoMsgForm = new InfoMsgForm(ureq, control, sysInfoMsg);
		listenTo(infoMsgForm);
		infoMsgEdit.put("infoMsgForm", infoMsgForm.getInitialComponent());
		
		// Maintenance message stuff
		SysInfoMessage sysMaintenanceMsg = mrg.getMaintenanceMessage();
		infoMsgView.contextPut("maintenanceMsg", sysMaintenanceMsg);		
		maintenanceMsgForm = new InfoMsgForm(ureq, control, sysMaintenanceMsg);
		listenTo(maintenanceMsgForm);
		infoMsgEdit.put("maintenanceMsgForm", maintenanceMsgForm.getInitialComponent());
		
		// /customizing/static/
		staticFolderCtrl = new FolderRunController(staticFolderMgr.getRootContainer(), true, ureq, control);
		listenTo(staticFolderCtrl);
		infoMsgView.put("staticFolder", staticFolderCtrl.getInitialComponent());
		
		String url = Settings.getServerContextPathURI() + "/raw/static/";
		infoMsgView.contextPut("extlink", url);

		container = putInitialPanel(infoMsgView);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == infomsgEditButton){
			infoMsgEdit.contextPut("infoEdit", Boolean.TRUE);
			infoMsgEdit.contextPut("cluster", Boolean.FALSE);
			container.pushContent(infoMsgEdit);
		} else if (source == maintenancemsgEditButton){
			infoMsgEdit.contextPut("infoEdit", Boolean.FALSE);
			infoMsgEdit.contextPut("cluster", Boolean.FALSE);
			container.pushContent(infoMsgEdit);
		} else if (source == maintenancemsgClearButton){
			mrg.setMaintenanceMessage(null, null, null, true);
			infoMsgView.contextRemove("maintenanceMsg");
			maintenanceMsgForm.reset();
		} else if (source == infomsgClearButton){
			mrg.setInfoMessage(null, null, null, false);
			infoMsgView.contextRemove("infomsg");
			infoMsgForm.reset();
		}
	}
		
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == infoMsgForm) {
			if(event == Event.DONE_EVENT) {
				String infoMsg = infoMsgForm.getInfoMsg();
				Date start = infoMsgForm.getStart();
				Date end = infoMsgForm.getEnd();
				boolean clearOnReboot = infoMsgForm.getClearOnReboot();
				SysInfoMessage sysInfoMsg = mrg.setInfoMessage(infoMsg, start, end, clearOnReboot);
				infoMsgView.contextPut("infomsg", sysInfoMsg);				
				if (sysInfoMsg.hasMessage()) {	
					getWindowControl().setInfo("New info message activated.");
				}
			}
			container.popContent();
		} else if (source == maintenanceMsgForm) {
			if(event == Event.DONE_EVENT) {
				String maintenanceMsg = maintenanceMsgForm.getInfoMsg();
				Date start = maintenanceMsgForm.getStart();
				Date end = maintenanceMsgForm.getEnd();
				boolean clearOnReboot = maintenanceMsgForm.getClearOnReboot();
				SysInfoMessage sysMaintenanceMsg = mrg.setMaintenanceMessage(maintenanceMsg, start, end, clearOnReboot);
				infoMsgView.contextPut("maintenanceMsg", sysMaintenanceMsg);		
				if (sysMaintenanceMsg.hasMessage()) {
					getWindowControl().setInfo("New maintenance message activated.");
				}
			}
			container.popContent();
		}
	}
	
	protected VelocityContainer getViewContainer() {
		return infoMsgView;
	}
	
	protected VelocityContainer getEditContainer() {
		return infoMsgEdit;
	}
	
	protected StackedPanel getMainContainer() {
		return container;
	}
	
	protected InfoMsgForm getMaintenanceMsgForm() {
		return maintenanceMsgForm;
	}
}