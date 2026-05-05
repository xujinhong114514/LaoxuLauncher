/*
 * Laoxu Minecraft Launcher
 * Copyright (C) 2026 Laoxu and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package org.jackhuang.hmcl.ui.achievement;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.jackhuang.hmcl.achievement.Achievement;
import org.jackhuang.hmcl.achievement.AchievementManager;
import org.jackhuang.hmcl.ui.construct.ComponentList;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

public class AchievementPage extends StackPane {

    public AchievementPage() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));

        Label title = new Label("🏆 " + i18n("achievement.title"));
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        title.setPadding(new Insets(0, 0, 10, 0));

        // 统计信息
        AchievementManager manager = AchievementManager.getInstance();
        int unlockedCount = manager.getUnlockedAchievements().size();
        int totalCount = manager.getAllAchievements().size();
        Label stats = new Label(i18n("achievement.stats", unlockedCount, totalCount));
        stats.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        stats.setPadding(new Insets(0, 0, 20, 0));

        // 已解锁成就
        ComponentList unlockedList = new ComponentList();
        Label unlockedTitle = new Label("✅ " + i18n("achievement.unlocked"));
        unlockedTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        unlockedTitle.setPadding(new Insets(10, 0, 5, 0));

        for (Achievement achievement : manager.getUnlockedAchievements()) {
            HBox itemBox = createAchievementItem(achievement, true);
            unlockedList.getContent().add(itemBox);
        }

        if (manager.getUnlockedAchievements().isEmpty()) {
            Label empty = new Label(i18n("achievement.no_unlocked"));
            empty.setStyle("-fx-text-fill: #666; -fx-padding: 15;");
            unlockedList.getContent().add(empty);
        }

        // 未解锁成就
        ComponentList lockedList = new ComponentList();
        Label lockedTitle = new Label("🔒 " + i18n("achievement.locked"));
        lockedTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        lockedTitle.setPadding(new Insets(20, 0, 5, 0));

        for (Achievement achievement : manager.getLockedAchievements()) {
            HBox itemBox = createAchievementItem(achievement, false);
            lockedList.getContent().add(itemBox);
        }

        content.getChildren().addAll(title, stats, unlockedTitle, unlockedList, lockedTitle, lockedList);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        getChildren().setAll(scrollPane);
    }

    private HBox createAchievementItem(Achievement achievement, boolean unlocked) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12, 15, 12, 15));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");
        
        // 图标
        Label iconLabel = new Label(achievement.getIcon());
        iconLabel.setStyle("-fx-font-size: 28px;");
        iconLabel.setPrefWidth(50);
        
        // 信息区域
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label nameLabel = new Label(achievement.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #eee;");
        
        Label descLabel = new Label(achievement.getDescription());
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        
        infoBox.getChildren().addAll(nameLabel, descLabel);
        
        // 进度/状态
        Label statusLabel;
        if (unlocked) {
            statusLabel = new Label("✓ " + i18n("achievement.completed"));
            statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
        } else {
            int progress = achievement.getProgress();
            int target = achievement.getTarget();
            String progressText = progress + "/" + target;
            if (target == 1 && progress == 0) {
                progressText = i18n("achievement.not_started");
            }
            statusLabel = new Label(progressText);
            statusLabel.setStyle("-fx-text-fill: #ffaa66; -fx-font-size: 12px;");
        }
        statusLabel.setPrefWidth(80);
        statusLabel.setAlignment(Pos.CENTER_RIGHT);
        
        box.getChildren().addAll(iconLabel, infoBox, statusLabel);
        return box;
    }
}