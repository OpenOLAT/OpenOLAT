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
package org.olat.modules.grade.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.Rounding;

/**
 * 
 * Initial date: 18 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeUIFactory {
	
	public static final DecimalFormat THREE_DIGITS = new DecimalFormat("#0.###", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final BigDecimal HALF = new BigDecimal("0.5");
	private static final BigDecimal QUARTER = new BigDecimal("0.25");
	private static final BigDecimal TENTH = new BigDecimal("0.1");
	
	public static String translateResolution(Translator translator, NumericResolution resolution) {
		return translator.translate("grade.system.resolution." + resolution.name());
	}

	public static String translateRounding(Translator translator, Rounding rounding) {
		return translator.translate("grade.system.rounding." + rounding.name());
	}
	
	public static String getGradeSystemI18nKey(GradeSystem gradeSystem) {
		return "grade.system.id." + gradeSystem.getIdentifier();
	}

	public static String translateGradeSystem(Translator translator, GradeSystem gradeSystem) {
		String i18nKey = getGradeSystemI18nKey(gradeSystem);
		String translation = translator.translate(i18nKey);
		if (i18nKey.equals(translation) || translation.length() > 256) {
			translation = String.valueOf(gradeSystem.getIdentifier());
		}
		return translation;
	}

	public static String getPerformanceClassI18nKey(String identifier) {
		return "performance.class." + identifier.toLowerCase();
	}
	
	public static String translatePerformanceClass(Translator translator, PerformanceClass performanceClass) {
		return translatePerformanceClass(translator, performanceClass.getIdentifier(), String.valueOf(performanceClass.getBestToLowest()));
	}
	
	public static String translatePerformanceClass(Translator translator, String identifier, String fallback) {
		if (!StringHelper.containsNonWhitespace(identifier)) return fallback;
		
		String i18nKey = getPerformanceClassI18nKey(identifier);
		String translation = translator.translate(i18nKey);
		if (i18nKey.equals(translation) || translation.length() > 256) {
			translation = fallback;
		}
		return translation;
	}
	
	public static String translatePerformanceClass(Translator translator, String identifier, String fallback, String gradeSystemIdent) {
		String translatePerformanceClass = translatePerformanceClass(translator, identifier, fallback);
		if (!StringHelper.containsNonWhitespace(gradeSystemIdent)) return translatePerformanceClass;
		
		String cssClass = "o_grs_" + gradeSystemIdent.replace(".", "_").toLowerCase();
		if (gradeSystemIdent.indexOf("percent") > 0) {
			cssClass += " o_gr_percent";
		}
		return "<span class='" + cssClass + "'>" + translatePerformanceClass + "</span>";
	}
	
	public static boolean validateInteger(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if (StringHelper.containsNonWhitespace(val)) {
				try {
					Integer.parseInt(val);
				} catch (NumberFormatException e) {
					el.setErrorKey("integer.element.int.error", null);
					allOk = false;
				}
			} else {
				el.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			}
		}
		return allOk;
	}
	
	public static boolean validateDouble(TextElement el, boolean mandatory) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if (StringHelper.containsNonWhitespace(val)) {
				try {
					Double.parseDouble(val);
				} catch (NumberFormatException e) {
					el.setErrorKey("error.double", null);
					allOk = false;
				}
			} else if (mandatory) {
				el.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			}
		}
		return allOk;
	}
	
	public static boolean validateBigDecimal(TextElement el, boolean mandatory, BigDecimal min, BigDecimal max) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if (StringHelper.containsNonWhitespace(val)) {
				try {
					BigDecimal value = new BigDecimal(val);
					if (min != null && max != null && (value.compareTo(min) < 0 || value.compareTo(max) > 0)) {
						el.setErrorKey("error.double.range", new String[] {THREE_DIGITS.format(min), THREE_DIGITS.format(max)});
						allOk = false;
					}
				} catch (NumberFormatException e) {
					el.setErrorKey("error.double", null);
					allOk = false;
				}
			} else if (mandatory) {
				el.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			}
		}
		return allOk;
	}
	
	public static boolean validateIdentifierChars(String s) {
		if (s == null || s.length() == 0) return false;
		
		return s.matches("^[a-z0-9\\.]*$");
	}
	
	public static boolean validateCutValue(TextElement el, NumericResolution resolution) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if (StringHelper.containsNonWhitespace(val)) {
				if (NumericResolution.whole == resolution) {
					try {
						Integer.parseInt(val);
					} catch (NumberFormatException e) {
						el.setErrorKey("integer.element.int.error", null);
						allOk = false;
					}
				} else {
					try {
						String value = THREE_DIGITS.format(Double.parseDouble(val));
						if (!validateCutValue(value, resolution)) {
							el.setErrorKey("error.digits", null);
							allOk = false;
						}
					} catch (NumberFormatException e) {
						el.setErrorKey("error.double", null);
						allOk = false;
					}
				}
			}
		}
		return allOk;
	}

	private static boolean validateCutValue(String value, NumericResolution resolution) {
		BigDecimal divisor = null;
		
		switch (resolution) {
		case half:
			divisor = HALF;
			break;
		case quarter:
			divisor = QUARTER;
			break;
		case tenth:
			divisor = TENTH;
			break;
		default:
			break;
		}
		
		if (divisor != null) {
			return new BigDecimal(value).remainder(divisor).compareTo(BigDecimal.ZERO) == 0;
		}
		
		return true;
	}

}
