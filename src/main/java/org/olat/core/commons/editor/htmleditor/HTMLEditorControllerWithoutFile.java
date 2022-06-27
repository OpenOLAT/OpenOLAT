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
package org.olat.core.commons.editor.htmleditor;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.SimpleHtmlParser;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Description:<br>
 * special case of HTMLEditorController without file
 * this handles html content directly
 * <p>
 * Use the WYSIWYGFactory to create an instance
 * 
 * <P>
 * Initial Date: 28.05.2009 <br>
 * 
 * @author Roman Haag, roman@frentix.com, www.frentix.com
 */
public class HTMLEditorControllerWithoutFile extends FormBasicController {
	private String body; // Content of body tag

	private RichTextElement htmlElement;
	private VFSContainer baseContainer;
	private FormLink cancel, save;
	private CustomLinkTreeModel customLinkTreeModel;

	private FormLink saveClose;
	

	protected HTMLEditorControllerWithoutFile(UserRequest ureq, WindowControl wControl, VFSContainer baseContainer, String htmlContent,
			CustomLinkTreeModel customLinkTreeModel) {
		super(ureq, wControl, "htmleditor");
		// set some basic variables
		this.baseContainer = baseContainer;
		this.customLinkTreeModel = customLinkTreeModel;
		
		// Parse the content of the page
		this.body = parsePage(htmlContent);
		// load form now
		initForm(ureq);
		// editor is never locked
		VelocityContainer vc = (VelocityContainer) flc.getComponent();
		vc.contextPut("lock", Boolean.FALSE);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		// form does not have button, form ok is triggered when user presses
		// command-save or uses the save icon in the toolbar
		doSaveData(ureq);
	}

	/** this will fire an event to catch in Controller, which uses html-editor
	 * getHTMLContent() should be used, to get value
	 * @param ureq
	 */
	private void doSaveData(UserRequest ureq) {
		// Set new content as default value in element
		// Note that the getValue() method does check for XSS attacks and filters invalid HTML
		String content = htmlElement.getValue();
		htmlElement.setNewOriginalValue(content);
		// Notfy parents
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == htmlElement) {
			// no events to catch
		} else if (source == save) {
			doSaveData(ureq);
		} else if (source == cancel) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		htmlElement = uifactory.addRichTextElementForStringData("rtfElement", null, body, -1, -1, true ,baseContainer, customLinkTreeModel, formLayout, ureq.getUserSession(), getWindowControl());

		// Add resize handler
		RichTextConfiguration editorConfiguration = htmlElement.getEditorConfiguration(); 
		editorConfiguration.setEditorHeight("full");

		// The buttons
		save = uifactory.addFormLink("savebuttontext", formLayout, Link.BUTTON);
		save.addActionListener(FormEvent.ONCLICK);
		cancel = uifactory.addFormLink("cancel", formLayout, Link.BUTTON);
		cancel.addActionListener(FormEvent.ONCLICK);
		saveClose = uifactory.addFormLink("saveandclosebuttontext", formLayout, Link.BUTTON);
		saveClose.setVisible(false);
	}

	/**
	 * Optional configuration option to display the save button below the HTML
	 * editor form. This will not disable the save button in the tinyMCE bar (if
	 * available). Default: true
	 * 
	 * @param buttonEnabled true: show save button; false: hide save button
	 */
	public void setSaveButtonEnabled(boolean buttonEnabled) {
		save.setVisible(buttonEnabled);
	}


	/**
	 * Optional configuration option to display the cancel button below the HTML
	 * editor form. This will not disable the cancel button in the tinyMCE bar (if
	 * available). Default: true
	 * 
	 * @param buttonEnabled true: show cancel button; false: hide cancel button
	 */	
	public void setCancelButtonEnabled(boolean buttonEnabled) {
		cancel.setVisible(buttonEnabled);
	}

	
	private String parsePage(String htmlContent) {
	
		if (htmlContent == null || htmlContent.length() == 0) {
			htmlContent = "";
		}
		SimpleHtmlParser parser = new SimpleHtmlParser(htmlContent);
	
		// now get the body part
		return parser.getHtmlContent();
	}
	
	/**
	 * returns actual value of textarea
	 */
	public String getHTMLContent() {
		return htmlElement.getValue();
	}
	
	/**
	 * Get the rich text config object. This can be used to fine-tune the editor
	 * features, e.g. to enable additional buttons or to remove available buttons
	 * 
	 * @return
	 */
	public RichTextConfiguration getRichTextConfiguration() {
		return htmlElement.getEditorConfiguration();
	}
	
}
