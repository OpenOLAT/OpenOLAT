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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.commons.services.search.ui;

import org.olat.core.commons.services.search.ResultDocument;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.Controller;

/**
 * 
 * Description:<br>
 * Controller which shows a result (a document) from a full text search
 * 
 * <P>
 * Initial Date:  4 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface ResultController extends Controller {
	
	public boolean isHighlight();
	
	public void setHighlight(boolean highlight);
	
	public FormItem getInitialFormItem();
}
