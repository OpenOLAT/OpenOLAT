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

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnalysisPresentationRef;
import org.olat.modules.quality.analysis.AnalysisPresentationSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSegment;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.model.AnalysisPresentationImpl;
import org.olat.modules.quality.analysis.ui.TrendDifference;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 01.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class AnalysisPresentationDAO {
	
	@Autowired
	private DB dbInstance;
	
	AnalysisPresentation create(RepositoryEntry formEntry, List<? extends OrganisationRef> dataCollectionOrganisationRefs) {
		AnalysisPresentationImpl presentation = new AnalysisPresentationImpl();
		presentation.setCreationDate(new Date());
		presentation.setLastModified(presentation.getCreationDate());
		presentation.setFormEntry(formEntry);
		presentation.setAnalysisSegment(AnalysisSegment.OVERVIEW);
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setFormEntryRef(formEntry);
		searchParams.setDataCollectionOrganisationRefs(dataCollectionOrganisationRefs);
		presentation.setSearchParams(searchParams);
		presentation.setHeatMapGrouping(MultiGroupBy.noGroupBy());
		presentation.setHeatMapInsufficientOnly(Boolean.FALSE);
		presentation.setTemporalGroupBy(TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR);
		presentation.setTrendDifference(TrendDifference.NONE);
		return presentation;
	}
	
	AnalysisPresentation save(AnalysisPresentation presentation) {
		toXMLs(presentation);
		
		AnalysisPresentation savedPresentation = null;
		if (presentation.getKey() == null) {
			savedPresentation = saveNew(presentation);
		} else {
			savedPresentation = saveExisting(presentation);
		}
		savedPresentation.getSearchParams().setDataCollectionOrganisationRefs(
				presentation.getSearchParams().getDataCollectionOrganisationRefs());
		return savedPresentation;
	}

	private AnalysisPresentation saveNew(AnalysisPresentation presentation) {
		dbInstance.getCurrentEntityManager().persist(presentation);
		return presentation;
	}

	private AnalysisPresentation saveExisting(AnalysisPresentation presentation) {
		presentation.setLastModified(new Date());
		AnalysisPresentation savedPresentation = dbInstance.getCurrentEntityManager().merge(presentation);
		// Hibernate does not deliver transient attributes in merged objects
		savedPresentation.setHeatMapGrouping(presentation.getHeatMapGrouping());
		savedPresentation.setSearchParams(presentation.getSearchParams());
		return savedPresentation;
	}

	AnalysisPresentation loadByKey(AnalysisPresentationRef presentationRef) {
		if (presentationRef == null || presentationRef.getKey() == null) return null;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select presentation");
		sb.append("  from qualityanalysispresentation as presentation");
		sb.append("       inner join fetch presentation.formEntry");
		sb.append(" where presentation.key = :presentationRef");
		
		List<AnalysisPresentation> presentations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AnalysisPresentation.class)
				.setParameter("presentationRef", presentationRef.getKey())
				.getResultList();
		AnalysisPresentation presentation = presentations.isEmpty() ? null : presentations.get(0);
		fromXMLs(presentation);
		return presentation;
	}

	List<AnalysisPresentation> load(AnalysisPresentationSearchParameter searchParams) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select presentation");
		sb.append("  from qualityanalysispresentation as presentation");
		sb.append("       inner join presentation.formEntry as formEntry");
		appendWhere(sb, searchParams);
		
		TypedQuery<AnalysisPresentation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AnalysisPresentation.class);
		appendParameters(query, searchParams);
		
		List<AnalysisPresentation> presentations = query.getResultList();
		for (AnalysisPresentation presentation : presentations) {
			fromXMLs(presentation);
		}
		return presentations;
	}

	private void appendWhere(QueryBuilder sb, AnalysisPresentationSearchParameter searchParams) {
		if (searchParams.getOrganisationRefs() != null) {
			sb.and();
			sb.append("formEntry.key in (");
			sb.append("select survey.formEntry.key");
			sb.append("  from qualitydatacollectiontoorganisation dcToOrg");
			sb.append("       join dcToOrg.dataCollection as collection");
			sb.append("       join evaluationformsurvey survey on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
			sb.append("                                       and survey.resId = collection.key");
			sb.append(" where dcToOrg.organisation.key in :organisationKeys");
			sb.append(")");
		}
	}

	private void appendParameters(TypedQuery<AnalysisPresentation> query,
			AnalysisPresentationSearchParameter searchParams) {
		if (searchParams.getOrganisationRefs() != null) {
			List<Long> keys = searchParams.getOrganisationRefs().stream().map(OrganisationRef::getKey).collect(toList());
			keys = !keys.isEmpty()? keys: Collections.singletonList(-1l);
			query.setParameter("organisationKeys", keys);
		}
	}

	private void toXMLs(AnalysisPresentation presentation) {
		if (presentation instanceof AnalysisPresentationImpl) {
			AnalysisPresentationImpl presentationImpl = (AnalysisPresentationImpl) presentation;

			String searchParamsXml = AnalysisPresentationXStream.toXml(presentation.getSearchParams());
			presentationImpl.setSearchParamsXml(searchParamsXml);

			String heatMapGroupingXml = AnalysisPresentationXStream.toXml(presentation.getHeatMapGrouping());
			presentationImpl.setHeatMapGroupingXml(heatMapGroupingXml);
		}
	}
	
	private void fromXMLs(AnalysisPresentation presentation) {
		if (presentation instanceof AnalysisPresentationImpl) {
			AnalysisPresentationImpl presentationImpl = (AnalysisPresentationImpl) presentation;

			AnalysisSearchParameter searchParams = AnalysisPresentationXStream
					.fromXml(presentationImpl.getSearchParamsXml(), AnalysisSearchParameter.class);
			presentation.setSearchParams(searchParams);

			MultiGroupBy groupBy = AnalysisPresentationXStream
					.fromXml(presentationImpl.getHeatMapGroupingXml(), MultiGroupBy.class);
			presentation.setHeatMapGrouping(groupBy);
		}
	}

	void delete(AnalysisPresentationRef presentationRef) {
		if (presentationRef == null || presentationRef.getKey() == null) return;
		
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("delete from qualityanalysispresentation as presentation");
		sb.append(" where presentation.key = :presentationKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("presentationKey", presentationRef.getKey())
				.executeUpdate();
	}
	
}
