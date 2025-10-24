/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.bigbluebutton.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;

/**
 * 
 * Initial date: 22 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "bigBlueButtonTemplateVO")
public class BigBlueButtonMeetingTemplateVO {
	
	private Long key;
	private String name;
	private String externalId;
	
	private boolean system;
	private boolean enabled;
	
	public BigBlueButtonMeetingTemplateVO() {
		//
	}
	
	public static final BigBlueButtonMeetingTemplateVO valueOf(BigBlueButtonMeetingTemplate template) {
		BigBlueButtonMeetingTemplateVO vo = new BigBlueButtonMeetingTemplateVO();
		vo.setKey(template.getKey());
		vo.setName(template.getName());
		vo.setExternalId(template.getExternalId());
		vo.setSystem(template.isSystem());
		vo.setEnabled(template.isEnabled());
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "BigBlueButtonMeetingTemplateVO[key=" + key + ":name=" + name + "]";
	}
	
	@Override
	public int hashCode() {
		return key == null ? 32948 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BigBlueButtonMeetingTemplateVO template) {
			return key != null && key.equals(template.key);
		}
		return false;
	}
}
