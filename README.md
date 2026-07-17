# Android Coding Challenge: System Integration & BLE

Welcome to your technical interview task! We are excited to see how you approach modern Android system integrations.

**Focus:** System APIs, permission handling, and modern Android lifecycle constraints (Android 14+).
**UI Requirement:** Keep it extremely simple. We care about the architecture and system interaction, not pixel-perfect design.

---

## Part 1: The Floating Quick-Launcher (Core Task)

**Goal:** Create an app that displays a persistent, floating overlay button on the screen. Clicking this button should launch another app (e.g., the default Calculator or Web Browser), regardless of which app the user is currently using.

### Requirements:
1. **Permissions & Settings:**
   * Check and request the `SYSTEM_ALERT_WINDOW` permission. Redirect the user to the system settings if not granted.
   * Handle the `POST_NOTIFICATIONS` permission required for modern Android versions.
2. **Foreground Service:**
   * The overlay logic must be hosted inside a Foreground Service to prevent the system from killing it.
   * Provide a persistent notification while the service is running.
   * *Hint: Pay attention to the required `foregroundServiceType` in your `AndroidManifest.xml`.*
3. **The Overlay:**
   * Use the `WindowManager` to draw a simple View (e.g., a colored Button or an Icon) on top of other apps.
4. **Background Activity Launch (BAL):**
   * When the overlay button is clicked, use an `Intent` to launch an external app.
   * Ensure the Intent is configured correctly to start an Activity from a Service context.

---

## Part 2: Device Announcer (Bonus Task)

**Goal:** If you finish early, extend the app (or create a second mini-app) to broadcast and discover device information via Bluetooth Low Energy (BLE).

*Note: You can use two physical devices for testing, or mock the receiver side if only one device is available.*

### Requirements:
1. **BLE Advertisement (Device A):**
   * The app should advertise a small data payload containing:
     * A custom Device Name (String)
     * An IP Address (String)
     * A Port Number (Integer)
   * *Hint: You can encode this data into the `AdvertiseData` payload (e.g., using Service Data).*
2. **BLE Scanning (Device B):**
   * The app should scan for this specific advertisement.
   * Upon discovering the device, parse the payload and display the Name, IP, and Port on the screen.
3. **Modern Bluetooth Permissions:**
   * Ensure you are using the modern API permissions (`BLUETOOTH_ADVERTISE`, `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`) introduced in recent Android versions, rather than relying on legacy location permissions.

---

## Evaluation Criteria
During the review, we will look at:
* **Modern API Usage:** How you handle Foreground Services and permissions on newer Android versions (API 34+).
* **Robustness:** Does the app crash if permissions are denied? How is the Service lifecycle managed?
* **Code Structure:** Even for a small task, we appreciate a clean separation of concerns.
* **Documentation:** Please provide brief comments explaining your critical architectural choices.

// Note: Please write all your code comments in English.

Happy coding!