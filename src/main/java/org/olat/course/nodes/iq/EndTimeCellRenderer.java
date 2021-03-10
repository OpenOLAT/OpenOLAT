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
package org.olat.course.nodes.iq;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
 * Initial date: 26 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EndTimeCellRenderer implements FlexiCellRenderer {
	
	private final Date endDate;
	private final int timeLimitInSeconds;
	private final Formatter formatter;
	
	public EndTimeCellRenderer(int timeLimitInSeconds, Date endDate, Locale locale) {
		this.endDate = endDate;
		this.timeLimitInSeconds = timeLimitInSeconds;
		formatter = Formatter.getInstance(locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof ExtraInfos) {
			ExtraInfos infos = (ExtraInfos)cellValue;
			Integer extraTimeInSeconds = infos.getExtraTimeInSeconds();
			Integer compensationExtraTimeInSeconds = infos.getCompensationExtraTimeInSeconds();
			
			if(infos.getStart() != null) {
				long totalTimeMs = timeLimitInSeconds * 1000l;
				if(endDate != null) {
					long leadingTimeInMs = endDate.getTime() - infos.getStart().getTime();
					if(timeLimitInSeconds > 0) {
						totalTimeMs = Math.min(totalTimeMs, leadingTimeInMs);
					} else {
						totalTimeMs = leadingTimeInMs;
					}
				}

				if(extraTimeInSeconds != null) {
					totalTimeMs += (extraTimeInSeconds * 1000l);
				}
				if(compensationExtraTimeInSeconds != null) {
					totalTimeMs += (compensationExtraTimeInSeconds * 1000l);
				}
				
				Calendar now = Calendar.getInstance();
				Calendar cal = Calendar.getInstance();
				cal.setTime(infos.getStart());
				cal.add(Calendar.MILLISECOND, (int)totalTimeMs);
				Date dueDate = cal.getTime();
				if(DateUtils.isSameDay(now, cal)) {
					target.append(formatter.formatTime(dueDate));
				} else {
					target.append(formatter.formatDateAndTime(dueDate));
				}
			} else if(infos.getEnd() != null) {
				Date now = new Date();
				Date end = infos.getEnd();
				if(DateUtils.isSameDay(now, end)) {
					target.append(formatter.formatTime(end));
				} else {
					target.append(formatter.formatDateAndTime(end));
				}
			}
		}
	}
}
