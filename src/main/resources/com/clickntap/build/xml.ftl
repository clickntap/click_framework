[#assign entities = this.projectElement.element("entities")]
[#if entities??]

[#assign prefix = entities.attributeValue("prefix")]
[#list entities.elements("g") as g]
[#assign package = g.attributeValue("name")]


[#assign xml]
<bean>
  [#list g.elements("entity") as entity]
  <read-list name="${entity.attributeValue("name")?lower_case}s"><![CDATA[
    [#noparse]
    [#assign query = (this.bean.query!"")?lower_case]
    [/#noparse]
    select
      id as "id"
    from ${prefix}_${entity.attributeValue("name")?lower_case}
    [#noparse]
    [#list query?split("[ .;:,]", "r") as word]
    [#if word_index == 0]where[#else]and[/#if]
    (
    [/#noparse]
    [#assign n = 0]
    [#list entity.elements("field") as field]
        [#if field.attributeValue("search")! == "yes" && (field.attributeValue("type") == "varchar" || field.attributeValue("type")?contains("text"))]
          [#if n != 0] or[/#if] lower(${field.attributeValue("name")}) like [#noparse]${[/#noparse]this.toString("%"+word+"%")}
        [#assign n = n + 1]
        [/#if]
    [/#list]
    [#if n == 0]id != 0[/#if]
    [#noparse]
    )
    [/#list]
    [/#noparse]
  ]]></read-list>
  [/#list]
  [#assign range]
  [#list g.elements("entity") as entity]
  [#assign n = 0]
  [#list entity.elements("field") as field]
  [#if field.attributeValue("name") == "start_date"] [#assign n = n + 1] [/#if]
  [#if field.attributeValue("name") == "end_date"] [#assign n = n + 1] [/#if]
  [/#list]
  [#if n == 2]
  <read-list name="range-${entity.attributeValue("name")?lower_case}s"><![CDATA[
    select
      id as "id"
    from ${prefix}_${entity.attributeValue("name")?lower_case}
    where start_date < [#noparse]${[/#noparse]this.endDate}
    and (end_date > [#noparse]${[/#noparse]this.startDate} or (end_date is null and start_date > [#noparse]${[/#noparse]this.startDate}))
  ]]></read-list>
  [/#if]
  [/#list]
  [/#assign]
  ${range?trim}
</bean>
[/#assign]
${this.save(xml,"src/main/resources/"+this.projectPackage.replace(".","/")+"/bo/"+package+"/"+package?capitalize+"Selector.xml")!}


[#list g.elements("entity") as entity]
[#assign xml]
<bean[#if entity.attributeValue("cache")?? && entity.attributeValue("cache") == "no"][#else] cache="${g.attributeValue("name")?lower_case}_${entity.attributeValue("name")?lower_case}"[/#if]>
    <validation>
      <group name="create,update"><![CDATA[
      	[#assign code]
        [#list entity.elements("field") as field]
        [#if (field.attributeValue("mandatory")!"") == "yes" && field.attributeValue("name") != "id" && !field.attributeValue("name")?contains("password")]
        [#if field.attributeValue("name")?ends_with("date")]
       	[#noparse]${this.assertNotEmpty([/#noparse]"${this.name(field.attributeValue("name")+"_only_date")}")}
        [#noparse]${this.assertNotEmpty([/#noparse]"${this.name(field.attributeValue("name")+"_only_time")}")}
        [/#if]
        [#noparse]${this.assertNotEmpty([/#noparse]"${this.name(field.attributeValue("name"))}")}
        [/#if]
        [#if (field.attributeValue("unique")!"") == "yes" && field.attributeValue("name") != "id"]
        [#noparse]${this.assertUnique([/#noparse]"${this.name(field.attributeValue("name"))}")}
        [/#if]
        [/#list]
        [#list entity.elements("field") as field]
        [#if field.attributeValue("name") == "email"]
        [#noparse]
        [#if ((this.target.email)!"") != ""]
        ${this.assertEmail("email")}
        [/#if]
        [/#noparse]
        [/#if]
        [#if field.attributeValue("name") == "password"]
        [#noparse]${this.assertUnique("username")}[/#noparse]
        [/#if]
        [/#list]
        [#assign n = 0][#list entity.elements("field") as field][#if field.attributeValue("name") == "start_date"][#assign n = n + 1][/#if][#if field.attributeValue("name") == "end_date"][#assign n = n + 1][/#if][/#list]
		[#if n == 2][#noparse]${this.assertGt("endDate","startDate")}[/#noparse][/#if]
		[/#assign]
		${code?trim}
        ]]></group>
      <group name="create"><![CDATA[
       	[#list entity.elements("field") as field]
       	[#if field.attributeValue("name")?contains("password") && (field.attributeValue("mandatory")!"") == "yes"]
  		  [#assign passwordName = this.getter(field.attributeValue("name"))?replace("get","")]
        [#noparse]${[/#noparse]this.assertNotEmpty("confirm${passwordName}")}
        [#noparse]${[/#noparse]this.assertEquals("confirm${passwordName}","${this.name(field.attributeValue("name"))}")}
        [#noparse]${[/#noparse]this.assertLength("${this.name(field.attributeValue("name"))}",4,16)}
        [/#if]
        [/#list]
    	[#assign code]
        [#list entity.elements("field") as field]
        [#if (field.attributeValue("mandatory")!"") == "yes" && field.attributeValue("name") == "id"]
        [#noparse]${this.assertNotEmpty([/#noparse]"${this.name(field.attributeValue("name"))}")}
        [/#if]
        [#if (field.attributeValue("unique")!"") == "yes" && field.attributeValue("name") == "id"]
        [#noparse]${this.assertUnique([/#noparse]"${this.name(field.attributeValue("name"))}")}
        [/#if]
        [/#list]
  		[/#assign]
		${code?trim}
      ]]></group>
      [#list entity.elements("field") as field]
      [#if field.attributeValue("name")?contains("password")]
  	  [#assign passwordName = this.getter(field.attributeValue("name"))?replace("get","")]
      <group name="execute-${field.attributeValue("name")?replace("_","")}"><![CDATA[
		[#noparse]${[/#noparse]this.assertExists("old${passwordName}")}
		[#noparse]${[/#noparse]this.assertNotEmpty("old${passwordName}")}
		[#noparse]${[/#noparse]this.assertNotEmpty("new${passwordName}")}
		[#noparse]${[/#noparse]this.assertEquals("confirmNew${passwordName}","new${passwordName}")}
		[#noparse]${[/#noparse]this.assertLength("new${passwordName}",4,16)}
  	  ]]></group>
      [/#if]
      [/#list]
    </validation>
    <read name="id"><![CDATA[
        select
        [#assign fields]
        [#list entity.elements("field") as field]
        [#if !(field.attributeValue("name")?contains("password"))]
	      ${field.attributeValue("name")} as "${this.name(field.attributeValue("name"))}",
        [/#if]
        [/#list]
        [/#assign]
          ${fields?trim?keep_before_last(',')}
        from
          ${prefix}_${entity.attributeValue("name")?lower_case}
        where
          id = [#noparse]${this.id}[/#noparse]
    ]]></read>
    [#list entities.elements("g") as og]
    [#assign opackage = og.attributeValue("name")]
    [#list og.elements("entity") as oentity]
    [#list oentity.elements("field") as ofield]
    [#if ofield.attributeValue("references")??]
    [#if ofield.attributeValue("references") == package+"."+entity.attributeValue("name")]
    [#if opackage+"."+oentity.attributeValue("name") == package+"."+entity.attributeValue("name")]
    <read-list name="children"><![CDATA[
      select id as "id" from ${prefix}_${oentity.attributeValue("name")?lower_case} where ${ofield.attributeValue("name")} = [#noparse]${this.id}[/#noparse]
    ]]></read-list>
    [#else]
    [#if ofield.attributeValue("method-name")??]
    <read-list name="${ofield.attributeValue("method-name")?lower_case}"><![CDATA[
      select id as "id" from ${prefix}_${oentity.attributeValue("name")?lower_case} where ${ofield.attributeValue("name")} = [#noparse]${this.id}[/#noparse]
    ]]></read-list>
    [#else]
    <read-list name="${oentity.attributeValue("name")?lower_case}s"><![CDATA[
      select id as "id" from ${prefix}_${oentity.attributeValue("name")?lower_case} where ${ofield.attributeValue("name")} = [#noparse]${this.id}[/#noparse]
    ]]></read-list>
    [/#if]
    [#if ofield.attributeValue("limited")??]
    <read-list name="${oentity.attributeValue("name")?lower_case}s-limited"><![CDATA[
      [#if this.projectElement.attributeValue("database")?? && this.projectElement.attributeValue("database") =="oracle"]
        select id as "id" from (
          select id from ${prefix}_${oentity.attributeValue("name")?lower_case} where ${ofield.attributeValue("name")} = [#noparse]${this.id}[/#noparse] order by ${ofield.attributeValue("limited")?lower_case} ${ofield.attributeValue("limited-sort")}
        ) where rownum <= ${ofield.attributeValue("limited-value")}
      [#else]
        select id as "id" from ${prefix}_${oentity.attributeValue("name")?lower_case} where ${ofield.attributeValue("name")} = [#noparse]${this.id}[/#noparse] order by ${ofield.attributeValue("limited")?lower_case} ${ofield.attributeValue("limited-sort")} limit ${ofield.attributeValue("limited-value")}
      [/#if]
    ]]></read-list>
    [/#if]
    [/#if]
    [/#if]
    [/#if]
    [/#list]
    [/#list]
    [/#list]
    [#list entity.elements("field") as field]
    [#if (field.attributeValue("unique")!"") == "yes"]
    <read-list name="${this.name(field.attributeValue("name"))}"><![CDATA[
        select id as "id" from ${prefix}_${entity.attributeValue("name")?lower_case} where ${field.attributeValue("name")} = [#noparse]${[/#noparse]this.${this.name(field.attributeValue("name"))}}
        [#if field.attributeValue("name") != "id"]
        and [#noparse](id != ${this.id} or ${this.id} is null)[/#noparse]
        [/#if]
    ]]></read-list>
    [/#if]
    [#if field.attributeValue("name") == "password"]
    <read name="auth"><![CDATA[
    		select id as "id" from ${prefix}_${entity.attributeValue("name")?lower_case} [#noparse]where (lower(username) = lower(${this.email}) or lower(email) = lower(${this.email})) and password = ${this.passwordMD5} limit 1[/#noparse]
    ]]></read>
    <read-list name="username"><![CDATA[
    		select id as "id" from ${prefix}_${entity.attributeValue("name")?lower_case} [#noparse]where username = ${this.username} and (id != ${this.id} or ${this.id} is null)[/#noparse]
    ]]></read-list>
 	[/#if]
    [#if field.attributeValue("name")?contains("password")]
  	  [#assign passwordName = this.getter(field.attributeValue("name"))?replace("get","")]
    <read-list name="old${passwordName}"><![CDATA[
    		select id as "id" from ${prefix}_${entity.attributeValue("name")?lower_case} where ${field.attributeValue("name")} = [#noparse]${[/#noparse]this.old${passwordName}MD5} and id = [#noparse]${this.id}[/#noparse]
    ]]></read-list>
     <execute name="${field.attributeValue("name")?replace("_","")},forgot-${field.attributeValue("name")?replace("_","")}"><![CDATA[
      update ${prefix}_${entity.attributeValue("name")?lower_case} set ${field.attributeValue("name")} = [#noparse]${[/#noparse]this.new${passwordName}MD5}, [#noparse]last_modified = ${this.now()} where id = ${this.id}[/#noparse]
    ]]></execute>
     [/#if]
    [/#list]

    [#list entity.elements("field") as field][#if (field.attributeValue("search")!"") == "yes"]<read name="${field.attributeValue("name")}"><![CDATA[
      select
        id as "id"
      from
        ${prefix}_${entity.attributeValue("name")?lower_case}
      where
        lower(${field.attributeValue("name")}) = lower([#noparse]${this.[/#noparse]${this.name(field.attributeValue("name"))}})
    ]]></read>[/#if][/#list]
    <update><![CDATA[
        update ${prefix}_${entity.attributeValue("name")?lower_case}
        set
        [#assign fields]
        [#list entity.elements("field") as field][#if field.attributeValue("name") != "id" && field.attributeValue("name") != "creation_date" && !field.attributeValue("name")?contains("password")]
          ${field.attributeValue("name")} = [#noparse]${this.[/#noparse]${this.name(field.attributeValue("name"))?replace("lastModified", "now()")?replace("creationDate", "now()")}},
  		[#elseif field.attributeValue("name")?contains("password")]
  		  [#noparse][#if this.bean.[/#noparse]${this.name(field.attributeValue("name"))}??]
 	        ${field.attributeValue("name")} = [#noparse]${this.[/#noparse]${this.name(field.attributeValue("name"))+"MD5"}},
		  [#noparse][/#if][/#noparse]
        [/#if][/#list]
        [/#assign]
          ${fields?trim?keep_before_last(',')}
        where
          id = [#noparse]${this.id}[/#noparse]
    ]]></update>
    <execute name="touch"><![CDATA[
        update ${prefix}_${entity.attributeValue("name")?lower_case}
        set
        [#list entity.elements("field") as field][#if  field.attributeValue("name") == "last_modified"]
          ${field.attributeValue("name")} = [#noparse]${this.[/#noparse]${this.name(field.attributeValue("name"))?replace("lastModified", "now()")?replace("creationDate", "now()")}}
        [/#if][/#list]
        where
          id = [#noparse]${this.id}[/#noparse]
    ]]></execute>
    <create><![CDATA[
        insert into ${prefix}_${entity.attributeValue("name")?lower_case} (
        [#if this.projectElement.attributeValue("database")?? && this.projectElement.attributeValue("database") =="oracle"]
          id,
        [#else]
          [#noparse][#if this.bean.id??]id,[/#if][/#noparse]
        [/#if]
        [#list entity.elements("field") as field][#if field.attributeValue("name") != "id"]
          ${field.attributeValue("name")}[#if field_has_next],[/#if]
        [/#if][/#list]
        ) values (
        [#if this.projectElement.attributeValue("database")?? && this.projectElement.attributeValue("database") =="oracle"]
          ${prefix}_${entity.attributeValue("name")?lower_case}_seq.nextval,
        [#else]
          [#noparse][#if this.bean.id??]${this.id},[/#if][/#noparse]
        [/#if]
        [#list entity.elements("field") as field][#if field.attributeValue("name") != "id"]
          [#if field.attributeValue("default")??]
          [#noparse]coalesce(${this.[/#noparse]${this.name(field.attributeValue("name"))?replace("lastModified", "now()")?replace("creationDate", "now()")}}[#if field_has_next],${field.attributeValue("default")}),[/#if]
          [#else]
          [#if field.attributeValue("name")?contains("password")]
          [#noparse]${this.[/#noparse]${this.name(field.attributeValue("name"))+"MD5"}}[#if field_has_next],[/#if]
          [#elseif field.attributeValue("name") == "creation_date"]
          [#noparse][#if this.bean.creationDate??]${this.creationDate}[#else]${this.now()}[/#if][/#noparse][#if field_has_next],[/#if]
          [#elseif field.attributeValue("name") == "last_modified"]
          [#noparse][#if this.bean.lastModified??]${this.lastModified}[#else]${this.now()}[/#if][/#noparse][#if field_has_next],[/#if]
          [#else]
          [#noparse]${this.[/#noparse]${this.name(field.attributeValue("name"))}}[#if field_has_next],[/#if]
          [/#if]
          [/#if]
        [/#if][/#list]
        )
    ]]></create>
    <delete><![CDATA[
      delete from
        ${prefix}_${entity.attributeValue("name")?lower_case}
      where
        id = [#noparse]${this.id}[/#noparse]
    ]]></delete>
    <curr-val><![CDATA[
      [#if this.projectElement.attributeValue("database")?? && this.projectElement.attributeValue("database") =="oracle"]
        select ${prefix}_${entity.attributeValue("name")?lower_case}_seq.currval as "id" from dual
      [#else]
        select LAST_INSERT_ID() as id
      [/#if]
    ]]></curr-val>
</bean>
[/#assign]
[#assign xml = xml?replace("{this.md5}","{this.get('md5')}")]
${this.save(xml,"src/main/resources/"+this.projectPackage.replace(".","/")+"/bo/"+package+"/"+entity.attributeValue("name")+".xml")!}

[/#list]
[/#list]


[/#if]
