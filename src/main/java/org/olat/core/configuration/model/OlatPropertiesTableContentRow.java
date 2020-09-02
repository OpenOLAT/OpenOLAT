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

package org.olat.core.configuration.model;

import org.olat.core.configuration.OLATProperty;


/**
 * Initial Date:  27.08.2020 <br>
 * @author aboeckle, mjenny, alexander.boeckle@frentix.com, http://www.frentix.com
 */

public class OlatPropertiesTableContentRow {
	
	private OLATProperty defaultProperty;
	private OLATProperty overwriteProperty;
	private OLATProperty systemProperty;
	private boolean hasRedundantEntry;
	
	public OlatPropertiesTableContentRow(OLATProperty olatProperty) {
		this.defaultProperty = olatProperty;		
	}
	
	public OLATProperty getDefaultProperty() {
		return defaultProperty;
	}
	
	public OLATProperty getOverwriteProperty() {
		return overwriteProperty;
	}
	
	public void setOverwriteProperty(OLATProperty overwriteProperty) {
		this.overwriteProperty = overwriteProperty;
	}

	public OLATProperty getSystemProperty() {
		return systemProperty;
	}

	public void setSystemProperty(OLATProperty systemProperty) {
		this.systemProperty = systemProperty;
	}

	public boolean hasRedundantEntry() {
		return hasRedundantEntry;
	}

	public void setRedundantEntry(boolean hasRedundantEntry) {
		this.hasRedundantEntry = hasRedundantEntry;
	}

}
