/*
 * Laoxu Minecraft Launcher
 * Copyright (C) 2026 Laoxu and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package org.jackhuang.hmcl.achievement;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Achievement {
    private final String id;
    private final String name;
    private final String description;
    private final String icon;
    private final int target;
    private final SimpleIntegerProperty progress = new SimpleIntegerProperty(0);
    private final BooleanProperty unlocked = new SimpleBooleanProperty(false);
    private long unlockTime;

    public Achievement(String id, String name, String description, String icon, int target) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.target = target;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public int getTarget() { return target; }
    public int getProgress() { return progress.get(); }
    public SimpleIntegerProperty progressProperty() { return progress; }
    public boolean isUnlocked() { return unlocked.get(); }
    public BooleanProperty unlockedProperty() { return unlocked; }
    public long getUnlockTime() { return unlockTime; }

    public void addProgress(int delta) {
        if (isUnlocked()) return;
        int newProgress = Math.min(progress.get() + delta, target);
        progress.set(newProgress);
        if (newProgress >= target) {
            unlock();
        }
    }

    public void setProgress(int value) {
        if (isUnlocked()) return;
        progress.set(Math.min(value, target));
        if (progress.get() >= target) {
            unlock();
        }
    }

    private void unlock() {
        unlocked.set(true);
        unlockTime = System.currentTimeMillis();
        AchievementManager.getInstance().saveAchievements();
    }

    public void setUnlockTime(long time) {
        this.unlockTime = time;
    }
}