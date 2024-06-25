package Tanks;

import processing.core.*;
import processing.event.*;

import java.util.*;



public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 0;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 30;

    public String configPath;

    public static Random random = new Random();

    private Terrain terrain;
    private GameConfig config;
    private Wind wind;
    private Scoreboard scoreboard;
    ArrayList<Tank> tanks;
    private int currentTankIndex = 0; 
    private ArrayList<Tree> trees;
    PImage treeImage;
    private ArrayList<Explosion> explosions;
    private boolean waitingForNextLevel = false;
    private int levelTransitionStartTime;
    private static final int LEVEL_TRANSITION_DELAY = 1000; 
    PImage parachutImage;
    private boolean displayFinalScoreboard = false;



	// Feel free to add any additional methods or attributes you want. Please put classes in different files.

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
        
    }
    

    /**
     * Load all resources such as images. Initialise the elements such as the player, enemies and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        config = new GameConfig(this, "config.json"); // Initialize game configuration
        initializeEnvironment(); // Setup wind, scoreboard, etc. before initializing game objects
        initializeTerrain(); // Load and setup the terrain
        initializeGameObjects(); // Setup tanks and possibly other objects
        scoreboard.initializeScores(tanks); // Initialize scores after tanks are fully created
        explosions = new ArrayList<>();
    }
    



    private void initializeTerrain() {
        PImage treeImage = config.getTreeImage();  // Retrieve the tree image
        int foregroundColour = config.getForegroundColour();  // Retrieve the foreground Colour
        terrain = new Terrain(this, treeImage, foregroundColour);   // Initialize terrain with the tree image
        terrain.loadTerrain(config.getLayoutFilename());  // Load the terrain layout
    }
    

    private void initializeGameObjects() {
        tanks = new ArrayList<>();
        trees = new ArrayList<>();
        String[] lines = loadStrings("level1.txt");  // Load level configuration
    
        for (int row = 0; row < lines.length; row++) {
            for (int col = 0; col < lines[row].length(); col++) {
                char ch = lines[row].charAt(col);
    
                if (ch >= 'A' && ch <= 'I') {  // Create tank if character is between A and I
                    int tankColour = config.getPlayerColour(ch);
                    Tank tank = new Tank(this, col * CELLSIZE + CELLSIZE / 2.0f, terrain.getHeightAtX(col * CELLSIZE + CELLSIZE / 2.0f), tankColour, terrain, ch, scoreboard);
                    tanks.add(tank);
                    scoreboard.registerTank(tank.getIdentifier());
                    scoreboard.updateScore(ch, 0);  // Initialize tank's score in scoreboard when tank is created
                } else if (ch == 'T') {
                    Tree tree = new Tree(this, config.getTreeImage(), col * CELLSIZE + CELLSIZE / 2.0f, terrain);
                    trees.add(tree);
                }
            }
        }
        
        //lambda function tanks to be sorted by identifier
        tanks.sort((tank1, tank2) -> Character.compare(tank1.getIdentifier(), tank2.getIdentifier()));

        if (!tanks.isEmpty()) {
            tanks.get(0).setActive(true); // Explicitly set the first tank as active
            currentTankIndex = 0; // Ensure the index is set to the start
        }
    }
    

    private void initializeEnvironment() {
        wind = new Wind(this);
        scoreboard = new Scoreboard(this, config);
    }

    /**
     * Goes to the next tanks turn
     */
    public void nextTurn() {
        if (!tanks.isEmpty() && currentTankIndex < tanks.size()) {
            tanks.get(currentTankIndex).setActive(false);
        }
    
        // Increment to the next tank
        currentTankIndex = (currentTankIndex + 1) % tanks.size();
        tanks.get(currentTankIndex).setActive(true);
        tanks.get(currentTankIndex).startTurn(millis());  // Pass the current time
    
        // Update the wind or any other environmental effects
        if (wind != null) {
            wind.updateWind();
        }
    }
    
    /**
     * Restarts the game by reloading the first level
     * Reinitalises most game objects and resets scoreboard and wind
     */
    private void restartGame() {
        config.resetGame(); // Load the first level configuration
        
        initializeTerrain(); // Reset and redraw the terrain based on the first level
        initializeGameObjects(); // Reinitialize tanks and other objects based on the first level
        scoreboard.resetScores(); // Explicitly reset the scoreboard scores
        scoreboard.initializeScores(tanks); // (Optional) Reinitialize scores based on the new game state
        scoreboard.resetDisplayState();

        wind = new Wind(this); // Reinitialize the wind
        displayFinalScoreboard = false; // Ensure final scoreboard is not displayed unless game ends
        waitingForNextLevel = false; // Reset waiting for next level flag
        currentTankIndex = 0; // Start from the first tank again
        tanks.get(currentTankIndex).setActive(true); // Ensure the first tank is active
    }

    /**
     * Checks if config has another level. If not displayFinalScoreBoard is active
     * Reinitializes game objects for next level once that level is loaded.
     */

    public void checkEndOfLevel() {
        // Check if there's only one tank left or if all tanks are inactive
        if (tanks.size() == 1 && !waitingForNextLevel) {
            waitingForNextLevel = true;
            levelTransitionStartTime = millis();
            // Check if there are more levels to play
            if (!config.hasNextLevel()) {
                displayFinalScoreboard = true; // Only display final scoreboard if no more levels
            }
        } else if (waitingForNextLevel && millis() - levelTransitionStartTime > LEVEL_TRANSITION_DELAY) {
            waitingForNextLevel = false;
            if (config.hasNextLevel()) {
                config.loadNextLevel(); // Load the next level configuration
                initializeTerrain();    // Reinitialize the terrain with the new level data
                initializeGameObjects(); // Re-setup tanks and other objects for the new level
            } 
        }
    }
    
    /**
     * Updates explosions and tanks' positions on the terrain.
     */
    public void updateExplosionsAndTanks() {
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update(); // Update explosion
            if (!explosion.isActive()) {
                explosionIterator.remove(); // Remove inactive explosions
            }
        }
    
        for (Tank tank : tanks) {
            float terrainHeight = terrain.getHeightAtX(tank.getX());
            if (tank.getY() > terrainHeight) {
                tank.setY(tank.getY());
                if (tank.getY() > this.height) { // Check if the tank is below the screen
                    tank.setActive(false);
                }
            } else {
                tank.recalibratePosition(); // Adjust tank position based on terrain
            }
        }
    }


    /**
     * Removes tanks that fall through the terrain
     */
    public void removeOffscreenTanks() {
        Iterator<Tank> iterator = tanks.iterator();
        while (iterator.hasNext()) {
            Tank tank = iterator.next();
            if (tank.getY() >= height) {  // Check if the tank is below the screen
                if (tank.isActive()) {
                }
                iterator.remove();
            }
        }
    }
    
    /**
     * Removes tanks that are no longer active, either from falling through the map
     * or having < 0 health.
     */
    public void removeInactiveTanks() {
        Iterator<Tank> iterator = tanks.iterator();
        while (iterator.hasNext()) {
            Tank tank = iterator.next();
            if (!tank.isActive() && tank.getHealth() <= 0) {
                iterator.remove();  // Remove the tank from the list
            }
        }
    
        // Ensure the currentTankIndex is still valid
        if (currentTankIndex >= tanks.size()) {
            currentTankIndex = 0;  // Reset to the first tank if the previous active tank was the last one
        }
        // Check if there are any active tanks left
        if (!tanks.isEmpty() && !tanks.get(currentTankIndex).isActive()) {
            nextTurn(); // Move to the next available tank
        }
    }
    

    /**
     * Removes trees if they fall below the screen.
     */
    public void removeOffscreenTrees() {
        Iterator<Tree> treeIterator = trees.iterator();
        while (treeIterator.hasNext()) {
            Tree tree = treeIterator.next();
            // Check if the bottom of the tree is below the height of the screen
            if (tree.getY() + tree.getTreeImageHeight() >= height) {
                treeIterator.remove(); // Remove the tree from the list
            }
        }
    }


    /**
     * Displays which player's turn it is.
     */
    public void displayTurnInfo() {
        if (currentTankIndex >= 0 && currentTankIndex < tanks.size()) {  // Check if index is valid
            Tank currentTank = tanks.get(currentTankIndex);
            String turnText = "Player " + currentTank.getIdentifier() + "'s Turn";
            textSize(16);
            fill(0);
            textAlign(PApplet.LEFT, PApplet.TOP);
            text(turnText, 10, 10);
        }
    }
    

    /**
     * Receive key pressed signal from the keyboard.
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (tanks.size() > 0 && currentTankIndex < tanks.size()) {
            Tank currentTank = tanks.get(currentTankIndex);

            switch (event.getKeyCode()) {
                case PApplet.UP:
                    currentTank.rotateTurretLeft();
                    break;
                case PApplet.DOWN:
                    currentTank.rotateTurretRight();
                    break;
                case PApplet.LEFT:
                    currentTank.moveLeft();
                    break;
                case PApplet.RIGHT:
                    currentTank.moveRight();
                    break;
                case ' ':
                    currentTank.fire(wind, explosions, terrain, tanks);
                    nextTurn();
                    break;
                case 'W':
                case 'w':
                    currentTank.increasePower();
                    break;
                case 'S':
                case 's':
                    currentTank.decreasePower();
                    break;
                case 'R':
                case 'r':
                    if (!displayFinalScoreboard) {
                        currentTank.repair();
                    } else {
                        restartGame();
                    }
                    break;  
                case 'F':
                case 'f':
                    currentTank.refuel(); 
                    break;
                case 'H':
                case 'h':
                    tanks.get(currentTankIndex).activateShield();
                    break;
                case 'P':
                case 'p':
                    currentTank.purchaseParachute(); 
                    break;
            }
        }
    }
    
    

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }
    

    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        background(0);
        config.displayBackground(WIDTH, HEIGHT);
        terrain.display();

        if (!displayFinalScoreboard) {
            displayTurnInfo();
            wind.displayWind();
            scoreboard.display();

            for (Tree tree : trees) {
                tree.updatePosition();
                tree.display();
            }

            removeOffscreenTrees();

            for (Tank tank : tanks) {
                tank.calibratePosition();
            }

            Tank activeTank = null;
            for (Tank tank : tanks) {
                tank.updatePosition(false);
                tank.updateProjectile(explosions, terrain, tanks);
                tank.display();
                if (tank.isActive()) {
                    activeTank = tank;
                    activeTank.displayHealthBar();
                    activeTank.displayPower();
                }
            }

            for (Explosion explosion : explosions) {
                explosion.update();
                explosion.display();
                if (!explosion.isActive()) {
                    explosions.remove(explosion);
                }
            }

            updateExplosionsAndTanks();
            removeOffscreenTanks();
            removeInactiveTanks();
            checkEndOfLevel();
        } else {
            scoreboard.displayFinalScoreboard();
        }
    }

    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }

}