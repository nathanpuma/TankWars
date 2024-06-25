package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class GameConfig {
    private PApplet app;
    private JSONObject config;
    private PImage background;
    private PImage treeImage;
    private JSONObject playerColours;
    private String layoutFilename;
    private int foregroundColour;
    private int currentLevelIndex = 0;
    private JSONArray levels;

    public GameConfig(PApplet app, String configPath) {
        this.app = app;
        this.config = app.loadJSONObject(configPath);
        this.levels = config.getJSONArray("levels");
        this.playerColours = config.getJSONObject("player_colours");
        loadLevel(currentLevelIndex);
    }
    

    public void loadNextLevel() {
        currentLevelIndex = (currentLevelIndex + 1) % levels.size(); // Loop back to the first level if it's the last
        loadLevel(currentLevelIndex);
    }

    /**
     * Checks if there is another level after the current one.
     *
     * @return true if there is at least one more level after the current level, false otherwise.
     */
    public boolean hasNextLevel() {
        return currentLevelIndex + 1 < levels.size();
    }

    /**
     * Resets the game to the first level.
     */
    public void resetGame() {
        currentLevelIndex = 0; // Explicitly set to the first level
        loadLevel(currentLevelIndex); // Reload the first level directly
    }
    
    
    /**
     * Reads through the json file to read the background and tree image, and the colour of the terrain.
     * @param levelIndex the level we are currently at
     */
    private void loadLevel(int levelIndex) {
        if (levelIndex >= levels.size()) {
            levelIndex = 0;  // Reset to first level if index is out of range
        }
        currentLevelIndex = levelIndex;
    
        JSONObject level = levels.getJSONObject(levelIndex);
        this.background = app.loadImage(level.getString("background"));
        this.layoutFilename = level.getString("layout");
    
        if (level.hasKey("trees")) {
            this.treeImage = app.loadImage(level.getString("trees"));
        } else {
            this.treeImage = null;
        }
    
        this.foregroundColour = parseColourString(level.getString("foreground-colour"));
    }
    
    /**
     * Method used to split the colour string so we can use that colour later on for tanks/projectiles
     * @param ColourString The string from the config file indicating colour
     * @return returns 3 Integers representing RGb colours
     */
    private int parseColourString(String ColourString) {
        String[] ColourValues = ColourString.split(",");
        return app.color(Integer.parseInt(ColourValues[0].trim()),
                         Integer.parseInt(ColourValues[1].trim()),
                         Integer.parseInt(ColourValues[2].trim()));
    }

    public PImage getBackground() {
        return background;
    }

    public String getLayoutFilename() {
        return layoutFilename;
    }

    public PImage getTreeImage() {
        return treeImage;
    }

    public int getForegroundColour() {
        return foregroundColour;
    }


    /**
     * Retrieves the player-specific color based on the player ID.
     *
     * @param playerID The character identifier of the player.
     * @return The integer color value for the specified player.
     */
    public int getPlayerColour(char playerID) {
        String key = String.valueOf(playerID);
        if (playerColours.hasKey(key)) {
            String ColourStr = playerColours.getString(key);
            if (ColourStr.equals("random")) {
                return app.color((int)app.random(256), (int)app.random(256), (int)app.random(256));
            } else {
                return parseColourString(ColourStr);
            }
        }
        return app.color(0); // Default Colour if no specific Colour is defined
    }

    public void displayBackground(int width, int height) {
        app.image(background, 0, 0, width, height);
    }
}
