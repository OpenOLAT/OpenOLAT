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
package org.olat.modules.coach.ui.em;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.media.MediaResource;

/**
 * Initial date: 2025-01-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CertificatesTableModel extends DefaultFlexiTableDataModel<CertificateRow> 
		implements SortableFlexiTableDataModel<CertificateRow>, ExportableFlexiTableDataModel {

	private final ExportableFlexiTableDataModel exportDelegate;

	public CertificatesTableModel(FlexiTableColumnModel columnModel, ExportableFlexiTableDataModel exportDelegate) {
		super(columnModel);
		this.exportDelegate = exportDelegate;
	}

	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		return exportDelegate.export(ftC);
	}

	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<CertificateRow> sorter = new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<CertificateRow> views = sorter.sort();
		super.setObjects(views);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificateRow object = getObject(row);
		return getValueAt(object, col);
	}

	@Override
	public Object getValueAt(CertificateRow row, int col) {
		if (col >= 0 && col < CertificateCols.values().length) {
			return switch (CertificateCols.values()[col]) {
				case id -> row.getCertificateId();
				case course -> row.getCourseDisplayName();
				case path -> row.getPath();
			};
		}
		
		int propsPos = col - CertificatesController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propsPos);
	}
	
	public enum CertificateCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		course("table.header.course"),
		path("table.header.path");
		
		private final String i18nKey;
		
		CertificateCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}


		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
