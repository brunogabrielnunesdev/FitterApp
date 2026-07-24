import { zodResolver } from '@hookform/resolvers/zod';
import { DMSans_400Regular, DMSans_500Medium, DMSans_700Bold } from '@expo-google-fonts/dm-sans';
import { Manrope_700Bold, Manrope_800ExtraBold } from '@expo-google-fonts/manrope';
import { useFonts } from 'expo-font';
import { Href, router } from 'expo-router';
import { useMutation } from '@tanstack/react-query';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import {
  Image,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { FormField } from '@/common/components/input/FormField';
import { colors } from '@/common/theme/colors';
import { useAuth } from '@/features/auth/context/AuthContext';
import { login } from '@/features/auth/services/authService';
import { getLoginErrorMessage } from '@/features/auth/utils/getLoginErrorMessage';
import { LoginForm, loginSchema } from '@/features/auth/validation/loginSchema';

export function LoginScreen() {
  const { startSession } = useAuth();
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [fontsLoaded] = useFonts({
    DMSans_400Regular,
    DMSans_500Medium,
    DMSans_700Bold,
    Manrope_700Bold,
    Manrope_800ExtraBold,
  });
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });
  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: async (session) => {
      await startSession(session);
      router.replace('/home');
    },
  });

  if (!fontsLoaded) {
    return <View style={styles.loadingScreen} />;
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <View pointerEvents="none" style={styles.background}>
        <View style={styles.limeGlow} />
        <View style={styles.violetGlow} />
        <View style={styles.gridLineOne} />
        <View style={styles.gridLineTwo} />
      </View>

      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={styles.keyboardView}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}>
          <View style={styles.brand}>
            <Image
              accessibilityLabel="FitterApp"
              resizeMode="contain"
              source={require('../../../../assets/images/fitterapp-logo.png')}
              style={styles.logo}
            />
            <Text style={styles.brandName}>FITTERAPP</Text>
          </View>

          <View style={styles.heading}>
            <Text style={styles.eyebrow}>BEM-VINDO DE VOLTA</Text>
            <Text style={styles.title}>Entre no seu ritmo.</Text>
            <Text style={styles.subtitle}>
              Acesse sua conta e continue construindo sua melhor versão.
            </Text>
          </View>

          <View style={styles.formCard}>
            <Controller
              control={control}
              name="email"
              render={({ field: { onBlur, onChange, ref, value } }) => (
                <FormField
                  ref={ref}
                  autoCapitalize="none"
                  autoComplete="email"
                  error={errors.email?.message}
                  keyboardType="email-address"
                  label="E-mail"
                  onBlur={onBlur}
                  onChangeText={onChange}
                  placeholder="voce@email.com"
                  returnKeyType="next"
                  value={value}
                />
              )}
            />

            <Controller
              control={control}
              name="password"
              render={({ field: { onBlur, onChange, ref, value } }) => (
                <FormField
                  ref={ref}
                  action={{
                    label: passwordVisible ? 'Ocultar' : 'Mostrar',
                    onPress: () => setPasswordVisible((visible) => !visible),
                  }}
                  autoCapitalize="none"
                  autoComplete="current-password"
                  error={errors.password?.message}
                  label="Senha"
                  onBlur={onBlur}
                  onChangeText={onChange}
                  onSubmitEditing={handleSubmit((form) => loginMutation.mutate(form))}
                  placeholder="Sua senha"
                  returnKeyType="done"
                  secureTextEntry={!passwordVisible}
                  value={value}
                />
              )}
            />

            {loginMutation.isError && (
              <View style={styles.errorBox}>
                <Text style={styles.errorText}>{getLoginErrorMessage(loginMutation.error)}</Text>
              </View>
            )}

            <PrimaryButton
              label="Entrar"
              loading={loginMutation.isPending}
              onPress={handleSubmit((form) => loginMutation.mutate(form))}
            />
          </View>

          <Pressable onPress={() => router.push('/register' as Href)}>
            <Text style={styles.footer}>
              NOVO POR AQUI? <Text style={styles.footerAccent}>CRIAR CONTA</Text>
            </Text>
          </Pressable>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  loadingScreen: { flex: 1, backgroundColor: colors.black },
  safeArea: { flex: 1, backgroundColor: colors.black },
  keyboardView: { flex: 1 },
  background: { ...StyleSheet.absoluteFill, overflow: 'hidden' },
  limeGlow: {
    position: 'absolute',
    width: 300,
    height: 300,
    borderRadius: 150,
    backgroundColor: colors.lime,
    opacity: 0.08,
    top: -180,
    right: -100,
  },
  violetGlow: {
    position: 'absolute',
    width: 240,
    height: 240,
    borderRadius: 120,
    backgroundColor: colors.violet,
    opacity: 0.08,
    bottom: -130,
    left: -130,
  },
  gridLineOne: {
    position: 'absolute',
    width: 1,
    height: '100%',
    backgroundColor: colors.line,
    opacity: 0.35,
    left: '24%',
  },
  gridLineTwo: {
    position: 'absolute',
    width: 1,
    height: '100%',
    backgroundColor: colors.line,
    opacity: 0.25,
    right: '16%',
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: 24,
    paddingVertical: 28,
    gap: 30,
  },
  brand: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  logo: { width: 48, height: 48 },
  brandName: {
    color: colors.warmWhite,
    fontFamily: 'Manrope_800ExtraBold',
    fontSize: 17,
    letterSpacing: 2.2,
  },
  heading: { gap: 10 },
  eyebrow: {
    color: colors.lime,
    fontFamily: 'DMSans_700Bold',
    fontSize: 11,
    letterSpacing: 2,
  },
  title: {
    color: colors.warmWhite,
    fontFamily: 'Manrope_800ExtraBold',
    fontSize: 39,
    lineHeight: 43,
    letterSpacing: -1.4,
    maxWidth: 320,
  },
  subtitle: {
    color: colors.gray,
    fontFamily: 'DMSans_400Regular',
    fontSize: 15,
    lineHeight: 23,
    maxWidth: 330,
  },
  formCard: {
    borderRadius: 28,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: 'rgba(17, 17, 17, 0.92)',
    padding: 20,
    gap: 18,
  },
  errorBox: {
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(255, 107, 107, 0.35)',
    backgroundColor: 'rgba(255, 107, 107, 0.08)',
    paddingHorizontal: 14,
    paddingVertical: 11,
  },
  errorText: {
    color: colors.danger,
    fontFamily: 'DMSans_500Medium',
    fontSize: 13,
    lineHeight: 19,
  },
  footer: {
    color: colors.muted,
    fontFamily: 'DMSans_700Bold',
    fontSize: 11,
    letterSpacing: 1.25,
    textAlign: 'center',
  },
  footerAccent: { color: colors.warmWhite },
});
