import { forwardRef } from 'react';
import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';

import { colors } from '@/common/theme/colors';

type FormFieldProps = TextInputProps & {
  label: string;
  error?: string;
  action?: { label: string; onPress: () => void };
};

export const FormField = forwardRef<TextInput, FormFieldProps>(function FormField(
  { label, error, action, ...inputProps },
  ref,
) {
  return (
    <View style={styles.wrapper}>
      <View style={styles.labelRow}>
        <Text style={styles.label}>{label}</Text>
        {action && (
          <Text onPress={action.onPress} style={styles.action}>
            {action.label}
          </Text>
        )}
      </View>
      <TextInput
        ref={ref}
        placeholderTextColor={colors.muted}
        selectionColor={colors.lime}
        style={[styles.input, error && styles.inputError]}
        {...inputProps}
      />
      {error && <Text style={styles.error}>{error}</Text>}
    </View>
  );
});

const styles = StyleSheet.create({
  wrapper: { gap: 8 },
  labelRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  label: {
    color: colors.gray,
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 1.25,
    textTransform: 'uppercase',
  },
  action: { color: colors.lime, fontSize: 12, fontWeight: '700' },
  input: {
    minHeight: 54,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.ink,
    color: colors.warmWhite,
    fontSize: 16,
    paddingHorizontal: 17,
  },
  inputError: { borderColor: colors.danger },
  error: { color: colors.danger, fontSize: 12 },
});
