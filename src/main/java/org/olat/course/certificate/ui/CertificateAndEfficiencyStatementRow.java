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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.CertificateLight;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 2 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateAndEfficiencyStatementRow implements FlexiTreeTableNode {
	
	private Float score = 0f;
	private Float scoreMax;
	private String grade;
	private Boolean passed;
	private Date lastModified;
	private String displayName;
	private Date lastUserModified;

	private Long resourceKey;
	private Long efficiencyStatementKey;
	private Long courseRepoKey;
	private CertificateLight certificate;
	private Double completion;
	
	private TaxonomyLevel taxonomyLevel;
	private CurriculumElement parentElement;	// Only used for taxonomy elements
	
	private CurriculumElement curriculumElement;
	private Curriculum curriculum;
	
	private boolean hasChildren;
	private boolean isTaxonomy;
	private boolean isCurriculumElement;
	private boolean holdsScore = true;
	private boolean isStatement = false;
	private boolean hasStatementChildren = false;
	
	private List<Long> addedToScoresIds = new ArrayList<>();
	
	private CertificateAndEfficiencyStatementRow parent;
	
	private FormLink toolsLink;

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
	
	public void addToScore(Float maxScore, Float score, Long statementKey) {
		if (holdsScore && !addedToScoresIds.contains(statementKey)) {
			if (scoreMax == null) {
				scoreMax = maxScore;
			} else if (maxScore != null) {
				scoreMax += maxScore;
			}
			
			if (score != null) {
				this.score += score;
			}
			
			addedToScoresIds.add(statementKey);
		}
		
		if (parent != null) {
			parent.addToScore(maxScore, score, statementKey);
		}
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
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
	
	public Long getCertificateKey() {
		return certificate == null ? null : certificate.getKey();
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
	public CertificateAndEfficiencyStatementRow getParent() {
		return parent;
	}
	
	public void setParent(CertificateAndEfficiencyStatementRow parent) {
		if (parent != null) {
			parent.setHasChildren(true);
			this.parent = parent;
		}
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
	
	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	public void setCurriculum(Curriculum curriculum) {
		this.curriculum = curriculum;
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}
	
	public void setTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
	}
	
	public CurriculumElement getParentElement() {
		return parentElement;
	}
	
	public void setParentElement(CurriculumElement parentElement) {
		this.parentElement = parentElement;
	}
	
	public boolean isStatement() {
		return isStatement;
	}
	
	public void setStatement(boolean isStatement) {
		this.isStatement = isStatement;
	}
	
	public boolean hasStatementChildren() {
		return hasStatementChildren;
	}
	
	public void setHasStatementChildren(boolean hasStatementChildren) {
		this.hasStatementChildren = hasStatementChildren;
	}
	
	public Long getCourseRepoKey() {
		return courseRepoKey;
	}
	
	public void setCourseRepoKey(Long courseRepoKey) {
		this.courseRepoKey = courseRepoKey;
	}
	
	public void setIsCurriculumElement(boolean isCurriculumElement) {
		this.isCurriculumElement = isCurriculumElement;
	}
	
	public String getCurriculumElementIdentifier() {
		String ident = "";
		
		if (isCurriculumElement) {
			if (curriculumElement != null && StringHelper.containsNonWhitespace(curriculumElement.getIdentifier())) {
				ident = curriculumElement.getIdentifier();
			}
		}
		
		return ident;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	@Override
	public String getCrump() {
		String crump = "";
		
		if (parent != null) {
			crump += "/";
			crump += parent.getCrump();
		}
		
		if (isTaxonomy) {
			crump += "/";
			crump += taxonomyLevel.getKey().toString();
		}
		
		if (isCurriculumElement) {
			crump += "/";
			crump += curriculumElement.getKey().toString();
		}
		
		if (isStatement) {
			crump += "/";
			crump += efficiencyStatementKey.toString();
		}
		
		return crump;
	}

}
