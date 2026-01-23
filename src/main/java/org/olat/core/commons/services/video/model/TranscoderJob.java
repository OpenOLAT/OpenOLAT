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
package org.olat.core.commons.services.video.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Initial date: 2026-01-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscoderJob {
	public static final String TRANSCODING_NAMESPACE = "transcoding";
	public static final String POST_JOB_COMMAND = "postJob";
	public static final String DELETE_GENERATED_COMMAND = "deleteGenerated";
	public static final String NOTIFY_RESULT_COMMAND = "notifyResult";
	
	private String uuid;
	private TranscoderJobType type;
	private String instance;
	private Long referenceId;
	private String notifyResultUrl;
	private TranscoderOriginal original;
	private List<Integer> resolutions;
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public TranscoderJobType getType() {
		return type;
	}

	public void setType(TranscoderJobType type) {
		this.type = type;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public Long getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(Long referenceId) {
		this.referenceId = referenceId;
	}

	public String getNotifyResultUrl() {
		return notifyResultUrl;
	}

	public void setNotifyResultUrl(String notifyResultUrl) {
		this.notifyResultUrl = notifyResultUrl;
	}

	public TranscoderOriginal getOriginal() {
		return original;
	}

	public void setOriginal(TranscoderOriginal original) {
		this.original = original;
	}

	public List<Integer> getResolutions() {
		return resolutions;
	}

	public void setResolutions(List<Integer> resolutions) {
		this.resolutions = resolutions;
	}
}
