/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.ui.admin;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.modules.bc.BriefcaseWebDAVMergeSource;
import org.olat.core.commons.services.vfs.ui.management.VFSSizeCellRenderer;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils.Usage;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.cemedia.MediaService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserQuotaController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private UserQuotaTableDataModel tableModel;
	
	private int count = 0;
	private final Roles editedRoles;
	private final Identity editedIdentity;
	
	private Controller quotaEditCtr;
	private CloseableModalController cmc;
	
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private BaseSecurity securityManager;
	
	public UserQuotaController(UserRequest ureq, WindowControl wControl, Identity editedIdentity) {
		super(ureq, wControl, "quotas");
		this.editedIdentity = editedIdentity;
		this.editedRoles = securityManager.getRoles(editedIdentity);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuotaCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuotaCols.files));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuotaCols.size, new VFSSizeCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuotaCols.quota, new VFSSizeCellRenderer(true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuotaCols.uploadLimit, new VFSSizeCellRenderer(true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QuotaCols.used));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit.quota", translate("edit.quota"), "edit.quota"));
		
		tableModel = new UserQuotaTableDataModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == quotaEditCtr) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(quotaEditCtr);
		removeAsListenerAndDispose(cmc);
		quotaEditCtr = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if("edit.quota".equals(cmd)) {
					doEditQuota(ureq, tableModel.getObject(se.getIndex()).relPath());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void loadModel() {
		List<QuotaRow> rows = new ArrayList<>(2);
		rows.add(loadPersonalFolderRow());
		rows.add(loadMediaCenterRow());
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private QuotaRow loadPersonalFolderRow() {
		BriefcaseWebDAVMergeSource personalFolder = new BriefcaseWebDAVMergeSource(editedIdentity, editedRoles, "quota");
		Quota quota = personalFolder.getLocalSecurityCallback().getQuota();
		Usage actualUsage = VFSManager.getUsage(personalFolder);
		return forgeRow(translate("tool.personal.folder"), actualUsage, quota);
	}
	
	private QuotaRow loadMediaCenterRow() {
		Quota quota = mediaService.getQuota(editedIdentity, editedRoles);
		Usage usage = mediaService.getFileUsage(editedIdentity);
		return forgeRow(translate("tool.personal.media.center"), usage, quota);
	}
	
	private QuotaRow forgeRow(String name, Usage usage, Quota quota) {
		long usageKB = quota.getUsageKB().longValue();
		long quotaKB = quota.getQuotaKB().longValue();
		long uploadLimitKB = quota.getUlLimitKB().longValue();
		ProgressBar usedBar = forgeProgressBar(quota.getQuotaKB(), usageKB);
		return new QuotaRow(name, quota.getPath(), usage, quotaKB, uploadLimitKB, usedBar);
	}
	
	private ProgressBar forgeProgressBar(long quotaInKB, long usageInKB) {
		ProgressBar currentlyUsedBar = new ProgressBar("used_" + (++count));
		currentlyUsedBar.setLabelAlignment(ProgressBar.LabelAlignment.none);
		currentlyUsedBar.setMax(100);
		
		float actual;
		if(usageInKB <= 0) {
			actual = 0.0f;
		} else {
			actual = (usageInKB / (float)quotaInKB) * 100.0f;
		}
		currentlyUsedBar.setActual(actual);
		ProgressBar.BarColor barColor = currentlyUsedBar.getActual() < 80
				? ProgressBar.BarColor.primary
				: ProgressBar.BarColor.danger;
		currentlyUsedBar.setBarColor(barColor);
		return currentlyUsedBar;
	}
	
	private void doEditQuota(UserRequest ureq, String relPath) {
		quotaEditCtr = quotaManager.getQuotaEditorInstance(ureq, getWindowControl(), relPath, false, true);
		listenTo(quotaEditCtr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), quotaEditCtr.getInitialComponent(), true, translate("qf.edit"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private record QuotaRow(String name, String relPath, Usage usage, long quota, long uploadLimit, ProgressBar used) {
		//
	}
	
	private static class UserQuotaTableDataModel extends DefaultFlexiTableDataModel<QuotaRow> {
		
		private static final QuotaCols[] COLS = QuotaCols.values();
		
		public UserQuotaTableDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			QuotaRow quotaRow = getObject(row);
			switch(COLS[col]) {
				case name: return quotaRow.name();
				case files: return quotaRow.usage().getNumOfFiles();
				case size: return quotaRow.usage().getSize();// bytes
				case quota: return quotaRow.quota();// kB
				case uploadLimit: return quotaRow.uploadLimit();// kB
				case used: return quotaRow.used();
				default: return "ERROR";
			}
		}
	}

	public enum QuotaCols implements FlexiSortableColumnDef {
		name("table.header.quota.name"),
		files("table.header.quota.files"),
		size("table.header.quota.size"),
		quota("table.header.quota"),
		uploadLimit("table.header.upload.limit.size"),
		used("table.header.used");
		
		private final String i18nKey;
		
		private QuotaCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
