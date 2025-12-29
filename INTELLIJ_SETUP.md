# IntelliJ IDEA Setup Guide - Fix Java Compilation Error

## 🐛 **Current Error:**
```
javac 25 was used to compile java sources
java.lang.ExceptionInInitializerError
com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

**Problem:** IntelliJ is using Java 25 (or 24), which is incompatible with Spring Boot 3.2 and Lombok.

---

## ✅ **Solution: Configure IntelliJ to Use Java 17**

### **Step 1: Download & Install Java 17**

#### **Option A: Direct Download (Recommended)**
1. Go to: https://adoptium.net/temurin/releases/?version=17
2. Choose: **Windows x64 JDK (.msi installer)**
3. Download and run installer
4. Note installation path (usually: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot\`)

#### **Option B: Via Chocolatey**
```powershell
# Open PowerShell as Administrator
choco install temurin17 -y
```

---

### **Step 2: Configure IntelliJ Project SDK**

1. **Open Project Structure:**
   - Press `Ctrl + Alt + Shift + S`
   - Or: **File** → **Project Structure**

2. **Set Project SDK:**
   - Click **Project** (left sidebar)
   - Under **SDK**, click dropdown
   - If Java 17 appears, select it
   - If not, click **Add SDK** → **Download JDK**
     - Vendor: **Eclipse Temurin (AdoptOpenJDK HotSpot)**
     - Version: **17**
     - Click **Download**

3. **Set Project Language Level:**
   - Same window, under **Project language level**
   - Select: **17 - Sealed types, always-strict floating-point semantics**

4. **Click:** **Apply** → **OK**

---

### **Step 3: Configure IntelliJ Platform Settings**

1. **Open Settings:**
   - Press `Ctrl + Alt + S`
   - Or: **File** → **Settings**

2. **Build, Execution, Deployment** → **Compiler** → **Java Compiler**
   - Set **Project bytecode version:** **17**
   - Set **Per-module bytecode version:** **17** (for task-management-system)

3. **Build Tools** → **Maven**
   - **JRE for importer:** Select Java 17
   - **JRE for Maven:** Select Java 17

4. **Click:** **Apply** → **OK**

---

### **Step 4: Configure Module Settings**

1. **Open Project Structure:** `Ctrl + Alt + Shift + S`

2. **Click Modules** (left sidebar)

3. **Select your module:** `task-management-system`

4. **Dependencies tab:**
   - **Module SDK:** Should show Java 17

5. **Sources tab:**
   - **Language level:** 17

6. **Click:** **Apply** → **OK**

---

### **Step 5: Reload Maven Project**

1. **Open Maven tool window:**
   - View → Tool Windows → Maven
   - Or press `Ctrl + E` → Maven

2. **Reload project:**
   - Click **Reload All Maven Projects** icon (circular arrows)
   - Or right-click on `pom.xml` → **Maven** → **Reload project**

---

### **Step 6: Rebuild Project**

1. **Invalidate Caches (Optional but recommended):**
   - **File** → **Invalidate Caches**
   - Check: **Clear file system cache and Local History**
   - Check: **Clear downloaded shared indexes**
   - Click: **Invalidate and Restart**

2. **After restart, rebuild:**
   - **Build** → **Rebuild Project**
   - Or press `Ctrl + Shift + F9`

---

### **Step 7: Enable Lombok Plugin**

1. **Open Settings:** `Ctrl + Alt + S`

2. **Plugins** → Search for **"Lombok"**

3. If not installed:
   - Click **Install**
   - Restart IntelliJ

4. **Settings** → **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
   - Check: ☑️ **Enable annotation processing**
   - Click: **Apply** → **OK**

---

## 🚀 **Verify Configuration**

Run these checks:

### **Check 1: Maven Compiler**
Open **Terminal** in IntelliJ and run:
```powershell
mvn -version
```
Should show: `Java version: 17.x.x`

### **Check 2: IntelliJ SDK**
- **File** → **Project Structure** → **Project**
- Should show: **SDK: 17**

### **Check 3: Compile**
- **Build** → **Build Project** (`Ctrl + F9`)
- Should succeed without errors

---

## 🎯 **Run the Application**

Once configured correctly:

### **Option 1: Via IntelliJ Run Configuration**

1. **Open:** `src/main/java/com/taskmanagement/TaskManagementApplication.java`

2. **Right-click** on the class → **Run 'TaskManagementApplication'**

3. **Access:** http://localhost:8080/login

### **Option 2: Via Maven**

In IntelliJ Terminal:
```powershell
mvn spring-boot:run
```

---

## ❌ **Troubleshooting**

### **Problem: Can't find Java 17 in IntelliJ**

**Solution:**
1. Close IntelliJ
2. Set system environment variable:
   ```powershell
   # Open PowerShell as Administrator
   [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot", "Machine")
   ```
3. Restart IntelliJ
4. Go to **Project Structure** → **Add SDK** → **Add JDK**
5. Browse to Java 17 installation folder

### **Problem: Still using wrong Java version**

**Solution:**
1. **File** → **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Maven** → **Runner**
2. Set **JRE:** Use Project JDK (should be 17)
3. **VM Options:** Add `-Djava.version=17`

### **Problem: Lombok not working**

**Solution:**
1. **Settings** → **Plugins** → Install **Lombok** plugin
2. **Settings** → **Build** → **Compiler** → **Annotation Processors**
3. Enable: ☑️ **Enable annotation processing**
4. **File** → **Invalidate Caches** → **Invalidate and Restart**

### **Problem: Build still fails with TypeTag error**

**Solution:**
Delete IntelliJ caches:
```powershell
# Close IntelliJ first
Remove-Item -Recurse -Force "$env:USERPROFILE\.IntelliJIdea*\system\compile-server"
Remove-Item -Recurse -Force "$env:USERPROFILE\.IntelliJIdea*\system\compiler"
```
Then restart IntelliJ and rebuild.

---

## 📋 **Quick Checklist**

Before running the application, verify:

- [ ] Java 17 installed
- [ ] IntelliJ Project SDK set to Java 17
- [ ] Project language level set to 17
- [ ] Maven JRE for importer set to Java 17
- [ ] Java Compiler bytecode version set to 17
- [ ] Lombok plugin installed and enabled
- [ ] Annotation processing enabled
- [ ] Maven project reloaded
- [ ] Project rebuilt successfully

---

## 🎨 **After Successful Build**

You should see:
```
Build completed successfully in XX sec
```

Then access your premium frontend:
- **Login:** http://localhost:8080/login
- **Demo credentials:** 
  - Email: `admin@taskmanagement.com`
  - Password: `Admin@123`

---

## 💡 **Pro Tips**

1. **Set Java 17 as default for new projects:**
   - **File** → **New Projects Setup** → **Structure**
   - Set SDK to Java 17

2. **Speed up builds:**
   - **Settings** → **Build** → **Compiler**
   - Set **Build process heap size:** 2048 MB
   - Check: ☑️ **Compile independent modules in parallel**

3. **Auto-reload changes:**
   - **Settings** → **Build** → **Compiler**
   - Check: ☑️ **Build project automatically**

---

**Once configured with Java 17, your Spring Boot application will compile and run perfectly!** ✨
