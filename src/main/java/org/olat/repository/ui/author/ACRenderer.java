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
package org.olat.repository.ui.author;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.catalog.CatalogEntryRow;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ACRenderer implements FlexiCellRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(ACRenderer.class);

	@Override
	public void render(Renderer renderer, StringOutput sb, Object val,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator)  {
		sb.append("<div class='o_nowrap o_repoentry_ac'>");
		if(val instanceof Collection) {
			Collection<?> accessTypes = (Collection<?>)val;
			for(Object accessType:accessTypes) {
				if(accessType instanceof String) {
					String type = (String)accessType;
					sb.append("<i class='o_icon o_icon-fw ").append(type).append(" o_icon-lg'> </i>");
				}
			}
		} else if(val instanceof Boolean) {
			boolean acessControlled = ((Boolean)val).booleanValue();
			if(acessControlled) {
				sb.append("<i class='o_icon o_icon-fw o_ac_group_icon o_icon-lg'> </i>");
			}
		} else if (val instanceof AuthoringEntryRow) {
			AuthoringEntryRow entry = (AuthoringEntryRow)val;			
			if(entry.getEntryStatus() != RepositoryEntryStatusEnum.trash && entry.getEntryStatus() != RepositoryEntryStatusEnum.deleted) {				
				try(StringOutput methodsSb = new StringOutput()) {
					renderPriceMethods(renderer, methodsSb, entry.getAccessTypes());
					sb.append(methodsSb);
					if (methodsSb.length() == 0 && entry.isOpenAccess()) {
						sb.append(" <span class='o_small text-muted'>");
						sb.append(translator.translate("table.allusers"));
						sb.append("</span>");
					}
				} catch(IOException e) {
					log.error("", e);
				}
			}
		} else if (val instanceof CatalogEntryRow) {
			CatalogEntryRow entry = (CatalogEntryRow)val;
			renderPriceMethods(renderer, sb, entry.getAccessTypes());
		}
		sb.append("</div>");
	}
	
	private void renderPriceMethods(Renderer renderer, StringOutput sb, List<PriceMethod> methods) {
		if (methods != null && !methods.isEmpty()) {
			if(renderer == null) {
				for (PriceMethod priceMethod : methods) {
					String price = priceMethod.getPrice();
					if(price != null && !price.isEmpty()) {
						sb.append(price).append(" ");
					}
				}
			} else {
				sb.append("<ul class='list-inline'>");
				for (PriceMethod priceMethod : methods) {
					String price = priceMethod.getPrice();
					String type = priceMethod.getType();
					sb.append("<li title=\"").append(priceMethod.getDisplayName()).append("\"><i class='o_icon o_icon-fw ").append(type).append(" o_icon-lg'> </i>");
					if(price != null && !price.isEmpty()) {
						sb.append(" ").append(price);
					}
					sb.append("</li>");
				}
				sb.append("</ul>");
			}
		}
	}
}
