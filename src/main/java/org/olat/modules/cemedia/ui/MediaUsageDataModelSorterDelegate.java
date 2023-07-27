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
import org.olat.modules.cemedia.ui.MediaUsageTableModel.MediaUsageCols;

/**
 * 
 * Initial date: 27 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUsageDataModelSorterDelegate extends SortableFlexiTableModelDelegate<MediaUsageRow> {

	private static final MediaUsageCols[] COLS = MediaUsageCols.values();
	
	public MediaUsageDataModelSorterDelegate(SortKey orderBy, MediaUsageTableModel model, Locale locale) {
		super(orderBy, model, locale);
	}
	
	@Override
	protected void sort(List<MediaUsageRow> rows) {
		Comparator<MediaUsageRow> comparator;
		switch(COLS[getColumnIndex()]) {
			case use: comparator = new UsageComparator(); break;
			case resource: comparator = new ResourceComparator(); break;
			default: comparator = new DefaultComparator(); break;
		}
		Collections.sort(rows, comparator);
	}
	
	private class UsageComparator implements Comparator<MediaUsageRow> {
		@Override
		public int compare(MediaUsageRow t1, MediaUsageRow t2) {
			String r1 = t1.getPage();
			String r2 = t2.getPage();
			return compareString(r1, r2);
		}
	}
	
	private class ResourceComparator implements Comparator<MediaUsageRow> {
		@Override
		public int compare(MediaUsageRow t1, MediaUsageRow t2) {
			String r1 = t1.getResourceName();
			String r2 = t2.getResourceName();
			return compareString(r1, r2);
		}
	}
}
