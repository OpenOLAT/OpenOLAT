/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.taxonomy.manager.importexport;

import static org.olat.modules.taxonomy.manager.importexport.TaxonomyFileFormat.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.TreeSet;

import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.TaxonomyManagedFlag;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.importexport.FileRow.Status;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Export and import of a complete taxonomy: metadata, level types, levels,
 * translations, teaser and background images. Not part of the export: the
 * lost+found, the management users, the competences, the relations to other
 * objects and the documents of the document pool.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
@Service
public class TaxonomyImportExportService {

	private static final Logger log = Tracing.createLoggerFor(TaxonomyImportExportService.class);

	public static final int MAX_TAXONOMY_IDENTIFIER = 64;
	public static final int MAX_LEVEL_IDENTIFIER = 255;
	public static final int MAX_TYPE_IDENTIFIER = 64;
	public static final int MAX_EXTERNAL_ID = 64;
	public static final int MAX_CSS_CLASS = 64;
	public static final int MAX_TITLE = 255;

	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private DocumentPoolModule docPoolModule;
	@Autowired
	private QuestionPoolModule questionPoolModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private CurriculumModule curriculumModule;

	// ------------------------------------------------------------------------
	// Export
	// ------------------------------------------------------------------------

	/**
	 * Write the complete taxonomy as ZIP.
	 */
	public void exportZip(Taxonomy taxonomy, Identity doer, Locale locale, OutputStream out) throws IOException {
		TaxonomyFileData data = loadExportData(taxonomy, doer, locale);
		new TaxonomyFileWriter(getTranslator(locale)).writeZip(data, out);
	}

	public String getExportFilename(Taxonomy taxonomy) {
		String identifier = StringHelper.containsNonWhitespace(taxonomy.getIdentifier())
				? taxonomy.getIdentifier() : taxonomy.getKey().toString();
		String date = Formatter.formatDatetimeFilesystemSave(new Date()).substring(0, 10).replace("-", "");
		return StringHelper.transformDisplayNameToFileSystemName("Taxonomy_" + identifier + "_" + date) + ".zip";
	}

	/**
	 * Load the data of the export. The order of all lists is deterministic, so
	 * that the same taxonomy produces the same sheets.
	 */
	public TaxonomyFileData loadExportData(Taxonomy taxonomy, Identity doer, Locale locale) {
		Taxonomy reloadedTaxonomy = taxonomyService.getTaxonomy(taxonomy);
		TaxonomyFileData data = new TaxonomyFileData();
		data.setIdentifier(reloadedTaxonomy.getIdentifier());
		data.setDisplayName(reloadedTaxonomy.getDisplayName());
		data.setDescription(reloadedTaxonomy.getDescription());
		data.setExternalId(reloadedTaxonomy.getExternalId());
		data.setManagedFlags(emptyToNull(reloadedTaxonomy.getManagedFlagsString()));

		loadExportInformation(reloadedTaxonomy, doer, locale, data);
		loadExportLevelTypes(reloadedTaxonomy, data);
		loadExportLevels(reloadedTaxonomy, data);
		return data;
	}

	private void loadExportInformation(Taxonomy taxonomy, Identity doer, Locale locale, TaxonomyFileData data) {
		Translator translator = getTranslator(locale);
		Map<String,String> info = data.getExportInformation();
		info.put(INFO_EXPORT_URL, Settings.getServerContextPathURI());
		info.put(INFO_EXPORT_VERSION, Settings.getVersion());
		info.put(INFO_EXPORT_LANGUAGE, locale.getDisplayLanguage(locale));
		info.put(INFO_EXPORT_DATE, Formatter.getInstance(locale).formatDateAndTime(new Date()));
		info.put(INFO_EXPORT_BY, doer == null ? null : userManager.getUserDisplayName(doer));
		info.put(INFO_TAXONOMY_USAGE, joinMultiValues(getUsage(taxonomy, translator)));
	}

	private List<String> getUsage(Taxonomy taxonomy, Translator translator) {
		Long key = taxonomy.getKey();
		List<String> usage = new ArrayList<>();
		if(repositoryModule.getTaxonomyRefs().stream().map(TaxonomyRef::getKey).anyMatch(key::equals)) {
			usage.add(translator.translate("taxonomy.infos.learning.resources.enabled"));
		}
		if(key.toString().equals(questionPoolModule.getTaxonomyQPoolKey())) {
			usage.add(translator.translate("taxonomy.infos.question.pool.enabled"));
		}
		if(key.toString().equals(docPoolModule.getTaxonomyTreeKey())) {
			usage.add(translator.translate("taxonomy.infos.doc.pool.enabled"));
		}
		if(portfolioModule.isTaxonomyLinkingEnabled() && portfolioModule.isTaxonomyLinked(key)) {
			usage.add(translator.translate("taxonomy.infos.e.portfolio.enabled"));
		}
		if(curriculumModule.getTaxonomyRefs().stream().map(TaxonomyRef::getKey).anyMatch(key::equals)) {
			usage.add(translator.translate("taxonomy.infos.course.planer.enabled"));
		}
		if(mediaModule.isTaxonomyLinked(key, false)) {
			usage.add(translator.translate("taxonomy.infos.mediacenter.enabled"));
		}
		return usage;
	}

	private void loadExportLevelTypes(Taxonomy taxonomy, TaxonomyFileData data) {
		List<TaxonomyLevelType> types = new ArrayList<>(taxonomyService.getTaxonomyLevelTypes(taxonomy));
		types.sort(Comparator.comparing(TaxonomyLevelType::getIdentifier, Comparator.nullsFirst(String::compareTo))
				.thenComparing(TaxonomyLevelType::getKey));

		for(TaxonomyLevelType type:types) {
			FileLevelType exType = new FileLevelType();
			exType.setIdentifier(type.getIdentifier());
			exType.setDisplayName(type.getDisplayName());
			exType.setDescription(type.getDescription());
			exType.setExternalId(type.getExternalId());
			exType.setCssClass(type.getCssClass());
			exType.setVisible(type.isVisible());
			exType.setAllowedAsSubject(type.isAllowedAsSubject());
			exType.setAllowedAsCompetence(type.isAllowedAsCompetence());
			exType.setManagedFlags(emptyToNull(type.getManagedFlagsString()));
			exType.setLibraryEnabled(type.isDocumentsLibraryEnabled());
			exType.setLibraryManage(type.isDocumentsLibraryManageCompetenceEnabled());
			exType.setLibraryTeachRead(type.isDocumentsLibraryTeachCompetenceReadEnabled());
			exType.setLibraryTeachReadParentLevels(type.getDocumentsLibraryTeachCompetenceReadParentLevels());
			exType.setLibraryTeachWrite(type.isDocumentsLibraryTeachCompetenceWriteEnabled());
			exType.setLibraryHaveRead(type.isDocumentsLibraryHaveCompetenceReadEnabled());
			exType.setLibraryTargetRead(type.isDocumentsLibraryTargetCompetenceReadEnabled());

			Set<String> subTypes = new TreeSet<>();
			for(TaxonomyLevelTypeToType typeToType:type.getAllowedTaxonomyLevelSubTypes()) {
				TaxonomyLevelType subType = typeToType.getAllowedSubTaxonomyLevelType();
				if(subType != null && subType.getIdentifier() != null) {
					subTypes.add(subType.getIdentifier());
				}
			}
			exType.setSubTypes(new ArrayList<>(subTypes));
			data.getLevelTypes().add(exType);
		}
	}

	private void loadExportLevels(Taxonomy taxonomy, TaxonomyFileData data) {
		Map<Long,TaxonomyLevel> levelsByKey = new LinkedHashMap<>();
		for(TaxonomyLevel level:taxonomyService.getTaxonomyLevels(taxonomy)) {
			levelsByKey.putIfAbsent(level.getKey(), level);
		}

		List<String> lostAndFoundIdentifiers = taxonomyModule.getLostAndFoundsIdentifiers();
		List<FileLevel> levels = new ArrayList<>(levelsByKey.size());
		Map<FileLevel,TaxonomyLevel> fileToLevel = new HashMap<>();
		for(TaxonomyLevel level:levelsByKey.values()) {
			List<String> identifiersLine = getIdentifiersLine(level, levelsByKey);
			// the lost+found levels of the document pool and their children are not exported
			if(identifiersLine.stream().anyMatch(lostAndFoundIdentifiers::contains)) {
				continue;
			}
			FileLevel exLevel = new FileLevel();
			exLevel.setPath(toPath(identifiersLine));
			exLevel.setIdentifier(level.getIdentifier());
			exLevel.setExternalId(level.getExternalId());
			exLevel.setType(level.getType() == null ? null : level.getType().getIdentifier());
			exLevel.setSortOrder(level.getSortOrder() == null ? null : level.getSortOrder().toString());
			exLevel.setManagedFlags(emptyToNull(level.getManagedFlagsString()));
			levels.add(exLevel);
			fileToLevel.put(exLevel, level);
		}
		levels.sort(Comparator.comparing(FileLevel::getPath));

		// Translations: only the languages with at least one value
		List<String> languageKeys = new ArrayList<>(new TreeSet<>(i18nModule.getEnabledLanguageKeys()));
		Set<String> usedLanguages = new TreeSet<>();
		for(FileLevel exLevel:levels) {
			TaxonomyLevel level = fileToLevel.get(exLevel);
			for(String languageKey:languageKeys) {
				FileTranslation translation = loadTranslation(level, languageKey);
				if(translation != null && !translation.isEmpty()) {
					exLevel.putTranslation(languageKey, translation.title(), translation.description());
					usedLanguages.add(languageKey);
				}
			}
		}
		data.getLanguages().addAll(usedLanguages);

		// Media
		Set<String> usedFolders = new HashSet<>();
		for(FileLevel exLevel:levels) {
			TaxonomyLevel level = fileToLevel.get(exLevel);
			VFSLeaf teaser = taxonomyService.getTeaserImage(level);
			VFSLeaf background = taxonomyService.getBackgroundImage(level);
			if(teaser instanceof LocalFileImpl || background instanceof LocalFileImpl) {
				String folder = getMediaFolder(exLevel, usedFolders);
				exLevel.setTeaserImage(toMedia(folder, TEASER, teaser));
				exLevel.setBackgroundImage(toMedia(folder, BACKGROUND, background));
			}
		}

		data.getLevels().addAll(levels);
	}

	private FileMedia toMedia(String folder, String kind, VFSLeaf leaf) {
		if(leaf instanceof LocalFileImpl localLeaf) {
			String filename = leaf.getName();
			return new FileMedia(folder + kind + "/" + filename, filename, localLeaf.getBasefile());
		}
		return null;
	}

	/**
	 * The media folder of a level: the identifiers of the path, normalized to
	 * be valid on all file systems. A folder used twice (case insensitive) gets
	 * a suffix. The levels are sorted, so the suffix is deterministic.
	 */
	private String getMediaFolder(FileLevel level, Set<String> usedFolders) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(MEDIA_FOLDER);
		for(String segment:level.getPathSegments()) {
			sb.append("/").append(FileUtils.normalizeFilename(segment));
		}
		String folder = sb.toString();
		String uniqueFolder = folder;
		for(int i=2; usedFolders.contains(uniqueFolder.toLowerCase(Locale.ROOT)); i++) {
			uniqueFolder = folder + "_" + i;
		}
		usedFolders.add(uniqueFolder.toLowerCase(Locale.ROOT));
		return uniqueFolder + "/";
	}

	private List<String> getIdentifiersLine(TaxonomyLevel level, Map<Long,TaxonomyLevel> levelsByKey) {
		List<String> identifiers = new ArrayList<>();
		Set<Long> visited = new HashSet<>();
		TaxonomyLevel current = level;
		while(current != null && visited.add(current.getKey())) {
			identifiers.add(0, current.getIdentifier() == null ? "" : current.getIdentifier());
			TaxonomyLevel parent = current.getParent();
			current = parent == null ? null : levelsByKey.getOrDefault(parent.getKey(), parent);
		}
		return identifiers;
	}

	private FileTranslation loadTranslation(TaxonomyLevel level, String languageKey) {
		I18nItem titleItem = getI18nItem(TaxonomyUIFactory.PREFIX_DISPLAY_NAME + level.getI18nSuffix(), languageKey);
		I18nItem descriptionItem = getI18nItem(TaxonomyUIFactory.PREFIX_DESCRIPTION + level.getI18nSuffix(), languageKey);
		if(titleItem == null || descriptionItem == null) {
			return null;
		}
		String title = i18nManager.getLocalizedString(titleItem, null);
		String description = i18nManager.getLocalizedString(descriptionItem, null);
		return new FileTranslation(title, description);
	}

	private I18nItem getI18nItem(String i18nKey, String languageKey) {
		Locale locale = i18nManager.getLocaleOrNull(languageKey);
		if(locale == null) return null;
		Locale overlayLocale = i18nModule.getOverlayLocales().get(locale);
		if(overlayLocale == null) return null;
		return i18nManager.getI18nItem(TaxonomyUIFactory.BUNDLE_NAME, i18nKey, overlayLocale);
	}

	/**
	 * @return A small example taxonomy with two level types and three levels,
	 * 		used as downloadable example of the import.
	 */
	public TaxonomyFileData createExampleData() {
		TaxonomyFileData data = new TaxonomyFileData();
		data.setIdentifier("SUBJECTS");
		data.setDisplayName("Subjects");
		data.getLanguages().addAll(List.of("de", "en"));

		FileLevelType subject = new FileLevelType();
		subject.setIdentifier("S");
		subject.setDisplayName("Subject");
		subject.setSubTypes(List.of("T"));
		data.getLevelTypes().add(subject);
		FileLevelType topic = new FileLevelType();
		topic.setIdentifier("T");
		topic.setDisplayName("Topic");
		data.getLevelTypes().add(topic);

		data.getLevels().add(exampleLevel("/MATH/", "MATH", "S", "1", "Mathematik", "Mathematics", "<p>Mathematics is an important discipline.</p>"));
		data.getLevels().add(exampleLevel("/MATH/GEO/", "GEO", "T", null, "Geometrie", "Geometry", null));
		data.getLevels().add(exampleLevel("/BIO/", "BIO", "S", "2", "Biologie", "Biology", null));
		return data;
	}

	private static FileLevel exampleLevel(String path, String identifier, String type, String sortOrder,
			String titleDe, String titleEn, String descriptionEn) {
		FileLevel level = new FileLevel();
		level.setPath(path);
		level.setIdentifier(identifier);
		level.setType(type);
		level.setSortOrder(sortOrder);
		level.putTranslation("de", titleDe, null);
		level.putTranslation("en", titleEn, descriptionEn);
		return level;
	}

	// ------------------------------------------------------------------------
	// Read and validate
	// ------------------------------------------------------------------------

	/**
	 * Read an import file (ZIP, XLSX, CSV). Call {@link TaxonomyFileData#cleanUp()}
	 * when the data is not needed anymore.
	 */
	public TaxonomyFileData read(File file, String filename) {
		Set<String> headerLabels = new HashSet<>();
		for(String languageKey:i18nModule.getEnabledLanguageKeys()) {
			Locale locale = i18nManager.getLocaleOrNull(languageKey);
			if(locale != null) {
				Translator translator = getTranslator(locale);
				headerLabels.add(translator.translate("importexport.level.path"));
				headerLabels.add(translator.translate("taxonomy.level.path"));
			}
		}
		return new TaxonomyFileReader(headerLabels, i18nModule.getEnabledLanguageKeys()).read(file, filename);
	}

	/**
	 * Validate the data against the target taxonomy. Set the status and the
	 * messages of every row.
	 *
	 * @param data The data read from the file
	 * @param target The existing taxonomy or null if the import creates a new taxonomy
	 */
	public void validate(TaxonomyFileData data, Taxonomy target) {
		List<TaxonomyLevelType> existingTypes = target == null ? List.of() : taxonomyService.getTaxonomyLevelTypes(target);
		List<TaxonomyLevel> existingLevels = target == null ? List.of() : taxonomyService.getTaxonomyLevels(target);

		validateLevelTypes(data, existingTypes);
		validateLevels(data, existingTypes, existingLevels);
	}

	private void validateLevelTypes(TaxonomyFileData data, List<TaxonomyLevelType> existingTypes) {
		Set<String> identifiers = new HashSet<>();
		for(FileLevelType type:data.getLevelTypes()) {
			String identifier = type.getIdentifier();
			if(!StringHelper.containsNonWhitespace(identifier)) {
				type.addError("importexport.type.identifier", "importexport.error.mandatory");
			} else if(identifier.length() > MAX_TYPE_IDENTIFIER) {
				type.addError("importexport.type.identifier", "importexport.error.too.long", Integer.toString(MAX_TYPE_IDENTIFIER));
			} else if(hasControlCharacters(identifier)) {
				type.addError("importexport.type.identifier", "importexport.error.control.characters");
			} else if(!identifiers.add(identifier)) {
				type.addError("importexport.type.identifier", "importexport.error.type.duplicate", identifier);
			}

			if(!StringHelper.containsNonWhitespace(type.getDisplayName())) {
				type.addWarning("importexport.type.display.name", "importexport.warning.type.display.name");
				type.setDisplayName(identifier);
			} else if(type.getDisplayName().length() > MAX_TITLE) {
				type.addError("importexport.type.display.name", "importexport.error.too.long", Integer.toString(MAX_TITLE));
			}
			validateLength(type, "importexport.type.external.id", type.getExternalId(), MAX_EXTERNAL_ID);
			validateLength(type, "importexport.type.css.class", type.getCssClass(), MAX_CSS_CLASS);
			if(type.getDescription() != null) {
				type.setDescription(FilterFactory.getXSSFilter().filter(type.getDescription()));
			}
			type.setManagedFlags(validateManagedFlags(type, "importexport.managed.flags", type.getManagedFlags(),
					TaxonomyLevelTypeManagedFlag.class));

			List<TaxonomyLevelType> matches = identifier == null ? List.of() : existingTypes.stream()
					.filter(existing -> identifier.equals(existing.getIdentifier()))
					.toList();
			if(matches.size() > 1) {
				type.addError("importexport.type.identifier", "importexport.error.type.existing.duplicate", identifier);
			} else if(matches.size() == 1) {
				type.setExistingType(matches.get(0));
			}
		}

		warnDuplicateExternalIds(data.getLevelTypes(), FileLevelType::getExternalId, FileLevelType::getExistingType,
				existingTypes, TaxonomyLevelType::getExternalId, "importexport.type.external.id");

		// Sub-types must exist in the file or in the target
		for(FileLevelType type:data.getLevelTypes()) {
			List<String> validSubTypes = new ArrayList<>();
			for(String subType:type.getSubTypes()) {
				if(identifiers.contains(subType) || existingTypes.stream().anyMatch(t -> subType.equals(t.getIdentifier()))) {
					validSubTypes.add(subType);
				} else {
					type.addWarning("importexport.type.sub.types", "importexport.warning.sub.type.unknown", subType);
				}
			}
			type.setSubTypes(validSubTypes);
			type.setStatus(getStatus(type));
		}
	}

	private Status getStatus(FileLevelType type) {
		if(type.hasErrors()) {
			return Status.error;
		}
		TaxonomyLevelType existing = type.getExistingType();
		if(existing == null) {
			return Status.created;
		}
		Set<String> existingSubTypes = new TreeSet<>();
		for(TaxonomyLevelTypeToType typeToType:existing.getAllowedTaxonomyLevelSubTypes()) {
			existingSubTypes.add(typeToType.getAllowedSubTaxonomyLevelType().getIdentifier());
		}
		compare(type, "importexport.type.display.name", existing.getDisplayName(), type.getDisplayName());
		compare(type, "importexport.type.description", existing.getDescription(), type.getDescription());
		compare(type, "importexport.type.external.id", existing.getExternalId(), type.getExternalId());
		compare(type, "importexport.type.css.class", existing.getCssClass(), type.getCssClass());
		compare(type, "importexport.type.visible", existing.isVisible(), type.isVisible());
		compare(type, "importexport.type.allowed.subject", existing.isAllowedAsSubject(), type.isAllowedAsSubject());
		compare(type, "importexport.type.allowed.competence", existing.isAllowedAsCompetence(), type.isAllowedAsCompetence());
		compare(type, "importexport.type.sub.types", joinMultiValues(new ArrayList<>(existingSubTypes)),
				joinMultiValues(new ArrayList<>(new TreeSet<>(type.getSubTypes()))));
		compare(type, "importexport.type.library.enabled", existing.isDocumentsLibraryEnabled(), type.isLibraryEnabled());
		compare(type, "importexport.type.library.manage", existing.isDocumentsLibraryManageCompetenceEnabled(), type.isLibraryManage());
		compare(type, "importexport.type.library.teach.read", existing.isDocumentsLibraryTeachCompetenceReadEnabled(), type.isLibraryTeachRead());
		compare(type, "importexport.type.library.teach.read.levels", Integer.toString(existing.getDocumentsLibraryTeachCompetenceReadParentLevels()),
				Integer.toString(type.getLibraryTeachReadParentLevels()));
		compare(type, "importexport.type.library.teach.write", existing.isDocumentsLibraryTeachCompetenceWriteEnabled(), type.isLibraryTeachWrite());
		compare(type, "importexport.type.library.have.read", existing.isDocumentsLibraryHaveCompetenceReadEnabled(), type.isLibraryHaveRead());
		compare(type, "importexport.type.library.target.read", existing.isDocumentsLibraryTargetCompetenceReadEnabled(), type.isLibraryTargetRead());
		return type.getChanges().isEmpty() ? Status.unchanged : Status.changed;
	}

	private void validateLevels(TaxonomyFileData data, List<TaxonomyLevelType> existingTypes, List<TaxonomyLevel> existingLevels) {
		Map<Long,TaxonomyLevel> existingByKey = new HashMap<>();
		for(TaxonomyLevel level:existingLevels) {
			existingByKey.putIfAbsent(level.getKey(), level);
		}
		Map<String,List<TaxonomyLevel>> existingByPath = new HashMap<>();
		for(TaxonomyLevel level:existingByKey.values()) {
			String path = toPath(getIdentifiersLine(level, existingByKey));
			existingByPath.computeIfAbsent(path, p -> new ArrayList<>(1)).add(level);
		}

		Map<String,String> enabledLanguages = new HashMap<>();
		for(String languageKey:i18nModule.getEnabledLanguageKeys()) {
			enabledLanguages.put(languageKey.toLowerCase(Locale.ROOT).replace('-', '_'), languageKey);
		}

		Map<String,FileLevel> levelsByPath = new HashMap<>();
		for(FileLevel level:data.getLevels()) {
			String path = level.getNormalizedPath();
			if(StringHelper.containsNonWhitespace(level.getPath()) && !level.getPathSegments().isEmpty()) {
				if(levelsByPath.containsKey(path)) {
					level.addError("importexport.level.path", "importexport.error.path.duplicate", path);
				} else {
					levelsByPath.put(path, level);
				}

				List<TaxonomyLevel> existing = existingByPath.get(path);
				if(existing != null && existing.size() > 1) {
					level.addError("importexport.level.path", "importexport.error.path.existing.duplicate", path);
				} else if(existing != null) {
					level.setExistingLevel(existing.get(0));
				}
			}
			validateLevelFields(level, enabledLanguages);

			// Type
			String type = level.getType();
			if(type != null && data.getLevelType(type) == null) {
				List<TaxonomyLevelType> matches = existingTypes.stream().filter(t -> type.equals(t.getIdentifier())).toList();
				if(matches.size() > 1) {
					level.addError("importexport.level.type", "importexport.error.type.existing.duplicate", type);
				} else if(matches.isEmpty()) {
					if(type.length() > MAX_TYPE_IDENTIFIER || hasControlCharacters(type)) {
						level.addError("importexport.level.type", "importexport.error.type.invalid", type);
					} else {
						FileLevelType implicitType = new FileLevelType();
						implicitType.setIdentifier(type);
						implicitType.setDisplayName(type);
						implicitType.setImplicit(true);
						implicitType.setStatus(Status.created);
						data.getLevelTypes().add(implicitType);
						level.addWarning("importexport.level.type", "importexport.warning.type.created", type);
					}
				}
			} else if(type != null && data.getLevelType(type).hasErrors()) {
				level.addError("importexport.level.type", "importexport.error.type.with.errors", type);
			}
		}

		warnDuplicateExternalIds(data.getLevels(), FileLevel::getExternalId, FileLevel::getExistingLevel,
				existingByKey.values(), TaxonomyLevel::getExternalId, "importexport.level.external.id");

		// Parents: in the target or in the file
		for(FileLevel level:data.getLevels()) {
			if(level.getPathSegments().size() <= 1 || level.hasErrors()) {
				continue;
			}
			String parentPath = level.getParentPath();
			FileLevel parentInFile = levelsByPath.get(parentPath);
			if(parentInFile != null) {
				if(parentInFile.hasErrors()) {
					level.addError("importexport.level.path", "importexport.error.parent.with.errors", parentPath);
				}
			} else if(!existingByPath.containsKey(parentPath)) {
				level.addError("importexport.level.path", "importexport.error.parent.missing", parentPath);
			}
		}

		// Status, repeated to propagate the errors of the parents
		for(FileLevel level:data.getLevels()) {
			if(!level.hasErrors() && level.getExistingLevel() == null
					&& level.getTranslations().values().stream().noneMatch(t -> StringHelper.containsNonWhitespace(t.title()))) {
				level.addError("importexport.level.title", "importexport.error.title.missing");
			}
		}
		boolean changed = true;
		while(changed) {
			changed = false;
			for(FileLevel level:data.getLevels()) {
				FileLevel parentInFile = levelsByPath.get(level.getParentPath());
				if(!level.hasErrors() && parentInFile != null && parentInFile != level && parentInFile.hasErrors()) {
					level.addError("importexport.level.path", "importexport.error.parent.with.errors", level.getParentPath());
					changed = true;
				}
			}
		}
		for(FileLevel level:data.getLevels()) {
			level.setStatus(getStatus(level));
		}
	}

	/**
	 * The external ID comes from external systems (REST) and is imported as it is.
	 * Two levels (or two types) of the same taxonomy with the same external ID are
	 * most certainly an error in the data: warn, but import.
	 * 
	 * @param rows The rows of the file
	 * @param existing The objects of the target taxonomy
	 */
	private <R extends FileRow, E> void warnDuplicateExternalIds(List<R> rows, Function<R,String> rowExternalId,
			Function<R,E> rowExisting, Collection<E> existing, Function<E,String> existingExternalId, String column) {
		Map<String,Integer> countInFile = new HashMap<>();
		for(R row:rows) {
			String externalId = rowExternalId.apply(row);
			if(StringHelper.containsNonWhitespace(externalId)) {
				countInFile.merge(externalId, 1, Integer::sum);
			}
		}
		for(R row:rows) {
			String externalId = rowExternalId.apply(row);
			if(!StringHelper.containsNonWhitespace(externalId)) continue;

			E self = rowExisting.apply(row);
			boolean inTarget = existing.stream()
					.anyMatch(object -> object != self && externalId.equals(existingExternalId.apply(object)));
			if(countInFile.get(externalId) > 1 || inTarget) {
				row.addWarning(column, "importexport.warning.external.id.duplicate", externalId);
			}
		}
	}

	private void validateLevelFields(FileLevel level, Map<String,String> enabledLanguages) {
		List<String> segments = level.getPathSegments();
		if(!StringHelper.containsNonWhitespace(level.getPath()) || segments.isEmpty()) {
			level.addError("importexport.level.path", "importexport.error.mandatory");
		}

		String identifier = level.getIdentifier();
		if(!StringHelper.containsNonWhitespace(identifier)) {
			level.addError("importexport.level.identifier", "importexport.error.mandatory");
		} else if(identifier.length() > MAX_LEVEL_IDENTIFIER) {
			level.addError("importexport.level.identifier", "importexport.error.too.long", Integer.toString(MAX_LEVEL_IDENTIFIER));
		} else if(hasControlCharacters(identifier)) {
			level.addError("importexport.level.identifier", "importexport.error.control.characters");
		} else if(!segments.isEmpty() && !identifier.equals(segments.get(segments.size() - 1))) {
			level.addError("importexport.level.path", "importexport.error.path.identifier", identifier);
		}

		validateLength(level, "importexport.level.external.id", level.getExternalId(), MAX_EXTERNAL_ID);

		String sortOrder = level.getSortOrder();
		if(sortOrder != null) {
			try {
				Integer.parseInt(sortOrder);
			} catch (NumberFormatException e) {
				level.addError("importexport.level.sort.order", "importexport.error.integer", sortOrder);
			}
		}
		level.setManagedFlags(validateManagedFlags(level, "importexport.managed.flags", level.getManagedFlags(),
				TaxonomyLevelManagedFlag.class));

		// Translations: normalize the language key, filter the description
		Map<String,FileTranslation> translations = new LinkedHashMap<>(level.getTranslations());
		level.getTranslations().clear();
		for(Map.Entry<String,FileTranslation> entry:translations.entrySet()) {
			FileTranslation translation = entry.getValue();
			if(translation.isEmpty() && level.getExistingLevel() == null) {
				continue;
			}
			String languageKey = enabledLanguages.get(entry.getKey().toLowerCase(Locale.ROOT).replace('-', '_'));
			if(languageKey == null && translation.isEmpty()) {
				continue;
			}
			if(languageKey == null) {
				level.addWarning("importexport.level.language", "importexport.warning.language.disabled", entry.getKey());
				continue;
			}
			String title = translation.title();
			if(title != null && title.length() > MAX_TITLE) {
				level.addError("importexport.level.title", "importexport.error.too.long", Integer.toString(MAX_TITLE));
			} else if(title != null && hasControlCharacters(title)) {
				level.addError("importexport.level.title", "importexport.error.control.characters");
			}
			String description = translation.description();
			if(description != null) {
				description = FilterFactory.getXSSFilter().filter(description);
			}
			level.putTranslation(languageKey, title, description);
		}

		validateMedia(level, level.getTeaserImage(), "importexport.level.teaser");
		validateMedia(level, level.getBackgroundImage(), "importexport.level.background");
	}

	private void validateMedia(FileLevel level, FileMedia media, String column) {
		if(media != null && media.file() == null
				&& level.getMessages().stream().noneMatch(message -> column.equals(message.column()))) {
			level.addWarning(column, "importexport.warning.media.not.in.file", media.zipPath());
		}
	}

	private Status getStatus(FileLevel level) {
		if(level.hasErrors()) {
			return Status.error;
		}
		TaxonomyLevel existing = level.getExistingLevel();
		if(existing == null) {
			return Status.created;
		}
		
		compare(level, "importexport.level.external.id", existing.getExternalId(), level.getExternalId());
		compare(level, "importexport.level.type", existing.getType() == null ? null : existing.getType().getIdentifier(), level.getType());
		compare(level, "importexport.level.sort.order", existing.getSortOrder() == null ? null : existing.getSortOrder().toString(), level.getSortOrder());
		for(Map.Entry<String,FileTranslation> entry:level.getTranslations().entrySet()) {
			FileTranslation current = loadTranslation(existing, entry.getKey());
			FileTranslation imported = entry.getValue();
			compare(level, "importexport.level.title", entry.getKey(), current == null ? null : current.title(), imported.title());
			compare(level, "importexport.level.description", entry.getKey(), current == null ? null : current.description(), imported.description());
		}
		compareImage(level, "importexport.level.teaser", level.getTeaserImage(), taxonomyService.getTeaserImage(existing));
		compareImage(level, "importexport.level.background", level.getBackgroundImage(), taxonomyService.getBackgroundImage(existing));
		return level.getChanges().isEmpty() ? Status.unchanged : Status.changed;
	}
	
	private void compare(FileRow row, String column, boolean before, boolean after) {
		compare(row, column, toOnOff(before), toOnOff(after));
	}
	
	private void compare(FileRow row, String column, String before, String after) {
		compare(row, column, null, before, after);
	}
	
	private void compare(FileRow row, String column, String language, String before, String after) {
		if(!Objects.equals(emptyToNull(before), emptyToNull(after))) {
			row.addChange(column, language, emptyToNull(before), emptyToNull(after), false);
		}
	}
	
	private void compareImage(FileLevel level, String column, FileMedia media, VFSLeaf stored) {
		if(!isSameImage(media, stored)) {
			String storedName = stored == null ? null : stored.getName();
			String newName = FileUtils.cleanFilename(media.filename());
			level.addChange(column, null, storedName, newName, newName.equals(storedName));
		}
	}

	/**
	 * @return true if the file does not bring an image or if the image is the
	 * 		same as the stored one (name and content).
	 */
	private boolean isSameImage(FileMedia media, VFSLeaf stored) {
		if(media == null || media.file() == null) {
			return true;
		}
		if(!(stored instanceof LocalFileImpl localStored)
				|| !FileUtils.cleanFilename(media.filename()).equals(stored.getName())) {
			return false;
		}
		try {
			return Files.mismatch(media.file().toPath(), localStored.getBasefile().toPath()) == -1l;
		} catch (IOException e) {
			log.debug("Cannot compare images", e);
			return false;
		}
	}

	private void validateLength(FileRow row, String column, String value, int maxLength) {
		if(value != null && value.length() > maxLength) {
			row.addError(column, "importexport.error.too.long", Integer.toString(maxLength));
		}
	}

	private <E extends Enum<E>> String validateManagedFlags(FileRow row, String column, String flags, Class<E> flagClass) {
		if(!StringHelper.containsNonWhitespace(flags)) {
			return null;
		}
		List<String> validFlags = new ArrayList<>();
		for(String flag:flags.split(",")) {
			String trimmed = flag.trim();
			if(trimmed.isEmpty()) continue;
			if(EnumUtils.isValidEnum(flagClass, trimmed)) {
				validFlags.add(trimmed);
			} else {
				row.addWarning(column, "importexport.warning.managed.flag.unknown", trimmed);
			}
		}
		return validFlags.isEmpty() ? null : String.join(",", validFlags);
	}

	// ------------------------------------------------------------------------
	// Import
	// ------------------------------------------------------------------------

	/**
	 * Import the validated data. Rows with errors and ignored rows are skipped.
	 *
	 * @param data The validated data
	 * @param target The existing taxonomy or null to create a new taxonomy
	 * @param options The options
	 * @return The result
	 */
	public TaxonomyImportResult importData(TaxonomyFileData data, Taxonomy target, TaxonomyImportOptions options) {
		TaxonomyImportResult result = new TaxonomyImportResult();

		// The database part in one transaction: an error leaves nothing behind
		List<ImportedLevel> importedLevels;
		try {
			Taxonomy taxonomy = target;
			// the metadata of an existing taxonomy is never changed by an import
			if(taxonomy == null) {
				taxonomy = createTaxonomy(data, options);
			}
			result.setTaxonomy(taxonomy);

			Map<String,TaxonomyLevelType> types = importLevelTypes(data, taxonomy, options, result);
			importedLevels = importLevels(data, taxonomy, types, options, result);
			dbInstance.commit();
		} catch(RuntimeException e) {
			dbInstance.rollbackAndCloseSession();
			throw e;
		}

		// Translations and images are files, written when the levels exist
		for(ImportedLevel importedLevel:importedLevels) {
			try {
				saveTranslations(importedLevel.level(), importedLevel.exLevel(), importedLevel.update());
				saveImages(importedLevel.level(), importedLevel.exLevel(), options.doer());
			} catch(Exception e) {
				log.error("Cannot save translations or images of taxonomy level: {}", importedLevel.level().getKey(), e);
			}
		}
		return result;
	}

	private record ImportedLevel(TaxonomyLevel level, FileLevel exLevel, boolean update) {
		//
	}

	private Taxonomy createTaxonomy(TaxonomyFileData data, TaxonomyImportOptions options) {
		String identifier = StringHelper.containsNonWhitespace(options.identifier()) ? options.identifier().trim() : data.getIdentifier();
		// never create a second taxonomy with the same identifier
		identifier = getUniqueTaxonomyIdentifier(identifier);
		String displayName = StringHelper.containsNonWhitespace(options.displayName()) ? options.displayName() : data.getDisplayName();
		if(!StringHelper.containsNonWhitespace(displayName)) {
			displayName = identifier;
		}
		String description = data.getDescription() == null ? null : FilterFactory.getXSSFilter().filter(data.getDescription());
		// the external ID is set by external systems and never edited in OpenOlat: taken as it is
		Taxonomy taxonomy = taxonomyService.createTaxonomy(identifier, displayName, description, data.getExternalId());
		if(StringHelper.containsNonWhitespace(data.getManagedFlags())) {
			taxonomy.setManagedFlags(TaxonomyManagedFlag.toEnum(data.getManagedFlags()));
			taxonomy = taxonomyService.updateTaxonomy(taxonomy);
		}
		return taxonomy;
	}

	/**
	 * @param identifier The identifier (reference)
	 * @return true if an other taxonomy already has this identifier
	 */
	public boolean isTaxonomyIdentifierUsed(String identifier) {
		return StringHelper.containsNonWhitespace(identifier) && getTaxonomyIdentifiers().contains(identifier);
	}

	/**
	 * @param identifier The identifier of the file
	 * @return The identifier, or with the first free suffix -2, -3, ... if an
	 * 		other taxonomy already uses it
	 */
	public String getUniqueTaxonomyIdentifier(String identifier) {
		if(!StringHelper.containsNonWhitespace(identifier)) {
			return identifier;
		}
		Set<String> usedIdentifiers = getTaxonomyIdentifiers();
		String base = identifier.trim();
		String candidate = base;
		for(int i=2; usedIdentifiers.contains(candidate); i++) {
			String suffix = "-" + i;
			candidate = (base.length() + suffix.length() > MAX_TAXONOMY_IDENTIFIER
					? base.substring(0, MAX_TAXONOMY_IDENTIFIER - suffix.length()) : base) + suffix;
		}
		return candidate;
	}

	private Set<String> getTaxonomyIdentifiers() {
		Set<String> identifiers = new HashSet<>();
		for(Taxonomy taxonomy:taxonomyService.getTaxonomyList()) {
			identifiers.add(taxonomy.getIdentifier());
		}
		return identifiers;
	}

	private Map<String,TaxonomyLevelType> importLevelTypes(TaxonomyFileData data, Taxonomy taxonomy,
			TaxonomyImportOptions options, TaxonomyImportResult result) {
		Map<String,TaxonomyLevelType> types = new HashMap<>();
		for(TaxonomyLevelType existingType:taxonomyService.getTaxonomyLevelTypes(taxonomy)) {
			types.putIfAbsent(existingType.getIdentifier(), existingType);
		}

		List<FileLevelType> updatedTypes = new ArrayList<>();
		for(FileLevelType exType:data.getLevelTypes()) {
			if(!exType.isImportable()) continue;

			TaxonomyLevelType type = types.get(exType.getIdentifier());
			if(type == null && options.mode().isAddNew()) {
				type = taxonomyService.createTaxonomyLevelType(exType.getIdentifier(), exType.getDisplayName(),
						exType.getDescription(), exType.getExternalId(), exType.isAllowedAsCompetence(), taxonomy);
				type = applyLevelType(type, exType, false);
				types.put(type.getIdentifier(), type);
				updatedTypes.add(exType);
				result.incrementCreatedTypes();
			} else if(type != null && options.mode().isUpdateExisting() && !exType.isImplicit() && exType.getStatus() == Status.changed) {
				type = applyLevelType(type, exType, true);
				types.put(type.getIdentifier(), type);
				updatedTypes.add(exType);
				result.incrementUpdatedTypes();
			}
		}

		// Allowed sub-types, when all types exist
		for(FileLevelType exType:updatedTypes) {
			if(exType.isImplicit()) continue;
			TaxonomyLevelType type = types.get(exType.getIdentifier());
			if(TaxonomyLevelTypeManagedFlag.isManaged(type, TaxonomyLevelTypeManagedFlag.subTypes)
					&& exType.getStatus() != Status.created) {
				continue;
			}
			List<TaxonomyLevelType> subTypes = exType.getSubTypes().stream()
					.map(types::get)
					.filter(Objects::nonNull)
					.toList();
			type = taxonomyService.updateTaxonomyLevelType(type, subTypes);
			types.put(type.getIdentifier(), type);
		}
		return types;
	}

	private TaxonomyLevelType applyLevelType(TaxonomyLevelType type, FileLevelType exType, boolean respectManagedFlags) {
		if(!respectManagedFlags || !TaxonomyLevelTypeManagedFlag.isManaged(type, TaxonomyLevelTypeManagedFlag.displayName)) {
			type.setDisplayName(exType.getDisplayName());
		}
		if(!respectManagedFlags || !TaxonomyLevelTypeManagedFlag.isManaged(type, TaxonomyLevelTypeManagedFlag.description)) {
			type.setDescription(exType.getDescription());
		}
		if(!respectManagedFlags || !TaxonomyLevelTypeManagedFlag.isManaged(type, TaxonomyLevelTypeManagedFlag.externalId)) {
			type.setExternalId(exType.getExternalId());
		}
		if(!respectManagedFlags || !TaxonomyLevelTypeManagedFlag.isManaged(type, TaxonomyLevelTypeManagedFlag.cssClass)) {
			type.setCssClass(exType.getCssClass());
		}
		if(!respectManagedFlags || !TaxonomyLevelTypeManagedFlag.isManaged(type, TaxonomyLevelTypeManagedFlag.visibility)) {
			type.setVisible(exType.isVisible());
		}
		type.setAllowedAsSubject(exType.isAllowedAsSubject());
		type.setAllowedAsCompetence(exType.isAllowedAsCompetence());
		if(!respectManagedFlags || !TaxonomyLevelTypeManagedFlag.isManaged(type, TaxonomyLevelTypeManagedFlag.librarySettings)) {
			type.setDocumentsLibraryEnabled(exType.isLibraryEnabled());
			type.setDocumentsLibraryManageCompetenceEnabled(exType.isLibraryManage());
			type.setDocumentsLibraryTeachCompetenceReadEnabled(exType.isLibraryTeachRead());
			type.setDocumentsLibraryTeachCompetenceReadParentLevels(exType.getLibraryTeachReadParentLevels());
			type.setDocumentsLibraryTeachCompetenceWriteEnabled(exType.isLibraryTeachWrite());
			type.setDocumentsLibraryHaveCompetenceReadEnabled(exType.isLibraryHaveRead());
			type.setDocumentsLibraryTargetCompetenceReadEnabled(exType.isLibraryTargetRead());
		}
		if(!respectManagedFlags) {
			// an update never changes the flags of an external management
			type.setManagedFlags(TaxonomyLevelTypeManagedFlag.toEnum(exType.getManagedFlags()));
		}
		return taxonomyService.updateTaxonomyLevelType(type);
	}

	private List<ImportedLevel> importLevels(TaxonomyFileData data, Taxonomy taxonomy, Map<String,TaxonomyLevelType> types,
			TaxonomyImportOptions options, TaxonomyImportResult result) {
		List<ImportedLevel> importedLevels = new ArrayList<>();
		Map<Long,TaxonomyLevel> existingByKey = new HashMap<>();
		for(TaxonomyLevel level:taxonomyService.getTaxonomyLevels(taxonomy)) {
			existingByKey.putIfAbsent(level.getKey(), level);
		}
		Map<String,TaxonomyLevel> levelsByPath = new HashMap<>();
		for(TaxonomyLevel level:existingByKey.values()) {
			levelsByPath.putIfAbsent(toPath(getIdentifiersLine(level, existingByKey)), level);
		}

		// Parents first, the order of the file within the same depth
		List<FileLevel> levels = new ArrayList<>(data.getLevels().stream().filter(FileLevel::isImportable).toList());
		levels.sort(Comparator.comparingInt(level -> level.getPathSegments().size()));

		for(FileLevel exLevel:levels) {
			String path = exLevel.getNormalizedPath();
			TaxonomyLevel parent = levelsByPath.get(exLevel.getParentPath());
			if(exLevel.getPathSegments().size() > 1 && parent == null) {
				// expected when the mode does not create the new parent
				log.debug("Parent not found for taxonomy level: {}", path);
				continue;
			}

			TaxonomyLevel level = levelsByPath.get(path);
			if(level == null && options.mode().isAddNew()) {
				level = taxonomyService.createTaxonomyLevel(exLevel.getIdentifier(), taxonomyService.createI18nSuffix(),
						exLevel.getExternalId(), TaxonomyLevelManagedFlag.toEnum(exLevel.getManagedFlags()), parent, taxonomy);
				level.setSortOrder(toInteger(exLevel.getSortOrder()));
				level.setType(exLevel.getType() == null ? null : types.get(exLevel.getType()));
				level = taxonomyService.updateTaxonomyLevel(level);
				importedLevels.add(new ImportedLevel(level, exLevel, false));
				levelsByPath.put(path, level);
				result.incrementCreatedLevels();
			} else if(level != null && options.mode().isUpdateExisting() && exLevel.getStatus() == Status.changed) {
				if(!TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.externalId)) {
					level.setExternalId(exLevel.getExternalId());
				}
				if(!TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.sortOrder)) {
					level.setSortOrder(toInteger(exLevel.getSortOrder()));
				}
				if(!TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.type)) {
					level.setType(exLevel.getType() == null ? null : types.get(exLevel.getType()));
				}
				level = taxonomyService.updateTaxonomyLevel(level);
				importedLevels.add(new ImportedLevel(level, exLevel, true));
				levelsByPath.put(path, level);
				result.incrementUpdatedLevels();
			}
		}
		return importedLevels;
	}

	private void saveTranslations(TaxonomyLevel level, FileLevel exLevel, boolean respectManagedFlags) {
		boolean titleManaged = respectManagedFlags && TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.displayName);
		boolean descriptionManaged = respectManagedFlags && TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.description);
		for(Map.Entry<String,FileTranslation> entry:exLevel.getTranslations().entrySet()) {
			FileTranslation translation = entry.getValue();
			if(!titleManaged) {
				I18nItem titleItem = getI18nItem(TaxonomyUIFactory.PREFIX_DISPLAY_NAME + level.getI18nSuffix(), entry.getKey());
				if(titleItem != null) {
					i18nManager.saveOrUpdateI18nItem(titleItem, translation.title());
				}
			}
			if(!descriptionManaged) {
				I18nItem descriptionItem = getI18nItem(TaxonomyUIFactory.PREFIX_DESCRIPTION + level.getI18nSuffix(), entry.getKey());
				if(descriptionItem != null) {
					i18nManager.saveOrUpdateI18nItem(descriptionItem, translation.description());
				}
			}
		}
	}

	private void saveImages(TaxonomyLevel level, FileLevel exLevel, Identity doer) {
		FileMedia teaser = exLevel.getTeaserImage();
		if(teaser != null && teaser.file() != null && teaser.file().exists()) {
			taxonomyService.storeTeaserImage(level, doer, teaser.file(), teaser.filename());
		}
		FileMedia background = exLevel.getBackgroundImage();
		if(background != null && background.file() != null && background.file().exists()) {
			taxonomyService.storeBackgroundImage(level, doer, background.file(), background.filename());
		}
	}

	private static Integer toInteger(String value) {
		if(!StringHelper.containsNonWhitespace(value)) return null;
		try {
			return Integer.valueOf(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String emptyToNull(String value) {
		return StringHelper.containsNonWhitespace(value) ? value : null;
	}

	private Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
	}
}
