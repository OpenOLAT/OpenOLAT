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
package org.olat.core.gui.components.sections;

import org.olat.core.gui.components.Component;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class Section {

	private final String id;
	private final String title;
	private final Component content;
	private final boolean initiallyOpen;

	public Section(String id, String title, Component content) {
		this(id, title, content, true);
	}

	public Section(String id, String title, Component content, boolean initiallyOpen) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.initiallyOpen = initiallyOpen;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public Component getContent() {
		return content;
	}

	public boolean isInitiallyOpen() {
		return initiallyOpen;
	}

}
