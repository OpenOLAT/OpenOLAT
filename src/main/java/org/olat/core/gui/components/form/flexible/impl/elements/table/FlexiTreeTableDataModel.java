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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.List;

/**
 * 
 * Initial date: 15 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 * @param <T>
 */
public interface FlexiTreeTableDataModel<T extends FlexiTreeTableNode> extends FlexiTableDataModel<T> {
	
	/**
	 * @return The total number of rows if the tree is fully opened.
	 */
	public int getTotalNodesCount();
	
	/**
	 * @param row The specified row
	 * @return The indentation of the specified row
	 */
	public int getIndentation(int row);
	
	/**
	 * @param row The specified row
	 * @return true if the row has some children
	 */
	public boolean hasChildren(int row);
	
	public boolean isOpen(int row);
	
	public void focus(int row);
	
	public void open(int row);
	
	public boolean hasOpenCloseAll();

	/**
	 * The table will call the openAll method and filter() right after.
	 * 
	 */
	public void openAll();
	
	public void close(int row);
	
	public void closeAll();
	
	public void popBreadcrumb(FlexiTreeTableNode node);
	
	public List<FlexiTreeTableNode> reloadBreadcrumbs(List<FlexiTreeTableNode> crumbs);
	
}
