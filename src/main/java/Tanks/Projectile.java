package Tanks;

import processing.core.PApplet;

public class Projectile extends GameObject {
    private float vx, vy; 
    static final float GRAVITY = 0.32f; 
    private float windEffect;
    private Tank owner;
    private int colour;

    public Projectile(PApplet app, float x, float y, float angle, int power, float windForce, Tank owner) {
        super(app, x, y);

        float radians = PApplet.radians(angle - 90);
        float velocityPerSecond = PApplet.map(power, 0, 100, 60, 540);
        float velocityPerFrame = velocityPerSecond / 30;

        this.vx = velocityPerFrame * PApplet.cos(radians);
        this.vy = velocityPerFrame * PApplet.sin(radians);
        this.windEffect = windForce * 0.03f;
        this.owner = owner;
        this.colour = owner.getColour();
    }

    /**
     * Returns the Tank instance that owns this projectile.
     * 
     * @return The owner tank of this projectile.
     */
    public Tank getOwner() {
        return owner;
    }

    /**
     * Updates the position and velocity of projectile based on
     * wind effect, current velocity.
     */
    public void update() {
        // Apply wind effect to horizontal velocity
        vx += windEffect / app.frameRate;

        // Update position with new velocities
        x += vx;
        y += vy;

        // Apply gravity to vertical velocity (gravity adjusted for frame rate)
        vy += GRAVITY;
    }

    /**
     * 
     * @param terrain The terrain where the projectile impacts
     * @return true if projectile has landed on the terrain, false otherwise
     */
    public boolean checkImpact(Terrain terrain) {
        float terrainHeight = terrain.getHeightAtX(x);
        return y >= terrainHeight; // Impact detection based on y-coordinate
    }

    /**
     * Checks if projectile is outside of the map, or has landed on the terrain.
     * @return true if projectile has landed, false otherwise
     */
    public boolean hasLanded() {
        return y >= app.height || y <= 0 || x < 0 || x > app.width;
    }

    public void display() {
        app.fill(colour);
        app.ellipse(x, y, 10, 10); // Display projectile as a small circle
    }
}
