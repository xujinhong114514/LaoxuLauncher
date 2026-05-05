/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2021  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.jackhuang.hmcl.ui.main;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;

import org.jackhuang.hmcl.Metadata;
import org.jackhuang.hmcl.bedrock.BedrockLauncher;
import org.jackhuang.hmcl.bedrock.BedrockVersion;
import org.jackhuang.hmcl.download.DefaultDependencyManager;
import org.jackhuang.hmcl.download.DownloadProvider;
import static org.jackhuang.hmcl.download.RemoteVersion.Type.RELEASE;
import org.jackhuang.hmcl.download.VersionList;
import org.jackhuang.hmcl.game.LauncherHelper;
import org.jackhuang.hmcl.game.Version;
import static org.jackhuang.hmcl.setting.ConfigHolder.config;
import org.jackhuang.hmcl.setting.DownloadProviders;
import org.jackhuang.hmcl.setting.Profile;
import org.jackhuang.hmcl.setting.Profiles;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.theme.Themes;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import static org.jackhuang.hmcl.ui.FXUtils.SINE;
import org.jackhuang.hmcl.ui.SVG;
import org.jackhuang.hmcl.ui.animation.AnimationUtils;
import org.jackhuang.hmcl.ui.animation.ContainerAnimations;
import org.jackhuang.hmcl.ui.animation.TransitionPane;
import org.jackhuang.hmcl.ui.bedrock.BedrockVersionPage;
import org.jackhuang.hmcl.ui.construct.MessageDialogPane;
import org.jackhuang.hmcl.ui.construct.TwoLineListItem;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jackhuang.hmcl.ui.versions.GameListPopupMenu;
import org.jackhuang.hmcl.ui.versions.Versions;
import org.jackhuang.hmcl.upgrade.RemoteVersion;
import org.jackhuang.hmcl.upgrade.UpdateChecker;
import org.jackhuang.hmcl.upgrade.UpdateHandler;
import org.jackhuang.hmcl.util.Holder;
import org.jackhuang.hmcl.util.Lang;
import org.jackhuang.hmcl.util.NativePatcher;
import org.jackhuang.hmcl.util.StringUtils;
import org.jackhuang.hmcl.util.TaskCancellationAction;
import org.jackhuang.hmcl.util.i18n.I18n;
import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import org.jackhuang.hmcl.util.javafx.BindingMapping;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;
import org.jackhuang.hmcl.util.platform.OperatingSystem;
import org.jackhuang.hmcl.util.platform.Platform;
import org.jackhuang.hmcl.util.versioning.GameVersionNumber;
import org.jackhuang.hmcl.game.LauncherHelper;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import org.jackhuang.hmcl.ui.achievement.AchievementPage;

public final class MainPage extends StackPane implements DecoratorPage {
    private static final String ANNOUNCEMENT = "announcement";

    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>();

    private final StringProperty currentGame = new SimpleStringProperty(this, "currentGame");
    private final BooleanProperty showUpdate = new SimpleBooleanProperty(this, "showUpdate");
    private final ObjectProperty<RemoteVersion> latestVersion = new SimpleObjectProperty<>(this, "latestVersion");
    private final ObservableList<Version> versions = FXCollections.observableArrayList();
    private Profile profile;
    
    // ==================== 新增：基岩版相关代码 ====================
    private enum GameType { JAVA, BEDROCK }
    private GameType currentGameType = GameType.JAVA;
    private final BedrockLauncher bedrockLauncher = new BedrockLauncher();
    private final ObservableList<BedrockVersion> bedrockVersions = FXCollections.observableArrayList();
    private JFXButton javaButton;
    private JFXButton bedrockButton;
    private BedrockVersionPage bedrockVersionPage;
    private final StringProperty currentBedrockVersion = new SimpleStringProperty(this, "currentBedrockVersion");
    // ==================== 基岩版代码结束 ====================

    private TransitionPane announcementPane;
    private final StackPane updatePane;
    private final JFXButton menuButton;
    
    // 信息卡片控件
    private VBox infoCard;
    private Label versionValueLabel;
    private Label timeValueLabel;

    {
        // ========== 1. 创建标题栏和 titleNode ==========
        HBox titleNode = new HBox(8);
        titleNode.setPadding(new Insets(0, 0, 0, 2));
        titleNode.setAlignment(Pos.CENTER_LEFT);

        ImageView titleIcon = new ImageView(FXUtils.newBuiltinImage("/assets/img/icon-title.png"));
        Label titleLabel = new Label(Metadata.FULL_TITLE);
        if (I18n.isUpsideDown()) {
            titleIcon.setRotate(180);
            titleLabel.setRotate(180);
        }
        titleLabel.getStyleClass().add("jfx-decorator-title");
        titleLabel.textFillProperty().bind(Themes.titleFillProperty());
        
        // 创建游戏类型选择器
        HBox gameTypeSelector = createGameTypeSelector();
        
        HBox titleContainer = new HBox(20);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        titleContainer.getChildren().addAll(titleNode, gameTypeSelector);
        
        // ========== 成就按钮 ==========
        JFXButton achievementsBtn = new JFXButton("🏆");
        achievementsBtn.getStyleClass().add("title-button");
        achievementsBtn.setTooltip(new Tooltip(i18n("achievement.title")));
        achievementsBtn.setOnAction(e -> {
            Controllers.navigate(new AchievementPage());
        });
        titleContainer.getChildren().add(achievementsBtn);
        
        // 添加弹簧让按钮靠右
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleContainer.getChildren().add(spacer);
        // ========== 成就按钮结束 ==========
        
        state.setValue(new State(null, titleContainer, false, false, true));
        
        setPadding(new Insets(20));

        // ========== 2. 公告面板 ==========
        if (Metadata.isNightly() || (Metadata.isDev() && !Objects.equals(Metadata.VERSION, config().getShownTips().get(ANNOUNCEMENT)))) {
            String title;
            String content;
            if (Metadata.isNightly()) {
                title = i18n("update.channel.nightly.title");
                content = i18n("update.channel.nightly.hint");
            } else {
                title = i18n("update.channel.dev.title");
                content = i18n("update.channel.dev.hint");
            }

            VBox announcementCard = new VBox();

            BorderPane titleBar = new BorderPane();
            titleBar.getStyleClass().add("title");
            titleBar.setLeft(new Label(title));

            JFXButton btnHide = new JFXButton();
            btnHide.setOnAction(e -> {
                announcementPane.setContent(new StackPane(), ContainerAnimations.FADE);
                if (Metadata.isDev()) {
                    config().getShownTips().put(ANNOUNCEMENT, Metadata.VERSION);
                }
            });
            btnHide.getStyleClass().add("announcement-close-button");
            btnHide.setGraphic(SVG.CLOSE.createIcon(20));
            titleBar.setRight(btnHide);

            TextFlow body = FXUtils.segmentToTextFlow(content, Controllers::onHyperlinkAction);
            body.setLineSpacing(4);

            announcementCard.getChildren().setAll(titleBar, body);
            announcementCard.setSpacing(16);
            announcementCard.getStyleClass().addAll("card", "announcement");

            VBox announcementBox = new VBox(16);
            announcementBox.setPadding(new Insets(15));
            announcementBox.getChildren().add(announcementCard);

            announcementPane = new TransitionPane();
            announcementPane.setContent(announcementBox, ContainerAnimations.NONE);

            StackPane.setMargin(announcementPane, new Insets(-15));
            getChildren().add(announcementPane);
        }

        // ========== 3. 更新面板 ==========
        updatePane = new StackPane();
        updatePane.setVisible(false);
        updatePane.getStyleClass().add("bubble");
        FXUtils.setLimitWidth(updatePane, 230);
        FXUtils.setLimitHeight(updatePane, 55);
        StackPane.setAlignment(updatePane, Pos.TOP_RIGHT);
        FXUtils.onClicked(updatePane, this::onUpgrade);
        FXUtils.onChange(showUpdateProperty(), this::showUpdate);

        {
            HBox hBox = new HBox();
            hBox.setSpacing(12);
            hBox.setAlignment(Pos.CENTER_LEFT);
            StackPane.setAlignment(hBox, Pos.CENTER_LEFT);
            StackPane.setMargin(hBox, new Insets(9, 12, 9, 16));
            {
                TwoLineListItem prompt = new TwoLineListItem();
                prompt.setSubtitle(i18n("update.bubble.subtitle"));
                prompt.setPickOnBounds(false);
                prompt.titleProperty().bind(BindingMapping.of(latestVersionProperty()).map(latestVersion ->
                        latestVersion == null ? "" : i18n("update.bubble.title", latestVersion.getVersion())));

                hBox.getChildren().setAll(SVG.UPDATE.createIcon(20), prompt);
            }

            JFXButton closeUpdateButton = new JFXButton();
            closeUpdateButton.setGraphic(SVG.CLOSE.createIcon(10));
            StackPane.setAlignment(closeUpdateButton, Pos.TOP_RIGHT);
            closeUpdateButton.getStyleClass().add("toggle-icon-tiny");
            StackPane.setMargin(closeUpdateButton, new Insets(5));
            closeUpdateButton.setOnAction(e -> closeUpdateBubble());

            updatePane.getChildren().setAll(hBox, closeUpdateButton);
        }

        // ========== 4. 启动面板 ==========
        HBox launchPane = new HBox();
        launchPane.getStyleClass().add("launch-pane");
        
        // 滚动切换逻辑
        FXUtils.onScroll(launchPane, versions, list -> {
            if (currentGameType == GameType.JAVA) {
                String currentId = getCurrentGame();
                return Lang.indexWhere(list, instance -> instance.getId().equals(currentId));
            }
            return -1;
        }, it -> {
            if (currentGameType == GameType.JAVA && it instanceof Version) {
                profile.setSelectedVersion(((Version) it).getId());
            }
        });
        
        StackPane.setAlignment(launchPane, Pos.BOTTOM_RIGHT);
        {
            JFXButton launchButton = new JFXButton();
            launchButton.getStyleClass().add("launch-button");
            launchButton.setDefaultButton(true);
            {
                VBox graphic = new VBox();
                graphic.setAlignment(Pos.CENTER);
                Label launchLabel = new Label();
                launchLabel.setStyle("-fx-font-size: 16px;");
                Label currentLabel = new Label();
                currentLabel.setStyle("-fx-font-size: 12px;");

                Runnable updateLaunchInfo = () -> {
                    if (currentGameType == GameType.JAVA) {
                        String currentGameId = getCurrentGame();
                        if (currentGameId == null || currentGameId.isEmpty()) {
                            launchLabel.setText(i18n("version.launch.empty"));
                            currentLabel.setText(null);
                            graphic.getChildren().setAll(launchLabel);
                        } else {
                            launchLabel.setText(i18n("version.launch"));
                            currentLabel.setText(currentGameId);
                            graphic.getChildren().setAll(launchLabel, currentLabel);
                        }
                    } else {
                        String currentBedrock = getCurrentBedrockVersion();
                        if (currentBedrock == null || currentBedrock.isEmpty()) {
                            launchLabel.setText(i18n("bedrock.launch.empty"));
                            currentLabel.setText(null);
                            graphic.getChildren().setAll(launchLabel);
                        } else {
                            launchLabel.setText(i18n("bedrock.launch"));
                            currentLabel.setText(currentBedrock);
                            graphic.getChildren().setAll(launchLabel, currentLabel);
                        }
                    }
                };
                
                FXUtils.onChange(currentGameProperty(), o -> updateLaunchInfo.run());
                FXUtils.onChange(currentBedrockVersionProperty(), o -> updateLaunchInfo.run());
                updateLaunchInfo.run();

                launchButton.setGraphic(graphic);
            }

            menuButton = new JFXButton();
            menuButton.getStyleClass().add("menu-button");
            
            menuButton.setOnAction(e -> {
                if (currentGameType == GameType.JAVA) {
                    GameListPopupMenu.show(
                        menuButton,
                        JFXPopup.PopupVPosition.BOTTOM,
                        JFXPopup.PopupHPosition.RIGHT,
                        0,
                        -menuButton.getHeight(),
                        profile, versions
                    );
                } else {
                    showBedrockPopupMenu();
                }
            });
            
            FXUtils.installFastTooltip(menuButton, i18n("version.switch"));
            menuButton.setGraphic(SVG.ARROW_DROP_UP.createIcon(30));

            EventHandler<MouseEvent> secondaryClickHandle = event -> {
                if (event.getButton() == MouseButton.SECONDARY && event.getClickCount() == 1) {
                    menuButton.fire();
                    event.consume();
                }
            };
            launchButton.addEventHandler(MouseEvent.MOUSE_CLICKED, secondaryClickHandle);
            menuButton.addEventHandler(MouseEvent.MOUSE_CLICKED, secondaryClickHandle);

            launchPane.getChildren().setAll(launchButton, menuButton);
        }

        getChildren().addAll(updatePane, launchPane);
        
        // ========== 信息卡片（小卡片，左下角） ==========
        infoCard = createInfoCard();
        StackPane.setAlignment(infoCard, Pos.BOTTOM_LEFT);
        StackPane.setMargin(infoCard, new Insets(0, 0, 100, 15));
        getChildren().add(infoCard);
        // ========== 信息卡片结束 ==========
        
        // 初始化基岩版
        initBedrock();
    }

    // ==================== 基岩版相关方法 ====================
    
    private HBox createGameTypeSelector() {
        HBox selector = new HBox(8);
        selector.setAlignment(Pos.CENTER_LEFT);
        
        javaButton = new JFXButton(i18n("game.java"));
        javaButton.getStyleClass().add("game-type-button");
        javaButton.setGraphic(createGameTypeIcon("/assets/img/grass.png", 18));
        javaButton.setOnAction(e -> switchToJava());
        
        bedrockButton = new JFXButton(i18n("game.bedrock"));
        bedrockButton.getStyleClass().add("game-type-button");
        bedrockButton.setGraphic(createGameTypeIcon("/assets/img/chest.png", 18));
        bedrockButton.setOnAction(e -> switchToBedrock());
        
        updateGameTypeButtonStyle();
        
        selector.getChildren().addAll(javaButton, bedrockButton);
        return selector;
    }
    
    private ImageView createGameTypeIcon(String path, double size) {
        try {
            ImageView icon = new ImageView(FXUtils.newBuiltinImage(path));
            icon.setFitHeight(size);
            icon.setFitWidth(size);
            icon.setPreserveRatio(true);
            return icon;
        } catch (Exception e) {
            return new ImageView();
        }
    }
    
    private void updateGameTypeButtonStyle() {
        if (currentGameType == GameType.JAVA) {
            javaButton.getStyleClass().add("active");
            bedrockButton.getStyleClass().remove("active");
        } else {
            bedrockButton.getStyleClass().add("active");
            javaButton.getStyleClass().remove("active");
        }
    }
    
    private void switchToJava() {
        if (currentGameType == GameType.JAVA) return;
        
        currentGameType = GameType.JAVA;
        updateGameTypeButtonStyle();
        
        if (bedrockVersionPage != null) {
            bedrockVersionPage.setVisible(false);
        }
        
        // 刷新信息卡片
        refreshInfoCard();
        
        Controllers.showToast(i18n("status.java_mode"));
    }
    
    private void switchToBedrock() {
        if (currentGameType == GameType.BEDROCK) return;
        
        if (!bedrockLauncher.isInstalled()) {
            Controllers.dialog(
                i18n("bedrock.not_installed.message"),
                i18n("message.info"),
                MessageDialogPane.MessageType.WARNING
            );
            return;
        }
        
        currentGameType = GameType.BEDROCK;
        updateGameTypeButtonStyle();
        
        if (bedrockVersionPage != null) {
            bedrockVersionPage.setVisible(true);
            bedrockVersionPage.refresh();
        }
        
        // 刷新信息卡片
        refreshInfoCard();
        
        Controllers.showToast(i18n("status.bedrock_mode"));
    }
    
    private void initBedrock() {
        bedrockVersionPage = new BedrockVersionPage();
        bedrockVersionPage.setVisible(false);
        getChildren().add(bedrockVersionPage);
        StackPane.setAlignment(bedrockVersionPage, Pos.CENTER);
        StackPane.setMargin(bedrockVersionPage, new Insets(50, 20, 100, 20));
        
        bedrockVersions.addListener((javafx.collections.ListChangeListener<BedrockVersion>) c -> {
            if (!bedrockVersions.isEmpty() && getCurrentBedrockVersion() == null) {
                setCurrentBedrockVersion(bedrockVersions.get(0).getVersion());
            }
        });
        
        loadBedrockVersions();
    }
    
    private void loadBedrockVersions() {
        new Thread(() -> {
            List<BedrockVersion> versions = bedrockLauncher.getInstalledVersions();
            javafx.application.Platform.runLater(() -> {
                bedrockVersions.setAll(versions);
                if (!versions.isEmpty()) {
                    setCurrentBedrockVersion(versions.get(0).getVersion());
                }
            });
        }).start();
    }
    
    private void showBedrockPopupMenu() {
        JFXPopup popup = new JFXPopup();
        VBox menu = new VBox();
        menu.getStyleClass().add("popup-menu");
        
        for (BedrockVersion version : bedrockVersions) {
            JFXButton item = new JFXButton(version.getVersion());
            item.getStyleClass().add("popup-menu-item");
            item.setOnAction(e -> {
                setCurrentBedrockVersion(version.getVersion());
                popup.hide();
            });
            menu.getChildren().add(item);
        }
        
        popup.setPopupContent(menu);
        popup.show(menuButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, 0, -menuButton.getHeight());
    }
    
    private void launchBedrock() {
        String currentVersion = getCurrentBedrockVersion();
        if (currentVersion == null || currentVersion.isEmpty()) {
            Controllers.showToast(i18n("bedrock.no_version_selected"));
            return;
        }
        
        bedrockLauncher.launch().thenAccept(success -> {
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    // 记录上次游玩
                    recordLastPlay(currentVersion);
                } else {
                    Controllers.dialog(
                        i18n("bedrock.launch.failed.message"),
                        i18n("message.error"),
                        MessageDialogPane.MessageType.ERROR
                    );
                }
            });
        });
    }
    
    public String getCurrentBedrockVersion() {
        return currentBedrockVersion.get();
    }
    
    public void setCurrentBedrockVersion(String version) {
        this.currentBedrockVersion.set(version);
    }
    
    public GameType getCurrentGameType() {
        return currentGameType;
    }
    
    public StringProperty currentBedrockVersionProperty() {
        return currentBedrockVersion;
    }
    
    // ==================== 信息卡片方法（小卡片） ====================
    
    private VBox createInfoCard() {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 15, 8, 15));
        card.setStyle(
            "-fx-background-color: rgba(30, 35, 50, 0.95); " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: rgba(255,255,255,0.15); " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        card.setMaxWidth(280);
        
        // 当前版本行
        HBox versionRow = new HBox(8);
        versionRow.setAlignment(Pos.CENTER_LEFT);
        Label versionIcon = new Label("📌");
        versionIcon.setStyle("-fx-font-size: 12px;");
        Label versionLabel = new Label(i18n("info.current_version"));
        versionLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        versionValueLabel = new Label();
        versionValueLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold; -fx-font-size: 11px;");
        versionRow.getChildren().addAll(versionIcon, versionLabel, versionValueLabel);
        
        // 上次游玩时间行
        HBox timeRow = new HBox(8);
        timeRow.setAlignment(Pos.CENTER_LEFT);
        Label timeIcon = new Label("🕐");
        timeIcon.setStyle("-fx-font-size: 12px;");
        Label timeLabel = new Label(i18n("info.last_play_time"));
        timeLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        timeValueLabel = new Label();
        timeValueLabel.setStyle("-fx-text-fill: #eee; -fx-font-size: 10px;");
        timeRow.getChildren().addAll(timeIcon, timeLabel, timeValueLabel);
        
        card.getChildren().addAll(versionRow, timeRow);
        
        // 绑定数据更新
        updateInfoCard();
        
        // 监听变化
        currentGameProperty().addListener((obs, old, val) -> updateInfoCard());
        currentBedrockVersionProperty().addListener((obs, old, val) -> updateInfoCard());
        
        return card;
    }
    
    private void updateInfoCard() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainPage.class);
        String lastVersion = prefs.get("last_version", "");
        long lastTime = prefs.getLong("last_play_time", 0);
        
        // 显示当前版本（根据当前游戏类型）
        if (currentGameType == GameType.JAVA) {
            String current = getCurrentGame();
            versionValueLabel.setText(current != null && !current.isEmpty() ? current : i18n("info.no_version"));
        } else {
            String current = getCurrentBedrockVersion();
            versionValueLabel.setText(current != null && !current.isEmpty() ? current : i18n("info.no_bedrock"));
        }
        
        // 显示上次游玩时间
        if (lastTime == 0) {
            timeValueLabel.setText(i18n("info.no_record"));
        } else {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            timeValueLabel.setText(sdf.format(new java.util.Date(lastTime)) + "  " + lastVersion);
        }
    }
    
    private void refreshInfoCard() {
        if (infoCard != null) {
            updateInfoCard();
        }
    }
    
    // 记录上次游玩（线程安全）
    private void recordLastPlay(String version) {
        new Thread(() -> {
            try {
                java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainPage.class);
                prefs.put("last_version", version);
                prefs.putLong("last_play_time", System.currentTimeMillis());
                javafx.application.Platform.runLater(this::refreshInfoCard);
            } catch (Exception e) {
                LOG.warning("Failed to record last play info: " + e.getMessage());
            }
        }).start();
    }
    
    // ==================== 原有方法 ====================

    private void showUpdate(boolean show) {
        doAnimation(show);

        if (show && !config().isDisableAutoShowUpdateDialog()
                && getLatestVersion() != null
                && !Objects.equals(config().getPromptedVersion(), getLatestVersion().getVersion())) {
            Controllers.dialog(new MessageDialogPane.Builder("", i18n("update.bubble.title", getLatestVersion().getVersion()), MessageDialogPane.MessageType.INFO)
                    .addAction(i18n("button.view"), () -> {
                        config().setPromptedVersion(getLatestVersion().getVersion());
                        onUpgrade();
                    })
                    .addCancel(null)
                    .build());
        }
    }

    private void doAnimation(boolean show) {
        if (AnimationUtils.isAnimationEnabled()) {
            Duration duration = Duration.millis(320);
            Timeline nowAnimation = new Timeline();
            nowAnimation.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(updatePane.translateXProperty(), show ? 260 : 0, SINE)),
                    new KeyFrame(duration,
                            new KeyValue(updatePane.translateXProperty(), show ? 0 : 260, SINE)));
            if (show) nowAnimation.getKeyFrames().add(
                    new KeyFrame(Duration.ZERO, e -> updatePane.setVisible(true)));
            else nowAnimation.getKeyFrames().add(
                    new KeyFrame(duration, e -> updatePane.setVisible(false)));
            nowAnimation.play();
        } else {
            updatePane.setVisible(show);
        }
    }

    private void launch() {
        if (currentGameType == GameType.JAVA) {
            launchJava();
        } else {
            launchBedrock();
        }
    }
    
    private void launchJava() {
        Profile profile = Profiles.getSelectedProfile();
        String version = profile.getSelectedVersion();
        
        if (version == null || version.isEmpty()) {
            Controllers.showToast(i18n("version.launch.empty"));
            return;
        }
        
        // 使用正确的启动方法（带回调）
        Versions.launch(profile, version, LauncherHelper::setKeep);
        
        // 记录游玩信息
        recordLastPlay(version);
    }
    
    private void launchNoGame() {
        if (currentGameType == GameType.JAVA) {
            launchNoGameJava();
        } else {
            Controllers.dialog(i18n("bedrock.no_version"), i18n("message.info"), MessageDialogPane.MessageType.WARNING);
        }
    }
    
    private void launchNoGameJava() {
        DownloadProvider downloadProvider = DownloadProviders.getDownloadProvider();
        VersionList<?> versionList = downloadProvider.getVersionListById("game");

        Holder<String> gameVersionHolder = new Holder<>();
        Task<?> task = versionList.refreshAsync("")
                .thenSupplyAsync(() -> versionList.getVersions("").stream()
                        .filter(it -> it.getVersionType() == RELEASE)
                        .filter(it -> NativePatcher.checkSupportedStatus(GameVersionNumber.asGameVersion(it.getGameVersion()), Platform.SYSTEM_PLATFORM, OperatingSystem.SYSTEM_VERSION) != NativePatcher.SupportStatus.UNSUPPORTED)
                        .sorted()
                        .findFirst()
                        .orElseThrow(() -> new IOException("No versions found")))
                .thenComposeAsync(version -> {
                    Profile profile = Profiles.getSelectedProfile();
                    DefaultDependencyManager dependency = profile.getDependency();
                    String gameVersion = gameVersionHolder.value = version.getGameVersion();

                    return dependency.gameBuilder()
                            .name(gameVersion)
                            .gameVersion(gameVersion)
                            .buildAsync();
                })
                .whenComplete(any -> profile.getRepository().refreshVersions())
                .whenComplete(Schedulers.javafx(), (result, exception) -> {
                    if (exception == null) {
                        profile.setSelectedVersion(gameVersionHolder.value);
                        launch();
                    } else if (exception instanceof CancellationException) {
                        Controllers.showToast(i18n("message.cancelled"));
                    } else {
                        LOG.warning("Failed to install game", exception);
                        Controllers.dialog(StringUtils.getStackTrace(exception),
                                i18n("install.failed"),
                                MessageDialogPane.MessageType.WARNING);
                    }
                });
        Controllers.taskDialog(task, i18n("version.launch.empty.installing"), TaskCancellationAction.NORMAL);
    }

    private void onUpgrade() {
        RemoteVersion target = UpdateChecker.getLatestVersion();
        if (target == null) {
            return;
        }
        UpdateHandler.updateFrom(target);
    }

    private void closeUpdateBubble() {
        showUpdate.unbind();
        showUpdate.set(false);
    }

    @Override
    public ReadOnlyObjectWrapper<State> stateProperty() {
        return state;
    }

    public Profile getProfile() {
        return profile;
    }

    public String getCurrentGame() {
        return currentGame.get();
    }

    public StringProperty currentGameProperty() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame.set(currentGame);
    }

    public ObservableList<Version> getVersions() {
        return versions;
    }

    public boolean isShowUpdate() {
        return showUpdate.get();
    }

    public BooleanProperty showUpdateProperty() {
        return showUpdate;
    }

    public void setShowUpdate(boolean showUpdate) {
        this.showUpdate.set(showUpdate);
    }

    public RemoteVersion getLatestVersion() {
        return latestVersion.get();
    }

    public ObjectProperty<RemoteVersion> latestVersionProperty() {
        return latestVersion;
    }

    public void setLatestVersion(RemoteVersion latestVersion) {
        this.latestVersion.set(latestVersion);
    }

    public void initVersions(Profile profile, List<Version> versions) {
        FXUtils.checkFxUserThread();
        this.profile = profile;
        this.versions.setAll(versions);
    }
}