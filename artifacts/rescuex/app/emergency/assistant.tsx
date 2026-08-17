import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React, { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Button, Card, DemoBadge, Header, Screen } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

const prompts = ['Tell me what happened.', 'Are you currently safe?', 'Is anyone injured?', 'Do you need medical assistance?'];

export default function AssistantScreen() {
  const colors = useColors();
  const router = useRouter();
  const { activeIncident, completeAssistant } = useRescueX();
  const [started, setStarted] = useState(false);
  const [step, setStep] = useState(0);
  const [complete, setComplete] = useState(false);

  useEffect(() => {
    if (!started || complete) return;
    const timer = setTimeout(() => {
      if (step >= prompts.length - 1) {
        setComplete(true);
        if (activeIncident) completeAssistant('User reported a high-severity medical emergency. One person may be injured. The current location has been shared with the demo responder.');
      } else setStep((current) => current + 1);
    }, 1250);
    return () => clearTimeout(timer);
  }, [activeIncident, complete, completeAssistant, started, step]);

  return (
    <Screen>
      <Header title="AI Assistant" subtitle="Short, calm questions" back right={<DemoBadge />} />
      <View style={styles.hero}>
        <View style={[styles.voiceOrb, { backgroundColor: colors.primary }]}><Feather name="mic" size={33} color={colors.primaryForeground} /></View>
        <Text style={[styles.heroTitle, { color: colors.foreground }]}>{complete ? 'Information collected' : started ? 'I’m listening' : 'You’re not alone'}</Text>
        <Text style={[styles.heroCopy, { color: colors.mutedForeground }]}>{complete ? 'Your summary is ready for the responder.' : 'RescueX will ask a few short questions and share the essentials.'}</Text>
      </View>
      {started && !complete ? <Card style={styles.promptCard}><View style={[styles.liveDot, { backgroundColor: colors.primary }]} /><Text style={[styles.promptText, { color: colors.foreground }]}>{prompts[step]}</Text><Text style={[styles.promptCount, { color: colors.mutedForeground }]}>{step + 1} of {prompts.length}</Text></Card> : null}
      {complete && activeIncident ? <Card style={styles.summaryCard}><View style={styles.summaryHeader}><Feather name="file-text" size={18} color={colors.accentForeground} /><Text style={[styles.summaryTitle, { color: colors.foreground }]}>Incident summary</Text></View><Text style={[styles.summaryText, { color: colors.foreground }]}>{activeIncident.summary}</Text></Card> : null}
      <View style={styles.bottom}>
        {!started ? <Button label="Start AI check-in" icon="mic" onPress={() => setStarted(true)} /> : complete ? <Button label="Return to emergency status" icon="arrow-right" onPress={() => router.replace('/emergency/active')} /> : <Pressable onPress={() => setComplete(true)}><Text style={[styles.skip, { color: colors.mutedForeground }]}>Skip demo conversation</Text></Pressable>}
        <Text style={[styles.disclaimer, { color: colors.mutedForeground }]}>Demo mode uses a simulated assistant. In production, this is where a secure Vapi session connects.</Text>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  hero: { alignItems: 'center', paddingVertical: 44 },
  voiceOrb: { width: 94, height: 94, borderRadius: 47, alignItems: 'center', justifyContent: 'center', shadowColor: '#E85B51', shadowOpacity: 0.3, shadowRadius: 24, shadowOffset: { width: 0, height: 10 }, elevation: 6 },
  heroTitle: { fontFamily: 'Inter_700Bold', fontSize: 26, letterSpacing: -0.7, marginTop: 24 },
  heroCopy: { fontFamily: 'Inter_400Regular', fontSize: 14, textAlign: 'center', lineHeight: 21, maxWidth: 300, marginTop: 7 },
  promptCard: { alignItems: 'center', paddingVertical: 27 },
  liveDot: { width: 9, height: 9, borderRadius: 5, marginBottom: 14 },
  promptText: { fontFamily: 'Inter_700Bold', fontSize: 19, textAlign: 'center', lineHeight: 26 },
  promptCount: { fontFamily: 'Inter_400Regular', fontSize: 12, marginTop: 12 },
  summaryCard: { gap: 15 },
  summaryHeader: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  summaryTitle: { fontFamily: 'Inter_700Bold', fontSize: 15 },
  summaryText: { fontFamily: 'Inter_400Regular', fontSize: 15, lineHeight: 24 },
  bottom: { marginTop: 'auto', gap: 17, paddingTop: 28 },
  skip: { textAlign: 'center', fontFamily: 'Inter_600SemiBold', fontSize: 13 },
  disclaimer: { textAlign: 'center', fontFamily: 'Inter_400Regular', fontSize: 11, lineHeight: 16 },
});