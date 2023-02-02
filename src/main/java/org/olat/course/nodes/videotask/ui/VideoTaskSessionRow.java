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
package org.olat.course.nodes.videotask.ui;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskCategoryScore;
import org.olat.modules.video.model.VideoTaskScore;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskSessionRow {
	
	private final VideoTaskScore scoring;
	private final Identity assessedIdentity;
	private final VideoTaskSession taskSession;
	private final VideoTaskCategoryScore[] categoriesScoring;
	private final String assessedIdentityFullName;
	
	private FormLink toolsButton;
	
	public VideoTaskSessionRow(VideoTaskSession taskSession, Identity assessedIdentity,
			String assessedIdentityFullName, VideoTaskScore scoring, VideoTaskCategoryScore[] categoriesScoring) {
		this.taskSession = taskSession;
		this.scoring = scoring;
		this.assessedIdentity = assessedIdentity;
		this.categoriesScoring = categoriesScoring;
		this.assessedIdentityFullName = assessedIdentityFullName;
	}
	
	public long getAttempt() {
		return taskSession.getAttempt();
	}
	
	public Long getTaskSessionKey() {
		return taskSession.getKey();
	}
	
	public Long getIdentityKey() {
		return assessedIdentity == null ? null : assessedIdentity.getKey();
	}
	
	public String getIdentityFullName() {
		return assessedIdentityFullName;
	}

	public VideoTaskSession getTaskSession() {
		return taskSession;
	}
	
	public BigDecimal getScoreInPercent() {
		return scoring == null ? null : scoring.getScoreInPercent();
	}
	
	public BigDecimal getPoints() {
		return scoring == null ? null : scoring.getPoints();
	}
	
	public boolean isFinished() {
		return taskSession.getFinishTime() != null;
	}
	
	public Date getFinishTime() {
		return taskSession.getFinishTime();
	}
	
	public long getDuration() {
		return taskSession.getDuration();
	}
	
	public VideoTaskCategoryScore getCategoryScoring(int index) {
		if(categoriesScoring != null && index >= 0 && index < categoriesScoring.length) {
			return categoriesScoring[index];
		}
		return null;
	}
	
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public FormLink getToolsButton() {
		return toolsButton;
	}

	public void setToolsButton(FormLink toolsButton) {
		this.toolsButton = toolsButton;
	}

	@Override
	public int hashCode() {
		return taskSession.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof VideoTaskSessionRow row) {
			return taskSession.equals(row.taskSession);
		}
		return false;
	}
}
