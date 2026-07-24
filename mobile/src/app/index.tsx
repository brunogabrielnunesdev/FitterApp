import { Redirect } from 'expo-router';
import { View } from 'react-native';

import { colors } from '@/common/theme/colors';
import { useAuth } from '@/features/auth/context/AuthContext';
import { LoginScreen } from '@/features/auth/screens/LoginScreen';

export default function IndexScreen() {
  const { isLoading, session } = useAuth();

  if (isLoading) return <View style={{ flex: 1, backgroundColor: colors.black }} />;
  if (session) return <Redirect href="/home" />;
  return <LoginScreen />;
}
