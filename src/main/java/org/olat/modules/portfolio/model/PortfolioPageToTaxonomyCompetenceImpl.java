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
package org.olat.modules.portfolio.model;

import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioPageToTaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.model.TaxonomyCompetenceImpl;

/**
 * Initial date: 08.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Entity(name = "pfpagetotaxonomycompetence")
@Table(name = "o_pf_page_to_tax_competence")
public class PortfolioPageToTaxonomyCompetenceImpl implements PortfolioPageToTaxonomyCompetence, Persistable, CreateInfo {

	private static final long serialVersionUID = 7492748503495032549L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;
	
	@ManyToOne(targetEntity = PageImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_pf_page", nullable = false, insertable = true, updatable = false)
	private Page portfolioPage;

	@ManyToOne(targetEntity = TaxonomyCompetenceImpl.class, fetch = FetchType.LAZY, optional = false, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "fk_tax_competence", nullable = false, insertable = true, updatable = false)
	private TaxonomyCompetence taxonomyCompetence;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}	
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Page getPortfolioPage() {
		return portfolioPage;
	}
	
	public void setPortfolioPage(Page portfolioPage) {
		this.portfolioPage = portfolioPage;
	}
	
	@Override
	public TaxonomyCompetence getTaxonomyCompetence() {
		return taxonomyCompetence;
	}
	
	public void setTaxonomyCompetence(TaxonomyCompetence taxonomyCompetence) {
		this.taxonomyCompetence = taxonomyCompetence;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 492365 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(obj instanceof PortfolioPageToTaxonomyCompetenceImpl) {
			PortfolioPageToTaxonomyCompetenceImpl rel = (PortfolioPageToTaxonomyCompetenceImpl)obj;
			return getKey() != null && getKey().equals(rel.getKey());
		}
		
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
