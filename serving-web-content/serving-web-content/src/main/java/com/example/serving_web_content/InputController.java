package com.example.serving_web_content;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;






@Controller
public class InputController {
    //stores username + profile class with Flashcard info and user info
    private Map<String, Profile> profiles = new HashMap<>();
    private String messageOutput = "No File Selected";
    private String currentUser = null;
    private boolean loggedIn = false;

    private final String filePath = null;
    private final File file;
    
    // Get directory of file to create/read data file
    public InputController() {
        String curDirectory = System.getProperty("user.dir");
        System.out.println(curDirectory);
        
        String filePath = Paths.get(curDirectory, "src", "main", "resources", "static", "FlashPoint_data.txt").toString();
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

                        System.out.println(subject + " - " + topic + " - " + description);
                    }
                }
                else if (line.equals("---")) {
                    currentProfile = null;
                    System.out.println("--- ");
                }
            }
        }
        catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    @GetMapping("/")
    public String showForm(Model model) {

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("message", messageOutput);

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
        
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("username", currentUser);
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
        }
        else {
            model.addAttribute("flashcards", null);
        }
        
        return "result";
    }

    //going to edit page and getting id and subject of flashcard
    @GetMapping("/edit")
    public String showEditPage(@RequestParam("id") Long id, @RequestParam("subject") String subject, Model model) {
        System.out.println(id + " " + subject);
        Profile currentProfile = profiles.get(currentUser);

        if (currentProfile != null) {
            Flashcard flashcard = currentProfile.getFlashcardByIdAndSubject(id, subject);
            model.addAttribute("id", id);
            model.addAttribute("subject", subject);
            model.addAttribute("topic", flashcard.getTopic());
            model.addAttribute("description", flashcard.getDescription());

            return "edit";
        }
        else {
            return "login";
        }
    }
    
    @PostMapping("/edit")
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
            redirectAttributes.addFlashAttribute("edit_message", "Flashcard added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect back to the subject's flashcard page or stay on the add page
        return "redirect:/result?subject=" + subject;
    }

}
    /*
    // submit current hashmap to front end
    @PostMapping("/submit")
    public String submitTopics(Model model) {
        if (filePath == null)
        {
            
            model.addAttribute("message", messageOutput = "No file to update.");
            return "redirect:/";
        }

        // Add the map to the model to display the topics and descriptions
        model.addAttribute("topics", topicMap.getTopicMap());

        try {
            FileWriter fileWrite = new FileWriter(uploadedFilePath, false);
            for (Map.Entry<String,String> entry: topicMap.getTopicMap().entrySet()) {
                fileWrite.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }

            fileWrite.close();
            System.out.println("Updated file with new info");
            model.addAttribute("message", messageOutput = "Updated File");
        }
        catch(IOException e)
        {
            model.addAttribute("message", messageOutput = "Error updating file " + e.getMessage());
        }

        return "result";

    }*/


    /* 
    @PostMapping("/upload")
    public String uploadTopics(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", messageOutput = "File not selected");
            return "index";
        }

        try {
            /* 
             * Will go to directory in C:/ drive to save the .txt file. If the text files exists,
             * it will store its path allowing it to get edited when clicking submit button later on.
             * If not, it will create a folder and then save its path.
            
            File directory = new File("C:/FlashPoint_FlashCards");
            if (!directory.exists())
            {
                directory.mkdirs();
            }

            uploadedFilePath = directory.getPath() + "/" + file.getOriginalFilename();

            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;

            // Iterates through file and cleans up data. Puts it into Hashmap as well
            while((line = reader.readLine()) != null) {
                
                String parts[] = line.split(":", 2);
                
                if(parts.length == 2) {
                    String topic = parts[0].trim();
                    String description = parts[1].trim();
                    
                    topicMap.addTopic(topic, description);
                }
                else {
                    System.out.println("Skipped line due to missing topic/description");
                }
            }

            model.addAttribute("message", messageOutput = "File read");
        }
        catch (IOException e) {
            model.addAttribute("message", messageOutput = "Error reading file" + e.getMessage());

        }

        return "index";
    }*/
    