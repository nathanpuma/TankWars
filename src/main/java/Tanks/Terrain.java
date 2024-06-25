package Tanks;

import processing.core.PApplet;
import processing.core.PImage;



public class Terrain {
    
    private PApplet app;
    private int[][] terrainMap;
    private PImage treeImage;
    private static final int CELL_SIZE = 32;
    private static final int CELL_HEIGHT = 32;
    private int foregroundColour;
    private Tank lastDestroyer;
    private int[] initialHeights;

    public Terrain(PApplet app, PImage treeImage, int foregroundColour) {
        this.app = app;
        this.treeImage = treeImage;
        terrainMap = new int[app.width / CELL_SIZE][app.height / CELL_HEIGHT];
        this.foregroundColour = foregroundColour;
        this.treeImage = treeImage;
        if (treeImage != null) {
            this.treeImage.resize(treeImage.width / 10, treeImage.height / 10);
        } else {
            this.treeImage = null;
        } 
    }
    

    /**
     * Loads terrain from a file, specifically indicated by X.
     * Initalizes the terrain height.
     * @param filename path to file containing terrain layout
     */
    public void loadTerrain(String filename) {
        String[] lines = app.loadStrings(filename);
        terrainMap = new int[28][20]; // Assuming fixed dimensions for simplicity
        initialHeights = new int[terrainMap.length]; // Store the initial heights for smoothing

        for (int row = 0; row < lines.length; row++) {
            for (int col = 0; col < lines[row].length(); col++) {
                if (lines[row].charAt(col) == 'X') {
                    initialHeights[col] = 20 - row; //  the bottom of the map is 20 rows down
                }
            }
        }     
    }

    public int getWidth() {
        return terrainMap.length * CELL_SIZE;
    }

    /**
     * Calculates the terrain height at a specific x-coordinate.
     * This function interpolates between discrete terrain height points to provide a smooth terrain profile.
     *
     * @param x The x-coordinate at which to retrieve the terrain height.
     * @return The interpolated height of the terrain at the given x-coordinate.
     */
    public float getHeightAtX(float x) {
        if (x < 0 || x >= terrainMap.length * CELL_SIZE) {
            return app.height; // Essentially out of bounds
        }
        
        // Calculates the X on the left of the original X
        int leftIndex = (int) Math.floor(x / CELL_SIZE);
        // Calculates the X on the right of the original X
        int rightIndex = (int) Math.ceil(x / CELL_SIZE);
        
        // Clamps the values to ensure leftIndex is not minimum the first X value (0)
        leftIndex = Math.max(0, leftIndex);
        // Clamps the values to ensure rightIndex is maximum the last index of initialHeights
        rightIndex = Math.min(initialHeights.length - 1, rightIndex);

        
        // Lerp factor is a point between 0 and 1. Any value between 0 and 1 indicates how far
        // between the two indicies the x coordinate is
        float lerpFactor = (x - leftIndex * CELL_SIZE) / CELL_SIZE;

        // Find how high the terrain is at these two indicies starting from the bottom of the screen
        float heightLeft = app.height - (initialHeights[leftIndex] * CELL_HEIGHT);
        float heightRight = app.height - (initialHeights[rightIndex] * CELL_HEIGHT);
        
        // Takes the height of the left and right indicies, and the lerp factor which finds a height in between
        return PApplet.lerp(heightLeft, heightRight, lerpFactor);
    }
    

    /**
     * Simulates the destruction of terrain due to an explosion. This method adjusts the terrain heights within the radius of the explosion.
     * It also records the tank responsible for the destruction.
     *
     * @param explosionX The x-coordinate of the explosion's center.
     * @param explosionY The y-coordinate of the explosion's center.
     * @param explosionRadius The radius of the explosion which affects the terrain.
     * @param destroyer The tank responsible for the explosion.
     */
    public void destroyTerrain(float explosionX, float explosionY, float explosionRadius, Tank destroyer) {
        int startIndex = Math.max(0, (int) ((explosionX - explosionRadius) / CELL_SIZE));
        int endIndex = Math.min(initialHeights.length - 1, (int) ((explosionX + explosionRadius) / CELL_SIZE));

        for (int i = startIndex; i <= endIndex; i++) {
                initialHeights[i] = Math.max(0, initialHeights[i] - 1);  // Lower by one unit for test
            }
        lastDestroyer = destroyer;
        }


    public Tank getLastDestroyer() {
        return lastDestroyer;
    }

    public void display() {
        app.noStroke();
        app.fill(foregroundColour);
        app.beginShape();
    
        // Duplicate the starting control point
        app.curveVertex(0, app.height - initialHeights[0] * CELL_HEIGHT);
    
        // Loop through the terrain heights and create vertices
        for (int i = 0; i < terrainMap.length; i++) {
            int x = i * CELL_SIZE;
            int y = app.height - (initialHeights[i] * CELL_HEIGHT);
            app.curveVertex(x, y);
        }
        
        // Duplicate the ending control point
        app.curveVertex(terrainMap.length * CELL_SIZE - CELL_SIZE, app.height - initialHeights[terrainMap.length - 1] * CELL_HEIGHT);
        // Close the shape at the bottom right
        app.vertex(terrainMap.length * CELL_SIZE - CELL_SIZE, app.height);
        // Close the shape at the bottom left
        app.vertex(0, app.height);
        app.endShape(app.CLOSE);
                
    }
}