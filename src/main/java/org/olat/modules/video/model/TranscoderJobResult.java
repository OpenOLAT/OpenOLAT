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
package org.olat.modules.video.model;

import org.json.JSONObject;

/**
 * Initial date: 2026-01-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TranscoderJobResult {
	private final String uuid;
	private final TranscoderJobType type;
	private final String instance;
	private final Long referenceId;
	private final Long size;
	private final String url;

	public TranscoderJobResult(JSONObject jsonObject) {
		uuid = jsonObject.getString("uuid");
		type = jsonObject.getEnum(TranscoderJobType.class, "type");
		instance = jsonObject.getString("instance");
		referenceId = jsonObject.getLong("referenceId");
		JSONObject generated = jsonObject.getJSONObject("generated");
		size = generated.getLong("size");
		url = generated.getString("url");
	}

	public String getUuid() {
		return uuid;
	}

	public TranscoderJobType getType() {
		return type;
	}

	public String getInstance() {
		return instance;
	}

	public Long getReferenceId() {
		return referenceId;
	}

	public Long getSize() {
		return size;
	}

	public String getUrl() {
		return url;
	}
}
