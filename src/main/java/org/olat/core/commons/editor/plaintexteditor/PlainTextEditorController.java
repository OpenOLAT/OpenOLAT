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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.commons.editor.plaintexteditor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.version.Versionable;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for Trans
 * 
 * <P>
 * Initial Date: 15.01.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */

public class PlainTextEditorController extends BasicController {

	private final VFSLeaf vfsfile;
	private final String encoding;
	private TextForm tf;
	private boolean newFile = false;
	private boolean readOnly = false;

	/**
	 * @param ureq
	 * @param wControl
	 * @param vfsfile the file to edit, must exist
	 * @param encoding the encoding the file has, e.g. "utf-8"
	 * @param offerCancel is ignored
	 * @param newFile create a new file
	 * @param findtext is ignored
	 */
	
	public PlainTextEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsfile, String encoding, boolean offerCancel, boolean newFile, String findtext) {
		super(ureq, wControl);
		this.vfsfile = vfsfile;
		this.encoding = encoding;
		this.newFile = newFile;
		long size = vfsfile.getSize(); //bytes
		
		if (size > FolderConfig.getMaxEditSizeLimit()) {
			// limit to reasonable size, see OLAT-3025
			getWindowControl().setError(translate("plaintext.error.tolarge", new String[]{(size / 1000) + "", (FolderConfig.getMaxEditSizeLimit()/1000)+""}));
			putInitialPanel(new Panel("empty"));
			return;
		}		
		
		String content = FileUtils.load(vfsfile.getInputStream(),encoding);
		
		VelocityContainer mainVc = createVelocityContainer("index");
		tf = new TextForm(ureq, wControl, content, offerCancel);
		listenTo(tf);
		mainVc.put("form", tf.getInitialComponent());
		if (findtext != null) {
			mainVc.contextPut("findtext", findtext);
		}
		putInitialPanel(mainVc);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == tf && event == Event.DONE_EVENT) {
			if (!readOnly) {
				if((!newFile) && vfsfile instanceof Versionable && ((Versionable)vfsfile).getVersions().isVersioned()) {
					try(InputStream inStream = FileUtils.getInputStream(tf.getTextValue(), encoding)) {
						((Versionable)vfsfile).getVersions().addVersion(ureq.getIdentity(), "", inStream);
					} catch(IOException e) {
						logError("", e);
					}
				} else {
					try(OutputStream out=vfsfile.getOutputStream(false)) {
						FileUtils.save(out, tf.getTextValue(), encoding);
					} catch(IOException e) {
						logError("", e);
					}
				}
			}
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	/**
	 * @param readOnly
	 */
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		tf.setReadOnly(readOnly);
	}
}

class TextForm extends FormBasicController {
	
	private TextElement textF;
	private String txt;
	
	/**
	 * @param name the technical name of the component
	 * @param offerCancel is ignored
	 */
	
	public TextForm(UserRequest ureq, WindowControl wControl, String content, boolean offerCancel) {
		super(ureq, wControl);
		txt = content;
		initForm(ureq);
	}

	protected void setReadOnly (boolean ro) {
		textF.setEnabled(!ro);
	}
	
	public String getTextValue() {
		return textF.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		textF = uifactory.addTextAreaElement("textarea", 25, 100, txt, formLayout);
		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}
