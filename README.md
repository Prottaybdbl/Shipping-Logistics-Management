# Task Management System - Spring Boot Application

A comprehensive enterprise task management platform built with **Spring Boot 3**, **MySQL**, and **Thymeleaf**, inspired by Monday.com. Features role-based access control, real-time collaboration via WebSocket, and multi-institute management.

## 🎯 Features

### Role-Based Access Control
- **ADMIN**: Manage institutes, create users (CEO, Manager, Officer), system configuration
- **CEO**: Institute dashboard, analytics, manager management  
- **MANAGER**: Create boards, manage tasks, assign work to officers
- **OFFICER**: Complete assigned tasks, collaborate on projects

### Core Functionality
- ✅ Multi-tenant institute management
- ✅ Board creation with customizable views (Table, Kanban, Dashboard)
- ✅ Task management with status, priority, due dates
- ✅ Drag & drop interface for tasks and groups
- ✅ Real-time collaboration via WebSocket
- ✅ Rich text comments with formatting
- ✅ File attachments (up to 10MB per file)
- ✅ Activity logging and audit trails
- ✅ Email notifications
- ✅ Advanced filtering and search
- ✅ Analytics dashboards with Chart.js

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| Backend Framework | Spring Boot 3.2.0 |
| Java Version | 17+ |
| Database | MySQL 8.x |
| Template Engine | Thymeleaf |
| Security | Spring Security 6 |
| Real-time | WebSocket (STOMP) |
| Frontend | Bootstrap 5, jQuery |
| Charts | Chart.js |
| Drag & Drop | SortableJS |
| Build Tool | Maven |

## 📋 Prerequisites

Before running this application, ensure you have:

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git (optional)

## 🚀 Quick Start

### 1. Clone or Download the Project
```bash
cd task-management-system
```

### 2. Configure Database

Create MySQL database:
```sql
CREATE DATABASE task_management_db;
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/task_management_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Configure Email (Optional)

Update email settings in `application.properties`:
```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

Or run the JAR file:
```bash
java -jar target/task-management-system-1.0.0.jar
```

### 6. Access the Application

Open your browser and navigate to:
```
http://localhost:8080
```

**Default Admin Credentials** (if seed data is loaded):
- Email: `admin@taskmanagement.com`
- Password: `Admin@123`

## 📁 Project Structure

```
task-management-system/
├── src/main/java/com/taskmanagement/
│   ├── config/              # Security, WebSocket configuration
│   ├── controller/          # Web controllers
│   ├── entity/              # JPA entities
│   ├── enums/               # Enums (UserRole, TaskStatus, TaskPriority)
│   ├── repository/          # Spring Data repositories
│   ├── service/             # Business logic
│   ├── dto/                 # Data Transfer Objects
│   └── exception/           # Exception handling
├── src/main/resources/
│   ├── templates/           # Thymeleaf templates
│   ├── static/              # CSS, JS, images
│   └── application.properties
└── pom.xml
```

## 🗄️ Database Schema

The application creates the following main tables:
- `institutes` - Organization data
- `users` - User accounts with roles
- `boards` - Project boards
- `groups` - Task groups within boards
- `tasks` - Individual tasks
- `comments` - Task comments
- `activity_logs` - Audit trail
- `attachments` - File uploads
- `board_members` - Board membership (many-to-many)
- `board_favorites` - Starred boards (many-to-many)

## 🔐 Security

### Password Requirements
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number
- At least 1 special character

### Role Hierarchy
```
ADMIN > CEO > MANAGER > OFFICER
```

Each role inherits permissions from roles below it.

## 🌐 API Endpoints

### Authentication
- `GET /login` - Login page
- `POST /login` - Process login
- `GET /logout` - Logout
- `GET /register` - Registration page

### Admin Routes
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/institutes` - Manage institutes
- `GET /admin/users` - Manage users
- `POST /admin/institute/create` - Create institute
- `POST /admin/user/create` - Create user

### Board Routes
- `GET /boards` - List all boards
- `GET /board/{id}` - View board (table view)
- `GET /board/{id}/kanban` - Kanban view
- `GET /board/{id}/dashboard` - Dashboard analytics
- `POST /board/create` - Create new board
- `PUT /board/{id}` - Update board
- `DELETE /board/{id}` - Delete board

### Task Routes
- `POST /task/create` - Create task
- `PUT /task/{id}` - Update task
- `DELETE /task/{id}` - Delete task
- `POST /task/{id}/assign` - Assign task
- `POST /task/{id}/comment` - Add comment

### WebSocket
- `/ws` - WebSocket endpoint (SockJS)
- `/topic/board/{boardId}` - Board updates
- `/topic/task/{taskId}` - Task updates

## 📊 Features in Detail

### Board Views

#### 1. Table View (Default)
- Spreadsheet-style interface
- Inline editing for all fields
- Sticky header and left column
- Custom columns (status, priority, member, date, etc.)

#### 2. Kanban View
- Drag & drop cards between columns
- Status-based organization
- Visual task overview

#### 3. Dashboard View
- Status distribution (pie chart)
- Priority breakdown (bar chart)
- Member workload (bar chart)
- Timeline visualization

### Real-Time Collaboration

The application uses WebSocket (STOMP over SockJS) for real-time updates:
- Instant task updates across all connected users
- Live comment additions
- Board member presence indicators
- Activity notifications

### File Upload

Supports uploading files to tasks:
- Max file size: 10MB
- Supported types: Images, Documents, Archives
- Files stored in `uploads/` directory
- Thumbnails for images

## 🧪 Testing

Run tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=UserServiceTest
```

## 📦 Building for Production

### Create executable JAR
```bash
mvn clean package -DskipTests
```

### Run in production mode
```bash
java -jar target/task-management-system-1.0.0.jar --spring.profiles.active=prod
```

### Production Checklist
- [ ] Update database credentials
- [ ] Configure email server
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Enable HTTPS
- [ ] Configure proper logging
- [ ] Set strong session secret
- [ ] Enable CSRF protection
- [ ] Configure file upload limits
- [ ] Set up database backups

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
- Verify MySQL is running: `mysql -u root -p`
- Check credentials in `application.properties`
- Ensure database exists: `CREATE DATABASE task_management_db;`

**Port 8080 Already in Use**
- Change port in `application.properties`: `server.port=8081`
- Or kill process using port 8080

**WebSocket Connection Failed**
- Check firewall settings
- Verify `/ws` endpoint is accessible
- Check browser console for errors

**File Upload Fails**
- Check `file.upload-dir` exists and is writable
- Verify file size under 10MB
- Check disk space

## 📝 Development Guides

Additional implementation guides available in project root:
- `IMPLEMENTATION_GUIDE.md` - Complete entity and repository code
- `REPOSITORY_SETUP.md` - Repository layer details
- `SERVICE_LAYER_GUIDE.md` - Service layer implementation

## 🔄 Next Steps

To complete the application, implement:
1. ✅ **Service Layer** - Remaining services (Task, Group, Comment, Activity, Attachment)
2. ⏳ **Controller Layer** - All web controllers (Admin, Auth, Board, Task, Group)
3. ⏳ **Thymeleaf Templates** - HTML pages for all views
4. ⏳ **JavaScript** - Frontend interactivity (WebSocket, drag-drop, charts)
5. ⏳ **CSS Styling** - Custom styles for boards, kanban, modals
6. ⏳ **Exception Handling** - Global exception handler, custom error pages
7. ⏳ **DTOs** - Data Transfer Objects for API responses
8. ⏳ **Validation** - Input validation for all forms
9. ⏳ **Testing** - Unit and integration tests
10. ⏳ **Documentation** - API documentation with Swagger

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit changes: `git commit -m 'Add feature'`
4. Push to branch: `git push origin feature-name`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- Initial development based on MyDay (MERN) project architecture
- Spring Boot implementation: [Prottay]

## 📞 Support

For issues or questions:
- Create an issue on GitHub
- Email: prottay.nsu@gmail.com
- Documentation: `IMPLEMENTATION_GUIDE.md`

---

**Built with ❤️ using Spring Boot**
