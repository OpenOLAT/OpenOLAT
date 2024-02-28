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
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.AlertBoxType;
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerLayout;
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
			
			for(PageFragment fragment:fragments) {
				if(!containerized.contains(fragment)) {
					render(renderer, sb, fragment, elementIdToFragments, ubu, translator, renderResult, args);
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
	
	private void render(Renderer renderer, StringOutput sb, PageFragment fragment, Map<String, PageFragment> elementIdToFragments,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		PageElement element = fragment.getPageElement();
		if(!elementIdToFragments.containsKey(element.getId())) {
			return;// already rendered fragments
		}
		elementIdToFragments.remove(element.getId());

		if(element instanceof ContainerElement containerElement) {
			renderContainer(renderer, sb, fragment, containerElement, elementIdToFragments, ubu, translator, renderResult, args);
		} else {
			sb.append("<div class='").append(fragment.getCssClass()).append("'>");
			Component subCmp = fragment.getComponent();
			if (subCmp.isVisible()) {
				subCmp.getHTMLRendererSingleton().render(renderer, sb, subCmp, ubu, translator, renderResult, args);
			}
			sb.append("</div>");
		}
		fragment.getComponent().setDirty(false);
		
	}
	
	private void renderContainer(Renderer renderer, StringOutput sb, PageFragment fragment, ContainerElement container, Map<String, PageFragment> elementIdToFragments,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		
		ContainerSettings settings =  container.getContainerSettings();
		AlertBoxSettings alertBoxSettings = settings.getAlertBoxSettingsIfActive();
		boolean showAlert = alertBoxSettings != null;
		AlertBoxType alertBoxType = showAlert ? alertBoxSettings.getType() : null;
		String alertBoxColor = showAlert ? alertBoxSettings.getColor() : null;
		ContainerLayout layout = settings.getType();

		sb.append("<div class='o_page_layout ").append(fragment.getCssClass()).append(" ").append(layout.cssClass());
		if (showAlert) {
			sb.append(" ").append("o_alert_box_active ").append(alertBoxType.getCssClass(alertBoxColor));
		}
		sb.append("'>");
		renderAlertHeader(sb, fragment, settings, ubu);
		renderContainerLayout(renderer, sb, fragment, settings, elementIdToFragments, ubu, translator, renderResult, args);
		sb.append("</div>");
	}

	private void renderContainerLayout(Renderer renderer, StringOutput sb, PageFragment containerFragment,
									   ContainerSettings settings, Map<String, PageFragment> elementIdToFragments,
									   URLBuilder ubu, Translator translator, RenderResult renderResult,
									   String[] args) {

		AlertBoxSettings alertBoxSettings = settings.getAlertBoxSettingsIfActive();
		boolean showAlert = alertBoxSettings != null;
		String title = showAlert ? alertBoxSettings.getTitle() : null;
		boolean showTitle = StringHelper.containsNonWhitespace(title);
		boolean collapsible = showTitle && alertBoxSettings.isCollapsible();
		boolean collapsed = collapsible && containerFragment.isCollapsed();

		List<ContainerColumn> columns = settings.getColumns();
		int numOfBlocks = settings.getNumOfBlocks();
		for(int i=0; i<numOfBlocks; i++) {
			sb.append("<div class='o_container_block'>");

			if (!collapsed && i < columns.size()) {
				ContainerColumn column = columns.get(i);
				for(String elementId:column.getElementIds()) {
					PageFragment fragment = elementIdToFragments.get(elementId);
					if(fragment != null) {
						render(renderer, sb, fragment,  elementIdToFragments, ubu, translator, renderResult, args);
					}
				}
			}
			sb.append("</div>");
		}
	}

	private void renderAlertHeader(StringOutput sb, PageFragment containerFragment, ContainerSettings settings, URLBuilder ubu) {
		AlertBoxSettings alertBoxSettings = settings.getAlertBoxSettings();
		boolean showAlert = alertBoxSettings != null && alertBoxSettings.isShowAlertBox();
		String title = showAlert ? alertBoxSettings.getTitle() : null;
		String iconCssClass = showAlert ? alertBoxSettings.getIconCssClass() : null;
		boolean showTitle = StringHelper.containsNonWhitespace(title);
		boolean showIcon = showAlert && alertBoxSettings.isWithIcon() && iconCssClass != null;
		boolean showAlertHeader = showTitle || showIcon;
		boolean collapsible = showTitle && alertBoxSettings.isCollapsible();

		if (showAlertHeader) {
			sb.append("<div class='o_container_block' style='grid-column: 1 / -1;'>");
			sb.append("<div class='o_container_block_alert'>");
			if (showIcon) {
				sb.append("<div class='o_alert_icon'><i class='o_icon ").append(iconCssClass).append("'> </i></div>");
			}
			if (showTitle) {
				if (collapsible) {
					openCollapseLink(sb, ubu, containerFragment, "o_alert_text o_alert_collapse_title");
					sb.append(title).append("</a>");
					openCollapseLink(sb, ubu, containerFragment, "o_alert_collapse_icon");
					sb.append("<i class='o_icon o_icon_lg ")
							.append(containerFragment.isCollapsed() ? "o_icon_details_expand" : "o_icon_details_collaps")
							.append("'> </i>")
							.append("</a>");
				} else {
					sb.append("<div class='o_alert_text'>")
							.append(title)
							.append("</div>");
				}
			}
			sb.append("</div>");
			sb.append("</div>");
		}
	}

	private void openCollapseLink(StringOutput sb, URLBuilder ubu, PageFragment containerFragment, String extraClasses) {
		sb.append("<a role='button' data-toggle='collapse' ")
				.append("href='javascript:;' onclick=\"");
		ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "toggle_collapsed"),
				new NameValuePair("fragment", containerFragment.getElementId()));
		sb.append(" return false;\" class='").append(extraClasses).append("'>");
	}
}
