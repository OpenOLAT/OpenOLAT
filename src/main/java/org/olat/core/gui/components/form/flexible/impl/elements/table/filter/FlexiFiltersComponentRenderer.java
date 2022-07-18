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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFiltersComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator transl,
			RenderResult renderResult, String[] args) {
		
		FlexiFiltersComponent ffC = (FlexiFiltersComponent)source;
		FlexiFiltersElementImpl ffE = ffC.getFlexiFiltersElement();
		boolean alwaysOn = ffE.isAlwaysExpanded();
		boolean expanded = ffE.isExpanded();
		boolean largeSearch = ffE.isLargeSearch();
		
		sb.append("<div id='o_c").append(ffC.getDispatchID()).append("' class='o_table_filters_wrapper")
			.append(" always-on", alwaysOn)
			.append(" o_expanded", " o_collapsed", expanded)
			.append(" o_table_filters_large_on", " o_table_filters_large_off", largeSearch).append("'>");
		if(expanded) {
			sb.append("<div class='o_table_filters_row'>")
			  .append("<ul class='nav nav-pills o_table_filters' role='navigation'>");
			List<FlexiFilterButton> filterButtons = ffE.getFiltersButtons();
			for(FlexiFilterButton filterButton:filterButtons) {
				renderFormItem(renderer, sb, filterButton.getButton(), "", args);
			}
			if (!alwaysOn) {
				FormLink addLink = ffE.getAddFiltersButton();
				renderFormItem(renderer, sb, addLink, null, args);				
			}
			sb.append("</ul>");
			
			if(alwaysOn) {
				ffE.getResetFiltersButton().getComponent().setDirty(false);
				ffE.getAddFiltersButton().getComponent().setDirty(false);
			} else {
				sb.append("<ul class='nav nav-pills o_table_filters_config'>");
				DropdownItem moreMenu = ffE.getMoreMenu();
				renderFormItem(renderer, sb, moreMenu, "pull-right", args);
				FormLink resetLink = ffE.getResetFiltersButton();
				resetLink.setVisible(!ffE.isTabsEnabled() && ffE.hasFilterChanges());
				renderFormItem(renderer, sb, resetLink, "pull-right", args);
				sb.append("</ul>");
			}
			
			sb.append("</div>");
		}
		
		// toggle / collapse button
		if(alwaysOn) {
			ffE.getCollpaseFiltersButton().getComponent().setDirty(false);
		} else {
			sb.append("<div class='o_button_group'>");
			FormLink collpaseLink = ffE.getCollpaseFiltersButton();
			renderer.render(collpaseLink.getComponent(), sb, args);
			sb.append("</div>");
		}
		
		// end div
		sb.append("</div>");
	}
	
	private void renderFormItem(Renderer renderer, StringOutput sb, FormItem item, String cssClass, String[] args) {
		if(!item.isVisible()) return;
		
		sb.append("<li role='presentation' class='").append(cssClass).append("'>");
		renderer.render(item.getComponent(), sb, args);
		sb.append("</li>");
	}
}
