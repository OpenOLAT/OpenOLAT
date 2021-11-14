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
package org.olat.core.commons.services.doceditor.ui;

import java.util.List;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 10.05.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocEditorStandaloneController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	private final DocEditorController editorCtrl;
	
	public DocEditorStandaloneController(UserRequest ureq, WindowControl wControl, Access access, DocEditorConfigs configs) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("editor_standalone");
		editorCtrl = new DocEditorController(ureq, wControl, access, configs);
		listenTo(editorCtrl);
		mainVC.put("editor", editorCtrl.getInitialComponent());
		
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		cc.addBodyCssClass("o_doceditor_body");

		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		editorCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editorCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void doDispose() {
		try {
			ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
			cc.removeBodyCssClass("o_doceditor_body");
		} catch(Exception e) {
			logError("", e);
		}
        super.doDispose();
	}
}
