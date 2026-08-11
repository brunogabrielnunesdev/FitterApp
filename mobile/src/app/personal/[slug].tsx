import { useMutation, useQuery } from '@tanstack/react-query';
import { router, useLocalSearchParams } from 'expo-router';
import { Linking, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { colors } from '@/common/theme/colors';
import {
  getPublicProfile,
  startWhatsappContact,
} from '@/features/catalog/services/catalogService';
import { ServiceMode } from '@/features/catalog/types/catalog';

const serviceModeLabels: Record<ServiceMode, string> = {
  IN_PERSON: 'Presencial',
  HOME_VISIT: 'Em casa',
  ONLINE: 'Online',
};

export default function PersonalProfileScreen() {
  const { slug } = useLocalSearchParams<{ slug: string }>();
  const profileQuery = useQuery({
    queryKey: ['public-profile', slug],
    queryFn: () => getPublicProfile(slug),
    enabled: Boolean(slug),
  });
  const contactMutation = useMutation({
    mutationFn: startWhatsappContact,
    onSuccess: ({ whatsappUrl }) => Linking.openURL(whatsappUrl),
  });

  if (!profileQuery.data) {
    return (
      <SafeAreaView style={styles.safeArea}>
        <View style={styles.errorState}>
          <Text style={styles.error}>
            {profileQuery.isLoading ? 'Carregando perfil...' : 'Não foi possível carregar este perfil.'}
          </Text>
          {profileQuery.isError && (
            <PrimaryButton label="Tentar novamente" onPress={() => profileQuery.refetch()} />
          )}
        </View>
      </SafeAreaView>
    );
  }

  const profile = profileQuery.data;
  const price = profile.startingPriceCents
    ? `A partir de R$ ${(profile.startingPriceCents / 100).toFixed(0)}`
    : 'Valor a combinar';

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.content}>
        <Pressable onPress={() => router.back()}>
          <Text style={styles.back}>‹ Voltar</Text>
        </Pressable>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{profile.fullName.slice(0, 1).toUpperCase()}</Text>
        </View>
        <Text style={styles.name}>{profile.fullName}</Text>
        <Text style={styles.biography}>{profile.biography ?? 'Perfil profissional no FitterApp.'}</Text>

        <ProfileSection title="Modalidades" values={profile.modalities.map((item) => item.name)} />
        <ProfileSection
          title="Atendimento"
          values={profile.serviceModes.map((mode) => serviceModeLabels[mode])}
        />
        <ProfileSection
          title="Regiões"
          values={profile.serviceAreas.map((area) =>
            [area.neighborhood, area.city, area.stateCode].filter(Boolean).join(' · '),
          )}
        />
        {profile.certifications && <ProfileSection title="Certificações" values={[profile.certifications]} />}
        {profile.gymsDescription && <ProfileSection title="Onde atende" values={[profile.gymsDescription]} />}

        <View style={styles.priceBox}>
          <Text style={styles.priceLabel}>INVESTIMENTO</Text>
          <Text style={styles.price}>{price}</Text>
        </View>
        {contactMutation.isError && (
          <Text style={styles.contactError}>Não foi possível abrir o WhatsApp. Tente novamente.</Text>
        )}
        <PrimaryButton
          label="Conversar no WhatsApp"
          loading={contactMutation.isPending}
          onPress={() => contactMutation.mutate(profile.slug)}
        />
      </ScrollView>
    </SafeAreaView>
  );
}

function ProfileSection({ title, values }: { title: string; values: string[] }) {
  if (!values.length) return null;
  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>{title.toUpperCase()}</Text>
      <Text style={styles.sectionValue}>{values.join(' • ')}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.black },
  content: { gap: 18, padding: 24, paddingBottom: 40 },
  back: { color: colors.lime, fontWeight: '800' },
  avatar: { alignItems: 'center', backgroundColor: colors.lime, borderRadius: 48, height: 96, justifyContent: 'center', marginTop: 12, width: 96 },
  avatarText: { color: colors.black, fontSize: 38, fontWeight: '900' },
  name: { color: colors.warmWhite, fontSize: 34, fontWeight: '900', letterSpacing: -1 },
  biography: { color: colors.gray, fontSize: 16, lineHeight: 24 },
  section: { borderTopColor: colors.line, borderTopWidth: 1, gap: 6, paddingTop: 16 },
  sectionTitle: { color: colors.gray, fontSize: 11, fontWeight: '800', letterSpacing: 1.4 },
  sectionValue: { color: colors.warmWhite, fontSize: 15, lineHeight: 22 },
  priceBox: { backgroundColor: colors.ink, borderColor: colors.line, borderRadius: 16, borderWidth: 1, gap: 6, padding: 16 },
  priceLabel: { color: colors.gray, fontSize: 11, fontWeight: '800', letterSpacing: 1.4 },
  price: { color: colors.lime, fontSize: 22, fontWeight: '900' },
  contactError: { color: colors.danger, fontSize: 13, textAlign: 'center' },
  error: { color: colors.warmWhite, padding: 24 },
  errorState: { flex: 1, justifyContent: 'center', padding: 24, gap: 12 },
});
