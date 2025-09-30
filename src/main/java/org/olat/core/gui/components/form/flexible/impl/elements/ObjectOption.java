/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Sep 1, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface ObjectOption {
	
	String getKey();
	
	String getTitle();
	
	String getSubTitle();
	
	String getSubTitleFull();
	
	public class ObjectOptionValues implements ObjectOption {
		
		private final String key;
		private final String title;
		private final String subTitle;
		private final String subTitleFull;
		
		public ObjectOptionValues(String key, String title, String subTitle, String subTitleFull) {
			this.key = key;
			this.title = title;
			this.subTitle = subTitle;
			this.subTitleFull = subTitleFull;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getSubTitle() {
			return subTitle;
		}

		@Override
		public String getSubTitleFull() {
			return subTitleFull;
		}
		
	}
	
	public static String createShortPath(List<TreeNode> treePath) {
		StringBuilder sb = new StringBuilder();
		if (treePath.size() == 1) {
			sb.append(treePath.get(0).getTitle());
			sb.append(" /");
		} else if (treePath.size() == 2) {
			sb.append(treePath.get(0).getTitle());
			sb.append(" / ");
			sb.append(treePath.get(1).getTitle());
			sb.append(" /");
		} else if (treePath.size() > 2) {
			sb.append(treePath.get(0).getTitle());
			sb.append(" / ... / ");
			sb.append(treePath.get(treePath.size() - 1).getTitle());
			sb.append(" /");
		}
		return sb.toString();
	}
	
	public static String createFullPath(List<TreeNode> treePath) {
		String fillPath = treePath.stream().map(TreeNode::getTitle).collect(Collectors.joining(" / "));
		if (StringHelper.containsNonWhitespace(fillPath)) {
			fillPath += " /";
		}
		return fillPath;
	}

}
