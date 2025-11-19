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
package org.olat.modules.certificationprogram.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.ui.CertificationProgramNotificationRow;

/**
 * 
 * Initial date: 18 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReminderTimeCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public ReminderTimeCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof CertificationProgramNotificationRow notificationRow && notificationRow.getDuration() != null) {
			if(notificationRow.getType() == CertificationProgramMailType.reminder_upcoming) {
				renderDuration(target, notificationRow.getDuration());
				target.append(" ").append(translator.translate("reminder.time.upcoming.addon"));
			} else if(notificationRow.getType() == CertificationProgramMailType.reminder_overdue) {
				renderDuration(target, notificationRow.getDuration());
				target.append(" ").append(translator.translate("reminder.time.overdue.addon"));
			}
		}
	}
	
	private void renderDuration(StringOutput target, Duration duration) {
		int value = duration.value();
		String i18nUnit = value <= 1
				? duration.unit().i18nPeriodSingular()
				: duration.unit().i18nPeriodPlural();
		target.append(translator.translate(i18nUnit, Integer.toString(value)));
	}
}