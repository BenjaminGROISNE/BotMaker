package com.botmaker.ui;

import com.botmaker.project.ProjectCreator;
import com.botmaker.project.ProjectInfo;
import com.botmaker.project.ProjectManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Project selection screen shown on startup with project creation capability
 */
public class ProjectSelectionScreen {

    private final ProjectManager projectManager;
    private final ProjectCreator projectCreator;
    private final Consumer<String> onProjectSelected;
    private final Stage stage;
    private ListView<ProjectInfo> projectListView;

    public ProjectSelectionScreen(Stage stage, Consumer<String> onProjectSelected) {
        this.stage = stage;
        this.projectManager = new ProjectManager();
        this.projectCreator = new ProjectCreator();
        this.onProjectSelected = onProjectSelected;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Header
        Label titleLabel = new Label("Select a Project");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        VBox header = new VBox(10, titleLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        // Project list
        projectListView = new ListView<>();
        projectListView.setPrefHeight(400);

        // Custom cell factory to show project info
        projectListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectInfo project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label nameLabel = new Label(project.getName());
                    nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                    Label pathLabel = new Label(project.getProjectPath().toString());
                    pathLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                    Label dateLabel = new Label("Last modified: " +
                            project.getLastModified().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                    box.getChildren().addAll(nameLabel, pathLabel, dateLabel);
                    setGraphic(box);
                }
            }
        });

        // Load projects
        refreshProjectList();

        // Select first project by default
        if (!projectListView.getItems().isEmpty()) {
            projectListView.getSelectionModel().select(0);
        }

        // Buttons
        Button openButton = new Button("Open Project");
        openButton.setPrefWidth(150);
        openButton.setDefaultButton(true);
        openButton.setOnAction(e -> openSelectedProject());

        Button createButton = new Button("Create New Project");
        createButton.setPrefWidth(150);
        createButton.setOnAction(e -> showCreateProjectDialog());

        // Handle double-click
        projectListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openSelectedProject();
            }
        });

        HBox buttonBox = new HBox(10, openButton, createButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        // Layout
        VBox center = new VBox(10, projectListView, buttonBox);
        root.setTop(header);
        root.setCenter(center);

        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/com/botmaker/styles.css").toExternalForm());

        return scene;
    }

    /**
     * Opens the selected project
     */
    private void openSelectedProject() {
        ProjectInfo selected = projectListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            onProjectSelected.accept(selected.getName());
        }
    }

    /**
     * Refreshes the project list
     */
    private void refreshProjectList() {
        List<ProjectInfo> projects = projectManager.listProjects();
        projectListView.getItems().clear();
        projectListView.getItems().addAll(projects);
    }

    /**
     * Shows the create project dialog
     */
    private void showCreateProjectDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Project");
        dialog.setHeaderText("Enter project name");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the project name field
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField projectNameField = new TextField();
        projectNameField.setPromptText("ProjectName");

        Label instructionLabel = new Label("Project name must:");
        instructionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        Label rule1 = new Label("• Start with an uppercase letter");
        rule1.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        Label rule2 = new Label("• Contain only letters and numbers");
        rule2.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        Label rule3 = new Label("• Be between 2-50 characters");
        rule3.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        Label exampleLabel = new Label("Example: MyFirstProject");
        exampleLabel.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: gray;");

        content.getChildren().addAll(
                new Label("Project Name:"),
                projectNameField,
                instructionLabel,
                rule1,
                rule2,
                rule3,
                exampleLabel
        );

        dialog.getDialogPane().setContent(content);

        // Enable/disable create button based on validation
        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Validation
        projectNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = isValidProjectName(newValue);
            createButton.setDisable(!isValid);

            // Visual feedback
            if (newValue.isEmpty()) {
                projectNameField.setStyle("");
            } else if (isValid) {
                projectNameField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            } else {
                projectNameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
        });

        // Request focus on the text field
        javafx.application.Platform.runLater(projectNameField::requestFocus);

        // Convert the result when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return projectNameField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::createProject);
    }

    /**
     * Validates project name format
     */
    private boolean isValidProjectName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Check format
        if (!name.matches("^[A-Z][a-zA-Z0-9]*$")) {
            return false;
        }

        // Check length
        if (name.length() < 2 || name.length() > 50) {
            return false;
        }

        // Check if already exists
        if (projectCreator.projectExists(name)) {
            return false;
        }

        return true;
    }

    /**
     * Creates a new project
     */
    private void createProject(String projectName) {
        try {
            // Show progress
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Creating Project");
            progressAlert.setHeaderText("Please wait...");
            progressAlert.setContentText("Creating project: " + projectName);
            progressAlert.show();

            // Create the project
            projectCreator.createProject(projectName);

            // Close progress alert
            progressAlert.close();

            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("Project Created");
            successAlert.setContentText("Project '" + projectName + "' has been created successfully!");
            successAlert.showAndWait();

            // Refresh the list
            refreshProjectList();

            // Select the newly created project
            for (ProjectInfo project : projectListView.getItems()) {
                if (project.getName().equals(projectName)) {
                    projectListView.getSelectionModel().select(project);
                    break;
                }
            }

        } catch (Exception e) {
            // Show error message
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Failed to create project");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
    }
}