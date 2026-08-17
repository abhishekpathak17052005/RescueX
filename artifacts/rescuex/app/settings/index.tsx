import { Feather } from '@expo/vector-icons';
import React, { useState } from 'react';
import { Pressable, StyleSheet, Switch, Text, View } from 'react-native';
import { Card, Header, Screen, SectionLabel } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

function SettingRow({ icon, title, copy, value, onValueChange }: { icon: keyof typeof Feather.glyphMap; title: string; copy: string; value?: boolean; onValueChange?: (value: boolean) => void }) {
  const colors = useColors();
  return <View style={styles.row}><View style={[styles.icon, { backgroundColor: colors.secondary }]}><Feather name={icon} size={17} color={colors.secondaryForeground} /></View><View style={{ flex: 1 }}><Text style={[styles.rowTitle, { color: colors.foreground }]}>{title}</Text><Text style={[styles.rowCopy, { color: colors.mutedForeground }]}>{copy}</Text></View>{value !== undefined && onValueChange ? <Switch value={value} onValueChange={onValueChange} trackColor={{ false: colors.muted, true: colors.accentForeground }} thumbColor={colors.card} /> : <Feather name="chevron-right" size={19} color={colors.mutedForeground} />}</View>;
}

export default function SettingsScreen() {
  const colors = useColors();
  const { refreshLocation } = useRescueX();
  const [notifications, setNotifications] = useState(true);
  const [privacy, setPrivacy] = useState(true);
  return (
    <Screen>
      <Header title="Settings" subtitle="Safety preferences" back />
      <SectionLabel>Permissions</SectionLabel>
      <Card style={styles.card}><Pressable onPress={refreshLocation}><SettingRow icon="map-pin" title="Location access" copy="Used only when you activate SOS" /><View style={[styles.divider, { backgroundColor: colors.border }]} /></Pressable><SettingRow icon="bell" title="Notifications" copy="Responder updates and incident status" value={notifications} onValueChange={setNotifications} /></Card>
      <SectionLabel>Privacy</SectionLabel>
      <Card style={styles.card}><SettingRow icon="lock" title="Share emergency details" copy="Allow responders to see your incident summary" value={privacy} onValueChange={setPrivacy} /><View style={[styles.divider, { backgroundColor: colors.border }]} /><SettingRow icon="file-text" title="Audit activity" copy="View how your emergency information is used" /></Card>
      <View style={[styles.notice, { backgroundColor: colors.accent }]}><Feather name="info" size={17} color={colors.accentForeground} /><Text style={[styles.noticeCopy, { color: colors.accentForeground }]}>RescueX is not an official police, ambulance, fire, or government service.</Text></View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  card: { paddingVertical: 4, marginBottom: 24 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 11, minHeight: 67 },
  icon: { width: 34, height: 34, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
  rowTitle: { fontFamily: 'Inter_600SemiBold', fontSize: 14 },
  rowCopy: { fontFamily: 'Inter_400Regular', fontSize: 11, marginTop: 3 },
  divider: { height: 1 },
  notice: { borderRadius: 16, padding: 13, flexDirection: 'row', gap: 9, alignItems: 'flex-start' },
  noticeCopy: { flex: 1, fontFamily: 'Inter_600SemiBold', fontSize: 11, lineHeight: 17 },
});