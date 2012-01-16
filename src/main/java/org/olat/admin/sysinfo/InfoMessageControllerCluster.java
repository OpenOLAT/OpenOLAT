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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.util.GlobalStickyMessage;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * Provides a maintenace message which is visible only on one node in the cluster
 * 
 * <P>
 * Initial Date:  16.12.2008 <br>
 * @author guido
 */
public class InfoMessageControllerCluster extends InfoMessageControllerSingleVM {

	private Link infomsgEditButtonCluster,infomsgClearButtonCluster,maintenancemsgEditButtonCluster,maintenancemsgClearButtonCluster;
	private InfoMsgForm infoMsgFormCluster, maintenanceMsgFormCluster;
	
	/**
	 * @param ureq
	 * @param control
	 */
	public InfoMessageControllerCluster(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		
		getViewContainer().contextPut("cluster", Boolean.TRUE);
		
		
		infomsgEditButtonCluster = LinkFactory.createButton("infomsgEditCluster", getViewContainer(), this);
		infomsgClearButtonCluster = LinkFactory.createButton("infomsgClearCluster", getViewContainer(), this);
		maintenancemsgEditButtonCluster = LinkFactory.createButton("maintenancemsgEditCluster", getViewContainer(), this);
		maintenancemsgClearButtonCluster = LinkFactory.createButton("maintenancemsgClearCluster", getViewContainer(), this);
		
		//info message stuff
		InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
		String infoMsg = mrg.getInfoMessageNodeOnly();
		if (infoMsg != null && infoMsg.length() > 0) {
			getViewContainer().contextPut("infoMsgCluster", infoMsg);
		}
		infoMsgFormCluster = new InfoMsgForm(ureq, control, infoMsg);
		listenTo(infoMsgFormCluster);
		getEditContainer().put("infoMsgFormCluster", infoMsgFormCluster.getInitialComponent());
		
		//maintenance message stuff
		String maintenanceMsg = GlobalStickyMessage.getGlobalStickyMessage(false);
		if (maintenanceMsg != null && maintenanceMsg.length() > 0) {
			getViewContainer().contextPut("maintenanceMsgThisNodeOnly", maintenanceMsg);
		}
		maintenanceMsgFormCluster = new InfoMsgForm(ureq, control, maintenanceMsg);
		listenTo(maintenanceMsgFormCluster);
		getEditContainer().put("maintenanceMsgFormCluster", maintenanceMsgFormCluster.getInitialComponent());
		
		setTranslator(super.getTranslator());
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		
		super.event(ureq, source, event);
		
		if (source == infomsgEditButtonCluster) {
			getEditContainer().contextPut("infoEdit", Boolean.TRUE);
			getEditContainer().contextPut("cluster", Boolean.TRUE);
			getMainContainer().pushContent(getEditContainer());
		}
		else if (source == maintenancemsgEditButtonCluster) {
			getEditContainer().contextPut("infoEdit", Boolean.FALSE);
			getEditContainer().contextPut("cluster", Boolean.TRUE);
			getMainContainer().pushContent(getEditContainer());
		}
		
		// clear buttons
		else if (source == maintenancemsgClearButtonCluster) {
			GlobalStickyMessage.setGlobalStickyMessage("", true);
			getViewContainer().contextRemove("maintenanceMsgThisNodeOnly");
			maintenanceMsgFormCluster.reset();
		}
		else if (source == infomsgClearButtonCluster) {
			InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
			mrg.setInfoMessage("");
			getViewContainer().contextRemove("infoMsgCluster");
			infoMsgFormCluster.reset();
		}
			
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		
		super.event(ureq, source, event);
		
		if (source == infoMsgFormCluster && event == Event.DONE_EVENT) {
					String infoMsg = infoMsgFormCluster.getInfoMsg();
					InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
					mrg.setInfoMessageNodeOnly(infoMsg);
					if (infoMsg != null && infoMsg.length() > 0) {
						getViewContainer().contextPut("infoMsgCluster", infoMsg);
						getWindowControl().setInfo("New info message activated. Only on this node!");
					} else {
						getViewContainer().contextRemove("infoMsgCluster");
					}
					getMainContainer().popContent();
		} else if (source == maintenanceMsgFormCluster && event == Event.DONE_EVENT) {
					String infoMsg = maintenanceMsgFormCluster.getInfoMsg();
					GlobalStickyMessage.setGlobalStickyMessage(infoMsg, false);
					if (infoMsg != null && infoMsg.length() > 0) {
						getViewContainer().contextPut("maintenanceMsgThisNodeOnly", infoMsg);
						getWindowControl().setInfo("New maintenance message activated. Only on this node!");
						getMaintenanceMsgForm().reset();
					} else {
						getViewContainer().contextRemove("maintenanceMsgThisNodeOnly");
					}
					getMainContainer().popContent();
				
		} 
		
		
		if (event == Event.CANCELLED_EVENT  && (source == infoMsgFormCluster || source == maintenanceMsgFormCluster)) {
			getMainContainer().popContent();
		}
		
	}

}
