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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.ICourse;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.AccessControlModule;

/**
 * 
 * Description:<br>
 * ActionExtension for the administration of Orders by Resource
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrdersActionExtension extends GenericActionExtension {
	
	private final AccessControlModule acModule;
	
	public OrdersActionExtension(AccessControlModule acModule) {
		this.acModule = acModule;
	}
	

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		if(arg instanceof ICourse) {
			ICourse course = (ICourse)arg;
			OLATResource resource = OLATResourceManager.getInstance().findResourceable(course);
			return new OrdersAdminController(ureq, wControl, null, resource);
		}
		return super.createController(ureq, wControl, arg);
	}

	@Override
	public boolean isEnabled() {
		return acModule.isEnabled();
	}
}
