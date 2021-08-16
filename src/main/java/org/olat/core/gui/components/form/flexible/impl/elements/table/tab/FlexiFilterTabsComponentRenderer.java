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
package org.olat.core.gui.components.form.flexible.impl.elements.table.tab;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
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
public class FlexiFilterTabsComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FlexiFilterTabsComponent tabCmp = (FlexiFilterTabsComponent)source;
		FlexiFilterTabsElementImpl tabEl = tabCmp.getFlexiFilterTabsElement();
		
		Form theForm = tabEl.getRootForm();
		String dispatchId = tabEl.getFormDispatchId();
		FlexiFiltersTab selectedTab = tabEl.getSelectedTab();
		
		List<FlexiFiltersTab> tabs = tabEl.getFilterTabs();
		sb.append("<div id=\"o_c").append(tabCmp.getDispatchID()).append("\" class='o_table_tabs'>")
		  .append("<ul class='o_segments o_segments_tab btn-group' role='navigation'>");
		for(FlexiFiltersTab tab:tabs) {
			sb.append("<li>");
			
			String id = tabEl.getFormDispatchId();
			sb.append("<a href=\"javascript:jQuery('#").append(id).append("').val('');")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, false, false, false,
					  new NameValuePair("tab", tab.getId())))
			  .append("\" class='btn btn-default").append(" btn-primary", selectedTab == tab).append("'><span>").append(tab.getLabel())
			  .append("</span></a>");

			sb.append("</li>");
		}
		sb.append("</ul>");
		
		if(!tabEl.isFiltersExpanded()) {
			Component cmp = tabEl.getRemoveFiltersButton().getComponent();
			Translator subTranslator = cmp.getTranslator();
			cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, subTranslator, renderResult, args);
			cmp.setDirty(false);
		}
		
		sb.append("</div>");	
	}
}
