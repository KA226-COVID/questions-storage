package com.juezlti.repository.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is the configuration that is used to filter (sanitise) HTML that is submitted as part
 * of the event description. At the moment it just allows a very restricted set of elements
 * through.
 */
@Configuration
public class HtmlFilter {

    @Bean
    public PolicyFactory policyFactory() {
        return new HtmlPolicyBuilder()
                .allowElements("i", "b", "u", "strong", "em")
                .allowElements("ul", "ol", "li")
                .allowElements("p", "br")
                .allowElements("pre", "code")
                .allowElements("h1", "h2", "h3", "h4", "h5")
                .allowElements("a")
                .allowUrlProtocols("https", "http", "mailto")
                .allowAttributes("href", "target").onElements("a")
                .toFactory();
    }

}
