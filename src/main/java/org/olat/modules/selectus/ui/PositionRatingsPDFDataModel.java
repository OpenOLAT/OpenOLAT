/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Country;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.ui.comparator.UserRatingComparator;
import org.olat.modules.selectus.ui.components.DefaultExportTableDataModel;

/**
 * 
 * Description:<br>
 * TableDataModel to export to PDF files
 * <P>
 * Initial Date:  3 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionRatingsPDFDataModel extends DefaultExportTableDataModel<ApplicationLight> {
	
	private static final PDFFields[] COLS = PDFFields.values();
	
	private final int numOfCommitteeMembers;
	private final Translator translator;
	private final List<UserRating> ratings;

	public PositionRatingsPDFDataModel(int numOfCommitteeMembers, List<ApplicationLight> rows, List<UserRating> ratings, Translator translator) {
		super(rows);
		this.numOfCommitteeMembers = numOfCommitteeMembers;
		this.ratings = ratings;
		this.translator = translator;
	}
	
	@Override
	public int getColumnCount() {
		return COLS.length;
	}
	
	@Override
	public String getHeader(int col) {
		PDFFields field = COLS[col];
		return translator.translate(field.key());
	}
	
	@Override
	public String getFieldNameAt(int col) {
		return COLS[col].name();
	}

	@Override
	public Class<?> getTypeAt(int row, int col) {
		if(col == PDFFields.yearOfBirthday.ordinal()) return Integer.class;
		if(col == PDFFields.ranking.ordinal()) return UserRating.class;
		return String.class;
	}
	
	@Override
	public Object getValueForExportAt(int row, int col) {
		return getValueAt(row, col);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ApplicationLight app = getObject(row);
		switch(COLS[col]) {
			case id: return app.getId();
			case title: {
				if(StringHelper.containsNonWhitespace(app.getPerson().getTitle())) {
					String title = translator.translate(app.getPerson().getTitle());
					if(title != null && title.length() < 15) {
						return title;
					}
				}
				return "";
			}
			case name: {
				StringBuilder sb = new StringBuilder();
				if(StringHelper.containsNonWhitespace(app.getPerson().getLastName())) {
					sb.append(app.getPerson().getLastName());
				}
				if(StringHelper.containsNonWhitespace(app.getPerson().getFirstName())) {
					if(sb.length() > 0) sb.append(", ");
					sb.append(app.getPerson().getFirstName());
				}
				return sb.toString();
			}
			case yearOfBirthday: return toYear(app.getPerson().getBirthday());
			case gender: return RecruitingHelper.formatGender(app.getPerson().getGender(), translator.getLocale());
			case highestDegreeYear: return app.getAcademicalBackground().getHighestDegreeDate();
			case highestDegreeYearPhD: return toYear(getHighestDegreeYearPhD(app));
			case nationality: {
				String enumKey = app.getPerson().getNationality();
				String country2Code;
				if(enumKey == null || enumKey.equals("") || enumKey.equals("-")) {
					country2Code = "";
				} else {
					Country country = Country.country(enumKey);
					if(country != null) {
						country2Code = country.country2Code().toUpperCase();
					} else {
						country2Code = "";
					}
				}
				return country2Code;
			}
			case nationalityRaw: return app.getPerson().getNationality();
			case additionalNationalities: return RecruitingHelper.beautifyCountriesList(app.getPerson().getAdditionalNationalities());
			case organization: return RecruitingHelper.mergeOrganizationAndAffiliation(app.getBusinessInformations());
			case currentPosition: return app.getBusinessInformations().getCurrentPosition();
			case numberOfOriginalPublications: return app.getAcademicalBackground().getNumberOfOriginalPublications();
			case numberOfFirstAuthorships: return app.getAcademicalBackground().getNumberOfFirstAuthorships();
			case numberOfLastAuthorships: return app.getAcademicalBackground().getNumberOfLastAuthorships();
			case citations: return app.getAcademicalBackground().getCitations();
			case impactFactor: return RecruitingHelper.formatFactor(app.getAcademicalBackground().getImpactFactor()); 
			case hFactor: return RecruitingHelper.formatFactor(app.getAcademicalBackground().getHFactor());
			case ranking: {
				List<UserRating> myRatings = new ArrayList<>();
				String resSubPath = app.getKey().toString();
				for(UserRating rating:ratings) {
					if(resSubPath.equals(rating.getResSubPath())) {
						myRatings.add(rating);
					}
				}
				Collections.sort(myRatings, new UserRatingComparator());
				for(int i=myRatings.size(); i<numOfCommitteeMembers; i++) {
					myRatings.add(null);
				}
				return myRatings;
			}
			case decision: return getDecision(app);
			case projectTitle: return app.getProject().getTitle();
			case projectAcronym: return app.getProject().getAcronym();
			case projectKeywords: return app.getProject().getKeywords();
			case projectDisciplines: return app.getProject().getDisciplines();
			case projectStartDate: return app.getProject().getStartDate();
			case projectDuration: return app.getProject().getDuration();
			case projectFinancialImpact1: return app.getProject().getFinancialImpact1();
			case projectFinancialImpact2: return app.getProject().getFinancialImpact2();
			case projectFinancialImpact3: return app.getProject().getFinancialImpact3();
			case projectFinancialImpact4: return app.getProject().getFinancialImpact4();
			case projectFinancialImpact5: return app.getProject().getFinancialImpact5();
			default: return "";
		}
	}
	
	private Integer toYear(Date date) {
		if(date == null) return null;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return Integer.valueOf(cal.get(Calendar.YEAR));
	}
	
	private String getDecision(ApplicationLight app) {
		String d = null;
		Integer decision = app.getDecision();
		if(decision != null && decision.intValue() > 0) {
			switch(decision.intValue()) {
				case 1: d = "C"; break;
				case 2: d = "B"; break;
				case 3: d = "A"; break;
				default: {}
			}
		}
		return d;
	}
	
	private Date getHighestDegreeYearPhD(ApplicationLight app)  {
		if(app.getAcademicalBackground().getHighestDegreeDate() == null) {
			return null;
		}
		String type = app.getAcademicalBackground().getHighestDegreeType();
		if(type != null && HighestDegreeType.phd.name().equals(type)) {
			return app.getAcademicalBackground().getHighestDegreeDate();
		}
		return null;
	}
	
	public enum PDFFields {
		id("edit.application.id"),
		title("edit.application.title"),
		name("edit.application.name"),
		yearOfBirthday("edit.application.birthday.pdf"),
		nationality("edit.application.nationality"),
		nationalityRaw("edit.application.nationality"),
		additionalNationalities("edit.application.additional.nationalities"),
		highestDegreeYear("table.header.highestdegreeyear"),
		highestDegreeYearPhD("table.header.highestdegreeyear"),
		gender("edit.application.gender"),
		numberOfOriginalPublications("edit.application.numberOfOriginalPublications.pdf"),
		numberOfFirstAuthorships("edit.application.numberOfFirstAuthorships.pdf"),
		numberOfLastAuthorships("edit.application.numberOfLastAuthorships.pdf"),
		citations("edit.application.citations.pdf"),
		impactFactor("edit.application.impactFactor.pdf"),
		hFactor("edit.application.hFactor.pdf"),
		organization("edit.application.organization"),
		currentPosition("edit.application.currentPosition"),
		ranking("edit.application.committee_rating"),
		decision("edit.application.decision"),
		projectTitle("edit.application.project"),
		projectAcronym("table.header.project.acronym"),
		projectKeywords("table.header.project.keywords"),
		projectDisciplines("table.header.project.disciplines"),
		projectStartDate("table.header.project.start.date"),
		projectDuration("table.header.project.duration"),
		projectFinancialImpact1("table.header.project.impactFactor.1"),
		projectFinancialImpact2("table.header.project.impactFactor.2"),
		projectFinancialImpact3("table.header.project.impactFactor.3"),
		projectFinancialImpact4("table.header.project.impactFactor.4"),
		projectFinancialImpact5("table.header.project.impactFactor.5");
		
		private final String key;
		
		private PDFFields(String key) {
			this.key = key;
		}
		
		public String key() {
			return key;
		}
	}
}