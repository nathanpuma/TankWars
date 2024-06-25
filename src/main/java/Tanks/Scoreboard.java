package Tanks;

import processing.core.PApplet;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Scoreboard {
    private PApplet parent; 
    private GameConfig config; 
    private HashMap<Character, Integer> scores; 
    private long lastTimeDisplayed = 0;  
    private int scoreIndex = 0; 
    private List<Map.Entry<Character, Integer>> entries;

    public Scoreboard(PApplet parent, GameConfig config) {
        this.parent = parent;
        this.config = config;
        this.scores = new HashMap<>();
        this.entries = null;
    }


    public void initializeScores(ArrayList<Tank> tanks) {
        for (Tank tank : tanks) {
            scores.putIfAbsent(tank.getIdentifier(), 0); // Put 0 if tank's identifier is not already in the map.
        }
    }

    /**
     * Updates the score for a given tank identified by its ID.
     * If the tank does not exist in the scoreboard, its score is initialized and then updated.
     *
     * @param tankId The identifier of the tank whose score is updated.
     * @param initialScore The score to be added to the current score of the identified tank.
     */
    public void updateScore(char tankId, int initialScore) {
        scores.put(tankId, scores.getOrDefault(tankId, 0) + initialScore);  // Set initial score if not present or update existing score
    }

    public void resetScores() {
        for (Map.Entry<Character, Integer> entry : scores.entrySet()) {
            entry.setValue(0); // Reset each score to zero
        }
    }

    public void registerTank(char tankId) {
        if (!scores.containsKey(tankId)) {
            scores.put(tankId, 0);  // Register new tank with initial score of 0
        }
    }

    /**
     * Retrieves the current score of the specified player.
     *
     * @param playerID The character identifier of the player whose score is
     * @return The current score of the player.
     */
    public int getScore(char playerID) {
        return scores.getOrDefault(playerID, 0);
    }

    public void resetDisplayState() {
        scoreIndex = 0;
        lastTimeDisplayed = 0;
    }

    /**
     * Displays the  scores of all playerson the game window.
     * Scores are sorted and displayed in ascending order by player identifier.
     */
    public void display() {
        int margin = 20; 
        int headingHeight = 30; 
        int rowHeight = 30; 
        int scoreboardWidth = 200; 
        // Set the height based on the maximum number of players expected
        int maxPlayers = scores.size();
        int scoreboardHeight = headingHeight + maxPlayers * rowHeight;
        int scoreboardX = parent.width - scoreboardWidth - margin;
        int scoreboardY = margin + 60;
    
        parent.fill(255, 255, 255, 0); // Transparent background.
        parent.stroke(0); 
        parent.strokeWeight(2); 
        parent.rect(scoreboardX, scoreboardY, scoreboardWidth, scoreboardHeight);
    
        parent.textSize(16); 
        parent.fill(0);
        parent.noStroke(); 
        parent.textAlign(parent.LEFT, parent.CENTER);
        parent.text("Scores", scoreboardX + margin, scoreboardY + headingHeight / 2);
    
        parent.textSize(14); // Text size for player scores
        int y = scoreboardY + headingHeight;
    
        // Use a sorted list of entries to maintain consistent order
        if (entries == null || entries.isEmpty()) {
            entries = new ArrayList<>(scores.entrySet());
            entries.sort(Map.Entry.comparingByKey()); // Sort by player ID
        }
        
    
        // Display scores in a consistent order
        for (Map.Entry<Character, Integer> entry : entries) {
            char playerID = entry.getKey();
            int score = entry.getValue();
            int Colour = config.getPlayerColour(playerID); // Get player Colour
            parent.fill(Colour);
            parent.text("Player " + playerID + ": " + score, scoreboardX + margin, y + rowHeight / 2);
            y += rowHeight;
        }
    }
    
    

    /**
     * Displays the final scores at the end of the game, including the winner.
     * Sorts the scores in descending order and displays them after a delay.
     */
    public void displayFinalScoreboard() {
        int width = 300;
        int height = 200;
        int startX = (parent.width - width) / 2;
        int startY = (parent.height - height) / 2;
        int margin = 20;
        int lineHeight = 30;
        int headerPosition = startY + margin + lineHeight + 10; 
    
        // Setup the background and borders for the scoreboard
        parent.fill(0, 102, 153, 204);
        parent.stroke(255); 
        parent.rect(startX, startY, width, height);
    
        // Sort and initialize entries if necessary
        if (entries == null || entries.isEmpty() || lastTimeDisplayed == 0) {
            entries = new ArrayList<>(scores.entrySet());
            entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            lastTimeDisplayed = parent.millis(); // Reset last displayed time to now
            scoreIndex = 0; // Reset scoreIndex
        }
    
        //  Display the winner
        if (!entries.isEmpty()) {
            Map.Entry<Character, Integer> winner = entries.get(0);
            parent.textAlign(PApplet.CENTER, PApplet.CENTER);
            parent.textSize(24);
            parent.fill(config.getPlayerColour(winner.getKey())); // Use the winner's Colour
            parent.text("Winner: Player " + winner.getKey(), startX + width / 2, startY - 50);
        }
    
        // Display scores with a delay
        parent.textSize(18);
        parent.fill(255); // White text for scores
        parent.text("Final Scores", startX + width / 2, headerPosition - 30);
    
        int yPos = headerPosition; // Position for scores starting just below the header
        for (int i = 0; i < scoreIndex && i < entries.size(); i++) {
            Map.Entry<Character, Integer> entry = entries.get(i);
            parent.fill(config.getPlayerColour(entry.getKey()), 255);
            parent.text("Player " + entry.getKey() + ": " + entry.getValue(), startX + width / 2, yPos);
            yPos += lineHeight;
        }
    
        // Increment the scoreIndex if enough time has passed since the last update
        if (parent.millis() - lastTimeDisplayed > 700 && scoreIndex < entries.size()) {
            lastTimeDisplayed = parent.millis();
            scoreIndex++;
        }
    }
}    