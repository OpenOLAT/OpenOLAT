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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * Initial date: 4 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
/*
{
  "id": "https://lms.example.com/context/2923/lineitems/1/results/5323497",
  "scoreOf": "https://lms.example.com/context/2923/lineitems/1",
  "userId": "5323497",
  "resultScore": 0.83,
  "resultMaximum": 1,
  "comment": "This is exceptional work."
}
 */
@JsonInclude(value = JsonInclude.Include.ALWAYS)
public class Result {

	@JsonProperty("id")
	private String id;
	@JsonProperty("scoreOf")
	private String scoreOf;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("resultScore")
	private Double resultScore;
	@JsonProperty("resultMaximum")
	private Double resultMaximum;
	@JsonProperty("comment")
	private String comment;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getScoreOf() {
		return scoreOf;
	}
	
	public void setScoreOf(String scoreOf) {
		this.scoreOf = scoreOf;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Double getResultScore() {
		return resultScore;
	}
	
	public void setResultScore(Double resultScore) {
		this.resultScore = resultScore;
	}
	
	public Double getResultMaximum() {
		return resultMaximum;
	}
	
	public void setResultMaximum(Double resultMaximum) {
		this.resultMaximum = resultMaximum;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
}
