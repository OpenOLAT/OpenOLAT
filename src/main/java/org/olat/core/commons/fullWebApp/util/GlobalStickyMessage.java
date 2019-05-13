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
package org.olat.core.commons.fullWebApp.util;

import org.olat.core.commons.chiefcontrollers.ChiefControllerMessageEvent;
import org.olat.core.configuration.Initializable;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * a message string which should be displayed on each page of the webbapplication.
 * This is used in the OLAT LMS to display the maintenance message.
 * 
 * <P>
 * Initial Date:  13.07.2009 <br>
 * @author patrickb
 */
public class GlobalStickyMessage implements GenericEventListener, Initializable{

	private static final Logger log = Tracing.createLoggerFor(GlobalStickyMessage.class);
	private static String maintenanceMessageGlobal = null;
	private static GlobalStickyMessage INSTANCE = null;
	private static String maintenanceMessagePerNode = null;
	private static CoordinatorManager coordinatorManager;

	/**
	 * to be used sending MAINTENANCE Messages to the Chiefcontrollers.
	 */
	private static final OLATResourceable GLOBAL_STICKY_MESSAGE = OresHelper.createOLATResourceableInstance(ChiefController.class,Long.valueOf(1));
	
	/**
	 * [used by spring]
	 */
	private GlobalStickyMessage() {
		INSTANCE = this;
	}
	
	@Override
	public void init() {
		registerForGlobalStickyMessage(this, null);
	}
	
	/**
	 * 
	 * @return
	 */
	public static GlobalStickyMessage getInstance(){
		return INSTANCE;
	}
	
	/**
	 * 
	 * @param gel
	 * @param identity
	 */
	public static void registerForGlobalStickyMessage(GenericEventListener gel, Identity identity){
		coordinatorManager.getCoordinator().getEventBus().registerFor(gel, identity, GLOBAL_STICKY_MESSAGE);
	}
	
	/**
	 * 
	 * @param gel
	 */
	public static void deregisterForGlobalStickyMessage(GenericEventListener gel){
		coordinatorManager.getCoordinator().getEventBus().deregisterFor(gel, GLOBAL_STICKY_MESSAGE);
	}
	
	public void event(Event event) {
		if (event instanceof ChiefControllerMessageEvent) {
			ChiefControllerMessageEvent mue = (ChiefControllerMessageEvent) event;
			if(mue.isClusterWideMessage()){
				//do not use setInfoMessage(..) this event comes in from another node, where the infomessage was set.
				GlobalStickyMessage.maintenanceMessageGlobal = mue.getMsg();
			}
		}
	}
	
	/**
	 * if you are intrested only in the presence of a message - independent of global or per node, use this method.
	 * @return null if no message is set, the per-node message first and then the global message.
	 */
	public static String getGlobalStickyMessage() {
		String msg = getGlobalStickyMessage(false);
		if(msg != null) return msg;
		return getGlobalStickyMessage(true);
	}
	
	
	/**
	 * @param boolean to get the global message or the per-node-message.
	 * @return String A global message or null if no such message exists. The
	 *         message will be displayed to all logged in users in a sticky way
	 */
	public static String getGlobalStickyMessage(boolean global) {
		if(global){
			return maintenanceMessageGlobal;
		}else{
			return maintenanceMessagePerNode;
		}
	}

	/**
	 * Sets a new global sticky message and updates all chief controllers to use
	 * the new message. The message will be displayed to all logged in users in
	 * a sticky way
	 * 
	 * @param message The message to display or null if no message should be
	 *          used at all
	 *  @param global, if you like to set a message only on one node set global to false
	 */
	public static void setGlobalStickyMessage(String message, boolean global) {
		message = (StringHelper.containsNonWhitespace(message) ? message : null);
		
		if(global){
			maintenanceMessageGlobal = message;
		}else{
			maintenanceMessagePerNode  = message;
		}
		log.info(Tracing.M_AUDIT, "Setting new maintenance message::" + maintenanceMessageGlobal);
		ChiefControllerMessageEvent mme = new ChiefControllerMessageEvent();
		if(message != null ){
			//create CHANGE_EVENT, and change message to something new
			mme.setMsg(message);
		}else if(maintenanceMessageGlobal == null && maintenanceMessagePerNode == null){
			//remove, clear message, in case no more message should be shown.
			mme = ChiefControllerMessageEvent.CLEAR;
		}
		//send message to all (Full)ChiefControllers
		mme.setClusterWideMessage(global);//
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(mme, GLOBAL_STICKY_MESSAGE);
	}


	/**
	 * [used by spring]
	 * @param coordinatorManager
	 */
	public void setCoordinatorManager(CoordinatorManager coordinatorManager) {
		GlobalStickyMessage.coordinatorManager = coordinatorManager;
	}

}
