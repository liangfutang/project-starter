package com.zjut.spring.boot.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
//@ConfigurationProperties(prefix = "test")
@PropertySource(value = "classpath:properties/datasource.properties", encoding = "UTF-8")
public class DruidWebStatProperties {
    /**
     * Enable WebStatFilter.
     */
    @Value("${druid.web-stat-filter.enabled}")
    private boolean enabled = true;
    @Value("${druid.web-stat-filter.url-pattern}")
    private String urlPattern;
    /**
     * 经常需要排除一些不必要的url，比如*.js,/jslib/*等等。配置在init-param中。比如：
     <init-param>
     <param-name>exclusions</param-name>
     <param-value>*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*</param-value>
     </init-param>

     */
    @Value("${druid.web-stat-filter.exclusions}")
    private String exclusions;
    //缺省sessionStatMaxCount是1000个。你可以按需要进行配置
    @Value("${druid.web-stat-filter.session-stat-max-count}")
    private String sessionStatMaxCount;
    //你可以关闭session统计功能
    @Value("${druid.web-stat-filter.session-stat-enable}")
    private String sessionStatEnable;
    /**
     * 你可以配置principalSessionName，使得druid能够知道当前的session的用户是谁。比如：

     <init-param>
     <param-name>principalSessionName</param-name>
     <param-value>xxx.user</param-value>
     </init-param>

     根据需要，把其中的xxx.user修改为你user信息保存在session中的sessionName。

     注意：如果你session中保存的是非string类型的对象，需要重载toString方法。
     */
    @Value("${druid.web-stat-filter.principal-session-name}")
    private String principalSessionName;
    /**
     * 如果你的user信息保存在cookie中，你可以配置principalCookieName，使得druid知道当前的user是谁

     <init-param>
     <param-name>principalCookieName</param-name>
     <param-value>xxx.user</param-value>
     </init-param>

     根据需要，把其中的xxx.user修改为你user信息保存在cookie中的cookieName
     */
    @Value("${druid.web-stat-filter.principal-cookie-name}")
    private String principalCookieName;
    /**
     * druid 0.2.7版本开始支持profile，配置profileEnable能够监控单个url调用的sql列表。

     <init-param>
     <param-name>profileEnable</param-name>
     <param-value>true</param-value>
     </init-param>

     */
    @Value("${druid.web-stat-filter.profile-enable}")
    private String profileEnable;
}
