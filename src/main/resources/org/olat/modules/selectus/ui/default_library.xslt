<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  
	<xsl:template name="personal-additional-attributes">
		<xsl:param name="title"/>
		<xsl:call-template name="custom-additional-attributes">
			<xsl:with-param name="title"><xsl:value-of select="$title"/></xsl:with-param>
			<xsl:with-param name="tab"><xsl:value-of select="'personalData'"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="academical-additional-attributes">
		<xsl:param name="title"/>
		<xsl:call-template name="custom-additional-attributes">
			<xsl:with-param name="title"><xsl:value-of select="$title"/></xsl:with-param>
			<xsl:with-param name="tab"><xsl:value-of select="'academicalBackground'"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="project-additional-attributes">
		<xsl:param name="title"/>
		<xsl:call-template name="custom-additional-attributes">
			<xsl:with-param name="title"><xsl:value-of select="''"/></xsl:with-param>
			<xsl:with-param name="tab"><xsl:value-of select="'project'"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="custom-step-additional-attributes">
		<xsl:if test="application/additionalAttributes/step[@tab = 'custom1' or @tab = 'custom2' or @tab = 'custom3' or @tab = 'custom4']/additionalAttribute">
			<fo:table>
				<xsl:call-template name="columns-def"/>
				<fo:table-body>
					<xsl:apply-templates select="application/additionalAttributes/step[@tab = 'custom1' or @tab = 'custom2' or @tab = 'custom3' or @tab = 'custom4']/additionalAttribute" />
					<xsl:call-template name="empty-row" />
				</fo:table-body>
			</fo:table>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="custom-additional-attributes">
		<xsl:param name="tab"/>
		<xsl:param name="title"/>
		<xsl:if test="application/additionalAttributes/step[@tab = $tab]/additionalAttribute">
			<fo:table>
				<xsl:call-template name="columns-def"/>
				<fo:table-body>
					<xsl:if test="$title">
						<fo:table-row>
	  						<fo:table-cell number-columns-spanned="2"><fo:block
	  							font-weight="bold" font-size="14pt" padding-top="0.5cm" padding-bottom="0.2cm"><xsl:value-of select="$title"/></fo:block></fo:table-cell>
						</fo:table-row>
					</xsl:if>
					<xsl:apply-templates select="application/additionalAttributes/step[@tab = $tab]/additionalAttribute" />
					<xsl:call-template name="empty-row" />
				</fo:table-body>
			</fo:table>
		</xsl:if>
	</xsl:template>

	<xsl:template match="additionalAttribute">
		<fo:table-row>
  			<fo:table-cell><fo:block font-weight="bold"><xsl:value-of select="@label"/></fo:block></fo:table-cell>
  			<fo:table-cell>
  				<xsl:choose>
  					<xsl:when test="count(multiline) &gt; 0">
  						<xsl:for-each select="multiline"><fo:block><xsl:value-of select="."/></fo:block></xsl:for-each>
  					</xsl:when>
  					<xsl:otherwise><fo:block><xsl:value-of select="."/></fo:block></xsl:otherwise>
  				</xsl:choose>
  			</fo:table-cell>
		</fo:table-row>
	</xsl:template>
	
	<xsl:template match="additionalAttribute[@type='heading']">
		<fo:table-row>
  			<fo:table-cell number-columns-spanned="2"><fo:block
  				font-weight="bold" font-size="14pt" padding-top="0.5cm" padding-bottom="0.2cm"><xsl:value-of select="@label"/></fo:block></fo:table-cell>
		</fo:table-row>
	</xsl:template>
	
	<xsl:template name="academical-background-high">
		<xsl:param name="label"/>
		<xsl:call-template name="academical-background-with-date">
			<xsl:with-param name="label"><xsl:value-of select="$label"/></xsl:with-param>
			<xsl:with-param name="background-type"><xsl:value-of select="application/academicalBackground/highestDegreeType"/></xsl:with-param>
			<xsl:with-param name="date"><xsl:value-of select="application/academicalBackground/highestDegreeDate"/></xsl:with-param>
			<xsl:with-param name="institution"><xsl:value-of select="application/academicalBackground/highestDegreeInstitution"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="academical-background-dissertation">
		<xsl:param name="label"/>
		<xsl:call-template name="academical-background-with-date">
			<xsl:with-param name="label"><xsl:value-of select="$label"/></xsl:with-param>
			<xsl:with-param name="background-type"><xsl:value-of select="application/academicalBackground/dissertationTitle"/></xsl:with-param>
			<xsl:with-param name="date"><xsl:value-of select="application/academicalBackground/dissertationDate"/></xsl:with-param>
			<xsl:with-param name="institution"><xsl:value-of select="application/academicalBackground/dissertationInstitution"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="academical-background-habilitation">
		<xsl:param name="label"/>
		<xsl:call-template name="academical-background-with-date">
			<xsl:with-param name="label"><xsl:value-of select="$label"/></xsl:with-param>
			<xsl:with-param name="background-type"><xsl:value-of select="application/academicalBackground/habilitationTitle"/></xsl:with-param>
			<xsl:with-param name="date"><xsl:value-of select="application/academicalBackground/habilitationDate"/></xsl:with-param>
			<xsl:with-param name="institution"><xsl:value-of select="application/academicalBackground/habilitationInstitution"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="academical-background-with-date">
		<xsl:param name="label"/>
		<xsl:param name="background-type"/>
		<xsl:param name="date"/>
		<xsl:param name="institution"/>
		<xsl:if test="$background-type != '' or $institution != ''">
			<fo:table>
				<xsl:call-template name="columns-def"/>
				<fo:table-body>
					<xsl:call-template name="applicant-row">
						<xsl:with-param name="label"><xsl:value-of select="$label"/>:</xsl:with-param>
						<xsl:with-param name="value"><xsl:call-template name="format-degree-type"><xsl:with-param name="degree-type">
							<xsl:value-of select="$background-type"/>
							</xsl:with-param></xsl:call-template>, <xsl:call-template name="format-sql-year">
								<xsl:with-param name="sql-date"><xsl:value-of select="$date"/></xsl:with-param>
							</xsl:call-template> at 
							<xsl:value-of select="$institution"/></xsl:with-param>
					</xsl:call-template>
					<xsl:call-template name="empty-row" />
				</fo:table-body>
			</fo:table>
		</xsl:if>
	</xsl:template>
	 
	<xsl:template name="applicant-row">
		<xsl:param name="label"/>
		<xsl:param name="value"/>
		<fo:table-row>
  		<fo:table-cell><fo:block font-weight="bold"><xsl:value-of select="$label"/></fo:block></fo:table-cell>
  		<fo:table-cell><fo:block><xsl:value-of select="$value"/></fo:block></fo:table-cell>
		</fo:table-row>
	</xsl:template>
	
	<xsl:template name="columns-def">
		<xsl:attribute name="table-layout">fixed</xsl:attribute>
		<xsl:attribute name="width">100%</xsl:attribute>
		<xsl:attribute name="border-collapse">collapse</xsl:attribute>
		<xsl:attribute name="margin-top">0.5cm</xsl:attribute>
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
	
	<xsl:template name="attachments">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:apply-templates select="documents"/>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="documents">
		<fo:table-row>
			<fo:table-cell><fo:block font-weight="bold">Documents:</fo:block></fo:table-cell>
			<fo:table-cell>
				<xsl:apply-templates select="document"/>
  				<fo:block></fo:block>
  			</fo:table-cell>
		</fo:table-row>
	</xsl:template>
	
	<xsl:template match="document">
		<fo:block><xsl:value-of select="@name"/></fo:block>
	</xsl:template>
  
</xsl:stylesheet>