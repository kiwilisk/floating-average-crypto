package org.kiwi.rest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class UnirestClient implements RestClient {

    @Override
    public String getGetResponseFrom(String endpoint) {
        try {
            HttpResponse<String> response = Unirest.get(endpoint).asString();
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to receive response from endpoint" + endpoint, e);
        }
    }
}
