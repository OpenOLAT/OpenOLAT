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
public class CatalogEntryState implements StateEntry {
	
	private static final long serialVersionUID = 6075616697161891923L;
	
	private String specialFilterLabel;
	private Collection<Long> specialFilterResourceKeys;
	
	public String getSpecialFilterLabel() {
		return specialFilterLabel;
	}

	public void setSpecialFilterLabel(String specialFilterLabel) {
		this.specialFilterLabel = specialFilterLabel;
	}

	public Collection<Long> getSpecialFilterResourceKeys() {
		return specialFilterResourceKeys;
	}

	public void setSpecialFilterResourceKeys(Collection<Long> specialFilterResourceKeys) {
		this.specialFilterResourceKeys = specialFilterResourceKeys;
	}

	@Override
	public StateEntry clone() {
		CatalogEntryState clone = new CatalogEntryState();
		
		clone.specialFilterLabel = this.specialFilterLabel;
		if (this.specialFilterResourceKeys != null) {
			clone.specialFilterResourceKeys = new ArrayList<>(this.specialFilterResourceKeys);
		}
		
		return clone;
	}

}
