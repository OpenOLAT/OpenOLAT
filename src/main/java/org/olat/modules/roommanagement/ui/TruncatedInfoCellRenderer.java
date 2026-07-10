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
package org.olat.modules.roommanagement.ui;

import java.util.function.Function;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Renders a truncated info text column with an optional "…" link to open the
 * detail view. Callers supply two functions: one to extract the full (raw) text
 * from the cell value, and one to extract the optional {@link FormLink}.
 *
 * Initial date: 10 Jul 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TruncatedInfoCellRenderer implements FlexiCellRenderer {

	private final Function<Object, String> fullTextExtractor;
	private final Function<Object, FormLink> linkExtractor;

	/**
	 * This renderer is used to display truncated textual information within a cell,
	 * optionally appending a link represented by a {@link FormLink}.
	 * The textual content is processed via the supplied {@code fullTextExtractor},
	 * and the optional link is determined using the {@code linkExtractor}.
	 *
	 * @param fullTextExtractor A function that extracts the full text content 
	 *                          from the provided cell value object.
	 * @param linkExtractor     A function that extracts an optional {@link FormLink}
	 *                          from the provided cell value object. It can return {@code null}
	 *                          if no link is required for the specific cell value.
	 */
	public TruncatedInfoCellRenderer(Function<Object, String> fullTextExtractor,
			Function<Object, FormLink> linkExtractor) {
		this.fullTextExtractor = fullTextExtractor;
		this.linkExtractor = linkExtractor;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue == null) return;
		String fullText = fullTextExtractor.apply(cellValue);
		FormLink link = linkExtractor.apply(cellValue);
		if (link == null || renderer == null) {
			String text = RoomUIHelper.truncateColumnInfoText(fullText);
			if (StringHelper.containsNonWhitespace(text)) {
				target.append(StringHelper.escapeHtml(text));
			}
		} else {
			String text = RoomUIHelper.truncateColumnInfoTextNoEllipsis(fullText);
			if (StringHelper.containsNonWhitespace(text)) {
				target.append(StringHelper.escapeHtml(text));
			}
			FlexiTableElementImpl ftE = source.getFormItem();
			if (ftE.getRootForm() != link.getRootForm()) {
				link.setRootForm(ftE.getRootForm());
			}
			ftE.addFormItem(link);
			Component cmp = link.getComponent();
			cmp.getHTMLRendererSingleton().render(renderer, target, cmp, ubu, translator, null, null);
			cmp.setDirty(false);
		}
	}
}
