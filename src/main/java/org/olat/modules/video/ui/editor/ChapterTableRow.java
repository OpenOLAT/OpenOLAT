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
package org.olat.modules.video.ui.editor;

import java.util.Date;
import java.util.Objects;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.video.ui.VideoChapterTableRow;

/**
 * Initial date: 2023-02-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChapterTableRow {
	private FormLink toolLink;
	private final VideoChapterTableRow videoChapterTableRow;
	public ChapterTableRow(VideoChapterTableRow videoChapterTableRow) {
		this.videoChapterTableRow = videoChapterTableRow;
	}

	public FormLink getToolLink() {
		return toolLink;
	}

	public void setToolLink(FormLink toolLink) {
		this.toolLink = toolLink;
	}

	public VideoChapterTableRow getVideoChapterTableRow() {
		return videoChapterTableRow;
	}

	public Date getBegin() {
		return videoChapterTableRow.getBegin();
	}

	public void setEnd(Date end) {
		videoChapterTableRow.setEnd(end);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ChapterTableRow that = (ChapterTableRow) o;
		return videoChapterTableRow.equals(that.videoChapterTableRow);
	}

	@Override
	public int hashCode() {
		return Objects.hash(videoChapterTableRow);
	}
}
