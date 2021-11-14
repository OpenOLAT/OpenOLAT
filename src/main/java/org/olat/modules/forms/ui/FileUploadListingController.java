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
package org.olat.modules.forms.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.ReportHelper.Legend;
import org.olat.modules.forms.ui.model.ResponseDataSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileUploadListingController extends BasicController {


	private final VelocityContainer mainVC;
	private Link downloadLink;
	
	private final ResponseDataSource dataSource;
	private final ReportHelper reportHelper;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public FileUploadListingController(UserRequest ureq, WindowControl wControl, ResponseDataSource dataSource,
			ReportHelper reportHelper) {
		super(ureq, wControl);
		this.dataSource = dataSource;
		this.reportHelper = reportHelper;
		
		mainVC = createVelocityContainer("file_upload_listing");
		
		Long showCount = dataSource.getLimitedResponsesCount();
		if (showCount > 0) {
			Long showAll = dataSource.getAllResponsesCount();
			if (showAll > showCount) {
				mainVC.contextPut("downloadInfo", translate("file.upload.download.info", new String[] { showCount.toString(), showAll.toString() }));
			}
			downloadLink = LinkFactory.createLink("file.upload.download.link", mainVC, this);
		} else {
			mainVC.contextPut("noText", translate("file.upload.no.text"));
		}
		
		List<FileUploadListingWrapper> wrappers = createWrappers(ureq);
		mainVC.contextPut("wrappers", wrappers);
		putInitialPanel(mainVC);
	}
	
	private List<FileUploadListingWrapper> createWrappers(UserRequest ureq) {
		List<FileUploadListingWrapper> wrappers = new ArrayList<>();
		List<EvaluationFormResponse> responses = dataSource.getLimitedResponses();
		for (EvaluationFormResponse response: responses) {
			FileUploadListingWrapper wrapper = createWrapper(ureq, response);
			wrappers.add(wrapper);
		}
		return wrappers;
	}
	
	private FileUploadListingWrapper createWrapper(UserRequest ureq, EvaluationFormResponse response) {
		EvaluationFormSession session = response.getSession();
		Legend legend = reportHelper.getLegend(session);
		String filename = response.getStringuifiedResponse();
		String filesize = null;
		String mapperUri = null;
		String iconCss = null;
		String thumbUri = null;
		VFSLeaf leaf = evaluationFormManager.loadResponseLeaf(response);
		if (leaf != null) {
			filename = leaf.getName();
			filesize = Formatter.formatBytes((leaf).getSize());
			mapperUri = registerCacheableMapper(ureq, "file-upload-" + CodeHelper.getRAMUniqueID() + "-" + leaf.getLastModified(), new VFSMediaMapper(leaf));
			iconCss = CSSHelper.createFiletypeIconCssClassFor(leaf.getName());

			VFSLeaf thumb = vfsRepositoryService.getThumbnail(leaf, 200, 200, false);
			if (thumb != null) {
				thumbUri = registerCacheableMapper(ureq, "file-upload-thumb" + CodeHelper.getRAMUniqueID() + "-" + leaf.getLastModified(), new VFSMediaMapper(thumb));
			}
		}
		return new FileUploadListingWrapper(legend.getColor(), legend.getName(), filename, filesize, mapperUri, iconCss, thumbUri);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == downloadLink) {
			doExport(ureq);
		}
	}
	
	private void doExport(UserRequest ureq) {
		List<EvaluationFormResponse> responses = dataSource.getAllResponses();
		File tmpDir = evaluationFormManager.createTmpDir();
		evaluationFormManager.copyFilesTo(responses, tmpDir);
		String name = "survey_files";
		ZippedDirectoryMediaResource zipResource = new ZippedDirectoryMediaResource(name, tmpDir);
		ureq.getDispatchResult().setResultingMediaResource(zipResource);
	}

	public static final class FileUploadListingWrapper {
		
		private final String color;
		private final String evaluator;
		private final String filename;
		private final String filesize;
		private final String mapperUri;
		private final String iconCss;
		private final String thumbUri;

		public FileUploadListingWrapper(String color, String evaluator, String filename, String filesize, String mapperUri,
				String iconCss, String thumbUri) {
			this.color = color;
			this.evaluator = evaluator;
			this.filename = filename;
			this.filesize = filesize;
			this.mapperUri = mapperUri;
			this.iconCss = iconCss;
			this.thumbUri = thumbUri;
		}

		public String getEvaluator() {
			return evaluator;
		}

		public String getColor() {
			return color;
		}

		public String getFilename() {
			return filename;
		}

		public String getFilesize() {
			return filesize;
		}

		public String getMapperUri() {
			return mapperUri;
		}

		public String getIconCss() {
			return iconCss;
		}

		public String getThumbUri() {
			return thumbUri;
		}
	}

}
