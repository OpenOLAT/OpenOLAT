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
package org.olat.modules.portfolio.ui.renderer;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.SectionStatus;

/**
 * 
 * Initial date: 06.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public StatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(cellValue instanceof PageStatus) {
			PageStatus status = (PageStatus)cellValue;
			String statusText = translator.translate(status.i18nKey());
			target.append("<span class='o_labeled_light ").append(status.statusClass()).append("'>")
				  .append("<i class='o_icon ").append(status.iconClass()).append(" o_icon-fw' title='").append(statusText).append("'> </i>")
				  .append(statusText)
			      .append("</span");
		} else if(cellValue instanceof SectionStatus) {
			SectionStatus status = (SectionStatus)cellValue;
			String statusText = translator.translate(status.i18nKey());
			target.append("<span class='o_labeled_light ").append(status.statusClass()).append("'>")
				  .append("<i class='o_icon ").append(status.iconClass()).append(" o_icon-fw' title='").append(statusText).append("'> </i>")
				  .append(statusText)
			      .append("</span");
		}
	}
}
