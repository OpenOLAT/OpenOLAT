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
package org.olat.modules.taxonomy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;

/**
 * 
 * Initial date: 2 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ctaxonomyleveltypetotype")
@Table(name="o_tax_taxonomy_type_to_type")
public class TaxonomyLevelTypeToTypeImpl implements Persistable, TaxonomyLevelTypeToType {

	private static final long serialVersionUID = -4154176752303740382L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@ManyToOne(targetEntity=TaxonomyLevelTypeImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_type", nullable=false, insertable=true, updatable=false)
	private TaxonomyLevelType taxonomyLevelType;
	@ManyToOne(targetEntity=TaxonomyLevelTypeImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_allowed_sub_type", nullable=false, insertable=true, updatable=false)
	private TaxonomyLevelType allowedSubTaxonomyLevelType;
	

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public TaxonomyLevelType getTaxonomyLevelType() {
		return taxonomyLevelType;
	}

	public void setTaxonomyLevelType(TaxonomyLevelType taxonomyLevelType) {
		this.taxonomyLevelType = taxonomyLevelType;
	}

	@Override
	public TaxonomyLevelType getAllowedSubTaxonomyLevelType() {
		return allowedSubTaxonomyLevelType;
	}

	public void setAllowedSubTaxonomyLevelType(TaxonomyLevelType allowedSubTaxonomyLevelType) {
		this.allowedSubTaxonomyLevelType = allowedSubTaxonomyLevelType;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 234379 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TaxonomyLevelTypeToTypeImpl) {
			TaxonomyLevelTypeToTypeImpl type = (TaxonomyLevelTypeToTypeImpl)obj;
			return getKey() != null && getKey().equals(type.getKey());
		}
		return false	;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
