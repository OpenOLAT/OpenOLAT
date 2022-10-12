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

import static org.olat.core.commons.services.doceditor.DocEditor.Mode.EDIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.DocTemplates.Builder;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.edusharing.VFSEdusharingProvider;
import org.olat.repository.ui.settings.LazyRepositoryEdusharingProvider;

/**
 * 
 * Initial date: 1 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GTAUIFactory {
	
	static Mode getOpenMode(Identity identity, Roles roles, VFSLeaf vfsLeaf, boolean readOnly) {
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		if (!readOnly && docEditorService.hasEditor(identity, roles, vfsLeaf, Mode.EDIT, true)) {
			return Mode.EDIT;
		} else if (docEditorService.hasEditor(identity, roles, vfsLeaf, Mode.VIEW, true)) {
			return Mode.VIEW;
		}
		return null;
	}
	
	static DocEditorConfigs getEditorConfig(VFSContainer vfsContainer, VFSLeaf vfsLeaf, String filePath, Mode mode, Long courseRepoKey) {
		VFSEdusharingProvider edusharingProvider = courseRepoKey != null
				? new LazyRepositoryEdusharingProvider(courseRepoKey)
				: null;
		 HTMLEditorConfig htmlEditorConfig = HTMLEditorConfig.builder(vfsContainer, filePath)
				.withMediaPath("media")
				.withAllowCustomMediaFactory(false)
				.withDisableMedia(true)
				.withEdusharingProvider(edusharingProvider)
				.build();
		 return DocEditorConfigs.builder()
				.withMode(mode)
				.addConfig(htmlEditorConfig)
				.build(vfsLeaf);
	}
	
	static DocTemplates htmlOffice(Identity identity, Roles roles, Locale locale) {
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		Builder builder = DocTemplates.builder(locale);
		if (docEditorService.hasEditor(identity, roles, "html", EDIT, true, false)) {
			builder.addHtml();
		}
		if (docEditorService.hasEditor(identity, roles, "docx", EDIT, true, false)) {
			builder.addDocx();
		}
		if (docEditorService.hasEditor(identity, roles, "xlsx", EDIT, true, false)) {
			builder.addXlsx();
		}
		if (docEditorService.hasEditor(identity, roles, "pptx", EDIT, true, false)) {
			builder.addPptx();
		}
		return builder.build();
	}
	
	static List<String> getCopySuffix(Identity identity, Roles roles) {
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		List<String> suffix = new ArrayList<>(3);
		if (docEditorService.hasEditor(identity, roles, "docx", EDIT, true, false)) {
			suffix.add("docx");
		}
		if (docEditorService.hasEditor(identity, roles, "xlsx", EDIT, true, false)) {
			suffix.add("xlsx");
		}
		if (docEditorService.hasEditor(identity, roles, "pptx", EDIT, true, false)) {
			suffix.add("pptx");
		}
		return suffix;
	}

}
