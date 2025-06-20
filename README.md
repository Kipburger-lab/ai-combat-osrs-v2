# AI Combat OSRS - Advanced Combat Automation Script

[![Version](https://img.shields.io/badge/version-2.0-blue.svg)]()
[![DreamBot](https://img.shields.io/badge/DreamBot-Compatible-green.svg)]()
[![Java](https://img.shields.io/badge/Java-11-orange.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

## 🎯 Overview

AI Combat OSRS is a state-of-the-art, multi-functional combat automation script for Old School RuneScape (OSRS) built using the DreamBot API. This project follows Scrum methodology for agile development and includes comprehensive task management, intelligent targeting, anti-detection measures, and dynamic combat optimization.

## ✨ Key Features

### 🗡️ Combat System
- **Multi-Style Combat**: Seamless support for melee, ranged, and magic combat
- **Intelligent Targeting**: Advanced NPC selection with multiple prioritization strategies
- **Combat Style Management**: Dynamic switching between attack, strength, defense, and other combat styles
- **Special Attack Optimization**: Automatic special attack usage based on weapon type and situation
- **Prayer Management**: Smart prayer activation/deactivation with flicking support

### 📋 Task Management
- **Goal-Oriented Training**: Set specific skill targets (e.g., "Train Attack to level 50")
- **Multi-Task Queues**: Chain multiple training objectives together
- **Progress Tracking**: Real-time monitoring of task completion and skill progression
- **Flexible Configuration**: Easy task modification and priority adjustment

### 🛡️ Anti-Detection System
- **Human-Like Behavior**: Mimics natural player patterns and movements
- **Fatigue Simulation**: Implements realistic break patterns and performance degradation
- **Randomized Actions**: Varies timing, camera movements, and interaction patterns
- **Adaptive Behavior**: Adjusts patterns based on game state and activity

### 🎮 User Interface
- **Intuitive Configuration**: Easy setup for NPCs, combat styles, and training goals
- **Real-Time Monitoring**: Live statistics and progress tracking
- **Comprehensive Logging**: Detailed activity logs and performance metrics
- **Status Updates**: Regular progress reports and system health checks

## 🏗️ Project Structure

```
AI Combat OSRS/
├── src/main/java/
│   ├── scripts/
│   │   └── AICombatScript.java          # Main script entry point
│   ├── core/
│   │   └── CombatEngine.java            # Core combat logic and coordination
│   ├── combat/
│   │   ├── TargetSelector.java          # NPC targeting and selection
│   │   └── CombatStyleManager.java      # Combat style management
│   ├── tasks/
│   │   └── TaskManager.java             # Task queue and progression system
│   └── antiban/
│       └── AntiBanManager.java          # Anti-detection and human behavior
├── docs/
│   ├── PROJECT_PLAN.md                  # Comprehensive project roadmap
│   ├── BACKLOG.md                       # Agile development backlog
│   └── API_REFERENCE.md                 # DreamBot API usage guide
├── pom.xml                              # Maven build configuration
└── README.md                            # This file
```

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- DreamBot client
- Maven (for building from source)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/ai-combat-osrs.git
   cd ai-combat-osrs
   ```

2. **Build the project**:
   ```bash
   mvn clean compile
   mvn package
   ```

3. **Load in DreamBot**:
   - Copy the generated JAR file from `target/` to your DreamBot scripts folder
   - Refresh scripts in DreamBot client
   - Select "AI Combat Script" from the script list

### Basic Usage

1. **Configure Target**:
   ```java
   // Set target NPC by name
   script.setTargetNpc("Cow");
   
   // Or by ID
   script.setTargetNpcId(2808);
   ```

2. **Set Training Goal**:
   ```java
   // Train Attack to level 20
   script.setTrainingGoal(Skill.ATTACK, 20);
   ```

3. **Configure Combat Style**:
   ```java
   // Set combat style
   script.setCombatStyle("attack");
   ```

4. **Add Training Tasks**:
   ```java
   // Add multiple tasks
   script.addTrainingTask("Train Attack", "Cow", Skill.ATTACK, 20, "attack");
   script.addTrainingTask("Train Strength", "Cow", Skill.STRENGTH, 20, "strength");
   ```

## 📊 Features in Detail

### Combat Engine
The `CombatEngine` class serves as the central coordinator for all combat activities:
- **Target Management**: Maintains current target and handles target switching
- **Combat Execution**: Manages attack sequences and combat timing
- **Health Management**: Automatic food consumption and health monitoring
- **Statistics Tracking**: Comprehensive combat performance metrics

### Target Selector
Advanced targeting system with multiple strategies:
- **Priority-Based Selection**: Closest, weakest, strongest, or custom priorities
- **Filtering Options**: Level ranges, combat status, area restrictions
- **Blacklist Support**: Avoid specific NPCs or areas
- **Line of Sight**: Ensures targets are reachable and visible

### Task Management
Flexible task system for complex training scenarios:
- **Goal-Based Tasks**: Train specific skills to target levels
- **Conditional Logic**: Switch tasks based on achievements or conditions
- **Progress Monitoring**: Real-time tracking of task completion
- **Queue Management**: Automatic progression through task lists

### Anti-Ban System
Sophisticated anti-detection measures:
- **Behavior Profiles**: Different activity patterns (conservative, normal, aggressive)
- **Fatigue Simulation**: Gradual performance degradation over time
- **Random Actions**: Camera movements, tab switching, skill hovering
- **Break Patterns**: Realistic pause and resume cycles

## 🔧 Configuration

### Combat Settings
```java
// Configure target selector
targetSelector.setPriority(TargetSelector.TargetPriority.CLOSEST);
targetSelector.setMaxDistance(8);
targetSelector.setAvoidCombatNpcs(true);
targetSelector.setMinCombatLevel(1);
targetSelector.setMaxCombatLevel(50);

// Configure anti-ban
antiBanManager.setBehaviorProfile(AntiBanManager.BehaviorProfile.NORMAL);
antiBanManager.setBreakFrequency(120); // Break every 2 hours
antiBanManager.setFatigueEnabled(true);
```

### Task Configuration
```java
// Create complex task chains
taskManager.addSimpleTask("Phase 1: Attack Training", "Cow", Skill.ATTACK, 30, "attack");
taskManager.addSimpleTask("Phase 2: Strength Training", "Cow", Skill.STRENGTH, 30, "strength");
taskManager.addSimpleTask("Phase 3: Defense Training", "Cow", Skill.DEFENCE, 30, "defence");
```

## 📈 Performance Monitoring

The script provides comprehensive monitoring and logging:

### Real-Time Statistics
- Combat performance metrics (kills/hour, XP/hour)
- Skill progression tracking
- Task completion percentages
- Anti-ban activity summaries

### Logging System
```
=== Status Update ===
Runtime: 45 minutes
Loop count: 2700
Current Attack level: 25
Current task: Train Attack to level 30
Task progress: 83.3%
Combat stats: 156 kills, 12,450 XP gained
Anti-ban: Fatigue 23%, Actions 47
===================
```

## 🛠️ Development

### Building from Source
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package JAR
mvn package

# Install dependencies
mvn dependency:resolve
```

### Project Structure
The project follows a modular architecture:
- **Core Package**: Essential combat functionality
- **Combat Package**: Targeting and style management
- **Tasks Package**: Task management and progression
- **Antiban Package**: Anti-detection measures

### Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📋 Roadmap

See [PROJECT_PLAN.md](docs/PROJECT_PLAN.md) for detailed development phases:

- **Phase 1**: Core Combat System ✅
- **Phase 2**: Advanced Targeting ✅
- **Phase 3**: Task Management ✅
- **Phase 4**: Anti-Ban System ✅
- **Phase 5**: Economy Integration (In Progress)
- **Phase 6**: GUI Development (Planned)
- **Phase 7**: Testing & Optimization (Planned)

## 🐛 Known Issues

- GUI integration pending (Phase 6)
- Grand Exchange automation not yet implemented
- Prayer flicking optimization needed
- Bank management system in development

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ⚠️ Disclaimer

This script is for educational purposes only. Use at your own risk. The developers are not responsible for any account actions taken by Jagex. Always follow the game's terms of service.

## 🤝 Support

For support, questions, or feature requests:
- Open an issue on GitHub
- Check the [documentation](docs/)
- Review the [project plan](docs/PROJECT_PLAN.md)

## 🏆 Acknowledgments

- DreamBot API team for excellent documentation
- OSRS community for testing and feedback
- Contributors and beta testers

---

**Built with ❤️ by TraeAI using Scrum methodology and best practices**