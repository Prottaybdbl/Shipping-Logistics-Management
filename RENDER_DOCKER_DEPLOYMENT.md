# 🐳 Render.com এ Docker দিয়ে Deploy (সহজ পদ্ধতি)

Docker select করেছেন - এটা সবচেয়ে সহজ এবং reliable method!

---

## 🚀 Quick Start - Docker Deployment

### Step 1: PostgreSQL Database তৈরি করুন

1. [Render.com Dashboard](https://dashboard.render.com/) এ যান
2. **New +** → **PostgreSQL** click করুন
3. Details fill করুন:
   ```
   Name: task-management-db
   Database: task_management_db
   Region: Singapore
   Instance Type: Free
   ```
4. **Create Database** button এ click করুন
5. Database তৈরি হলে **Internal Database URL** copy করুন

---

### Step 2: Web Service Create করুন (Docker)

1. **New +** → **Web Service** click করুন
2. GitHub repository connect করুন:
   - Repository: `Prottaybdbl/Shipping-Logistics-Management`
   - Branch: `main`

3. Service Configuration:
   ```
   Name: task-management-app
   Region: Singapore
   Branch: main
   Root Directory: (খালি রাখুন)
   
   ⭐ Runtime: Docker  ← এটা select করুন!
   ```

> [!IMPORTANT]
> **Runtime** এ **Docker** select করলে Build Command এবং Start Command automatically handle হবে। আপনার Dockerfile ব্যবহার হবে।

---

### Step 3: Environment Variables Set করুন

**Environment** section এ scroll করে এই variables add করুন:

#### Required Variables:

```bash
# Database Configuration (আপনার Render PostgreSQL থেকে নিন)
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxxxx.singapore-postgres.render.com:5432/task_management_db

SPRING_DATASOURCE_USERNAME=task_management_db_user

SPRING_DATASOURCE_PASSWORD=xxxxxxxxxxxxxxxxxxxx

# PostgreSQL Dialect
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# Server Port (Render.com এর জন্য)
SERVER_PORT=8080

# Database Auto-create Tables
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

#### কিভাবে Database URL পাবেন:

1. আপনার PostgreSQL database এ click করুন
2. **Internal Database URL** দেখুন - এরকম হবে:
   ```
   postgresql://user:password@host:5432/database
   ```
3. এটাকে JDBC format এ convert করুন:
   ```
   jdbc:postgresql://host:5432/database
   ```

**Example:**
```bash
# Render থেকে পেলেন:
postgresql://task_db_user:Xy9zabc123@dpg-abc123.singapore-postgres.render.com:5432/task_management_db

# Environment Variable এ দিবেন:
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-abc123.singapore-postgres.render.com:5432/task_management_db
SPRING_DATASOURCE_USERNAME=task_db_user
SPRING_DATASOURCE_PASSWORD=Xy9zabc123
```

---

### Step 4: Deploy করুন!

1. সব configuration verify করুন
2. **Create Web Service** button এ click করুন
3. Render automatically:
   - ✅ Docker image build করবে
   - ✅ Container run করবে
   - ✅ Database এ connect করবে
   - ✅ Tables automatically create করবে

Deploy logs এ দেখবেন:
```
==> Building...
Dockerfile detected
Building image...
Successfully built docker image
==> Deploying...
Container started successfully
Application running on port 8080
```

---

## 📊 Deployment Summary

### What Happens:

| Step | Action | Time |
|------|--------|------|
| 1 | Clone GitHub repository | ~10 sec |
| 2 | Build Docker image (Multi-stage) | ~3-5 min |
| 3 | Start container | ~30 sec |
| 4 | Connect to database | ~5 sec |
| 5 | Create tables (Hibernate DDL) | ~10 sec |

**Total Deployment Time**: প্রথমবার ~5-7 minutes, পরের বার ~3-4 minutes

---

## ✅ Docker এর Advantages

| Feature | Docker | Java Runtime |
|---------|--------|--------------|
| **Setup** | ✅ Automatic | ❌ Manual configuration |
| **Dependencies** | ✅ All included | ❌ Must configure separately |
| **Build** | ✅ Dockerfile handles everything | ❌ Need Maven commands |
| **Consistency** | ✅ Same on local & production | ⚠️ Can differ |
| **Debugging** | ✅ Easy to reproduce locally | ⚠️ Harder |

---

## 🔍 Verify Deployment

Deploy সম্পূর্ণ হলে:

1. **Dashboard** এ আপনার service দেখুন
2. **URL** click করুন (e.g., `https://task-management-app.onrender.com`)
3. Login page দেখা যাবে
4. Super Admin credentials দিয়ে login করুন

---

## 🐛 Common Issues & Solutions

### Issue 1: Build Failed - Docker Image Too Large
**Error**: `Docker build failed - out of memory`

**Solution**: আপনার Dockerfile ইতিমধ্যে **multi-stage build** ব্যবহার করছে, এটা optimized। তবুও যদি হয়:
- Render.com free tier এ 512MB RAM limit আছে
- Paid tier ($7/month) better performance দেয়

### Issue 2: Container Crashes - Database Connection
**Error**: `Connection refused` or `Unable to connect to database`

**Solution**:
```bash
# Check করুন:
1. SPRING_DATASOURCE_URL সঠিক আছে কিনা
2. jdbc:postgresql:// দিয়ে শুরু হচ্ছে কিনা
3. Internal Database URL ব্যবহার করছেন (External নয়)
4. Username/Password সঠিক আছে কিনা
```

### Issue 3: Port Binding Failed
**Error**: `Port 8081 already in use`

**Solution**: 
- আপনার Dockerfile এ `EXPOSE 8080` আছে (এটা ঠিক আছে)
- `SERVER_PORT=8080` environment variable set করুন

### Issue 4: Tables Not Created
**Error**: `Table 'xxx' doesn't exist`

**Solution**:
```bash
# এই environment variable add করুন:
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# অথবা manually run করুন (Optional):
SPRING_JPA_HIBERNATE_DDL_AUTO=create
```

---

## 📱 Access Your Application

Deploy successful হলে:

```
🌐 URL: https://your-service-name.onrender.com
👤 Login: Super Admin credentials
📊 Dashboard: /dashboard
🔧 Admin Panel: /admin
```

---

## 🔄 Auto-Deploy on Git Push

Render.com এ auto-deploy ইতিমধ্যে enabled:

1. Local এ code change করুন
2. Git commit এবং push করুন:
   ```bash
   git add .
   git commit -m "Your changes"
   git push origin main
   ```
3. Render automatically নতুন Docker image build করবে
4. Zero-downtime deployment হবে

---

## 💡 Local Testing (Optional)

Deploy করার আগে locally test করতে চাইলে:

```bash
# Docker image build করুন
docker build -t task-management-app .

# Container run করুন
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/task_management_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=Qwertyuiop \
  task-management-app

# Browser এ open করুন
http://localhost:8080
```

---

## 📋 Final Checklist

Deploy করার আগে verify করুন:

- [x] PostgreSQL database created on Render
- [x] GitHub repository accessible  
- [x] **Runtime: Docker** selected
- [x] Environment variables properly set:
  - [x] `SPRING_DATASOURCE_URL`
  - [x] `SPRING_DATASOURCE_USERNAME`
  - [x] `SPRING_DATASOURCE_PASSWORD`
  - [x] `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`
  - [x] `SERVER_PORT=8080`
  - [x] `SPRING_JPA_HIBERNATE_DDL_AUTO=update`
- [x] Dockerfile exists in repository root
- [x] Port 8080 in Dockerfile

---

## 🎉 You're Ready!

এখন শুধু **"Create Web Service"** button এ click করুন এবং Render.com আপনার application deploy করবে!

**Deployment time**: প্রথমবার ~5-7 minutes

---

## 💰 Cost

- **Database (PostgreSQL)**: FREE tier (512MB RAM, 90 days retention)
- **Web Service (Docker)**: FREE tier (512MB RAM, 750 hours/month)
- **Total**: **$0/month** (Free tier এ)

> [!TIP]
> Free tier এ service 15 minutes idle থাকলে sleep mode এ যাবে। প্রথম request এ ~1-2 minutes cold start time লাগবে।

Need always-on service? Upgrade to **$7/month** paid tier.
