package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import java.util.Random;

public class Wind {
    private PApplet parent;
    private PImage windImageLeft;
    private PImage windImageRight;
    private int windForce;
    private Random random;

    public Wind(PApplet p) {
        parent = p;
        windImageLeft = parent.loadImage("Tanks/wind-1.png"); // Image for wind blowing left
        windImageRight = parent.loadImage("Tanks/wind.png"); // Image for wind blowing right
        random = new Random();
        initializeWind();
    }

    private void initializeWind() {
        // Initialize windForce with a random value between -35 and 35
        windForce = random.nextInt(71) - 35; // 71 because 35 + 35 + 1
    }

    public void updateWind() {
        // Change wind by a random value between -5 and 5
        windForce += random.nextInt(11) - 5; // 11 because 5 + 5 + 1
        // Keep windForce within the bounds
    }

    public void displayWind() {
        // Display the wind image in the top right corner of the screen
        PImage currentImage = windForce < 0 ? windImageLeft : windImageRight;
        int imageX = parent.width - currentImage.width - 50;
        int imageY = 10;
        parent.image(currentImage, imageX, imageY);
        
        //Display wind image text
        String windText = Math.abs(windForce) + ""; // Use absolute value to remove the negative sign
        int textX = parent.width - currentImage.width + 30; 
        int textY = imageY + currentImage.height - 40;
        parent.fill(0);
        parent.textSize(12);
        parent.text(windText, textX, textY);
    }
    

    public int getWindForce() {
        return windForce;
    }
}
