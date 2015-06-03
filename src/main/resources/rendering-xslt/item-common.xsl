<?xml version="1.0" encoding="UTF-8"?>
<!--

Common templates for QTI flow elements, used in both item and test
rendering.

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti xs qw saxon m">

  <!-- ************************************************************ -->

  <xsl:import href="qti-common.xsl"/>

  <!-- State of item being rendered -->
  <xsl:param name="itemSessionState" as="element(qw:itemSessionState)"/>

  <!-- Flag to enable modal rendering of model solution for this item -->
  <xsl:param name="solutionMode" as="xs:boolean" required="yes"/>

  <!-- Extract information from the <itemSessionState> -->
  <xsl:variable name="shuffledChoiceOrders" select="$itemSessionState/qw:shuffledInteractionChoiceOrder"
    as="element(qw:shuffledInteractionChoiceOrder)*"/>
  <xsl:variable name="templateValues" select="$itemSessionState/qw:templateVariable" as="element(qw:templateVariable)*"/>
  <xsl:variable name="responseValues" select="$itemSessionState/qw:responseVariable" as="element(qw:responseVariable)*"/>
  <xsl:variable name="outcomeValues" select="$itemSessionState/qw:outcomeVariable" as="element(qw:outcomeVariable)*"/>
  <xsl:variable name="overriddenCorrectResponses" select="$itemSessionState/qw:overriddenCorrectResponse" as="element(qw:overriddenCorrectResponse)*"/>
  <xsl:variable name="sessionStatus" select="$itemSessionState/@sessionStatus" as="xs:string"/>
  <xsl:variable name="isItemSessionEnded" as="xs:boolean" select="$itemSessionState/@endTime!='' or $solutionMode"/>
  <xsl:variable name="isItemSessionOpen" as="xs:boolean" select="$itemSessionState/@entryTime!='' and not($isItemSessionEnded)"/>
  <xsl:variable name="isItemSessionExited" as="xs:boolean" select="$itemSessionState/@exitTime!=''"/>

  <!-- Raw response inputs -->
  <xsl:variable name="responseInputs" select="$itemSessionState/qw:responseInput" as="element(qw:responseInput)*"/>

  <!-- Uncommitted response values -->
  <xsl:variable name="uncommittedResponseValues" select="$itemSessionState/qw:uncommittedResponseValue" as="element(qw:uncommittedResponseValue)*"/>

  <!-- Bad/invalid responses -->
  <xsl:variable name="unboundResponseIdentifiers" select="tokenize($itemSessionState/@unboundResponseIdentifiers, '\s+')" as="xs:string*"/>
  <xsl:variable name="invalidResponseIdentifiers" select="tokenize($itemSessionState/@invalidResponseIdentifiers, '\s+')" as="xs:string*"/>

  <!-- Is a model solution provided? -->
  <xsl:variable name="hasModelSolution" as="xs:boolean" select="exists(/qti:assessmentItem/qti:responseDeclaration/qti:correctResponse) or exists($overriddenCorrectResponses)"/>

  <!-- Is there templateProcessing or responseProcessing? -->
  <xsl:variable name="hasTemplateProcessing" as="xs:boolean" select="exists(/qti:assessmentItem/qti:templateProcessing)"/>
  <xsl:variable name="hasResponseProcessing" as="xs:boolean" select="exists(/qti:assessmentItem/qti:responseProcessing)"/>

  <!-- Include stylesheets handling each type of interaction -->
  <xsl:include href="interactions/associateInteraction.xsl"/>
  <xsl:include href="interactions/choiceInteraction.xsl"/>
  <xsl:include href="interactions/drawingInteraction.xsl"/>
  <xsl:include href="interactions/endAttemptInteraction.xsl"/>
  <xsl:include href="interactions/extendedTextInteraction.xsl"/>
  <xsl:include href="interactions/gapMatchInteraction.xsl"/>
  <xsl:include href="interactions/graphicAssociateInteraction.xsl"/>
  <xsl:include href="interactions/graphicGapMatchInteraction.xsl"/>
  <xsl:include href="interactions/graphicOrderInteraction.xsl"/>
  <xsl:include href="interactions/hotspotInteraction.xsl"/>
  <xsl:include href="interactions/hottextInteraction.xsl"/>
  <xsl:include href="interactions/inlineChoiceInteraction.xsl"/>
  <xsl:include href="interactions/matchInteraction.xsl"/>
  <xsl:include href="interactions/mediaInteraction.xsl"/>
  <xsl:include href="interactions/orderInteraction.xsl"/>
  <xsl:include href="interactions/positionObjectInteraction.xsl"/>
  <xsl:include href="interactions/selectPointInteraction.xsl"/>
  <xsl:include href="interactions/sliderInteraction.xsl"/>
  <xsl:include href="interactions/textEntryInteraction.xsl"/>
  <xsl:include href="interactions/uploadInteraction.xsl"/>
  <xsl:include href="interactions/mathEntryInteraction.xsl"/>

  <!-- ************************************************************ -->
  <!-- Response helpers -->

  <xsl:function name="qw:get-response-input" as="element(qw:responseInput)?">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$responseInputs[@identifier=$identifier]"/>
  </xsl:function>

  <xsl:function name="qw:is-bad-response" as="xs:boolean">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$unboundResponseIdentifiers=$identifier"/>
  </xsl:function>

  <xsl:function name="qw:is-invalid-response" as="xs:boolean">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$invalidResponseIdentifiers=$identifier"/>
  </xsl:function>

  <xsl:function name="qw:extract-single-cardinality-response-input" as="xs:string">
    <xsl:param name="responseInput" as="element(qw:responseInput)?"/>
    <xsl:choose>
      <xsl:when test="$responseInput/qw:file">
        <xsl:message terminate="yes">This function does not support file responses</xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="strings" select="$responseInput/qw:string" as="element(qw:string)*"/>
        <xsl:choose>
          <xsl:when test="not(exists($strings))">
            <xsl:sequence select="''"/>
          </xsl:when>
          <xsl:when test="count($strings)=1">
            <xsl:sequence select="$strings[1]"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="yes">
              Expected response input <xsl:copy-of select="$responseInput"/> to contain one string value only
            </xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- ************************************************************ -->
  <!-- Variable declaration helpers -->

  <xsl:function name="qw:get-template-declaration" as="element(qti:templateDeclaration)?">
    <xsl:param name="document" as="document-node()"/>
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$document/qti:assessmentItem/qti:templateDeclaration[@identifier=$identifier]"/>
  </xsl:function>

  <xsl:function name="qw:get-outcome-declaration" as="element(qti:outcomeDeclaration)?">
    <xsl:param name="document" as="document-node()"/>
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$document/qti:assessmentItem/qti:outcomeDeclaration[@identifier=$identifier]"/>
  </xsl:function>

  <xsl:function name="qw:get-response-declaration" as="element(qti:responseDeclaration)?">
    <xsl:param name="document" as="document-node()"/>
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$document/qti:assessmentItem/qti:responseDeclaration[@identifier=$identifier]"/>
  </xsl:function>

  <!-- ************************************************************ -->
  <!-- Variable value helpers -->

  <xsl:function name="qw:get-template-value" as="element(qw:templateVariable)?">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$templateValues[@identifier=$identifier]"/>
  </xsl:function>

  <xsl:function name="qw:get-outcome-value" as="element(qw:outcomeVariable)?">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:sequence select="$outcomeValues[@identifier=$identifier]"/>
  </xsl:function>

  <!-- NB: This now checks *uncommitted* responses first, then *committed* responses -->
  <xsl:function name="qw:get-response-value" as="element(qw:responseVariable)?">
    <xsl:param name="document" as="document-node()"/>
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:variable name="responseDeclaration" select="qw:get-response-declaration($document, $identifier)" as="element(qti:responseDeclaration)?"/>
    <xsl:choose>
      <xsl:when test="$solutionMode and $overriddenCorrectResponses[@identifier=$identifier]">
        <!-- Correct response has been set during template processing -->
        <xsl:for-each select="$overriddenCorrectResponses[@identifier=$identifier]">
          <qw:responseVariable>
            <xsl:copy-of select="@cardinality, @baseType"/>
            <xsl:copy-of select="qw:value"/>
          </qw:responseVariable>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$solutionMode and $responseDeclaration/qti:correctResponse">
        <!-- <correctResponse> has been set in the QTI -->
        <!-- (We need to convert QTI <qti:correctResponse/> to <qw:responseVariable/>) -->
        <xsl:for-each select="$responseDeclaration/qti:correctResponse">
          <qw:responseVariable>
            <xsl:copy-of select="../@cardinality, ../@baseType"/>
            <xsl:for-each select="qti:value">
              <qw:value>
                <xsl:copy-of select="@fieldIdentifier, @baseType"/>
                <xsl:copy-of select="text()"/>
              </qw:value>
            </xsl:for-each>
          </qw:responseVariable>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$uncommittedResponseValues[@identifier=$identifier]">
        <!-- There's an uncommitted value here. We don't distinguish between uncommitted and committed during rendering -->
        <xsl:for-each select="$uncommittedResponseValues[@identifier=$identifier]">
          <qw:responseVariable>
            <xsl:copy-of select="@cardinality, @baseType"/>
            <xsl:copy-of select="qw:value"/>
          </qw:responseVariable>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <!-- This is a committed value, which is already in a <qw:responseVariable/> -->
        <xsl:sequence select="$responseValues[@identifier=$identifier]"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- ************************************************************ -->

  <!-- Tests the @showHide and @templateIdentifier attributes of the given (choice) element to determine whether it
  should be shown or not -->
  <xsl:function name="qw:is-visible" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[$overrideTemplate
        or not(@templateIdentifier)
        or (qw:value-contains(qw:get-template-value(@templateIdentifier), @identifier) and not(@showHide='hide'))])"/>
  </xsl:function>

  <!-- Filters out the elements in the given sequence having @showHide and @templateIdentifier attributes to return
  the ones that will actually be visible -->
  <xsl:function name="qw:filter-visible" as="element()*">
    <xsl:param name="elements" as="element()*"/>
    <xsl:sequence select="$elements[qw:is-visible(.)]"/>
  </xsl:function>

  <xsl:function name="qw:get-shuffled-choice-order" as="xs:string*">
    <xsl:param name="interaction" as="element()"/>
    <xsl:variable name="choiceSequence" as="xs:string?"
      select="$shuffledChoiceOrders[@responseIdentifier=$interaction/@responseIdentifier]/@choiceSequence"/>
    <xsl:sequence select="tokenize($choiceSequence, ' ')"/>
  </xsl:function>

  <xsl:function name="qw:get-visible-ordered-choices" as="element()*">
    <xsl:param name="interaction" as="element()"/>
    <xsl:param name="choices" as="element()*"/>
    <xsl:variable name="orderedChoices" as="element()*">
      <xsl:choose>
        <xsl:when test="$interaction/@shuffle='true'">
          <xsl:for-each select="qw:get-shuffled-choice-order($interaction)">
            <xsl:sequence select="$choices[@identifier=current()]"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="$choices"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:sequence select="qw:filter-visible($orderedChoices)"/>
  </xsl:function>

  <xsl:template name="qw:generic-bad-response-message">
    <div class="badResponse">
      Please complete this interaction as directed.
    </div>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="qti:infoControl" as="element(div)">
    <div class="infoControl">
      <input type="submit" onclick="return QtiWorksRendering.showInfoControlContent(this)" value="{@title}"/>
      <div class="infoControlContent">
        <xsl:apply-templates/>
      </div>
    </div>
  </xsl:template>

  <!-- Stylesheet link -->
  <xsl:template match="qti:stylesheet" as="element(link)">
    <link rel="stylesheet">
      <xsl:copy-of select="@* except @href"/>
      <xsl:if test="exists(@href)">
        <xsl:attribute name="href" select="qw:convert-link(@href)"/>
      </xsl:if>
    </link>
  </xsl:template>

  <!-- prompt -->
  <xsl:template match="qti:prompt">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- param (override to handle template variables) -->
  <xsl:template match="qti:param">
    <xsl:variable name="templateValue" select="qw:get-template-value(@value)" as="element(qw:templateVariable)?"/>
    <!-- Note: spec is not explicit in that we really only allow single cardinality param substitution -->
    <param data-dave="yes" name="{@name}" value="{if (exists($templateValue)
        and qw:is-single-cardinality-value($templateValue)
        and qw:get-template-declaration(/, @value)[@paramVariable='true'])
      then qw:extract-single-cardinality-value($templateValue) else @value}"/>
  </xsl:template>

  <xsl:template match="qti:rubricBlock" as="element(div)">
    <div class="rubric {@view}">
      <xsl:if test="not($view) or ($view = @view)">
        <xsl:apply-templates/>
      </xsl:if>
    </div>
  </xsl:template>

  <!-- printedVariable. Numeric output currently only supports Java String.format formatting. -->
  <xsl:template match="qti:assessmentItem//qti:printedVariable" as="element(span)">
    <xsl:variable name="identifier" select="@identifier" as="xs:string"/>
    <xsl:variable name="templateValue" select="qw:get-template-value(@identifier)" as="element(qw:templateVariable)?"/>
    <xsl:variable name="outcomeValue" select="qw:get-outcome-value(@identifier)" as="element(qw:outcomeVariable)?"/>
    <span class="printedVariable">
      <xsl:choose>
        <xsl:when test="exists($outcomeValue)">
          <xsl:call-template name="printedVariable">
            <xsl:with-param name="source" select="."/>
            <xsl:with-param name="valueHolder" select="$outcomeValue"/>
            <xsl:with-param name="valueDeclaration" select="qw:get-outcome-declaration(/, @identifier)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="exists($templateValue)">
          <xsl:call-template name="printedVariable">
            <xsl:with-param name="source" select="."/>
            <xsl:with-param name="valueHolder" select="$templateValue"/>
            <xsl:with-param name="valueDeclaration" select="qw:get-template-declaration(/, @identifier)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          (variable <xsl:value-of select="$identifier"/> was not found)
        </xsl:otherwise>
      </xsl:choose>
    </span>
  </xsl:template>

  <!-- Keep MathML by default -->
  <xsl:template match="m:*" as="element()">
    <xsl:element name="{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <!-- MathML parallel markup containers: we'll remove any non-XML annotations, which may
  result in the container also being removed as it's no longer required in that case. -->
  <xsl:template match="m:semantics" as="element()*">
    <xsl:choose>
      <xsl:when test="not(*[position()!=1 and self::m:annotation-xml])">
        <!-- All annotations are non-XML so remove this wrapper completely (and unwrap a container mrow if required) -->
        <xsl:apply-templates select="if (*[1][self::m:mrow]) then *[1]/* else *[1]"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Keep non-XML annotations -->
        <xsl:element name="semantics" namespace="http://www.w3.org/1998/Math/MathML">
          <xsl:apply-templates select="* except m:annotation"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="m:*/text()" as="text()">
    <!-- NOTE: The XML input is produced using JQTI's toXmlString() method, which has
    the unfortunate effect of indenting MathML, so we'll renormalise -->
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

  <!-- mathml (mi) -->
  <!--
  We are extending the spec here in 2 ways:
  1. Allowing MathsContent variables to be substituted
  2. Allowing arbitrary response and outcome variables to be substituted.
  -->
  <xsl:template match="qti:assessmentItem//m:mi" as="element()">
    <xsl:variable name="content" select="normalize-space(text())" as="xs:string"/>
    <xsl:variable name="templateValue" select="qw:get-template-value($content)" as="element(qw:templateVariable)?"/>
    <xsl:variable name="responseValue" select="qw:get-response-value(/, $content)" as="element(qw:responseVariable)?"/>
    <xsl:variable name="outcomeValue" select="qw:get-outcome-value($content)" as="element(qw:outcomeVariable)?"/>
    <xsl:choose>
      <xsl:when test="exists($templateValue) and qw:get-template-declaration(/, $content)[@mathVariable='true']">
        <xsl:call-template name="substitute-mi">
          <xsl:with-param name="identifier" select="$content"/>
          <xsl:with-param name="value" select="$templateValue"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="exists($responseValue)">
        <xsl:call-template name="substitute-mi">
          <xsl:with-param name="identifier" select="$content"/>
          <xsl:with-param name="value" select="$responseValue"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="exists($outcomeValue)">
        <xsl:call-template name="substitute-mi">
          <xsl:with-param name="identifier" select="$content"/>
          <xsl:with-param name="value" select="$outcomeValue"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="mi" namespace="http://www.w3.org/1998/Math/MathML">
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- mathml (ci) -->
  <!--
  We are extending the spec here in 2 ways:
  1. Allowing MathsContent variables to be substituted
  2. Allowing arbitrary response and outcome variables to be substituted.
  -->
  <xsl:template match="qti:assessmentItem//m:ci" as="element()*">
    <xsl:variable name="content" select="normalize-space(text())" as="xs:string"/>
    <xsl:variable name="templateValue" select="qw:get-template-value($content)" as="element(qw:templateVariable)?"/>
    <xsl:variable name="responseValue" select="qw:get-response-value(/, $content)" as="element(qw:responseVariable)?"/>
    <xsl:variable name="outcomeValue" select="qw:get-outcome-value($content)" as="element(qw:outcomeVariable)?"/>
    <xsl:choose>
      <xsl:when test="exists($templateValue) and qw:get-template-declaration(/, $content)[@mathVariable='true']">
        <xsl:call-template name="substitute-ci">
          <xsl:with-param name="identifier" select="$content"/>
          <xsl:with-param name="value" select="$templateValue"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="exists($responseValue)">
        <xsl:call-template name="substitute-ci">
          <xsl:with-param name="identifier" select="$content"/>
          <xsl:with-param name="value" select="$responseValue"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="exists($outcomeValue)">
        <xsl:call-template name="substitute-ci">
          <xsl:with-param name="identifier" select="$content"/>
          <xsl:with-param name="value" select="$outcomeValue"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="ci" namespace="http://www.w3.org/1998/Math/MathML">
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- feedback (block and inline) -->
  <xsl:template name="feedback" as="node()*">
    <xsl:choose>
      <xsl:when test="$overrideFeedback">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="identifierMatch" select="boolean(qw:value-contains(qw:get-outcome-value(@outcomeIdentifier), @identifier))" as="xs:boolean"/>
        <xsl:if test="($identifierMatch and @showHide='show') or (not($identifierMatch) and @showHide='hide')">
          <xsl:apply-templates/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- templateBlock -->
  <xsl:template match="qti:templateBlock" as="node()*">
    <xsl:call-template name="template"/>
  </xsl:template>

  <!-- templateInline -->
  <xsl:template match="qti:templateInline" as="node()*">
    <xsl:call-template name="template"/>
  </xsl:template>

  <!-- template (block and feedback) -->
  <xsl:template name="template" as="node()*">
    <xsl:choose>
      <xsl:when test="$overrideTemplate">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="identifierMatch" select="boolean(qw:value-contains(qw:get-template-value(@templateIdentifier),@identifier))" as="xs:boolean"/>
        <xsl:if test="($identifierMatch and @showHide='show') or (not($identifierMatch) and @showHide='hide')">
          <xsl:apply-templates/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
