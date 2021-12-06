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
package org.olat.modules.quality.analysis.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.modules.quality.analysis.HeatMapStatistic;

/**
 * 
 * Initial date: 8 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class FooterGroupByDataModel extends GroupByDataModel implements FlexiTableFooterModel {
	
	private final String footerHeader;
	private List<?> footerDataValues;
	private Object footerTotal;

	FooterGroupByDataModel(FlexiTableColumnModel columnsModel, Locale locale, String footerHeader) {
		super(columnsModel, locale);
		this.footerHeader = footerHeader;
	}
	
	public void setObjects(List<GroupByRow> objects, List<?> footerDataValues, HeatMapStatistic footerTotal) {
		super.setObjects(objects);
		this.footerDataValues = footerDataValues;
		this.footerTotal = footerTotal;
	}

	@Override
	public String getFooterHeader() {
		return footerHeader;
	}

	@Override
	public Object getFooterValueAt(int col) {
		if (footerDataValues != null && col >= GroupByController.DATA_OFFSET) {
			int pos = col - GroupByController.DATA_OFFSET;
			return footerDataValues.get(pos);
		}
		if (col == GroupByController.TOTAL_OFFSET) {
			return footerTotal;
		}
		return null;
	}

}
