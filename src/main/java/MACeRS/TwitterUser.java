/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MACeRS;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Mir Saman
 */
public class TwitterUser {

    double StatusRate;
    Color backgroundColor;
    double[] weights;
    double tetha;
    List<TwitterUser> followings = new ArrayList<>();
    List<TwitterUser> followers = new ArrayList<>();

    List<TwitterUser> celebs = new ArrayList<>();
    List<TwitterUser> noncelebs = new ArrayList<>();

    boolean isCeleb;

    public TwitterUser() {
        //init weights
        Random rnd = new Random();
        weights = new double[3];
        weights[0] = (double) rnd.nextInt(100);
        weights[1] = rnd.nextInt(100 - (int) weights[0]);
        weights[2] = rnd.nextInt(100 - (int) (weights[0] + weights[1]));

        weights[0] = weights[0] / 100.0d;
        weights[1] = weights[1] / 100.0d;
        weights[2] = weights[2] / 100.0d;
    }

    public void setStatusRate(double StatusRate) {
        this.StatusRate = StatusRate;
    }

    public List<TwitterUser> getCelebs() {
        if (celebs.size() > 0) {
            return celebs;
        }

        for (TwitterUser u : getFollowings()) {
            if (u.isCeleb()) {
                celebs.add(u);
            }
        }

        return celebs;
    }

    public List<TwitterUser> getNonCelebs() {
        if (noncelebs.size() > 0) {
            return noncelebs;
        }

        for (TwitterUser u : getFollowings()) {
            if (!u.isCeleb()) {
                noncelebs.add(u);
            }
        }

        return noncelebs;
    }

    public int getFollowerCount() {
        if (followers != null) {
            return followers.size();
        }
        return 0;
    }

    public int getFollowingCount() {
        if (followings != null) {
            return followings.size();
        }
        return 0;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public double[] getWeights() {
        return weights;
    }

    public double getStatusRate() {
        return StatusRate;
    }

    public double getTetha() {
        double result;
        result = getFollowerCount() * (1 + (getStatusRate() * ((getFollowerCount() + getFollowingCount()) / (getFollowerCount()))));
        return result;
    }

    public List<TwitterUser> getFollowings() {
        return followings;
    }

    public List<TwitterUser> getFollowers() {
        return followers;
    }

    public boolean isCeleb() {
        return getFollowerCount() >= Main.celebThreshold;
    }

    @Override
    public String toString() {
        return "Celeb: " + isCeleb() + " - Fl: " + getFollowers().size() + " - Fg: " + getFollowings().size();
    }
}
