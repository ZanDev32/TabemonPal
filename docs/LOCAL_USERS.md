# Local User Authentication System

## Overview
The TabemonPal application now includes a local user authentication system with two predefined accounts stored in `LocalUserData.xml`. This system allows users to log in without requiring an external API connection.

## Local Accounts

### Administrator Account
- **Username:** `admin`
- **Password:** `admin123`
- **Email:** `admin@tabemonpal.local`
- **Full Name:** Administrator
- **Access Level:** Full admin privileges (can access Recipe Manager and User Manager)

### Regular User Account
- **Username:** `user`
- **Password:** `user123`
- **Email:** `user@tabemonpal.local`
- **Full Name:** Regular User
- **Access Level:** Standard user privileges

## Files Created

### 1. `LocalUserData.xml`
**Location:** `src/main/java/com/starlight/models/LocalUserData.xml`
- Contains XML data for the two local user accounts
- Can be modified to add more local users if needed

### 2. `LocalUserDataRepository.java`
**Location:** `src/main/java/com/starlight/models/LocalUserDataRepository.java`
- Repository class for managing local user accounts
- Provides methods for authentication, user lookup, and admin checking
- Caches users for better performance

### 3. `LocalUserManager.java`
**Location:** `src/main/java/com/starlight/models/LocalUserManager.java`
- Utility class for testing and managing local accounts
- Includes a main method for testing authentication
- Can display account information

## How It Works

### Login Process
1. **Local Authentication First:** When a user tries to log in, the system first attempts to authenticate against local accounts
2. **API Fallback:** If local authentication fails, the system falls back to the original API authentication
3. **Session Management:** Successfully authenticated users are stored in the session just like API users

### Admin Features
- The admin account (`admin`) has access to special admin-only features:
  - Recipe Manager
  - User Manager
- These features are automatically shown/hidden based on the logged-in user's username

### Modified Files
- **LoginViewController.java:** Updated to include local authentication alongside API authentication
- **MainController.java:** Admin detection logic already works with the local admin account

## Usage Instructions

### For Testing
1. Run the application
2. Use either of these login combinations:
   - Admin: `admin` / `admin123`
   - User: `user` / `user123`
3. The admin account will have access to additional menu items (Recipe Manager, User Manager)

### For Development
```java
// Create a local user repository
LocalUserDataRepository localRepo = new LocalUserDataRepository();

// Authenticate a user
User authenticatedUser = localRepo.authenticateLocalUser("admin", "admin123");

// Check if user is admin
boolean isAdmin = localRepo.isLocalAdmin("admin");

// Get specific users
User adminUser = localRepo.getAdminUser();
User regularUser = localRepo.getRegularUser();
```

### Testing the System
Run the `LocalUserManager` class directly to test the authentication system:
```bash
java com.starlight.models.LocalUserManager
```

## Benefits

1. **Offline Testing:** No need for API connectivity during development
2. **Consistent Admin Testing:** Reliable admin account for testing admin features
3. **Development Convenience:** Easy-to-remember login credentials
4. **Fallback Authentication:** Maintains compatibility with existing API authentication

## Security Notes

- These are development/testing accounts with simple passwords
- The local authentication system is designed for development and testing purposes
- In production, ensure proper password security and user management
