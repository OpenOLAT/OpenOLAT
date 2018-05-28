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

import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.assessment.ui.ToReviewRow;

/**
 * 
 * Initial date: 09.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ElementToReviewCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	private final Map<String,String> nodeIdentToNodeShortTitles;
	
	public ElementToReviewCellRenderer(Map<String,String> nodeIdentToNodeShortTitles, Translator translator) {
		this.nodeIdentToNodeShortTitles = nodeIdentToNodeShortTitles;
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(cellValue instanceof ToReviewRow) {
			ToReviewRow rowToReview = (ToReviewRow)cellValue;
			List<String> elements = rowToReview.getSubIndents();
			if(elements.size() == 1) {
				String msg = nodeIdentToNodeShortTitles.get(elements.get(0));
				target.append(msg != null ? msg : "");
			} else {
				String msg = translator.translate("elements.to.review", new String[]{ Integer.toString(elements.size()) });
				target.append("<i class='o_icon o_icon_important'> </i> ");
				target.append(msg);
			}
		}
	}
}
