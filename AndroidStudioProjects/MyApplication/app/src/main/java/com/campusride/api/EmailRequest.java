package com.campusride.api;

import java.util.Map;

/**
 * EmailRequest model following the recommended EmailJS API structure
 * Matches the exact JSON format expected by EmailJS
 */
public class EmailRequest {
    private String service_id;
    private String template_id;
    private String user_id;
    private Map<String, String> template_params;

    public EmailRequest(String service_id, String template_id, String user_id, Map<String, String> template_params) {
        this.service_id = service_id;
        this.template_id = template_id;
        this.user_id = user_id;
        this.template_params = template_params;
    }

    // Getters and setters (required for Gson serialization)
    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getTemplate_id() {
        return template_id;
    }

    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Map<String, String> getTemplate_params() {
        return template_params;
    }

    public void setTemplate_params(Map<String, String> template_params) {
        this.template_params = template_params;
    }
}
