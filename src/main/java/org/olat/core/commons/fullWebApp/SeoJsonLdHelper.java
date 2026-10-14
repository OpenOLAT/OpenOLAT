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
package org.olat.core.commons.fullWebApp;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.Price;

/**
 * Spring-free helper with the schema.org/Course JSON-LD logic shared by the
 * repository entry and the curriculum element builders.
 *
 * Initial date: 14 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SeoJsonLdHelper {
	
	public static final int MAX_SUB_EVENTS = 50;
	
	private static final Pattern WORKLOAD_PATTERN = Pattern.compile(
			"^\\s*(?:ca\\.?\\s*)?(\\d+(?:[.,]\\d+)?)\\s*(h|hrs?|hours?|std\\.?|stunden?|min\\.?|minutes?|minuten?)\\s*$",
			Pattern.CASE_INSENSITIVE);
	
	private SeoJsonLdHelper() {
	}
	
	/**
	 * @param html The html text, may be null
	 * @return The text without tags and with decoded entities, null if blank
	 */
	public static String plainText(String html) {
		if (!StringHelper.containsNonWhitespace(html)) {
			return null;
		}
		String text = StringHelper.unescapeHtml(FilterFactory.getHtmlTagsFilter().filter(html)).trim();
		return StringHelper.containsNonWhitespace(text) ? text : null;
	}
	
	/**
	 * @return The first candidate with a plain text, null if there is none
	 */
	public static String firstPlainText(String... candidates) {
		for (String candidate : candidates) {
			String text = plainText(candidate);
			if (StringHelper.containsNonWhitespace(text)) {
				return text;
			}
		}
		return null;
	}
	
	public static void putIdentity(JSONObject jsonLd, String canonicalUrl) {
		if (StringHelper.containsNonWhitespace(canonicalUrl)) {
			jsonLd.put("@id", canonicalUrl);
			jsonLd.put("mainEntityOfPage", canonicalUrl);
		}
	}
	
	public static String courseMode(String location) {
		return StringHelper.containsNonWhitespace(location) ? "Onsite" : "Online";
	}
	
	/**
	 * @param expenditureOfWork The free text, e.g. "12 h", "90 min", "1.5 Std"
	 * @return The ISO 8601 duration, null if the text does not parse
	 */
	public static String workloadDuration(String expenditureOfWork) {
		if (!StringHelper.containsNonWhitespace(expenditureOfWork)) {
			return null;
		}
		Matcher matcher = WORKLOAD_PATTERN.matcher(expenditureOfWork);
		if (!matcher.matches()) {
			return null;
		}
		boolean minutes = matcher.group(2).toLowerCase().startsWith("m");
		long total = new BigDecimal(matcher.group(1).replace(',', '.'))
				.multiply(BigDecimal.valueOf(minutes ? 1 : 60)).longValue();
		if (total <= 0) {
			return null;
		}
		return "PT" + (total / 60 > 0 ? total / 60 + "H" : "") + (total % 60 > 0 ? total % 60 + "M" : "");
	}
	
	/**
	 * @param mainLanguage The free text of the main language
	 * @param keys The language keys
	 * @param namesOf The names of a language key, only asked if no key matches
	 * @return The language as BCP 47 tag (de_CH becomes de-CH), null if no language matches
	 */
	public static String languageCode(String mainLanguage, Collection<String> keys, Function<String, Collection<String>> namesOf) {
		if (!StringHelper.containsNonWhitespace(mainLanguage)) {
			return null;
		}
		String language = mainLanguage.trim();
		List<String> sortedKeys = keys.stream().sorted().toList();
		return sortedKeys.stream().filter(language::equalsIgnoreCase).findFirst()
				.or(() -> sortedKeys.stream().filter(key -> namesOf.apply(key).stream().anyMatch(language::equalsIgnoreCase)).findFirst())
				.map(key -> key.replace('_', '-'))
				.orElse(null);
	}
	
	/**
	 * Free (open access, guest access or no price) wins over paid and has no
	 * currency. Paid uses the lowest price of the first currency in alphabetical
	 * order. Nothing is written without offers.
	 */
	public static void putOffers(JSONObject jsonLd, List<Offer> offers) {
		if (offers == null || offers.isEmpty()) {
			return;
		}
		Optional<Offer> lowest = offers.stream()
				.filter(offer -> hasPrice(offer))
				.min(Comparator.comparing((Offer offer) -> StringUtils.defaultString(offer.getPrice().getCurrencyCode()))
						.thenComparing(offer -> offer.getPrice().getAmount()));
		boolean free = offers.stream().anyMatch(offer -> offer.isOpenAccess() || offer.isGuestAccess() || !hasPrice(offer));
		
		JSONObject offerJson = new JSONObject();
		offerJson.put("@type", "Offer");
		offerJson.put("category", free ? "Free" : "Paid");
		if (free) {
			offerJson.put("price", 0);
		} else {
			offerJson.put("price", lowest.get().getPrice().getAmount());
			offerJson.put("validFrom", isoDate(lowest.get().getValidFrom()));
			offerJson.put("validThrough", isoDate(lowest.get().getValidTo()));
			lowest.map(offer -> offer.getPrice().getCurrencyCode())
					.filter(StringHelper::containsNonWhitespace)
					.ifPresent(currency -> offerJson.put("priceCurrency", currency));
		}
		jsonLd.put("offers", offerJson);
		jsonLd.put("isAccessibleForFree", free);
	}
	
	private static boolean hasPrice(Offer offer) {
		Price price = offer.getPrice();
		return price != null && !price.isEmpty() && price.getAmount() != null;
	}
	
	public static void putTaxonomy(JSONObject jsonLd, List<String> levelNames) {
		if (levelNames == null) {
			return;
		}
		LinkedHashSet<String> names = new LinkedHashSet<>();
		levelNames.stream().filter(StringHelper::containsNonWhitespace).map(String::trim).forEach(names::add);
		if (names.isEmpty()) {
			return;
		}
		JSONArray about = new JSONArray();
		names.forEach(name -> about.put(new JSONObject().put("@type", "DefinedTerm").put("name", name)));
		jsonLd.put("about", about);
		jsonLd.put("keywords", String.join(", ", names));
	}
	
	/**
	 * One course instance for the whole run. Its dates are the fallback dates, or
	 * the first start and the last end of the lecture blocks if there are none. The
	 * lecture blocks are sub events of the instance. Cancelled blocks and blocks
	 * without start are skipped. A sub event without location gets the location of
	 * the instance, or a virtual location with the page URL if there is none.
	 * Nothing is written without dates and blocks.
	 */
	public static void putCourseInstance(JSONObject jsonLd, List<LectureBlock> lectureBlocks, List<String> instructors,
			String location, Date begin, Date end, String workload, String url) {
		List<LectureBlock> blocks = lectureBlocks == null ? List.of() : lectureBlocks.stream()
				.filter(block -> block.getStartDate() != null && block.getStatus() != LectureBlockStatus.cancelled)
				.sorted(Comparator.comparing(LectureBlock::getStartDate))
				.toList();
		if (blocks.isEmpty() && begin == null && end == null) {
			return;
		}
		JSONObject instance = newInstance(location, instructors, workload);
		Date instanceBegin = begin != null || end != null ? begin : blocks.get(0).getStartDate();
		Date instanceEnd = begin != null || end != null ? end : blocks.stream()
				.map(block -> block.getEndDate() != null ? block.getEndDate() : block.getStartDate())
				.max(Comparator.naturalOrder()).orElse(null);
		instance.put("startDate", isoDate(instanceBegin));
		instance.put("endDate", isoDate(instanceEnd));
		JSONArray subEvents = new JSONArray();
		for (LectureBlock block : blocks.stream().limit(MAX_SUB_EVENTS).toList()) {
			JSONObject subEvent = new JSONObject();
			subEvent.put("@type", "Event");
			subEvent.put("name", block.getTitle());
			subEvent.put("startDate", isoDateTime(block.getStartDate()));
			subEvent.put("endDate", isoDateTime(block.getEndDate()));
			String subEventLocation = StringHelper.containsNonWhitespace(block.getLocation()) ? block.getLocation() : location;
			if (StringHelper.containsNonWhitespace(subEventLocation)) {
				subEvent.put("location", new JSONObject().put("@type", "Place").put("name", subEventLocation));
			} else if (StringHelper.containsNonWhitespace(url)) {
				subEvent.put("location", new JSONObject().put("@type", "VirtualLocation").put("url", url));
				subEvent.put("eventAttendanceMode", "https://schema.org/OnlineEventAttendanceMode");
			}
			subEvents.put(subEvent);
		}
		if (!subEvents.isEmpty()) {
			instance.put("subEvent", subEvents);
		}
		jsonLd.put("hasCourseInstance", instance);
	}
	
	private static JSONObject newInstance(String location, List<String> instructors, String workload) {
		JSONObject instance = new JSONObject();
		instance.put("@type", "CourseInstance");
		instance.put("courseMode", courseMode(location));
		if (StringHelper.containsNonWhitespace(location)) {
			instance.put("location", new JSONObject().put("@type", "Place").put("name", location));
		}
		if (StringHelper.containsNonWhitespace(workload)) {
			instance.put("courseWorkload", workload);
		}
		if (instructors != null && !instructors.isEmpty()) {
			JSONArray persons = new JSONArray();
			instructors.stream().filter(StringHelper::containsNonWhitespace).distinct()
					.forEach(name -> persons.put(new JSONObject().put("@type", "Person").put("name", name)));
			if (!persons.isEmpty()) {
				instance.put("instructor", persons);
			}
		}
		return instance;
	}
	
	public static String isoDate(Date date) {
		return date == null ? null : new SimpleDateFormat("yyyy-MM-dd").format(date);
	}
	
	public static String isoDateTime(Date date) {
		return date == null ? null : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(date);
	}
}
