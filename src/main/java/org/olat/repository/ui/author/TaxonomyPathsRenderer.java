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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * 
 * Initial date: 7 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyPathsRenderer implements FlexiCellRenderer {

	private final Translator taxonomyTranslator;

	public TaxonomyPathsRenderer(Locale locale) {
		taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue != null) {
			@SuppressWarnings("unchecked")
			List<TaxonomyLevel> taxonomyLevels = (List<TaxonomyLevel>)cellValue;
			List<String> taxonomyLevelNames = new ArrayList<>(taxonomyLevels.size());
			for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
				ArrayList<String> names = new ArrayList<>();
				addParentNames(names, taxonomyLevel);
				Collections.reverse(names);
				String value = String.join(" / ", names);
				taxonomyLevelNames.add(value);
			}
			
			taxonomyLevelNames.sort(String::compareToIgnoreCase);
			for (String name : taxonomyLevelNames) {
				target.append("<div class='o_nowrap'>").append(name).append("</div>");
			}
		}
	}
	
	private void addParentNames(List<String> names, TaxonomyLevel level) {
		names.add(TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, level));
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentNames(names, parent);
		}
	}

}
