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
package org.olat.course.member;

import java.util.Set;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.member.model.OriginCoursePlannerRow;

/**
 * Initial date: 2026-03-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class DynamicCurriculumElementCellRenderer implements FlexiCellRenderer {
	private final StaticFlexiCellRenderer staticFlexiCellRenderer;
	private final CurriculumElementCellRenderer nameRenderer;
	private final Set<Long> accessibleKeys;

	public DynamicCurriculumElementCellRenderer(String action,
												CurriculumElementCellRenderer nameRenderer,
												Set<Long> accessibleKeys) {
		this.staticFlexiCellRenderer = new StaticFlexiCellRenderer(action, nameRenderer);
		this.nameRenderer = nameRenderer;
		this.accessibleKeys = accessibleKeys;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof OriginCoursePlannerRow originCoursePlannerRow) {
			if (accessibleKeys.contains(originCoursePlannerRow.elementKey())) {
				staticFlexiCellRenderer.render(renderer, target, originCoursePlannerRow.elementName(), row, source, ubu, translator);
			} else {
				nameRenderer.render(renderer, target, originCoursePlannerRow.elementName(), row, source, ubu, translator);
			}
		}
	}
}
