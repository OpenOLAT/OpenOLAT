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
package org.olat.course.certificate.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 22 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecertificationInDaysFlexiCellRenderer implements FlexiCellRenderer {

	private final Formatter formatter;
	private final Translator translator;
	
	public RecertificationInDaysFlexiCellRenderer(Translator translator) {
		this.translator = translator;
		formatter = Formatter.getInstance(translator.getLocale());
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof RecertificationInDays recertification
				&& recertification.nextRecertificationDate() != null) {
			Long nextDays = recertification.days();
			Date nextRecertificationDate = recertification.nextRecertificationDate();
			if(nextRecertificationDate != null) {
				target.append(formatter.formatDate(nextRecertificationDate));
			}
			
			if(nextDays != null) {
				target.append(" &ndash; ");
				
				if(nextDays.longValue() > 0l) {
					target.append(translator.translate("recertification.in", nextDays.toString()));
				} else if(nextDays.longValue() == 0l) {
					target.append(translator.translate("recertification.today"));
				} else if(nextDays.longValue() == 1l) {
					target.append(translator.translate("recertification.tomorrow"));
				} else if(nextDays.longValue() == -1l) {
					target.append(translator.translate("recertification.yesterday"));
				} else if(nextDays.longValue() < 0l) {
					target.append(translator.translate("recertification.since", Long.toString(Math.abs(nextDays.longValue()))));
				}
			}
		}
	}
}
