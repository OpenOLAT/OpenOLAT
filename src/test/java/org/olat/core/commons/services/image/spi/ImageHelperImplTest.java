package org.olat.core.commons.services.image.spi;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Test;
import org.olat.core.commons.services.image.Size;
import org.testng.Assert;


public class ImageHelperImplTest {

  @Test
  public void scaleImage() throws URISyntaxException {
  	Integer maxHeight = 120;
  	Integer maxWidth = 180;

  	URL sourceURL = ImageHelperImpl.class.getResource("mini.png");
  	URL scaledURL = ImageHelperImpl.class.getResource("testimg.jpg");

  	File scaledImg =  new File(sourceURL.toURI());
  	File sourceImg = new File(scaledURL.toURI());

  	ImageHelperImpl imageHelper = new ImageHelperImpl();
  	Size size = imageHelper.scaleImage(sourceImg, "jpg", scaledImg, maxWidth, maxHeight, true);
  	assertThat(size.getWidth()).isEqualTo(180);
  	assertThat(size.getHeight()).isEqualTo(120);
  	assertThat(size.getYOffset()).isEqualTo(0);
  	assertThat(size.getXOffset()).isEqualTo(90);
  }
}