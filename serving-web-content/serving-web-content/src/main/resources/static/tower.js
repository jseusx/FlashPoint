let flashcards = new Map();
let height = 0;
let mistakes = 0;
let xp = currentXP;
let rank = currentRank;
let currentFlashcard;
let flashcardList = [];
let serverFlashcards = [];

function loadFlashcards() {
    const subject = document.getElementById("gameContainer").dataset.subject;
    serverFlashcards = serverFlashcardsKey;
    console.log("Raw serverFlashcards:", serverFlashcards);

    if (!Array.isArray(serverFlashcards) || serverFlashcards.length === 0) {
        console.error(`No flashcards available for the subject: ${subject}`);
        document.getElementById("definition").innerText = `No flashcards available for ${subject}.`;
        return;
    }
    
    // Populate flashcards map and list
    flashcards.clear(); // Clear any existing flashcards
    serverFlashcards.forEach(flashcard => {
        // Use topic + id as a unique identifier for flashcards
        const key = `${flashcard.topic}-${flashcard.id}`;
        flashcards.set(key, flashcard.description);
    });

    flashcardList = Array.from(flashcards.entries());
    shuffleFlashcards();

    console.log(`Loaded ${flashcardList.length} flashcards for subject: ${subject}`);
}

// Shuffle flashcards
function shuffleFlashcards() {
    flashcardList.sort(() => Math.random() - 0.5);
}

// Display a new definition
function newRound() {
    if (flashcardList.length === 0) {
        // If all flashcards have been used, shuffle and reset the list
        flashcardList = Array.from(flashcards.entries());
        shuffleFlashcards();
    }

    currentFlashcard = flashcardList.pop();

    if (!currentFlashcard || currentFlashcard.length < 2) {
        console.error("Invalid flashcard:", currentFlashcard);
        document.getElementById("definition").innerText = "Invalid flashcard. Please restart the game.";
        return;
    }

    const key = currentFlashcard[0];

    const topic = key.split("-")[0]; // The topic name (e.g., 'Biology')
    //console.log("topic:" + topic);
    document.getElementById("definition").innerText = `Definition: ${currentFlashcard[1]}`;

    currentFlashcard = topic;
    //console.log("currentFlashcard: " + currentFlashcard);
    
}

// Save highest tower height for the current subject
function saveHighestTowerHeight() {
    const subject = document.getElementById("gameContainer").dataset.subject;
    const storedHeight = parseInt(localStorage.getItem(`highestTower-${subject}`)) || 0;

    if (height > storedHeight) {
        localStorage.setItem(`highestTower-${subject}`, height);
    }
}

// Load highest tower height for the current subject
function loadHighestTowerHeight() {
    const subject = document.getElementById("gameContainer").dataset.subject;
    return parseInt(localStorage.getItem(`highestTower-${subject}`)) || 0;
}

// Function to add a block visually
function addBlock() {
    const block = document.createElement("div");
    block.className = "block";

    // Calculate the number of blocks already in the container
    const existingBlocks = document.querySelectorAll("#blocksContainer .block").length;

    // Set the position based on the number of existing blocks
    block.style.bottom = `${existingBlocks * 50}px`; // Each block is 50px tall

    document.getElementById("blocksContainer").appendChild(block);
}

function updateXpDisplay() {
    document.getElementById("xp").innerText = `XP: ${xp}`;
    const subject = document.getElementById("gameContainer").dataset.subject;
    const game = "Knowledge Tower"; // Or dynamically get this value for the current game
    sendXpToServer(subject, game, xp); // Send to server when XP changes
}

// Check the user's answer
function submitAnswer() {
    const userAnswer = document.getElementById("userAnswer").value.trim().toLowerCase();
    const subject = document.getElementById("gameContainer").dataset.subject;
    const game = "Knowledge Tower"; // Or dynamically get this value for the current game


    if (userAnswer === "leave") {
        endGame();
        return;
    }

    if (!currentFlashcard) {
        console.error("No current flashcard available.");
        document.getElementById("message").innerText = "No active flashcard. Please try again.";
        return;
    }

    const correctAnswer = currentFlashcard.toLowerCase();
    console.log("correct answer:" + correctAnswer);

    if (userAnswer === correctAnswer) {
        height++;
        xp+= 50;
        console.log("XP after correct answer:", xp); // Check XP here

        saveXPToLocalStorage(subject, xp);

        document.getElementById("message").innerText = "Correct!";
        document.getElementById("towerHeight").innerText = `Tower height: ${height} ${height === 1 ? "block" : "blocks"}.`;
        
        // Add a block visually
        addBlock();
    } else {
        mistakes++;
        xp = Math.max(0, xp - 50); // Deduct XP but ensure it doesn't go below zero
        console.log("XP after incorrect answer:", xp); // Check XP here

        saveXPToLocalStorage(subject, xp);

        document.getElementById("message").innerText = `Incorrect! The correct answer was "${currentFlashcard}". Mistakes: ${mistakes}/3`;

        // Shake the blocks if mistakes are less than 3
        if (mistakes < 3) {
            const blocks = document.querySelectorAll("#blocksContainer .block");
            blocks.forEach(block => {
                block.classList.add("shake");
                setTimeout(() => block.classList.remove("shake"), 500); // Remove the class after the animation
            });
        }
    }

    updateXpDisplay(); // Update XP display on the screen
    updateRankDisplay(subject, game); // Update Rank display for this subject and game

    document.getElementById("userAnswer").value = "";

    if (mistakes >= 3) {
        endGame();
    } else {
        newRound();
    }
}

// Update rank display
function updateRankDisplay(subject, game) {
    let rank = calculateRankForXP(xp);  // Example method to calculate rank based on XP
    document.getElementById("rankDisplay").innerText = `Rank: ${rank}`;
}

// Save XP to localStorage
function saveXPToLocalStorage(subject, xp) {
    localStorage.setItem(`xp-${subject}`, xp); // Store XP for this subject
}

// Calculate rank based on XP
function calculateRankForXP(xp) {
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

// End the game
function endGame() {
    document.getElementById("message").innerText = `Game over! The tower reached ${height} ${height === 1 ? "block" : "blocks"} before collapsing.`;
    document.getElementById("definition").innerText = "";
    document.getElementById("userAnswer").disabled = true;
    document.getElementById("submitAnswerButton").disabled = true; // Disable the button

    saveHighestTowerHeight();

    // Simulate block collapse
    const blocks = document.querySelectorAll("#blocksContainer .block");
    blocks.forEach(block => {
        const randomX = Math.random() * 80; // Random position along the width
        const randomRotation = Math.random() * 360; // Random rotation

        block.style.transition = "all 1s ease"; // Smooth transition for collapse
        block.style.bottom = "0px"; // Move block to the ground
        block.style.left = `${randomX}%`; // Scatter blocks randomly along the width
        block.style.transform = `rotate(${randomRotation}deg)`; // Rotate randomly
    });
    
    const restartButton = document.createElement("button");
    restartButton.id = "restartButton";
    restartButton.innerText = "Restart Game";
    restartButton.onclick = resetGame;

    // Add the restart button to the user input container
    const userInputContainer = document.getElementById("userInputContainer");
    userInputContainer.appendChild(restartButton);
}

// Reset the game
function resetGame() {
    height = 0;
    mistakes = 0;
    document.getElementById("towerHeight").innerText = "Tower height: 0 blocks";
    document.getElementById("blocksContainer").innerHTML = ""; // Clear the tower blocks
    document.getElementById("message").innerText = "";
    document.getElementById("definition").innerText = "Definition will appear here.";
    document.getElementById("userAnswer").disabled = false;
    document.getElementById("submitAnswerButton").disabled = false;
    document.getElementById("userAnswer").value = "";

    // Remove the restart button
    const restartButton = document.getElementById("restartButton");
    if (restartButton) restartButton.remove();

    // Reload flashcards and start a new round
    loadFlashcards();
    newRound();
}

function sendXpToServer(subject, game, xp) {
    fetch('/update-xp', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            subject: subject,
            game: game,
            xp: xp
        })
    })
    .then(response => response.json())
    .then(data => {
        console.log('XP updated on server:', data);
    })
    .catch(error => {
        console.error('Error updating XP on server:', error);
    });
}


// Initialize game when the window loads
window.onload = async function() {
    try {
        await loadFlashcards();
        if (flashcardList.length > 0) {
            newRound();
        } else {
            document.getElementById("definition").innerText = "No flashcards available for this subject.";
        }

        const subject = document.getElementById("gameContainer").dataset.subject;
        const highestTower = loadHighestTowerHeight();
        document.getElementById("message").innerText = `Highest tower for ${subject}: ${highestTower} blocks.`;

        // Add event listener to the submit button
        document.getElementById("submitAnswerButton").addEventListener("click", submitAnswer);
    } catch (error) {
        console.error("Error initializing the game:", error);
        document.getElementById("definition").innerText = "Failed to initialize the game. Please try again.";
    }
};