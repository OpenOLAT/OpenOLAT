/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import org.olat.core.util.StringHelper;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 23.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditApplicationPage {
	
	private WebDriver browser;
	
	public EditApplicationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Fill the first and last name, the gender if not null and the title.
	 * 
	 * @param title
	 * @param gender Gender is optional
	 * @param firsName
	 * @param lastName
	 * @return
	 */
	public EditApplicationPage fillPerson(String title, String gender, String firsName, String lastName) {
		if(title != null) {
			By titleBy = By.cssSelector(".o_sel_edit_person_title select");
			WebElement titleEl = browser.findElement(titleBy);
			new Select(titleEl).selectByValue(title);
		}
		
		if(gender != null) {
			By genderBy = By.cssSelector(".o_sel_edit_person_gender select");
			WebElement genderEl = browser.findElement(genderBy);
			new Select(genderEl).selectByValue(gender);
		}
		
		By firstnameBy = By.cssSelector(".o_sel_edit_person_firstname input[type='text']");
		browser.findElement(firstnameBy).sendKeys(firsName);
		
		By lastnameBy = By.cssSelector(".o_sel_edit_person_lastname input[type='text']");
		browser.findElement(lastnameBy).sendKeys(lastName);
		
		return this;
	}
	
	public EditApplicationPage fillNationality(String nationality) {
		By nationalityBy = By.cssSelector(".o_sel_edit_person_nationality input[type='text']");
		browser.findElement(nationalityBy).sendKeys(nationality);
		return this;
	}
	
	public EditApplicationPage selectNationality(String nationality) {
		By nationalityBy = By.cssSelector(".o_sel_edit_person_nationality select");
		WebElement nationalityEl = browser.findElement(nationalityBy);
		new Select(nationalityEl).selectByValue(nationality);
		return this;
	}
	
	public EditApplicationPage fillAddress(boolean business, String line1, String country) {
		By addressTypeBy;
		if(business) {
			addressTypeBy = By.cssSelector(".o_sel_edit_person_address_type input[type='radio'][value='business']");
		} else {
			addressTypeBy = By.cssSelector(".o_sel_edit_person_address_type input[type='radio'][value='private']");
		}
		browser.findElement(addressTypeBy).click();
		OOGraphene.waitBusy(browser);
		
		By line1By = By.cssSelector(".o_sel_edit_address_line_1 input[type='text']");
		browser.findElement(line1By).sendKeys(line1);
		
		By countryBy = By.cssSelector(".o_sel_edit_address_country select");
		WebElement countrySelectEl = browser.findElement(countryBy);
		new Select(countrySelectEl).selectByValue(country);
		OOGraphene.waitBusy(browser);
		
		return this;
	}
	
	public EditApplicationPage fillPrivateAddress(String line1, String zipCode, String city, String country) {
		By line1By = By.cssSelector(".o_sel_edit_private_address_line1 input[type='text']");
		browser.findElement(line1By).sendKeys(line1);
		
		By zipCodeBy = By.cssSelector(".o_sel_edit_private_address_zipcode input[type='text']");
		browser.findElement(zipCodeBy).sendKeys(zipCode);
		
		By cityBy = By.cssSelector(".o_sel_edit_private_address_city input[type='text']");
		browser.findElement(cityBy).sendKeys(city);
		
		By countryBy = By.cssSelector(".o_sel_edit_private_address_country select");
		WebElement countrySelectEl = browser.findElement(countryBy);
		new Select(countrySelectEl).selectByValue(country);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditApplicationPage fillBusinessAddress(String line1, String zipCode, String city, String country) {
		By line1By = By.cssSelector(".o_sel_edit_business_address_line1 input[type='text']");
		browser.findElement(line1By).sendKeys(line1);
		
		By zipCodeBy = By.cssSelector(".o_sel_edit_business_address_zipcode input[type='text']");
		browser.findElement(zipCodeBy).sendKeys(zipCode);
		
		By cityBy = By.cssSelector(".o_sel_edit_business_address_city input[type='text']");
		browser.findElement(cityBy).sendKeys(city);
		
		By countryBy = By.cssSelector(".o_sel_edit_business_address_country select");
		WebElement countrySelectEl = browser.findElement(countryBy);
		new Select(countrySelectEl).selectByValue(country);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditApplicationPage fillCity(String zipCode, String city) {
		By zipCodeBy = By.cssSelector(".o_sel_edit_address_zipcode input[type='text']");
		browser.findElement(zipCodeBy).sendKeys(zipCode);
		
		By cityBy = By.cssSelector(".o_sel_edit_address_city input[type='text']");
		browser.findElement(cityBy).sendKeys(city);
		return this;
	}
	
	public EditApplicationPage fillEmail(String email) {
		By emailBy = By.cssSelector(".o_sel_edit_person_email input[type='text']");
		browser.findElement(emailBy).sendKeys(email);
		return this;
	}
	
	public EditApplicationPage fillPhone(String phone) {
		By phoneBy = By.cssSelector(".o_sel_edit_person_phone input[type='text']");
		browser.findElement(phoneBy).sendKeys(phone);
		return this;
	}
	
	public EditApplicationPage fillMobilePhone(String phone) {
		By phoneBy = By.cssSelector(".o_sel_edit_person_mobile_phone input[type='text']");
		browser.findElement(phoneBy).sendKeys(phone);
		return this;
	}
	
	public EditApplicationPage fillMaritalStatus(String status) {
		By maritalStatusBy = By.cssSelector(".o_sel_edit_person_marital_status select");
		WebElement maritalStatusEl = browser.findElement(maritalStatusBy);
		new Select(maritalStatusEl).selectByValue(status);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditApplicationPage fillBusinessInfos(String organization, String unit, String position) {
		if(StringHelper.containsNonWhitespace(organization)) {
			By organizationBy = By.cssSelector(".o_sel_edit_person_organization input[type='text']");
			browser.findElement(organizationBy).sendKeys(organization);
		}
		if(StringHelper.containsNonWhitespace(unit)) {
			By unitBy = By.cssSelector(".o_sel_edit_person_unit input[type='text']");
			browser.findElement(unitBy).sendKeys(unit);
		}
		if(StringHelper.containsNonWhitespace(position)) {
			By positionBy = By.cssSelector(".o_sel_edit_person_current_position input[type='text']");
			browser.findElement(positionBy).sendKeys(position);
		}
		return this;
	}
	
	/**
	 * 
	 * @param day Between 1 - 31
	 * @param month Between 0 - 11
	 * @param year The year
	 * @return
	 */
	public EditApplicationPage fillBirthday(int day, int month, int year) {
		By dayBy = By.cssSelector(".o_sel_edit_person_birthday input[type='text'][size='2']");
		By monthBy = By.cssSelector(".o_sel_edit_person_birthday select");
		By yearBy = By.cssSelector(".o_sel_edit_person_birthday input[type='text'][size='4']");

		browser.findElement(dayBy).sendKeys(Integer.toString(day));
		WebElement selectMonthEl = browser.findElement(monthBy);
		new Select(selectMonthEl).selectByValue(Integer.toString(month));
		browser.findElement(yearBy).sendKeys(Integer.toString(year));
		return this;
	}
	
	/**
	 * 
	 * @param gender
	 * @return
	 */
	public EditApplicationPage fillGender(String gender) {
		By genderBy = By.cssSelector(".o_sel_edit_person_gender select");
		WebElement selectGenderEl = browser.findElement(genderBy);
		new Select(selectGenderEl).selectByValue(gender);
		return this;
	}
	
	/**
	 * Choose the segment to edit the person informations
	 * @return
	 */
	public EditApplicationPage selectEditPerson() {
		By editPersonBy = By.cssSelector("a.o_sel_edit_person_nav");
		OOGraphene.waitElement(editPersonBy, browser);
		browser.findElement(editPersonBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Choose the segment to edit the background informations as highest degree,
	 * affiliation...
	 * 
	 * @return
	 */
	public EditApplicationPage selectEditAcademicalBackground() {
		By editBackgroundBy = By.cssSelector("a.o_sel_edit_app_background_nav");
		OOGraphene.waitElement(editBackgroundBy, browser);
		browser.findElement(editBackgroundBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditApplicationPage fillHighestDegree(String type, String year, String institution) {
		if(StringHelper.containsNonWhitespace(type)) {
			By typeBy = By.cssSelector(".o_sel_edit_highest_degree_type_date select");
			OOGraphene.waitElement(typeBy, browser);
			new Select(browser.findElement(typeBy)).selectByValue(type);
			OOGraphene.waitBusy(browser);
		}

		if(StringHelper.containsNonWhitespace(year)) {
			By dateBy = By.cssSelector(".o_sel_edit_highest_degree_type_date input[type='text']");
			browser.findElement(dateBy).sendKeys(year);
		}

		if(StringHelper.containsNonWhitespace(institution)) {
			By institutionBy = By.cssSelector(".o_sel_edit_highest_degree_institution input[type='text']");
			browser.findElement(institutionBy).sendKeys(institution);
		}
		return this;
	}
	
	public EditApplicationPage fillHabilitation(String title, String date, String institution) {
		By titleBy = By.cssSelector(".o_sel_edit_habilitation_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		By dateBy = By.cssSelector(".o_sel_edit_habilitation_date input[type='text']");
		browser.findElement(dateBy).sendKeys(date);

		By institutionBy = By.cssSelector(".o_sel_edit_habilitation_institution input[type='text']");
		browser.findElement(institutionBy).sendKeys(institution);
		return this;
	}
	
	public EditApplicationPage fillDissertation(String title, String date, String institution) {
		By titleBy = By.cssSelector(".o_sel_edit_dissertation_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		By dateBy = By.cssSelector(".o_sel_edit_dissertation_date input[type='text']");
		browser.findElement(dateBy).sendKeys(date);

		By institutionBy = By.cssSelector(".o_sel_edit_dissertation_institution input[type='text']");
		browser.findElement(institutionBy).sendKeys(institution);
		return this;
	}
	
	public EditApplicationPage fillPublications(String title, String date, String institution) {
		By titleBy = By.cssSelector(".o_sel_edit_dissertation_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		By dateBy = By.cssSelector(".o_sel_edit_dissertation_date input[type='text']");
		browser.findElement(dateBy).sendKeys(date);

		By institutionBy = By.cssSelector(".o_sel_edit_dissertation_institution input[type='text']");
		browser.findElement(institutionBy).sendKeys(institution);
		return this;
	}
	
	public EditApplicationPage fillPublications(Integer originalPublications, Integer firstAuthorships, Integer lastAuthorships, Integer citations) {
		if(originalPublications != null) {
			By publicationsBy = By.cssSelector(".o_sel_edit_original_publications input[type='text']");
			OOGraphene.waitElement(publicationsBy, browser);
			browser.findElement(publicationsBy).sendKeys(originalPublications.toString());
		}
		
		if(firstAuthorships != null) {
			By authorshipsBy = By.cssSelector(".o_sel_edit_first_authorships input[type='text']");
			OOGraphene.waitElement(authorshipsBy, browser);
			browser.findElement(authorshipsBy).sendKeys(firstAuthorships.toString());
		}
		
		if(lastAuthorships != null) {
			By lastAuthorshipsBy = By.cssSelector(".o_sel_edit_last_authorships input[type='text']");
			OOGraphene.waitElement(lastAuthorshipsBy, browser);
			browser.findElement(lastAuthorshipsBy).sendKeys(lastAuthorships.toString());
		}
		
		if(citations != null) {
			By citationsBy = By.cssSelector(".o_sel_edit_citations input[type='text']");
			OOGraphene.waitElement(citationsBy, browser);
			browser.findElement(citationsBy).sendKeys(citations.toString());
		}
		
		return this;
	}
	
	public EditApplicationPage fillFactors(Integer impactFactor, Integer hFactor) {
		if(impactFactor != null) {
			By impactFactorBy = By.cssSelector(".o_sel_edit_impact_factor input[type='text']");
			OOGraphene.waitElement(impactFactorBy, browser);
			browser.findElement(impactFactorBy).sendKeys(impactFactor.toString());
		}

		if(hFactor != null) {
			By hFactorBy = By.cssSelector(".o_sel_edit_h_factor input[type='text']");
			OOGraphene.waitElement(hFactorBy, browser);
			browser.findElement(hFactorBy).sendKeys(hFactor.toString());
		}
		return this;
	}
	
	public EditApplicationPage fillWorkedInAcademiaSince(String years) {
		By workedBy = By.cssSelector(".o_sel_edit_worked_in_academia_since input[type='text']");
		OOGraphene.waitElement(workedBy, browser);
		browser.findElement(workedBy).sendKeys(years);
		return this;
	}
	
	/**
	 * 
	 * @return Itself
	 */
	public EditApplicationPage selectEditProject() {
		By editProjectBy = By.cssSelector("a.o_sel_edit_app_project_nav");
		browser.findElement(editProjectBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditApplicationPage fillProjectTitle(String title) {
		By titleBy = By.cssSelector(".o_sel_edit_project_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		return this;
	}
	
	public EditApplicationPage fillProjectMetadata(String acronym, String keywords, String disciplines) {
		if(StringHelper.containsNonWhitespace(acronym)) {
			By acronymBy = By.cssSelector(".o_sel_edit_project_acronym input[type='text']");
			browser.findElement(acronymBy).sendKeys(acronym);
		}
		if(StringHelper.containsNonWhitespace(keywords)) {
			By keywordsBy = By.cssSelector(".o_sel_edit_project_keywords input[type='text']");
			browser.findElement(keywordsBy).sendKeys(keywords);
		}
		if(StringHelper.containsNonWhitespace(disciplines)) {
			By disciplinesBy = By.cssSelector(".o_sel_edit_project_disciplines input[type='text']");
			browser.findElement(disciplinesBy).sendKeys(disciplines);
		}
		return this;
	}
	
	public EditApplicationPage fillProjectDuration(String duration) {
		By durationBy = By.cssSelector(".o_sel_edit_project_duration input[type='text']");
		browser.findElement(durationBy).sendKeys(duration);
		return this;
	}
	
	public EditApplicationPage fillProjectStartDate(int day) {
		By startBy = By.cssSelector(".o_sel_edit_project div.o_sel_edit_project_start span.input-group-addon i");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		return selectDayInDatePicker(day);
	}
	
	private EditApplicationPage selectDayInDatePicker(int day) {
		By datePickerBy = By.id("ui-datepicker-div");
		OOGraphene.waitElement(datePickerBy, browser);
		
		By dayBy = By.xpath("//div[@id='ui-datepicker-div']//td//a[normalize-space(text())='" + day + "']");
		OOGraphene.waitElement(dayBy, browser);
		browser.findElement(dayBy).click();
		OOGraphene.waitElementDisappears(datePickerBy, 5, browser);
		return this;
	}
	
	public EditApplicationPage fillProjectFinancialImpacts(String impact1, String impact2, String impact3, String impact4, String impact5) {
		if(StringHelper.containsNonWhitespace(impact1)) {
			By impact1By = By.cssSelector(".o_sel_edit_project_financial_1 input[type='text']");
			browser.findElement(impact1By).sendKeys(impact1);
		}
		if(StringHelper.containsNonWhitespace(impact2)) {
			By impact2By = By.cssSelector(".o_sel_edit_project_financial_2 input[type='text']");
			browser.findElement(impact2By).sendKeys(impact2);
		}
		if(StringHelper.containsNonWhitespace(impact3)) {
			By impact3By = By.cssSelector(".o_sel_edit_project_financial_3 input[type='text']");
			browser.findElement(impact3By).sendKeys(impact3);
		}
		if(StringHelper.containsNonWhitespace(impact4)) {
			By impact4By = By.cssSelector(".o_sel_edit_project_financial_4 input[type='text']");
			browser.findElement(impact4By).sendKeys(impact4);
		}
		if(StringHelper.containsNonWhitespace(impact5)) {
			By impact5By = By.cssSelector(".o_sel_edit_project_financial_5 input[type='text']");
			browser.findElement(impact5By).sendKeys(impact5);
		}
		return this;
	}
	
	public EditApplicationPage assertProjectFinancialImpactSum(int pos, String value) {
		By impactBy = By.xpath("//div[contains(@class,'o_sel_edit_project_financial_" + pos + "')]//span[contains(@id,'ref_o_')][text()[contains(.,'" + value + "')]]");
		OOGraphene.waitElement(impactBy, browser);
		return this;
	}
		
	public EditApplicationPage fillProjectDescription(String description) {
		By descriptionBy = By.cssSelector(".o_sel_edit_project_description textarea");
		browser.findElement(descriptionBy).sendKeys(description);
		return this;
	}
	
	/**
	 * Choose the segment about documents
	 * 
	 * @return Itself
	 */
	public EditApplicationPage selectEditDocument() {
		By editDocumentsBy = By.cssSelector("a.o_sel_edit_app_documents_nav");
		browser.findElement(editDocumentsBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Choose the segment about the status (withdrawn)
	 * 
	 * @return Itself
	 */
	public EditApplicationPage selectEditStatus() {
		By editStatusBy = By.cssSelector("a.o_sel_edit_app_status_nav");
		browser.findElement(editStatusBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditApplicationPage setApplicationOnhold() {
		By statusBy = By.cssSelector("div.o_sel_edit_application_status select");
		OOGraphene.waitElement(statusBy, browser);
		new Select(browser.findElement(statusBy)).selectByValue("onhold");
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditApplicationPage setApplicationStatusComment(String comment) {
		By statusCommentBy = By.cssSelector(".o_sel_edit_application_status_comment textarea");
		browser.findElement(statusCommentBy).sendKeys(comment);
		return this;
	}
	
	public EditApplicationPage saveApplication() {
		By saveBy = By.cssSelector(".o_sel_edit_application_overview button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
