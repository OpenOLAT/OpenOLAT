/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.stack;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.Controller;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface BreadcrumbPanel extends StackedPanel {
	
	public int size();
	
	/**
	 * Dismiss all controller and replace the root
	 * @param displayName
	 * @param controller
	 */
	public void rootController(String displayName, Controller controller);
	
	/**
	 * @return The first controller of the stack.
	 */
	public Controller getRootController();
	
	/**
	 * Dissmiss all controllers but the root
	 */
	public void popUpToRootController(UserRequest ureq);
	
	/**
	 * Push a controller in the stack with its name.
	 * 
	 * @param displayName The name shown as bread crumb.
	 * @param controller The controller to push
	 */
	public void pushController(String displayName, Controller controller);
	
	/**
	 * Push a controller in the stack with name and icon.
	 * 
	 * @param displayName The name shown as bread crumb
	 * @param iconLeftCss The icon shown as decoration of the bread crumb
	 * @param controller The controller to push
	 */
	public void pushController(String displayName, String iconLeftCss, Controller controller);
	
	/**
	 * Push a controller in the stack with name and icon.
	 * 
	 * @param displayName The name shown as bread crumb
	 * @param iconLeftCss The icon shown as decoration of the bread crumb
	 * @param controller The controller to push
	 * @param dispose If true, automatically dispose the controller, if false, you need to dispose the controller yourself.
	 */
	public void pushController(String displayName, String iconLeftCss, Object uobject);
	
	/**
	 * Change the display name of the last crumb.
	 * 
	 * @param displayName The new display name
	 */
	public void changeDisplayname(String displayName);
	
	/**
	 * Change the name and the icon of a bread crumb.
	 * 
	 * @param displayName The name shown as bread crumb
	 * @param iconLeftCss The icon shown as decoration of the bread crumb, if null, the icon is removed
	 * @param ctrl The controller
	 */
	public void changeDisplayname(String displayName, String iconLeftCss, Controller ctrl);
	
	/**
	 * 
	 * @param controller
	 * @return true if the controller has been found
	 */
	public boolean popUpToController(Controller controller);
	
	/**
	 * Remove and dispose the specified controller and all
	 * controllers under this one.
	 * 
	 * @param controller
	 */
	public void popController(Controller controller);
	
	public void popUserObject(Object uobject);
	
	

}