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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Description:<br>
 * The portlet container wrapps a portlet and adds the features to move
 * portlets around in the portal
 * <P>
 * Initial Date:  08.07.2005 <br>
 * @author gnaegi
 */
public class PortletContainer extends BasicController implements PortletContainerPositioning {
		
	private static String MODE_EDIT = "editMode";

	private VelocityContainer portletContainerVC;
	private VelocityContainer toolboxContainer;
	private Portlet portlet;	
	private Component runComponent;
	
	private Link moveLeftLink;
	private Link moveUpLink;
	private Link moveDownLink;
	private Link moveRightLink;
	private Link close;

	/**
	 * Constructor for a portlet container
	 * @param wControl
	 * @param ureq
	 * @param portlet
	 */
	public PortletContainer(WindowControl wControl, UserRequest ureq, Portlet portlet) {
		super(ureq, wControl);
		this.portlet = portlet;
	
		portletContainerVC = this.createVelocityContainer("portletContainer");
		portletContainerVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID

		this.portletContainerVC.contextPut("title", portlet.getTitle());
		this.portletContainerVC.contextPut("cssClass", portlet.getCssClass());
		this.portletContainerVC.contextPut(MODE_EDIT, Boolean.FALSE);
		putInitialPanel(portletContainerVC);
				
		toolboxContainer = createVelocityContainer("portletToolbox");
				
		moveLeftLink = LinkFactory.createCustomLink("move.left", "move.left", null, Link.NONTRANSLATED, toolboxContainer, this);
		moveLeftLink.setTextReasonForDisabling(translate("move.left.impossible"));		
		moveLeftLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_move_left");
		moveLeftLink.setElementCssClass("o_portlet_edit_left");
		
		moveUpLink = LinkFactory.createCustomLink("move.up", "move.up", null, Link.NONTRANSLATED, toolboxContainer, this);
		moveUpLink.setTextReasonForDisabling(translate("move.up.impossible"));
		moveUpLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_move_up");
		moveUpLink.setElementCssClass("o_portlet_edit_up");
		
		moveDownLink = LinkFactory.createCustomLink("move.down", "move.down", null, Link.NONTRANSLATED, toolboxContainer, this);
		moveDownLink.setTextReasonForDisabling(translate("move.down.impossible"));
		moveDownLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_move_down");
		moveDownLink.setElementCssClass("o_portlet_edit_down");
				
		moveRightLink = LinkFactory.createCustomLink("move.right", "move.right", null, Link.NONTRANSLATED, toolboxContainer, this);
		moveRightLink.setTextReasonForDisabling(translate("move.right.impossible"));
		moveRightLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_move_right");
		moveRightLink.setElementCssClass("o_portlet_edit_right");
		
		close = LinkFactory.createCustomLink("close", "close", null, Link.NONTRANSLATED, toolboxContainer, this);
		close.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_delete_item");
		close.setElementCssClass("o_portlet_edit_delete");
		
		portletContainerVC.put("toolbox", toolboxContainer);
	}

	/**
	 * Initializes the portlet runtime view
	 * @param ureq
	 */
	protected void initializeRunComponent(UserRequest ureq, boolean editModeEnabled) {
		runComponent = portlet.getInitialRunComponent(getWindowControl(), ureq);
		portletContainerVC.put("portlet", runComponent);
		addAdditonalTools(ureq, editModeEnabled);
	}
	
	/**
	 * Dispose the portlets run component without disposing the container
	 * and the portlet itself
	 * @param asynchronous
	 */
	protected void deactivateRunComponent() {
		portlet.disposeRunComponent();
		portletContainerVC.remove(runComponent);
		runComponent = null;
	}
	
	/**
	 * expose values to velocity
	 * @param name Name of value
	 * @param value Boolean value
	 */
	protected void contextPut(String name, Boolean value) {
		portletContainerVC.contextPut(name, value);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// forward event to portal
		fireEvent(ureq, event);
	}

	/**
	 * @return AbstractPortlet The portlet from this container
	 */
	public Portlet getPortlet() {
		return this.portlet;
	}

	@Override
	protected void doDispose() {
		if (portlet != null) {
			portlet.dispose();
			portlet = null;
		}
        super.doDispose();
	}
	
	/**
	 * @param editModeEnabled true: portal is in edit mode, false in run mode
	 */
	protected void setIsEditMode(UserRequest ureq, boolean editModeEnabled) {
		portletContainerVC.contextPut(MODE_EDIT, editModeEnabled);
		//only create sorting and moving stuff if switching to edit mode otherwise lots or memory is wasted!
		addAdditonalTools(ureq, editModeEnabled);
	}
	
	private void addAdditonalTools(UserRequest ureq, boolean editModeEnabled) {
		if(!editModeEnabled) return;
		
		Controller additionalPortletTools = portlet.getTools(ureq, getWindowControl());
		if(additionalPortletTools!=null) {
			toolboxContainer.contextPut("hasAdditional", Boolean.TRUE);
			toolboxContainer.put("additionalTools", additionalPortletTools.getInitialComponent());
		}
	}

	@Override
	public void setCanMoveDown(boolean canMoveDown) {
		toolboxContainer.contextPut("canDown", new Boolean(canMoveDown));
		moveDownLink.setEnabled(canMoveDown);
	}

	@Override
	public void setCanMoveLeft(boolean canMoveLeft) {
		toolboxContainer.contextPut("canLeft", new Boolean(canMoveLeft));
		moveLeftLink.setEnabled(canMoveLeft);
	}

	@Override
	public void setCanMoveRight(boolean canMoveRight) {
		toolboxContainer.contextPut("canRight", new Boolean(canMoveRight));
		moveRightLink.setEnabled(canMoveRight);
	}

	@Override
	public void setCanMoveUp(boolean canMoveUp) {		
		toolboxContainer.contextPut("canUp", new Boolean(canMoveUp));
		moveUpLink.setEnabled(canMoveUp);
	}

}
