import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Button, Card, DemoBadge, Header, MiniMap, Screen, SectionLabel, StatusPill } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function ResponderHomeScreen() {
  const colors = useColors();
  const router = useRouter();
  const { incidents, setRole } = useRescueX();
  const active = incidents.filter((incident) => incident.status !== 'resolved');
  return (
    <Screen>
      <Header title="Active emergencies" subtitle="Responder console" back right={<DemoBadge />} />
      <View style={styles.heroRow}><View><Text style={[styles.heroTitle, { color: colors.foreground }]}>{active.length} incident{active.length === 1 ? '' : 's'}</Text><Text style={[styles.heroCopy, { color: colors.mutedForeground }]}>Ready for a response</Text></View><View style={[styles.online, { backgroundColor: colors.accent }]}><View style={[styles.onlineDot, { backgroundColor: colors.accentForeground }]} /><Text style={[styles.onlineText, { color: colors.accentForeground }]}>Online</Text></View></View>
      <SectionLabel>Dispatch queue</SectionLabel>
      {active.length === 0 ? <Card style={styles.empty}><Feather name="shield" size={24} color={colors.mutedForeground} /><Text style={[styles.emptyTitle, { color: colors.foreground }]}>All clear</Text><Text style={[styles.emptyCopy, { color: colors.mutedForeground }]}>New demo incidents will appear here.</Text></Card> : active.map((incident) => <Pressable key={incident.id} onPress={() => router.push('/responder/incident')} style={({ pressed }) => ({ opacity: pressed ? 0.75 : 1, marginBottom: 13 })}><Card style={styles.incident}><View style={styles.incidentTop}><View><Text style={[styles.type, { color: colors.foreground }]}>{incident.type}</Text><Text style={[styles.meta, { color: colors.mutedForeground }]}>{incident.id} · 2 min ago</Text></View><StatusPill label={incident.severity} tone="coral" /></View><MiniMap latitude={incident.location.latitude} longitude={incident.location.longitude} /><View style={styles.incidentBottom}><View style={styles.distance}><Feather name="navigation" size={14} color={colors.accentForeground} /><Text style={[styles.distanceText, { color: colors.foreground }]}>0.8 mi away</Text></View><Feather name="chevron-right" size={19} color={colors.primary} /></View></Card></Pressable>)}
      <Button label="Return to user view" variant="ghost" icon="arrow-left" onPress={() => { setRole('user'); router.replace('/'); }} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  heroRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 25 },
  heroTitle: { fontFamily: 'Inter_700Bold', fontSize: 25, letterSpacing: -0.5 },
  heroCopy: { fontFamily: 'Inter_400Regular', fontSize: 13, marginTop: 4 },
  online: { paddingHorizontal: 10, paddingVertical: 7, borderRadius: 20, flexDirection: 'row', gap: 6, alignItems: 'center' },
  onlineDot: { width: 6, height: 6, borderRadius: 3 },
  onlineText: { fontFamily: 'Inter_700Bold', fontSize: 11 },
  incident: { gap: 14 },
  incidentTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  type: { fontFamily: 'Inter_700Bold', fontSize: 16 },
  meta: { fontFamily: 'Inter_400Regular', fontSize: 11, marginTop: 4 },
  incidentBottom: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  distance: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  distanceText: { fontFamily: 'Inter_600SemiBold', fontSize: 12 },
  empty: { alignItems: 'center', paddingVertical: 31 },
  emptyTitle: { fontFamily: 'Inter_700Bold', fontSize: 16, marginTop: 12 },
  emptyCopy: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 5 },
});