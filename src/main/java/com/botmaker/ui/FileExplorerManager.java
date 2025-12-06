package com.botmaker.ui;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.services.CodeEditorService;
import com.botmaker.state.ApplicationState;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class FileExplorerManager {

    private final ApplicationConfig config;
    private final CodeEditorService codeEditorService;
    private final ApplicationState state;
    private final TreeView<Path> fileTree;

    public FileExplorerManager(ApplicationConfig config, CodeEditorService codeEditorService, ApplicationState state) {
        this.config = config;
        this.codeEditorService = codeEditorService;
        this.state = state;
        this.fileTree = new TreeView<>();
    }

    public VBox createView() {
        VBox container = new VBox();
        container.getStyleClass().add("file-explorer");

        Label header = new Label("Project Files");
        header.getStyleClass().add("sidebar-header");

        Button newFileBtn = new Button("New Function Library");
        newFileBtn.setMaxWidth(Double.MAX_VALUE);
        newFileBtn.setOnAction(e -> showCreateFileDialog());

        configureTree();
        refreshTree();

        container.getChildren().addAll(header, newFileBtn, fileTree);
        return container;
    }

    private void configureTree() {
        // We hide the root "java" folder to make it look cleaner
        fileTree.setShowRoot(false);

        fileTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    setContextMenu(null);
                } else {
                    String fileName = item.getFileName().toString();

                    // Visual Logic
                    boolean isDirectory = Files.isDirectory(item);
                    String pathStr = item.toString().replace("\\", "/");
                    boolean isLibrary = pathStr.contains("com/botmaker/library");

                    if (isDirectory) {
                        setText(fileName);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
                        setContextMenu(null); // No context menu for directories for now
                    }
                    else if (isLibrary) {
                        setText(fileName + " [Lib]");
                        // Locked/Library style
                        setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
                        setContextMenu(null); // Cannot delete library files
                    }
                    else {
                        setText(fileName);
                        // Highlight active file
                        if (state.getActiveFile() != null && item.equals(state.getActiveFile().getPath())) {
                            setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
                        } else {
                            setStyle("-fx-text-fill: black;");
                        }

                        // Add Context Menu for user files
                        ContextMenu cm = new ContextMenu();
                        MenuItem deleteItem = new MenuItem("Delete File");
                        deleteItem.setOnAction(e -> {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Delete File");
                            alert.setHeaderText("Delete " + fileName + "?");
                            alert.setContentText("Are you sure you want to delete this file? This cannot be undone.");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                codeEditorService.deleteFile(item);
                                refreshTree();
                            }
                        });
                        cm.getItems().add(deleteItem);
                        setContextMenu(cm);
                    }
                }
            }
        });

        fileTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                Path selectedPath = newVal.getValue();

                // Only open if it's a file, not a directory
                if (Files.isRegularFile(selectedPath)) {
                    // Allow opening all files, including library files (for visualization)
                    if (state.getActiveFile() == null || !state.getActiveFile().getPath().equals(selectedPath)) {
                        codeEditorService.switchToFile(selectedPath);
                        // Force refresh to update the bold highlighting
                        fileTree.refresh();
                    }
                }
            }
        });
    }

    /**
     * Saves the paths of all currently expanded items.
     */
    private Set<String> saveExpansionState() {
        Set<String> expanded = new HashSet<>();
        if (fileTree.getRoot() != null) {
            saveExpansionStateRecursive(fileTree.getRoot(), expanded);
        }
        return expanded;
    }

    private void saveExpansionStateRecursive(TreeItem<Path> item, Set<String> expanded) {
        if (item.isExpanded()) {
            expanded.add(item.getValue().toAbsolutePath().toString());
        }
        for (TreeItem<Path> child : item.getChildren()) {
            saveExpansionStateRecursive(child, expanded);
        }
    }

    /**
     * Restores expansion state based on saved paths.
     */
    private void restoreExpansionState(TreeItem<Path> item, Set<String> expanded) {
        if (expanded.contains(item.getValue().toAbsolutePath().toString())) {
            item.setExpanded(true);
        }
        for (TreeItem<Path> child : item.getChildren()) {
            restoreExpansionState(child, expanded);
        }
    }

    public void refreshTree() {
        // 1. Save current expansion state
        Set<String> expandedState = saveExpansionState();

        // 2. Find root
        Path current = config.getSourceFilePath().getParent();
        while (current != null && !current.getFileName().toString().equals("java")) {
            current = current.getParent();
        }
        if (current == null) {
            current = config.getSourceFilePath().getParent();
        }

        TreeItem<Path> root = new TreeItem<>(current);
        // Always expand the invisible root
        root.setExpanded(true);

        // 3. Rebuild tree
        buildFileTree(root, current);

        // 4. Restore expansion state
        restoreExpansionState(root, expandedState);

        fileTree.setRoot(root);
    }

    private void buildFileTree(TreeItem<Path> parentItem, Path parentPath) {
        try (Stream<Path> files = Files.list(parentPath)) {
            files.sorted((p1, p2) -> {
                // Sort directories first, then files
                boolean d1 = Files.isDirectory(p1);
                boolean d2 = Files.isDirectory(p2);
                if (d1 && !d2) return -1;
                if (!d1 && d2) return 1;
                return p1.getFileName().toString().compareTo(p2.getFileName().toString());
            }).forEach(path -> {
                TreeItem<Path> item = new TreeItem<>(path);
                parentItem.getChildren().add(item);

                if (Files.isDirectory(path)) {
                    // Auto-expand specific folders if not handled by state restore (e.g. initial load)
                    // But generally rely on state or default expanded for root/packages
                    buildFileTree(item, path);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showCreateFileDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Function Library");
        dialog.setHeaderText("Create a new library of functions");
        dialog.setContentText("Name (e.g. Movement):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String className = name.trim().replaceAll("[^a-zA-Z0-9]", "");
            if (!className.isEmpty()) {
                codeEditorService.createFile(className);
                refreshTree();
            }
        });
    }
}