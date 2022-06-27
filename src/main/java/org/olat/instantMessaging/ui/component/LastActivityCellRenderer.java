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
package org.olat.instantMessaging.ui.component;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 24 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LastActivityCellRenderer implements FlexiCellRenderer {
	
	private final Date now = new Date();
	private final Formatter formatter;
	private final Translator translator;
	
	public LastActivityCellRenderer(Translator translator) {
		formatter = Formatter.getInstance(translator.getLocale());
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof Date) {
			Date lastActivity = (Date)cellValue;
			if(DateUtils.isSameDay(lastActivity, now)) {
				String time = formatter.formatTimeShort(lastActivity);
				target.append(translator.translate("today.time", time));
			} else if(DateUtils.countDays(lastActivity, now) == 1) {
				String time = formatter.formatTimeShort(lastActivity);
				target.append(translator.translate("yesterday.time", time));
			} else {
				String date = formatter.formatDate(lastActivity);
				target.append(date);
			}
		}
	}
}
