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
package org.olat.repository.ui.admin;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.ui.RepositoyUIFactory;

/**
 * 
 * Initial date: 25.08.2021<br>
 * @author Florian Gnägi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class EducationalTypeCellRenderer implements FlexiCellRenderer {
	private final Translator translator;
	
	public EducationalTypeCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Object val,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator trans)  {
		
		if(val instanceof RepositoryEntryEducationalType educationalType) {
			sb.append("<span class='").append(educationalType.getCssClass()).append("'><span class='o_educational_type'>")
			.appendHtmlEscaped(translator.translate(RepositoyUIFactory.getI18nKey(educationalType)))			
			.append("</span></span>");
			
		}
	}
}
