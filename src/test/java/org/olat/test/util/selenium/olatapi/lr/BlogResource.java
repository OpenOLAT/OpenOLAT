package org.olat.test.util.selenium.olatapi.lr;

import com.thoughtworks.selenium.Selenium;

public class BlogResource extends ResourceEditor {

	public BlogResource(Selenium selenium, String title) {
		super(selenium, title);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param title
	 * @param description
	 * @param imageName
	 */
	public void editBlog(String title, String description, String imageName) {
		selenium.click("ui=blog::edit()");
		selenium.waitForPageToLoad("30000");
			
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);			
		selenium.type("ui=commons::tinyMce_styledTextArea()", description);
				
		if(imageName!=null) {
			//TODO: LD: add image
		}
		
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Create blog entry.
	 * @param title
	 * @param description
	 * @param content
	 * @param publish TODO
	 */
	public void createEntry(String title, String description, String content, boolean publish) {
		if(selenium.isElementPresent("ui=blog::create()")) {
	    selenium.click("ui=blog::create()");
		  selenium.waitForPageToLoad("30000");
		} else if(selenium.isElementPresent("ui=blog::createNewEntry()")) {
		  selenium.click("ui=blog::createNewEntry()");
		  selenium.waitForPageToLoad("30000");
		}
		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);			
		this.sleepThread(3000);
		
		editBlogEntry(description, content, publish);
	}
	
	/**
	 * This edits an existing entry, therefore if description or content are null, they don't get changed.
	 * The title cannot be null.
	 * 
	 * @param title
	 * @param description
	 * @param content
	 * @param publish
	 */
	public void editEntry(String title, String description, String content, boolean publish) {	  
	  selenium.click("ui=blog::editEntry(entryTitle=" + title + ")");
	  //selenium.waitForPageToLoad("30000");
    this.sleepThread(3000);

    editBlogEntry(description, content, publish);
	}
	
	private void editBlogEntry(String description, String content, boolean publish) {
	  if(description!=null) {
      selenium.selectFrame("ui=blog::blogEntryDescriptionFrame()");    
      selenium.type("ui=commons::tinyMce_styledTextArea()", description);
      selenium.selectFrame("relative=top");     
    }   

    this.sleepThread(3000);
    if(content!=null && selenium.isElementPresent("ui=blog::blogEntryContentFrame()")) {
      selenium.selectFrame("ui=blog::blogEntryContentFrame()");
      selenium.type("ui=commons::tinyMce_styledTextArea()", content);
      selenium.selectFrame("relative=top");
    }

    if(publish) {
      selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Publish)");
      selenium.waitForPageToLoad("30000");
    } else {
      selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Save draft)");
      selenium.waitForPageToLoad("30000");
    }
	}
	
	public boolean hasDraftEntry(String entryTitle) {
	  return selenium.isElementPresent("ui=blog::draft(entryTitle=" + entryTitle + ")");	  
	}
	
	/**
	 * Add one comment and go back to the blog view.
	 * 
	 * @param entryTitle
	 * @param comment
	 */
	public void commentEntry(String entryTitle, String comment) {
	  selenium.click("ui=blog::addComment(entryTitle=" + entryTitle + ")");
	  selenium.waitForPageToLoad("30000");
	  this.sleepThread(5000);
	  
	  //typeInRichText(comment);
	  selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
    selenium.type("ui=commons::tinyMce_styledTextArea()", comment);
    selenium.selectFrame("relative=top"); 
    
	  selenium.click("ui=commons::save()");
	  selenium.waitForPageToLoad("30000");
	  selenium.click("ui=commons::backLink()");
	  selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * TODO: fix this implementation. It does not use the entryTitle.
	 * @param entryTitle
	 * @param num
	 * @return
	 */
	public boolean hasComments(String entryTitle, int num) {
	  return selenium.isTextPresent("Comments (" + num + ")");
	}
	
	/**
	 * Include external blog. 
	 * Suppose that a valid feedURL is provided.
	 * @param title
	 * @param description
	 * @param feedURL
	 */
	public void includeExternalBlog(String title, String description, String feedURL) {
		selenium.click("ui=blog::includeExternal()");
		selenium.waitForPageToLoad("30000");
		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);	  			
		selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Description)", description);
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Feed URL)", feedURL);	  	
				
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
		//TODO: LD: if no valid URL is provided, cancel or enter a new URL
	}
}
