/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.sysinfo;

import java.util.List;

import org.olat.admin.sysinfo.LockTableModel.Cols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.coordinate.LockRemovedEvent;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;

/**
 *  Controller to manage non persisitent locks. Allow to release certain lock manually.
 *  Normally locks will be released in dispose method. 
 *  A lock must release manually only in case of an error (in dispose or shutdown)   
 *  otherwise a locks should be released after shutdown.
 *  @author Christian Guretzki
 */

public class LockController extends BasicController {
	
	private VelocityContainer myContent;
	private TableController tableCtr;
	private LockTableModel locksTableModel;
	private DialogBoxController dialogController;

	/**
	 * Controls locks in admin view.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public LockController(UserRequest ureq, WindowControl wControl) { 
		super(ureq, wControl);
		
		myContent = createVelocityContainer("locks");
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(false);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("lock.key", Cols.key.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("lock.owner", Cols.ownerFullname.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("lock.aquiretime", Cols.acquiredTime.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("lock.release", "lock.release", translate("lock.release")));
		listenTo(tableCtr);
		resetTableModel();
		myContent.put("locktable", tableCtr.getInitialComponent());
		putInitialPanel(myContent);
	}

	/**
	 * Re-initialize this controller. Fetches sessions again.
	 */
	public void resetTableModel() {
		List<LockEntry> locks = CoordinatorManager.getInstance().getCoordinator().getLocker().adminOnlyGetLockEntries();
		locksTableModel = new LockTableModel(locks);
		tableCtr.setTableDataModel(locksTableModel);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				LockEntry lockToRelease = locksTableModel.getObject(te.getRowId());
				dialogController = activateYesNoDialog(ureq, null, translate("lock.release.sure"), dialogController);
				dialogController.setUserObject(lockToRelease);
			}
		} else if (source == dialogController) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				LockEntry lockToRelease = (LockEntry)dialogController.getUserObject();
				MultiUserEvent mue = new LockRemovedEvent(lockToRelease);
				OLATResourceable lockEntryOres = OresHelper.createOLATResourceableInstance(LockEntry.class, 0l);
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, lockEntryOres);
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLockEntry(lockToRelease);
				lockToRelease = null;
				resetTableModel();
			}
		}

	}

	@Override
	protected void doDispose() {
		// DialogBoxController and TableController get disposed by BasicController
		locksTableModel = null;
        super.doDispose();
	}
}
