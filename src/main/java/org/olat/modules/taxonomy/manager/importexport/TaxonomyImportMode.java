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

/**
 * What an import does with the level types and levels of the file.
 * 
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public enum TaxonomyImportMode {
	
	/** Update the existing level types and levels, do not create new ones */
	updateExisting(false, true),
	/** Create the new level types and levels, do not change the existing ones */
	addNew(true, false),
	/** Create the new level types and levels, update the existing ones */
	addNewAndUpdateExisting(true, true);
	
	private final boolean adds;
	private final boolean updates;
	
	private TaxonomyImportMode(boolean adds, boolean updates) {
		this.adds = adds;
		this.updates = updates;
	}
	
	public boolean isAddNew() {
		return adds;
	}
	
	public boolean isUpdateExisting() {
		return updates;
	}
}
