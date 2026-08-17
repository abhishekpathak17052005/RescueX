import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Button, Card, DemoBadge, Header, Screen, SectionLabel } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function ProfileScreen() {
  const colors = useColors();
  const router = useRouter();
  const { role, setRole } = useRescueX();
  return (
    <Screen>
      <Header title="Profile" subtitle="Your RescueX account" right={<DemoBadge />} />
      <Card style={styles.profileCard}><View style={[styles.avatar, { backgroundColor: colors.primary }]}><Text style={[styles.avatarText, { color: colors.primaryForeground }]}>A</Text></View><View style={{ flex: 1 }}><Text style={[styles.name, { color: colors.foreground }]}>Alex Morgan</Text><Text style={[styles.email, { color: colors.mutedForeground }]}>alex@rescuex.test</Text></View><Feather name="check-circle" size={20} color={colors.accentForeground} /></Card>
      <SectionLabel>Demo role</SectionLabel>
      <Card style={styles.roleCard}><Text style={[styles.roleTitle, { color: colors.foreground }]}>Switch your demo perspective</Text><Text style={[styles.roleCopy, { color: colors.mutedForeground }]}>Try the responder workflow without signing out.</Text><View style={styles.roleChoices}><Pressable onPress={() => setRole('user')} style={[styles.roleChoice, { borderColor: role === 'user' ? colors.primary : colors.border, backgroundColor: role === 'user' ? '#FBE7E4' : colors.card }]}><Feather name="user" size={17} color={role === 'user' ? colors.primary : colors.mutedForeground} /><Text style={[styles.roleText, { color: colors.foreground }]}>User</Text></Pressable><Pressable onPress={() => setRole('responder')} style={[styles.roleChoice, { borderColor: role === 'responder' ? colors.accentForeground : colors.border, backgroundColor: role === 'responder' ? colors.accent : colors.card }]}><Feather name="shield" size={17} color={role === 'responder' ? colors.accentForeground : colors.mutedForeground} /><Text style={[styles.roleText, { color: colors.foreground }]}>Responder</Text></Pressable></View></Card>
      <SectionLabel>Account</SectionLabel>
      <Card style={styles.menu}><Pressable onPress={() => router.push('/settings')} style={styles.menuRow}><View style={[styles.menuIcon, { backgroundColor: colors.secondary }]}><Feather name="settings" size={17} color={colors.secondaryForeground} /></View><Text style={[styles.menuText, { color: colors.foreground }]}>Settings</Text><Feather name="chevron-right" size={19} color={colors.mutedForeground} /></Pressable><View style={[styles.divider, { backgroundColor: colors.border }]} /><View style={styles.menuRow}><View style={[styles.menuIcon, { backgroundColor: colors.secondary }]}><Feather name="shield" size={17} color={colors.secondaryForeground} /></View><Text style={[styles.menuText, { color: colors.foreground }]}>Privacy & safety</Text><Feather name="chevron-right" size={19} color={colors.mutedForeground} /></View></Card>
      <Button label="Sign out of demo" variant="ghost" icon="log-out" onPress={() => setRole('user')} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  profileCard: { flexDirection: 'row', alignItems: 'center', gap: 13, padding: 15, marginBottom: 24 },
  avatar: { width: 50, height: 50, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
  avatarText: { fontFamily: 'Inter_700Bold', fontSize: 22 },
  name: { fontFamily: 'Inter_700Bold', fontSize: 16 },
  email: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 4 },
  roleCard: { gap: 7, marginBottom: 24 },
  roleTitle: { fontFamily: 'Inter_700Bold', fontSize: 14 },
  roleCopy: { fontFamily: 'Inter_400Regular', fontSize: 12, lineHeight: 18 },
  roleChoices: { flexDirection: 'row', gap: 9, marginTop: 7 },
  roleChoice: { flex: 1, borderRadius: 14, borderWidth: 1, minHeight: 46, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 7 },
  roleText: { fontFamily: 'Inter_700Bold', fontSize: 12 },
  menu: { paddingVertical: 4, marginBottom: 11 },
  menuRow: { minHeight: 59, alignItems: 'center', flexDirection: 'row', gap: 11 },
  menuIcon: { width: 33, height: 33, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
  menuText: { flex: 1, fontFamily: 'Inter_600SemiBold', fontSize: 14 },
  divider: { height: 1 },
});