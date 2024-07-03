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
package org.olat.modules.topicbroker.model;

import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldType;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCustomFieldDefinitionExport {
	
	private String identifier;
	private TBCustomFieldType type;
	private String name;
	private boolean displayInTable;
	private int sortOrder;
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public TBCustomFieldType getType() {
		return type;
	}
	
	public void setType(TBCustomFieldType type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isDisplayInTable() {
		return displayInTable;
	}
	
	public void setDisplayInTable(boolean displayInTable) {
		this.displayInTable = displayInTable;
	}
	
	public int getSortOrder() {
		return sortOrder;
	}
	
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public static TBCustomFieldDefinitionExport of(TBCustomFieldDefinition definition) {
		TBCustomFieldDefinitionExport export = new TBCustomFieldDefinitionExport();
		export.setIdentifier(definition.getIdentifier());
		export.setType(definition.getType());
		export.setName(definition.getName());
		export.setDisplayInTable(definition.isDisplayInTable());
		export.setSortOrder(definition.getSortOrder());
		return export;
	}

}
