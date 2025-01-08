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

   // Achievements organized by game and subject
   //<Game, Map<Subject, Badge>>
   private Map<String, Map<String, Badge>> achievementsByGame;

     // Store XP and rank for each subject and game
     private Map<String, Map<String, Integer>> xpBySubjectAndGame = new HashMap<>(); // Map<Subject, Map<Game, XP>>
     private Map<String, Map<String, Badge.rank>> rankBySubjectAndGame = new HashMap<>(); // Map<Subject, Map<Game, Rank>>
 
    public Profile(String username, String password) {
        this.username = username;
        this.password = password;
        this.flashcardsBySubject = new HashMap<>();
        this.random = new Random();
        this.achievementsByGame = new HashMap<>();
    }

        // Get XP for a specific subject and game
    public int getXp(String subject, String game) {
        return xpBySubjectAndGame
            .getOrDefault(subject, new HashMap<>())
            .getOrDefault(game, 0);  // Default to 0 XP if not found
    }

    // Set XP for a specific subject and game
    public void setXp(String subject, String game, int xp) {
        xpBySubjectAndGame
            .computeIfAbsent(subject, k -> new HashMap<>())  // Create map for the subject if it doesn't exist
            .put(game, xp);  // Set XP for the game within the subject
    }
    // Add XP for a specific subject and game
    public void addXp(String subject, String game, int points) {
        Map<String, Integer> gameXp = xpBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>());
        int currentXp = gameXp.getOrDefault(game, 0);
        currentXp += points;
        gameXp.put(game, currentXp);

        // After adding XP, check for rank-up
        rankUp(subject, game);
    }

    // Check and update the rank based on XP for a specific subject and game
    public void rankUp(String subject, String game) {
        int xp = xpBySubjectAndGame.getOrDefault(subject, new HashMap<>()).getOrDefault(game, 0);

        // Determine the rank based on XP
        Badge.rank currentRank = rankBySubjectAndGame.getOrDefault(subject, new HashMap<>()).getOrDefault(game, Badge.rank.INITIATE);

        if (xp >= 500 && currentRank == Badge.rank.INITIATE) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.BRONZE1);
        } else if (xp >= 1000 && currentRank == Badge.rank.BRONZE1) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.BRONZE2);
        } else if (xp >= 1500 && currentRank == Badge.rank.BRONZE2) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.BRONZE3);
        } else if (xp >= 2000 && currentRank == Badge.rank.BRONZE3) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.SILVER1);
        } else if (xp >= 2500 && currentRank == Badge.rank.SILVER1) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.SILVER2);
        } else if (xp >= 3000 && currentRank == Badge.rank.SILVER2) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.SILVER3);
        } else if (xp >= 3500 && currentRank == Badge.rank.SILVER3) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.GOLD1);
        } else if (xp >= 4000 && currentRank == Badge.rank.GOLD1) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.GOLD2);
        } else if (xp >= 4500 && currentRank == Badge.rank.GOLD2) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.GOLD3);
        } else if (xp >= 5000 && currentRank == Badge.rank.GOLD3) {
            rankBySubjectAndGame.computeIfAbsent(subject, k -> new HashMap<>()).put(game, Badge.rank.MASTER);
        }
    }

    public String calculateRankForXP(int xp) {
        if (xp < 500) {
            return "INITIATE";
        } else if (xp < 1000) {
            return "BRONZE1";
        } else if (xp < 1500) {
            return "BRONZE2";
        } else if (xp < 2000) {
            return "BRONZE3";
        } else if (xp < 2500) {
            return "SILVER1";
        } else if (xp < 3000) {
            return "SILVER2";
        } else if (xp < 3500) {
            return "SILVER3";
        } else if (xp < 4000) {
            return "GOLD1";
        } else if (xp < 4500) {
            return "GOLD2";
        } else if (xp < 5000) {
            return "GOLD3";
        } else {
            return "MASTER";
        }
    }
    

    // Get the rank for a specific subject and game
    public Badge.rank getRankForSubjectAndGame(String subject, String game) {
        return rankBySubjectAndGame.getOrDefault(subject, new HashMap<>()).getOrDefault(game, Badge.rank.INITIATE);
    }

    // Get the XP for a specific subject and game
    public int getXpForSubjectAndGame(String subject, String game) {
        return xpBySubjectAndGame.getOrDefault(subject, new HashMap<>()).getOrDefault(game, 0);
    }

    // Add a badge to a specific subject
    public void addBadge(String game, String subject, Badge badge) {
        achievementsByGame
            .computeIfAbsent(game, k -> new HashMap<>()) // Create a map for the game if it doesn't exist
            .put(subject, badge); // Add or update the badge for the subject
    }

    // Get all badges for a specific game
    public Map<String, Badge> getAchievementsByGame(String game) {
        return achievementsByGame.getOrDefault(game, new HashMap<>());
    }

    // Get the badge for a specific subject in a specific game
    public Badge getBadge(String game, String subject) {
        Map<String, Badge> subjectAchievements = achievementsByGame.get(game);
        if (subjectAchievements != null) {
            return subjectAchievements.get(subject);
        }
        return null; // Return null if no badge exists for the subject
    }

    // Get all achievements grouped by game
    public Map<String, Map<String, Badge>> getAllAchievementsByGame() {
        return achievementsByGame;
    }

    // Remove a badge from a specific subject
    public boolean removeBadge(String game, String subject, Badge badge) {
        // Get the achievements for the specified game
        Map<String, Badge> gameAchievements = achievementsByGame.get(game);
        if (gameAchievements != null) {
            // Get the badge for the specified subject
            Badge subjectBadge = gameAchievements.get(subject);
            if (subjectBadge != null && subjectBadge.equals(badge)) {
                // Remove the badge if it matches
                gameAchievements.remove(subject);
                return true;
            }
        }
        return false; // Badge not found
    }

    // Rank up a badge in a specific subject
    public boolean rankUpBadge(String game, String subject, Badge.rank rank) {
        // Get the achievements for the specified game
        Map<String, Badge> gameAchievements = achievementsByGame.get(game);
        if (gameAchievements != null) {
            // Get the badge for the specified subject
            Badge subjectBadge = gameAchievements.get(subject);
            if (subjectBadge != null && subjectBadge.getBadgeRank() == rank) {
                // Rank up the badge if it matches the specified rank
                subjectBadge.rankUp();
                return true;
            }
        }
        return false; // Badge not found or rank mismatch
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

    public Boolean editSubject(String oldSubject, String newSubject) {
        //check if oldSubject exists
        if(!getFlashcardSubjects().contains(oldSubject)) {
            return false;
        }

        //check if newSubject name already exists
        if(getFlashcardSubjects().contains(newSubject)) {
            return false;
        }

        //transfer flashcards associated with subject to new subject
        List<Flashcard> flashcards = flashcardsBySubject.remove(oldSubject);
        flashcardsBySubject.put(newSubject, flashcards);

        // Now handle achievements (badges) under each game for the old subject
        for (String game : achievementsByGame.keySet()) {
            Map<String, Badge> gameAchievements = achievementsByGame.get(game);
            if (gameAchievements.containsKey(oldSubject)) {
                // Get the badge for the old subject
                Badge badge = gameAchievements.remove(oldSubject);
                // Put the badge under the new subject
                gameAchievements.put(newSubject, badge);
            }
    }
        return true;
    }

    public Boolean deleteSubject(String subject) {
        if(flashcardsBySubject.keySet().contains(subject)) {
            flashcardsBySubject.remove(subject);
            return true;
        }
        return false;
    }
}
