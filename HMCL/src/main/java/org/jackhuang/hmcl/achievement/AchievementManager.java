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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jackhuang.hmcl.Metadata;
import org.jackhuang.hmcl.util.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class AchievementManager {
    private static final AchievementManager INSTANCE = new AchievementManager();
    private final Map<String, Achievement> achievements = new LinkedHashMap<>();
    private final List<Runnable> onUnlockListeners = new ArrayList<>();
    private Path savePath;

    private AchievementManager() {}

    public static AchievementManager getInstance() { return INSTANCE; }

    public void init() {
        savePath = Paths.get(Metadata.HMCL_CURRENT_DIRECTORY.toString(), "achievements.json");
        registerDefaultAchievements();
        loadAchievements();
    }

    private void registerDefaultAchievements() {
        register(new Achievement("first_launch", "初来乍到", "首次成功启动游戏", "🎮", 1));
        register(new Achievement("java_master", "Java 大师", "启动 Java 版 10 次", "☕", 10));
        register(new Achievement("java_expert", "Java 专家", "启动 Java 版 50 次", "🏆", 50));
        register(new Achievement("bedrock_explorer", "基岩探险家", "首次启动基岩版", "🧱", 1));
        register(new Achievement("version_collector", "版本收藏家", "安装过 5 个不同版本", "📦", 5));
        register(new Achievement("day_1", "忠实玩家", "累计游玩 7 天", "📅", 7));
        register(new Achievement("mod_lover", "模组爱好者", "安装过 10 个模组", "🔧", 10));
    }

    private void register(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }

    public Achievement getAchievement(String id) {
        return achievements.get(id);
    }

    public Collection<Achievement> getAllAchievements() {
        return achievements.values();
    }

    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> list = new ArrayList<>();
        for (Achievement a : achievements.values()) {
            if (a.isUnlocked()) list.add(a);
        }
        list.sort(Comparator.comparingLong(Achievement::getUnlockTime));
        return list;
    }

    public List<Achievement> getLockedAchievements() {
        List<Achievement> list = new ArrayList<>();
        for (Achievement a : achievements.values()) {
            if (!a.isUnlocked()) list.add(a);
        }
        return list;
    }

    public void addProgress(String id, int delta) {
        Achievement achievement = achievements.get(id);
        if (achievement != null && !achievement.isUnlocked()) {
            achievement.addProgress(delta);
            if (achievement.isUnlocked()) {
                notifyUnlock(achievement);
            }
            saveAchievements();
        }
    }

    public void setProgress(String id, int value) {
        Achievement achievement = achievements.get(id);
        if (achievement != null && !achievement.isUnlocked()) {
            achievement.setProgress(value);
            if (achievement.isUnlocked()) {
                notifyUnlock(achievement);
            }
            saveAchievements();
        }
    }

    private void notifyUnlock(Achievement achievement) {
        Logger.LOG.info("Achievement unlocked: " + achievement.getName());
        for (Runnable listener : onUnlockListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                Logger.LOG.warning("Error in unlock listener: " + e.getMessage());
            }
        }
    }

    public void addUnlockListener(Runnable listener) {
        onUnlockListeners.add(listener);
    }

    public void saveAchievements() {
        if (savePath == null) return;
        try {
            Map<String, Map<String, Object>> saveData = new LinkedHashMap<>();
            for (Achievement achievement : achievements.values()) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("progress", achievement.getProgress());
                data.put("unlocked", achievement.isUnlocked());
                data.put("unlockTime", achievement.getUnlockTime());
                saveData.put(achievement.getId(), data);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(saveData);
            Files.writeString(savePath, json);
        } catch (IOException e) {
            Logger.LOG.warning("Failed to save achievements: " + e.getMessage());
        }
    }

    private void loadAchievements() {
        if (savePath == null || !Files.exists(savePath)) return;
        try {
            String json = Files.readString(savePath);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> saveData = gson.fromJson(json, type);
            for (Map.Entry<String, Map<String, Object>> entry : saveData.entrySet()) {
                Achievement achievement = achievements.get(entry.getKey());
                if (achievement != null) {
                    Map<String, Object> data = entry.getValue();
                    int progress = ((Double) data.get("progress")).intValue();
                    boolean unlocked = (boolean) data.get("unlocked");
                    if (unlocked) {
                        achievement.setProgress(achievement.getTarget());
                        if (data.containsKey("unlockTime")) {
                            long time = ((Double) data.get("unlockTime")).longValue();
                            achievement.setUnlockTime(time);
                        }
                    } else {
                        achievement.setProgress(progress);
                    }
                }
            }
        } catch (Exception e) {
            Logger.LOG.warning("Failed to load achievements: " + e.getMessage());
        }
    }
}