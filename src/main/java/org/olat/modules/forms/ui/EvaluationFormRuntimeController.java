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
package org.olat.modules.forms.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;

/**
 * 
 * Initial date: 6 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormRuntimeController extends RepositoryEntryRuntimeController {
	
	public EvaluationFormRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re,
			RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		 if(source == toolbarPanel) {
			if(event instanceof PopEvent) {
				processPopEvent(ureq, (PopEvent)event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void processPopEvent(UserRequest ureq, PopEvent pop) {
		super.processPopEvent(ureq, pop);
		if(pop.getController() == editorCtrl && editorCtrl instanceof EvaluationFormEditorController) {
			if(((EvaluationFormEditorController)editorCtrl).hasChanges()) {
				doReloadRuntimeController(ureq);
			}
		} else if (toolbarPanel.getRootController() == toolbarPanel.getLastController()) {
			doReloadRuntimeController(ureq);
		}
	}
	
	private void doReloadRuntimeController(UserRequest ureq) {
		disposeRuntimeController();
		launchContent(ureq);
		if(toolbarPanel.getTools().isEmpty()) {
			initToolbar();
		}
	}

}
