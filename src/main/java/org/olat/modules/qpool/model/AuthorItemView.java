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
@Entity(name="qauthoritem")
@Table(name="o_qp_item_author_v")
public class AuthorItemView extends AbstractItemView {

	private static final long serialVersionUID = 503607331953283037L;
	
	@Column(name="item_author", nullable=false, insertable=false, updatable=false)
	private Long authorKey;
	
	@Override
	public boolean isEditable() {
		return true;
	}

	public Long getAuthorKey() {
		return authorKey;
	}

	public void setAuthorKey(Long authorKey) {
		this.authorKey = authorKey;
	}


	@Override
	public int hashCode() {
		return getKey() == null ? 97489 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AuthorItemView) {
			AuthorItemView q = (AuthorItemView)obj;
			return getKey() != null && getKey().equals(q.getKey());
		}
		return false;
	}
}
