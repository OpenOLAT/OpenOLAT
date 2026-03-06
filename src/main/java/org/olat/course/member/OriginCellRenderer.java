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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.group.BusinessGroupShort;
import org.olat.group.ui.main.BusinessGroupNameCellRenderer;
import org.olat.group.ui.main.CourseMembership;
import org.olat.group.ui.main.MemberRow;
import org.olat.modules.curriculum.CurriculumElementShort;

/**
 * Initial date: 2026-01-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OriginCellRenderer implements FlexiCellRenderer {

	private static final int MAX_ITEMS = 5;

	private final String openDetailsAction;

	public OriginCellRenderer(String openDetailsAction) {
		this.openDetailsAction = openDetailsAction;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof MemberRow memberRow) {
			CourseMembership courseMembership = memberRow.getMembership();
			Set<CurriculumElementShort> curriculumElements = new HashSet<>(memberRow.getCurriculumElements() != null ? 
					memberRow.getCurriculumElements() : List.of());

			int totalItems = (courseMembership.isRepositoryEntryMember() ? 1 : 0)
					+ (courseMembership.isBusinessGroupMember() ? memberRow.getGroups().size() : 0)
					+ (courseMembership.isCurriculumElementMember() ? curriculumElements.size() : 0);

			int renderedItems = 0;
			target.append("<div class='o_origin_cell'>");
			
			if (courseMembership.isRepositoryEntryMember()) {
				target.append("<i class='o_icon o_CourseModule_icon'> </i> ");
				target.append(translator.translate("course"));
				renderedItems++;
			}
			
			if (courseMembership.isBusinessGroupMember()) {
				FlexiCellRenderer businessGroupRenderer = new BusinessGroupNameCellRenderer();
				for (BusinessGroupShort businessGroup : memberRow.getGroups()) {
					if (renderedItems >= MAX_ITEMS) {
						break;
					}
					if (renderedItems > 0) {
						target.append(", ");
					}
					businessGroupRenderer.render(renderer, target, businessGroup, row, source, ubu, translator);
					renderedItems++;
				}
			}

			if (courseMembership.isCurriculumElementMember()) {
				FlexiCellRenderer curriculumElementRenderer = new CurriculumElementCellRenderer();
				for (CurriculumElementShort curriculumElement : curriculumElements) {
					if (renderedItems >= MAX_ITEMS) {
						break;
					}
					if (renderedItems > 0) {
						target.append(", ");
					}
					curriculumElementRenderer.render(renderer, target, curriculumElement.getDisplayName(), row, source, ubu, translator);
					renderedItems++;
				}
			}

			if (totalItems > MAX_ITEMS) {
				int remaining = totalItems - MAX_ITEMS;
				target.append(" ");
				new StaticFlexiCellRenderer(openDetailsAction, new TextFlexiCellRenderer())
						.render(renderer, target, "+" + remaining, row, source, ubu, translator);
			}

			target.append("</div>");
		}
	}
}