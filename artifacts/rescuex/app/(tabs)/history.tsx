import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Card, DemoBadge, Header, Screen, SectionLabel, StatusPill } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function HistoryScreen() {
  const colors = useColors();
  const router = useRouter();
  const { incidents } = useRescueX();
  return (
    <Screen>
      <Header title="History" subtitle="Your emergency activity" right={<DemoBadge />} />
      <SectionLabel>Recent incidents</SectionLabel>
      {incidents.length === 0 ? <Card style={styles.empty}><View style={[styles.emptyIcon, { backgroundColor: colors.accent }]}><Feather name="clock" size={20} color={colors.accentForeground} /></View><Text style={[styles.emptyTitle, { color: colors.foreground }]}>Nothing here yet</Text><Text style={[styles.emptyCopy, { color: colors.mutedForeground }]}>Your resolved and active incidents will appear here.</Text></Card> : incidents.map((incident) => (
        <Pressable key={incident.id} onPress={() => router.push('/emergency/details')} style={({ pressed }) => [styles.rowWrap, { opacity: pressed ? 0.72 : 1 }]}>
          <Card style={styles.incidentCard}>
            <View style={[styles.typeIcon, { backgroundColor: incident.status === 'resolved' ? colors.accent : '#FBE7E4' }]}><Feather name={incident.status === 'resolved' ? 'check' : 'activity'} size={18} color={incident.status === 'resolved' ? colors.accentForeground : colors.destructive} /></View>
            <View style={{ flex: 1 }}><Text style={[styles.incidentTitle, { color: colors.foreground }]}>{incident.type}</Text><Text style={[styles.incidentMeta, { color: colors.mutedForeground }]}>{incident.id} · {new Date(incident.createdAt).toLocaleDateString()}</Text></View>
            <StatusPill label={incident.status === 'resolved' ? 'RESOLVED' : 'ACTIVE'} tone={incident.status === 'resolved' ? 'teal' : 'coral'} />
          </Card>
        </Pressable>
      ))}
    </Screen>
  );
}

const styles = StyleSheet.create({
  empty: { alignItems: 'center', paddingVertical: 30 },
  emptyIcon: { width: 48, height: 48, borderRadius: 17, alignItems: 'center', justifyContent: 'center' },
  emptyTitle: { fontFamily: 'Inter_700Bold', fontSize: 16, marginTop: 15 },
  emptyCopy: { fontFamily: 'Inter_400Regular', fontSize: 13, textAlign: 'center', lineHeight: 20, marginTop: 5, maxWidth: 250 },
  rowWrap: { marginBottom: 11 },
  incidentCard: { flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14 },
  typeIcon: { width: 42, height: 42, borderRadius: 15, alignItems: 'center', justifyContent: 'center' },
  incidentTitle: { fontFamily: 'Inter_700Bold', fontSize: 14 },
  incidentMeta: { fontFamily: 'Inter_400Regular', fontSize: 11, marginTop: 4 },
});