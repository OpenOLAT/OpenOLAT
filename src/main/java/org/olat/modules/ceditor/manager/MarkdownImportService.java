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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.olat.basesecurity.MediaServerModule;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.jpa.ContainerPart;
import org.olat.modules.ceditor.ui.PageEditorV2Controller;
import org.olat.modules.ceditor.ui.PageElementTarget;
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
		List<Extension> extensions = new java.util.ArrayList<>(List.of(
			TablesExtension.create(),
			StrikethroughExtension.create(),
			TaskListItemsExtension.create(),
			AutolinkExtension.create(),
			FootnotesExtension.create(),
			ImageAttributesExtension.create(),
			YamlFrontMatterExtension.create(),
			HighlightExtension.create()
		));
		return extensions;
	}

	@Autowired
	private PageService pageService;

	@Autowired
	private ImageHandler imageHandler;

	@Autowired
	private MediaServerModule mediaServerModule;

	@Autowired
	private HttpClientService httpClientService;

	/**
	 * Convert markdown and persist all parts to the given page,
	 * wrapped in a container.
	 *
	 * @param markdown            The CommonMark markdown text
	 * @param page                The target page to append parts to
	 * @param author              The identity performing the import
	 * @param aiOres              The ores for the AI usage context
	 * @param subIdent            The ores subIdent the AI usage context
	 * @param basePath            Optional base directory for resolving relative image paths
	 * @param locale              The user's locale for translating admonition titles
	 * @param targetContainerId   Optional container element ID to add parts to (null for default behavior)
	 * @param targetColumn        Column index within the target container (-1 for default behavior)
	 * @param referenceElementId  Optional element ID for before/after positioning (null for default)
	 * @param target              Optional position target (above/below relative to referenceElementId)
	 * @return MarkdownImportResult with any warnings
	 */
	public MarkdownImportResult convertAndPersist(String markdown, Page page, Identity author, OLATResourceable aiOres,
			String subIdent, File basePath, Locale locale, String targetContainerId, int targetColumn,
			String referenceElementId, PageElementTarget target) {
		if (markdown == null || markdown.isBlank()) {
			return new MarkdownImportResult(List.of());
		}

		// 1a. Pre-process math blocks FIRST so $$...$$ content is replaced with
		// placeholders before the admonition preprocessor scans the text. This
		// prevents any stray `!!!` inside LaTeX math from being mistaken for a
		// MkDocs admonition marker.
		MarkdownMathPreprocessor.PreprocessResult preprocessed =
			MarkdownMathPreprocessor.preprocess(markdown);

		// 1b. Pre-process MkDocs-style admonitions (!!! type) into blockquote form
		String withAdmonitions =
			MarkdownMkDocsAdmonitionPreprocessor.preprocess(preprocessed.text());

		// 2. Parse with CommonMark + GFM Tables
		Parser parser = Parser.builder()
			.extensions(markdownExtensions())
			.build();
		Node document = parser.parse(withAdmonitions);

		// Extract image dimensions from ImageAttributes nodes BEFORE the visitor
		// processes the tree (the HtmlRenderer strips ImageAttributes during rendering)
		Map<String, int[]> imageDimensions = extractImageDimensions(document);

		// 3. Visit AST
		Translator translator = Util.createPackageTranslator(PageEditorV2Controller.class, locale);
		MarkdownPagePartVisitor visitor = new MarkdownPagePartVisitor(author, aiOres, subIdent, basePath, imageHandler,
				mediaServerModule, httpClientService, preprocessed.mathBlocks(), translator);
		visitor.setImageDimensions(imageDimensions);
		document.accept(visitor);

		// 4. Persist parts in container
		List<PagePart> parts = visitor.getParts();
		if (!parts.isEmpty()) {
			// Resolve target container BEFORE appending parts (appending changes the last element)
			int[] resolvedColumn = new int[]{ 0 };
			int[] insertIndex = new int[]{ -1 };
			ContainerPart resolvedContainer = resolveTargetContainer(page, targetContainerId, targetColumn,
					referenceElementId, target, resolvedColumn, insertIndex);

			List<String> elementIds = new ArrayList<>();
			for (PagePart part : parts) {
				pageService.appendNewPagePart(page, part);
				elementIds.add(part.getId());
			}

			if (resolvedContainer != null) {
				ContainerSettings containerSettings = resolvedContainer.getContainerSettings();
				ContainerColumn column = containerSettings.getColumn(resolvedColumn[0]);
				if (column == null) {
					column = containerSettings.getColumn(0);
				}
				if (insertIndex[0] >= 0 && insertIndex[0] <= column.getElementIds().size()) {
					column.getElementIds().addAll(insertIndex[0], elementIds);
				} else {
					column.getElementIds().addAll(elementIds);
				}
				resolvedContainer.setLayoutOptions(ContentEditorXStream.toXml(containerSettings));
				pageService.updatePart(resolvedContainer);
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

	/**
	 * Resolve the target container with a single DB query.
	 * Priority: explicit container ID → before/after reference element → last empty container.
	 *
	 * @param page               The page to search
	 * @param containerId        Optional explicit container ID (may be null)
	 * @param requestedColumn    Requested column index
	 * @param referenceElementId Optional element ID for before/after positioning
	 * @param target             Optional position target (above/below)
	 * @param outColumn          Single-element array; receives the resolved column index
	 * @param outInsertIndex     Single-element array; receives the insert position within the column (-1 = append)
	 * @return The resolved container, or null if a new one must be created
	 */
	private ContainerPart resolveTargetContainer(Page page, String containerId, int requestedColumn,
			String referenceElementId, PageElementTarget target, int[] outColumn, int[] outInsertIndex) {
		if (page == null) return null;
		List<PagePart> existingParts = pageService.getPageParts(page);
		if (existingParts == null || existingParts.isEmpty()) return null;

		// 1. Explicit target container from "add content" dialog (within)
		if (containerId != null) {
			for (PagePart part : existingParts) {
				if (part instanceof ContainerPart container && containerId.equals(container.getId())) {
					outColumn[0] = Math.max(requestedColumn, 0);
					outInsertIndex[0] = -1;
					return container;
				}
			}
		}

		// 2. Before/after a reference element: find its container and position
		if (referenceElementId != null && (target == PageElementTarget.above || target == PageElementTarget.below)) {
			for (PagePart part : existingParts) {
				if (part instanceof ContainerPart container) {
					ContainerSettings settings = container.getContainerSettings();
					if (settings == null) continue;
					for (int col = 0; col < settings.getNumOfColumns(); col++) {
						ContainerColumn column = settings.getColumn(col);
						if (column == null) continue;
						int idx = column.getElementIds().indexOf(referenceElementId);
						if (idx >= 0) {
							outColumn[0] = col;
							outInsertIndex[0] = (target == PageElementTarget.below) ? idx + 1 : idx;
							return container;
						}
					}
				}
			}
		}

		// 3. Fallback: last empty container
		PagePart lastPart = existingParts.get(existingParts.size() - 1);
		if (lastPart instanceof ContainerPart container) {
			ContainerSettings settings = container.getContainerSettings();
			if (settings != null && settings.getAllElementIds().isEmpty()) {
				outColumn[0] = 0;
				outInsertIndex[0] = -1;
				return container;
			}
		}

		return null;
	}

	/**
	 * Walk the parsed AST and extract image dimensions from ImageAttributes nodes.
	 * Must be called BEFORE the visitor processes the tree, because the HtmlRenderer
	 * used during visiting strips ImageAttributes nodes.
	 *
	 * @return map of image destination → [width, height] in pixels
	 */
	private Map<String, int[]> extractImageDimensions(Node document) {
		Map<String, int[]> dimensions = new HashMap<>();
		Node child = document.getFirstChild();
		while (child != null) {
			if (child instanceof Paragraph para) {
				Node pc = para.getFirstChild();
				while (pc != null) {
					if (pc instanceof Image image) {
						Node lc = image.getLastChild();
						if (lc instanceof ImageAttributes imgAttrs) {
							int w = 0, h = 0;
							String ws = imgAttrs.getAttributes().get("width");
							String hs = imgAttrs.getAttributes().get("height");
							if (ws != null) try { w = Integer.parseInt(ws.replace("px", "").trim()); } catch (NumberFormatException e) { /* */ }
							if (hs != null) try { h = Integer.parseInt(hs.replace("px", "").trim()); } catch (NumberFormatException e) { /* */ }
							if (w > 0) {
								dimensions.put(image.getDestination(), new int[]{w, h});
							}
						}
					}
					pc = pc.getNext();
				}
			}
			child = child.getNext();
		}
		return dimensions;
	}
}
