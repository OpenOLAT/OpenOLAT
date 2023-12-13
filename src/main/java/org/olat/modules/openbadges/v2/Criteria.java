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
package org.olat.modules.openbadges.v2;

import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;

import org.json.JSONObject;

/**
 * Initial date: 2023-05-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class Criteria {
	private static final String TYPE_VALUE = "Criteria";
	private static final String NARRATIVE_KEY = "narrative";

	/*
	 * Typical value: https://instance.openolat.org/badges/badgeclass/34565.
	 * The URL returns a HTML page with information about the badge class and its
	 * criteria.
	 */
	private String id;
	private String narrative;

	public Criteria(JSONObject jsonObject) {
		for (String key : jsonObject.keySet()) {
			if (Constants.ID_KEY.equals(key)) {
				if (jsonObject.get(Constants.ID_KEY) instanceof String idString) {
					setId(idString);
				} else {
					throw new IllegalArgumentException("Invalid criteria ID.");
				}
			} else if (Constants.TYPE_KEY.equals(key)) {
				if (!TYPE_VALUE.equals(jsonObject.get(Constants.TYPE_KEY))) {
					throw new IllegalArgumentException("Only type 'Criteria' supported.");
				}
			} else if (NARRATIVE_KEY.equals(key)) {
				if (jsonObject.get(NARRATIVE_KEY) instanceof String nameString) {
					setNarrative(nameString);
				} else {
					throw new IllegalArgumentException("Invalid criteria narrative.");
				}
			}
		}
	}

	public Criteria(BadgeClass badgeClass) {
		setId(OpenBadgesFactory.createCriteriaUrl(badgeClass.getUuid()));
		BadgeCriteria criteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
		if (criteria != null) {
			setNarrative(criteria.getDescriptionWithScan());
		} else {
			setNarrative("");
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNarrative() {
		return narrative;
	}

	public void setNarrative(String narrative) {
		this.narrative = narrative;
	}

	public JSONObject asJsonObject() {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put(Constants.TYPE_KEY, TYPE_VALUE);
		jsonObject.put(Constants.ID_KEY, getId());
		jsonObject.put(NARRATIVE_KEY, getNarrative());

		return jsonObject;
	}
}
