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
package org.olat.modules.forms.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Zips the export of one or more evaluation forms: the Excel export of each
 * form, the files uploaded in the sessions and, if a print provider is given
 * and the PDF module is enabled, a PDF of every session.
 *
 * Initial date: 8 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EvaluationFormExportResource implements MediaResource {

	private static final Logger log = Tracing.createLoggerFor(EvaluationFormExportResource.class);
	
	private final WindowControl wControl;
	private final Identity doer;
	private final String fileName;
	private final List<FormExportInfos> exportInfos;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	
	public EvaluationFormExportResource(WindowControl wControl, Identity doer, String fileName,
			List<FormExportInfos> exportInfos) {
		this.wControl = wControl;
		this.doer = doer;
		this.fileName = fileName;
		this.exportInfos = exportInfos;
		
		CoreSpringFactory.autowireObject(this);
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}
	
	@Override
	public boolean acceptRanges() {
		return false;
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
	public void prepare(HttpServletResponse hres) {
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
		hres.setHeader("Content-Description", fileName);
		
		try (ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			for (FormExportInfos infos : exportInfos) {
				exportForm(zout, infos);
			}
		} catch (Exception e) {
			log.error("Error during export of evaluation forms {}", fileName, e);
		}
	}
	
	private void exportForm(ZipOutputStream zout, FormExportInfos infos) throws IOException {
		if (infos.excelExport() != null) {
			infos.excelExport().export(zout, infos.path());
		}
		
		List<EvaluationFormSession> sessions = evaluationFormManager.loadSessionsFiltered(infos.filter(), 0, -1);
		if (sessions == null || sessions.isEmpty()) {
			return;
		}
		
		List<String> fileUploadIds = getFileUploadIds(infos.form());
		EvaluationFormResponses responses = evaluationFormManager.loadResponsesBySessions(infos.filter());
		boolean withPdf = pdfModule.isEnabled() && infos.printProvider() != null;
		
		for (EvaluationFormSession session : sessions) {
			List<VFSLeaf> vfsLeafs = fileUploadIds.stream()
					.map(id -> responses.getResponse(session, id))
					.filter(Objects::nonNull)
					.map(response -> evaluationFormManager.loadResponseLeaf(response))
					.filter(Objects::nonNull)
					.toList();
			
			if (!withPdf && vfsLeafs.isEmpty()) {
				continue;
			}
			
			boolean executorAvailable = session.getParticipation() != null && session.getParticipation().getExecutor() != null;
			String sessionPath = ZipUtil.concat(infos.path(), "files/" + getSessionFolder(infos, session, executorAvailable));
			
			if (withPdf && executorAvailable) {
				exportSessionPdf(zout, sessionPath, session, infos.printProvider());
			}
			exportSessionFiles(zout, sessionPath, vfsLeafs);
		}
	}
	
	private List<String> getFileUploadIds(Form form) {
		return evaluationFormManager.getUncontainerizedElements(form).stream()
				.filter(FileUpload.class::isInstance)
				.map(element -> element.getId())
				.toList();
	}
	
	private String getSessionFolder(FormExportInfos infos, EvaluationFormSession session, boolean executorAvailable) {
		if (!executorAvailable) {
			return String.valueOf(session.getKey());
		}
		
		Identity executor = session.getParticipation().getExecutor();
		User user = executor.getUser();
		String name = user.getLastName()
				+ "_" + user.getFirstName()
				+ "_" + (StringHelper.containsNonWhitespace(user.getNickName()) ? user.getNickName() : executor.getName());
		
		String folder = StringHelper.transformDisplayNameToFileSystemName(name);
		if (infos.withDateFolder()) {
			folder += "/" + Formatter.formatDatetimeFilesystemSave(session.getSubmissionDate());
		}
		return folder;
	}
	
	private void exportSessionFiles(ZipOutputStream zout, String sessionPath, List<VFSLeaf> vfsLeafs) {
		Set<String> uniqueFileNames = new HashSet<>(vfsLeafs.size());
		for (VFSLeaf vfsLeaf : vfsLeafs) {
			String leafName = vfsLeaf.getName();
			if (uniqueFileNames.contains(leafName)) {
				leafName = FileUtils.appendAtTheEndOfFilename(leafName, "_" + Encoder.md5hash(vfsLeaf.getRelPath()));
			}
			uniqueFileNames.add(leafName);
			
			InputStream inputStream = vfsLeaf.getInputStream();
			try {
				zout.putNextEntry(new ZipEntry(sessionPath + "/" + leafName));
				FileUtils.copy(inputStream, zout);
				zout.closeEntry();
			} catch (Exception e) {
				log.error("Error during export of file {} of evaluation form", vfsLeaf.getName(), e);
			} finally {
				FileUtils.closeSafely(inputStream);
			}
		}
	}
	
	private void exportSessionPdf(ZipOutputStream zout, String sessionPath, EvaluationFormSession session,
			SessionPrintProvider printProvider) {
		try {
			ControllerCreator printControllerCreator = printProvider.create(session);
			if (printControllerCreator == null) {
				return;
			}
			
			zout.putNextEntry(new ZipEntry(sessionPath + "/form.pdf"));
			pdfService.convert(doer, printControllerCreator, wControl, PdfOutputOptions.defaultOptions(), zout);
			zout.closeEntry();
		} catch (Exception e) {
			log.error("Error during export of the PDF of an evaluation form session {}", session.getKey(), e);
		}
	}
	
	@Override
	public void release() {
		//
	}
	
	/**
	 * @param path Folder inside the zip, empty for the root of the zip.
	 * @param withDateFolder Adds the submission date as an additional folder,
	 *            needed if one participant can have several sessions.
	 * @param printProvider Creates the printed PDF of a session, null if the
	 *            export has no PDF.
	 */
	public record FormExportInfos(EvaluationFormExcelExport excelExport, Form form, SessionFilter filter, String path,
			boolean withDateFolder, SessionPrintProvider printProvider) {
	}
	
	@FunctionalInterface
	public interface SessionPrintProvider {
	
		ControllerCreator create(EvaluationFormSession session);
	
	}

}
