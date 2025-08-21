# TabemonPal: The Healthy Eating Gamification App


[![Java](https://img.shields.io/badge/Java-17-red.svg)](https://openjdk.java.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-19-blue.svg)](https://openjfx.io/)
[![MaterialFX](https://img.shields.io/badge/MaterialFX-11.13.5-purple.svg)](https://github.com/palexdev/MaterialFX)

**TabemonPal** adalah aplikasi makan sehat dengan gamifikasi yang dirancang untuk mendorong pengguna mengadopsi kebiasaan makan yang lebih sehat melalui UI yang menarik dan interaktif. Dengan menggabungkan fitur sosial, misi, dan elemen gamifikasi, TabemonPal membuat makan sehat menjadi menyenangkan dan mudah diakses.

**TabemonPal** is a gamified healthy eating application designed to encourage users to adopt healthier eating habits through an engaging and interactive user interface. By combining social features, missions, and gamification elements, TabemonPal makes healthy eating fun and accessible.

## ✨ Features

- **User Authentication System**: Secure login and registration with account management
- **Community Sharing**: Share food recipes and healthy eating tips with other users
- **Social Interaction**: Like, comment, and engage with posts from the community
- **Profile Management**: Personalized user profiles with account settings
- **Achievement System**: Track accomplishments and progress in healthy eating goals
- **Mission System**: Complete challenges to earn rewards and recognition
- **Consultation**: Get advice and guidance on healthy eating habits with AI-powered ChatGPT integration
- **Article**: Access a comprehensive knowledge base on nutrition and healthy foods
- **Embedded User API Server**: Handles user data and authentication requests

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed on your system:
- Java Development Kit (JDK) 17 or higher
- Maven for dependency management

### Installation

Follow these steps to set up the project locally:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/ZanDev32/TabemonPal.git && cd TabemonPal
   ```

2. **Build the project with Maven**:
   ```bash
   mvn -B clean install
   ```

3. **Run the application**:
   ```bash
   mvn javafx:run
   ```

### Data Storage Location

TabemonPal stores all user data (XML files, uploaded images, and application settings) in a directory called `.tabemonpal` located in your user's home directory:

- **Windows**: `C:\Users\[username]\.tabemonpal\`
- **macOS**: `/Users/[username]/.tabemonpal/`
- **Linux**: `/home/[username]/.tabemonpal/`

This ensures that your data persists across application updates and remains separate from the installation directory.

## 📂 Project Structure

```
TabemonPal/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── starlight/
    │   │           ├── App.java                  # Main application entry point
    │   │           ├── api/
    │   │           ├── controller/
    │   │           ├── data/    
    │   │           ├── model/
    │   │           ├── repository/    
    │   │           └── util/
    │   └── resources/
    │       └── com/
    │           └── starlight/
    │               ├── fonts/
    │               ├── images/
    │               └── view/                     # FXML layout files
    └── test/
        └── java/
            └── com/
                └── starlight/
                    ├── api/
                    ├── controller/
                    ├── model/
                    └── util/
```

## 💻 Technologies Used

- **JavaFX**: UI framework for desktop application development
- **MaterialFX**: Modern UI components and styling
- **XStream**: XML serialization/deserialization for data persistence
- **ChatGPT Java API**: Integration with OpenAI's ChatGPT for consultation features
- **HttpServer**: Java's built-in HTTP server for API endpoints
- **CSS**: Custom styling for the application interface

## 🌟 Key Components

### User API Server
An embedded HTTP server that handles user authentication, registration, and data management through RESTful endpoints.

### Data Repositories
XML-based repositories for storing and retrieving user accounts and community posts with support for CRUD operations.

All user-generated data (XML files and uploaded images) is stored in a directory named `.tabemonpal` inside the current user's home folder. This keeps application data separate from the installation path and ensures it persists across updates.

### ChatGPT Integration
The application leverages OpenAI's ChatGPT to provide intelligent consultation and nutritional advice to users.

### MVC Architecture
The application follows the Model-View-Controller pattern with clear separation between data models, JavaFX views, and controller logic.

## 🛠️ Development

### Building from Source

The project uses Maven for dependency management. To build from source:

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Required Libraries

- MaterialFX: https://github.com/palexdev/MaterialFX
- XStream: https://x-stream.github.io/

## 🙏 Acknowledgments

- MaterialFX library by Alessandro Parisi for the modern UI components
- PlexPt for the ChatGPT Java API integration
- Praventa-desktop by rizalkalam for some code inpiration
- XStream library for simplified XML serialization

## 📄 License

Copyright © 2025 Starlight Inc. developers. All rights reserved.    
