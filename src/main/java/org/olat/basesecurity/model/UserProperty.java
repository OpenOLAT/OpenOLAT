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
package org.olat.basesecurity.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 
 * This is only an helper mapping for the power search. Don't use it to persist
 * or update user property.
 * 
 * Initial date: 14.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Table(name="o_userproperty")
@Entity(name="userproperty")
public class UserProperty {
	
    @EmbeddedId
    private UserPropertyId propertyId;
    
	@Column(name="propvalue", nullable=false, insertable=false, updatable=false)
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	@Embeddable
	public static class UserPropertyId implements Serializable {

		private static final long serialVersionUID = -1215870965613146741L;
		
		@Column(name = "fk_user_id")
	    private Long userId;
	    @Column(name = "propname")
		private String name;
	    
	    public UserPropertyId() {
	    	//
	    }
		
		public Long getUserId() {
			return userId;
		}


		public void setUserId(Long userId) {
			this.userId = userId;
		}


		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}


		@Override
		public int hashCode() {
			return (userId == null ? 478 : userId.hashCode())
				+ (name == null ? 765912 : name.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof UserPropertyId) {
				UserPropertyId id = (UserPropertyId)obj;
				return userId != null && userId.equals(id.userId)
						&& name != null && name.equals(id.name);
			}
			return false;
		}
	}
}