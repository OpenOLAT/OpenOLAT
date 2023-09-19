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
package org.olat.ims.lti13.manager;

import java.util.Date;
import java.util.Map;

import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13ContentItemPresentationEnum;
import org.olat.ims.lti13.model.json.TimestampDeserializer;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13ContentItemClaimParser {
	
	private final TimestampDeserializer timestampDeserializer = new TimestampDeserializer();
	
	@SuppressWarnings("unchecked")
	public void parseLtiResourceLink(LTI13ContentItem item, Map<Object,Object> contentItemsObj) {
		item.setTitle(toString(contentItemsObj.get("title"), 255));
		item.setText((String)contentItemsObj.get("text"));
		item.setUrl((String)contentItemsObj.get("url"));
		
		parseWindowIframe(item, contentItemsObj);
		
		Map<Object,Object> lineItemMap = (Map<Object,Object>)contentItemsObj.get("lineItem");
		if(lineItemMap != null) {
			item.setLineItemLabel(toString(lineItemMap.get("label"), 255));
			item.setLineItemGradesReleased(toBoolean(lineItemMap.get("gradesReleased")));
			item.setLineItemResourceId(toString(lineItemMap.get("resourceId"), 255));
			item.setLineItemTag(toString(lineItemMap.get("tag"), 255));
			item.setLineItemScoreMaximum(toDouble(lineItemMap.get("scoreMaximum")));
		}

		Map<Object,Object> submissionMap = (Map<Object,Object>)contentItemsObj.get("submission");
		if(submissionMap != null) {
			item.setSubmissionStartDateTime(toDate(submissionMap.get("startDateTime")));
			item.setSubmissionEndDateTime(toDate(submissionMap.get("endDateTime")));
		}
		
		Map<Object,Object> availableMap = (Map<Object,Object>)contentItemsObj.get("available");
		if(availableMap != null) {
			item.setAvailableStartDateTime(toDate(availableMap.get("startDateTime")));
			item.setAvailableEndDateTime(toDate(availableMap.get("endDateTime")));
		}
		
		parseIcon(item, contentItemsObj);
		parseThumbnail(item, contentItemsObj);
	}

	public void parseLink(LTI13ContentItem item, Map<Object,Object> contentItemsObj) {
		item.setTitle(toString(contentItemsObj.get("title"), 255));
		item.setText((String)contentItemsObj.get("text"));
		item.setUrl((String)contentItemsObj.get("url"));

		parseIcon(item, contentItemsObj);
		parseThumbnail(item, contentItemsObj);
		parseWindowIframe(item, contentItemsObj);
		
		@SuppressWarnings("unchecked")
		Map<Object,Object> embedMap = (Map<Object,Object>)contentItemsObj.get("embed");
		if(embedMap != null) {
			item.setHtml((String)embedMap.get("html"));
		}
	}
	
	public void parseImage(LTI13ContentItem item, Map<Object,Object> contentItemsObj) {
		item.setTitle(toString(contentItemsObj.get("title"), 255));
		item.setText((String)contentItemsObj.get("text"));
		item.setUrl(toString(contentItemsObj.get("url"), 1024));

		parseIcon(item, contentItemsObj);
		parseThumbnail(item, contentItemsObj);
		
		item.setHeight(toLong(contentItemsObj.get("height")));
		item.setWidth(toLong(contentItemsObj.get("width")));
	}
	
	public void parseHtml(LTI13ContentItem item, Map<Object,Object> contentItemsObj) {
		item.setTitle(toString(contentItemsObj.get("title"), 255));
		item.setText((String)contentItemsObj.get("text"));
		item.setHtml((String)contentItemsObj.get("html"));
	}
	
	public void parseFile(LTI13ContentItem item, Map<Object,Object> contentItemsObj) {
		item.setTitle(toString(contentItemsObj.get("title"), 255));
		item.setText((String)contentItemsObj.get("text"));
		item.setUrl(toString(contentItemsObj.get("url"), 1024));
		
		parseIcon(item, contentItemsObj);
		parseThumbnail(item, contentItemsObj);
		
		item.setExpiresAt(toDate(contentItemsObj.get("expiresAt")));
	}
	
	@SuppressWarnings("unchecked")
	private void parseWindowIframe(LTI13ContentItem item, Map<Object,Object> contentItemsObj) {
		Map<Object,Object> presentationMap = (Map<Object,Object>)contentItemsObj.get("presentation");
		if(presentationMap != null) {
			LTI13ContentItemPresentationEnum presentation = LTI13ContentItemPresentationEnum.secureValueOf(presentationMap.get("documentTarget"));
			item.setPresentation(presentation);
		}
		
		Map<Object,Object> windowMap = (Map<Object,Object>)contentItemsObj.get("window");
		if(windowMap != null) {
			item.setWindowTargetName(toString(windowMap.get("targetName"), 255));
			item.setWindowHeight(toLong(windowMap.get("height")));
			item.setWindowWidth(toLong(windowMap.get("width")));
			item.setWindowFeatures(toString(windowMap.get("windowFeatures"), 1024));
		}
		
		Map<Object,Object> iframeMap = (Map<Object,Object>)contentItemsObj.get("iframe");
		if(iframeMap != null) {
			item.setIframeHeight(toLong(iframeMap.get("height")));
			item.setIframeWidth(toLong(iframeMap.get("width")));
			item.setIframeSrc(toString(iframeMap.get("src"), 1024));
		}
	}
	

	private void parseThumbnail(LTI13ContentItem item, Map<Object,Object> contentItemsObj) {
		@SuppressWarnings("unchecked")
		Map<Object,Object> thumbnailMap = (Map<Object,Object>)contentItemsObj.get("thumbnail");
		if(thumbnailMap != null) {
			item.setThumbnailUrl(toString(thumbnailMap.get("url"), 255));
			item.setThumbnailHeight(toLong(thumbnailMap.get("height")));
			item.setThumbnailWidth(toLong(thumbnailMap.get("width")));
		}
	}
	
	private void parseIcon(LTI13ContentItem item, Map<Object,Object> contentItemsObj) {
		@SuppressWarnings("unchecked")
		Map<Object,Object> iconMap = (Map<Object,Object>)contentItemsObj.get("icon");
		if(iconMap != null) {
			item.setIconUrl(toString(iconMap.get("url"), 255));
			item.setIconHeight(toLong(iconMap.get("height")));
			item.setIconWidth(toLong(iconMap.get("width")));
		}
	}
	
	private Long toLong(Object val) {
		if(val instanceof Number number) {
			return Long.valueOf(number.longValue());
		}
		String text = null;
		if(val != null) {
			text = val.toString();
		}
		if(text != null) {
			return Long.valueOf(text);
		}
		return null;
	}
	
	private Double toDouble(Object val) {
		if(val instanceof Number number) {
			return Double.valueOf(number.doubleValue());
		}
		String text = null;
		if(val != null) {
			text = val.toString();
		}
		if(text != null) {
			return Double.valueOf(text);
		}
		return null;
	}
	
	private Date toDate(Object val) {
		if(val instanceof Date date) {
			return date;
		}
		String text = null;
		if(val != null) {
			text = val.toString();
		}
		if(text != null) {
			synchronized(timestampDeserializer) {
				return timestampDeserializer.getDate(text);
			}
		}
		return null;
	}
	
	private String toString(Object val, int length) {
		String text = (String)val;
		if(text != null && text.length() > length) {
			text = text.substring(0, length - 1);
		}
		return text;
	}
	
	private Boolean toBoolean(Object val) {
		if(val instanceof Boolean bool) {
			return bool;
		}
		
		String text = null;
		if(val != null) {
			text = val.toString();
		}
		
		if("true".equalsIgnoreCase(text)) {
			return Boolean.TRUE;
		}
		if("false".equalsIgnoreCase(text)) {
			return Boolean.FALSE;
		}
		return null;
	}
}
