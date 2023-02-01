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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectStatus;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 21 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectUIFactory {
	
	public static String getStatusIconCss(ProjectStatus status) {
		if (status != null) {
			switch (status) {
			case active: return "o_icon_proj_project_status_active";
			case done: return "o_icon_proj_project_status_done";
			case deleted: return "o_icon_proj_project_status_deleted";
			default:
			}
		}
		return null;
	}

	public static String translateStatus(Translator translator, ProjectStatus status) {
		if (status != null) {
			switch (status) {
			case active: return translator.translate("status.active");
			case done: return translator.translate("status.done");
			case deleted: return translator.translate("status.deleted");
			default:
			}
		}
		return null;
	}

	public static String translateRole(Translator translator, ProjectRole role) {
		if (role != null) {
			return translator.translate("role." + role.name());
		}
		return null;
	}
	
	public static String getDisplayName(ProjFile file) {
		return StringHelper.containsNonWhitespace(file.getVfsMetadata().getTitle())
				? file.getVfsMetadata().getTitle()
				: file.getVfsMetadata().getFilename();
	}

	public static String getDisplayName(Translator translator, ProjNote note) {
		return StringHelper.containsNonWhitespace(note.getTitle())
				? note.getTitle()
				: translator.translate("no.title");
	}
	
	public static MultipleSelectionElement createMembersElement(FormUIFactory uifactory, FormItemContainer formLayout,
			UserManager userManager, Collection<Identity> projectMembers, Collection<Identity> currentMembers) {
		Set<Identity> allMembers = new HashSet<>(projectMembers);
		allMembers.addAll(currentMembers);
		
		SelectionValues membersSV = new SelectionValues();
		allMembers.forEach(member -> membersSV.add(
				SelectionValues.entry(
						member.getKey().toString(),
						userManager.getUserDisplayName(member.getKey()))));
		membersSV.sort(SelectionValues.VALUE_ASC);
		
		MultipleSelectionElement membersEl = uifactory.addCheckboxesDropdown("members", "members", formLayout,
				membersSV.keys(), membersSV.values());
		currentMembers.forEach(member -> membersEl.select(member.getKey().toString(), true));
		
		return membersEl;
	}

	public static String getActionIconCss(Action action) {
		switch (action.getTarget()) {
		case project: return "o_icon_proj_project";
		case file: return "o_icon_proj_file";
		case note: return "o_icon_proj_note";
		default: return null;
		}
	}
	
	public static StringMediaResource createMediaResource(ProjNote note) {
		StringMediaResource resource = new StringMediaResource();
		
		StringBuilder sb = new StringBuilder();
		boolean newLine = false;
		if (StringHelper.containsNonWhitespace(note.getTitle())) {
			sb.append(note.getTitle());
			newLine = true;
		}
		if (StringHelper.containsNonWhitespace(note.getText())) {
			if (newLine) {
				sb.append("\n\n");
			}
			sb.append(note.getText());
		}
		resource.setData(sb.toString());
		
		String downloadFileName = StringHelper.containsNonWhitespace(note.getTitle())
				? note.getTitle()
				: "note_" + note.getKey();
		downloadFileName += "_";
		downloadFileName += Formatter.formatDatetimeFilesystemSave(new Date());
		downloadFileName = FileUtils.normalizeFilename(downloadFileName);
		downloadFileName += ".txt";
		resource.setDownloadable(true, downloadFileName);
		
		resource.setContentType("text/txt");
		resource.setEncoding("UTF-8");
		
		return resource;
	}

}
