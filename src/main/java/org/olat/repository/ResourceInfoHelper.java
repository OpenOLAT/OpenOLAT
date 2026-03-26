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
package org.olat.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.SeoMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Organisation;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * Helper to build SEO meta descriptions for repository entries and
 * curriculum elements. Used by dispatchers and catalog controllers
 * to produce consistent meta description tags.
 *
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ResourceInfoHelper {

	private ResourceInfoHelper() {
		// static utility
	}

	/**
	 * Build a meta description for a repository entry.
	 * Format: "Teaser/Description | Date range | Taxonomy level(s)"
	 *
	 * @param entry The repository entry
	 * @param locale The locale for date formatting
	 * @param taxonomyTranslator Translator capable of resolving taxonomy display names
	 * @param taxonomyLevels The taxonomy levels for this entry (from RepositoryService.getTaxonomy())
	 * @return The meta description string, never null
	 */
	public static String buildMetaDescription(RepositoryEntry entry, Locale locale,
			Translator taxonomyTranslator, List<TaxonomyLevel> taxonomyLevels) {
		StringBuilder sb = new StringBuilder();
		// Teaser or description
		appendText(sb, entry.getTeaser(), entry.getDescription());
		// Lifecycle dates
		RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
		if (lifecycle != null) {
			Formatter formatter = Formatter.getInstance(locale);
			if (lifecycle.isPrivateCycle()) {
				appendDates(sb, formatter, lifecycle.getValidFrom(), lifecycle.getValidTo());
			} else if (StringHelper.containsNonWhitespace(lifecycle.getLabel())) {
				appendSeparator(sb);
				sb.append(lifecycle.getLabel());
			}
		}
		// Taxonomy levels
		appendTaxonomyLevels(sb, taxonomyTranslator, taxonomyLevels);
		return sb.toString();
	}

	/**
	 * Build a meta description for a curriculum element.
	 * Format: "Teaser/Description | Date range | Taxonomy level(s)"
	 *
	 * @param curriculumElement The curriculum element
	 * @param locale The locale for date formatting
	 * @param taxonomyTranslator Translator capable of resolving taxonomy display names
	 * @return The meta description string, never null
	 */
	public static String buildMetaDescription(CurriculumElement curriculumElement, Locale locale,
			Translator taxonomyTranslator) {
		StringBuilder sb = new StringBuilder();
		// Teaser or description
		appendText(sb, curriculumElement.getTeaser(), curriculumElement.getDescription());
		// Dates
		Formatter formatter = Formatter.getInstance(locale);
		appendDates(sb, formatter, curriculumElement.getBeginDate(), curriculumElement.getEndDate());
		// Taxonomy levels
		List<TaxonomyLevel> levels = curriculumElement.getTaxonomyLevels().stream()
				.map(CurriculumElementToTaxonomyLevel::getTaxonomyLevel)
				.toList();
		appendTaxonomyLevels(sb, taxonomyTranslator, levels);
		return sb.toString();
	}

	/**
	 * Populate all SEO metadata for a repository entry: meta description,
	 * and — when a canonical URL is provided — canonical link, OG image,
	 * and JSON-LD structured data.
	 *
	 * @param seo The SeoMetadata object to populate
	 * @param entry The repository entry
	 * @param canonicalUrl The canonical URL (null to skip canonical/OG/JSON-LD)
	 * @param locale The locale for date formatting
	 * @param taxonomyTranslator Translator for taxonomy display names
	 * @param repositoryService Service for taxonomy and image lookups
	 */
	public static void populateEntrySeoMetadata(SeoMetadata seo, RepositoryEntry entry,
			String canonicalUrl, Locale locale, Translator taxonomyTranslator,
			RepositoryService repositoryService) {
		seo.resetMetadata();
		List<TaxonomyLevel> levels = repositoryService.getTaxonomy(entry);
		String metaDesc = buildMetaDescription(entry, locale, taxonomyTranslator, levels);
		if (StringHelper.containsNonWhitespace(metaDesc)) {
			seo.setMetaDescription(metaDesc);
		}
		if (StringHelper.containsNonWhitespace(canonicalUrl)) {
			seo.setCanonicalUrl(canonicalUrl);
			String ogImageUrl = buildOgImageUrl(entry, repositoryService);
			if (ogImageUrl != null) {
				seo.setOgImageUrl(ogImageUrl);
			}
			String jsonLd = buildCourseJsonLd(entry, canonicalUrl, ogImageUrl, locale);
			if (jsonLd != null) {
				seo.setStructuredDataJson(jsonLd);
			}
		}
	}

	/**
	 * Build a stable OG image URL for a repository entry's teaser image,
	 * served via the /resourceinfo/ path.
	 *
	 * @param entry The repository entry
	 * @param repositoryService Service for image lookup
	 * @return The image URL, or null if no image available
	 */
	public static String buildOgImageUrl(RepositoryEntry entry,
			RepositoryService repositoryService) {
		VFSLeaf image = repositoryService.getIntroductionImage(entry);
		if (image != null) {
			String ext = image.getName().substring(image.getName().lastIndexOf('.') + 1);
			return ResourceInfoDispatcher.getUrl(entry.getKey() + "." + ext);
		}
		return null;
	}

	/**
	 * Build a schema.org/Course JSON-LD object for a repository entry.
	 *
	 * @param entry The repository entry
	 * @param url The canonical URL
	 * @param imageUrl The OG image URL (may be null)
	 * @param locale The locale for formatting
	 * @return JSON-LD string, or null if entry has no displayname
	 */
	public static String buildCourseJsonLd(RepositoryEntry entry, String url,
			String imageUrl, Locale locale) {
		if (entry == null || !StringHelper.containsNonWhitespace(entry.getDisplayname())) {
			return null;
		}
		JSONObject jsonLd = new JSONObject();
		jsonLd.put("@context", "https://schema.org");
		jsonLd.put("@type", "Course");
		jsonLd.put("name", entry.getDisplayname());

		String description = entry.getTeaser();
		if (!StringHelper.containsNonWhitespace(description)) {
			description = entry.getDescription();
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
		if (StringHelper.containsNonWhitespace(entry.getMainLanguage())) {
			jsonLd.put("inLanguage", entry.getMainLanguage());
		}

		SimpleDateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd");
		if (entry.getStatusPublishedDate() != null) {
			jsonLd.put("datePublished", isoDate.format(entry.getStatusPublishedDate()));
		}
		if (entry.getLastModified() != null) {
			jsonLd.put("dateModified", isoDate.format(entry.getLastModified()));
		}

		if (StringHelper.containsNonWhitespace(entry.getAuthors())) {
			JSONObject author = new JSONObject();
			author.put("@type", "Person");
			author.put("name", entry.getAuthors());
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

		RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
		if (lifecycle != null) {
			JSONObject instance = new JSONObject();
			instance.put("@type", "CourseInstance");
			instance.put("courseMode", "Online");
			if (lifecycle.isPrivateCycle()) {
				if (lifecycle.getValidFrom() != null) {
					instance.put("startDate", isoDate.format(lifecycle.getValidFrom()));
				}
				if (lifecycle.getValidTo() != null) {
					instance.put("endDate", isoDate.format(lifecycle.getValidTo()));
				}
				if (lifecycle.getValidFrom() != null || lifecycle.getValidTo() != null) {
					jsonLd.put("hasCourseInstance", instance);
				}
			} else if (StringHelper.containsNonWhitespace(lifecycle.getLabel())) {
				instance.put("name", lifecycle.getLabel());
				jsonLd.put("hasCourseInstance", instance);
			}
		}

		return jsonLd.toString();
	}

	private static void appendText(StringBuilder sb, String teaser, String description) {
		String text = teaser;
		if (!StringHelper.containsNonWhitespace(text)) {
			text = description;
		}
		if (StringHelper.containsNonWhitespace(text)) {
			sb.append(FilterFactory.getHtmlTagsFilter().filter(text).trim());
		}
	}

	private static void appendDates(StringBuilder sb, Formatter formatter, Date from, Date to) {
		if (from != null || to != null) {
			appendSeparator(sb);
			if (from != null && to != null) {
				sb.append(formatter.formatDate(from)).append(" - ").append(formatter.formatDate(to));
			} else if (from != null) {
				sb.append(formatter.formatDate(from));
			} else {
				sb.append(formatter.formatDate(to));
			}
		}
	}

	private static void appendTaxonomyLevels(StringBuilder sb, Translator translator, List<TaxonomyLevel> levels) {
		if (levels != null && !levels.isEmpty()) {
			for (TaxonomyLevel level : levels) {
				String name = TaxonomyUIFactory.translateDisplayName(translator, level);
				if (StringHelper.containsNonWhitespace(name)) {
					appendSeparator(sb);
					sb.append(name);
				}
			}
		}
	}

	private static void appendSeparator(StringBuilder sb) {
		if (!sb.isEmpty()) {
			sb.append(" | ");
		}
	}
}
