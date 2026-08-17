import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Card, Header, MiniMap, Screen, SectionLabel, StatusPill, Timeline } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function IncidentDetailsScreen() {
  const colors = useColors();
  const { activeIncident } = useRescueX();
  if (!activeIncident) return <Screen><Header title="Incident details" back /><Text style={[styles.empty, { color: colors.mutedForeground }]}>No incident selected.</Text></Screen>;
  return (
    <Screen>
      <Header title="Incident details" subtitle={activeIncident.id} back />
      <View style={styles.detailHeader}><View><Text style={[styles.type, { color: colors.foreground }]}>{activeIncident.type}</Text><Text style={[styles.date, { color: colors.mutedForeground }]}>{new Date(activeIncident.createdAt).toLocaleString()}</Text></View><StatusPill label={activeIncident.severity} tone="coral" /></View>
      <SectionLabel>AI summary</SectionLabel>
      <Card><Text style={[styles.summary, { color: colors.foreground }]}>{activeIncident.summary}</Text></Card>
      <SectionLabel>Shared location</SectionLabel>
      <Card><MiniMap latitude={activeIncident.location.latitude} longitude={activeIncident.location.longitude} /><Text style={[styles.locationCopy, { color: colors.mutedForeground }]}>Location shared at {new Date(activeIncident.location.updatedAt).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })} with ±{Math.round(activeIncident.location.accuracy ?? 0)}m accuracy.</Text></Card>
      <SectionLabel>Timeline</SectionLabel>
      <Card><Timeline items={activeIncident.timeline} /></Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  detailHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 26 },
  type: { fontFamily: 'Inter_700Bold', fontSize: 19 },
  date: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 5 },
  summary: { fontFamily: 'Inter_400Regular', fontSize: 15, lineHeight: 23 },
  locationCopy: { fontFamily: 'Inter_400Regular', fontSize: 12, lineHeight: 18, marginTop: 11 },
  empty: { fontFamily: 'Inter_400Regular' },
});