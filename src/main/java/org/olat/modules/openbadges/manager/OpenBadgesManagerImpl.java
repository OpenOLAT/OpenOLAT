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
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.FileStorage;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesBakeContext;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Badge;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
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
	private MovieService movieService;

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
	public void createTemplate(String name, File templateFile, String targetFileName, String description,
							   String category, Collection<String> scopes, Identity savedBy) {
		String templateFileName = copyTemplate(templateFile, targetFileName, savedBy);
		if (templateFileName != null) {
			BadgeTemplate template = templateDAO.createTemplate(templateFileName, name);
			template.setDescription(description);
			template.setCategory(category);
			template.setScopesAsCollection(scopes);
			templateDAO.updateTemplate(template);
		}
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
	public List<BadgeTemplate> getTemplates() {
		return templateDAO.getTemplates();
	}

	@Override
	public List<TemplateWithSize> getTemplatesWithSizes() {
		return getTemplates().stream().map((template) -> new TemplateWithSize(template, sizeForTemplate(template))).toList();
	}

	private Size sizeForTemplate(BadgeTemplate template) {
		return movieService.getSize(getTemplateVfsLeaf(template.getImage()), "svg+xml");
	}

	@Override
	public void updateTemplate(BadgeTemplate template) {
		templateDAO.updateTemplate(template);
	}

	@Override
	public void deleteTemplate(BadgeTemplate template) {
		if (getBadgeTemplatesRootContainer().resolve(template.getImage()) instanceof VFSLeaf templateLeaf) {
			templateLeaf.delete();
		}

		templateDAO.deleteTemplate(template);
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
	public void createBadgeClass(String uuid, String version, File uploadedFile, String targetFileName,
								 String name, String description, String criteria, String issuer, String tags,
								 Identity savedBy) {
		String badgeClassImageFileName = copyBadgeClassFile(uploadedFile, targetFileName, savedBy);
		if (badgeClassImageFileName != null) {
			BadgeClass badgeClass = badgeClassDAO.createBadgeClass(uuid, version, badgeClassImageFileName,
					name, description, criteria, issuer);
			badgeClass.setTags(tags);
			badgeClassDAO.updateBadgeClass(badgeClass);
		}
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
	public List<BadgeClass> getBadgeClasses() {
		return badgeClassDAO.getBadgeClasses();
	}

	@Override
	public BadgeClass getBadgeClass(String uuid) {
		return badgeClassDAO.getBadgeClass(uuid);
	}

	@Override
	public List<BadgeClassWithSize> getBadgeClassesWithSizes() {
		return getBadgeClasses().stream().map((badgeClass) -> new BadgeClassWithSize(badgeClass, sizeForBadgeClass(badgeClass))).toList();
	}

	private Size sizeForBadgeClass(BadgeClass badgeClass) {
		return movieService.getSize(getBadgeClassVfsLeaf(badgeClass.getImage()), "svg+xml");
	}

	@Override
	public void updateBadgeClass(BadgeClass badgeClass) {
		badgeClassDAO.updateBadgeClass(badgeClass);
	}

	@Override
	public void deleteBadgeClass(BadgeClass badgeClass) {
		if (getBadgeClassesRootContainer().resolve(badgeClass.getImage()) instanceof VFSLeaf badgeClassImageLeaf) {
			badgeClassImageLeaf.delete();
		}

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
	public void createBadgeAssertion(String uuid, String recipientEmail, BadgeClass badgeClass, Date issuedOn,
									 Identity savedBy) {
		String verification = "{\"type\":\"hosted\"}";
		badgeAssertionDAO.createBadgeAssertion(uuid, recipientEmail, badgeClass, verification, issuedOn, savedBy);
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
			return null;
		}
		VFSLeaf bakedImageLeaf = getBadgeAssertionVfsLeaf(badgeAssertion.getBakedImage());
		if (bakedImageLeaf != null && bakedImageLeaf.exists()) {
			return movieService.getSize(bakedImageLeaf, "svg+xml");
		}
		return null;
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
		badgeAssertionDAO.updateBadgeAssertion(badgeAssertion);
	}

	@Override
	public void deleteBadgeAssertion(BadgeAssertion badgeAssertion) {
		badgeAssertionDAO.deleteBadgeAssertion(badgeAssertion);
	}

	@Override
	public BadgeAssertion getBadgeAssertion(String uuid) {
		return null;
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
}
