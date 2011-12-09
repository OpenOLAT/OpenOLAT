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
package org.olat.core.util.vfs.util;

import java.util.Comparator;

import org.olat.core.util.vfs.VFSItem;

/**
 * <p>
 * Orders VFSItems by their last modified date descendingly
 * <p>
 * Initial Date: Sep 16, 2009 <br>
 * 
 * @author gwassmann, gwassmann@frentix.com, www.frentix.com
 */
public class DescendingLastModifiedComparator implements Comparator<VFSItem> {
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(VFSItem o1, VFSItem o2) {
		long o1mod = o1.getLastModified();
		long o2mod = o2.getLastModified();
		return o1mod > o2mod ? -1 : 1;
	}
}