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
package org.olat.modules.curriculum.ui.widgets;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;

/**
 * 
 * Initial date: 9 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LectureBlockWidgetRow {
	
	private final LectureBlock lectureBlock;
	private final boolean onlineMeeting;
	private final boolean nextScheduledEvent;
	private final LectureBlockVirtualStatus virtualStatus;
	private FormLink toolsLink;

	public LectureBlockWidgetRow(LectureBlock lectureBlock, LectureBlockVirtualStatus virtualStatus,
			boolean onlineMeeting, boolean nextScheduledEvent) {
		this.lectureBlock = lectureBlock;
		this.onlineMeeting = onlineMeeting;
		this.virtualStatus = virtualStatus;
		this.nextScheduledEvent = nextScheduledEvent;
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
	
	public String getLocation() {
		return lectureBlock.getLocation();
	}
	
	public boolean hasOnlineMeeting() {
		return onlineMeeting;
	}
	
	public boolean isNextScheduledEvent() {
		return nextScheduledEvent;
	}

	public LectureBlockVirtualStatus getVirtualStatus() {
		return virtualStatus;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
