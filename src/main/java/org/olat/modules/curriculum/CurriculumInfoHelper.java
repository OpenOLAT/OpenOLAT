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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONObject;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.SeoJsonLdHelper;
import org.olat.core.commons.fullWebApp.SeoMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.catalog.WebCatalogDispatcher;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ResourceInfoHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.user.UserManager;

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
	 * @param curriculumService Service for teaser image lookups
	 * @param lectureBlocks The lecture blocks of the element, as shown on the page
	 * @param taughtBys The persons of the "Taught by" section, as shown on the page
	 */
	public static void populateSeoMetadata(SeoMetadata seo, CurriculumElement element,
			RepositoryEntry singleCourseEntry, String canonicalUrl, Locale locale,
			Translator taxonomyTranslator, RepositoryService repositoryService,
			CurriculumService curriculumService, List<LectureBlock> lectureBlocks, Set<Identity> taughtBys) {
		seo.resetMetadata();
		String metaDesc = ResourceInfoHelper.buildMetaDescription(element, locale, taxonomyTranslator);
		if (StringHelper.containsNonWhitespace(metaDesc)) {
			seo.setMetaDescription(metaDesc);
		}
		if (StringHelper.containsNonWhitespace(canonicalUrl)) {
			seo.setCanonicalUrl(canonicalUrl);
			VFSLeaf teaserImage = curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserImage);
			String ogImageUrl = WebCatalogDispatcher.getImageUrl(teaserImage, element.getResource().getKey());
			if (ogImageUrl == null && singleCourseEntry != null) {
				ogImageUrl = ResourceInfoHelper.buildOgImageUrl(singleCourseEntry, repositoryService);
			}
			if (ogImageUrl != null) {
				seo.setOgImageUrl(ogImageUrl);
			}
			List<Offer> offers = CoreSpringFactory.getImpl(ACService.class).getOffers(element, true, true, null, false, Boolean.TRUE, null);
			UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
			List<String> instructors = taughtBys.stream().map(userManager::getUserDisplayName).sorted().toList();
			List<TaxonomyLevel> levels = element.getTaxonomyLevels().stream()
					.map(CurriculumElementToTaxonomyLevel::getTaxonomyLevel)
					.toList();
			String jsonLd = buildCourseJsonLd(element, canonicalUrl, ogImageUrl, metaDesc,
					ResourceInfoHelper.translateTaxonomyLevels(taxonomyTranslator, levels), offers,
					element.isShowLectures() && CoreSpringFactory.getImpl(LectureModule.class).isEnabled() ? lectureBlocks : List.of(), instructors);
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
	 * @param metaDescription The meta description, used if the element has no teaser and no description
	 * @param taxonomyNames The translated taxonomy level names
	 * @param offers The web published offers
	 * @param lectureBlocks The lecture blocks to publish, empty if the page shows none
	 * @param instructors The names of the taught by persons
	 * @return JSON-LD string, or null if element has no displayname
	 */
	public static String buildCourseJsonLd(CurriculumElement element, String url, String imageUrl,
			String metaDescription, List<String> taxonomyNames, List<Offer> offers,
			List<LectureBlock> lectureBlocks, List<String> instructors) {
		if (element == null || !StringHelper.containsNonWhitespace(element.getDisplayName())) {
			return null;
		}
		JSONObject jsonLd = new JSONObject();
		jsonLd.put("@context", "https://schema.org");
		jsonLd.put("@type", "Course");
		jsonLd.put("name", element.getDisplayName());

		jsonLd.put("description", SeoJsonLdHelper.firstPlainText(element.getTeaser(), element.getDescription(),
				metaDescription, element.getDisplayName()));

		if (StringHelper.containsNonWhitespace(url)) {
			jsonLd.put("url", url);
		}
		SeoJsonLdHelper.putIdentity(jsonLd, url);
		if (StringHelper.containsNonWhitespace(imageUrl)) {
			jsonLd.put("image", imageUrl);
		}
		String languageCode = ResourceInfoHelper.languageCode(element.getMainLanguage());
		if (StringHelper.containsNonWhitespace(languageCode)) {
			jsonLd.put("inLanguage", languageCode);
		}
		SeoJsonLdHelper.putTaxonomy(jsonLd, taxonomyNames);

		if (element.getCreationDate() != null) {
			jsonLd.put("datePublished", SeoJsonLdHelper.isoDate(element.getCreationDate()));
		}
		if (element.getLastModified() != null) {
			jsonLd.put("dateModified", SeoJsonLdHelper.isoDate(element.getLastModified()));
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

		SeoJsonLdHelper.putOffers(jsonLd, offers);
		SeoJsonLdHelper.putCourseInstance(jsonLd, lectureBlocks, instructors, element.getLocation(),
				element.getBeginDate(), element.getEndDate(), SeoJsonLdHelper.workloadDuration(element.getExpenditureOfWork()), url);

		return jsonLd.toString();
	}
	
	/**
	 * @return The persons of the "Taught by" section of the page
	 */
	public static Set<Identity> loadTaughtBys(CurriculumElement element, List<LectureBlock> lectureBlocks,
			CurriculumService curriculumService, LectureService lectureService) {
		Set<Identity> taughtBys = new HashSet<>();
		
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElementsDescendants(element);
		curriculumElements.add(element);
		List<CurriculumRoles> roles = new ArrayList<>(2);
		if (element.getTaughtBys().contains(TaughtBy.coaches)) {
			roles.add(CurriculumRoles.coach);
		}
		if (element.getTaughtBys().contains(TaughtBy.owners)) {
			roles.add(CurriculumRoles.owner);
		}
		if (!roles.isEmpty()) {
			SearchMemberParameters searchParams = new SearchMemberParameters(curriculumElements);
			searchParams.setRoles(roles);
			curriculumService.getCurriculumElementsMembers(searchParams)
					.stream()
					.map(CurriculumMember::getIdentity)
					.forEach(taughtBys::add);
		}
		
		if (element.getTaughtBys().contains(TaughtBy.teachers) && lectureBlocks != null && !lectureBlocks.isEmpty()
				&& CoreSpringFactory.getImpl(LectureModule.class).isEnabled()) {
			taughtBys.addAll(lectureService.getTeachers(lectureBlocks));
		}
		return taughtBys;
	}
}
