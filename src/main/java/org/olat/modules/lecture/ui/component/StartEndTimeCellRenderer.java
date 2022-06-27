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
package org.olat.modules.lecture.ui.component;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.modules.lecture.LectureBlock;

/**
 * 
 * Initial date: 7 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StartEndTimeCellRenderer implements FlexiCellRenderer {
	
	private final Formatter format;
	
	public StartEndTimeCellRenderer(Locale locale) {
		format = Formatter.getInstance(locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		
		if(cellValue instanceof LectureBlock) {
			LectureBlock lectureBlock = (LectureBlock)cellValue;
			Date startDate = lectureBlock.getStartDate();
			Date endDate = lectureBlock.getEndDate();
			target.append(format.formatTimeShort(startDate))
		          .append(" - ")
		          .append(format.formatTimeShort(endDate));
		}
	}
}
