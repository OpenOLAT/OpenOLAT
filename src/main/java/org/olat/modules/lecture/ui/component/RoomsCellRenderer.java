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
package org.olat.modules.lecture.ui.component;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Room;

/**
 * Renders a list of rooms as "<reference> · <description>" entries,
 * where description is shown only when it differs from the reference.
 *
 * Initial date: 3 Jul 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomsCellRenderer implements FlexiCellRenderer {

	@Override
	@SuppressWarnings("unchecked")
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if (!(cellValue instanceof List)) {
			return;
		}
		List<Room> rooms = (List<Room>) cellValue;
		if (rooms.isEmpty()) {
			return;
		}

		boolean first = true;
		for (Room room : rooms) {
			String ref = room.getExternalRef();
			String desc = room.getDescription();
			boolean hasRef = StringHelper.containsNonWhitespace(ref);
			boolean hasDesc = StringHelper.containsNonWhitespace(desc);
			if (!hasRef && !hasDesc) {
				continue;
			}
			if (!first) {
				target.append(", ");
			}
			first = false;
			if (hasRef) {
				target.append("<span>").appendHtmlEscaped(ref).append("</span>");
			}
			if (hasDesc && !(hasRef && desc.equals(ref))) {
				target.append("<span class=\"text-muted\"> &middot; ").appendHtmlEscaped(desc).append("</span>");
			}
		}
	}
}
