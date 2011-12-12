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
package org.olat.core.commons.controllers.impressum;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.GenericMainController;

/**
 * <h3>Description:</h3>
 * 
 * This controller displays the impressum and related controllers in a 3-column
 * layout. Since this uses a {@link GenericMainController}, it is configured
 * automatically, so see the file webapp/WEB-INF/olat_extensions.xml to
 * configure this.
 * 
 * Initial Date: Aug 7, 2009 <br>
 * 
 * @author twuersch, frentix GmbH, http://www.frentix.com
 */
public class ImpressumDmzMainController extends GenericMainController {

	public ImpressumDmzMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		init(ureq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.olat.core.gui.control.generic.layout.GenericMainController#
	 * handleOwnMenuTreeEvent(java.lang.Object, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected Controller handleOwnMenuTreeEvent(Object uobject, UserRequest ureq) {
		// No own menu tree events defined, so don't do anything here.
		return null;
	}
}
