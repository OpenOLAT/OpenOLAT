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
package org.olat.modules.ceditor.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.olat.basesecurity.MediaServerModule;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.jpa.ContainerPart;
import org.olat.modules.ceditor.ui.PageEditorV2Controller;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for converting CommonMark markdown into ceditor PagePart elements
 * and persisting them to a page.
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
@Service
public class MarkdownImportService {

	/**
	 * Shared CommonMark extension list used by both the Parser and HtmlRenderer.
	 */
	static List<Extension> markdownExtensions() {
		return List.of(
			TablesExtension.create(),
			StrikethroughExtension.create(),
			TaskListItemsExtension.create(),
			AutolinkExtension.create(),
			FootnotesExtension.create()
		);
	}

	@Autowired
	private PageService pageService;

	@Autowired
	private ImageHandler imageHandler;

	@Autowired
	private MediaServerModule mediaServerModule;

	/**
	 * Convert markdown and persist all parts to the given page,
	 * wrapped in a container.
	 *
	 * @param markdown  The CommonMark markdown text
	 * @param page      The target page to append parts to
	 * @param author    The identity performing the import
	 * @param basePath  Optional base directory for resolving relative image paths
	 * @param locale    The user's locale for translating admonition titles
	 * @return MarkdownImportResult with any warnings
	 */
	public MarkdownImportResult convertAndPersist(String markdown, Page page,
			Identity author, File basePath, Locale locale) {
		if (markdown == null || markdown.isBlank()) {
			return new MarkdownImportResult(List.of());
		}

		// 1. Pre-process math blocks
		MarkdownMathPreprocessor.PreprocessResult preprocessed =
			MarkdownMathPreprocessor.preprocess(markdown);

		// 2. Parse with CommonMark + GFM Tables
		Parser parser = Parser.builder()
			.extensions(markdownExtensions())
			.build();
		Node document = parser.parse(preprocessed.text());

		// 3. Visit AST
		Translator translator = Util.createPackageTranslator(PageEditorV2Controller.class, locale);
		MarkdownPagePartVisitor visitor = new MarkdownPagePartVisitor(
			author, basePath, imageHandler, mediaServerModule, preprocessed.mathBlocks(), translator);
		document.accept(visitor);

		// 4. Persist parts in container
		List<PagePart> parts = visitor.getParts();
		if (!parts.isEmpty()) {
			ContainerPart targetContainer = findLastEmptyContainer(page);
			List<String> elementIds = new ArrayList<>();
			for (PagePart part : parts) {
				pageService.appendNewPagePart(page, part);
				elementIds.add(part.getId());
			}
			if (targetContainer != null) {
				ContainerSettings containerSettings = targetContainer.getContainerSettings();
				containerSettings.getColumn(0).getElementIds().addAll(elementIds);
				targetContainer.setLayoutOptions(ContentEditorXStream.toXml(containerSettings));
				pageService.updatePart(targetContainer);
			} else {
				ContainerSettings containerSettings = new ContainerSettings();
				containerSettings.setType(ContainerLayout.block_1col);
				containerSettings.setNumOfColumns(ContainerLayout.block_1col.numberOfBlocks());
				containerSettings.getColumn(0).setElementIds(elementIds);

				ContainerPart containerPart = new ContainerPart();
				containerPart.setLayoutOptions(ContentEditorXStream.toXml(containerSettings));
				pageService.appendNewPagePart(page, containerPart);
			}
		}

		return new MarkdownImportResult(visitor.getWarnings());
	}

	private ContainerPart findLastEmptyContainer(Page page) {
		if (page == null) return null;
		List<PagePart> existingParts = pageService.getPageParts(page);
		if (existingParts == null || existingParts.isEmpty()) return null;
		PagePart lastPart = existingParts.get(existingParts.size() - 1);
		if (lastPart instanceof ContainerPart container) {
			ContainerSettings settings = container.getContainerSettings();
			if (settings != null && settings.getAllElementIds().isEmpty()) {
				return container;
			}
		}
		return null;
	}
}
