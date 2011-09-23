/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Frentix GmbH, www.frentix.com
 * <p>
 */
package org.olat.core.commons.modules.glossary;

import org.olat.core.commons.editor.htmleditor.HTMLEditorControllerWithoutFile;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmatheditor.OlatMathEditorPlugin;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Description:<br>
 * loads the term-definition in TinyMCE Editor and updates GlossaryItem
 * 
 * <P>
 * Initial Date: 23.12.2008 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
// TODO: RH: gloss: should once be changed to a flexi and then use normal tiny-component over uifactory!
public class GlossaryDefinitionController extends BasicController {

	private final static String MULTIMEDIA_SUBFOLDER = "multimedia";
	private GlossaryItem glossaryItem;
	private HTMLEditorControllerWithoutFile wCtrl;
	
	/**
	 * @param ureq
	 * @param control
	 */
	public GlossaryDefinitionController(UserRequest ureq, WindowControl control, GlossaryItem glossaryItem, VFSContainer glossaryFolder) {
		super(ureq, control);
		this.glossaryItem = glossaryItem;
		String glossDef = glossaryItem.getGlossDef();
		VFSContainer tempMultimediaFolder = glossaryFolder.createChildContainer(MULTIMEDIA_SUBFOLDER);
		VFSContainer multimediaFolder;
		if (tempMultimediaFolder == null) {
			multimediaFolder = (VFSContainer) glossaryFolder.resolve(MULTIMEDIA_SUBFOLDER);
		} else {
			multimediaFolder = tempMultimediaFolder;
		}
		//FIXME:RH:use a mapper to get image-paths into glossary-content, TinyMce needs an improvement. use multimediaFolder after this
		//FIXME:FG:use a mapper to get image-paths into glossary-content
		wCtrl = WysiwygFactory.createWysiwygControllerWithoutFile(ureq, control, null, glossDef, null);
		listenTo(wCtrl);
		putInitialPanel(wCtrl.getInitialComponent());
		//
		// Femp hack to disable the jsmath latex formulas, do not work in iframe tooltips
		wCtrl.getRichTextConfiguration().disableButton(OlatMathEditorPlugin.BUTTONS);
	}

	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//nothing to do	
		wCtrl.dispose();
	}
	

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == wCtrl) {
			if (event == Event.DONE_EVENT){
				String glossDef = wCtrl.getHTMLContent();
				glossDef = glossDef.replaceAll(System.getProperty("line.separator"), "");
				glossaryItem.setGlossDef(glossDef); 
			}
			else if (event == Event.CANCELLED_EVENT){
				//nothing to do, editor handles this itself. changes aren't persisted without clicking "save"
//				getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
			}
		}
	}


	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// 		
	}

}
