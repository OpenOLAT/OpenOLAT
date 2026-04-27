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
package org.olat.repository.ui.list;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.run.leave.ConfirmLeaveController;
import org.olat.group.BusinessGroupService;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryDetailsHeaderController extends AbstractDetailsHeaderController {

	private CloseableModalController cmc;
	private ConfirmLeaveController leaveDialogBox;
	
	private final RepositoryEntry entry;
	private final boolean closeTabOnLeave;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private BusinessGroupService businessGroupService;

	public RepositoryEntryDetailsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean closeTabOnLeave, DetailsHeaderConfig config) {
		super(ureq, wControl, config);
		this.entry = entry;
		this.closeTabOnLeave = closeTabOnLeave;
		
		init(ureq);
	}

	@Override
	protected String getIconCssClass() {
		return RepositoyUIFactory.getIconCssClass(entry);
	}

	@Override
	protected String getExternalRef() {
		return entry.getExternalRef();
	}

	@Override
	protected String getTranslatedTechnicalType() {
		if (StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
			NodeAccessType type = NodeAccessType.of(entry.getTechnicalType());
			return ConditionNodeAccessProvider.TYPE.equals(type.getType())
					? translate("CourseModule")
					: nodeAccessService.getNodeAccessTypeName(type, getLocale());
		}
		return translate(entry.getOlatResource().getResourceableTypeName());
	}

	@Override
	protected String getTitle() {
		return entry.getDisplayname();
	}

	@Override
	protected String getAuthors() {
		return entry.getAuthors();
	}

	@Override
	protected String getTeaser() {
		return entry.getTeaser();
	}

	@Override
	protected VFSLeaf getTeaserImage() {
		return repositoryService.getIntroductionImage(entry);
	}

	@Override
	protected VFSLeaf getTeaserMovie() {
		return repositoryService.getIntroductionMovie(entry);
	}

	@Override
	protected RepositoryEntryEducationalType getEducationalType() {
		return entry.getEducationalType();
	}

	@Override
	protected String getPendingMessageElementName() {
		return translate("CourseModule");
	}
	
	@Override
	protected String getLeaveText(boolean withFee) {
		return translate("sign.out.type", translate(entry.getOlatResource().getResourceableTypeName()));
	}
	
	@Override
	protected String getStartLinkText() {
		return translate("open.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
	}

	@Override
	protected OLATResource getResource() {
		return entry.getOlatResource();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == startCtrl) {
			if (event == LEAVE_EVENT) {
				doConfirmLeave(ureq);
			}
		} else if (leaveDialogBox == source) {
			if (event.equals(Event.DONE_EVENT)) {
				doLeave(ureq);
				if (!closeTabOnLeave) {
					fireEvent(ureq, new LeavingEvent(entry));
				}
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(leaveDialogBox);
		removeAsListenerAndDispose(cmc);
		leaveDialogBox = null;
		cmc = null;
	}

	private void doConfirmLeave(UserRequest ureq) {
		if (guardModalController(leaveDialogBox)) return;

		String title = translate("sign.out.type", translate(entry.getOlatResource().getResourceableTypeName()));
		leaveDialogBox = new ConfirmLeaveController(ureq, getWindowControl(), entry);
		listenTo(leaveDialogBox);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), leaveDialogBox.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doLeave(UserRequest ureq) {
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(result, getWindowControl().getBusinessControl().getAsString(), true);
		LeavingStatusList status = new LeavingStatusList();
		//leave course
		repositoryManager.leave(getIdentity(), entry, status, reMailing);
		//leave groups
		businessGroupService.leave(getIdentity(), entry, status, reMailing);
		DBFactory.getInstance().commit();//make sure all changes are committed
		
		if(status.isWarningManagedGroup() || status.isWarningManagedCourse()) {
			showWarning("sign.out.warning.managed");
		} else if(status.isWarningGroupWithMultipleResources()) {
			showWarning("sign.out.warning.mutiple.resources");
		} else {
			showInfo("sign.out.success", new String[]{ StringHelper.escapeHtml(entry.getDisplayname()) });
			if (closeTabOnLeave) {
				getWindowControl().getWindowBackOffice().getWindow().getDTabs().closeDTab(ureq, entry.getOlatResource(), null);
			}
		}
	}

}
