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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.portfolio;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.home.HomeSite;

/**
 * Description:<br>
 * load my maps menu-entry. config here instead of xml allows en-/disabling at
 * runtime
 * 
 * <P>
 * Initial Date: 03.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
@SuppressWarnings("unused")
public class EPOtherMapsExtension {

	public EPOtherMapsExtension() {

		NewControllerFactory.getInstance().addContextEntryControllerCreator("Map",
				new ContextEntryControllerCreator() {

					@Override
					public Controller createController(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
						return null;
					}

					@Override
				public String getTabName(ContextEntry ce) {
					// opens in home-tab
						return null;
					}

					@Override
					public String getSiteClassName(ContextEntry ce) {
						return HomeSite.class.getName();
					}

					@Override
					public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
						return true;
					}

			});
	}
}