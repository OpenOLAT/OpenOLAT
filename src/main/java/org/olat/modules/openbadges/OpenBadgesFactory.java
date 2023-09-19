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

import org.olat.core.helpers.Settings;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.openbadges.criteria.BadgeCriteria;

/**
 * Initial date: 2023-06-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesFactory {

	public static final String BADGE_PATH = "badge/";
	public static final String ASSERTION_PATH = "assertion/";
	public static final String CLASS_PATH = "class/";
	public static final String BAKED_PATH = "baked/";
	public static final String CRITERIA_PATH = "criteria/";
	public static final String ISSUER_PATH = "issuer/";
	public static final String IMAGE_PATH = "image/";
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

	public static String createAssertionVerifyUrl(String identifier) {
		return Settings.getServerContextPathURI() + "/" + BADGE_PATH + ASSERTION_PATH + identifier;
	}

	public static String createAssertionPublicUrl(String identifier) {
		return createAssertionVerifyUrl(identifier) + WEB_SUFFIX;
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

	public static String getNameScanned(BadgeClass badgeClass) {
		return StringHelper.xssScan(badgeClass.getName());
	}

	public static String getDescriptionScanned(BadgeClass badgeClass) {
		return StringHelper.xssScan(badgeClass.getDescription());
	}

	public static String getDescriptionScanned(BadgeCriteria badgeCriteria) {
		return StringHelper.xssScan(badgeCriteria.getDescription());
	}

	public static String getVersionScanned(BadgeClass badgeClass) {
		return StringHelper.xssScan(badgeClass.getVersion());
	}
}
