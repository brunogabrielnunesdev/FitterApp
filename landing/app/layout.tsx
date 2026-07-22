import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  metadataBase: new URL("https://fitterapp.com.br"),
  title: {
    default: "FitterApp | Encontre o personal certo para o seu treino",
    template: "%s | FitterApp",
  },
  description:
    "Encontre personal trainers na sua região, compare modalidades e fale diretamente pelo WhatsApp.",
  icons: {
    icon: "/fitterapp-logo.png",
    shortcut: "/fitterapp-logo.png",
    apple: "/fitterapp-logo.png",
  },
  openGraph: {
    title: "FitterApp | A conexão certa para o seu treino",
    description:
      "Descubra personal trainers na sua região e fale diretamente pelo WhatsApp.",
    locale: "pt_BR",
    type: "website",
    images: [
      {
        url: "/og.png",
        width: 1792,
        height: 935,
        alt: "FitterApp — A conexão certa para o seu treino",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "FitterApp | A conexão certa para o seu treino",
    description:
      "Descubra personal trainers na sua região e fale diretamente pelo WhatsApp.",
    images: ["/og.png"],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
