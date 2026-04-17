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
package org.olat.modules.ceditor.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.ceditor.model.jpa.TocPart;

/**
 * Initial date: 15 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TocRunController extends BasicController {

	/**
	 * A single entry in the rendered TOC list.
	 */
	public static class TitleEntry {
		private final Long key;
		private final String text;
		private final int indent;

		public TitleEntry(Long key, String text, int indent) {
			this.key = key;
			this.text = text;
			this.indent = indent;
		}

		public Long getKey() {
			return key;
		}

		public String getText() {
			return text;
		}

		public int getIndent() {
			return indent;
		}
	}

	public TocRunController(UserRequest ureq, WindowControl wControl, TocPart tocPart, List<TitleEntry> entries, String title) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("toc_run");
		mainVC.contextPut("title", title);
		mainVC.contextPut("entries", entries);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}