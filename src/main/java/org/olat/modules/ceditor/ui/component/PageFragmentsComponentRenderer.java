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
package org.olat.modules.ceditor.ui.component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.AlertBoxType;
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.model.PageFragment;

/**
 * 
 * Initial date: 11 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageFragmentsComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		PageFragmentsComponent cmp = (PageFragmentsComponent)source;
		renderFlatFragments(renderer, sb, cmp, ubu, translator, renderResult, args);
		cmp.setDirty(false);
	}
	
	private void renderFlatFragments(Renderer renderer, StringOutput sb, PageFragmentsComponent cmp,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		
		List<? extends PageFragment> fragments = cmp.getFragments();
		if(fragments.isEmpty()) {
			renderEmptyState(sb, translator);
		} else {
			Map<String, PageFragment> elementIdToFragments = fragments.stream().collect(Collectors
					.toMap(PageFragment::getElementId, f -> f, (u,v) -> u));
			
			Set<PageFragment> containerized = new HashSet<>();
			for(PageFragment fragment:fragments) {
				if(fragment.getPageElement() instanceof ContainerElement container) {
					List<String> allElementIds = container.getContainerSettings().getAllElementIds();
					for(String elementId:allElementIds) {
						PageFragment containerizedFragment = elementIdToFragments.get(elementId);
						if(containerizedFragment != null) {
							containerized.add(containerizedFragment);
						}
					}
				}
			}

			Set<String> spacingElementIds = FragmentRendererHelper.getContainerElementIdsWithSpacingAfter(cmp);

			for(PageFragment fragment:fragments) {
				if(!containerized.contains(fragment)) {
					render(renderer, sb, fragment, spacingElementIds, elementIdToFragments, ubu, translator, renderResult, cmp.isInForm(), args);
				}
			}
		}
	}

	protected void renderEmptyState(StringOutput sb, Translator translator) {
			sb.append("<div class=\"o_empty_state\"");
			sb.append("><div class=\"o_empty_visual\"><i class='o_icon o_icon_empty_indicator'></i><i class='o_icon o_page_icon'></i></div>")
			  .append("<h3 class=\"o_empty_msg\">").append(translator.translate("no.content")).append("</h3>");						
			sb.append("</div>");
		}
	
	private void render(Renderer renderer, StringOutput sb, PageFragment fragment, Set<String> spacingElementIds, Map<String,
			PageFragment> elementIdToFragments, URLBuilder ubu, Translator translator, RenderResult renderResult,
						boolean inForm, String[] args) {
		PageElement element = fragment.getPageElement();
		if(!elementIdToFragments.containsKey(element.getId())) {
			return;// already rendered fragments
		}
		elementIdToFragments.remove(element.getId());

		if(element instanceof ContainerElement containerElement) {
			renderContainer(renderer, sb, fragment, containerElement, elementIdToFragments, ubu, translator, renderResult,
					spacingElementIds.contains(containerElement.getId()), inForm, args);
		} else {
			AlertBoxSettings alertBoxSettings = FragmentRendererHelper.getAlertBoxSettingsIfActive(element);
			AlertBoxType alertBoxType = alertBoxSettings != null ? alertBoxSettings.getType() : null;
			String alertBoxColor = alertBoxSettings != null ? alertBoxSettings.getColor() : null;

			sb.append("<div class='o_page_part ").append(fragment.getCssClass());
			if (alertBoxType != null) {
				sb.append(" o_alert_box_active ").append(alertBoxType.getCssClass(alertBoxColor));
			}
			sb.append("'>");

			int numberOfItems = 1;
			FragmentRendererHelper.renderAbsolutePositionAlertDiv(sb, fragment.getComponentName(), fragment.getComponent().getDispatchID(), element, numberOfItems, inForm);
			FragmentRendererHelper.renderAlertHeaderWithAbsolutePositionCheck(sb, fragment.getComponentName(), element, numberOfItems, inForm);

			boolean collapsible = FragmentRendererHelper.isCollapsible(element);
			if (collapsible) {
				sb.append("<div class='collapse in ")
						.append(FragmentRendererHelper.buildCollapsibleClass(fragment.getComponentName()))
						.append("' aria-expanded='true'>");
			}

			Component subCmp = fragment.getComponent();
			if (subCmp.isVisible()) {
				subCmp.getHTMLRendererSingleton().render(renderer, sb, subCmp, ubu, translator, renderResult, args);
			}

			if (collapsible) {
				sb.append("</div>");
			}
			sb.append("</div>");

			sb.append("<script>\n")
					.append("\"use strict\";\n")
					.append("jQuery(function() {\n");

			FragmentRendererHelper.renderAbsolutePositionAlertDivScript(sb, fragment.getComponent().getDispatchID(), element);

			sb.append("});\n");
			sb.append("</script>");
		}
		fragment.getComponent().setDirty(false);
		
	}
	
	private void renderContainer(Renderer renderer, StringOutput sb, PageFragment fragment, ContainerElement container,
								 Map<String, PageFragment> elementIdToFragments, URLBuilder ubu, Translator translator,
								 RenderResult renderResult, boolean applyLayoutSpacingAfter, boolean inForm,
								 String[] args) {

		ContainerSettings settings =  container.getContainerSettings();

		sb.append("<div class='");
		sb.append(fragment.getCssClass());
		FragmentRendererHelper.renderContainerLayoutClasses(sb, settings, applyLayoutSpacingAfter);
		sb.append("'>");

		FragmentRendererHelper.renderAlertHeader(sb, fragment.getComponentName(), settings, inForm);
		renderContainerLayout(renderer, sb, fragment.getComponentName(), settings, elementIdToFragments, ubu, translator, renderResult, inForm, args);
		sb.append("</div>");
	}

	private void renderContainerLayout(Renderer renderer, StringOutput sb, String fragmentName,
									   ContainerSettings settings, Map<String, PageFragment> elementIdToFragments,
									   URLBuilder ubu, Translator translator, RenderResult renderResult,
									   boolean inForm, String[] args) {

		List<ContainerColumn> columns = settings.getColumns();
		int numOfBlocks = settings.getNumOfBlocks();
		boolean collapsible = FragmentRendererHelper.isCollapsible(settings);
		for(int i=0; i<numOfBlocks; i++) {
			FragmentRendererHelper.renderContainerBlockDivOpen(sb, "o_container_block", settings,
					collapsible, fragmentName);

			if (i < columns.size()) {
				ContainerColumn column = columns.get(i);
				for(String elementId:column.getElementIds()) {
					PageFragment fragment = elementIdToFragments.get(elementId);
					if(fragment != null) {
						render(renderer, sb, fragment, Set.of(), elementIdToFragments, ubu, translator, renderResult, inForm, args);
					}
				}
			}
			sb.append("</div>");
		}
	}
}
