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
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/
package de.tuchemnitz.wizard.helper.catalog;

import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.course.ICourse;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.CatalogManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Description:<br>
 * Helper class for common catalog operations that are not existent in the
 * {@link org.olat.repository.manager.CatalogManager} yet.
 *
 * <P>
 * Initial Date: 12.12.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class CatalogHelper {

	/**
	 * Add a persisted course to the given catalog entry.
	 *
	 * @param course course object
	 * @param catEntry catalog entry
	 */
	public static final void addCourseToCatalogEntry(final ICourse course, final CatalogEntry catEntry) {
		OLATResource ores = OLATResourceManager.getInstance().findResourceable(course.getResourceableId(), course.getResourceableTypeName());
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry re = rm.lookupRepositoryEntry(ores, true);
		CatalogManager cm = CoreSpringFactory.getImpl(CatalogManager.class);
		CatalogEntry newLinkNotPersistedYet = cm.createCatalogEntry();
		newLinkNotPersistedYet.setName(re.getDisplayname());
		newLinkNotPersistedYet.setDescription(re.getDescription());
		newLinkNotPersistedYet.setRepositoryEntry(re);
		newLinkNotPersistedYet.setType(CatalogEntry.TYPE_LEAF);
		newLinkNotPersistedYet.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
		cm.addCatalogEntry(catEntry, newLinkNotPersistedYet);
	}

	protected static final TreeNode buildCatalogNode(final CatalogEntry rootEntry) {
		final CatalogManager cm = CoreSpringFactory.getImpl(CatalogManager.class);
		List<CatalogEntry> children = cm.getChildrenOf(rootEntry);

		GenericTreeNode ctn = new GenericTreeNode(rootEntry.getName(), rootEntry);
		ctn.setAccessible(true);

		for (int i = 0; i < children.size(); i++) {
			// add child itself
			CatalogEntry cchild = children.get(i);
			if (cchild.getType() == CatalogEntry.TYPE_NODE) {
				TreeNode ctchild = buildCatalogNode(cchild);
				((GenericTreeNode) ctchild).setAccessible(true);
				ctn.addChild(ctchild);
			}
		}

		return ctn;

	}

	/**
	 * Map the OLAT catalog structure to a new tree model.
	 *
	 * @param rootEntry root catalog entry
	 * @return tree model with catalog structure
	 */
	public static final TreeModel createCatalogTree(final CatalogEntry rootEntry) {
		final GenericTreeModel tm = new GenericTreeModel();
		tm.setRootNode(buildCatalogNode(rootEntry));
		return tm;
	}

	/**
	 * Create a path like "/19234817/19234819" from a specific catalog entry.
	 * @param catalogEntry
	 * @return
	 */
	public static final String getPath(CatalogEntry catalogEntry) {
		StringBuffer path = new StringBuffer();
		CatalogEntry gce = catalogEntry;
		while (gce.getParent() != null) {
			path.insert(0, "/"+gce.getKey().toString());
			gce = gce.getParent();
		}
		path.insert(0, "/"+gce.getKey().toString());
		return path.toString();
	}
}
