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
package org.olat.modules.taxonomy.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Page;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * Initial date: 19.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetencesOverviewTableRow implements FlexiTreeTableNode {
	
	private Translator translator;
	
	private Taxonomy taxonomy;
	private TaxonomyLevel level;
	private TaxonomyCompetence competence;
	private Page portfolioLocation;
	private CompetencesOverviewTableRow parent;
	private FormLink detailsLink;
	
	private boolean isUsed;
	private boolean isManaged;
	private boolean hasChildren = true;
	
	public CompetencesOverviewTableRow(Translator translator) {
		this.translator = translator;
	}
	
	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}
	
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
	
	public void setLevel(TaxonomyLevel level) {
		this.level = level;
	}
	
	public TaxonomyLevel getLevel() {
		return level;
	}
	
	public void setCompetence(TaxonomyCompetence competence) {
		this.competence = competence;
		this.hasChildren = false;
		setUsed(true);
	}
	
	public void setParent(CompetencesOverviewTableRow parent) {
		this.parent = parent;
	}
	
	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
		
		if (parent != null) {
			parent.setUsed(isUsed);
		}
	}
	
	public boolean isUsed() {
		return isUsed;
	}
	
	public void setManaged(boolean isManaged) {
		this.isManaged = isManaged;
	}
	
	public boolean isManaged() {
		return isManaged;
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}
	
	public Long getKey() {
		if (competence != null) {
			return competence.getKey();
		} else if (level != null) {
			return level.getKey();
		} else if (taxonomy!= null) {
			return taxonomy.getKey();
		} else {
			return null;
		}		
	}
	
	public String getDisplayName() {
		if (competence != null) {
			if (competence.getLinkLocation().equals(TaxonomyCompetenceLinkLocations.PORTFOLIO)) {
				// Should not happen, just in case some deletion is not completed or there is old data in the database
				try {
					return portfolioLocation.getSection().getBinder().getTitle() + " / " + portfolioLocation.getSection().getTitle() + " / " + portfolioLocation.getTitle();
				} catch (Exception e) {
					return "ERROR";
				}
			} else if (competence.getLinkLocation() != null) {
				if (translator != null ) {
					return translator.translate(competence.getLinkLocation().i18nKey());
				}
				return competence.getLinkLocation().name();
			}
			return "";
		} else if (level != null) {
			return TaxonomyUIFactory.translateDisplayName(translator, level);
		} else if (taxonomy!= null) {
			return taxonomy.getDisplayName();
		} else {
			return null;
		}
	}
	
	public Date getExpiration() {
		if (competence != null) {
			return competence.getExpiration();
		} else {
			return null;
		}
	}
	
	public String getType() {
		if (competence != null) {
			if (translator != null) {
				return translator.translate(competence.getCompetenceType().i18nKey());
			} else {
				return competence.getCompetenceType().toString();
			}
		} 
		
		return null;
	}
	
	public String getTaxonomyDisplayName() {
		if (taxonomy != null) {
			return taxonomy.getDisplayName();
		}
		
		return null;
	}
	
	
	public String getTaxonomyIdentifier() {
		if (taxonomy != null) {
			return taxonomy.getIdentifier();
		}
		
		return null;
	}
	
	public String getTaxonomyExternalId() {
		if (taxonomy != null) {
			return taxonomy.getExternalId();
		}
		
		return null;
	}
	
	public String getTaxonomyLevelIdentifier() {
		if (level != null) {
			return level.getIdentifier();
		}
		
		return null;
	}
	
	public String getTaxonomyLevelDisplayName() {
		if (level != null) {
			return TaxonomyUIFactory.translateDisplayName(translator, level);
		}
		
		return null;
	}
	
	public String getTaxonomyLevelExternalId() {
		if (level != null) {
			return level.getExternalId();
		}
		
		return null;
	}
	
	public String getTaxonomyLevelType() {
		if (level != null && level.getType() != null) {
			return level.getType().getDisplayName();
		}
		
		return null;
	}
	
	public String getResource() {
		if (competence != null) {
			if (translator != null) {
				return translator.translate(competence.getLinkLocation().i18nKey());
			}
			return competence.getLinkLocation().name();
		} else {
			return null;
		}
	}
	
	public void setPortfolioLocation(Page portfolioLocation) {
		this.portfolioLocation = portfolioLocation;
	}
	
	public Page getPortfolioLocation() {
		return portfolioLocation;
	}

	public TaxonomyCompetence getCompetence() {
		return competence;
	}
	
	public boolean isCompetence() {
		return competence != null;
	}
	
	public Date getCreationDate() {
		return competence != null ? competence.getCreationDate() : null;
	}
	
	public boolean hasDescription() {
		if (competence != null) {
			return StringHelper.containsNonWhitespace(competence.getSourceText()) || StringHelper.containsNonWhitespace(competence.getSourceUrl());
		} else if (level != null) {
			return StringHelper.containsNonWhitespace(TaxonomyUIFactory.translateDescription(translator, level));
		} else if (taxonomy != null) {
			return StringHelper.containsNonWhitespace(taxonomy.getDescription());
		} else {
			return false;
		}
	}
	
	public FormLink getDetailsLink() {
		return detailsLink;
	}
	
	public void setDetailsLink(FormLink detailsLink) {
		this.detailsLink = detailsLink;
	}
	
	public String getDescription() {
		if (level != null) {
			return TaxonomyUIFactory.translateDescription(translator, level);
		} else if (taxonomy != null) {
			return taxonomy.getDescription();
		} else {
			return null;
		}
	}
	
	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}

	@Override
	public String getCrump() {
		if (getDisplayName() != null) {
			return getDisplayName();
		} else if (translator != null) {
			return translator.translate("competences");
		} else {
			return null;
		}
	}
}
