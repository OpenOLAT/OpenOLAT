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
package org.olat.course.nodes.videotask.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 31 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DurationFlexiCellRenderer implements FlexiCellRenderer {

	private static final long ONE_HOUR_IN_MILLISEC = 60l * 60l * 1000l;
	private static final long ONE_MINUTE_IN_MILLISEC = 60l * 1000l;
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		
		if(cellValue == null) {
			target.append("<span class='o_ochre'>").append(translator.translate("duration.open")).append("</span>");
		} else if(cellValue instanceof Long lduration) {
			long durationMillisec = lduration.longValue();
			
			long hours = (durationMillisec / ONE_HOUR_IN_MILLISEC) % 24;
			long minutes = ((durationMillisec - (hours * ONE_HOUR_IN_MILLISEC)) / ONE_MINUTE_IN_MILLISEC) % 60;
			long seconds = ((durationMillisec - (hours * ONE_HOUR_IN_MILLISEC) - (minutes * ONE_MINUTE_IN_MILLISEC)) / 1000l) % 60;

			if(hours >= 0) {
				target.append(" ").append(hours).append(translator.translate("duration.h"));
			}
			target.append(" ").append(minutes).append(translator.translate("duration.m"));
			if(seconds >= 0) {
				target.append(" ").append(seconds).append(translator.translate("duration.s"));
			}
		}
	}
}
