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
package org.olat.modules.qpool.ui.admin;

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.modules.singlepage.SinglePageController;
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
import org.olat.modules.qpool.QuestionPoolModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class InfoPageController extends BasicController {
	
	private Link createPageButton;
	private Link editPageButton;
	private final VelocityContainer mainVC;
	
	private SinglePageController indexCtrl;
	private HTMLEditorController editInfoPageCtrl;
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	
	public InfoPageController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("infos_page_admin");
		
		editPageButton = LinkFactory.createButton("edit.info.page", mainVC, this);
		editPageButton.setIconLeftCSS("o_icon o_icon_edit");
		
		VFSContainer container = qpoolModule.getInfoPageContainer();
		if(container.resolve("index.html") == null) {
			createPageButton = LinkFactory.createButton("create.infos.page", mainVC, this);
			createPageButton.setCustomEnabledLinkCSS("btn btn-primary");
		} else {
			doViewInfoPage(ureq);
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(createPageButton == source) {
			doEditInfoPage(ureq);
		} else if(editPageButton == source) {
			doEditInfoPage(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editInfoPageCtrl == source) {
			doViewInfoPage(ureq);
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editInfoPageCtrl);
		removeAsListenerAndDispose(indexCtrl);
		editInfoPageCtrl = null;
		indexCtrl = null;
	}
	
	private void doEditInfoPage(UserRequest ureq) {
		cleanUp();
		
		VFSContainer container = qpoolModule.getInfoPageContainer();
		String pageRelPath = "index.html";
		if(container.resolve(pageRelPath) == null) {
			container.createChildLeaf(pageRelPath);
		}
		editInfoPageCtrl = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, getWindowControl(), container, pageRelPath, true, null);
		listenTo(editInfoPageCtrl);
		mainVC.put("index", editInfoPageCtrl.getInitialComponent());
		mainVC.remove(createPageButton);
	}
	
	private void doViewInfoPage(UserRequest ureq) {
		cleanUp();
		
		VFSContainer container = qpoolModule.getInfoPageContainer();
		indexCtrl = new SinglePageController(ureq, getWindowControl(), container, "index.html", false);
		listenTo(indexCtrl);
		mainVC.put("index", indexCtrl.getInitialComponent());
	}
}