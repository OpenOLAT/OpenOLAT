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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.components.choice.ChoiceModel;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 19.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VisibleFlexiColumnsModel implements ChoiceModel<FlexiColumnModel> {
	
	private Set<Integer> enabledCols = new HashSet<>();
	private final Translator translator;
	private final FlexiTableColumnModel columns;
	
	public VisibleFlexiColumnsModel(FlexiTableColumnModel columns, Set<Integer> enabledCols, Translator translator) {
		this.columns = columns;
		this.translator = translator;
		this.enabledCols = new HashSet<>(enabledCols);
	}

	@Override
	public int getRowCount() {
		return columns == null ? 0 : columns.getColumnCount();
	}

	@Override
	public Boolean isEnabled(int row) {
		FlexiColumnModel cd = getObject(row);
		return cd.isAlwaysVisible() || enabledCols.contains(Integer.valueOf(cd.getColumnIndex()))
				? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public String getLabel(int row) {
		FlexiColumnModel cd = getObject(row);
		return cd.getHeaderLabel() == null ?
				translator.translate(cd.getHeaderKey()) : cd.getHeaderLabel();
	}

	@Override
	public boolean isDisabled(int row) {
		FlexiColumnModel cd = getObject(row);
		return cd.isAlwaysVisible();
	}

	@Override
	public FlexiColumnModel getObject(int row) {
		if(columns == null) return null;
		return columns.getColumnModel(row);
	}
}
