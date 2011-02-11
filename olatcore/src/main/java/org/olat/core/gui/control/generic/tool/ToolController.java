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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.control.generic.tool;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;

/**
 * Description: <br>
 * A graphical thing displayed on the right side of the screen, containing
 * mainly links which are offered in the current process. The order the Header,
 * Links, or component are added determines their vertical position. Direction
 * is top-down.
 * 
 * @author Felix Jost
 */
public interface ToolController extends Controller {

	/**
	 * a header text like 'Course tools' 'Actions' 'Edit' or whatever
	 * 
	 * @param text
	 */
	public void addHeader(String text);

	/**
	 * a header text like 'Course tools' 'Actions' 'Edit' or whatever
	 * 
	 * @param text
	 * @param ident An ident to reference this entry
	 */
	public void addHeader(String text, String ident);

	/**
	 * a header text like 'Course tools' 'Actions' 'Edit' or whatever
	 * 
	 * @param text
	 * @param ident An ident to reference this entry
	 * @param cssClass a css class that provides an toolbox title bar image.
	 *          optional, can be null (default image)
	 */
	public void addHeader(String text, String ident, String cssClass);

	/**
	 * a link (= a velocity $r.link(..)). in order to receive events, the code
	 * creating the tool must use
	 * tool.addControllerListener(ControllerEventListener el) to receive the link
	 * events (us usual: subscribe to controller events)
	 * 
	 * @param action the command which the event will have when the event is fired
	 *          because the user clicked this link.
	 * @param text
	 */
	public void addLink(String action, String text);

	/**
	 * a link (= a velocity $r.link(..)). in order to receive events, the code
	 * creating the tool must use
	 * tool.addControllerListener(ControllerEventListener el) to receive the link
	 * events (us usual: subscribe to controller events)
	 * 
	 * @param action the command which the event will have when the event is fired
	 *          because the user clicked this link.
	 * @param text
	 * @param ident An ident to reference this entry
	 * @param cssClass The class for the enclosing div tag or null if default
	 *          class used
	 */
	public void addLink(String action, String text, String ident, String cssClass);


	/**
	 * a link (= a velocity $r.link(..)). in order to receive events, the code
	 * creating the tool must use
	 * tool.addControllerListener(ControllerEventListener el) to receive the link
	 * events (us usual: subscribe to controller events)
	 * 
	 * @param action the command which the event will have when the event is fired
	 *          because the user clicked this link.
	 * @param text
	 * @param ident An ident to reference this entry
	 * @param cssClass The class for the enclosing div tag or null if default
	 *          class used
	 * @param isDownloadLink if the link results in a download !! works not together with cssClass != null
	 */
	public void addLink(String action, String text, String ident, String cssClass, boolean isDownloadLink);
	
	/**
	 * a link (= a velocity $r.link(..)). in order to receive events, the code
	 * creating the tool must use
	 * tool.addControllerListener(ControllerEventListener el) to receive the link
	 * events (us usual: subscribe to controller events)
	 * 
	 * @param action the command which the event will have when the event is fired
	 *          because the user clicked this link.
	 * @param text
	 * @param ident An ident to reference this entry
	 * @param cssClass The class for the enclosing div tag or null if default
	 *          class used
	 * @param width popup window width
	 * @param height popup window height
	 * @param browserMenubarEnabled true: browser menu bar visible
	 */
	public void addPopUpLink(String action, String text, String ident, String cssClass, String width, String height,
			boolean browserMenubarEnabled);

	/**
	 * the most generic solution, the Tool renderer will just display the
	 * component, nothing more
	 * 
	 * @param component
	 */
	public void addComponent(Component component);

	/**
	 * the most generic solution, the Tool renderer will just display the
	 * component, nothing more
	 * 
	 * @param component
	 * @param ident An ident to reference this entry
	 */
	public void addComponent(Component component, String ident);

	/**
	 * Remove tool entry with given ident
	 * 
	 * @param ident
	 */
	public void remove(String ident);

	/**
	 * Enable/Disable a tool entry.
	 * 
	 * @param ident
	 * @param enabled
	 */
	public void setEnabled(String ident, boolean enabled);
	
	/**
	 * 
	 * @param dragEnabled if true, all active toolentries can be dragged
	 */
	public void setDragEnabled(boolean dragEnabled);

	/**
	 * @return true when toolbox has no entries, false if toolbox has at least one entry
	 */
	public boolean isEmpty();


}