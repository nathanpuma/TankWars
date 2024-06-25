package Tanks;

import processing.core.PApplet;
import java.util.ArrayList;

public class Explosion extends GameObject {
    private float radius;
    private boolean active;
    private static final float MAX_RADIUS = 30;
    private Terrain terrain;
    private ArrayList<Tank> tanks;
    private Tank owner;

    public Explosion(PApplet app, float x, float y, Terrain terrain, ArrayList<Tank> tanks, Tank owner) {
        super(app, x, y);
        this.radius = 0;
        this.active = true;
        this.terrain = terrain;
        this.tanks = tanks;
        this.owner = owner;
    }
    
    /**
     * Updates the explosion's state by expanding its radius until reaching the maximum limit,
     * at which point it damages the terrain and tanks within the pixels of the explosion
     */
    public void update() {
        if (active && radius < MAX_RADIUS) {
            radius += 3;
            if (radius >= MAX_RADIUS) {
                active = false;
                terrain.destroyTerrain(x, y, MAX_RADIUS, owner); // Pass owner as the destroyer
                applyDamageToTanks();
            }
        }
    }

     /**
     * Applies damage to tanks within the explosion's radius based on their proximity to the center.
     */
    private void applyDamageToTanks() {
        for (Tank tank : tanks) {
            float distance = PApplet.dist(x, y, tank.getX(), tank.getY());
            if (distance < MAX_RADIUS) {
                int damage = (int)((1 - (distance / MAX_RADIUS)) * 60);
                tank.applyDamage(damage, owner);  // Pass the owner as the attacker
            }
        }
    }

    /**
     * Returns whether the explosion is still active.
     * @return true if the explosion is active, false otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Displays the explosion that has 3 separate circles to display a red-organe-yellow explosion
     */
    public void display() {
        if (active) {
            // Draw the red circle
            app.fill(255, 0, 0);
            app.stroke(255, 0, 0);  // Red
            app.ellipse(x, y, radius * 2, radius * 2);

            // Draw the orange circle at 50% of the main radius
            app.fill(255, 165, 0);
            app.stroke(255, 165, 0);  // Orange
            app.ellipse(x, y, radius, radius);

            // Draw the yellow circle at 20% of the main radius
            app.fill(255, 255, 0);
            app.stroke(255, 255, 0);  // Yellow
            app.ellipse(x, y, radius * 0.4f, radius * 0.4f);
        }
    }
}
