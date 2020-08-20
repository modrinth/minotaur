package com.diluv.diluvgradle.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseError {
    
    @Expose
    @SerializedName("type")
    private String type;
    
    @Expose
    @SerializedName("error")
    private String error;
    
    @Expose
    @SerializedName("message")
    private String message;

    public String getType () {
        
        return type;
    }

    public String getError () {
        
        return error;
    }

    public String getMessage () {
        
        return message;
    }
}