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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.logging.log4j.Logger;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
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
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.ai.AiImageDescriptionSPI;
import org.olat.core.commons.services.ai.AiImageHelper;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.model.AiImageDescriptionData;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
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
import org.olat.modules.cemedia.MediaCenterLicenseHandler;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.model.SearchMediaParameters.Scope;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

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
	private final Translator translator;

	private final HtmlRenderer inlineRenderer;
	private final HttpClientService httpClientService;

	public MarkdownPagePartVisitor(Identity author, File basePath,
			ImageHandler imageHandler, MediaServerModule mediaServerModule,
			HttpClientService httpClientService,
			Map<String, String> mathBlocks, Translator translator) {
		this.author = author;
		this.basePath = basePath;
		this.imageHandler = imageHandler;
		this.mediaServerModule = mediaServerModule;
		this.httpClientService = httpClientService;
		this.mathBlocks = mathBlocks;
		this.translator = translator;
		this.inlineRenderer = HtmlRenderer.builder()
			.escapeHtml(true)
			.sanitizeUrls(true)
			.urlSanitizer(new DefaultUrlSanitizer(List.of("http", "https", "mailto")))
			.extensions(MarkdownImportService.markdownExtensions())
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
		// Detect GitHub-style admonition [!TYPE]
		MarkdownAdmonitionMapping.AdmonitionResult admonition =
			MarkdownAdmonitionMapping.detectAdmonition(blockQuote);

		AlertBoxType alertType = admonition != null ? admonition.type() : AlertBoxType.note;

		String html = renderChildrenToHtml(blockQuote);

		ParagraphPart part = new ParagraphPart();
		part.setContent(html);
		TextSettings textSettings = new TextSettings();
		AlertBoxSettings alertBox = AlertBoxSettings.getPredefined();
		alertBox.setShowAlertBox(true);
		alertBox.setWithIcon(admonition != null);
		alertBox.setType(alertType);
		if (admonition != null && translator != null) {
			alertBox.setTitle(translator.translate(alertType.getI18nKey()));
		}
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
		warnings.add("import.markdown.warn.html.skipped");
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
			warnings.add("import.markdown.warn.table.empty");
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
			warnings.add("import.markdown.warn.image.not.found\t" + StringHelper.escapeHtml(destination));
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
			// check for duplicate media file, reuse same image that has already been uploaded
			Media media = null;
			MediaService mediaService = (MediaService) CoreSpringFactory.getImpl(MediaService.class); 
			if (mediaService.isInMediaCenter(author, imageFile)) {
				SearchMediaParameters params = new SearchMediaParameters();
				String checksum = FileUtils.checksumSha256(imageFile);
				params.setChecksum(checksum);
				params.setIdentity(author);
				params.setScope(Scope.ALL);
				List<MediaWithVersion> versions =  mediaService.searchMedias(params);
				if (versions.size() > 0) {
					media = versions.get(0).media();
				}
			}
			if (media == null && imageHandler != null) {
				media = imageHandler.createMedia(
					mediaTitle, null, altText, imageFile, filename,
					null, author, MediaLog.Action.IMPORTED
				);
				// Set default license if license module is enabled for media center
				LicenseModule licenseModule = CoreSpringFactory.getImpl(LicenseModule.class);
				MediaCenterLicenseHandler licenseHandler = CoreSpringFactory.getImpl(MediaCenterLicenseHandler.class);
				if (licenseModule.isEnabled(licenseHandler)) {
					LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);
					licenseService.createDefaultLicense(media, licenseHandler, author);
				}
				// Enrich media with AI-generated metadata
				doAutoGenerateAiMetadata(media, imageFile, filename, mediaService);
			}

			MediaPart mediaPart = MediaPart.valueOf(author, media);
			parts.add(mediaPart);
			imageCount++;
		} catch (Exception e) {
			log.warn("Failed to import image: {}", destination, e);
			warnings.add("import.markdown.warn.image.import.failed\t" + StringHelper.escapeHtml(destination) + "\t" + StringHelper.escapeHtml(e.getMessage()));
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

		// Data URIs: decode base64 payload to a temp file
		if (destination.startsWith("data:")) {
			return decodeDataUriImage(destination);
		}

		// Local file paths are only allowed when a basePath is provided (file upload).
		// Without basePath (text paste), only remote URLs are supported to prevent
		// arbitrary file reads from the server filesystem.
		if (basePath == null) {
			warnings.add("import.markdown.warn.image.local.unsupported\t" + StringHelper.escapeHtml(destination));
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
				warnings.add("import.markdown.warn.image.path.outside\t" + StringHelper.escapeHtml(destination));
				return null;
			}
		} catch (Exception e) {
			warnings.add("import.markdown.warn.image.path.resolve\t" + StringHelper.escapeHtml(destination));
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
				log.debug("Could not extract filename from URL: {}", destination, e);
			}
		}
		if (destination.startsWith("data:")) {
			String mimeType = extractDataUriMimeType(destination);
			String suffix = mimeTypeToSuffix(mimeType);
			return "image" + suffix;
		}
		return imageFile.getName();
	}

	private void doAutoGenerateAiMetadata(Media media, File imageFile, String filename, MediaService mediaService) {
		try {
			AiModule aiModule = CoreSpringFactory.getImpl(AiModule.class);
			if (!aiModule.isImageDescriptionGeneratorEnabled()) return;

			AiImageHelper aiImageHelper = CoreSpringFactory.getImpl(AiImageHelper.class);
			String suffix = getSuffix(filename);
			if (suffix == null) return;
			String mimeType = aiImageHelper.getMimeType(suffix);
			if (mimeType == null) return;

			String base64 = aiImageHelper.prepareImageBase64(imageFile, suffix);
			if (base64 == null) return;

			AiImageDescriptionSPI generator = aiModule.getImageDescriptionGenerator();
			if (generator == null) return;

			Locale locale = translator != null ? translator.getLocale() : Locale.ENGLISH;
			AiImageDescriptionResponse response = generator.generateImageDescription(base64, mimeType, locale);
			if (!response.isSuccess() || response.getDescription() == null) return;

			AiImageDescriptionData data = response.getDescription();

			// Update title if current title looks like a filename
			if (StringHelper.containsNonWhitespace(data.getTitle())) {
				String currentTitle = media.getTitle();
				if (!StringHelper.containsNonWhitespace(currentTitle) || isFilenameLike(currentTitle)) {
					media.setTitle(data.getTitle());
				}
			}
			if (StringHelper.containsNonWhitespace(data.getDescription())) {
				media.setDescription(data.getDescription());
			}
			if (StringHelper.containsNonWhitespace(data.getAltText())) {
				media.setAltText(data.getAltText());
			}
			mediaService.updateMedia(media);

			// Add tags
			List<String> tags = new ArrayList<>();
			if (StringHelper.containsNonWhitespace(data.getOrientation())) {
				tags.add(data.getOrientation().toLowerCase());
			}
			for (String tag : data.getColorTags()) {
				tags.add(tag.toLowerCase());
			}
			for (String tag : data.getCategoryTags()) {
				tags.add(tag.toLowerCase());
			}
			for (String tag : data.getKeywords()) {
				tags.add(tag.toLowerCase());
			}
			if (!tags.isEmpty()) {
				mediaService.updateTags(author, media, tags);
			}

			// Map AI subject to taxonomy level
			if (StringHelper.containsNonWhitespace(data.getSubject())) {
				mapSubjectToTaxonomy(media, data.getSubject(), mediaService);
			}
		} catch (Exception e) {
			log.warn("Failed to auto-generate AI metadata for image: {}", filename, e);
		}
	}

	private void mapSubjectToTaxonomy(Media media, String subject, MediaService mediaService) {
		MediaModule mediaModule = CoreSpringFactory.getImpl(MediaModule.class);
		List<TaxonomyRef> taxonomyRefs = mediaModule.getTaxonomyRefs();
		if (taxonomyRefs.isEmpty()) return;

		TaxonomyService taxonomyService = CoreSpringFactory.getImpl(TaxonomyService.class);
		List<TaxonomyLevel> levels = taxonomyService.getTaxonomyLevels(taxonomyRefs);
		String subjectLower = subject.trim().toLowerCase();

		for (TaxonomyLevel level : levels) {
			if (translator != null) {
				String displayName = TaxonomyUIFactory.translateDisplayName(translator, level);
				if (displayName != null && subjectLower.equals(displayName.trim().toLowerCase())) {
					mediaService.updateTaxonomyLevels(media, List.of(new TaxonomyLevelRefImpl(level.getKey())));
					return;
				}
			}
			String identifier = level.getIdentifier();
			if (identifier != null && subjectLower.equals(identifier.trim().toLowerCase())) {
				mediaService.updateTaxonomyLevels(media, List.of(new TaxonomyLevelRefImpl(level.getKey())));
				return;
			}
		}
	}

	private String getSuffix(String filename) {
		if (filename == null) return null;
		int dotPos = filename.lastIndexOf('.');
		if (dotPos >= 0 && dotPos < filename.length() - 1) {
			return filename.substring(dotPos + 1);
		}
		return null;
	}

	private boolean isFilenameLike(String title) {
		if (title == null) return false;
		return title.matches("(?i).*\\.(jpe?g|png|gif|webp|svg|bmp|tiff?)$");
	}

	private File decodeDataUriImage(String dataUri) {
		String mimeType = extractDataUriMimeType(dataUri);
		if (mimeType == null || !ImageHandler.mimeTypes.contains(mimeType)) {
			warnings.add("import.markdown.warn.datauri.type\t" + (mimeType != null ? mimeType : "unknown"));
			return null;
		}

		int commaIdx = dataUri.indexOf(',');
		if (commaIdx < 0) {
			warnings.add("import.markdown.warn.datauri.malformed");
			return null;
		}

		// Only accept base64 encoding
		String header = dataUri.substring(0, commaIdx);
		if (!header.contains(";base64")) {
			warnings.add("import.markdown.warn.datauri.encoding");
			return null;
		}

		String base64Data = dataUri.substring(commaIdx + 1);
		try {
			// Check estimated decoded size before allocating memory to prevent
			// OutOfMemoryError from oversized payloads (base64 expands 3 bytes → 4 chars)
			long estimatedBytes = (long) base64Data.length() * 3 / 4;
			if (estimatedBytes > MAX_DOWNLOAD_BYTES) {
				warnings.add("import.markdown.warn.datauri.toolarge\t" + (MAX_DOWNLOAD_BYTES / (1024 * 1024)));
				return null;
			}
			byte[] decoded = Base64.getDecoder().decode(base64Data);

			String suffix = mimeTypeToSuffix(mimeType);
			Path tempFile = Files.createTempFile("md_img_", suffix);
			Files.write(tempFile, decoded);
			File file = tempFile.toFile();
			file.deleteOnExit();
			return file;
		} catch (IllegalArgumentException e) {
			warnings.add("import.markdown.warn.datauri.base64");
			return null;
		} catch (Exception e) {
			log.warn("Failed to decode data URI image", e);
			warnings.add("import.markdown.warn.datauri.decode.failed\t" + StringHelper.escapeHtml(e.getMessage()));
			return null;
		}
	}

	private String extractDataUriMimeType(String dataUri) {
		// Format: data:<mime>;base64,<data> or data:<mime>,<data>
		if (!dataUri.startsWith("data:")) {
			return null;
		}
		int semicolonIdx = dataUri.indexOf(';');
		int commaIdx = dataUri.indexOf(',');
		int endIdx = semicolonIdx > 0 && (commaIdx < 0 || semicolonIdx < commaIdx) ? semicolonIdx : commaIdx;
		if (endIdx <= 5) {
			return null;
		}
		return dataUri.substring(5, endIdx).toLowerCase();
	}

	private static String mimeTypeToSuffix(String mimeType) {
		if (mimeType == null) return ".png";
		return switch (mimeType) {
			case "image/gif" -> ".gif";
			case "image/jpg", "image/jpeg" -> ".jpg";
			case "image/svg+xml" -> ".svg";
			default -> ".png";
		};
	}

	private File downloadRemoteImage(String url) {
		// Check domain allowlist via MediaServerModule (SSRF protection)
		if (mediaServerModule != null && mediaServerModule.isRestrictedDomain(url)) {
			warnings.add("import.markdown.warn.image.domain.blocked\t" + StringHelper.escapeHtml(url));
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

			HttpGet get = new HttpGet(uri);
			try (CloseableHttpClient httpClient = httpClientService.createThreadSafeHttpClient(true);
					CloseableHttpResponse response = httpClient.execute(get)) {

				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != 200) {
					warnings.add("import.markdown.warn.image.download.http\t" + statusCode + "\t" + StringHelper.escapeHtml(url));
					return null;
				}

				Path tempFile = Files.createTempFile("md_img_", suffix);
				try (InputStream in = response.getEntity().getContent()) {
					// Enforce size limit to prevent disk exhaustion
					long bytesWritten = 0;
					byte[] buffer = new byte[8192];
					try (var out = Files.newOutputStream(tempFile)) {
						int read;
						while ((read = in.read(buffer)) != -1) {
							bytesWritten += read;
							if (bytesWritten > MAX_DOWNLOAD_BYTES) {
								Files.deleteIfExists(tempFile);
								warnings.add("import.markdown.warn.image.download.toolarge\t" + (MAX_DOWNLOAD_BYTES / (1024 * 1024)) + "\t" + StringHelper.escapeHtml(url));
								return null;
							}
							out.write(buffer, 0, read);
						}
					}
				}

				File downloaded = tempFile.toFile();
				downloaded.deleteOnExit();
				return downloaded;
			}
		} catch (Exception e) {
			log.warn("Failed to download remote image: {}", url, e);
			warnings.add("import.markdown.warn.image.download.failed\t" + StringHelper.escapeHtml(url));
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
