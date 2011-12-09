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
* <p>
*/ 

package org.olat.core.gui.formelements;

/**
 * Description:<br>
 * TODO: guido Class Description for TextAreaElementAutoSize
 * 
 * @Deprecated The wiki markup area is no longer supported. In the legacy form
 *             infrastructure it's still there, but it won't be available in the
 *             new flexi forms. In flexi forms use the RichTextElement instead.
 * <P>
 * Initial Date:  Aug 15, 2006 <br>
 * @author guido
 */
@Deprecated
public class WikiMarkupTextAreaElementAutoSize extends TextAreaElement {

	public WikiMarkupTextAreaElementAutoSize(String labelKey, int rows, int cols) {
		super(labelKey, rows, cols, "");
	}
	
	public WikiMarkupTextAreaElementAutoSize(String labelKey, int rows, int cols, String value) {
		super(labelKey, rows, cols, value);
	}

}
