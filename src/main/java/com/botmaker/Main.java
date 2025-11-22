package com.botmaker;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.di.DependencyContainer;
import com.botmaker.init.AppDependencyConfigurator;
import com.botmaker.init.AppServiceInitializer;
import com.botmaker.project.ProjectConfig;
import com.botmaker.services.CodeEditorService;
import com.botmaker.services.LanguageServerService;
import com.botmaker.ui.ProjectSelectionScreen;
import com.botmaker.ui.UIManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends Application {

    private DependencyContainer container;
    private LanguageServerService languageServerService;

    @Override
    public void start(Stage primaryStage) {
        String lastProject = ProjectConfig.getLastOpened();

        // Check if last project exists and auto-load
        if (lastProject != null && projectExists(lastProject)) {
            System.out.println("Auto-loading last project: " + lastProject);
            openProject(primaryStage, lastProject, false);
        } else {
            showProjectSelection(primaryStage);
        }
    }

    private void showProjectSelection(Stage primaryStage) {
        ProjectSelectionScreen selectionScreen = new ProjectSelectionScreen(
                primaryStage,
                (projectName, clearCache) -> openProject(primaryStage, projectName, clearCache)
        );

        primaryStage.setScene(selectionScreen.createScene());
        primaryStage.setTitle("BotMaker - Select Project");
        primaryStage.show();
    }

    private void openProject(Stage primaryStage, String projectName, boolean clearCache) {
        try {
            ProjectConfig.updateLastOpened(projectName);
            ApplicationConfig config = ApplicationConfig.forProject(projectName);

            // 1. Configure Container
            container = new DependencyContainer();
            AppDependencyConfigurator.configure(container, config, primaryStage);

            // 2. Handle Cache Flag
            if (clearCache) {
                container.resolve(LanguageServerService.class).setShouldClearCache(true);
            }

            // 3. Initialize Services & Wiring
            AppServiceInitializer.initialize(container);

            // 4. Keep reference for shutdown
            this.languageServerService = container.resolve(LanguageServerService.class);

            // 5. Setup UI
            UIManager uiManager = container.resolve(UIManager.class);
            uiManager.setOnSelectProject(v -> shutdownAndShowSelector(primaryStage));

            primaryStage.setScene(uiManager.createScene());
            primaryStage.setTitle("BotMaker Blocks - " + projectName);

            // 6. Load Code
            container.resolve(CodeEditorService.class).loadInitialCode();

            primaryStage.setOnCloseRequest(e -> {
                e.consume();
                performShutdown();
            });

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error opening project: " + e.getMessage());
            showProjectSelection(primaryStage);
        }
    }

    private void shutdownAndShowSelector(Stage primaryStage) {
        // Non-terminal shutdown (just stop LSP, keep JVM alive)
        if (languageServerService != null) {
            languageServerService.shutdown();
            languageServerService = null;
        }
        showProjectSelection(primaryStage);
    }

    private void performShutdown() {
        new Thread(() -> {
            try {
                if (languageServerService != null) {
                    System.out.println("Shutting down Language Server...");
                    languageServerService.shutdown();
                }
            } catch (Exception ex) {
                System.err.println("Error during shutdown: " + ex.getMessage());
            } finally {
                Platform.runLater(() -> {
                    Platform.exit();
                    System.exit(0);
                });
            }
        }).start();
    }

    private boolean projectExists(String projectName) {
        return Files.exists(Paths.get("projects", projectName)) &&
                Files.exists(Paths.get("projects", projectName, "build.gradle"));
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to open project");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}