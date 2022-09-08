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
package org.olat.user.ui.admin;

import java.util.List;

import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionDelegateCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityOrganisationsCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {
	
	public static final String CMD_OTHER_ORGANISATIONS = "oOrganisations";
	private static final List<String> actions = List.of(CMD_OTHER_ORGANISATIONS);
	
	private StaticFlexiCellRenderer otherOrganisationsRenderer = new OtherCellRenderer();

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof List) {
			@SuppressWarnings("unchecked")
			List<OrganisationWithParents> organisations = (List<OrganisationWithParents>)cellValue;
			if(!organisations.isEmpty()) {
				OrganisationWithParents organisation = organisations.get(0);
				String escapedTitle = parentLineToString(organisation);
				String escapedName = StringHelper.escapeHtml(organisation.getDisplayName());
				target.append("<span title='").append(escapedTitle).append("'>")
				      .append(escapedName).append("</span>");
			}
			if(organisations.size() > 1) {
				target.append(" | ");
				otherOrganisationsRenderer.render(renderer, target, cellValue, row, source, ubu, translator);
			}
		}
	}
	
	private String parentLineToString(OrganisationWithParents organisation) {
		List<Organisation> parents = organisation.getParents();
		StringBuilder sb = new StringBuilder(128);
		for(Organisation parent:parents) {
			sb.append(StringHelper.escapeHtml(parent.getDisplayName()))
			  .append(" / ");
		}
		sb.append(StringHelper.escapeHtml(organisation.getDisplayName()));
		return sb.toString();
	}
	
	@Override
	public List<String> getActions() {
		return actions;
	}
	
	public static String getOtherOrganisationsId(int row) {
		return "o_c" + CMD_OTHER_ORGANISATIONS + "_" + row;
	}
	
	private static class OtherCellRenderer extends StaticFlexiCellRenderer {
		
		public OtherCellRenderer() {
			super("", CMD_OTHER_ORGANISATIONS);
		}

		@Override
		protected String getId(Object cellValue, int row, FlexiTableComponent source) {
			return getOtherOrganisationsId(row);
		}

		@Override
		protected void getLabel(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {	
			@SuppressWarnings("unchecked")
			List<Organisation> organisations = (List<Organisation>)cellValue;
			if(organisations.size() > 1) {
				target.append("+").append(organisations.size() - 1);
			}
		}
	}
}
