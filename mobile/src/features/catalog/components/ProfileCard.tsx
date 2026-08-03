import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors } from '@/common/theme/colors';
import { PublicProfileCard } from '@/features/catalog/types/catalog';

type ProfileCardProps = {
  profile: PublicProfileCard;
  onPress: () => void;
};

export function ProfileCard({ profile, onPress }: ProfileCardProps) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`Ver perfil de ${profile.fullName}`}
      onPress={onPress}
      style={({ pressed }) => [styles.card, pressed && styles.cardPressed]}>
      <View style={styles.avatar}>
        <Text style={styles.avatarText}>{profile.fullName.slice(0, 1).toUpperCase()}</Text>
      </View>
      <View style={styles.content}>
        <Text style={styles.name}>{profile.fullName}</Text>
        <Text numberOfLines={2} style={styles.biography}>
          {profile.biography ?? 'Perfil profissional no FitterApp.'}
        </Text>
        <Text style={styles.price}>{formatPrice(profile)}</Text>
      </View>
    </Pressable>
  );
}

function formatPrice(profile: PublicProfileCard) {
  if (!profile.startingPriceCents) return 'Valor a combinar';

  const unit = profile.priceUnit === 'PER_MONTH' ? '/ mês' : '/ sessão';
  return `A partir de R$ ${(profile.startingPriceCents / 100).toFixed(0)} ${unit}`;
}

const styles = StyleSheet.create({
  card: {
    alignItems: 'center',
    backgroundColor: colors.ink,
    borderColor: colors.line,
    borderRadius: 20,
    borderWidth: 1,
    flexDirection: 'row',
    gap: 14,
    padding: 16,
  },
  cardPressed: { opacity: 0.72 },
  avatar: {
    alignItems: 'center',
    backgroundColor: colors.lime,
    borderRadius: 28,
    height: 56,
    justifyContent: 'center',
    width: 56,
  },
  avatarText: { color: colors.black, fontSize: 22, fontWeight: '900' },
  content: { flex: 1, gap: 4 },
  name: { color: colors.warmWhite, fontSize: 17, fontWeight: '800' },
  biography: { color: colors.gray, lineHeight: 19 },
  price: { color: colors.lime, fontSize: 13, fontWeight: '800', marginTop: 4 },
});
