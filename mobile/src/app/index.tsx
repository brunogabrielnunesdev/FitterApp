import { Redirect } from 'expo-router';

import { SessionLoadingScreen } from '@/common/components/session/SessionLoadingScreen';
import { useAuth } from '@/features/auth/context/AuthContext';

export default function IndexScreen() {
  const { isLoading } = useAuth();

  if (isLoading) return <SessionLoadingScreen />;
  return <Redirect href="/catalog" />;
}
