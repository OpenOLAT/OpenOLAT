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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.AlertBoxType;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.GeneralStyleSettings;
import org.olat.modules.ceditor.model.ImageHorizontalAlignment;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.jpa.CodePart;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.model.jpa.MathPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.model.jpa.TablePart;
import org.olat.modules.ceditor.ui.model.PageFragment;
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
		if (element instanceof QuizPart quizPart) {
			if (quizPart.getSettings() != null) {
				return quizPart.getSettings().getAlertBoxSettings();
			}
		}
		if (element instanceof GalleryPart galleryPart) {
			if (galleryPart.getSettings() != null) {
				return galleryPart.getSettings().getAlertBoxSettings();
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

	public static Set<String> getContainerElementIdsWithSpacingAfter(PageFragmentsComponent pageFragmentsComponent) {
		List<? extends PageFragment> fragments = pageFragmentsComponent.getFragments();
		List<ContainerElement> containerElements = fragments.stream()
				.filter(f -> f.getPageElement() instanceof ContainerElement)
				.map(f -> (ContainerElement) f.getPageElement()).toList();
		Set<String> containerIds = new HashSet<>();
		for (int i = 0; i < (containerElements.size() - 1); i++) {
			ContainerElement containerElement = containerElements.get(i);
			ContainerElement containerElementAfter = containerElements.get(i + 1);
			if (isContainerElementWithSpacing(containerElement) && isContainerElementWithSpacing(containerElementAfter)) {
				containerIds.add(containerElement.getId());
			}
		}
		return containerIds;
	}

	private static boolean isContainerElementWithSpacing(ContainerElement containerElement) {
		ContainerSettings containerSettings = containerElement.getContainerSettings();
		if (containerSettings == null) {
			return false;
		}
		GeneralStyleSettings generalStyleSettings = containerSettings.getGeneralStyleSettings();
		if (generalStyleSettings == null) {
			return false;
		}
		return generalStyleSettings.getBackgroundColor() != null && generalStyleSettings.isShowSpacing();
	}

	public static void renderContainerLayoutClasses(StringOutput sb, ContainerSettings settings, boolean applyLayoutSpacingAfter) {
		AlertBoxSettings alertBoxSettings = settings.getAlertBoxSettingsIfActive();
		boolean showAlert = alertBoxSettings != null;
		AlertBoxType alertBoxType = showAlert ? alertBoxSettings.getType() : null;
		String alertBoxColor = showAlert ? alertBoxSettings.getColor() : null;
		ContainerLayout layout = settings.getType();
		GeneralStyleSettings generalStyleSettings = settings.getGeneralStyleSettings();
		boolean applyBackgroundColor = generalStyleSettings != null && generalStyleSettings.getBackgroundColor() != null;
		boolean applyLayoutSpacing = generalStyleSettings != null && generalStyleSettings.isShowSpacing();

		sb.append(" o_page_layout ").append(layout.cssClass());

		if (showAlert) {
			sb.append(" o_alert_box_active ").append(alertBoxType.getCssClass(alertBoxColor));
		}
		if (applyLayoutSpacing) {
			sb.append(" o_apply_layout_spacing");
		}
		if (applyLayoutSpacingAfter) {
			sb.append(" o_apply_layout_spacing_after");
		}
		if (applyBackgroundColor) {
			sb.append(" o_apply_background_color");
		}
	}

	public static void renderContainerBlockDivOpen(StringOutput sb, String mainDivClass, ContainerSettings settings,
												   boolean collapsible, String fragmentName) {
		GeneralStyleSettings generalStyleSettings = settings.getGeneralStyleSettings();
		boolean withBackgroundColor = generalStyleSettings != null && generalStyleSettings.getBackgroundColor() != null;

		sb.append("<div class='").append(mainDivClass);

		if (withBackgroundColor) {
			sb.append(" ").append("o_color_background_10 ").append("o_color_").append(generalStyleSettings.getBackgroundColor());
		}

		if (collapsible) {
			sb.append(" collapse in ").append(buildCollapsibleClass(fragmentName));
		}
		sb.append("'");

		if (collapsible) {
			sb.append(" aria-expanded='true'");
		}

		sb.append(">");
	}

	public static void renderAlertHeader(StringOutput sb, String fragmentId, ContainerSettings settings, boolean inForm) {
		renderAlertHeader(sb, fragmentId, settings.getAlertBoxSettings(), settings.getNumOfBlocks(), inForm);
	}

	public static void renderAlertHeaderWithAbsolutePositionCheck(StringOutput sb, String fragmentId, PageElement element, int numberOfItems, boolean inForm) {
		if (needsAbsolutePositionAlertDiv(element)) {
			return;
		}
		renderAlertHeader(sb, fragmentId, getAlertBoxSettings(element), numberOfItems, inForm);
	}

	public static void renderAlertHeader(StringOutput sb, String fragmentId, AlertBoxSettings alertBoxSettings,
										 int numberOfItems, boolean inForm) {
		renderAlertHeader(sb, fragmentId, alertBoxSettings, numberOfItems, inForm, false);
	}

	public static void renderAlertHeader(StringOutput sb, String fragmentId, AlertBoxSettings alertBoxSettings,
										 int numberOfItems, boolean inForm, boolean disableCollapsible) {
		boolean showAlert = alertBoxSettings != null && alertBoxSettings.isShowAlertBox();
		String title = showAlert ? alertBoxSettings.getTitle() : null;
		String iconCssClass = showAlert ? alertBoxSettings.getIconCssClass() : null;
		boolean showTitle = StringHelper.containsNonWhitespace(title);
		boolean showIcon = showAlert && alertBoxSettings.isWithIcon() && iconCssClass != null;
		boolean showAlertHeader = showTitle || showIcon;
		boolean collapsible = !disableCollapsible && showTitle && alertBoxSettings.isCollapsible();

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
			return needsSelectionFrame(mediaPart.getImageSettings());
		}
		return false;
	}

	private static boolean needsSelectionFrame(ImageSettings imageSettings) {
		if (imageSettings != null) {
			ImageHorizontalAlignment alignment = imageSettings.getAlignment();
			if (alignment != null) {
				return alignment == ImageHorizontalAlignment.leftfloat ||
						alignment == ImageHorizontalAlignment.rightfloat;
			}
		}
		return false;
	}

	public static boolean needsAbsolutePositionAlertDiv(PageElement element) {
		if (element instanceof Image image) {
			return needsAbsolutePositionAlertDiv(image.getImageSettings());
		}
		if (element instanceof MediaPart mediaPart) {
			return needsAbsolutePositionAlertDiv(mediaPart.getImageSettings());
		}
		return false;
	}

	private static boolean needsAbsolutePositionAlertDiv(ImageSettings imageSettings) {
		if (imageSettings == null) {
			return false;
		}
		ImageHorizontalAlignment alignment = imageSettings.getAlignment();
		if (alignment == null) {
			return false;
		}
		AlertBoxSettings alertBoxSettings = imageSettings.getAlertBoxSettings();
		if (alertBoxSettings == null || !alertBoxSettings.isShowAlertBox()) {
			return false;
		}
		return alignment == ImageHorizontalAlignment.leftfloat || alignment == ImageHorizontalAlignment.rightfloat;
	}

	private static ImageHorizontalAlignment getHorizontalAlignment(PageElement element) {
		if (element instanceof Image image) {
			return getHorizontalAlignment(image.getImageSettings());
		}
		if (element instanceof MediaPart mediaPart) {
			return getHorizontalAlignment(mediaPart.getImageSettings());
		}
		return null;
	}

	private static ImageHorizontalAlignment getHorizontalAlignment(ImageSettings imageSettings) {
		if (imageSettings == null) {
			return null;
		}
		return imageSettings.getAlignment();
	}

	public static void renderSelectionFrame(StringOutput sb, String dispatchId, PageElement element) {
		if (!needsSelectionFrame(element)) {
			return;
		}

		sb.append("<div id='")
				.append(frameId(dispatchId)).append("' class='o_fragment_selection_frame'")
				.append("></div>");
	}

	public static void renderSelectionFrameJavaScript(StringOutput sb, String dispatchId, PageElement element) {
		if (!needsSelectionFrame(element)) {
			return;
		}
		boolean alertDivPresent = needsAbsolutePositionAlertDiv(element);
		boolean leftFloat = ImageHorizontalAlignment.leftfloat.equals(getHorizontalAlignment(element));
		int xOffset = leftFloat ? -4 : 4;

		sb.append("var xOffset = " + xOffset + ";\n", alertDivPresent);
		sb.append("var frameDiv = jQuery('#").append(frameId(dispatchId)).append("');\n");
		sb.append("var imageDiv = frameDiv.parent().find('div.o_image');\n");
		sb.append("setTimeout(() => {\n");
		sb.append(" frameDiv.width(imageDiv.innerWidth() - 4);\n", !alertDivPresent);
		sb.append(" frameDiv.width(imageDiv.innerWidth() + 4);\n", alertDivPresent);
		sb.append(" frameDiv.height(imageDiv.innerHeight() - 4);\n", !alertDivPresent);
		sb.append(" frameDiv.height(imageDiv.innerHeight() + 40);\n", alertDivPresent);
		sb.append(" var top = imageDiv.position().top + 'px';\n");
		sb.append(" var left = imageDiv.position().left + 'px';\n", !alertDivPresent);
		sb.append(" var left = (imageDiv.position().left - 4 + xOffset) + 'px';\n", alertDivPresent);
		sb.append(" frameDiv.css({top: top, left: left});\n");
		sb.append("}, 250);\n");
	}

	private static String frameId(String dispatchId) {
		return "o_c" + dispatchId + "_frame";
	}

	public static void renderAbsolutePositionAlertDiv(StringOutput sb, String fragmentId, String dispatchId, PageElement element, int numberOfItems, boolean inForm) {
		if (!needsAbsolutePositionAlertDiv(element)) {
			return;
		}
		sb.append("<div id='")
				.append(alertDivId(dispatchId)).append("' ");
		sb.append("style='position: absolute; z-index: 1;' >");
		renderAlertHeader(sb, fragmentId, getAlertBoxSettings(element), numberOfItems, inForm, true);
		sb.append("</div>");
	}

	private static String alertDivId(String dispatchId) {
		return "o_c" + dispatchId + "_alert_div";
	}

	public static void renderAbsolutePositionAlertDivScript(StringOutput sb, String dispatchId, PageElement element) {
		if (!needsAbsolutePositionAlertDiv(element)) {
			return;
		}
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(element);
		String alertDivCss = alertBoxSettings.getType().getCssClass(alertBoxSettings.getColor());
		boolean leftFloat = ImageHorizontalAlignment.leftfloat.equals(getHorizontalAlignment(element));
		int xOffset = leftFloat ? -4 : 4;
		sb.append("var alertDiv = jQuery('#").append(alertDivId(dispatchId)).append("');\n");
		sb.append("var parentDiv = alertDiv.parent();\n");
		sb.append("parentDiv.css({border: 'none', backgroundColor: 'unset'});\n");
		sb.append("var xOffset = ").append(xOffset).append(";\n");
		sb.append("var imageDiv = parentDiv.find('div.o_image');\n");
		sb.append("setTimeout(() => {\n");
		sb.append(" alertDiv.width(imageDiv.innerWidth());\n");
		sb.append(" alertDiv.height(imageDiv.innerHeight() - 4 + 40);\n");
		sb.append(" var top = imageDiv.position().top + 'px';\n");
		sb.append(" var left = (imageDiv.position().left + xOffset) + 'px';\n");
		sb.append(" alertDiv.css({top: top, left: left});\n");
		sb.append(" alertDiv.addClass('o_alert_div o_alert_box_active ").append(alertDivCss).append("');\n");
		sb.append(" imageDiv.css({position: 'relative', zIndex: 2, marginTop: '40px', marginLeft: (xOffset + 'px'), marginRight: (-xOffset + 'px')});\n");
		sb.append("}, 250);\n");
	}
}
