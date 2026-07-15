/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.cemedia.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiImageDescriptionService;
import org.olat.core.commons.services.ai.AiImageHelper;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.model.ImageDescriptionData;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingHelper;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Asynchronous AI metadata enrichment for image medias. Callers submit a
 * freshly created media right after it has been saved; the actual vision-LLM
 * call and the optional taxonomy-matching call run later in a persisted
 * {@link MediaAiMetadataGenerationTask} on the {@code aiBatch} pool, so the
 * user never waits on a provider round-trip (same pattern as the AI question
 * generation, see {@code EssayGenerationService}).
 * <p>
 * Because the task runs after the user may have edited the media, all fields
 * are applied defensively: the title is only replaced when empty or
 * filename-like, description and alt text only when empty, tags and taxonomy
 * levels only when none are assigned yet.
 *
 * Initial date: 2026-07-06<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class MediaAiMetadataService {

	private static final Logger log = Tracing.createLoggerFor(MediaAiMetadataService.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiImageHelper aiImageHelper;
	@Autowired
	private AiImageDescriptionService imageDescriptionService;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private BaseSecurity baseSecurity;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyMatchingService taxonomyMatchingService;

	public boolean isEnabled() {
		return imageDescriptionService.isEnabled();
	}

	/**
	 * @return true when the filename has a raster image suffix the vision
	 *         models support (JPEG, PNG, GIF, WebP)
	 */
	public boolean isSupportedImage(String filename) {
		return getMimeType(filename) != null;
	}

	/**
	 * Synchronous generation for the manual "Generate metadata with AI"
	 * buttons in the media collect / upload forms. The caller fills the
	 * form fields with the result and the user confirms via save — nothing
	 * is persisted here. Deliberately blocking: the user explicitly asked
	 * for the metadata and reviews it right away.
	 *
	 * @return the provider response, or {@code null} when the file cannot
	 *         be read or is not a supported raster image
	 */
	public AiImageDescriptionResponse generateNow(File imageFile, String filename, Identity requester,
			Locale locale, String usageContextType) {
		if (imageFile == null || filename == null) {
			return null;
		}
		String mimeType = getMimeType(filename);
		if (mimeType == null) {
			return null;
		}
		String base64 = aiImageHelper.prepareImageBase64(imageFile, getSuffix(filename));
		if (base64 == null) {
			return null;
		}
		AiUsageContext usageContext = AiUsageContext.builder()
				.usageContextType(usageContextType)
				.identity(requester)
				.locale(locale)
				.resourceType("MediaCenter")
				.resourceId(0L)
				.build();
		return imageDescriptionService.generateImageDescription(usageContext, base64, mimeType, locale);
	}

	/**
	 * Schedule asynchronous AI metadata generation for the given media. A
	 * no-op returning {@code false} when the feature is disabled or the
	 * media's current version is not a supported raster image (SVG, video
	 * etc.).
	 *
	 * @param media            the freshly saved media to enrich
	 * @param requester        the user who triggered the creation
	 * @param locale           locale for the generated metadata
	 * @param usageContextType usage-log context type of the calling flow
	 *                         (e.g. {@code page-markdown-import})
	 * @param resourceType     usage-log resource type, may be {@code null}
	 * @param resourceId       usage-log resource id, may be {@code null}
	 * @param resourceSubId    usage-log resource sub id, may be {@code null}
	 * @param overwrite        {@code true} to replace existing metadata with
	 *                         the AI values (import flows, where title / alt
	 *                         text are machine-generated, e.g. Word auto alt
	 *                         texts), {@code false} to only fill empty fields
	 *                         (form flows, where the user may have entered
	 *                         values before saving)
	 * @return {@code true} when a task has been scheduled
	 */
	public boolean submit(Media media, Identity requester, Locale locale, String usageContextType,
			String resourceType, Long resourceId, String resourceSubId, boolean overwrite) {
		if (media == null || media.getKey() == null || requester == null) {
			return false;
		}
		if (!imageDescriptionService.isEnabled()) {
			return false;
		}
		MediaVersion currentVersion = getCurrentVersion(media);
		if (currentVersion == null || getMimeType(currentVersion.getRootFilename()) == null) {
			return false;
		}
		String languageTag = locale == null ? null : locale.toLanguageTag();
		MediaAiMetadataGenerationTask task = new MediaAiMetadataGenerationTask(media.getKey(),
				requester.getKey(), languageTag, usageContextType, resourceType, resourceId, resourceSubId,
				overwrite);
		taskExecutorManager.execute(task, requester, null, null, null);
		return true;
	}

	/**
	 * Execute the task body. Called from {@link MediaAiMetadataGenerationTask#run()}
	 * but also reachable directly for synchronous execution in tests. Throws
	 * when the provider call fails so the task executor marks the task row
	 * failed; a media deleted in the meantime is a silent no-op.
	 */
	public void runTask(MediaAiMetadataGenerationTask task) {
		if (task == null) return;
		Identity requester = task.getRequesterKey() == null ? null
				: baseSecurity.loadIdentityByKey(task.getRequesterKey());
		if (requester == null) {
			log.warn("Media AI metadata task has no resolvable requester (key={}) — skipping",
					task.getRequesterKey());
			return;
		}
		Media media = mediaService.getMediaByKey(task.getMediaKey());
		if (media == null) {
			log.info("Media {} no longer exists — skipping AI metadata generation", task.getMediaKey());
			return;
		}
		MediaVersion currentVersion = getCurrentVersion(media);
		if (currentVersion == null) {
			return;
		}
		String filename = currentVersion.getRootFilename();
		String mimeType = getMimeType(filename);
		if (mimeType == null) {
			return;
		}
		File mediaDir = fileStorage.getMediaDirectory(currentVersion);
		File imageFile = new File(mediaDir, filename);
		if (!imageFile.exists()) {
			log.warn("Image file of media {} not found in storage — skipping AI metadata generation",
					media.getKey());
			return;
		}
		String base64 = aiImageHelper.prepareImageBase64(imageFile, getSuffix(filename));
		if (base64 == null) {
			return;
		}

		Locale locale = task.getLanguageTag() == null ? Locale.ENGLISH
				: Locale.forLanguageTag(task.getLanguageTag());
		AiUsageContext usageContext = AiUsageContext.builder()
				.usageContextType(task.getUsageContextType())
				.identity(requester)
				.locale(locale)
				.resourceType(task.getResourceType())
				.resourceId(task.getResourceId())
				.resourceSubId(task.getResourceSubId())
				.build();
		AiImageDescriptionResponse response = imageDescriptionService
				.generateImageDescription(usageContext, base64, mimeType, locale);
		if (!response.isSuccess() || response.getDescription() == null) {
			// Usage log row with the error is already written by the service.
			throw new IllegalStateException("AI image description for media " + media.getKey()
					+ " failed: " + response.getError());
		}

		applyMetadata(media, response.getDescription(), requester, usageContext, locale, task.isOverwrite());
		dbInstance.commit();
	}

	private void applyMetadata(Media media, ImageDescriptionData data, Identity requester,
			AiUsageContext usageContext, Locale locale, boolean overwrite) {
		boolean changed = false;
		if (StringHelper.containsNonWhitespace(data.getTitle())
				&& (overwrite || !StringHelper.containsNonWhitespace(media.getTitle()) || isFilenameLike(media.getTitle()))) {
			media.setTitle(data.getTitle());
			changed = true;
		}
		if (StringHelper.containsNonWhitespace(data.getDescription())
				&& (overwrite || !StringHelper.containsNonWhitespace(media.getDescription()))) {
			media.setDescription(data.getDescription());
			changed = true;
		}
		if (StringHelper.containsNonWhitespace(data.getAltText())
				&& (overwrite || !StringHelper.containsNonWhitespace(media.getAltText()))) {
			media.setAltText(data.getAltText());
			changed = true;
		}
		if (changed) {
			media = mediaService.updateMedia(media);
		}

		if (overwrite || !hasTags(media, requester)) {
			Set<String> tags = new LinkedHashSet<>();
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
			if (StringHelper.containsNonWhitespace(data.getSubject())) {
				tags.add(data.getSubject());
			}
			if (!tags.isEmpty()) {
				mediaService.updateTags(requester, media, new ArrayList<>(tags));
			}
		}

		if (StringHelper.containsNonWhitespace(data.getSubject())
				&& (overwrite || mediaService.getTaxonomyLevels(media).isEmpty())) {
			mapSubjectToTaxonomy(media, data.getSubject(), usageContext, locale);
		}
	}

	private boolean hasTags(Media media, Identity requester) {
		return mediaService.getTagInfos(media, requester, true).stream()
				.anyMatch(tagInfo -> tagInfo.isSelected());
	}

	private void mapSubjectToTaxonomy(Media media, String subject, AiUsageContext usageContext, Locale locale) {
		List<TaxonomyRef> taxonomyRefs = mediaModule.getTaxonomyRefs();
		if (taxonomyRefs.isEmpty()) {
			log.debug("No media center taxonomy configured — skipping taxonomy mapping for media {}",
					media.getKey());
			return;
		}
		List<TaxonomyLevelRef> matches = TaxonomyMatchingHelper.matchTaxonomyLevels(
				usageContext, subject, taxonomyRefs, taxonomyService, taxonomyMatchingService, aiModule, locale);
		if (matches.isEmpty()) {
			// Embedding matching needs the taxonomy matching module enabled
			// with an SPI + embedding model; without it only exact title
			// matches hit. Log the miss so operators can diagnose.
			log.info("No taxonomy match for AI subject '{}' on media {} (embedding matching enabled: {})",
					subject, media.getKey(), aiModule.isTaxonomyMatchingEnabled());
		} else {
			mediaService.updateTaxonomyLevels(media, matches);
		}
	}

	private MediaVersion getCurrentVersion(Media media) {
		List<MediaVersion> versions = media.getVersions();
		if (versions == null || versions.isEmpty()) {
			versions = mediaService.getVersions(media);
		}
		return versions == null || versions.isEmpty() ? null : versions.get(0);
	}

	/**
	 * MIME type of the filename when it is a raster image supported by the
	 * vision models (JPEG, PNG, GIF, WebP), {@code null} otherwise (SVG,
	 * video, documents).
	 */
	private String getMimeType(String filename) {
		String suffix = getSuffix(filename);
		return suffix == null ? null : aiImageHelper.getMimeType(suffix);
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
}
