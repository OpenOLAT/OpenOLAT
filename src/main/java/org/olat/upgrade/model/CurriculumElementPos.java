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
package org.olat.upgrade.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.model.CurriculumElementImpl;

/**
 * Only use to rewrite the position of curriculum elements.
 * 
 * Initial date: 25 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="curriculumelementpos")
@Table(name="o_cur_curriculum_element")
public class CurriculumElementPos implements Persistable {
	
	private static final long serialVersionUID = 547658342562646552L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Column(name="pos", insertable=true, updatable=true)
	private Long pos;
	@Column(name="fk_parent", nullable=true, insertable=false, updatable=false)
	private Long parentKey;
	
	@Column(name="pos_cur", insertable=true, updatable=true)
	private Long posCurriculum;
	@Column(name="fk_curriculum", nullable=true, insertable=false, updatable=false)
	private Long curriculumKey;
	@Column(name="fk_curriculum_parent", nullable=true, insertable=false, updatable=true)
	private Long parentCurriculumKey;
	
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public Long getPos() {
		return pos;
	}

	public void setPos(Long pos) {
		this.pos = pos;
	}

	public Long getParentKey() {
		return parentKey;
	}

	public void setParentKey(Long parentKey) {
		this.parentKey = parentKey;
	}

	public Long getPosCurriculum() {
		return posCurriculum;
	}

	public void setPosCurriculum(Long posCurriculum) {
		this.posCurriculum = posCurriculum;
	}

	public Long getCurriculumKey() {
		return curriculumKey;
	}

	public void setCurriculumKey(Long curriculumKey) {
		this.curriculumKey = curriculumKey;
	}

	public Long getParentCurriculumKey() {
		return parentCurriculumKey;
	}

	public void setParentCurriculumKey(Long parentCurriculumKey) {
		this.parentCurriculumKey = parentCurriculumKey;
	}

	@Override
	public int hashCode() {
		return key == null ? 28562153 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CurriculumElementImpl) {
			CurriculumElementImpl el = (CurriculumElementImpl)obj;
			return getKey() != null && getKey().equals(el.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
