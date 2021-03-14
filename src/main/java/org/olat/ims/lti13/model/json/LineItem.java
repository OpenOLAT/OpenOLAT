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
package org.olat.ims.lti13.model.json;

import java.util.Date;

import org.olat.ims.lti13.LTI13Constants;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 
 * Initial date: 4 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LineItem {

	@JsonProperty("id")
	private String id;
	@JsonProperty("scoreMaximum")
	private Double scoreMaximum;
	@JsonProperty("label")
	private String label;
	@JsonProperty("resourceId")
	private String resourceId;
	@JsonProperty("tag")
	private String tag;
	@JsonProperty("startDateTime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern=LTI13Constants.DATE_PATTERN)
    @JsonDeserialize(using = TimestampDeserializer.class, as=Date.class)
	private Date startDateTime;
	@JsonProperty("endDateTime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern=LTI13Constants.DATE_PATTERN)
    @JsonDeserialize(using = TimestampDeserializer.class, as=Date.class)
	private Date endDateTime;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Double getScoreMaximum() {
		return scoreMaximum;
	}
	
	public void setScoreMaximum(Double scoreMaximum) {
		this.scoreMaximum = scoreMaximum;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public Date getStartDateTime() {
		return startDateTime;
	}
	
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	public Date getEndDateTime() {
		return endDateTime;
	}
	
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}
}
