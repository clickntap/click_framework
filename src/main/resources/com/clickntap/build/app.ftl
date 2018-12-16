[#assign entities = this.projectElement.element("entities")]
[#if entities??]

[#assign prefix = entities.attributeValue("prefix")]
[#list entities.elements("g") as g]
[#assign package = g.attributeValue("name")]
[#assign xml]
<?xml version="1.0" encoding="UTF-8"?>
<smart-controller>
[#list g.elements("entity") as entity]
	[#assign name = entity.attributeValue("name")?lower_case]
	[#assign className = g.attributeValue("name")+"."+entity.attributeValue("name")]

	<method name="add-${name}">
		<bind name="${name}" channel="app" class="${this.projectPackage}.bo.${className}" validation-group="create" scope="session"> [#noparse]${[/#noparse]${name}.clear()} </bind>
		<exec> [#noparse]${[/#noparse]${name}.create()} [#noparse]${ws.setForm(this.param("form"))}[/#noparse] </exec>
	</method>
	<method name="edit-${name}">
		<bind name="${name}" channel="app" class="${this.projectPackage}.bo.${className}" validation-group="update" scope="session"> [#noparse]${[/#noparse]${name}.read()} </bind>
		<exec> [#noparse]${[/#noparse]${name}.update()} [#noparse]${ws.setForm(this.param("form"))}[/#noparse] </exec>
	</method>
	[#list entity.elements("field") as field][#if field.attributeValue("name") == "password"]
	<method name="edit-${name}-password">
		<bind name="${name}" channel="app" class="${this.projectPackage}.bo.${className}" validation-group="execute-password" scope="session"> [#noparse]${[/#noparse]${name}.read()} </bind>
		<exec> [#noparse]${[/#noparse]${name}.execute("password")} [#noparse]${ws.setForm(this.param("form"))}[/#noparse] </exec>
	</method>
	[/#if][/#list]
	<method name="delete-${name}">
		<bind name="${name}" channel="app" class="${this.projectPackage}.bo.${className}" scope="session"> [#noparse]${[/#noparse]${name}.read()} </bind>
		<exec> [#noparse]${[/#noparse]${name}.delete()} [#noparse]${ws.setForm(this.param("form"))}[/#noparse] </exec>
	</method>

[/#list]
</smart-controller>
[/#assign]
${this.save(xml,"src/main/webapp/WEB-INF/smart-app/app-"+package+".xml")!}
[/#list]


[/#if]
	