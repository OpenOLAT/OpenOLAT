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
package org.olat.modules.openbadges.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.FileStorage;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeCategory;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesBakeContext;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.OpenBadgesModule;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Badge;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.manager.RepositoryEntryDAO;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Initial date: 2023-05-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class OpenBadgesManagerImpl implements OpenBadgesManager, InitializingBean {
	private static final Logger log = Tracing.createLoggerFor(OpenBadgesManagerImpl.class);
	private static final String BADGES_VFS_FOLDER = "badges";
	private static final String TEMPLATES_VFS_FOLDER = "templates";
	private static final String CLASSES_VFS_FOLDER = "classes";
	private static final String ASSERTIONS_VFS_FOLDER = "assertions";
	private static final String TEMPLATE_IMAGE_PREVIEW_PREFIX = "._oo_preview_";
	private static final Pattern svgOpeningTagPattern = Pattern.compile("<svg[^>]*>");;
	private static final String OPEN_BADGES_ASSERTION_XML_NAMESPACE = "xmlns:openbadges=\"http://openbadges.org\"";

	@Autowired
	private FolderModule folderModule;
	@Autowired
	private BadgeTemplateDAO templateDAO;
	@Autowired
	private BadgeClassDAO badgeClassDAO;
	@Autowired
	private BadgeAssertionDAO badgeAssertionDAO;
	@Autowired
	private BadgeCategoryDAO badgeCategoryDAO;
	@Autowired
	private MovieService movieService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private TagService tagService;
	@Autowired
	private BadgeEntryConfigurationDAO badgeEntryConfigurationDAO;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private DB dbInstance;
	@Autowired
	private OpenBadgesModule openBadgesModule;
	@Autowired
	private ColorService colorService;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private AssessmentToolManager assessmentToolManager;

	private VelocityEngine velocityEngine;
	private FileStorage bakedBadgesStorage;
	private FileStorage badgeTemplatesStorage;
	private FileStorage unbakedBadgesStorage;

	@Override
	public void afterPropertiesSet() {
		getBadgeAssertionsRoot();
		bakedBadgesStorage = new FileStorage(getBadgeAssertionsRootContainer());

		getBadgeTemplatesRoot();
		badgeTemplatesStorage = new FileStorage(getBadgeTemplatesRootContainer());

		getBadgeClassesRoot();
		unbakedBadgesStorage = new FileStorage(getBadgeClassesRootContainer());

		Properties p = new Properties();
		try {
			velocityEngine = new VelocityEngine();
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("config error " + p);
		}

		createFactoryBadgeTemplates();
	}

	//
	// Template
	//

	@Override
	public void createFactoryBadgeTemplates() {
		try (InputStream jsonInputStream = OpenBadgesManagerImpl.class.getResourceAsStream("_content/badge_templates.json")) {
			String jsonString = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);
			JSONObject badgeTemplatesObject = new JSONObject(jsonString);
			JSONArray badgeTemplates = badgeTemplatesObject.getJSONArray("badgeTemplates");
			for (int i = 0; i < badgeTemplates.length(); i++) {
				JSONObject badgeTemplate = badgeTemplates.getJSONObject(i);
				String imageFileName = badgeTemplate.getString("imageFileName");
				String identifier = badgeTemplate.getString("identifier");
				JSONObject name = badgeTemplate.getJSONObject("name");
				String nameEn = name.getString("en");
				BadgeTemplate existingBadgeTemplate = getTemplate(identifier);
				if (existingBadgeTemplate != null) {
					log.debug("Badge template with identifier {} exists already", existingBadgeTemplate);
					continue;
				}
				try (InputStream imageInputStream = OpenBadgesManagerImpl.class.getResourceAsStream("_content/" + imageFileName)) {
					Collection<String> scopes = List.of(BadgeTemplate.Scope.global.toString(), BadgeTemplate.Scope.course.toString());
					createTemplate(identifier, nameEn, imageInputStream, imageFileName, "", scopes, null);
					setTemplateName(identifier, nameEn);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void setTemplateName(String identifier, String nameEn) {
		String bundleName = OpenBadgesUIFactory.getBundleName();
		String nameKey = OpenBadgesUIFactory.getTemplateNameI18nKey(identifier);

		SelectionValues languageKV = getTemplateTranslationLanguages(null);
		Map<Locale, Locale> overlayLocales = i18nModule.getOverlayLocales();

		for (String languageKey : languageKV.keys()) {
			Locale locale = i18nManager.getLocaleOrDefault(languageKey);
			Locale overlayLocale = overlayLocales.get(locale);
			I18nItem nameItem = i18nManager.getI18nItem(bundleName, nameKey, overlayLocale);
			i18nManager.saveOrUpdateI18nItem(nameItem, nameEn);
		}
	}

	@Override
	public BadgeTemplate createTemplate(String identifier, String name, File templateFile, String targetFileName, String description,
										Collection<String> scopes, Identity savedBy) {
		String templateFileName = copyTemplate(templateFile, targetFileName, savedBy);
		if (templateFileName != null) {
			BadgeTemplate badgeTemplate = templateDAO.createTemplate(identifier, templateFileName, name);
			badgeTemplate.setDescription(description);
			badgeTemplate.setScopesAsCollection(scopes);
			createPreviewImage(badgeTemplate, savedBy);
			return templateDAO.updateTemplate(badgeTemplate);
		}
		return null;
	}

	private BadgeTemplate createTemplate(String identifier, String name, InputStream imageInputStream,
										 String targetFileName, String description, Collection<String> scopes,
										 Identity savedBy) {
		String templateFileName = copyTemplate(imageInputStream, targetFileName, savedBy);
		if (templateFileName != null) {
			BadgeTemplate badgeTemplate = templateDAO.createTemplate(identifier, templateFileName, name);
			badgeTemplate.setDescription(description);
			badgeTemplate.setScopesAsCollection(scopes);
			createPreviewImage(badgeTemplate, savedBy);
			return templateDAO.updateTemplate(badgeTemplate);
		}
		return null;
	}

	private void createPreviewImage(BadgeTemplate badgeTemplate, Identity savedBy) {
		Set<String> substitutionVariables = getTemplateSvgSubstitutionVariables(badgeTemplate.getImage());
		if (substitutionVariables.isEmpty()) {
			return;
		}

		String backgroundColorId = "gold";
		String title = "Badge";

		String image = badgeTemplate.getImage();
		String previewImage = TEMPLATE_IMAGE_PREVIEW_PREFIX + image;
		String svg = getTemplateSvgImageWithSubstitutions(badgeTemplate.getImage(), backgroundColorId, title);
		VFSContainer templatesContainer = getBadgeTemplatesRootContainer();
		VFSLeaf targetLeaf = templatesContainer.createChildLeaf(previewImage);
		try (InputStream inputStream = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8))) {
			VFSManager.copyContent(inputStream, targetLeaf, savedBy, vfsRepositoryService);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Override
	public BadgeTemplate getTemplate(Long key) {
		return templateDAO.getTemplate(key);
	}

	private BadgeTemplate getTemplate(String identifier) {
		return templateDAO.getTemplate(identifier);
	}

	@Override
	public String getTemplateSvgPreviewImage(String templateImage) {
		String previewImage = TEMPLATE_IMAGE_PREVIEW_PREFIX + templateImage;
		VFSItem item = getBadgeTemplatesRootContainer().resolve(previewImage);
		if (item != null) {
			return previewImage;
		}
		return null;
	}

	@Override
	public VFSLeaf getTemplateVfsLeaf(String templateFile) {
		VFSContainer templateContainer = getBadgeTemplatesRootContainer();
		if (templateContainer.resolve(templateFile) instanceof VFSLeaf templateFileLeaf) {
			return templateFileLeaf;
		} else {
			log.error("Could not resolve file " + templateFile + " in " + templateContainer.getRelPath());
		}
		return null;
	}

	@Override
	public String getColorAsRgb(String colorId) {
		return switch (colorId) {
			case "lightgray" -> "rgb(192,192,192)";
			case "yellow" -> "rgb(255,255,0)";
			case "orange" -> "rgb(255,192,0)";
			case "brown" -> "rgb(192,128,0)";
			case "red" -> "rgb(255,0,0)";
			case "orchid" -> "rgb(255,0,255)";
			case "purple" -> "rgb(192,0,255)";
			case "lightblue" -> "rgb(128,128,255)";
			case "cobaltblue" -> "rgb(64,64,192)";
			case "darkblue" -> "rgb(0,0,128)";
			case "lightgreen" -> "rgb(128,255,128)";
			case "seagreen" -> "rgb(0,192,0)";
			case "gold" -> "rgb(255,216,42)";
			case "silver" -> "rgb(192,192,192)";
			case "bronze" -> "rgb(255,160,128)";
			default -> "rgb(255,255,255)";
		};
	}

	@Override
	public List<BadgeTemplate> getTemplates(BadgeTemplate.Scope scope) {
		return templateDAO.getTemplates(scope);
	}

	@Override
	public List<TemplateWithSize> getTemplatesWithSizes(BadgeTemplate.Scope scope) {
		return getTemplates(scope).stream().map((template) -> new TemplateWithSize(template, sizeForTemplate(template))).toList();
	}

	private Size sizeForTemplate(BadgeTemplate template) {
		VFSLeaf imageLeaf = getTemplateVfsLeaf(template.getImage());
		return sizeForVfsLeaf(imageLeaf);
	}

	private Size sizeForVfsLeaf(VFSLeaf imageLeaf) {
		Size imageSize = null;

		if (imageLeaf != null && imageLeaf.exists()) {
			String suffix = FileUtils.getFileSuffix(imageLeaf.getName());
			if (!OpenBadgesFactory.isSvgFileSuffix(suffix)) {
				imageSize = imageService.getSize(imageLeaf, suffix);
			}
			if (imageSize == null) {
				if (OpenBadgesFactory.isSvgFileSuffix(suffix)) {
					suffix = "svg+xml";
				}
				imageSize = movieService.getSize(imageLeaf, suffix);
			}
		}

		if (imageSize == null) {
			imageSize = new Size(0, 0, false);
		}

		return imageSize;
	}

	@Override
	public Set<String> getTemplateSvgSubstitutionVariables(String image) {
		HashSet<String> result = new HashSet<>();

		if (!OpenBadgesFactory.isSvgFileName(image)) {
			return result;
		}

		VFSLeaf templateLeaf = getTemplateVfsLeaf(image);
		if (templateLeaf instanceof LocalFileImpl localFile) {
			try {
				String svg = new String(Files.readAllBytes(localFile.getBasefile().toPath()), "UTF8");
				if (svg.contains(VAR_BACKGROUND)) {
					result.add(VAR_BACKGROUND);
				}
				if (svg.contains(VAR_TITLE)) {
					result.add(VAR_TITLE);
				}
			} catch (IOException e) {
				log.error(e);
			}
		}

		return result;
	}

	@Override
	public String getTemplateSvgImageWithSubstitutions(Long templateKey, String backgroundColorId, String title) {
		BadgeTemplate template = getTemplate(templateKey);
		return getTemplateSvgImageWithSubstitutions(template.getImage(), backgroundColorId, title);
	}

	@Override
	public String getTemplateSvgImageWithSubstitutions(String templateImage, String backgroundColorId, String title) {
		try {
			VFSLeaf templateLeaf = getTemplateVfsLeaf(templateImage);
			if (templateLeaf instanceof LocalFileImpl localFile) {
				String svg;
				svg = new String(Files.readAllBytes(localFile.getBasefile().toPath()), "UTF8");
				if (title != null) {
					svg = svg.replace(VAR_TITLE, title);
				}
				if (backgroundColorId != null) {
					svg = svg.replace(VAR_BACKGROUND, getColorAsRgb(backgroundColorId));
				}
				return svg;
			}
		} catch (Exception e) {
			log.error(e);
			return null;
		}

		return null;
	}

	public SelectionValues getTemplateTranslationLanguages(Locale displayLocale) {
		SelectionValues result = new SelectionValues();
		Collection<String> enabledKeys = i18nModule.getEnabledLanguageKeys();
		for (String enabledKey : enabledKeys) {
			Locale locale = i18nManager.getLocaleOrNull(enabledKey);
			if (locale != null) {
				if (displayLocale != null) {
					String displayName = locale.getDisplayName(displayLocale);
					result.add(SelectionValues.entry(enabledKey, displayName));
				} else {
					result.add(SelectionValues.entry(enabledKey, enabledKey));
				}
			}
		}
		return result;
	}

	@Override
	public void updateTemplate(BadgeTemplate badgeTemplate) {
		templateDAO.updateTemplate(badgeTemplate);
	}

	@Override
	public void updateTemplate(BadgeTemplate badgeTemplate, File templateFile, String targetFileName, Identity savedBy) {
		String templateFileName = copyTemplate(templateFile, targetFileName, savedBy);
		if (templateFileName != null) {
			deleteTemplateImages(badgeTemplate.getImage());
			badgeTemplate.setImage(templateFileName);
			createPreviewImage(badgeTemplate, savedBy);
			templateDAO.updateTemplate(badgeTemplate);
		}
	}

	@Override
	public void deleteTemplate(BadgeTemplate badgeTemplate) {
		try {
			deleteTranslations(badgeTemplate);
		} catch (Exception e) {
			log.error(e);
		}

		String image = badgeTemplate.getImage();
		deleteTemplateImages(image);

		badgeCategoryDAO.delete(badgeTemplate);
		templateDAO.deleteTemplate(badgeTemplate);
	}

	private void deleteTemplateImages(String image) {
		if (getBadgeTemplatesRootContainer().resolve(image) instanceof VFSLeaf imageLeaf) {
			imageLeaf.delete();
		}

		String previewImage = getTemplateSvgPreviewImage(image);
		if (getBadgeTemplatesRootContainer().resolve(previewImage) instanceof VFSLeaf previewLeaf) {
			previewLeaf.delete();
		}
	}

	private void deleteTranslations(BadgeTemplate badgeTemplate) {
		SelectionValues languageKV = getTemplateTranslationLanguages(null);
		Map<Locale, Locale> overlayLocales = i18nModule.getOverlayLocales();

		for (String languageKey : languageKV.keys()) {
			Locale locale = i18nManager.getLocaleOrDefault(languageKey);
			Locale overlayLocale = overlayLocales.get(locale);
			String bundleName = OpenBadgesUIFactory.getBundleName();

			String nameKey = OpenBadgesUIFactory.getTemplateNameI18nKey(badgeTemplate.getIdentifier());
			I18nItem nameItem = i18nManager.getI18nItem(bundleName, nameKey, overlayLocale);
			i18nManager.saveOrUpdateI18nItem(nameItem, "");

			String descriptionKey = OpenBadgesUIFactory.getTemplateDescriptionI18nKey(badgeTemplate.getIdentifier());
			I18nItem descriptionItem = i18nManager.getI18nItem(bundleName, descriptionKey, overlayLocale);
			i18nManager.saveOrUpdateI18nItem(descriptionItem, "");
		}
	}

	private String copyTemplate(File sourceFile, String targetFileName, Identity savedBy) {
		VFSContainer templateContainer = getBadgeTemplatesRootContainer();
		return copyFile(templateContainer, sourceFile, targetFileName, savedBy);
	}

	private String copyTemplate(InputStream inputStream, String targetFileName, Identity savedBy) {
		VFSContainer templateContainer = getBadgeTemplatesRootContainer();
		return copy(templateContainer, inputStream, targetFileName, savedBy);
	}

	private File getBadgeTemplatesRoot() {
		Path path = Paths.get(folderModule.getCanonicalRoot(), BADGES_VFS_FOLDER, TEMPLATES_VFS_FOLDER);
		File root = path.toFile();
		if (!root.exists()) {
			root.mkdirs();
		}
		return root;
	}

	private VFSContainer getBadgeTemplatesRootContainer() {
		return VFSManager.olatRootContainer(File.separator + BADGES_VFS_FOLDER + File.separator + TEMPLATES_VFS_FOLDER, null);
	}

	//
	// Class
	//

	@Override
	public void createBadgeClass(BadgeClassImpl badgeClass) {
		badgeClassDAO.createBadgeClass(badgeClass);
	}

	@Override
	public String createBadgeClassImageFromSvgTemplate(Long templateKey, String backgroundColorId, String title,
													   Identity savedBy) {
		BadgeTemplate template = getTemplate(templateKey);
		String svg = getTemplateSvgImageWithSubstitutions(template.getImage(), backgroundColorId, title);
		VFSContainer classesContainer = getBadgeClassesRootContainer();
		String targetFileName = VFSManager.rename(classesContainer, template.getImage());
		VFSLeaf targetLeaf = classesContainer.createChildLeaf(targetFileName);
		try (InputStream inputStream = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8))) {
			if (VFSManager.copyContent(inputStream, targetLeaf, savedBy)) {
				return targetFileName;
			}
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	@Override
	public String createBadgeClassImage(File tempBadgeFileImage, String targetBadgeImageFileName, Identity savedBy) {
		return copyBadgeClassFile(tempBadgeFileImage, targetBadgeImageFileName, savedBy);
	}

	@Override
	public VFSLeaf getBadgeClassVfsLeaf(String badgeClassFile) {
		VFSContainer badgeClassesContainer = getBadgeClassesRootContainer();
		if (badgeClassesContainer.resolve(badgeClassFile) instanceof VFSLeaf classFileLeaf) {
			return classFileLeaf;
		} else {
			log.error("Could not resolve file " + badgeClassFile + " in " + badgeClassesContainer.getRelPath());
		}
		return null;
	}

	@Override
	public List<BadgeClass> getBadgeClasses(RepositoryEntry entry) {
		return badgeClassDAO.getBadgeClasses(entry);
	}

	@Override
	public List<BadgeClassDAO.BadgeClassWithUseCount> getBadgeClassesWithUseCounts(RepositoryEntry entry) {
		return badgeClassDAO.getBadgeClassesWithUseCounts(entry);
	}

	@Override
	public Long getNumberOfBadgeClasses(RepositoryEntryRef entry) {
		return badgeClassDAO.getNumberOfBadgeClasses(entry);
	}

	@Override
	public BadgeClass getBadgeClass(String uuid) {
		return badgeClassDAO.getBadgeClass(uuid);
	}

	@Override
	public BadgeClass getBadgeClass(Long key) {
		return badgeClassDAO.getBadgeClass(key);
	}

	@Override
	public List<BadgeClassWithSizeAndCount> getBadgeClassesWithSizesAndCounts(RepositoryEntry entry) {
		return getBadgeClassesWithUseCounts(entry).stream().map((obj) -> new BadgeClassWithSizeAndCount(obj.getBadgeClass(), sizeForBadgeClass(obj.getBadgeClass()), obj.getUseCount())).toList();
	}

	private Size sizeForBadgeClass(BadgeClass badgeClass) {
		VFSLeaf imageLeaf = getBadgeClassVfsLeaf(badgeClass.getImage());
		return sizeForVfsLeaf(imageLeaf);
	}

	@Override
	public BadgeClass updateBadgeClass(BadgeClass badgeClass) {
		return badgeClassDAO.updateBadgeClass(badgeClass);
	}

	@Override
	public void removeCourseEntryFromCourseBadgeClasses(RepositoryEntry entry) {
		List<BadgeClass> courseBadgeClasses = badgeClassDAO.getBadgeClasses(entry, false);
		for (BadgeClass courseBadgeClass : courseBadgeClasses) {
			courseBadgeClass.setEntry(null);
			updateBadgeClass(courseBadgeClass);
		}
	}

	@Override
	public void deleteBadgeClassAndAssertions(BadgeClass badgeClass) {
		List<BadgeAssertion> badgeAssertions = getBadgeAssertions(badgeClass);
		for (BadgeAssertion badgeAssertion : badgeAssertions) {
			deleteBadgeAssertion(badgeAssertion);
		}

		if (getBadgeClassesRootContainer().resolve(badgeClass.getImage()) instanceof VFSLeaf badgeClassImageLeaf) {
			badgeClassImageLeaf.delete();
		}

		badgeCategoryDAO.delete(badgeClass);
		badgeClassDAO.deleteBadgeClass(badgeClass);
	}

	private File getBadgeClassesRoot() {
		Path path = Paths.get(folderModule.getCanonicalRoot(), BADGES_VFS_FOLDER, CLASSES_VFS_FOLDER);
		File root = path.toFile();
		if (!root.exists()) {
			root.mkdirs();
		}
		return root;
	}

	private VFSContainer getBadgeClassesRootContainer() {
		return VFSManager.olatRootContainer(File.separator + BADGES_VFS_FOLDER + File.separator + CLASSES_VFS_FOLDER, null);
	}

	private String copyBadgeClassFile(File sourceFile, String targetFileName, Identity savedBy) {
		VFSContainer classesContainer = getBadgeClassesRootContainer();
		return copyFile(classesContainer, sourceFile, targetFileName, savedBy);
	}

	private String copyFile(VFSContainer targetContainer, File sourceFile, String targetFileName, Identity savedBy) {
		String finalTargetFileName = VFSManager.rename(targetContainer, targetFileName);
		if (finalTargetFileName != null) {
			VFSLeaf targetLeaf = targetContainer.createChildLeaf(finalTargetFileName);
			try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
				if (VFSManager.copyContent(inputStream, targetLeaf, savedBy)) {
					return finalTargetFileName;
				}
			} catch (IOException e) {
				log.error("", e);
			}
		} else {
			log.error("Could not set a target file name for {}.", targetFileName);
		}
		return null;
	}

	private String copy(VFSContainer targetContainer, InputStream inputStream, String targetFileName, Identity savedBy) {
		String finalTargetFileName = VFSManager.rename(targetContainer, targetFileName);
		if (finalTargetFileName != null) {
			VFSLeaf targetLeaf = targetContainer.createChildLeaf(finalTargetFileName);
			if (VFSManager.copyContent(inputStream, targetLeaf, savedBy, vfsRepositoryService)) {
				return finalTargetFileName;
			}
		} else {
			log.error("Could not set a target file name for {}.", targetFileName);
		}
		return null;
	}

	//
	// Assertion
	//

	@Override
	public BadgeAssertion createBadgeAssertion(String uuid, BadgeClass badgeClass, Date issuedOn,
									 Identity recipient, Identity awardedBy) {
		if (badgeAssertionExists(recipient, badgeClass)) {
			log.debug("Badge assertion exists for user " + recipient.toString() + " and badge " + badgeClass.getName());
			return null;
		}

		String verification = "{\"type\":\"hosted\"}";
		String recipientObject = createRecipientObject(recipient, badgeClass.getSalt());
		if (recipientObject == null) {
			return null;
		}
		BadgeAssertion badgeAssertion = badgeAssertionDAO.createBadgeAssertion(uuid, recipientObject, badgeClass,
				verification, issuedOn, recipient, awardedBy);

		String bakedImage = createBakedBadgeImage(badgeAssertion, awardedBy);
		badgeAssertion.setBakedImage(bakedImage);

		badgeAssertion = badgeAssertionDAO.updateBadgeAssertion(badgeAssertion);

		if (log.isDebugEnabled()) {
			log.debug("Created badge assertion {}", badgeAssertion.getUuid());
		}

		if (badgeAssertion.getBakedImage() == null) {
			log.error("Badge assertion {} does not have an image.", badgeAssertion.getUuid());
			return badgeAssertion;
		}

		File bakedImageFile;
		if (getBadgeAssertionVfsLeaf(badgeAssertion.getBakedImage()) instanceof LocalFileImpl bakedFileImpl) {
			bakedImageFile = bakedFileImpl.getBasefile();
		} else {
			log.error("Invalid baked badge image for badge assertion {}.", badgeAssertion.getUuid());
			return badgeAssertion;
		}

		MailerResult mailerResult = sendBadgeEmail(badgeAssertion, bakedImageFile);
		if (!mailerResult.isSuccessful()) {
			log.error("Sending Badge \"{}\" to \"{}\" failed", badgeAssertion.getBadgeClass().getName(),
					badgeAssertion.getRecipient().getKey());
		}

		if (badgeClass.getStatus() != BadgeClass.BadgeClassStatus.active) {
			badgeClass.setStatus(BadgeClass.BadgeClassStatus.active);
			updateBadgeClass(badgeClass);
		}

		return badgeAssertion;
	}

	private MailerResult sendBadgeEmail(BadgeAssertion badgeAssertion, File bakedImageFile) {
		MailBundle mailBundle = new MailBundle();
		Identity recipient = badgeAssertion.getRecipient();
		mailBundle.setToId(recipient);
		mailBundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));

		String recipientLanguage = recipient.getUser().getPreferences().getLanguage();
		Locale recipientLocale = i18nManager.getLocaleOrDefault(recipientLanguage);
		Translator translator = Util.createPackageTranslator(OpenBadgesUIFactory.class, recipientLocale);
		String[] args = createMailArgs(badgeAssertion);
		String subject = translator.translate("email.subject", args);
		String body = translator.translate("email.body", args);

		mailBundle.setContent(subject, body, bakedImageFile);

		return mailManager.sendMessage(mailBundle);
	}

	private String[] createMailArgs(BadgeAssertion badgeAssertion) {
		return new String[] {
				badgeAssertion.getBadgeClass().getName(), // badge name
				Settings.getServerContextPathURI() + "/url/HomeSite/" + badgeAssertion.getRecipient().getKey() +
						"/badges/0/key/" + badgeAssertion.getKey() // badge URL
		};
	}

	@Override
	public void issueBadgesAutomatically(Identity recipient, Identity awardedBy, RepositoryEntry courseEntry,
										 Boolean passed, Float score) {
		Date issuedOn = new Date();
		for (BadgeClass badgeClass : getBadgeClasses(courseEntry)) {
			BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
			if (!badgeCriteria.isAwardAutomatically()) {
				continue;
			}
			if (badgeCriteria.allConditionsMet(passed != null ? passed : false, score != null ? score : 0)) {
				String uuid = OpenBadgesUIFactory.createIdentifier();
				createBadgeAssertion(uuid, badgeClass, issuedOn, recipient, awardedBy);
			}
		}
	}

	@Override
	public void issueBadgesAutomatically(RepositoryEntry courseEntry, Identity awardedBy) {
		if (courseEntry.getEntryStatus() != RepositoryEntryStatusEnum.published) {
			return;
		}
		List<IdentityWithAssessmentEntry> identitiesWithAssessmentEntries = getIdentitiesWithAssessmentEntries(courseEntry, awardedBy);
		List<BadgeClass> badgeClasses = getBadgeClasses(courseEntry);
		for (BadgeClass badgeClass : badgeClasses) {
			List<Identity> automaticRecipients = getAutomaticRecipients(badgeClass, identitiesWithAssessmentEntries);
			issueBadge(badgeClass, automaticRecipients, awardedBy);
		}
	}

	private record IdentityWithAssessmentEntry(Identity identity, AssessmentEntry assessmentEntry) {}

	private List<IdentityWithAssessmentEntry> getIdentitiesWithAssessmentEntries(RepositoryEntry courseEntry, Identity identity) {
		List<IdentityWithAssessmentEntry> result = new ArrayList<>();

		ICourse course = CourseFactory.loadCourse(courseEntry.getOlatResource().getResourceableId());
		String rootIdent = course.getRunStructure().getRootNode().getIdent();
		AssessmentToolSecurityCallback secCallback = new AssessmentToolSecurityCallback(true, false,
				false, true, true, true,
				null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(
				courseEntry, rootIdent, null, secCallback);

		Map<Long, AssessmentEntry> identityKeyToAssessmentEntry = new HashMap<>();
		assessmentToolManager.getAssessmentEntries(identity, params, null)
				.stream()
				.filter(entry -> entry.getIdentity() != null)
				.forEach(entry -> identityKeyToAssessmentEntry.put(entry.getIdentity().getKey(), entry));
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(identity, params);

		for (Identity assessedIdentity : assessedIdentities) {
			AssessmentEntry assessmentEntry = identityKeyToAssessmentEntry.get(assessedIdentity.getKey());
			if (assessmentEntry == null) {
				log.info("Could not find assessment entry for {}", assessedIdentity.getKey());
				continue;
			}
			result.add(new IdentityWithAssessmentEntry(assessedIdentity, assessmentEntry));
		}
		return result;
	}

	private List<Identity> getAutomaticRecipients(BadgeClass badgeClass, List<IdentityWithAssessmentEntry> identitiesWithAssessmentEntries) {
		BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());

		List<Identity> automaticRecipients = new ArrayList<>();

		for (IdentityWithAssessmentEntry identityWithAssessmentEntry : identitiesWithAssessmentEntries) {
			AssessmentEntry assessmentEntry = identityWithAssessmentEntry.assessmentEntry();
			Identity assessedIdentity = identityWithAssessmentEntry.identity();
			boolean passed = assessmentEntry.getPassed() != null ? assessmentEntry.getPassed() : false;
			double score = assessmentEntry.getScore() != null ? assessmentEntry.getScore().doubleValue() : 0;
			log.debug("Badge '{}', participant '{}': passed = {}, score = {}",
					badgeClass.getName(), assessedIdentity.getName(), passed, score);
			if (badgeCriteria.isAwardAutomatically() && badgeCriteria.allConditionsMet(passed, score)) {
				automaticRecipients.add(assessedIdentity);
			}
		}
		return automaticRecipients;
	}

	@Override
	public void issueBadge(BadgeClass badgeClass, List<Identity> recipients, Identity awardedBy) {
		if (recipients == null || recipients.isEmpty()) {
			return;
		}
		Date issuedOn = new Date();
		for (Identity recipient : recipients) {
			String uuid = OpenBadgesUIFactory.createIdentifier();
			createBadgeAssertion(uuid, badgeClass, issuedOn, recipient, awardedBy);
		}
	}

	private String createRecipientObject(Identity recipient, String salt) {
		if (recipient.getUser() == null || !StringHelper.containsNonWhitespace(recipient.getUser().getEmail())) {
			log.error("recipient has no email address");
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "email");
		jsonObject.put("hashed", true);
		jsonObject.put("salt", salt);
		jsonObject.put("identity", "sha256$" + DigestUtils.sha256Hex(recipient.getUser().getEmail() + salt));
		return jsonObject.toString();
	}

	private String createBakedBadgeImage(BadgeAssertion badgeAssertion, Identity savedBy) {
		String badgeClassImage = badgeAssertion.getBadgeClass().getImage();
		if (OpenBadgesFactory.isSvgFileName(badgeClassImage)) {
			try {
				return createBakedSvgBadgeImage(badgeAssertion, savedBy);
			} catch (Exception e) {
				log.error("Error creating baked SVG badge image", e);
			}
		} else if (OpenBadgesFactory.isPngFileName(badgeClassImage)) {
			return createBakedPngBadgeImage(badgeAssertion);
		}
		return null;
	}

	private String createBakedSvgBadgeImage(BadgeAssertion badgeAssertion, Identity savedBy) {
		VFSLeaf badgeClassImage = getBadgeClassVfsLeaf(badgeAssertion.getBadgeClass().getImage());
		if (badgeClassImage instanceof LocalFileImpl localFile) {
			try {
				String svg = new String(Files.readAllBytes(localFile.getBasefile().toPath()), "UTF8");
				String jsonString = createBakedJsonString(badgeAssertion);
				String verifyUrl = OpenBadgesFactory.createAssertionVerifyUrl(badgeAssertion.getUuid());
				svg = mergeAssertionJson(svg, jsonString, verifyUrl);
				InputStream inputStream = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8));
				VFSContainer assertionsContainer = getBadgeAssertionsRootContainer();
				String bakedImage = badgeAssertion.getUuid() + ".svg";
				VFSLeaf targetLeaf = assertionsContainer.createChildLeaf(bakedImage);
				VFSManager.copyContent(inputStream, targetLeaf, savedBy);
				return bakedImage;
			} catch (IOException e) {
				log.error(e);
				return null;
			}
		}
		return null;
	}

	public String createBakedJsonString(BadgeAssertion badgeAssertion) {
		Assertion assertion = new Assertion(badgeAssertion);
		JSONObject jsonObject = assertion.asJsonObject();
		return jsonObject.toString(2);
	}

	public String mergeAssertionJson(String svg, String jsonString, String verifyUrl) {
		Matcher matcher = svgOpeningTagPattern.matcher(svg);
		if (matcher.find()) {
			String upToSvgOpeningTag = svg.substring(0, matcher.start());
			String svgTagKeyword = svg.substring(matcher.start(), matcher.start() + 4);
			String restOfSvgOpeningTag = svg.substring(matcher.start() + 4, matcher.end());
			String contentAndSvgClosingTag = svg.substring(matcher.end());
			return upToSvgOpeningTag + svgTagKeyword + " " + OPEN_BADGES_ASSERTION_XML_NAMESPACE + restOfSvgOpeningTag + "\n" +
					"<openbadges:assertion verify=\"" + verifyUrl + "\">\n" +
					"<![CDATA[\n" +
					jsonString +
					"]]>\n" +
					"</openbadges:assertion>" +
					contentAndSvgClosingTag;
		}
		return "";
	}

	private String createBakedPngBadgeImage(BadgeAssertion badgeAssertion) {
		return null;
	}

	public boolean badgeAssertionExists(Identity recipient, BadgeClass badgeClass) {
		return badgeAssertionDAO.getNumberOfBadgeAssertions(recipient, badgeClass) > 0;
	}

	@Override
	public List<BadgeAssertion> getBadgeAssertions(BadgeClass badgeClass) {
		return badgeAssertionDAO.getBadgeAssertions(badgeClass);
	}

	@Override
	public List<BadgeAssertion> getBadgeAssertions(Identity recipient, RepositoryEntry courseEntry,
												   boolean nullEntryMeansAll) {
		if (log.isDebugEnabled()) {
			log.debug("Read badge assertions for recipient " + recipient + " and course " + courseEntry);
		}
		return badgeAssertionDAO.getBadgeAssertions(recipient, courseEntry, nullEntryMeansAll);
	}

	@Override
	public List<BadgeAssertionWithSize> getBadgeAssertionsWithSizes(Identity identity, RepositoryEntry courseEntry,
																	boolean nullEntryMeansAll) {
		return getBadgeAssertions(identity, courseEntry, nullEntryMeansAll).stream().map((badgeAssertion) -> new BadgeAssertionWithSize(badgeAssertion, sizeForBadgeAssertion(badgeAssertion))).toList();
	}

	private Size sizeForBadgeAssertion(BadgeAssertion badgeAssertion) {
		if (badgeAssertion.getBakedImage() == null) {
			return new Size(0, 0, false);
		}
		VFSLeaf bakedImageLeaf = getBadgeAssertionVfsLeaf(badgeAssertion.getBakedImage());
		return sizeForVfsLeaf(bakedImageLeaf);
	}

	@Override
	public VFSLeaf getBadgeAssertionVfsLeaf(String badgeAssertionFile) {
		VFSContainer badgeAssertionsContainer = getBadgeAssertionsRootContainer();
		if (badgeAssertionsContainer.resolve(badgeAssertionFile) instanceof VFSLeaf assertionFileLeaf) {
			return assertionFileLeaf;
		} else {
			log.error("Could not resolve file " + badgeAssertionFile + " in " + badgeAssertionsContainer.getRelPath());
		}
		return null;
	}


	@Override
	public void updateBadgeAssertion(BadgeAssertion badgeAssertion, Identity awardedBy) {
		String recipientObject = createRecipientObject(badgeAssertion.getRecipient(), badgeAssertion.getBadgeClass().getSalt());
		if (recipientObject == null) {
			return;
		}
		badgeAssertion.setRecipientObject(recipientObject);
		badgeAssertionDAO.updateBadgeAssertion(badgeAssertion);

		deleteBadgeAssertionImage(badgeAssertion.getBakedImage());
		String bakedImage = createBakedBadgeImage(badgeAssertion, awardedBy);
		badgeAssertion.setBakedImage(bakedImage);
		badgeAssertionDAO.updateBadgeAssertion(badgeAssertion);
	}

	@Override
	public void revokeBadgeAssertion(Long key) {
		badgeAssertionDAO.revokeBadgeAssertion(key);
	}

	@Override
	public void deleteBadgeAssertion(BadgeAssertion badgeAssertion) {
		deleteBadgeAssertionImage(badgeAssertion.getBakedImage());
		badgeAssertionDAO.deleteBadgeAssertion(badgeAssertion);
	}

	private void deleteBadgeAssertionImage(String bakedImage) {
		if (getBadgeAssertionsRootContainer().resolve(bakedImage) instanceof VFSLeaf imageLeaf) {
			imageLeaf.delete();
		}
	}

	@Override
	public BadgeAssertion getBadgeAssertion(String uuid) {
		return badgeAssertionDAO.getAssertion(uuid);
	}

	public void bakeBadge(FileType fileType, String templateName) {
		try (InputStream inputStream = OpenBadgesManagerImpl.class.getResourceAsStream("_content/Computergrafik_Basis_Badge.png")) {
			NamedNodeMap attributes = extractAssertionJsonStringFromPng(inputStream);
			if (attributes == null) {
				log.error("Could not found assertion inside PNG");
			}
			OpenBadgesBakeContext bakeContext = new OpenBadgesBakeContext(attributes);
			Assertion assertion = bakeContext.getTextAsAssertion();
			Badge badge = assertion.getBadge();
			if (fileType == FileType.png) {
				VFSLeaf pngImage = badge.storeImageAsPng(unbakedBadgesStorage.getContainer(""));
				if (pngImage != null) {
					System.err.println("Wrote file " + ((LocalFileImpl) pngImage).getBasefile().getAbsolutePath());
				} else {
					System.err.println("go debug");
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public BadgeAssertion getBadgeAssertion(Identity recipient, BadgeClass badgeClass) {
		List<BadgeAssertion> badgeAssertions = badgeAssertionDAO.getBadgeAssertions(recipient, badgeClass);
		if (badgeAssertions.isEmpty()) {
			return null;
		}
		return badgeAssertions.get(0);
	}

	private File getBadgeAssertionsRoot() {
		Path path = Paths.get(folderModule.getCanonicalRoot(), BADGES_VFS_FOLDER, ASSERTIONS_VFS_FOLDER);
		File root = path.toFile();
		if (!root.exists()) {
			root.mkdirs();
		}
		return root;
	}

	private VFSContainer getBadgeAssertionsRootContainer() {
		return VFSManager.olatRootContainer(File.separator + BADGES_VFS_FOLDER + File.separator + ASSERTIONS_VFS_FOLDER, null);
	}

	private static NamedNodeMap extractAssertionJsonStringFromPng(InputStream inputStream) {
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(imageInputStream);
			if (!imageReaderIterator.hasNext()) {
				throw new RuntimeException("No PNG reader found");
			}
			ImageReader imageReader = imageReaderIterator.next();
			imageReader.setInput(imageInputStream);

			IIOImage iioImage = imageReader.readAll(0, null);

			IIOMetadata iioMetadata = iioImage.getMetadata();
			for (String formatName : iioMetadata.getMetadataFormatNames()) {
				System.err.println("format name: " + formatName);
				Node node = iioMetadata.getAsTree(formatName);
				NamedNodeMap attributes = findTextAttributes(node, "");
				if (attributes != null) {
					return attributes;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	private static NamedNodeMap findTextAttributes(Node node, String space) {
		System.err.println(space + node.getNodeName() + " " + node.getAttributes().getLength());
		if (node.getNodeName().equals("iTXtEntry")) {
			return node.getAttributes();
		}
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node child = node.getChildNodes().item(i);
			NamedNodeMap namedNodeMap = findTextAttributes(child, space + " ");
			if (namedNodeMap != null) {
				return namedNodeMap;
			}
		}
		return null;
	}

	//
	// Category
	//
	@Override
	public List<TagInfo> getCategories(BadgeTemplate badgeTemplate, BadgeClass badgeClass) {
		return badgeCategoryDAO.readBadgeCategoryTags(badgeTemplate, badgeClass);
	}

	@Override
	public List<TagInfo> readBadgeCategoryTags() {
		return badgeCategoryDAO.readBadgeCategoryTags();
	}

	@Override
	public void updateCategories(BadgeTemplate badgeTemplate, BadgeClass badgeClass, List<String> displayNames) {
		List<Tag> requiredTags = tagService.getOrCreateTags(displayNames);
		List<BadgeCategory> badgeCategories = badgeCategoryDAO.readBadgeCategories(badgeTemplate, badgeClass);
		List<Tag> currentTags = badgeCategories.stream().map(BadgeCategory::getTag).toList();

		for (Tag requiredTag : requiredTags) {
			if (!currentTags.contains(requiredTag)) {
				badgeCategoryDAO.create(requiredTag, badgeTemplate, badgeClass);
			}
		}

		for (BadgeCategory badgeCategory : badgeCategories) {
			if (!requiredTags.contains(badgeCategory.getTag())) {
				badgeCategoryDAO.delete(badgeCategory);
			}
		}
	}

	//
	// Entry Configuration
	//
	@Override
	public BadgeEntryConfiguration getConfiguration(RepositoryEntry entry) {
		BadgeEntryConfiguration configuration = badgeEntryConfigurationDAO.getConfiguration(entry);
		if (configuration == null) {
			RepositoryEntry reloadedEntry = repositoryEntryDao.loadForUpdate(entry);
			configuration = badgeEntryConfigurationDAO.getConfiguration(entry);
			if (configuration == null) {
				configuration = badgeEntryConfigurationDAO.createConfiguration(reloadedEntry);
			}
			dbInstance.commit();
		}
		return configuration;
	}

	@Override
	public BadgeEntryConfiguration updateConfiguration(BadgeEntryConfiguration configuration) {
		return badgeEntryConfigurationDAO.updateConfiguration(configuration);
	}

	@Override
	public void deleteConfiguration(RepositoryEntryRef entry) {
		badgeEntryConfigurationDAO.delete(entry);
	}

	@Override
	public boolean isEnabled() {
		return openBadgesModule.isEnabled();
	}
}
