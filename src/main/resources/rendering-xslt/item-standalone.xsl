<?xml version="1.0" encoding="UTF-8"?>
<!--

Renders a standalone assessmentItem

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
  <xsl:import href="item-common.xsl"/>
  <xsl:import href="utils.xsl"/>

  <!-- Item prompt -->
  <xsl:param name="prompt" select="()" as="xs:string?"/>

  <!-- Action permissions -->
  <xsl:param name="endAllowed" as="xs:boolean" required="yes"/>
  <xsl:param name="solutionAllowed" as="xs:boolean" required="yes"/>
  <xsl:param name="softSoftResetAllowed" as="xs:boolean" required="yes"/>
  <xsl:param name="hardResetAllowed" as="xs:boolean" required="yes"/>
  <xsl:param name="candidateCommentAllowed" as="xs:boolean" required="yes"/>

  <!-- Action URLs -->
  <xsl:param name="softResetUrl" as="xs:string" required="yes"/>
  <xsl:param name="hardResetUrl" as="xs:string" required="yes"/>
  <xsl:param name="endUrl" as="xs:string" required="yes"/>
  <xsl:param name="solutionUrl" as="xs:string" required="yes"/>
  <xsl:param name="exitUrl" as="xs:string" required="yes"/>

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
            UpConversionAjaxController.setUpConversionServiceUrl('<xsl:value-of select="$webappContextPath"/>/candidate/verifyAsciiMath');
            UpConversionAjaxController.setDelay(300);
          </script>
        </xsl:if>

        <!-- Include stylesheet declared within item -->
        <xsl:apply-templates select="qti:stylesheet"/>
      </head>
      <body class="qtiworks assessmentItem">
        <xsl:call-template name="maybeAddAuthoringLink"/>

        <!-- Item title -->
        <h1 class="itemTitle">
          <xsl:apply-templates select="$itemSessionState" mode="item-status"/>
          <xsl:value-of select="@title"/>
        </h1>

        <!-- Delivery prompt -->
        <xsl:if test="$prompt">
          <div class="itemPrompt">
            <xsl:value-of select="$prompt"/>
          </div>
        </xsl:if>

        <!-- Item body -->
        <xsl:apply-templates select="qti:itemBody"/>

        <!-- Display active modal feedback (only after responseProcessing) -->
        <xsl:if test="$sessionStatus='final'">
          <xsl:variable name="modalFeedback" as="element()*">
            <xsl:for-each select="qti:modalFeedback">
              <xsl:variable name="feedback" as="node()*">
                <xsl:call-template name="feedback"/>
              </xsl:variable>
              <xsl:if test="$feedback">
                <div class="modalFeedbackItem">
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

        <!-- Session control -->
        <xsl:call-template name="qw:item-controls"/>
       </body>
    </html>
  </xsl:template>

  <xsl:template name="qw:item-controls">
    <ul class="sessionControl">
      <xsl:if test="$softSoftResetAllowed">
        <li>
          <form action="{$webappContextPath}{$softResetUrl}" method="post" target="oaa0">
            <input type="submit" value="Reset{if ($isItemSessionEnded) then ' and play again' else ''}" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
      <xsl:if test="$hardResetAllowed and $hasTemplateProcessing">
        <li>
          <form action="{$webappContextPath}{$hardResetUrl}" method="post" target="oaa0">
            <input type="submit" value="Reinitialise{if ($isItemSessionEnded) then ' and play again' else ''}" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
      <xsl:if test="$endAllowed and $hasResponseProcessing">
        <li>
          <form action="{$webappContextPath}{$endUrl}" method="post" target="oaa0">
            <input type="submit" value="Finish and review" class="btn btn-default"/>
          </form>
        </li>
      </xsl:if>
      <xsl:if test="$solutionAllowed and $hasModelSolution">
        <li>
          <form action="{$webappContextPath}{$solutionUrl}" method="post" target="oaa0">
            <input type="submit" value="Show model solution" class="btn btn-default">
              <xsl:if test="$solutionMode">
                <!-- Already in solution mode -->
                <xsl:attribute name="disabled" select="'disabled'"/>
              </xsl:if>
            </input>
          </form>
        </li>
      </xsl:if>
      <li>
        <form action="{$webappContextPath}{$exitUrl}" method="post" target="oaa0">
          <input type="submit" value="Exit" class="btn btn-default"/>
        </form>
      </li>
    </ul>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="qti:itemBody">
    <div id="itemBody">
      <form method="post" action="{$webappContextPath}{$responseUrl}"
        enctype="multipart/form-data" accept-charset="UTF-8" target="oaa0"
        onsubmit="return QtiWorksRendering.maySubmit()"
        onreset="QtiWorksRendering.reset()" autocomplete="off">

        <xsl:apply-templates/>

        <xsl:if test="$candidateCommentAllowed">
          <fieldset class="candidateComment">
            <legend>Please use the following text box if you need to provide any additional information, comments or feedback during this test:</legend>
            <input name="qtiworks_comment_presented" type="hidden" value="true"/>
            <textarea name="qtiworks_comment"><xsl:value-of select="$itemSessionState/qw:candidateComment"/></textarea>
          </fieldset>
        </xsl:if>

        <xsl:if test="$isItemSessionOpen">
          <div class="controls">
            <input id="submit_button" name="submit" type="submit" value="SUBMIT RESPONSE"/>
          </div>
        </xsl:if>
      </form>
    </div>
  </xsl:template>

  <!-- Overridden to add support for solution state -->
  <xsl:template match="qw:itemSessionState" mode="item-status">
    <xsl:choose>
      <xsl:when test="$solutionMode">
        <span class="itemStatus review">Model Solution</span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-imports/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
