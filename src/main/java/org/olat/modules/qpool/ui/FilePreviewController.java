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
package org.olat.modules.qpool.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.qpool.QPoolItemEditorController;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;

/**
 * 
 * Initial date: 27.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FilePreviewController extends BasicController implements QPoolItemEditorController {

	private final VelocityContainer mainVC;

	private final QuestionItem qitem;
	
	public FilePreviewController(UserRequest ureq, WindowControl wControl, QuestionItem qitem) {
		super(ureq, wControl);
		this.qitem = qitem;
		QPoolService qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		mainVC = createVelocityContainer("file_preview");
		
		VFSLeaf leaf = qpoolService.getRootLeaf(qitem);
		if(leaf != null) {
			mainVC.contextPut("filename", leaf.getName());
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	public QuestionItem getItem() {
		return qitem;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
