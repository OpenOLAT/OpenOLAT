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
package org.olat.modules.curriculum.ui.widgets;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;

/**
 * 
 * Initial date: 19 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LectureBlockWidgetStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public LectureBlockWidgetStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(cellValue instanceof LectureBlockWidgetRow block) {
			getStatus(target, "o_labeled_light", block.getVirtualStatus(), block.isNextScheduledEvent(), translator);
		}
	}
	
	private static final void getStatus(StringOutput target, String type, LectureBlockVirtualStatus vStatus, boolean nextScheduled, Translator trans) {
		if(vStatus == LectureBlockVirtualStatus.RUNNING || nextScheduled) {
			String statusName;
			if(nextScheduled && vStatus != LectureBlockVirtualStatus.RUNNING) {
				statusName = "next";
			} else {
				statusName = vStatus.name().toLowerCase();
			}
			target.append("<span class=\"").append(type).append(" o_lecture_status_")
			      .append(statusName).append("\" title=\"").append(trans.translate(statusName))
			      .append("\">").append(trans.translate(statusName))
			      .append("</span>");
		}
	}
}
