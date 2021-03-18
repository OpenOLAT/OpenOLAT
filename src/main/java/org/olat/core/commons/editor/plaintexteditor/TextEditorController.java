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
package org.olat.core.commons.editor.plaintexteditor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Mar 2019<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TextEditorController extends FormBasicController {

	private TextElement contentEl;
	private FormLink saveLink;
	private FormLink saveCloseLink;
	private FormLink closeLink;

	private final VFSLeaf vfsLeaf;
	private final String encoding;
	private final boolean readonly;
	private final boolean versionControlled;

	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public TextEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf, String encoding,
			boolean readonly, boolean versionControlled) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.vfsLeaf = vfsLeaf;
		this.encoding = encoding;
		this.readonly = readonly;
		this.versionControlled = versionControlled;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contentEl = uifactory.addTextAreaElement("textarea", "textarea", -1, 25, 100, true, false, "", formLayout);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setElementCssClass("o_button_group");
		formLayout.add("buttons", buttonCont);

		long size = vfsLeaf.getSize(); // bytes
		if (size > FolderConfig.getMaxEditSizeLimit()) {
			setFormWarning("plaintext.error.tolarge", new String[] { String.valueOf(size / 1000),
					String.valueOf(FolderConfig.getMaxEditSizeLimit() / 1000) });
		} else {
			String content = FileUtils.load(vfsLeaf.getInputStream(), encoding);
			contentEl.setValue(content);
			contentEl.setLabel("file.name", new String[] { vfsLeaf.getName() });
			contentEl.setEnabled(!readonly);

			saveLink = uifactory.addFormLink("save", buttonCont, Link.BUTTON);
			saveLink.setVisible(!readonly);
			saveCloseLink = uifactory.addFormLink("save.close", buttonCont, Link.BUTTON);
			saveCloseLink.setVisible(!readonly);
		}

		closeLink = uifactory.addFormLink("close", buttonCont, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == saveLink) {
			doSave();
		} else if (source == saveCloseLink) {
			doSave();
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == closeLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSave() {
		String content = contentEl.getValue();
		if (versionControlled && vfsLeaf.canVersion() == VFSConstants.YES) {
			try (InputStream inStream = FileUtils.getInputStream(content, encoding)) {
				vfsRepositoryService.addVersion(vfsLeaf, getIdentity(), false, "", inStream);
			} catch (IOException e) {
				logError("", e);
			}
		} else {
			try (OutputStream out = vfsLeaf.getOutputStream(false)) {
				FileUtils.save(out, content, encoding);
			} catch (IOException e) {
				logError("", e);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
