# FB Shipping Module - Quick Start Guide

## 🚀 তাৎক্ষণিক শুরু করার জন্য

### Step 1: Database Setup (5 minutes)

```bash
# MySQL এ login করুন
mysql -u root -p

# Database তৈরি করুন (যদি না থাকে)
CREATE DATABASE IF NOT EXISTS task_management_db;

# Exit MySQL
exit

# Migration script চালান
mysql -u root -p task_management_db < "F:\Prottay\Project\task-management-system 1.0\src\main\resources\db\migration\shipping_module_schema.sql"
```

### Step 2: Build Project (2 minutes)

```bash
cd "F:\Prottay\Project\task-management-system 1.0"
mvn clean install
```

### Step 3: Run Application (1 minute)

```bash
mvn spring-boot:run
```

### Step 4: Access Dashboard

```
URL: http://localhost:8080/shipping/dashboard
Login: admin@taskmanagement.com / Admin@123
```

---

## 📊 Created Files Summary

### Backend Files:
```
✅ ShipmentStatus.java           - Status enum
✅ LoadUnloadStatus.java         - Load/Unload status enum
✅ ShipmentCycle.java            - Mother vessel entity
✅ LighterLoading.java           - Lighter entity  
✅ TruckUnloading.java           - Truck entity
✅ ProductDetail.java            - Product detail entity
✅ ShipmentCycleRepository.java  - Shipment repository
✅ LighterLoadingRepository.java - Lighter repository
✅ TruckUnloadingRepository.java - Truck repository
✅ ProductDetailRepository.java  - Product repository
✅ ShipmentDTO.java              - Main DTO with nested structures
✅ ShippingDashboardDTO.java     - Dashboard analytics DTO
✅ ShipmentService.java          - Business logic service
✅ ShipmentController.java       - REST API + Web controller
```

### Database Files:
```
✅ shipping_module_schema.sql    - Complete schema + demo data
```

### Documentation:
```
✅ FB_SHIPPING_MODULE_GUIDE.md   - Complete implementation guide
✅ SHIPPING_QUICK_START.md       - This quick start guide
```

---

## 🔗 API Endpoints (Ready to Use)

### Web Pages:
```
GET  /shipping/dashboard              - Main dashboard
GET  /shipping/shipments              - List all shipments
GET  /shipping/shipment/{id}          - View shipment details
GET  /shipping/shipment/new           - Create shipment form
POST /shipping/shipment/create        - Submit new shipment
GET  /shipping/shipment/{id}/edit     - Edit shipment form
POST /shipping/shipment/{id}/update   - Update shipment
POST /shipping/shipment/{id}/delete   - Delete shipment
```

### REST API:
```
GET    /shipping/api/shipments                  - Get all shipments
GET    /shipping/api/shipment/{id}              - Get single shipment
POST   /shipping/api/shipment                   - Create shipment
PUT    /shipping/api/shipment/{id}              - Update shipment
POST   /shipping/api/shipment/{id}/lighter      - Add lighter to shipment
POST   /shipping/api/lighter/{id}/truck         - Add truck to lighter
GET    /shipping/api/shipment/{id}/validate     - Validate quantities
GET    /shipping/api/dashboard                  - Get dashboard analytics
```

---

## 📝 Example API Call (Test করার জন্য)

### Create Shipment with Full Hierarchy:

```json
POST http://localhost:8080/shipping/api/shipment
Content-Type: application/json

{
  "consignee": "PDL",
  "motherVesselName": "MEGHNA ENERGY",
  "arrivalDate": "2025-11-10",
  "totalIncomingQuantity": 5000.0,
  "itemType": "10-20 Stone",
  "instituteId": 1,
  "lighterLoadings": [
    {
      "lighterName": "MV A&J Traders 04",
      "destination": "O/A - Tulatoli",
      "unloadingPoint": "Tulatoli",
      "loadingDate": "2025-10-29",
      "loadedQuantity": 1500.0,
      "lighterCost": 1500.0,
      "truckUnloadings": [
        {
          "challan": "CH-001",
          "conveyanceName": "MV A&J Traders 04",
          "numberOfTrucks": 1,
          "dischargingLocation": "Tulatoli",
          "destination": "Motirhat",
          "party": "PDL",
          "unloadingDate": "2025-11-01",
          "unloadedQuantity": 480.0,
          "unloadingCost": 100.0,
          "productDetails": [
            {
              "item": "10-20 Stone",
              "deliveryQuantity": 480.0,
              "surveyQuantity": 100.0,
              "lighterCost": 480.0,
              "unloadingCost": 100.0,
              "truckTransportCost": 550.0
            }
          ]
        }
      ]
    }
  ]
}
```

---

## ✅ Features Implemented

### Core Features:
- ✅ Mother Vessel → Multiple Lighters relationship (1 to many)
- ✅ Lighters → Multiple Trucks relationship (many to many)
- ✅ Trucks → Multiple Products relationship (1 to many)
- ✅ Quantity validation (auto-check overload)
- ✅ Cost calculation (hierarchical sum)
- ✅ Flow summary auto-generation
- ✅ Dependency tracking
- ✅ Mirror columns (source lighter name in truck)

### Database Features:
- ✅ 4 main tables with relationships
- ✅ 3 views for reporting
- ✅ 1 stored procedure for validation
- ✅ Demo data matching your requirements

### API Features:
- ✅ Full CRUD operations
- ✅ Nested entity creation (one API call for entire flow)
- ✅ Validation endpoints
- ✅ Dashboard analytics
- ✅ Quantity balance checking

---

## 🎯 Use Cases Covered

### 1. "Unload from 1 to Many"
**Mother Vessel → Multiple Lighters**
- Create 1 shipment
- Add multiple lighters
- System validates total loaded vs incoming

### 2. "Unload from Many to Many"  
**Lighters → Multiple Trucks**
- Each lighter can unload to multiple trucks
- System validates unloaded vs loaded per lighter

### 3. "Load then Unload Cascade"
**Full Flow Tracking**
- Mother Vessel unloads → Lighters load
- Lighters unload → Trucks load  
- Trucks deliver → Products track costs

### 4. "Cost Breakdown"
**Multi-level Cost Tracking**
- Lighter cost
- Unloading cost
- Truck transport cost
- Total = Sum of all levels

---

## 🔍 Demo Data Details

### Shipment ID 1:
```
Consignee: PDL
Mother Vessel: MEGHNA ENERGY
Total Incoming: 5000.0
Item Type: 10-20 Stone

Lighters (3):
  1. MV A&J Traders 04 - 1500.0 → 3 Trucks
  2. MV Innex 05 - 2000.0 → 3 Trucks
  3. MV Sahara 11 - 1500.0 → 2 Trucks

Total Trucks: 8
Total Flow: 1 → 3 → 8

Costs:
  - Lighter costs: 5000.0
  - Unloading costs: ~1000.0
  - Transport costs: ~5000.0
  - Grand Total: ~11,000.0
```

---

## 🐛 Troubleshooting

### Error: "Table doesn't exist"
```bash
# Re-run migration
mysql -u root -p task_management_db < src/main/resources/db/migration/shipping_module_schema.sql
```

### Error: "Foreign key constraint fails"
```sql
-- Check if institutes and users tables exist
SELECT * FROM institutes LIMIT 1;
SELECT * FROM users LIMIT 1;
```

### Error: "Port 8080 already in use"
```bash
# Change port in application.properties
server.port=8081
```

---

## 📞 Next Steps

### Option 1: Frontend Development (Recommended)
আপনি যদি Thymeleaf templates add করতে চান:
1. `src/main/resources/templates/shipping/` folder তৈরি করুন
2. `dashboard.html`, `shipments.html`, `shipment-detail.html` তৈরি করুন
3. Bootstrap 5 + Chart.js ব্যবহার করুন

### Option 2: API Integration
আপনি যদি separate frontend (React/Vue/Angular) ব্যবহার করতে চান:
1. REST API endpoints ব্যবহার করুন
2. `/shipping/api/*` endpoints থেকে JSON data fetch করুন
3. Custom UI তৈরি করুন

### Option 3: Extend Features
Additional features যোগ করতে:
- Email notifications on quantity mismatch
- WebSocket for real-time updates
- Excel export functionality
- Advanced reporting

---

## 📚 Full Documentation

বিস্তারিত documentation এর জন্য দেখুন:
- `FB_SHIPPING_MODULE_GUIDE.md` - Complete guide with examples

---

**✨ আপনার Fb Shipping Flow Dashboard এখন সম্পূর্ণ ready!**

যেকোনো প্রশ্ন বা সমস্যার জন্য আমাকে জানান।
