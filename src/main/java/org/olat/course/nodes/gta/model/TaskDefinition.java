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
package org.olat.course.nodes.gta.model;

import java.io.Serializable;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskDefinition implements Serializable {

	private static final long serialVersionUID = 7379460034157198296L;
	
	private String title;
	private String description;
	private String filename;
	private boolean inTranscoding;
	
	public TaskDefinition() {
		//
	}
	
	public static TaskDefinition fromFile(String filename) {
		TaskDefinition def = new TaskDefinition();
		def.setFilename(filename);
		def.setTitle(filename);
		return def;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	@Override
	public int hashCode() {
		return (title == null ? 7215649 : title.hashCode())
				+ (filename == null ? -212459 : filename.hashCode());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof TaskDefinition) {
			TaskDefinition taskDef = (TaskDefinition)obj;
			return ((title == null && taskDef.title == null) || (title != null && title.equals(taskDef.title)))
					&& ((filename == null && taskDef.filename == null) || (filename != null && filename.equals(taskDef.filename)));
		}
		return false;
	}

	public boolean isInTranscoding() {
		return inTranscoding;
	}

	public void setInTranscoding(boolean inTranscoding) {
		this.inTranscoding = inTranscoding;
	}
}
