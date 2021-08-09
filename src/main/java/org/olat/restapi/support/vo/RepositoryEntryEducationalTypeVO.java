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
package org.olat.restapi.support.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.repository.RepositoryEntryEducationalType;

/**
 * 
 * Initial date: 9 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repositoryEntryEducationalTypeVO")
public class RepositoryEntryEducationalTypeVO {
	
	private Long key;
	
	private String identifier;
	private Boolean predefined;
	private String cssClass;
	
	public RepositoryEntryEducationalTypeVO() {
		//
	}
	
	public static RepositoryEntryEducationalTypeVO valueOf(RepositoryEntryEducationalType type) {
		RepositoryEntryEducationalTypeVO vo = new RepositoryEntryEducationalTypeVO();
		vo.setKey(type.getKey());
		vo.setIdentifier(type.getIdentifier());
		vo.setCssClass(type.getCssClass());
		vo.setPredefined(Boolean.valueOf(type.isPredefined()));
		return vo;
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public Boolean getPredefined() {
		return predefined;
	}
	
	public void setPredefined(Boolean predefined) {
		this.predefined = predefined;
	}
	
	public String getCssClass() {
		return cssClass;
	}
	
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	
	@Override
	public String toString() {
		return "RepositoryEntryEducationalTypeVO[key=" + key + ":identifier=" + identifier + "]";
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -256471238 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RepositoryEntryEducationalTypeVO) {
			RepositoryEntryEducationalTypeVO vo = (RepositoryEntryEducationalTypeVO)obj;
			return key != null && key.equals(vo.key);
		}
		return false;
	}
}
