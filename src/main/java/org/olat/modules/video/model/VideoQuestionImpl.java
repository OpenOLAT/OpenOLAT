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
package org.olat.modules.video.model;

import java.util.Date;

import org.olat.modules.video.VideoQuestion;

/**
 * 
 * Initial date: 4 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoQuestionImpl implements VideoQuestion {
	
	private String id;
	private String assessmentItemIdentifier;
	private Date begin;
	private String style;
	private String title;
	private String type;
	private Double maxScore;
	private long timeLimit;
	private boolean allowSkipping;
	private boolean allowNewAttempt;
	
	private String questionFilename;
	private String questionRootPath;
	
	@Override
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getAssessmentItemIdentifier() {
		return assessmentItemIdentifier;
	}

	@Override
	public void setAssessmentItemIdentifier(String identifier) {
		this.assessmentItemIdentifier = identifier;
	}

	@Override
	public Date getBegin() {
		return begin;
	}

	@Override
	public void setBegin(Date begin) {
		this.begin = begin;
	}

	@Override
	public String getStyle() {
		return style;
	}

	@Override
	public void setStyle(String style) {
		this.style = style;
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
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Double getMaxScore() {
		return maxScore;
	}

	@Override
	public void setMaxScore(Double maxScore) {
		this.maxScore = maxScore;
	}

	@Override
	public long getTimeLimit() {
		return timeLimit;
	}

	@Override
	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}

	@Override
	public boolean isAllowSkipping() {
		return allowSkipping;
	}

	@Override
	public void setAllowSkipping(boolean allowSkipping) {
		this.allowSkipping = allowSkipping;
	}


	@Override
	public boolean isAllowNewAttempt() {
		return allowNewAttempt;
	}

	@Override
	public void setAllowNewAttempt(boolean allow) {
		allowNewAttempt = allow;
	}

	@Override
	public String getQuestionFilename() {
		return questionFilename;
	}

	public void setQuestionFilename(String questionFilename) {
		this.questionFilename = questionFilename;
	}

	@Override
	public String getQuestionRootPath() {
		return questionRootPath;
	}

	public void setQuestionRootPath(String questionRootPath) {
		this.questionRootPath = questionRootPath;
	}
	
	@Override
	public long toSeconds() {
		return begin == null ? 0 : begin.getTime() / 1000l;
	}

	@Override
	public int hashCode() {
		return id == null ? -9612 : id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof VideoQuestionImpl) {
			VideoQuestionImpl question = (VideoQuestionImpl)obj;
			return id != null && id.equals(question.getId());
		}
		return false;
	}
}
