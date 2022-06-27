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

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.nodes.practice.ui.PracticeParticipantTaxonomyStatisticsRow;
import org.olat.course.nodes.practice.ui.PracticeResourceTaxonomyRow;

/**
 * 
 * Initial date: 1 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeTaxonomyCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof PracticeResourceTaxonomyRow) {
			PracticeResourceTaxonomyRow val = (PracticeResourceTaxonomyRow)cellValue;
			render(target, val.getTaxonomyLevel(), val.getTaxonomyPath());
		} else if(cellValue instanceof PracticeParticipantTaxonomyStatisticsRow) {
			PracticeParticipantTaxonomyStatisticsRow val = (PracticeParticipantTaxonomyStatisticsRow)cellValue;
			render(target, val.getTaxonomyLevelName(), val.getTaxonomyPath());
		}
	}
	
	private void render(StringOutput target, String level, List<String> path) {
		if(path != null && !path.isEmpty()) {
			target.append("<small class='text-muted'>");
			for(int i=0; i<path.size(); i++) {
				if(i > 0) {
					target.append(" / ");
				}
				target.append(path.get(i));
			}
			target.append("</small><br>");
		}
		target.append("<strong>").append(level).append("</strong>");
	}
}
