import { Redirect, router } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { SessionLoadingScreen } from '@/common/components/session/SessionLoadingScreen';
import { colors } from '@/common/theme/colors';
import { useAuth } from '@/features/auth/context/AuthContext';

export default function AuthenticatedHomeScreen() {
  const { endSession, isLoading, session } = useAuth();

  async function handleLogout() {
    await endSession();
    router.replace('/catalog');
  }

  if (isLoading) return <SessionLoadingScreen />;
  if (!session) return <Redirect href="/login?returnTo=%2Fhome" />;

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.content}>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>FITTERAPP</Text>
        </View>
        <Text style={styles.title}>Seu treino começa aqui.</Text>
        <Text style={styles.description}>
          Encontre profissionais ou crie seu perfil para aparecer no catálogo da sua região.
        </Text>
        <PrimaryButton label="Explorar personais" onPress={() => router.push('/catalog')} />
        <PrimaryButton
          label="Quero ser personal"
          onPress={() => router.push('/personal-profile')}
          variant="secondary"
        />
        <PrimaryButton label="Sair da conta" onPress={handleLogout} variant="secondary" />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.black },
  content: { flex: 1, justifyContent: 'center', paddingHorizontal: 24, gap: 18 },
  badge: {
    alignSelf: 'flex-start',
    backgroundColor: colors.lime,
    borderRadius: 999,
    paddingHorizontal: 14,
    paddingVertical: 7,
  },
  badgeText: { color: colors.black, fontSize: 11, fontWeight: '800', letterSpacing: 1.5 },
  title: { color: colors.warmWhite, fontSize: 40, fontWeight: '800', letterSpacing: -1.4 },
  description: { color: colors.gray, fontSize: 16, lineHeight: 25, marginBottom: 16 },
});
