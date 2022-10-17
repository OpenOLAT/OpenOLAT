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
package org.olat.modules.taxonomy.restapi;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.taxonomy.TaxonomyCompetence;

/**
 * 
 * Initial date: 6 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "taxonomyCompetenceVO")
public class TaxonomyCompetenceVO {
	
	private Long key;
	private Long identityKey;
	private Long taxonomyLevelKey;
	private String taxonomyCompetenceType;
	private Date expiration;
	
	public TaxonomyCompetenceVO() {
		//
	}
	
	public TaxonomyCompetenceVO(TaxonomyCompetence competence) {
		key = competence.getKey();
		identityKey = competence.getIdentity().getKey();
		taxonomyLevelKey = competence.getTaxonomyLevel().getKey();
		taxonomyCompetenceType = competence.getCompetenceType().name();
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getTaxonomyLevelKey() {
		return taxonomyLevelKey;
	}

	public void setTaxonomyLevelKey(Long taxonomyLevelKey) {
		this.taxonomyLevelKey = taxonomyLevelKey;
	}

	public String getTaxonomyCompetenceType() {
		return taxonomyCompetenceType;
	}

	public void setTaxonomyCompetenceType(String taxonomyCompetenceType) {
		this.taxonomyCompetenceType = taxonomyCompetenceType;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
}