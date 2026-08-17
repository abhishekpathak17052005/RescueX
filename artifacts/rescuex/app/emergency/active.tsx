import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Button, Card, DemoBadge, Header, MiniMap, Screen, SectionLabel, StatusPill, Timeline } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function ActiveEmergencyScreen() {
  const colors = useColors();
  const router = useRouter();
  const { activeIncident, demoMode } = useRescueX();
  if (!activeIncident) {
    return <Screen><Header title="Emergency status" back /><Card><Text style={[styles.emptyTitle, { color: colors.foreground }]}>No active emergency</Text><Text style={[styles.emptyCopy, { color: colors.mutedForeground }]}>Your incident status will appear here after you activate SOS.</Text></Card></Screen>;
  }
  const statusLabel = activeIncident.status === 'activated' ? 'Waiting for responder' : activeIncident.status === 'assigned' ? 'Responder assigned' : activeIncident.status === 'enroute' ? 'Responder en route' : activeIncident.status === 'arrived' ? 'Responder arrived' : 'Incident resolved';
  return (
    <Screen>
      <Header title="Emergency Active" subtitle={`Incident ${activeIncident.id}`} back right={<DemoBadge />} />
      <View style={styles.statusRow}><View style={[styles.pulse, { backgroundColor: activeIncident.status === 'resolved' ? colors.accentForeground : colors.primary }]} /><Text style={[styles.status, { color: colors.foreground }]}>{statusLabel}</Text><StatusPill label={activeIncident.severity} tone="coral" /></View>
      {demoMode ? <Text style={[styles.demoNotice, { color: colors.accentForeground, backgroundColor: colors.accent }]}>This incident is simulated for demonstration.</Text> : null}
      <SectionLabel>Your location</SectionLabel>
      <Card style={styles.locationCard}>
        <MiniMap latitude={activeIncident.location.latitude} longitude={activeIncident.location.longitude} />
        <View style={styles.locationMeta}><Feather name="check-circle" size={16} color={colors.accentForeground} /><Text style={[styles.locationShared, { color: colors.foreground }]}>Location shared</Text><Text style={[styles.accuracy, { color: colors.mutedForeground }]}>Accuracy ±{Math.round(activeIncident.location.accuracy ?? 0)}m</Text></View>
      </Card>
      <SectionLabel>AI assistant</SectionLabel>
      <Card style={styles.aiCard}>
        <View style={[styles.aiIcon, { backgroundColor: colors.accent }]}><Feather name="mic" size={20} color={colors.accentForeground} /></View>
        <View style={{ flex: 1 }}><Text style={[styles.aiTitle, { color: colors.foreground }]}>AI Assistant Connected</Text><Text style={[styles.aiCopy, { color: colors.mutedForeground }]}>Calmly collecting emergency details.</Text></View>
        <Pressable onPress={() => router.push('/emergency/assistant')} hitSlop={10}><Feather name="chevron-right" size={20} color={colors.primary} /></Pressable>
      </Card>
      <Button label="Talk to AI Assistant" icon="mic" onPress={() => router.push('/emergency/assistant')} />
      <SectionLabel>Response status</SectionLabel>
      <Card><Timeline items={activeIncident.timeline} /></Card>
      <View style={styles.bottomLinks}><Button label="View incident details" variant="ghost" icon="file-text" onPress={() => router.push('/emergency/details')} /><Button label="Close" variant="ghost" onPress={() => router.replace('/')} /></View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  statusRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 20 },
  pulse: { width: 9, height: 9, borderRadius: 5 },
  status: { flex: 1, fontFamily: 'Inter_700Bold', fontSize: 14 },
  demoNotice: { fontFamily: 'Inter_600SemiBold', fontSize: 12, borderRadius: 12, padding: 11, marginBottom: 20 },
  locationCard: { gap: 12, marginBottom: 22 },
  locationMeta: { flexDirection: 'row', alignItems: 'center', gap: 7 },
  locationShared: { fontFamily: 'Inter_700Bold', fontSize: 13 },
  accuracy: { fontFamily: 'Inter_400Regular', fontSize: 12, marginLeft: 'auto' },
  aiCard: { flexDirection: 'row', alignItems: 'center', gap: 12, marginBottom: 12 },
  aiIcon: { width: 38, height: 38, borderRadius: 14, alignItems: 'center', justifyContent: 'center' },
  aiTitle: { fontFamily: 'Inter_700Bold', fontSize: 13 },
  aiCopy: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 3 },
  bottomLinks: { alignItems: 'center', marginTop: 8 },
  emptyTitle: { fontFamily: 'Inter_700Bold', fontSize: 17 },
  emptyCopy: { fontFamily: 'Inter_400Regular', lineHeight: 21, marginTop: 7 },
});