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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;

/**
 * 
 * Initial date: 25 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="upgradequestionitem")
@Table(name="o_qp_item")
public class UpgradeQuestionItem implements Persistable {

	private static final long serialVersionUID = 6264601750280239307L;

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
	
	
	//classification
	@ManyToOne(targetEntity=UpgradeTaxonomyLevel.class)
	@JoinColumn(name="fk_taxonomy_level", nullable=true, insertable=true, updatable=true)
	private UpgradeTaxonomyLevel oldTaxonomyLevel;
	
	@ManyToOne(targetEntity=TaxonomyLevelImpl.class)
	@JoinColumn(name="fk_taxonomy_level_v2", nullable=true, insertable=true, updatable=true)
	private TaxonomyLevel newTaxonomyLevel;
	

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public UpgradeTaxonomyLevel getOldTaxonomyLevel() {
		return oldTaxonomyLevel;
	}

	public void setOldTaxonomyLevel(UpgradeTaxonomyLevel oldTaxonomyLevel) {
		this.oldTaxonomyLevel = oldTaxonomyLevel;
	}

	public TaxonomyLevel getNewTaxonomyLevel() {
		return newTaxonomyLevel;
	}

	public void setNewTaxonomyLevel(TaxonomyLevel newTaxonomyLevel) {
		this.newTaxonomyLevel = newTaxonomyLevel;
	}

	@Override
	public int hashCode() {
		return key == null ? 97489 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UpgradeQuestionItem) {
			UpgradeQuestionItem q = (UpgradeQuestionItem)obj;
			return key != null && key.equals(q.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("question[key=").append(key).append("]").append(super.toString());
		return sb.toString();
	}
}
