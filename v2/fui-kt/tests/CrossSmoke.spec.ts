import * as path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createRequire } from 'node:module';

import { expect, test } from '@playwright/test';

import { startStaticServer, type StaticServerHandle } from './static_server.js';

const require = createRequire(import.meta.url);
const { PNG } = require('pngjs');

declare global {
  interface Window {
    __fuiKtReady?: boolean;
    __fuiRsReady?: boolean;
    __fuiAsReady?: boolean;
    __bridgeReady?: boolean;
  }
}

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PUBLIC_DIR = path.join(__dirname, '..', '..', '..', 'public');

let server: StaticServerHandle;
let baseUrl: string;

test.beforeAll(async () => {
  server = await startStaticServer(PUBLIC_DIR, 11_400);
  baseUrl = `http://127.0.0.1:${String(server.port)}`;
});

test.afterAll(async () => {
  await server.close();
});

async function captureCanvasPng(page: import('@playwright/test').Page, url: string, readyKey: string): Promise<Buffer> {
  await page.goto(`${baseUrl}${url}`);
  await expect.poll(async () => {
    return await page.evaluate((key) => {
      const w = window as unknown as Record<string, unknown>;
      return w[key] === true || w.__bridgeReady === true ? 'ready' : 'pending';
    }, readyKey);
  }).toBe('ready');

  const box = await page.locator('#fui-canvas').boundingBox();
  expect(box).not.toBeNull();
  return await page.screenshot({ clip: box!, type: 'png' });
}

function pngToRgba(buf: Buffer): { w: number; h: number; data: Buffer } {
  const png = PNG.sync.read(buf);
  return { w: png.width, h: png.height, data: Buffer.from(png.data) };
}

test('all three smokes render pixel-identical canvas output', async ({ page }) => {
  const asPng = await captureCanvasPng(page, '/v2/fui-as/index.html', '__fuiAsReady');
  const rsPng = await captureCanvasPng(page, '/v2/fui-rs/index.html', '__fuiRsReady');
  const ktPng = await captureCanvasPng(page, '/v2/fui-kt/index.html', '__fuiKtReady');

  const as = pngToRgba(asPng);
  const rs = pngToRgba(rsPng);
  const kt = pngToRgba(ktPng);

  expect(rs.w).toBe(as.w);
  expect(rs.h).toBe(as.h);
  expect(kt.w).toBe(as.w);
  expect(kt.h).toBe(as.h);

  expect(rs.data.equals(as.data)).toBe(true);
  expect(kt.data.equals(as.data)).toBe(true);
});
