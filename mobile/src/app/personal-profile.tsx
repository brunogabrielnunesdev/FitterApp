import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { PrimaryButton } from '@/common/components/button/PrimaryButton';
import { colors } from '@/common/theme/colors';
import {
  createPersonalProfile,
  getOwnProfile,
  getProfileDraft,
  listModalities,
  ProfileDraft,
  OwnProfileStatus,
  publishProfile,
  ServiceArea,
  submitProfile,
  startProfileRevision,
  updateCref,
  updateProfileDraft,
  updateProfileModalities,
  updateServiceAreas,
  updateServiceModes,
  unpublishProfile,
} from '@/features/profile/services/profileService';

const steps = ['Dados', 'CREF', 'Modalidades', 'Atendimento', 'Regiões', 'Revisão'];
const serviceModeLabels = {
  IN_PERSON: 'Em academia ou estúdio',
  HOME_VISIT: 'Atendimento em domicílio',
  ONLINE: 'Atendimento online',
} as const;

type FieldErrors = Record<string, string>;

export default function PersonalProfileScreen() {
  const queryClient = useQueryClient();
  const [step, setStep] = useState(0);
  const [errors, setErrors] = useState<FieldErrors>({});
  const [requestError, setRequestError] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState(false);
  const [editingRejected, setEditingRejected] = useState(false);
  const status = useQuery({ queryKey: ['own-profile-status'], queryFn: getOwnProfile, retry: false });
  const draft = useQuery({ queryKey: ['profile-draft'], queryFn: getProfileDraft, retry: false });
  const modalities = useQuery({ queryKey: ['modalities'], queryFn: listModalities });
  const create = useMutation({
    mutationFn: createPersonalProfile,
    onSuccess: () => draft.refetch(),
    onError: (error) => setRequestError(getErrorMessage(error)),
  });

  const profile = draft.data;
  const save = useMutation({
    mutationFn: async () => {
      if (!profile) return;
      if (step === 0) await updateProfileDraft(profile.profileId, profile);
      if (step === 1) {
        await updateCref(
          profile.profileId,
          profile.crefRegistrationCode!.trim(),
          profile.crefDocumentImageKey!.trim(),
        );
      }
      if (step === 2) await updateProfileModalities(profile.profileId, profile.modalityIds);
      if (step === 3) await updateServiceModes(profile.profileId, profile.serviceModes);
      if (step === 4) await updateServiceAreas(profile.profileId, normalizeAreas(profile.serviceAreas));
    },
    onError: (error) => setRequestError(getErrorMessage(error)),
  });
  const submit = useMutation({
    mutationFn: () => submitProfile(profile!.profileId),
    onSuccess: async () => {
      setSubmitted(true);
      await Promise.all([draft.refetch(), status.refetch()]);
    },
    onError: (error) => setRequestError(getErrorMessage(error)),
  });

  const missing = useMemo(() => (profile ? getMissingItems(profile) : []), [profile]);

  if (draft.isLoading || status.isLoading) return <CenteredMessage title="Carregando seu perfil..." />;
  if (!profile) {
    return (
      <SafeAreaView style={styles.safe}>
        <View style={styles.content}>
          <Text style={styles.title}>Divulgue seu trabalho.</Text>
          <Text style={styles.text}>Crie seu rascunho para iniciar a solicitação.</Text>
          {requestError && <ErrorBanner message={requestError} />}
          <PrimaryButton
            label="Criar meu perfil"
            loading={create.isPending}
            onPress={() => {
              setRequestError(null);
              create.mutate();
            }}
          />
        </View>
      </SafeAreaView>
    );
  }

  if (
    status.data &&
    status.data.profileStatus !== 'DRAFT' &&
    status.data.revisionStatus !== 'DRAFT' &&
    !(status.data.revisionStatus === 'REJECTED' && editingRejected)
  ) {
    return (
      <ProfileStatusView
        profile={status.data}
        currentRevisionId={profile.revisionId}
        onEditRejected={() => setEditingRejected(true)}
        onRefresh={async () => {
          await Promise.all([status.refetch(), draft.refetch()]);
        }}
      />
    );
  }

  if (submitted || profile.revisionStatus === 'PENDING_REVIEW') {
    return (
      <CenteredMessage
        title="Perfil enviado!"
        message="Seus dados estão em análise. Avisaremos quando a revisão for concluída."
        actionLabel="Voltar para o início"
        onAction={() => router.replace('/home')}
      />
    );
  }

  const setProfile = (changes: Partial<ProfileDraft>) => {
    queryClient.setQueryData<ProfileDraft>(['profile-draft'], { ...profile, ...changes });
    setRequestError(null);
  };
  const goBack = () => {
    setErrors({});
    setRequestError(null);
    if (step === 0) router.back();
    else setStep((value) => value - 1);
  };
  const next = async () => {
    const nextErrors = validateStep(step, profile);
    setErrors(nextErrors);
    setRequestError(null);
    if (Object.keys(nextErrors).length > 0) return;
    try {
      await save.mutateAsync();
      setStep((value) => Math.min(value + 1, steps.length - 1));
    } catch {
      // A mensagem é tratada pelo mutation.
    }
  };

  return (
    <SafeAreaView style={styles.safe}>
      <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={styles.content}>
        <Pressable onPress={goBack}><Text style={styles.back}>‹ Voltar</Text></Pressable>
        <Text style={styles.kicker}>ÁREA PROFISSIONAL</Text>
        <Text style={styles.title}>{steps[step]}</Text>
        <Text style={styles.progress}>Etapa {step + 1} de {steps.length}</Text>
        <View style={styles.steps}>
          {steps.map((label, index) => <View key={label} style={[styles.dot, index <= step && styles.dotActive]} />)}
        </View>

        {profile.revisionStatus === 'REJECTED' && profile.rejectionReason && (
          <ErrorBanner message={`Ajustes solicitados: ${profile.rejectionReason}`} />
        )}
        {requestError && <ErrorBanner message={requestError} />}

        {step === 0 && (
          <>
            <Field label="Nome profissional *" value={profile.fullName ?? ''} error={errors.fullName} onChangeText={(fullName) => setProfile({ fullName })} />
            <Field label="Biografia *" multiline value={profile.biography ?? ''} error={errors.biography} onChangeText={(biography) => setProfile({ biography })} />
            <Field label="WhatsApp *" keyboardType="phone-pad" value={profile.whatsapp ?? ''} error={errors.whatsapp} onChangeText={(whatsapp) => setProfile({ whatsapp })} />
            <Field label="Ano de início da experiência" keyboardType="number-pad" value={profile.experienceStartedYear?.toString() ?? ''} error={errors.experienceStartedYear} onChangeText={(value) => setProfile({ experienceStartedYear: value ? Number(value) : null })} />
            <Field label="Certificações" multiline value={profile.certifications ?? ''} onChangeText={(certifications) => setProfile({ certifications })} />
            <Field label="Academias ou locais de atuação" multiline value={profile.gymsDescription ?? ''} onChangeText={(gymsDescription) => setProfile({ gymsDescription })} />
          </>
        )}

        {step === 1 && (
          <>
            <Field label="Número do CREF *" autoCapitalize="characters" value={profile.crefRegistrationCode ?? ''} error={errors.crefRegistrationCode} onChangeText={(crefRegistrationCode) => setProfile({ crefRegistrationCode })} />
            <Field label="Referência do documento *" autoCapitalize="none" value={profile.crefDocumentImageKey ?? ''} error={errors.crefDocumentImageKey} onChangeText={(crefDocumentImageKey) => setProfile({ crefDocumentImageKey })} />
            <Text style={styles.hint}>Informe a chave ou URL do documento já armazenado. O seletor de arquivos será conectado quando o serviço de upload estiver disponível.</Text>
          </>
        )}

        {step === 2 && (
          <View style={styles.options}>
            {modalities.isLoading && <Text style={styles.text}>Carregando modalidades...</Text>}
            {modalities.isError && <ErrorBanner message="Não foi possível carregar as modalidades." />}
            {modalities.data?.map((item) => {
              const selected = profile.modalityIds.includes(item.id);
              return <Option key={item.id} label={item.name} selected={selected} onPress={() => setProfile({ modalityIds: selected ? profile.modalityIds.filter((id) => id !== item.id) : [...profile.modalityIds, item.id] })} />;
            })}
            {errors.modalityIds && <Text style={styles.fieldError}>{errors.modalityIds}</Text>}
          </View>
        )}

        {step === 3 && (
          <View style={styles.options}>
            {(Object.keys(serviceModeLabels) as ProfileDraft['serviceModes']).map((mode) => {
              const selected = profile.serviceModes.includes(mode);
              return <Option key={mode} label={serviceModeLabels[mode]} selected={selected} onPress={() => setProfile({ serviceModes: selected ? profile.serviceModes.filter((item) => item !== mode) : [...profile.serviceModes, mode] })} />;
            })}
            {errors.serviceModes && <Text style={styles.fieldError}>{errors.serviceModes}</Text>}
          </View>
        )}

        {step === 4 && (
          <ServiceAreasEditor
            areas={profile.serviceAreas}
            errors={errors}
            onChange={(serviceAreas) => setProfile({ serviceAreas })}
          />
        )}

        {step === 5 && (
          <>
            <ReviewSection title="Dados profissionais" onEdit={() => setStep(0)} lines={[profile.fullName, profile.biography, profile.whatsapp]} />
            <ReviewSection title="CREF" onEdit={() => setStep(1)} lines={[profile.crefRegistrationCode, profile.crefDocumentImageKey ? 'Documento informado' : null]} />
            <ReviewSection title="Modalidades" onEdit={() => setStep(2)} lines={profile.modalityIds.map((id) => modalities.data?.find((item) => item.id === id)?.name ?? `Modalidade ${id}`)} />
            <ReviewSection title="Atendimento" onEdit={() => setStep(3)} lines={profile.serviceModes.map((mode) => serviceModeLabels[mode])} />
            <ReviewSection title="Regiões" onEdit={() => setStep(4)} lines={profile.serviceAreas.map(formatArea)} />
            {missing.length > 0 && <ErrorBanner message={`Complete antes de enviar: ${missing.join(', ')}.`} />}
            <PrimaryButton label="Enviar para análise" loading={submit.isPending} disabled={missing.length > 0} onPress={() => { setRequestError(null); submit.mutate(); }} />
          </>
        )}

        {step < steps.length - 1 && <PrimaryButton label="Salvar e continuar" loading={save.isPending} onPress={next} />}
      </ScrollView>
    </SafeAreaView>
  );
}

const statusContent = {
  PENDING_REVIEW: {
    eyebrow: 'EM ANÁLISE',
    title: 'Seu perfil está com a nossa equipe.',
    message: 'Os dados foram enviados e agora aguardam a revisão administrativa.',
  },
  APPROVED: {
    eyebrow: 'PERFIL APROVADO',
    title: 'Tudo pronto para aparecer no catálogo.',
    message: 'Seu cadastro foi aprovado. Publique quando quiser deixá-lo visível para os alunos.',
  },
  PUBLISHED: {
    eyebrow: 'PERFIL PUBLICADO',
    title: 'Seu perfil está no catálogo.',
    message: 'Alunos já podem encontrar seus dados e iniciar contato pelo WhatsApp.',
  },
  REJECTED: {
    eyebrow: 'AJUSTES NECESSÁRIOS',
    title: 'Seu perfil precisa de correções.',
    message: 'Revise o motivo informado, corrija os dados e envie novamente para análise.',
  },
  SUSPENDED: {
    eyebrow: 'PERFIL SUSPENSO',
    title: 'Seu perfil não está disponível.',
    message: 'A publicação foi suspensa pela administração. Entre em contato com o suporte para entender os próximos passos.',
  },
} as const;

function ProfileStatusView({
  profile,
  currentRevisionId,
  onEditRejected,
  onRefresh,
}: {
  profile: OwnProfileStatus;
  currentRevisionId: string;
  onEditRejected: () => void;
  onRefresh: () => Promise<void>;
}) {
  const [error, setError] = useState<string | null>(null);
  const hasNewApprovedRevision =
    profile.revisionStatus === 'APPROVED' &&
    profile.published &&
    currentRevisionId !== profile.publishedRevisionId;
  const displayStatus =
    profile.profileStatus === 'SUSPENDED'
      ? 'SUSPENDED'
      : profile.revisionStatus === 'PENDING_REVIEW' || profile.revisionStatus === 'REJECTED'
        ? profile.revisionStatus
        : hasNewApprovedRevision
          ? 'APPROVED'
          : profile.profileStatus;
  const content = statusContent[displayStatus as keyof typeof statusContent];
  const publication = useMutation({
    mutationFn: () =>
      displayStatus === 'PUBLISHED'
        ? unpublishProfile(profile.profileId)
        : publishProfile(profile.profileId),
    onSuccess: onRefresh,
    onError: (mutationError) => setError(getErrorMessage(mutationError)),
  });
  const revision = useMutation({
    mutationFn: () => startProfileRevision(profile.profileId),
    onSuccess: onRefresh,
    onError: (mutationError) => setError(getErrorMessage(mutationError)),
  });

  if (!content) return null;
  return (
    <SafeAreaView style={styles.safe}>
      <ScrollView contentContainerStyle={[styles.content, styles.statusContent]}>
        <Pressable onPress={() => router.back()}><Text style={styles.back}>‹ Voltar</Text></Pressable>
        <View style={styles.statusCard}>
          <View style={styles.statusIcon}><Text style={styles.statusIconText}>{displayStatus === 'PUBLISHED' ? '✓' : '●'}</Text></View>
          <Text style={styles.kicker}>{content.eyebrow}</Text>
          <Text style={styles.title}>{content.title}</Text>
          {profile.fullName && <Text style={styles.profileName}>{profile.fullName}</Text>}
          <Text style={styles.text}>{content.message}</Text>
          {displayStatus === 'REJECTED' && profile.rejectionReason && (
            <ErrorBanner message={`Motivo: ${profile.rejectionReason}`} />
          )}
          {error && <ErrorBanner message={error} />}
          {displayStatus === 'APPROVED' && (
            <PrimaryButton label="Publicar meu perfil" loading={publication.isPending} onPress={() => { setError(null); publication.mutate(); }} />
          )}
          {displayStatus === 'PUBLISHED' && (
            <>
              <PrimaryButton label="Ver catálogo" onPress={() => router.push('/catalog')} />
              <PrimaryButton variant="secondary" label="Editar perfil" loading={revision.isPending} onPress={() => { setError(null); revision.mutate(); }} />
              <PrimaryButton variant="secondary" label="Despublicar perfil" loading={publication.isPending} onPress={() => { setError(null); publication.mutate(); }} />
            </>
          )}
          {displayStatus === 'APPROVED' && !profile.published && (
            <PrimaryButton variant="secondary" label="Editar antes de publicar" loading={revision.isPending} onPress={() => { setError(null); revision.mutate(); }} />
          )}
          {displayStatus === 'REJECTED' && (
            <PrimaryButton label="Corrigir meu perfil" onPress={onEditRejected} />
          )}
          <PrimaryButton variant="secondary" label="Atualizar status" onPress={() => { setError(null); void onRefresh(); }} />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

function ServiceAreasEditor({ areas, errors, onChange }: { areas: ServiceArea[]; errors: FieldErrors; onChange: (areas: ServiceArea[]) => void }) {
  const update = (index: number, changes: Partial<ServiceArea>) => onChange(areas.map((area, current) => current === index ? { ...area, ...changes } : area));
  return (
    <View style={styles.options}>
      {areas.map((area, index) => (
        <View key={index} style={styles.areaCard}>
          <View style={styles.sectionHeader}><Text style={styles.sectionTitle}>Região {index + 1}</Text><Pressable onPress={() => onChange(areas.filter((_, current) => current !== index))}><Text style={styles.remove}>Remover</Text></Pressable></View>
          <Field label="Cidade *" value={area.city} error={errors[`area.${index}.city`]} onChangeText={(city) => update(index, { city })} />
          <Field label="UF *" maxLength={2} autoCapitalize="characters" value={area.stateCode} error={errors[`area.${index}.stateCode`]} onChangeText={(stateCode) => update(index, { stateCode: stateCode.toUpperCase() })} />
          <Field label="Bairro" value={area.neighborhood ?? ''} onChangeText={(neighborhood) => update(index, { neighborhood })} />
          <Field label="Observações" value={area.description ?? ''} onChangeText={(description) => update(index, { description })} />
        </View>
      ))}
      {errors.serviceAreas && <Text style={styles.fieldError}>{errors.serviceAreas}</Text>}
      <PrimaryButton variant="secondary" label="Adicionar região" onPress={() => onChange([...areas, { city: '', stateCode: '', neighborhood: null, description: null }])} />
    </View>
  );
}

function ReviewSection({ title, lines, onEdit }: { title: string; lines: (string | null | undefined)[]; onEdit: () => void }) {
  return <View style={styles.reviewCard}><View style={styles.sectionHeader}><Text style={styles.sectionTitle}>{title}</Text><Pressable onPress={onEdit}><Text style={styles.edit}>Editar</Text></Pressable></View>{lines.filter(Boolean).map((line, index) => <Text key={`${line}-${index}`} style={styles.text}>{line}</Text>)}</View>;
}

function Option({ label, selected, onPress }: { label: string; selected: boolean; onPress: () => void }) {
  return <Pressable accessibilityRole="checkbox" accessibilityState={{ checked: selected }} style={[styles.option, selected && styles.selected]} onPress={onPress}><Text style={[styles.optionText, selected && styles.selectedText]}>{selected ? '✓ ' : ''}{label}</Text></Pressable>;
}

function Field({ label, error, multiline, ...props }: { label: string; error?: string; multiline?: boolean } & React.ComponentProps<typeof TextInput>) {
  return <View style={styles.field}><Text style={styles.label}>{label}</Text><TextInput placeholderTextColor={colors.gray} selectionColor={colors.lime} multiline={multiline} style={[styles.input, multiline && styles.multiline, error && styles.inputError]} {...props} />{error && <Text style={styles.fieldError}>{error}</Text>}</View>;
}

function ErrorBanner({ message }: { message: string }) { return <View style={styles.errorBanner}><Text style={styles.errorText}>{message}</Text></View>; }

function CenteredMessage({ title, message, actionLabel, onAction }: { title: string; message?: string; actionLabel?: string; onAction?: () => void }) {
  return <SafeAreaView style={styles.safe}><View style={[styles.content, styles.centered]}><Text style={styles.title}>{title}</Text>{message && <Text style={styles.text}>{message}</Text>}{actionLabel && onAction && <PrimaryButton label={actionLabel} onPress={onAction} />}</View></SafeAreaView>;
}

function validateStep(step: number, profile: ProfileDraft): FieldErrors {
  const errors: FieldErrors = {};
  if (step === 0) {
    if (!profile.fullName?.trim()) errors.fullName = 'Informe seu nome profissional.';
    if (!profile.biography?.trim()) errors.biography = 'Conte um pouco sobre seu trabalho.';
    if (!profile.whatsapp?.trim()) errors.whatsapp = 'Informe seu WhatsApp.';
    if (profile.experienceStartedYear && (profile.experienceStartedYear < 1900 || profile.experienceStartedYear > new Date().getFullYear())) errors.experienceStartedYear = 'Informe um ano válido.';
  }
  if (step === 1) {
    if (!profile.crefRegistrationCode?.trim()) errors.crefRegistrationCode = 'Informe o número do CREF.';
    if (!profile.crefDocumentImageKey?.trim()) errors.crefDocumentImageKey = 'Informe a referência do documento.';
  }
  if (step === 2 && profile.modalityIds.length === 0) errors.modalityIds = 'Selecione ao menos uma modalidade.';
  if (step === 3 && profile.serviceModes.length === 0) errors.serviceModes = 'Selecione ao menos uma forma de atendimento.';
  if (step === 4) {
    if (profile.serviceAreas.length === 0) errors.serviceAreas = 'Adicione ao menos uma região.';
    profile.serviceAreas.forEach((area, index) => {
      if (!area.city.trim()) errors[`area.${index}.city`] = 'Informe a cidade.';
      if (!/^[A-Za-z]{2}$/.test(area.stateCode.trim())) errors[`area.${index}.stateCode`] = 'Use a sigla da UF com 2 letras.';
    });
  }
  return errors;
}

function getMissingItems(profile: ProfileDraft) {
  const missing: string[] = [];
  if (!profile.fullName?.trim() || !profile.biography?.trim() || !profile.whatsapp?.trim()) missing.push('dados profissionais');
  if (!profile.crefRegistrationCode?.trim() || !profile.crefDocumentImageKey?.trim()) missing.push('CREF e documento');
  if (profile.modalityIds.length === 0) missing.push('modalidade');
  if (profile.serviceModes.length === 0) missing.push('forma de atendimento');
  if (profile.serviceAreas.length === 0 || profile.serviceAreas.some((area) => !area.city.trim() || !/^[A-Za-z]{2}$/.test(area.stateCode.trim()))) missing.push('região válida');
  return missing;
}

function normalizeAreas(areas: ServiceArea[]) {
  return areas.map((area) => ({ city: area.city.trim(), stateCode: area.stateCode.trim().toUpperCase(), neighborhood: area.neighborhood?.trim() || null, description: area.description?.trim() || null }));
}

function formatArea(area: ServiceArea) { return [area.city, area.stateCode, area.neighborhood].filter(Boolean).join(' · '); }

function getErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    const data = error.response?.data as { message?: string; detail?: string } | undefined;
    if (data?.message || data?.detail) return data.message ?? data.detail!;
    if (!error.response) return 'Não foi possível conectar ao servidor. Tente novamente.';
  }
  return 'Não foi possível concluir a operação. Tente novamente.';
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.black }, content: { gap: 16, padding: 24, paddingBottom: 48 }, centered: { flex: 1, justifyContent: 'center' }, statusContent: { flexGrow: 1, justifyContent: 'center' }, statusCard: { gap: 16, backgroundColor: colors.ink, borderColor: colors.line, borderWidth: 1, borderRadius: 24, padding: 22 }, statusIcon: { width: 48, height: 48, borderRadius: 24, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.lime }, statusIconText: { color: colors.black, fontSize: 22, fontWeight: '900' }, profileName: { color: colors.warmWhite, fontSize: 18, fontWeight: '800' }, back: { color: colors.lime, fontWeight: '700' }, kicker: { color: colors.lime, fontWeight: '800' }, title: { color: colors.warmWhite, fontSize: 34, fontWeight: '900' }, progress: { color: colors.gray }, steps: { flexDirection: 'row', gap: 6 }, dot: { backgroundColor: colors.line, height: 4, flex: 1, borderRadius: 4 }, dotActive: { backgroundColor: colors.lime }, text: { color: colors.gray, lineHeight: 22 }, hint: { color: colors.muted, fontSize: 12, lineHeight: 18 }, field: { gap: 6 }, label: { color: colors.warmWhite, fontWeight: '700' }, input: { minHeight: 52, backgroundColor: colors.ink, borderColor: colors.line, borderWidth: 1, borderRadius: 12, color: colors.warmWhite, paddingHorizontal: 14, paddingVertical: 12 }, multiline: { minHeight: 100, textAlignVertical: 'top' }, inputError: { borderColor: colors.danger }, fieldError: { color: colors.danger, fontSize: 12 }, options: { gap: 10 }, option: { borderColor: colors.line, borderWidth: 1, borderRadius: 12, padding: 14 }, selected: { backgroundColor: colors.lime, borderColor: colors.lime }, optionText: { color: colors.warmWhite, fontWeight: '700' }, selectedText: { color: colors.black }, areaCard: { gap: 12, backgroundColor: colors.ink, borderColor: colors.line, borderWidth: 1, borderRadius: 16, padding: 16 }, reviewCard: { gap: 8, backgroundColor: colors.ink, borderColor: colors.line, borderWidth: 1, borderRadius: 16, padding: 16 }, sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 12 }, sectionTitle: { color: colors.warmWhite, fontSize: 16, fontWeight: '800' }, edit: { color: colors.lime, fontWeight: '700' }, remove: { color: colors.danger, fontWeight: '700' }, errorBanner: { backgroundColor: '#2A1515', borderColor: colors.danger, borderWidth: 1, borderRadius: 12, padding: 12 }, errorText: { color: colors.danger, lineHeight: 20 },
});
