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
package org.olat.modules.cemedia.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.cemedia.ui.MediaRelationsTableModel.MediaRelationsCols;

/**
 * 
 * Initial date: 10 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaRelationsDataModelSorterDelegate extends SortableFlexiTableModelDelegate<MediaShareRow> {

	private static final MediaRelationsCols[] COLS = MediaRelationsCols.values();
	
	public MediaRelationsDataModelSorterDelegate(SortKey orderBy, MediaRelationsTableModel model, Locale locale) {
		super(orderBy, model, locale);
	}
	
	@Override
	protected void sort(List<MediaShareRow> rows) {
		Comparator<MediaShareRow> comparator;
		switch(COLS[getColumnIndex()]) {
			case name: comparator = new NameComparator(); break;
			case editable: comparator = new EditableComparator(); break;
			default: comparator = new DefaultComparator(); break;
		}
		Collections.sort(rows, comparator);
	}
	
	private class NameComparator implements Comparator<MediaShareRow> {
		@Override
		public int compare(MediaShareRow t1, MediaShareRow t2) {
			String r1 = t1.getDisplayName();
			String r2 = t2.getDisplayName();
			return compareString(r1, r2);
		}
	}

	private class EditableComparator implements Comparator<MediaShareRow> {
		@Override
		public int compare(MediaShareRow t1, MediaShareRow t2) {
			boolean e1 = t1.isEditable();
			boolean e2 = t2.isEditable();
			
			int c = Boolean.compare(e1, e2);
			if(c == 0) {
				String r1 = t1.getDisplayName();
				String r2 = t2.getDisplayName();
				c = compareString(r1, r2);
			}
			return c;
		}
	}
}
