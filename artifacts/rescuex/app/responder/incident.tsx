import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Button, Card, DemoBadge, Header, MiniMap, Screen, SectionLabel, StatusPill, Timeline } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function ResponderIncidentScreen() {
  const colors = useColors();
  const router = useRouter();
  const { activeIncident, updateIncidentStatus } = useRescueX();
  if (!activeIncident) return <Screen><Header title="Incident" back /><Text style={[styles.empty, { color: colors.mutedForeground }]}>No active incident.</Text></Screen>;
  const next: { label: string; status: 'assigned' | 'enroute' | 'arrived' | 'resolved'; icon: keyof typeof Feather.glyphMap } | null = activeIncident.status === 'activated' ? { label: 'Accept incident', status: 'assigned', icon: 'check' } : activeIncident.status === 'assigned' ? { label: 'Mark en route', status: 'enroute', icon: 'navigation' } : activeIncident.status === 'enroute' ? { label: 'Mark arrived', status: 'arrived', icon: 'map-pin' } : activeIncident.status === 'arrived' ? { label: 'Resolve incident', status: 'resolved', icon: 'check-circle' } : null;
  return (
    <Screen>
      <Header title="Incident" subtitle={activeIncident.id} back right={<DemoBadge />} />
      <View style={styles.heading}><View><Text style={[styles.type, { color: colors.foreground }]}>{activeIncident.type}</Text><Text style={[styles.meta, { color: colors.mutedForeground }]}>Reported 2 minutes ago</Text></View><StatusPill label={activeIncident.severity} tone="coral" /></View>
      <SectionLabel>AI summary</SectionLabel>
      <Card><Text style={[styles.summary, { color: colors.foreground }]}>{activeIncident.summary}</Text></Card>
      <SectionLabel>Location</SectionLabel>
      <Card><MiniMap latitude={activeIncident.location.latitude} longitude={activeIncident.location.longitude} /><View style={styles.locationLabel}><Feather name="map-pin" size={15} color={colors.accentForeground} /><Text style={[styles.locationText, { color: colors.foreground }]}>Shared location · 0.8 mi away</Text></View></Card>
      <SectionLabel>Emergency information</SectionLabel>
      <Card style={styles.infoGrid}><View><Text style={[styles.infoLabel, { color: colors.mutedForeground }]}>Type</Text><Text style={[styles.infoValue, { color: colors.foreground }]}>Medical</Text></View><View><Text style={[styles.infoLabel, { color: colors.mutedForeground }]}>People</Text><Text style={[styles.infoValue, { color: colors.foreground }]}>1 reported</Text></View><View><Text style={[styles.infoLabel, { color: colors.mutedForeground }]}>Location</Text><Text style={[styles.infoValue, { color: colors.foreground }]}>Shared</Text></View><View><Text style={[styles.infoLabel, { color: colors.mutedForeground }]}>Safety</Text><Text style={[styles.infoValue, { color: colors.foreground }]}>Checking</Text></View></Card>
      <SectionLabel>Response</SectionLabel>
      <Card><Timeline items={activeIncident.timeline} /></Card>
      {next ? <Button label={next.label} icon={next.icon} onPress={() => { updateIncidentStatus(next.status); if (next.status === 'resolved') router.replace('/responder'); }} /> : <Button label="Return to queue" variant="secondary" onPress={() => router.back()} />}
    </Screen>
  );
}

const styles = StyleSheet.create({
  heading: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 25 },
  type: { fontFamily: 'Inter_700Bold', fontSize: 20 },
  meta: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 5 },
  summary: { fontFamily: 'Inter_400Regular', fontSize: 14, lineHeight: 22 },
  locationLabel: { flexDirection: 'row', alignItems: 'center', gap: 7, marginTop: 12 },
  locationText: { fontFamily: 'Inter_600SemiBold', fontSize: 12 },
  infoGrid: { flexDirection: 'row', flexWrap: 'wrap', rowGap: 18 },
  infoLabel: { fontFamily: 'Inter_400Regular', fontSize: 11 },
  infoValue: { fontFamily: 'Inter_700Bold', fontSize: 13, marginTop: 4 },
  empty: { fontFamily: 'Inter_400Regular' },
});