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

import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.CodeSettings;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.MathSettings;
import org.olat.modules.ceditor.model.MediaSettings;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.model.TitleSettings;

/**
 * Initial date: 2024-01-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BlockLayoutClassFactory {

	public static String buildClass(CodeSettings settings, boolean inForm) {
		if (settings == null) {
			return defaultClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), inForm);
	}

	public static String buildClass(ImageSettings settings, boolean inForm) {
		if (settings == null) {
			return defaultClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), inForm);
	}

	public static String buildClass(MathSettings settings, boolean inForm) {
		if (settings == null) {
			return defaultClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), inForm);
	}

	public static String buildClass(MediaSettings settings, boolean inForm) {
		if (settings == null) {
			return defaultClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), inForm);
	}

	public static String buildClass(TableSettings settings, boolean inForm) {
		if (settings == null) {
			return defaultClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), inForm);
	}

	public static String buildClass(TextSettings settings, boolean inForm) {
		if (settings == null) {
			return defaultClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), inForm);
	}

	public static String buildClass(TitleSettings settings, boolean inForm) {
		if (settings == null) {
			return defaultClass(inForm);
		}
		return buildClass(settings.getLayoutSettings(), inForm);
	}

	public static String defaultClass(boolean inForm) {
		return BlockLayoutSettings.getDefaults(inForm).getCssClass();
	}

	public static String buildClass(BlockLayoutSettings blockLayoutSettings, boolean inForm) {
		if (blockLayoutSettings == null) {
			return defaultClass(inForm);
		}
		return blockLayoutSettings.getCssClass();
	}
}
