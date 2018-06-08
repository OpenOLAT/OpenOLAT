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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroupShort;
import org.olat.modules.curriculum.CurriculumElementShort;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof MemberRow) {
			render(target, (MemberRow) cellValue);
		}
	}
	
	private void render(StringOutput sb, MemberRow member) {
		boolean and = false;
		List<BusinessGroupShort> groups = member.getGroups();
		if(groups != null && !groups.isEmpty()) {
			for(BusinessGroupShort group:groups) {
				and = and(sb, and);
				if(group.getName() == null && group.getKey() != null) {
					sb.append(group.getKey());
				} else {
					sb.append(StringHelper.escapeHtml(group.getName()));
				}
			}
		}

		List<CurriculumElementShort> curriculumElements = member.getCurriculumElements();
		if(curriculumElements != null && !curriculumElements.isEmpty()) {
			for(CurriculumElementShort curriculumElement:curriculumElements) {
				and = and(sb, and);
				if(curriculumElement.getDisplayName() == null && curriculumElement.getKey() != null) {
					sb.append(curriculumElement.getKey());
				} else {
					sb.append(StringHelper.escapeHtml(curriculumElement.getDisplayName()));
				}
			}
		}
	}
	
	private final boolean and(StringOutput sb, boolean and) {
		if(and) sb.append(", ");
		return true;
	}
}
