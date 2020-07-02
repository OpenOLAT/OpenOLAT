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
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntryShort;

/**
 * 
 * Description:<br>
 * Render a list of resources / with link
 * 
 * <P>
 * Initial Date:  7 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGResourcesCellRenderer implements FlexiCellRenderer {

	private final AtomicInteger counter = new AtomicInteger();
	
	private FormLayoutContainer formLayout;
	private final FormUIFactory uifactory = FormUIFactory.getInstance();
	
	public BGResourcesCellRenderer(FormLayoutContainer formLayout) {
		this.formLayout = formLayout;
	}
	

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		if(cellValue instanceof BGTableItem) {
			BGTableItem item = (BGTableItem)cellValue;
			if (item.getRelations() != null && !item.getRelations().isEmpty()) {
				List<RepositoryEntryShort> relations = item.getRelations();
				int count = 0;
				for(RepositoryEntryShort relation:relations) {
					if(renderer == null) {
						if(sb.length() > 0) {
							sb.append(", ");
						}
						sb.append(StringHelper.escapeHtml(relation.getDisplayname()));
					} else if(count >= 2) {
						sb.append(" ");
						
						FormLink allResourcesLink = item.getAllResourcesLink();
						if(allResourcesLink == null) {
							allResourcesLink = uifactory.addFormLink("repo_entry_" + counter.incrementAndGet(), "allresources", "...",
								null, formLayout, Link.NONTRANSLATED);
							allResourcesLink.setUrl(BusinessControlFactory.getInstance()
									.getAuthenticatedURLFromBusinessPathString("[BusinessGroup:" + item.getBusinessGroupKey() + "][toolresources:0]"));
						}
						allResourcesLink.setUserObject(item);
						Component allResourcesCmp = allResourcesLink.getComponent();
						allResourcesCmp.getHTMLRendererSingleton()
							.render(renderer, sb, allResourcesCmp, ubu, translator, null, null);
						allResourcesCmp.setDirty(false);
						break;
					} else {
						if(count > 0) sb.append(" ");
						
						String name = "repo_entry_" + item.getBusinessGroupKey() + "_" + relation.getKey();
						FormLink markLink = (FormLink)formLayout.getFormComponent(name);
						if(markLink == null) {
							String resourceName = StringHelper.escapeHtml(relation.getDisplayname());
							markLink = uifactory.addFormLink("repo_entry_" + relation.getKey(), "resource", resourceName, null, formLayout, Link.NONTRANSLATED);
							markLink.setUrl(BusinessControlFactory.getInstance()
									.getAuthenticatedURLFromBusinessPathString("[Repository:" + relation.getKey() + "]"));
							markLink.setIconLeftCSS("o_icon o_CourseModule_icon");
							markLink.setUserObject(relation);
							formLayout.add(name, markLink);
						}
						Link markCmp = markLink.getComponent();
						markCmp.getHTMLRendererSingleton()
							.render(renderer, sb, markCmp, ubu, translator, null, null);
						markCmp.setDirty(false);
						count++;
					}
				}
			}
		}
	}
}
