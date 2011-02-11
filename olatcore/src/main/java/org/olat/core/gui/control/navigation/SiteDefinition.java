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

package org.olat.core.gui.control.navigation;

import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for SiteDefinition
 * 
 * <P>
 * Initial Date:  12.07.2005 <br>
 *
 * @author Felix Jost
 */
public interface SiteDefinition extends ConfigOnOff {
	
	public SiteInstance createSite(UserRequest ureq, WindowControl wControl);
	
	public int getOrder();
}

