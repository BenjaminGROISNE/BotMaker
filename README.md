# BotMaker Blocks

A visual block-based programming environment for Java, designed to make coding accessible and intuitive. Built with JavaFX and powered by the Eclipse JDT Language Server for intelligent code completion and error detection.

## Features

- 🧩 **Visual Block Programming** - Drag and drop blocks to write Java code
- 🔍 **Intelligent Code Completion** - LSP-powered suggestions for variables, types, and methods
- 🐛 **Built-in Debugger** - Step through your code with visual highlighting
- ⚡ **Real-time Compilation** - Instant feedback on errors and warnings
- 📁 **Multi-Project Support** - Manage multiple Java projects with ease
- 🎨 **Light/Dark Themes** - Choose your preferred visual style
- 🔧 **Gradle Integration** - Standard Gradle project structure

## Screenshots

*Coming soon*

## Requirements

- **Java 17 or higher** (Java 21+ recommended)
- **JavaFX 21+** (included in dependencies)
- **Linux/macOS/Windows** (Linux is primary development platform)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/BotMaker.git
cd BotMaker
```

### 2. Download JDT Language Server

Download the Eclipse JDT Language Server and extract it to `tools/jdt-language-server/`:

```bash
mkdir -p tools
cd tools
wget https://download.eclipse.org/jdtls/milestones/1.40.0/jdt-language-server-1.40.0-202410171240.tar.gz
mkdir jdt-language-server
tar -xzf jdt-language-server-*.tar.gz -C jdt-language-server
cd ..
```

### 3. Build the Project

Using Gradle:

```bash
./gradlew build
```

Or using your IDE (IntelliJ IDEA, Eclipse, etc.)

### 4. Run the Application

```bash
./gradlew run
```

Or run `com.botmaker.Main` from your IDE.

## Project Structure

```
BotMaker/
├── src/main/java/com/botmaker/
│   ├── Main.java                    # Application entry point
│   ├── blocks/                      # Visual block implementations
│   │   ├── IfBlock.java
│   │   ├── PrintBlock.java
│   │   ├── VariableDeclarationBlock.java
│   │   └── ...
│   ├── core/                        # Core interfaces and abstractions
│   │   ├── CodeBlock.java
│   │   ├── StatementBlock.java
│   │   ├── ExpressionBlock.java
│   │   └── ...
│   ├── config/                      # Configuration management
│   │   ├── ApplicationConfig.java
│   │   └── Constants.java
│   ├── di/                          # Dependency injection
│   │   └── DependencyContainer.java
│   ├── events/                      # Event bus system
│   │   ├── EventBus.java
│   │   ├── ApplicationEvent.java
│   │   └── CoreApplicationEvents.java
│   ├── lsp/                         # Language Server Protocol integration
│   │   ├── JdtLanguageServerLauncher.java
│   │   └── CompletionContext.java
│   ├── parser/                      # Code parsing and AST manipulation
│   │   ├── BlockFactory.java
│   │   ├── AstRewriter.java
│   │   └── CodeEditor.java
│   ├── project/                     # Project management
│   │   ├── ProjectManager.java
│   │   └── ProjectInfo.java
│   ├── runtime/                     # Code execution and debugging
│   │   ├── CodeExecutionService.java
│   │   ├── DebuggerService.java
│   │   └── DebuggingManager.java
│   ├── services/                    # High-level application services
│   │   ├── CodeEditorService.java
│   │   ├── LanguageServerService.java
│   │   ├── ExecutionService.java
│   │   └── DebuggingService.java
│   ├── state/                       # Application state management
│   │   └── ApplicationState.java
│   ├── ui/                          # User interface components
│   │   ├── UIManager.java
│   │   ├── ProjectSelectionScreen.java
│   │   ├── BlockDragAndDropManager.java
│   │   └── AddableBlock.java
│   ├── util/                        # Utility classes
│   │   ├── TypeManager.java
│   │   └── DefaultNames.java
│   └── validation/                  # Error handling and translation
│       ├── DiagnosticsManager.java
│       └── ErrorTranslator.java
├── src/main/resources/
│   └── com/botmaker/
│       └── styles.css               # Application styling
├── projects/                        # User projects directory
│   └── [ProjectName]/               # Each project is a Gradle project
│       ├── src/main/java/com/[projectname]/[ProjectName].java
│       ├── build.gradle
│       ├── settings.gradle
│       └── build/
├── tools/
│   └── jdt-language-server/        # Eclipse JDT LS (not in repo)
└── build.gradle                     # Main build configuration
```

## How It Works

### Architecture Overview

BotMaker follows an **event-driven architecture** with clear separation of concerns:

1. **Event Bus** - Central communication hub for decoupled components
2. **Dependency Injection** - Service lifecycle management
3. **LSP Integration** - Real-time code intelligence via Eclipse JDT LS
4. **AST Manipulation** - Code generation and editing using Eclipse JDT Core
5. **Visual Blocks** - JavaFX UI components representing code structures

### Key Components

#### Block System
- **CodeBlock** - Base interface for all visual blocks
- **StatementBlock** - Blocks that represent executable statements (if, print, variable declaration)
- **ExpressionBlock** - Blocks that represent values (literals, variables, operations)
- **BodyBlock** - Container blocks that hold other statements

#### Code Synchronization
1. User drags a block → `BlockDragAndDropManager` handles drop
2. `CodeEditor` updates the AST using `AstRewriter`
3. `LanguageServerService` syncs changes with JDT LS
4. `DiagnosticsManager` processes errors/warnings
5. `UIManager` reflects changes in the visual blocks

#### Execution Flow
- **Compile** → Uses `javac` to compile to bytecode
- **Run** → Executes compiled class in separate JVM
- **Debug** → Uses JDI (Java Debug Interface) with breakpoints on blocks

## Creating a New Project

1. Launch BotMaker
2. Click "New Project" (future feature)
3. Enter project name (e.g., "Calculator")
4. Start coding with blocks!

### Manual Project Creation

Create a new Gradle project in the `projects/` directory:

```bash
mkdir -p projects/MyProject/src/main/java/com/myproject
```

**Create `projects/MyProject/build.gradle`:**

```gradle
plugins {
    id 'java'
    id 'application'
}

group = 'com.myproject'
version = '0.0.1-SNAPSHOT'

repositories {
    mavenCentral()
}

application {
    mainClass = 'com.myproject.MyProject'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

**Create `projects/MyProject/settings.gradle`:**

```gradle
rootProject.name = 'MyProject'
```

**Create `projects/MyProject/src/main/java/com/myproject/MyProject.java`:**

```java
package com.myproject;

public class MyProject {
    public static void main(String[] args) {
        System.out.println("Hello from MyProject!");
    }
}
```

Restart BotMaker, and your project will appear in the selection screen!

## Available Blocks

### Statements
- **Print** - Output text to console
- **Declare Int** - Create integer variable
- **Declare Double** - Create decimal variable
- **Declare Boolean** - Create true/false variable
- **Declare String** - Create text variable
- **If Statement** - Conditional branching (with optional else)

### Expressions
- **Literal** - Numbers, text, true/false values
- **Variable** - Reference a declared variable
- **Binary Expression** - Math operations (+, -, *, /, etc.)

*More blocks coming soon!*

## Development

### Running in Development Mode

```bash
./gradlew run --console=plain
```

### Building Distribution

```bash
./gradlew installDist
```

The distributable application will be in `build/install/BotMaker/`.

### Project Roadmap

- [ ] **Loop blocks** (for, while, foreach)
- [ ] **Method/function creation**
- [ ] **Array/List support**
- [ ] **Object-oriented programming** (classes, objects)
- [ ] **Import external libraries**
- [ ] **Export to standalone Java project**
- [ ] **Project templates** (Game, Calculator, etc.)
- [ ] **Block search/palette filtering**
- [ ] **Undo/Redo**
- [ ] **Code view toggle** (show generated Java code)
- [ ] **Custom block creation**

## Architecture Decisions

### Why Event Bus?
Decouples components for easier testing and modification. Services publish events without knowing who's listening.

### Why Eclipse JDT?
Provides production-grade Java parsing, AST manipulation, and LSP server for code intelligence.

### Why JavaFX?
Native desktop UI with good performance and extensive styling options.

### Why Gradle Project Structure?
Ensures projects are standard Java projects that can be opened in any IDE.

## Troubleshooting

### JDT Language Server Not Starting

**Error:** `Launcher JAR not found`

**Solution:** Make sure you downloaded the JDT LS and extracted it to `tools/jdt-language-server/`

### Projects Not Showing Up

**Error:** No projects appear in the selection screen

**Solution:** Ensure your project has the correct structure:
```
projects/[ProjectName]/
├── src/main/java/com/[projectname]/[ProjectName].java
└── build.gradle
```

### Compilation Errors

**Error:** `java.nio.file.NoSuchFileException`

**Solution:** Check that paths in `ApplicationConfig` match your project structure.

### Out of Memory

**Error:** `OutOfMemoryError` when running

**Solution:** Increase heap size in `Constants.JVM_MAX_HEAP` (default is `-Xmx1G`)

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Use 4 spaces for indentation
- Follow Java naming conventions
- Add JavaDoc comments for public APIs
- Keep methods focused and under 50 lines when possible

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- **Eclipse JDT** - Java language server and AST manipulation
- **LSP4J** - Language Server Protocol implementation
- **JavaFX** - UI framework
- **Scratch/Blockly** - Inspiration for block-based programming

## Contact

**Project Repository:** https://github.com/yourusername/BotMaker

**Issues:** https://github.com/yourusername/BotMaker/issues

---

**Made with ❤️ for making programming accessible to everyone**