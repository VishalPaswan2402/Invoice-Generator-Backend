# 🚀 Invoice Generator Backend (Spring Boot)

A complete **Spring Boot (Java)** backend application for managing invoices, authentication with OTP, company details,
PDF generation, Excel export, and automated email notifications with retry logic.

## ✨ Features

### 🔐 Authentication & Security

- User Signup with OTP verification
- Login with hashed passwords
- Forgot Password with OTP
- Reset Password
- Email verification during signup & recovery

### 📄 Invoice Management

- Create invoices
- Edit invoices
- Update invoices
- Delete invoices
- Download invoice as **PDF**
- Email invoice to customer
- Insert company details into invoice template automatically

### 🏢 Company Management

- Add company details
- Edit company details
- Used across invoices and emails

### 📈 Dashboard Stats

- Total invoices generated
- Total revenue
- Pending vs Paid invoices
- Real-time analytics

### 📊 Excel Export

Export invoice reports for:

- Last **15 days**
- Last **7 weeks**
- Last **1 year**
- **All-time invoices**

### 📧 Email System (Auto-Retry)

- Send invoice via email
- OTP emails
- Automatic **3 retry attempts** if email fails

### 📌 Email Retry Logic (Queue-Based – Every 30 Minutes)

    ```bash

    Send Email
    │
    ├─✔ Success → Completed
    │
    └─✖ Failure → Add to Retry Queue
                     │
             Queue runs every 30 min
                     │
             Retry (max 3 attempts)
                     │
         ┌───────────┴───────────┐
     ✔ Success              ✖ Failed after 3 attempts

    ```

## 🛠️ Tech Stack

- Java 17+
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- MongoDB
- Spring Mail
- Apache POI (Excel export)
- React2Pdf (PDF generation)
- Maven

## 🖥️ Project Structure

    ```bash

    Invoice-Generator-Backend/
    │
    ├── src/
    │ └── main/
    │ ├── java/
    │ │ └── com/invoice/
    │ │ ├── controller/                     # REST controllers
    │ │ ├── service/                        # Business logic services
    │ │ ├── repository/                     # Spring Data JPA repositories
    │ │ ├── entity/                         # JPA entity classes
    │ │ ├── dto/                            # Request/Response DTOs
    │ │ ├── config/                         # Security & config classes
    │ │ ├── utils/                          # Email, PDF, Excel utilities
    │ │ └── exception/                      # Custom exceptions (if any)
    │ │
    │ └── resources/
    │ ├── templates/                        # Invoice HTML template for PDF
    │ ├── static/                           # Static assets (if any)
    │ ├── application.properties            # Main configuration file
    │ └── banner.txt                        # Custom Spring Boot banner (optional)
    │
    ├── pom.xml                             # Maven dependencies
    └── README.md                           # Project documentation

    ```

## Installation

### Prerequisites

- Java 17+ installed
- Maven installed
- Spring Boot 3+ compatible environment
- MongoDb database running
- A code editor like IntelliJ IDEA or VS Code

### Steps to run project

1. **Clone the repository :**

    ```bash
    git clone https://github.com/VishalPaswan2402/Invoice-Generator-Backend.git
    cd Invoice-Generator-Backend
    ```

2. **Configure application properties :**

    ```bash
    src/main/resources/application.properties
    ```

3. **Add your database , Jwt & email credentials into a .env file :**

    ```bash
    SPRING_APPLICATION_NAME=invoiceGen
    SERVER_PORT=3000
    LOG_LEVEL=DEBUG
   
    ### MongoDb Config

    SPRING_DATA_MONGODB_URI=your_mongodb_connection_string
   
    ### JWT Token Config

    JWT_SECRET_KEY=your_secret_key
    JWT_EXPIRATION=expiry_time_in_second

    ### Email Config

    SPRING_MAIL_HOST=smtp.ethereal.email
    SPRING_MAIL_PORT=587
    SPRING_MAIL_USERNAME=your_email_app_username
    spring.mail.password=your_email_app_password
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
   
    ```

4. **Build the project :**

    ```bash
    mvn clean install
    ```

5. **Run the Spring Boot application :**
    ```bash
    mvn spring-boot:run
    ```

6. **Backend Server Running :**
    - The server will be running on `http://localhost:8080`.