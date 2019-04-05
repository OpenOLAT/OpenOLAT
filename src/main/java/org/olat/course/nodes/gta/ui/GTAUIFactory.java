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

import static org.olat.core.commons.services.vfs.VFSLeafEditor.Mode.EDIT;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.services.filetemplate.FileTypes;
import org.olat.core.commons.services.filetemplate.FileTypes.Builder;
import org.olat.core.commons.services.vfs.VFSLeafEditorConfigs;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.edusharing.VFSEdusharingProvider;
import org.olat.repository.ui.settings.LazyRepositoryEdusharingProvider;

/**
 * 
 * Initial date: 1 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class GTAUIFactory {
	
	static VFSLeafEditorConfigs getEditorConfig(VFSContainer vfsContainer, String filePath, Long courseRepoKey) {
		VFSEdusharingProvider edusharingProvider = courseRepoKey != null
				? new LazyRepositoryEdusharingProvider(courseRepoKey)
				: null;
		 HTMLEditorConfig htmlEditorConfig = HTMLEditorConfig.builder(vfsContainer, filePath)
				 .withMediaPath("media")
				.withAllowCustomMediaFactory(false)
				.withDisableMedia(true)
				.withEdusharingProvider(edusharingProvider)
				.build();
		 return VFSLeafEditorConfigs.builder()
				.addConfig(htmlEditorConfig)
				.build();
	}
	
	static FileTypes htmlOffice(Locale locale) {
		VFSRepositoryService vfsService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		Builder builder = FileTypes.builder(locale);
		if (vfsService.hasEditor("html", EDIT)) {
			builder.addHtml();
		}
		if (vfsService.hasEditor("docx", EDIT)) {
			builder.addDocx();
		}
		if (vfsService.hasEditor("xlsx", EDIT)) {
			builder.addXlsx();
		}
		if (vfsService.hasEditor("pptx", EDIT)) {
			builder.addPptx();
		}
		return builder.build();
	}

}
