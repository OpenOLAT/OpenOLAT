/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.archiver.wizard;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.AbstractExportTask;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.manager.PersistentTaskProgressCallback;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.CourseArchiveListController;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.archiver.wizard.CourseArchiveContext.LogSettings;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.statistic.export.SimpleLogExporter;
import org.olat.ims.qti21.resultexport.QTI21ResultsExport;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 19 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveExportTask extends AbstractExportTask {

	private static final long serialVersionUID = -6025539405700360199L;
	private static final Logger log = Tracing.createLoggerFor(CourseArchiveExportTask.class);
	private static final String encoding = "UTF-8";
	
	private transient VFSLeaf exportZip;

	private String title;
	private Locale locale;
	private String description;
	private OLATResourceable courseRes;
	private CourseArchiveOptions options;

	private Set<String> usedPath = new HashSet<>();
	
	public CourseArchiveExportTask() {
		//
	}
	
	public CourseArchiveExportTask(CourseArchiveOptions options, OLATResourceable courseRes, Locale locale) {
		this.title = options.getTitle();
		this.courseRes = courseRes;
		this.options = options;
		this.locale = locale;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public VFSLeaf getExportZip() {
		return exportZip;
	}

	@Override
	public void run() {
		
		log.debug("Export course archive: {}", title);
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
		
		ICourse course = CourseFactory.loadCourse(courseRes.getResourceableId());
		VFSContainer subFolder = exportManager
				.getExportContainer(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), CourseArchiveListController.COURSE_ARCHIVE_SUB_IDENT);
		
		ExportMetadata metadata = exportManager.getExportMetadataByTask((PersistentTask)task);
		String vfsName = metadata.getFilename();
		exportZip = subFolder.createChildLeaf(vfsName);
		if(exportZip == null) {
			vfsName = VFSManager.rename(subFolder, vfsName);
			exportZip = subFolder.createChildLeaf(vfsName);
		} else {
			String metadataDescr = Formatter.truncate(description, 31000);
			fillMetadata(exportZip, title, metadataDescr, metadata.getExpirationDate());
		}
		
		if(task.getStatus() == TaskStatus.cancelled) {
			if(exportZip != null) {
				exportZip.deleteSilently();
			}
			return;
		}
		metadata.setFilename(exportZip.getName());
		metadata.setFilePath(exportZip.getRelPath());
		metadata.setMetadata(exportZip.getMetaInfo());
		metadata = exportManager.updateMetadata(metadata);

		List<String> courseNodesIdents = options.getCourseNodesIdents();
		if(courseNodesIdents == null) {
			courseNodesIdents = getAllCourseNodes(course);
		}
		final int numOfCourseNodes = courseNodesIdents.size();
		
		TaskExecutorManager taskExecutorManager = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		NodesProgress progress = new NodesProgress(task, courseNodesIdents.size(), taskExecutorManager);
		if(!StringHelper.containsNonWhitespace(task.getCheckpoint())) {
			progress.setProgress(0.0d, null);
		}

		ArchiveOptions nodeOptions = new ArchiveOptions();
		nodeOptions.setDoer(task.getCreator());
		nodeOptions.setWithPdfs(options.isResultsWithPDFs());
		nodeOptions.setExportFormat(options.getQTI21ExportFormat());

		List<Identity> participants = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment(), nodeOptions);
		nodeOptions.setIdentities(participants);

		try(OutputStream out=exportZip.getOutputStream(true);
				ZipOutputStream zout = new ZipOutputStream(out)) {
			for(int i=0; i<numOfCourseNodes; i++) {
				String courseNodeIdent = courseNodesIdents.get(i);
				progress.setNodeProgress(i, courseNodeIdent);
				
				CourseNode courseNode = course.getRunStructure().getNode(courseNodeIdent);
				if(courseNode != null) {
					archive(course, courseNode, numOfCourseNodes, nodeOptions, progress, zout);
				}
			}
			
			if (options.isLogFilesAuthors()) {
				exportCourseLogs(course, org.olat.course.statistic.ExportManager.FILENAME_ADMIN_LOG, true, false, zout);
			}
			if (options.isLogFilesUsers()) {
				exportCourseLogs(course, org.olat.course.statistic.ExportManager.FILENAME_USER_LOG, false, false, zout);
			}
			if (options.isLogFilesStatistics()) {
				exportCourseLogs(course, org.olat.course.statistic.ExportManager.FILENAME_STATISTIC_LOG, false, true, zout);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		
		// Reload the metadata
		TaskStatus status = taskExecutorManager.getStatus(task);
		if(status == TaskStatus.cancelled) {
			exportZip.deleteSilently();
			taskExecutorManager.delete(task);
		} else {
			vfsRepositoryService.getMetadataFor(exportZip);
			sendMail(course, metadata, task.getCreator());
		}
	}
	
	private void exportCourseLogs(ICourse course, String name, boolean resourceAdminAction, boolean anonymize, ZipOutputStream zout) {
		SimpleLogExporter exporter = CoreSpringFactory.getImpl(SimpleLogExporter.class);
		try(OutputStream shieldOut = new ShieldOutputStream(zout)) {
			zout.putNextEntry(new ZipEntry(name));
			
			exporter.exportCourseLog(shieldOut, course.getResourceableId(), options.getLogFilesStartDate(), options.getLogFilesEndDate(),
					resourceAdminAction, anonymize, options.getLogSettings() == LogSettings.PERSONALISED);	

			shieldOut.flush();
			zout.closeEntry();
		} catch(Exception e) {
			log.error("Cannot export course logs: {}", name, e);
		}
	}
	
	private void archive(ICourse course, CourseNode courseNode, int numOfCourseNodes,
			ArchiveOptions nodeOptions, PersistentTaskProgressCallback progress, ZipOutputStream zout) {
		try {
			String nodePath = getNodePath(courseNode, numOfCourseNodes);
			if(courseNode instanceof IQTESTCourseNode testNode) {
				QTI21ResultsExport export = new QTI21ResultsExport(course.getCourseEnvironment(),
						nodeOptions.getIdentities(), false, options.isResultsWithPDFs(), testNode, nodePath, locale,
						task.getCreator(), new WindowControlMocker());

				export.exportTestResults(zout, progress);
				export.exportExcelResults(zout);
			} else {
				courseNode.archiveNodeData(locale, course, nodeOptions, zout, nodePath, encoding);
			}
		} catch (IOException e) {
			log.error("Error during course archive of: {} ({}) and node {} ({})", course.getCourseTitle(), course.getResourceableId(),
					courseNode.getLongTitle(), courseNode.getIdent(), e);
		}
	}
	
	private String getNodePath(CourseNode courseNode, int numOfCourseNodes) {
		String nodePath = "";
		if(numOfCourseNodes > 1) {
			nodePath = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName());
			if(!StringHelper.containsNonWhitespace(nodePath)) {
				nodePath = courseNode.getType() + "_" + courseNode.getIdent();
			}
			if(usedPath.contains(nodePath)) {
				nodePath += "_" + courseNode.getIdent();
			}
			usedPath.add(nodePath);
		}
		return nodePath;
	}
	
	private void sendMail(ICourse course, ExportMetadata metadata, Identity creator) {
		Translator translator = Util.createPackageTranslator(CourseArchiveExportTask.class, locale);
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		try {
			String email = creator.getUser().getEmail();
			MailBundle bundle = new MailBundle();
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setTo(email);

			RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			List<ContextEntry> cEntries = BusinessControlFactory.getInstance()
					.createCEListFromString("[RepositoryEntry:" + entry.getKey() + "][Participants:0][Export:0]");
			String url = BusinessControlFactory.getInstance().getAsURIString(cEntries, true);

			String[] args = {
					metadata.getTitle(),
					url,
					userManager.getUserDisplayName(creator)
				};

			String subject = translator.translate("export.notification.subject", args);
			String body = translator.translate("export.notification.body", args);
			bundle.setContent(subject, body);

			CoreSpringFactory.getImpl(MailManager.class).sendMessage(bundle);
		} catch (Exception e) {
			log.error("Error sending information email to user that file was saved successfully.", e);
		}
	}
	
	public static class NodesProgress extends PersistentTaskProgressCallback {

		private int currentCourseNode = 0;
		private final int numOfCourseNodes;
		
		public NodesProgress(Task task, int numOfCourseNodes, TaskExecutorManager taskExecutorManager) {
			super(task, taskExecutorManager);
			this.numOfCourseNodes = numOfCourseNodes;
		}
		
		public void setNodeProgress(int currentCourseNode, String checkpoint) {
			this.currentCourseNode = currentCourseNode;
			double progress = currentCourseNode / (double)numOfCourseNodes;
			super.setProgress(progress, checkpoint);
		}

		@Override
		public void setProgress(double progress, String checkpoint) {
			double totalProgress = currentCourseNode / (double)numOfCourseNodes;
			double nodeProgress = progress / numOfCourseNodes;
			super.setProgress(totalProgress + nodeProgress, checkpoint);
		}
	}
	
	public static String getArchiveName(RepositoryEntry courseEntry, ArchiveType type, Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseArchiveExportTask.class, locale);
		
		StringBuilder sb = new StringBuilder();
		sb.append(courseEntry.getDisplayname());
		if(StringHelper.containsNonWhitespace(courseEntry.getExternalRef())) {
			sb.append(" - ").append(courseEntry.getExternalRef());
		}

		if(type == ArchiveType.COMPLETE) {
			sb.append(" - ").append(translator.translate("archive.types.complete"));
		} else if(type == ArchiveType.PARTIAL) {
			sb.append(" - ").append(translator.translate("archive.types.partial"));
		}
		
		String date = Formatter.getInstance(locale).formatDate(new Date());
		sb.append(" (").append(date).append(").zip");
		
		return sb.toString();
	}
	
	public static String getFilename(String title) {
		String cleanUpName;
		int lastIndex = title.lastIndexOf('.');
		if(lastIndex >= 0) {
			cleanUpName = StringHelper.transformDisplayNameToFileSystemName(title.substring(0, lastIndex));
			cleanUpName += title.substring(lastIndex);
		} else {
			cleanUpName = StringHelper.transformDisplayNameToFileSystemName(title);
		}
		cleanUpName = cleanUpName.replace("__", "_");
		return cleanUpName;
	}
	
	public static String getDescription(CourseArchiveOptions archiveOptions, RepositoryEntry entry, Locale locale) {
		ICourse course = CourseFactory.loadCourse(entry.getOlatResource());

		List<CourseNode> courseNodes = new ArrayList<>();
		new TreeVisitor(node -> {
			if(node instanceof CourseNode cNode && CourseArchiveContext.acceptCourseElement(cNode)) {
				courseNodes.add(cNode);
			}
		}, course.getRunStructure().getRootNode(), false).visitAll();
		return getDescription(archiveOptions,  courseNodes,  locale);
	}
	
	public static List<String> getAllCourseNodes(ICourse course) {
		List<String> courseNodesIdents = new ArrayList<>();
		new TreeVisitor(node -> {
			if(node instanceof CourseNode cNode && CourseArchiveContext.acceptCourseElement(cNode)) {
				courseNodesIdents.add(cNode.getIdent());
			}
		}, course.getRunStructure().getRootNode(), false).visitAll();
		return courseNodesIdents;
	}
	
	public static String getDescription(CourseArchiveOptions archiveOptions, List<CourseNode> courseNodes, Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseArchiveExportTask.class, locale);
		List<String> objects = new ArrayList<>();
		if(archiveOptions.isLogFiles()) {
			objects.add(translator.translate("log.files.options"));
		}
		if(archiveOptions.isCourseResults()) {
			objects.add(translator.translate("others.objects.course.results"));
		}
		if(archiveOptions.isCourseChat()) {
			objects.add(translator.translate("others.objects.course.chat"));
		}

		List<String> nodes = new ArrayList<>();
		if(courseNodes != null) {
			for(CourseNode node:courseNodes) {
				nodes.add(StringHelper.escapeHtml(node.getShortTitle()));
			}
		}

		StringBuilder sb = new StringBuilder();
		if(!objects.isEmpty()) {
			sb.append(String.join(", ", objects));
		}
		if(!nodes.isEmpty()) {
			if(!objects.isEmpty()) {
				sb.append(" | ");
			}
			
			String i18nKey = nodes.size() == 1 ? "archive.node" : "archive.nodes";
			sb.append(translator.translate(i18nKey, Integer.toString(nodes.size()))).append(": ")
			  .append(String.join(", ", nodes));
		}
		
		return sb.toString();
	}
}
