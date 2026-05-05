/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Country;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
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
public class PositionApplicationsPDFDataModel extends DefaultExportTableDataModel<ApplicationLight> {
	
	private final Identity identity;
	private final Position position;
	private final Translator translator;
	private final List<UserRating> ratings;

	public PositionApplicationsPDFDataModel(Identity identity, Position position, List<ApplicationLight> rows, List<UserRating> ratings, Translator translator) {
		super(rows);
		this.identity = identity;
		this.position = position;
		this.ratings = ratings;
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
					
					if(StringHelper.containsNonWhitespace(app.getAcademicalBackground().getHighestDegreeInstitution())) {
						if(sb.length() > 0) sb.append(", ");
						sb.append(app.getAcademicalBackground().getHighestDegreeInstitution());
					}
				}
				
				return sb.toString();
			}
			case numberOfOriginalPublications: return app.getAcademicalBackground().getNumberOfOriginalPublications();
			case numberOfFirstAuthorships: return app.getAcademicalBackground().getNumberOfFirstAuthorships();
			case numberOfLastAuthorships: return app.getAcademicalBackground().getNumberOfLastAuthorships();
			case citations: return app.getAcademicalBackground().getCitations();
			case impactFactor: return RecruitingHelper.formatFactor(app.getAcademicalBackground().getImpactFactor()); 
			case hFactor: return RecruitingHelper.formatFactor(app.getAcademicalBackground().getHFactor());
			case unit: return app.getBusinessInformations().getUnit();
			case organization: return RecruitingHelper.mergeOrganizationAndAffiliation(app.getBusinessInformations());
			case currentPosition: return app.getBusinessInformations().getCurrentPosition();
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
				if(rating > 0 && rating <= 3) {
					return translator.translate("rating." + (rating - 1));
				}
				if(rating == RecruitingService.ABSTENTION) {
					return translator.translate("abstain.title");
				}
				return " ";
			}
			case afterDeadline: {
				Date deadline = position.getApplicationDeadline();
				if(deadline == null) return Boolean.FALSE;
				Date appDate = app.getCreationDate();
				if(appDate == null) return Boolean.FALSE;
				return Boolean.valueOf(appDate.after(deadline));
			}
			case infos: {
				if(app.getApplicationStatus() == ApplicationStatus.withdrawn && app.getWithdrawnDate() != null) {
					String date = DateCellRenderer.formatShort(app.getWithdrawnDate());
					return translator.translate("edit.application.withdrawn.at", new String[]{date});
				}
				return "";
			}
			case creationDate: return DateCellRenderer.formatShort(app.getCreationDate());
			case projectTitle: return app.getProject().getTitle();
			case projectDescription: return app.getProject().getDescription();
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
		unit("edit.application.unit"),
		currentPosition("edit.application.currentPosition"),
		ranking("edit.application.my_rating"),
		afterDeadline("edit.application.after_deadline"),
		infos("edit.application.infos"),
		creationDate("application.creationDate"),
		projectTitle("edit.application.project"),
		projectDescription("table.header.project.description"),
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