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

package org.olat.portfolio.model.artefacts;

import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.meta.MetaInfo;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.portfolio.EPAbstractHandler;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.ui.artefacts.view.details.FileArtefactDetailsController;
import org.olat.repository.RepositoryManager;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * 
 * Description:<br>
 * Artefacthandler for collected or uploaded files
 * 
 * <P>
 * Initial Date: 25 jun. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class FileArtefactHandler extends EPAbstractHandler<FileArtefact> {
	
	private static final OLog log = Tracing.createLoggerFor(FileArtefactHandler.class);

	@Override
	public FileArtefact createArtefact() {
		return new FileArtefact();
	}

	@Override
	public void prefillArtefactAccordingToSource(AbstractArtefact artefact, Object source) {
		super.prefillArtefactAccordingToSource(artefact, source);
		if (source instanceof VFSItem) {
			VFSItem fileSource = (VFSItem) source;
			((FileArtefact) artefact).setFilename(fileSource.getName());
			
			MetaInfo meta = null;
			if(fileSource.canMeta() == VFSConstants.YES) {
				meta = fileSource.getMetaInfo();
			}

			if (meta != null && StringHelper.containsNonWhitespace(meta.getTitle())) {
				artefact.setTitle(meta.getTitle());
			} else {
				artefact.setTitle(fileSource.getName());
			}
			if (meta != null && StringHelper.containsNonWhitespace(meta.getComment())) {
				artefact.setDescription(meta.getComment());
			}
			artefact.setSignature(60);

			String path = fileSource.getRelPath();
			String[] pathElements = path.split("/");

			String finalBusinessPath = null;
			String sourceInfo = null;
			// used to rebuild businessPath and source for a file:
			if (pathElements[1].equals("homes") && meta != null && pathElements[2].equals(meta.getAuthor())) {
				// from users briefcase
				String lastParts = "/";
				for (int i = 4; i < (pathElements.length - 1); i++) {
					lastParts = lastParts + pathElements[i] + "/";
				}
				sourceInfo = "Home -> " + pathElements[3] + " -> " + lastParts + fileSource.getName();
			} else if (pathElements[3].equals("BusinessGroup")) {
				// out of a businessgroup
				String lastParts = "/";
				for (int i = 5; i < (pathElements.length - 1); i++) {
					lastParts = lastParts + pathElements[i] + "/";
				}
				BusinessGroup bGroup = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(new Long(pathElements[4]));
				if (bGroup != null) {
					sourceInfo = bGroup.getName() + " -> " + lastParts + " -> " + fileSource.getName();
				}
				finalBusinessPath = "[BusinessGroup:" + pathElements[4] + "][toolfolder:0][path=" + lastParts + fileSource.getName() + ":0]";
			} else if (pathElements[4].equals("coursefolder")) {
				// the course folder
				sourceInfo = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(new Long(pathElements[2]))
						+ " -> " + fileSource.getName();

			} else if (pathElements[1].equals("course") && pathElements[3].equals("foldernodes")) {
				// folders inside a course
				sourceInfo = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(new Long(pathElements[2]))
						+ " -> " + pathElements[4] + " -> " + fileSource.getName();
				finalBusinessPath = "[RepositoryEntry:" + pathElements[2] + "][CourseNode:" + pathElements[4] + "]";
			}

			if (sourceInfo == null) {
				// unknown source, keep full path
				sourceInfo = VFSManager.getRealPath(fileSource.getParentContainer()) + "/" + fileSource.getName();
			}

			artefact.setBusinessPath(finalBusinessPath);
			artefact.setSource(sourceInfo);
		}

	}

	/**
	 * @see org.olat.portfolio.EPAbstractHandler#createDetailsController(org.olat.core.gui.UserRequest, org.olat.portfolio.model.artefacts.AbstractArtefact)
	 */
	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		return new FileArtefactDetailsController(ureq, wControl, artefact, readOnlyMode);		
	}

	@Override
	public String getType() {
		return FileArtefact.FILE_ARTEFACT_TYPE;
	}
	
	

	@Override
	protected void getContent(AbstractArtefact artefact, StringBuilder sb, SearchResourceContext context, EPFrontendManager ePFManager) {
		FileArtefact fileArtefact = (FileArtefact)artefact;
		String filename = fileArtefact.getFilename();
		
		VFSItem file = ePFManager.getArtefactContainer(artefact).resolve(filename);
		if (file instanceof VFSLeaf) {
			try {
				FileDocumentFactory docFactory = CoreSpringFactory.getImpl(FileDocumentFactory.class);
				if (docFactory.isFileSupported((VFSLeaf)file)) {
					Document doc = docFactory.createDocument(context, (VFSLeaf)file);
					String content = doc.get(AbstractOlatDocument.CONTENT_FIELD_NAME);
					sb.append(content);
				}
			} catch (Exception e) {
				log.error("Could not get content of file "+file.getName()+" (file-artefact "+artefact.getKey()+")", e);
			}
		}
	}
}
