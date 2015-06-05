<?xml version="1.0" encoding="UTF-8"?>
<!--

Renders the test(Part) feedback

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qti qw m">

  <!-- ************************************************************ -->

  <xsl:import href="qti-fallback.xsl"/>
  <xsl:import href="test-common.xsl"/>
  <xsl:import href="utils.xsl"/>

  <!-- This test -->
  <xsl:variable name="assessmentTest" select="/*[1]" as="element(qti:assessmentTest)"/>

  <!-- ************************************************************ -->

  <xsl:template match="qti:assessmentTest" as="element(html)">
    <html>
      <xsl:if test="@lang">
        <xsl:copy-of select="@lang"/>
        <xsl:attribute name="xml:lang" select="@lang"/>
      </xsl:if>
      <head>
        <title><xsl:value-of select="@title"/></title>
        <xsl:call-template name="includeAssessmentJsAndCss"/>
      </head>
      <body class="qtiworks assessmentTest testFeedback">
        <xsl:call-template name="maybeAddAuthoringLink"/>

        <h1><xsl:value-of select="$testOrTestPart"/> Complete</h1>

        <!-- Show 'atEnd' testPart feedback -->
        <xsl:apply-templates select="$currentTestPart/qti:testFeedback[@access='atEnd']"/>

        <!-- Show 'atEnd' test feedback f there's only 1 testPart -->
        <xsl:if test="not($hasMultipleTestParts)">
          <xsl:apply-templates select="qti:testFeedback[@access='atEnd']"/>
        </xsl:if>

        <!-- Review -->
        <xsl:apply-templates select="$currentTestPartNode" mode="testPart-review"/>

        <!-- Test session control -->
        <xsl:call-template name="qw:test-controls"/>
       </body>
    </html>
  </xsl:template>

  <xsl:template name="qw:test-controls">
    <ul class="sessionControl">
      <li>
        <form action="{$webappContextPath}{$advanceTestPartUrl}" method="post" target="oaa0"
          onsubmit="return confirm({qw:to-javascript-string($exitTestPartAlertMessage)})">
          <input type="submit" value="Exit {$testOrTestPart}" class="btn btn-default"/>
        </form>
      </li>
    </ul>
  </xsl:template>

  <xsl:template match="qw:node[@type='TEST_PART']" mode="testPart-review">
    <xsl:variable name="reviewable-items" select=".//qw:node[@type='ASSESSMENT_ITEM_REF' and (@allowReview='true' or @showFeedback='true')]" as="element(qw:node)*"/>
    <xsl:if test="exists($reviewable-items)">
      <h2>Review your responses</h2>
      <p>
        You may review your responses to some (or all) questions. These are listed below.
      </p>
      <ul class="testPartNavigation">
        <xsl:apply-templates mode="testPart-review"/>
      </ul>
    </xsl:if>
  </xsl:template>

  <xsl:template match="qw:node[@type='ASSESSMENT_SECTION']" mode="testPart-review">
    <xsl:variable name="assessmentSessionSessionState" select="$testSessionState/qw:assessmentSection[@key=current()/@key]/qw:assessmentSectionSessionState"
      as="element(qw:assessmentSectionSessionState)"/>
    <xsl:if test="$currentTestPart/@navigationMode='nonlinear' or exists($assessmentSessionSessionState/@entryTime)">
      <li class="assessmentSection">
        <header>
          <!-- Section title -->
          <h2><xsl:value-of select="@sectionPartTitle"/></h2>
          <!-- Handle rubrics -->
          <xsl:variable name="sectionIdentifier" select="qw:extract-identifier(.)" as="xs:string"/>
          <xsl:variable name="assessmentSection" select="$assessmentTest//qti:assessmentSection[@identifier=$sectionIdentifier]" as="element(qti:assessmentSection)*"/>
          <xsl:apply-templates select="$assessmentSection/qti:rubricBlock"/>
        </header>
        <!-- Descend -->
        <div class="testPartNavigationInner btn-group-vertical">
          <xsl:apply-templates mode="testPart-review"/>
        </div>
      </li>
    </xsl:if>
  </xsl:template>

  <xsl:template match="qw:node[@type='ASSESSMENT_ITEM_REF']" mode="testPart-review">
    <xsl:variable name="reviewable" select="@allowReview='true' or @showFeedback='true'" as="xs:boolean"/>
    <xsl:variable name="itemSessionState" select="$testSessionState/qw:item[@key=current()/@key]/qw:itemSessionState" as="element(qw:itemSessionState)"/>
    <xsl:if test="$currentTestPart/@navigationMode='nonlinear' or exists($itemSessionState/@entryTime)">
        <form action="{$webappContextPath}{$reviewTestItemUrl}/{@key}" method="post" target="oaa0" class="assessmentItem btn-group">
          <button type="submit" class="btn btn-default">
            <xsl:if test="not($reviewable)">
              <xsl:attribute name="disabled" select="'disabled'"/>
            </xsl:if>
            <span class="questionTitle"><xsl:value-of select="@sectionPartTitle"/></span>
            <xsl:choose>
              <xsl:when test="not($reviewable)">
                <span class="itemStatus reviewNotAllowed">Not Reviewable</span>
              </xsl:when>
              <xsl:when test="not(empty($itemSessionState/@unboundResponseIdentifiers) and empty($itemSessionState/@invalidResponseIdentifiers))">
                <span class="itemStatus reviewInvalid">Review (Invalid Answer)</span>
              </xsl:when>
              <xsl:when test="$itemSessionState/@responded='true'">
                <span class="itemStatus review">Review</span>
              </xsl:when>
              <xsl:when test="$itemSessionState/@entryTime!=''">
                <span class="itemStatus reviewNotAnswered">Review (Not Answered)</span>
              </xsl:when>
              <xsl:otherwise>
                <span class="itemStatus reviewNotSeen">Review (Not Seen)</span>
              </xsl:otherwise>
            </xsl:choose>
          </button>
        </form>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
