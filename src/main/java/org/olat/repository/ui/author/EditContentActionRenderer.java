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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.author;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Quick action column that opens the type specific editor. The label shown
 * as tooltip is driven by {@link RepositoryHandler#getEditorLinkI18nKey()},
 * the same key used for the "Edit" / "Edit content" entry in the tools menu.
 *
 * Initial date: 11.09.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EditContentActionRenderer implements FlexiCellRenderer {

	private final RepositoryHandlerFactory handlerFactory;
	private final Identity identity;
	private final Roles roles;

	public EditContentActionRenderer(RepositoryHandlerFactory handlerFactory, Identity identity, Roles roles) {
		this.handlerFactory = handlerFactory;
		this.identity = identity;
		this.roles = roles;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(!(cellValue instanceof AuthoringEntryRow authoringEntryRow)) {
			return;
		}

		RepositoryHandler handler = handlerFactory.getRepositoryHandler(authoringEntryRow.getResourceType());
		if(handler == null) {
			return;
		}

		EditionSupport editionSupport = handler.supportsEdit(authoringEntryRow.getOLATResourceable(), identity, roles);
		if((editionSupport != EditionSupport.yes && editionSupport != EditionSupport.embedded)
				|| authoringEntryRow.getEntryStatus().decommissioned()) {
			return;
		}

		String title = translator.translate(handler.getEditorLinkI18nKey());
		StaticFlexiCellRenderer delegate = new StaticFlexiCellRenderer("", "edit", null, "o_icon-lg o_icon_edit", title);
		delegate.render(renderer, target, cellValue, row, source, ubu, translator);
	}
}
