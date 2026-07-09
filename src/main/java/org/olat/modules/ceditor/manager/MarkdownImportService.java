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
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
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
import org.olat.core.logging.Tracing;
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

	private static final Logger log = Tracing.createLoggerFor(MarkdownImportService.class);

	// Mirror the fence patterns of the CommonMark YamlFrontMatterExtension
	// (YamlFrontMatterBlockParser.REGEX_BEGIN / REGEX_END)
	private static final Pattern FRONT_MATTER_BEGIN = Pattern.compile("^-{3}(\\s.*)?");
	private static final Pattern FRONT_MATTER_END = Pattern.compile("^(-{3}|\\.{3})(\\s.*)?");

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

	/**
	 * Build the CommonMark parser for the given markdown. The YAML front matter
	 * extension consumes lines until a closing fence and silently swallows the
	 * whole document when the fence is never closed. In that case the parser is
	 * built without the extension, so the opening fence parses as a thematic
	 * break and the content is imported as regular markdown (OO-9402).
	 *
	 * @param markdown The preprocessed markdown that will be parsed
	 * @return a parser, without the front matter extension if the fence is unclosed
	 */
	static Parser buildParser(String markdown) {
		return buildParser(hasUnclosedFrontMatter(markdown));
	}

	private static Parser buildParser(boolean unclosedFrontMatter) {
		List<Extension> extensions = markdownExtensions();
		if (unclosedFrontMatter) {
			extensions.removeIf(YamlFrontMatterExtension.class::isInstance);
		}
		return Parser.builder()
			.extensions(extensions)
			.build();
	}

	/**
	 * Detect a YAML front matter opening fence that is never closed. Mirrors the
	 * fence handling of the CommonMark YamlFrontMatterExtension: the opening
	 * fence must be the first content line of the document (leading blank lines
	 * are allowed), the closing fence is a line starting with --- or ...
	 *
	 * @param markdown The markdown to check
	 * @return true if an opening fence exists but no closing fence follows
	 */
	static boolean hasUnclosedFrontMatter(String markdown) {
		String[] lines = markdown.split("\n", -1);
		int first = 0;
		while (first < lines.length && lines[first].isBlank()) {
			first++;
		}
		if (first == lines.length || !FRONT_MATTER_BEGIN.matcher(lines[first]).matches()) {
			return false;
		}
		for (int i = first + 1; i < lines.length; i++) {
			if (FRONT_MATTER_END.matcher(lines[i]).matches()) {
				return false;
			}
		}
		return true;
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
			return new MarkdownImportResult(List.of(), null, 0, -1, 0);
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

		// 2. Parse with CommonMark + GFM Tables. An unclosed YAML front matter
		// fence would swallow the whole document, so parse without the front
		// matter extension in that case and keep the content as plain markdown.
		boolean unclosedFrontMatter = hasUnclosedFrontMatter(withAdmonitions);
		if (unclosedFrontMatter) {
			log.info("Unclosed YAML front matter fence in markdown import, importing fence and metadata as plain text");
		}
		Node document = buildParser(unclosedFrontMatter).parse(withAdmonitions);

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
		ContainerPart effectiveContainer = null;
		int effectiveColumn = 0;
		int followUpInsertIndex = -1;
		if (!parts.isEmpty()) {
			// Resolve target container BEFORE appending parts (appending changes the last element)
			List<PagePart> existingParts = page == null ? List.of() : pageService.getPageParts(page);
			int[] resolvedColumn = new int[]{ 0 };
			int[] insertIndex = new int[]{ -1 };
			ContainerPart resolvedContainer = resolveTargetContainer(existingParts, targetContainerId, targetColumn,
					referenceElementId, target, resolvedColumn, insertIndex);

			List<String> elementIds = new ArrayList<>();
			for (PagePart part : parts) {
				pageService.appendNewPagePart(page, part);
				elementIds.add(part.getId());
			}

			if (resolvedContainer != null) {
				ContainerSettings containerSettings = resolvedContainer.getContainerSettings();
				effectiveColumn = effectiveColumnIndex(containerSettings, resolvedColumn[0]);
				ContainerColumn column = containerSettings.getColumn(effectiveColumn);
				if (insertIndex[0] >= 0 && insertIndex[0] <= column.getElementIds().size()) {
					column.getElementIds().addAll(insertIndex[0], elementIds);
					// A follow-up part (AI quiz placeholder) belongs directly
					// after the imported parts, not at the end of the column.
					followUpInsertIndex = insertIndex[0] + elementIds.size();
				} else {
					column.getElementIds().addAll(elementIds);
				}
				resolvedContainer.setLayoutOptions(ContentEditorXStream.toXml(containerSettings));
				effectiveContainer = pageService.updatePart(resolvedContainer);
			} else {
				ContainerSettings containerSettings = new ContainerSettings();
				containerSettings.setType(ContainerLayout.block_1col);
				containerSettings.setNumOfColumns(ContainerLayout.block_1col.numberOfBlocks());
				containerSettings.getColumn(0).setElementIds(elementIds);

				ContainerPart containerPart = new ContainerPart();
				containerPart.setLayoutOptions(ContentEditorXStream.toXml(containerSettings));
				// Position the new wrapping container at the reference element
				// (import triggered above/below a part that is not embedded in
				// any layout container); append at the page end otherwise.
				int pageIdx = pageInsertIndex(existingParts, referenceElementId, target);
				if (pageIdx >= 0) {
					effectiveContainer = pageService.appendNewPagePartAt(page, containerPart, pageIdx);
				} else {
					effectiveContainer = pageService.appendNewPagePart(page, containerPart);
				}
			}
		}

		List<String> warnings = new ArrayList<>(visitor.getWarnings());
		if (unclosedFrontMatter) {
			warnings.add(0, "import.markdown.warn.frontmatter.unclosed");
		}
		return new MarkdownImportResult(warnings, effectiveContainer, effectiveColumn, followUpInsertIndex,
				visitor.getAiMetadataJobCount());
	}

	/**
	 * Append a new {@link PagePart} to the given page and register it inside
	 * the given column of the provided container. Uses the same pattern as
	 * {@link #convertAndPersist}: persist the part on the page, then add its
	 * element id to the container column and persist the updated container.
	 *
	 * @param page        the page the container belongs to
	 * @param container   the container that should embed the new part (must not be null)
	 * @param column      the column index within the container; falls back to the
	 *                    first column when out of range
	 * @param insertIndex the position within the column, -1 or out of range to
	 *                    append at the end of the column
	 * @param part        the part to append
	 * @return the managed part returned by the PageService
	 */
	public <U extends PagePart> U appendNewPartToContainer(Page page, ContainerPart container, int column,
			int insertIndex, U part) {
		U persisted = pageService.appendNewPagePart(page, part);
		ContainerSettings settings = container.getContainerSettings();
		ContainerColumn targetColumn = settings.getColumn(effectiveColumnIndex(settings, column));
		addElementId(targetColumn.getElementIds(), persisted.getId(), insertIndex);
		container.setLayoutOptions(ContentEditorXStream.toXml(settings));
		pageService.updatePart(container);
		return persisted;
	}

	/**
	 * Insert an element id at the given position of a column's element id
	 * list; append at the end when the index is negative or out of range.
	 */
	static void addElementId(List<String> elementIds, String elementId, int insertIndex) {
		if (insertIndex >= 0 && insertIndex <= elementIds.size()) {
			elementIds.add(insertIndex, elementId);
		} else {
			elementIds.add(elementId);
		}
	}

	/**
	 * Clamp a requested column index to one that exists in the given layout:
	 * the requested index if the layout has such a column, the first column
	 * otherwise (negative or out-of-range indexes).
	 */
	static int effectiveColumnIndex(ContainerSettings settings, int requestedColumn) {
		if (requestedColumn > 0 && settings.getColumn(requestedColumn) != null) {
			return requestedColumn;
		}
		return 0;
	}

	/**
	 * Resolve the target container from the page's existing parts.
	 * Priority: explicit container ID → before/after reference element → last empty container.
	 *
	 * @param existingParts      The page's parts, loaded before any new parts are appended
	 * @param containerId        Optional explicit container ID (may be null)
	 * @param requestedColumn    Requested column index
	 * @param referenceElementId Optional element ID for before/after positioning
	 * @param target             Optional position target (above/below)
	 * @param outColumn          Single-element array; receives the resolved column index
	 * @param outInsertIndex     Single-element array; receives the insert position within the column (-1 = append)
	 * @return The resolved container, or null if a new one must be created
	 */
	static ContainerPart resolveTargetContainer(List<PagePart> existingParts, String containerId, int requestedColumn,
			String referenceElementId, PageElementTarget target, int[] outColumn, int[] outInsertIndex) {
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

		// 2. Before/after a reference element: find its container and position.
		// Iterate over the layout's real block count (getNumOfBlocks), NOT the
		// persisted numOfColumns field — updateType() leaves that field stale
		// when the author switches a layout in the inspector (OO-9497).
		if (referenceElementId != null && (target == PageElementTarget.above || target == PageElementTarget.below)) {
			for (PagePart part : existingParts) {
				if (part instanceof ContainerPart container) {
					ContainerSettings settings = container.getContainerSettings();
					if (settings == null) continue;
					for (int col = 0; col < settings.getNumOfBlocks(); col++) {
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
	 * Page-level insert position for a NEW container when the import was
	 * triggered above/below a reference part that is not embedded in any
	 * layout container: the reference part's index (above) or the index
	 * after it (below) in the page's part list.
	 *
	 * @return the insert index, or -1 to append at the end (no reference,
	 *         reference not found, or target is not above/below)
	 */
	static int pageInsertIndex(List<PagePart> existingParts, String referenceElementId, PageElementTarget target) {
		if (existingParts == null || referenceElementId == null
				|| (target != PageElementTarget.above && target != PageElementTarget.below)) {
			return -1;
		}
		for (int i = 0; i < existingParts.size(); i++) {
			if (referenceElementId.equals(existingParts.get(i).getId())) {
				return (target == PageElementTarget.below) ? i + 1 : i;
			}
		}
		return -1;
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
