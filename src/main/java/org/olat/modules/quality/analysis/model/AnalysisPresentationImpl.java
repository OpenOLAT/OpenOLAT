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
package org.olat.modules.quality.analysis.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.core.id.Persistable;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSegment;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.ui.TrendDifference;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 28.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qualityanalysispresentation")
@Table(name="o_qual_analysis_presentation")
public class AnalysisPresentationImpl implements AnalysisPresentation, Persistable {

	private static final long serialVersionUID = 1061934209099333284L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="q_name", nullable=true, insertable=true, updatable=true)
	private String name;
	@Enumerated(EnumType.STRING)
	@Column(name="q_analysis_segment", nullable=true, insertable=true, updatable=true)
	private AnalysisSegment analysisSegment;
	@Column(name="q_search_params", nullable=true, insertable=true, updatable=true)
	private String searchParamsXml;
	@Transient
	private AnalysisSearchParameter searchParams;
	@Column(name="q_heatmap_grouping", nullable=true, insertable=true, updatable=true)
	private String heatMapGroupingXml;
	@Transient
	private MultiGroupBy heatMapGrouping;
	@Column(name="q_heatmap_insufficient_only", nullable=true, insertable=true, updatable=true)
	private Boolean heatMapInsufficientOnly;
	@Enumerated(EnumType.STRING)
	@Column(name="q_temporal_grouping", nullable=true, insertable=true, updatable=true)
	private TemporalGroupBy temporalGroupBy;
	@Enumerated(EnumType.STRING)
	@Column(name="q_trend_difference", nullable=true, insertable=true, updatable=true)
	private TrendDifference trendDifference;
	@Column(name="q_rubric_id", nullable=true, insertable=true, updatable=true)
	private String rubricId;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_form_entry", nullable=false, insertable=true, updatable=false)
	private RepositoryEntry formEntry;
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public AnalysisSegment getAnalysisSegment() {
		return analysisSegment;
	}

	@Override
	public void setAnalysisSegment(AnalysisSegment analysisSegment) {
		this.analysisSegment = analysisSegment;
	}

	@Override
	public AnalysisSearchParameter getSearchParams() {
		return searchParams;
	}

	@Override
	public void setSearchParams(AnalysisSearchParameter searchParams) {
		this.searchParams = searchParams;
	}

	public String getSearchParamsXml() {
		return searchParamsXml;
	}

	public void setSearchParamsXml(String searchParamsXml) {
		this.searchParamsXml = searchParamsXml;
	}

	@Override
	public MultiGroupBy getHeatMapGrouping() {
		return heatMapGrouping;
	}

	@Override
	public void setHeatMapGrouping(MultiGroupBy heatMapGrouping) {
		this.heatMapGrouping = heatMapGrouping;
	}

	public String getHeatMapGroupingXml() {
		return heatMapGroupingXml;
	}

	public void setHeatMapGroupingXml(String heatMapGroupingXml) {
		this.heatMapGroupingXml = heatMapGroupingXml;
	}

	@Override
	public Boolean getHeatMapInsufficientOnly() {
		return heatMapInsufficientOnly;
	}

	@Override
	public void setHeatMapInsufficientOnly(Boolean heatMapInsufficientOnly) {
		this.heatMapInsufficientOnly = heatMapInsufficientOnly;
	}

	@Override
	public TemporalGroupBy getTemporalGroupBy() {
		return temporalGroupBy;
	}

	@Override
	public void setTemporalGroupBy(TemporalGroupBy temporalGroupBy) {
		this.temporalGroupBy = temporalGroupBy;
	}

	@Override
	public TrendDifference getTrendDifference() {
		return trendDifference;
	}

	@Override
	public void setTrendDifference(TrendDifference trendDifference) {
		this.trendDifference = trendDifference;
	}

	@Override
	public String getRubricId() {
		return rubricId;
	}

	@Override
	public void setRubricId(String rubricId) {
		this.rubricId = rubricId;
	}

	@Override
	public RepositoryEntry getFormEntry() {
		return formEntry;
	}

	public void setFormEntry(RepositoryEntry formEntry) {
		this.formEntry = formEntry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalysisPresentationImpl other = (AnalysisPresentationImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
