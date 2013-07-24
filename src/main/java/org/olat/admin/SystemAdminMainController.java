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

package org.olat.admin;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * <h3>Description:</h3> Creates a MainController, which is configurable by
 * olat_extensions
 * 
 * Initial Date: Apr 27, 2004
 * 
 * @author Felix Jost
 * @author strentini, sergio.trentini@frentix.com, www.frentix.com
 * 
 */
public class SystemAdminMainController extends GenericMainController implements Activateable2 {

	public SystemAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		init(ureq);
		getMenuTree().setExpandSelectedNode(false);
		addCssClassToMain("o_sysadmin");
	}

	/**
	 * @see org.olat.core.gui.control.generic.layout.GenericMainController#handleOwnMenuTreeEvent(java.lang.Object,
	 *      org.olat.core.gui.UserRequest)
	 */
	@Override
	protected Controller handleOwnMenuTreeEvent(Object uobject, UserRequest ureq) {
		return null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
	}
}