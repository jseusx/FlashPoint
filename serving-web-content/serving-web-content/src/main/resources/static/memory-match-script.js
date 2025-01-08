//let terms = {};
let termKeys = [];
let xp = 0; // ADDED AND Changed
let currentTerm = '';
let startTime, timerInterval, bestTime = null, timeLimit = null;
let isFirstRound = true; // Track if it's the first round

document.getElementById('start-button').addEventListener('click', startGame);
document.getElementById('term-input').addEventListener('keypress', handleInput);

function loadTerms() {
    termKeys = Object.keys(terms);
    return Promise.resolve(); // Mimic asynchronous behavior for consistency
}

async function startGame() {
    await loadTerms(); // Ensure terms are fully loaded before starting

    currentTerm = '';
    startTime = Date.now();

    // Only apply timeLimit if it's not the first round and a bestTime exists
    if (!isFirstRound && bestTime !== null) {
        timeLimit = parseInt(bestTime);
    } else {
        timeLimit = null; // No time limit on the first round
    }

    document.getElementById('definition-text').innerText = "Get Ready...";
    setTimeout(nextDefinition, 1000); // Delay before showing first definition

    // Start the timer display
    timerInterval = setInterval(updateTimer, 100);
    document.getElementById('term-input').value = '';
    document.getElementById('term-input').focus();
}

function nextDefinition() {
    if (termKeys.length === 0) return endGame();

    const randomIndex = Math.floor(Math.random() * termKeys.length);
    currentTerm = termKeys[randomIndex];

    //document.getElementById('definition-text').innerText = terms[currentTerm];
    document.getElementById('definition-text').innerText = `${terms[currentTerm].description}`;
}

function handleInput(event) {
    if (event.key === 'Enter') {
        const input = document.getElementById('term-input').value.trim().toLowerCase();
        const expectedTopic = terms[currentTerm].topic.toLowerCase();

        if (input === expectedTopic) {
            // Correct Guess
            termKeys = termKeys.filter(term => term !== currentTerm);
            if (termKeys.length > 0) {
                nextDefinition();
                document.getElementById('term-input').value = '';
            } else {
                endGame();
            }
        } else {
            // Wrong answer: deduct points
            xp = Math.max(0, xp - 10); // Deduct 50 XP, but ensure it doesn't drop below 0
            updateXpDisplay(); // Update the XP display
            document.getElementById('definition-text').innerText = 'Incorrect! Try again.';

            gameOver();
        }
    }
}

function updateTimer() {
    const elapsedTime = Math.floor((Date.now() - startTime) / 1000);
    document.getElementById('timer').innerText = `Time: ${elapsedTime}s`;

    // Enforce time limit only if bestTime (timeLimit) is set after the first round
    if (timeLimit && elapsedTime > timeLimit) {
        gameOver();
    }
}

function updateXpDisplay() {
    document.getElementById("xp").innerText = `XP: ${xp}`;
}

function endGame() {
    clearInterval(timerInterval);
    const timeTaken = Math.floor((Date.now() - startTime) / 1000);
    document.getElementById('timer').innerText = `Time: ${timeTaken}s`;

    // Get the current subject
    const subject = document.getElementById('definition-text').dataset.subject;

    // Update best time for the current subject
    const storedBestTime = localStorage.getItem(`bestTime-${subject}`);
    if (storedBestTime === null || timeTaken < parseInt(storedBestTime)) {
        localStorage.setItem(`bestTime-${subject}`, timeTaken);
        bestTime = timeTaken;

        // Award XP for beating the high score
        xp += 50; // Add 50 XP ADDED AND CHANGED
        console.log("xp after correct answer:", xp);
        updateXpDisplay(); // Update XP display ADDED AND CHANGED

        document.getElementById('best-time').innerText = `Best Time: ${timeTaken}s`;
    } else {
        document.getElementById('best-time').innerText = `Best Time: ${bestTime}s`;
    }

    // After the first round, we set isFirstRound to false
    isFirstRound = false;

    document.getElementById('definition-text').innerText = 'Congratulations! You finished!';
}

document.getElementById('reset-button').addEventListener('click', resetBestTime);

function resetBestTime() {
    const subject = document.getElementById('definition-text').dataset.subject;
    // Clear the best time from localStorage
    localStorage.removeItem(`bestTime-${subject}`);
    bestTime = null; // Reset the bestTime variable for the current session

    document.getElementById('best-time').innerText = 'Best Time: Not set';
}

function gameOver() {
    clearInterval(timerInterval);
    document.getElementById('definition-text').innerText = 'Game Over! Try Again.';
    document.getElementById('timer').innerText = 'Time: 0s';
}

document.getElementById('end-game-button').addEventListener('click', endGameManually);

function endGameManually() {
    // Stop the timer
    clearInterval(timerInterval);

    // Show the end message
    document.getElementById('definition-text').innerText = 'Game ended by user!';
    document.getElementById('timer').innerText = 'Time: 0s';

    // Optionally reset the term input field
    document.getElementById('term-input').value = '';

    // Reset the game state
    currentTerm = '';
    termKeys = Object.keys(terms);
}

document.addEventListener('DOMContentLoaded', () => {
    // Load best time from localStorage, but reset if it's invalid
    const subject = document.getElementById('definition-text').dataset.subject;
    const storedBestTime = localStorage.getItem(`bestTime-${subject}`);
    if (storedBestTime && parseInt(storedBestTime) > 1) {
        bestTime = parseInt(storedBestTime);
        document.getElementById('best-time').innerText = `Best Time: ${bestTime}s`;
    } else {
        // Clear invalid data or show "Not set" if no valid best time
        //localStorage.removeItem('bestTime');
        document.getElementById('best-time').innerText = 'Best Time: Not set';
        bestTime = null; // Ensure bestTime is not set to an invalid number
    }
});
