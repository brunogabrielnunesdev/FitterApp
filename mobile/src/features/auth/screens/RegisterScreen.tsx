import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { Href, router } from 'expo-router';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { Pressable, Text, View } from 'react-native';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { FormField } from '@/common/components/input/FormField';
import {
  AuthScreen,
  authScreenStyles,
} from '@/features/auth/components/AuthScreen';
import { register } from '@/features/auth/services/authService';
import { getAuthErrorMessage } from '@/features/auth/utils/getAuthErrorMessage';
import {
  RegisterForm,
  registerSchema,
} from '@/features/auth/validation/registerSchema';

export function RegisterScreen() {
  const [passwordVisible, setPasswordVisible] = useState(false);
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      fullName: '',
      email: '',
      phoneNumber: '',
      password: '',
      passwordConfirmation: '',
    },
  });
  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (_, request) =>
      router.replace(`/confirm-email?email=${encodeURIComponent(request.email)}` as Href),
  });

  return (
    <AuthScreen
      eyebrow="CRIE SUA CONTA"
      title="Comece sua evolução."
      subtitle="Seu cadastro leva menos de um minuto. Depois, confirme seu e-mail para entrar."
      footer={
        <Pressable onPress={() => router.replace('/')}>
          <Text style={authScreenStyles.footer}>
            JÁ POSSUI CONTA? <Text style={authScreenStyles.footerAccent}>ENTRAR</Text>
          </Text>
        </Pressable>
      }>
      <Controller
        control={control}
        name="fullName"
        render={({ field }) => (
          <FormField
            autoCapitalize="words"
            autoComplete="name"
            error={errors.fullName?.message}
            label="Nome completo"
            onBlur={field.onBlur}
            onChangeText={field.onChange}
            placeholder="Seu nome"
            value={field.value}
          />
        )}
      />
      <Controller
        control={control}
        name="email"
        render={({ field }) => (
          <FormField
            autoCapitalize="none"
            autoComplete="email"
            error={errors.email?.message}
            keyboardType="email-address"
            label="E-mail"
            onBlur={field.onBlur}
            onChangeText={field.onChange}
            placeholder="voce@email.com"
            value={field.value}
          />
        )}
      />
      <Controller
        control={control}
        name="phoneNumber"
        render={({ field }) => (
          <FormField
            autoComplete="tel"
            error={errors.phoneNumber?.message}
            keyboardType="phone-pad"
            label="WhatsApp"
            onBlur={field.onBlur}
            onChangeText={field.onChange}
            placeholder="+5544999999999"
            value={field.value}
          />
        )}
      />
      <Controller
        control={control}
        name="password"
        render={({ field }) => (
          <FormField
            action={{
              label: passwordVisible ? 'Ocultar' : 'Mostrar',
              onPress: () => setPasswordVisible((value) => !value),
            }}
            autoCapitalize="none"
            autoComplete="new-password"
            error={errors.password?.message}
            label="Senha"
            onBlur={field.onBlur}
            onChangeText={field.onChange}
            placeholder="Mínimo de 8 caracteres"
            secureTextEntry={!passwordVisible}
            value={field.value}
          />
        )}
      />
      <Controller
        control={control}
        name="passwordConfirmation"
        render={({ field }) => (
          <FormField
            autoCapitalize="none"
            autoComplete="new-password"
            error={errors.passwordConfirmation?.message}
            label="Confirmar senha"
            onBlur={field.onBlur}
            onChangeText={field.onChange}
            onSubmitEditing={handleSubmit((form) => mutation.mutate(form))}
            placeholder="Repita sua senha"
            returnKeyType="done"
            secureTextEntry={!passwordVisible}
            value={field.value}
          />
        )}
      />
      {mutation.isError && (
        <View style={authScreenStyles.errorBox}>
          <Text style={authScreenStyles.errorText}>{getAuthErrorMessage(mutation.error)}</Text>
        </View>
      )}
      <PrimaryButton
        label="Criar minha conta"
        loading={mutation.isPending}
        onPress={handleSubmit((form) => mutation.mutate(form))}
      />
    </AuthScreen>
  );
}
