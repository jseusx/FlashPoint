# FlashPoint
A free alternative for viewing and creating flashcards.<br>
This was a collaborative project. I was in charge of handling implementations of code handed in from the group and editing to make use of Springboot. <br>
## Tech/Framework used:

This project was built using the following technologies:

- Spring Boot
- Java
- Thymeleaf
- HTML
- CSS

# How to setup:
Using vscode, download the folder titled "serving-web-content." This will be the folder you will open in vscode, it will look similar to this once opened up: <br>
![Image of sidebar from VSCODE](/page_images/vscode_serving.png) <br>
From here, you will want to download all the springboot extensions, specifically the **Spring Boot Extension Pack**. You will also need Gradle or any other build system you prefer to run with Spring. <br>
Once that is all setup, you may start the project from the "Spring Boot Dashboard." On startup, you may navigate to: **http://localhost:8080**

# What can you do?
## Login
Initially, you will be greeted with a blank page saying for you to login. By clicking the login button, you will then be shown a login page: <br>
![Login page](/page_images/Login_Page.png) <br>

> [!Note]
> If you wish to use the admin account, instead of creating an account you may skip this section.
> The username and pass for the admin account is as follows:
> Username: admin
> Password: pass

## Creating an account
By clicking the create account button, you will be taken to this page: <br>
![Create account page](/page_images/Create_Account_Page.png) <br>
There are certain criterias needed when creating a password/username, if they are not met or interfere with current accounts, you will have a warning pop up. <br>
Once the account is created, you will not have any subjects or flashcards to view and will need to create them manually.

## Subjects
Once you are logged in you will be presented with a list of flashcard subjects. If you do not have any, you can create some using the "Add Subject" button. <br>
![Viewing subjects page](/page_images/Main_Page.png) <br>

### Adding, Editing, and Deleting Subjects
Whenever you are looking at a subject, there are two buttons, <br>
- **-** button will delete a subject with a warning beforehand. <br>
- **✎** button will allow you to edit the subject.
To **add** a subject, you can click "Add Subject" the page will then update and show:
![Edit subject page](/page_images/Edit_Subject_Page.png) <br>
Here, the inside the text box will show what is currently the subject. You can either edit it or leave it how it is then click "Submit."

## Flashcards
To view the flashcards under a subject, just click the subject card and a list of flashcards will be shown if any have been created. <br>
![flashcards page](/page_images/flashcards_page)
