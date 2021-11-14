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
package org.olat.admin.bc;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BriefcaseAdminController extends FormBasicController {
	
	private FormLink thumbnailReset;
	@Autowired
	private TaskExecutorManager taskExecutor;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public BriefcaseAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "bc_admin");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		thumbnailReset = uifactory.addFormLink("thumbnails.reset", formLayout, Link.BUTTON);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(thumbnailReset == source) {
			flc.contextPut("recalculating", Boolean.TRUE);
			ResetThumbnails task = new ResetThumbnails();
			taskExecutor.execute(task);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private class ResetThumbnails implements Runnable {
	
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			logInfo("Start reset of thumbnails");
			
			String metaRoot = FolderConfig.getCanonicalMetaRoot();
			vfsRepositoryService.resetThumbnails(new File(metaRoot));
			flc.contextPut("recalculating", Boolean.FALSE);
			
			logInfo("Finished reset of thumbnails in " + (System.currentTimeMillis() - start) + " (ms)");
		}
	}
}
