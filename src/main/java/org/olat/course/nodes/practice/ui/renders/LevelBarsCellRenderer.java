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
package org.olat.course.nodes.practice.ui.renders;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.nodes.practice.ui.Levels;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LevelBarsCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Levels) {
			Levels levels = (Levels)cellValue;
			target.append("<div class='progress' style='width:100%;'>");
			for(int i=1; i<=levels.getNumOfLevels(); i++) {
				int val = levels.getLevel(i);
				if(val > 0) {
					render(target, "o_practice_progress_l" + i, val, levels.getTotal());
				}
			}
			if(levels.getNot() > 0) {
				render(target, "o_practice_progress_not", levels.getNot(), levels.getTotal());
			}
			target.append("</div>");
		}
	}
	
	private void render(StringOutput target, String cssClass, int val, int total) {
		double percentValue = 0.0d;
		if(total > 0) {
			percentValue = (val / (double)total) * 100.0d;
		}
		target.append("<div class='progress-bar ").append(cssClass).append("' style='width:").append(percentValue)
		      .append("%' title=\"").append(percentValue).append("%\">")
		      .append("</div>");
	}
}
