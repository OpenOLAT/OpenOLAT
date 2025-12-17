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
package org.olat.course.assessment.ui.tool;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.AbstractExportTask;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
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
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti21.resultexport.QTI21ResultsExportTask;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: Dec 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentCourseNodeExportTask extends AbstractExportTask {

	private static final long serialVersionUID = -9021665955749689528L;
	private static final Logger log = Tracing.createLoggerFor(AssessmentCourseNodeExportTask.class);

	protected final String title;
	protected final String description;
	protected final List<Long> identitiesKeys;
	
	protected final OLATResourceable courseRes;
	protected final CourseNode courseNode;
	protected final Locale locale;

	protected final boolean withPdfs;
	protected final boolean withNonParticipants;
	
	private VFSLeaf exportZip;
	
	public AssessmentCourseNodeExportTask(OLATResourceable courseRes, CourseNode courseNode, IdentitiesList identities,
			String title, String description, boolean withPdfs, Locale locale) {
		this.courseNode = courseNode;
		this.courseRes = OresHelper.clone(courseRes);
		identitiesKeys = identities.getIdentities().stream()
				.map(Identity::getKey)
				.collect(Collectors.toList());
		this.withNonParticipants = identities.isWithNonParticipants();
		this.withPdfs = withPdfs;
		this.title = title;
		this.locale = locale;
		this.description = description;
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public VFSLeaf getExportZip() {
		return exportZip;
	}

	public String getDescription() {
		return description;
	}
	
	@Override
	public boolean isDelayed() {
		return false;
	}

	@Override
	public Queue getExecutorsQueue() {
		return withPdfs ? Queue.standard : Queue.external;
	}
	
	@Override
	public void run() {
		log.debug("Export results: {}", title);
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
		
		ICourse course = CourseFactory.loadCourse(courseRes.getResourceableId());
		VFSContainer subFolder = exportManager
				.getExportContainer(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
		
		ExportMetadata metadata = exportManager.getExportMetadataByTask((PersistentTask)task);
		String vfsName = metadata.getFilename();
		exportZip = subFolder.createChildLeaf(vfsName);
		if(exportZip == null) {
			vfsName = VFSManager.rename(subFolder, vfsName);
			exportZip = subFolder.createChildLeaf(vfsName);
		} else {
			String metadataDescr = Formatter.truncate(description, 31000);
			Date expirationDate = CalendarUtils.endOfDay(DateUtils.addDays(new Date(), 10));
			fillMetadata(exportZip, title, metadataDescr, expirationDate);
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
		
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		List<Identity> identities = securityManager.loadIdentityByKeys(identitiesKeys);
		
		TaskExecutorManager taskExecutorManager = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		PersistentTaskProgressCallback progress = new PersistentTaskProgressCallback(task, taskExecutorManager);
		if(!StringHelper.containsNonWhitespace(task.getCheckpoint())) {
			progress.setProgress(0.0d, null);
		}
		
		AssessmentCourseNodeExport export = createExport(course, identities);
		if (export == null) {
			return;
		}
		
		try(OutputStream out=exportZip.getOutputStream(true);
				ZipOutputStream zout = new ZipOutputStream(out)) {
			if(StringHelper.containsNonWhitespace(task.getCheckpoint())) {
				export.setStartPoint(task.getCheckpoint());
			}
			export.export(zout, progress);
		} catch(Exception e) {
			log.error("", e);
		}
		
		// Reload the metadata
		TaskStatus status = taskExecutorManager.getStatus(task);
		if(export.isCancelled() || status == TaskStatus.cancelled) {
			exportZip.deleteSilently();
			taskExecutorManager.delete(task);
		} else {
			vfsRepositoryService.getMetadataFor(exportZip);
			sendMail(course, courseNode);
		}
	}

	protected AssessmentCourseNodeExport createExport(ICourse course, List<Identity> identities) {
		return new AssessmentCourseNodeExport(task.getCreator(), course.getCourseEnvironment(), courseNode,
				identities, withNonParticipants, withPdfs, locale, new WindowControlMocker());
	}
	
	private void sendMail(ICourse course, CourseNode courseNode) {
		Translator translator = Util.createPackageTranslator(QTI21ResultsExportTask.class, locale);
		try {
			String email = task.getCreator().getUser().getEmail();
			MailBundle bundle = new MailBundle();
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setTo(email);
			
			RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			List<ContextEntry> cEntries = BusinessControlFactory.getInstance()
					.createCEListFromString("[RepositoryEntry:" + entry.getKey() + "][CourseNode:" + courseNode.getIdent() + "][Participants:0][Export:0]");
			String url = BusinessControlFactory.getInstance().getAsURIString(cEntries, true);

			String[] args = { title, url };

			String subject = translator.translate("export.notification.subject", args);
			String body = translator.translate("export.notification.body", args);
			bundle.setContent(subject, body);

			CoreSpringFactory.getImpl(MailManager.class).sendMessage(bundle);
		} catch (Exception e) {
			log.error("Error sending information email to user that file was saved successfully.", e);
		}
	}

}
