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
package org.olat.modules.project.ui;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjFile;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileMetadataController extends FormBasicController {
	
	private final VFSMetadata vfsMetadata;
	private final Formatter formatter;
	
	@Autowired
	private UserManager userManager;
	
	public ProjFileMetadataController(UserRequest ureq, WindowControl wControl, Form mainForm, ProjFile file) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		vfsMetadata = file.getVfsMetadata();
		formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormStyle("o_proj_metadata");
		
		String type = FileUtils.getFileSuffix(vfsMetadata.getFilename());
		if (StringHelper.containsNonWhitespace(type)) {
			type = translate("file.type.unknown");
		}
		uifactory.addStaticTextElement("file.type", type, formLayout);
		
		String size = Formatter.formatBytes(vfsMetadata.getFileSize());
		uifactory.addStaticTextElement("file.size", size, formLayout);
		
		String createdDateBy = translate("file.date.by",
				formatter.formatDate(vfsMetadata.getCreationDate()),
				userManager.getUserDisplayName(vfsMetadata.getFileInitializedBy()));
		uifactory.addStaticTextElement("file.created", createdDateBy, formLayout);
		
		String modifiedDateBy = translate("file.date.by",
				formatter.formatDate(vfsMetadata.getFileLastModified()),
				userManager.getUserDisplayName(vfsMetadata.getFileLastModifiedBy()));
		uifactory.addStaticTextElement("file.modified", modifiedDateBy, formLayout);
		
		String lockedBy = vfsMetadata.isLocked()
				? userManager.getUserDisplayName(vfsMetadata.getLockedBy())
				: translate("file.locked.not");
		uifactory.addStaticTextElement("file.locked.by", lockedBy, formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
