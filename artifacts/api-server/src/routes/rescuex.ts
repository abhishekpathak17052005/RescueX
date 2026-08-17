import { Router, type IRouter, type Request } from "express";
import { rescuexStore, makeIncident, updateIncidentStatus, type RescuexStatus } from "../lib/rescuex-store";

const router: IRouter = Router();
const allowedStatuses = new Set<RescuexStatus>(["activated", "assigned", "enroute", "arrived", "resolved"]);

function userId(req: Request) {
  const role = req.header("x-demo-role");
  return role === "responder" ? "demo-responder" : "demo-user";
}

function validLocation(value: unknown): value is { latitude: number; longitude: number; accuracy: number | null; updatedAt: string } {
  if (!value || typeof value !== "object") return false;
  const location = value as Record<string, unknown>;
  return typeof location.latitude === "number" && typeof location.longitude === "number";
}

router.post("/auth/register", (req, res) => {
  const email = typeof req.body?.email === "string" ? req.body.email.trim() : "";
  const name = typeof req.body?.name === "string" ? req.body.name.trim() : "";
  if (!email || !name) return res.status(400).json({ message: "Name and email are required." });
  const id = `user-${Date.now()}`;
  rescuexStore.users.set(id, { id, email, name, role: "user" });
  return res.status(201).json({ token: `demo-token-${id}`, user: rescuexStore.users.get(id) });
});

router.post("/auth/login", (req, res) => {
  const email = typeof req.body?.email === "string" ? req.body.email.trim() : "";
  const user = [...rescuexStore.users.values()].find((candidate) => candidate.email === email);
  if (!user) return res.status(401).json({ message: "Demo account not found." });
  return res.json({ token: `demo-token-${user.id}`, user });
});

router.get("/users/me", (req, res) => {
  return res.json(rescuexStore.users.get(userId(req)) ?? rescuexStore.users.get("demo-user"));
});

router.post("/incidents", (req, res) => {
  const location = validLocation(req.body?.location) ? req.body.location : rescuexStore.demoLocation;
  const severity = ["LOW", "MEDIUM", "HIGH", "CRITICAL"].includes(req.body?.severity) ? req.body.severity : "HIGH";
  const incident = makeIncident({ userId: userId(req), type: typeof req.body?.type === "string" ? req.body.type : undefined, severity, location });
  rescuexStore.notifications.push({ id: `notification-${Date.now()}`, incidentId: incident.id, message: "Emergency activated.", createdAt: new Date().toISOString() });
  return res.status(201).json(incident);
});

router.get("/incidents", (req, res) => {
  const requestedUser = userId(req);
  const incidents = [...rescuexStore.incidents.values()].filter((incident) => requestedUser === "demo-responder" || incident.userId === requestedUser);
  return res.json(incidents);
});

router.get("/incidents/:id", (req, res) => {
  const incident = rescuexStore.incidents.get(req.params.id);
  if (!incident) return res.status(404).json({ message: "Incident not found." });
  return res.json(incident);
});

router.patch("/incidents/:id/status", (req, res) => {
  const incident = rescuexStore.incidents.get(req.params.id);
  const status = req.body?.status as RescuexStatus;
  if (!incident) return res.status(404).json({ message: "Incident not found." });
  if (!allowedStatuses.has(status)) return res.status(400).json({ message: "Invalid incident status." });
  updateIncidentStatus(incident, status);
  rescuexStore.notifications.push({ id: `notification-${Date.now()}`, incidentId: incident.id, message: `Responder status: ${status}.`, createdAt: new Date().toISOString() });
  return res.json(incident);
});

router.post("/incidents/:id/location", (req, res) => {
  const incident = rescuexStore.incidents.get(req.params.id);
  if (!incident) return res.status(404).json({ message: "Incident not found." });
  if (!validLocation(req.body?.location)) return res.status(400).json({ message: "A valid location is required." });
  incident.location = { ...req.body.location, updatedAt: new Date().toISOString() };
  return res.json(incident.location);
});

router.get("/incidents/:id/timeline", (req, res) => {
  const incident = rescuexStore.incidents.get(req.params.id);
  if (!incident) return res.status(404).json({ message: "Incident not found." });
  return res.json(incident.timeline);
});

router.post("/incidents/:id/ai-session", (req, res) => {
  const incident = rescuexStore.incidents.get(req.params.id);
  if (!incident) return res.status(404).json({ message: "Incident not found." });
  incident.summary = typeof req.body?.summary === "string" && req.body.summary.trim()
    ? req.body.summary.trim()
    : "User reported a high-severity medical emergency. One person may be injured. The current location has been shared.";
  updateIncidentStatus(incident, "assigned");
  return res.status(201).json({ incident, provider: process.env.VAPI_API_KEY ? "vapi" : "demo", connected: true });
});

router.post("/vapi/webhook", (req, res) => {
  const incidentId = typeof req.body?.incidentId === "string" ? req.body.incidentId : "";
  const incident = rescuexStore.incidents.get(incidentId);
  if (incident && typeof req.body?.summary === "string") incident.summary = req.body.summary;
  return res.json({ received: true, demoMode: !process.env.VAPI_API_KEY });
});

router.get("/emergency-contacts", (req, res) => {
  return res.json(rescuexStore.contacts.get(userId(req)) ?? []);
});

router.post("/emergency-contacts", (req, res) => {
  const name = typeof req.body?.name === "string" ? req.body.name.trim() : "";
  const phone = typeof req.body?.phone === "string" ? req.body.phone.trim() : "";
  if (!name || !phone) return res.status(400).json({ message: "Name and phone are required." });
  const contacts = rescuexStore.contacts.get(userId(req)) ?? [];
  const contact = { id: `contact-${Date.now()}`, userId: userId(req), name, phone, relationship: req.body?.relationship ?? "Emergency contact", primary: contacts.length === 0 };
  rescuexStore.contacts.set(userId(req), [...contacts, contact]);
  return res.status(201).json(contact);
});

router.patch("/emergency-contacts/:id", (req, res) => {
  const contacts = rescuexStore.contacts.get(userId(req)) ?? [];
  const contact = contacts.find((item) => item.id === req.params.id);
  if (!contact) return res.status(404).json({ message: "Contact not found." });
  Object.assign(contact, {
    name: typeof req.body?.name === "string" ? req.body.name.trim() : contact.name,
    phone: typeof req.body?.phone === "string" ? req.body.phone.trim() : contact.phone,
    relationship: typeof req.body?.relationship === "string" ? req.body.relationship.trim() : contact.relationship,
    primary: typeof req.body?.primary === "boolean" ? req.body.primary : contact.primary,
  });
  rescuexStore.contacts.set(userId(req), contacts.map((item) => ({ ...item, primary: item.id === contact.id ? contact.primary : contact.primary ? false : item.primary })));
  return res.json(contact);
});

router.delete("/emergency-contacts/:id", (req, res) => {
  const contacts = rescuexStore.contacts.get(userId(req)) ?? [];
  rescuexStore.contacts.set(userId(req), contacts.filter((item) => item.id !== req.params.id));
  return res.status(204).send();
});

router.get("/responders", (_req, res) => {
  return res.json([{ id: "demo-responder", name: "RescueX Responder", status: "online", distance: "0.8 mi" }]);
});

router.post("/incidents/:id/assign", (req, res) => {
  const incident = rescuexStore.incidents.get(req.params.id);
  if (!incident) return res.status(404).json({ message: "Incident not found." });
  updateIncidentStatus(incident, "assigned");
  return res.json({ incident, responderId: req.body?.responderId ?? "demo-responder" });
});

export default router;