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

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocumentEditorService;
import org.olat.core.commons.services.doceditor.DocTemplates.Builder;
import org.olat.core.id.Identity;
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
class GTAUIFactory {
	
	static Mode getOpenMode(VFSLeaf vfsLeaf, Identity identity, boolean readOnly) {
		DocumentEditorService docEditorService = CoreSpringFactory.getImpl(DocumentEditorService.class);
		if (!readOnly && docEditorService.hasEditor(vfsLeaf, Mode.EDIT, identity)) {
			return Mode.EDIT;
		} else if (docEditorService.hasEditor(vfsLeaf, Mode.VIEW, identity)) {
			return Mode.VIEW;
		}
		return null;
	}
	
	static DocEditorConfigs getEditorConfig(VFSContainer vfsContainer, String filePath, Long courseRepoKey) {
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
				.addConfig(htmlEditorConfig)
				.build();
	}
	
	static DocTemplates htmlOffice(Locale locale) {
		DocumentEditorService docEditorService = CoreSpringFactory.getImpl(DocumentEditorService.class);
		Builder builder = DocTemplates.builder(locale);
		if (docEditorService.hasEditor("html", EDIT)) {
			builder.addHtml();
		}
		if (docEditorService.hasEditor("docx", EDIT)) {
			builder.addDocx();
		}
		if (docEditorService.hasEditor("xlsx", EDIT)) {
			builder.addXlsx();
		}
		if (docEditorService.hasEditor("pptx", EDIT)) {
			builder.addPptx();
		}
		return builder.build();
	}

}
