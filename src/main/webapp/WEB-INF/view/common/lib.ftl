<#macro menuinternal items>
    <#list items as item>
        <#if item.page>
        <li><span><a turl="${item.url}">${item.name}</a></span></li>
        <#elseif item.leaf>
        <li><span>${item.name}</span></li>
        <#else>
        <li><span>${item.name}</span>
            <ul>
                <@menuinternal item.child />
            </ul>
        </li>
        </#if>
    </#list>
</#macro>

<#macro menu code>
    <@menuinternal resource(code, "mp") />
</#macro>

<#macro toolbar code>
toolbar: [<#list resource(code, "b") as item>{text:'${item.name}',iconCls:'${item.icon!""}',handler:${item.action!""}}<#if item_has_next>,</#if></#list>]
</#macro>
