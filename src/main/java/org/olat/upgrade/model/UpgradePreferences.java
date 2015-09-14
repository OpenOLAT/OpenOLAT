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
package org.olat.upgrade.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Initial date: 25.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UpgradePreferences implements Serializable {
	
	private static final long serialVersionUID = 3828851618949061953L;

	// keys: prefs-keys; values: any Prefs-Objects
	private Map<String,Object> prefstore = new HashMap<String,Object>();
	
	public Map<String,Object> getPreferences() {
		return prefstore;
	}
}
