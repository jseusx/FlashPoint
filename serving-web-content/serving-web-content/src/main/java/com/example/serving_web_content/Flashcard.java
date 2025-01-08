package com.example.serving_web_content;

import java.util.Map;
import java.util.HashMap;

public class Flashcard {
    private static long nextId = 1; // Static variable to auto-increment ID
    private Long id;
    private String topic;
    private String description;

	public Flashcard(String topic, String description)
	{
        this.id = nextId++;
		this.topic = topic;
		this.description = description;
	}

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
