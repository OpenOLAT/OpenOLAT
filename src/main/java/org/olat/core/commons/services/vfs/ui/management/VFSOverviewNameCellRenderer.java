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
package org.olat.core.commons.services.vfs.ui.management;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;

/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class VFSOverviewNameCellRenderer implements FlexiCellRenderer{
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		switch (cellValue.toString()) {
		case "vfs.overview.files":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_FILETYPE_FILE));
			break;
		case "vfs.overview.versions":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_VERSION));
			break;
		case "vfs.overview.thumbnails":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_THUMBNAIL));
			break;
		case "vfs.overview.trash":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_TRASHED));
			break;
		case "vfs.overview.total":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_GLOBE));
			break;
		default:
			break;
		}
		target.append(translator.translate(cellValue.toString()));
	}
}
