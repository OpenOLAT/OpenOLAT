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
package org.olat.selenium.page.course;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * 
 * Initial date: 7 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberListPage {
	
	private final WebDriver browser;
	
	public MemberListPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MemberListPage switchToThumbnailsView() {
		By thumbnailsViewBy = By.cssSelector("a.o_sel_cmembers_thumbnails_view");
		OOGraphene.waitElement(thumbnailsViewBy, browser);
		browser.findElement(thumbnailsViewBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MemberListPage switchToTableView() {
		By tableViewBy = By.cssSelector("a.o_sel_cmembers_table_view");
		OOGraphene.waitElement(tableViewBy, browser);
		browser.findElement(tableViewBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MemberListPage emailAll() {
		By tableViewBy = By.cssSelector("a.o_sel_cmembers_email_all");
		OOGraphene.waitElement(tableViewBy, browser);
		browser.findElement(tableViewBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public MemberListPage contactAllParticipants() {
		By allParticipantsBy = By.cssSelector("fieldset.o_sel_cmembers_mail_participant input[name='contact.all.participants']");
		WebElement allParticipantEl = browser.findElement(allParticipantsBy);
		OOGraphene.check(allParticipantEl, Boolean.TRUE);
		return this;
	}
	
	public MemberListPage contactExternal(String mail) {
		By externalBy = By.cssSelector("fieldset.o_sel_cmembers_mail_external input[name='contact.external']");
		WebElement externalEl = browser.findElement(externalBy);
		OOGraphene.check(externalEl, Boolean.TRUE);
		
		By externalMailBy = By.cssSelector("div.o_sel_cmembers_external_mail textarea");
		OOGraphene.waitElement(externalMailBy, browser);
		browser.findElement(externalMailBy).sendKeys(mail);
		return this;
	}
	
	public MemberListPage contactSubject(String subject) {
		By subjectBy = By.cssSelector("div.o_sel_cmembers_mail_subject input[type='text']");
		browser.findElement(subjectBy).sendKeys(subject);
		return this;
	}
	
	public MemberListPage send() {
		By sendBy = By.cssSelector("div.modal-dialog fieldset button.btn-primary");
		browser.findElement(sendBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public MemberListPage assertOnMembers() {
		By membersBy = By.cssSelector("div.o_cmembers");
		OOGraphene.waitElement(membersBy, browser);
		return this;
	}
	
	public MemberListPage assertOnOwner(String name) {
		By ownerBy = By.xpath("//div[contains(@class,'o_sel_owners')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(ownerBy, browser);
		return this;
	}
	
	public MemberListPage assertOnNotOwner(String name) {
		By ownerBy = By.xpath("//div[contains(@class,'o_sel_owners')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		List<WebElement> ownersEl = browser.findElements(ownerBy);
		Assert.assertEquals(0, ownersEl.size());
		return this;
	}
	
	public MemberListPage assertOnCoach(String name) {
		By coachBy = By.xpath("//div[contains(@class,'o_sel_coaches')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(coachBy, browser);
		return this;
	}
	
	public MemberListPage assertOnNotCoach(String name) {
		By coachBy = By.xpath("//div[contains(@class,'o_sel_coaches')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		List<WebElement> coachEls = browser.findElements(coachBy);
		Assert.assertEquals(0, coachEls.size());
		return this;
	}
	
	public MemberListPage assertOnParticipant(String name) {
		By participantBy = By.xpath("//div[contains(@class,'o_sel_participants')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(participantBy, browser);
		return this;
	}
	
	public MemberListPage assertOnNotParticipant(String name) {
		By participantBy = By.xpath("//div[contains(@class,'o_sel_participants')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		List<WebElement> participantEls = browser.findElements(participantBy);
		Assert.assertEquals(0, participantEls.size());
		return this;
	}
	
	/**
	 * 
	 * @param row The row in the table of the peekview (starts with 1)
	 * @param number The number of members
	 * @return Itself
	 */
	public MemberListPage assertOnPeekview(int row, int number) {
		By rowBy = By.xpath("//div[contains(@class,'o_portlet_table')]//div[@class='o_table_wrapper']/div/div/table/tbody/tr[" + row + "]/td[contains(text(),'" + number+ "')]");
		OOGraphene.waitElement(rowBy, browser);
		return this;
	}
	
	public MemberListPage assertOnTableOwner(String name) {
		By ownerBy = By.xpath("//div[@class='o_sel_cmembers_owners']//table//td/a[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(ownerBy, browser);
		return this;
	}
	
	public MemberListPage assertOnTableCoach(String name) {
		By coachBy = By.xpath("//div[@class='o_sel_cmembers_coaches']//table//td/a[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(coachBy, browser);
		return this;
	}
	
	public MemberListPage assertOnTableParticipant(String name) {
		By participantBy = By.xpath("//div[@class='o_sel_cmembers_participants']//table//td/a[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(participantBy, browser);
		return this;
	}
	
	public MemberListPage assertOnTableNotParticipant(String name) {
		By participantBy = By.xpath("//div[@class='o_sel_cmembers_participants']//table//td/a[contains(text(),'" + name + "')]");
		List<WebElement> participantEls = browser.findElements(participantBy);
		Assert.assertEquals(0, participantEls.size());
		return this;
	}
}
