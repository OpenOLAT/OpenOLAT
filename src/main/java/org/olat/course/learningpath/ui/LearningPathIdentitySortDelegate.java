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
package org.olat.course.learningpath.ui;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;

/**
 * 
 * Initial date: 26 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathIdentitySortDelegate extends SortableFlexiTableModelDelegate<LearningPathIdentityRow> {

	public LearningPathIdentitySortDelegate(SortKey orderBy,
			SortableFlexiTableDataModel<LearningPathIdentityRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<LearningPathIdentityRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex < LearningPathIdentityDataModel.USER_PROPS_OFFSET) {
			LearningPathIdentityDataModel.LearningPathIdentityCols column = LearningPathIdentityDataModel.LearningPathIdentityCols.values()[columnIndex];
			switch(column) {
				case completion: Collections.sort(rows, LearningProgressCompletionCellRenderer.createComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}

}
