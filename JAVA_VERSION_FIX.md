# Java Version Configuration Fix

## ⚠️ Error Details
```
java: error: release version 5 not supported
Module task-management-system SDK 24 does not support source version 1.5
```

## 🔧 Quick Fix (Choose One Method)

### Method 1: IntelliJ IDEA Settings (Recommended)

#### Step 1: Open Project Structure
Press `Ctrl+Alt+Shift+S` or go to `File > Project Structure`

#### Step 2: Fix Project Settings
1. **Project Settings > Project**
   - SDK: Select **17** (or download Java 17)
   - Language Level: Select **17 - Sealed types, always-strict floating-point semantics**
   - Click **Apply**

2. **Project Settings > Modules**
   - Select `task-management-system`
   - Language Level: **17 (Project Default)**
   - Click **Apply**

#### Step 3: Reload Maven
1. Right-click on `pom.xml`
2. Select **Maven > Reload Project**

#### Step 4: Invalidate Caches
1. Go to `File > Invalidate Caches and Restart`
2. Click **Invalidate and Restart**

---

### Method 2: Command Line Fix

#### Step 1: Check Java Version
```bash
java -version
```
Should show Java 17. If not, install Java 17.

#### Step 2: Set JAVA_HOME (Windows)
```powershell
# Temporary (current session only)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Permanent (add to System Environment Variables)
# 1. Search "Environment Variables" in Windows
# 2. Add/Edit JAVA_HOME variable
# 3. Point to Java 17 installation directory
```

#### Step 3: Clean and Rebuild
```bash
mvn clean install
```

---

### Method 3: Download Java 17 (If Not Installed)

1. **Download Java 17 (Eclipse Temurin)**
   - Visit: https://adoptium.net/temurin/releases/
   - Select: **Version 17 (LTS)**
   - Platform: **Windows x64**
   - Download and install

2. **After Installation**
   - Follow Method 1 or Method 2 above
   - Select the newly installed Java 17

---

## 🎯 Verify Fix

After applying the fix, verify:

```bash
# Check Java version
java -version

# Clean and compile
mvn clean compile

# Run application
mvn spring-boot:run
```

You should see:
```
Java version: 17.x.x
BUILD SUCCESS
```

---

## ❓ Why This Happened

- IntelliJ was configured to use SDK 24 (Java 24)
- But Maven was trying to compile with Java 1.5 (default fallback)
- Spring Boot 3.2.0 requires Java 17 minimum

---

## ✅ Solution Applied

I've updated `pom.xml` with explicit compiler configuration:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <release>17</release>
    </configuration>
</plugin>
```

This forces Maven to use Java 17 regardless of IDE settings.

---

## 🚀 After Fix

Once fixed, you can proceed with:

```bash
# Build project
mvn clean install

# Run database migration
mysql -u root -p task_management_db < src/main/resources/db/migration/shipping_module_schema.sql

# Start application
mvn spring-boot:run

# Access dashboard
http://localhost:8080/shipping/dashboard
```

---

## 📞 Still Having Issues?

If you still see errors:

1. **Restart IntelliJ completely**
2. **Delete `.idea` folder** and re-import project
3. **Ensure Java 17 is installed**: `java -version`
4. **Check Maven uses Java 17**: `mvn -version`
5. **Rebuild project**: `Build > Rebuild Project` in IntelliJ

---

**Last Updated:** 2025-11-10  
**Status:** ✅ Fixed
