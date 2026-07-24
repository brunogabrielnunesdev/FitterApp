import { useMutation } from '@tanstack/react-query';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import { Pressable, Text, View } from 'react-native';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { FormField } from '@/common/components/input/FormField';
import {
  AuthScreen,
  authScreenStyles,
} from '@/features/auth/components/AuthScreen';
import {
  confirmEmail,
  resendConfirmation,
} from '@/features/auth/services/authService';
import { getAuthErrorMessage } from '@/features/auth/utils/getAuthErrorMessage';

function firstParam(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value ?? '';
}

export function ConfirmEmailScreen() {
  const params = useLocalSearchParams<{ email?: string | string[]; token?: string | string[] }>();
  const [email, setEmail] = useState(() => firstParam(params.email));
  const [token, setToken] = useState(() => firstParam(params.token));
  const processedToken = useRef('');
  const confirmMutation = useMutation({
    mutationFn: confirmEmail,
    onSuccess: () => setTimeout(() => router.replace('/'), 900),
  });
  const resendMutation = useMutation({ mutationFn: resendConfirmation });
  const { mutate: confirm } = confirmMutation;

  useEffect(() => {
    const receivedToken = firstParam(params.token);
    if (receivedToken && processedToken.current !== receivedToken) {
      processedToken.current = receivedToken;
      confirm(receivedToken);
    }
  }, [confirm, params.token]);

  return (
    <AuthScreen
      eyebrow="CONFIRME SEU E-MAIL"
      title="Só falta um passo."
      subtitle="Abra o e-mail enviado pelo FitterApp. O link confirma automaticamente; no teste local, você também pode colar o token."
      footer={
        <Pressable onPress={() => router.replace('/')}>
          <Text style={authScreenStyles.footer}>
            VOLTAR PARA <Text style={authScreenStyles.footerAccent}>ENTRAR</Text>
          </Text>
        </Pressable>
      }>
      <FormField
        autoCapitalize="none"
        label="Token de confirmação"
        multiline
        onChangeText={setToken}
        placeholder="Cole aqui o token recebido"
        value={token}
      />
      {confirmMutation.isError && (
        <View style={authScreenStyles.errorBox}>
          <Text style={authScreenStyles.errorText}>
            {getAuthErrorMessage(confirmMutation.error)}
          </Text>
        </View>
      )}
      {confirmMutation.isSuccess && (
        <View style={authScreenStyles.successBox}>
          <Text style={authScreenStyles.successText}>
            E-mail confirmado! Você já pode entrar.
          </Text>
        </View>
      )}
      <PrimaryButton
        disabled={!token.trim() || confirmMutation.isSuccess}
        label="Confirmar e-mail"
        loading={confirmMutation.isPending}
        onPress={() => confirmMutation.mutate(token.trim())}
      />
      <FormField
        autoCapitalize="none"
        autoComplete="email"
        keyboardType="email-address"
        label="Não recebeu? Informe o e-mail"
        onChangeText={setEmail}
        placeholder="voce@email.com"
        value={email}
      />
      {resendMutation.isSuccess && (
        <View style={authScreenStyles.successBox}>
          <Text style={authScreenStyles.successText}>Novo e-mail solicitado.</Text>
        </View>
      )}
      {resendMutation.isError && (
        <View style={authScreenStyles.errorBox}>
          <Text style={authScreenStyles.errorText}>
            {getAuthErrorMessage(resendMutation.error)}
          </Text>
        </View>
      )}
      <PrimaryButton
        disabled={!email.trim()}
        label="Reenviar confirmação"
        loading={resendMutation.isPending}
        onPress={() => resendMutation.mutate(email.trim().toLowerCase())}
        variant="secondary"
      />
    </AuthScreen>
  );
}
