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
package org.olat.modules.forms.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 05.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReportHelper {
	
	private static final String DEFAULT_COLOR = "#777";
	private static final String[] DEFAULT_COLORS = new String[]{
			"#EDC951", "#CC333F", "#00A0B0", "#4E4E6C", "#8DC1A1",
			"#F7BC00", "#BB6511", "#B28092", "#003D40", "#FF69D1"
	};
	
	private final Legend anonymousLegend;
	private final ColorGenerator colorGenerator;
	
	private final Map<EvaluationFormSession, Legend> sessionToData = new HashMap<>();
	private final Map<EvaluationFormParticipation, Legend> participationToLegend = new HashMap<>();
	private final Map<Identity, Legend> executorToLegend = new HashMap<>();
	
	private UserManager userManager;
	
	private ReportHelper(Builder builder) {
		this.userManager = UserManager.getInstance();
		
		String anonymousName;
		if (StringHelper.containsNonWhitespace(builder.anonymousName)) {
			anonymousName = builder.anonymousName;
		} else {
			Translator translator = Util.createPackageTranslator(ReportHelper.class, builder.locale);
			anonymousName = translator.translate("report.anonymous.user");
		}
		String anonymousColor = builder.anonymousColor;
		if (!StringHelper.containsNonWhitespace(anonymousColor)) {
			anonymousColor = DEFAULT_COLOR;
		}
		this.anonymousLegend = new Legend(anonymousName, anonymousColor);
		
		String[] colors;
		if (builder.hasColors) {
			colors = DEFAULT_COLORS;
		} else {
			colors = new String[] { DEFAULT_COLOR };
		}
		this.colorGenerator = new ColorGenerator(colors);
	}
	
	/**
	 * Use this method only for testing purposes.
	 *
	 * @param userManager
	 */
	void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
	
	Legend getLegend(EvaluationFormSession session) {
		Legend legend = sessionToData.get(session);
		if (legend == null && session.getParticipation() != null) {
			legend = participationToLegend.get(session.getParticipation());
			if (legend == null && session.getParticipation().getExecutor() != null) {
				legend = executorToLegend.get(session.getParticipation().getExecutor());
			}
		}
		if (legend == null) {
			legend = addLegend(session);
		}
		if (legend == null) {
			legend = anonymousLegend;
		}
		return legend;
	}
	
	private Legend addLegend(EvaluationFormSession session) {
		if (session.getParticipation() != null && session.getParticipation().getExecutor() != null) {
			Identity executor = session.getParticipation().getExecutor();
			String name = userManager.getUserDisplayName(executor);
			String color = colorGenerator.getColor();
			Legend legend = new Legend(name, color);
			sessionToData.put(session, legend);
			participationToLegend.put(session.getParticipation(), legend);
			executorToLegend.put(executor, legend);
			return legend;
		}
		return null;
	}

	public static Builder builder(Locale locale) {
		return new Builder(locale);
	}
	
	public static class Builder {
		
		private Locale locale;
		private String anonymousName = null;
		private String anonymousColor = null;
		private boolean hasColors = false;
		
		Builder(Locale locale) {
			this.locale = locale;
		}
		
		public Builder withAnonymousName(String name) {
			this.anonymousName = name;
			return this;
		}
		
		public Builder withAnonymousColor(String color) {
			this.anonymousColor = color;
			return this;
		}
		
		public Builder withColors() {
			this.hasColors = true;
			return this;
		}
		
		public ReportHelper build() {
			return new ReportHelper(this);
		}
	}
	
	final static class Legend {
		
		private final String name;
		private final String color;
		
		public Legend(String name, String color) {
			this.name = name;
			this.color = color;
		}

		public String getName() {
			return name;
		}

		public String getColor() {
			return color;
		}
	}

}
