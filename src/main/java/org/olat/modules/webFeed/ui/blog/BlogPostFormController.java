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
package org.olat.modules.webFeed.ui.blog;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.ui.ItemFormController;

/**
 * Form controller for blog posts.
 * 
 * <P>
 * Initial Date: Aug 3, 2009 <br>
 * 
 * @author gwassmann
 */
public class BlogPostFormController extends ItemFormController {
	
	public BlogPostFormController(UserRequest ureq, WindowControl control, Item item, Translator translator) {
		super(ureq, control, item, translator);
	}
	
	@Override
	protected String getType() {
		return BlogFileResource.TYPE_NAME;
	}
	
	@Override 
	protected boolean hasContent() {
		return true;
	}
	
	@Override
	protected boolean hasDraftMode() {
		return true;
	}
	
	@Override
	protected boolean hasMandatoryMedia() {
		return false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/learning_activities/Working_With_Course_Elements/#blog");
		super.initForm(formLayout, listener, ureq);
	}
}
