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
package org.olat.course;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResourceManager;

/**
 * Initial Date: 19.04.2008 <br>
 * 
 * @author patrickb
 */
public class DisposedCourseRestartController extends BasicController {

	private VelocityContainer initialContent;
	private Link restartLink;
	private RepositoryEntry courseRepositoryEntry;
	private StackedPanel panel;

	public DisposedCourseRestartController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseRepositoryEntry) {
		super(ureq, wControl);
		initialContent = createVelocityContainer("disposedcourserestart");
		restartLink = LinkFactory.createButton("course.disposed.command.restart", initialContent, this);
		restartLink.setElementCssClass("o_sel_course_restart");
		this.courseRepositoryEntry = courseRepositoryEntry;
		panel = putInitialPanel(initialContent);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == restartLink) {
			OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(
					courseRepositoryEntry.getOlatResource().getResourceableId(),
					courseRepositoryEntry.getOlatResource().getResourceableTypeName());
			if(ores == null ) {
				//course was deleted!				
				MessageController msgController = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), translate("course.deleted.title"), translate("course.deleted.text"));
				panel.setContent(msgController.getInitialComponent());
			} else {
				OLATResourceable reOres = OresHelper.clone(courseRepositoryEntry);
				DTabs dtabs = getWindowControl().getWindowBackOffice().getWindow().getDTabs();
				if(dtabs != null) {
					DTab dt = dtabs.getDTab(reOres);
					if(dt != null) {
						dtabs.removeDTab(ureq, dt);
					}
				}
				
				List<ContextEntry> entries = null;
				List<HistoryPoint> stacks = ureq.getUserSession().getHistoryStack();
				for(int i=stacks.size(); i-->0; ) {
					HistoryPoint point = stacks.get(i);
					if(point != null && point.getEntries() != null && point.getEntries().size() > 0) {
						ContextEntry entry = point.getEntries().get(0);
						if(reOres.equals(entry.getOLATResourceable())) {
							entries = point.getEntries();
							break;
						}
					}
				}
				
				WindowControl bwControl;
				if(entries == null) {
					bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(getWindowControl(), reOres);
				} else {
					BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(entries);
					bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
				}

				NewControllerFactory.getInstance().launch(ureq, bwControl);
				dispose();
			}
		}
	}
}
