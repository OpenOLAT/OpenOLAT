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
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.Document;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.renderer.html.DefaultUrlSanitizer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.olat.basesecurity.MediaServerModule;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.AlertBoxType;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.CodeLanguage;
import org.olat.modules.ceditor.model.CodeSettings;
import org.olat.modules.ceditor.model.TableContent;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.model.TitleSettings;
import org.olat.modules.ceditor.model.jpa.CodePart;
import org.olat.modules.ceditor.model.jpa.MathPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.model.jpa.SpacerPart;
import org.olat.modules.ceditor.model.jpa.TablePart;
import org.olat.modules.ceditor.model.jpa.TitlePart;
import org.olat.modules.ceditor.ui.MarkdownImportController;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.handler.ImageHandler;

/**
 * CommonMark AST visitor that converts top-level block nodes into
 * ceditor PagePart instances.
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownPagePartVisitor extends AbstractVisitor {

	private static final Logger log = Tracing.createLoggerFor(MarkdownPagePartVisitor.class);

	private final List<PagePart> parts = new ArrayList<>();
	private final List<String> warnings = new ArrayList<>();
	private int imageCount = 0;

	private static final long MAX_DOWNLOAD_BYTES = MarkdownImportController.MAX_UPLOAD_SIZE_KB * 1024L;

	private final Identity author;
	private final File basePath;
	private final ImageHandler imageHandler;
	private final MediaServerModule mediaServerModule;
	private final Map<String, String> mathBlocks;

	private final HtmlRenderer inlineRenderer;
	private HttpClient httpClient;

	public MarkdownPagePartVisitor(Identity author, File basePath,
			ImageHandler imageHandler, MediaServerModule mediaServerModule,
			Map<String, String> mathBlocks) {
		this.author = author;
		this.basePath = basePath;
		this.imageHandler = imageHandler;
		this.mediaServerModule = mediaServerModule;
		this.mathBlocks = mathBlocks;
		this.inlineRenderer = HtmlRenderer.builder()
			.escapeHtml(true)
			.sanitizeUrls(true)
			.urlSanitizer(new DefaultUrlSanitizer(List.of("http", "https", "mailto")))
			.extensions(List.of(TablesExtension.create()))
			.build();
	}

	public List<PagePart> getParts() { return parts; }
	public List<String> getWarnings() { return warnings; }
	public int getImageCount() { return imageCount; }

	// --- Block node visitors ---

	@Override
	public void visit(Document document) {
		Node child = document.getFirstChild();
		while (child != null) {
			Node next = child.getNext();
			child.accept(this);
			child = next;
		}
	}

	@Override
	public void visit(Heading heading) {
		String text = renderChildrenToPlainText(heading);
		int level = heading.getLevel();

		TitlePart part = new TitlePart();
		part.setContent(text);
		TitleSettings settings = new TitleSettings();
		settings.setSize(level);
		settings.setLayoutSettings(BlockLayoutSettings.getPredefined());
		part.setLayoutOptions(ContentEditorXStream.toXml(settings));
		parts.add(part);
	}

	@Override
	public void visit(Paragraph paragraph) {
		String html = renderNodeToHtml(paragraph);
		if (isMathPlaceholder(html)) {
			addMathPart(html);
			return;
		}
		if (isStandaloneImage(paragraph)) {
			handleStandaloneImage(paragraph);
			return;
		}

		// Merge consecutive paragraphs into a single ParagraphPart
		if (!parts.isEmpty() && parts.get(parts.size() - 1) instanceof ParagraphPart prevParagraph
				&& prevParagraph.getLayoutOptions() == null) {
			prevParagraph.setContent(prevParagraph.getContent() + "\n" + html);
		} else {
			ParagraphPart part = new ParagraphPart();
			part.setContent(html);
			parts.add(part);
		}
	}

	@Override
	public void visit(FencedCodeBlock codeBlock) {
		CodePart part = new CodePart();
		part.setContent(codeBlock.getLiteral().stripTrailing());
		CodeSettings settings = new CodeSettings();
		settings.setCodeLanguage(
			MarkdownCodeLanguageMapping.mapToCodeLanguage(codeBlock.getInfo()));
		settings.setLayoutSettings(BlockLayoutSettings.getPredefined());
		part.setLayoutOptions(ContentEditorXStream.toXml(settings));
		parts.add(part);
	}

	@Override
	public void visit(IndentedCodeBlock codeBlock) {
		CodePart part = new CodePart();
		part.setContent(codeBlock.getLiteral().stripTrailing());
		CodeSettings settings = new CodeSettings();
		settings.setCodeLanguage(CodeLanguage.plaintext);
		settings.setLayoutSettings(BlockLayoutSettings.getPredefined());
		part.setLayoutOptions(ContentEditorXStream.toXml(settings));
		parts.add(part);
	}

	@Override
	public void visit(ThematicBreak thematicBreak) {
		SpacerPart part = new SpacerPart();
		parts.add(part);
	}

	@Override
	public void visit(BlockQuote blockQuote) {
		String html = renderChildrenToHtml(blockQuote);

		ParagraphPart part = new ParagraphPart();
		part.setContent(html);
		TextSettings textSettings = new TextSettings();
		AlertBoxSettings alertBox = AlertBoxSettings.getPredefined();
		alertBox.setShowAlertBox(true);
		alertBox.setType(AlertBoxType.note);
		alertBox.setWithIcon(true);
		textSettings.setAlertBoxSettings(alertBox);
		part.setLayoutOptions(ContentEditorXStream.toXml(textSettings));
		parts.add(part);
	}

	@Override
	public void visit(BulletList bulletList) {
		String html = renderNodeToHtml(bulletList);
		ParagraphPart part = new ParagraphPart();
		part.setContent(html);
		parts.add(part);
	}

	@Override
	public void visit(OrderedList orderedList) {
		String html = renderNodeToHtml(orderedList);
		ParagraphPart part = new ParagraphPart();
		part.setContent(html);
		parts.add(part);
	}

	@Override
	public void visit(HtmlBlock htmlBlock) {
		// HTML blocks are skipped entirely to prevent XSS and HTML injection.
		// We only accept pure markdown content.
		warnings.add("HTML block skipped (HTML is not allowed in markdown import).");
	}

	@Override
	public void visit(CustomBlock customBlock) {
		if (customBlock instanceof TableBlock tableBlock) {
			handleTable(tableBlock);
		} else {
			String html = renderNodeToHtml(customBlock);
			if (html != null && !html.isBlank()) {
				ParagraphPart part = new ParagraphPart();
				part.setContent(html);
				parts.add(part);
			}
		}
	}

	// --- Table handling ---

	private void handleTable(TableBlock tableBlock) {
		int numCols = 0;
		int numRows = 0;
		boolean hasHeader = false;

		TableHead head = null;
		TableBody body = null;
		Node child = tableBlock.getFirstChild();
		while (child != null) {
			if (child instanceof TableHead th) {
				head = th;
				hasHeader = true;
			} else if (child instanceof TableBody tb) {
				body = tb;
			}
			child = child.getNext();
		}

		if (head != null) {
			Node headerRow = head.getFirstChild();
			if (headerRow instanceof TableRow) {
				numCols = countChildren(headerRow);
				numRows++;
			}
		}

		if (body != null) {
			Node row = body.getFirstChild();
			while (row != null) {
				numRows++;
				row = row.getNext();
			}
		}

		if (numCols == 0 || numRows == 0) {
			warnings.add("Empty table encountered; skipping.");
			return;
		}

		TableContent tc = new TableContent(numRows, numCols);
		int rowIdx = 0;

		if (head != null) {
			Node headerRow = head.getFirstChild();
			if (headerRow instanceof TableRow) {
				fillTableRow(tc, headerRow, rowIdx);
				rowIdx++;
			}
		}

		if (body != null) {
			Node row = body.getFirstChild();
			while (row != null) {
				if (row instanceof TableRow) {
					fillTableRow(tc, row, rowIdx);
					rowIdx++;
				}
				row = row.getNext();
			}
		}

		TablePart tablePart = new TablePart();
		tablePart.setContent(ContentEditorXStream.toXml(tc));

		TableSettings ts = new TableSettings();
		ts.setColumnHeaders(hasHeader);
		ts.setBordered(true);
		ts.setLayoutSettings(BlockLayoutSettings.getPredefined());
		tablePart.setLayoutOptions(ContentEditorXStream.toXml(ts));

		parts.add(tablePart);
	}

	private void fillTableRow(TableContent tc, Node tableRow, int rowIdx) {
		int colIdx = 0;
		Node cell = tableRow.getFirstChild();
		while (cell != null) {
			if (cell instanceof TableCell) {
				String cellContent = renderChildrenToPlainText(cell);
				tc.addContent(rowIdx, colIdx, cellContent);
				colIdx++;
			}
			cell = cell.getNext();
		}
	}

	private int countChildren(Node node) {
		int count = 0;
		Node child = node.getFirstChild();
		while (child != null) {
			count++;
			child = child.getNext();
		}
		return count;
	}

	// --- Image handling ---

	private boolean isStandaloneImage(Paragraph paragraph) {
		Node child = paragraph.getFirstChild();
		while (child instanceof SoftLineBreak) {
			child = child.getNext();
		}
		if (!(child instanceof Image)) {
			return false;
		}
		Node afterImage = child.getNext();
		while (afterImage instanceof SoftLineBreak) {
			afterImage = afterImage.getNext();
		}
		return afterImage == null;
	}

	private Image findImageNode(Paragraph paragraph) {
		Node child = paragraph.getFirstChild();
		while (child != null) {
			if (child instanceof Image image) {
				return image;
			}
			child = child.getNext();
		}
		return null;
	}

	private void handleStandaloneImage(Paragraph paragraph) {
		Image imageNode = findImageNode(paragraph);
		if (imageNode == null) {
			return;
		}
		String destination = imageNode.getDestination();
		String altText = renderChildrenToPlainText(imageNode);
		String title = imageNode.getTitle();

		File imageFile = resolveImageFile(destination);
		if (imageFile == null || !imageFile.exists()) {
			warnings.add("Image not found: " + destination);
			ParagraphPart fallback = new ParagraphPart();
			fallback.setContent("<p>" + (altText.isEmpty() ? destination : altText) + "</p>");
			parts.add(fallback);
			return;
		}

		try {
			String filename = extractFilename(destination, imageFile);
			String mediaTitle = title != null && !title.isBlank() ? title : altText;
			if (mediaTitle.isBlank()) {
				mediaTitle = filename;
			}

			Media media = imageHandler.createMedia(
				mediaTitle, null, altText, imageFile, filename,
				null, author, MediaLog.Action.IMPORTED
			);

			MediaPart mediaPart = MediaPart.valueOf(author, media);
			parts.add(mediaPart);
			imageCount++;
		} catch (Exception e) {
			log.warn("Failed to import image: {}", destination, e);
			warnings.add("Failed to import image: " + destination + " (" + e.getMessage() + ")");
		}
	}

	private File resolveImageFile(String destination) {
		if (destination == null || destination.isBlank()) {
			return null;
		}

		// Download remote images to a temporary file
		if (destination.startsWith("http://") || destination.startsWith("https://")) {
			return downloadRemoteImage(destination);
		}

		// Data URIs are not supported
		if (destination.startsWith("data:")) {
			warnings.add("Data URI images are not supported: " + destination.substring(0, Math.min(destination.length(), 40)) + "...");
			return null;
		}

		// Local file paths are only allowed when a basePath is provided (file upload).
		// Without basePath (text paste), only remote URLs are supported to prevent
		// arbitrary file reads from the server filesystem.
		if (basePath == null) {
			warnings.add("Local image paths are not supported when pasting text: " + destination);
			return null;
		}

		// Relative or absolute file path
		File file = new File(destination);
		if (!file.isAbsolute()) {
			file = new File(basePath, destination);
		}

		// Path traversal protection
		try {
			File resolved = file.getCanonicalFile();
			if (!resolved.toPath().startsWith(basePath.getCanonicalFile().toPath())) {
				warnings.add("Image path outside allowed directory: " + destination);
				return null;
			}
		} catch (Exception e) {
			warnings.add("Cannot resolve image path: " + destination);
			return null;
		}

		return file.exists() ? file : null;
	}

	private String extractFilename(String destination, File imageFile) {
		if (destination.startsWith("http://") || destination.startsWith("https://")) {
			try {
				String path = URI.create(destination).getPath();
				String name = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
				if (!name.isBlank()) {
					return name;
				}
			} catch (Exception e) {
				// fall through
			}
		}
		return imageFile.getName();
	}

	private File downloadRemoteImage(String url) {
		// Check domain allowlist via MediaServerModule (SSRF protection)
		if (mediaServerModule != null && mediaServerModule.isRestrictedDomain(url)) {
			warnings.add("Image download blocked (domain not on allowlist): " + url);
			return null;
		}

		try {
			URI uri = URI.create(url);
			String path = uri.getPath();
			String filename = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
			if (filename.isBlank()) {
				filename = "image";
			}
			// Determine file extension from filename or default to .png
			String suffix = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')) : ".png";

			if (httpClient == null) {
				httpClient = HttpClient.newBuilder()
					.followRedirects(HttpClient.Redirect.NORMAL)
					.connectTimeout(Duration.ofSeconds(10))
					.build();
			}
			HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.timeout(Duration.ofSeconds(30))
				.GET()
				.build();
			HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

			if (response.statusCode() != 200) {
				warnings.add("Failed to download image (HTTP " + response.statusCode() + "): " + url);
				return null;
			}

			Path tempFile = Files.createTempFile("md_img_", suffix);
			try (InputStream in = response.body()) {
				// Enforce size limit to prevent disk exhaustion
				long bytesWritten = 0;
				byte[] buffer = new byte[8192];
				try (var out = Files.newOutputStream(tempFile)) {
					int read;
					while ((read = in.read(buffer)) != -1) {
						bytesWritten += read;
						if (bytesWritten > MAX_DOWNLOAD_BYTES) {
							Files.deleteIfExists(tempFile);
							warnings.add("Image too large (max " + (MAX_DOWNLOAD_BYTES / (1024 * 1024)) + " MB): " + url);
							return null;
						}
						out.write(buffer, 0, read);
					}
				}
			}

			File downloaded = tempFile.toFile();
			downloaded.deleteOnExit();
			return downloaded;
		} catch (Exception e) {
			log.warn("Failed to download remote image: {}", url, e);
			warnings.add("Failed to download image: " + url);
			return null;
		}
	}

	// --- Math handling ---

	private boolean isMathPlaceholder(String html) {
		String stripped = html.replaceAll("</?p>", "").strip();
		return stripped.startsWith(MarkdownMathPreprocessor.PLACEHOLDER_PREFIX);
	}

	private void addMathPart(String html) {
		String stripped = html.replaceAll("</?p>", "").strip();
		String latex = mathBlocks.get(stripped);
		if (latex != null) {
			MathPart part = new MathPart();
			part.setContent(latex);
			parts.add(part);
		}
	}

	// --- Inline rendering helpers ---

	private String renderNodeToHtml(Node node) {
		Document tempDoc = new Document();
		tempDoc.appendChild(node);
		return inlineRenderer.render(tempDoc).strip();
	}

	private String renderChildrenToHtml(Node node) {
		StringBuilder sb = new StringBuilder();
		Node child = node.getFirstChild();
		while (child != null) {
			Node next = child.getNext();
			sb.append(renderNodeToHtml(child));
			child = next;
		}
		return sb.toString().strip();
	}

	private String renderChildrenToPlainText(Node node) {
		StringBuilder sb = new StringBuilder();
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof Text text) {
				sb.append(text.getLiteral());
			} else if (child instanceof Code code) {
				sb.append(code.getLiteral());
			} else if (child instanceof SoftLineBreak) {
				sb.append(' ');
			} else if (child instanceof HardLineBreak) {
				sb.append(' ');
			} else if (child instanceof HtmlInline) {
				// Skip inline HTML entirely for security
			} else {
				sb.append(renderChildrenToPlainText(child));
			}
			child = child.getNext();
		}
		return sb.toString();
	}
}
