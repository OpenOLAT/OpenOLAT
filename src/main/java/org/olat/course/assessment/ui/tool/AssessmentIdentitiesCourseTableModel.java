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
package org.olat.course.assessment.ui.tool;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.certificate.CertificateLight;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentIdentitiesCourseTableModel extends DefaultFlexiTableDataModel<AssessedIdentityCourseRow>
	implements SortableFlexiTableDataModel<AssessedIdentityCourseRow> {
	
	private ConcurrentMap<Long, CertificateLight> certificateMap;
	
	public AssessmentIdentitiesCourseTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	public void setCertificateMap(ConcurrentMap<Long, CertificateLight> certificateMap) {
		this.certificateMap = certificateMap;
	}

	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<AssessedIdentityCourseRow> sorter
				= new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<AssessedIdentityCourseRow> views = sorter.sort();
		super.setObjects(views);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessedIdentityCourseRow entry = getObject(row);
		return getValueAt(entry, col);
	}

	@Override
	public Object getValueAt(AssessedIdentityCourseRow row, int col) {
		if(col >= 0 && col < IdentityCourseCols.values().length) {
	
			switch(IdentityCourseCols.values()[col]) {
				case username: return row.getIdentityName();
				case certificate: return certificateMap.get(row.getIdentityKey());
				case score: return row.getScore();
				case passed: return row.getPassed();
				case lastScoreUpdate: return row.getLastModified();
			}
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<AssessedIdentityCourseRow> createCopyWithEmptyList() {
		return new AssessmentIdentitiesCourseTableModel(getTableColumnModel());
	}
	
	public enum IdentityCourseCols implements FlexiSortableColumnDef {
		username("table.header.name"),
		passed("table.header.passed"),
		certificate("table.header.certificate"),
		score("table.header.score"),
		lastScoreUpdate("table.header.lastScoreDate");

		private final String i18nKey;
		
		private IdentityCourseCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
