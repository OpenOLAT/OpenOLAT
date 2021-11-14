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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 24.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CollectionTargetController extends BasicController {
	
	public static final String ADD_TO_LIST_POOL_CMD = "qpool.list.add";
	public static final String NEW_LIST_CMD = "qpool.list.new";
	public static final String RENAME_LIST_CMD = "qpool.list.rename";
	public static final String DELETE_LIST_CMD = "qpool.list.delete";

	private final Link newList, addToList;
	private Link deleteSource, renameSource;
	
	public CollectionTargetController(UserRequest ureq, WindowControl wControl, boolean editItemCollection) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("collection_target");
		newList = LinkFactory.createLink("create.list", mainVC, this);
		addToList = LinkFactory.createLink("add.to.list", mainVC, this);
		if(editItemCollection) {
			renameSource = LinkFactory.createLink("rename.collection", mainVC, this);
			deleteSource = LinkFactory.createLink("delete.collection", mainVC, this);
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(newList == source) {
			fireEvent(ureq, new Event(NEW_LIST_CMD));
		} else if(addToList == source) {
			fireEvent(ureq, new Event(ADD_TO_LIST_POOL_CMD));
		} else if(deleteSource == source) {
			fireEvent(ureq, new Event(DELETE_LIST_CMD));
		} else if(renameSource == source) {
			fireEvent(ureq, new Event(RENAME_LIST_CMD));
		}
	}
}
