import Image from "next/image";

const modalities = [
  "Musculação",
  "Corrida",
  "Funcional",
  "Emagrecimento",
  "Mobilidade",
  "Treino online",
];

const studentBenefits = [
  "Busque por modalidade e região",
  "Compare perfis em um só lugar",
  "Fale diretamente pelo WhatsApp",
];

const personalBenefits = [
  "Perfil profissional organizado",
  "Mais visibilidade na sua região",
  "Contato direto com novos alunos",
];

const faqs = [
  {
    question: "O FitterApp já está disponível?",
    answer:
      "Estamos preparando o primeiro lançamento e formando o catálogo inicial de profissionais. Entre na lista para acompanhar a chegada do FitterApp à sua região.",
  },
  {
    question: "O uso será gratuito?",
    answer:
      "Sim. O lançamento será gratuito para alunos e profissionais enquanto validamos a experiência e entendemos o que realmente gera valor para a comunidade.",
  },
  {
    question: "Em quais regiões o FitterApp vai funcionar?",
    answer:
      "A plataforma nasce preparada para diferentes cidades. A divulgação inicial será concentrada em Umuarama, Maringá e São Paulo, expandindo conforme a comunidade cresce.",
  },
  {
    question: "Como os profissionais aparecem no catálogo?",
    answer:
      "O personal cria seu perfil e envia as informações para análise. Somente perfis aprovados e publicados ficam visíveis no marketplace.",
  },
  {
    question: "O pagamento da aula acontece pelo aplicativo?",
    answer:
      "Não no MVP. O FitterApp facilita a descoberta e o primeiro contato. Valores, contratação e pagamento são combinados diretamente com o profissional.",
  },
];

export default function Home() {
  return (
    <main>
      <header className="site-header">
        <a className="brand" href="#top" aria-label="FitterApp — início">
          <Image src="/fitterapp-logo.png" alt="" width={36} height={36} priority unoptimized />
          <span>FitterApp</span>
        </a>

        <nav className="desktop-nav" aria-label="Navegação principal">
          <a href="#como-funciona">Como funciona</a>
          <a href="#para-alunos">Para alunos</a>
          <a href="#para-personais">Para personais</a>
          <a href="#faq">Dúvidas</a>
        </nav>

        <a className="button button-small button-primary" href="#participar">
          Quero participar <span aria-hidden="true">↗</span>
        </a>
      </header>

      <section className="hero" id="top">
        <div className="hero-grid" aria-hidden="true" />
        <div className="hero-glow hero-glow-lime" aria-hidden="true" />
        <div className="hero-glow hero-glow-violet" aria-hidden="true" />

        <div className="hero-content">
          <Image
            className="hero-logo"
            src="/fitterapp-logo.png"
            alt="Logo FitterApp"
            width={104}
            height={104}
            priority
            unoptimized
          />
          <div className="eyebrow">
            <span className="eyebrow-dot" />
            O marketplace fitness da sua região
          </div>

          <h1>
            Seu treino começa com a <em>conexão certa.</em>
          </h1>

          <p className="hero-description">
            Encontre personal trainers, compare modalidades e formas de
            atendimento e converse diretamente pelo WhatsApp.
          </p>

          <div className="hero-actions">
            <a className="button button-primary" href="#lista-aluno">
              Quero encontrar um personal <span aria-hidden="true">↗</span>
            </a>
            <a className="button button-secondary" href="#lista-personal">
              Sou personal trainer
            </a>
          </div>

          <div className="hero-notes" aria-label="Diferenciais do lançamento">
            <span>✓ Lançamento gratuito</span>
            <span>✓ Perfis analisados</span>
            <span>✓ Contato direto</span>
          </div>
        </div>

        <div className="phone-stage" aria-label="Prévia do aplicativo FitterApp">
          <div className="orbit orbit-one" aria-hidden="true" />
          <div className="orbit orbit-two" aria-hidden="true" />

          <div className="floating-tag floating-tag-top">
            <span className="tag-icon">⌁</span>
            <span>
              <small>Busca regional</small>
              Perto de você
            </span>
          </div>

          <div className="phone-shell">
            <div className="phone-speaker" />
            <div className="phone-screen">
              <div className="app-topbar">
                <Image src="/fitterapp-logo.png" alt="" width={25} height={25} unoptimized />
                <span>FitterApp</span>
                <div className="profile-dot">BR</div>
              </div>

              <div className="app-copy">
                <small>ENCONTRE SEU PERSONAL</small>
                <strong>Treine do seu jeito.</strong>
              </div>

              <div className="search-field">
                <span aria-hidden="true">⌕</span>
                Buscar por nome ou modalidade
              </div>

              <div className="filter-row">
                <span className="filter-active">Musculação</span>
                <span>Funcional</span>
                <span>Mais +</span>
              </div>

              <article className="trainer-card trainer-card-featured">
                <div className="trainer-photo photo-one">
                  <span>F</span>
                  <small>PERFIL</small>
                </div>
                <div className="trainer-info">
                  <small>PERFIL DEMONSTRATIVO</small>
                  <strong>Especialista em força</strong>
                  <span>Musculação · Presencial</span>
                  <button type="button">Ver perfil</button>
                </div>
              </article>

              <article className="trainer-card">
                <div className="trainer-photo photo-two">
                  <span>F</span>
                  <small>PERFIL</small>
                </div>
                <div className="trainer-info">
                  <small>PERFIL DEMONSTRATIVO</small>
                  <strong>Treino funcional</strong>
                  <span>Funcional · Online</span>
                  <button type="button">Ver perfil</button>
                </div>
              </article>
            </div>
          </div>

          <div className="floating-tag floating-tag-bottom">
            <span className="status-pulse" />
            <span>
              <small>Contato simples</small>
              Direto no WhatsApp
            </span>
          </div>
        </div>
      </section>

      <section className="signal-strip" aria-label="Pilares do FitterApp">
        <div>
          <span>01</span>
          <strong>BUSQUE</strong>
          <small>por modalidade e região</small>
        </div>
        <div>
          <span>02</span>
          <strong>COMPARE</strong>
          <small>perfis e atendimentos</small>
        </div>
        <div>
          <span>03</span>
          <strong>CONECTE-SE</strong>
          <small>direto pelo WhatsApp</small>
        </div>
      </section>

      <section className="light-section modalities-section" id="como-funciona">
        <div className="section-heading">
          <div>
            <span className="section-kicker">DO SEU JEITO</span>
            <h2>Um lugar para encontrar o treino que combina com você.</h2>
          </div>
          <p>
            Diferentes objetivos pedem diferentes profissionais. Pesquise,
            compare e escolha com mais clareza.
          </p>
        </div>

        <div className="modality-list">
          {modalities.map((modality, index) => (
            <div
              className={`modality-row ${index === 1 ? "modality-highlight" : ""}`}
              key={modality}
            >
              <span>{String(index + 1).padStart(2, "0")}</span>
              <strong>{modality}</strong>
              <span className="modality-arrow" aria-hidden="true">
                ↗
              </span>
            </div>
          ))}
        </div>
      </section>

      <section className="audiences-section">
        <div className="section-heading section-heading-dark">
          <div>
            <span className="section-kicker">DOIS LADOS. UMA CONEXÃO.</span>
            <h2>Feito para quem treina e para quem transforma.</h2>
          </div>
          <p>
            Menos tempo procurando em lugares diferentes. Mais espaço para
            criar relações profissionais reais.
          </p>
        </div>

        <div className="audience-grid">
          <article className="audience-card student-card" id="para-alunos">
            <span className="card-number">01 / ALUNO</span>
            <h3>Encontre quem entende do seu objetivo.</h3>
            <p>
              Descubra profissionais e tenha as informações principais antes
              da primeira conversa.
            </p>
            <ul>
              {studentBenefits.map((benefit) => (
                <li key={benefit}>
                  <span>✓</span> {benefit}
                </li>
              ))}
            </ul>
            <a href="#lista-aluno">Entrar na lista <span>↗</span></a>
          </article>

          <article className="audience-card personal-card" id="para-personais">
            <span className="card-number">02 / PERSONAL</span>
            <h3>Seu trabalho merece ser encontrado.</h3>
            <p>
              Apresente sua experiência com clareza e seja descoberto por
              pessoas da sua região.
            </p>
            <ul>
              {personalBenefits.map((benefit) => (
                <li key={benefit}>
                  <span>✓</span> {benefit}
                </li>
              ))}
            </ul>
            <a href="#lista-personal">Quero fazer parte <span>↗</span></a>
          </article>
        </div>
      </section>

      <section className="regional-section">
        <div className="regional-art" aria-hidden="true">
          <span className="region-ring region-ring-one" />
          <span className="region-ring region-ring-two" />
          <span className="region-ring region-ring-three" />
          <span className="region-point point-one" />
          <span className="region-point point-two" />
          <span className="region-point point-three" />
          <Image src="/fitterapp-logo.png" alt="" width={130} height={130} unoptimized />
        </div>

        <div className="regional-copy">
          <span className="section-kicker">CRESCIMENTO REGIONAL</span>
          <h2>Começamos perto para construir conexões de verdade.</h2>
          <p>
            O FitterApp nasce preparado para diferentes cidades, mas cresce
            região por região. Assim, cada novo catálogo reúne profissionais e
            alunos que realmente podem se encontrar.
          </p>
          <div className="region-pills">
            <span>Umuarama</span>
            <span>Maringá</span>
            <span>São Paulo</span>
            <span>+ sua região</span>
          </div>
        </div>
      </section>

      <section className="interest-section" id="participar">
        <div className="interest-heading">
          <span className="section-kicker">PRIMEIROS PASSOS</span>
          <h2>Escolha como você quer fazer parte.</h2>
          <p>
            Deixe seu interesse e ajude o FitterApp a chegar com profissionais
            e oportunidades relevantes para a sua região.
          </p>
        </div>

        <div className="form-grid">
          <form className="interest-form" id="lista-aluno">
            <div className="form-title">
              <span>PARA ALUNOS</span>
              <h3>Quero encontrar um personal</h3>
            </div>

            <label>
              Nome
              <input type="text" name="studentName" placeholder="Como podemos te chamar?" />
            </label>
            <div className="field-pair">
              <label>
                Cidade
                <input type="text" name="studentCity" placeholder="Sua cidade" />
              </label>
              <label>
                Estado
                <input type="text" name="studentState" placeholder="UF" maxLength={2} />
              </label>
            </div>
            <label>
              WhatsApp ou e-mail
              <input type="text" name="studentContact" placeholder="Seu melhor contato" />
            </label>
            <label>
              Modalidade de interesse
              <select name="studentModality" defaultValue="">
                <option value="" disabled>Selecione uma modalidade</option>
                {modalities.map((modality) => (
                  <option value={modality} key={modality}>{modality}</option>
                ))}
              </select>
            </label>
            <label className="consent-field">
              <input type="checkbox" name="studentConsent" />
              <span>Aceito receber novidades sobre o lançamento do FitterApp.</span>
            </label>
            <button className="button button-primary form-button" type="submit">
              Avisem quando chegar <span>↗</span>
            </button>
          </form>

          <form className="interest-form personal-form" id="lista-personal">
            <div className="form-title">
              <span>PARA PERSONAIS</span>
              <h3>Quero divulgar meu trabalho</h3>
            </div>

            <label>
              Nome profissional
              <input type="text" name="personalName" placeholder="Seu nome" />
            </label>
            <div className="field-pair">
              <label>
                Cidade
                <input type="text" name="personalCity" placeholder="Onde atende?" />
              </label>
              <label>
                Estado
                <input type="text" name="personalState" placeholder="UF" maxLength={2} />
              </label>
            </div>
            <label>
              WhatsApp
              <input type="tel" name="personalContact" placeholder="(00) 00000-0000" />
            </label>
            <label>
              Principal modalidade
              <select name="personalModality" defaultValue="">
                <option value="" disabled>Selecione uma modalidade</option>
                {modalities.map((modality) => (
                  <option value={modality} key={modality}>{modality}</option>
                ))}
              </select>
            </label>
            <label className="consent-field">
              <input type="checkbox" name="personalConsent" />
              <span>Aceito receber contato sobre o catálogo inicial do FitterApp.</span>
            </label>
            <button className="button button-violet form-button" type="submit">
              Quero fazer parte <span>↗</span>
            </button>
          </form>
        </div>

        <p className="form-disclaimer">
          Esta é uma prévia da experiência. Os formulários serão conectados à
          API antes da publicação.
        </p>
      </section>

      <section className="faq-section" id="faq">
        <div className="faq-heading">
          <span className="section-kicker">SEM ENROLAÇÃO</span>
          <h2>Dúvidas frequentes.</h2>
          <p>Ainda ficou alguma dúvida? Fale com a gente pelo canal de suporte.</p>
        </div>

        <div className="faq-list">
          {faqs.map((faq, index) => (
            <details key={faq.question} open={index === 0}>
              <summary>
                <span>{String(index + 1).padStart(2, "0")}</span>
                {faq.question}
                <i aria-hidden="true">+</i>
              </summary>
              <p>{faq.answer}</p>
            </details>
          ))}
        </div>
      </section>

      <section className="final-cta">
        <div className="final-cta-glow" aria-hidden="true" />
        <Image src="/fitterapp-logo.png" alt="" width={82} height={82} unoptimized />
        <span className="section-kicker">O MOVIMENTO COMEÇA AGORA</span>
        <h2>Seu próximo treino pode começar aqui.</h2>
        <p>
          Entre para a comunidade inicial e acompanhe a chegada do FitterApp à
          sua região.
        </p>
        <div className="hero-actions">
          <a className="button button-primary" href="#lista-aluno">Sou aluno</a>
          <a className="button button-secondary" href="#lista-personal">Sou personal</a>
        </div>
      </section>

      <footer>
        <div className="footer-brand">
          <a className="brand" href="#top">
            <Image src="/fitterapp-logo.png" alt="" width={36} height={36} unoptimized />
            <span>FitterApp</span>
          </a>
          <p>Performance, proximidade e tecnologia para conectar pessoas ao treino certo.</p>
        </div>

        <div className="footer-links">
          <div>
            <strong>PRODUTO</strong>
            <a href="#como-funciona">Como funciona</a>
            <a href="#para-alunos">Para alunos</a>
            <a href="#para-personais">Para personais</a>
          </div>
          <div>
            <strong>INFORMAÇÕES</strong>
            <a href="#faq">Dúvidas</a>
            <a href="#">Privacidade</a>
            <a href="#">Termos de uso</a>
          </div>
        </div>

        <div className="footer-bottom">
          <span>© 2026 FitterApp.</span>
          <span>Feito para conectar movimento e oportunidade.</span>
        </div>
      </footer>
    </main>
  );
}
