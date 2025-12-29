# 🚀 Render.com Deployment Guide

এই guide অনুসরণ করে আপনি সহজেই আপনার Task Management System Render.com এ deploy করতে পারবেন।

## ✅ আপনার App Already Ready!

আপনার application ইতিমধ্যে **production-ready** কারণ:
- ✅ Environment variables ব্যবহার করছে (username, password hardcoded নেই)
- ✅ MySQL এবং PostgreSQL উভয়ই support করে
- ✅ Docker configuration আছে
- ✅ GitHub repository তে code push করা আছে

## 📋 Deployment Steps

### Step 1: PostgreSQL Database তৈরি করুন

1. [Render.com Dashboard](https://dashboard.render.com/) এ যান
2. **New +** → **PostgreSQL** select করুন
3. Database details fill করুন:
   - **Name**: `task-management-db` (বা আপনার পছন্দের নাম)
   - **Database**: `task_management_db`
   - **User**: auto-generated হবে
   - **Region**: Singapore (বাংলাদেশের কাছে)
   - **Instance Type**: Free tier select করুন
4. **Create Database** button এ click করুন
5. Database create হলে **Internal Database URL** copy করুন

> [!IMPORTANT]
> Database URL এর format হবে: `postgresql://user:password@host:5432/database`

### Step 2: Web Service তৈরি করুন

1. **New +** → **Web Service** select করুন
2. GitHub repository connect করুন:
   - **Repository**: `Prottaybdbl/Shipping-Logistics-Management`
3. Service details fill করুন:
   - **Name**: `task-management-app` (বা আপনার পছন্দের নাম)
   - **Region**: Singapore
   - **Branch**: `main`
   - **Root Directory**: খালি রাখুন
   - **Runtime**: `Java`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/task-management-system-1.0.0.jar`

### Step 3: Environment Variables Configure করুন

**Environment** section এ নিচের variables add করুন:

```bash
# Database Configuration (আপনার Render PostgreSQL URL দিয়ে replace করুন)
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxxxx.singapore-postgres.render.com:5432/task_management_db
SPRING_DATASOURCE_USERNAME=task_management_db_user
SPRING_DATASOURCE_PASSWORD=xxxxxxxxxxxxx

# Server Configuration
SERVER_PORT=8080

# JPA Configuration (PostgreSQL এর জন্য)
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

> [!TIP]
> Internal Database URL থেকে username, password, host, database name copy করে উপরের format এ বসান।

### Step 4: Deploy করুন

1. সব configuration check করুন
2. **Create Web Service** button এ click করুন
3. Render automatically build এবং deploy শুরু করবে

Build log এ এরকম দেখাবে:
```
[INFO] Building Task Management System 1.0.0
[INFO] Packaging task-management-system
[INFO] BUILD SUCCESS
```

Deploy সম্পূর্ণ হলে আপনার app live হবে: `https://task-management-app.onrender.com`

## 🔧 Environment Variables Details

### Database URL Format

Render.com এ PostgreSQL URL এর format:
```
Internal Database URL: postgresql://user:pass@host:5432/dbname
JDBC Format: jdbc:postgresql://host:5432/dbname
```

**Example:**
```bash
# Render থেকে পাবেন:
postgresql://task_db_user:Xy9z...abc@dpg-xxxxx.singapore-postgres.render.com:5432/task_management_db

# Environment variable এ দিতে হবে:
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxxxx.singapore-postgres.render.com:5432/task_management_db
SPRING_DATASOURCE_USERNAME=task_db_user
SPRING_DATASOURCE_PASSWORD=Xy9z...abc
```

### All Required Environment Variables

| Variable | Value | Description |
|----------|-------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://...` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | Render DB username | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Render DB password | Database password |
| `SERVER_PORT` | `8080` | Application port |
| `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT` | `org.hibernate.dialect.PostgreSQLDialect` | PostgreSQL dialect |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Auto-create tables |

## 🎯 Common Issues & Solutions

### Issue 1: Build Failed - Java Version
**Error**: `Unsupported Java version`
**Solution**: Render.com free tier এ Java 17 support করে। আপনার `pom.xml` এ ইতিমধ্যে Java 17 configured আছে।

### Issue 2: Database Connection Failed
**Error**: `Could not connect to database`
**Solution**: 
- Environment variables সঠিকভাবে set করা আছে কিনা check করুন
- Internal Database URL ব্যবহার করছেন কিনা verify করুন (External URL নয়)

### Issue 3: Application Crashed After Deploy
**Error**: `Application error`
**Solution**:
- Logs check করুন (Dashboard → Service → Logs)
- `SPRING_JPA_HIBERNATE_DDL_AUTO=update` set করা আছে কিনা verify করুন

## 📊 Post-Deployment

### Access Your Application
```
URL: https://your-service-name.onrender.com
Login: Super Admin credentials (যেটা আপনার code এ আছে)
```

### Database Management
- Render Dashboard → PostgreSQL → **PSQL Command** দিয়ে database access করতে পারবেন
- pgAdmin বা DBeaver দিয়ে External Database URL ব্যবহার করে connect করতে পারবেন

## 🔐 Security Recommendations

> [!CAUTION]
> Production এ deploy করার আগে default admin password change করুন!

1. **Default Password Change**: 
   - আপনার `SecurityConfig` বা initialization code এ hardcoded password আছে
   - First login এর পর immediately change করুন

2. **Environment Variables**: 
   - সব sensitive data environment variables এ রাখুন
   - GitHub এ কখনও credentials push করবেন না

3. **HTTPS**: 
   - Render.com automatically HTTPS provide করে
   - সব internal API calls HTTPS দিয়ে হয়

## 💰 Pricing

### Free Tier Includes:
- ✅ 750 hours/month (1টি service continuously চালাতে পারবেন)
- ✅ PostgreSQL database (90 days data retention)
- ✅ Auto deploy on git push
- ✅ Custom domain support
- ⚠️ Service 15 minutes idle থাকলে sleep যাবে (cold start ~1-2 minutes)

### Paid Tier ($7/month):
- Always-on service (no sleep)
- Better performance
- Priority support

## 🔄 Auto-Deploy Setup

Render.com এ auto-deploy ইতিমধ্যে enabled:
- GitHub এ code push করলেই automatically deploy হবে
- Build logs realtime দেখতে পারবেন
- Failed deploy হলে previous version active থাকবে

---

## ✅ Final Checklist

Deploy করার আগে verify করুন:

- [ ] GitHub repository public/accessible
- [ ] PostgreSQL database created on Render
- [ ] All environment variables properly set
- [ ] Build command correct: `mvn clean package -DskipTests`
- [ ] Start command correct: `java -jar target/task-management-system-1.0.0.jar`
- [ ] Java version 17 in `pom.xml`
- [ ] PostgreSQL dependency added in `pom.xml`

---

**Need Help?** Render.com এর [Documentation](https://render.com/docs) অথবা [Community Forum](https://community.render.com/) দেখুন।
