import { api } from '@/common/services/api';

export function profileImageUrl(key: string | null) {
  if (!key || !/^personals\/[0-9a-f-]{36}\/[0-9a-f-]{36}\.webp$/.test(key)) return undefined;
  return `${(api.defaults.baseURL ?? '').replace(/\/$/, '')}/api/v1/public/media/${key}`;
}
