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

import java.io.IOException;

import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Initial date: 2023-05-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class Badge {
	private static final Logger log = Tracing.createLoggerFor(Badge.class);

	private static final String TYPE_VALUE = "BadgeClass";

	/*
	 * Typical value: https://instance.openolat.org/badges/badge/23456
	 * The URL returns a JSON of the badge.
	 */
	private String id;
	private String uuid;
	private String language;
	private String version;
	private String name;
	private String description;
	private String image;
	private Criteria criteria;
	private Profile issuer;

	public Badge(JSONObject jsonObject) {
		for (String key : jsonObject.keySet()) {
			if (Constants.ID_KEY.equals(key)) {
				if (jsonObject.get(Constants.ID_KEY) instanceof String idString) {
					setId(idString);
				} else {
					throw new IllegalArgumentException("Invalid badge ID.");
				}
			} else if (Constants.VERSION_KEY.equals(key)) {
				if (jsonObject.get(Constants.VERSION_KEY) instanceof String versionString) {
					setVersion(versionString);
				} else {
					throw new IllegalArgumentException("Invalid badge version.");
				}
			} else if (Constants.TYPE_KEY.equals(key)) {
				if (!TYPE_VALUE.equals(jsonObject.get(Constants.TYPE_KEY))) {
					throw new IllegalArgumentException("Only type 'BadgeClass' supported.");
				}
			} else if (Constants.NAME_KEY.equals(key)) {
				if (jsonObject.get(Constants.NAME_KEY) instanceof String nameString) {
					setName(nameString);
				} else {
					throw new IllegalArgumentException("Invalid badge name.");
				}
			} else if (Constants.DESCRIPTION_KEY.equals(key)) {
				if (jsonObject.get(Constants.DESCRIPTION_KEY) instanceof String descriptionString) {
					setDescription(descriptionString);
				} else {
					throw new IllegalArgumentException("Invalid badge description.");
				}
			} else if (Constants.IMAGE_KEY.equals(key)) {
				if (jsonObject.get(Constants.IMAGE_KEY) instanceof String imageString) {
					setImage(imageString);
				} else if (jsonObject.get(Constants.IMAGE_KEY) instanceof JSONObject imageJsonObject) {
					System.err.println("got an image object");
				} else {
					throw new IllegalArgumentException("Invalid badge image.");
				}
			} else if (Constants.CRITERIA_KEY.equals(key)) {
				if (jsonObject.get(Constants.CRITERIA_KEY) instanceof JSONObject criteriaJsonObject) {
					setCriteria(new Criteria(criteriaJsonObject));
				} else {
					throw new IllegalArgumentException("Invalid badge criteria.");
				}
			} else if (Constants.ISSUER_KEY.equals(key)) {
				if (jsonObject.get(Constants.ISSUER_KEY) instanceof JSONObject issuerJsonObject) {
					setIssuer(new Profile(issuerJsonObject));
				} else {
					throw new IllegalArgumentException("Invalid badge criteria.");
				}
			}
		}
	}

	public Badge(BadgeClass badgeClass) {
		setId(OpenBadgesFactory.createBadgeClassUrl(badgeClass.getUuid()));
		setUuid(badgeClass.getUuid());
		setLanguage(badgeClass.getLanguage());
		setVersion(badgeClass.getVersion());
		setName(badgeClass.getName());
		setDescription(badgeClass.getDescription());
		setImage(badgeClass.getImage());
		setCriteria(new Criteria(badgeClass));
		setIssuer(new Profile(badgeClass));
	}

	public JSONObject asJsonObject() {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put(Constants.TYPE_KEY, TYPE_VALUE);
		jsonObject.put(Constants.CONTEXT_KEY, Constants.CONTEXT_VALUE);
		if (StringHelper.containsNonWhitespace(language)) {
			jsonObject.put(Constants.LANGUAGE_KEY, language);
		}
		jsonObject.put(Constants.ID_KEY, getId());
		jsonObject.put(Constants.VERSION_KEY, getVersion());
		jsonObject.put(Constants.NAME_KEY, getName());
		jsonObject.put(Constants.DESCRIPTION_KEY, getDescription());
		jsonObject.put(Constants.IMAGE_KEY, getImageAsJsonObject());
		jsonObject.put(Constants.CRITERIA_KEY, getCriteria().asJsonObject());
		jsonObject.put(Constants.ISSUER_KEY, getIssuer().asJsonObject(Constants.TYPE_VALUE_ISSUER));

		return jsonObject;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImage() {
		return image;
	}

	public JSONObject getImageAsJsonObject() {
		JSONObject jsonObject = new JSONObject();
		String id = OpenBadgesFactory.createImageUrl(getUuid());
		jsonObject.put(Constants.ID_KEY, id);
		return jsonObject;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public Profile getIssuer() {
		return issuer;
	}

	public void setIssuer(Profile issuer) {
		this.issuer = issuer;
	}

	public VFSLeaf storeImageAsPng(VFSContainer targetContainer) {
		if (StringHelper.containsNonWhitespace(getName()) && StringHelper.containsNonWhitespace(getImage())) {
			String fileName = getName() + ".png";
			VFSLeaf leaf = targetContainer.createChildLeaf(fileName);
			if (leaf != null) {
				if (getImage().startsWith(Constants.PNG_BASE64_PREFIX)) {
					byte[] pngBuffer = Base64.decodeBase64(image.substring(Constants.PNG_BASE64_PREFIX.length()));
					try {
						leaf.getOutputStream(false).write(pngBuffer);
						return leaf;
					} catch (IOException e) {
						log.error("Failed to write PNG image.", e);
					}
				}
			}
		}
		return null;
	}
}
