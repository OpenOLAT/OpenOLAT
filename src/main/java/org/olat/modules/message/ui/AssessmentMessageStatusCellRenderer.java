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
package org.olat.modules.message.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.message.AssessmentMessageStatusEnum;

/**
 * 
 * Initial date: 15 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public AssessmentMessageStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof AssessmentMessageStatusEnum) {
			AssessmentMessageStatusEnum status = (AssessmentMessageStatusEnum)cellValue;
			if(status == AssessmentMessageStatusEnum.planned) {
				renderStatus(target, "status.planned", "o_im_status_requested");
			} else if(status == AssessmentMessageStatusEnum.published) {
				renderStatus(target, "status.published", "o_im_status_active");
			} else if(status == AssessmentMessageStatusEnum.expired) {
				renderStatus(target, "status.expired", "o_im_status_completed");
			}
		}
	}
	
	private void renderStatus(StringOutput sb, String i18n, String cssClass) {
		sb.append("<span class='o_labeled ").append(cssClass).append("'>").append(translator.translate(i18n)).append("</span>");
	}
}
