package org.olat.admin.bc;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfoFileImpl;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 20.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BriefcaseAdminController extends FormBasicController {
	
	private FormLink thumbnailReset;

	public BriefcaseAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "bc_admin");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		thumbnailReset = uifactory.addFormLink("thumbnails.reset", formLayout, Link.BUTTON);
	}
	
	@Override
	protected void doDispose() {
		//
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
			TaskExecutorManager.getInstance().runTask(task);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private class ResetThumbnails implements Runnable {
	
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			logInfo("Start reset of thumbnails", null);
			
			String metaRoot = FolderConfig.getCanonicalMetaRoot();
			File metaRootFile = new File(metaRoot);
			resetThumbnails(metaRootFile);
			flc.contextPut("recalculating", Boolean.FALSE);
			
			logInfo("Finished reset of thumbnails in " + (System.currentTimeMillis() - start) + " (ms)", null);
		}
			
		private void resetThumbnails(File directory) {
			for(File file:directory.listFiles()) {
				if(file.isHidden()) {
					//do nothing
				} else if(file.isDirectory()) {
					resetThumbnails(file);
				} else if(file.getName().endsWith(".xml")) {
					resetThumbnailsInMeta(file);
				}
			}
		}
		
		private void resetThumbnailsInMeta(File metafile) {
			try {
				MetaInfoFileImpl metaInfo = new MetaInfoFileImpl(metafile);
				metaInfo.clearThumbnails();
			} catch (Exception e) {
				logError("", e);
			}
		}
	}
}
