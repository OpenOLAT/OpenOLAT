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
package org.olat.modules.ceditor.model;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * Initial date: 17 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContainerSettingsTest {

	@Test
	public void addColumns() {
		ContainerSettings settings = new ContainerSettings();
		settings.setNumOfColumns(4);
		settings.setElementAt("112", 3, null);
		
		Assert.assertEquals(4, settings.getNumOfBlocks());
		Assert.assertEquals(4, settings.getColumns().size());
		
		ContainerColumn column = settings.getColumn(3);
		Assert.assertNotNull(column);
		Assert.assertTrue(column.getElementIds().contains("112"));
	}
	
	
	@Test
	public void reduceColumns() {
		ContainerSettings settings = new ContainerSettings();
		settings.setType(ContainerLayout.block_3cols);
		settings.setElementAt("100", 0, null);
		settings.setElementAt("200", 1, null);
		
		Assert.assertEquals(3, settings.getNumOfBlocks());
		
		// check the second column
		ContainerColumn column = settings.getColumn(1);
		Assert.assertNotNull(column);
		Assert.assertTrue(column.getElementIds().contains("200"));

		// Reduce the number of slots
		settings.updateType(ContainerLayout.block_2cols);
		
		// Check first and second column after reduction
		ContainerColumn firstColumn = settings.getColumn(0);
		assertThat(firstColumn.getElementIds())
			.containsExactly("100");
		ContainerColumn secondColumn = settings.getColumn(1);
		assertThat(secondColumn.getElementIds())
			.containsExactly("200");
	}
}
