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
package org.olat.course.nodes.edubase;

import org.olat.core.gui.translator.Translator;

/**
 * Helper to format some Elements in the Edubase views.
 * 
 * Initial date: 03.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseViewHelper {
	
	Translator translator;
	
	public EdubaseViewHelper(Translator translator) {
		this.translator = translator;
	}

	/**
	 * Format the page from and page to of a BookSection. If both parameter are
	 * null, null is returned. If one of the two parameter is null, the not null
	 * parameter will be used for page from and for page to.
	 * 
	 * @param pageFrom
	 * @param pageTo
	 * @return
	 */
	public String formatPageFromTo(Integer pageFrom, Integer pageTo) {
		if (pageFrom == null && pageTo == null)
			return null;

		String from = pageFrom != null ? Integer.toString(pageFrom) : Integer.toString(pageTo);
		String to = pageTo != null ? Integer.toString(pageTo) : Integer.toString(pageFrom);
		return translator.translate("edubase.book.section.page.from.to", new String[] { from, to});
	}
}
