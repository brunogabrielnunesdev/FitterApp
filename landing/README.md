# FitterApp Landing Page

Landing page pública do FitterApp, criada para apresentar o produto e captar o interesse inicial de alunos e personal trainers em diferentes regiões.

## Tecnologias

- React 19.
- TypeScript.
- vinext + Vite.
- Tailwind CSS.
- Cloudflare Workers para o build de produção.

## Desenvolvimento

```bash
npm install
npm run dev
```

A aplicação local fica disponível em `http://localhost:3000`.

## Verificações

```bash
npm run build
npm test
npm run lint
```

## Estrutura principal

```text
landing/
├── app/
│   ├── globals.css
│   ├── layout.tsx
│   └── page.tsx
├── public/
│   ├── fitterapp-logo.png
│   └── og.png
├── tests/
└── worker/
```

Os formulários são uma prévia visual. Eles serão conectados à API antes da publicação da landing.
