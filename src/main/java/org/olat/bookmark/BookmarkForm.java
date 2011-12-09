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

import org.olat.ControllerFactory;
import org.olat.core.gui.UserRequest;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Initial Date: Jul 14, 2003
 * 
 * @author jeger Comment: Form for creating and changing the bookmark.
 */
public class BookmarkForm extends FormBasicController {
	
	private Bookmark bm;

	protected static final String FORMNAME = "bookmarkform";
	protected static final String BM_TYPE = "bookmarktype";
	protected static final String BM_TITLE = "bookmarktitle";
	protected static final String BM_DESCRIPTION = "bookmarkdesc";
	
	private   TextElement bmtype;
	protected TextElement bmtitle;
	protected TextElement bmdescription;
	
	/**
	 * @param name
	 * @param locale
	 * @param bm
	 */
	public BookmarkForm(UserRequest ureq, WindowControl wControl, Bookmark bm) {
		super(ureq, wControl);
		this.bm = bm;
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		bmtype = uifactory.addTextElement(BM_TYPE, "form.bmtype", 128, ControllerFactory.translateResourceableTypeName(bm.getDisplayrestype(),getLocale()), formLayout);
		bmtype.setEnabled(false);
		
		bmtitle = uifactory.addTextElement(BM_TITLE, "form.bmtitle", 255, bm.getTitle(), formLayout);
		bmtitle.setMandatory(true);
		bmtitle.setNotEmptyCheck("error.title.empty");
		bmdescription = uifactory.addTextElement(BM_DESCRIPTION,"form.bmdescription", 255, bm.getDescription(),formLayout);

		uifactory.addFormSubmitButton("save", "save", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}