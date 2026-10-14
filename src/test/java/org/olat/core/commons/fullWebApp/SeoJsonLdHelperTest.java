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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.Price;

/**
 *
 * Initial date: 14 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SeoJsonLdHelperTest {
	
	@Test
	public void plainTextShouldRemoveTagsAndDecodeEntities() {
		assertThat(SeoJsonLdHelper.plainText("<p>f&uuml;r  <b>alle</b></p>")).isEqualTo("für  alle");
		assertThat(SeoJsonLdHelper.plainText("  ")).isNull();
		assertThat(SeoJsonLdHelper.plainText(null)).isNull();
	}
	
	@Test
	public void firstPlainTextShouldFollowTheOrder() {
		assertThat(SeoJsonLdHelper.firstPlainText(null, " ", "<p>desc</p>", "title")).isEqualTo("desc");
		assertThat(SeoJsonLdHelper.firstPlainText(null, "", null, "title")).isEqualTo("title");
	}
	
	@Test
	public void courseModeShouldDependOnLocation() {
		assertThat(SeoJsonLdHelper.courseMode("Zürich")).isEqualTo("Onsite");
		assertThat(SeoJsonLdHelper.courseMode(" ")).isEqualTo("Online");
		assertThat(SeoJsonLdHelper.courseMode(null)).isEqualTo("Online");
	}
	
	@Test
	public void workloadDurationShouldParseHoursAndMinutes() {
		assertThat(SeoJsonLdHelper.workloadDuration("12h")).isEqualTo("PT12H");
		assertThat(SeoJsonLdHelper.workloadDuration("12 h")).isEqualTo("PT12H");
		assertThat(SeoJsonLdHelper.workloadDuration("2 Std")).isEqualTo("PT2H");
		assertThat(SeoJsonLdHelper.workloadDuration("12 Stunden")).isEqualTo("PT12H");
		assertThat(SeoJsonLdHelper.workloadDuration("3 hours")).isEqualTo("PT3H");
		assertThat(SeoJsonLdHelper.workloadDuration("90 min")).isEqualTo("PT1H30M");
		assertThat(SeoJsonLdHelper.workloadDuration("45 Minuten")).isEqualTo("PT45M");
		assertThat(SeoJsonLdHelper.workloadDuration("1.5h")).isEqualTo("PT1H30M");
		assertThat(SeoJsonLdHelper.workloadDuration("1,5 h")).isEqualTo("PT1H30M");
		assertThat(SeoJsonLdHelper.workloadDuration("ca. 2 Std")).isEqualTo("PT2H");
	}
	
	@Test
	public void workloadDurationShouldBeNullIfNotParsable() {
		assertThat(SeoJsonLdHelper.workloadDuration("ca. 2 Tage")).isNull();
		assertThat(SeoJsonLdHelper.workloadDuration("viel")).isNull();
		assertThat(SeoJsonLdHelper.workloadDuration("0 h")).isNull();
		assertThat(SeoJsonLdHelper.workloadDuration("")).isNull();
		assertThat(SeoJsonLdHelper.workloadDuration(null)).isNull();
	}
	
	@Test
	public void languageCodeShouldMatchKeyAndNames() {
		Map<String, Collection<String>> names = Map.of(
				"de", List.of("German", "Deutsch"),
				"de_CH", List.of("German (Switzerland)", "Deutsch (Schweiz)"),
				"en", List.of("English", "English"));
		assertThat(SeoJsonLdHelper.languageCode("Deutsch", names.keySet(), names::get)).isEqualTo("de");
		assertThat(SeoJsonLdHelper.languageCode(" german ", names.keySet(), names::get)).isEqualTo("de");
		assertThat(SeoJsonLdHelper.languageCode("EN", names.keySet(), names::get)).isEqualTo("en");
		assertThat(SeoJsonLdHelper.languageCode("de_CH", names.keySet(), names::get)).isEqualTo("de-CH");
		assertThat(SeoJsonLdHelper.languageCode("Deutsch (Schweiz)", names.keySet(), names::get)).isEqualTo("de-CH");
		assertThat(SeoJsonLdHelper.languageCode("Klingon", names.keySet(), names::get)).isNull();
		assertThat(SeoJsonLdHelper.languageCode(null, names.keySet(), names::get)).isNull();
	}
	
	@Test
	public void putOffersWithoutOffersShouldWriteNothing() {
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putOffers(jsonLd, List.of());
		SeoJsonLdHelper.putOffers(jsonLd, null);
		assertThat(jsonLd.has("offers")).isFalse();
		assertThat(jsonLd.has("isAccessibleForFree")).isFalse();
	}
	
	@Test
	public void putOffersWithPaidOfferShouldUseLowestPrice() {
		JSONObject jsonLd = new JSONObject();
		Offer expensive = offer("250", "CHF", false, false);
		Offer cheap = offer("120", "CHF", false, false);
		when(cheap.getValidFrom()).thenReturn(new Date(0));
		SeoJsonLdHelper.putOffers(jsonLd, List.of(expensive, cheap));
		
		JSONObject offers = jsonLd.getJSONObject("offers");
		assertThat(offers.getString("category")).isEqualTo("Paid");
		assertThat(offers.getBigDecimal("price")).isEqualByComparingTo("120");
		assertThat(offers.getString("priceCurrency")).isEqualTo("CHF");
		assertThat(offers.has("validFrom")).isTrue();
		assertThat(offers.has("validThrough")).isFalse();
		assertThat(jsonLd.getBoolean("isAccessibleForFree")).isFalse();
	}
	
	@Test
	public void putOffersWithMixedCurrenciesShouldUseFirstCurrency() {
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putOffers(jsonLd, List.of(offer("100", "EUR", false, false), offer("90", "CHF", false, false), offer("95", "CHF", false, false)));
		
		JSONObject offers = jsonLd.getJSONObject("offers");
		assertThat(offers.getBigDecimal("price")).isEqualByComparingTo("90");
		assertThat(offers.getString("priceCurrency")).isEqualTo("CHF");
	}
	
	@Test
	public void putOffersWithOpenAccessShouldBeFree() {
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putOffers(jsonLd, List.of(offer("250", "EUR", false, false), offer(null, null, true, false)));
		
		JSONObject offers = jsonLd.getJSONObject("offers");
		assertThat(offers.getString("category")).isEqualTo("Free");
		assertThat(offers.getInt("price")).isZero();
		assertThat(offers.has("priceCurrency")).isFalse();
		assertThat(jsonLd.getBoolean("isAccessibleForFree")).isTrue();
	}
	
	@Test
	public void putOffersWithGuestAccessOrEmptyPriceShouldBeFree() {
		JSONObject guest = new JSONObject();
		SeoJsonLdHelper.putOffers(guest, List.of(offer("10", "CHF", false, true)));
		assertThat(guest.getBoolean("isAccessibleForFree")).isTrue();
		
		JSONObject noPrice = new JSONObject();
		SeoJsonLdHelper.putOffers(noPrice, List.of(offer(null, null, false, false)));
		assertThat(noPrice.getBoolean("isAccessibleForFree")).isTrue();
		assertThat(noPrice.getJSONObject("offers").has("priceCurrency")).isFalse();
	}
	
	@Test
	public void putTaxonomyShouldDeduplicateAndSkipBlank() {
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putTaxonomy(jsonLd, Arrays.asList("Academy", " ", null, "Administration", "Academy"));
		
		assertThat(jsonLd.getJSONArray("about")).hasSize(2);
		assertThat(jsonLd.getJSONArray("about").getJSONObject(0).getString("@type")).isEqualTo("DefinedTerm");
		assertThat(jsonLd.getJSONArray("about").getJSONObject(1).getString("name")).isEqualTo("Administration");
		assertThat(jsonLd.getString("keywords")).isEqualTo("Academy, Administration");
	}
	
	@Test
	public void putTaxonomyWithoutLevelsShouldWriteNothing() {
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putTaxonomy(jsonLd, List.of());
		SeoJsonLdHelper.putTaxonomy(jsonLd, null);
		assertThat(jsonLd.has("about")).isFalse();
		assertThat(jsonLd.has("keywords")).isFalse();
	}
	
	@Test
	public void putIdentityShouldSetIdAndMainEntity() {
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putIdentity(jsonLd, "https://example.org/catalog/Offer/1");
		assertThat(jsonLd.getString("@id")).isEqualTo("https://example.org/catalog/Offer/1");
		assertThat(jsonLd.getString("mainEntityOfPage")).isEqualTo("https://example.org/catalog/Offer/1");
		
		JSONObject empty = new JSONObject();
		SeoJsonLdHelper.putIdentity(empty, null);
		assertThat(empty.has("@id")).isFalse();
	}
	
	@Test
	public void putCourseInstanceShouldWriteOneInstanceWithSubEventsPerActiveBlockSorted() {
		JSONObject jsonLd = new JSONObject();
		LectureBlock second = block("Second", "Room 2", 2_000_000_000L, LectureBlockStatus.active);
		LectureBlock first = block("First", null, 1_000_000_000L, LectureBlockStatus.done);
		LectureBlock cancelled = block("Cancelled", "Room 3", 1_500_000_000L, LectureBlockStatus.cancelled);
		LectureBlock noStart = block("No start", null, -1, LectureBlockStatus.active);
		SeoJsonLdHelper.putCourseInstance(jsonLd, List.of(second, cancelled, noStart, first),
				List.of("Anna Muster", "Anna Muster", "Ben Beispiel"), "Bern", null, null, "PT2H", null);
		
		JSONObject instance = jsonLd.getJSONObject("hasCourseInstance");
		assertThat(instance.getString("@type")).isEqualTo("CourseInstance");
		assertThat(instance.getString("courseMode")).isEqualTo("Onsite");
		assertThat(instance.getString("courseWorkload")).isEqualTo("PT2H");
		assertThat(instance.getJSONObject("location").getString("name")).isEqualTo("Bern");
		assertThat(instance.getJSONArray("instructor")).hasSize(2);
		assertThat(instance.getString("startDate")).matches("\\d{4}-\\d{2}-\\d{2}");
		assertThat(instance.getString("endDate")).matches("\\d{4}-\\d{2}-\\d{2}");
		assertThat(instance.getString("startDate")).isEqualTo(SeoJsonLdHelper.isoDate(new Date(1_000_000_000L)));
		assertThat(instance.getString("endDate")).isEqualTo(SeoJsonLdHelper.isoDate(new Date(2_000_000_000L + 3_600_000L)));
		
		assertThat(instance.getJSONArray("subEvent")).hasSize(2);
		JSONObject firstJson = instance.getJSONArray("subEvent").getJSONObject(0);
		assertThat(firstJson.getString("@type")).isEqualTo("Event");
		assertThat(firstJson.getString("name")).isEqualTo("First");
		assertThat(firstJson.getJSONObject("location").getString("name")).isEqualTo("Bern");
		assertThat(firstJson.getString("startDate")).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}");
		JSONObject secondJson = instance.getJSONArray("subEvent").getJSONObject(1);
		assertThat(secondJson.getJSONObject("location").getString("name")).isEqualTo("Room 2");
	}
	
	@Test
	public void putCourseInstanceSubEventsShouldFallBackToInstanceLocationThenVirtualLocation() {
		LectureBlock block = block("First", null, 1_000_000_000L, LectureBlockStatus.active);
		JSONObject withLocation = new JSONObject();
		SeoJsonLdHelper.putCourseInstance(withLocation, List.of(block), List.of(), "Bern", null, null, null, "https://example.org/offer");
		JSONObject place = withLocation.getJSONObject("hasCourseInstance").getJSONArray("subEvent").getJSONObject(0).getJSONObject("location");
		assertThat(place.getString("@type")).isEqualTo("Place");
		assertThat(place.getString("name")).isEqualTo("Bern");
		
		JSONObject withoutLocation = new JSONObject();
		SeoJsonLdHelper.putCourseInstance(withoutLocation, List.of(block), List.of(), null, null, null, null, "https://example.org/offer");
		JSONObject virtual = withoutLocation.getJSONObject("hasCourseInstance").getJSONArray("subEvent").getJSONObject(0).getJSONObject("location");
		assertThat(virtual.getString("@type")).isEqualTo("VirtualLocation");
		assertThat(virtual.getString("url")).isEqualTo("https://example.org/offer");
		assertThat(withoutLocation.getJSONObject("hasCourseInstance").getJSONArray("subEvent").getJSONObject(0).getString("eventAttendanceMode"))
				.isEqualTo("https://schema.org/OnlineEventAttendanceMode");
		assertThat(withLocation.getJSONObject("hasCourseInstance").getJSONArray("subEvent").getJSONObject(0).has("eventAttendanceMode")).isFalse();
	}
	
	@Test
	public void putCourseInstanceShouldPreferTheElementDates() {
		JSONObject jsonLd = new JSONObject();
		LectureBlock block = block("First", null, 1_000_000_000L, LectureBlockStatus.active);
		SeoJsonLdHelper.putCourseInstance(jsonLd, List.of(block), List.of(), null, new Date(0), null, null, null);
		
		JSONObject instance = jsonLd.getJSONObject("hasCourseInstance");
		assertThat(instance.getString("startDate")).isEqualTo(SeoJsonLdHelper.isoDate(new Date(0)));
		assertThat(instance.has("endDate")).isFalse();
		assertThat(instance.has("courseWorkload")).isFalse();
		assertThat(instance.getString("courseMode")).isEqualTo("Online");
	}
	
	@Test
	public void putCourseInstanceShouldCapTheSubEvents() {
		List<LectureBlock> blocks = new ArrayList<>();
		for (int i = 0; i < SeoJsonLdHelper.MAX_SUB_EVENTS + 10; i++) {
			blocks.add(block("Block " + i, null, 1_000_000_000L + i * 1000L, LectureBlockStatus.active));
		}
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putCourseInstance(jsonLd, blocks, List.of(), null, null, null, null, null);
		
		assertThat(jsonLd.getJSONObject("hasCourseInstance").getJSONArray("subEvent")).hasSize(SeoJsonLdHelper.MAX_SUB_EVENTS);
	}
	
	@Test
	public void putCourseInstanceWithoutBlocksShouldUseElementDates() {
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putCourseInstance(jsonLd, List.of(), List.of("Anna Muster"), "Bern", new Date(0), null, "PT2H", null);
		
		JSONObject instance = jsonLd.getJSONObject("hasCourseInstance");
		assertThat(instance.getString("startDate")).matches("\\d{4}-\\d{2}-\\d{2}");
		assertThat(instance.has("endDate")).isFalse();
		assertThat(instance.has("subEvent")).isFalse();
		assertThat(instance.getString("courseMode")).isEqualTo("Onsite");
		assertThat(instance.getString("courseWorkload")).isEqualTo("PT2H");
		assertThat(instance.getJSONArray("instructor")).hasSize(1);
	}
	
	@Test
	public void putCourseInstanceWithoutBlocksAndDatesShouldWriteNothing() {
		JSONObject jsonLd = new JSONObject();
		SeoJsonLdHelper.putCourseInstance(jsonLd, null, List.of(), null, null, null, null, null);
		assertThat(jsonLd.has("hasCourseInstance")).isFalse();
	}
	
	private Offer offer(String amount, String currency, boolean openAccess, boolean guestAccess) {
		Offer offer = mock(Offer.class);
		when(offer.isOpenAccess()).thenReturn(openAccess);
		when(offer.isGuestAccess()).thenReturn(guestAccess);
		if (amount != null) {
			Price price = mock(Price.class);
			when(price.getAmount()).thenReturn(new BigDecimal(amount));
			when(price.getCurrencyCode()).thenReturn(currency);
			when(offer.getPrice()).thenReturn(price);
		}
		return offer;
	}
	
	private LectureBlock block(String title, String location, long start, LectureBlockStatus status) {
		LectureBlock block = mock(LectureBlock.class);
		when(block.getTitle()).thenReturn(title);
		when(block.getLocation()).thenReturn(location);
		when(block.getStartDate()).thenReturn(start < 0 ? null : new Date(start));
		when(block.getEndDate()).thenReturn(start < 0 ? null : new Date(start + 3_600_000L));
		when(block.getStatus()).thenReturn(status);
		return block;
	}
}
