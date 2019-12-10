[#macro select json count]
select
[#if count]
count(*) as "count"
[#else]
[#if json.has("key")]${json.key}[#else]id[/#if] as "id"
[#if json.has("fields")]
[#list this.target.list(json.fields) as field]
[#if !(field?contains("."))]
,${field} as "${this.target.toCamelCase(field)}"
[/#if]
[/#list]
[/#if]
[#if json.has("selectFields")]
[#list this.target.list(json.selectFields) as field]
,(${field.sql}) as "${field.name}"
[/#list]
[/#if]
[/#if]
from ${json.table}
[#assign where = true]
[#if json.has("filters")]
  [#list this.target.list(json.filters) as filter]
    [#if filter.operator == "match"]
      [#assign query = filter.value?lower_case]
    	[#list query?split("[ .;:,]", "r") as word]
  	   	[#if word?length > 3]
          [#if where]where[#else]and[/#if]
          [#assign where = false]
  		    (
  		  		 MATCH (
  		  		 ${filter.name}
  		  		 ) AGAINST ( ${this.toString("+"+word)} IN BOOLEAN MODE)
  		    )
  	    [/#if]
      [/#list]
    [#elseif filter.operator == "text"]
        [#assign query = filter.value?lower_case]
        [#list query?split("[ .;:,]", "r") as word]
          [#if where]where[#else]and[/#if]
          [#assign where = false]
          (
            [#list filter.name?split("[ .;:,]", "r") as field]
         	  [#if field_index !=0]or [/#if]lower(${field}) like ${this.toString("%"+word+"%")}
            [/#list]
          )
        [/#list]
    [#elseif filter.operator == "like"]
      [#if where]where[#else]and[/#if]
      [#assign where = false]
      ${filter.name} like ${this.toString("%"+filter.value?lower_case+"%")}
    [#else]
      [#if where]where[#else]and[/#if]
      [#assign where = false]
      [#if filter.name?contains(".")]
      ${filter.name} ${filter.operator} [#if filter.value?lower_case == "null"]null[#else]${filter.value?lower_case}[/#if]
      [#else]
      ${filter.name} ${filter.operator} [#if filter.value?lower_case == "null"]null[#else]${this.toString(filter.value?lower_case)}[/#if]
      [/#if]
    [/#if]
  [/#list]
[/#if]
[#if json.has("intersect")]
  [#list this.target.list(json.intersect) as intersect]
    [#if where]where[#else]and[/#if]
    [#assign where = false]
    ${intersect.intersectKey} in (
      [#if intersect.has("query")]
      	${intersect.query}
      [#else]
      [@select intersect false/]
      [/#if]
     )
  [/#list]
[/#if]
[/#macro]
[#compress]
[#assign json = this.target.getJson()]

[@select json this.target.count/]

[#if !(this.target.count)]

[#if json.has("sort")]
  order by
  [#list this.target.list(json.sort) as sort]
    [#if sort_index !=0],[/#if] ${sort.name} ${sort.type}
  [/#list]
[/#if]

[#if json.has("limit")]
limit
[#if json.has("from")]
${json.from},
[/#if]
${json.limit}
[/#if]
[/#if]
[/#compress]
