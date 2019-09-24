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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;

/**
 * Initial Date:  9 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface MarkingService {
	
	public MarkManager getMarkManager();
	
	
	/**
	 * Return the MarkController of a resource, check if the resource is marked or not.
	 * @param ureq
	 * @param wControl
	 * @param ores
	 * @param subPath
	 * @param businessPath
	 * @return
	 */
	public Controller getMarkController(UserRequest ureq, WindowControl wControl, OLATResourceable ores,  String subPath, String businessPath);

	/**
	 * Return the MarkController of a mark. The resource is considered as marked because a mark exists.
	 * @param ureq
	 * @param wControl
	 * @param mark (cannot be null)
	 * @return
	 */
	public Controller getMarkController(UserRequest ureq, WindowControl wControl, Mark mark);
	
	/**
	 * Return the MarkController of a mark. The resource is considered as marked if the parameter mark is not null.
	 * The controller don't check if the resource is marked or not.
	 * @param ureq
	 * @param wControl
	 * @param mark the mark (can be null)
	 * @param ores (cannot be null)
	 * @param subPath (cannot be null)
	 * @param businessPath (cannot be null)
	 * @return
	 */
	public Controller getMarkController(UserRequest ureq, WindowControl wControl, Mark mark, MarkResourceStat stat, OLATResourceable ores,  String subPath, String businessPath);
}
