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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * 
 * 
 * Initial date: 15.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportSourcesController extends BasicController {
	
	public static final String IMPORT_REPO = "qpool.import.repository";
	public static final String IMPORT_FILE = "qpool.import.file";
	public static final String IMPORT_EXCEL_QTI_21 = "qpool.import.excellike.21";

	private final Link importFile;
	private final Link importRepository;
	private final Link importExcelLikeQTI21;

	public ImportSourcesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("import_sources");
		importRepository = LinkFactory.createLink("import.repository", mainVC, this);
		importRepository.setIconLeftCSS("o_icon o_icon-fw o_FileResource-TEST_icon");
		importFile = LinkFactory.createLink("import.file", mainVC, this);
		importFile.setIconLeftCSS("o_icon o_icon-fw o_filetype_file");
		importExcelLikeQTI21 = LinkFactory.createLink("import.excellike.21", mainVC, this);
		importExcelLikeQTI21.setIconLeftCSS("o_icon o_icon-fw o_icon_table");
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(importRepository == source) {
			fireEvent(ureq, new Event(IMPORT_REPO));
		} else if(importFile == source) {
			fireEvent(ureq, new Event(IMPORT_FILE));
		} else if(importExcelLikeQTI21 == source) {
			fireEvent(ureq, new Event(IMPORT_EXCEL_QTI_21));
		}
	}
}
