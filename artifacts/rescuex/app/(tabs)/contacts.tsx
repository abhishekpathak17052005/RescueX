import { Feather } from '@expo/vector-icons';
import React, { useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { Button, Card, Header, Screen, SectionLabel } from '@/components/RescueUI';
import { useColors } from '@/hooks/useColors';
import { useRescueX } from '@/state/RescueXContext';

export default function ContactsScreen() {
  const colors = useColors();
  const { contacts, addContact, deleteContact, setPrimaryContact } = useRescueX();
  const [adding, setAdding] = useState(false);
  const [name, setName] = useState('');
  const [relationship, setRelationship] = useState('');
  const [phone, setPhone] = useState('');
  const save = () => {
    if (!name.trim() || !phone.trim()) return Alert.alert('Add a contact', 'Please add a name and phone number.');
    addContact({ name: name.trim(), relationship: relationship.trim() || 'Emergency contact', phone: phone.trim(), primary: contacts.length === 0 });
    setName(''); setRelationship(''); setPhone(''); setAdding(false);
  };
  return (
    <Screen>
      <Header title="Contacts" subtitle="People RescueX can notify" />
      <View style={styles.topLine}><SectionLabel>Emergency contacts</SectionLabel><Pressable onPress={() => setAdding((current) => !current)} hitSlop={10}><Feather name={adding ? 'x' : 'plus'} size={21} color={colors.primary} /></Pressable></View>
      {adding ? <Card style={styles.form}><TextInput placeholder="Full name" placeholderTextColor={colors.mutedForeground} value={name} onChangeText={setName} style={[styles.input, { color: colors.foreground, borderColor: colors.border }]} /><TextInput placeholder="Relationship" placeholderTextColor={colors.mutedForeground} value={relationship} onChangeText={setRelationship} style={[styles.input, { color: colors.foreground, borderColor: colors.border }]} /><TextInput placeholder="Phone number" placeholderTextColor={colors.mutedForeground} value={phone} onChangeText={setPhone} keyboardType="phone-pad" style={[styles.input, { color: colors.foreground, borderColor: colors.border }]} /><Button label="Save contact" icon="check" onPress={save} /></Card> : null}
      {contacts.map((contact) => <Card key={contact.id} style={styles.contactCard}><View style={[styles.avatar, { backgroundColor: colors.accent }]}><Text style={[styles.avatarText, { color: colors.accentForeground }]}>{contact.name.charAt(0)}</Text></View><View style={{ flex: 1 }}><Text style={[styles.contactName, { color: colors.foreground }]}>{contact.name}</Text><Text style={[styles.contactMeta, { color: colors.mutedForeground }]}>{contact.relationship} · {contact.phone}</Text>{contact.primary ? <Text style={[styles.primary, { color: colors.accentForeground }]}>Primary contact</Text> : null}</View><Pressable onPress={() => setPrimaryContact(contact.id)} hitSlop={8} style={styles.action}><Feather name={contact.primary ? 'star' : 'star'} size={18} color={contact.primary ? colors.primary : colors.mutedForeground} /></Pressable><Pressable onPress={() => deleteContact(contact.id)} hitSlop={8} style={styles.action}><Feather name="trash-2" size={17} color={colors.mutedForeground} /></Pressable></Card>)}
      {contacts.length === 0 ? <Card style={styles.empty}><Feather name="users" size={22} color={colors.mutedForeground} /><Text style={[styles.emptyTitle, { color: colors.foreground }]}>Add someone you trust</Text><Text style={[styles.emptyCopy, { color: colors.mutedForeground }]}>RescueX will keep them ready for an active emergency.</Text></Card> : null}
    </Screen>
  );
}

const styles = StyleSheet.create({
  topLine: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  form: { gap: 11, marginBottom: 14 },
  input: { minHeight: 48, borderWidth: 1, borderRadius: 14, paddingHorizontal: 14, fontFamily: 'Inter_400Regular', fontSize: 14 },
  contactCard: { flexDirection: 'row', alignItems: 'center', gap: 11, padding: 13, marginBottom: 10 },
  avatar: { width: 44, height: 44, borderRadius: 16, alignItems: 'center', justifyContent: 'center' },
  avatarText: { fontFamily: 'Inter_700Bold', fontSize: 18 },
  contactName: { fontFamily: 'Inter_700Bold', fontSize: 14 },
  contactMeta: { fontFamily: 'Inter_400Regular', fontSize: 11, marginTop: 4 },
  primary: { fontFamily: 'Inter_700Bold', fontSize: 10, marginTop: 5, letterSpacing: 0.3 },
  action: { padding: 3 },
  empty: { alignItems: 'center', paddingVertical: 28, marginTop: 5 },
  emptyTitle: { fontFamily: 'Inter_700Bold', fontSize: 15, marginTop: 12 },
  emptyCopy: { fontFamily: 'Inter_400Regular', fontSize: 12, textAlign: 'center', marginTop: 5 },
});