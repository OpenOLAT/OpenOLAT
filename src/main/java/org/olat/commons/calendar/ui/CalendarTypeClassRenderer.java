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
package org.olat.commons.calendar.ui;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarTypeClassRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof KalendarRenderWrapper) {
			KalendarRenderWrapper wrapper = (KalendarRenderWrapper)cellValue;
			if(wrapper.isImported()) {
				renderImported(target);
			} else {
				renderType(target, wrapper.getKalendar().getType());
			}
		} else if(cellValue instanceof String) {
			renderType(target, (String)cellValue);
		}
	}
	
	private void renderImported(StringOutput target) {
		target.append(" <i class='o_icon o_icon_external_link'> </i>");
	}
	
	private void renderType(StringOutput target, String type) {
		if(CalendarManager.TYPE_USER.equals(type)) {
			target.append("<i class='o_icon o_icon_user'> </i>");
		} else if(CalendarManager.TYPE_GROUP.equals(type)) {
			target.append("<i class='o_icon o_icon_group'> </i>");
		} else if(CalendarManager.TYPE_COURSE.equals(type)) {
			target.append("<i class='o_icon o_CourseModule_icon'> </i>");
		} else {
			target.append("<i class='o_circle ").append(type).append("'> </i>");
		}
	}
}
