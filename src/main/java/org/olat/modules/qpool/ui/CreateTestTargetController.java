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
package org.olat.modules.qpool.ui;

import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.ui.events.ExportFormatSelectionEvent;

/**
 * 
 * Initial date: 19.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateTestTargetController extends BasicController {

	public static final String FORMAT_QTI_21 = "create.test.format.qti21";
	
	private final Link formatQti21;
	
	private final Set<ExportFormatOptions> exportFormats;
	
	public CreateTestTargetController(UserRequest ureq, WindowControl wControl, Set<ExportFormatOptions> exportFormats) {
		super(ureq, wControl);
		this.exportFormats = exportFormats;
		
		VelocityContainer mainVC = createVelocityContainer("create_test_target");
		formatQti21 = LinkFactory.createLink(FORMAT_QTI_21, mainVC, this);
		formatQti21.setUserObject(QTI21Constants.QTI_21_FORMAT);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == formatQti21) {
			Link link = (Link) source;
			String format = (String) link.getUserObject();
			ExportFormatOptions exportFormat = getExportFormat(format);
			fireEvent(ureq, new ExportFormatSelectionEvent(exportFormat));
		}
	}

	private ExportFormatOptions getExportFormat(String format) {
		for (ExportFormatOptions exportFormat: exportFormats) {
			if (exportFormat.getFormat().equals(format)) {
				return exportFormat;
			}
		}
		return null;
	}
}
