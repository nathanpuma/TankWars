package Tanks;

import processing.core.PApplet;

public abstract class GameObject {
    protected PApplet app;
    protected float x, y; // Position of the object
    
    public GameObject(PApplet app, float x, float y) {
        this.app = app;
        this.x = x;
        this.y = y;
    }

    public abstract void display();

    // Getter methods for position
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
    
    // Setter methods for position
    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
}
