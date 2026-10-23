import { defineConfig, Plugin } from 'vite';
import react from '@vitejs/plugin-react';
import fs from 'fs';
import path from 'path';

function narrativeBridgePlugin(): Plugin {
  return {
    name: 'pitchiq-narrative-bridge',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = req.url || '';

        // GET /api/overrides/:matchId
        if (req.method === 'GET' && url.startsWith('/api/overrides/')) {
          const matchIdStr = url.replace('/api/overrides/', '').split('?')[0];
          const matchId = Number(matchIdStr);
          const overrideFile = path.resolve(__dirname, 'public', 'overrides', `match_${matchId}.json`);

          res.setHeader('Content-Type', 'application/json');
          res.setHeader('Access-Control-Allow-Origin', '*');

          if (fs.existsSync(overrideFile)) {
            const content = fs.readFileSync(overrideFile, 'utf-8');
            res.statusCode = 200;
            res.end(content);
          } else {
            res.statusCode = 200;
            res.end(JSON.stringify({ matchId, cachedAt: Date.now(), overrides: [] }));
          }
          return;
        }

        // POST /api/publish-override
        if (req.method === 'POST' && url === '/api/publish-override') {
          let body = '';
          req.on('data', (chunk) => { body += chunk; });
          req.on('end', () => {
            try {
              const payload = JSON.parse(body);
              const { overrideDoc } = payload;
              const matchId = overrideDoc.matchId;

              // 1. Save to admin/public/overrides/match_<matchId>.json
              const overridesDir = path.resolve(__dirname, 'public', 'overrides');
              if (!fs.existsSync(overridesDir)) {
                fs.mkdirSync(overridesDir, { recursive: true });
              }

              const overrideFile = path.join(overridesDir, `match_${matchId}.json`);
              let existingOverrides: any[] = [];
              if (fs.existsSync(overrideFile)) {
                try {
                  const existingJson = JSON.parse(fs.readFileSync(overrideFile, 'utf-8'));
                  existingOverrides = existingJson.overrides || [];
                } catch (e) {}
              }

              // Replace or add override
              existingOverrides = existingOverrides.filter((o: any) => o.id !== overrideDoc.id);
              existingOverrides.push(overrideDoc);

              const outPayload = {
                matchId,
                cachedAt: Date.now(),
                overrides: existingOverrides
              };
              fs.writeFileSync(overrideFile, JSON.stringify(outPayload, null, 2), 'utf-8');

              // 2. Also patch the warehouse asset JSON in app/src/main/assets/warehouse/matches/
              const projectAssetFile = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'warehouse', 'matches', `match_${matchId}.json`);
              const adminPublicAssetFile = path.resolve(__dirname, 'public', 'matches', `match_${matchId}.json`);

              [projectAssetFile, adminPublicAssetFile].forEach((filePath) => {
                if (fs.existsSync(filePath)) {
                  try {
                    const matchJson = JSON.parse(fs.readFileSync(filePath, 'utf-8'));
                    applyOverrideToMatchJson(matchJson, overrideDoc);
                    fs.writeFileSync(filePath, JSON.stringify(matchJson, null, 2), 'utf-8');
                  } catch (e) {
                    console.error('Error patching match JSON file:', filePath, e);
                  }
                }
              });

              res.setHeader('Content-Type', 'application/json');
              res.setHeader('Access-Control-Allow-Origin', '*');
              res.statusCode = 200;
              res.end(JSON.stringify({ success: true, count: existingOverrides.length }));
            } catch (err: any) {
              res.statusCode = 500;
              res.end(JSON.stringify({ success: false, error: err.message }));
            }
          });
          return;
        }

        // POST /api/revert-override
        if (req.method === 'POST' && url === '/api/revert-override') {
          let body = '';
          req.on('data', (chunk) => { body += chunk; });
          req.on('end', () => {
            try {
              const payload = JSON.parse(body);
              const { item } = payload;
              const matchId = item.matchId;

              const overrideFile = path.resolve(__dirname, 'public', 'overrides', `match_${matchId}.json`);
              if (fs.existsSync(overrideFile)) {
                try {
                  const existingJson = JSON.parse(fs.readFileSync(overrideFile, 'utf-8'));
                  const remaining = (existingJson.overrides || []).filter((o: any) => o.id !== item.id);
                  existingJson.overrides = remaining;
                  fs.writeFileSync(overrideFile, JSON.stringify(existingJson, null, 2), 'utf-8');
                } catch (e) {}
              }

              // Revert in project warehouse asset file
              const projectAssetFile = path.resolve(__dirname, '..', 'app', 'src', 'main', 'assets', 'warehouse', 'matches', `match_${matchId}.json`);
              const adminPublicAssetFile = path.resolve(__dirname, 'public', 'matches', `match_${matchId}.json`);

              [projectAssetFile, adminPublicAssetFile].forEach((filePath) => {
                if (fs.existsSync(filePath)) {
                  try {
                    const matchJson = JSON.parse(fs.readFileSync(filePath, 'utf-8'));
                    applyOverrideToMatchJson(matchJson, { ...item, publishedText: item.originalText });
                    fs.writeFileSync(filePath, JSON.stringify(matchJson, null, 2), 'utf-8');
                  } catch (e) {}
                }
              });

              res.setHeader('Content-Type', 'application/json');
              res.setHeader('Access-Control-Allow-Origin', '*');
              res.statusCode = 200;
              res.end(JSON.stringify({ success: true, reverted: item.id }));
            } catch (err: any) {
              res.statusCode = 500;
              res.end(JSON.stringify({ success: false, error: err.message }));
            }
          });
          return;
        }

        next();
      });
    }
  };
}

function applyOverrideToMatchJson(matchJson: any, override: any) {
  const { narrativeType, itemKey, publishedText } = override;

  if (narrativeType === 'momentum_story') {
    const sIdx = Number(itemKey.replace(/^s/, ''));
    if (matchJson.momentumTab?.points) {
      matchJson.momentumTab.points.forEach((pt: any) => {
        if (pt.sessionIndex === sIdx || pt.session_index === sIdx) {
          pt.contextStory = publishedText;
          pt.narrative = publishedText;
        }
      });
    }
    if (matchJson.momentumTab?.topSwings) {
      matchJson.momentumTab.topSwings.forEach((pt: any) => {
        if (pt.sessionIndex === sIdx || pt.session_index === sIdx) {
          pt.contextStory = publishedText;
          pt.narrative = publishedText;
        }
      });
    }
  } else if (narrativeType === 'summary_point') {
    const idx = Number(itemKey);
    if (Array.isArray(matchJson.summaryPoints) && matchJson.summaryPoints.length > idx) {
      matchJson.summaryPoints[idx] = publishedText;
    }
    if (Array.isArray(matchJson.summaryTab?.summaryPoints) && matchJson.summaryTab.summaryPoints.length > idx) {
      matchJson.summaryTab.summaryPoints[idx] = publishedText;
    }
  } else if (narrativeType === 'summary_story') {
    if (matchJson.matchStory !== undefined) matchJson.matchStory = publishedText;
    if (matchJson.summaryTab?.matchStory !== undefined) matchJson.summaryTab.matchStory = publishedText;
  } else if (narrativeType === 'timeline_item') {
    if (matchJson.storyTab?.timeline) {
      matchJson.storyTab.timeline.forEach((tl: any, i: number) => {
        const d = tl.dayNumber || tl.day_number || 1;
        const s = tl.sessionOfDay || tl.session_of_day || (i + 1);
        if (`d${d}_s${s}` === itemKey) {
          tl.summary = publishedText;
        }
      });
    }
  } else if (narrativeType === 'collapse') {
    if (matchJson.storyTab?.primaryCollapse) {
      matchJson.storyTab.primaryCollapse.explanation = publishedText;
    }
  } else if (narrativeType === 'recovery') {
    if (matchJson.storyTab?.primaryRecovery) {
      matchJson.storyTab.primaryRecovery.explanation = publishedText;
    }
  } else if (narrativeType === 'pressure_explanation') {
    const sIdx = Number(itemKey.replace(/^s/, ''));
    if (matchJson.pressureTab?.pressurePoints) {
      matchJson.pressureTab.pressurePoints.forEach((pt: any) => {
        if (pt.sessionIndex === sIdx || pt.session_index === sIdx) {
          pt.explanation = publishedText;
        }
      });
    }
    if (matchJson.pressureTab?.highestPressureMoments) {
      matchJson.pressureTab.highestPressureMoments.forEach((pt: any) => {
        if (pt.sessionIndex === sIdx || pt.session_index === sIdx) {
          pt.explanation = publishedText;
        }
      });
    }
  } else if (narrativeType === 'pressure_verdict') {
    const sIdx = Number(itemKey.replace(/^s/, ''));
    if (matchJson.pressureTab?.pressurePoints) {
      matchJson.pressureTab.pressurePoints.forEach((pt: any) => {
        if (pt.sessionIndex === sIdx || pt.session_index === sIdx) {
          pt.verdict = publishedText;
        }
      });
    }
  } else if (narrativeType === 'turning_point') {
    const rank = Number(itemKey);
    if (matchJson.turningPointsTab?.topTurningPoints) {
      matchJson.turningPointsTab.topTurningPoints.forEach((tp: any, i: number) => {
        if (tp.rank === rank || i + 1 === rank) {
          tp.description = publishedText;
        }
      });
    }
  } else if (narrativeType === 'verdict_node') {
    const step = Number(itemKey);
    if (matchJson.turningPointsTab?.matchVerdictChain) {
      matchJson.turningPointsTab.matchVerdictChain.forEach((vc: any, i: number) => {
        if (vc.stepIndex === step || i + 1 === step) {
          vc.description = publishedText;
        }
      });
    }
  }
}

export default defineConfig({
  plugins: [react(), narrativeBridgePlugin()],
  server: {
    port: 5173,
    host: true
  }
});
