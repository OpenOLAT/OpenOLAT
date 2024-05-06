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
package org.olat.modules.ceditor.ui;

import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.CodeSettings;
import org.olat.modules.ceditor.model.GallerySettings;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.MathSettings;
import org.olat.modules.ceditor.model.MediaSettings;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.model.TitleSettings;
import org.olat.modules.ceditor.model.jpa.MediaPart;

/**
 * Initial date: 2024-01-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BlockLayoutClassFactory {

	public static String buildClass(GallerySettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), settings.getAlertBoxSettings(), inForm);
	}

	public static String buildClass(QuizSettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), settings.getAlertBoxSettings(), inForm);
	}

	public static String buildClass(CodeSettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), settings.getAlertBoxSettings(), inForm);
	}

	public static String buildClass(ImageSettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), settings.getAlertBoxSettings(), inForm);
	}

	public static String buildClass(MathSettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), settings.getAlertBoxSettings(), inForm);
	}

	public static String buildClass(PageElement pageElement, boolean inForm) {
		if (pageElement == null) {
			return "";
		}
		if (pageElement instanceof MediaPart mediaPart) {
			return buildClass(mediaPart, inForm);
		}
		return "";
	}

	public static String buildClass(MediaPart mediaPart, boolean inForm) {
		if (mediaPart == null) {
			return "";
		}
		return buildClass(mediaPart.getMediaSettings(), inForm);
	}

	public static String buildClass(MediaSettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), settings.getAlertBoxSettings(), inForm);
	}

	public static String buildClass(TableSettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), settings.getAlertBoxSettings(), inForm);
	}

	public static String buildClass(TextSettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), settings.getAlertBoxSettings(), inForm);
	}

	public static String buildClass(TitleSettings settings, boolean inForm) {
		if (settings == null) {
			return getPredefinedCssClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), inForm);
	}

	private static String getPredefinedCssClass(boolean inForm) {
		return BlockLayoutSettings.getPredefined().getCssClass(inForm);
	}

	public static String buildClass(BlockLayoutSettings blockLayoutSettings, boolean inForm) {
		if (blockLayoutSettings == null) {
			return getPredefinedCssClass(inForm);
		}
		return blockLayoutSettings.getCssClass(inForm);
	}

	private static String buildClass(BlockLayoutSettings blockLayoutSettings, AlertBoxSettings alertBoxSettings, boolean inForm) {
		String prefix = alertBoxSettings != null && alertBoxSettings.isShowAlertBox() ? "o_alert_mode " : "";
		return prefix + buildClass(blockLayoutSettings, inForm);
	}
}
