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
package org.olat.user;

import org.olat.core.gui.control.Event;
import org.olat.properties.Property;

/**
 * Initial Date:  Feb 19, 2004
 *
 * @author jeger
 * 
 * Comment:  
 * The UserFoundEvent has an additional field that tells which subject has been found
 */
public class PropFoundEvent extends Event {

	private static final long serialVersionUID = -5998337372128356860L;
	private Property prop;
	
	/**
	 * Event of type 'UserFoundEvent' with extra parameter, the subject itself
	 * @param prop found property
	 */
	public PropFoundEvent(Property prop) {
		super("PropFound");
		this.prop = prop;
	}
		
	/**
	 * @return Returns the subject.
	 */
	public Property getProperty() {
		return prop;
	}
}
