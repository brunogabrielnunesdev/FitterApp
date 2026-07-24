import { router } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { colors } from '@/common/theme/colors';
import { clearSession } from '@/features/auth/services/sessionStorage';

export default function AuthenticatedHomeScreen() {
  async function handleLogout() {
    await clearSession();
    router.replace('/');
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.content}>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>LOGIN VALIDADO</Text>
        </View>
        <Text style={styles.title}>Você está dentro.</Text>
        <Text style={styles.description}>
          Esta é uma área temporária para confirmar a sessão mobile com a API do FitterApp.
        </Text>
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
