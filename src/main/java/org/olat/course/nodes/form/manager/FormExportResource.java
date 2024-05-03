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
package org.olat.course.nodes.form.manager;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;

/**
 * 
 * Initial date: 2 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FormExportResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(FormExportResource.class);
	
	private final EvaluationFormManager evaluationFormManager;
	private final String nodeName;
	private final SessionFilter filter;
	private final EvaluationFormExcelExport excelExport;
	private final List<String> fileUploadIds;

	public FormExportResource(EvaluationFormManager evaluationFormManager, String nodeName, SessionFilter filter,
			EvaluationFormExcelExport excelExport, List<FileUpload> fileUploads) {
		this.evaluationFormManager = evaluationFormManager;
		this.nodeName = nodeName;
		this.filter = filter;
		this.excelExport = excelExport;
		this.fileUploadIds = fileUploads.stream().map(FileUpload::getId).toList();
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
		String urlEncodedLabel = new StringBuilder()
			.append(StringHelper.transformDisplayNameToFileSystemName(nodeName))
			.append("_")
			.append(Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())))
			.append(".zip")
		.toString();
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			if (excelExport != null) {
				excelExport.export(zout, "/");
			}
			
			List<EvaluationFormSession> sessions = evaluationFormManager.loadSessionsFiltered(filter, 0, -1);
			if (sessions != null && !sessions.isEmpty()) {
				EvaluationFormResponses responses = evaluationFormManager.loadResponsesBySessions(filter);
				
				for (EvaluationFormSession session : sessions) {
					List<VFSLeaf> vfsLeafs = fileUploadIds.stream()
							.map(id -> responses.getResponse(session, id))
							.filter(Objects::nonNull)
							.map(response -> evaluationFormManager.loadResponseLeaf(response))
							.filter(Objects::nonNull)
							.toList();
					
					if (!vfsLeafs.isEmpty()) {
						String filesPath = "files/";
						if (session.getParticipation() != null && session.getParticipation().getExecutor() != null) {
							Identity executor = session.getParticipation().getExecutor();
							User user = executor.getUser();
							String name = user.getLastName()
									+ "_" + user.getFirstName()
									+ "_" + (StringHelper.containsNonWhitespace(user.getNickName()) ? user.getNickName() : executor.getName());
							
							filesPath += StringHelper.transformDisplayNameToFileSystemName(name);
						} else {
							filesPath += session.getKey();
						}
						
						Set<String> uniqueFileNames = new HashSet<>(vfsLeafs.size());
						for (VFSLeaf vfsLeaf : vfsLeafs) {
							String filename = vfsLeaf.getName();
							if (uniqueFileNames.contains(filename)) {
								String filenameEnd = "_" + Encoder.md5hash(vfsLeaf.getRelPath());
								filename = FileUtils.appendAtTheEndOfFilename(filename, filenameEnd);
							}
							uniqueFileNames.add(filename);
							
							String filenameInZip = filesPath + "/" + filename;
							
							InputStream inputStream = vfsLeaf.getInputStream();
							try {
								zout.putNextEntry(new ZipEntry(filenameInZip));
								FileUtils.copy(inputStream, zout);
								zout.closeEntry();
							} catch(Exception e) {
								log.error("Error during export of file {} of form course node {}", vfsLeaf.getName(), nodeName, e);
							} finally {
								FileUtils.closeSafely(inputStream);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error during export of form course node {}", nodeName, e);
		}
	}

	@Override
	public void release() {
		//
	}

}
