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
package org.olat.modules.ceditor.ui;

import java.util.List;
import java.util.function.Function;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.model.jpa.TocPart;
import org.olat.modules.ceditor.ui.TocRunController.TitleEntry;
import org.olat.modules.ceditor.ui.component.EditModeAware;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.PageStructureChangedEvent;

/**
 * Initial date: 15 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TocEditorController extends BasicController implements PageElementEditorController {

	private final EditModeAwareVelocityContainer mainVC;
	private TocPart tocPart;
	private final Function<TocPart, List<TitleEntry>> entryProvider;
	private final boolean editorUsedAsView;

	public TocEditorController(UserRequest ureq, WindowControl wControl, TocPart tocPart, 
							   Function<TocPart, List<TitleEntry>> entryProvider, boolean editorUsedAsView) {
		super(ureq, wControl);
		this.tocPart = tocPart;
		this.entryProvider = entryProvider;
		this.editorUsedAsView = editorUsedAsView;
		mainVC = new EditModeAwareVelocityContainer("toc_editor", getTranslator(), this);
		doUpdateEntries();
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof ChangePartEvent cpe && cpe.getElement() instanceof TocPart updatedTocPart) {
			tocPart = updatedTocPart;
			doUpdateEntries();
		} else if (event instanceof PageStructureChangedEvent) {
			doUpdateEntries();
		}
	}

	private void doUpdateEntries() {
		mainVC.contextPut("title", tocPart.getTocSettings().getTitle());
		mainVC.contextPut("entries", entryProvider.apply(tocPart));
		mainVC.setDirty(true);
	}

	class EditModeAwareVelocityContainer extends VelocityContainer implements EditModeAware {
		private boolean editMode = true;
		
		
		public EditModeAwareVelocityContainer(String page, Translator translator, ComponentEventListener listeningController) {
			super("vc_" + page, getVelocityTemplatePath(page), translator, listeningController);
		}

		@Override
		public void editModeSet(boolean editMode) {
			if (editorUsedAsView) {
				if (editMode) {
					this.editMode = true;
				} else {
					if (this.editMode) {
						this.editMode = false;
						// Update entries when leaving edit mode of the TOC element
						doUpdateEntries();
					}
				}
			}
		}
	}
}