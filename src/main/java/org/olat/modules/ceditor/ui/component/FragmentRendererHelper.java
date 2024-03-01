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
package org.olat.modules.ceditor.ui.component;

import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.ContainerSettings;

/**
 * Initial date: 2024-03-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FragmentRendererHelper {

	public static void renderAlertHeader(StringOutput sb, String fragmentId, ContainerSettings settings, URLBuilder ubu) {
		AlertBoxSettings alertBoxSettings = settings.getAlertBoxSettings();
		boolean showAlert = alertBoxSettings != null && alertBoxSettings.isShowAlertBox();
		String title = showAlert ? alertBoxSettings.getTitle() : null;
		String iconCssClass = showAlert ? alertBoxSettings.getIconCssClass() : null;
		boolean showTitle = StringHelper.containsNonWhitespace(title);
		boolean showIcon = showAlert && alertBoxSettings.isWithIcon() && iconCssClass != null;
		boolean showAlertHeader = showTitle || showIcon;
		boolean collapsible = showTitle && alertBoxSettings.isCollapsible();

		if (showAlertHeader) {
			sb.append("<div class='o_container_block' style='grid-column: 1 / -1;'>");
			sb.append("<div class='o_container_block_alert'>");
			if (showIcon) {
				sb.append("<div class='o_alert_icon'><i class='o_icon ").append(iconCssClass).append("'> </i></div>");
			}
			if (showTitle) {
				if (collapsible) {
					int numberOfItems = settings.getNumOfBlocks();
					String collapsingId = FragmentRendererHelper.buildCollapsingId(fragmentId);
					FragmentRendererHelper.openCollapseLink(null, collapsingId, sb, fragmentId, numberOfItems, "o_alert_text o_alert_collapse_title");
					sb.append(title).append("</a>");
					FragmentRendererHelper.openCollapseLink(collapsingId, collapsingId, sb, fragmentId, numberOfItems,"o_alert_collapse_icon");
					sb.append("<i class='o_icon o_icon-lg o_icon_details_expand' aria-hidden='true'> </i>");
					sb.append("<i class='o_icon o_icon-lg o_icon_details_collaps' aria-hidden='true'> </i>");
					sb.append("</a>");
				} else {
					sb.append("<div class='o_alert_text'>")
							.append(title)
							.append("</div>");
				}
			}
			sb.append("</div>");
			sb.append("</div>");
		}
	}

	public static String buildCollapsibleClass(String fragmentName) {
		return "collapsible-class-" + fragmentName;
	}

	public static String buildCollapsibleIds(String fragmentName, int numberOfItems) {
		try (StringOutput sb = new StringOutput()) {
			for (int i = 0; i < numberOfItems; i++) {
				sb.append(" ", sb.length() > 0).append("collapsible-item-").append(fragmentName).append("-").append(i + 1);
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public static String buildCollapsingId(String fragmentName) {
		return "collapsing-" + fragmentName;
	}

	public static void openCollapseLink(String id, String collapsingId, StringOutput sb, String fragmentName,
										int numberOfItems, String extraClasses) {
		sb.append("<a ")
				.append("id='" + id + "' ", StringHelper.containsNonWhitespace(id))
				.append("onclick=\"document.getElementById('").append(collapsingId).append("').classList.toggle('collapsed');\" ")
				.append("href='.").append(buildCollapsibleClass(fragmentName)).append("' ")
				.append("role='button' data-toggle='collapse' aria-controls='")
				.append(buildCollapsibleIds(fragmentName, numberOfItems)).append("'")
				.append("aria-expanded='false' ")
				.append("class='").append(extraClasses).append("'>");
	}

	public static boolean isCollapsible(ContainerSettings containerSettings) {
		AlertBoxSettings alertBoxSettings = containerSettings.getAlertBoxSettingsIfActive();
		boolean showAlert = alertBoxSettings != null;
		String title = showAlert ? alertBoxSettings.getTitle() : null;
		boolean showTitle = StringHelper.containsNonWhitespace(title);
		return showTitle && alertBoxSettings.isCollapsible();
	}
}
