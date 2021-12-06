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
package org.olat.repository.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LifecycleDataModel extends DefaultFlexiTableDataModel<RepositoryEntryLifecycle> {
	
	private static final LCCols[] COLS = LCCols.values();
	
	public LifecycleDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RepositoryEntryLifecycle cycle = getObject(row);
		switch(COLS[col]) {
			case label: return cycle.getLabel();
			case softkey: return cycle.getSoftKey();
			case validFrom: return cycle.getValidFrom();
			case validTo: return cycle.getValidTo();
			case delete: return Boolean.FALSE;
			default: return "ERROR";
		}
	}

	public enum LCCols {
		softkey("lifecycle.softkey"),
		label("lifecycle.label"),
		validFrom("lifecycle.validFrom"),
		validTo("lifecycle.validTo"),
		edit("edit"),
		delete("delete");
		
		private final String i18nKey;
	
		private LCCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
		
	}
	

}
