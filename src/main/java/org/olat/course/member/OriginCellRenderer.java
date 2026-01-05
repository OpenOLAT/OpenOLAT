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
import java.util.Set;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
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

	public static final String BUSINESS_GROUP_ACTION_PREFIX = "businessGroupAction_";
	public static final String CURRICULUM_ELEMENT_ACTION_PREFIX = "curriculumElementAction_";
	
	public OriginCellRenderer() {
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof MemberRow memberRow) {
			CourseMembership courseMembership = memberRow.getMembership();
			boolean atLeastOneItem = false;
			target.append("<div class='o_origin_cell'>");
			
			if (courseMembership.isRepositoryEntryMember()) {
				target.append(translator.translate("course"));
				atLeastOneItem = true;
			}
			
			if (courseMembership.isBusinessGroupMember()) {
				for (BusinessGroupShort businessGroup : memberRow.getGroups()) {
					if (atLeastOneItem) {
						target.append(", ");
					}
					String action = BUSINESS_GROUP_ACTION_PREFIX + businessGroup.getKey();
					FlexiCellRenderer businessGroupRenderer = new StaticFlexiCellRenderer(action, new BusinessGroupNameCellRenderer());
					businessGroupRenderer.render(renderer, target, businessGroup, row, source, ubu, translator);
					atLeastOneItem = true;
				}
			}
			
			if (courseMembership.isCurriculumElementMember()) {
				Set<CurriculumElementShort> curriculumElementSet = new HashSet<>(memberRow.getCurriculumElements());
				for (CurriculumElementShort curriculumElement : curriculumElementSet) {
					if (atLeastOneItem) {
						target.append(", ");
					}
					String action = CURRICULUM_ELEMENT_ACTION_PREFIX + curriculumElement.getKey();
					FlexiCellRenderer curriculumElementRenderer = new StaticFlexiCellRenderer(action, new CurriculumElementCellRenderer());
					curriculumElementRenderer.render(renderer, target, curriculumElement.getDisplayName(), row, source, ubu, translator);
					atLeastOneItem = true;
				}
			}
			
			target.append("</div>");
		}
	}
}
