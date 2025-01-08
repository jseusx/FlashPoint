package com.example.serving_web_content;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InputController {
    //stores username + profile class with Flashcard info and user info
    private Map<String, Profile> profiles = new HashMap<>();
    private String messageOutput = "No File Selected";
    private String currentUser = null;
    private boolean loggedIn = false;

    private String filePath = null;
    private final File file;
    
    // Get directory of file to create/read data file
    public InputController() {
        String curDirectory = System.getProperty("user.dir");
        System.out.println(curDirectory);
        
        filePath = Paths.get(curDirectory, "src", "main", "resources", "static", "FlashPoint_data.txt").toString();
        System.out.println(filePath);
        //create data file if it does not currently exist
        file = new File(filePath);
        try {
            if (file.createNewFile()) {
                System.out.println("File created at: " + filePath);
            } else {
                System.out.println("File already exists at: " + filePath);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file: " + e.getMessage());
        }

        loadProfilesFromFile(filePath);
    }
    
    //will read data file and put them into profiles Map which can be accessed to get each user's info
    private void loadProfilesFromFile(String filePath) {
        Profile currentProfile = null;
        String currentGame = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
    
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Username:")) {
                    String username = line.substring(9).trim();
                    currentProfile = new Profile(username, null);
                    profiles.put(username, currentProfile);
                    System.out.println("Username: " + username);
                } 
                else if (line.startsWith("Password:") && currentProfile != null) {
                    String password = line.substring(9).trim();
                    currentProfile.setPassword(password);
                    System.out.println("Password: " + password);
                } 
                else if (line.startsWith("Flashcard:") && currentProfile != null) {
                    String[] parts = line.substring(10).split(" - ", 3);
                    if (parts.length == 3) {
                        String subject = parts[0].trim();
                        String topic = parts[1].trim();
                        String description = parts[2].trim();
                        currentProfile.addFlashcard(subject, new Flashcard(topic, description));
                        System.out.println("Flashcard: " + subject + " - " + topic + " - " + description);
                    }
                } 
                else if (line.startsWith("Achievements:") && currentProfile != null) {
                    // Start parsing achievements
                    currentGame = null;
                    System.out.println("Achievements:");
                } 
                else if (line.startsWith("    Game:") && currentProfile != null) {
                    currentGame = line.substring(10).trim(); // Extract game name
                    System.out.println("    Game: " + currentGame);
                } 
                else if (line.startsWith("        ") && currentProfile != null && currentGame != null) {
                    String[] parts = line.trim().split(":");
                    if (parts.length == 2) {
                        String subject = parts[0].trim();
                        String badgeRank = parts[1].trim();
    
                        // Parse rank and create Badge object
                        Badge.rank rank;
                        try {
                            rank = Badge.rank.valueOf(badgeRank);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid badge rank: " + badgeRank);
                            continue; // Skip this badge
                        }
    
                        // Add the badge to the profile
                        currentProfile.addBadge(currentGame, subject, new Badge(rank));
                        System.out.println("        " + subject + ": " + badgeRank);
                    }
                } 
                else if (line.equals("---")) {
                    currentProfile = null;
                    currentGame = null;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private void writeProfiles(String filePath) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
        for (String username : profiles.keySet()) {
            Profile profile = profiles.get(username);
            
            // Write username and password
            writer.write("Username: " + username);
            writer.newLine();
            writer.write("Password: " + profile.getPassword());
            writer.newLine();

            
          // Write flashcards
            for (String subject : profile.getFlashcardSubjects()) {
                List<Flashcard> flashcards = profile.getFlashcardsBySubject(subject);
                for (Flashcard flashcard : flashcards) {
                    writer.write("Flashcard: " + subject + " - " + flashcard.getTopic() + " - " + flashcard.getDescription());
                    writer.newLine();
                }
            }

            // Write achievements
            writer.write("Achievements: ");
            writer.newLine();
            for (String game : profile.getAllAchievementsByGame().keySet()) {
                writer.write("    Game: " + game);
                writer.newLine();
                Map<String, Badge> achievements = profile.getAchievementsByGame(game);
                for (Map.Entry<String, Badge> entry : achievements.entrySet()) {
                    writer.write("        " + entry.getKey() + ": " + entry.getValue().getBadgeRank());
                    writer.newLine();
                }
            }

            writer.write("---");
            writer.newLine();
        }
    } catch (IOException e) {
        System.err.println("Error writing to file: " + e.getMessage());
    }
    System.out.println("Written to file");
    }

    public void updateXP(String username, String subject, String game, int xpChange) {
        Profile currentProfile = profiles.get(username);
        if (currentProfile != null) {
            // Get the current XP for the subject and game
            int currentXP = currentProfile.getXp(subject, game);
            currentProfile.setXp(subject, game, currentXP + xpChange);  // Update the XP
        }
    }

    @GetMapping("/")
    public String showForm(Model model) {

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("currentUser", currentUser);

        //check if user is logged in
        if(currentUser != null && loggedIn) {
            Profile currentProfile = profiles.get(currentUser);
            
            if (currentProfile != null) {
                //returns all flashcard subjects under user
                model.addAttribute("subjects", currentProfile.getFlashcardSubjects());
            }
            else {
                model.addAttribute("subjects", null);
            }
        }
        return "index";
    }

    @GetMapping("/create_account")
    public String showCreateAccountPage(Model model) {
        return "create_account";
    }
    
    @PostMapping("/create_account")
    public String createAccount(
        @RequestParam("username") String username, 
        @RequestParam("password") String password, 
        @RequestParam("password_2") String password_2,
        Model model) {
            //regex includes all nonspecial characters
            String regex = "[^a-zA-Z0-9_]";
            //regex_num_ includes numbers and underscores only
            String regex_num_ = "^[0-9_].*"; 
            //checks username to see if it contains any special characters
            if (username.matches(".*" + regex + ".*")) {
                model.addAttribute("error_message", "Username can only contain letters, numbers, and underscores.");
                System.out.println("Username can only contain letters, numbers, and underscores.");
                return "create_account";
            }

            //checks to see if username already exists
            if (profiles.containsKey(username)) {
                model.addAttribute("error_message", "Username already exists.");
                System.out.println("Username already exists.");
                return "create_account";
            }
            //checks username max length <= 14 characters
            if (username.length() > 14) {
                model.addAttribute("error_message", "Username must be less than 12 characters.");
                System.out.println("Username must be less than 12 characters");
                return "create_account";
            }

            //checks username to see if it starts with underscore or numbers
            if (username.matches(regex_num_)) {
                model.addAttribute("error_message", "Username must start with letter.");
                System.out.println("Username must start with letter");
                return "create_account";

            }

            //checks password length > 5
            if (password.length() < 5) {
                model.addAttribute("error_message", "Password must be more than 5 characters.");
                System.out.println("Password must be more than 5 characters.");
                return "create_account";
            }

            //checks if passwords match
            if (!password.matches(password_2)) {
                model.addAttribute("error_message", "Passwords do not match");
                System.out.println("Passwords do not match");
                return "create_account";
            }

            System.out.println("User: " + username + " created.");
            profiles.put(username, new Profile(username, password));

            writeProfiles(filePath);
            return "login";
    }
    

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }
    
    //Checks inputted username and password to find any matches
    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, Model model) {
        Profile profile = profiles.get(username);

        if (profile != null && profile.getPassword().equals(password)) {
            currentUser = username;
            loggedIn = true;

            model.addAttribute("loggedIn", loggedIn);
            model.addAttribute("username", currentUser);
            
            System.out.println("Login succesful for user:" + username);
            return "redirect:/";
        }
        else {
            System.out.println("Login failed for user: " + username);

            model.addAttribute("login_message", messageOutput = "Invalid username or password.");
            return "login";
        }
        
    }
    
    @GetMapping("/add-subject")
    public String showAddSubjectForm(Model model) {
        if (loggedIn) {
            return ("/add-subject");
        }
        else {
            return "redirect:/";
        }
    }

    //adds new subject, if duplicate subject is found it is not created
    @PostMapping("/add-subject")
    public String addSubject(@RequestParam("subject") String subject, Model model) {
        if (!loggedIn)
        {
            return "login";
        }
        
        Profile currentProfile = profiles.get(currentUser);

        currentProfile.addSubject(subject);

        writeProfiles(filePath);
        
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("username", currentUser);
        return "redirect:/";
    }

    @PostMapping("/delete-subject")
    public String deleteSubject(@RequestParam("subject") String subject, Model model) {
        Profile currentProfile = profiles.get(currentUser);

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("username", currentUser);

        if(currentProfile.deleteSubject(subject)) {
            writeProfiles(filePath);
            model.addAttribute("subject_message", "Subject succesfully deleted.");
        }
        else {
            model.addAttribute("subject_message", "Subject not found.");
        }
        return "redirect:/";

    }
    
    
    @GetMapping("/logout")
    public String logout() {
        System.out.println("Logout for user:" + currentUser);

        loggedIn = false;
        currentUser = null;
        return "redirect:/";
    }

    @GetMapping("/result")
    public String showResultPage(@RequestParam("subject") String subject, Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("loggedIn", loggedIn);
        Profile currentProfile = profiles.get(currentUser);
        model.addAttribute("subject", subject);

        if (currentProfile != null) {
            model.addAttribute("flashcards", currentProfile.getFlashcardsBySubject(subject));
            int xp = currentProfile.getXpForSubjectAndGame(subject, "Knowledge Tower"); // Get XP for the game
            String rank = currentProfile.calculateRankForXP(xp);       // Calculate rank
            model.addAttribute("xp", xp);
            model.addAttribute("rank", rank);
        }
        else {
            model.addAttribute("flashcards", null);
        }
        
        return "result";
    }

    @GetMapping("/edit-subject")
    public String showEditSubjectPage(@RequestParam("subject") String subject, Model model) {
        Profile currentProfile = profiles.get(currentUser);
        if (currentProfile != null)
        {
            model.addAttribute("subject", subject);
            return "edit-subject";
        }
        return "login";
    }

    @PostMapping("/edit-subject")
    public String editSubject(@RequestParam("subject") String subject, 
    @RequestParam("newSubject") String newSubject, 
    RedirectAttributes redirectAttributes) {
        Profile currentProfile = profiles.get(currentUser);

        if(currentProfile == null || loggedIn == false) {
            return "login";
        }

        boolean success = currentProfile.editSubject(subject, newSubject);
        redirectAttributes.addFlashAttribute("loggedIn", loggedIn);
        redirectAttributes.addFlashAttribute("currentUser", currentUser);

        //return message 
        if(!success) {
            redirectAttributes.addFlashAttribute("subject_message", "Subject either already exists or does not exist.");
            return "redirect:/";
        }

        writeProfiles(filePath);
        
        redirectAttributes.addFlashAttribute("subject_message", "Subject name changed succesfully");
        return "redirect:/";
    }
    
    

    //going to edit page and getting id and subject of flashcard
    @GetMapping("/edit-flashcard")
    public String showEditPage(@RequestParam("id") Long id, @RequestParam("subject") String subject, Model model) {
        System.out.println(id + " " + subject);
        Profile currentProfile = profiles.get(currentUser);

        if (currentProfile != null) {
            Flashcard flashcard = currentProfile.getFlashcardByIdAndSubject(id, subject);
            model.addAttribute("id", id);
            model.addAttribute("subject", subject);
            model.addAttribute("topic", flashcard.getTopic());
            model.addAttribute("description", flashcard.getDescription());

            return "edit-flashcard";
        }
        else {
            return "login";
        }
    }
    
    @PostMapping("/edit-flashcard")
    public String editFlashcard(
        @RequestParam("id") Long id,
        @RequestParam("subject") String subject,
        @RequestParam("topic") String topic,
        @RequestParam("description") String description,
        RedirectAttributes redirectAttributes) {

        if (currentUser == null) {
            return "redirect:/login";
        }

        System.out.println("Received id: " + id);
        System.out.println("Received subject: " + subject);
        System.out.println("Received topic: " + topic);
        System.out.println("Received description: " + description);

        Profile currentProfile = profiles.get(currentUser);

        if (currentProfile != null) {
            // Attempt to edit the flashcard
            boolean editSuccess = currentProfile.editFlashcardByID(id, subject, topic, description);
            if (editSuccess) {
                writeProfiles(filePath);
                redirectAttributes.addFlashAttribute("edit_message", "Flashcard updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("edit_message", "Failed to update the flashcard.");
            }
        } else {
            redirectAttributes.addFlashAttribute("edit_message", "No flashcard found to edit.");
        }

        // Redirect back to the result page for the subject
        return "redirect:/result?subject=" + subject;
    }

    //deletes a flashcard if it exists with id and subject.
    @PostMapping("/delete-flashcard")
    public String deleteFlashcard(
        @RequestParam("id") Long id,
        @RequestParam("subject") String subject,
        RedirectAttributes redirectAttributes
    ) {
        System.out.println("Received id: " + id);
        System.out.println("Received subject: " + subject);
        
        Profile currentProfile = profiles.get(currentUser);
        if (currentProfile != null) {
            boolean flashcardDeleted = currentProfile.deleteFlashcardById(id, subject);

            redirectAttributes.addFlashAttribute("subject", subject);
            redirectAttributes.addFlashAttribute("flashcards", currentProfile.getFlashcardsBySubject(subject));

            if (flashcardDeleted) {
                writeProfiles(filePath);
                redirectAttributes.addFlashAttribute("edit_message", "Flashcard deleted successfully.");
            }
            else {
                redirectAttributes.addFlashAttribute("edit_message", "Flashcard was not found.");
            }
        }
        else {
            redirectAttributes.addFlashAttribute("edit_message", "No flashcard to delete");
        }

        return "redirect:/result?subject=" + subject;
        }

    @GetMapping("/add-flashcard")
    public String showAddFlashcardPage(@RequestParam("subject") String subject, Model model) {
        if (loggedIn) {
            model.addAttribute("subject", subject);
            return "add-flashcard";
        }
        return "login";
    }
    
    @PostMapping("/add-flashcard")
    public String addFlashcard(
        @RequestParam("subject") String subject, 
        @RequestParam("topic") String topic, 
        @RequestParam("description") String description,
        RedirectAttributes redirectAttributes) {
        Profile currentProfile = profiles.get(currentUser);

        Flashcard flashcard = new Flashcard(topic, description);

        try {
            currentProfile.addFlashcard(subject, flashcard);
            writeProfiles(filePath);
            redirectAttributes.addFlashAttribute("edit_message", "Flashcard added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect back to the subject's flashcard page or stay on the add page
        return "redirect:/result?subject=" + subject;
    }

    @GetMapping("/knowledge-tower")
    public String getKnowledgeTower(@RequestParam("subject") String subject, Model model) {
        if (!loggedIn) {
            return "redirect:/login";
        }

        Profile currentProfile = profiles.get(currentUser);

        if (currentProfile == null || !currentProfile.getFlashcardSubjects().contains(subject)) {
            return "redirect:/";
        }

        List<Flashcard> flashcards = currentProfile.getFlashcardsBySubject(subject);

        // Get XP and rank for the current subject and game
        int xp = currentProfile.getXpForSubjectAndGame(subject, "Knowledge Tower"); // Assuming "Knowledge Tower" as the game
        Badge.rank rank = currentProfile.getRankForSubjectAndGame(subject, "Knowledge Tower");

        System.out.println("Subject: " + subject);
        System.out.println("Flashcards: " + flashcards);
    
        // Map flashcard ID to Flashcard object
        Map<Long, Flashcard> flashcardMap = new HashMap<>();
        for (Flashcard flashcard : flashcards) {
            flashcardMap.put(flashcard.getId(), flashcard);
        }
    
        // Convert the map to a list of objects to pass to the view (or serialize as JSON)
        List<Map<String, String>> flashcardList = new ArrayList<>();
        for (Map.Entry<Long, Flashcard> entry : flashcardMap.entrySet()) {
            Map<String, String> flashcardData = new HashMap<>();
            flashcardData.put("id", entry.getKey().toString());
            flashcardData.put("topic", entry.getValue().getTopic());
            flashcardData.put("description", entry.getValue().getDescription());
            flashcardList.add(flashcardData);
        }
    
        model.addAttribute("subject", subject);
        model.addAttribute("flashcards", flashcardList);
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("xp", xp);
        model.addAttribute("rank", rank);
    
        return "knowledge-tower";

        /*List<Flashcard> flashcards = currentProfile.getFlashcardsBySubject(subject);

        System.out.println("Subject: " + subject);
        System.out.println("Flashcards: " + flashcards);
        
        model.addAttribute("subject", subject);
        model.addAttribute("flashcards", flashcards);
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("currentUser", currentUser);

        return "knowledge-tower";*/
    }
    
    
    @GetMapping("/memory-match")
    public String getMemoryMatchPage(@RequestParam("subject") String subject, Model model) {
        if (!loggedIn) {
            return "redirect:/login";
        }
        
        Profile currentProfile = profiles.get(currentUser);

        if (currentProfile == null || !currentProfile.getFlashcardSubjects().contains(subject)) {
            return "redirect:/"; // Redirect if subject is invalid
        }

        // Get terms (topics and descriptions) for the selected subject
        List<Flashcard> flashcards = currentProfile.getFlashcardsBySubject(subject);
        Map<Long, Flashcard> terms = new HashMap<>();
        for (Flashcard flashcard : flashcards) {
            terms.put(flashcard.getId(), flashcard);
        }

        // Convert terms map to a list of objects (or simply serialize to JSON)
        List<Map<String, String>> termsList = new ArrayList<>();
        for (Map.Entry<Long, Flashcard> entry : terms.entrySet()) {
            Map<String, String> termData = new HashMap<>();
            termData.put("id", entry.getKey().toString());
            termData.put("topic", entry.getValue().getTopic());
            termData.put("description", entry.getValue().getDescription());
            termsList.add(termData);
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("subject", subject);
        model.addAttribute("terms", termsList);

        return "memory-match";
    }

    @PostMapping("/update-xp")
    @ResponseBody
    public Map<String, Object> updateXp(@RequestBody Map<String, Object> payload) {
        String subject = (String) payload.get("subject");
        String game = (String) payload.get("game");
        int xpChange = (int) payload.get("xp");
        String username = currentUser;  // Assuming you have access to the current logged-in user
        
        updateXP(username, subject, game, xpChange); // Update XP on server
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        return response;
    }

}