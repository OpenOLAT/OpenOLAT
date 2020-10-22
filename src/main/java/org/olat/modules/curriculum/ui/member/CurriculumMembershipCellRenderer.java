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
package org.olat.modules.curriculum.ui.member;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.group.ui.main.CourseMembership;

/**
 * 
 * Initial date: 21 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMembershipCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public CurriculumMembershipCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if (cellValue instanceof CourseMembership) {
			render(target, (CourseMembership) cellValue);
		}
	}
	
	private void render(StringOutput sb, CourseMembership membership) {
		boolean and = false;

		// curriculum
		if(membership.isCurriculumElementParticipant()) {
			and = and(sb, and);
			sb.append(translator.translate("role.participant"));
		}
		if(membership.isCurriculumElementCoach()) {
			and = and(sb, and);
			sb.append(translator.translate("role.coach"));
		}
		if(membership.isRepositoryEntryOwner()) {
			and = and(sb, and);
			sb.append(translator.translate("role.owner"));
		}
		if(membership.isCurriculumElementOwner()) {
			and = and(sb, and);
			sb.append(translator.translate("role.curriculumelementowner"));
		}
		if(membership.isCurriculumElementMasterCoach()) {
			and = and(sb, and);
			sb.append(translator.translate("role.mastercoach"));
		}
	}
	
	private final boolean and(StringOutput sb, boolean and) {
		if(and) sb.append(", ");
		return true;
	}
}
