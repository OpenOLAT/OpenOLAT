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

import java.util.Calendar;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Country;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.Notes;
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
public class NotesPDFDataModel extends DefaultExportTableDataModel<ApplicationLight> {

	private final Identity identity;
	private final List<Notes> notes;
	private final Translator translator;
	private final List<UserRating> ratings;

	public NotesPDFDataModel(Identity identity, List<ApplicationLight> rows, List<Notes> notes, List<UserRating> ratings,
			Translator translator) {
		super(rows);
		this.notes = notes;
		this.ratings = ratings;
		this.identity = identity;
		this.translator = translator;
	}
	
	@Override
	public int getColumnCount() {
		return PDFFields.values().length;
	}
	
	@Override
	public String getHeader(int col) {
		PDFFields field = PDFFields.values()[col];
		return translator.translate(field.key());
	}
	
	@Override
	public String getFieldNameAt(int col) {
		PDFFields field = PDFFields.values()[col];
		return field.name();
	}

	@Override
	public Class<?> getTypeAt(int row, int col) {
		if(col == PDFFields.yearOfBirthday.ordinal()) return Integer.class;
		if(col == PDFFields.notes.ordinal()) return Notes.class;
		return String.class;
	}
	
	@Override
	public Object getValueForExportAt(int row, int col) {
		return getValueAt(row, col);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ApplicationLight app = getObject(row);
		PDFFields field = PDFFields.values()[col];
		switch(field) {
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
			case yearOfBirthday: {
				if(app.getPerson().getBirthday() == null) {
					return null;
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(app.getPerson().getBirthday());
				return Integer.valueOf(cal.get(Calendar.YEAR));
			}
			case gender: return RecruitingHelper.formatGender(app.getPerson().getGender(), translator.getLocale());
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
			case phd: {
				StringBuilder sb = new StringBuilder();
				String type = app.getAcademicalBackground().getHighestDegreeType();
				if(type != null && HighestDegreeType.phd.name().equals(type) &&
						app.getAcademicalBackground().getHighestDegreeDate() != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(app.getAcademicalBackground().getHighestDegreeDate());
					int year = cal.get(Calendar.YEAR);
					sb.append(year);
				}
				if(StringHelper.containsNonWhitespace(app.getAcademicalBackground().getHighestDegreeInstitution())) {
					if(sb.length() > 0) sb.append(", ");
					sb.append(app.getAcademicalBackground().getHighestDegreeInstitution());
				}
				return sb.toString();
			}
			case numberOfOriginalPublications: return app.getAcademicalBackground().getNumberOfOriginalPublications();
			case numberOfFirstAuthorships: return app.getAcademicalBackground().getNumberOfFirstAuthorships();
			case numberOfLastAuthorships: return app.getAcademicalBackground().getNumberOfLastAuthorships();
			case citations: return app.getAcademicalBackground().getCitations();
			case impactFactor: return RecruitingHelper.formatFactor(app.getAcademicalBackground().getImpactFactor()); 
			case hFactor: return RecruitingHelper.formatFactor(app.getAcademicalBackground().getHFactor());
			case organization: return RecruitingHelper.mergeOrganizationAndAffiliation(app.getBusinessInformations());
			case currentPosition: return app.getBusinessInformations().getCurrentPosition();
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
			case notes: {
				for(Notes note:notes) {
					if(app.getKey().equals(note.getApplicationKey())) {
						return note;
					}
				}
				return null;
			}
			case ranking: {
				UserRating myRating = null;
				String resSubPath = app.getKey().toString();
				for(UserRating rating:ratings) {
					if(resSubPath.equals(rating.getResSubPath()) &&
						identity.getKey().equals(rating.getCreator().getKey())) {
						myRating = rating;
						break;
					}
				}
				if(myRating == null || myRating.getRating() == null) {
					return " ";
				}
				int rating = myRating.getRating().intValue();
				if(rating == RecruitingService.ABSTENTION) {
					return "abstain";
				}
				return translator.translate("rating." + (rating - 1));
			}
			default: return "";
		}
	}
	
	public enum PDFFields {
		id("edit.application.id"),
		title("edit.application.title"),
		name("edit.application.name"),
		yearOfBirthday("edit.application.birthday.pdf"),
		nationality("edit.application.nationality"),
		nationalityRaw("edit.application.nationality"),
		additionalNationalities("edit.application.additional.nationalities"),
		gender("edit.application.gender"),
		phd("edit.application.highestdegree.pdf"),
		numberOfOriginalPublications("edit.application.numberOfOriginalPublications.pdf"),
		numberOfFirstAuthorships("edit.application.numberOfFirstAuthorships.pdf"),
		numberOfLastAuthorships("edit.application.numberOfLastAuthorships.pdf"),
		citations("edit.application.citations.pdf"),
		impactFactor("edit.application.impactFactor.pdf"),
		hFactor("edit.application.hFactor.pdf"),
		organization("edit.application.organization"),
		currentPosition("edit.application.currentPosition"),
		ranking("edit.application.my_rating"),
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
		projectFinancialImpact5("table.header.project.impactFactor.5"),
		notes("edit.notes");
		
		private final String key;
		
		private PDFFields(String key) {
			this.key = key;
		}
		
		public String key() {
			return key;
		}
	}
}