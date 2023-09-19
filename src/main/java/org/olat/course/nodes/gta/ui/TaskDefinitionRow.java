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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.nodes.gta.model.TaskDefinition;

/**
 * 
 * Initial date: 26.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskDefinitionRow {
	
	private final TaskDefinition taskDefinition;
	private final DownloadLink downloadLink;
	private final FormLink openLink;
	
	public TaskDefinitionRow(TaskDefinition taskDefinition, DownloadLink downloadLink, FormLink openLink) {
		this.taskDefinition = taskDefinition;
		this.downloadLink = downloadLink;
		this.openLink = openLink;
	}
	
	public DownloadLink getDownloadLink() {
		return downloadLink;
	}

	public TaskDefinition getTaskDefinition() {
		return taskDefinition;
	}

	public FormLink getOpenLink() {
		return openLink;
	}

}
