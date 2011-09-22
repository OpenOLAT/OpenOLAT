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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.core.gui.components.table;

/**
 * @deprecated Needs new concept. Does not play well with Table using form.submit().
 */
public interface HrefGenerator {
	/**
	 * @deprecated FIXME:fj:b a new concept is needed here, since it is too generic to conform to e.g. urlbuilding (any javascript can be inserted here)
	 * @param row
	 * @param href
	 * @return
	 */
	String generate(int row, String href);

}
