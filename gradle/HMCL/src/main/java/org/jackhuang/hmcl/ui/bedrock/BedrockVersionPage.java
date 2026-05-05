// HMCL/src/main/java/org/jackhuang/hmcl/ui/bedrock/BedrockVersionPage.java
package org.jackhuang.hmcl.ui.bedrock;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.jackhuang.hmcl.bedrock.BedrockLauncher;
import org.jackhuang.hmcl.bedrock.BedrockVersion;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;

import java.nio.file.Path;
import java.util.List;

public class BedrockVersionPage extends VBox {
    
    private final BedrockLauncher bedrockLauncher;
    private Button launchButton;
    private Label statusLabel;
    private Label pathLabel;
    
    public BedrockVersionPage() {
        bedrockLauncher = new BedrockLauncher();
        
        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.CENTER);
        
        // 标题
        Label title = new Label("🎮 基岩版");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4caf50;");
        
        // 状态信息
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa;");
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);
        
        // 路径信息
        pathLabel = new Label();
        pathLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        pathLabel.setWrapText(true);
        
        // 启动按钮
        launchButton = FXUtils.newRaisedButton("🚀 启动基岩版");
        launchButton.setPrefWidth(250);
        launchButton.setPrefHeight(50);
        launchButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // 刷新按钮
        Button refreshBtn = FXUtils.newRaisedButton("🔄 刷新检测");
        refreshBtn.setOnAction(e -> checkAndUpdate());
        
        getChildren().addAll(title, statusLabel, pathLabel, launchButton, refreshBtn);
        
        checkAndUpdate();
    }
    
    private void checkAndUpdate() {
        statusLabel.setText("🔍 正在扫描所有磁盘查找基岩版...");
        pathLabel.setText("");
        launchButton.setDisable(true);
        
        new Thread(() -> {
            boolean installed = bedrockLauncher.isInstalled();
            
            javafx.application.Platform.runLater(() -> {
                if (installed) {
                    String installPath = bedrockLauncher.getInstallPath() != null ? 
                        bedrockLauncher.getInstallPath().toString() : "未知";
                    statusLabel.setText("✅ 已检测到基岩版安装");
                    pathLabel.setText("📁 路径: " + installPath);
                    launchButton.setDisable(false);
                    launchButton.setOnAction(e -> launch());
                } else {
                    statusLabel.setText("❌ 未检测到基岩版\n\n请确保已从 Microsoft Store 或 Xbox 应用\n安装 Minecraft for Windows\n\n启动器将自动扫描所有磁盘分区\n(C:\\XboxGames\\, D:\\XboxGames\\, 等)");
                    pathLabel.setText("");
                    launchButton.setDisable(true);
                }
            });
        }).start();
    }
    
    private void launch() {
        launchButton.setDisable(true);
        statusLabel.setText("🚀 正在启动基岩版...");
        
        bedrockLauncher.launch().thenAccept(success -> {
            javafx.application.Platform.runLater(() -> {
                launchButton.setDisable(false);
                if (success) {
                    statusLabel.setText("✅ 基岩版已启动");
                } else {
                    statusLabel.setText("❌ 启动失败，请检查安装");
                }
            });
        });
    }
    
    public void refresh() {
        checkAndUpdate();
    }
}