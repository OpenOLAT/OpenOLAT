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
package org.olat.repository.ui.list;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 29.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class RepositoryEntryDataModel extends DefaultFlexiTableDataSourceModel<RepositoryEntryRow> {
	
	private static final Cols[] COLS = Cols.values();
	
	public RepositoryEntryDataModel(DefaultRepositoryEntryDataSource source, FlexiTableColumnModel columnModel) {
		super(source, columnModel);
	}

	@Override
	public void clear() {
		super.clear();
		getSourceDelegate().resetCount();
	}

	@Override
	public DefaultFlexiTableDataSourceModel<RepositoryEntryRow> createCopyWithEmptyList() {
		return new RepositoryEntryDataModel(getSourceDelegate(), getTableColumnModel());
	}

	@Override
	public DefaultRepositoryEntryDataSource getSourceDelegate() {
		return (DefaultRepositoryEntryDataSource)super.getSourceDelegate();
	}

	@Override
	public Object getValueAt(int row, int col) {
		RepositoryEntryRow item = getObject(row);
		if(item == null) {
			return null;//don't break here
		}
		
		switch(COLS[col]) {
			case key: return item.getKey();
			case displayName: return item.getDisplayName();
			case externalId: return item.getExternalId();
			case externalRef: return item.getExternalRef();
			case lifecycleLabel: return item.getLifecycleLabel();
			case lifecycleSoftkey: return item.getLifecycleSoftKey();
			case lifecycleStart: return item.getLifecycleStart();
			case lifecycleEnd: return item.getLifecycleEnd();
			case mark: return item.getMarkLink();
			case select: return item.getSelectLink();
			case start: return item.getStartLink();
			case location: return item.getLocation();
			case educationalType: return item.getEducationalType();
			case completion: return item.getCompletionItem();
			case details: return item.getDetailsLink();
			case ratings: return item.getRatingFormItem();
			case comments: return item.getCommentsLink();
			case type: return item;
		}
		return null;
	}
	
	public enum Cols {
		key("table.header.key"),
		displayName("cif.displayname"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftkey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		location("table.header.location"),
		educationalType("table.header.educational.type"),
		completion("table.header.completion"),
		details("table.header.details"),
		select("table.header.details"),
		start("table.header.start"),
		mark("table.header.mark"),
		ratings("ratings"),
		comments("comments"),
		type("table.header.typeimg");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
