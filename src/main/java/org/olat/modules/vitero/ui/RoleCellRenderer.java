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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.ui;

import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.vitero.model.GroupRole;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RoleCellRenderer implements CustomCellRenderer {
	
	private Translator translator;
	
	protected RoleCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof GroupRole) {
			GroupRole role = (GroupRole)val;
			switch(role) {
				case participant: sb.append(translator.translate("role.participant")); break;
				case assistant: sb.append(translator.translate("role.assistant")); break;
				case teamleader: sb.append(translator.translate("role.teamLeader")); break;
				case audience: sb.append(translator.translate("role.audience")); break;
			}
		} else if("owner".equals(val) || "coach".equals(val)) {
			sb.append("<i>").append(translator.translate("role.teamLeader")).append("</i>");
		}
	}
}
