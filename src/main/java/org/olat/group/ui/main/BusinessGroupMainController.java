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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupMainController extends GenericMainController implements Activateable2 {

	public BusinessGroupMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		init(ureq);
		getMenuTree().setRootVisible(false);
		addCssClassToMain("o_groups");
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