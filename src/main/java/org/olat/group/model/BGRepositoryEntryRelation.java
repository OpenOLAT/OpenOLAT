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
package org.olat.group.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Table(name="o_gp_business_to_repository_v")
@Entity(name="repoentryrelationview")
public class BGRepositoryEntryRelation implements Serializable {

	private static final long serialVersionUID = -589388325515371455L;

    @EmbeddedId
    private RelationId relationId;
	
	@Column(name="re_displayname", nullable=false, insertable=true, updatable=true)
	private String repositoryEntryDisplayName;

	public BGRepositoryEntryRelation() {
		//
	}
	
	public Long getGroupKey() {
		return relationId.getGroupKey();
	}

	public Long getRepositoryEntryKey() {
		return relationId.getRepositoryEntryKey();
	}
	
	public String getRepositoryEntryDisplayName() {
		return repositoryEntryDisplayName;
	}
	
	public void setRepositoryEntryDisplayName(String repositoryEntryDisplayName) {
		this.repositoryEntryDisplayName = repositoryEntryDisplayName;
	}
	
	@Override
	public int hashCode() {
		return (getGroupKey() == null ? 8934 : getGroupKey().hashCode()) +
				(getRepositoryEntryKey() == null ? 8934 : getRepositoryEntryKey().hashCode());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BGRepositoryEntryRelation) {
			BGRepositoryEntryRelation rel = (BGRepositoryEntryRelation)obj;
			return getGroupKey() != null && getGroupKey().equals(rel.getGroupKey())
					&& getRepositoryEntryKey() != null && getRepositoryEntryKey().equals(rel.getRepositoryEntryKey());
		}
		return false;
	}
	
	@Embeddable
	public static class RelationId implements Serializable {

		private static final long serialVersionUID = -1215870965613146741L;
		
		@Column(name = "grp_id")
	    private Long groupKey;
	    @Column(name = "re_id")
		private Long repositoryEntryKey;
	    
	    public RelationId() {
	    	//
	    }
	    
		public Long getGroupKey() {
			return groupKey;
		}
		
		public void setGroupKey(Long groupKey) {
			this.groupKey = groupKey;
		}
		
		public Long getRepositoryEntryKey() {
			return repositoryEntryKey;
		}
		
		public void setRepositoryEntryKey(Long repositoryEntryKey) {
			this.repositoryEntryKey = repositoryEntryKey;
		}
		
		@Override
		public int hashCode() {
			return (groupKey == null ? 478 : groupKey.hashCode())
				+ (repositoryEntryKey == null ? 765912 : repositoryEntryKey.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof RelationId) {
				RelationId id = (RelationId)obj;
				return groupKey != null && groupKey.equals(id.groupKey)
						&& repositoryEntryKey != null && repositoryEntryKey.equals(id.repositoryEntryKey);
			}
			return false;
		}
	}
}