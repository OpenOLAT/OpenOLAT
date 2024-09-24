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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
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
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeCategory;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeClasses;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.BadgeOrganization;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.LinkedInUrl;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.OpenBadgesModule;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Constants;
import org.olat.modules.openbadges.v2.Profile;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.manager.RepositoryEntryDAO;
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
@Service("openBadgesManager")
public class OpenBadgesManagerImpl implements OpenBadgesManager, InitializingBean {
	private static final Logger log = Tracing.createLoggerFor(OpenBadgesManagerImpl.class);
	private static final String BADGES_VFS_FOLDER = "badges";
	private static final String TEMPLATES_VFS_FOLDER = "templates";
	private static final String CLASSES_VFS_FOLDER = "classes";
	private static final String ASSERTIONS_VFS_FOLDER = "assertions";
	private static final String TEMPLATE_IMAGE_PREVIEW_PREFIX = "._oo_preview_";
	private static final Pattern svgOpeningTagPattern = Pattern.compile("<svg[^>]*>");
	private static final String OPEN_BADGES_ASSERTION_XML_NAMESPACE = "xmlns:openbadges=\"http://openbadges.org\"";
	private static final int MAX_BADGE_CLASS_IMAGE_WIDTH = 512;
	private static final int MAX_BADGE_CLASS_IMAGE_HEIGHT = 512;

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
	@Autowired
	private BadgeOrganizationDAO badgeOrganizationDAO;

	@Autowired
	private CourseAssessmentService courseAssessmentService;

	@Override
	public void afterPropertiesSet() {
		createBadgeAssertionsRoot();
		createBadgeTemplatesRoot();
		createBadgeClassesRoot();

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
			String jsonString = FileUtils.load(jsonInputStream, "utf-8");
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

		SelectionValues languageKV = getAvailableLanguages(null);
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
		return getTemplates(scope).stream().map(template -> new TemplateWithSize(template, sizeForTemplate(template))).toList();
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

	@Override
	public SelectionValues getAvailableLanguages(Locale displayLocale) {
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
			imageLeaf.deleteSilently();
		}

		String previewImage = getTemplateSvgPreviewImage(image);
		if (getBadgeTemplatesRootContainer().resolve(previewImage) instanceof VFSLeaf previewLeaf) {
			previewLeaf.deleteSilently();
		}
	}

	private void deleteTranslations(BadgeTemplate badgeTemplate) {
		SelectionValues languageKV = getAvailableLanguages(null);
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

	private void createBadgeTemplatesRoot() {
		Path path = Paths.get(folderModule.getCanonicalRoot(), BADGES_VFS_FOLDER, TEMPLATES_VFS_FOLDER);
		File root = path.toFile();
		if (!root.exists()) {
			root.mkdirs();
		}
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
	public void copyBadgeClass(Long sourceClassKey, Translator translator, Identity author) {
		BadgeClass sourceClass = badgeClassDAO.getBadgeClass(sourceClassKey);

		BadgeClassImpl targetClass = new BadgeClassImpl();
		targetClass.setUuid(OpenBadgesFactory.createIdentifier());
		targetClass.setEntry(sourceClass.getEntry());

		copyBadgeClassFields(sourceClass, targetClass);

		targetClass.setName(sourceClass.getName() + " " + translator.translate("badge.copy.suffix"));

		badgeClassDAO.createBadgeClass(targetClass);

		VFSContainer classesContainer = getBadgeClassesRootContainer();
		if (classesContainer.resolve(sourceClass.getImage()) instanceof LocalFileImpl sourceLeaf) {
			copyFile(classesContainer, sourceLeaf.getBasefile(), targetClass.getImage(), author);
		}
	}

	@Override
	public File copyBadgeClassWithTemporaryImage(Long sourceClassKey, BadgeClass targetClass, Translator translator) {
		BadgeClass sourceClass = badgeClassDAO.getBadgeClass(sourceClassKey);

		copyBadgeClassFields(sourceClass, targetClass);

		targetClass.setName(sourceClass.getName() + " " + translator.translate("badge.copy.suffix"));

		// get the source image
		VFSLeaf sourceImageLeaf = getBadgeClassVfsLeaf(sourceClass.getImage());

		// copy the source image to a temporary target file and return the temporary file
		File targetFile = new File(WebappHelper.getTmpDir(), "copied_" + targetClass.getImage());
		try {
			FileUtils.copyToFile(sourceImageLeaf.getInputStream(), targetFile, "");
		} catch (IOException e) {
			log.error("", e);
		}
		return targetFile;
	}

	@Override
	public List<String> getBadgeClassNames(boolean excludeBadgeClass, BadgeClass badgeClass) {
		final String excludeName = excludeBadgeClass ? badgeClass.getName() : null;
		return badgeClassDAO.getBadgeClassNames(badgeClass.getEntry()).stream().filter(s -> {
			if (excludeName != null) {
				return !excludeName.equals(s);
			}
			return true;
		}).toList();
	}

	@Override
	public List<BadgeClassDAO.NameAndVersion> getBadgeClassNameVersionTuples(boolean excludeBadgeClass, BadgeClass badgeClass) {
		final BadgeClassDAO.NameAndVersion excludeTuple = excludeBadgeClass ?
				new BadgeClassDAO.NameAndVersion(badgeClass.getName(), badgeClass.getVersion()) : null;
		return badgeClassDAO.getBadgeClassNameVersionTuples(badgeClass.getEntry()).stream().filter(tuple -> {
			if (excludeTuple != null) {
				return !excludeTuple.equals(tuple);
			}
			return true;
		}).toList();
	}

	/**
	 * Copies fields from a 'sourceClass' to a 'targetClass'.
	 * The 'targetClass' must have the fields 'uuid' and 'entry' set already. All other fields in
	 * 'targetClass' are overwritten with values from 'sourceClass'.
	 *
	 * @param sourceClass A badge class object to be used as the source.
	 * @param targetClass A badge class object to be used as the target.
	 */
	private void copyBadgeClassFields(BadgeClass sourceClass, BadgeClass targetClass) {
		targetClass.setStatus(BadgeClass.BadgeClassStatus.preparation);
		targetClass.setSalt(OpenBadgesFactory.createSalt(targetClass));
		targetClass.setVersion(sourceClass.getVersion());
		targetClass.setName(sourceClass.getName());
		targetClass.setDescription(sourceClass.getDescription());
		targetClass.setLanguage(sourceClass.getLanguage());
		targetClass.setValidityEnabled(sourceClass.isValidityEnabled());
		targetClass.setValidityTimelapse(sourceClass.getValidityTimelapse());
		targetClass.setValidityTimelapseUnit(sourceClass.getValidityTimelapseUnit());
		targetClass.setCriteria(sourceClass.getCriteria());
		targetClass.setIssuer(cloneIssuerString(sourceClass.getIssuer(), targetClass.getEntry()));
		targetClass.setImage(OpenBadgesFactory.createBadgeClassFileName(targetClass.getUuid(), sourceClass.getImage()));
	}

	private BadgeClass cloneBadgeClass(BadgeClass sourceClass, RepositoryEntry targetEntry, Identity author,
									   File sourceDirectory) {
		BadgeClassImpl targetClass = new BadgeClassImpl();
		targetClass.setEntry(targetEntry);
		targetClass.setUuid(OpenBadgesFactory.createIdentifier());

		copyBadgeClassFields(sourceClass, targetClass);

		badgeClassDAO.createBadgeClass(targetClass);

		VFSContainer classesContainer = getBadgeClassesRootContainer();

		if (sourceDirectory != null) {
			copyFile(sourceDirectory, sourceClass.getImage(), classesContainer, targetClass.getImage(), author);
		} else if (classesContainer.resolve(sourceClass.getImage()) instanceof LocalFileImpl sourceLeaf) {
			copyFile(classesContainer, sourceLeaf.getBasefile(), targetClass.getImage(), author);
		}

		return targetClass;
	}

	private String cloneIssuerString(String sourceIssuerString, RepositoryEntry targetEntry) {
		if (targetEntry == null) {
			return sourceIssuerString;
		}

		if (!StringHelper.containsNonWhitespace(sourceIssuerString)) {
			return sourceIssuerString;
		}

		String urlPart = "/url/RepositoryEntry/";
		int index = sourceIssuerString.indexOf(urlPart);
		if (index != -1) {
			Profile profile = new Profile(new JSONObject(sourceIssuerString));
			String clonedUrl = Settings.getServerContextPathURI() + urlPart + targetEntry.getKey();
			profile.setUrl(clonedUrl);
			return profile.asJsonObject(Constants.TYPE_VALUE_ISSUER).toString();
		}

		return sourceIssuerString;
	}

	private void copyFile(File sourceDirectory, String sourceFileName, VFSContainer targetContainer,
						  String targetFileName, Identity savedBy) {
		File sourceFile = new File(sourceDirectory, sourceFileName);
		if (!sourceFile.exists()) {
			log.error("Couldn't find source file: {}", sourceFileName);
			return;
		}

		copyFile(targetContainer, sourceFile, targetFileName, savedBy);
	}

	@Override
	public String createBadgeClassImageFromSvgTemplate(String uuid, Long templateKey, String backgroundColorId,
													   String title, Identity savedBy) {
		BadgeTemplate template = getTemplate(templateKey);
		String svg = getTemplateSvgImageWithSubstitutions(template.getImage(), backgroundColorId, title);
		VFSContainer classesContainer = getBadgeClassesRootContainer();
		String targetFileName = OpenBadgesFactory.createBadgeClassFileName(uuid, template.getImage());
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
	public String createBadgeClassImageFromPngTemplate(String uuid, Long templateKey) {
		BadgeTemplate template = getTemplate(templateKey);
		String templateImage = template.getImage();
		VFSLeaf templateLeaf = getTemplateVfsLeaf(templateImage);
		VFSContainer classesContainer = getBadgeClassesRootContainer();
		String targetFileName = OpenBadgesFactory.createBadgeClassFileName(uuid, template.getImage());
		VFSLeaf targetLeaf = classesContainer.createChildLeaf(targetFileName);
		imageService.scaleImage(templateLeaf, targetLeaf, MAX_BADGE_CLASS_IMAGE_WIDTH, MAX_BADGE_CLASS_IMAGE_HEIGHT, false);
		return targetFileName;
	}

	@Override
	public String createBadgeClassImage(String uuid, File tempBadgeFileImage, String targetBadgeImageFileName,
										Identity savedBy) {
		String targetFileName = OpenBadgesFactory.createBadgeClassFileName(uuid, targetBadgeImageFileName);
		return copyBadgeClassFile(tempBadgeFileImage, targetFileName, savedBy);
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
	public List<BadgeClass> getBadgeClassesInCoOwnedCourseSet(RepositoryEntry entry) {
		return badgeClassDAO.getBadgeClassesInCoOwnedCourseSet(entry);
	}

	@Override
	public List<BadgeClassDAO.BadgeClassWithUseCount> getBadgeClassesWithUseCounts(RepositoryEntry entry) {
		return badgeClassDAO.getBadgeClassesWithUseCounts(entry);
	}

	@Override
	public List<String> getBadgeClassNames(Collection<Long> badgeClassKeys) {
		return badgeClassDAO.getBadgeClassNames(badgeClassKeys);
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
	public List<BadgeClassWithSize> getBadgeClassesWithSizes(RepositoryEntry entry) {
		List<BadgeClass> badgeClasses = badgeClassDAO.getBadgeClasses(entry);
		return badgeClasses.stream().map(bc -> new BadgeClassWithSize(bc, sizeForBadgeClass(bc))).toList();
	}

	@Override
	public List<BadgeClassWithSizeAndCount> getBadgeClassesWithSizesAndCounts(RepositoryEntry entry) {
		List<BadgeClassDAO.BadgeClassWithUseCount> enhancedBadgeClasses = getBadgeClassesWithUseCounts(entry);
		if (updateBadgeClasses(enhancedBadgeClasses)) {
			enhancedBadgeClasses = getBadgeClassesWithUseCounts(entry);
		}
		return enhancedBadgeClasses.stream().map(obj -> new BadgeClassWithSizeAndCount(obj.getBadgeClass(),
				sizeForBadgeClass(obj.getBadgeClass()), obj.getUseCount(), obj.getRevokedCount(),
				obj.getResetCount())).toList();
	}

	private boolean updateBadgeClasses(List<BadgeClassDAO.BadgeClassWithUseCount> enhancedBadgeClasses) {
		boolean updated = false;
		for (BadgeClassDAO.BadgeClassWithUseCount enhancedBadgeClass : enhancedBadgeClasses) {
			if (updateBadgeClass(enhancedBadgeClass)) {
				updated = true;
			}
		}
		return updated;
	}

	private boolean updateBadgeClass(BadgeClassDAO.BadgeClassWithUseCount enhancedBadgeClass) {
		switch (enhancedBadgeClass.getBadgeClass().getStatus()) {
			case preparation:
				if (enhancedBadgeClass.isPreparation()) {
					return false;
				}
				break;
			case active:
				if (enhancedBadgeClass.isActive()) {
					return false;
				}
				break;
			case revoked:
				if (enhancedBadgeClass.isRevoked()) {
					return false;
				}
				break;
			case deleted:
				return false;
		}

		if (enhancedBadgeClass.isPreparation()) {
			enhancedBadgeClass.getBadgeClass().setStatus(BadgeClass.BadgeClassStatus.preparation);
		}

		if (enhancedBadgeClass.isActive()) {
			enhancedBadgeClass.getBadgeClass().setStatus(BadgeClass.BadgeClassStatus.active);
		}

		if (enhancedBadgeClass.isRevoked()) {
			enhancedBadgeClass.getBadgeClass().setStatus(BadgeClass.BadgeClassStatus.revoked);
		}

		updateBadgeClass(enhancedBadgeClass.getBadgeClass());
		log.debug("Set badge class {} to {}.", enhancedBadgeClass.getBadgeClass().getKey(),
				enhancedBadgeClass.getBadgeClass().getStatus().name());

		return true;
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
			badgeClassImageLeaf.deleteSilently();
		}

		badgeCategoryDAO.delete(badgeClass);
		badgeClassDAO.deleteBadgeClass(badgeClass);
	}

	private void createBadgeClassesRoot() {
		Path path = Paths.get(folderModule.getCanonicalRoot(), BADGES_VFS_FOLDER, CLASSES_VFS_FOLDER);
		File root = path.toFile();
		if (!root.exists()) {
			root.mkdirs();
		}
	}

	private VFSContainer getBadgeClassesRootContainer() {
		return VFSManager.olatRootContainer(File.separator + BADGES_VFS_FOLDER + File.separator + CLASSES_VFS_FOLDER, null);
	}

	private String copyBadgeClassFile(File sourceFile, String targetFileName, Identity savedBy) {
		VFSContainer classesContainer = getBadgeClassesRootContainer();
		if ("png".equals(FileUtils.getFileSuffix(targetFileName))) {
			return copyPngFile(classesContainer, sourceFile, targetFileName);
		} else {
			return copyFile(classesContainer, sourceFile, targetFileName, savedBy);
		}
	}

	private String copyPngFile(VFSContainer targetContainer, File sourceFile, String targetFileName) {
		String finalTargetFileName = VFSManager.rename(targetContainer, targetFileName);
		if (finalTargetFileName != null) {
			VFSLeaf targetLeaf = targetContainer.createChildLeaf(finalTargetFileName);
			imageService.scaleImage(sourceFile, "png", targetLeaf, MAX_BADGE_CLASS_IMAGE_WIDTH, MAX_BADGE_CLASS_IMAGE_HEIGHT);
			return finalTargetFileName;
		} else {
			log.error("Could not set a target file name for {}.", targetFileName);
		}
		return null;
	}

	private String copyFile(VFSContainer targetContainer, File sourceFile, String targetFileName, Identity savedBy) {
		String finalTargetFileName = VFSManager.rename(targetContainer, targetFileName);
		if (finalTargetFileName == null) {
			log.error("Couldn't rename file to target file name {}.", targetFileName);
			return null;
		}

		VFSLeaf targetLeaf = targetContainer.createChildLeaf(finalTargetFileName);
		try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
			if (VFSManager.copyContent(inputStream, targetLeaf, savedBy)) {
				return finalTargetFileName;
			} else {
				log.error("Couldn't copy file {} to {}", sourceFile.getName(), targetFileName);
			}
		} catch (IOException e) {
			log.error("", e);
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

	@Override
	public boolean hasCourseBadgeClasses(Identity author) {
		return badgeClassDAO.hasCourseBadgeClasses(author);
	}

	@Override
	public List<BadgeClassDAO.BadgeClassWithUseCount> getCourseBadgeClassesWithUseCounts(Identity identity) {
		return badgeClassDAO.getCourseBadgeClassesWithUseCounts(identity);
	}

	@Override
	public List<BadgeClassWithSizeAndCount> getCourseBadgeClassesWithSizesAndCounts(Identity identity) {
		return getCourseBadgeClassesWithUseCounts(identity).stream()
				.map(obj -> new BadgeClassWithSizeAndCount(obj.getBadgeClass(), sizeForBadgeClass(obj.getBadgeClass()),
						obj.getUseCount(), obj.getRevokedCount(), obj.getResetCount())).toList();
	}

	//
	// Assertion
	//

	@Override
	public BadgeAssertion createBadgeAssertion(String uuid, BadgeClass badgeClass, Date issuedOn,
									 Identity recipient, Identity awardedBy) {
		if (badgeAssertionExists(recipient, badgeClass)) {
			log.debug("Badge assertion exists for user " + recipient.toString() + " and badge " + badgeClass.getName());
			recreateBadgeAssertionIfNeeded(recipient, badgeClass, issuedOn, awardedBy);
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

	private void recreateBadgeAssertionIfNeeded(Identity recipient, BadgeClass badgeClass, Date issuedOn, Identity awardedBy) {
		BadgeAssertion badgeAssertion = badgeAssertionDAO.getBadgeAssertion(recipient, badgeClass);
		if (badgeAssertion == null) {
			if (log.isDebugEnabled()) {
				log.debug("No badge assertion exists for user {} and badge {}.", recipient, badgeClass);
			}
			return;
		}
		if (badgeAssertion.getStatus().equals(BadgeAssertion.BadgeAssertionStatus.issued)) {
			if (log.isDebugEnabled()) {
				log.debug("No need to update badge assertion for user {} and badge {}.", recipient, badgeClass);
			}
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Recreating badge '{}' for user '{}'.", badgeClass.getName(), recipient.getName());
		}

		badgeAssertion.setStatus(BadgeAssertion.BadgeAssertionStatus.issued);
		badgeAssertion.setIssuedOn(issuedOn);
		badgeAssertion.setAwardedBy(awardedBy);
		badgeAssertionDAO.updateBadgeAssertion(badgeAssertion);

		File bakedImageFile;
		if (getBadgeAssertionVfsLeaf(badgeAssertion.getBakedImage()) instanceof LocalFileImpl bakedFileImpl) {
			bakedImageFile = bakedFileImpl.getBasefile();
			MailerResult mailerResult = sendBadgeEmail(badgeAssertion, bakedImageFile);
			if (!mailerResult.isSuccessful()) {
				log.error("Sending badge creation email for badge \"{}\" to \"{}\" failed",
						badgeAssertion.getBadgeClass().getName(), badgeAssertion.getRecipient().getKey());
			}
		} else {
			log.error("Missing baked badge image for badge assertion {}.", badgeAssertion.getUuid());
		}
	}

	private MailerResult sendBadgeEmail(BadgeAssertion badgeAssertion, File bakedImageFile) {
		MailBundle mailBundle = new MailBundle();
		Identity recipient = badgeAssertion.getRecipient();
		mailBundle.setToId(recipient);
		mailBundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));

		String recipientLanguage = recipient.getUser().getPreferences().getLanguage();
		Locale recipientLocale = i18nManager.getLocaleOrDefault(recipientLanguage);
		Translator translator = Util.createPackageTranslator(OpenBadgesUIFactory.class, recipientLocale);
		String[] args = createMailArgs(badgeAssertion, translator);
		String subject = translator.translate("email.subject", args);
		String body = translator.translate("email.body", args);
		if (log.isDebugEnabled()) {
			log.debug("Email recipient: {}", recipient.getUser() != null ? recipient.getUser().getEmail() : "-");
			log.debug("Email language:  {}", recipientLanguage);
			log.debug("Email subject:   {}", subject);
			log.debug("Email body:      {}", body);
		}
		mailBundle.setContent(subject, body, bakedImageFile);

		return mailManager.sendMessage(mailBundle);
	}

	private String[] createMailArgs(BadgeAssertion badgeAssertion, Translator translator) {
		BadgeClass badgeClass = badgeAssertion.getBadgeClass();
		String badgeName = badgeClass.getName();
		String issuerName = badgeAssertion.getBadgeClass().getIssuerDisplayString();
		String issuerLine = StringHelper.containsNonWhitespace(issuerName) ?
				translator.translate("email.issuer", issuerName) : "";
		String courseName = badgeClass.getEntry() != null ? badgeClass.getEntry().getDisplayname() : "";
		String courseLine = StringHelper.containsNonWhitespace(courseName) ?
				translator.translate("email.course", courseName) : "";
		String courseReference = badgeClass.getEntry() != null ? badgeClass.getEntry().getExternalRef() : "";
		String courseReferenceLine = StringHelper.containsNonWhitespace(courseReference) ?
				translator.translate("email.course.reference", courseReference) : "";
		BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
		String criteria = badgeCriteria.getDescriptionWithScan();
		String criteriaLine = translator.translate("email.criteria", criteria);
		String badgeUrl = Settings.getServerContextPathURI() + "/url/HomeSite/" +
				badgeAssertion.getRecipient().getKey() + "/badges/0/key/" + badgeAssertion.getKey();
		String downloadButton = translator.translate("email.download.button", badgeUrl);

		return new String[] {
				badgeName,
				issuerLine,
				courseLine,
				courseReferenceLine,
				criteriaLine,
				downloadButton,
		};
	}

	@Override
	public void handleCourseReset(RepositoryEntry courseEntry, boolean learningPath, Identity doer) {
		// Direct course entry badge classes
		List<BadgeClass> badgeClasses = getBadgeClasses(courseEntry);
		if (badgeClasses == null || badgeClasses.isEmpty()) {
			return;
		}
		AssessmentToolSecurityCallback secCallback = new AssessmentToolSecurityCallback(true, false,
				false, true, true, true,
				null, null);
		getParticipantsWithAssessmentEntries(courseEntry, doer, secCallback, (participant, assessmentEntries) -> {
			for (BadgeClass badgeClass : badgeClasses) {
				handleCourseReset(badgeClass, learningPath, participant, assessmentEntries);
			}
		});
	}

	private void handleCourseReset(BadgeClass badgeClass, boolean learningPath,
								   Identity participant, List<AssessmentEntry> assessmentEntries) {
		BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
		assert badgeCriteria != null;

		if (!badgeCriteria.isAwardAutomatically()) {
			if (log.isDebugEnabled()) {
				log.debug("Not resetting manually awarded badge '{}' for participant '{}'", badgeClass.getName(), participant.getName());
			}
			return;
		}

		if (!badgeCriteria.allCourseConditionsMet(participant, learningPath, assessmentEntries)) {
			if (log.isDebugEnabled()) {
				log.debug("Resetting badge '{}' for participant '{}'.", badgeClass.getName(), participant.getName());
			}
			handleCourseReset(participant, badgeClass);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Not resetting badge '{}' for participant '{}' because conditions are still met.",
						badgeClass.getName(), participant.getName());
			}
		}
	}

	private void handleCourseReset(Identity assessedIdentity, BadgeClass badgeClass) {
		BadgeAssertion badgeAssertion = badgeAssertionDAO.getBadgeAssertion(assessedIdentity, badgeClass);
		if (badgeAssertion == null) {
			if (log.isDebugEnabled()) {
				log.debug("'{}' doesn't have badge '{}'. Nothing to reset.", assessedIdentity.getName(),
						badgeClass.getName());
			}
			return;
		}

		if (badgeAssertion.getStatus().equals(BadgeAssertion.BadgeAssertionStatus.reset)) {
			if (log.isDebugEnabled()) {
				log.debug("'{}' already has badge '{}' in reset state. No need to reset.", assessedIdentity.getName(),
						badgeClass.getName());
			}
			return;
		}

		badgeAssertion.setStatus(BadgeAssertion.BadgeAssertionStatus.reset);
		badgeAssertionDAO.updateBadgeAssertion(badgeAssertion);

		if (log.isDebugEnabled()) {
			log.debug("'{}' has badge '{}' in reset state now.", assessedIdentity.getName(),
					badgeClass.getName());
		}
	}

	@Override
	public void issueBadgesAutomatically(Identity recipient, Identity awardedBy, RepositoryEntry courseEntry,
										 boolean learningPath, List<AssessmentEntry> assessmentEntries) {
		Date issuedOn = new Date();
		for (BadgeClass badgeClass : getBadgeClasses(courseEntry)) {
			BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
			if (!badgeCriteria.isAwardAutomatically()) {
				continue;
			}
			if (isCourseBadge(badgeClass) && badgeCriteria.allCourseConditionsMet(recipient, learningPath, assessmentEntries)) {
				String uuid = OpenBadgesFactory.createIdentifier();
				createBadgeAssertion(uuid, badgeClass, issuedOn, recipient, awardedBy);
			}
			if (isGlobalBadge(badgeClass) && badgeCriteria.allGlobalBadgeConditionsMet(recipient, null)) {
				String uuid = OpenBadgesFactory.createIdentifier();
				createBadgeAssertion(uuid, badgeClass, issuedOn, recipient, awardedBy);
			}
		}
	}

	private boolean isCourseBadge(BadgeClass badgeClass) {
		return badgeClass.getEntry() != null;
	}

	private boolean isGlobalBadge(BadgeClass badgeClass) {
		return !isCourseBadge(badgeClass);
	}

	@Override
	public void issueBadgesAutomatically(RepositoryEntry courseEntry, boolean learningPath, Identity awardedBy) {
		if (courseEntry.getEntryStatus() != RepositoryEntryStatusEnum.published) {
			return;
		}
		if (!isEnabled()) {
			return;
		}
		List<BadgeClass> badgeClasses = getBadgeClasses(courseEntry);
		if (badgeClasses == null || badgeClasses.isEmpty()) {
			return;
		}
		AssessmentToolSecurityCallback secCallback = new AssessmentToolSecurityCallback(true, false,
				false, true, true, true,
				null, null);

		getParticipantsWithAssessmentEntries(courseEntry, awardedBy, secCallback, (participant, assessmentEntries) -> {
			for (BadgeClass badgeClass : badgeClasses) {
				issueBadgeAutomatically(badgeClass, learningPath, participant, assessmentEntries, awardedBy);
			}
		});
	}

	private void issueBadgeAutomatically(BadgeClass badgeClass, boolean learningPath, Identity participant,
										 List<AssessmentEntry> assessmentEntries, Identity awardedBy) {
		BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
		assert badgeCriteria != null;

		if (!badgeCriteria.isAwardAutomatically()) {
			if (log.isDebugEnabled()) {
				log.debug("Badge '{}' is not awarded automatically for participant '{}' (manual mode)", badgeClass.getName(), participant.getName());
			}
			return;
		}

		if (badgeCriteria.allCourseConditionsMet(participant, learningPath, assessmentEntries)) {
			if (log.isDebugEnabled()) {
				log.debug("Issuing badge '{}' automatically for participant '{}'.", badgeClass.getName(),
						participant.getName());
			}
			issueBadge(badgeClass, List.of(participant), awardedBy);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Not issuing badge '{}' for participant '{}' because conditions are not met.",
						badgeClass.getName(), participant.getName());
			}
		}
	}

	@Override
	public void getParticipantsWithAssessmentEntries(RepositoryEntry courseEntry, Identity identity,
													 AssessmentToolSecurityCallback securityCallback,
													 AssessmentEntriesHandler assessmentEntriesHandler) {

		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(
				courseEntry, null, null, securityCallback);

		Set<Identity> assessedIdentities = new HashSet<>(assessmentToolManager.getAssessedIdentities(identity, params));

		final Identity[] currentParticipant = {null};
		final List<AssessmentEntry>[] currentAssessmentEntries = new List[]{null};

		assessmentToolManager.getAssessmentEntries(identity, params, null, (assessmentEntry) -> {
			if (assessmentEntry.getIdentity() == null) {
				return;
			}
			Identity participant = assessmentEntry.getIdentity();
			if (currentParticipant[0] == null) {
				currentParticipant[0] = participant;
				currentAssessmentEntries[0] = new ArrayList<>();
			} else if (!currentParticipant[0].equals(participant)) {
				if (assessedIdentities.contains(currentParticipant[0])) {
					assessmentEntriesHandler.handleAssessmentEntries(currentParticipant[0], currentAssessmentEntries[0]);
				}
				currentParticipant[0] = participant;
				currentAssessmentEntries[0] = new ArrayList<>();
			}

			currentAssessmentEntries[0].add(assessmentEntry);
		});

		if (!currentAssessmentEntries[0].isEmpty() && assessedIdentities.contains(currentParticipant[0])) {
			assessmentEntriesHandler.handleAssessmentEntries(currentParticipant[0], currentAssessmentEntries[0]);
		}
	}

	@Override
	public List<ParticipantAndAssessmentEntries> associateParticipantsWithAssessmentEntries(List<AssessmentEntry> assessmentEntries) {
		Map<Identity, List<AssessmentEntry>> participantToAssessmentEntries = new HashMap<>();
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			Identity assessedIdentity = assessmentEntry.getIdentity();
			if (!participantToAssessmentEntries.containsKey(assessedIdentity)) {
				participantToAssessmentEntries.put(assessedIdentity, new ArrayList<>());
			}
			participantToAssessmentEntries.get(assessedIdentity).add(assessmentEntry);
		}
		List<ParticipantAndAssessmentEntries> result = new ArrayList<>();
		for (Identity assessedIdentity : participantToAssessmentEntries.keySet()) {
			List<AssessmentEntry> participantAssessmentEntries = participantToAssessmentEntries.get(assessedIdentity);
			result.add(new ParticipantAndAssessmentEntries(assessedIdentity, participantAssessmentEntries));
		}
		return result;
	}

	@Override
	public void issueBadge(BadgeClass badgeClass, List<Identity> recipients, Identity awardedBy) {
		if (recipients == null || recipients.isEmpty()) {
			return;
		}
		Date issuedOn = new Date();
		for (Identity recipient : recipients) {
			String uuid = OpenBadgesFactory.createIdentifier();
			createBadgeAssertion(uuid, badgeClass, issuedOn, recipient, awardedBy);
		}
	}

	static String createRecipientObject(Identity recipient, String salt) {
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
			return createBakedSvgBadgeImage(badgeAssertion, savedBy);
		} else if (OpenBadgesFactory.isPngFileName(badgeClassImage)) {
			return createBakedPngBadgeImage(badgeAssertion);
		}
		return null;
	}

	private String createBakedSvgBadgeImage(BadgeAssertion badgeAssertion, Identity savedBy) {
		VFSLeaf badgeClassImage = getBadgeClassVfsLeaf(badgeAssertion.getBadgeClass().getImage());
		if (badgeClassImage instanceof LocalFileImpl localFile) {
			try {
				String svg = Files.readString(localFile.getBasefile().toPath());
				String jsonString = createBakedJsonString(badgeAssertion);
				String verifyUrl = OpenBadgesFactory.createAssertionVerifyUrl(badgeAssertion.getUuid());
				String bakedSvg = mergeAssertionJson(svg, jsonString, verifyUrl);
				InputStream inputStream = new ByteArrayInputStream(bakedSvg.getBytes(StandardCharsets.UTF_8));
				VFSContainer assertionsContainer = getBadgeAssertionsRootContainer();
				String bakedImage = badgeAssertion.getUuid() + ".svg";
				VFSLeaf targetLeaf = assertionsContainer.createChildLeaf(bakedImage);
				VFSManager.copyContent(inputStream, targetLeaf, savedBy);
				return bakedImage;
			} catch (IOException e) {
				log.error("Error creating baked SVG badge image", e);
				return null;
			}
		}
		return null;
	}

	static String createBakedJsonString(BadgeAssertion badgeAssertion) {
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
		VFSLeaf badgeClassImage = getBadgeClassVfsLeaf(badgeAssertion.getBadgeClass().getImage());
		if (badgeClassImage == null) {
			log.error("No image found for badge class {}", badgeAssertion.getBadgeClass().getUuid());
			return null;
		}

		IIOImage iioImage = readIIOImage(badgeClassImage.getInputStream());
		if (iioImage == null) {
			return null;
		}

		String jsonString = createBakedJsonString(badgeAssertion);

		IIOMetadata metadata = iioImage.getMetadata();
		try {
			addNativePngTextEntry(metadata, "openbadges", jsonString);
		} catch (IIOInvalidTreeException e) {
			log.error("", e);
			return null;
		}

		IIOImage bakedImage = new IIOImage(iioImage.getRenderedImage(), null, metadata);
		try {
			String bakedImageName = badgeAssertion.getUuid() + ".png";
			VFSContainer assertionsContainer = getBadgeAssertionsRootContainer();
			VFSLeaf targetLeaf = assertionsContainer.createChildLeaf(bakedImageName);
			writeImageIOImage(bakedImage, targetLeaf.getOutputStream(false));
			return bakedImageName;
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}

	static IIOImage readIIOImage(InputStream inputStream) {
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(imageInputStream);
			if (!imageReaderIterator.hasNext()) {
				log.error("No PNG reader found");
				return null;
			}
			ImageReader imageReader = imageReaderIterator.next();
			imageReader.setInput(imageInputStream);

			return imageReader.readAll(0, null);
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}

	// https://stackoverflow.com/questions/41265608/png-metadata-read-and-write
	static void addNativePngTextEntry(IIOMetadata metadata, String keyword, String value) throws IIOInvalidTreeException {
		IIOMetadataNode textEntry = new IIOMetadataNode("iTXtEntry");
		textEntry.setAttribute("keyword", keyword);
		textEntry.setAttribute("text", value);
		textEntry.setAttribute("compressionFlag", "FALSE");
		textEntry.setAttribute("compressionMethod", "0");
		textEntry.setAttribute("languageTag", "");
		textEntry.setAttribute("translatedKeyword", "");

		IIOMetadataNode text = new IIOMetadataNode("iTXt");
		text.appendChild(textEntry);

		IIOMetadataNode root = new IIOMetadataNode(metadata.getNativeMetadataFormatName());
		root.appendChild(text);

		metadata.mergeTree(metadata.getNativeMetadataFormatName(), root);
	}

	static void writeImageIOImage(IIOImage iioImage, OutputStream outputStream) throws IOException {
		Iterator imageWriters = ImageIO.getImageWritersByFormatName("png");
		ImageWriter imageWriter = (ImageWriter) imageWriters.next();
		ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
		imageWriter.setOutput(imageOutputStream);
		imageWriter.write(null, iioImage, null);
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
		return getBadgeAssertions(identity, courseEntry, nullEntryMeansAll).stream().map(badgeAssertion -> new BadgeAssertionWithSize(badgeAssertion, sizeForBadgeAssertion(badgeAssertion))).toList();
	}

	private Size sizeForBadgeAssertion(BadgeAssertion badgeAssertion) {
		if (badgeAssertion.getBakedImage() == null) {
			return new Size(0, 0, false);
		}
		VFSLeaf bakedImageLeaf = getBadgeAssertionVfsLeaf(badgeAssertion.getBakedImage());
		return sizeForVfsLeaf(bakedImageLeaf);
	}

	@Override
	public boolean isBadgeAssertionExpired(BadgeAssertion badgeAssertion) {
		Date expiryDate = getBadgeAssertionExpirationDate(badgeAssertion);
		if (expiryDate == null) {
			return false;
		}
		return new Date().after(expiryDate);
	}

	private Date getBadgeAssertionExpirationDate(BadgeAssertion badgeAssertion) {
		if (badgeAssertion == null) {
			return null;
		}
		if (badgeAssertion.getExpires() != null) {
			return badgeAssertion.getExpires();
		}
		BadgeClass badgeClass = badgeAssertion.getBadgeClass();
		if (badgeClass == null) {
			return null;
		}
		if (!badgeClass.isValidityEnabled()) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(badgeAssertion.getIssuedOn());
		switch (badgeClass.getValidityTimelapseUnit()) {
			case year -> calendar.add(Calendar.YEAR, badgeClass.getValidityTimelapse());
			case month -> calendar.add(Calendar.MONTH, badgeClass.getValidityTimelapse());
			case week -> calendar.add(Calendar.WEEK_OF_YEAR, badgeClass.getValidityTimelapse());
			case day -> calendar.add(Calendar.DAY_OF_YEAR, badgeClass.getValidityTimelapse());
		}
		return calendar.getTime();
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
	public boolean unrevokedBadgeAssertionsExist(BadgeClass badgeClass) {
		return badgeAssertionDAO.unrevokedBadgeAssertionsExist(badgeClass);
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
	public void revokeBadgeAssertions(BadgeClass badgeClass) {
		badgeAssertionDAO.revokeBadgeAssertions(badgeClass);
	}

	@Override
	public void deleteBadgeAssertion(BadgeAssertion badgeAssertion) {
		deleteBadgeAssertionImage(badgeAssertion.getBakedImage());
		badgeAssertionDAO.deleteBadgeAssertion(badgeAssertion);
	}

	private void deleteBadgeAssertionImage(String bakedImage) {
		if (getBadgeAssertionsRootContainer().resolve(bakedImage) instanceof VFSLeaf imageLeaf) {
			imageLeaf.deleteSilently();
		}
	}

	@Override
	public BadgeAssertion getBadgeAssertion(String uuid) {
		return badgeAssertionDAO.getAssertion(uuid);
	}

	@Override
	public List<Identity> getBadgeAssertionIdentities(Collection<Long> badgeClassKeys) {
		return badgeAssertionDAO.getBadgeAssertionIdentities(badgeClassKeys);
	}

	@Override
	public boolean hasBadgeAssertion(Identity recipient, String badgeClassUuid) {
		return badgeAssertionDAO.hasBadgeAssertion(recipient.getKey(), badgeClassUuid);
	}

	@Override
	public boolean hasBadgeAssertion(Identity recipient, Long badgeClassKey) {
		return badgeAssertionDAO.hasBadgeAssertion(recipient.getKey(), badgeClassKey);
	}

	@Override
	public List<BadgeAssertion> getRuleEarnedBadgeAssertions(IdentityRef recipient, RepositoryEntryRef courseEntry, String courseNodeIdent) {
		if (recipient == null || courseEntry == null || courseNodeIdent == null) {
			return new ArrayList<>();
		}
		return badgeAssertionDAO.getBadgeAssertions(recipient, courseEntry, false).stream()
				.filter(ba -> conditionForCourseNodeExists(ba.getBadgeClass(), courseNodeIdent)).toList();
	}

	public boolean conditionForCourseNodeExists(BadgeClass badgeClass, String courseNodeIdent) {
		BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
		if (badgeCriteria.conditionForCourseNodeExists(courseNodeIdent)) {
			return true;
		}
		return false;
	}

	private void createBadgeAssertionsRoot() {
		Path path = Paths.get(folderModule.getCanonicalRoot(), BADGES_VFS_FOLDER, ASSERTIONS_VFS_FOLDER);
		File root = path.toFile();
		if (!root.exists()) {
			root.mkdirs();
		}
	}

	@Override
	public String badgeAssertionAsLinkedInUrl(BadgeAssertion badgeAssertion) {
		BadgeClass badgeClass = badgeAssertion.getBadgeClass();
		LinkedInUrl.LinkedInUrlBuilder linkedInUrlBuilder = new LinkedInUrl.LinkedInUrlBuilder();

		Date issuedOn = badgeAssertion.getIssuedOn();
		Calendar cal = Calendar.getInstance();
		cal.setTime(issuedOn);

		linkedInUrlBuilder
				.add(LinkedInUrl.Field.name, badgeClass.getName())
				.add(LinkedInUrl.Field.certId, badgeAssertion.getUuid())
				.add(LinkedInUrl.Field.certUrl, OpenBadgesFactory.createAssertionPublicUrl(badgeAssertion.getUuid()))
				.add(LinkedInUrl.Field.issueYear, Integer.toString(cal.get(Calendar.YEAR)))
				.add(LinkedInUrl.Field.issueMonth, Integer.toString(cal.get(Calendar.MONTH) + 1));

		if (badgeAssertion.getBadgeClass().getBadgeOrganization() != null) {
			linkedInUrlBuilder.add(LinkedInUrl.Field.organizationId, badgeClass.getBadgeOrganization().getOrganizationKey());
		} else {
			linkedInUrlBuilder.add(LinkedInUrl.Field.organizationName, badgeClass.getIssuerDisplayString());
		}

		Date expiresOn = getBadgeAssertionExpirationDate(badgeAssertion);
		if (expiresOn != null) {
			Calendar expiresOnCal = Calendar.getInstance();
			expiresOnCal.setTime(expiresOn);
			linkedInUrlBuilder
					.add(LinkedInUrl.Field.expirationYear, Integer.toString(expiresOnCal.get(Calendar.YEAR)))
					.add(LinkedInUrl.Field.expirationMonth, Integer.toString(expiresOnCal.get(Calendar.MONTH) + 1));
		}

		return linkedInUrlBuilder.asUrl();
	}

	private VFSContainer getBadgeAssertionsRootContainer() {
		return VFSManager.olatRootContainer(File.separator + BADGES_VFS_FOLDER + File.separator + ASSERTIONS_VFS_FOLDER, null);
	}

	static NamedNodeMap extractAssertionJsonStringFromPng(InputStream inputStream) {
		IIOImage iioImage = readIIOImage(inputStream);
		if (iioImage == null) {
			return null;
		}
		return extractTextAttributes(iioImage);
	}

	private static NamedNodeMap extractTextAttributes(IIOImage iioImage) {
		IIOMetadata iioMetadata = iioImage.getMetadata();
		for (String formatName : iioMetadata.getMetadataFormatNames()) {
			Node node = iioMetadata.getAsTree(formatName);
			NamedNodeMap attributes = findTextAttributes(node, "");
			if (attributes != null) {
				return attributes;
			}
		}
		return null;
	}

	private static NamedNodeMap findTextAttributes(Node node, String space) {
		System.err.println(space + node.getNodeName() + "(" + node.getNodeType() + ")");
		if (node.getNodeName().equals("iTXtEntry")) {
			Node keyword = node.getAttributes().getNamedItem("keyword");
			if (keyword != null && keyword.getNodeValue() != null) {
				if ("openbadges".equals(keyword.getNodeValue())) {
					return node.getAttributes();
				}
			}
		}
		for (int i = 0; i < node.getAttributes().getLength(); i++) {
			Node attribute = node.getAttributes().item(i);
			if (attribute.getNodeValue() != null && attribute.getNodeValue().length() > 64) {
				System.err.println(space + "- " + attribute.getNodeName() + "(" + attribute.getNodeType() + ") = " + attribute.getNodeValue().substring(0, 64) + "...");
			} else {
				System.err.println(space + "- " + attribute.getNodeName() + "(" + attribute.getNodeType() + ") = " + attribute.getNodeValue());
			}
		}
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node child = node.getChildNodes().item(i);
			NamedNodeMap namedNodeMap = findTextAttributes(child, space + "  ");
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
	public void copyConfigurationAndBadgeClasses(RepositoryEntry sourceEntry, RepositoryEntry targetEntry, Identity author) {
		BadgeEntryConfiguration sourceConfiguration = badgeEntryConfigurationDAO.getConfiguration(sourceEntry);
		if (sourceConfiguration != null) {
			badgeEntryConfigurationDAO.cloneConfiguration(sourceConfiguration, targetEntry);
		}
		List<BadgeClass> sourceClasses = badgeClassDAO.getBadgeClasses(sourceEntry, true);
		List<BadgeClass> targetClasses = sourceClasses.stream()
				.filter(bc -> !BadgeClass.BadgeClassStatus.revoked.equals(bc.getStatus()))
				.map(bc -> cloneBadgeClass(bc, targetEntry, author, null)).toList();
		Map<String, String> uuidMap = createUuidMap(sourceClasses, targetClasses);
		remapUuids(targetClasses, uuidMap);
	}

	private void remapUuids(List<BadgeClass> badgeClasses, Map<String, String> uuidMap) {
		for (BadgeClass badgeClass : badgeClasses) {
			BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
			if (badgeCriteria != null && badgeCriteria.remapBadgeClassUuids(uuidMap)) {
				badgeClass.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
				badgeClassDAO.updateBadgeClass(badgeClass);
			}
		}
	}

	private Map<String, String> createUuidMap(List<BadgeClass> sourceClasses, List<BadgeClass> targetClasses) {
		Map<String, String> uuidMap = new HashMap<>();
		if (sourceClasses != null && targetClasses != null && sourceClasses.size() == targetClasses.size()) {
			for (int i = 0; i < sourceClasses.size(); i++) {
				BadgeClass sourceClass = sourceClasses.get(i);
				BadgeClass targetClass = targetClasses.get(i);
				uuidMap.put(sourceClass.getUuid(), targetClass.getUuid());
			}
		}
		return uuidMap;
	}

	@Override
	public void importConfiguration(RepositoryEntry targetEntry, BadgeEntryConfiguration importedConfiguration) {
		BadgeEntryConfiguration targetConfiguration = getConfiguration(targetEntry);
		targetConfiguration.setAwardEnabled(importedConfiguration.isAwardEnabled());
		targetConfiguration.setOwnerCanAward(importedConfiguration.isOwnerCanAward());
		targetConfiguration.setCoachCanAward(importedConfiguration.isCoachCanAward());
		updateConfiguration(targetConfiguration);
	}

	@Override
	public void importBadgeClasses(RepositoryEntry targetEntry, BadgeClasses badgeClasses, File fImportBaseDirectory, Identity author) {
		List<BadgeClass> targetClasses = badgeClasses.getItems().stream()
				.filter(bc -> !BadgeClass.BadgeClassStatus.revoked.equals(bc.getStatus()))
				.map(bc -> cloneBadgeClass(bc, targetEntry, author, fImportBaseDirectory)).toList();
		Map<String, String> uuidMap = createUuidMap(badgeClasses.getItems(), targetClasses);
		remapUuids(targetClasses, uuidMap);
	}

	@Override
	public List<BadgeOrganization> loadLinkedInOrganizations() {
		return badgeOrganizationDAO.loadBadgeOrganizations(BadgeOrganization.BadgeOrganizationType.linkedInOrganization);
	}

	@Override
	public BadgeOrganization loadLinkedInOrganization(Long key) {
		return badgeOrganizationDAO.loadBadgeOrganization(key);
	}

	@Override
	public BadgeOrganization loadLinkedInOrganization(String organizationId) {
		return badgeOrganizationDAO.loadBadgeOrganization(organizationId);
	}

	@Override
	public boolean isBadgeOrganizationInUse(Long key) {
		return badgeOrganizationDAO.isBadgeOrganizationInUse(key);
	}

	@Override
	public void addLinkedInOrganization(String organizationId, String organizationName) {
		badgeOrganizationDAO.createBadgeOrganization(BadgeOrganization.BadgeOrganizationType.linkedInOrganization,
				organizationId, organizationName);
	}

	@Override
	public void updateBadgeOrganization(BadgeOrganization badgeOrganization) {
		badgeOrganizationDAO.updateBadgeOrganization(badgeOrganization);
	}

	@Override
	public void deleteBadgeOrganization(BadgeOrganization badgeOrganization) {
		badgeOrganizationDAO.deleteBadgeOrganization(badgeOrganization);
	}

	@Override
	public boolean isEnabled() {
		return openBadgesModule.isEnabled();
	}

	@Override
	public boolean showBadgesEditTab(RepositoryEntry courseEntry, CourseNode courseNode) {
		if (!isEnabled()) {
			return false;
		}

		BadgeEntryConfiguration badgeEntryConfiguration = getConfiguration(courseEntry);
		if (!badgeEntryConfiguration.isAwardEnabled()) {
			return false;
		}

		if (!AssessmentHelper.checkIfNodeIsAssessable(courseEntry, courseNode)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean showBadgesRunSegment(RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		if (!isEnabled()) {
			return false;
		}

		BadgeEntryConfiguration badgeEntryConfiguration = getConfiguration(courseEntry);
		if (!badgeEntryConfiguration.isAwardEnabled()) {
			return false;
		}

		if (userCourseEnv.isCourseReadOnly() || userCourseEnv.isParticipant()) {
			return false;
		}

		if (userCourseEnv.isCoach()) {
			if (!badgeEntryConfiguration.isCoachCanAward()) {
				return false;
			}
		}

		if (!AssessmentHelper.checkIfNodeIsAssessable(courseEntry, courseNode)) {
			return false;
		}

		return true;
	}
}
