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
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 
 * Initial date: 3 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@JsonInclude(value = JsonInclude.Include.ALWAYS)
public class LineItemScore {

	@JsonProperty("userId")
	private String userId;
	@JsonProperty("timestamp")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern=LTI13Constants.DATE_PATTERN, lenient=OptBoolean.TRUE)
    @JsonDeserialize(using = TimestampDeserializer.class, as=Date.class)
	private Date timestamp;
	
	@JsonProperty("scoreGiven")
	private Double scoreGiven;
	@JsonProperty("scoreMaximum")
	private Double scoreMaximum;
	
	@JsonProperty("gradingProgress")
	private String gradingProgress;
	@JsonProperty("activityProgress")
	private String activityProgress;

	@JsonProperty("comment")
	private String comment;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Double getScoreGiven() {
		return scoreGiven;
	}

	public void setScoreGiven(Double scoreGiven) {
		this.scoreGiven = scoreGiven;
	}

	public Double getScoreMaximum() {
		return scoreMaximum;
	}

	public void setScoreMaximum(Double scoreMaximum) {
		this.scoreMaximum = scoreMaximum;
	}

	public String getGradingProgress() {
		return gradingProgress;
	}

	public void setGradingProgress(String gradingProgress) {
		this.gradingProgress = gradingProgress;
	}

	public String getActivityProgress() {
		return activityProgress;
	}

	public void setActivityProgress(String activityProgress) {
		this.activityProgress = activityProgress;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
