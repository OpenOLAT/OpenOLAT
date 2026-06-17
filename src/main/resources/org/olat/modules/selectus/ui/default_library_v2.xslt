<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  
	<xsl:template match="coverxml">
		<xsl:apply-templates select="cover-v2" />
	</xsl:template>

	<xsl:template match="cover-v2">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="simpleA4"
					page-height="29.7cm" page-width="21cm" margin-top="2cm"
					margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
					<fo:region-body margin-top="0mm" margin-bottom="0mm" />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<fo:page-sequence master-reference="simpleA4">
				<fo:flow flow-name="xsl-region-body">
					<xsl:call-template name="page-body" />
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
	
	<xsl:template name="page-body">
		<fo:block-container
			font-family="sans-serif" font-size="18pt"
			margin-top="0cm" margin-bottom="0cm"><xsl:call-template name="position" /></fo:block-container>
		<fo:block-container
			font-family="sans-serif" font-size="12pt"
			margin-top="0.2cm" margin-bottom="0cm"><xsl:apply-templates select="application" /></fo:block-container>
	</xsl:template>
  
	<xsl:template match="application/person">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="id/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="id"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="fullname/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="fullname"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="gender/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="gender"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="maritalStatus/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="maritalStatus"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="academicTitle/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="academicTitle"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="birthday/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="birthday"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="nationality/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="nationality"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="addNationalities/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="addNationalities"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="disability/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="disability"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="application/contact[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-sub-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="phone/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="phone"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="mobilePhone/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="mobilePhone"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="mail/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="mail"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="application/businessInformations[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-sub-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="addressOrg/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="addressOrg"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="addressUnit/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="addressUnit"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="addressCurrentPosition/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="addressCurrentPosition"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="application/address[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-sub-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="addressLine1/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="addressLine1"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="addressLine2/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="addressLine2"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="addressLine3/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="addressLine3"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="city/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="city"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="country/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="country"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<xsl:template match="application/businessAddress[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-sub-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="biz_addressLine1/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="biz_addressLine1"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="biz_addressLine2/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="biz_addressLine2"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="biz_addressLine3/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="biz_addressLine3"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="biz_city/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="biz_city"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="biz_country/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="biz_country"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="biz_phone/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="biz_phone"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="biz_mail/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="biz_mail"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="academicalBackground">
		<xsl:if test="highestDegree[@visible='true'] or highestDegreeWorkedSince[@visible='true'] or dissertation[@visible='true'] or customAttributesGroup[@visible='true']">
			<fo:table>
				<xsl:call-template name="columns-def"/>
				<fo:table-body>
					<xsl:call-template name="step-title">
						<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
					</xsl:call-template>
				</fo:table-body>
			</fo:table>
		</xsl:if>
		<xsl:apply-templates select="highestDegree" />
		<xsl:apply-templates select="highestDegreeWorkedSince" />
		<xsl:apply-templates select="dissertation" />
		<xsl:apply-templates select="customAttributesGroup" />
	</xsl:template>
	
	<xsl:template match="highestDegree[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-sub-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="numOfPublications/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="numOfPublications"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="numOfFirstAuthorships/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="numOfFirstAuthorships"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="numOfLastAuthorships/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="numOfLastAuthorships"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="citations/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="citations"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="impactFactor/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="impactFactor"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="hFactor/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="hFactor"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="highestdegree/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="highestdegree"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="highestDegreeWorkedSince[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-sub-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="workedInAcademiaWS/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="workedInAcademiaWS"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="workedOutAcademiaWS/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="workedOutAcademiaWS"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="workedOutAcademiaCareWS/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="workedOutAcademiaCareWS"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="careerDescriptionWS/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="careerDescriptionWS"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="dissertation[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-sub-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="dissertation/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="dissertation"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="dissertationKeyword1/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="dissertationKeyword1"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="dissertationKeyword2/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="dissertationKeyword2"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="dissertationKeyword3/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="dissertationKeyword3"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="habilitation/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="habilitation"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="orcid/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="orcid"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="project[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectTitle/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectTitle"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectAcronym/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectAcronym"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectKeywords/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectKeywords"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectDisciplines/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectDisciplines"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectStartDate/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectStartDate"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectDuration/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectDuration"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectFinancialImpact1/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectFinancialImpact1"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectFinancialImpact2/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectFinancialImpact2"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectFinancialImpact3/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectFinancialImpact3"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectFinancialImpact4/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectFinancialImpact4"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectFinancialImpact5/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectFinancialImpact5"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-conditional">
					<xsl:with-param name="label"><xsl:value-of select="projectDescription/@label"/></xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="projectDescription"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
		
		<xsl:apply-templates select="customAttributesGroup" />
	</xsl:template>
	
	<xsl:template match="personalCustomAttributes">
		<xsl:apply-templates select="customAttributesGroup" />
	</xsl:template>
	
	<xsl:template match="customSteps">
		<xsl:apply-templates select="customStep" />
	</xsl:template>
	
	<xsl:template match="customStep[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
		<xsl:apply-templates select="customAttributesGroup" />
	</xsl:template>

	<xsl:template match="customAttributesGroup[@visible='true']">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="step-sub-title">
					<xsl:with-param name="title"><xsl:value-of select="@title"/></xsl:with-param>
				</xsl:call-template>
				<xsl:apply-templates select="customAttribute" />
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="customAttribute">
		<xsl:call-template name="applicant-row-conditional">
			<xsl:with-param name="label"><xsl:value-of select="@label"/></xsl:with-param>
			<xsl:with-param name="value"><xsl:value-of select="."/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="step-title">
		<xsl:param name="title"/>
		<xsl:if test="$title != ''">
			<fo:table-row>
  				<fo:table-cell number-columns-spanned="2"><fo:block font-weight="bold" font-size="20pt" padding-top="0.7cm" padding-bottom="0.2cm">
  					<xsl:value-of select="$title"/></fo:block></fo:table-cell>
			</fo:table-row>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="step-sub-title">
		<xsl:param name="title"/>
		<xsl:if test="$title != ''">
			<fo:table-row>
  				<fo:table-cell number-columns-spanned="2"><fo:block font-weight="bold" font-size="16pt" padding-top="0.3cm" padding-bottom="0.2cm">
  					<xsl:value-of select="$title"/></fo:block></fo:table-cell>
			</fo:table-row>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="applicant-row-big-conditional">
		<xsl:param name="label"/>
		<xsl:param name="value"/>
		<xsl:if test="$value != ''">
			<fo:table-row>
  				<fo:table-cell><fo:block font-weight="bold" font-size="24pt"><xsl:value-of select="$label"/><xsl:value-of select="':'"/></fo:block></fo:table-cell>
  				<fo:table-cell><fo:block font-size="24pt"><xsl:value-of select="$value"/></fo:block></fo:table-cell>
			</fo:table-row>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="applicant-row-conditional">
		<xsl:param name="label"/>
		<xsl:param name="value"/>
		<xsl:if test="$value != ''">
			<fo:table-row>
  				<fo:table-cell><fo:block font-weight="bold" font-size="11pt"><xsl:value-of select="$label"/><xsl:value-of select="':'"/></fo:block></fo:table-cell>
  				<fo:table-cell>
  					<xsl:if test="string-length(label) &gt; 22">
  						<fo:block font-size="11pt"><fo:inline color="#ffffff"><xsl:value-of select="'___'"/></fo:inline></fo:block>
  					</xsl:if>
  					<xsl:if test="string-length(label) &gt; 55">
  						<fo:block font-size="11pt"><fo:inline color="#ffffff"><xsl:value-of select="'___'"/></fo:inline></fo:block>
  					</xsl:if>
  					<fo:block font-size="11pt"><xsl:value-of select="$value"/></fo:block>
  				</fo:table-cell>
			</fo:table-row>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="columns-def">
		<xsl:attribute name="table-layout">fixed</xsl:attribute>
		<xsl:attribute name="width">100%</xsl:attribute>
		<xsl:attribute name="border-collapse">collapse</xsl:attribute>
		<xsl:attribute name="margin-top">0cm</xsl:attribute>
		<xsl:attribute name="margin-bottom">0cm</xsl:attribute>
		<fo:table-column column-width="5.50cm"/>
		<fo:table-column column-width="12.00cm"/>
	</xsl:template>
	
	<xsl:template name="empty-row">
		<fo:table-row>
  		<fo:table-cell><fo:block></fo:block></fo:table-cell>
  		<fo:table-cell><fo:block></fo:block></fo:table-cell>
		</fo:table-row>
	</xsl:template>
	
	<xsl:template name="format-sql-date">
		<xsl:param name="sql-date"/>
		<xsl:variable name="year" select="substring($sql-date, 1, 4)" />
		<xsl:variable name="monthNumber" select="substring($sql-date, 6, 2)" />
		<xsl:variable name="month">
			<xsl:choose>
				<xsl:when test="$monthNumber = '01' or $monthNumber = '1'">January</xsl:when>
				<xsl:when test="$monthNumber = '02' or $monthNumber = '2'">February</xsl:when>
				<xsl:when test="$monthNumber = '03' or $monthNumber = '3'">March</xsl:when>
				<xsl:when test="$monthNumber = '04' or $monthNumber = '4'">April</xsl:when>
				<xsl:when test="$monthNumber = '05' or $monthNumber = '5'">May</xsl:when>
				<xsl:when test="$monthNumber = '06' or $monthNumber = '6'">June</xsl:when>
				<xsl:when test="$monthNumber = '07' or $monthNumber = '7'">July</xsl:when>
				<xsl:when test="$monthNumber = '08' or $monthNumber = '8'">August</xsl:when>
				<xsl:when test="$monthNumber = '09' or $monthNumber = '9'">September</xsl:when>
				<xsl:when test="$monthNumber = '10'">October</xsl:when>
				<xsl:when test="$monthNumber = '11'">November</xsl:when>
				<xsl:when test="$monthNumber = '12'">December</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="day" select="substring($sql-date, 9, 2)" />
		<xsl:variable name="date_complete" select="concat($day,' ',$month,' ',$year)" />
		<xsl:value-of select="$date_complete"/>
	</xsl:template>
	
	<xsl:template name="format-sql-year">
		<xsl:param name="sql-date"/>
		<xsl:variable name="year" select="substring($sql-date, 1, 4)" />
		<xsl:value-of select="$year"/>
	</xsl:template>
	
	<xsl:template name="format-degree-type">
		<xsl:param name="degree-type"/>
		<xsl:choose>
			<xsl:when test="$degree-type = 'master'"><xsl:value-of select="'Master'"/></xsl:when>
			<xsl:when test="$degree-type = 'bachelor'"><xsl:value-of select="'Bachelor'"/></xsl:when>
			<xsl:when test="$degree-type = 'phd'"><xsl:value-of select="'PhD'"/></xsl:when>
			<xsl:when test="$degree-type = 'drphd'"><xsl:value-of select="'Dr./PhD'"/></xsl:when>
			<xsl:when test="$degree-type = 'md'"><xsl:value-of select="'MD'"/></xsl:when>
			<xsl:when test="$degree-type = 'dr'"><xsl:value-of select="'Dr.'"/></xsl:when>
			<xsl:when test="$degree-type = 'pd'"><xsl:value-of select="'PD'"/></xsl:when>
			<xsl:when test="$degree-type = 'diplom'"><xsl:value-of select="'Diploma'"/></xsl:when>
			<xsl:when test="$degree-type = 'diplommaster'"><xsl:value-of select="'Diploma/Master'"/></xsl:when>
			<xsl:when test="$degree-type = 'prof'"><xsl:value-of select="'Prof.'"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="$degree-type"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="format-gender">
		<xsl:param name="gender"/>
		<xsl:choose>
			<xsl:when test="$gender = 'f'"><xsl:value-of select="'Female'"/></xsl:when>
			<xsl:when test="$gender = 'm'"><xsl:value-of select="'Male'"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="$gender"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="documents-v2">
		<xsl:if test="attachments[@visible='true'] | refereesLetters[@visible='true']">
			<fo:table>
				<xsl:call-template name="columns-def"/>
				<fo:table-body>
					<xsl:call-template name="step-title">
						<xsl:with-param name="title"><xsl:value-of select="'Documents'"/></xsl:with-param>
					</xsl:call-template>
					<xsl:apply-templates select="attachments"/>
					<xsl:apply-templates select="refereesLetters"/>
				</fo:table-body>
			</fo:table>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="attachments[@visible='true'] | refereesLetters[@visible='true']">
		<fo:table-row>
			<fo:table-cell><fo:block font-weight="bold" font-size="11pt"><xsl:value-of select="@title"/><xsl:value-of select="':'"/></fo:block></fo:table-cell>
			<fo:table-cell>
  				<xsl:if test="string-length(@title) &gt; 22">
  					<fo:block font-size="11pt"><fo:inline color="#ffffff"><xsl:value-of select="'___'"/></fo:inline></fo:block>
  				</xsl:if>
  				<xsl:if test="string-length(@title) &gt; 55">
  					<fo:block font-size="11pt"><fo:inline color="#ffffff"><xsl:value-of select="'___'"/></fo:inline></fo:block>
  				</xsl:if>
				<xsl:apply-templates select="document"/>
  			</fo:table-cell>
		</fo:table-row>
	</xsl:template>
	
	<xsl:template match="document">
		<fo:block><xsl:value-of select="@name"/><xsl:if test="@institution != ''"><xsl:value-of select="', '"/><xsl:value-of select="@institution"/></xsl:if></fo:block>
	</xsl:template>
  
</xsl:stylesheet>