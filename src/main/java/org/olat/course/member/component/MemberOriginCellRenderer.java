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
package org.olat.course.member.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.group.ui.main.CourseMembership;

/**
 * 
 * Initial date: 11 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberOriginCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public MemberOriginCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if (cellValue instanceof CourseMembership courseMembership) {
			render(target, courseMembership);
		}
	}
	
	public void render(StringOutput sb, CourseMembership membership) {
		boolean and = false;
		
		// default repository entry group
		if(membership.isRepositoryEntryOwner() || membership.isRepositoryEntryCoach() || membership.isRepositoryEntryParticipant()) {
			and = and(sb, and);
			sb.append(translator.translate("origin.repo"));
		}
		
		// business groups
		if(membership.isBusinessGroupCoach() ||membership.isBusinessGroupParticipant()) {
			and = and(sb, and);
			sb.append(translator.translate("origin.group"));
		}
		
		// curriculum
		if(membership.isCurriculumElementParticipant() || membership.isCurriculumElementCoach() || membership.isCurriculumElementOwner()) {
			and = and(sb, and);
			sb.append(translator.translate("origin.curriculum"));
		}
	}
	
	private final boolean and(StringOutput sb, boolean and) {
		if(and) sb.append(", ");
		return true;
	}
}
