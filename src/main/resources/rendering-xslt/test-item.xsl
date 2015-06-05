<?xml version="1.0" encoding="UTF-8"?>
<!--

Renders an AssessmentItem within an AssessmentTest, as seen by candidates.

NB: This is used both while being presented, and during review.

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qti qw m">

  <!-- ************************************************************ -->

  <xsl:import href="qti-fallback.xsl"/>
  <xsl:import href="test-common.xsl"/>
  <xsl:import href="item-common.xsl"/>
  <xsl:import href="utils.xsl"/>

  <!--
  Flag to indicate that item is being modally rendered in review mode at the end
  of the current testPart. When unset, we will be rendering the current state of
  the current item.
  -->
  <xsl:param name="reviewMode" as="xs:boolean" required="yes"/>

  <!--
  Key for item being rendered is passed here.
  NB: Can't simply extract $testSessionState/@currentItemKey as this will be null
  when *reviewing* an item.
  -->
  <xsl:param name="itemKey" as="xs:string"/>

  <!-- Action permissions -->
  <xsl:param name="advanceTestItemAllowed" as="xs:boolean" required="yes"/>
  <xsl:param name="endTestPartAllowed" as="xs:boolean" required="yes"/>
  <xsl:param name="testPartNavigationAllowed" as="xs:boolean" required="yes"/>

  <!-- Relevant properties of EffectiveItemSessionControl for this item -->
  <xsl:param name="showFeedback" as="xs:boolean" required="yes"/>
  <xsl:param name="allowComment" as="xs:boolean" required="yes"/>
  <xsl:param name="showSolution" as="xs:boolean" required="yes"/>

  <!--
  Keep reference to assesssmentItem element as the processing chain goes off on a tangent
  at one point.
  -->
  <xsl:variable name="assessmentItem" select="/*[1]" as="element(qti:assessmentItem)"/>

  <xsl:variable name="itemFeedbackAllowed" as="xs:boolean"
    select="if ($reviewMode)
      then (/qti:assessentItem/@adaptive='true' or $showFeedback)
      else (not($solutionMode))"/>

  <xsl:variable name="provideItemSolutionButton" as="xs:boolean"
    select="$reviewMode and $showSolution and not($solutionMode)"/>

  <!-- Text to use on submit button, which depends on submissionMode -->
  <xsl:variable name="submitButtonText" as="xs:string"
    select="if ($currentTestPart/@submissionMode='individual') then 'Submit Answer' else 'Save Answer'"/>

  <!-- ************************************************************ -->

  <!-- Item may be QTI 2.0 or 2.1, so we'll put a template in here to fix namespaces to QTI 2.1 -->
  <xsl:template match="/">
    <xsl:apply-templates select="qw:to-qti21(/)/*"/>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="qti:assessmentItem" as="element(html)">
    <xsl:variable name="containsMathEntryInteraction"
      select="exists(qti:itemBody//qti:customInteraction[@class='org.qtitools.mathassess.MathEntryInteraction'])"
      as="xs:boolean"/>
    <html>
      <xsl:if test="@lang">
        <xsl:copy-of select="@lang"/>
        <xsl:attribute name="xml:lang" select="@lang"/>
      </xsl:if>
      <head>
        <title><xsl:value-of select="@title"/></title>
        <xsl:call-template name="includeAssessmentJsAndCss"/>

        <!--
        Import ASCIIMathML stuff if there are any MathEntryInteractions in the question.
        (It would be quite nice if we could allow each interaction to hook into this
        part of the result generation directly.)
        -->
        <xsl:if test="$containsMathEntryInteraction">
          <script src="{$webappContextPath}/rendering/javascript/UpConversionAjaxController.js?v={$qtiWorksVersion}"/>
          <script src="{$webappContextPath}/rendering/javascript/AsciiMathInputController.js?v={$qtiWorksVersion}"/>
          <script>
            UpConversionAjaxController.setUpConversionServiceUrl('<xsl:value-of select="$webappContextPath"/>restapi/math/verifyAsciiMath');
            UpConversionAjaxController.setDelay(300);
          </script>
        </xsl:if>

        <!-- Include stylesheet declared within item -->
        <xsl:apply-templates select="qti:stylesheet"/>
      </head>
      <body class="qtiworks assessmentItem assessmentTest">
        <xsl:call-template name="maybeAddAuthoringLink"/>

        <!--
        Show 'during' tetFeedback for the current testPart and/or the test itself.
        The info model says this should be shown directly after outcome processing.
        This is equivalent in this case to the item's sessionStatus='final'
        -->
        <xsl:if test="$sessionStatus='final'">
          <!-- Show any 'during' testFeedback for the current testPart -->
          <xsl:apply-templates select="$currentTestPart/qti:testFeedback[@access='during']"/>

          <!-- Show any 'during' testFeedback for the test -->
          <xsl:apply-templates select="$assessmentTest/qti:testFeedback[@access='during']"/>
        </xsl:if>

        <!-- Drill down into current item via current testPart structure -->
        <xsl:apply-templates select="$currentTestPartNode" mode="testPart-drilldown"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="qw:test-controls">
    <ul class="sessionControl">
      <!-- Interacting state -->
      <xsl:if test="$advanceTestItemAllowed">
        <li>
          <form action="{$webappContextPath}{$advanceTestItemUrl}" method="post" target="oaa0">
            <input type="submit" value="Next Question" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
      <xsl:if test="$testPartNavigationAllowed">
        <li>
          <form action="{$webappContextPath}{$testPartNavigationUrl}" method="post" target="oaa0">
            <input type="submit" value="Test Question Menu" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
      <xsl:if test="$endTestPartAllowed">
        <li>
          <form action="{$webappContextPath}{$endTestPartUrl}" method="post" target="oaa0"
            onsubmit="return confirm({qw:to-javascript-string($endTestPartAlertMessage)})">
            <input type="submit" value="End {$testOrTestPart}" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
      <!-- Review state -->
      <xsl:if test="$reviewMode">
        <li>
          <form action="{$webappContextPath}{$reviewTestPartUrl}" method="post" target="oaa0">
            <input type="submit" value="Back to Test Feedback" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
      <xsl:if test="$provideItemSolutionButton">
        <li>
          <form action="{$webappContextPath}{$showTestItemSolutionUrl}/{$itemKey}" method="post" target="oaa0">
            <input type="submit" value="Show Solution" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
      <xsl:if test="$reviewMode and $solutionMode">
        <!-- Allow return to item review state -->
        <li>
          <form action="{$webappContextPath}{$reviewTestItemUrl}/{$itemKey}" method="post" target="oaa0">
            <input type="submit" value="Hide Solution" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
    </ul>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="qw:node[@type='TEST_PART']" mode="testPart-drilldown">
    <ul class="testPartDrilldown">
      <xsl:apply-templates mode="testPart-drilldown"/>
    </ul>
  </xsl:template>

  <xsl:template match="qw:node[@type='ASSESSMENT_SECTION']" mode="testPart-drilldown">
    <xsl:if test=".//qw:node[@key=$itemKey]">
      <!-- Only show sections that ancestors of current item -->
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
        <ul class="testPartDrilldownInner">
          <xsl:apply-templates mode="testPart-drilldown"/>
        </ul>
      </li>
    </xsl:if>
  </xsl:template>

  <xsl:template match="qw:node[@type='ASSESSMENT_ITEM_REF']" mode="testPart-drilldown">
    <xsl:if test="@key=$itemKey">
      <!-- We've reached the current item -->
      <li class="currentItem">

        <!-- Render item -->
        <xsl:apply-templates select="$assessmentItem" mode="render-item"/>

        <!-- Put session controls here -->
        <xsl:call-template name="qw:test-controls"/>
      </li>
    </xsl:if>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="qti:assessmentItem" mode="render-item">
    <!-- Item title -->
    <h1 class="itemTitle">
      <xsl:apply-templates select="$itemSessionState" mode="item-status"/>
      <xsl:value-of select="@title"/>
    </h1>

    <!-- Render item body -->
    <xsl:apply-templates select="qti:itemBody"/>

    <!-- Display active modal feedback (only after responseProcessing) -->
    <xsl:if test="$itemFeedbackAllowed and $sessionStatus='final'">
      <xsl:variable name="modalFeedback" as="element()*">
        <xsl:for-each select="qti:modalFeedback">
          <xsl:variable name="feedback" as="node()*">
            <xsl:call-template name="feedback"/>
          </xsl:variable>
          <xsl:if test="$feedback">
            <div class="modalFeedback">
              <xsl:if test="@title"><h3><xsl:value-of select="@title"/></h3></xsl:if>
              <xsl:sequence select="$feedback"/>
            </div>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      <xsl:if test="exists($modalFeedback)">
        <div class="modalFeedback">
          <h2>Feedback</h2>
          <xsl:sequence select="$modalFeedback"/>
        </div>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="qti:itemBody">
    <div id="itemBody">
      <form method="post" action="{$webappContextPath}{$responseUrl}"
        enctype="multipart/form-data" accept-charset="UTF-8" target="oaa0"
        onsubmit="return QtiWorksRendering.maySubmit()"
        onreset="QtiWorksRendering.reset()" autocomplete="off">

        <xsl:apply-templates/>

        <xsl:choose>
          <xsl:when test="$allowComment and $isItemSessionOpen">
            <fieldset class="candidateComment">
              <legend>Please use the following text box if you need to provide any additional information, comments or feedback during this test:</legend>
              <input name="qtiworks_comment_presented" type="hidden" value="true"/>
              <textarea name="qtiworks_comment"><xsl:value-of select="$itemSessionState/qw:candidateComment"/></textarea>
            </fieldset>
          </xsl:when>
          <xsl:when test="$allowComment and $isItemSessionEnded and exists($itemSessionState/qw:candidateComment)">
            <fieldset class="candidateComment">
              <legend>You submitted the folllowing comment with this item:</legend>
              <input name="qtiworks_comment_presented" type="hidden" value="true"/>
              <textarea name="qtiworks_comments" disabled="disabled"><xsl:value-of select="$itemSessionState/qw:candidateComment"/></textarea>
            </fieldset>
          </xsl:when>
        </xsl:choose>

        <xsl:if test="$isItemSessionOpen">
          <div class="testItemControl">
            <input id="submit_button" name="submit" type="submit" value="{$submitButtonText}" class="btn btn-primary"/>
          </div>
        </xsl:if>
      </form>
    </div>
  </xsl:template>

  <!-- Override using 'showFeedback' -->
  <xsl:template match="qti:feedbackInline | qti:feedbackBlock">
    <xsl:if test="$itemFeedbackAllowed">
      <xsl:apply-imports/>
    </xsl:if>
  </xsl:template>

  <!-- Disable any buttons in the question (from endAttemptInteraction) if not in interacting state -->
  <xsl:template match="qti:endAttemptInteraction[not($isItemSessionOpen)]">
    <input type="submit" name="{@responseIdentifier}" value="{@title}" disabled="disabled"/>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Overridden to add support for review state -->
  <xsl:template match="qw:itemSessionState" mode="item-status">
    <xsl:choose>
      <xsl:when test="$solutionMode">
        <span class="itemStatus review">Model Solution</span>
      </xsl:when>
      <xsl:when test="$reviewMode">
        <xsl:choose>
          <xsl:when test="not(empty(@unboundResponseIdentifiers) and empty(@invalidResponseIdentifiers))">
            <span class="itemStatus reviewInvalid">Review (Invalid Answer)</span>
          </xsl:when>
          <xsl:when test="@responded='true'">
            <span class="itemStatus review">Review</span>
          </xsl:when>
          <xsl:when test="@entryTime!=''">
            <span class="itemStatus reviewNotAnswered">Review (Not Answered)</span>
          </xsl:when>
          <xsl:otherwise>
            <span class="itemStatus reviewNotSeen">Review (Not Seen)</span>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-imports/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
