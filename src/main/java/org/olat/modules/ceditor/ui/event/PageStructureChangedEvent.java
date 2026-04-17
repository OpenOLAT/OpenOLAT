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
package org.olat.modules.ceditor.ui.event;

import org.olat.core.gui.control.Event;

/**
 * Fired by {@link org.olat.modules.ceditor.ui.PageEditorV2Controller} to all editor controllers
 * whenever the page structure changes (element moved, reordered via drag-and-drop or move buttons).
 * Editor controllers that depend on the relative position of other elements (e.g. the TOC element)
 * should handle this event to refresh their view.
 *
 * Initial date: 16 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PageStructureChangedEvent extends Event {

	private static final long serialVersionUID = 6831204759345180293L;

	public static final String PAGE_STRUCTURE_CHANGED = "page-structure-changed";

	public PageStructureChangedEvent() {
		super(PAGE_STRUCTURE_CHANGED);
	}
}