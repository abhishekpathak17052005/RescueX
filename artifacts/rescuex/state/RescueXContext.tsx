import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Haptics from 'expo-haptics';
import React, { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';
import { getCurrentLocation, LocationSnapshot } from '@/services/location';

export type UserRole = 'user' | 'responder';
export type IncidentStatus = 'activated' | 'assigned' | 'enroute' | 'arrived' | 'resolved';

export type TimelineItem = {
  label: string;
  complete: boolean;
  time?: string;
};

export type Incident = {
  id: string;
  createdAt: string;
  status: IncidentStatus;
  type: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  location: LocationSnapshot;
  summary: string;
  timeline: TimelineItem[];
};

export type EmergencyContact = {
  id: string;
  name: string;
  relationship: string;
  phone: string;
  primary: boolean;
};

type StoredState = {
  incidents: Incident[];
  contacts: EmergencyContact[];
  role: UserRole;
};

type RescueXContextValue = StoredState & {
  hydrated: boolean;
  activeIncident: Incident | null;
  demoMode: boolean;
  location: LocationSnapshot | null;
  activateSOS: () => Promise<Incident>;
  updateIncidentStatus: (status: IncidentStatus) => void;
  completeAssistant: (summary: string) => void;
  addContact: (contact: Omit<EmergencyContact, 'id'>) => void;
  deleteContact: (id: string) => void;
  setPrimaryContact: (id: string) => void;
  setRole: (role: UserRole) => void;
  refreshLocation: () => Promise<void>;
};

const STORAGE_KEY = '@rescuex/state/v1';
const initialContacts: EmergencyContact[] = [
  { id: 'contact-1', name: 'Maya Chen', relationship: 'Partner', phone: '+1 415 555 0182', primary: true },
  { id: 'contact-2', name: 'Daniel Ortiz', relationship: 'Brother', phone: '+1 415 555 0127', primary: false },
];

const initialLocation: LocationSnapshot = {
  latitude: 37.7749,
  longitude: -122.4194,
  accuracy: 12,
  updatedAt: new Date().toISOString(),
};

const RescueXContext = createContext<RescueXContextValue | null>(null);

function formatTime() {
  return new Date().toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
}

function makeDemoIncident(location: LocationSnapshot): Incident {
  return {
    id: `RX-${Date.now().toString().slice(-6)}`,
    createdAt: new Date().toISOString(),
    status: 'activated',
    type: 'Medical emergency',
    severity: 'HIGH',
    location,
    summary: 'RescueX is ready to collect a calm, short account of what happened.',
    timeline: [
      { label: 'SOS activated', complete: true, time: formatTime() },
      { label: 'Emergency information collected', complete: false },
      { label: 'Responder assigned', complete: false },
      { label: 'Responder en route', complete: false },
      { label: 'Responder arrived', complete: false },
      { label: 'Resolved', complete: false },
    ],
  };
}

export function RescueXProvider({ children }: PropsWithChildren) {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [contacts, setContacts] = useState<EmergencyContact[]>(initialContacts);
  const [role, setRoleState] = useState<UserRole>('user');
  const [location, setLocation] = useState<LocationSnapshot | null>(initialLocation);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    AsyncStorage.getItem(STORAGE_KEY)
      .then((value) => {
        if (value) {
          const parsed = JSON.parse(value) as StoredState;
          setIncidents(parsed.incidents ?? []);
          setContacts(parsed.contacts?.length ? parsed.contacts : initialContacts);
          setRoleState(parsed.role ?? 'user');
        }
      })
      .catch(() => undefined)
      .finally(() => setHydrated(true));
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    const next: StoredState = { incidents, contacts, role };
    AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(next)).catch(() => undefined);
  }, [contacts, hydrated, incidents, role]);

  const activeIncident = useMemo(
    () => incidents.find((incident) => incident.status !== 'resolved') ?? null,
    [incidents],
  );

  const refreshLocation = async () => {
    try {
      const next = await getCurrentLocation();
      setLocation(next);
    } catch {
      setLocation((current) => current ?? initialLocation);
    }
  };

  const activateSOS = async () => {
    await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Warning);
    let nextLocation = location ?? initialLocation;
    try {
      nextLocation = await getCurrentLocation();
      setLocation(nextLocation);
    } catch {
      setLocation(nextLocation);
    }
    const incident = makeDemoIncident(nextLocation);
    setIncidents((current) => [incident, ...current]);
    return incident;
  };

  const updateIncidentStatus = (status: IncidentStatus) => {
    setIncidents((current) =>
      current.map((incident) => {
        if (incident.id !== activeIncident?.id) return incident;
        const statusIndex: Record<IncidentStatus, number> = {
          activated: 0,
          assigned: 2,
          enroute: 3,
          arrived: 4,
          resolved: 5,
        };
        const completedThrough = statusIndex[status];
        return {
          ...incident,
          status,
          timeline: incident.timeline.map((item, index) => ({
            ...item,
            complete: index <= completedThrough,
            time: index <= completedThrough ? item.time ?? formatTime() : item.time,
          })),
        };
      }),
    );
  };

  const completeAssistant = (summary: string) => {
    setIncidents((current) =>
      current.map((incident) =>
        incident.id === activeIncident?.id
          ? {
              ...incident,
              summary,
              status: 'assigned',
              timeline: incident.timeline.map((item, index) => ({
                ...item,
                complete: index <= 2,
                time: index <= 2 ? item.time ?? formatTime() : item.time,
              })),
            }
          : incident,
      ),
    );
  };

  const addContact = (contact: Omit<EmergencyContact, 'id'>) => {
    const next = { ...contact, id: `contact-${Date.now()}` };
    setContacts((current) => (next.primary ? [next, ...current.map((item) => ({ ...item, primary: false }))] : [...current, next]));
  };

  const deleteContact = (id: string) => setContacts((current) => current.filter((contact) => contact.id !== id));
  const setPrimaryContact = (id: string) => setContacts((current) => current.map((contact) => ({ ...contact, primary: contact.id === id })));
  const setRole = (nextRole: UserRole) => setRoleState(nextRole);

  return (
    <RescueXContext.Provider
      value={{
        incidents,
        contacts,
        role,
        hydrated,
        activeIncident,
        demoMode: true,
        location,
        activateSOS,
        updateIncidentStatus,
        completeAssistant,
        addContact,
        deleteContact,
        setPrimaryContact,
        setRole,
        refreshLocation,
      }}
    >
      {children}
    </RescueXContext.Provider>
  );
}

export function useRescueX() {
  const context = useContext(RescueXContext);
  if (!context) throw new Error('useRescueX must be used inside RescueXProvider');
  return context;
}