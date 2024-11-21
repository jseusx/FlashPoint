package com.example.serving_web_content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Profile {
    private String username;
    private String password;

    private Map<String, List<Flashcard>> flashcardsBySubject;
    private Random random;

    public Profile(String username, String password) {
        this.username = username;
        this.password = password;
        this.flashcardsBySubject = new HashMap<>();
        this.random = new Random();
    }

    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /* 
    Will check for subject in Map.
    If it exists it will check to see if there exists a list of flashcards associated with the subject. 
    If there exists, add flashcard to list.
    If it does not exist, creat a new ArrayList associated with Subject as key and add flashcard to set.
    */
    public void addFlashcard(String subject, Flashcard flashcard) {
        List<Flashcard> flashcards = flashcardsBySubject.computeIfAbsent(subject, k -> new ArrayList<>());
    
        // Check if a flashcard with the same topic and description already exists
        boolean duplicateExists = flashcards.stream()
            .anyMatch(existingFlashcard -> 
                existingFlashcard.getTopic().equals(flashcard.getTopic()) && 
                existingFlashcard.getDescription().equals(flashcard.getDescription())
            );
    
        if (!duplicateExists) {
            flashcards.add(flashcard);
        } else {
            throw new IllegalArgumentException("A flashcard with the same topic and description already exists.");
        }
    }
    

    public Set<String> getFlashcardSubjects() {
        return flashcardsBySubject.keySet();
    }

    // Gets flashcards based on subject
    public List<Flashcard> getFlashcardsBySubject(String subject) {
        return flashcardsBySubject.getOrDefault(subject, new ArrayList<>());
    }

    public Flashcard getRandomFlashcard(String subject) {
        List<Flashcard> flashcards = getFlashcardsBySubject(subject);

        if (flashcards.isEmpty()) {
            return null;
        }

        // get random flashcard
        int randomIndex = random.nextInt(flashcards.size());
        return flashcards.get(randomIndex);
    }

    public Flashcard getFlashcardByIdAndSubject(Long id, String subject) {
        List<Flashcard> flashcards = getFlashcardsBySubject(subject);

        for(Flashcard flashcard : flashcards) {
            if(flashcard.getId().equals(id)) {
                return flashcard;
            }
        }
        
        return null;
    }

    //deletes flashcard based on subject and id.
    public boolean deleteFlashcardById(Long id, String subject) {
       List<Flashcard> flashcards = getFlashcardsBySubject(subject);
       // Iterate through the list and try to find the flashcard by its ID
        for (Flashcard flashcard : flashcards) {
            if (flashcard.getId().equals(id)) {
                // If found, remove the flashcard from the list
                flashcards.remove(flashcard);
                return true;  // Successfully deleted
            }
        }

        // If the flashcard with the given ID wasn't found, return false
        return false;
    }

    public boolean editFlashcardByID(Long id, String subject, String topic, String description) {
        Flashcard flashcard = getFlashcardByIdAndSubject(id, subject);

        if(flashcard != null) {
            flashcard.setTopic(topic);
            flashcard.setDescription(description);
            return true;
        }

        return false;
    }

    public void addSubject(String subject) {
        flashcardsBySubject.computeIfAbsent(subject, k -> new ArrayList<>());
    }
}
