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
package org.olat.core.commons.modules.glossary;

import org.olat.core.commons.editor.htmleditor.HTMLEditorControllerWithoutFile;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Description:<br>
 * loads the term-definition in TinyMCE Editor and updates GlossaryItem
 * 
 * <P>
 * Initial Date: 23.12.2008 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class GlossaryDefinitionController extends BasicController {

	private GlossaryItem glossaryItem;
	private HTMLEditorControllerWithoutFile wCtrl;
	
	/**
	 * @param ureq
	 * @param control
	 */
	public GlossaryDefinitionController(UserRequest ureq, WindowControl control, GlossaryItem glossaryItem) {
		super(ureq, control);
		this.glossaryItem = glossaryItem;
		String glossDef = glossaryItem.getGlossDef();

		wCtrl = WysiwygFactory.createWysiwygControllerWithoutFile(ureq, control, null, glossDef, null);
		wCtrl.getRichTextConfiguration().disableMathEditor();
		listenTo(wCtrl);
		putInitialPanel(wCtrl.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//nothing to do	
		wCtrl.dispose();
        super.doDispose();
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == wCtrl) {
			if (event == Event.DONE_EVENT){
				String glossDef = wCtrl.getHTMLContent();
				glossDef = glossDef.replaceAll(System.getProperty("line.separator"), "");
				glossaryItem.setGlossDef(glossDef); 
			} else if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// 		
	}
}