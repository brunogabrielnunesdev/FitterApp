import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useEffect, useRef, useState } from 'react';
import { AppState } from 'react-native';
import { api } from '@/common/services/api';
import { createIdempotencyKey, getMetricHeaders, MOBILE_EVENT_SOURCE } from '@/common/services/metrics';

import { colors } from '@/common/theme/colors';
import { AuthProvider } from '@/features/auth/context/AuthContext';

function TrackAppAccess() {
  const previous = useRef(AppState.currentState);
  const initialTracked = useRef(false);
  useEffect(() => {
    const track = () => {
      const key = createIdempotencyKey();
      void getMetricHeaders(key).then((headers) => api.post('/api/v1/public/events/app-open', undefined, { params: { source: MOBILE_EVENT_SOURCE }, headers })).catch(() => {});
    };
    if (!initialTracked.current) { initialTracked.current = true; track(); }
    const subscription = AppState.addEventListener('change', (next) => {
      if (previous.current.match(/inactive|background/) && next === 'active') track();
      previous.current = next;
    });
    return () => subscription.remove();
  }, []);
  return null;
}

export default function RootLayout() {
  const [queryClient] = useState(() => new QueryClient());

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <TrackAppAccess />
        <StatusBar style="light" />
        <Stack
          screenOptions={{
            headerShown: false,
            contentStyle: { backgroundColor: colors.black },
            animation: 'fade',
          }}
        />
      </AuthProvider>
    </QueryClientProvider>
  );
}
