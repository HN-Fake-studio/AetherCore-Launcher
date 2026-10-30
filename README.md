# AetherCore Launcher (ai)

## 📌 ภาพรวมโครงการ (Project Overview)
**AetherCore Launcher** คือโปรเจกต์ Android Minecraft Java Launcher ระดับพรีเมียมที่พัฒนาต่อยอดโดยใช้ฐานโค้ดหลักจาก `ZalithLauncher2` ออกแบบมาด้วยภาษา **Kotlin**, **Jetpack Compose**, และ **Material Design 3** มุ่งเน้นประสิทธิภาพสูง ขนาดแอปเริ่มต้นกะทัดรัด และระบบเสริมอัจฉริยะ

---

## 📂 โครงสร้างโฟลเดอร์และการตั้งชื่อ (Storage & Naming Convention)
โปรเจกต์ทั้งหมดถูกจัดเก็บไว้ที่ตำแหน่ง:
`Download/AetherCore_Project_(ai)/`

โมดูล โฟลเดอร์ และไฟล์โค้ด Kotlin ทุกไฟล์ในช่วงพัฒนานี้ จะมีป้ายกำกับ `_(ai)` กำกับไว้เพื่อความสะดวกรวดเร็วในการตรวจสอบ และสามารถรันสคริปต์ทำความสะอาดเพื่อลบป้ายกำกับออกได้ทันทีเมื่อโปรเจกต์เสร็จสมบูรณ์

---

## 🛠️ ระบบหลักที่สำคัญ (Core Modules)
 1. Modular Runtime Downloader
- **ไฟล์:** `app-core_(ai)/src/main/java/com/aethercore/launcher/runtime/ModularRuntimeDownloaderManager_(ai).kt`
- **หน้าที่:** ควบคุมขนาด Base APK ให้มีขนาดเล็กกว่า 100 MB และดาวน์โหลดรันไทม์จำเป็น On-Demand เช่น JRE 17, JRE 21 และ Vulkan/Zink Drivers ตามสเปกฮาร์ดแวร์ของผู้ใช้

 2. Multi-Scenario Mode Switcher
- **ไฟล์:** `app-core_(ai)/src/main/java/com/aethercore/launcher/switcher/MultiScenarioModeSwitcherManager_(ai).kt`
- **หน้าที่:** ระบบสลับ 4 โหมดการใช้งานอัจฉริยะ:
  - *Extreme Gaming Mode_(ai)*: รีดพลังสูงสุด, เคลียร์ RAM, บังคับใช้ Vulkan/Zink
  - *Battery Saver & Travel Mode_(ai)*: จำกัด FPS, ลดความละเอียด, OLED Black Theme ประหยัดแบต
  - *Developer & Modding Playground_(ai)*: เปิด Floating Console และสลับ Mod Loader (Fabric/Forge)
  - *Offline & Local LAN Mode_(ai)*: เล่นแบบ Offline และค้นหาห้อง LAN โดยไม่ต้องเชื่อมต่ออินเทอร์เน็ต

 3. Launcher UI Extension / Plugin API System (.eamp)
- **ไฟล์ SDK:** `aethercore-sdk_(ai)/src/main/java/com/aethercore/sdk/AetherPluginApi_(ai).kt`
- **ไฟล์ Loader:** `app-core_(ai)/src/main/java/com/aethercore/launcher/plugin/UiPluginLoaderManager_(ai).kt`
- **หน้าที่:** ระบบสถาปัตยกรรมปลั๊กอิน `.eamp` (Extension Archive) ใช้ `DexClassLoader` ให้นักพัฒนาภายนอกเขียน UI, Custom Widgets หรือเปลี่ยนธีมของ Launcher ได้

 4. AI Direct Pipeline System
- **ไฟล์:** `app-core_(ai)/src/main/java/com/aethercore/launcher/ai/AiDirectPipelineManager_(ai).kt`
- **หน้าที่:** ปุ่มอัจฉริยะ **"🤖 ส่งวิเคราะห์ด่วนให้ Gemini AI"** ในหน้า Log / Crash Report ซึ่งจะรวบรวมไฟล์ `latest.log`, System Specs, และ Exit Code จัดฟอร์มเป็น Prompt และเปิดเบราว์เซอร์ส่งให้ Gemini วิเคราะห์ทันทีใน 1 คลิก
-----------------------------------------------
