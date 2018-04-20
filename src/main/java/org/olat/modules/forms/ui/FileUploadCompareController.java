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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.ui.model.CompareResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileUploadCompareController extends FormBasicController implements Controller {
	
	private final FileUpload fileUpload;
	private final List<CompareResponse> compareResponses;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public FileUploadCompareController(UserRequest ureq, WindowControl wControl, FileUpload fileUpload,
			List<CompareResponse> compareResponses) {
		super(ureq, wControl, "file_upload_compare");
		this.fileUpload = fileUpload;
		this.compareResponses = compareResponses;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("wrappers", createWrappers(ureq));
	}

	private List<FileUploadCompareWrapper> createWrappers(UserRequest ureq) {
		List<FileUploadCompareWrapper> wrappers = new ArrayList<>();
		for (CompareResponse compareResponse: compareResponses) {
			if (isValid(compareResponse)) {
				FileUploadCompareWrapper wrapper = createWrapper(ureq, compareResponse);
				wrappers.add(wrapper);
			}	
		}
		return wrappers;
	}
	
	private boolean isValid(CompareResponse compareResponse) {
		List<EvaluationFormResponse> responses = compareResponse.getResponses();
		if (responses == null)
			return false;
		if (responses.isEmpty())
			return false;
		EvaluationFormResponse response = responses.get(0);
		if (response.getResponseIdentifier() == null)
			return false;
		if (!response.getResponseIdentifier().equals(fileUpload.getId()))
			return false;
		if (response.getFileResponse() == null)
			return false;
		return true;
	}

	private FileUploadCompareWrapper createWrapper(UserRequest ureq, CompareResponse compareResponse) {
		EvaluationFormResponse response = compareResponse.getResponses().get(0);
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
			if (leaf instanceof MetaTagged) {
				MetaTagged metaTaggedLeaf = (MetaTagged) leaf;
				MetaInfo meta = metaTaggedLeaf.getMetaInfo();
				if (meta != null && meta.isThumbnailAvailable()) {
					VFSLeaf thumb = meta.getThumbnail(200, 200, false);
					if (thumb != null) {
						thumbUri = registerCacheableMapper(ureq, "file-upload-thumb" + CodeHelper.getRAMUniqueID() + "-" + leaf.getLastModified(), new VFSMediaMapper(thumb));;
					}
				}
			}
		}
		return new FileUploadCompareWrapper(compareResponse.getColor(), compareResponse.getLegendName(), filename, filesize, mapperUri, iconCss, thumbUri);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public static final class FileUploadCompareWrapper {
		
		private final String color;
		private final String evaluator;
		private final String filename;
		private final String filesize;
		private final String mapperUri;
		private final String iconCss;
		private final String thumbUri;

		public FileUploadCompareWrapper(String color, String evaluator, String filename, String filesize, String mapperUri,
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
