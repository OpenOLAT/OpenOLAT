package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.lr.BlogResource;

import com.thoughtworks.selenium.Selenium;

/**
 * Uses a <code>BlogResource</code> to delegate the implementation to it.
 * The difference between a <code>BlogRun</code> and a <code>BlogResource</code> is that a <code>BlogRun</code> instance
 * could only be constructed within a <code>CourseRun</code> whereas a <code>BlogResource</code> lives in a separate resource tab.
 * 
 * @author lavinia
 *
 */

public class BlogRun extends OLATSeleniumWrapper {
  
  private BlogResource blogResource;

  public BlogRun(Selenium selenium) {
    super(selenium);

    blogResource = new BlogResource(selenium,"");
  }
  
  public void createEntry(String title, String description, String content, boolean publish) {
    blogResource.createEntry(title, description, content, publish);
  }
  
  public boolean hasDraft(String title) {
    return blogResource.hasDraftEntry(title);
  }
  
  public void editEntry(String title, String description, String content, boolean publish) {
    blogResource.editEntry(title, description, content, publish);
  }
  
  public void commentEntry(String entryTitle, String comment) {
    blogResource.commentEntry(entryTitle, comment);
  }
  
  public boolean hasComments(String entryTitle, int numComments) {
    return blogResource.hasComments(entryTitle, numComments);
  }
}
