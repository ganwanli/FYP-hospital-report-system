package com.hospital.report.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "app.security.ignore-urls")
public class IgnoreUrlsConfig {
    private String[] urls;
    public String[] getUrls() {
        return urls;
    }
    public void setUrls(String[] urls) {
        this.urls = urls;
    }
}
