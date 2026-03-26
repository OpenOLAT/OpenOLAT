/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONObject;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.SeoMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ResourceInfoHelper;

/**
 * Helper to build SEO metadata for curriculum elements. Mirrors
 * {@link ResourceInfoHelper} for repository entries.
 *
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class CurriculumInfoHelper {

	private CurriculumInfoHelper() {
		// static utility
	}

	/**
	 * Populate all SEO metadata for a curriculum element: meta description,
	 * and — when a canonical URL is provided — canonical link, OG image,
	 * and JSON-LD structured data.
	 *
	 * @param seo The SeoMetadata object to populate
	 * @param element The curriculum element
	 * @param singleCourseEntry The single-course repository entry (may be null)
	 * @param canonicalUrl The canonical URL (null to skip canonical/OG/JSON-LD)
	 * @param locale The locale for date formatting
	 * @param taxonomyTranslator Translator for taxonomy display names
	 * @param repositoryService Service for image lookups
	 */
	public static void populateSeoMetadata(SeoMetadata seo, CurriculumElement element,
			RepositoryEntry singleCourseEntry, String canonicalUrl, Locale locale,
			Translator taxonomyTranslator, RepositoryService repositoryService) {
		seo.resetMetadata();
		String metaDesc = ResourceInfoHelper.buildMetaDescription(element, locale, taxonomyTranslator);
		if (StringHelper.containsNonWhitespace(metaDesc)) {
			seo.setMetaDescription(metaDesc);
		}
		if (StringHelper.containsNonWhitespace(canonicalUrl)) {
			seo.setCanonicalUrl(canonicalUrl);
			String ogImageUrl = null;
			if (singleCourseEntry != null) {
				ogImageUrl = ResourceInfoHelper.buildOgImageUrl(singleCourseEntry, repositoryService);
				if (ogImageUrl != null) {
					seo.setOgImageUrl(ogImageUrl);
				}
			}
			String jsonLd = buildCourseJsonLd(element, canonicalUrl, ogImageUrl, locale);
			if (jsonLd != null) {
				seo.setStructuredDataJson(jsonLd);
			}
		}
	}

	/**
	 * Build a schema.org/Course JSON-LD object for a curriculum element.
	 *
	 * @param element The curriculum element
	 * @param url The canonical URL
	 * @param imageUrl The OG image URL (may be null)
	 * @param locale The locale for formatting
	 * @return JSON-LD string, or null if element has no displayname
	 */
	public static String buildCourseJsonLd(CurriculumElement element, String url,
			String imageUrl, Locale locale) {
		if (element == null || !StringHelper.containsNonWhitespace(element.getDisplayName())) {
			return null;
		}
		JSONObject jsonLd = new JSONObject();
		jsonLd.put("@context", "https://schema.org");
		jsonLd.put("@type", "Course");
		jsonLd.put("name", element.getDisplayName());

		String description = element.getTeaser();
		if (!StringHelper.containsNonWhitespace(description)) {
			description = element.getDescription();
		}
		if (StringHelper.containsNonWhitespace(description)) {
			jsonLd.put("description", FilterFactory.getHtmlTagsFilter().filter(description).trim());
		}

		if (StringHelper.containsNonWhitespace(url)) {
			jsonLd.put("url", url);
		}
		if (StringHelper.containsNonWhitespace(imageUrl)) {
			jsonLd.put("image", imageUrl);
		}
		if (StringHelper.containsNonWhitespace(element.getMainLanguage())) {
			jsonLd.put("inLanguage", element.getMainLanguage());
		}

		SimpleDateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd");
		if (element.getLastModified() != null) {
			jsonLd.put("dateModified", isoDate.format(element.getLastModified()));
		}

		if (StringHelper.containsNonWhitespace(element.getAuthors())) {
			JSONObject author = new JSONObject();
			author.put("@type", "Person");
			author.put("name", element.getAuthors());
			jsonLd.put("author", author);
		}

		JSONObject provider = new JSONObject();
		provider.put("@type", "Organization");
		String organisationName = null;
		OAIPmhModule oaiPmhModule = CoreSpringFactory.getImpl(OAIPmhModule.class);
		if (StringHelper.containsNonWhitespace(oaiPmhModule.getSeoOrganisationName())) {
			organisationName = oaiPmhModule.getSeoOrganisationName();
		} else {
			Organisation defaultOrg = CoreSpringFactory.getImpl(OrganisationService.class).getDefaultOrganisation();
			organisationName = defaultOrg.getDisplayName();
		}
		provider.put("name", organisationName);
		provider.put("url", Settings.getServerContextPathURI());
		jsonLd.put("provider", provider);

		JSONObject offers = new JSONObject();
		offers.put("@type", "Offer");
		offers.put("category", "Free");
		offers.put("isAccessibleForFree", true);
		offers.put("price", 0);
		offers.put("priceCurrency", "CHF");
		jsonLd.put("offers", offers);

		if (element.getBeginDate() != null || element.getEndDate() != null) {
			JSONObject instance = new JSONObject();
			instance.put("@type", "CourseInstance");
			instance.put("courseMode", "Online");
			if (element.getBeginDate() != null) {
				instance.put("startDate", isoDate.format(element.getBeginDate()));
			}
			if (element.getEndDate() != null) {
				instance.put("endDate", isoDate.format(element.getEndDate()));
			}
			jsonLd.put("hasCourseInstance", instance);
		}

		return jsonLd.toString();
	}
}
