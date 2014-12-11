<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:uploadInteraction">
    <input name="qtiworks_uploadpresented_{@responseIdentifier}" type="hidden" value="1"/>
    <div class="{local-name()}">
      <xsl:if test="qti:prompt">
        <div class="prompt">
          <xsl:apply-templates select="qti:prompt"/>
        </div>
      </xsl:if>
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
      <xsl:choose>
        <xsl:when test="not(empty($responseValue))">
          <!-- Already uploaded something, so show file and ability to replace it -->
          <div class="fileUploadStatus">
            Uploaded: <xsl:value-of select="$responseValue/qw:value/@fileName"/>
          </div>
          <xsl:choose>
            <xsl:when test="$isItemSessionOpen">
              <div class="fileUploadInstruction">
                Upload New File
              </div>
              <input type="file" name="qtiworks_uploadresponse_{@responseIdentifier}"/>
            </xsl:when>
            <xsl:otherwise>
              <input type="file" name="qtiworks_uploadresponse_{@responseIdentifier}" disabled="disabled"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <!-- Nothing uploaded yet -->
          <xsl:choose>
            <xsl:when test="$isItemSessionOpen">
              <div class="fileUploadInstruction">
                Upload File
              </div>
              <input type="file" name="qtiworks_uploadresponse_{@responseIdentifier}"/>
            </xsl:when>
            <xsl:otherwise>
              <input type="file" name="qtiworks_uploadresponse_{@responseIdentifier}" disabled="disabled"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

</xsl:stylesheet>
