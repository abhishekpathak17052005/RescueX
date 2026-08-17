import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React, { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Button, DemoBadge, Screen } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function ConfirmScreen() {
  const colors = useColors();
  const router = useRouter();
  const { activateSOS } = useRescueX();
  const [loading, setLoading] = useState(false);
  const activate = async () => {
    setLoading(true);
    await activateSOS();
    setLoading(false);
    router.replace('/emergency/active');
  };
  return (
    <Screen scroll={false} style={styles.screen}>
      <View style={styles.top}><DemoBadge /><Pressable onPress={() => router.back()} hitSlop={12}><Feather name="x" size={24} color={colors.foreground} /></Pressable></View>
      <View style={styles.center}>
        <View style={[styles.iconCircle, { backgroundColor: colors.primary }]}><Feather name="alert-triangle" size={36} color={colors.primaryForeground} /></View>
        <Text style={[styles.title, { color: colors.foreground }]}>Do you need emergency assistance?</Text>
        <Text style={[styles.copy, { color: colors.mutedForeground }]}>RescueX will share your current location and open a demo responder incident. You can stop at any time.</Text>
      </View>
      <View style={styles.actions}>
        <Button label="Activate SOS" icon="radio" onPress={activate} loading={loading} testID="activate-sos" />
        <Button label="Cancel" variant="ghost" onPress={() => router.back()} />
      </View>
      <Text style={[styles.disclaimer, { color: colors.mutedForeground }]}>RescueX is an AI-assisted coordination app, not an official emergency service.</Text>
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { justifyContent: 'space-between' },
  top: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  center: { alignItems: 'center', paddingHorizontal: 20, marginTop: -60 },
  iconCircle: { width: 88, height: 88, borderRadius: 44, alignItems: 'center', justifyContent: 'center', marginBottom: 28 },
  title: { fontFamily: 'Inter_700Bold', fontSize: 30, textAlign: 'center', letterSpacing: -0.8, lineHeight: 36 },
  copy: { fontFamily: 'Inter_400Regular', fontSize: 15, textAlign: 'center', lineHeight: 23, marginTop: 14 },
  actions: { gap: 5 },
  disclaimer: { fontFamily: 'Inter_400Regular', fontSize: 11, lineHeight: 16, textAlign: 'center', marginTop: 16 },
});