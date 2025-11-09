package com.ripple.post_service.model;

public class Post {
    private String id;
    private String userId;
    private String content;

    public Post() {}

    public Post(String id, String userId, String content) {
        this.id = id;
        this.userId = userId;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
