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
package org.olat.core.gui.components.link;

import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.impl.Form;

/**
 * Description:<br>
 * TODO: patrickb Class Description for FormLinkFactory
 * 
 * <P>
 * Initial Date:  12.12.2006 <br>
 * @author patrickb
 */
public class FormLinkFactory {

	/**
	 * link factory method especially for the new flexibel form!
	 * @param name
	 * @param cmd
	 * @param key
	 * @return
	 */
	public static Link createFormLink(String name, Form form) {
		return createFormLink(null, name, form);
	}
	
	/**
	 * Create a link with a fix ID
	 * @param id The id of the component, must be unique or null
	 * @param name
	 * @param form
	 * @return
	 */
	public static Link createFormLink(String id, String name, Form form) {
		Link foLnk = new Link(id, name, name, name,  Link.LINK + Link.FLEXIBLEFORMLNK, form);
		foLnk.setElementId(FormBaseComponentIdProvider.DISPPREFIX+foLnk.getDispatchID());
		return foLnk;
	}
	
	/**
	 * link factory method especially for the new flexibel form!
	 * @param name
	 * @param cmd
	 * @param key
	 * @param presentation
	 * @return
	 */
	public static Link createCustomFormLink(String name, String cmd, String key, int presentation, Form form) {
		return createCustomFormLink(null, name, cmd, key, presentation, form);
	}
	
	/**
	 * 
	 * @param id The unique identifier of the link, must be unique or null
	 * @param name
	 * @param cmd
	 * @param key
	 * @param presentation
	 * @param form
	 * @return
	 */
	public static Link createCustomFormLink(String id, String name, String cmd, String key, int presentation, Form form) {
		Link foLnk = new Link(id, name, cmd, key, presentation + Link.FLEXIBLEFORMLNK, form);
		foLnk.setElementId(FormBaseComponentIdProvider.DISPPREFIX+foLnk.getDispatchID());
		return foLnk;
	}
}