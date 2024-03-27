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
package org.olat.modules.video.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Initial date: 2023-02-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class HeaderCommandsController extends BasicController {
	public static final Event DELETE_EVENT = new Event("header.commands.delete");
	public static final Event IMPORT_EVENT = new Event("header.commands.import");
	public static final Event EXPORT_EVENT = new Event("header.commands.export");
	public static final Event EXPORT_ALL_EVENT = new Event("header.commands.export.all");
	private Link importLink;
	private Link exportLink;
	private Link exportAllLink;
	private Link deleteLink;

	public HeaderCommandsController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, false, false, true);
	}

	public HeaderCommandsController(UserRequest ureq, WindowControl wControl, boolean withImport, boolean withExport) {
		this(ureq, wControl, withImport, withExport, true);
	}
	public HeaderCommandsController(UserRequest ureq, WindowControl wControl, boolean withImport, boolean withExport, boolean withDelete) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("header_commands");

		if (withImport) {
			importLink = LinkFactory.createLink("form.common.import", "import", getTranslator(), mainVC, this,
					Link.LINK);
			importLink.setIconLeftCSS("o_icon o_icon-fw o_mi_qpool_import");
			mainVC.put("import", importLink);
		}

		if (withExport) {
			exportLink = LinkFactory.createLink("tools.export.pool", "export", getTranslator(), mainVC, this,
					Link.LINK);
			exportLink.setIconLeftCSS("o_icon o_icon-fw o_mi_qpool_import");
			mainVC.put("export", exportLink);

			exportAllLink = LinkFactory.createLink("tools.export.all.pool", "exportAll", getTranslator(), mainVC, this,
					Link.LINK);
			exportAllLink.setIconLeftCSS("o_icon o_icon-fw o_mi_qpool_import");
			mainVC.put("exportAll", exportAllLink);
		}

		if (withDelete) {
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this,
					Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("delete", deleteLink);
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (deleteLink == source) {
			fireEvent(ureq, DELETE_EVENT);
		} else if (importLink == source) {
			fireEvent(ureq, IMPORT_EVENT);
		} else if (exportLink == source) {
			fireEvent(ureq, EXPORT_EVENT);
		} else if (exportAllLink == source) {
			fireEvent(ureq, EXPORT_ALL_EVENT);
		}
	}

	public void setCanDelete(boolean canDelete) {
		if (deleteLink != null) {
			deleteLink.setEnabled(canDelete);
		}
	}

	public void setCanExport(boolean canExport) {
		if (exportLink != null) {
			exportLink.setEnabled(canExport);
		}
		if (exportAllLink != null) {
			exportAllLink.setEnabled(canExport);
		}
	}
}
