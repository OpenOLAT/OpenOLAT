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
package org.olat.core.commons.services.folder.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 27 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderIconRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof FolderRow folderRow) {
			target.append("<div class=\"o_folder_row_thumbnail\">");
			if (folderRow.getVfsItem() instanceof VFSContainer) {
				target.append("<div class=\"o_folder_row_thumbnail_icon\"><i class=\"o_icon o_filetype_folder\"></i></div>");
			} else if (folderRow.isThumbnailAvailable()) {
				target.append("<img src=\"").append(folderRow.getThumbnailUrl()).append("\" alt=\"\"/>");
			} else {
				target.append("<div class=\"o_folder_row_thumbnail_icon\"><i class=\"o_icon ")
				.append(CSSHelper.createFiletypeIconCssClassFor(folderRow.getVfsItem().getName()))
				.append("\"></i></div>");
			}
			target.append("</div>");
		}
	}

}
