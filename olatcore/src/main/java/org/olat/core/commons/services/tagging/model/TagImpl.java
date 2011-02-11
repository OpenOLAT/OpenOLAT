/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.commons.services.tagging.model;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Description:<br>
 * Hibernate implementation of @ref{org.olat.core.commons.services.tagging.model.Tag} 
 * 
 * <P>
 * Initial Date:  19 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TagImpl extends PersistentObject implements Tag {

	private String tag;
	private String resName;
	private Long resId;
	private String resSubPath;
	private String businessPath;
	private Identity author;
	
	public TagImpl() {
		//
	}
	
	@Override
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@Override
	public OLATResourceable getOLATResourceable() {
		final Long id = resId;
		final String name = resName;
		
		return new OLATResourceable() {
			@Override
			public Long getResourceableId() {
				return id;
			}

			@Override
			public String getResourceableTypeName() {
				return name;
			}
		};
	}
	
	public String getResName() {
		return resName;
	}
	
	public void setResName(String resName) {
		this.resName = resName;
	}
	
	public Long getResId() {
		return resId;
	}
	
	public void setResId(Long resId) {
		this.resId = resId;
	}
	
	@Override
	public String getResSubPath() {
		return resSubPath;
	}
	
	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}
	
	@Override
	public String getBusinessPath() {
		return businessPath;
	}
	
	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}
	
	@Override
	public Identity getAuthor() {
		return author;
	}
	
	public void setAuthor(Identity author) {
		this.author = author;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj instanceof TagImpl) {
			TagImpl tagImpl = (TagImpl)obj;
			return this.getKey().equals(tagImpl.getKey());
		}
		return false;
	}
}
