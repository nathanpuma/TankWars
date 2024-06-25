package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import java.util.Random; 

public class Tree extends GameObject {
    private PImage treeImage;
    private Terrain terrain;
    private Random rand;

    public Tree(PApplet app, PImage treeImage, float x, Terrain terrain) {
        super(app, x, 0);
        this.treeImage = treeImage;
        this.terrain = terrain;
        this.rand = new Random(); 
        this.x += rand.nextInt(61) - 30; // Random.nextInt(61) gives a value from 0 to 60, subtract 30 to get range -30 to +30
        updatePosition(); // Set initial y position based on terrain height
    }


    /**
     * Updates the vertical position of the tree based on the terrain's height at its current x value.
     * This method ensures that the tree is aligned with the terrain surface.
     * If no image is associated, the tree's base directly sits on the terrain.
     */
    public void updatePosition() {
        float terrainHeight = terrain.getHeightAtX(this.x);
        if (treeImage != null) {
            this.y = terrainHeight - treeImage.height / 2; // Adjusting so that the center of the tree aligns with the terrain surface
        } else {
            this.y = terrainHeight;
        }
    }

    public int getTreeImageHeight() {
        return (treeImage != null) ? treeImage.height : 0;
    }

    public void display() {
        if (treeImage != null) {
            app.imageMode(PApplet.CENTER);
            app.image(treeImage, x, y);
            app.imageMode(PApplet.CORNER);
        }
    }
}
