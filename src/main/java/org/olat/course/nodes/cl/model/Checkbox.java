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
package org.olat.course.nodes.cl.model;

import java.io.Serializable;
import java.util.UUID;

import org.olat.course.nodes.cl.ui.CheckboxLabelEnum;
import org.olat.course.nodes.cl.ui.CheckboxReleaseEnum;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Checkbox implements Serializable {
	
	private static final long serialVersionUID = 7361576695269164580L;

	private String title;
	private Float points;
	private String checkboxId;
	private CheckboxReleaseEnum release;
	private CheckboxLabelEnum label;
	private String description;
	private String filename;
	
	public Checkbox() {
		//
	}

	/**
	 * This is an UUID
	 * @return
	 */
	public String getCheckboxId() {
		return checkboxId;
	}

	public void setCheckboxId(String checkboxId) {
		this.checkboxId = checkboxId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Float getPoints() {
		return points;
	}

	public void setPoints(Float points) {
		this.points = points;
	}

	public CheckboxReleaseEnum getRelease() {
		return release;
	}

	public void setRelease(CheckboxReleaseEnum release) {
		this.release = release;
	}

	public CheckboxLabelEnum getLabel() {
		return label;
	}

	public void setLabel(CheckboxLabelEnum label) {
		this.label = label;
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
	public Checkbox clone() {
		Checkbox clone = new Checkbox();
		clone.title = title;
		clone.points = points;
		clone.checkboxId = UUID.randomUUID().toString();
		clone.release = release;
		clone.label = label;
		clone.description = description;
		clone.filename = filename;
		return clone;
	}
}
