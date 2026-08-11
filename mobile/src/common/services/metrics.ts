import * as SecureStore from 'expo-secure-store';

const VISITOR_ID_KEY = 'fitterapp.visitor-id';

export const MOBILE_EVENT_SOURCE = 'MOBILE_APP';

function randomIdentifier() {
  const uuid = globalThis.crypto?.randomUUID?.();
  if (uuid) return uuid;
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}-${Math.random().toString(36).slice(2)}`;
}

export async function getVisitorId() {
  const existing = await SecureStore.getItemAsync(VISITOR_ID_KEY);
  if (existing) return existing;

  const visitorId = randomIdentifier();
  await SecureStore.setItemAsync(VISITOR_ID_KEY, visitorId);
  return visitorId;
}

export function createIdempotencyKey() {
  return randomIdentifier();
}

export async function getMetricHeaders(idempotencyKey: string) {
  return {
    'X-Visitor-Id': await getVisitorId(),
    'X-Idempotency-Key': idempotencyKey,
  };
}
