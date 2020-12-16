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
package org.olat.course.run.tools;

public enum CourseTool {
	
	// All lower case to get better matching with manually written tool names by authors.
	blog("command.blog", "o_blog_icon"),
	documents("command.documents", "o_bc_icon"),
	efficiencystatement("command.efficiencystatement", "o_icon_certificate"),
	email("command.email", "o_co_icon"),
	forum("command.forum", "o_fo_icon"),
	participantlist("command.participant.list", "o_cmembers_icon"),
	participantinfos("command.participant.info", "o_infomsg_icon"),
	learningpath("command.learning.path", "o_icon_learning_path"),
	wiki("command.wiki", "o_wiki_icon"),
	teams("command.teams", "o_vc_icon"),
	bigbluebutton("command.bigbluebutton", "o_vc_icon");
	
	private final String i18nKey;
	private final String iconCss;

	private CourseTool(String i18nKey, String iconCss) {
		this.i18nKey = i18nKey;
		this.iconCss = iconCss;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public String getIconCss() {
		return iconCss;
	}
}