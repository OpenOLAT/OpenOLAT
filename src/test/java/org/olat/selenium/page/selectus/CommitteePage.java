/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 19 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteePage {
	
	private WebDriver browser;
	
	public CommitteePage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CommitteeMemberWizardPage addCommitteeMember() {
		By addCommitteeMemebrBy = By.className("o_sel_add_committee_member");
		browser.findElement(addCommitteeMemebrBy).click();
		OOGraphene.waitBusy(browser);
		return new CommitteeMemberWizardPage(browser);
	}
	
	public CommitteePage edit(String lastName) {
		By editBy = By.xpath("//div[contains(@class,'o_sel_position_committee_list')]//table[contains(@class,'table')]//tr[td/a[contains(text(),'" + lastName + "')]]/td/a[contains(@onclick,'edit')]");
		browser.findElement(editBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public String getUsername() {
		By usernameBy = By.cssSelector(".modal-body div.form-group p.form-control-static");
		WebElement usernameEl = OOGraphene.waitElement(usernameBy, browser);
		String username = usernameEl.getText();
		return username.trim();
	}
	
	public CommitteePage cancel() {
		By cancelBy = By.xpath("//div[contains(@class,'modal-body')]//a[contains(@onclick,'cancel')]");
		OOGraphene.click(cancelBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}

}
