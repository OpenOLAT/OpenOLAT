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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.course.ICourse;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 01.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "olatResourceVO")
public class OlatResourceVO {
	
	private Long key;
	private Long resourceableId;
	private String resourceableTypeName;
	
	public OlatResourceVO() {
		//
	}
	
	public OlatResourceVO(ICourse course) {
		OLATResource resource = course.getCourseEnvironment().getCourseGroupManager()
				.getCourseEntry().getOlatResource();
		
		key = resource.getKey();
		resourceableId = resource.getResourceableId();
		resourceableTypeName = resource.getResourceableTypeName();
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public Long getResourceableId() {
		return resourceableId;
	}

	public void setResourceableId(Long resourceableId) {
		this.resourceableId = resourceableId;
	}

	public String getResourceableTypeName() {
		return resourceableTypeName;
	}

	public void setResourceableTypeName(String resourceableTypeName) {
		this.resourceableTypeName = resourceableTypeName;
	}

	@Override
	public String toString() {
		return "OlatResourceVO[key=" + key + ":name=" + resourceableTypeName + "]";
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -20161 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof OlatResourceVO) {
			OlatResourceVO vo = (OlatResourceVO)obj;
			return key != null && key.equals(vo.key);
		}
		return false;
	}
}
