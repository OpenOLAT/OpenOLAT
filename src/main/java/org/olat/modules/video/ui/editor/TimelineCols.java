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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum TimelineCols implements FlexiColumnDef {
	startTime("table.header.timeline.startTime"),
	type("table.header.timeline.type"),
	text("table.header.timeline.text"),
	color("table.header.timeline.color");

	private final String i18nHeaderKey;

	TimelineCols(String i18nHeaderKey) {
		this.i18nHeaderKey = i18nHeaderKey;
	}

	@Override
	public String i18nHeaderKey() {
		return i18nHeaderKey;
	}

	@Override
	public String iconHeader() {
		return FlexiColumnDef.super.iconHeader();
	}
}
