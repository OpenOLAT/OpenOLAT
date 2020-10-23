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
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 20 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExtraTimeCellRenderer implements FlexiCellRenderer {
	
	private final Date endDate;
	private final boolean renderDueDate;
	private final int timeLimitInSeconds;
	private final Formatter formatter;
	
	public ExtraTimeCellRenderer(boolean renderDueDate, int timeLimitInSeconds, Date endDate, Locale locale) {
		this.endDate = endDate;
		this.renderDueDate = renderDueDate;
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
			
			if(renderDueDate) {
				if(infos.getStart() != null) {
					int totalTime = timeLimitInSeconds;
					if(endDate != null) {
						long leadingTimeInMilliSeconds = endDate.getTime() - infos.getStart().getTime();
						int leadingTime = Math.round(leadingTimeInMilliSeconds / 1000f);
						if(timeLimitInSeconds > 0) {
							totalTime = Math.min(totalTime, leadingTime);
						} else {
							totalTime = leadingTime;
						}
					}
	
					if(extraTimeInSeconds != null) {
						totalTime += extraTimeInSeconds;
					}
					if(compensationExtraTimeInSeconds != null) {
						totalTime += compensationExtraTimeInSeconds;
					}
					
					Calendar now = Calendar.getInstance();
					Calendar cal = Calendar.getInstance();
					cal.setTime(infos.getStart());
					cal.add(Calendar.SECOND, totalTime);
					Date dueDate = cal.getTime();

					boolean sameDay = now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
							&& now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR);
					if(sameDay) {
						target.append(formatter.formatTime(dueDate));
					} else {
						target.append(formatter.formatDateAndTime(dueDate));
					}
				}
			}  else {
				if(extraTimeInSeconds != null) {
					int extraTimeInMinutes = extraTimeInSeconds.intValue() / 60;
					target.append("<i class='o_icon o_icon_extra_time'> </i> ").append("+").append(extraTimeInMinutes).append("m");
				}
				if(compensationExtraTimeInSeconds != null) {
					int extraTimeInMinutes = compensationExtraTimeInSeconds.intValue() / 60;
					target.append("<i class='o_icon o_icon_disadvantage_compensation'> </i> ").append("+").append(extraTimeInMinutes).append("m");
				}
			}
		}
	}
}
