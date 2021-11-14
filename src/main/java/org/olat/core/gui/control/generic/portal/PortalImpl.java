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
* <p>
*/ 

package org.olat.core.gui.control.generic.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ObjectCloner;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;

/**
 * Description:<br>
 * The portal implementation has the ability to display a portal page as defined in the
 * WEB-INF/olat_extensions.xml. Use the PortalFactory to create a new portal instance.
 * 
 * <P>
 * Initial Date:  08.07.2005 <br>
 * @author gnaegi
 */
public class PortalImpl extends DefaultController implements Portal, ControllerEventListener {
	private static final Logger log = Tracing.createLoggerFor(PortalImpl.class);
	
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PortalImpl.class);

	private static String MODE_EDIT = "editMode";
	
	private VelocityContainer portalVC;
	private Translator trans;
	private List<List<String>> portalColumns; // list containing the lists of portlets
	private Map<String, PortletContainer> portletContainers; // map of all portlet containers (defined in portal columns + inactive portlets)
	private List<String> inactivePortlets; // list containing the names of inactive portlets
	private String name;
	private boolean editModeEnabled = false;

	/**
	 * Do use PortalFactory for create new Portals
	 */
	public PortalImpl(){
		super(null);
		// used by spring framework. Use PortalFactory to create a runtime portal
	}
	
	/**
	 * Do use PortalFactory for create new Portals!
	 * @param portalName identifyer of the portal
	 * @param ureq
	 * @param wControl
	 * @param portalColumns List containing the default columns and rows
	 * @param portletsConfigurations Map containing the portlet configurations
	 */
	protected PortalImpl(String portalName, UserRequest ureq, WindowControl wControl, List<List<String>> portalColumns) {
		super(wControl);
		this.name = portalName;
		
		this.portalColumns = portalColumns;
		this.portletContainers = new HashMap<>();
		this.inactivePortlets = new ArrayList<>();

		trans = Util.createPackageTranslator(PortalImpl.class, ureq.getLocale());
		portalVC = new VelocityContainer("portalVC", VELOCITY_ROOT + "/portal.html", trans, this);		// initialize arrays
		portalVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		
		// init all portlets enabled in the portal columns
		initPortlets(ureq);
		// push the columns to velocity
		portalVC.contextPut("portalColumns", portalColumns);
		// push list of inactive portlets to velocity
		portalVC.contextPut("inactivePortlets", inactivePortlets);
		// push all portlets to velocity
		portalVC.contextPut("portletContainers", portletContainers);
		portalVC.contextPut("locale", ureq.getLocale());
		// in run mode
		portalVC.contextPut(MODE_EDIT, Boolean.FALSE);
		setInitialComponent(portalVC);
	}

	/**
	 * Factory method to create a portal instance of the current type. Used by the PortalFactory.
	 * The method checks for invalid user configurations and removes them 
	 * @param wControl
	 * @param ureq
	 * @return PortalImpl of same type and configuration
	 */
	public PortalImpl createInstance(WindowControl wContr, UserRequest ureq) {
		// user users personal configuration
		List<List<String>> userColumns = getUserPortalColumns(ureq);
		// clone default configuration for this user if user has no own configuration
		if (userColumns == null) {
			userColumns = (List<List<String>>) ObjectCloner.deepCopy(portalColumns);
		}

		// check if users portal columns contain only defined portals. remove all non existing portals
		// to make it possible to change the portlets in a next release or to remove a portlet
		List<List<String>> cleanedUserColumns = new ArrayList<>();
		Set<String> availablePortlets = PortletFactory.getPortlets().keySet();
		for (List<String> row: userColumns) {
			// add this row as new cleaned row to columns
			List<String> cleanedRow = new ArrayList<>(row.size());
			cleanedUserColumns.add(cleanedRow);
			// check all portlets in old row and copy to cleaned row if it exists
			for(String portletName : row) {
				if (availablePortlets.contains(portletName)) {
					cleanedRow.add(portletName);					
				}
				// discard invalid portlet names
			}
		}
		dispose();
		return new PortalImpl(this.name, ureq, wContr, cleanedUserColumns);
	}
	
	private List<List<String>> getUserPortalColumns(UserRequest ureq) {
		Preferences gp = ureq.getUserSession().getGuiPreferences();
		return (List<List<String>>) gp.get(PortalImpl.class, "userPortalColumns" + name);
	}

	private void saveUserPortalColumnsConfiguration(UserRequest ureq, List userColumns) {
		Preferences gp = ureq.getUserSession().getGuiPreferences();
		gp.putAndSave(PortalImpl.class, "userPortalColumns" + name, userColumns);
	}
	
	/**	
	 * Initialize all portles found in the configuration
	 * @param ureq
	 */
	private void initPortlets(UserRequest ureq) {
		// load all possible portlets, portlets run controller is only loaded when really used
		Iterator<Portlet> portletsIter = PortletFactory.getPortlets().values().iterator();
		while (portletsIter.hasNext()) {
			Portlet portlet = portletsIter.next();
			log.debug("initPortlets portletName=" + portlet.getName());
			if (portlet.isEnabled()) {
				PortletContainer pc = null;
				//fxdiff make the system tolerant to portlet errors
				try {
					pc = PortletFactory.getPortletContainerFor(portlet, getWindowControl(), ureq);
				} catch (Exception e) {
					log.error("Cannot open a portlet: " + portlet, e);
				}			
				pc.addControllerListener(this);
				// remember this portlet container
				portletContainers.put(portlet.getName(), pc);
				String addLinkName = "command.add." + portlet.getName();
				Link tmp = LinkFactory.createCustomLink(addLinkName, addLinkName, "add", Link.BUTTON_SMALL, portalVC, this);
				tmp.setIconLeftCSS("o_icon o_icon_add");
				tmp.setElementCssClass("o_portlet_edit_add");
				tmp.setUserObject(portlet.getName());
				// and add to velocity
				portalVC.put(portlet.getName(), pc.getInitialComponent());
				
				// check if portlet is active for this user
				Iterator<List<String>> colIter = portalColumns.iterator();
				boolean isActive = false;
				while (colIter.hasNext()) {
					List<String> row = colIter.next();
					Iterator<String> rowIter = row.iterator();
					while (rowIter.hasNext()) {
						String activePortletName = rowIter.next();
						if (portlet.getName().equals(activePortletName)) isActive = true;
					}
				}
				if (isActive) {
					// initialize portlet container for active portlets only
					pc.initializeRunComponent(ureq, editModeEnabled);
				} else {
					// add it to inacitve portlets list if not active
					inactivePortlets.add(portlet.getName());
				}
			}
		}
		// update links on visible portlets
		updatePositionChangeLinks();
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link && portalVC.contains(source)) {
			Link tmp = (Link)source;
			String portletName = (String)tmp.getUserObject();
			List<String> firstColumn = portalColumns.get(0);
			PortletContainer pc = portletContainers.get(portletName);
			if (pc == null) throw new AssertException("trying to add portlet with name::" + portletName + " to portal, but portlet container did not exist. Could be a user modifying the URL...");
			// add to users portlet list
			firstColumn.add(portletName);
			// remove from inactive portlets list
			inactivePortlets.remove(portletName);
			// initialize portlet run component
			pc.initializeRunComponent(ureq, editModeEnabled);
			// save user config in db
			saveUserPortalColumnsConfiguration(ureq, portalColumns);
			// update possible links in gui
			updatePositionChangeLinks();
			portalVC.setDirty(true);
		}
		// nothin to catch
	}

	/**
	 * Enable/disable the edit mode of the portal
	 * @param editModeEnabled true: enabled, false: disabled
	 */
	public void setIsEditMode(UserRequest ureq, boolean editModeEnabled) {
		this.editModeEnabled = editModeEnabled;
		updatePorletContainerEditMode(ureq, editModeEnabled);
		portalVC.contextPut(MODE_EDIT, editModeEnabled);
	}
	
	/**
	 * Updates all portles using the given mode
	 * @param editMode true: edit mode activated, false: deactivated
	 */
	private void updatePorletContainerEditMode(UserRequest ureq, boolean editMode) {
		for (String portletName : PortletFactory.getPortlets().keySet()) {
			PortletContainer pc = portletContainers.get(portletName);
			if (pc != null ) {
				pc.setIsEditMode(ureq, editMode);
			}
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof PortletContainer) {
			PortletContainer pc = (PortletContainer) source;
			String cmd = event.getCommand();
			boolean found = false;
			for (int column = 0; column < portalColumns.size(); column++) {
				List<String> rows = portalColumns.get(column);
				for (int row = 0; row < rows.size(); row++) {
					String portletName = rows.get(row);
					if (portletName.equals(pc.getPortlet().getName())){
						if (cmd.equals("move.up")) {
							Collections.swap(rows, row, row - 1);
							found = true;
							break;
						} else if (cmd.equals("move.down")){
							Collections.swap(rows, row, row+1);
							found = true;
							break;
						} else if (cmd.equals("move.right")){
							rows.remove(row);
							List<String> newCol = portalColumns.get(column + 1);
							newCol.add(portletName);
							found = true;
							break;
						} else if (cmd.equals("move.left")){
							rows.remove(row);
							List<String> newCol = portalColumns.get(column - 1);
							newCol.add(portletName);
							found = true;
							break;
						} else if (cmd.equals("close")){
							pc.deactivateRunComponent();
							rows.remove(row);
							this.inactivePortlets.add(portletName);
							found = true;
							break;
						}
					}
				}
				if (found) break;
			}
			// save user config in db
			saveUserPortalColumnsConfiguration(ureq, portalColumns);
			// update possible links in gui
			updatePositionChangeLinks();
			portalVC.setDirty(true);
		}
	}

	/**
	 * Updates the velocity containers of all portlet containers to display the move
	 * links correctly
	 */
	private void updatePositionChangeLinks(){
		Iterator<List<String>> colIter = portalColumns.iterator();
		int colcount = 0;
		while (colIter.hasNext()) {
			List<String> rows = colIter.next();
			Iterator<String> rowIter = rows.iterator();
			int rowcount = 0;
			while (rowIter.hasNext()) {
				String portletName = rowIter.next();
				PortletContainer pc = portletContainers.get(portletName);
				if(pc != null) {
					// up command
					if(rowcount == 0) pc.setCanMoveUp(false);
					else pc.setCanMoveUp(true);
					// down command
					if (rowIter.hasNext()) pc.setCanMoveDown(true);
					else pc.setCanMoveDown(false);
					// left command
					if(colcount == 0) pc.setCanMoveLeft(false); 
					else pc.setCanMoveLeft(true);
					// right command
					if (colIter.hasNext()) pc.setCanMoveRight(true); 
					else pc.setCanMoveRight(false);
					
					rowcount++;
				}
			}
			colcount++;
		}
	}
	
	@Override
	protected void doDispose() {
		// cleanup all portlet containers
		if(portletContainers != null) {
			for (PortletContainer element:portletContainers.values()) {
				element.dispose();
			}
			portletContainers = null;
		}
        super.doDispose();
	}

	@Override
	public void setPortalColumns(List<List<String>> portalColumns) {
		this.portalColumns = portalColumns;
	}

	/**
	 * Bean method used by spring
	 * @param numbColumns
	 */
	public void setName(String name){
		this.name = name;
	}

	/**
	 * @return Name of portal
	 */
	public String getName(){
		return name;
	}

}
