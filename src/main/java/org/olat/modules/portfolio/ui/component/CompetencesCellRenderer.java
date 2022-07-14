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
package org.olat.modules.portfolio.ui.component;

import static org.olat.core.util.StringHelper.EMPTY;
import static org.olat.modules.taxonomy.ui.TaxonomyUIFactory.translateDisplayName;

import java.text.Collator;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * 
 * Initial date: 18.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompetencesCellRenderer implements FlexiCellRenderer {

	private final Translator taxonomyTranslator;

	public CompetencesCellRenderer(Locale locale) {
		taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		
		if(cellValue instanceof Collection) {
			@SuppressWarnings("unchecked")
			List<TaxonomyCompetence> competences = (List<TaxonomyCompetence>)cellValue;
			
			if(competences.size() > 0) {
				Collator collator = Collator.getInstance(translator.getLocale());
				competences.sort((c1, c2) -> {
					String dn1 = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, c1.getTaxonomyLevel(), EMPTY);
					String dn2 = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, c2.getTaxonomyLevel(), EMPTY);
					return collator.compare(dn1, dn2);
				});
				
				for(TaxonomyCompetence competence : competences) {
					target.append("<span class='o_tag o_competence o_small o_block_inline' id='").append("o_competence_" + competence.getKey()).append("'>");
					target.append(translateDisplayName(taxonomyTranslator, competence.getTaxonomyLevel()));
					target.append("</span>");
					target.append("<script>")
					  	  .append("jQuery(function() {\n")
					  	  .append("  jQuery('#").append("o_competence_" + competence.getKey()).append("').tooltip({\n")
					  	  .append("    html: true,\n")
					  	  .append("    container: 'body',\n")
					  	  .append("    title: '").append(competence.getTaxonomyLevel().getMaterializedPathIdentifiersWithoutSlash()).append("' \n")
					  	  .append("  });\n")
					  	  .append("});")
					  	  .append("</script>");
				}
			}
		}
	}
}
