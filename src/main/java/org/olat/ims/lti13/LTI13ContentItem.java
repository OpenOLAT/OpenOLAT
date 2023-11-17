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
package org.olat.ims.lti13;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 7 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface LTI13ContentItem extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public LTI13ContentItemTypesEnum getType();
	
	public String getUrl();

	public void setUrl(String url);

	public String getTitle();

	public void setTitle(String title);

	public String getText();

	public void setText(String text);
	
	public String getMediaType();
	
	public void setMediaType(String mediaType);
	
	/**
	 * @return html of item of type HTML, or embed html of item of type link.
	 */
	public String getHtml();

	public void setHtml(String html);

	public Long getWidth();

	public void setWidth(Long width);

	public Long getHeight();

	public void setHeight(Long height);

	public String getIconUrl();

	public void setIconUrl(String iconUrl);

	public Long getIconHeight();

	public void setIconHeight(Long iconHeight);

	public Long getIconWidth();

	public void setIconWidth(Long iconWidth);

	public String getThumbnailUrl();

	public void setThumbnailUrl(String thumbnailUrl);

	public Long getThumbnailHeight();

	public void setThumbnailHeight(Long thumbnailHeight);

	public Long getThumbnailWidth();

	public void setThumbnailWidth(Long thumbnailWidth);
	
	public LTI13ContentItemPresentationEnum getPresentation();
	
	public void setPresentation(LTI13ContentItemPresentationEnum presentation);

	public String getWindowTargetName();

	public void setWindowTargetName(String windowTargetName);

	public Long getWindowWidth();

	public void setWindowWidth(Long windowWidth);

	public Long getWindowHeight();

	public void setWindowHeight(Long windowHeight);

	public String getWindowFeatures();

	public void setWindowFeatures(String windowFeatures);

	public Long getIframeWidth();

	public void setIframeWidth(Long iframeWidth);

	public Long getIframeHeight();

	public void setIframeHeight(Long iframeHeight);
	
	public String getIframeSrc();

	public void setIframeSrc(String src);

	public String getCustom();

	public void setCustom(String custom);

	public String getLineItemLabel();

	public void setLineItemLabel(String lineItemLabel);

	public Double getLineItemScoreMaximum();

	public void setLineItemScoreMaximum(Double lineItemScoreMaximum);

	public String getLineItemResourceId();

	public void setLineItemResourceId(String lineItemResourceId);

	public String getLineItemTag();

	public void setLineItemTag(String lineItemTag);

	public Boolean getLineItemGradesReleased();

	public void setLineItemGradesReleased(Boolean lineItemGradesReleased);

	public Date getAvailableStartDateTime();

	public void setAvailableStartDateTime(Date availableStartDateTime);

	public Date getAvailableEndDateTime();

	public void setAvailableEndDateTime(Date availableEndDateTime);

	public Date getSubmissionStartDateTime();

	public void setSubmissionStartDateTime(Date submissionStartDateTime);

	public Date getSubmissionEndDateTime();

	public void setSubmissionEndDateTime(Date submissionEndDateTime);

	public Date getExpiresAt();

	public void setExpiresAt(Date expiresAt);
	
	public LTI13Tool getTool();
	
	public LTI13ToolDeployment getDeployment();
	
	public LTI13Context getContext();

}
