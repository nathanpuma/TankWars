package Tanks;

import processing.core.PImage;
import java.util.ArrayList;
import java.util.Iterator;
import processing.core.PApplet;

public class Tank extends GameObject {
    private float angle;
    private int colour;
    private Terrain terrain;
    private float moveSpeed = 60;
    private int fuel = 250;
    private PImage fuelImage, parachuteImage;
    private int health = 100, power = 50;
    private ArrayList<Projectile> projectiles;
    private boolean isActive, shieldActive = false, hasExploded = false;
    private char identifier;
    private Scoreboard scoreboard;
    private int maxPower = 100, minPower = 0;
    private int parachutes = 1, parachutesUsedThisLevel = 0;
    private int explosionRadius;
    private long turnIndicatorStartTime = -1;
    private static final int TURN_INDICATOR_DURATION = 2000;

    public Tank(PApplet app, float x, float y, int colour, Terrain terrain, char identifier, Scoreboard scoreboard) {
        super(app, x, y);
        this.colour = colour;
        this.terrain = terrain;
        this.fuelImage = app.loadImage("Tanks/fuel.png");
        this.parachuteImage = app.loadImage("Tanks/parachute.png");
        this.isActive = false;
        this.projectiles = new ArrayList<>();
        this.identifier = identifier;
        this.scoreboard = scoreboard;
        updatePosition(false);
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public char getIdentifier() {
        return identifier;
    }

    // Activates or deactivates the tank
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Updates the tank's vertical position to align with the terrain, ensuring the tank is not above the ground.
     */
    public void calibratePosition() {
        float terrainHeight = terrain.getHeightAtX(x);
        if (y > terrainHeight) {
            y = terrainHeight;  // Set y to terrain height if it's above, ensuring tanks are 'on' the ground
        }
    }
    
    /**
     * Updates the position of the tank, including handling parachute deployment and fall damage calculations.
     * @param isMoving indicates whether the tank is moving horizontally (true) or not (false).
     */
    public void updatePosition(boolean isMoving) {
        float terrainHeight = terrain.getHeightAtX(x);
        float previousY = y; // Store the previous Y position to compare after potential fall
    
        // Calculate the fall speed based on whether a parachute is available
        float fallSpeed = 120 / app.frameRate; // Default fall speed without parachute
        boolean parachuteDeployed = false;
    
        // When moving, avoid deploying parachutes or calculating fall damage
        if (!isMoving) {
            if (parachutes > 0 && y < terrainHeight) {
                fallSpeed = 60 / app.frameRate; // Reduced fall speed with parachute
                parachuteDeployed = true;
            }
        }
    
        // Simulate falling or descending
        y += fallSpeed;
        y = Math.min(y, terrainHeight); // Ensure the tank does not go below the terrain
    
        // Apply falling logic and check parachute usage if not just moving horizontally
        if (!isMoving) {
            if (parachuteDeployed && y >= terrainHeight) {
                parachutes--; // Decrement parachute count if it was used
                parachutesUsedThisLevel++; // Increment the used parachute count for the level
            } else if (!parachuteDeployed && previousY < terrainHeight && y >= terrainHeight) {
                if (previousY < y) {
                    int verticalDistance = (int) (terrainHeight - previousY);
                    int damage = verticalDistance; // Damage is 1 HP per pixel fallen
                    applyDamage(damage, null);
                }
            }
        }
    }

    /**
     * Purchases 1 parachute for the tank if the tank has more than or equal to 15 points
     */
    public void purchaseParachute() {
        int parachuteCost = 15;
        if (scoreboard.getScore(identifier) >= parachuteCost) {
            parachutes += 1; // Increment the parachute count
            scoreboard.updateScore(identifier, -parachuteCost); // Deduct the score
        }
    }
    
    /**
     * After the next level loads, all parachutes are set to 1 for that particular level
     */
    public void resetParachuteForNewLevel() {
        if (parachutesUsedThisLevel > 0) {
            parachutes = 1; // Reset to one parachute per level
            parachutesUsedThisLevel = 0; // Reset the count of used parachutes
        }
    }
    
    
    public void recalibratePosition() {
        updatePosition(false); // Calls existing method to adjust position based on terrain height
    }
    
    /**
     * Tank moves to the left, consuming fuel every time it moves. Doesn't go past the left side of the map.
     */
    public void moveLeft() {
        if (fuel > 0) {
            float distance = moveSpeed / app.frameRate;
            if (x - distance >= 0) { // Check if moving left is within the terrain bounds
                x -= distance;
                updatePosition(true); // Update the position to reflect movement
                fuel -= distance; // Reduce fuel by the distance moved
            }
        }
    }

    /**
     * Tank moves to the right, consuming fuel every time it moves. Doesn't go past the right side of the map.
     * The 30 added on is for the tank body.
     */
    public void moveRight() {
        if (fuel > 0) {
            float distance = moveSpeed / app.frameRate;
            if (x + distance + 30 <= terrain.getWidth()) { // Check if moving right is within the terrain bounds
                x += distance;
                updatePosition(true); // Pass true to indicate it's a movement update
                fuel -= distance;
            }
        }
    }

    /**
     * Rotates the tank's turret to the left, only capable of facing 90 degrees from its starting point
     * It's starting point is pointing straight upwards.
     */
    public void rotateTurretLeft() {
        float rotationSpeed = 3; // +3 radians per second
        angle = Math.min(angle + rotationSpeed / app.frameRate, (float) Math.PI / 2);
    }

    /**
     * Rotates the tank's turret to the right, only capable of facing 90 degrees from its starting point
     * It's starting point is pointing straight upwards.
     */
    public void rotateTurretRight() {
        float rotationSpeed = 3; // -3 radians per second
        angle = Math.max(angle - rotationSpeed / app.frameRate, -(float) Math.PI / 2);
    }

    /**
     * Fires a projectile from the tank in the direction the turret is facing, influenced by wind.
     * @param wind The current wind affecting projectile trajectories.
     * @param explosions The list of active explosions to which the new explosion will be added.
     * @param terrain The terrain where the projectile can land and effect.
     * @param allTanks The list of all tanks in the game, which could be hit by the projectile.
     */
    public void fire(Wind wind, ArrayList<Explosion> explosions, Terrain terrain, ArrayList<Tank> allTanks) {
        Projectile newProjectile = new Projectile(app, x, y, (float) (-angle * (180.0 / Math.PI)), power, wind.getWindForce(), this);
        projectiles.add(newProjectile);
    }

    // Updates and manages projectiles
    public void updateProjectile(ArrayList<Explosion> explosions, Terrain terrain, ArrayList<Tank> allTanks) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile projectile = it.next();
            projectile.update();
            if (projectile.checkImpact(terrain)) {
                explosions.add(new Explosion(app, projectile.getX(), projectile.getY(), terrain, allTanks, projectile.getOwner()));
                it.remove(); // Remove the projectile as it has impacted
            } else if (projectile.hasLanded()) {
                it.remove(); // Remove projectiles that have landed without impact
            }
        }
    }

    /**
     * Applies damage to the tank and potentially triggers an explosion if health drops to zero.
     * @param damage The amount of damage to apply.
     * @param attacker The tank causing the damage, possibly null if environment-induced.
     */
    public void applyDamage(int damage, Tank attacker) {
        if (!shieldActive) {
            int oldHealth = health;
            health -= damage;
            health = Math.max(0, health);
    
            // Adjust power to not exceed the new health
            if (power > health) {
                power = health;
                displayPower(); // Update power display as it might have changed
            }
    
            if (health <= 0 && !hasExploded) {
                explode(15);
            }
            if (attacker != null && attacker != this) {
                attacker.getScoreboard().updateScore(attacker.getIdentifier(), oldHealth - health);
            }
        } else {
            shieldActive = false;
        }
    }
    
    public int getColour() {
        return colour;
    }

    public int getHealth() {
        return health;
    }

    public boolean isActive() {
        return this.isActive;
    }

    /**
     * Increases the power of the tank's next shot, not exceeding the tank's current health.
     */
    public void increasePower() {
        if (isActive && health > 0) { // Only allow power increase if the tank is active and has health
            int oldPower = power; // Store old power for comparison
            power += 36 / app.frameRate;  // Increase power by 36 units per second
            power = Math.min(power, maxPower); // Cap power at maxPower
            power = Math.min(power, health); // Ensure power does not exceed current health
    
            // Display power should not show higher than actual usable power
            if (power != oldPower) { // Only update if there's a change
                displayPower(); // Update display to reflect new power level
            }
        }
    }
    
    /**
     * Decreases the power of the tank's next shot, not falling below 0.
     */
    public void decreasePower() {
        if (isActive && health > 0) { // Only allow power decrease if the tank is active and has health
            int oldPower = power; // Store old power for comparison
            power -= 36 / app.frameRate;  // Decrease power by 36 units per second
            power = Math.max(power, minPower); // Ensure power does not go below minPower
            power = Math.min(power, health); // Ensure power does not exceed current health
    
            // Display power should not show lower than actual usable power
            if (power != oldPower) { // Only update if there's a change
                displayPower(); // Update display to reflect new power level
            }
        }
    }

    /**
     * Repairs the current tank by increasing its health by 20, costing 20 points.
     */
    public void repair() {
        if (scoreboard.getScore(identifier) >= 20) { // Assuming the scoreboard can return the score for a given identifier
            health += 20;
            health = Math.min(health, 100); // Cap health at 100
            scoreboard.updateScore(identifier, -20); // Deduct score
        }
    }

    /**
     * Increases the fuel of the tank by 200, costing 10 points.
     */

    public void refuel() {
        if (scoreboard.getScore(identifier) >= 10) {
            fuel += 200;
            scoreboard.updateScore(identifier, -10); // Deduct score
        }
    }

    /**
     * Activates a protective shield around the tank, costing 20 points.
     */
    public void activateShield() {
        if (scoreboard.getScore(identifier) >= 20) {
            shieldActive = true;
            scoreboard.updateScore(identifier, -20); // Deduct the cost of the shield
        }
    }
    
    /**
     * If the tank goes below the map, it explodes a circle with a radius of 30
     */
    public void belowMapExplosion() {
        if (y > app.height && !hasExploded) {
            explode(30); // Explode with a radius of 30 if it falls below the map
        }
    }

    /**
     * Is called when the tank is below 0 or falls below the map
     * @param radius is the size of the circle which indicates the explosion
     */
    private void explode(int radius) {
        this.explosionRadius = radius;
        this.hasExploded = true;
        displayExplosion(); // This will render the explosion visually
    }

    /**
     * This explosion is only for tanks when they reach 0 health or fall below the map
     */
    private void displayExplosion() {
        app.fill(255, 0, 0); // Use red color for the explosion
        app.noStroke();
        app.ellipse(x, y, explosionRadius * 2, explosionRadius * 2); // Draw the explosion as a simple circle
    }

    public void displayPower() {
        app.fill(0);  // White color for text
        app.textSize(16);
        app.textAlign(PApplet.CENTER, PApplet.TOP);
        app.text("Power: " + PApplet.round(power), app.width / 2, 50);  // Display at a fixed position
    }

    private void displayFuelInfo() {
        int newWidth = fuelImage.width / 10;
        int newHeight = fuelImage.height / 10;
        app.image(fuelImage, 135, 5, newWidth, newHeight);
        app.fill(0);
        app.textSize(16);
        app.textAlign(PApplet.LEFT, PApplet.TOP);
        app.text("Fuel: " + fuel, 165, 10);
    }
    
    private void displayParachuteInfo() {
        int imageWidth = parachuteImage.width / 2;
        int imageHeight = parachuteImage.height / 2;
        int x = 135; // Starting position, align under the fuel icon or adjust as needed
        int y = 50; // Position below the fuel info
    
        // Display parachute image
        app.image(parachuteImage, x, y, imageWidth, imageHeight);
        // Display the number of parachutes remaining next to the image
        app.fill(0); // Set text color to black
        app.textSize(16);
        app.textAlign(PApplet.LEFT, PApplet.TOP);
        app.text(parachutes, x + imageWidth + 5, y + imageHeight / 2 - 8); // Adjust text positioning for alignment
    }

    public void startTurn(long currentTime) {
        turnIndicatorStartTime = currentTime;
    }
    
    private void displayTurnIndicator(long currentTime) {
        if (currentTime - turnIndicatorStartTime < TURN_INDICATOR_DURATION) {
            app.pushMatrix();
            app.translate(x, y - 100);
            app.fill(0, 0, 0);
            app.noStroke();
            app.rect(-1.5f, 0, 3, 30);
            app.triangle(-10, 30, 10, 30, 0, 45);
            app.popMatrix();
        }
    }
    
    

    public void displayHealthBar() {
        if (isActive) {
            int healthBarX = app.width / 2 - 100;
            int healthBarY = 10;
            int healthBarWidth = 200;
            int healthBarHeight = 20;

            // Border for health bar
            app.fill(0);
            app.noStroke();
            app.rect(healthBarX - 2, healthBarY - 2, healthBarWidth + 4, healthBarHeight + 4);

    
            // Draw the health bar in the tank's color based on current health
            app.fill(colour);
            float currentHealthWidth = healthBarWidth * health / 100;
            app.rect(healthBarX, healthBarY, currentHealthWidth, healthBarHeight);
    
            // Draw power indicator line
            float powerRatio = (float) power / maxPower;
            float powerIndicatorPosition = healthBarWidth * powerRatio;
            app.stroke(0, 255, 0); // Green color for the power indicator line
            app.strokeWeight(2);
            app.line(healthBarX + powerIndicatorPosition, healthBarY - 5, healthBarX + powerIndicatorPosition, healthBarY + healthBarHeight + 5);
    
            // Draw text for health
            app.fill(0); // White color for the text
            app.textSize(16);
            app.textAlign(PApplet.CENTER, PApplet.TOP);
            app.text("Health: " + health, app.width / 2, 35);
        }
    }
    
    // Displays the tank and its projectiles
    public void display() {
        if (!hasExploded) {
            app.pushMatrix();
            app.noStroke();
            app.translate(x, y);
            if (shieldActive) {
                app.fill(0, 0, 255, 100); // Semi-transparent blue bubble
                app.ellipse(0, 0, 40, 40); // Assuming the shield bubble size
            } 
            if (parachutes > 0 && y < terrain.getHeightAtX(x)) {
                app.image(parachuteImage, -30, -60); // Display parachute above tank
            }

            app.fill(colour);
            app.rect(-15, 0, 30, 10);  // Draw the tank body
            app.pushMatrix();
            app.rotate(-angle);
            app.fill(0);
            app.rect(-2.5f, -20, 5, 20);  // Draw the turret
            app.popMatrix();
            app.popMatrix();

            for (Projectile projectile : projectiles) {
                projectile.display();
            }

            if (isActive) {
                displayFuelInfo();
                displayTurnIndicator(app.millis());
                displayParachuteInfo();
            }
        } else {
            displayExplosion();
        }
    }
}