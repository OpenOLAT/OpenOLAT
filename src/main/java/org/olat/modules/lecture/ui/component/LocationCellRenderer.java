/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.model.LectureBlockRow;

/**
 * 
 * Initial date: 25 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LocationCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public LocationCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof LectureBlockRow blockRow) {
			boolean onlineMeeting = blockRow.hasOnlineMeeting();
			boolean location = StringHelper.containsNonWhitespace(blockRow.getLocation());	
			if(onlineMeeting || location) {
				target.append("<span><i class='o_icon o_icon-fw ").append("o_vc_icon", "o_icon_location", onlineMeeting).append("'> </i> ");
				if(StringHelper.containsNonWhitespace(blockRow.getLocation())) {
					target.appendHtmlEscaped(blockRow.getLocation());
				} else if(StringHelper.containsNonWhitespace(blockRow.getMeetingTitle())) {
					target.appendHtmlEscaped(blockRow.getMeetingTitle());
				} else if(onlineMeeting) {
					target.append(translator.translate("lecture.online.meeting"));
				}
				target.append("</span>");
			}
		}
	}
}
