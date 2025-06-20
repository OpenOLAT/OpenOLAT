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
package org.olat.modules.openbadges;

import java.util.UUID;

import org.olat.core.helpers.Settings;
import org.olat.core.util.FileUtils;

/**
 * Initial date: 2023-06-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesFactory {

	public static final String BADGE_PATH = "badge/";
	public static final String ASSERTION_PATH = "assertion/";
	public static final String CLASS_PATH = "class/";
	public static final String CRITERIA_PATH = "criteria/";
	public static final String ISSUER_PATH = "issuer/";
	public static final String IMAGE_PATH = "image/";
	public static final String KEY_PATH = "key/";
	public static final String ORGANIZATION_PATH = "organization/";
	public static final String REVOCATION_LIST_PATH = "revocation/";
	public static final String PUBLIC_KEY_JSON = "publicKey.json";
	public static final String ORGANIZATION_JSON = "organization.json";
	public static final String REVOCATION_LIST_JSON = "revocationList.json";
	public static final String WEB_SUFFIX = "/web";

	public static boolean isSvgFileName(String fileName) {
		String suffix = FileUtils.getFileSuffix(fileName);
		return isSvgFileSuffix(suffix);
	}

	public static boolean isSvgFileSuffix(String suffix) {
		return "svg".equalsIgnoreCase(suffix);
	}

	public static boolean isPngFileName(String fileName) {
		String suffix = FileUtils.getFileSuffix(fileName);
		return "png".equalsIgnoreCase(suffix);
	}

	public static String createAssertionUrl(String identifier) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + ASSERTION_PATH + identifier;
	}

	public static String createAssertionPublicUrl(String identifier) {
		return createAssertionUrl(identifier) + WEB_SUFFIX;
	}

	public static String createBadgeClassUrl(String identifier) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + CLASS_PATH + identifier;
	}

	public static String createImageUrl(String identifier) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + IMAGE_PATH + identifier;
	}

	public static String createCriteriaUrl(String identifier) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + CRITERIA_PATH + identifier;
	}

	public static String createIssuerUrl(String identifier) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + ISSUER_PATH + identifier;
	}
	
	public static String createPublicKeyUrl(String badgeClassUuid) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + KEY_PATH + badgeClassUuid + "/" + PUBLIC_KEY_JSON;
	}
	
	public static String createOrganizationUrl(String badgeClassUuid) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + ORGANIZATION_PATH + badgeClassUuid + "/" + ORGANIZATION_JSON;
	}
	
	public static String createRevocationListUrl(String badgeClassUuid) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + REVOCATION_LIST_PATH + badgeClassUuid + "/" + REVOCATION_LIST_JSON;
	}

	public static String createSalt(BadgeClass badgeClass) {
		return "badgeClass" + Math.abs(badgeClass.getUuid().hashCode());
	}

	public static String createBadgeClassFileName(String uuid, String sourceName) {
		return uuid + "." + FileUtils.getFileSuffix(sourceName);
	}

	public static String createIdentifier() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public static String getDefaultVersion() {
		return "1";
	}
	
	public static String incrementVersion(String oldVersionString) {
		try {
			int oldVersion = Integer.parseInt(oldVersionString);
			return Integer.toString(oldVersion + 1);
		} catch (Exception e) {
			return getDefaultVersion();
		}
	}
}
