package com.botmaker.ui;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.services.CodeEditorService;
import com.botmaker.state.ApplicationState;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
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
                } else {
                    String fileName = item.getFileName().toString();

                    // Visual Logic
                    boolean isDirectory = Files.isDirectory(item);
                    String pathStr = item.toString().replace("\\", "/");
                    boolean isLibrary = pathStr.contains("com/botmaker/library");

                    if (isDirectory) {
                        setText(fileName);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
                    }
                    else if (isLibrary) {
                        setText(fileName + " [Lib]");
                        // Locked/Library style
                        setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
                    }
                    else {
                        setText(fileName);
                        // Highlight active file
                        if (state.getActiveFile() != null && item.equals(state.getActiveFile().getPath())) {
                            setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
                        } else {
                            setStyle("-fx-text-fill: black;");
                        }
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
                        fileTree.refresh();
                    }
                }
            }
        });
    }

    public void refreshTree() {
        // 1. Find the root 'src/main/java' directory
        // Start from Main file: .../src/main/java/com/myproject/Main.java
        Path current = config.getSourceFilePath().getParent();

        // Traverse up until we hit "java" (or run out of parents)
        while (current != null && !current.getFileName().toString().equals("java")) {
            current = current.getParent();
        }

        // Fallback if structure is weird
        if (current == null) {
            current = config.getSourceFilePath().getParent();
        }

        TreeItem<Path> root = new TreeItem<>(current);
        root.setExpanded(true);

        // 2. Recursively build the tree
        buildFileTree(root, current);

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
                    // Auto-expand 'com' and 'botmaker' folders for convenience
                    String name = path.getFileName().toString();
                    if (name.equals("com") || name.equals("botmaker") || name.equals("library")) {
                        item.setExpanded(true);
                    }
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