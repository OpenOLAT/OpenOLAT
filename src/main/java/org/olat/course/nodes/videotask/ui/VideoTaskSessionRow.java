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
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskScore;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskSessionRow {
	
	private final VideoTaskScore scoring;
	private final VideoTaskSession taskSession;
	private final CategoryColumn[] categoriesScoring;
	
	private FormLink toolsButton;
	
	public VideoTaskSessionRow(VideoTaskSession taskSession, VideoTaskScore scoring, CategoryColumn[] categoriesScoring) {
		this.taskSession = taskSession;
		this.scoring = scoring;
		this.categoriesScoring = categoriesScoring;
	}
	
	public long getAttempt() {
		return taskSession.getAttempt();
	}
	
	public Long getTaskSessionKey() {
		return taskSession.getKey();
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
		Date start = taskSession.getCreationDate();
		Date finish = taskSession.getFinishTime();
		if(finish != null && start != null) {
			return finish.getTime() - start.getTime();
		}
		return -1l;
	}
	
	public CategoryColumn getCategoryScoring(int index) {
		if(categoriesScoring != null && index >= 0 && index < categoriesScoring.length) {
			return categoriesScoring[index];
		}
		return null;
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
	
	public static class CategoryColumn {
		
		private VideoSegmentCategory category;
		private int correct;
		private int notCorrect;
		
		public VideoSegmentCategory getCategory() {
			return category;
		}
		
		public void setCategory(VideoSegmentCategory category) {
			this.category = category;
		}
		
		public int getCorrect() {
			return correct;
		}
		
		public void setCorrect(int correct) {
			this.correct = correct;
		}
		
		public int getNotCorrect() {
			return notCorrect;
		}
		
		public void setNotCorrect(int notCorrect) {
			this.notCorrect = notCorrect;
		}
	}
}
