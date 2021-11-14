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
package org.olat.course.nodes.document.ui;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.course.nodes.document.DocumentSource;

/**
 * 
 * Initial date: 4 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentPeekviewController extends BasicController {
	
	private Link fileLink;
	private final Long courseRepoKey;
	private final String nodeId;

	public DocumentPeekviewController(UserRequest ureq, WindowControl wControl, DocumentCourseNode courseNode,
			Long courseRepoKey, VFSContainer courseFolderCont) {
		super(ureq, wControl);
		this.courseRepoKey = courseRepoKey;
		this.nodeId = courseNode.getIdent();
		
		VelocityContainer mainVC = createVelocityContainer("peekview");
		
		DocumentSource documentSource = courseNode.getDocumentSource(courseFolderCont);
		if (documentSource != null && documentSource.getVfsLeaf() != null) {
			VFSLeaf vfsLeaf = documentSource.getVfsLeaf();
			String fileCssClass = CSSHelper.createFiletypeIconCssClassFor(vfsLeaf.getName());
			fileLink = LinkFactory.createCustomLink("file", "file", "", Link.LINK_CUSTOM_CSS, mainVC, this);
			fileLink.setCustomDisplayText(vfsLeaf.getName());
			fileLink.setIconLeftCSS("o_icon " + fileCssClass);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == fileLink) {
			String businessPath = "[RepositoryEntry:" + courseRepoKey + "][CourseNode:" + nodeId + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}

}
