import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { colors } from '@/common/theme/colors';

export function SessionLoadingScreen() {
  return (
    <View accessibilityLabel="Restaurando sua sessão" accessibilityRole="progressbar" style={styles.container}>
      <ActivityIndicator color={colors.lime} size="large" />
      <Text style={styles.text}>Preparando o FitterApp...</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    backgroundColor: colors.black,
    flex: 1,
    gap: 16,
    justifyContent: 'center',
    padding: 24,
  },
  text: { color: colors.gray, fontSize: 14 },
});
