package com.christnow.devotionals.models;
import java.util.List;

public class PaymentRequest {
	 private List<String> courseNames;
	    private long amount;
	    private String successUrl;
	    private String cancelUrl;

	    // Getters and Setters
	    public List<String> getCourseNames() {
	        return courseNames;
	    }
	    public void setCourseNames(List<String> courseNames) {
	        this.courseNames = courseNames;
	    }
	    public long getAmount() {
	        return amount;
	    }
	    public void setAmount(long amount) {
	        this.amount = amount;
	    }
	    public String getSuccessUrl() {
	        return successUrl;
	    }
	    public void setSuccessUrl(String successUrl) {
	        this.successUrl = successUrl;
	    }
	    public String getCancelUrl() {
	        return cancelUrl;
	    }
	    public void setCancelUrl(String cancelUrl) {
	        this.cancelUrl = cancelUrl;
	    }
}
