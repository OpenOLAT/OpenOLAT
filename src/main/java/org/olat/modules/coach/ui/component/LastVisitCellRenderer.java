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
package org.olat.modules.coach.ui.component;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LastVisitCellRenderer implements FlexiCellRenderer {
	
	private static final long DAY = 24 * 60 * 60 * 1000;
	private static final long HOUR = 60 * 60 * 1000;
	
	private final Translator translator;
	
	public LastVisitCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Date date) {
			render(target, date);
		}
	}
	
	private void render(StringOutput target, Date date) {
		Date now = new Date();
		
		long diff = now.getTime() - date.getTime();
		if(diff < 0) {
			target.append(translator.translate("last.visit.hours", "0"));
		} else if(diff < DAY) {
			long numOfHours = diff / HOUR;
			target.append(translator.translate("last.visit.hours", Long.toString(numOfHours)));
		} else {
			long numOfDays = DateUtils.countDays(date, now);
			target.append(translator.translate("last.visit.days", Long.toString(numOfDays)));
		}
	}
}
