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
package org.olat.core.gui.components.htmlheader.jscss;

/**
 * Description:<br>
 * This interface provides access to a custom CSS that might be used for sites, tabs or windows
 * 
 * <P>
 * Initial Date:  25.03.2009 <br>
 * @author gnaegi
 */
public interface CustomCSSProvider {
	
	/**
	 * Get the custom css object or NULL if no custom CSS is available
	 * @return
	 */
	public CustomCSS getCustomCSS();

}
