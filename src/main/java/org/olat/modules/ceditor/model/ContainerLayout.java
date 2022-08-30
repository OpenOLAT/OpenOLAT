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

import java.util.List;

/**
 * 
 * Initial date: 15 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum ContainerLayout {
	
	block_1col("o_container_block_1_cols", 1, false),
	block_2cols("o_container_block_2_cols", 2, false),
	block_3cols("o_container_block_3_cols", 3, false),
	block_4cols("o_container_block_4_cols", 4, true),
	block_5cols("o_container_block_5_cols", 5, true),
	block_6cols("o_container_block_6_cols", 6, true),
	block_3rows("o_container_block_3_rows", 3, false),
	block_2_1rows("o_container_block_2_1_rows", 3, false),
	block_1_3rows("o_container_block_1_3_rows", 4, false),
	block_1_1lcols("o_container_block_1_1l_cols", 2, false),
	block_1_2rows("o_container_block_1_2_rows", 3, false),
	block_1_2cols("o_container_block_1_2_cols", 3, false);
	
	private final String cssClass;
	private final int numOfBlocks;
	private final boolean deprecated;
	
	private ContainerLayout(String cssClass, int numOfBlocks, boolean deprecated) {
		this.cssClass = cssClass;
		this.numOfBlocks = numOfBlocks;
		this.deprecated = deprecated;
	}
	
	public boolean deprecated() {
		return deprecated;
	}
	
	public String cssClass() {
		return cssClass;
	}
	
	public int numberOfBlocks() {
		return numOfBlocks;
	}
	
	public static ContainerLayout ofColumn(int numOfColumns, List<?> columns) {
		int val = Math.max(numOfColumns, (columns == null ? 0 : columns.size()));
		switch(val) {
			case -1:
			case 0:
			case 1: return block_1col;
			case 2: return block_2cols;
			case 3: return block_3cols;
			case 4: return block_4cols;
			case 5: return block_5cols;
			default: return block_6cols;
		}
	}
	
	public String pseudoIcons() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("<div class='o_page_layout ").append(cssClass()).append("'>");
		for(int i=numberOfBlocks(); i-->0; ) {
			sb.append("<div>&nbsp;</div>");
		}
		sb.append("</div>");
		return sb.toString();
	}
}
