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
package org.olat.ims.lti13.model;

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

import org.olat.core.id.Persistable;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13ContentItemPresentationEnum;
import org.olat.ims.lti13.LTI13ContentItemTypesEnum;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;

/**
 * 
 * Initial date: 7 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="lticontentitem")
@Table(name="o_lti_content_item")
public class LTI13ContentItemImpl implements LTI13ContentItem, Persistable {
	
	private static final long serialVersionUID = -4445136905412689913L;

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

	@Enumerated(EnumType.STRING)
    @Column(name="l_type", nullable=false, insertable=true, updatable=false)
	private LTI13ContentItemTypesEnum type;

    @Column(name="l_url", nullable=false, insertable=true, updatable=true)
	private String url;
    @Column(name="l_title", nullable=true, insertable=true, updatable=true)
	private String title;
    @Column(name="l_text", nullable=true, insertable=true, updatable=true)
	private String text;
    @Column(name="l_media_type", nullable=true, insertable=true, updatable=true)
	private String mediaType;
	
    @Column(name="l_html", nullable=true, insertable=true, updatable=true)
	private String html;
    @Column(name="l_width", nullable=true, insertable=true, updatable=true)
	private Long width;
    @Column(name="l_height", nullable=true, insertable=true, updatable=true)
	private Long height;
	
    @Column(name="l_icon_url", nullable=true, insertable=true, updatable=true)
	private String iconUrl;
    @Column(name="l_icon_height", nullable=true, insertable=true, updatable=true)
	private Long iconHeight;
    @Column(name="l_icon_width", nullable=true, insertable=true, updatable=true)
	private Long iconWidth;

    @Column(name="l_thumbnail_url", nullable=true, insertable=true, updatable=true)
	private String thumbnailUrl;
    @Column(name="l_thumbnail_height", nullable=true, insertable=true, updatable=true)
	private Long thumbnailHeight;
    @Column(name="l_thumbnail_width", nullable=true, insertable=true, updatable=true)
	private Long thumbnailWidth;
    
	@Enumerated(EnumType.STRING)
    @Column(name="l_presentation", nullable=true, insertable=true, updatable=true)
	private LTI13ContentItemPresentationEnum presentation;

    @Column(name="l_window_targetname", nullable=true, insertable=true, updatable=true)
	private String windowTargetName;
    @Column(name="l_window_width", nullable=true, insertable=true, updatable=true)
	private Long windowWidth;
    @Column(name="l_window_height", nullable=true, insertable=true, updatable=true)
	private Long windowHeight;
    @Column(name="l_window_features", nullable=true, insertable=true, updatable=true)
	private String windowFeatures;

    @Column(name="l_iframe_width", nullable=true, insertable=true, updatable=true)
	private Long iframeWidth;
    @Column(name="l_iframe_height", nullable=true, insertable=true, updatable=true)
	private Long iframeHeight;
    @Column(name="l_iframe_src", nullable=true, insertable=true, updatable=true)
	private String iframeSrc;

    @Column(name="l_custom", nullable=true, insertable=true, updatable=true)
	private String custom;

    @Column(name="l_lineitem_label", nullable=true, insertable=true, updatable=true)
	private String lineItemLabel;
    @Column(name="l_lineitem_score_maximum", nullable=true, insertable=true, updatable=true)
	private Double lineItemScoreMaximum;
    @Column(name="l_lineitem_resource_id", nullable=true, insertable=true, updatable=true)
	private String lineItemResourceId;
    @Column(name="l_lineitem_tag", nullable=true, insertable=true, updatable=true)
	private String lineItemTag;
    @Column(name="l_lineitem_grades_release", nullable=true, insertable=true, updatable=true)
	private Boolean lineItemGradesReleased;
	
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_available_start", nullable=true, insertable=true, updatable=true)
	private Date availableStartDateTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_available_end", nullable=true, insertable=true, updatable=true)
	private Date availableEndDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_submission_start", nullable=true, insertable=true, updatable=true)
	private Date submissionStartDateTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_submission_end", nullable=true, insertable=true, updatable=true)
	private Date submissionEndDateTime;

    @Column(name="l_expires_at", nullable=true, insertable=true, updatable=true)
	private Date expiresAt;
    
	@ManyToOne(targetEntity=LTI13ToolImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_tool_id", nullable=false, insertable=true, updatable=false)
	private LTI13Tool tool;
	
	@ManyToOne(targetEntity=LTI13ToolDeploymentImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_tool_deployment_id", nullable=false, insertable=true, updatable=false)
	private LTI13ToolDeployment deployment;

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
	public LTI13ContentItemTypesEnum getType() {
		return type;
	}

	public void setType(LTI13ContentItemTypesEnum type) {
		this.type = type;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getMediaType() {
		return mediaType;
	}

	@Override
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	@Override
	public String getHtml() {
		return html;
	}

	@Override
	public void setHtml(String html) {
		this.html = html;
	}

	@Override
	public Long getWidth() {
		return width;
	}

	@Override
	public void setWidth(Long width) {
		this.width = width;
	}

	@Override
	public Long getHeight() {
		return height;
	}

	@Override
	public void setHeight(Long height) {
		this.height = height;
	}

	@Override
	public String getIconUrl() {
		return iconUrl;
	}

	@Override
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	@Override
	public Long getIconHeight() {
		return iconHeight;
	}

	@Override
	public void setIconHeight(Long iconHeight) {
		this.iconHeight = iconHeight;
	}

	@Override
	public Long getIconWidth() {
		return iconWidth;
	}

	@Override
	public void setIconWidth(Long iconWidth) {
		this.iconWidth = iconWidth;
	}

	@Override
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	@Override
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	@Override
	public Long getThumbnailHeight() {
		return thumbnailHeight;
	}

	@Override
	public void setThumbnailHeight(Long thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	@Override
	public Long getThumbnailWidth() {
		return thumbnailWidth;
	}

	@Override
	public void setThumbnailWidth(Long thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	@Override
	public LTI13ContentItemPresentationEnum getPresentation() {
		return presentation;
	}

	@Override
	public void setPresentation(LTI13ContentItemPresentationEnum presentation) {
		this.presentation = presentation;
	}

	@Override
	public String getWindowTargetName() {
		return windowTargetName;
	}

	@Override
	public void setWindowTargetName(String windowTargetName) {
		this.windowTargetName = windowTargetName;
	}

	@Override
	public Long getWindowWidth() {
		return windowWidth;
	}

	@Override
	public void setWindowWidth(Long windowWidth) {
		this.windowWidth = windowWidth;
	}

	@Override
	public Long getWindowHeight() {
		return windowHeight;
	}

	@Override
	public void setWindowHeight(Long windowHeight) {
		this.windowHeight = windowHeight;
	}

	@Override
	public String getWindowFeatures() {
		return windowFeatures;
	}

	@Override
	public void setWindowFeatures(String windowFeatures) {
		this.windowFeatures = windowFeatures;
	}

	@Override
	public Long getIframeWidth() {
		return iframeWidth;
	}

	@Override
	public void setIframeWidth(Long iframeWidth) {
		this.iframeWidth = iframeWidth;
	}

	@Override
	public Long getIframeHeight() {
		return iframeHeight;
	}

	@Override
	public void setIframeHeight(Long iframeHeight) {
		this.iframeHeight = iframeHeight;
	}

	@Override
	public String getIframeSrc() {
		return iframeSrc;
	}

	@Override
	public void setIframeSrc(String iframeSrc) {
		this.iframeSrc = iframeSrc;
	}

	@Override
	public String getCustom() {
		return custom;
	}

	@Override
	public void setCustom(String custom) {
		this.custom = custom;
	}

	@Override
	public String getLineItemLabel() {
		return lineItemLabel;
	}

	@Override
	public void setLineItemLabel(String lineItemLabel) {
		this.lineItemLabel = lineItemLabel;
	}

	@Override
	public Double getLineItemScoreMaximum() {
		return lineItemScoreMaximum;
	}

	@Override
	public void setLineItemScoreMaximum(Double lineItemScoreMaximum) {
		this.lineItemScoreMaximum = lineItemScoreMaximum;
	}

	@Override
	public String getLineItemResourceId() {
		return lineItemResourceId;
	}

	@Override
	public void setLineItemResourceId(String lineItemResourceId) {
		this.lineItemResourceId = lineItemResourceId;
	}

	@Override
	public String getLineItemTag() {
		return lineItemTag;
	}

	@Override
	public void setLineItemTag(String lineItemTag) {
		this.lineItemTag = lineItemTag;
	}

	@Override
	public Boolean getLineItemGradesReleased() {
		return lineItemGradesReleased;
	}

	@Override
	public void setLineItemGradesReleased(Boolean lineItemGradesReleased) {
		this.lineItemGradesReleased = lineItemGradesReleased;
	}

	@Override
	public Date getAvailableStartDateTime() {
		return availableStartDateTime;
	}

	@Override
	public void setAvailableStartDateTime(Date availableStartDateTime) {
		this.availableStartDateTime = availableStartDateTime;
	}

	@Override
	public Date getAvailableEndDateTime() {
		return availableEndDateTime;
	}

	@Override
	public void setAvailableEndDateTime(Date availableEndDateTime) {
		this.availableEndDateTime = availableEndDateTime;
	}

	@Override
	public Date getSubmissionStartDateTime() {
		return submissionStartDateTime;
	}

	@Override
	public void setSubmissionStartDateTime(Date submissionStartDateTime) {
		this.submissionStartDateTime = submissionStartDateTime;
	}

	@Override
	public Date getSubmissionEndDateTime() {
		return submissionEndDateTime;
	}

	@Override
	public void setSubmissionEndDateTime(Date submissionEndDateTime) {
		this.submissionEndDateTime = submissionEndDateTime;
	}

	@Override
	public Date getExpiresAt() {
		return expiresAt;
	}

	@Override
	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}

	@Override
	public LTI13Tool getTool() {
		return tool;
	}

	public void setTool(LTI13Tool tool) {
		this.tool = tool;
	}
	
	@Override
	public LTI13ToolDeployment getDeployment() {
		return deployment;
	}

	public void setDeployment(LTI13ToolDeployment deployment) {
		this.deployment = deployment;
	}

	@Override
	public int hashCode() {
		return key == null ? 265379 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof LTI13ContentItemImpl link) {
			return getKey() != null && getKey().equals(link.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
