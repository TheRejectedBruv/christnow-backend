package com.christnow.devotionals.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDto {
    private int created;
    private int skipped;
    private int updated;
    private List<String> messages = new ArrayList<>();

    public int getCreated() { return created; }
    public void setCreated(int created) { this.created = created; }

    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }

    public int getUpdated() { return updated; }
    public void setUpdated(int updated) { this.updated = updated; }

    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }

    public void addMessage(String message) {
        this.messages.add(message);
    }
}
