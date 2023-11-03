/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.ui;

import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.course.nodes.CourseNodeWithDefaults;

/**
 * Initial date: Nov 01, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseNodeDefaultConfigRow {

	private final String courseElement;

	private final FormToggle enabledToggle;
	private final ExternalLinkItem externalManualLinkItem;

	private CourseNodeWithDefaults courseNodeWithDefaults;

	public CourseNodeDefaultConfigRow(String courseElement, FormToggle enabledToggle, ExternalLinkItem externalManualLinkItem) {
		this.courseElement = courseElement;
		this.enabledToggle = enabledToggle;
		this.externalManualLinkItem = externalManualLinkItem;
	}

	public String getCourseElement() {
		return courseElement;
	}

	public FormToggle getEnabledToggle() {
		return enabledToggle;
	}

	public ExternalLinkItem getExternalManualLinkItem() {
		return externalManualLinkItem;
	}

	public CourseNodeWithDefaults getCourseNodeWithDefaults() {
		return courseNodeWithDefaults;
	}

	public void setCourseNodeWithDefaults(CourseNodeWithDefaults courseNodeWithDefaults) {
		this.courseNodeWithDefaults = courseNodeWithDefaults;
	}
}
