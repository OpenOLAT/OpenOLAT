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
package org.olat.modules.curriculum.ui.component;

import java.util.Locale;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.CurriculumManagerController;

/**
 * 
 * Initial date: 11 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupMembershipStatusRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public GroupMembershipStatusRenderer(Locale locale) {
		translator = Util.createPackageTranslator(CurriculumManagerController.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof GroupMembershipStatus status) {
			render(target, status);
		} else if(cellValue instanceof String str) {
			target.appendHtmlEscaped(str);
		}
	}
	
	public void render(StringOutput target, GroupMembershipStatus status) {
		String statusName = status.name().toLowerCase();
		String label = translator.translate("membership.".concat(status.name()));
		target.append("<span class='o_labeled_light o_gmembership_status_").append(statusName).append("'>")
		  .append("<i class='o_icon o_membership_status_").append(statusName.toLowerCase()).append(" o_icon-fw' title='").append(label).append("'> </i>")
		  .append(label)
	      .append("</span");
	}
}
