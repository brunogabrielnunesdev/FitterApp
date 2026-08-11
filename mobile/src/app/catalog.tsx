import { useInfiniteQuery, useQuery, useQueryClient } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { router } from 'expo-router';
import { useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { colors } from '@/common/theme/colors';
import { createIdempotencyKey } from '@/common/services/metrics';
import { useAuth } from '@/features/auth/context/AuthContext';
import { ProfileCard } from '@/features/catalog/components/ProfileCard';
import { useDebouncedValue } from '@/features/catalog/hooks/useDebouncedValue';
import {
  listActiveModalities,
  listPublicProfiles,
} from '@/features/catalog/services/catalogService';
import {
  PublicProfileCard,
  ServiceMode,
} from '@/features/catalog/types/catalog';

const PAGE_SIZE = 20;

const serviceModeFilters: { label: string; value?: ServiceMode }[] = [
  { label: 'Todos' },
  { label: 'Presencial', value: 'IN_PERSON' },
  { label: 'Em casa', value: 'HOME_VISIT' },
  { label: 'Online', value: 'ONLINE' },
];

export default function CatalogScreen() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [modalityId, setModalityId] = useState<number>();
  const [neighborhood, setNeighborhood] = useState('');
  const [serviceMode, setServiceMode] = useState<ServiceMode>();
  const debouncedSearch = useDebouncedValue(search.trim());
  const debouncedNeighborhood = useDebouncedValue(neighborhood.trim());
  const searchIntentKeys = useRef(new Map<string, string>());
  const modalitiesQuery = useQuery({
    queryKey: ['public-modalities'],
    queryFn: listActiveModalities,
    staleTime: 5 * 60 * 1000,
  });
  const catalogQueryKey = [
    'public-profiles',
    debouncedSearch,
    modalityId,
    debouncedNeighborhood,
    serviceMode,
  ] as const;
  const profilesQuery = useInfiniteQuery({
    queryKey: catalogQueryKey,
    initialPageParam: 0,
    queryFn: ({ pageParam }) => {
      const intent = `${debouncedSearch}|${modalityId ?? ''}|${debouncedNeighborhood}|${serviceMode ?? ''}|${pageParam}`;
      const idempotencyKey = searchIntentKeys.current.get(intent) ?? createIdempotencyKey();
      searchIntentKeys.current.set(intent, idempotencyKey);
      return listPublicProfiles({
        page: pageParam,
        size: PAGE_SIZE,
        query: debouncedSearch,
        modalityId,
        neighborhood: debouncedNeighborhood,
        serviceMode,
        idempotencyKey,
      });
    },
    getNextPageParam: (lastPage) =>
      lastPage.page + 1 < lastPage.totalPages ? lastPage.page + 1 : undefined,
  });
  const profiles = useMemo(
    () => {
      const seen = new Set<string>();
      return (profilesQuery.data?.pages.flatMap((page) => page.content) ?? []).filter((profile) => {
        if (seen.has(profile.profileId)) return false;
        seen.add(profile.profileId);
        return true;
      });
    },
    [profilesQuery.data],
  );

  function renderProfile({ item }: { item: PublicProfileCard }) {
    return (
      <ProfileCard
        onPress={() => router.push({ pathname: '/personal/[slug]', params: { slug: item.slug } })}
        profile={item}
      />
    );
  }

  function startNewCatalogIntent(action: () => void) {
    searchIntentKeys.current.clear();
    action();
  }

  return (
    <SafeAreaView edges={['top']} style={styles.safeArea}>
      <View style={styles.header}>
        <Text style={styles.brand}>FITTERAPP</Text>
        <Pressable
          accessibilityLabel={session ? 'Abrir minha conta' : 'Entrar ou criar conta'}
          accessibilityRole="button"
          onPress={() => router.push(session ? '/home' : '/login')}>
          <Text style={styles.login}>{session ? 'Minha conta' : 'Entrar'}</Text>
        </Pressable>
      </View>

      <FlatList
        contentContainerStyle={styles.content}
        data={profiles}
        keyExtractor={(profile) => profile.profileId}
        ListEmptyComponent={
          <CatalogEmptyState
            error={profilesQuery.error}
            isError={profilesQuery.isError}
            isLoading={profilesQuery.isLoading}
            onRetry={() => profilesQuery.refetch()}
          />
        }
        ListFooterComponent={
          profilesQuery.isFetchingNextPage ? (
            <ActivityIndicator color={colors.lime} style={styles.footer} />
          ) : profilesQuery.isFetchNextPageError ? (
            <View style={styles.footerState}>
              <Text style={styles.footerError}>{getCatalogErrorMessage(profilesQuery.error)}</Text>
              <Pressable
                accessibilityLabel="Tentar carregar mais personais"
                accessibilityRole="button"
                onPress={() => profilesQuery.fetchNextPage()}>
                <Text style={styles.retry}>Tentar novamente</Text>
              </Pressable>
            </View>
          ) : null
        }
        ListHeaderComponent={
          <CatalogHeader
            search={search}
            modalityId={modalityId}
            modalities={modalitiesQuery.data ?? []}
            modalitiesError={modalitiesQuery.isError}
            neighborhood={neighborhood}
            serviceMode={serviceMode}
            onChangeSearch={(value) => startNewCatalogIntent(() => setSearch(value))}
            onChangeModality={(value) => startNewCatalogIntent(() => setModalityId(value))}
            onChangeNeighborhood={(value) => startNewCatalogIntent(() => setNeighborhood(value))}
            onChangeServiceMode={(value) => startNewCatalogIntent(() => setServiceMode(value))}
            onRetryModalities={() => modalitiesQuery.refetch()}
          />
        }
        onEndReached={() => {
          if (profilesQuery.hasNextPage && !profilesQuery.isFetching) {
            profilesQuery.fetchNextPage();
          }
        }}
        onEndReachedThreshold={0.4}
        refreshControl={
          <RefreshControl
            colors={[colors.lime]}
            refreshing={profilesQuery.isRefetching && !profilesQuery.isFetchingNextPage}
            tintColor={colors.lime}
            onRefresh={() => {
              searchIntentKeys.current.clear();
              void queryClient.resetQueries({ queryKey: catalogQueryKey });
            }}
          />
        }
        renderItem={renderProfile}
        showsVerticalScrollIndicator={false}
      />
    </SafeAreaView>
  );
}

type CatalogHeaderProps = {
  search: string;
  modalityId?: number;
  modalities: { id: number; name: string; slug: string }[];
  modalitiesError: boolean;
  neighborhood: string;
  serviceMode?: ServiceMode;
  onChangeSearch: (value: string) => void;
  onChangeModality: (value?: number) => void;
  onChangeNeighborhood: (value: string) => void;
  onChangeServiceMode: (value?: ServiceMode) => void;
  onRetryModalities: () => void;
};

function CatalogHeader({
  search,
  modalityId,
  modalities,
  modalitiesError,
  neighborhood,
  serviceMode,
  onChangeSearch,
  onChangeModality,
  onChangeNeighborhood,
  onChangeServiceMode,
  onRetryModalities,
}: CatalogHeaderProps) {
  return (
    <View style={styles.listHeader}>
      <Text style={styles.title}>Encontre seu próximo treino.</Text>
      <Text style={styles.subtitle}>Personais analisados, perto da sua rotina.</Text>
      <TextInput
        accessibilityLabel="Buscar personal por nome"
        autoCapitalize="words"
        onChangeText={onChangeSearch}
        placeholder="Buscar por nome"
        placeholderTextColor={colors.gray}
        returnKeyType="search"
        style={styles.search}
        value={search}
      />
      <TextInput
        accessibilityLabel="Filtrar por bairro ou região"
        autoCapitalize="words"
        clearButtonMode="while-editing"
        onChangeText={onChangeNeighborhood}
        placeholder="Filtrar por bairro ou região"
        placeholderTextColor={colors.gray}
        returnKeyType="search"
        style={styles.search}
        value={neighborhood}
      />
      <View style={styles.filters}>
        {serviceModeFilters.map((filter) => {
          const selected = filter.value === serviceMode;
          return (
            <Pressable
              accessibilityRole="button"
              accessibilityState={{ selected }}
              key={filter.label}
              onPress={() => onChangeServiceMode(filter.value)}
              style={[styles.filter, selected && styles.filterSelected]}>
              <Text style={[styles.filterLabel, selected && styles.filterLabelSelected]}>
                {filter.label}
              </Text>
            </Pressable>
          );
        })}
      </View>
      {modalities.length > 0 && (
        <View style={styles.filters}>
          <Pressable
            accessibilityLabel="Remover filtro de modalidade"
            accessibilityRole="button"
            onPress={() => onChangeModality(undefined)}
            style={[styles.filter, modalityId === undefined && styles.filterSelected]}>
            <Text style={[styles.filterLabel, modalityId === undefined && styles.filterLabelSelected]}>
              Todas as modalidades
            </Text>
          </Pressable>
          {modalities.map((modality) => {
            const selected = modality.id === modalityId;
            return (
              <Pressable
                accessibilityLabel={`Filtrar por ${modality.name}`}
                accessibilityRole="button"
                accessibilityState={{ selected }}
                key={modality.id}
                onPress={() => onChangeModality(selected ? undefined : modality.id)}
                style={[styles.filter, selected && styles.filterSelected]}>
                <Text style={[styles.filterLabel, selected && styles.filterLabelSelected]}>
                  {modality.name}
                </Text>
              </Pressable>
            );
          })}
        </View>
      )}
      {modalitiesError && (
        <View style={styles.inlineError}>
          <Text style={styles.inlineErrorText}>Não foi possível carregar as modalidades.</Text>
          <Pressable accessibilityRole="button" onPress={onRetryModalities}>
            <Text style={styles.retry}>Tentar novamente</Text>
          </Pressable>
        </View>
      )}
      {(modalityId !== undefined || neighborhood.trim()) && (
        <Pressable
          accessibilityLabel="Limpar filtros de modalidade e região"
          accessibilityRole="button"
          onPress={() => {
            onChangeModality(undefined);
            onChangeNeighborhood('');
          }}
          style={styles.clearFilters}>
          <Text style={styles.clearFiltersLabel}>Limpar modalidade e região</Text>
        </Pressable>
      )}
    </View>
  );
}

type CatalogEmptyStateProps = {
  isLoading: boolean;
  isError: boolean;
  error: unknown;
  onRetry: () => void;
};

function CatalogEmptyState({ error, isLoading, isError, onRetry }: CatalogEmptyStateProps) {
  if (isLoading) {
    return <ActivityIndicator color={colors.lime} style={styles.state} />;
  }

  if (isError) {
    return (
      <View style={styles.state}>
        <Text style={styles.emptyText}>{getCatalogErrorMessage(error)}</Text>
        <Pressable accessibilityRole="button" onPress={onRetry}>
          <Text style={styles.retry}>Tentar novamente</Text>
        </Pressable>
      </View>
    );
  }

  return <Text style={styles.emptyText}>Nenhum personal encontrado por enquanto.</Text>;
}

function getCatalogErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    if (error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
      return 'A conexão demorou demais. Verifique sua internet e tente novamente.';
    }
    if (!error.response) return 'Você está offline ou a API está indisponível. Tente novamente.';
  }
  return 'Não foi possível carregar os personais. Tente novamente.';
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.black },
  header: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 24,
    paddingVertical: 16,
  },
  brand: { color: colors.lime, fontWeight: '800', letterSpacing: 2 },
  login: { color: colors.warmWhite, fontWeight: '700' },
  content: { gap: 14, padding: 24, paddingBottom: 40 },
  listHeader: { marginBottom: 6 },
  title: { color: colors.warmWhite, fontSize: 36, fontWeight: '800', letterSpacing: -1.2 },
  subtitle: { color: colors.gray, fontSize: 16, lineHeight: 23, marginBottom: 20, marginTop: 8 },
  search: {
    backgroundColor: '#171717',
    borderColor: '#2B2B2B',
    borderRadius: 16,
    borderWidth: 1,
    color: colors.warmWhite,
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  clearFilters: { alignSelf: 'flex-start', marginTop: 10, paddingVertical: 4 },
  clearFiltersLabel: { color: colors.lime, fontSize: 12, fontWeight: '800' },
  filters: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: 12 },
  filter: {
    borderColor: colors.line,
    borderRadius: 999,
    borderWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  filterSelected: { backgroundColor: colors.lime, borderColor: colors.lime },
  filterLabel: { color: colors.gray, fontSize: 12, fontWeight: '700' },
  filterLabelSelected: { color: colors.black },
  state: { alignItems: 'center', gap: 12, paddingVertical: 48 },
  footer: { paddingVertical: 16 },
  footerState: { alignItems: 'center', gap: 8, paddingVertical: 16 },
  footerError: { color: colors.gray, fontSize: 12, textAlign: 'center' },
  inlineError: { gap: 6, marginTop: 12 },
  inlineErrorText: { color: colors.gray, fontSize: 12 },
  emptyText: { color: colors.gray, paddingVertical: 36, textAlign: 'center' },
  retry: { color: colors.lime, fontWeight: '800' },
});
