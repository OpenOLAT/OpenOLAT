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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnalysisPresentationSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSegment;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.ui.TrendDifference;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisPresentationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper testHelper;
	
	@Autowired
	private AnalysisPresentationDAO sut;
	
	@Before
	public void cleanUp() {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualityanalysispresentation")
				.executeUpdate();
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void shouldCreatePresentation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		List<OrganisationRef> dcOrganisationRefs = new ArrayList<>();
		OrganisationRef organisationRef = new OrganisationRefImpl(123L);
		dcOrganisationRefs.add(organisationRef);
		dbInstance.commitAndCloseSession();
		
		AnalysisPresentation presentation = sut.create(formEntry, dcOrganisationRefs);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(presentation).isNotNull();
		softly.assertThat(presentation.getKey()).isNull();
		softly.assertThat(presentation.getCreationDate()).isNotNull();
		softly.assertThat(presentation.getLastModified()).isNotNull();
		softly.assertThat(presentation.getFormEntry()).isEqualTo(formEntry);
		softly.assertThat(presentation.getName()).isNull();
		softly.assertThat(presentation.getAnalysisSegment()).isEqualTo(AnalysisSegment.OVERVIEW);
		softly.assertThat(presentation.getSearchParams()).isNotNull();
		softly.assertThat(presentation.getSearchParams().getFormEntryRef().getKey()).isEqualTo(formEntry.getKey());
		softly.assertThat(presentation.getSearchParams().getDataCollectionOrganisationRefs().get(0).getKey()).isEqualTo(organisationRef.getKey());
		softly.assertThat(presentation.getHeatMapGrouping()).isNotNull();
		softly.assertThat(presentation.getHeatMapInsufficientOnly()).isFalse();
		softly.assertThat(presentation.getTemporalGroupBy()).isEqualTo(TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR);
		softly.assertThat(presentation.getTrendDifference()).isEqualTo(TrendDifference.NONE);
		softly.assertThat(presentation.getRubricId()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldSaveNewPresentation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		List<OrganisationRef> dcOrganisationRefs = new ArrayList<>();
		OrganisationRef organisationRef = new OrganisationRefImpl(123L);
		dcOrganisationRefs.add(organisationRef);
		dbInstance.commitAndCloseSession();
		
		AnalysisPresentation presentation = sut.create(formEntry, dcOrganisationRefs);
		AnalysisSegment segment = AnalysisSegment.HEAT_MAP;
		presentation.setAnalysisSegment(segment);
		MultiGroupBy groupBy = MultiGroupBy.noGroupBy();
		presentation.setHeatMapGrouping(groupBy);
		String name = "presentation one";
		presentation.setName(name);
		AnalysisSearchParameter searchParams = presentation.getSearchParams().clone();
		presentation.setSearchParams(searchParams);
		TemporalGroupBy temporalGroupBy = TemporalGroupBy.DATA_COLLECTION_DEADLINE_HALF_YEAR;
		presentation.setTemporalGroupBy(temporalGroupBy);
		TrendDifference trendDifference = TrendDifference.ABSOLUTE;
		presentation.setTrendDifference(trendDifference);
		String rubricId = UUID.randomUUID().toString();
		presentation.setRubricId(rubricId);
		
		presentation = sut.save(presentation);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(presentation).isNotNull();
		softly.assertThat(presentation.getKey()).isNotNull();
		softly.assertThat(presentation.getCreationDate()).isNotNull();
		softly.assertThat(presentation.getLastModified()).isNotNull();
		softly.assertThat(presentation.getFormEntry()).isEqualTo(formEntry);
		softly.assertThat(presentation.getName()).isEqualTo(name);
		softly.assertThat(presentation.getAnalysisSegment()).isEqualTo(segment);
		softly.assertThat(presentation.getHeatMapGrouping()).isEqualTo(groupBy);
		softly.assertThat(presentation.getSearchParams()).isNotNull();
		softly.assertThat(presentation.getTemporalGroupBy()).isEqualTo(temporalGroupBy);
		softly.assertThat(presentation.getTrendDifference()).isEqualTo(trendDifference);
		softly.assertThat(presentation.getRubricId()).isEqualTo(rubricId);
		softly.assertThat(presentation.getSearchParams().getDataCollectionOrganisationRefs().get(0).getKey()).isEqualTo(organisationRef.getKey());
		softly.assertAll();
	}

	@Test
	public void shouldUpdatePresentation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		List<OrganisationRef> dcOrganisationRefs = new ArrayList<>();
		OrganisationRef organisationRef = new OrganisationRefImpl(123L);
		dcOrganisationRefs.add(organisationRef);
		dbInstance.commitAndCloseSession();
		AnalysisPresentation presentation = sut.create(formEntry, dcOrganisationRefs);
		presentation = sut.save(presentation);
		dbInstance.commitAndCloseSession();
		
		AnalysisSegment segment = AnalysisSegment.HEAT_MAP;
		presentation.setAnalysisSegment(segment);
		MultiGroupBy groupBy = MultiGroupBy.noGroupBy();
		presentation.setHeatMapGrouping(groupBy);
		String name = "presentation one";
		presentation.setName(name);
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setDataCollectionOrganisationRefs(dcOrganisationRefs);
		presentation.setSearchParams(searchParams);
		TemporalGroupBy temporalGroupBy = TemporalGroupBy.DATA_COLLECTION_DEADLINE_HALF_YEAR;
		presentation.setTemporalGroupBy(temporalGroupBy);
		TrendDifference trendDifference = TrendDifference.ABSOLUTE;
		presentation.setTrendDifference(trendDifference);
		String rubricId = UUID.randomUUID().toString();
		presentation.setRubricId(rubricId);
		
		presentation = sut.save(presentation);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(presentation).isNotNull();
		softly.assertThat(presentation.getKey()).isNotNull();
		softly.assertThat(presentation.getCreationDate()).isNotNull();
		softly.assertThat(presentation.getLastModified()).isNotNull();
		softly.assertThat(presentation.getFormEntry()).isEqualTo(formEntry);
		softly.assertThat(presentation.getName()).isEqualTo(name);
		softly.assertThat(presentation.getAnalysisSegment()).isEqualTo(segment);
		softly.assertThat(presentation.getHeatMapGrouping()).isEqualTo(groupBy);
		softly.assertThat(presentation.getSearchParams()).isNotNull();
		softly.assertThat(presentation.getTemporalGroupBy()).isEqualTo(temporalGroupBy);
		softly.assertThat(presentation.getTrendDifference()).isEqualTo(trendDifference);
		softly.assertThat(presentation.getRubricId()).isEqualTo(rubricId);
		softly.assertThat(presentation.getSearchParams().getDataCollectionOrganisationRefs().get(0).getKey()).isEqualTo(organisationRef.getKey());
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadPresentation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		List<OrganisationRef> dcOrganisationRefs = new ArrayList<>();
		OrganisationRef organisationRef = new OrganisationRefImpl(123L);
		dcOrganisationRefs.add(organisationRef);
		dbInstance.commitAndCloseSession();
		AnalysisPresentation presentation = sut.create(formEntry, dcOrganisationRefs);
		presentation = sut.save(presentation);
		dbInstance.commitAndCloseSession();
		
		AnalysisSegment segment = AnalysisSegment.HEAT_MAP;
		presentation.setAnalysisSegment(segment);
		MultiGroupBy groupBy = MultiGroupBy.noGroupBy();
		presentation.setHeatMapGrouping(groupBy);
		String name = "presentation one";
		presentation.setName(name);
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setDataCollectionOrganisationRefs(dcOrganisationRefs);
		presentation.setSearchParams(searchParams);
		TemporalGroupBy temporalGroupBy = TemporalGroupBy.DATA_COLLECTION_DEADLINE_HALF_YEAR;
		presentation.setTemporalGroupBy(temporalGroupBy);
		TrendDifference trendDifference = TrendDifference.ABSOLUTE;
		presentation.setTrendDifference(trendDifference);
		String rubricId = UUID.randomUUID().toString();
		presentation.setRubricId(rubricId);
		presentation = sut.save(presentation);
		dbInstance.commitAndCloseSession();
		
		presentation = sut.loadByKey(presentation);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(presentation).isNotNull();
		softly.assertThat(presentation.getKey()).isNotNull();
		softly.assertThat(presentation.getCreationDate()).isNotNull();
		softly.assertThat(presentation.getLastModified()).isNotNull();
		softly.assertThat(presentation.getFormEntry()).isEqualTo(formEntry);
		softly.assertThat(presentation.getName()).isEqualTo(name);
		softly.assertThat(presentation.getAnalysisSegment()).isEqualTo(segment);
		softly.assertThat(presentation.getHeatMapGrouping()).isEqualTo(groupBy);
		softly.assertThat(presentation.getSearchParams()).isNotNull();
		softly.assertThat(presentation.getTemporalGroupBy()).isEqualTo(temporalGroupBy);
		softly.assertThat(presentation.getTrendDifference()).isEqualTo(trendDifference);
		softly.assertThat(presentation.getRubricId()).isEqualTo(rubricId);
		softly.assertThat(presentation.getSearchParams().getDataCollectionOrganisationRefs()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldFilterByOrganisations() {
		// Create a form with a presentation
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AnalysisPresentation presentation1 = sut.create(formEntry, null);
		presentation1 = sut.save(presentation1);
		Organisation organisation1 = testHelper.createOrganisation();
		testHelper.createDataCollection(organisation1, formEntry);
		// Add the form to a data collection of an other organisation to test distinct
		Organisation organisation2 = testHelper.createOrganisation();
		testHelper.createDataCollection(organisation2, formEntry);
		// Create a second presentation of the same form
		AnalysisPresentation presentation2 = sut.create(formEntry, null);
		presentation2 = sut.save(presentation2);
		// Create a second form with an other organisation
		RepositoryEntry formEntryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		AnalysisPresentation presentationOther = sut.create(formEntryOther, null);
		presentationOther = sut.save(presentationOther);
		Organisation organisationOther = testHelper.createOrganisation();
		testHelper.createDataCollection(organisationOther, formEntryOther);
		dbInstance.commitAndCloseSession();
		
		AnalysisPresentationSearchParameter searchParams = new AnalysisPresentationSearchParameter();
		searchParams.setOrganisationRefs(asList(organisation1));
		List<AnalysisPresentation> presentations = sut.load(searchParams);
		
		assertThat(presentations)
				.containsExactlyInAnyOrder(presentation1, presentation2)
				.doesNotContain(presentationOther);
	}

	@Test
	public void shouldFilterByAllOrganisations() {
		// Create a form with a presentation
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AnalysisPresentation presentation1 = sut.create(formEntry, null);
		presentation1 = sut.save(presentation1);
		Organisation organisation1 = testHelper.createOrganisation();
		testHelper.createDataCollection(organisation1, formEntry);
		// Add the form to a data collection of an other organisation to test distinct
		Organisation organisation2 = testHelper.createOrganisation();
		testHelper.createDataCollection(organisation2, formEntry);
		// Create a second presentation of the same form
		AnalysisPresentation presentation2 = sut.create(formEntry, null);
		presentation2 = sut.save(presentation2);
		// Create a second form with an other organisation
		RepositoryEntry formEntryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		AnalysisPresentation presentationOther = sut.create(formEntryOther, null);
		presentationOther = sut.save(presentationOther);
		Organisation organisationOther = testHelper.createOrganisation();
		testHelper.createDataCollection(organisationOther, formEntryOther);
		dbInstance.commitAndCloseSession();
		
		AnalysisPresentationSearchParameter searchParams = new AnalysisPresentationSearchParameter();
		searchParams.setOrganisationRefs(null);
		List<AnalysisPresentation> presentations = sut.load(searchParams);
		
		assertThat(presentations)
				.containsExactlyInAnyOrder(presentation1, presentation2, presentationOther);
	}
	
	@Test
	public void shouldFilterByNoOrganisations() {
		// Create a form with a presentation
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AnalysisPresentation presentation1 = sut.create(formEntry, null);
		presentation1 = sut.save(presentation1);
		Organisation organisation1 = testHelper.createOrganisation();
		testHelper.createDataCollection(organisation1, formEntry);
		// Add the form to a data collection of an other organisation to test distinct
		Organisation organisation2 = testHelper.createOrganisation();
		testHelper.createDataCollection(organisation2, formEntry);
		// Create a second presentation of the same form
		AnalysisPresentation presentation2 = sut.create(formEntry, null);
		presentation2 = sut.save(presentation2);
		// Create a second form with an other organisation
		RepositoryEntry formEntryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		AnalysisPresentation presentationOther = sut.create(formEntryOther, null);
		presentationOther = sut.save(presentationOther);
		Organisation organisationOther = testHelper.createOrganisation();
		testHelper.createDataCollection(organisationOther, formEntryOther);
		dbInstance.commitAndCloseSession();
		
		AnalysisPresentationSearchParameter searchParams = new AnalysisPresentationSearchParameter();
		searchParams.setOrganisationRefs(Collections.emptyList());
		List<AnalysisPresentation> presentations = sut.load(searchParams);
		
		assertThat(presentations).isEmpty();
	}
	
	@Test
	public void shouldDeletePresentation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		AnalysisPresentation presentation = sut.create(formEntry, null);
		presentation = sut.save(presentation);
		dbInstance.commitAndCloseSession();
		
		sut.delete(presentation);
		
		presentation = sut.loadByKey(presentation);
		
		assertThat(presentation).isNull();
	}

}
