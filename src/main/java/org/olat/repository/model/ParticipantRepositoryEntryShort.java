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
package org.olat.repository.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 11.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Cacheable(false)
@Entity(name="participantrepoentry")
@Table(name="o_re_participant_v")
@NamedQueries({
	@NamedQuery(name="loadStudentResources", query="select v from participantrepoentry v where v.memberId=:identityKey")
})
public class ParticipantRepositoryEntryShort implements Persistable {

	private static final long serialVersionUID = 4425548169277454215L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="re_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Column(name="member_id", nullable=false, insertable=false, updatable=false)
	private Long memberId;

	@Override
	public Long getKey() {
		return key;
	}
	
	public Long getMemberId() {
		return memberId;
	}

	@Override
	public int hashCode() {
		return key == null ? 92867 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ParticipantRepositoryEntryShort) {
			ParticipantRepositoryEntryShort entry = (ParticipantRepositoryEntryShort)obj;
			return key != null && key.equals(entry.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {	
		return equals(persistable);
	}
}
