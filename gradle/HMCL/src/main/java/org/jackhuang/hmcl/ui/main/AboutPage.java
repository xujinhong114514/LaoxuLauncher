/*
 * Laoxu Minecraft Launcher
 * Copyright (C) 2026 Laoxu and contributors
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

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jackhuang.hmcl.Metadata;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.construct.ComponentList;
import org.jackhuang.hmcl.ui.construct.LineButton;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

public final class AboutPage extends StackPane {

    public AboutPage() {
        // ==================== 关于启动器 ====================
        ComponentList about = new ComponentList();
        {
            // 启动器信息
            var launcher = LineButton.createExternalLinkButton(Metadata.PUBLISH_URL);
            launcher.setLargeTitle(true);
            launcher.setLeading(FXUtils.newBuiltinImage("/assets/img/icon.png"));
            launcher.setTitle("Laoxu Minecraft Launcher");
            launcher.setSubtitle(Metadata.VERSION);

            // 作者信息
            var author = LineButton.createExternalLinkButton("https://github.com/xujinhong114514");
            author.setLargeTitle(true);
            author.setLeading(FXUtils.newBuiltinImage("/assets/img/icon.png"));
            author.setTitle("Laoxu");
            author.setSubtitle(i18n("about.author.statement"));

            about.getContent().setAll(launcher, author);
        }

        // ==================== 项目链接 ====================
        ComponentList links = new ComponentList();
        {
            // GitHub 仓库
            var github = LineButton.createExternalLinkButton("https://github.com/xujinhong114514/LaoxuLauncher");
            github.setLargeTitle(true);
            github.setTitle("GitHub");
            github.setSubtitle(i18n("about.github.statement"));

            // GitCode 镜像
            var gitcode = LineButton.createExternalLinkButton("https://gitcode.com/xujinhong114514/LaoxuLauncher");
            gitcode.setLargeTitle(true);
            gitcode.setTitle("GitCode");
            gitcode.setSubtitle(i18n("about.gitcode.statement"));

            links.getContent().setAll(github, gitcode);
        }

        // ==================== 鸣谢 ====================
        ComponentList thanks = new ComponentList();
        {
            // MCAPPX
            var mcappx = LineButton.createExternalLinkButton("https://www.mcappx.com");
            mcappx.setLargeTitle(true);
            mcappx.setTitle("MCAPPX");
            mcappx.setSubtitle(i18n("about.mcappx.statement"));

            // HMCL 致敬
            var hmcl = LineButton.createExternalLinkButton("https://hmcl.huangyuhui.net");
            hmcl.setLargeTitle(true);
            hmcl.setTitle("HMCL");
            hmcl.setSubtitle(i18n("about.hmcl.statement"));

            thanks.getContent().setAll(mcappx, hmcl);
        }

        // ==================== 法律声明 ====================
        ComponentList legal = new ComponentList();
        {
            // 版权
            var copyright = LineButton.createExternalLinkButton("https://github.com/xujinhong114514/LaoxuLauncher");
            copyright.setLargeTitle(true);
            copyright.setTitle(i18n("about.copyright"));
            copyright.setSubtitle(i18n("about.copyright.statement"));

            // 许可证
            var license = LineButton.createExternalLinkButton("https://www.gnu.org/licenses/gpl-3.0.html");
            license.setLargeTitle(true);
            license.setTitle(i18n("about.license"));
            license.setSubtitle(i18n("about.license.statement"));

            legal.getContent().setAll(copyright, license);
        }

        // ==================== 构建页面 ====================
        VBox content = new VBox(16);
        content.setPadding(new Insets(10));
        content.getChildren().setAll(
                ComponentList.createComponentListTitle(i18n("about")),
                about,

                ComponentList.createComponentListTitle(i18n("about.links")),
                links,

                ComponentList.createComponentListTitle(i18n("about.thanks")),
                thanks,

                ComponentList.createComponentListTitle(i18n("about.legal")),
                legal
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        FXUtils.smoothScrolling(scrollPane);
        getChildren().setAll(scrollPane);
    }
}