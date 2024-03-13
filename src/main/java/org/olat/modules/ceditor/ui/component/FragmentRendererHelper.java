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
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.ImageHorizontalAlignment;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.jpa.CodePart;
import org.olat.modules.ceditor.model.jpa.MathPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.model.jpa.TablePart;
import org.olat.modules.forms.model.xml.Disclaimer;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.HTMLParagraph;
import org.olat.modules.forms.model.xml.Image;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.Table;
import org.olat.modules.forms.model.xml.TextInput;

/**
 * Initial date: 2024-03-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FragmentRendererHelper {

	public static AlertBoxSettings getAlertBoxSettingsIfActive(PageElement element) {
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(element);
		if (alertBoxSettings == null) {
			return null;
		}
		if (!alertBoxSettings.isShowAlertBox()) {
			return null;
		}
		return alertBoxSettings;
	}

	public static AlertBoxSettings getAlertBoxSettings(PageElement element) {
		if (element instanceof MediaPart mediaPart) {
			if (mediaPart.getImageSettings() != null) {
				return mediaPart.getImageSettings().getAlertBoxSettings();
			}
			if (mediaPart.getMediaSettings() != null) {
				return mediaPart.getMediaSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof Image image) {
			if (image.getImageSettings() != null) {
				return image.getImageSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof ParagraphPart paragraphPart) {
			if (paragraphPart.getTextSettings() != null) {
				return paragraphPart.getTextSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof HTMLParagraph htmlParagraph) {
			if (htmlParagraph.getTextSettings() != null) {
				return htmlParagraph.getTextSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof TablePart tablePart) {
			if (tablePart.getTableSettings() != null) {
				return tablePart.getTableSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof MathPart mathPart) {
			if (mathPart.getMathSettings() != null) {
				return mathPart.getMathSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof CodePart codePart) {
			if (codePart.getSettings() != null) {
				return codePart.getSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof Table table) {
			if (table.getTableSettings() != null) {
				return table.getTableSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof Rubric rubric) {
			return rubric.getAlertBoxSettings();
		}
		if (element instanceof SingleChoice singleChoice) {
			return singleChoice.getAlertBoxSettings();
		}
		if (element instanceof MultipleChoice multipleChoice) {
			return multipleChoice.getAlertBoxSettings();
		}
		if (element instanceof TextInput textInput) {
			return textInput.getAlertBoxSettings();
		}
		if (element instanceof FileUpload fileUpload) {
			return fileUpload.getAlertBoxSettings();
		}
		if (element instanceof Disclaimer disclaimer) {
			return disclaimer.getAlertBoxSettings();
		}
		if (element instanceof SessionInformations sessionInformation) {
			return sessionInformation.getAlertBoxSettings();
		}
		return null;
	}

	public static void renderAlertHeader(StringOutput sb, String fragmentId, ContainerSettings settings, boolean inForm) {
		renderAlertHeader(sb, fragmentId, null, settings.getAlertBoxSettings(), settings.getNumOfBlocks(), inForm);
	}

	public static void renderAlertHeader(StringOutput sb, String fragmentId, BlockLayoutSettings layoutSettings,
										 AlertBoxSettings alertBoxSettings, int numberOfItems, boolean inForm) {
		boolean showAlert = alertBoxSettings != null && alertBoxSettings.isShowAlertBox();
		String title = showAlert ? alertBoxSettings.getTitle() : null;
		String iconCssClass = showAlert ? alertBoxSettings.getIconCssClass() : null;
		boolean showTitle = StringHelper.containsNonWhitespace(title);
		boolean showIcon = showAlert && alertBoxSettings.isWithIcon() && iconCssClass != null;
		boolean showAlertHeader = showTitle || showIcon;
		boolean collapsible = showTitle && alertBoxSettings.isCollapsible();

		if (showAlertHeader) {
			sb.append("<div class='o_container_block o_alert_header");
			sb.append(" o_in_form", inForm);
			sb.append("' style='grid-column: 1 / -1;'>");
			sb.append("<div class='o_container_block_alert'>");
			if (showIcon) {
				sb.append("<div class='o_alert_icon'><i class='o_icon o_icon-lg ").append(iconCssClass).append("'> </i></div>");
			}
			if (showTitle) {
				if (collapsible) {
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

	public static boolean isCollapsible(PageElement pageElement) {
		return isCollapsible(getAlertBoxSettingsIfActive(pageElement));
	}

	public static boolean isCollapsible(ContainerSettings containerSettings) {
		AlertBoxSettings alertBoxSettings = containerSettings.getAlertBoxSettingsIfActive();
		return isCollapsible(alertBoxSettings);
	}

	public static boolean isCollapsible(AlertBoxSettings alertBoxSettings) {
		boolean showAlert = alertBoxSettings != null;
		String title = showAlert ? alertBoxSettings.getTitle() : null;
		boolean showTitle = StringHelper.containsNonWhitespace(title);
		return showTitle && alertBoxSettings.isCollapsible();
	}

	public static BlockLayoutSettings getLayoutSettings(PageElement element) {
		if (element instanceof MediaPart mediaPart) {
			if (mediaPart.getImageSettings() != null) {
				return getLayoutSettings(mediaPart.getImageSettings().getLayoutSettings());
			}
			if (mediaPart.getMediaSettings() != null) {
				return getLayoutSettings(mediaPart.getMediaSettings().getLayoutSettings());
			}
		}
		if (element instanceof Image image) {
			if (image.getImageSettings() != null) {
				return getLayoutSettings(image.getImageSettings().getLayoutSettings());
			}
		}
		if (element instanceof ParagraphPart paragraphPart) {
			if (paragraphPart.getTextSettings() != null) {
				return getLayoutSettings(paragraphPart.getTextSettings().getLayoutSettings());
			}
		}
		if (element instanceof HTMLParagraph htmlParagraph) {
			if (htmlParagraph.getTextSettings() != null) {
				return getLayoutSettings(htmlParagraph.getTextSettings().getLayoutSettings());
			}
		}
		if (element instanceof TablePart tablePart) {
			if (tablePart.getTableSettings() != null) {
				return getLayoutSettings(tablePart.getTableSettings().getLayoutSettings());
			}
		}
		if (element instanceof MathPart mathPart) {
			if (mathPart.getMathSettings() != null) {
				return getLayoutSettings(mathPart.getMathSettings().getLayoutSettings());
			}
		}
		if (element instanceof CodePart codePart) {
			if (codePart.getSettings() != null) {
				return getLayoutSettings(codePart.getSettings().getLayoutSettings());
			}
		}
		if (element instanceof Table table) {
			if (table.getTableSettings() != null) {
				return getLayoutSettings(table.getTableSettings().getLayoutSettings());
			}
		}
		if (element instanceof Rubric rubric) {
			return getLayoutSettings(rubric.getLayoutSettings());
		}
		if (element instanceof SingleChoice singleChoice) {
			return getLayoutSettings(singleChoice.getLayoutSettings());
		}
		if (element instanceof MultipleChoice multipleChoice) {
			return getLayoutSettings(multipleChoice.getLayoutSettings());
		}
		if (element instanceof TextInput textInput) {
			return getLayoutSettings(textInput.getLayoutSettings());
		}
		if (element instanceof FileUpload fileUpload) {
			return getLayoutSettings(fileUpload.getLayoutSettings());
		}
		if (element instanceof Disclaimer disclaimer) {
			return getLayoutSettings(disclaimer.getLayoutSettings());
		}
		if (element instanceof SessionInformations sessionInformation) {
			return getLayoutSettings(sessionInformation.getLayoutSettings());
		}
		return null;
	}

	private static BlockLayoutSettings getLayoutSettings(BlockLayoutSettings layoutSettings) {
		if (layoutSettings != null) {
			return layoutSettings;
		}
		return BlockLayoutSettings.getPredefined();
	}

	public static boolean needsSelectionFrame(PageElement element) {
		if (element instanceof Image image) {
			return needsSelectionFrame(image.getImageSettings());
		}
		if (element instanceof MediaPart mediaPart) {
			if (mediaPart.getImageSettings() != null) {
				return needsSelectionFrame(mediaPart.getImageSettings());
			}
		}
		return false;
	}

	public static boolean needsSelectionFrame(ImageSettings imageSettings) {
		if (imageSettings != null) {
			ImageHorizontalAlignment alignment = imageSettings.getAlignment();
			if (alignment != null) {
				return alignment == ImageHorizontalAlignment.leftfloat ||
						alignment == ImageHorizontalAlignment.rightfloat;
			}
		}
		return false;
	}
}
