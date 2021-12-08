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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement;

/**
 * 
 * Initial date: 22.10.2014<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateAndEfficiencyStatementListModel
		extends DefaultFlexiTreeTableDataModel<CertificateAndEfficiencyStatement>
		implements SortableFlexiTableDataModel<CertificateAndEfficiencyStatement> {

	private final Locale locale;

	public CertificateAndEfficiencyStatementListModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CertificateAndEfficiencyStatement> views = new CertificateAndEfficiencyStatementListSort(orderBy, this,
					locale).sort();
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
		switch (Cols.values()[col]) {
		case displayName:
			return statement.getDisplayName();
		case score:
			return statement.getScore();
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
		}
		return null;
	}
	
	@Override
	public boolean hasChildren(int row) {
		CertificateAndEfficiencyStatement element = getObject(row);
		return element.hasChildren();
	}

	public enum Cols implements FlexiSortableColumnDef {

		displayName("table.header.course", true), score("table.header.score", true),
		passed("table.header.passed", true), completion("table.header.learning.progress", false),
		lastModified("table.header.lastScoreDate", true), lastUserUpdate("table.header.lastUserModificationDate", true),
		efficiencyStatement("table.header.certificate", true), certificate("table.header.certificate", true),
		recertification("table.header.recertification", true), deleteEfficiencyStatement("table.action.delete", false),
		artefact("table.header.artefact", false);

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

	public static class CertificateAndEfficiencyStatement implements FlexiTreeTableNode {

		private Float score = 0f;
		private Float scoreMax;
		private Boolean passed;
		private Date lastModified;
		private String displayName;
		private Date lastUserModified;

		private Long resourceKey;
		private Long efficiencyStatementKey;
		private CertificateLight certificate;
		private Double completion;
		
		private boolean hasChildren;
		private boolean isTaxonomy;
		private boolean holdsScore = true;
		
		private CertificateAndEfficiencyStatement parent;

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getScore() {
			String returnScore = "";
			
			if (holdsScore) {
				if (score != null) {
					returnScore += AssessmentHelper.getRoundedScore(score).toString();
				}
				
				if (scoreMax != null) {
					if (score == null) {
						returnScore += "0";
					}
					
					returnScore += " / ";
					returnScore += AssessmentHelper.getRoundedScore(scoreMax).toString();
				}
			}
			
			return returnScore;
		}

		public void setScore(Float score) {
			this.score = score;
		}
		
		public void addToScore(Float maxScore, Float score, boolean addToParent) {
			if (holdsScore) {
				if (scoreMax == null) {
					scoreMax = maxScore;
				} else if (maxScore != null) {
					scoreMax += maxScore;
				}
				
				if (score != null) {
					this.score += score;
				}
			}
			
			if (addToParent && parent != null) {
				parent.addToScore(maxScore, score, addToParent);
			}
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

		public Date getLastUserModified() {
			return lastUserModified;
		}

		public void setLastUserModified(Date lastUserModified) {
			this.lastUserModified = lastUserModified;
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

		public Double getCompletion() {
			return completion;
		}

		public void setCompletion(Double completion) {
			this.completion = completion;
		}

		@Override
		public FlexiTreeTableNode getParent() {
			return parent;
		}
		
		public void setParent(CertificateAndEfficiencyStatement parent) {
			parent.setHasChildren(true);
			this.parent = parent;
		}
		
		public boolean hasChildren() {
			return hasChildren;
		}
		
		public void setHasChildren(boolean hasChildren) {
			this.hasChildren = hasChildren;
		}
		
		public boolean isTaxonomy() {
			return isTaxonomy;
		}
		
		public void setTaxonomy(boolean isTaxonomy) {
			this.isTaxonomy = isTaxonomy;
		}
		
		public void setHoldsScore(boolean holdsScore) {
			this.holdsScore = holdsScore;
		}

		@Override
		public String getCrump() {
			return null;
		}

	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		// TODO Auto-generated method stub
		
	}
}
