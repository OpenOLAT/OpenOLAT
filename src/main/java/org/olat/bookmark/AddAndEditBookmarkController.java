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

package org.olat.bookmark;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;

/**
 * <!--**************-->
 * <h3>Responsability:</h3>
 * Offers the possibility to save a new bookmark with a title and a description,
 * or edit an existing one.
 * <p>
 * <!--**************-->
 * <h3>Events fired:</h3>
 * <ul>
 * <li><i>{@link org.olat.core.gui.control.Event#DONE_EVENT DONE_EVENT}</i>:<br>
 * Only if the changed, added bookmark was successfully saved.</li>
 * <li><i>{@link org.olat.core.gui.control.Event#CANCELLED_EVENT CANCELLED_EVENT}</i>:<br>
 * If the workflow is canceled by the user.</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Workflow:</h3>
 * <ul>
 * <li><i>Mainflow:</i><br>
 * ask for a title and description.<br>
 * update or save the bookmark.</li>
 * <li><i>Cancel:</i><br>
 * cancel entering bookmark.<br>
 * </li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Hints:</h3>
 * empty <code>doDispose()</code>.
 * <p>
 * 
 * @author Sabina Jeger
 * 
 */
public class AddAndEditBookmarkController extends BasicController {

	private Bookmark myBook;
	private VelocityContainer myContent;
	private BookmarkForm bmForm;
	private boolean updateMode;	
	
	/**
	 * Constructor of controller for adding a bookmark
	 * 
	 * @param ureq
	 * @param wControl
	 * @param proposedTitle max 255 chars
	 * @param detailData max 255 chars
	 * @param olatres
	 * @param displayResourceableType
	 */
	public AddAndEditBookmarkController(UserRequest ureq, WindowControl wControl, String proposedTitle, String detailData,
			OLATResourceable olatres, String displayResourceableType) {
		super(ureq, wControl);
		Bookmark mb = new BookmarkImpl(displayResourceableType, olatres.getResourceableTypeName(), olatres.getResourceableId(), proposedTitle,
				detailData, ureq.getIdentity());		
		init(ureq, wControl, mb, false);
	}

	/**
	 * Constructor of controller for editing a bookmark
	 * 
	 * @param ureq
	 * @param wControl
	 * @param bm bookmark to be edited
	 */
	public AddAndEditBookmarkController(UserRequest ureq, WindowControl wControl, Bookmark bm) {
		super(ureq, wControl);
		
		init(ureq, wControl, bm, true);
	}

	/**
	 * Internal helper to initialize the controller. Should be called only once
	 * 
	 * @param bm
	 * @param aupdateMode
	 */
	private void init(UserRequest ureq, WindowControl wControl, Bookmark bm, boolean aupdateMode) {

		this.myBook = bm;
		this.updateMode = aupdateMode;

		myContent = createVelocityContainer("addbookmark");

		bmForm = new BookmarkForm(ureq, wControl, myBook);
		listenTo(bmForm);
		myContent.put(BookmarkForm.FORMNAME, bmForm.getInitialComponent());

		putInitialPanel(myContent);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public void event(UserRequest ureq, Controller source, Event event) {
	
		
		if (source == bmForm && event == Event.DONE_EVENT) {

			String newTitle = bmForm.bmtitle.getValue();
			String newDesc =  bmForm.bmdescription.getValue();
			
			myBook.setTitle(newTitle);
			myBook.setDescription(newDesc);
			BookmarkManager bm = BookmarkManager.getInstance();
			if (updateMode) {
				bm.updateBookmark(myBook);
				showInfo("bookmark.update.successful");
			} else {
				bm.createAndPersistBookmark(myBook);
				showInfo("bookmark.create.successful");
			}
			fireEvent(ureq, Event.DONE_EVENT);				
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// nothing to do here
	}
}
