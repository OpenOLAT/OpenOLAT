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
package org.olat.selenium.page.graphene;

import java.util.function.Function;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Predicate which test if TinyMCE is fully loaded
 * 
 * Initial date: 07.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TinyMCELoadedPredicate implements Function<WebDriver, Boolean> {

	private static final Logger log = Tracing.createLoggerFor(TinyMCELoadedPredicate.class);
	
	@Override
	public Boolean apply(WebDriver driver) {
        try {
			Object active = ((JavascriptExecutor)driver)
					.executeScript("return window != null && typeof tinymce !== \"undefined\" && tinymce != null && tinymce.activeEditor != null "
							+ " && tinymce.activeEditor.initialized && tinymce.editors[0].initialized "
							+ " && (tinymce.editors.length > 1 ? tinymce.editors[1].initialized : true);");
			return Boolean.TRUE.equals(active);
		} catch (Exception e) {
			log.error("", e);
			return Boolean.FALSE;
		}
    }
}
