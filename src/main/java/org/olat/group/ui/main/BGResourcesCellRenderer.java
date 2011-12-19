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
package org.olat.group.ui.main;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Description:<br>
 * Render a list of resources / with link
 * 
 * <P>
 * Initial Date:  7 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGResourcesCellRenderer implements CustomCellRenderer {
	
	private final Translator translator;
	private VelocityContainer container;
	private final BGMainController listeningController;
	
	public BGResourcesCellRenderer(BGMainController listeningController, VelocityContainer container, Translator translator) {
		this.listeningController = listeningController;
		this.container = container;
		this.translator = translator;
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof BGTableItem) {
			BGTableItem item = (BGTableItem)val;
			List<RepositoryEntry> resources = item.getResources();
			
			int count = 0;
			for(RepositoryEntry resource:resources) {
				if(renderer == null) {//fxdiff: FXOLAT-267 for XSL export
					if(sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(resource.getDisplayname());
				} else if(count >= 2) {
					Link link = LinkFactory.createLink("repo_entry_" + UUID.randomUUID().toString(), container, listeningController);
					link.setCustomDisplayText("...");
					link.setUserObject(item.getBusinessGroup());
					
					URLBuilder ubu = renderer.getUrlBuilder().createCopyFor(link);
					RenderResult renderResult = new RenderResult();
					link.getHTMLRendererSingleton().render(renderer, sb, link, ubu, translator, renderResult, null);
					break;
				} else {
					Link link = LinkFactory.createLink("repo_entry_" + UUID.randomUUID().toString(), container, listeningController);
					link.setCustomDisplayText(resource.getDisplayname());
					link.setUserObject(resource.getKey());
					
					URLBuilder ubu = renderer.getUrlBuilder().createCopyFor(link);
					RenderResult renderResult = new RenderResult();
					link.getHTMLRendererSingleton().render(renderer, sb, link, ubu, translator, renderResult, null);
					count++;
				}
			}
		}
	}
}
