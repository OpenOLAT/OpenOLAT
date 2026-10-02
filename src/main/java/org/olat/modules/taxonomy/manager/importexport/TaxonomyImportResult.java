/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.manager.importexport;

import org.olat.modules.taxonomy.Taxonomy;

/**
 * Result of an import.
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyImportResult {
	
	private Taxonomy taxonomy;
	private int createdTypes;
	private int updatedTypes;
	private int createdLevels;
	private int updatedLevels;
	
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
	
	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}
	
	public int getCreatedTypes() {
		return createdTypes;
	}
	
	public void incrementCreatedTypes() {
		createdTypes++;
	}
	
	public int getUpdatedTypes() {
		return updatedTypes;
	}
	
	public void incrementUpdatedTypes() {
		updatedTypes++;
	}
	
	public int getCreatedLevels() {
		return createdLevels;
	}
	
	public void incrementCreatedLevels() {
		createdLevels++;
	}
	
	public int getUpdatedLevels() {
		return updatedLevels;
	}
	
	public void incrementUpdatedLevels() {
		updatedLevels++;
	}
}
