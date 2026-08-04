import { useMutation } from '@tanstack/react-query';
import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Text, View } from 'react-native';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { FormField } from '@/common/components/input/FormField';
import { AuthScreen, authScreenStyles } from '@/features/auth/components/AuthScreen';
import { resetPassword } from '@/features/auth/services/authService';

export function ResetPasswordScreen() {
  const params = useLocalSearchParams<{ token?: string }>();
  const [token, setToken] = useState(params.token ?? '');
  const [password, setPassword] = useState('');
  const reset = useMutation({ mutationFn: () => resetPassword(token.trim(), password), onSuccess: () => setTimeout(() => router.replace('/login'), 1200) });
  return (
    <AuthScreen eyebrow="NOVA SENHA" title="Crie uma nova senha." subtitle="Use o token recebido no link de recuperação e escolha uma senha com ao menos 8 caracteres.">
      <FormField autoCapitalize="none" label="Token" value={token} onChangeText={setToken} />
      <FormField autoCapitalize="none" label="Nova senha" secureTextEntry value={password} onChangeText={setPassword} />
      {reset.isSuccess && <View style={authScreenStyles.successBox}><Text style={authScreenStyles.successText}>Senha alterada. Redirecionando para o login...</Text></View>}
      {reset.isError && <View style={authScreenStyles.errorBox}><Text style={authScreenStyles.errorText}>Token inválido, expirado ou já utilizado.</Text></View>}
      <PrimaryButton disabled={!token.trim() || password.length < 8} loading={reset.isPending} label="Alterar senha" onPress={() => reset.mutate()} />
    </AuthScreen>
  );
}
