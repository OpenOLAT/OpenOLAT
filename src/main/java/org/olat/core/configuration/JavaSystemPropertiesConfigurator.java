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
* <p>
*/
package org.olat.core.configuration;

import java.util.Map;
import java.util.Properties;

/**
 * Description:<br>
 * Set derby system properties
 * 
 * <P>
 * Initial Date:  30.04.2009 <br>
 * @author guido
 */
public class JavaSystemPropertiesConfigurator {
	
	Map<String, String> map;
	
	/**
	 * Set java system properties provided by spring at runtime. See spring config
	 * for properties set
	 * [used by spring]
	 */
	private JavaSystemPropertiesConfigurator(Map<String, String> map) {
		Properties p = System.getProperties();
		p.putAll(map);
	}	

}
