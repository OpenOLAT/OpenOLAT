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

package org.olat.core.commons.services.mark;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * Interface for bookmark
 * 
 * <P>
 * Initial Date:  9 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface Mark {
	
	public static final String MARK_CSS_ICON = "o_icon o_icon_bookmark";
	public static final String MARK_ADD_CSS_ICON = "o_icon o_icon_bookmark_add";
	
	public static final String MARK_CSS_LARGE = "o_icon o_icon_bookmark o_icon-lg";
	public static final String MARK_ADD_CSS_LARGE = "o_icon o_icon_bookmark_add o_icon-lg";
	
	public Long getKey();
	
	public boolean isMarked();
	
	public OLATResourceable getOLATResourceable();
	
	public String getResSubPath();
	
	public String getBusinessPath();
	
	public Identity getCreator();

}
