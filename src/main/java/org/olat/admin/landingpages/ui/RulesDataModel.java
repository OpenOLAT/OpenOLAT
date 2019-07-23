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
package org.olat.admin.landingpages.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RulesDataModel implements FlexiTableDataModel<RuleWrapper>, TableDataModel<RuleWrapper> {
	
	private FlexiTableColumnModel columnModel;
	private List<RuleWrapper> rules;
	
	public RulesDataModel(FlexiTableColumnModel columnModel, List<RuleWrapper> rules) {
		this.columnModel = columnModel;
		this.rules = rules;
	}
	
	@Override
	public FlexiTableColumnModel getTableColumnModel() {
		return columnModel;
	}

	@Override
	public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
		this.columnModel = tableColumnModel;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}
	
	public List<RuleWrapper> getObjects() {
		return rules;
	}

	@Override
	public boolean isSelectable(int row) {
		return true;
	}

	@Override
	public void setObjects(List<RuleWrapper> objects) {
		rules = new ArrayList<>(objects);
	}

	@Override
	public RulesDataModel createCopyWithEmptyList() {
		return new RulesDataModel(columnModel, new ArrayList<RuleWrapper>());
	}

	@Override
	public int getRowCount() {
		return rules == null ? 0 : rules.size();
	}

	@Override
	public boolean isRowLoaded(int row) {
		return true;
	}

	@Override
	public RuleWrapper getObject(int row) {
		return rules.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RuleWrapper rule = getObject(row);
		switch(RCols.values()[col]) {
			case position: return rule.getPosition();
			case role: return rule.getRoleEl();
			case userAttributeKey: return rule.getAttrNameEl();
			case userAttributeValue: return rule.getAttrValueEl();
			case landingPage: return rule.getLandingPageEl();
			case landingPageChooser: return rule.getLandingPageChooser();
			default: return "ERROR";
		}
	}

	public enum RCols {
		position("rules.position"),
		role("rules.role"),
		userAttributeKey("rules.user.attribute.key"),
		userAttributeValue("rules.user.attribute.value"),
		landingPage("rules.landing.page"),
		landingPageChooser("rules.landing.page.chooser");
		
		private final String i18nKey;
	
		private RCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}