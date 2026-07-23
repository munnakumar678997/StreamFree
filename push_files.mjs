import { readFileSync, readdirSync, statSync } from 'fs';
import { join } from 'path';

const TOKEN = process.env.GITHUB_PERSONAL_ACCESS_TOKEN;
const REPO = "munnakumar678997/StreamFree";
const ACCEPT = "application/vnd.github.v3+json";

export async function pushFile(path, content, message) {
  let sha = null;
  const getRes = await fetch(`https://api.github.com/repos/${REPO}/contents/${path}`, {
    headers: { Authorization: `token ${TOKEN}`, Accept: ACCEPT }
  });
  if (getRes.status === 200) { const e = await getRes.json(); sha = e.sha; }
  const body = { message: message || `Add ${path}`, content: Buffer.from(content).toString("base64") };
  if (sha) body.sha = sha;
  const res = await fetch(`https://api.github.com/repos/${REPO}/contents/${path}`, {
    method: "PUT",
    headers: { Authorization: `token ${TOKEN}`, Accept: ACCEPT, "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  if (res.status === 200 || res.status === 201) { console.log(`OK  ${path}`); return true; }
  else { const r = await res.json(); console.log(`ERR ${path}: ${r.message}`); return false; }
}
