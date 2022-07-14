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

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseRoleCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public CourseRoleCellRenderer(Locale locale) {
		translator = Util.createPackageTranslator(CourseRoleCellRenderer.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if (cellValue instanceof CourseMembership) {
			render(target, (CourseMembership) cellValue);
		}
	}
	
	private void render(StringOutput sb, CourseMembership membership) {
		boolean and = false;
		
		// default repository entry group
		if(membership.isRepositoryEntryOwner()) {
			and = and(sb, and);
			sb.append(translator.translate("role.repo.owner"));
		}
		if(membership.isRepositoryEntryCoach()) {
			and = and(sb, and);
			sb.append(translator.translate("role.repo.tutor"));
		}
		if(membership.isRepositoryEntryParticipant()) {
			and = and(sb, and);
			sb.append(translator.translate("role.repo.participant"));
		}
		
		// business groups
		if(membership.isBusinessGroupCoach()) {
			and = and(sb, and);
			sb.append(translator.translate("role.group.tutor"));
		}
		if(membership.isBusinessGroupParticipant()) {
			and = and(sb, and);
			sb.append(translator.translate("role.group.participant"));
		}
		
		// curriculum
		if(membership.isCurriculumElementParticipant()) {
			and = and(sb, and);
			sb.append(translator.translate("role.curriculum.participant"));
		}
		if(membership.isCurriculumElementCoach()) {
			and = and(sb, and);
			sb.append(translator.translate("role.curriculum.coach"));
		}
		if(membership.isCurriculumElementOwner()) {
			and = and(sb, and);
			sb.append(translator.translate("role.curriculum.owner"));
		}
		
		if(membership.isWaiting()) {
			and = and(sb, and);
			sb.append(translator.translate("role.group.waiting"));
		}
		if(membership.isPending()) {
			and = and(sb, and);
			sb.append(translator.translate("role.pending"));
		}
		if(membership.isExternalUser()) {
			sb.append(" ").append(translator.translate("role.external.user"));
		}
	}
	
	private final boolean and(StringOutput sb, boolean and) {
		if(and) sb.append(", ");
		return true;
	}
}
