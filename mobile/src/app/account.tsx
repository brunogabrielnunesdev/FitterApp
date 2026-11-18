import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Redirect, router } from 'expo-router';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { FormField } from '@/common/components/input/FormField';
import { SessionLoadingScreen } from '@/common/components/session/SessionLoadingScreen';
import { colors } from '@/common/theme/colors';
import { Account, getOwnAccount, updateOwnAccount } from '@/features/account/services/accountService';
import { useAuth } from '@/features/auth/context/AuthContext';

export default function AccountScreen() {
  const { session, isLoading } = useAuth();
  if (isLoading) return <SessionLoadingScreen />;
  if (!session) return <Redirect href="/login?returnTo=%2Faccount" />;
  return <AccountContent />;
}

function AccountContent() {
  const query = useQuery({ queryKey: ['own-account'], queryFn: getOwnAccount, retry: false });
  if (query.isLoading) return <SessionLoadingScreen />;
  if (!query.data) return <SafeAreaView style={styles.safe}><View style={styles.content}><Text style={styles.error}>Não foi possível carregar seus dados.</Text><PrimaryButton label="Tentar novamente" onPress={() => void query.refetch()} /></View></SafeAreaView>;
  return <AccountForm account={query.data} />;
}

function AccountForm({ account }: { account: Account }) {
  const queryClient = useQueryClient();
  const [fullName, setFullName] = useState(account.fullName);
  const [phoneNumber, setPhoneNumber] = useState(account.phoneNumber);
  const [attempted, setAttempted] = useState(false);
  const nameValid = fullName.trim().length > 0 && fullName.trim().length <= 120;
  const phoneValid = /^\+[1-9][0-9]{7,14}$/.test(phoneNumber.trim());
  const save = useMutation({
    mutationFn: () => updateOwnAccount(fullName.trim(), phoneNumber.trim()),
    onSuccess: (updated) => { queryClient.setQueryData(['own-account'], updated); setFullName(updated.fullName); setPhoneNumber(updated.phoneNumber); },
  });
  return (
    <SafeAreaView style={styles.safe}>
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <Pressable onPress={() => router.back()}><Text style={styles.back}>‹ Voltar</Text></Pressable>
        <Text style={styles.kicker}>SUA CONTA</Text>
        <Text style={styles.title}>Seus dados.</Text>
        <Text style={styles.description}>Atualize seu nome e telefone. O e-mail é usado para acessar sua conta.</Text>
        <FormField label="Nome completo" value={fullName} onChangeText={(value) => { setFullName(value); save.reset(); }} />
        {attempted && !nameValid && <Text style={styles.error}>Informe um nome com até 120 caracteres.</Text>}
        <FormField label="Telefone" value={phoneNumber} onChangeText={(value) => { setPhoneNumber(value); save.reset(); }} keyboardType="phone-pad" autoCapitalize="none" />
        {attempted && !phoneValid && <Text style={styles.error}>Use o formato internacional, por exemplo +5544999999999.</Text>}
        <View style={styles.emailBox}><Text style={styles.label}>E-MAIL</Text><Text style={styles.email}>{account.email}</Text></View>
        {save.isSuccess && <Text style={styles.success}>Dados atualizados.</Text>}
        {save.isError && <Text style={styles.error}>Não foi possível salvar seus dados. Tente novamente.</Text>}
        <PrimaryButton label="Salvar alterações" loading={save.isPending} onPress={() => { setAttempted(true); if (nameValid && phoneValid) save.mutate(); }} />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.black }, content: { gap: 16, padding: 24, paddingBottom: 48 },
  back: { color: colors.lime, fontWeight: '700' }, kicker: { color: colors.lime, fontWeight: '800' },
  title: { color: colors.warmWhite, fontSize: 34, fontWeight: '900' }, description: { color: colors.gray, lineHeight: 22 },
  emailBox: { gap: 5, paddingVertical: 8 }, label: { color: colors.gray, fontSize: 11, fontWeight: '800' },
  email: { color: colors.warmWhite, fontSize: 16 }, error: { color: colors.danger }, success: { color: colors.lime },
});
