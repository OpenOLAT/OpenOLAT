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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.FileStorage;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeCategory;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesBakeContext;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.OpenBadgesModule;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Badge;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryDAO;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
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
	}

	//
	// Template
	//

	@Override
	public BadgeTemplate createTemplate(String name, File templateFile, String targetFileName, String description,
										Collection<String> scopes, Identity savedBy) {
		String templateFileName = copyTemplate(templateFile, targetFileName, savedBy);
		if (templateFileName != null) {
			BadgeTemplate badgeTemplate = templateDAO.createTemplate(templateFileName, name);
			badgeTemplate.setDescription(description);
			badgeTemplate.setScopesAsCollection(scopes);
			return templateDAO.updateTemplate(badgeTemplate);
		}
		return null;
	}

	@Override
	public BadgeTemplate getTemplate(Long key) {
		return templateDAO.getTemplate(key);
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
			default -> "rgb(255,255,255)";
		};
	}

	@Override
	public List<BadgeTemplate> getTemplates() {
		return templateDAO.getTemplates();
	}

	@Override
	public List<TemplateWithSize> getTemplatesWithSizes() {
		return getTemplates().stream().map((template) -> new TemplateWithSize(template, sizeForTemplate(template))).toList();
	}

	private Size sizeForTemplate(BadgeTemplate template) {
		VFSLeaf imageLeaf = getTemplateVfsLeaf(template.getImage());
		return sizeForVfsLeaf(imageLeaf);
	}

	private Size sizeForVfsLeaf(VFSLeaf imageLeaf) {
		Size imageSize = null;

		if (imageLeaf != null && imageLeaf.exists()) {
			String suffix = FileUtils.getFileSuffix(imageLeaf.getName());
			if (!"svg".equalsIgnoreCase(suffix)) {
				imageSize = imageService.getSize(imageLeaf, suffix);
			}
			if (imageSize == null) {
				if (StringHelper.containsNonWhitespace(suffix)) {
					if ("svg".equalsIgnoreCase(suffix)) {
						suffix = "svg+xml";
					}
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
	public void updateTemplate(BadgeTemplate template) {
		templateDAO.updateTemplate(template);
	}

	@Override
	public void deleteTemplate(BadgeTemplate badgeTemplate) {
		if (getBadgeTemplatesRootContainer().resolve(badgeTemplate.getImage()) instanceof VFSLeaf templateLeaf) {
			templateLeaf.delete();
		}

		badgeCategoryDAO.delete(badgeTemplate);
		templateDAO.deleteTemplate(badgeTemplate);
	}

	private String copyTemplate(File sourceFile, String targetFileName, Identity savedBy) {
		VFSContainer templateContainer = getBadgeTemplatesRootContainer();
		return copyFile(templateContainer, sourceFile, targetFileName, savedBy);
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
	public BadgeClass createBadgeClass(String uuid, String version, String language, File sourceFile, String targetFileName,
									   String name, String description, String criteria, String salt, String issuer,
									   Identity savedBy) {
		String badgeClassImageFileName = copyBadgeClassFile(sourceFile, targetFileName, savedBy);
		if (badgeClassImageFileName != null) {
			BadgeClass badgeClass = badgeClassDAO.createBadgeClass(uuid, version, badgeClassImageFileName,
					name, description, criteria, salt, issuer);
			badgeClass.setLanguage(language);
			return badgeClassDAO.updateBadgeClass(badgeClass);
		}
		return null;
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
	public BadgeClass getBadgeClass(String uuid) {
		return badgeClassDAO.getBadgeClass(uuid);
	}

	@Override
	public List<BadgeClassWithSize> getBadgeClassesWithSizes(RepositoryEntry entry) {
		return getBadgeClasses(entry).stream().map((badgeClass) -> new BadgeClassWithSize(badgeClass, sizeForBadgeClass(badgeClass))).toList();
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
	public void deleteBadgeClass(BadgeClass badgeClass) {
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

	//
	// Assertion
	//

	@Override
	public BadgeAssertion createBadgeAssertion(String uuid, BadgeClass badgeClass, Date issuedOn,
									 Identity recipient, Identity awardedBy) {
		String verification = "{\"type\":\"hosted\"}";
		String recipientObject = createRecipientObject(recipient, badgeClass.getSalt());
		if (recipientObject == null) {
			return null;
		}
		return badgeAssertionDAO.createBadgeAssertion(uuid, recipientObject, badgeClass, verification, issuedOn,
				recipient, awardedBy);
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

	@Override
	public List<BadgeAssertion> getBadgeAssertions() {
		return badgeAssertionDAO.getBadgeAssertions();
	}

	@Override
	public List<BadgeAssertionWithSize> getBadgeAssertionsWithSizes() {
		return getBadgeAssertions().stream().map((badgeAssertion) -> new BadgeAssertionWithSize(badgeAssertion, sizeForBadgeAssertion(badgeAssertion))).toList();
	}

	private Size sizeForBadgeAssertion(BadgeAssertion badgeAssertion) {
		if (badgeAssertion.getBakedImage() == null) {
			return new Size(0, 0, false);
		}
		VFSLeaf bakedImageLeaf = getBadgeAssertionVfsLeaf(badgeAssertion.getBakedImage());
		return sizeForVfsLeaf(bakedImageLeaf);
	}

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
	public void updateBadgeAssertion(BadgeAssertion badgeAssertion) {
		String recipientObject = createRecipientObject(badgeAssertion.getRecipient(), badgeAssertion.getBadgeClass().getSalt());
		if (recipientObject == null) {
			return;
		}
		badgeAssertion.setRecipientObject(recipientObject);
		badgeAssertionDAO.updateBadgeAssertion(badgeAssertion);
	}

	@Override
	public void deleteBadgeAssertion(BadgeAssertion badgeAssertion) {
		badgeAssertionDAO.deleteBadgeAssertion(badgeAssertion);
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
	public List<? extends TagInfo> getCategories(BadgeTemplate badgeTemplate, BadgeClass badgeClass) {
		return badgeCategoryDAO.readBadgeCategoryTags(badgeTemplate, badgeClass);
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
