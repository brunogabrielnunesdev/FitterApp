import { useInfiniteQuery } from '@tanstack/react-query';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
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
import { ProfileCard } from '@/features/catalog/components/ProfileCard';
import { useDebouncedValue } from '@/features/catalog/hooks/useDebouncedValue';
import { listPublicProfiles } from '@/features/catalog/services/catalogService';
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
  const [search, setSearch] = useState('');
  const [serviceMode, setServiceMode] = useState<ServiceMode>();
  const debouncedSearch = useDebouncedValue(search.trim());
  const profilesQuery = useInfiniteQuery({
    queryKey: ['public-profiles', debouncedSearch, serviceMode],
    initialPageParam: 0,
    queryFn: ({ pageParam }) =>
      listPublicProfiles({
        page: pageParam,
        size: PAGE_SIZE,
        query: debouncedSearch,
        serviceMode,
      }),
    getNextPageParam: (lastPage) =>
      lastPage.page + 1 < lastPage.totalPages ? lastPage.page + 1 : undefined,
  });
  const profiles = useMemo(
    () => profilesQuery.data?.pages.flatMap((page) => page.content) ?? [],
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

  return (
    <SafeAreaView edges={['top']} style={styles.safeArea}>
      <View style={styles.header}>
        <Text style={styles.brand}>FITTERAPP</Text>
        <Pressable onPress={() => router.push('/login')}>
          <Text style={styles.login}>Entrar</Text>
        </Pressable>
      </View>

      <FlatList
        contentContainerStyle={styles.content}
        data={profiles}
        keyExtractor={(profile) => profile.profileId}
        ListEmptyComponent={<CatalogEmptyState isError={profilesQuery.isError} isLoading={profilesQuery.isLoading} onRetry={profilesQuery.refetch} />}
        ListFooterComponent={
          profilesQuery.isFetchingNextPage ? <ActivityIndicator color={colors.lime} /> : null
        }
        ListHeaderComponent={
          <CatalogHeader
            search={search}
            serviceMode={serviceMode}
            onChangeSearch={setSearch}
            onChangeServiceMode={setServiceMode}
          />
        }
        onEndReached={() => {
          if (profilesQuery.hasNextPage && !profilesQuery.isFetchingNextPage) {
            profilesQuery.fetchNextPage();
          }
        }}
        onEndReachedThreshold={0.4}
        refreshControl={
          <RefreshControl
            colors={[colors.lime]}
            refreshing={profilesQuery.isRefetching}
            tintColor={colors.lime}
            onRefresh={profilesQuery.refetch}
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
  serviceMode?: ServiceMode;
  onChangeSearch: (value: string) => void;
  onChangeServiceMode: (value?: ServiceMode) => void;
};

function CatalogHeader({
  search,
  serviceMode,
  onChangeSearch,
  onChangeServiceMode,
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
      <View style={styles.filters}>
        {serviceModeFilters.map((filter) => {
          const selected = filter.value === serviceMode;
          return (
            <Pressable
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
    </View>
  );
}

type CatalogEmptyStateProps = {
  isLoading: boolean;
  isError: boolean;
  onRetry: () => void;
};

function CatalogEmptyState({ isLoading, isError, onRetry }: CatalogEmptyStateProps) {
  if (isLoading) {
    return <ActivityIndicator color={colors.lime} style={styles.state} />;
  }

  if (isError) {
    return (
      <View style={styles.state}>
        <Text style={styles.emptyText}>Não foi possível carregar os personais.</Text>
        <Pressable onPress={onRetry}>
          <Text style={styles.retry}>Tentar novamente</Text>
        </Pressable>
      </View>
    );
  }

  return <Text style={styles.emptyText}>Nenhum personal encontrado por enquanto.</Text>;
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
  emptyText: { color: colors.gray, paddingVertical: 36, textAlign: 'center' },
  retry: { color: colors.lime, fontWeight: '800' },
});
