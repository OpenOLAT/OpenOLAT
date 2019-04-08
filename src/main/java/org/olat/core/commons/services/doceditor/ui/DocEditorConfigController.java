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
import java.util.Optional;

import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocumentEditorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.FileUtils;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocEditorConfigController extends BasicController implements Activateable2 {

	private static final String GUIPREF_SEPARATOR = "::";
	private static final String GUIPREF_KEY_EDITOR = "editor";

	private Dropdown editorDropdown;
	private Link backButton;
	
	private final String guiEditorKey;
	private final List<DocEditor> editors;
	
	@Autowired
	private DocumentEditorService editorService;

	public DocEditorConfigController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			DocEditorSecurityCallback secCallback) {
		super(ureq, wControl);
		this.guiEditorKey = getGuiPrefixKey(vfsLeaf);
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		editors = editorService.getEditors(suffix, secCallback.getMode());
		
		VelocityContainer mainVC = createVelocityContainer("editor_config");
		
		if (secCallback.canClose()) {
			backButton = LinkFactory.createLinkBack(mainVC, this);
		}
		
		if (editors.size() > 1) {
			editorDropdown = new Dropdown("editor.selection", null, false, getTranslator());
			editorDropdown.setTranslatedLabel(editors.get(0).getDisplayName(getLocale()));
			editorDropdown.setButton(true);
			for (DocEditor editor : editors) {
				Link editorLink = LinkFactory.createToolLink(editor.getType(), "select", editor.getDisplayName(getLocale()), this);
				editorLink.setUserObject(editor);
				editorDropdown.addComponent(editorLink);
			}
			mainVC.put("editor.selection", editorDropdown);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (editors.size() >= 1) {
			Optional<DocEditor> prefsEditor = getEditorFromGuiPrefs(ureq);
			DocEditor initialEditor = prefsEditor.isPresent() && editorsContains(prefsEditor.get())
					? prefsEditor.get()
					: editors.get(0);
			doSelectEditor(ureq, initialEditor);
		}
	}

	private boolean editorsContains(DocEditor editor) {
		for (DocEditor vfsLeafEditor : editors) {
			if (vfsLeafEditor.getType().equals(editor.getType()) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == backButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source instanceof Link) {
			Link link = (Link)source;
			DocEditor editor = (DocEditor) link.getUserObject();
			doSelectEditor(ureq, editor);
		}
	}

	private void doSelectEditor(UserRequest ureq, DocEditor editor) {
		if (editorDropdown != null) {
			editorDropdown.setTranslatedLabel(editor.getDisplayName(getLocale()));
		}
		saveEditorPrefs(ureq, editor);
		fireEvent(ureq, new DocEditorSelectionEvent(editor));
	}
	
	private void saveEditorPrefs(UserRequest ureq, DocEditor editor) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(DocEditorConfigController.class, guiEditorKey, editor.getType());
		}
	}

	private String getGuiPrefixKey(VFSLeaf vfsLeaf) {
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		return GUIPREF_KEY_EDITOR + GUIPREF_SEPARATOR + suffix;
	}
	
	private Optional<DocEditor> getEditorFromGuiPrefs(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		String editorType = (String) guiPrefs.get(DocEditorConfigController.class, guiEditorKey);
		return editorService.getEditor(editorType);
	}

	@Override
	protected void doDispose() {
		//
	}

}
