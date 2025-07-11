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
package org.olat.course.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSRevisionsAndThumbnailsFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.manager.RepositoryEntryCertificateConfigurationXStream;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigManager;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CPCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.course.nodes.bc.CoachFolderFactory;
import org.olat.course.nodes.bc.CourseDocumentsFactory;
import org.olat.course.nodes.cp.CPEditController;
import org.olat.course.nodes.video.VideoEditController;
import org.olat.course.style.CourseStyleService;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeClasses;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.manager.BadgeClassesXStream;
import org.olat.modules.openbadges.manager.BadgeEntryConfigurationXStream;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.CourseHandler;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 17 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseExportMediaResource implements MediaResource, StreamingOutput {
	
	private static Logger log = Tracing.createLoggerFor(CourseExportMediaResource.class);
	
	private final OLATResourceable resource;
	private final RepositoryEntryImportExportLinkEnum withLinkedReferences;
	
	public CourseExportMediaResource(OLATResourceable resource, RepositoryEntryImportExportLinkEnum withLinkedReferences) {
		this.resource = resource;
		this.withLinkedReferences = withLinkedReferences;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		try(ZipOutputStream zout = new ZipOutputStream(output)) {
			exportCourseToZIP(resource, zout);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}

		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(resource, true);
			String label = StringHelper.transformDisplayNameToFileSystemName(entry.getDisplayname());
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(label + ".zip"));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));
			exportCourseToZIP(resource, zout);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
	
	/**
	 * Exports an entire course to a zip file.
	 *
	 * @param sourceRes
	 * @param fTargetZIP
	 * @return true if successfully exported, false otherwise.
	 */
	private void exportCourseToZIP(OLATResourceable sourceRes, ZipOutputStream outStream) {
		PersistingCourseImpl sourceCourse = (PersistingCourseImpl) CourseFactory.loadCourse(sourceRes);
		log.info("Start course export: {}", sourceRes);
		synchronized (sourceCourse) { //o_clusterNOK - cannot be solved with doInSync since could take too long (leads to error: "Lock wait timeout exceeded")
			OLATResource courseResource = sourceCourse.getCourseEnvironment().getCourseGroupManager().getCourseResource();
			exportToFilesystem(courseResource, sourceCourse, outStream);
		}
	}
	
	/**
	 * @see org.olat.course.ICourse#exportToFilesystem(java.io.File)
	 * <p>
	 * See OLAT-5368: Course Export can take longer than say 2min.
	 * <p>
	 */
	private void exportToFilesystem(OLATResource originalCourseResource, PersistingCourseImpl sourceCourse, ZipOutputStream outStream) {
		long s = System.currentTimeMillis();
		
		File fExportDir = new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID());
		try {
			fExportDir.mkdirs();
			log.info("exportToFilesystem: exporting course {} to {}", originalCourseResource, fExportDir);
			exportToFilesystem(sourceCourse, outStream, fExportDir);
		} catch(Exception e) {
			log.error("", e);
		} finally {
			FileUtils.deleteDirsAndFiles(fExportDir, true, true);
		}
		
		log.info("exportToFilesystem: exporting course {} to {} done", originalCourseResource, fExportDir);
		log.info("finished export course '{}' in {}s", sourceCourse.getCourseTitle(), Long.toString((System.currentTimeMillis() - s) / 1000l));
	}

	private void exportToFilesystem(PersistingCourseImpl sourceCourse, ZipOutputStream zout, File exportDirectory) {
		LocalFolderImpl courseBaseContainer = sourceCourse.getCourseBaseContainer();
		File fCourseBase = courseBaseContainer.getBasefile();

		ZipUtil.addFileToZip(CourseConfigManager.COURSECONFIG_XML, new File(fCourseBase, CourseConfigManager.COURSECONFIG_XML), zout);
		ZipUtil.addFileToZip(PersistingCourseImpl.EDITORTREEMODEL_XML, new File(fCourseBase, PersistingCourseImpl.EDITORTREEMODEL_XML), zout);
		ZipUtil.addFileToZip(PersistingCourseImpl.RUNSTRUCTURE_XML, new File(fCourseBase, PersistingCourseImpl.RUNSTRUCTURE_XML), zout);
		// export layout and media folder
		File layoutDirectory = new File(fCourseBase, "layout");
		if(layoutDirectory.exists()) {
			ZipUtil.addPathToZip("layout", layoutDirectory.toPath(), zout);
		}
		File mediaDirectory = new File(fCourseBase, "media");
		if(mediaDirectory.exists()) {
			ZipUtil.addPathToZip("media", mediaDirectory.toPath(), zout);
		}
		File courseStylesDirectory = new File(fCourseBase, CourseStyleService.FOLDER_ROOT);
		if(courseStylesDirectory.exists()) {
			ZipUtil.addPathToZip(CourseStyleService.FOLDER_ROOT, courseStylesDirectory.toPath(), zout);
		}
		File documentsDirectory = new File(fCourseBase, CourseDocumentsFactory.FOLDER_NAME);
		if(documentsDirectory.exists()) {
			ZipUtil.addPathToZip(CourseDocumentsFactory.FOLDER_NAME, documentsDirectory.toPath(), zout);
		}
		File coachFilesDirectory = new File(fCourseBase, CoachFolderFactory.FOLDER_NAME);
		if(coachFilesDirectory.exists()) {
			ZipUtil.addPathToZip(CoachFolderFactory.FOLDER_NAME, coachFilesDirectory.toPath(), zout);
		}
		
		try {
			exportCoursefolder(sourceCourse, zout);
		} catch (IOException e) {
			log.error("Cannot zip course folder: {}", sourceCourse, e);
		}

		//make the folder structure
		File fExportedDataDir = new File(exportDirectory, ICourse.EXPORTED_DATA_FOLDERNAME);
		fExportedDataDir.mkdirs();
		
		exportBusinessGroupData(sourceCourse, fExportedDataDir, zout);

		// export any node data
		log.info("exportToFilesystem: exporting course exporting all nodes: {}", sourceCourse);
		TreeVisitor tv = new TreeVisitor(node -> exportNode(sourceCourse, fExportedDataDir, node, zout),
				sourceCourse.getEditorTreeModel().getRootNode(), true);
		tv.visitAll();
		log.info("exportToFilesystem: exporting all course nodes done: {}", sourceCourse);

		// export shared folder
		CourseConfig config = sourceCourse.getCourseConfig();
		if (config.hasCustomSharedFolder() && withLinkedReferences == RepositoryEntryImportExportLinkEnum.WITH_REFERENCE) {
			exportSharedFolder(config, sourceCourse, zout);
		}
		// export glossary
		if (config.hasGlossary() && withLinkedReferences == RepositoryEntryImportExportLinkEnum.WITH_REFERENCE) {
			exportGlossary(config, sourceCourse, fExportedDataDir, zout);
		}
		
		log.info("exportToFilesystem: exporting course configuration and repo data: {}", sourceCourse);

		exportRepositoryEntryMetadata(sourceCourse, zout);
		exportReminders(sourceCourse, zout);
		exportCertificatesConfigurations(sourceCourse, zout);
		exportCourseBadgeClasses(sourceCourse, zout);

		DBFactory.getInstance().commitAndCloseSession();
	}

	private void exportReminders(PersistingCourseImpl sourceCourse,  ZipOutputStream zout) {
		try {
			zout.putNextEntry(new ZipEntry(ZipUtil.concat(ICourse.EXPORTED_DATA_FOLDERNAME, ReminderService.REMINDERS_XML)));

			RepositoryEntry entry = sourceCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			CoreSpringFactory.getImpl(ReminderService.class).exportReminders(entry, zout);
			
			zout.closeEntry();
		} catch(Exception e) {
			log.error("", e);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	private void exportCertificatesConfigurations(PersistingCourseImpl sourceCourse,  ZipOutputStream zout) {
		try {
			zout.putNextEntry(new ZipEntry(ZipUtil.concat(ICourse.EXPORTED_DATA_FOLDERNAME, CourseHandler.EXPORT_CERTIFICATES_CONFIG)));

			RepositoryEntry entry = sourceCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			RepositoryEntryCertificateConfiguration configuration = CoreSpringFactory.getImpl(CertificatesManager.class).getConfiguration(entry);
			if(configuration != null) {
				String configurationXml = RepositoryEntryCertificateConfigurationXStream.toXML(configuration);
				zout.write(configurationXml.getBytes(StandardCharsets.UTF_8));
			}
			zout.closeEntry();
		} catch(Exception e) {
			log.error("", e);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}

	private void exportCourseBadgeClasses(PersistingCourseImpl sourceCourse, ZipOutputStream zout) {
		try {
			RepositoryEntry entry = sourceCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			OpenBadgesManager openBadgesManager = CoreSpringFactory.getImpl(OpenBadgesManager.class);

			// BadgeConfiguration.xml
			zout.putNextEntry(new ZipEntry(ZipUtil.concat(ICourse.EXPORTED_DATA_FOLDERNAME, CourseHandler.BADGE_CONFIGURATION_XML)));
			BadgeEntryConfiguration badgeConfiguration = openBadgesManager.getConfiguration(entry);
			if (badgeConfiguration != null) {
				String xmlString = BadgeEntryConfigurationXStream.toXML(badgeConfiguration);
				zout.write(xmlString.getBytes(StandardCharsets.UTF_8));
			}
			zout.closeEntry();
			log.info("badges: exported course badge configuration: {}", CourseHandler.BADGE_CONFIGURATION_XML);

			// Badge images
			BadgeClasses badgeClasses = new BadgeClasses(openBadgesManager.getBadgeClasses(entry).stream().map(openBadgesManager::badgeClassForExport).collect(Collectors.toList()));
			for (BadgeClass badgeClass : badgeClasses.getItems()) {
				VFSLeaf leaf = openBadgesManager.getBadgeClassVfsLeaf(badgeClass.getImage());
				if (leaf != null) {
					ZipUtil.exportFile(ICourse.EXPORTED_DATA_FOLDERNAME, leaf, zout);
					log.info("badges: exported image: {}", leaf.getName());
				}
			}

			// BadgeClasses.xml
			zout.putNextEntry(new ZipEntry(ZipUtil.concat(ICourse.EXPORTED_DATA_FOLDERNAME, CourseHandler.BADGE_CLASSES_XML)));
			String xmlString = BadgeClassesXStream.toXML(badgeClasses);
			zout.write(xmlString.getBytes(StandardCharsets.UTF_8));
			zout.closeEntry();
			log.info("badges: exported course badge classe: {}", CourseHandler.BADGE_CLASSES_XML);

		} catch(Exception e) {
			log.error("", e);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}

	private void exportRepositoryEntryMetadata(PersistingCourseImpl sourceCourse, ZipOutputStream zout) {
		try {
			RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(sourceCourse, true);
			RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(entry, null);
			importExport.exportDoExportProperties(ICourse.EXPORTED_DATA_FOLDERNAME, zout);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void exportGlossary(CourseConfig config, PersistingCourseImpl sourceCourse, File fExportedDataDir, ZipOutputStream zout) {
		File glossaryExportDataDir = new File(fExportedDataDir, "glossary");
		try {
			glossaryExportDataDir.mkdir();
		
			log.info("exportToFilesystem: exporting course glossary: {}", sourceCourse);
			final GlossaryManager glossaryManager = CoreSpringFactory.getImpl(GlossaryManager.class);
			final CourseConfigManager courseConfigManager = CoreSpringFactory.getImpl(CourseConfigManager.class);
			if (!glossaryManager.exportGlossary(config.getGlossarySoftKey(), glossaryExportDataDir)) {
				// export failed, delete reference to glossary in the course config
				log.info("exportToFilesystem: export of glossary failed.");
				config.setGlossarySoftKey(null);
				courseConfigManager.saveConfigTo(sourceCourse, config);
			}
			log.info("exportToFilesystem: exporting course glossary done: {}", sourceCourse);
			
			ZipUtil.addDirectoryToZip(glossaryExportDataDir.toPath(), ICourse.EXPORTED_DATA_FOLDERNAME, zout);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			FileUtils.deleteDirsAndFiles(glossaryExportDataDir, true, true);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void exportSharedFolder(CourseConfig config, PersistingCourseImpl sourceCourse, ZipOutputStream zout) {
		try {
			log.info("exportToFilesystem: exporting shared folder course: {}", sourceCourse);
			String exportPath = ICourse.EXPORTED_DATA_FOLDERNAME + "/" + "sharedfolder";
			if (!SharedFolderManager.getInstance().exportSharedFolder(config.getSharedFolderSoftkey(), exportPath, zout)) {
				// export failed, delete reference to shared folder in the course config
				log.info("exportToFilesystem: export of shared folder failed.");
				config.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				CoreSpringFactory.getImpl(CourseConfigManager.class).saveConfigTo(sourceCourse, config);
			}
			log.info("exportToFilesystem: exporting shared folder course done: {}", sourceCourse);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void exportCoursefolder(PersistingCourseImpl sourceCourse, ZipOutputStream zout) throws IOException {
		VFSContainer courseFolder = sourceCourse.getIsolatedCourseBaseContainer();
		List<VFSItem> hasChildren = courseFolder.getItems(new VFSSystemItemFilter());
		if(hasChildren != null && !hasChildren.isEmpty()) {
			zout.putNextEntry(new ZipEntry("oocoursefolder.zip"));
			// export course folder
			try(OutputStream shieldedStream = new ShieldOutputStream(zout);
					ZipOutputStream exportStream = new ZipOutputStream(shieldedStream)) {
				for(VFSItem child:hasChildren) {
					ZipUtil.addToZip(child, "", exportStream, new VFSSystemItemFilter(), true);
				}
			} catch(Exception e) {
				log.error("", e);
			}
			zout.closeEntry();
		}			
	}
	
	private void exportBusinessGroupData(PersistingCourseImpl sourceCourse, File fExportedDataDir, ZipOutputStream zout) {
		File groupExportDataDir = new File(fExportedDataDir, "groups");
		try {
			groupExportDataDir.mkdir();
			//export business groups
			CourseEnvironmentMapper envMapper = sourceCourse.getCourseEnvironment().getCourseGroupManager().getBusinessGroupEnvironment();
			sourceCourse.getCourseEnvironment().getCourseGroupManager().exportCourseBusinessGroups(groupExportDataDir, envMapper);
			ZipUtil.addDirectoryToZip(groupExportDataDir.toPath(), ICourse.EXPORTED_DATA_FOLDERNAME, zout);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			FileUtils.deleteDirsAndFiles(groupExportDataDir, true, true);
			DBFactory.getInstance().commitAndCloseSession();
		}	
	}
	
	private void exportNode(PersistingCourseImpl sourceCourse, File fExportedDataDir, INode node, ZipOutputStream zout) {
		CourseEditorTreeNode cNode = (CourseEditorTreeNode) node;
		CourseNode courseNode = cNode.getCourseNode();
		if(courseNode instanceof ScormCourseNode
				|| courseNode instanceof CPCourseNode) {
			exportCPCourseNode(courseNode, zout);
		} else if(courseNode instanceof VideoCourseNode) {
			exportVideoCourseNode((VideoCourseNode)courseNode, zout);
		} else if(courseNode instanceof BCCourseNode) {
			exportBCCourseNode(sourceCourse, (BCCourseNode)courseNode, zout);
		} else {
			exportCourseNode(sourceCourse, fExportedDataDir, courseNode, zout);
		}	
	}

	private void exportCourseNode(PersistingCourseImpl sourceCourse, File fExportedDataDir, CourseNode courseNode, ZipOutputStream zout) {
		File nodeExportDataDir = new File(fExportedDataDir, "node_" + courseNode.getIdent());
		try {
			nodeExportDataDir.mkdir();
			
			courseNode.exportNode(nodeExportDataDir, sourceCourse, withLinkedReferences);
			ZipUtil.addDirectoryToZip(nodeExportDataDir.toPath(), ICourse.EXPORTED_DATA_FOLDERNAME, zout);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			FileUtils.deleteDirsAndFiles(nodeExportDataDir, true, true);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void exportBCCourseNode(PersistingCourseImpl sourceCourse, BCCourseNode courseNode, ZipOutputStream zout) {
		if(courseNode.isSharedFolder()) return;
		
		try(ShieldOutputStream fOut = new ShieldOutputStream(zout)) {
			VFSContainer nodeContainer = VFSManager.olatRootContainer(BCCourseNode.getFoldernodePathRelToFolderBase(sourceCourse.getCourseEnvironment(), courseNode), null);
	
			String nodeDirectory = ZipUtil.concat(ICourse.EXPORTED_DATA_FOLDERNAME, courseNode.getIdent());
			zout.putNextEntry(new ZipEntry(ZipUtil.concat(nodeDirectory, "oonode.zip")));
			
			ZipUtil.zip(nodeContainer, fOut, new VFSRevisionsAndThumbnailsFilter(), true);
			
			zout.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	/**
	 * Export video course node 
	 * @param courseNode
	 * @param zout
	 */
	private void exportVideoCourseNode(VideoCourseNode courseNode, ZipOutputStream zout) {
		try {
			RepositoryEntry videoEntry = VideoEditController.getVideoReference(courseNode.getModuleConfiguration(), false);
			exportCourseNodeWithRepositoryEntry(videoEntry, courseNode, zout);
		} catch(Exception e) {
			log.error("", e);
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	/**
	 * Export course node with IMS CP or SCORM packages
	 * @param courseNode
	 * @param zout
	 */
	private void exportCPCourseNode(CourseNode courseNode, ZipOutputStream zout) {
		try {
			RepositoryEntry scormEntry = CPEditController.getCPReference(courseNode.getModuleConfiguration(), false);
			exportCourseNodeWithRepositoryEntry(scormEntry, courseNode, zout);
		} catch(Exception e) {
			log.error("", e);
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void exportCourseNodeWithRepositoryEntry(RepositoryEntry entry, CourseNode courseNode, ZipOutputStream zout) {
		if(entry != null) {
			RepositoryEntryImportExport reie = new RepositoryEntryImportExport(entry, null);
			reie.exportDoExport(ZipUtil.concat(ICourse.EXPORTED_DATA_FOLDERNAME, courseNode.getIdent()), zout, withLinkedReferences);
		}
	}
}
