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
package org.olat.ims.cp.ui;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * Description:<br>
 * The filter exclude system files (start with .) and the CP specific standard file,
 * manifests and XSD.
 * 
 * <P>
 * Initial Date:  4 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff FXOLAT-125: virtual file system for CP
public class VFSMediaFilter implements VFSItemFilter {
	
	private static final Set<String> blackLists = new HashSet<>();
	
	static {
		blackLists.add("__macosx");
		blackLists.add("imscp_v1p1.xsd");
		blackLists.add("imsmanifest.xml");
		blackLists.add("imsmd_v1p2p2.xsd");
		blackLists.add("ims_xml.xsd");
	}
	
	private boolean filterHtml;
	
	public VFSMediaFilter(boolean filterHtml) {
		this.filterHtml = filterHtml;
	}

	@Override
	public boolean accept(VFSItem vfsItem) {
		String name = vfsItem.getName().toLowerCase();
		if(name.startsWith(".") || blackLists.contains(name)) {
			return false;
		}
		if(filterHtml && (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".xhtml"))) {
			return false;
		}
		return true;
	}
}
