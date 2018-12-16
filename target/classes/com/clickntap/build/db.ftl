[#assign sql]
[#assign entities = this.projectElement.element("entities")]
[#if entities??]
[#assign prefix = entities.attributeValue("prefix")]
[#list entities.elements("g") as g]
[#list g.elements("entity") as entity]
create table ${prefix}_${entity.attributeValue("name")?lower_case} (
[#assign fields]
[#list entity.elements("field") as field]
${field.attributeValue("name")} ${field.attributeValue("type")}
[#if (field.attributeValue("size")!"") != ""] (${field.attributeValue("size")})[/#if]
[#if (field.attributeValue("unsigned")!"") == "yes"] unsigned[/#if]
[#if (field.attributeValue("primary")!"") == "yes"] primary key auto_increment[/#if]
[#if (field.attributeValue("references")!"") != ""][/#if]
[#if field_has_next],[/#if]
[/#list]
[#list entity.elements("field") as field][#if (field.attributeValue("references")!"") != ""],foreign key (${field.attributeValue("name")}) references ${prefix}_${field.attributeValue("references")?split('.')[1]?lower_case}(id)
[/#if][/#list]
[/#assign]
	${fields?replace('\n', '')?replace(',', ',\n\t')}
);

[/#list]
[/#list]
[/#if]
[/#assign]
${this.save(sql,"etc/db-mysql.sql")!}

[#assign sql]
[#assign entities = this.projectElement.element("entities")]
[#if entities??]
[#assign prefix = entities.attributeValue("prefix")]
[#list entities.elements("g") as g]
[#list g.elements("entity") as entity]
create table ${prefix}_${entity.attributeValue("name")?lower_case} (
[#assign fields]
[#list entity.elements("field") as field]
${field.attributeValue("name")} ${field.attributeValue("type")?replace("varchar","varchar2")?replace("int","number")?replace("datetime","date")?replace("double","binary_double")}
[#if (field.attributeValue("size")!"") != ""] (${field.attributeValue("size")})[/#if]
[#if (field.attributeValue("primary")!"") == "yes"] primary key[/#if]
[#if (field.attributeValue("references")!"") != ""][/#if]
[#if field_has_next],[/#if]
[/#list]
[#list entity.elements("field") as field][#if (field.attributeValue("references")!"") != ""],foreign key (${field.attributeValue("name")}) references ${prefix}_${field.attributeValue("references")?split('.')[1]?lower_case}(id)
[/#if][/#list]
[/#assign]
	${fields?replace('\n', '')?replace(',', ',\n\t')}
);

create sequence ${prefix}_${entity.attributeValue("name")?lower_case}_seq start with 1 increment by 1 nocache nocycle;

[/#list]
[/#list]
[/#if]
[/#assign]
${this.save(sql,"etc/db-oracle.sql")!}
