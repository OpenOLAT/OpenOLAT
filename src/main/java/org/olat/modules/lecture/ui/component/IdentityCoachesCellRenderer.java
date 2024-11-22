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
package org.olat.modules.lecture.ui.component;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionDelegateCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 21 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCoachesCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {
	
	public static final String CMD_OTHER_TEACHERS = "oCoaches";
	private static final List<String> actions = List.of(CMD_OTHER_TEACHERS);
	
	private StaticFlexiCellRenderer otherTeachersRenderer = new OtherCellRenderer();
	
	private final UserManager userManager;
	
	public IdentityCoachesCellRenderer(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof LectureBlockRow blockRow) {
			List<Identity> teachers = blockRow.getTeachersList();
			if(!teachers.isEmpty()) {
				Identity teacher = teachers.get(0);
				String fulllName = userManager.getUserDisplayName(teacher);
				target.append("<span>")
				      .appendHtmlEscaped(fulllName).append("</span>");
			}
			if(teachers.size() > 1) {
				target.append(" | ");
				otherTeachersRenderer.render(renderer, target, cellValue, row, source, ubu, translator);
			}
		}
	}
	
	@Override
	public List<String> getActions() {
		return actions;
	}
	
	public static String getOtherTeachersId(int row) {
		return "o_c" + CMD_OTHER_TEACHERS + "_" + row;
	}
	
	private static class OtherCellRenderer extends StaticFlexiCellRenderer {
		
		public OtherCellRenderer() {
			super("", CMD_OTHER_TEACHERS);
		}

		@Override
		protected String getId(Object cellValue, int row, FlexiTableComponent source) {
			return getOtherTeachersId(row);
		}

		@Override
		protected String getLabel(Renderer renderer, Object cellValue, int row, FlexiTableComponent source,
				URLBuilder ubu, Translator translator) {	
			List<Identity> teachers = ((LectureBlockRow)cellValue).getTeachersList();
			StringBuilder sb = new StringBuilder();
			if(teachers.size() > 1) {
				sb.append("+").append(teachers.size() - 1);
			}
			return sb.toString();
		}
	}
}
