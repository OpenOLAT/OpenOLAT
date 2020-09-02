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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;


/**
 * Initial Date:  27.08.2020 <br>
 * @author aboeckle, mjenny, alexander.boeckle@frentix.com, http://www.frentix.com
 */


public class ConfigurationPropertiesContentRow implements FlexiTreeTableNode {

	private boolean hasChildren;
	private ConfigurationPropertiesContentRow parent;
	
	private OLATProperty configurationProperty;
	private String configurationFileName;
	
	public ConfigurationPropertiesContentRow(OLATProperty configurationProperty, ConfigurationPropertiesContentRow parent) {
		this.configurationProperty = configurationProperty;
		this.parent = parent;
	}
	
	public ConfigurationPropertiesContentRow(String configurationFileName) {
		this.configurationFileName = configurationFileName;
	}
	
	public String getKey() {
		if (configurationProperty != null) {
			return configurationProperty.getKey();
		} else {
			return configurationFileName;
		}
	}
	
	public String getValue() {
		if (configurationProperty != null) {
			return configurationProperty.getValue();
		} else {
			return null;
		}
	}

	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}
	
	public boolean hasChidlren() {
		return this.hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	@Override
	public String getCrump() {
		return getKey();
	}
}
