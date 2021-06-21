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
package org.olat.course.assessment.ui.tool;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 08.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator trans;
	
	public AssessmentStatusCellRenderer(Locale locale) {
		trans = Util.createPackageTranslator(AssessmentStatusCellRenderer.class, locale);
	}
	
	public AssessmentStatusCellRenderer(Translator trans) {
		this.trans = trans;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof AssessmentEntryStatus) {
			AssessmentEntryStatus status = (AssessmentEntryStatus)cellValue;
			switch(status) {
				case notReady: render(renderer, target, "o_icon_status_not_ready", "assessment.status.notReady"); break;
				case notStarted: render(renderer, target, "o_icon_status_not_started", "assessment.status.notStart"); break;
				case inProgress: render(renderer, target, "o_icon_status_in_progress", "assessment.status.inProgress"); break;
				case inReview: render(renderer, target, "o_icon_status_in_review", "assessment.status.inReview"); break;
				case done: render(renderer, target, "o_icon_status_done", "assessment.status.done"); break;
			}	
		} else {
			target.append("-");
		}
	}
	
	private void render(Renderer renderer, StringOutput target, String iconCss, String i18nKey) {
		if(renderer != null) {
			target.append("<i class='o_icon ").append(iconCss).append(" o_icon-fw'> </i> ");
		}
		target.append(trans.translate(i18nKey));
	}
}