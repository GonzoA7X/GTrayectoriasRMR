package com.example.gtrayectoriasrmr.MobileAnarchy;

/**
 * Created by g on 30/06/2015.
 */
public interface JoystickMovedListener {
    public void OnMoved(int pan, int tilt);
    public void OnReleased();
    public void OnReturnedToCenter();
}
