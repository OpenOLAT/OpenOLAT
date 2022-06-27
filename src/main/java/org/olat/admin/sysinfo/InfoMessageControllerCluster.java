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

import org.olat.core.CoreSpringFactory;
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
				
		// Info message stuff
		InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
		SysInfoMessage sysInfoMsg = mrg.getInfoMessageNodeOnly();
		getViewContainer().contextPut("infoMsgCluster", sysInfoMsg);
		infoMsgFormCluster = new InfoMsgForm(ureq, control, sysInfoMsg);
		listenTo(infoMsgFormCluster);
		getEditContainer().put("infoMsgFormCluster", infoMsgFormCluster.getInitialComponent());
		
		// Maintenance message stuff
		SysInfoMessage maintenanceMsg = mrg.getMaintenanceMessageNodeOnly();
		getViewContainer().contextPut("maintenanceMsgThisNodeOnly", maintenanceMsg);		
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
			InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
			mrg.setMaintenanceMessageNodeOnly(null, null, null, false);
			getViewContainer().contextRemove("maintenanceMsgThisNodeOnly");
			maintenanceMsgFormCluster.reset();
		}
		else if (source == infomsgClearButtonCluster) {
			InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
			mrg.setInfoMessageNodeOnly(null, null, null, false);
			getViewContainer().contextRemove("infoMsgCluster");
			infoMsgFormCluster.reset();
		}
			
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		
		super.event(ureq, source, event);
		
		if (source == infoMsgFormCluster) {
			if(event == Event.DONE_EVENT) {
				String infoMsg = infoMsgFormCluster.getInfoMsg();
				Date start = infoMsgFormCluster.getStart();
				Date end = infoMsgFormCluster.getEnd();
				InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
				SysInfoMessage sysInfoMsg = mrg.setInfoMessageNodeOnly(infoMsg, start, end, false);
				getViewContainer().contextPut("infoMsgCluster", sysInfoMsg);				
				if (sysInfoMsg.hasMessage()) {	
					getWindowControl().setInfo("New info message activated. Only on this node!");
				}
			}
			getMainContainer().popContent();
		} else if (source == maintenanceMsgFormCluster) {
			if(event == Event.DONE_EVENT) {
				String maintenanceMsg = maintenanceMsgFormCluster.getInfoMsg();
				Date start = maintenanceMsgFormCluster.getStart();
				Date end = maintenanceMsgFormCluster.getEnd();
				InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
				SysInfoMessage sysMaintenanceMsg = mrg.setMaintenanceMessageNodeOnly(maintenanceMsg, start, end, false); 
				getViewContainer().contextPut("maintenanceMsgThisNodeOnly", sysMaintenanceMsg);		
				if (sysMaintenanceMsg.hasMessage()) {
					getWindowControl().setInfo("New maintenance message activated. Only on this node!");
				}
			}
			getMainContainer().popContent();
		} 
		
	}

}
