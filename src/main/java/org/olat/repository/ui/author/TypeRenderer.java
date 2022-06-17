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

import org.olat.NewControllerFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.list.RepositoryEntryRow;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TypeRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {

		String type = null;
		if (cellValue instanceof RepositoryEntryShort) { // add image and typename code
			type = ((RepositoryEntryShort)cellValue).getResourceType();
		} else if(cellValue instanceof RepositoryEntry) {
			type = ((RepositoryEntry)cellValue).getOlatResource().getResourceableTypeName();
		} else if(cellValue instanceof RepositoryEntryRow) {
			type = ((RepositoryEntryRow)cellValue).getOLATResourceable().getResourceableTypeName();
		} else if(cellValue instanceof CatalogRepositoryEntryRow) {
			type = ((CatalogRepositoryEntryRow)cellValue).getOlatResource().getResourceableTypeName();
		}
		
		if(type == null) {
			type = translator.translate("cif.type.na");
		} else {
			type = NewControllerFactory.translateResourceableTypeName(type, translator.getLocale());
		}
		type = StringHelper.escapeHtml(type);
		
		String cssClass = "";
		boolean managed = false;
		RepositoryEntryStatusEnum status = null;
		if(cellValue instanceof AuthoringEntryRow) {
			AuthoringEntryRow re = (AuthoringEntryRow) cellValue;
			cssClass = RepositoyUIFactory.getIconCssClass(re.getResourceType());
			managed = re.isManaged();
			status = re.getEntryStatus();
		} else if (cellValue instanceof RepositoryEntryShort) {
			RepositoryEntryShort re = (RepositoryEntryShort) cellValue;
			cssClass = RepositoyUIFactory.getIconCssClass(re.getResourceType());
			status = re.getEntryStatus();
		} else if (cellValue instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry) cellValue;
			cssClass = RepositoyUIFactory.getIconCssClass(re.getOlatResource().getResourceableTypeName());
			managed = StringHelper.containsNonWhitespace(re.getManagedFlagsString());
			status = re.getEntryStatus();
		} else if (cellValue instanceof RepositoryEntryRow) {
			RepositoryEntryRow re = (RepositoryEntryRow) cellValue;
			cssClass = RepositoyUIFactory.getIconCssClass(re.getOLATResourceable().getResourceableTypeName());
			managed = false;// no indication for this type of row
			status = re.getStatus();
		} else if(cellValue instanceof CatalogRepositoryEntryRow) {
			CatalogRepositoryEntryRow re = (CatalogRepositoryEntryRow) cellValue;
			cssClass = RepositoyUIFactory.getIconCssClass(re.getOlatResource().getResourceableTypeName());
			managed = false;// no indication for this type of row
			status = re.getStatus();
		}
		
		if(renderer == null) {
			// excel download
			target.append(type);
		} else {
			target.append("<div class='o_nowrap o_repoentry_type'>")
			      .append("<i class='o_icon o_icon-lg ").append(cssClass).append("' title=\"").append(type).append("\"> </i>");
			if (managed) {
				target.append(" <i class='o_icon o_icon_managed' title=\"").append(translator.translate("cif.managedflags")).append("\"> </i> ");
			}

			if (status == null || status == RepositoryEntryStatusEnum.deleted) {
				target.append(" <i class='o_icon o_icon-lg o_icon_repo_status_deleted'> </i> ");
			} else if(status == RepositoryEntryStatusEnum.trash) {
				target.append(" <i class='o_icon o_icon-lg o_icon_repo_status_trash'> </i> ");
			} else if (status == RepositoryEntryStatusEnum.closed) {
				target.append(" <i class='o_icon o_icon-lg o_icon_repo_status_closed'> </i> ");
			}
			target.append("</div>");
		}
	}
}