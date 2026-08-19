# 🚑 RescueX

### AI-Powered Emergency Response & Coordination Platform

RescueX is an AI-assisted emergency response platform designed to connect patients, ambulances, and hospitals through a real-time emergency coordination system.

The platform combines **voice AI, emergency incident management, GPS location, ambulance coordination, hospital selection, and real-time cloud synchronization** into a single ecosystem.

> ⚠️ **Current Status:** RescueX is currently an MVP / simulation. Ambulance dispatch and hospital availability are simulated and are not connected to real emergency-service providers.

---

## 🚨 Problem

During an emergency, a patient may not be able to communicate their location, describe their condition clearly, contact relatives, find the right hospital, or coordinate with an ambulance.

At the same time, ambulance and hospital teams may lack real-time information about the patient and the destination facility.

RescueX aims to reduce this communication gap by creating a connected emergency-response workflow.

---

## 💡 Solution

RescueX provides a voice-first emergency workflow:

```text
Patient presses SOS
        ↓
AI Assistant
        ↓
"Do you need an ambulance?"
        ↓
Ambulance dispatch
        ↓
Patient describes emergency
        ↓
Gemini classifies the emergency
        ↓
Suitable hospital identified
        ↓
Ambulance receives destination
        ↓
Hospital receives incoming emergency
        ↓
Real-time status synchronization
