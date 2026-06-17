/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model.committee;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionImpl;

/**
 * 
 * Initial date: 3 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="committeereport")
@Table(name="o_selectus_committee_report")
public class ReportCommitteeImpl implements ReportCommittee {
	
	private static final long serialVersionUID = -515588877729034234L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="r_role", nullable=true, insertable=true, updatable=true)
	private String role;
	@Column(name="r_ratings_rights", nullable=true, insertable=true, updatable=true)
	private String ratingsRights;
	@Column(name="r_gender", nullable=true, insertable=true, updatable=true)
	private String gender;
	@Column(name="r_user_classification", nullable=true, insertable=true, updatable=true)
	private String userClassification;
	
	@Column(name="r_num_ratings_a", nullable=true, insertable=true, updatable=true)
	private Integer numOfRatingsA;
	@Column(name="r_num_ratings_b", nullable=true, insertable=true, updatable=true)
	private Integer numOfRatingsB;
	@Column(name="r_num_ratings_c", nullable=true, insertable=true, updatable=true)
	private Integer numOfRatingsC;
	@Column(name="r_num_abstentions", nullable=true, insertable=true, updatable=true)
	private Integer numOfAbstentions;
	
	@ManyToOne(targetEntity=PositionImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_position_id", nullable=true, insertable=true, updatable=false)
	private Position position;
	
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
	
	public void setCreationDate(Date date) {
		this.creationDate = date;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}
	
	@Override
	public void setLastModified(Date date) {
		lastModified = date;
	}

	@Override
	public String getRole() {
		return role;
	}

	@Override
	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String getRatingsRights() {
		return ratingsRights;
	}

	@Override
	public void setRatingsRights(String ratingsRights) {
		this.ratingsRights = ratingsRights;
	}

	@Override
	public String getGender() {
		return gender;
	}

	@Override
	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public String getUserClassification() {
		return userClassification;
	}

	@Override
	public void setUserClassification(String userClassification) {
		this.userClassification = userClassification;
	}

	@Override
	public Integer getNumOfRatingsA() {
		return numOfRatingsA;
	}

	@Override
	public void setNumOfRatingsA(Integer numOfRatingsA) {
		this.numOfRatingsA = numOfRatingsA;
	}

	@Override
	public Integer getNumOfRatingsB() {
		return numOfRatingsB;
	}

	@Override
	public void setNumOfRatingsB(Integer numOfRatingsB) {
		this.numOfRatingsB = numOfRatingsB;
	}

	@Override
	public Integer getNumOfRatingsC() {
		return numOfRatingsC;
	}

	@Override
	public void setNumOfRatingsC(Integer numOfRatingsC) {
		this.numOfRatingsC = numOfRatingsC;
	}

	@Override
	public Integer getNumOfAbstentions() {
		return numOfAbstentions;
	}

	@Override
	public void setNumOfAbstentions(Integer numOfAbstentions) {
		this.numOfAbstentions = numOfAbstentions;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 76612 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ReportCommitteeImpl) {
			ReportCommitteeImpl report = (ReportCommitteeImpl)obj;
			return getKey() != null && getKey().equals(report.getKey());
		}
		return super.equals(obj);
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
