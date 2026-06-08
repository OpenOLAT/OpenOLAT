/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model.review;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rpositionreviewdefinition")
@Table(name="o_selectus_review_position_def")
public class PositionReviewDefinitionImpl implements PositionReviewDefinition, CreateInfo, Persistable {

	private static final long serialVersionUID = -4782097166222710284L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="review_comment_enable", nullable=true, insertable=true, updatable=true)
	private boolean reviewCommentEnabled = false;

	@Column(name="r_rev_name_visibility", nullable=false, insertable=true, updatable=true)
	private String reviewNameVisibilityString;

	@Column(name="r_rev_visibility", nullable=false, insertable=true, updatable=true)
	private String reviewVisibilityString;
	@Column(name="r_rev_visibility_head", nullable=false, insertable=true, updatable=true)
	private String reviewVisibilityHeadString;
	@Column(name="r_rev_visibility_secretary", nullable=false, insertable=true, updatable=true)
	private String reviewVisibilitySecretaryString;
	@Column(name="r_rev_visibility_exofficio", nullable=false, insertable=true, updatable=true)
	private String reviewVisibilityExofficioString;
	
	@Column(name="r_rev_fill", nullable=false, insertable=true, updatable=true)
	private String reviewFillString;
	@Column(name="r_rev_fill_head", nullable=false, insertable=true, updatable=true)
	private String reviewFillHeadString;
	@Column(name="r_rev_fill_secretary", nullable=false, insertable=true, updatable=true)
	private String reviewFillSecretaryString;
	@Column(name="r_rev_fill_exofficio", nullable=false, insertable=true, updatable=true)
	private String reviewFillExofficioString;

	@Column(name="r_slider_steps", nullable=true, insertable=true, updatable=true)
	private Integer defaultSliderSteps;
	@Column(name="r_slider_left_label", nullable=true, insertable=true, updatable=true)
	private String defaultSliderLeftLabel;
	@Column(name="r_slider_right_label", nullable=true, insertable=true, updatable=true)
	private String defaultSliderRightLabel;
	
	@Column(name="r_rev_statistics_enable", nullable=true, insertable=true, updatable=true)
	private Boolean reviewStatisticsEnabled = false;
	@Column(name="r_rev_chart_enable", nullable=true, insertable=true, updatable=true)
	private Boolean reviewRadarChartEnabled = false;

	
	@OneToMany(targetEntity=ReviewElementDefinitionImpl.class, mappedBy="positionReviewDefinition", fetch=FetchType.LAZY)
	@OrderColumn(name="pos")
	@ListIndexBase(0)
	private List<ReviewElementDefinition> elements;
	
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
	
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public boolean isReviewCommentEnabled() {
		return reviewCommentEnabled;
	}

	@Override
	public void setReviewCommentEnabled(boolean reviewCommentEnabled) {
		this.reviewCommentEnabled = reviewCommentEnabled;
	}

	public String getReviewNameVisibilityString() {
		return reviewNameVisibilityString;
	}

	public void setReviewNameVisibilityString(String reviewNameVisibilityString) {
		this.reviewNameVisibilityString = reviewNameVisibilityString;
	}

	@Override
	public ReviewerNameVisibilityEnum getReviewNameVisibility() {
		return StringHelper.containsNonWhitespace(reviewNameVisibilityString)
				? ReviewerNameVisibilityEnum.valueOf(reviewNameVisibilityString) : ReviewerNameVisibilityEnum.visible;
	}

	@Override
	public void setReviewNameVisibility(ReviewerNameVisibilityEnum reviewNameVisibility) {
		if(reviewNameVisibility == null) {
			reviewNameVisibilityString = ReviewerNameVisibilityEnum.visible.name();
		} else {
			reviewNameVisibilityString = reviewNameVisibility.name();
		}
	}

	public String getReviewVisibilityString() {
		return reviewVisibilityString;
	}

	public void setReviewVisibilityString(String reviewVisibilityString) {
		this.reviewVisibilityString = reviewVisibilityString;
	}

	@Override
	public ReviewVisibilityEnum getReviewVisibilityCommittee() {
		return StringHelper.containsNonWhitespace(reviewVisibilityString)
				? ReviewVisibilityEnum.valueOf(reviewVisibilityString) : ReviewVisibilityEnum.always;
	}

	@Override
	public void setReviewVisibilityCommittee(ReviewVisibilityEnum reviewVisibility) {
		if(reviewVisibility == null) {
			reviewVisibilityString = ReviewVisibilityEnum.always.name();
		} else {
			reviewVisibilityString = reviewVisibility.name();
		}
	}

	public String getReviewVisibilityHeadString() {
		return reviewVisibilityHeadString;
	}

	public void setReviewVisibilityHeadString(String reviewVisibilityHeadString) {
		this.reviewVisibilityHeadString = reviewVisibilityHeadString;
	}

	@Override
	public ReviewVisibilityEnum getReviewVisibilityHead() {
		return StringHelper.containsNonWhitespace(reviewVisibilityHeadString)
				? ReviewVisibilityEnum.valueOf(reviewVisibilityHeadString) : ReviewVisibilityEnum.always;
	}

	@Override
	public void setReviewVisibilityHead(ReviewVisibilityEnum reviewVisibility) {
		if(reviewVisibility == null) {
			reviewVisibilityHeadString = ReviewVisibilityEnum.always.name();
		} else {
			reviewVisibilityHeadString = reviewVisibility.name();
		}
	}

	public String getReviewVisibilitySecretaryString() {
		return reviewVisibilitySecretaryString;
	}

	public void setReviewVisibilitySecretaryString(String reviewVisibilitySecretaryString) {
		this.reviewVisibilitySecretaryString = reviewVisibilitySecretaryString;
	}

	@Override
	public ReviewVisibilityEnum getReviewVisibilitySecretary() {
		return StringHelper.containsNonWhitespace(reviewVisibilitySecretaryString)
				? ReviewVisibilityEnum.valueOf(reviewVisibilitySecretaryString) : ReviewVisibilityEnum.always;
	}

	@Override
	public void setReviewVisibilitySecretary(ReviewVisibilityEnum reviewVisibility) {
		if(reviewVisibility == null) {
			reviewVisibilitySecretaryString = ReviewVisibilityEnum.always.name();
		} else {
			reviewVisibilitySecretaryString = reviewVisibility.name();
		}
	}

	public String getReviewVisibilityExofficioString() {
		return reviewVisibilityExofficioString;
	}

	public void setReviewVisibilityExofficioString(String reviewVisibilityExofficioString) {
		this.reviewVisibilityExofficioString = reviewVisibilityExofficioString;
	}

	@Override
	public ReviewVisibilityEnum getReviewVisibilityExofficio() {
		return StringHelper.containsNonWhitespace(reviewVisibilityExofficioString)
				? ReviewVisibilityEnum.valueOf(reviewVisibilityExofficioString) : ReviewVisibilityEnum.always;
	}

	@Override
	public void setReviewVisibilityExofficio(ReviewVisibilityEnum reviewVisibility) {
		if(reviewVisibility == null) {
			reviewVisibilityExofficioString = ReviewVisibilityEnum.always.name();
		} else {
			reviewVisibilityExofficioString = reviewVisibility.name();
		}
	}

	public String getReviewFillString() {
		return reviewFillString;
	}

	public void setReviewFillString(String reviewFillString) {
		this.reviewFillString = reviewFillString;
	}

	@Override
	public ReviewFillEnum getReviewFillCommittee() {
		return StringHelper.containsNonWhitespace(reviewFillString)
				? ReviewFillEnum.valueOf(reviewFillString) : ReviewFillEnum.fill;
	}

	@Override
	public void setReviewFillCommittee(ReviewFillEnum reviewFill) {
		if(reviewFill == null) {
			reviewFillString = ReviewFillEnum.fill.name();
		} else {
			reviewFillString = reviewFill.name();
		}
	}

	public String getReviewFillHeadString() {
		return reviewFillHeadString;
	}

	public void setReviewFillHeadString(String reviewFillHeadString) {
		this.reviewFillHeadString = reviewFillHeadString;
	}

	@Override
	public ReviewFillEnum getReviewFillHead() {
		return StringHelper.containsNonWhitespace(reviewFillHeadString)
				? ReviewFillEnum.valueOf(reviewFillHeadString) : ReviewFillEnum.fill;
	}

	@Override
	public void setReviewFillHead(ReviewFillEnum reviewFill) {
		if(reviewFill == null) {
			reviewFillHeadString = ReviewFillEnum.no.name();
		} else {
			reviewFillHeadString = reviewFill.name();
		}
	}

	public String getReviewFillSecretaryString() {
		return reviewFillSecretaryString;
	}

	public void setReviewFillSecretaryString(String reviewFillSecretaryString) {
		this.reviewFillSecretaryString = reviewFillSecretaryString;
	}

	@Override
	public ReviewFillEnum getReviewFillSecretary() {
		return StringHelper.containsNonWhitespace(reviewFillSecretaryString)
				? ReviewFillEnum.valueOf(reviewFillSecretaryString) : ReviewFillEnum.fill;
	}

	@Override
	public void setReviewFillSecretary(ReviewFillEnum reviewFill) {
		if(reviewFill == null) {
			reviewFillSecretaryString = ReviewFillEnum.no.name();
		} else {
			reviewFillSecretaryString = reviewFill.name();
		}
	}

	public String getReviewFillExofficioString() {
		return reviewFillExofficioString;
	}

	public void setReviewFillExofficioString(String reviewFillExofficioString) {
		this.reviewFillExofficioString = reviewFillExofficioString;
	}

	@Override
	public ReviewFillEnum getReviewFillExofficio() {
		return StringHelper.containsNonWhitespace(reviewFillExofficioString)
				? ReviewFillEnum.valueOf(reviewFillExofficioString) : ReviewFillEnum.fill;
	}

	@Override
	public void setReviewFillExofficio(ReviewFillEnum reviewFill) {
		if(reviewFill == null) {
			reviewFillExofficioString = ReviewFillEnum.no.name();
		} else {
			reviewFillExofficioString = reviewFill.name();
		}
	}

	@Override
	@Transient
	public PositionRole[] getReviewFillRoles() {
		List<PositionRole> roles = new ArrayList<>(5);
		if(getReviewFillCommittee() == ReviewFillEnum.fill) {
			roles.add(PositionRole.member);
		}
		if(getReviewFillHead() == ReviewFillEnum.fill) {
			roles.add(PositionRole.head);
		}
		if(getReviewFillSecretary() == ReviewFillEnum.fill) {
			roles.add(PositionRole.secretary);
		}
		if(getReviewFillExofficio() == ReviewFillEnum.fill) {
			roles.add(PositionRole.exofficio);
		}
		return roles.toArray(new PositionRole[roles.size()]);
	}

	@Override
	public Integer getDefaultSliderSteps() {
		return defaultSliderSteps;
	}

	@Override
	public void setDefaultSliderSteps(Integer defaultSliderSteps) {
		this.defaultSliderSteps = defaultSliderSteps;
	}

	@Override
	public String getDefaultSliderLeftLabel() {
		return defaultSliderLeftLabel;
	}

	@Override
	public void setDefaultSliderLeftLabel(String defaultSliderLeftLabel) {
		this.defaultSliderLeftLabel = defaultSliderLeftLabel;
	}

	@Override
	public String getDefaultSliderRightLabel() {
		return defaultSliderRightLabel;
	}

	@Override
	public void setDefaultSliderRightLabel(String defaultSliderRightLabel) {
		this.defaultSliderRightLabel = defaultSliderRightLabel;
	}

	@Override
	public List<ReviewElementDefinition> getElements() {
		if(elements == null) {
			elements = new ArrayList<>();
		}
		return elements;
	}

	public void setElements(List<ReviewElementDefinition> elements) {
		this.elements = elements;
	}

	@Override
	public Boolean getReviewStatisticsEnabled() {
		return reviewStatisticsEnabled;
	}

	@Override
	public void setReviewStatisticsEnabled(Boolean reviewStatisticsEnabled) {
		this.reviewStatisticsEnabled = reviewStatisticsEnabled;
	}

	@Override
	public Boolean getReviewRadarChartEnabled() {
		return reviewRadarChartEnabled;
	}

	@Override
	public void setReviewRadarChartEnabled(Boolean reviewRadarChartEnabled) {
		this.reviewRadarChartEnabled = reviewRadarChartEnabled;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 36871 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PositionReviewDefinitionImpl) {
			PositionReviewDefinitionImpl def = (PositionReviewDefinitionImpl)obj;
			return getKey() != null && getKey().equals(def.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
