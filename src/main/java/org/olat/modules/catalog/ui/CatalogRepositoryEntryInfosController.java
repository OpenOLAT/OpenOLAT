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
package org.olat.modules.catalog.ui;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryInfosController extends RepositoryEntryDetailsController implements Activateable2 {

	private final BreadcrumbedStackedPanel stackPanel;
	
	private CatalogRepositoryEntryAccessController accessCtrl;
	
	@Autowired
	protected ACService acService;

	public CatalogRepositoryEntryInfosController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, entry);
		this.stackPanel = stackPanel;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if ("Offers".equalsIgnoreCase(type)) {
			doBook(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessCtrl) {
			if (event instanceof BookedEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void doStart(UserRequest ureq) {
		try {
			String businessPath = "[RepositoryEntry:" + getEntry().getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + getEntry().getKey() + " (" + getEntry().getOlatResource().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}

	@Override
	protected void doBook(UserRequest ureq) {
		if (getEntry() != null) {
			AccessResult acResult = acService.isAccessible(getEntry(), getIdentity(), Boolean.FALSE, ureq.getUserSession().getRoles().isGuestOnly(), false);
			if (acResult.isAccessible() || acService.tryAutoBooking(getIdentity(), getEntry(), acResult)) {
				doStart(ureq);
			} else {
				doOpenBooking(ureq);
			}
		}
	}
	
	private void doOpenBooking(UserRequest ureq) {
		removeAsListenerAndDispose(accessCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Offers", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		accessCtrl = new CatalogRepositoryEntryAccessController(ureq, bwControl, getEntry());
		listenTo(accessCtrl);
		addToHistory(ureq, accessCtrl);
		
		String displayName = translate("offers");
		stackPanel.pushController(displayName, accessCtrl);
	}

}
