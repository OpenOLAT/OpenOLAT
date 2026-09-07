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
package org.olat.resource.accesscontrol.model;

import java.util.Date;

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
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.jpa.EvaluationFormSurveyImpl;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferToSurvey;

/**
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="offertosurvey")
@Table(name="o_ac_offer_to_survey")
public class OfferToSurveyImpl implements OfferToSurvey, Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = -1657304821903714208L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="a_pos", nullable=false, insertable=true, updatable=true)
	private int pos;

	@ManyToOne(targetEntity=OfferImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_offer", nullable=false, insertable=true, updatable=false)
	private Offer offer;

	@ManyToOne(targetEntity=EvaluationFormSurveyImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_survey", nullable=false, insertable=true, updatable=false)
	private EvaluationFormSurvey survey;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public int getPos() {
		return pos;
	}

	@Override
	public void setPos(int pos) {
		this.pos = pos;
	}

	@Override
	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	@Override
	public EvaluationFormSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(EvaluationFormSurvey survey) {
		this.survey = survey;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -1657304 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OfferToSurveyImpl offerToSurvey) {
			return getKey() != null && getKey().equals(offerToSurvey.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
