import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React, { PropsWithChildren, ReactNode } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View, ViewStyle } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useColors } from '@/hooks/useColors';

export function Screen({ children, scroll = true, style }: PropsWithChildren<{ scroll?: boolean; style?: ViewStyle }>) {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const content = (
    <View style={[styles.screen, { backgroundColor: colors.background, paddingTop: insets.top + 14, paddingBottom: insets.bottom + 28 }, style]}>
      {children}
    </View>
  );
  return scroll ? <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ flexGrow: 1 }}>{content}</ScrollView> : content;
}

export function Header({ title, subtitle, back = false, right }: { title: string; subtitle?: string; back?: boolean; right?: ReactNode }) {
  const colors = useColors();
  const router = useRouter();
  return (
    <View style={styles.header}>
      <View style={styles.headerMain}>
        {back ? (
          <Pressable accessibilityRole="button" testID="header-back" onPress={() => router.back()} hitSlop={12} style={styles.backButton}>
            <Feather name="arrow-left" size={22} color={colors.foreground} />
          </Pressable>
        ) : null}
        <View style={{ flex: 1 }}>
          <Text style={[styles.title, { color: colors.foreground }]}>{title}</Text>
          {subtitle ? <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>{subtitle}</Text> : null}
        </View>
        {right}
      </View>
    </View>
  );
}

export function BrandMark({ compact = false }: { compact?: boolean }) {
  const colors = useColors();
  return (
    <View style={styles.brandRow}>
      <View style={[styles.brandIcon, { backgroundColor: colors.primary }]}>
        <Feather name="crosshair" size={compact ? 16 : 18} color={colors.primaryForeground} />
      </View>
      <Text style={[styles.brandText, { color: colors.foreground, fontSize: compact ? 18 : 22 }]}>RescueX</Text>
    </View>
  );
}

export function DemoBadge() {
  const colors = useColors();
  return (
    <View style={[styles.demoBadge, { backgroundColor: colors.accent }]}>
      <View style={[styles.demoDot, { backgroundColor: colors.primary }]} />
      <Text style={[styles.demoText, { color: colors.accentForeground }]}>DEMO MODE</Text>
    </View>
  );
}

export function Button({ label, onPress, variant = 'primary', icon, disabled = false, loading = false, testID }: { label: string; onPress: () => void; variant?: 'primary' | 'secondary' | 'ghost' | 'danger'; icon?: keyof typeof Feather.glyphMap; disabled?: boolean; loading?: boolean; testID?: string }) {
  const colors = useColors();
  const backgroundColor = variant === 'primary' ? colors.primary : variant === 'danger' ? colors.destructive : variant === 'secondary' ? colors.secondary : 'transparent';
  const foreground = variant === 'primary' || variant === 'danger' ? colors.primaryForeground : variant === 'ghost' ? colors.primary : colors.secondaryForeground;
  return (
    <Pressable
      accessibilityRole="button"
      testID={testID}
      disabled={disabled || loading}
      onPress={onPress}
      style={({ pressed }) => [styles.button, { backgroundColor, borderColor: variant === 'ghost' ? 'transparent' : colors.border, opacity: disabled || loading ? 0.45 : pressed ? 0.78 : 1 }, variant === 'ghost' ? styles.ghostButton : null]}
    >
      {loading ? <ActivityIndicator color={foreground} /> : icon ? <Feather name={icon} size={18} color={foreground} /> : null}
      <Text style={[styles.buttonLabel, { color: foreground }]}>{label}</Text>
    </Pressable>
  );
}

export function Card({ children, style }: PropsWithChildren<{ style?: ViewStyle }>) {
  const colors = useColors();
  return <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }, style]}>{children}</View>;
}

export function SectionLabel({ children }: PropsWithChildren) {
  const colors = useColors();
  return <Text style={[styles.sectionLabel, { color: colors.mutedForeground }]}>{children}</Text>;
}

export function StatusPill({ label, tone = 'teal' }: { label: string; tone?: 'teal' | 'coral' | 'amber' | 'slate' }) {
  const colors = useColors();
  const palette = {
    teal: { backgroundColor: colors.accent, color: colors.accentForeground },
    coral: { backgroundColor: '#FBE7E4', color: colors.destructive },
    amber: { backgroundColor: '#FFF1D9', color: '#946200' },
    slate: { backgroundColor: colors.secondary, color: colors.secondaryForeground },
  }[tone];
  return <View style={[styles.statusPill, { backgroundColor: palette.backgroundColor }]}><Text style={[styles.statusPillText, { color: palette.color }]}>{label}</Text></View>;
}

export function Timeline({ items }: { items: { label: string; complete: boolean; time?: string }[] }) {
  const colors = useColors();
  return (
    <View>
      {items.map((item, index) => (
        <View key={item.label} style={styles.timelineRow}>
          <View style={styles.timelineRail}>
            <View style={[styles.timelineDot, { backgroundColor: item.complete ? colors.primary : colors.muted, borderColor: item.complete ? colors.primary : colors.border }]}>
              {item.complete ? <Feather name="check" size={11} color={colors.primaryForeground} /> : null}
            </View>
            {index < items.length - 1 ? <View style={[styles.timelineLine, { backgroundColor: item.complete ? colors.primary : colors.border }]} /> : null}
          </View>
          <View style={styles.timelineCopy}>
            <Text style={[styles.timelineLabel, { color: item.complete ? colors.foreground : colors.mutedForeground }]}>{item.label}</Text>
            {item.time ? <Text style={[styles.timelineTime, { color: colors.mutedForeground }]}>{item.time}</Text> : null}
          </View>
        </View>
      ))}
    </View>
  );
}

export function MiniMap({ latitude, longitude }: { latitude: number; longitude: number }) {
  const colors = useColors();
  return (
    <View style={[styles.map, { backgroundColor: colors.accent, borderColor: colors.border }]}>
      <View style={[styles.mapRoad, styles.mapRoadOne, { backgroundColor: colors.card }]} />
      <View style={[styles.mapRoad, styles.mapRoadTwo, { backgroundColor: colors.card }]} />
      <View style={[styles.mapRoad, styles.mapRoadThree, { backgroundColor: colors.card }]} />
      <View style={[styles.mapPin, { backgroundColor: colors.primary, borderColor: colors.primaryForeground }]}>
        <Feather name="map-pin" size={15} color={colors.primaryForeground} />
      </View>
      <View style={[styles.mapCaption, { backgroundColor: colors.card }]}>
        <Text style={[styles.mapCaptionText, { color: colors.foreground }]}>{latitude.toFixed(4)}, {longitude.toFixed(4)}</Text>
      </View>
    </View>
  );
}

export const styles = StyleSheet.create({
  screen: { flex: 1, paddingHorizontal: 20 },
  header: { marginBottom: 24 },
  headerMain: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  backButton: { width: 38, height: 38, borderRadius: 19, alignItems: 'center', justifyContent: 'center' },
  title: { fontFamily: 'Inter_700Bold', fontSize: 28, letterSpacing: -0.8 },
  subtitle: { fontFamily: 'Inter_400Regular', fontSize: 14, marginTop: 4, lineHeight: 20 },
  brandRow: { flexDirection: 'row', alignItems: 'center', gap: 9 },
  brandIcon: { width: 34, height: 34, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
  brandText: { fontFamily: 'Inter_700Bold', letterSpacing: -0.5 },
  demoBadge: { alignSelf: 'flex-start', borderRadius: 20, paddingHorizontal: 10, paddingVertical: 7, flexDirection: 'row', alignItems: 'center', gap: 6 },
  demoDot: { width: 6, height: 6, borderRadius: 3 },
  demoText: { fontFamily: 'Inter_700Bold', fontSize: 10, letterSpacing: 1.1 },
  button: { minHeight: 54, borderRadius: 17, borderWidth: 1, paddingHorizontal: 17, flexDirection: 'row', gap: 9, alignItems: 'center', justifyContent: 'center' },
  ghostButton: { minHeight: 44, paddingHorizontal: 8 },
  buttonLabel: { fontFamily: 'Inter_700Bold', fontSize: 15 },
  card: { borderRadius: 21, borderWidth: 1, padding: 18 },
  sectionLabel: { fontFamily: 'Inter_700Bold', fontSize: 12, letterSpacing: 1.1, textTransform: 'uppercase', marginBottom: 10 },
  statusPill: { borderRadius: 20, paddingHorizontal: 10, paddingVertical: 6, alignSelf: 'flex-start' },
  statusPillText: { fontFamily: 'Inter_700Bold', fontSize: 11, letterSpacing: 0.5 },
  timelineRow: { flexDirection: 'row', minHeight: 44 },
  timelineRail: { width: 28, alignItems: 'center' },
  timelineDot: { width: 21, height: 21, borderRadius: 11, borderWidth: 1, alignItems: 'center', justifyContent: 'center' },
  timelineLine: { flex: 1, width: 2, marginVertical: 2 },
  timelineCopy: { flex: 1, paddingLeft: 9, paddingBottom: 14 },
  timelineLabel: { fontFamily: 'Inter_600SemiBold', fontSize: 14 },
  timelineTime: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 3 },
  map: { height: 172, borderRadius: 18, borderWidth: 1, overflow: 'hidden', position: 'relative' },
  mapRoad: { position: 'absolute', opacity: 0.8 },
  mapRoadOne: { height: 12, width: 280, transform: [{ rotate: '26deg' }], left: -25, top: 72 },
  mapRoadTwo: { height: 9, width: 300, transform: [{ rotate: '-38deg' }], left: 85, top: 70 },
  mapRoadThree: { height: 7, width: 250, transform: [{ rotate: '70deg' }], left: 14, top: 20 },
  mapPin: { position: 'absolute', width: 38, height: 38, borderRadius: 19, alignItems: 'center', justifyContent: 'center', left: '50%', top: '50%', marginLeft: -19, marginTop: -19, borderWidth: 3 },
  mapCaption: { position: 'absolute', left: 12, bottom: 12, borderRadius: 10, paddingHorizontal: 9, paddingVertical: 6 },
  mapCaptionText: { fontFamily: 'Inter_600SemiBold', fontSize: 11 },
});