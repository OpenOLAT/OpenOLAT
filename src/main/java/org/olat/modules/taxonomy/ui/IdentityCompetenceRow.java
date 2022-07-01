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

import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;

/**
 * 
 * Initial date: 27 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCompetenceRow {

	private final TaxonomyCompetence competence;
	private final String displayName;

	public IdentityCompetenceRow(TaxonomyCompetence competence, String displayName) {
		this.competence = competence;
		this.displayName = displayName;
	}
	
	public Taxonomy getTaxonomy() {
		return competence.getTaxonomyLevel().getTaxonomy();
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return competence.getTaxonomyLevel();
	}
	
	public TaxonomyCompetence getCompetence() {
		return competence;
	}

	public TaxonomyCompetenceTypes getCompetenceType() {
		return competence.getCompetenceType();
	}
	
	public Date getCompetenceExpiration() {
		return competence.getExpiration();
	}
	
	public boolean isManaged() {
		TaxonomyLevel level = competence.getTaxonomyLevel();
		TaxonomyCompetenceTypes competenceType = competence.getCompetenceType();
		TaxonomyLevelManagedFlag marker = TaxonomyLevelManagedFlag.getCorrespondingFlag(competenceType);
		return TaxonomyLevelManagedFlag.isManaged(level, marker);
	}
	
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public int hashCode() {
		return competence == null ? 2364 : competence.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof IdentityCompetenceRow) {
			IdentityCompetenceRow row = (IdentityCompetenceRow)obj;
			return competence != null && competence.equals(row.competence);
		}
		return false;
	}
}
