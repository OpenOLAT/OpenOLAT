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
		Map<String, PageFragment> elementIdToFragments = fragments.stream().collect(Collectors
				.toMap(PageFragment::getElementId, f -> f, (u,v) -> u));
		
		Set<PageFragment> containerized = new HashSet<>();
		for(PageFragment fragment:fragments) {
			if(fragment.getPageElement() instanceof ContainerElement) {
				ContainerElement container = (ContainerElement)fragment.getPageElement();
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
	
	private void render(Renderer renderer, StringOutput sb, PageFragment fragment, Map<String, PageFragment> elementIdToFragments,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		PageElement element = fragment.getPageElement();
		if(!elementIdToFragments.containsKey(element.getId())) {
			return;// already rendered fragments
		}
		elementIdToFragments.remove(element.getId());

		if(element instanceof ContainerElement) {
			renderContainer(renderer, sb, fragment, (ContainerElement)element, elementIdToFragments, ubu, translator, renderResult, args);
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
		ContainerLayout layout = settings.getType();
		sb.append("<div class='o_page_layout ").append(fragment.getCssClass())
		  .append(" ").append(layout.cssClass()).append("'>");
		renderContainerLayout(renderer, sb, settings, elementIdToFragments, ubu, translator, renderResult, args);
		sb.append("</div>");
	}
	
	private void renderContainerLayout(Renderer renderer, StringOutput sb, ContainerSettings settings,
			Map<String, PageFragment> elementIdToFragments, URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {

		List<ContainerColumn> columns = settings.getColumns();
		int numOfBlocks = settings.getNumOfBlocks();
		for(int i=0; i<numOfBlocks; i++) {
			sb.append("<div class=''>");
			if(i < columns.size()) {
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
}
