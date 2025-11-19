package com.botmaker.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Manages the application menu bar
 */
public class MenuBarManager {

    private final MenuBar menuBar;
    private final Stage primaryStage;
    private Consumer<Void> onSelectProject;

    public MenuBarManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.menuBar = new MenuBar();
        createMenus();
    }

    /**
     * Creates all menus
     */
    private void createMenus() {
        // File menu
        Menu fileMenu = createFileMenu();

        // Edit menu (placeholder for future)
        Menu editMenu = createEditMenu();

        // View menu (placeholder for future)
        Menu viewMenu = createViewMenu();

        // Help menu
        Menu helpMenu = createHelpMenu();

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);
    }

    /**
     * Creates the File menu
     */
    private Menu createFileMenu() {
        Menu fileMenu = new Menu("File");

        // Select Project
        MenuItem selectProjectItem = new MenuItem("Select Project...");
        selectProjectItem.setAccelerator(new KeyCodeCombination(
                KeyCode.O,
                KeyCombination.CONTROL_DOWN,
                KeyCombination.SHIFT_DOWN
        ));
        selectProjectItem.setOnAction(e -> {
            if (onSelectProject != null) {
                onSelectProject.accept(null);
            }
        });

        // Separator
        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        // Exit
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(
                KeyCode.Q,
                KeyCombination.CONTROL_DOWN
        ));

        // UPDATED: Force system exit
        exitItem.setOnAction(e -> {
            javafx.application.Platform.exit(); // Close JavaFX
            System.exit(0); // Kill JVM (stops LSP, Debugger, etc.)
        });

        fileMenu.getItems().addAll(
                selectProjectItem,
                separator1,
                exitItem
        );

        return fileMenu;
    }

    /**
     * Creates the Edit menu
     */
    private Menu createEditMenu() {
        Menu editMenu = new Menu("Edit");

        // Placeholder items for future implementation
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        undoItem.setDisable(true); // Not implemented yet

        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        redoItem.setDisable(true); // Not implemented yet

        SeparatorMenuItem separator = new SeparatorMenuItem();

        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        cutItem.setDisable(true); // Not implemented yet

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        copyItem.setDisable(true); // Not implemented yet

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        pasteItem.setDisable(true); // Not implemented yet

        editMenu.getItems().addAll(
                undoItem,
                redoItem,
                separator,
                cutItem,
                copyItem,
                pasteItem
        );

        return editMenu;
    }

    /**
     * Creates the View menu
     */
    private Menu createViewMenu() {
        Menu viewMenu = new Menu("View");

        // Placeholder items for future implementation
        MenuItem zoomInItem = new MenuItem("Zoom In");
        zoomInItem.setAccelerator(new KeyCodeCombination(
                KeyCode.PLUS,
                KeyCombination.CONTROL_DOWN
        ));
        zoomInItem.setDisable(true); // Not implemented yet

        MenuItem zoomOutItem = new MenuItem("Zoom Out");
        zoomOutItem.setAccelerator(new KeyCodeCombination(
                KeyCode.MINUS,
                KeyCombination.CONTROL_DOWN
        ));
        zoomOutItem.setDisable(true); // Not implemented yet

        MenuItem resetZoomItem = new MenuItem("Reset Zoom");
        resetZoomItem.setAccelerator(new KeyCodeCombination(
                KeyCode.DIGIT0,
                KeyCombination.CONTROL_DOWN
        ));
        resetZoomItem.setDisable(true); // Not implemented yet

        viewMenu.getItems().addAll(
                zoomInItem,
                zoomOutItem,
                resetZoomItem
        );

        return viewMenu;
    }

    /**
     * Creates the Help menu
     */
    private Menu createHelpMenu() {
        Menu helpMenu = new Menu("Help");

        MenuItem aboutItem = new MenuItem("About BotMaker");
        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().add(aboutItem);

        return helpMenu;
    }

    /**
     * Shows the about dialog
     */
    private void showAboutDialog() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.initOwner(primaryStage);
        alert.setTitle("About BotMaker");
        alert.setHeaderText("BotMaker Blocks");
        alert.setContentText(
                "Version: 1.0.0\n\n" +
                        "A visual block-based programming environment for Java.\n\n" +
                        "Build Java applications using drag-and-drop blocks!"
        );
        alert.showAndWait();
    }

    /**
     * Gets the menu bar
     */
    public MenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Sets the callback for when "Select Project" is clicked
     */
    public void setOnSelectProject(Consumer<Void> callback) {
        this.onSelectProject = callback;
    }
}