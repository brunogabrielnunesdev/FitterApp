import { useMutation } from '@tanstack/react-query';
import { Href, router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Pressable, Text, View } from 'react-native';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { FormField } from '@/common/components/input/FormField';
import { AuthScreen, authScreenStyles } from '@/features/auth/components/AuthScreen';
import { requestPasswordReset } from '@/features/auth/services/authService';
import { getSafeReturnPath, withReturnPath } from '@/features/auth/utils/authNavigation';

export function ForgotPasswordScreen() {
  const { returnTo } = useLocalSearchParams<{ returnTo?: string | string[] }>();
  const returnPath = getSafeReturnPath(returnTo);
  const [email, setEmail] = useState('');
  const request = useMutation({ mutationFn: () => requestPasswordReset(email.trim()) });
  return (
    <AuthScreen eyebrow="RECUPERAR ACESSO" title="Redefina sua senha." subtitle="Informe seu e-mail. Quando a entrega de e-mail estiver ativa, você receberá o link de redefinição." footer={<Pressable onPress={() => router.replace(withReturnPath('/login', returnPath) as Href)}><Text style={authScreenStyles.footer}>VOLTAR PARA O LOGIN</Text></Pressable>}>
      <FormField autoCapitalize="none" keyboardType="email-address" label="E-mail" placeholder="voce@email.com" value={email} onChangeText={setEmail} />
      {request.isSuccess && <View style={authScreenStyles.successBox}><Text style={authScreenStyles.successText}>Se a conta existir, a solicitação foi registrada.</Text></View>}
      {request.isError && <View style={authScreenStyles.errorBox}><Text style={authScreenStyles.errorText}>Não foi possível registrar a solicitação.</Text></View>}
      <PrimaryButton disabled={!email.trim()} loading={request.isPending} label="Solicitar redefinição" onPress={() => request.mutate()} />
    </AuthScreen>
  );
}
