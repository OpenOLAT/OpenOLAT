/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.edusharing;

/**
 * How OpenOlat shows the HTML that edu-sharing renders for an embedded item
 * (course element "edu-sharing", rich text fields, course info description).
 * <p>
 * The edu-sharing HTML is foreign content. It can carry script, event handlers
 * and style sheets, e.g. through a prepared user name in the metadata. Each mode
 * makes sure that this content never runs with the session of the viewer on the
 * OpenOlat domain. A mode without cleaning and without isolation does not exist
 * on purpose.
 * <p>
 * The modes need the content domain. Without content domain, OpenOlat cleans all
 * edu-sharing HTML and inserts it directly into the page, whatever mode is set.
 * <p>
 * Script on the content domain runs with the content domain session of the viewer.
 * It cannot reach the OpenOlat page or the session on the OpenOlat domain, but it
 * can load the edu-sharing content that the viewer may see.
 * <p>
 * HTML pages (course elements "HTML page", "Structure", task documents) run in an
 * iframe on the content domain. Their edu-sharing items are shown unchanged in both
 * modes. This iframe has no sandbox.
 * <p>
 * A mode change reaches the rich text fields of a logged-in user after the next login.
 *
 * Initial date: 27.11.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public enum EdusharingDisplayMode {
	
	/**
	 * The course element "edu-sharing" shows its item unchanged in an iframe on the
	 * content domain. Items in rich text fields are cleaned on the server and inserted
	 * directly into the page. The cleaning removes script, event handlers, style sheets
	 * and embedded content from hosts that are not allowed. OpenOlat provides the
	 * removed functions itself (metadata box, H5P height, layout).
	 * <p>
	 * Pro: rich text keeps its text flow, the course element keeps all edu-sharing functions.<br>
	 * Con: in rich text, functions and layout of edu-sharing that OpenOlat does not rebuild are lost.
	 */
	SANITIZED_RICH_TEXT,
	
	/**
	 * Default. OpenOlat shows every edu-sharing item unchanged in its own iframe on
	 * the content domain, in the course element and in rich text fields. Script runs,
	 * but only on the content domain. These iframes are sandboxed, so the script cannot
	 * redirect the main window.
	 * <p>
	 * Pro: full edu-sharing functions and layout everywhere.<br>
	 * Con: one iframe per item, content in rich text does not flow with the text, the
	 * metadata box can be clipped, less accessible.
	 */
	ORIGINAL_IFRAME
	
}
