package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.lr.PodcastResource;

import com.thoughtworks.selenium.Selenium;

public class PodcastRun extends OLATSeleniumWrapper {

  private PodcastResource podcastResource;
  
  public PodcastRun(Selenium selenium) {
    super(selenium);

    podcastResource = new PodcastResource(selenium, "");
  }
  
  public void createEpisode(String title, String description, String fileName) {
    podcastResource.createEpisode(title, description, fileName);
  }
  
  public void editEpisode(String title, String description, String fileName) {
    podcastResource.editEpisode(title, description, fileName);
  }
  
  public void commentEpisode(String title,String comment) {
    podcastResource.commentEpisode(title, comment);
  }
  
  public boolean hasComments(String entryTitle, int numComments) {
    return podcastResource.hasComments(entryTitle, numComments);
  }

}
