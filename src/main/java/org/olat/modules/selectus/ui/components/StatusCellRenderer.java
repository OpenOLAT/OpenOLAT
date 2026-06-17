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
package org.olat.modules.selectus.ui.components;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterIconRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class StatusCellRenderer implements CustomCellRenderer, FlexiCellRenderer, FlexiTableFilterIconRenderer {

	private Translator translator;
	
	public StatusCellRenderer() {
		//
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		render(target, cellValue, translator.getLocale());
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		render(sb, val, locale);
	}
	
	@Override
	public void render(StringOutput target, FlexiTableFilter filter, FlexiTableComponent source, Translator translator) {
		renderIcons(target, filter.getFilter());
	}

	private void render(StringOutput sb, Object val, Locale locale) {
		if(translator == null) {
			translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
		}
			
		if(val instanceof Position) {
			Position position = (Position)val;
			val = position.getStatus();
		}
		
		if(val instanceof String) {
			renderIcons(sb, (String)val);
			sb.append(translator.translate("status." + val));
		}
	}
	
	private void renderIcons(StringOutput sb, String val) {
		PositionStatus status = PositionStatus.valueOf(val);
		switch(status) {
			case preparation:
				sb.append("<i class='o_icon o_position_status_filter o_preparation'> </i> ");
				break;
			case published:
				sb.append("<i class='o_icon o_position_status_filter o_published'> </i> ");
				break;
			case publishedAndInScreening:
				sb.append("<span class='o_icon-stack o_position_status'>")
				  .append("<i class='o_icon o_icon-stack-1x o_position_status_filter o_published_screening_back'></i>")
				  .append("<i class='o_icon o_icon-rotate-180 o_icon-stack-1x o_position_status_filter o_published_screening'></i>")
				  .append("</span> ");
				break;
			case closedAndInScreening:
				sb.append("<span class='o_icon-stack o_position_status'>")
				  .append("<i class='o_icon o_icon-stack-1x o_position_status_filter o_closed_screening_back'></i>")
				  .append("<i class='o_icon o_icon-stack-1x o_position_status_filter o_closed_screening'></i>")
				  .append("</span> ");
				break;
			case closedAndNoRating:
				sb.append("<i class='o_icon o_position_status_filter o_closed_no_rating'> </i> ");
				break;
			case closed:
				sb.append("<i class='o_icon o_position_status_filter o_closed'> </i> ");
				break;
			case reporting:
				sb.append("<i class='o_icon o_position_status_filter o_reporting'> </i> ");
				break;
		}
	}
}
