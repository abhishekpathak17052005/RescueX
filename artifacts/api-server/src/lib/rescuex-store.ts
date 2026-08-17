export type RescuexStatus = "activated" | "assigned" | "enroute" | "arrived" | "resolved";

export type RescuexTimelineItem = {
  label: string;
  complete: boolean;
  time?: string;
};

export type RescuexIncident = {
  id: string;
  userId: string;
  createdAt: string;
  status: RescuexStatus;
  type: string;
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  location: {
    latitude: number;
    longitude: number;
    accuracy: number | null;
    updatedAt: string;
  };
  summary: string;
  timeline: RescuexTimelineItem[];
};

export type RescuexContact = {
  id: string;
  userId: string;
  name: string;
  relationship: string;
  phone: string;
  primary: boolean;
};

const demoLocation = {
  latitude: 37.7749,
  longitude: -122.4194,
  accuracy: 12,
  updatedAt: new Date().toISOString(),
};

export const rescuexStore = {
  users: new Map([
    ["demo-user", { id: "demo-user", email: "demo.user@rescuex.test", name: "Alex Morgan", role: "user" }],
    ["demo-responder", { id: "demo-responder", email: "demo.responder@rescuex.test", name: "RescueX Responder", role: "responder" }],
  ]),
  contacts: new Map<string, RescuexContact[]>([
    [
      "demo-user",
      [
        { id: "contact-1", userId: "demo-user", name: "Maya Chen", relationship: "Partner", phone: "+1 415 555 0182", primary: true },
      ],
    ],
  ]),
  incidents: new Map<string, RescuexIncident>(),
  notifications: [] as { id: string; incidentId: string; message: string; createdAt: string }[],
  demoLocation,
};

export function nowLabel() {
  return new Date().toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
}

export function makeIncidentId() {
  return `RX-${Date.now().toString().slice(-6)}`;
}

export function makeIncident(input: {
  userId: string;
  type?: RescuexIncident["type"];
  severity?: RescuexIncident["severity"];
  location?: RescuexIncident["location"];
}) {
  const incident: RescuexIncident = {
    id: makeIncidentId(),
    userId: input.userId,
    createdAt: new Date().toISOString(),
    status: "activated",
    type: input.type ?? "Medical emergency",
    severity: input.severity ?? "HIGH",
    location: input.location ?? { ...demoLocation, updatedAt: new Date().toISOString() },
    summary: "RescueX is ready to collect a calm, short account of what happened.",
    timeline: [
      { label: "SOS activated", complete: true, time: nowLabel() },
      { label: "Emergency information collected", complete: false },
      { label: "Responder assigned", complete: false },
      { label: "Responder en route", complete: false },
      { label: "Responder arrived", complete: false },
      { label: "Resolved", complete: false },
    ],
  };
  rescuexStore.incidents.set(incident.id, incident);
  return incident;
}

export function updateIncidentStatus(incident: RescuexIncident, status: RescuexStatus) {
  const statusIndex: Record<RescuexStatus, number> = {
    activated: 0,
    assigned: 2,
    enroute: 3,
    arrived: 4,
    resolved: 5,
  };
  incident.status = status;
  const completedThrough = statusIndex[status];
  incident.timeline = incident.timeline.map((item, index) => ({
    ...item,
    complete: index <= completedThrough,
    time: index <= completedThrough ? item.time ?? nowLabel() : item.time,
  }));
  return incident;
}