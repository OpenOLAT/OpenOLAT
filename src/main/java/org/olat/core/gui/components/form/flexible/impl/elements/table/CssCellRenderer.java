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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 1 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CssCellRenderer implements FlexiCellRenderer {
	
	private final String cssClass;
	private final FlexiCellRenderer delegate;
	
	public CssCellRenderer(String cssClass) {
		this(cssClass, new TextFlexiCellRenderer());
	}
	
	public CssCellRenderer(String cssClass, FlexiCellRenderer delegate) {
		this.cssClass = cssClass;
		this.delegate = delegate;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (StringHelper.containsNonWhitespace(cssClass)) {
			target.append("<div class='").append(cssClass).append("'>");
		}
		delegate.render(renderer, target, cellValue, row, source, ubu, translator);
		if (StringHelper.containsNonWhitespace(cssClass)) {
			target.append("</div>");
		}
	}

}
