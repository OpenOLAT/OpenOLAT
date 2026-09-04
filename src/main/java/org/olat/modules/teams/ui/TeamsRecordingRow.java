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
package org.olat.modules.teams.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.teams.TeamsRecording;
import org.olat.modules.teams.TeamsRecordingsPublishedRoles;

/**
 * 
 * Initial date: 25 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TeamsRecordingRow {
	
	private FormLink toolsLink;
	private FormLink publishLink;
	
	private final String name;
	private final boolean published;
	private final TeamsRecording recording;
	
	public TeamsRecordingRow(String name, TeamsRecording recording, boolean published) {
		this.published = published;
		this.recording = recording;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Date getStart() {
		return recording.getStartDate();
	}
	
	public Date getEnd() {
		return recording.getEndDate();
	}
	
	public Boolean getPermanent() {
		return recording.getPermanent();
	}
	
	public TeamsRecording getRecording() {
		return recording;
	}

	public boolean isPublished() {
		return published;
	}
	
	public TeamsRecordingsPublishedRoles[] getPublishToEnum() {
		return recording.getPublishToEnum();
	}

	public FormLink getPublishLink() {
		return publishLink;
	}

	public void setPublishLink(FormLink publishLink) {
		this.publishLink = publishLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

}
