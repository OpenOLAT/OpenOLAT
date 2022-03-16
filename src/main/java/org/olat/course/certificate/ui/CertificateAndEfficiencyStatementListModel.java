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
package org.olat.course.certificate.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 22.10.2014<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateAndEfficiencyStatementListModel
		extends DefaultFlexiTreeTableDataModel<CertificateAndEfficiencyStatementRow>
		implements SortableFlexiTableDataModel<CertificateAndEfficiencyStatementRow> {
	
	private static final Cols[] COLS = Cols.values();

	private final Locale locale;

	public CertificateAndEfficiencyStatementListModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CertificateAndEfficiencyStatementRow> views= new SortableFlexiTableModelDelegate<>(orderBy, this, locale)
					.sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificateAndEfficiencyStatementRow statement = getObject(row);
		return getValueAt(statement, col);
	}

	@Override
	public Object getValueAt(CertificateAndEfficiencyStatementRow statement, int col) {
		switch (COLS[col]) {
		case displayName:
			return statement.getDisplayName();
		case curriculumElIdent:
			return statement.getCurriculumElementIdentifier();
		case score:
			return statement.getScore();
		case grade:
			return statement.getGrade();
		case passed:
			return statement.getPassed();
		case completion:
			return statement.getCompletion();
		case lastModified:
			return statement.getLastModified();
		case lastUserUpdate:
			return statement.getLastUserModified();
		case certificate:
			return statement.getCertificate();
		case recertification: {
			if (statement.getCertificate() != null) {
				return statement.getCertificate().getNextRecertificationDate();
			}
			return null;
		}
		case efficiencyStatement:
			return statement.getEfficiencyStatementKey();
		case deleteEfficiencyStatement:
			return true;
		case artefact:
			return statement.getEfficiencyStatementKey() != null;
		case tools:
			return statement.getToolsLink();
		}
		return null;
	}
	
	@Override
	public boolean hasChildren(int row) {
		CertificateAndEfficiencyStatementRow element = getObject(row);
		return element.hasChildren();
	}

	public enum Cols implements FlexiSortableColumnDef {

		displayName("table.header.course", true),
		curriculumElIdent("table.header.curriculum.element", true),
		score("table.header.score", true),
		grade("table.header.grade", true),
		passed("table.header.passed", true), 
		completion("table.header.learning.progress", false),
		lastModified("table.header.lastScoreDate", true), 
		lastUserUpdate("table.header.lastUserModificationDate", true),
		efficiencyStatement("table.header.certificate", true), 
		certificate("table.header.certificate", true),
		recertification("table.header.recertification", true), 
		deleteEfficiencyStatement("table.action.delete", false),
		artefact("table.header.artefact", false),
		tools("table.header.actions", false);

		private final String i18n;
		private final boolean sortable;

		private Cols(String i18n, boolean sortable) {
			this.i18n = i18n;
			this.sortable = sortable;
		}

		@Override
		public String i18nHeaderKey() {
			return i18n;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
