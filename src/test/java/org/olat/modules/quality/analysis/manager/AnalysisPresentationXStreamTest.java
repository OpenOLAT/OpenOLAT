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
package org.olat.modules.quality.analysis.manager;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.RepositoryEntryRefImpl;


/**
 * 
 * Initial date: 01.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisPresentationXStreamTest {
	
	@Test
	public void shouldSerializeMultiGroupBy() {
		GroupBy groupBy1 = GroupBy.CONTEXT_CURRICULUM;
		GroupBy groupBy2 = GroupBy.CONTEXT_CURRICULUM_ELEMENT;
		GroupBy groupBy3 = GroupBy.CONTEXT_LOCATION;
		MultiGroupBy multiGroupBy = MultiGroupBy.of(groupBy1, groupBy2, groupBy3);
		
		String xml = AnalysisPresentationXStream.toXml(multiGroupBy);
		MultiGroupBy groupByFromXml = AnalysisPresentationXStream.fromXml(xml, MultiGroupBy.class);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(groupByFromXml.getGroupBy1()).isEqualTo(groupBy1);
		softly.assertThat(groupByFromXml.getGroupBy2()).isEqualTo(groupBy2);
		softly.assertThat(groupByFromXml.getGroupBy3()).isEqualTo(groupBy3);
		softly.assertAll();
	}

	@Test
	public void shouldSerializeSearchParamSimpleValues() {
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		RepositoryEntryRef formEntryRef = new RepositoryEntryRefImpl(6l);
		Date dateRangeFrom = new Date();
		Date dateRangeTo = new Date();
		boolean withUserInfosOnly = true;
		searchParams.setFormEntryRef(formEntryRef);
		searchParams.setDateRangeFrom(dateRangeFrom);
		searchParams.setDateRangeTo(dateRangeTo);
		searchParams.setWithUserInfosOnly(withUserInfosOnly);

		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(searchParamsFromXml.getFormEntryRef().getKey()).isEqualTo(formEntryRef.getKey());
		softly.assertThat(searchParamsFromXml.getDateRangeFrom()).isEqualTo(dateRangeFrom);
		softly.assertThat(searchParamsFromXml.getDateRangeTo()).isEqualTo(dateRangeTo);
		softly.assertThat(searchParams.isWithUserInfosOnly()).isEqualTo(withUserInfosOnly);
		softly.assertAll();
	}

	@Test
	public void shouldSerializeSearchParamTopicIdentityRefs() {
		IdentityRef ref1 = new IdentityRefImpl(1l);
		IdentityRef ref2 = new IdentityRefImpl(2l);
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		Collection<IdentityRef> refs = asList(ref1, ref2);
		searchParams.setTopicIdentityRefs(refs);

		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getTopicIdentityRefs()).extracting(IdentityRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}

	@Test
	public void shouldSerializeSearchParamTopicOrganisations() {
		OrganisationRef ref1 = new OrganisationRefImpl(8l);
		OrganisationRef ref2 = new OrganisationRefImpl(8l);
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<? extends OrganisationRef> topicOrganisationRefs = asList(ref1, ref2);
		searchParams.setTopicOrganisationRefs(topicOrganisationRefs);
		
		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getTopicOrganisationRefs()).extracting(OrganisationRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}

	@Test
	public void shouldSerializeSearchParamTopicCurriculums() {
		CurriculumRef ref1 = new CurriculumRefImpl(8l);
		CurriculumRef ref2 = new CurriculumRefImpl(8l);
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<? extends CurriculumRef> topicCurriculumRefs = asList(ref1, ref2);
		searchParams.setTopicCurriculumRefs(topicCurriculumRefs);
		
		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getTopicCurriculumRefs()).extracting(CurriculumRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}

	@Test
	public void shouldSerializeSearchParamTopicCurriculumElements() {
		CurriculumElementRef ref1 = new CurriculumElementRefImpl(8l);
		CurriculumElementRef ref2 = new CurriculumElementRefImpl(8l);
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<? extends CurriculumElementRef> topicCurriculumElementRefs = asList(ref1, ref2);
		searchParams.setTopicCurriculumElementRefs(topicCurriculumElementRefs);
	
		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getTopicCurriculumElementRefs()).extracting(CurriculumElementRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}

	@Test
	public void shouldSerializeSearchParamTopicRepositoryEntrys() {
		RepositoryEntryRef ref1 = new RepositoryEntryRefImpl(8l);
		RepositoryEntryRef ref2 = new RepositoryEntryRefImpl(8l);
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<? extends RepositoryEntryRef> topicRepositoryEntryRefs = asList(ref1, ref2);
		searchParams.setTopicRepositoryRefs(topicRepositoryEntryRefs);
		
		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getTopicRepositoryRefs()).extracting(RepositoryEntryRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}

	@Test
	public void shouldSerializeSearchParamContextLocations() {
		String loc1 = "l1";
		String loc2 = "l2";

		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		Collection<String> contextLocations = asList(loc1, loc2);
		searchParams.setContextLocations(contextLocations);

		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getContextLocations()).containsExactlyInAnyOrder(loc1, loc2);
	}

	@Test
	public void shouldSerializeSearchParamContextOrganisations() {
		OrganisationRef ref1 = new OrganisationRefImpl(8l);
		OrganisationRef ref2 = new OrganisationRefImpl(8l);
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<? extends OrganisationRef> topicOrganisationRefs = asList(ref1, ref2);
		searchParams.setContextOrganisationRefs(topicOrganisationRefs);
		
		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getContextOrganisationRefs()).extracting(OrganisationRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}

	@Test
	public void shouldSerializeSearchParamContextCurriculums() {
		CurriculumRef ref1 = new CurriculumRefImpl(8l);
		CurriculumRef ref2 = new CurriculumRefImpl(8l);
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<? extends CurriculumRef> topicCurriculumRefs = asList(ref1, ref2);
		searchParams.setContextCurriculumRefs(topicCurriculumRefs);
		
		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getContextCurriculumRefs()).extracting(CurriculumRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}

	@Test
	public void shouldSerializeSearchParamContextCurriculumElements() {
		CurriculumElementRef ref1 = new CurriculumElementRefImpl(8l);
		CurriculumElementRef ref2 = new CurriculumElementRefImpl(8l);
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<? extends CurriculumElementRef> topicCurriculumElementRefs = asList(ref1, ref2);
		searchParams.setContextCurriculumElementRefs(topicCurriculumElementRefs);
		
		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getContextCurriculumElementRefs()).extracting(CurriculumElementRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}

	@Test
	public void shouldSerializeSearchParamContextTaxonomyLevels() {
		TaxonomyLevelRef ref1 = new TaxonomyLevelRefImpl(8l);
		TaxonomyLevelRef ref2 = new TaxonomyLevelRefImpl(8l);
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<? extends TaxonomyLevelRef> topicTaxonomyLevelRefs = asList(ref1, ref2);
		searchParams.setContextTaxonomyLevelRefs(topicTaxonomyLevelRefs);
		
		String xml = AnalysisPresentationXStream.toXml(searchParams);
		AnalysisSearchParameter searchParamsFromXml = AnalysisPresentationXStream.fromXml(xml,
				AnalysisSearchParameter.class);

		assertThat(searchParamsFromXml.getContextTaxonomyLevelRefs()).extracting(TaxonomyLevelRef::getKey)
				.containsExactlyInAnyOrder(ref1.getKey(), ref2.getKey());
	}
}
