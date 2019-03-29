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
package org.olat.course.nodes.gta.ui;

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.HTMLReadOnlyController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.edusharing.VFSEdusharingProvider;
import org.olat.repository.ui.settings.LazyRepositoryEdusharingProvider;

/**
 * 
 * Initial date: 26 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EditHTMLController extends BasicController {
	
	private VelocityContainer mainVC;
	private Link backButton;
	private Controller editorCtrl;

	public EditHTMLController(UserRequest ureq, WindowControl wControl, VFSContainer tasksContainer,
			VFSLeaf vfsLeaf, Long courseRepoKey, boolean readOnly) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("edit_html");
		
		backButton = LinkFactory.createLinkBack(mainVC, this);
		
		if (readOnly) {
			editorCtrl = new HTMLReadOnlyController(ureq, getWindowControl(), vfsLeaf.getParentContainer(), vfsLeaf.getName(), true);
		} else {
			VFSEdusharingProvider edusharingProvider = new LazyRepositoryEdusharingProvider(courseRepoKey);
			HTMLEditorController htmlCtrl = WysiwygFactory.createWysiwygController(ureq, getWindowControl(),
					tasksContainer, vfsLeaf.getName(), "media", true, true, edusharingProvider);
			htmlCtrl.getRichTextConfiguration().disableMedia();
			htmlCtrl.getRichTextConfiguration().setAllowCustomMediaFactory(false);
			editorCtrl = htmlCtrl;
		}
		
		listenTo(editorCtrl);
		mainVC.put("editor", editorCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
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
		if (source == backButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

}
