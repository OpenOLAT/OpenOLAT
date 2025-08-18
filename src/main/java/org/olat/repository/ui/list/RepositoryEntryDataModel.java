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
		
		return switch(COLS[col]) {
			case key -> item.getKey();
			case displayName -> item.getDisplayName();
			case externalId -> item.getExternalId();
			case externalRef -> item.getExternalRef();
			case lifecycleLabel -> item.getLifecycleLabel();
			case lifecycleSoftkey -> item.getLifecycleSoftKey();
			case lifecycleStart -> item.getLifecycleStart();
			case lifecycleEnd -> item.getLifecycleEnd();
			case mark -> item.getMarkLink();
			case select -> item.getSelectLink();
			case start -> item.getStartLink();
			case location -> item.getLocation();
			case educationalType -> item.getEducationalType();
			case completion -> item.getCompletionItem();
			case successStatus -> item.getPassed();
			case details -> item.getDetailsLink();
			case ratings -> item.getRatingFormItem();
			case type -> item;
			case taxonomyLevels -> item.getTaxonomyLevelsLink();
		};
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
		successStatus("table.header.success.status"),
		details("table.header.learn.more"),
		select("table.header.learn.more"),
		start("table.header.start"),
		mark("table.header.mark"),
		ratings("ratings"),
		type("table.header.typeimg"),
		taxonomyLevels("table.header.num.of.levels");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
