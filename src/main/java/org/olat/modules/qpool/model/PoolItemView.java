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
package org.olat.modules.qpool.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 
 * Initial date: 21.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Cacheable(false)
@Entity(name="qpoolitem")
@Table(name="o_qp_item_pool_v")
public class PoolItemView extends AbstractItemView {

	private static final long serialVersionUID = 503607331953283037L;

	@Column(name="item_editable", nullable=false, insertable=false, updatable=false)
	private boolean editable;
	@Column(name="item_pool", nullable=false, insertable=false, updatable=false)
	private Long poolKey;
	
	@Override
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public Long getPoolKey() {
		return poolKey;
	}

	public void setPoolKey(Long poolKey) {
		this.poolKey = poolKey;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PoolItemView) {
			PoolItemView q = (PoolItemView)obj;
			return getKey() != null && getKey().equals(q.getKey());
		}
		return false;
	}
}
