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
package org.olat.core.commons.services.export.ui;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportInfosController extends FormBasicController {
	
	private FormLink entryLink;
	
	private final ExportRow exportRow;
	private final RepositoryEntry entry;
	private final ExportMetadata exportMetadata;
	
	public ExportInfosController(UserRequest ureq, WindowControl wControl, ExportRow exportRow) {
		super(ureq, wControl);
		this.exportRow = exportRow;
		this.exportMetadata = exportRow.getMetadata();
		entry = exportMetadata == null ? null : exportMetadata.getEntry();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(entry != null) {
			String displayName = StringHelper.escapeHtml(entry.getDisplayname());
			entryLink = uifactory.addFormLink("details.entry.title", displayName, "details.entry.title", formLayout, Link.LINK | Link.NONTRANSLATED);
			entryLink.setIconLeftCSS("o_icon o_CourseModule_icon");
			
			String ref = StringHelper.escapeHtml(entry.getExternalRef());
			if(StringHelper.containsNonWhitespace(ref)) {
				uifactory.addStaticTextElement("details.entry.external.ref", ref, formLayout);
			}
		}

		String title = StringHelper.escapeHtml(exportRow.getTitle());
		uifactory.addStaticTextElement("export.title", title, formLayout);
		
		if(exportRow.getArchiveType() != null) {
			String type = translate("archive." + exportRow.getArchiveType().name().toLowerCase());
			uifactory.addStaticTextElement("export.archive.type", type, formLayout);
		}

		VFSLeaf archive = exportRow.getArchive();
		if(archive != null) {
			uifactory.addStaticTextElement("filename", archive.getName(), formLayout);
		}
		
		if(exportRow.getArchiveType() == ArchiveType.PARTIAL) {
			String description = StringHelper.containsNonWhitespace(exportRow.getDescription()) ? exportRow.getDescription() : "-";
			uifactory.addStaticTextElement("description", description, formLayout);
		}
		
		String creator = StringHelper.escapeHtml(exportRow.getCreatorFullName());
		uifactory.addStaticTextElement("created.by", creator, formLayout);
		
		if(exportMetadata != null && exportMetadata.isOnlyAdministrators()) {
			uifactory.addStaticTextElement("access", translate("only.administrators"), formLayout);
		}
	
		Formatter formatter = Formatter.getInstance(getLocale());
		String creationDate = formatter.formatDate(exportRow.getCreationDate());
		uifactory.addStaticTextElement("creation.date", creationDate, formLayout);
		
		String expirationDate = formatter.formatDate(exportRow.getExpirationDate());
		int days = exportRow.getExpirationInDays();
		String expirationDays;
		if(days == 1) {
			expirationDays = translate("row.expiration.day", Integer.toString(days));
		} else {
			expirationDays = translate("row.expiration.days", Integer.toString(days));
		}
		uifactory.addStaticTextElement("available.until", expirationDate + " - " + expirationDays, formLayout);

		uifactory.addStaticTextElement("file.type", translate("row.zip"), formLayout);
		
		String size = Formatter.formatBytes(exportRow.getArchiveSize());
		uifactory.addStaticTextElement("file.size", size, formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("close", formLayout);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == entryLink) {
			doOpenRepositoryEntry(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doOpenRepositoryEntry(UserRequest ureq) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}
