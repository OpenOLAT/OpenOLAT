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
package org.olat.ims.qti21.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.olat.core.id.Persistable;

/**
 * Need to update extra time column.
 * 
 * Initial date: 27 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qtiassessmenttestsessionextratime")
@Table(name="o_qti_assessmenttest_session")
public class AssessmentTestSessionExtraTime implements Persistable {

	private static final long serialVersionUID = -2701419201645089881L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
    @Column(name="q_extra_time", nullable=true, insertable=false, updatable=true)
    private Integer extraTime;
    @Column(name="q_compensation_extra_time", nullable=true, insertable=false, updatable=true)
    private Integer compensationExtraTime;
    
    @Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Integer getExtraTime() {
		return extraTime;
	}

	public void setExtraTime(Integer extraTime) {
		this.extraTime = extraTime;
	}

	public Integer getCompensationExtraTime() {
		return compensationExtraTime;
	}

	public void setCompensationExtraTime(Integer compensationExtraTime) {
		this.compensationExtraTime = compensationExtraTime;
	}

	@Override
	public int hashCode() {
		return key == null ? 2687 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AssessmentTestSessionExtraTime) {
			AssessmentTestSessionExtraTime time = (AssessmentTestSessionExtraTime)obj;
			return key != null && key.equals(time.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
