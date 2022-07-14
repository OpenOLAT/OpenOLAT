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

import org.olat.core.commons.fullWebApp.util.GlobalStickyMessage;
import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
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
public class InfoMessageManager implements GenericEventListener {

	private static final String INFO_MSG = "InfoMsg";
	private static final String INFO_MSG_NODE_ONLY = "InfoMsgNode-";
	private static final String MAINTENANCE_MSG = "MaintenanceMsg";
	private static final String MAINTENANCE_MSG_NODE_ONLY = "MaintenanceMsgNode-";
	
	//random long to make sure we create always the same dummy ores
	private static final Long KEY = Long.valueOf(857394857);
	
	private SysInfoMessage infoMessage;
	private SysInfoMessage infoMessageNodeOnly;
	private SysInfoMessage maintenanceMessage;	
	private SysInfoMessage maintenanceMessageNodeOnly;
	
	private static final OLATResourceable INFO_MESSAGE_ORES = OresHelper.createOLATResourceableType(InfoMessageManager.class);
	//identifies a node in the cluster
	private int nodeId;
	private CoordinatorManager coordinatorManager;
	
	/**
	 * [used by spring]
	 * @param nodeId
	 */
	private InfoMessageManager(CoordinatorManager coordinatorManager, int nodeId) {
		this.coordinatorManager = coordinatorManager;
		// Init InfoMessage
		infoMessage = loadSysInfoMessage(INFO_MSG);
		// Init InfoMessage for this node only
		infoMessageNodeOnly = loadSysInfoMessage(INFO_MSG_NODE_ONLY + nodeId);
		// Init maintenanceMessage
		maintenanceMessage = loadSysInfoMessage(MAINTENANCE_MSG);
		GlobalStickyMessage.setGlobalStickyMessage(maintenanceMessage.getTimedMessage(), true);		
		// Init maintenanceMessage for this node only
		maintenanceMessageNodeOnly = loadSysInfoMessage(MAINTENANCE_MSG_NODE_ONLY + nodeId);
		GlobalStickyMessage.setGlobalStickyMessage(maintenanceMessageNodeOnly.getTimedMessage(), false);
		
		// Register for info and maintenance change events
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, INFO_MESSAGE_ORES);
		this.nodeId = nodeId;
	}		
	

	/**
	 * Get the currently configured info message displayed on all nodes
	 * @return the SysInfoMessage representing the message
	 */
	public SysInfoMessage getInfoMessage() {
		return infoMessage;
	}
	/**
	 * Get the currently configured info message displayed only on this node
	 * @return the SysInfoMessage representing the message
	 */
	public SysInfoMessage getInfoMessageNodeOnly() {
		return infoMessageNodeOnly;
	}
	
	/**
	 * Get the currently configured maintenance message displayed on all nodes
	 * @return the SysInfoMessage representing the message
	 */
	public SysInfoMessage getMaintenanceMessage() {
		return maintenanceMessage;
	}
	
	/**
	 * Get the currently configured maintenance message displayed only on this node
	 * @return the SysInfoMessage representing the message
	 */
	public SysInfoMessage getMaintenanceMessageNodeOnly() {
		return maintenanceMessageNodeOnly;
	}
	
	
	
	/**
	 * Set a new info message visible only on all nodes
	 * 
	 * @param message The message: empty or NULL value means "no message"
	 * @param start The optional display start date or NULL to start immediately
	 * @param end The optional display end date  or NULL to never expire the message
	 * @param clearOnRestart true: remove the message when the system restarts;
	 *                       false: persist message
	 * @return The SysInfoMessage
	 */
	public SysInfoMessage setInfoMessage(final String message, final Date start, final Date end, boolean clearOnRestart) {
		infoMessage = new SysInfoMessage(INFO_MSG, message, start, end, clearOnRestart);
		saveSysInfoMessageAndFireEvent(infoMessage);	
		return infoMessage;
	}
	
	/**
	 * Set a new info message visible only on this node
	 * 
	 * @param message The message: empty or NULL value means "no message"
	 * @param start The optional display start date or NULL to start immediately
	 * @param end The optional display end date  or NULL to never expire the message
	 * @param clearOnRestart true: remove the message when the system restarts;
	 *                       false: persist message
	 * @return The SysInfoMessage
	 */
	public SysInfoMessage setInfoMessageNodeOnly(final String message, final Date start, final Date end, boolean clearOnRestart) {
		infoMessageNodeOnly = new SysInfoMessage(INFO_MSG_NODE_ONLY + nodeId, message, start, end, clearOnRestart);
		saveSysInfoMessageAndFireEvent(infoMessageNodeOnly);		
		return infoMessageNodeOnly;
	}	
	
	/**
	 * Set a new maintenance message visible on all nodes and notify everybody about the change
	 * 
	 * @param message The message: empty or NULL value means "no message"
	 * @param start The optional display start date or NULL to start immediately
	 * @param end The optional display end date  or NULL to never expire the message
	 * @param clearOnRestart true: remove the message when the system restarts;
	 *                       false: persist message
	 * @return The SysInfoMessage
	 */
	public SysInfoMessage setMaintenanceMessage(final String message,final  Date start, final Date end, boolean clearOnRestart) {
		this.maintenanceMessage = new SysInfoMessage(MAINTENANCE_MSG, message, start, end, clearOnRestart);
		GlobalStickyMessage.setGlobalStickyMessage(this.maintenanceMessage.getTimedMessage(), true);
		saveSysInfoMessageAndFireEvent(maintenanceMessage);
		return maintenanceMessage;
	}	
	
	/**
	 * Set a new maintenance message visible only on this node and notify everybody about the change
	 * 
	 * @param message The message: empty or NULL value means "no message"
	 * @param start The optional display start date or NULL to start immediately
	 * @param end The optional display end date  or NULL to never expire the message
	 * @param clearOnRestart true: remove the message when the system restarts;
	 *                       false: persist message
	 * @return The SysInfoMessage
	 */
	public SysInfoMessage setMaintenanceMessageNodeOnly(final String message, final Date start, final Date end, boolean clearOnRestart)  {
		this.maintenanceMessageNodeOnly = new SysInfoMessage(MAINTENANCE_MSG_NODE_ONLY + nodeId, message, start, end, clearOnRestart);
		GlobalStickyMessage.setGlobalStickyMessage(this.maintenanceMessageNodeOnly.getTimedMessage(), false);
		saveSysInfoMessageAndFireEvent(maintenanceMessageNodeOnly);		
		return maintenanceMessageNodeOnly;
	}

	
	/**
	 * Method called by MaintenanceMessageJob to update the maintenance message UI
	 * of logged in users. This does not modify the message in anyway.
	 */
	protected void updateMaintenanceMessageFromJob() {
		if (maintenanceMessage.hasMessage()) {
			String newMaintenanceMessage = maintenanceMessage.getTimedMessage();
			String oldMaintenanceMessage = GlobalStickyMessage.getGlobalStickyMessage(true);
			if (oldMaintenanceMessage == null) {
				// fix for comparison with the empty timed message
				oldMaintenanceMessage = SysInfoMessage.EMPTY_MESSAGE;
			}
			if (!newMaintenanceMessage.equals(oldMaintenanceMessage)) {
				GlobalStickyMessage.setGlobalStickyMessage(newMaintenanceMessage, true);			
			}			
		}
		if (maintenanceMessageNodeOnly.hasMessage()) {
			String newMaintenanceNodeOnlyMessage = maintenanceMessageNodeOnly.getTimedMessage();
			String oldMaintenanceNodeOnlyMessage = GlobalStickyMessage.getGlobalStickyMessage(false);
			if (oldMaintenanceNodeOnlyMessage == null) {
				// fix for comparison with the empty timed message
				oldMaintenanceNodeOnlyMessage = SysInfoMessage.EMPTY_MESSAGE;
			}
			if (!newMaintenanceNodeOnlyMessage.equals(oldMaintenanceNodeOnlyMessage)) {
				GlobalStickyMessage.setGlobalStickyMessage(newMaintenanceNodeOnlyMessage, false);			
			}			
		}
	}
	
	/**
	 * Persist the given SysInfoMessage in the database and send events to all
	 * listeners (all users on all nodes and the InfoMessageManager on other nodes)
	 * 
	 * @param sysInfoMessage
	 */
	private void saveSysInfoMessageAndFireEvent(final SysInfoMessage sysInfoMessage) { //o_clusterOK synchronized
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(sysInfoMessage.getType(), KEY);
		
		coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor(){

			public void execute() {
				PropertyManager pm = PropertyManager.getInstance();
				Property p = pm.findProperty(null, null, null, "_o3_", sysInfoMessage.getType());
				if (p == null) {
					p =	pm.createPropertyInstance(null,	null,	null,	"_o3_", sysInfoMessage.getType(), null, null, null, "");
					pm.saveProperty(p);
				}
				if (sysInfoMessage.isClearOnRestart()) {
					// Remove any old message and save an empty one instead. On next startup the
					// system will initialize with the cleared message
					p.setTextValue(SysInfoMessage.EMPTY_MESSAGE);
					p.setLongValue(null);
					p.setFloatValue(null);					
				} else {
					// Message stored as text, start as long and end as float in one single property
					// to reduce queries and compact storage
					p.setTextValue(sysInfoMessage.getMessage());
					Date start = sysInfoMessage.getStart();
					p.setLongValue(start == null ? null : start.getTime());
					Date end = sysInfoMessage.getEnd();
					p.setFloatValue(end == null ? null : (float)end.getTime());					
				}
				
				pm.updateProperty(p);
			}
			
		});//end syncerCallback
		
		// Inform everybody on all nodes about changed SysInfoMessage 
		EventBus eb = coordinatorManager.getCoordinator().getEventBus(); 
		SysInfoMessageChangedEvent simce = new SysInfoMessageChangedEvent(sysInfoMessage);
		eb.fireEventToListenersOf(simce, INFO_MESSAGE_ORES);
	}
	
	/**
	 * Load the persisted SysInfoMessage from the database
	 * 
	 * @param type The SysInfoMessage type
	 * @return SysInfoMessage with empty default values, never null
	 */
	private SysInfoMessage loadSysInfoMessage(final String type) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(type, KEY);
		
		return coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<SysInfoMessage>() {
		
			public SysInfoMessage execute() {
			PropertyManager pm = PropertyManager.getInstance();
			Property p = pm.findProperty(null, null, null, "_o3_", type);
			if (p == null) {
				p =	pm.createPropertyInstance(null,	null,	null,	"_o3_", type, null, null, null, "");
				pm.saveProperty(p);
			}
			String msg = p.getTextValue();
			// Message stored as text, start as long and end as float in one single property
			// to reduce queries and compact storage
			Date start = null;
			Long startValue = p.getLongValue();
			if(startValue != null){
				start =  new Date(startValue.longValue());
			} 
			Date end = null;
			Float endValue = p.getFloatValue();
			if(endValue != null){
				end =  new Date(endValue.longValue());
			}	
			
			boolean clearOnRestart = false;
			if ( type.equals(MAINTENANCE_MSG) || type.equals(MAINTENANCE_MSG_NODE_ONLY) ) {
				clearOnRestart = true;
			}
			
			return new SysInfoMessage(type, msg, start, end, clearOnRestart);
			
			}
			
		});//end syncerCallback
	}

	
	/**
	 *  GenericEventListener events sent by this or other nodes.  
	 */
	@Override
	public void event(Event event) {
		if (event instanceof SysInfoMessageChangedEvent) {
			SysInfoMessageChangedEvent simce = (SysInfoMessageChangedEvent) event;
			// Only update our local messages if node fired from another node. If modified
			// on this node, it is already set to the correct values
			if (!simce.isEventOnThisNode()) {
				SysInfoMessage sysInfoMessage = simce.getSysInfoMessage();
				// Set new message in manager for this instance for next usage. 
				// Update in windows done by individual dispatch of the events of each registered controller
				if (sysInfoMessage.getType().equals(INFO_MSG)) {					
					infoMessage = sysInfoMessage;
				} else if (sysInfoMessage.getType().equals(MAINTENANCE_MSG)) {			
					maintenanceMessage = sysInfoMessage;
					GlobalStickyMessage.setGlobalStickyMessage(maintenanceMessage.getTimedMessage(), true);		
				}
				// ignore node-only events
			}
		}
	}
}
