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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.id.context;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  23.06.2006 <br>
 *
 * @author Felix Jost
 */
public interface ContextEntryControllerCreator {
	
	/**
	 * Return a request scoped instance of the creator if the
	 * creator cannot be accessed by multiple users simultaneously.
	 * @return
	 */
	public ContextEntryControllerCreator clone();
	
	/**
	 * @return true if the context can be resumed
	 */
	public boolean isResumable();

	/**
	 * Factory method to create the run controller for this contex.
	 * <p>
	 * Just create the correct controller given the contextentry. Everthing else
	 * already done. (no need to advance pos in stack or such)
	 * <p>
	 * If context opens a site instead of creating a new dtab, the method can
	 * return NULL
	 * <p>
	 * The controller delivered by the factory method must take care of its
	 * entire business path.
	 * 
	 * @param ce
	 * @param ureq
	 * @param wControl
	 * @return the controller or NULL if the context is an existing site
	 */
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl);

	/**
	 * The class name of the site that must be activated or NULL if opened as dTab
	 * 
	 * @param ce
	 * @return Return the class name that is used to activate an existing site or
	 *         NULL if the target is a new dtab
	 */
	public String getSiteClassName(List<ContextEntry> entries, UserRequest ureq);
	
	/**
	 * 
	 * @param ce
	 * @param ureq
	 * @param wControl
	 * @return true, if this contextentry can be launched
	 */
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl);
	
	/**
	 * Allow to rewrite the business path
	 * @param ureq
	 * @param entries
	 * @return
	 */
	public TabContext getTabContext(UserRequest ureq, OLATResourceable ores, ContextEntry mainEntry,  List<ContextEntry> entries);
}
