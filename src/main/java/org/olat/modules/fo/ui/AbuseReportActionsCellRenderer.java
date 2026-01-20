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
package org.olat.modules.fo.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.fo.ui.AbuseReportAdminController.AbuseReportRow;

/**
 * Cell renderer for abuse report actions column.
 * 
 * Initial date: January 2026<br>
 * @author OpenOLAT Community
 */
public class AbuseReportActionsCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		if (cellValue instanceof AbuseReportRow) {
			AbuseReportRow reportRow = (AbuseReportRow) cellValue;
			
			target.append("<div class='o_button_group'>");
			
			// Dismiss button
			if (reportRow.getDismissLink() != null) {
				reportRow.getDismissLink().getComponent().getHTMLRendererSingleton()
					.render(renderer, target, reportRow.getDismissLink().getComponent(), ubu, translator, null, null);
			}
			
			// Take action button
			if (reportRow.getActionLink() != null) {
				target.append(" ");
				reportRow.getActionLink().getComponent().getHTMLRendererSingleton()
					.render(renderer, target, reportRow.getActionLink().getComponent(), ubu, translator, null, null);
			}
			
			target.append("</div>");
		}
	}
}
