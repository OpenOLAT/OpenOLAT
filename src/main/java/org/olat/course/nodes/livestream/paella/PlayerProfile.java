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
package org.olat.course.nodes.livestream.paella;

import org.olat.core.gui.render.StringOutput;

/**
 * 
 * Initial date: 17 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum PlayerProfile {
	
	both("player.profile.both") {
		@Override
		public void appendPlayerConfig(StringOutput sb) {
			sb.append("      'es.upv.paella.singleStreamProfilePlugin': {");
			sb.append("          'enabled': true,");
			sb.append("          'videoSets': [");
			sb.append("            { 'icon':'professor_icon.svg', 'id':'professor', 'content':['stream1']},");
			sb.append("            { 'icon':'slide_icon.svg', 'id':'slide', 'content':['stream2']}");
			sb.append("          ]");
			sb.append("      },");
			sb.append("      'es.upv.paella.dualStreamProfilePlugin': { 'enabled':true,");
			sb.append("        'videoSets': [");
			sb.append("          { 'icon':'slide_professor_icon.svg', 'id':'slide_over_professor', 'content':['stream1','stream2'] }");
			sb.append("        ]");
			sb.append("      },");
		}

		@Override
		public String[] filterUrls(String[] urls) {
			return urls;
		}
	},
	stream1("player.profile.stream1") {
		@Override
		public void appendPlayerConfig(StringOutput sb) {
			sb.append("      'es.upv.paella.singleStreamProfilePlugin': {");
			sb.append("          'enabled': true,");
			sb.append("          'videoSets': [");
			sb.append("            { 'icon':'professor_icon.svg', 'id':'professor', 'content':['stream1']}");
			sb.append("          ]");
			sb.append("      },");
		}

		@Override
		public String[] filterUrls(String[] urls) {
			return new String[] { urls[0] };
		}
	},
	stream2("player.profile.stream2") {
		@Override
		public void appendPlayerConfig(StringOutput sb) {
			sb.append("      'es.upv.paella.singleStreamProfilePlugin': {");
			sb.append("          'enabled': true,");
			sb.append("          'videoSets': [");
			sb.append("            { 'icon':'slide_icon.svg', 'id':'slide', 'content':['stream1']}");
			sb.append("          ]");
			sb.append("      },");
		}

		@Override
		public String[] filterUrls(String[] urls) {
			return urls.length > 1
					? new String[] { urls[1] }
					: new String[] { urls[0] };
		}
	};
	
	private final String i18nKey;

	private PlayerProfile(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public abstract void appendPlayerConfig(StringOutput sb);

	public abstract String[] filterUrls(String[] urls);

}
