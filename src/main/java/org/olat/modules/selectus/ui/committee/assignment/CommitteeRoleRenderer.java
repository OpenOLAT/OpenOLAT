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
package org.olat.modules.selectus.ui.committee.assignment;

import java.util.Set;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 24 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeRoleRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	private final Set<PositionRole> ratingRoles;
	
	public CommitteeRoleRenderer(Translator translator, Set<PositionRole> ratingRoles) {
		this.translator = translator;
		this.ratingRoles = ratingRoles;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof PositionRole) {
			PositionRole role = (PositionRole)cellValue;
			if(!ratingRoles.contains(role)) {
				target.append("<span title='")
				      .append(translator.translate("assignment.identity.cannot.rate"))
				      .append("'><i class='o_icon o_icon_important'> </i> ")
					  .append(translator.translate(role.role()))
					  .append("</span>");
			} else {
				target.append(translator.translate(role.role()));
			}
		}
	}
}
