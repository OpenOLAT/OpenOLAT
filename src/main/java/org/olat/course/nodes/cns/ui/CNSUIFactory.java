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
package org.olat.course.nodes.cns.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.nodes.INode;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 23 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSUIFactory {
	
	public static final List<CourseNode> getChildNodes(CourseNode parent) {
		List<CourseNode> children = new ArrayList<>(parent.getChildCount());
		for (int i = 0; i < parent.getChildCount(); i++) {
			INode childNode = parent.getChildAt(i);
			if (childNode instanceof CourseNode courseNode) {
				children.add(courseNode);
			}
		}
		return children;
	}
	
	public static final String getConfigMessageParticipant(Translator translator, int requiredSelections) {
		String infos = "<div>" + translator.translate("config.overview.selection.hint") + "</div>";
		infos += "<br>";
		infos += getConfigMessage(translator, requiredSelections);
		return infos;
	}
	
	public static final String getConfigMessage(Translator translator, int requiredSelections) {
		String infos = "<ul class=\"list-unstyled\">";
		infos += "<li><span><i class=\"o_icon o o_icon-fw " + CNSCourseNode.ICON_CSS + "\"></i> "
				+ translator.translate("config.overview.required.selections", String.valueOf(requiredSelections)) + "</span></li>";
		infos += "</ul>";
		return infos;
	}

	public static String getSelected(Translator translator, int requiredSelections, int numSelections) {
		String selected = String.valueOf(numSelections);
		if (numSelections < requiredSelections) {
			selected = "<span title=\"" + translator.translate("participant.msg.too.few.selected")
					+ "\"><i class=\"o_icon o_icon_warn\"></i> "
					+ selected
					+ "</span>";
		}
		return selected;
	}

}
