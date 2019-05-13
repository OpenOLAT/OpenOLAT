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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 * Description:<br>
 * Set/get the Info Message property
 * 
 * <P>
 * Initial Date:  12.08.2008 <br>
 * @author guido
 */
public class InfoMessageManager implements GenericEventListener{
	
	private static final String INFO_MSG = "InfoMsg";
	private static final String INFO_MSG_NODE_ONLY = "InfoMsgNode-";
	//random long to make sure we create always the same dummy ores
	private static final Long KEY = Long.valueOf(857394857);
	protected static String infoMessage;
	private static String infoMessageNodeOnly;
	private static final OLATResourceable INFO_MESSAGE_ORES = OresHelper.createOLATResourceableType(InfoMessageManager.class);
	public static final String EMPTY_MESSAGE = "";
	//identifies a node in the cluster
	private int nodeId;
	private AutoCreator actionControllerCreator;
	private CoordinatorManager coordinatorManager;
	
	/**
	 * [used by spring]
	 * @param nodeId
	 */
	private InfoMessageManager(CoordinatorManager coordinatorManager, int nodeId) {
		this.coordinatorManager = coordinatorManager;
		// it must exist, ensured by LoginModule
		infoMessage = EMPTY_MESSAGE;
		String currInfoMsg = getInfoMsgProperty(INFO_MSG).getTextValue();
		if(StringHelper.containsNonWhitespace(currInfoMsg)){
			// set info message on startup OLAT-3539
			infoMessage = currInfoMsg;
		}
		
		infoMessageNodeOnly = EMPTY_MESSAGE;
		String currInfoMsgNode = getInfoMsgProperty(INFO_MSG_NODE_ONLY+nodeId).getTextValue();
		if(StringHelper.containsNonWhitespace(currInfoMsgNode)){
			// set info message on startup OLAT-3539
			infoMessageNodeOnly = currInfoMsgNode;
		}
		
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, INFO_MESSAGE_ORES);
		this.nodeId = nodeId;
	}
	

	/**
	 * @return the info message configured in the admin area
	 */
	public String getInfoMessage() {
		return infoMessage;
	}

	/**
	 * @param message The new info message that will show up on the login screen
	 * Synchronized to prevent two users creating or updating the info message property
	 * at the same time
	 */
	public void setInfoMessage(final String message) { //o_clusterOK synchronized
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(INFO_MSG, KEY);
		
		coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor(){

			public void execute() {
				PropertyManager pm = PropertyManager.getInstance();
				Property p = pm.findProperty(null, null, null, "_o3_", INFO_MSG);
				if (p == null) {
					p =	pm.createPropertyInstance(null,	null,	null,	"_o3_", INFO_MSG, null, null, null, "");
					pm.saveProperty(p);
				}
				p.setTextValue(message);
				//set Message in RAM
				InfoMessageManager.infoMessage = message;
				pm.updateProperty(p);
			}
			
		});//end syncerCallback
		EventBus eb = coordinatorManager.getCoordinator().getEventBus(); 
		MultiUserEvent mue = new MultiUserEvent(message);
		eb.fireEventToListenersOf(mue, INFO_MESSAGE_ORES);
	}

	private Property getInfoMsgProperty(final String key) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(INFO_MSG, KEY);
		
		return coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Property>() {
		
			public Property execute() {
			PropertyManager pm = PropertyManager.getInstance();
			Property p = pm.findProperty(null, null, null, "_o3_", key);
			if (p == null) {
				p =	pm.createPropertyInstance(null,	null,	null,	"_o3_", key, null, null, null, "");
				pm.saveProperty(p);
			}
			return p;
			}
			
		});//end syncerCallback
	}

	public void event(Event event) {
		if (event instanceof MultiUserEvent) {
			MultiUserEvent mue = (MultiUserEvent) event;
			//do not use setInfoMessage(..) this event comes in from another node, where the infomessage was set.
			InfoMessageManager.infoMessage = mue.getCommand();
		}
	}

	/**
	 * set info message on node level only, no need to sync
	 * @param message
	 */
	public void setInfoMessageNodeOnly(String message) {
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, null, "_o3_", INFO_MSG_NODE_ONLY+nodeId);
		if (p == null) {
			p =	pm.createPropertyInstance(null,	null,	null,	"_o3_", INFO_MSG_NODE_ONLY+nodeId, null, null, null, "");
			pm.saveProperty(p);
		}
		p.setTextValue(message);
		//set Message in RAM
		InfoMessageManager.infoMessageNodeOnly = message;
		pm.updateProperty(p);
	}

	public String getInfoMessageNodeOnly() {
		return infoMessageNodeOnly;
	}
	
	/**
	 * get an controller instance, either the singleVM or the cluster version
	 * @param ureq
	 * @param control
	 */
	public Controller getInfoMessageController(UserRequest ureq, WindowControl control) {
		return actionControllerCreator.createController(ureq, control);
	}
	
	/**
	 * [used by spring]
	 */
	public void setActionController(ControllerCreator actionControllerCreator) {
		this.actionControllerCreator = (AutoCreator) actionControllerCreator;
	}
	

}
