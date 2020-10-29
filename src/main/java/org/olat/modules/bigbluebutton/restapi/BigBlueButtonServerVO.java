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
package org.olat.modules.bigbluebutton.restapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.modules.bigbluebutton.BigBlueButtonServer;

/**
 * 
 * Initial date: 29 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "bigBlueButtonServerVO")
public class BigBlueButtonServerVO {
	
	private Long key;
	private String name;
	private String url;
	private String sharedSecret;
	private String recordingUrl;
	private Double capacityFactory;
	private Boolean enabled;
	
	public BigBlueButtonServerVO() {
		//
	}
	
	public static final BigBlueButtonServerVO valueOf(BigBlueButtonServer server) {
		BigBlueButtonServerVO vo = new BigBlueButtonServerVO();
		vo.setKey(server.getKey());
		vo.setName(server.getName());
		vo.setUrl(server.getUrl());
		vo.setSharedSecret(server.getSharedSecret());
		vo.setRecordingUrl(server.getRecordingUrl());
		vo.setCapacityFactory(server.getCapacityFactory());
		vo.setEnabled(Boolean.valueOf(server.isEnabled()));
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public String getRecordingUrl() {
		return recordingUrl;
	}

	public void setRecordingUrl(String recordingUrl) {
		this.recordingUrl = recordingUrl;
	}

	public Double getCapacityFactory() {
		return capacityFactory;
	}

	public void setCapacityFactory(Double capacityFactory) {
		this.capacityFactory = capacityFactory;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	

}
