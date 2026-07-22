import assert from "node:assert/strict";
import test from "node:test";

async function render() {
  const workerUrl = new URL("../dist/server/index.js", import.meta.url);
  workerUrl.searchParams.set("test", `${process.pid}-${Date.now()}`);
  const { default: worker } = await import(workerUrl.href);

  return worker.fetch(
    new Request("http://localhost/", {
      headers: { accept: "text/html" },
    }),
    {
      ASSETS: {
        fetch: async () => new Response("Not found", { status: 404 }),
      },
    },
    {
      waitUntil() {},
      passThroughOnException() {},
    },
  );
}

test("renders the FitterApp landing page", async () => {
  const response = await render();
  assert.equal(response.status, 200);
  assert.match(response.headers.get("content-type") ?? "", /^text\/html\b/i);

  const html = await response.text();
  assert.match(html, /<html lang="pt-BR">/i);
  assert.match(
    html,
    /<title>FitterApp \| Encontre o personal certo para o seu treino<\/title>/i,
  );
  assert.match(html, /Seu treino começa com a/);
  assert.match(html, /Quero encontrar um personal/);
  assert.match(html, /Sou personal trainer/);
  assert.match(html, /CRESCIMENTO REGIONAL/);
  assert.match(html, /Umuarama/);
  assert.match(html, /Maringá/);
  assert.match(html, /São Paulo/);
  assert.match(html, /Dúvidas frequentes/);
  assert.doesNotMatch(html, /codex-preview|Starter Project|SkeletonPreview/);
});

test("includes product metadata and social preview", async () => {
  const response = await render();
  const html = await response.text();

  assert.match(html, /name="description"/i);
  assert.match(html, /property="og:title"/i);
  assert.match(html, /property="og:image" content="https:\/\/fitterapp\.com\.br\/og\.png"/i);
  assert.match(html, /name="twitter:card" content="summary_large_image"/i);
  assert.match(html, /rel="icon" href="https:\/\/fitterapp\.com\.br\/fitterapp-logo\.png"/i);
});
