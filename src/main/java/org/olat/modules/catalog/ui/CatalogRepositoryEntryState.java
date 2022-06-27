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
package org.olat.modules.catalog.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 10 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryState implements StateEntry {
	
	private static final long serialVersionUID = 6075616697161891923L;
	
	private String specialFilterRepositoryEntryLabel;
	private Collection<Long> specialFilterRepositoryEntryKeys;
	
	public String getSpecialFilterRepositoryEntryLabel() {
		return specialFilterRepositoryEntryLabel;
	}

	public void setSpecialFilterRepositoryEntryLabel(String specialFilterRepositoryEntryLabel) {
		this.specialFilterRepositoryEntryLabel = specialFilterRepositoryEntryLabel;
	}

	public Collection<Long> getSpecialFilterRepositoryEntryKeys() {
		return specialFilterRepositoryEntryKeys;
	}

	public void setSpecialFilterRepositoryEntryKeys(Collection<Long> specialFilterRepositoryEntryKeys) {
		this.specialFilterRepositoryEntryKeys = specialFilterRepositoryEntryKeys;
	}

	@Override
	public StateEntry clone() {
		CatalogRepositoryEntryState clone = new CatalogRepositoryEntryState();
		
		clone.specialFilterRepositoryEntryLabel = this.specialFilterRepositoryEntryLabel;
		if (specialFilterRepositoryEntryKeys != null) {
			clone.specialFilterRepositoryEntryKeys = new ArrayList<>(this.specialFilterRepositoryEntryKeys);
		}
		
		return clone;
	}

}
