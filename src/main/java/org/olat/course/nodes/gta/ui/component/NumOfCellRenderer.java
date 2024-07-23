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
package org.olat.course.nodes.gta.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.ui.peerreview.CoachPeerReviewRow.NumOf;

/**
 * 
 * Initial date: 10 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NumOfCellRenderer implements FlexiCellRenderer {
	
	private final boolean withWarning;
	private final String warningText;
	
	public NumOfCellRenderer(boolean withWarning, String warningText) {
		this.withWarning = withWarning;
		this.warningText = warningText;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof NumOf numOf) {
			target.append("<span>");
			if(withWarning && numOf.number() < numOf.reference()) {
				target.append("<i class='o_icon o_icon-fw o_icon_important'");
				if(StringHelper.containsNonWhitespace(warningText)) {
					target.append(" title=\"").append(warningText).append("\"");
				}
				target.append("> </i> ");
			}
			target.append(numOf.number()).append("/").append(numOf.reference()).append("</span>");
		}
	}
}
