import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Button, Card, DemoBadge, BrandMark, Screen, SectionLabel, StatusPill } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function HomeScreen() {
  const colors = useColors();
  const router = useRouter();
  const { activeIncident, location, demoMode, role } = useRescueX();
  const locationReady = Boolean(location);
  return (
    <Screen>
      <View style={styles.topRow}>
        <BrandMark />
        <DemoBadge />
      </View>
      <Text style={[styles.greeting, { color: colors.foreground }]}>Good morning, Alex.</Text>
      <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>Your AI-powered emergency assistance.</Text>

      {role === 'responder' ? (
        <Card style={styles.responderBanner}>
          <View style={[styles.bannerIcon, { backgroundColor: colors.accent }]}><Feather name="shield" size={20} color={colors.accentForeground} /></View>
          <View style={{ flex: 1 }}>
            <Text style={[styles.bannerTitle, { color: colors.foreground }]}>Responder mode is on</Text>
            <Text style={[styles.bannerCopy, { color: colors.mutedForeground }]}>View and manage active incidents.</Text>
          </View>
          <Pressable onPress={() => router.push('/responder')} hitSlop={10}><Feather name="arrow-up-right" size={20} color={colors.primary} /></Pressable>
        </Card>
      ) : null}

      <View style={styles.sosWrap}>
        <View style={[styles.sosHalo, { borderColor: colors.primary }]} />
        <Pressable
          testID="sos-button"
          accessibilityRole="button"
          onPress={() => router.push('/confirm')}
          style={({ pressed }) => [styles.sosButton, { backgroundColor: colors.primary, shadowColor: colors.primary, transform: [{ scale: pressed ? 0.96 : 1 }] }]}
        >
          <Feather name="crosshair" size={36} color={colors.primaryForeground} />
          <Text style={[styles.sosText, { color: colors.primaryForeground }]}>SOS</Text>
        </Pressable>
      </View>
      <Text style={[styles.sosCaption, { color: colors.foreground }]}>Press for emergency assistance</Text>
      <Text style={[styles.sosHint, { color: colors.mutedForeground }]}>You’ll confirm before anything is shared.</Text>

      {activeIncident ? (
        <Card style={styles.activeCard}>
          <View style={styles.cardRow}>
            <View>
              <SectionLabel>Active incident</SectionLabel>
              <Text style={[styles.activeTitle, { color: colors.foreground }]}>{activeIncident.type}</Text>
              <Text style={[styles.activeCopy, { color: colors.mutedForeground }]}>{activeIncident.id} · {activeIncident.status === 'assigned' ? 'Responder assigned' : 'Waiting for responder'}</Text>
            </View>
            <StatusPill label={activeIncident.status === 'assigned' ? 'ASSIGNED' : 'ACTIVE'} tone="coral" />
          </View>
          <Button label="Open emergency status" icon="arrow-right" onPress={() => router.push('/emergency/active')} />
        </Card>
      ) : null}

      <View style={styles.locationRow}>
        <View style={[styles.locationDot, { backgroundColor: locationReady ? colors.accentForeground : colors.destructive }]} />
        <Text style={[styles.locationText, { color: colors.foreground }]}>{locationReady ? 'Location Ready' : 'Location Permission Required'}</Text>
        <Text style={[styles.locationMeta, { color: colors.mutedForeground }]}>{demoMode ? 'Demo location available' : 'Last checked just now'}</Text>
      </View>

      <View style={styles.quickActions}>
        <Pressable onPress={() => router.push('/emergency/assistant')} style={[styles.quickAction, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Feather name="mic" size={20} color={colors.primary} />
          <Text style={[styles.quickLabel, { color: colors.foreground }]}>AI Assistant</Text>
        </Pressable>
        <Pressable onPress={() => router.push('/(tabs)/contacts')} style={[styles.quickAction, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Feather name="users" size={20} color={colors.accentForeground} />
          <Text style={[styles.quickLabel, { color: colors.foreground }]}>Contacts</Text>
        </Pressable>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  topRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 28 },
  greeting: { fontFamily: 'Inter_700Bold', fontSize: 24, letterSpacing: -0.6 },
  subtitle: { fontFamily: 'Inter_400Regular', fontSize: 14, marginTop: 5 },
  responderBanner: { flexDirection: 'row', alignItems: 'center', gap: 12, marginTop: 20, padding: 14 },
  bannerIcon: { width: 38, height: 38, borderRadius: 13, alignItems: 'center', justifyContent: 'center' },
  bannerTitle: { fontFamily: 'Inter_700Bold', fontSize: 13 },
  bannerCopy: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 3 },
  sosWrap: { height: 242, alignItems: 'center', justifyContent: 'center', marginTop: 14 },
  sosHalo: { position: 'absolute', width: 224, height: 224, borderRadius: 112, borderWidth: 1, opacity: 0.16 },
  sosButton: { width: 174, height: 174, borderRadius: 87, alignItems: 'center', justifyContent: 'center', shadowOpacity: 0.28, shadowRadius: 28, shadowOffset: { width: 0, height: 12 }, elevation: 8 },
  sosText: { fontFamily: 'Inter_700Bold', fontSize: 36, letterSpacing: 1.2, marginTop: 2 },
  sosCaption: { fontFamily: 'Inter_700Bold', textAlign: 'center', fontSize: 16 },
  sosHint: { fontFamily: 'Inter_400Regular', textAlign: 'center', fontSize: 12, marginTop: 5 },
  activeCard: { marginTop: 24, gap: 15 },
  cardRow: { flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'space-between', gap: 10 },
  activeTitle: { fontFamily: 'Inter_700Bold', fontSize: 16, marginTop: -4 },
  activeCopy: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 5 },
  locationRow: { alignItems: 'center', flexDirection: 'row', flexWrap: 'wrap', gap: 7, marginTop: 26, paddingHorizontal: 2 },
  locationDot: { width: 8, height: 8, borderRadius: 4 },
  locationText: { fontFamily: 'Inter_700Bold', fontSize: 13 },
  locationMeta: { width: '100%', marginLeft: 15, marginTop: -4, fontFamily: 'Inter_400Regular', fontSize: 11 },
  quickActions: { flexDirection: 'row', gap: 10, marginTop: 20 },
  quickAction: { flex: 1, minHeight: 74, borderRadius: 18, borderWidth: 1, padding: 14, justifyContent: 'space-between' },
  quickLabel: { fontFamily: 'Inter_600SemiBold', fontSize: 12 },
});