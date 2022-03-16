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
package org.olat.modules.grade.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.modules.grade.PerformanceClass;

/**
 * 
 * Initial date: 21 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PerformanceClassRow {
	
	private final PerformanceClass performanceClass;
	private Integer position;
	private String identifier;
	private FormLink nameLink;
	private MultipleSelectionElement markPassedEl;
	
	public PerformanceClassRow(PerformanceClass performanceClass) {
		this.performanceClass = performanceClass;
	}

	public PerformanceClass getPerformanceClass() {
		return performanceClass;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public FormLink getNameLink() {
		return nameLink;
	}

	public void setNameLink(FormLink nameLink) {
		this.nameLink = nameLink;
	}

	public MultipleSelectionElement getMarkPassedEl() {
		return markPassedEl;
	}

	public void setMarkPassedEl(MultipleSelectionElement markPassedEl) {
		this.markPassedEl = markPassedEl;
	}
	
}
