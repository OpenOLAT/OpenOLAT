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
package org.olat.group.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.area.BGAreaManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.ui.area.BGAreaFormController;
import org.olat.testutils.codepoints.server.Codepoint;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * Shows the group form until a a group could be created or the form is
 * cancelled. A group can not be created if its name already exists in the given
 * context. This will show an error on the form.<br>
 * Sends {@link Event#DONE_EVENT} in the case of successfully group creation and
 * {@link Event#CANCELLED_EVENT} if the user no longer wishes to create a group.
 * <P>
 * Initial Date: 28.06.2007 <br>
 * 
 * @author patrickb
 */
public class NewAreaController extends BasicController {

	private BGContext bgContext;
	private VelocityContainer contentVC;
	private BGAreaFormController areaCreateController;
	private boolean bulkMode = false;
	private Set<BGArea> newAreas;
	private HashSet<String> newAreaNames;
	private BGAreaManager areaManager;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param minMaxEnabled
	 * @param bgContext
	 * @param bulkMode
	 * @param csvGroupNames
	 */
	NewAreaController(UserRequest ureq, WindowControl wControl, BGContext bgContext, boolean bulkMode, String csvAreaNames) {
		super(ureq, wControl);
		this.bgContext = bgContext;
		this.bulkMode = bulkMode;
		//
		this.areaManager = BGAreaManagerImpl.getInstance();
		this.contentVC = this.createVelocityContainer("areaform");
		this.contentVC.contextPut("bulkMode", bulkMode ? Boolean.TRUE : Boolean.FALSE);
		//
		this.areaCreateController = new BGAreaFormController(ureq, wControl, null, bulkMode);
		listenTo(this.areaCreateController);
		this.contentVC.put("areaForm", this.areaCreateController.getInitialComponent());
		
		if (csvAreaNames != null) {
			this.areaCreateController.setAreaName(csvAreaNames);
		}
		this.putInitialPanel(this.contentVC);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// Don't dispose anything
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// Don't do anything.
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == this.areaCreateController) {
			if (event == Event.DONE_EVENT) {
				String areaDesc = this.areaCreateController.getAreaDescription();
				
				Set<String> allNames = new HashSet<String>();
				if (this.bulkMode) {
					allNames = this.areaCreateController.getGroupNames();
				} else {
					allNames.add(this.areaCreateController.getAreaName());
				}

				if(areaManager.checkIfOneOrMoreNameExistsInContext(allNames, bgContext)){
					// set error of non existing name
					this.areaCreateController.setAreaNameExistsError(null);
				} else {
					Codepoint.codepoint(this.getClass(), "createArea");
					// create bulkgroups only if there is no name which already exists. 
					newAreas = new HashSet<BGArea>();
					newAreaNames = new HashSet<String>();
					for (Iterator<String> iter = allNames.iterator(); iter.hasNext();) {
						String areaName = iter.next();
						BGArea newArea = areaManager.createAndPersistBGAreaIfNotExists(areaName, areaDesc, bgContext);
						newAreas.add(newArea);
						newAreaNames.add(areaName);
					}
					// do loggin if ual given
					for (Iterator<BGArea> iter = newAreas.iterator(); iter.hasNext();) {
						BGArea a = iter.next();
						ThreadLocalUserActivityLogger.log(GroupLoggingAction.AREA_CREATED, getClass(), LoggingResourceable.wrap(a));	
					}						
					// workflow successfully finished
					// so far no events on the systembus to inform about new groups in BGContext 
					fireEvent(ureq, Event.DONE_EVENT);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				// workflow cancelled
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	/**
	 * name of created area
	 * @return
	 */
	public String getCreatedAreaName(){
		return newAreas.iterator().next().getName();
	}
	
	/**
	 * if Event.DONE_EVENT received the return value is always NOT NULL. If
	 * Event_FORM_CANCELLED ist received this will be null.
	 * 
	 * @return
	 */
	public BGArea getCreatedArea() {
		return newAreas.iterator().next();
	}
	
	/**
	 * in bulkmode the created ares
	 * @return
	 */
	public Set<BGArea> getCreatedAreas(){
		return newAreas;
	}
	
	/**
	 * in bulkmode the validated area names
	 * @return
	 */
	public Set<String> getCreatedAreaNames(){
		return newAreaNames;
	}
	
	/**
	 * if bulkmode is on or not
	 * @return
	 */
	public boolean isBulkMode(){
		return bulkMode;
	}

}
