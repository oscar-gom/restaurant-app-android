# Restaurant Companion App

This is the Android frontend app for the Restaurant system.

**Backend Repository:** https://github.com/oscar-gom/restaurant-app-backend/tree/main

## What the project does

This project provides a mobile client built with Kotlin and Jetpack Compose to manage core restaurant entities: **Orders**, **Order Items**, and **Menu Items**.

- Create, retrieve, update, and delete these resources via a RESTful API.
- Real-time synchronization using WebSockets to reflect changes instantly (orders and order items).
- Minimalist UI that follows Google Material 3 guidelines with dynamic colors and soft elevations.

## Why the project is useful

- **Operational Efficiency:** Staff can manage orders and items quickly, with immediate updates across devices.
- **Real-time Synchronization:** WebSocket topics push new/updated/deleted data to the app without manual refresh, ideal in busy kitchen environments.
- **Scalable Integration:** Works against the Spring Boot backend and can adapt to different environments by changing the base URL.

## How users can get started with the project

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- A running instance of the backend (see repository above)

### Setup & Running

1) **Clone the repository**

```bash
git clone <repository-url>
cd restaurantcompanionapp
```

2) **Configure API base URL (important)**
Update the IP/host used by the API client in your `ApiService` (Retrofit/OkHttp config) so it points to your environment.

Examples:
- Emulator: `http://10.0.2.2:8080/`
- Physical device (same network): `http://<your-local-ip>:8080/`
- Remote server: `https://<your-domain>/`

Also ensure the WebSocket URL matches your backend:
- WebSocket endpoint: `ws://<host>:<port>/ws`
- Subscribe to topics: `/topic/orders` and `/topic/order-items`

3) **HTTP (cleartext) IPs require XML config**
If you use `http` (not `https`) for local development, add/adjust your domain in `app/src/main/res/xml/network_security_config.xml` and ensure your `AndroidManifest.xml` references this config. Example entry:

```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">192.168.1.161</domain>
        <!-- Add your local IP or host here if different -->
    </domain-config>
</network-security-config>
```

And in `AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
</application>
```

4) **Build & Run**
- Open the project in Android Studio
- Sync Gradle and run on an emulator or device

## WebSocket Functionality

This app consumes real-time updates exposed by the backend using STOMP-like topics over WebSocket.

- **WebSocket Endpoint:** `/ws`
- **Topics:**
  - **`/topic/orders`**: Receives updates when an **Order** is created, updated, or deleted. Payload: the `Order` object.
  - **`/topic/order-items`**: Receives updates when an **Order Item** is created, updated, or deleted. Payload: `OrderItemResponseDTO`.

## Key Screens (space for screenshots)

### Orders View
- Filter orders (All vs Active/Pending) with segmented buttons
- Status chips with clear color coding (Pending, Completed, Cancelled)
- Tap a card to open Order Detail
- Floating Action Button for creating orders (opens bottom sheet)

<img width="180" height="390" alt="imagen" src="https://github.com/user-attachments/assets/579bb433-da69-4305-b18f-151554f8cf5a" />
<img width="180" height="390" alt="imagen" src="https://github.com/user-attachments/assets/39f51ad2-9666-407e-9893-43d76e1bfd3e" />
<img width="180" height="390" alt="imagen" src="https://github.com/user-attachments/assets/fcc9a84e-fc2f-4e17-bee9-f9e739cff2b4" />

### Order Detail View
- Shows order information and its order items
- Bottom sheet to add order items (menu item select, quantity, special instructions)
- Edit or delete order items
- Change order status with a selector

<img width="180" height="390" alt="imagen" src="https://github.com/user-attachments/assets/c1818880-16f1-4999-aff8-9d2aa9626de2" />

### Menu Items View
- List, create, edit, and delete menu items
- Prevents deleting an item if it’s used in any order item and shows the related order IDs

<img width="180" height="390" alt="imagen" src="https://github.com/user-attachments/assets/630ef5f9-a1da-4fb3-bdc4-f5a43bcb45ea" />

### Settings
- App preferences and global actions
- "Delete All" (with confirmation) to clear datasets when needed

## Design principles
- Minimalist look aligned with Material 3: pastel palette, soft elevations, simple shapes
- Dynamic color support (Material You) where applicable
- Status colors: Cancelled uses error color, Completed uses success green, Pending uses a distinct neutral tone

## Tech stack
- Kotlin, Jetpack Compose, Material 3
- ViewModel + State
- Retrofit + OkHttp for REST
- WebSockets for real-time updates
- Gradle (KTS), AndroidX

## Troubleshooting
- If WebSocket returns 404, verify the path `/ws` and backend availability
- If REST calls fail, confirm base URL/IP and network permissions (AndroidManifest)
- For emulator access to localhost, use `10.0.2.2`
