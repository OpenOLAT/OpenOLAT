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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement;

/**
 * 
 * Initial date: 22.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateAndEfficiencyStatementListModel extends DefaultFlexiTableDataModel<CertificateAndEfficiencyStatement>
	implements SortableFlexiTableDataModel<CertificateAndEfficiencyStatement> {
	
	private final Locale locale;
	
	public CertificateAndEfficiencyStatementListModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public DefaultFlexiTableDataModel<CertificateAndEfficiencyStatement> createCopyWithEmptyList() {
		return new CertificateAndEfficiencyStatementListModel(getTableColumnModel(), locale);
	}
	
	

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CertificateAndEfficiencyStatement> views = new CertificateAndEfficiencyStatementListSort(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificateAndEfficiencyStatement statement = getObject(row);
		return getValueAt(statement, col);
	}
	
	@Override
	public Object getValueAt(CertificateAndEfficiencyStatement statement, int col) {
		switch(Cols.values()[col]) {
			case displayName: return statement.getDisplayName();
			case score:
				Float score = statement.getScore();
				return AssessmentHelper.getRoundedScore(score);
			case passed: return statement.getPassed();
			case lastModified: return statement.getLastModified();
			case certificate:
				return statement.getCertificate();
			case efficiencyStatement: return statement.getEfficiencyStatementKey();
		}
		return null;
	}

	public static enum Cols {
		
		displayName("table.header.course"),
		score("table.header.score"),
		passed("table.header.passed"),
		lastModified("table.header.lastScoreDate"),
		efficiencyStatement("table.header.certificate"),
		certificate("table.header.certificate");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
	
	public static class CertificateAndEfficiencyStatement {
		
		private Float score;
		private Boolean passed;
		private Date lastModified;
		private String displayName;
		
		private Long resourceKey;
		private Long efficiencyStatementKey;
		private CertificateLight certificate;

		public String getDisplayName() {
			return displayName;
		}
		
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public Float getScore() {
			return score;
		}

		public void setScore(Float score) {
			this.score = score;
		}

		public Boolean getPassed() {
			return passed;
		}

		public void setPassed(Boolean passed) {
			this.passed = passed;
		}

		public Date getLastModified() {
			return lastModified;
		}

		public void setLastModified(Date lastModified) {
			this.lastModified = lastModified;
		}

		public CertificateLight getCertificate() {
			return certificate;
		}
		
		public void setCertificate(CertificateLight certificate) {
			this.certificate = certificate;
		}

		public Long getResourceKey() {
			return resourceKey;
		}

		public void setResourceKey(Long resourceKey) {
			this.resourceKey = resourceKey;
		}

		public Long getEfficiencyStatementKey() {
			return efficiencyStatementKey;
		}

		public void setEfficiencyStatementKey(Long efficiencyStatementKey) {
			this.efficiencyStatementKey = efficiencyStatementKey;
		}
	}
}
