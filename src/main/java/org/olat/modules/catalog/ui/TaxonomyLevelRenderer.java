/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.catalog.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;

/**
 * 
 * Initial date: Dec 19, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TaxonomyLevelRenderer implements FlexiCellRenderer {
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue != null) {
			@SuppressWarnings("unchecked")
			List<TaxonomyLevelNamePath> taxonomyLevelNamePaths = (List<TaxonomyLevelNamePath>)cellValue;
			target.append("<div class='o_taxonomy_cell'>");
			for (TaxonomyLevelNamePath taxonomyLevelNamePath : taxonomyLevelNamePaths) {
				target.append("<div class='o_tag o_taxonomy'>").appendHtmlEscaped(taxonomyLevelNamePath.getDisplayName()).append("</div>");
			}
			target.append("</div>");
		}
	}
	

}
