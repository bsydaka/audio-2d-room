//imports:
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

class Audio2DRoom {
    private static final String PAGE = String.join("",
"""
<!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>RoomSketch — 2D Room Planner</title>
              <style>
                :root {
                  color-scheme: light;
                  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                  color: #202b3c;
                  background: #f3f5f8;
                  font-synthesis: none;
                }
                * { box-sizing: border-box; }
                body { margin: 0; min-width: 320px; min-height: 100vh; }
                button, input { font: inherit; }
                button { cursor: pointer; }
                .topbar {
                  min-height: 68px; display: flex; align-items: center; justify-content: space-between;
                  gap: 16px; padding: 12px 24px; background: #fff; border-bottom: 1px solid #e4e8ef;
                }
                .brand { display: flex; align-items: center; gap: 11px; font-size: 17px; font-weight: 750; letter-spacing: -.3px; }
                .brand-mark {
                  width: 34px; height: 34px; display: grid; place-items: center; border-radius: 10px;
                  color: #fff; background: #5268e8; font-size: 19px;
                }
                .top-actions { display: flex; gap: 9px; }
                .button {
                  min-height: 38px; padding: 8px 13px; border: 1px solid #dce1ea; border-radius: 8px;
                  background: #fff; color: #354156; font-size: 13px; font-weight: 650;
                }
                .button:hover { background: #f7f8fb; }
                .button.primary { border-color: #5268e8; background: #5268e8; color: #fff; }
                .button.primary:hover { background: #4359d8; }
                .layout {
                  width: min(1500px, 100%); min-height: calc(100vh - 68px); margin: 0 auto; padding: 22px;
                  display: grid; grid-template-columns: 220px minmax(0, 1fr) 220px; gap: 18px;
                }
                .panel, .workspace { background: #fff; border: 1px solid #e4e8ef; border-radius: 12px; }
                .panel { padding: 17px; align-self: start; }
                h1 { margin: 0 0 5px; font-size: 19px; letter-spacing: -.35px; }
                h2 { margin: 0 0 13px; color: #344158; font-size: 13px; font-weight: 750; }
                .subheading { margin: 0 0 17px; color: #788397; font-size: 12px; line-height: 1.5; }
                .palette { display: grid; gap: 8px; }
                .item {
                  min-height: 51px; display: flex; align-items: center; gap: 11px; padding: 8px 10px;
                  border: 1px solid #e8ebf1; border-radius: 9px; background: #fff; color: #354156;
                  text-align: left; transition: border-color .15s, background .15s;
                }
                .item:hover { border-color: #aab4f5; background: #f8f8ff; }
                .item:active { cursor: grabbing; }
                .item-icon { width: 34px; height: 29px; display: grid; place-items: center; border-radius: 6px; font-size: 17px; }
                .item-name { font-size: 13px; font-weight: 650; }
                .item-size { display: block; margin-top: 2px; color: #8791a2; font-size: 11px; font-weight: 450; }
                .workspace { min-width: 0; min-height: 560px; display: flex; flex-direction: column; overflow: hidden; }
                .workspace-head {
                  min-height: 58px; display: flex; align-items: center; justify-content: space-between;
                  gap: 12px; padding: 12px 17px; border-bottom: 1px solid #edf0f4;
                }
                .workspace-title { font-size: 13px; font-weight: 750; }
                .room-size { display: flex; align-items: center; gap: 7px; color: #788397; font-size: 12px; }
                .room-size input {
                  width: 57px; height: 32px; padding: 4px 7px; border: 1px solid #dce1ea;
                  border-radius: 7px; color: #344158; text-align: center;
                }
                .unit { margin-left: -4px; }
                .canvas-wrap {
                  flex: 1; min-height: 460px; display: grid; place-items: center; overflow: auto;
                  padding: 26px; background-color: #fafbfc;
                  background-image: radial-gradient(#dfe3eb 1px, transparent 1px); background-size: 18px 18px;
                }
                svg#room {
                  display: block; width: min(100%, 760px); max-height: 70vh; height: auto;
                  overflow: visible; touch-action: none; box-shadow: 0 5px 22px #1f2c4517;
                }
                .room-object { cursor: grab; }
                .room-object:active { cursor: grabbing; }
                .room-object.selected > rect { stroke: #5268e8; stroke-width: 3; }
                .room-object text { fill: #28334a; font-size: 11px; font-weight: 700; text-anchor: middle; dominant-baseline: central; pointer-events: none; }
                .hint { padding: 11px 16px; border-top: 1px solid #edf0f4; color: #8791a2; font-size: 11px; text-align: center; }
                .details-empty { color: #8791a2; font-size: 12px; line-height: 1.55; }
                .selection-name { margin-bottom: 5px; font-size: 15px; font-weight: 750; }
                .selection-meta { margin-bottom: 15px; color: #788397; font-size: 12px; }
                .detail-actions { display: grid; gap: 8px; }
                .detail-actions .button { width: 100%; }
                .editor-fields { display: grid; grid-template-columns: 1fr 1fr; gap: 9px; margin: 12px 0; }
                .editor-fields label { display: grid; gap: 4px; color: #788397; font-size: 11px; }
                .editor-fields label.wide { grid-column: 1 / -1; }
                .editor-fields input { min-width: 0; width: 100%; height: 33px; padding: 5px 7px; border: 1px solid #dce1ea; border-radius: 7px; color: #344158; }
                .editor-fields input[type="color"] { padding: 3px; }
                .palette + .divider { margin: 16px 0; }
                .measurements { display: grid; gap: 1px; }
                .measurement { display: flex; justify-content: space-between; gap: 8px; padding: 6px 0; color: #788397; font-size: 11px; }
                .measurement strong { color: #354156; text-align: right; font-weight: 650; }
                .divider { height: 1px; margin: 17px 0; background: #edf0f4; }
                .stat { display: flex; justify-content: space-between; gap: 10px; padding: 6px 0; color: #788397; font-size: 12px; }
                .stat strong { color: #354156; font-weight: 650; }
                .status { min-height: 17px; margin-top: 13px; color: #68758a; font-size: 11px; line-height: 1.45; }
                .status.error { color: #b42318; }
                .audio-legend { display: grid; gap: 7px; margin: 0 0 18px; color: #68758a; font-size: 11px; }
                .audio-legend span { display: flex; align-items: center; gap: 8px; }
                .legend-dot { width: 10px; height: 10px; border-radius: 50%; }
                .source-dot { background: #e66848; }
                .listener-dot { background: #4777df; }
                .acoustic-note { margin: 0 0 13px; color: #788397; font-size: 11px; line-height: 1.5; }
                .meter-row { display: flex; align-items: center; gap: 10px; margin: 10px 0; }
                .meter {
                  position: relative; width: 25px; height: 112px; overflow: hidden; flex: 0 0 25px;
                  border: 1px solid #dfe4ed; border-radius: 7px; background: #f1f3f7;
                }
                .meter-fill {
                  position: absolute; right: 0; bottom: 0; left: 0; height: 0;
                  background: linear-gradient(0deg, #5369e8, #65c6aa); transition: height .2s ease;
                }
                .meter-labels { display: flex; height: 112px; flex-direction: column; justify-content: space-between; color: #8791a2; font-size: 10px; }
                .meter-reading { min-width: 0; }
                .meter-value { color: #27344a; font-size: 23px; font-weight: 750; letter-spacing: -.5px; }
                .meter-caption { margin-top: 2px; color: #788397; font-size: 11px; line-height: 1.4; }
                .timing { margin: 8px 0 13px; color: #56637a; font-size: 11px; line-height: 1.5; }
                .room-object text { user-select: none; }
                .audio-marker { cursor: grab; touch-action: none; }
                .audio-marker:active { cursor: grabbing; }
                .audio-marker circle { stroke: #fff; stroke-width: 3; }
                .audio-marker.selected circle { stroke: #27344a; stroke-width: 4; }
                .audio-marker text { fill: #fff; font-size: 15px; font-weight: 750; text-anchor: middle; dominant-baseline: central; pointer-events: none; }
                .audio-marker .marker-label { fill: #344158; font-size: 11px; text-anchor: start; paint-order: stroke; stroke: #fff; stroke-width: 3px; stroke-linejoin: round; }
                @media (max-width: 980px) {
                  .layout { grid-template-columns: 190px minmax(0, 1fr); }
                  .inspector { grid-column: 1 / -1; display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
                  .inspector h2 { grid-column: 1 / -1; margin-bottom: -9px; }
                }
                @media (max-width: 650px) {
                  .topbar { padding: 11px 14px; }
                  .top-actions .button { padding: 7px 9px; }
                  .layout { grid-template-columns: 1fr; padding: 12px; gap: 12px; }
                  .workspace { grid-row: 1; min-height: 490px; }
                  .palette { grid-template-columns: repeat(2, minmax(0, 1fr)); }
                  .panel { padding: 14px; }
                  .inspector { grid-column: auto; }
                  .canvas-wrap { padding: 16px; min-height: 390px; }
                  .workspace-head { align-items: flex-start; }
                  .room-size { gap: 4px; }
                }
              .acoustic-tools { max-width: 1500px; margin: 0 auto; padding: 0 22px 24px; display: grid; gap: 14px; }
.acoustic-card { background: white; border: 1px solid #e4e8ef; border-radius: 12px; padding: 17px; min-width: 0; }
.acoustic-card p { color: #526176; font-size: 13px; line-height: 1.55; }
.acoustic-row { display: flex; align-items: center; gap: 8px 12px; flex-wrap: wrap; margin: 6px 0; }
.acoustic-row label { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; }
.acoustic-row select, .acoustic-row input[type="number"] { padding: 5px; border: 1px solid #dce1ea; border-radius: 6px; background: white; color: #202b3c; }
.acoustic-row input[type="number"] { width: 72px; }
.acoustic-card output { font-size: 13px; color: #354156; }
#arrival-graph { display: block; width: 100%; height: auto; background: #f6f8fc; border-radius: 8px; margin-bottom: 8px; }
.acoustic-card details { margin-top: 8px; }
.table-scroll { overflow-x: auto; }
.acoustic-card table { border-collapse: collapse; width: 100%; font-size: 13px; }
.acoustic-card th, .acoustic-card td { text-align: left; padding: 7px; border-bottom: 1px solid #e4e8ef; }
.acoustic-card summary { cursor: pointer; }
.topbar, .top-actions { flex-wrap: wrap; }
[hidden] { display: none !important; }
button:disabled { opacity: .55; cursor: default; }
.workspace .acoustic-card { margin: 12px 12px 0; }
.workspace .acoustic-card:last-child { margin-bottom: 12px; }
.workspace .canvas-wrap { flex: 0 1 auto; min-height: 320px; }
.workspace .workspace-head { flex-wrap: wrap; }
#arrival-graph { min-width: 460px; }
.graph-scroll { overflow-x: auto; }
@media (prefers-reduced-motion: reduce) { .item { transition: none; } }
.wall-inspector { border-top: 1px solid #e4e8ef; margin-top: 14px; padding-top: 14px; }
.wall-inspector p { color: #526176; font-size: 12px; line-height: 1.5; }
.wall-object [data-wall-end] { cursor: crosshair; }
#room-walls > line { pointer-events: none; }
.skip-link { position: absolute; top: 8px; left: 8px; z-index: 5; padding: 10px; background: #fff9de; transform: translateY(-160%); }
.skip-link:focus { transform: none; }
:root { --ink:#143743; --paper:#fff5d9; --cream:#f9ebc6; --teal:#08788a; --cyan:#79ced0; --orange:#ffb446; --red:#a73925; --violet:#65428d; font-family: system-ui, sans-serif; color:var(--ink); background:#f6eac7; }
body { background-image: radial-gradient(#855b3226 0.65px, transparent 0.65px); background-size:5px 5px; }
.topbar { background:var(--teal); border-bottom:2px solid var(--ink); color:white; }
.brand { font-family: Georgia, serif; font-size:clamp(25px, 4vw, 38px); letter-spacing:-.055em; }
.brand-mark { color:var(--ink); background:var(--orange); border:2px solid var(--ink); border-radius:50%; }
.top-actions { gap:8px; }
.layout { gap:18px; grid-template-columns: 215px minmax(0,1fr) 258px; }
.panel,.workspace,.acoustic-card { background:var(--paper); border:2px solid var(--ink); border-radius:16px; box-shadow:3px 4px 0 #16384440; }
.workspace-head { background:var(--cyan); border-bottom:2px solid var(--ink); }
.workspace-title,h1,h2 { font-family:Georgia,serif; color:var(--ink); letter-spacing:-.025em; }
.workspace-title,h1 { font-size:clamp(19px,3vw,25px); }
h2 { font-size:18px; }
.panel h2,.inspector h2 { margin-bottom:9px; }
.button,.item { min-height:44px; color:var(--ink); background:#fff9e6; border:1.5px solid var(--ink); border-radius:8px; }
.button.primary { color:var(--ink); background:var(--orange); border-color:var(--ink); }
.button:hover,.item:hover { color:var(--ink); background:#e3f1e7; border-color:var(--teal); }
.button.primary:hover { color:var(--ink); background:#ffd17a; border-color:var(--ink); }
button:disabled { opacity:.63; }
.item-icon { border:1px solid var(--ink); }
.canvas-wrap { background-color:#ead6a4; background-image:radial-gradient(#715b3b80 1px,transparent 1px); }
#room { background:var(--paper); }
.hint { border-color:var(--ink); padding:10px; }
.subheading,.hint,.item-size,.details-empty,.selection-meta,.acoustic-note,.meter-labels,.meter-caption,.timing,.editor-fields label,.room-size,.wall-inspector p,.acoustic-card p,.acoustic-card output { color:#334c52; font-size:13px; }
.item-name,.measurement,.stat { font-size:13px; }
a { color:#005464; text-underline-offset:.18em; }
:focus-visible { outline:3px solid #8c2e15; outline-offset:3px; }
.topbar :focus-visible { outline-color:#ffdc86; }
input,select,progress { accent-color:#006477; }
.wall-inspector { border-color:#8e8270; }
#arrival-graph { background:#fff9e8; }
.audio-deck { padding:13px 16px; background:#ffe8ad; border-bottom:2px solid var(--ink); }
.audio-deck p { margin:5px 0 0; font-size:13px; }
.deck-heading { display:flex; flex-wrap:wrap; gap:6px 12px; align-items:baseline; }
.deck-heading h2 { margin:0; }
#audio-name { max-width:100%; overflow-wrap:anywhere; font-size:14px; font-weight:650; }
.audio-deck .file-label { align-items:flex-start; flex-direction:column; max-width:100%; font-weight:700; }
#mp3-file { max-width:100%; font:inherit; }
#mp3-file::file-selector-button { min-height:38px; font:inherit; color:var(--ink); border:1.5px solid var(--ink); border-radius:7px; background:#fff9e6; cursor:pointer; }
.level-label { flex-wrap:wrap; }
#audio-error { color:#8c2e15; font-weight:700; }
#track-progress-wrap { margin-top:9px; font-size:13px; }
#track-progress { max-width:45%; vertical-align:middle; }
.keyboard-controls { padding:8px 12px; background:#fdf2d2; }
.keyboard-controls select { max-width:100%; }
.keyboard-controls .acoustic-row { gap:5px 8px; }
.keyboard-controls .button { min-width:44px; }
details > summary { min-height:32px; padding-block:5px; }
@media (max-width:1050px) { .layout { grid-template-columns:185px minmax(0,1fr); } .inspector { display:block; } }
@media (max-width:650px) { .layout { grid-template-columns:minmax(0,1fr); } .workspace { grid-row:auto; } .workspace .canvas-wrap { min-height:230px; } }
@media (prefers-reduced-motion:reduce) { *,*::before,*::after { animation-duration:0s !important; transition-duration:0s !important; } }
.editor-links { display:flex; flex-wrap:wrap; gap:4px 15px; padding:0 14px; border-bottom:1px solid var(--ink); background:#fff9e6; }
.editor-links a { display:inline-flex; align-items:center; min-height:38px; font-size:13px; font-weight:650; }
.measurement span, .stat span { color:#334c52; }
#coverage-layer { pointer-events:none; }
.coverage-key { display:flex; justify-content:space-between; gap:6px; padding:7px 9px; border:1px solid var(--ink); background:linear-gradient(90deg,#357eaf55,#fadd8755,#d7412755); color:var(--ink); font-size:13px; }
#mode-graph { display:block; min-width:430px; width:100%; background:#fff9e8; margin-block:8px; }
#speaker-profile input[type=file] { height:auto; padding-block:5px; }
""",
"""
#speaker-profile p { margin:4px 0; color:#334c52; font-size:13px; overflow-wrap:anywhere; }
.compare-preview { margin:8px 0; font-size:13px; color:#334c52; }
#room [data-aim-handle] { cursor:crosshair; }
.audition-tools { margin-top:9px; padding-top:5px; border-top:1px solid #9d8258; font-size:13px; }
.audition-tools button[aria-pressed=true] { background:#08788a; color:white; }
.preview-banner { padding:9px 15px; background:#08788a; color:#fff; font-size:14px; font-weight:700; border-bottom:1px solid #143743; }
#room.is-audition-preview { opacity:.96; }
#room.is-audition-preview #coverage-layer { display:none; }
#room.is-audition-preview * { cursor:default; }
.preview-banner,.preview-actions { display:flex; align-items:center; flex-wrap:wrap; gap:8px; }
.preview-banner { justify-content:space-between; }
.preview-banner button[aria-pressed=true] { background:#ffb446; }
</style>
            </head>
            <body>
<a class="skip-link" href="#workspace">Skip to room editor</a>
              <header class="topbar">
                <div class="brand"><span class="brand-mark" aria-hidden="true">⌂</span>RoomSketch</div>
                <div class="top-actions">
                  <input type="file" id="import-file" accept=".json,application/json" hidden>
                  <button class="button" id="undo-layout" disabled>Undo</button>
                  <button class="button" id="redo-layout" disabled>Redo</button>
                  <button class="button" id="import-layout">Import layout</button>
                  <button class="button" id="export-button" type="button">Export layout</button>
                  <button class="button primary" id="save-button" type="button">Save layout</button>
                </div>
              </header>
              <main class="layout">
                <section class="panel" aria-labelledby="furniture-heading">
                  <h1>Build your room</h1>
                  <p class="subheading">Drag a piece into the room. Move, rotate, and arrange it to fit.</p>
                  <h2 id="furniture-heading">Furniture &amp; fixtures</h2>
                  <div class="palette" id="palette"></div>
                  <div class="divider"></div><h2 id="speaker-tools-heading">Sound positions</h2>
                  <div class="audio-legend">
                    <span><i class="legend-dot source-dot"></i>Speakers</span>
                    <span><i class="legend-dot listener-dot"></i>Listener</span>
                  </div>
                  <p class="subheading">Drag the markers to position speakers and listener.</p>
                  <h2>Speaker channels</h2>
                  <div class="palette" id="speaker-palette"></div>
                  <div class="divider"></div>
                  <div class="divider"></div>
                  <h2>Custom furniture</h2>
                  <div class="editor-fields">
                    <label class="wide">Label<input id="custom-name" type="text" maxlength="24" value="Custom item"></label>
                    <label>Width (m)<input id="custom-width" type="number" min="0.2" max="8" step="0.1" value="1"></label>
                    <label>Depth (m)<input id="custom-height" type="number" min="0.2" max="8" step="0.1" value="0.8"></label>
                    <label class="wide">Color<input id="custom-color" type="color" value="#cbd5e1"></label>
                  </div>
                  <button class="button" id="add-custom-button" type="button" style="width: 100%">Add custom furniture</button>
                </section>
                <section id="workspace" tabindex="-1" class="workspace" aria-label="Room editor">
                  <div class="workspace-head">
                    <span class="workspace-title">Room plan</span><button class="button primary" id="start-button" type="button">Play sound waves</button>
                    <div class="room-size" aria-label="Room dimensions in meters">
                      <label for="room-width">Width</label><input id="room-width" type="number" min="3" max="12" step="0.5" value="6"><span class="unit">m</span>
                      <label for="room-height">Depth</label><input id="room-height" type="number" min="3" max="12" step="0.5" value="4"><span class="unit">m</span>
                    </div>
                  </div>
                  <div class="audio-deck">
  <div class="deck-heading"><h2>Audio source</h2><span id="audio-name">Room test pulse</span></div>
  <div class="acoustic-row">
    <label class="file-label">Add MP3 <input id="mp3-file" type="file" accept=".mp3,audio/mpeg" aria-describedby="audio-help"></label>
    <button class="button" id="clear-audio" type="button" hidden>Use test pulse</button>
    <label class="level-label">Preview volume <input id="preview-level" type="range" min="0" max="100" value="35"><output id="preview-value" for="preview-level">35%</output></label>
  </div>
  <p id="audio-help">Your file stays in this browser. The same mix feeds every speaker. Speaker positions, levels, and reflections shape what you hear at the listener.</p>
  <div class="audition-tools"><div class="acoustic-row"><button class="button" id="listen-a" data-listen="a" disabled aria-pressed="false">Listen to A</button><button class="button" id="listen-b" data-listen="b" disabled aria-pressed="false">Listen to B</button></div><p class="compare-preview" id="listen-help">Add an MP3 and capture both layouts. Switch between them while the song plays.</p><a href="#compare-heading">Capture A/B layouts</a></div><p id="audio-error" role="status" hidden></p>
  <div id="track-progress-wrap" hidden><label for="track-progress">Track progress</label> <progress id="track-progress" value="0" max="1"></progress> <span id="track-time"></span></div>
</div>

<nav class="editor-links" aria-label="Editor shortcuts"><a href="#furniture-heading">Furniture &amp; fixtures</a><a href="#speaker-tools-heading">Speakers</a><a href="#wall-settings-heading">Surface materials</a><a href="#coverage-card">Coverage</a><a href="#bass-card">Bass modes</a></nav><div class="preview-banner" id="audition-plan-banner" tabindex="-1" hidden><span id="audition-plan-label" role="status"></span><div class="preview-actions"><button class="button" type="button" data-listen="a" aria-pressed="false">Listen to A</button><button class="button" type="button" data-listen="b" aria-pressed="false">Listen to B</button><button class="button" id="stop-preview" type="button">Stop</button></div></div><div class="canvas-wrap" id="canvas-wrap">
                    <svg id="room" role="group" aria-label="Room floor plan. Drag items or use the position controls below.">
                      <defs>
                        <pattern id="grid" width="60" height="60" patternUnits="userSpaceOnUse">
                          <path d="M 60 0 L 0 0 0 60" fill="none" stroke="#d7dce5" stroke-width="1"/>
                          <path d="M 30 0 V 60 M 0 30 H 60" fill="none" stroke="#eceff4" stroke-width="1"/>
                        </pattern>
                        <clipPath id="room-clip"><rect x="0" y="0"/></clipPath>
                      </defs>
                      <rect id="floor" x="0" y="0" fill="#fff9e6" stroke="#143743" stroke-width="5"/>
                      <rect id="grid-overlay" x="0" y="0" fill="url(#grid)" pointer-events="none"/>
                      <g id="coverage-layer" clip-path="url(#room-clip)"></g>
                      <g id="waves" clip-path="url(#room-clip)" pointer-events="none"></g>
                      <g id="objects"></g>
                      <g id="room-walls"></g>
                      <g id="audio-markers"></g>
                    </svg>
                  </div>

  <details class="keyboard-controls">
    <summary>Position items with buttons</summary>
    <div class="acoustic-row"><label>Item to move <select id="position-target"></select></label>
    <button type="button" class="button" data-nudge="left" aria-label="Move left 10 centimeters">←</button>
    <button type="button" class="button" data-nudge="up" aria-label="Move up 10 centimeters">↑</button>
    <button type="button" class="button" data-nudge="down" aria-label="Move down 10 centimeters">↓</button>
    <button type="button" class="button" data-nudge="right" aria-label="Move right 10 centimeters">→</button></div>
  </details>
<div class="hint">Grid squares are 0.5 m · Select a speaker and drag its square handle to aim</div>
<div class="acoustic-card">
    <h2>Arrival timeline</h2>
    <div class="acoustic-row"><label><input type="checkbox" id="silent"> Visual only</label><label>Air temperature (°C) <input type="number" id="air-temp" min="-10" max="45" step="1" value="20"></label><output id="speed-readout"></output></div>
    <p>Expanding blue arcs show direct wavefronts, shaded by 1 kHz speaker direction and partition loss. Solid paths go straight to the listener. Dashed paths bounce off a wall. The cursor replays arrivals in slow motion.</p>
    <div class="graph-scroll"><svg id="arrival-graph" role="img" aria-label="Sound arrival times for the active room layout"></svg></div>
    <output id="arrival-summary"></output>
    <details><summary>Path measurements</summary><div class="table-scroll"><table><thead><tr><th>Speaker</th><th>Path</th><th>Distance</th><th>Arrival</th><th>Relative level</th></tr></thead><tbody id="path-rows"></tbody></table></div></details>
  </div>

<details class="acoustic-card coverage-card" id="coverage-card">
  <summary>Coverage heatmap</summary>
  <div class="acoustic-row">
    <label><input type="checkbox" id="coverage-enabled"> Show on room plan</label>
    <label>Frequency <select id="coverage-band"><option value="0">Bass · 125 Hz</option><option value="1" selected>Mids · 1 kHz</option><option value="2">Highs · 4 kHz</option></select></label>
  </div>
  <div class="coverage-key" role="img" aria-label="Coverage scale from 40 to 100 dB"><span>≤40 dB</span><span>70 dB</span><span>≥100 dB</span></div>
  <p>Estimated direct and early-reflection levels at listener height. The fixed scale makes layouts comparable.</p>
  <output id="coverage-reading"></output>
</details>
<details class="acoustic-card" id="bass-card">
  <summary>Room height and bass modes</summary>
  <div class="acoustic-row">
    <label>Ceiling height (m) <input id="ceiling-height" type="number" min="1.5" max="8" step="0.1" value="2.6"></label>
    <label>Listener height (m) <input id="listener-height" type="number" min="0.1" max="2.5" step="0.05" value="1.2"></label>
  </div>
  <p>Choose floor and ceiling materials in Surfaces &amp; materials. The arrival graph includes first reflections from both surfaces. The chart below shows estimated axial bass modes, which form between opposite surfaces.</p>
  <div class="graph-scroll"><svg id="mode-graph" role="img" aria-label="Estimated axial bass response from 20 to 200 hertz"></svg></div>
  <div class="table-scroll"><table><thead><tr><th>Frequency</th><th>Axis</th><th>Mode</th><th>Listener at</th></tr></thead><tbody id="mode-rows"></tbody></table></div>
  <p>The bass chart is a diagnostic based on a rectangular room. Partitions and diagonal modes are outside this approximation.</p>
</details>


                </section>
                <aside class="panel inspector" aria-labelledby="inspector-heading">
                  <h2 id="inspector-heading">Selection</h2>
                  <div>
                    <div id="selection" class="details-empty">Select an item in the room to see its size and actions.</div>
                    <div class="editor-fields" id="furniture-editor" hidden>
                      <label class="wide">Label<input id="edit-name" type="text" maxlength="24"></label>
                      <label>Width (m)<input id="edit-width" type="number" min="0.2" max="8" step="0.1"></label>
                      <label>Depth (m)<input id="edit-height" type="number" min="0.2" max="8" step="0.1"></label>
                      <label class="wide">Color<input id="edit-color" type="color"></label>
                    </div>
                    <div class="editor-fields" id="speaker-editor" hidden>
                      <label class="wide">Label<input id="speaker-name" type="text" maxlength="24"></label>
                      <label>Sensitivity (dB @ 1 W/1 m)<input id="speaker-sensitivity" type="number" min="60" max="110" step="0.5"></label>
                      <label>Input power (W)<input id="speaker-power" type="number" min="0.1" max="2000" step="0.1"></label>
                      <label>Direction (°)<input id="speaker-angle" type="number" min="-180" max="180" step="1"></label>
                      <label class="wide">Projection width (°)<input id="speaker-coverage" type="number" min="15" max="360" step="5"></label>
                    </div>
                    <div id="speaker-profile" class="editor-fields" hidden>
  <label>Speaker height (m) <input id="speaker-z" type="number" min="0.1" max="2.5" step="0.05" value="1.2"></label>
  <div class="wide"><strong>Speaker profile</strong><p id="profile-name">Generic speaker</p></div>
  <label class="wide">Import speaker profile JSON <input id="profile-file" type="file" accept=".json,application/json" aria-describedby="profile-help"></label>
  <button class="button" id="clear-profile" type="button" hidden>Use generic speaker</button>
  <details class="wide" id="profile-help"><summary>Profile format and example</summary><p>Use measured horizontal polar data, with angles from 0° to 180°. Response is relative to nominal sensitivity. Off-axis entries are relative to the on-axis response at each frequency.</p><a href="data:application/json;charset=utf-8,%7B%0A%20%20%22name%22%3A%20%22Example%20two-way%20speaker%22%2C%0A%20%20%22frequenciesHz%22%3A%20%5B125%2C%20500%2C%201000%2C%202000%2C%204000%2C%208000%5D%2C%0A%20%20%22anglesDeg%22%3A%20%5B0%2C%2030%2C%2060%2C%2090%2C%20120%2C%20180%5D%2C%0A%20%20%22responseDb%22%3A%20%5B-3%2C%200%2C%200%2C%200.5%2C%20-1%2C%20-3%5D%2C%0A%20%20%22offAxisDb%22%3A%20%5B%0A%20%20%20%20%5B0%2C%200%2C%200%2C%200%2C%200%2C%200%5D%2C%0A%20%20%20%20%5B0%2C%20-0.2%2C%20-0.5%2C%20-1%2C%20-2%2C%20-4%5D%2C%0A%20%20%20%20%5B-0.3%2C%20-1%2C%20-2%2C%20-4%2C%20-7%2C%20-12%5D%2C%0A%20%20%20%20%5B-1%2C%20-2%2C%20-4%2C%20-8%2C%20-13%2C%20-20%5D%2C%0A%20%20%20%20%5B-2%2C%20-4%2C%20-8%2C%20-13%2C%20-20%2C%20-25%5D%2C%0A%20%20%20%20%5B-3%2C%20-7%2C%20-12%2C%20-19%2C%20-25%2C%20-32%5D%0A%20%20%5D%0A%7D%0A" download="speaker-profile.example.json">Download example JSON</a></details>
  <p class="wide" id="profile-status" role="status"></p>
</div>

                    <div class="detail-actions" id="detail-actions" hidden>
                      <button class="button" id="rotate-button" type="button">Rotate 90°</button>
                      <button class="button" id="delete-button" type="button">Remove item</button>
                      <button class="button" id="remove-speaker-button" type="button" hidden>Remove speaker</button>
                    </div>
                  </div>
                  <div>
                    <div class="divider"></div>
                    <h2>Sound check</h2>

                    <div class="meter-row" aria-label="Estimated sound pressure level received">
                      <div class="meter" role="meter" aria-label="Estimated SPL at listener in decibels" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">
                        <div class="meter-fill" id="meter-fill"></div>
                      </div>
                      <div class="meter-labels" aria-hidden="true"><span>100 dB</span><span>50 dB</span><span>0 dB</span></div>
                      <div class="meter-reading">
                        <div class="meter-value" id="energy-value">— dB</div>
                        <div class="meter-caption">estimated direct SPL at listener</div>
                      </div>
                    </div>
                    <div class="divider"></div>
                    <h2>Listening measurements</h2>
                    <div class="measurements">
                      <div class="measurement"><span>Listening distance</span><strong id="listening-distance">—</strong></div>
                      <div class="measurement"><span>Horizontal angle (0° = right)</span><strong id="horizontal-angle">—</strong></div>
                      <div class="measurement"><span>Projection angle</span><strong id="projection-angle">—</strong></div>
                      <div class="measurement"><span>Speaker symmetry</span><strong id="speaker-symmetry">—</strong></div>
                      <div class="measurement"><span>Sound travel time</span><strong id="travel-time">—</strong></div>
                      <div class="measurement"><span>Distance balance</span><strong id="distance-balance">—</strong></div>
                      <div class="measurement"><span>Estimated SPL</span><strong id="estimated-spl">—</strong></div>
                      <div class="measurement"><span>Relative level (1 m ref.)</span><strong id="relative-level">—</strong></div>
""",
"""
                    </div>
                    <div class="timing" id="timing">Arrival time is estimated at 343 m/s; SPL is an estimate based on speaker sensitivity and input power.</div>
                    <div class="measurements" id="speaker-arrivals"></div><p><a href="#arrival-graph">See arrival timeline</a></p>
                    <p class="acoustic-note">Estimated SPL uses nominal sensitivity, input power, distance, and a simplified direction adjustment. It is not a measurement; reflections, obstacles, room response, and real amplifier/speaker behavior are not modeled.</p>
                    <button class="button" id="add-speaker-button" type="button" style="width: 100%">Add speaker</button>
                    <div class="divider"></div>
                    <h2>Room summary</h2>
                    <div class="wall-inspector">
    <h2 id="wall-settings-heading">Surfaces &amp; materials</h2>
    <div class="acoustic-row"><label>Wall <select id="wall-select"></select></label></div>
    <div id="wall-endpoints" class="acoustic-row" hidden></div>
    <div class="acoustic-row"><label>Material <select id="wall-material"></select></label><div id="wall-bands" class="acoustic-row"></div></div>
    <p>Absorption is the fraction of energy removed on reflection. Presets are illustrative. Select a partition in the room to edit its length and material.</p>
  </div>
                    <div class="stat"><span>Floor area</span><strong id="area">24 m²</strong></div>
                    <div class="stat"><span>Fixtures placed</span><strong id="item-count">0</strong></div>
                    <div class="status" id="status" role="status" aria-live="polite"></div>
                  </div>
                </aside>
              </main>
              <section class="acoustic-tools" aria-label="Acoustic tools">

  <div class="acoustic-card">
    <h2 id="compare-heading">Compare layouts</h2>
    <div class="acoustic-row"><button class="button" id="capture-a">Capture A</button><button class="button" id="capture-b">Capture B</button><button class="button" id="restore-a" disabled>Load A</button><button class="button" id="restore-b" disabled>Load B</button></div>
    <div class="table-scroll"><table><thead><tr><th>Listener metric</th><th>Layout A</th><th>Layout B</th><th>B minus A</th></tr></thead><tbody id="comparison"></tbody></table></div><div class="acoustic-row"><button class="button" type="button" data-listen="a" aria-pressed="false" disabled>Listen to A</button><button class="button" type="button" data-listen="b" aria-pressed="false" disabled>Listen to B</button></div><p id="comparison-listen-status" role="status"></p>
  </div>
  <details class="acoustic-card"><summary>How this model works</summary>
    <p>Direct sound and first specular reflections use three-dimensional path lengths, air temperature, and frequency-dependent direction and absorption. The plan shows a top-down projection. Partitions use a single editable transmission loss. The playback uses three tone bands with path-specific gain and delay, with an overall output cap. The SPL estimate in the sidebar describes direct sound only.</p>
    <p>The bass-mode chart is separate from playback. Playback supports imported horizontal speaker response. Diffraction, scattering, and later reverberation are outside this model. Material mounting and real room dimensions can substantially change the result.</p>
    <p><a href="https://jontallen.ece.illinois.edu/uploads/537.F18/Papers/Public/Allen-PDFs/AllenBerkley79.pdf">Allen &amp; Berkley image-source method</a> · <a href="https://www.genelec.com/en/key-technologies/directivity-control-waveguide-technology">Genelec speaker directivity</a> · <a href="https://www.ecophon.com/uk/about-ecophon/acoustic-knowledge/how-to-create-good-room-acoustics/choosing-the-right-sound-absorption/">Ecophon absorption measurements</a> · <a href="https://www.grc.nasa.gov/www/k-12/BGP/sound.html">NASA speed of sound</a></p>
  </details>
</section>

<script>
                "use strict";
                const SCALE = 60;
                const STORAGE_KEY = "roomsketch-layout-v1";
                const catalog = {
                  sofa:   { name: "Sofa",   width: 1.8, height: 0.8, color: "#d9d4fb", icon: "🛋️" },
                  table:  { name: "Table",  width: 1.2, height: 0.8, color: "#f7dfb4", icon: "▦" },
                  chair:  { name: "Chair",  width: 0.6, height: 0.6, color: "#f6d2c8", icon: "🪑" },
                  screen: { name: "TV / Projector screen", width: 1.8, height: 0.12, color: "#c7d2e2", icon: "▰" }
                };
                const legacyCatalog = {
                  bed: { name: "Bed", width: 2, height: 1.4, color: "#cfe5fb" },
                  plant: { name: "Plant", width: 0.5, height: 0.5, color: "#cce8d6" },
                  door: { name: "Door", width: 0.9, height: 0.15, color: "#e8c89f" },
                  window: { name: "Window", width: 1.2, height: 0.15, color: "#b9e3ef" }
                };
                const speakerPresets = [
                  { role: "Front Left", icon: "◀" },
                  { role: "Front Right", icon: "▶" },
                  { role: "Center", icon: "●" },
                  { role: "Surround Left", icon: "◀" },
                  { role: "Surround Right", icon: "▶" },
                  { role: "Rear Left", icon: "◀" },
                  { role: "Rear Right", icon: "▶" },
                  { role: "Subwoofer", icon: "◉" },
                  { role: "Atmos Left", icon: "⬆" },
                  { role: "Atmos Right", icon: "⬆" }
                ];
                const svg = document.getElementById("room");
                const objectLayer = document.getElementById("objects");
                const selection = document.getElementById("selection");
                const status = document.getElementById("status");
                const meter = document.querySelector(".meter");
                const waveLayer = document.getElementById("waves");
                const markerLayer = document.getElementById("audio-markers");
                const state = {
                  width: 6, height: 4, objects: [], selectedId: null, drag: null,
                  audio: {
                    speakers: [
                      { id: "speaker-1", role: "Front Left", name: "Front Left", x: 1.5, y: 1.35, angle: 11, coverage: 120, sensitivity: 88, power: 1 },
                      { id: "speaker-2", role: "Front Right", name: "Front Right", x: 1.5, y: 2.65, angle: -11, coverage: 120, sensitivity: 88, power: 1 }
                    ],
                    listener: { x: 5, y: 2 }
                  }
                };
                const svgNS = "http://www.w3.org/2000/svg";
                let animationFrame = null;
                let runStartedAt = 0;
                let audioContext = null;
                let soundCheckRun = 0;
                let soundCheckRunning = false;

                function showStatus(message, isError = false) {
                  status.textContent = message;
                  status.classList.toggle("error", isError);
                }

                function clamp(value, min, max) {
                  return Math.max(min, Math.min(max, value));
                }

                function dimensions(item) {
                  const definition = itemDefinition(item);
                  return item.rotation % 180 === 0
                    ? { width: definition.width, height: definition.height }
                    : { width: definition.height, height: definition.width };
                }

                function itemDefinition(item) {
                  const base = catalog[item.type] || {};
                  return {
                    name: item.name || base.name || "Custom item",
                    width: item.width ?? base.width ?? 1,
                    height: item.height ?? base.height ?? 1,
                    color: item.color || base.color || "#cbd5e1"
                  };
                }

                function constrain(item) {
                  const size = dimensions(item);
                  item.x = clamp(item.x, 0, Math.max(0, state.width - size.width));
                  item.y = clamp(item.y, 0, Math.max(0, state.height - size.height));
                }

                function constrainAudioPositions() {
                  for (const point of [...state.audio.speakers, state.audio.listener]) {
                    point.x = clamp(point.x, 0.25, state.width - 0.25);
                    point.y = clamp(point.y, 0.25, state.height - 0.25);
                  }
                }

                function renderPlan(layout) {
                  const width = layout.width * SCALE;
                  const height = layout.height * SCALE;
                  svg.setAttribute("viewBox", `0 0 ${width} ${height}`);
                  svg.setAttribute("width", width);
                  svg.setAttribute("height", height);
                  for (const id of ["floor", "grid-overlay"]) {
                    document.getElementById(id).setAttribute("width", width);
                    document.getElementById(id).setAttribute("height", height);
                  }
                  const clip = document.getElementById("room-clip");
                  if (clip) {
                    clip.firstElementChild.setAttribute("width", width);
                    clip.firstElementChild.setAttribute("height", height);
                  }
                  objectLayer.replaceChildren();
                  markerLayer.replaceChildren();

                  for (const item of layout.objects) {
                    const definition = itemDefinition(item);
                    const size = dimensions(item);
                    const w = definition.width * SCALE;
                    const h = definition.height * SCALE;
                    const centerX = (item.x + size.width / 2) * SCALE;
                    const centerY = (item.y + size.height / 2) * SCALE;
                    const group = document.createElementNS(svgNS, "g");
                    group.setAttribute("class", `room-object${item.id === layout.selectedId ? " selected" : ""}`);
                    group.setAttribute("data-id", item.id);
                    group.setAttribute("transform", `translate(${centerX} ${centerY}) rotate(${item.rotation}) translate(${-w / 2} ${-h / 2})`);
                    const rect = document.createElementNS(svgNS, "rect");
                    rect.setAttribute("width", w);
                    rect.setAttribute("height", h);
                    rect.setAttribute("rx", "5");
                    rect.setAttribute("fill", definition.color);
                    rect.setAttribute("stroke", "#788397");
                    rect.setAttribute("stroke-width", "1.5");
                    group.append(rect);
                    const label = document.createElementNS(svgNS, "text");
                    label.setAttribute("x", w / 2);
                    label.setAttribute("y", h / 2);
                    label.textContent = definition.name;
                    group.append(label);
                    objectLayer.append(group);
                  }

                  for (const speaker of layout.audio.speakers) {
                    const selected = layout.selectedId === speaker.id;
                    const group = document.createElementNS(svgNS, "g");
                    group.setAttribute("class", `audio-marker${selected ? " selected" : ""}`);
                    group.setAttribute("data-audio", "speaker");
                    group.setAttribute("data-speaker", speaker.id);
                    group.setAttribute("transform", `translate(${speaker.x * SCALE} ${speaker.y * SCALE})`);
                    const cone = document.createElementNS(svgNS, "path");
                    cone.setAttribute("d", projectionPath(speaker));
                    cone.setAttribute("fill", "#e6684830");
                    cone.setAttribute("stroke", "#e6684870");
                    cone.setAttribute("stroke-width", "1");
                    cone.setAttribute("pointer-events", "none");
                    group.append(cone);
                    const direction = document.createElementNS(svgNS, "line");
                    direction.setAttribute("x1", "0");
                    direction.setAttribute("y1", "0");
                    direction.setAttribute("x2", String(Math.cos(speaker.angle * Math.PI / 180) * 28));
                    direction.setAttribute("y2", String(Math.sin(speaker.angle * Math.PI / 180) * 28));
                    direction.setAttribute("stroke", "#b94c32");
                    direction.setAttribute("stroke-width", "2");
                    direction.setAttribute("pointer-events", "none");
                    group.append(direction);
                    if (selected) {
                      const angle = speaker.angle*Math.PI/180;
                      const hx = Math.cos(angle)*39, hy = Math.sin(angle)*39;
                      const arm = svgNode("line",{x1:0,y1:0,x2:hx,y2:hy,stroke:"#8c2e15","stroke-width":2,"pointer-events":"none"});
                      const handle = svgNode("rect",{x:-8,y:-8,width:16,height:16,rx:4,
                        transform:"translate("+hx+" "+hy+") rotate("+speaker.angle+")",
                        fill:"#ffd17a",stroke:"#8c2e15","stroke-width":3,"data-aim-handle":""});
                      handle.append(svgNode("title",{},"Drag to aim speaker"));
                      group.append(arm,handle);
                    }
                    const circle = document.createElementNS(svgNS, "circle");
                    circle.setAttribute("r", "16");
                    circle.setAttribute("fill", "#e66848");
                    group.append(circle);
                    const letter = document.createElementNS(svgNS, "text");
                    letter.textContent = "S";
                    group.append(letter);
                    const label = document.createElementNS(svgNS, "text");
                    label.setAttribute("class", "marker-label");
                    label.setAttribute("x", "21");
                    label.setAttribute("y", "0");
                    label.textContent = speaker.name;
                    group.append(label);
                    markerLayer.append(group);
                  }

                  {
                    const point = layout.audio.listener;
                    const group = document.createElementNS(svgNS, "g");
                    group.setAttribute("class", `audio-marker${layout.selectedId === "listener" ? " selected" : ""}`);
                    group.setAttribute("data-audio", "listener");
                    group.setAttribute("transform", `translate(${point.x * SCALE} ${point.y * SCALE})`);
                    const circle = document.createElementNS(svgNS, "circle");
                    circle.setAttribute("r", "16");
                    circle.setAttribute("fill", "#4777df");
                    group.append(circle);
                    const letter = document.createElementNS(svgNS, "text");
                    letter.textContent = "L";
                    group.append(letter);
                    const label = document.createElementNS(svgNS, "text");
                    label.setAttribute("class", "marker-label");
                    label.setAttribute("x", "21");
                    label.setAttribute("y", "0");
                    label.textContent = "Listener";
                    group.append(label);
                    markerLayer.append(group);
                  }

                }

                function render() {
                  renderPlan(auditionLayout || state);
                  document.getElementById("area").textContent = `${formatNumber(state.width * state.height)} m²`;
                  document.getElementById("item-count").textContent = String(state.objects.length + state.partitions.length);
                  updateSelection();
                  updateAcousticTools();
                  updateAcousticEstimate();
                }

                function formatNumber(value) {
                  return Number(value.toFixed(2)).toString();
                }

                function updateSelection() {
                  const speaker = state.audio.speakers.find(candidate => candidate.id === state.selectedId);
                  const item = state.objects.find(candidate => candidate.id === state.selectedId);
                  const actions = document.getElementById("detail-actions");
                  const furnitureEditor = document.getElementById("furniture-editor");
                  const speakerEditor = document.getElementById("speaker-editor");
                  updateProfileEditor(speaker);
                  const rotateButton = document.getElementById("rotate-button");
                  const deleteButton = document.getElementById("delete-button");
                  const removeSpeakerButton = document.getElementById("remove-speaker-button");
""",
"""
                  if (speaker) {
                    selection.className = "";
                    selection.replaceChildren();
                    const name = document.createElement("div");
                    name.className = "selection-name";
                    name.textContent = speaker.name;
                    const meta = document.createElement("div");
                    meta.className = "selection-meta";
                    meta.textContent = `${speaker.sensitivity} dB @ 1 W/1 m · ${speaker.power} W · ${speaker.angle}° direction`;
                    selection.append(name, meta);
                    document.getElementById("speaker-name").value = speaker.name;
                    document.getElementById("speaker-sensitivity").value = speaker.sensitivity;
                    document.getElementById("speaker-power").value = speaker.power;
                    document.getElementById("speaker-angle").value = speaker.angle;
                    document.getElementById("speaker-coverage").value = speaker.coverage;
                    furnitureEditor.hidden = true;
                    speakerEditor.hidden = false;
                    rotateButton.hidden = true;
                    deleteButton.hidden = true;
                    removeSpeakerButton.hidden = false;
                    actions.hidden = false;
                    return;
                  }
                  const wall = selectedPartition();
                  if (wall) {
                    selection.className = "";
                    const name = document.createElement("div");
                    name.className = "selection-name";
                    name.textContent = "Wall partition";
                    const meta = document.createElement("div");
                    meta.className = "selection-meta";
                    meta.textContent = formatNumber(wallLength(wall))+" m · Drag either end to resize";
                    selection.replaceChildren(name,meta);
                    furnitureEditor.hidden = true;
                    speakerEditor.hidden = true;
                    rotateButton.hidden = false;
                    deleteButton.hidden = false;
                    removeSpeakerButton.hidden = true;
                    actions.hidden = false;
                    return;
                  }
                  if (!item) {
                    selection.className = "details-empty";
                    selection.textContent = "Select an item in the room to see its size and actions.";
                    furnitureEditor.hidden = true;
                    speakerEditor.hidden = true;
                    actions.hidden = true;
                    return;
                  }
                  const definition = itemDefinition(item);
                  const size = dimensions(item);
                  selection.className = "";
                  selection.replaceChildren();
                  const name = document.createElement("div");
                  name.className = "selection-name";
                  name.textContent = definition.name;
                  const meta = document.createElement("div");
                  meta.className = "selection-meta";
                  meta.textContent = `${formatNumber(size.width)} × ${formatNumber(size.height)} m · ${item.rotation}°`;
                  selection.append(name, meta);
                  document.getElementById("edit-name").value = definition.name;
                  document.getElementById("edit-width").value = definition.width;
                  document.getElementById("edit-height").value = definition.height;
                  document.getElementById("edit-color").value = definition.color;
                  furnitureEditor.hidden = false;
                  speakerEditor.hidden = true;
                  rotateButton.hidden = false;
                  deleteButton.hidden = false;
                  removeSpeakerButton.hidden = true;
                  actions.hidden = false;
                }

                function pointInRoom(event) {
                  const point = svg.createSVGPoint();
                  point.x = event.clientX;
                  point.y = event.clientY;
                  const transformed = point.matrixTransform(svg.getScreenCTM().inverse());
                  return { x: transformed.x / SCALE, y: transformed.y / SCALE };
                }

                function updateAcousticEstimate() {
                  const listener = state.audio.listener;
                  const measurements = currentPaths.filter(path => !path.wall).map(path => {
                    const {speaker, distance, delay, db:spl} = path;
                    const bearing = Math.atan2(listener.y-speaker.y,listener.x-speaker.x)*180/Math.PI;
                    const referenceSpl = spl+20*Math.log10(Math.max(distance,0.25));
                    return {speaker,distance,bearing,delay,spl,referenceSpl};
                  });
                  if (!measurements.length) {
                    for (const id of ["listening-distance", "horizontal-angle", "projection-angle",
                      "speaker-symmetry", "travel-time", "distance-balance", "estimated-spl", "relative-level"]) {
                      document.getElementById(id).textContent = "—";
                    }
                    document.getElementById("energy-value").textContent = "— dB";
                    document.getElementById("meter-fill").style.height = "0%";
                    meter.setAttribute("aria-valuenow", "0");
                    document.getElementById("speaker-arrivals").replaceChildren();
                    byId("timing").textContent = "Add a speaker to calculate arrival time.";
                    return;
                  }
                  const average = key => measurements.reduce((total, entry) => total + entry[key], 0) / measurements.length;
                  const totalPower = measurements.reduce((total, entry) => total + 10 ** (entry.spl / 10), 0);
                  const referencePower = measurements.reduce((total, entry) => total + 10 ** (entry.referenceSpl / 10), 0);
                  const estimatedSpl = 10 * Math.log10(totalPower);
                  const referenceSpl = 10 * Math.log10(referencePower);
                  const first = measurements[0];
                  const selectedEntry = measurements.find(entry => entry.speaker.id === state.selectedId) || first;
                  const selectedSpeaker = selectedEntry.speaker;
                  const balance = pairedBalance(measurements);
                  document.getElementById("listening-distance").textContent = `${formatNumber(average("distance"))} m avg`;
                  document.getElementById("horizontal-angle").textContent =
                    `${selectedSpeaker.name}: ${Math.round(normalizeAngle(selectedEntry.bearing))}°`;
                  document.getElementById("projection-angle").textContent =
                    `${selectedSpeaker.name}: ${selectedSpeaker.angle}° / ${selectedSpeaker.coverage}°`;
                  document.getElementById("speaker-symmetry").textContent =
                    balance.symmetry === "—" ? "—" : `${balance.symmetry}%`;
                  document.getElementById("travel-time").textContent = `${formatNumber(average("delay"))} ms avg`;
                  document.getElementById("distance-balance").textContent =
                    balance.distance === "—" ? "—" : `${balance.distance}%`;
                  document.getElementById("estimated-spl").textContent = `${formatNumber(estimatedSpl)} dB`;
                  const relativeLevel = estimatedSpl - referenceSpl;
                  document.getElementById("relative-level").textContent =
                    `${relativeLevel >= 0 ? "+" : ""}${formatNumber(relativeLevel)} dB vs 1 m`;
                  document.getElementById("energy-value").textContent = `${formatNumber(estimatedSpl)} dB`;
                  const meterPercent = clamp(estimatedSpl, 0, 100);
                  document.getElementById("meter-fill").style.height = `${meterPercent}%`;
                  meter.setAttribute("aria-valuenow", meterPercent.toFixed(1));
                  const earliest = Math.min(...measurements.map(entry => entry.delay));
                  const latest = Math.max(...measurements.map(entry => entry.delay));
                  document.getElementById("timing").textContent =
                    `Arrival-time difference (earliest to latest): ${formatNumber(latest - earliest)} ms`;
                  const arrivals = document.getElementById("speaker-arrivals");
                  arrivals.replaceChildren();
                  for (const entry of measurements) {
                    const row = document.createElement("div");
                    row.className = "measurement";
                    const name = document.createElement("span");
                    name.textContent = entry.speaker.name;
                    const arrival = document.createElement("strong");
                    arrival.textContent = `${formatNumber(entry.delay)} ms`;
                    row.append(name, arrival);
                    arrivals.append(row);
                  }
                }

                function normalizeAngle(angle) {
                  return ((angle + 180) % 360 + 360) % 360 - 180;
                }

                function pairedBalance(measurements) {
                  if (measurements.length < 2) return { symmetry: "—", distance: "—" };
                  const listener = state.audio.listener;
                  const centerX = measurements.reduce((total, entry) => total + entry.speaker.x, 0) / measurements.length;
                  const centerY = measurements.reduce((total, entry) => total + entry.speaker.y, 0) / measurements.length;
                  let forwardX = centerX - listener.x;
                  let forwardY = centerY - listener.y;
                  const magnitude = Math.hypot(forwardX, forwardY) || 1;
                  forwardX /= magnitude;
                  forwardY /= magnitude;
                  const positioned = measurements.map(entry => ({
                    ...entry,
                    lateral: forwardX * (entry.speaker.y - listener.y) -
                      forwardY * (entry.speaker.x - listener.x)
                  }));
                  const left = positioned.filter(entry => entry.lateral < 0).sort((a, b) => Math.abs(a.lateral) - Math.abs(b.lateral));
                  const right = positioned.filter(entry => entry.lateral >= 0).sort((a, b) => Math.abs(a.lateral) - Math.abs(b.lateral));
                  const pairs = Math.min(left.length, right.length);
                  if (!pairs) return { symmetry: "0", distance: "0" };
                  const pairedCoverage = pairs / Math.max(left.length, right.length);
                  let symmetryTotal = 0;
                  let distanceTotal = 0;
                  for (let index = 0; index < pairs; index++) {
                    const leftEntry = left[index];
                    const rightEntry = right[index];
                    const maxDistance = Math.max(leftEntry.distance, rightEntry.distance, 0.01);
                    const maxLateral = Math.max(Math.abs(leftEntry.lateral), Math.abs(rightEntry.lateral), 0.01);
                    symmetryTotal += (1 - Math.abs(Math.abs(leftEntry.lateral) - Math.abs(rightEntry.lateral)) / maxLateral) * 100;
                    distanceTotal += (1 - Math.abs(leftEntry.distance - rightEntry.distance) / maxDistance) * 100;
                  }
                  return {
                    symmetry: String(Math.round(clamp(symmetryTotal / pairs * pairedCoverage, 0, 100))),
                    distance: String(Math.round(clamp(distanceTotal / pairs * pairedCoverage, 0, 100)))
                  };
                }

                function projectionPath(speaker) {
                  const radius = 72;
                  const halfAngle = speaker.coverage * Math.PI / 360;
                  const angle = speaker.angle * Math.PI / 180;
                  const start = angle - halfAngle;
                  const end = angle + halfAngle;
                  const startX = Math.cos(start) * radius;
                  const startY = Math.sin(start) * radius;
                  const endX = Math.cos(end) * radius;
                  const endY = Math.sin(end) * radius;
                  if (speaker.coverage >= 360) return `M 0 0 m -${radius} 0 a ${radius} ${radius} 0 1 0 ${radius * 2} 0 a ${radius} ${radius} 0 1 0 -${radius * 2} 0`;
                  return `M 0 0 L ${startX} ${startY} A ${radius} ${radius} 0 ${speaker.coverage > 180 ? 1 : 0} 1 ${endX} ${endY} Z`;
                }

                function createId() {
                  return globalThis.crypto?.randomUUID
                    ? globalThis.crypto.randomUUID()
                    : `item-${Date.now()}-${Math.random().toString(16).slice(2)}`;
                }

                function addItem(type, x, y, custom = null) {
                  if (type === "wall-partition") { addPartition(x,y); return; }
                  const definition = custom || catalog[type];
                  if (!definition) return;
                  const item = {
                    id: createId(),
                    type,
                    x: x - definition.width / 2,
                    y: y - definition.height / 2,
                    rotation: 0,
                    name: definition.name,
                    width: definition.width,
                    height: definition.height,
                    color: definition.color
                  };
                  constrain(item);
                  state.objects.push(item);
                  state.selectedId = item.id;
                  render();
                }

                function addSpeaker(role = "Speaker", position = null) {
                  const sameRoleCount = state.audio.speakers.filter(speaker => speaker.role === role).length;
                  const name = sameRoleCount ? `${role} ${sameRoleCount + 1}` : role;
                  const positionByRole = {
                    "Front Left": [0.2, 0.3],
                    "Front Right": [0.2, 0.7],
                    "Center": [0.18, 0.5],
                    "Surround Left": [0.62, 0.12],
                    "Surround Right": [0.62, 0.88],
                    "Rear Left": [0.84, 0.3],
                    "Rear Right": [0.84, 0.7],
                    "Subwoofer": [0.42, 0.5],
                    "Atmos Left": [0.5, 0.32],
                    "Atmos Right": [0.5, 0.68],
                    "Speaker": [0.25, 0.5]
                  };
                  const relativePosition = position || positionByRole[role] || positionByRole.Speaker;
                  const speaker = {
                    id: createId(),
                    role,
                    name,
                    x: clamp(state.width * relativePosition[0], 0.25, state.width - 0.25),
                    y: clamp(state.height * relativePosition[1], 0.25, state.height - 0.25),
                    angle: 0,
                    coverage: 120,
                    sensitivity: 88,
                    power: 1,
                    z: defaultElevation
                  };
                  const listener = state.audio.listener;
                  speaker.angle = Math.round(Math.atan2(listener.y - speaker.y, listener.x - speaker.x) * 180 / Math.PI);
                  state.audio.speakers.push(speaker);
                  state.selectedId = speaker.id;
                  render();
                }

                function updateFurniture() {
                  const item = state.objects.find(candidate => candidate.id === state.selectedId);
                  if (!item) return;
                  const name = document.getElementById("edit-name").value.trim();
                  const width = Number(document.getElementById("edit-width").value);
                  const height = Number(document.getElementById("edit-height").value);
                  const color = document.getElementById("edit-color").value;
                  const rotatedWidth = item.rotation % 180 === 0 ? width : height;
                  const rotatedHeight = item.rotation % 180 === 0 ? height : width;
                  if (!name || name.length > 24 || !Number.isFinite(width) || !Number.isFinite(height) ||
                      width < 0.2 || width > 8 || height < 0.2 || height > 8 ||
                      rotatedWidth > state.width || rotatedHeight > state.height ||
                      !/^#[0-9a-f]{6}$/i.test(color)) {
                    showStatus("Furniture needs a label and dimensions that fit the room in its current rotation (0.2–8 m).", true);
                    updateSelection();
                    return;
                  }
                  const centerX = item.x + dimensions(item).width / 2;
                  const centerY = item.y + dimensions(item).height / 2;
                  item.name = name;
                  item.width = width;
                  item.height = height;
""",
"""
                  item.color = color;
                  const size = dimensions(item);
                  item.x = centerX - size.width / 2;
                  item.y = centerY - size.height / 2;
                  constrain(item);
                  showStatus("Furniture updated.");
                  render();
                }

                function updateSpeaker() {
                  const speaker = state.audio.speakers.find(candidate => candidate.id === state.selectedId);
                  if (!speaker) return;
                  const name = document.getElementById("speaker-name").value.trim();
                  const sensitivity = Number(document.getElementById("speaker-sensitivity").value);
                  const power = Number(document.getElementById("speaker-power").value);
                  const angle = Number(document.getElementById("speaker-angle").value);
                  const coverage = Number(document.getElementById("speaker-coverage").value);
                  if (!name || name.length > 24 || !Number.isFinite(sensitivity) || sensitivity < 60 || sensitivity > 110 ||
                      !Number.isFinite(power) || power < 0.1 || power > 2000 ||
                      !Number.isFinite(angle) || angle < -180 || angle > 180 ||
                      !Number.isFinite(coverage) || coverage < 15 || coverage > 360) {
                    showStatus("Enter a label, sensitivity from 60–110 dB, input power from 0.1–2000 W, direction from −180–180°, and projection width from 15–360°.", true);
                    updateSelection();
                    return;
                  }
                  speaker.name = name;
                  speaker.sensitivity = sensitivity;
                  speaker.power = power;
                  speaker.angle = angle;
                  speaker.coverage = coverage;
                  showStatus("Speaker settings updated.");
                  render();
                }

                function addCustomFurniture() {
                  const name = document.getElementById("custom-name").value.trim();
                  const width = Number(document.getElementById("custom-width").value);
                  const height = Number(document.getElementById("custom-height").value);
                  const color = document.getElementById("custom-color").value;
                  if (!name || name.length > 24 || !Number.isFinite(width) || !Number.isFinite(height) ||
                      width < 0.2 || width > Math.min(8, state.width) ||
                      height < 0.2 || height > Math.min(8, state.height)) {
                    showStatus("Custom furniture needs a label and dimensions that fit the room (0.2–8 m).", true);
                    return;
                  }
                  addItem("custom", state.width / 2, state.height / 2, { name, width, height, color });
                  showStatus("Custom furniture added.");
                }

                function parseLayout(raw) {
                  const saved = JSON.parse(raw);
                  if (!saved || typeof saved !== "object" || !Array.isArray(saved.objects)) {
                    throw new Error("Saved layout has an invalid format.");
                  }
                  const roomWidth = Number(saved.width);
                  const roomHeight = Number(saved.height);
                  if (!Number.isFinite(roomWidth) || !Number.isFinite(roomHeight) ||
                      roomWidth < 3 || roomWidth > 12 || roomHeight < 3 || roomHeight > 12) {
                    throw new Error("Saved room dimensions are invalid.");
                  }
                  const objects = saved.objects.map(item => {
                    if (!item || (!catalog[item.type] && !legacyCatalog[item.type] && item.type !== "custom") ||
                        !Number.isFinite(item.x) || !Number.isFinite(item.y) || !Number.isFinite(item.rotation)) {
                      throw new Error("A saved furniture item is invalid.");
                    }
                    const base = catalog[item.type] || legacyCatalog[item.type] || {};
                    const width = item.width ?? base.width;
                    const height = item.height ?? base.height;
                    const name = item.name ?? base.name ?? "Custom item";
                    const color = item.color ?? base.color ?? "#cbd5e1";
                    const rotation = ((Math.round(item.rotation / 90) * 90) % 360 + 360) % 360;
                    const rotatedWidth = rotation % 180 === 0 ? width : height;
                    const rotatedHeight = rotation % 180 === 0 ? height : width;
                    if (!Number.isFinite(width) || !Number.isFinite(height) ||
                        width < 0.2 || width > 8 || height < 0.2 || height > 8 ||
                        rotatedWidth > roomWidth || rotatedHeight > roomHeight ||
                        typeof name !== "string" || !name.trim() || name.length > 24 ||
                        typeof color !== "string" || !/^#[0-9a-f]{6}$/i.test(color)) {
                      throw new Error("A saved furniture item has invalid custom settings.");
                    }
                    return {
                      id: typeof item.id === "string" ? item.id : createId(),
                      type: legacyCatalog[item.type] ? "custom" : item.type,
                      x: item.x,
                      y: item.y,
                      rotation,
                      name: name.trim(), width, height, color
                    };
                  });
                  const audio = saved.audio || {};
                  const speakers = Array.isArray(audio.speakers)
                    ? audio.speakers
                    : audio.source ? [{ ...audio.source, id: "speaker-1", role: "Front Left", name: "Front Left", angle: 0, coverage: 120, db: 80 }] : [
                      { id: "speaker-1", role: "Front Left", name: "Front Left", x: Math.min(1.5, roomWidth / 3), y: roomHeight / 2 - 0.6, angle: 0, coverage: 120, db: 80 },
                      { id: "speaker-2", role: "Front Right", name: "Front Right", x: Math.min(1.5, roomWidth / 3), y: roomHeight / 2 + 0.6, angle: 0, coverage: 120, db: 80 }
                    ];
                  const checkedSpeakers = speakers.map((speaker, index) => {
                    if (!speaker || !Number.isFinite(speaker.x) || !Number.isFinite(speaker.y) ||
                        !Number.isFinite(speaker.angle ?? 0) || !Number.isFinite(speaker.coverage ?? 120) ||
                        !Number.isFinite(speaker.sensitivity ?? speaker.db ?? 88) ||
                        !Number.isFinite(speaker.power ?? 1)) {
                      throw new Error("Saved speaker settings are invalid.");
                    }
                    const role = speaker.role ?? (index === 0 ? "Front Left" : index === 1 ? "Front Right" : "Speaker");
                    const name = speaker.name ?? role;
                    const angle = speaker.angle ?? 0;
                    const coverage = speaker.coverage ?? 120;
                    const sensitivity = speaker.sensitivity ?? speaker.db ?? 88;
                    const power = speaker.power ?? 1;
                    if (typeof name !== "string" || !name.trim() || name.length > 24 ||
                        typeof role !== "string" || !role.trim() || role.length > 24 ||
                        angle < -180 || angle > 180 || coverage < 15 || coverage > 360 ||
                        sensitivity < 60 || sensitivity > 110 || power < 0.1 || power > 2000) {
                      throw new Error("Saved speaker settings are out of range.");
                    }
                    return {
                      id: typeof speaker.id === "string" ? speaker.id : createId(),
                      role: role.trim(), name: name.trim(), x: speaker.x, y: speaker.y,
                      angle, coverage, sensitivity, power, ...readSpeakerExtras(speaker, saved.roomHeight)
                    };
                  });
                  const listener = audio.listener || { x: Math.max(0.25, roomWidth - 1), y: roomHeight / 2 };
                  if (!Number.isFinite(listener.x) || !Number.isFinite(listener.y)) {
                    throw new Error("Saved listener position is invalid.");
                  }
                  return {width:roomWidth,height:roomHeight,objects,audio:{speakers:checkedSpeakers,listener:{...listener,z:readSpeakerExtras(listener,saved.roomHeight).z}},...readAcousticSettings(saved,roomWidth,roomHeight)};
                }

                function saveLayout() {
                  try {
                    localStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot()));
                    showStatus("Layout saved in this browser.");
                  } catch (error) {
                    showStatus(`Could not save layout: ${error.message}`, true);
                  }
                }

                function exportLayout() {
                  const data = JSON.stringify(snapshot(), null, 2);
                  const url = URL.createObjectURL(new Blob([data], { type: "application/json" }));
                  const link = document.createElement("a");
                  link.href = url;
                  link.download = "room-layout.json";
                  link.click();
                  URL.revokeObjectURL(url);
                  showStatus("Layout exported as room-layout.json.");
                }

                function loadLayout() {
                  try {
                    const raw = localStorage.getItem(STORAGE_KEY);
                    if (!raw) return;
                    applyLayout(parseLayout(raw));
                  } catch (error) {
                    showStatus(`Could not load saved layout: ${error.message}`, true);
                  }
                }

                for (const preset of speakerPresets) {
                  const button = document.createElement("button");
                  button.type = "button";
                  button.className = "item";
                  button.draggable = true;
                  button.dataset.role = preset.role;
                  button.setAttribute("aria-label", `Add ${preset.role} speaker`);
                  const icon = document.createElement("span");
                  icon.className = "item-icon";
                  icon.style.background = "#fbe0d8";
                  icon.textContent = preset.icon;
                  const text = document.createElement("span");
                  text.className = "item-name";
                  text.textContent = preset.role;
                  button.append(icon, text);
                  button.addEventListener("dragstart", event => {
                    event.dataTransfer.setData("text/plain", `speaker:${preset.role}`);
                    event.dataTransfer.effectAllowed = "copy";
                  });
                  button.addEventListener("click", () => addSpeaker(preset.role));
                  document.getElementById("speaker-palette").append(button);
                }

                for (const [type, definition] of Object.entries({...catalog,"wall-partition":{name:"Wall partition",width:2,height:0.1,color:"#e1e5ec",icon:"▥"}})) {
                  const button = document.createElement("button");
                  button.type = "button";
                  button.className = "item";
                  button.draggable = true;
                  button.dataset.type = type;
                  button.setAttribute("aria-label", `Drag ${definition.name}, ${definition.width} by ${definition.height} meters, into the room`);
                  const icon = document.createElement("span");
                  icon.className = "item-icon";
                  icon.style.background = definition.color;
                  icon.textContent = definition.icon;
                  const text = document.createElement("span");
                  text.className = "item-name";
                  text.append(document.createTextNode(definition.name));
                  const size = document.createElement("span");
                  size.className = "item-size";
                  size.textContent = type === "wall-partition" ? "2 m · resizable" : `${definition.width} × ${definition.height} m`;
                  text.append(size);
                  button.append(icon, text);
                  button.addEventListener("dragstart", event => {
                    event.dataTransfer.setData("text/plain", type);
                    event.dataTransfer.effectAllowed = "copy";
                  });
                  button.addEventListener("click", () => addItem(type, state.width / 2, state.height / 2));
                  document.getElementById("palette").append(button);
                }

                svg.addEventListener("dragover", event => {
                  event.preventDefault();
                  event.dataTransfer.dropEffect = "copy";
                });
                svg.addEventListener("drop", event => {
                  event.preventDefault();
                  if (auditionLayout) return;
                  const payload = event.dataTransfer.getData("text/plain");
                  const point = pointInRoom(event);
                  if (payload.startsWith("speaker:")) addSpeaker(payload.slice("speaker:".length), [point.x / state.width, point.y / state.height]);
                  else addItem(payload, point.x, point.y);
                });
                svg.addEventListener("pointerdown", event => {
                  if (auditionLayout) return;
                  const audioMarker = event.target.closest(".audio-marker");
                  if (audioMarker) {
                    const point = pointInRoom(event);
                    const kind = audioMarker.dataset.audio;
                    const speakerId = audioMarker.dataset.speaker;
                    if (speakerId && event.target.closest("[data-aim-handle]")) {
                      state.selectedId = speakerId;
                      state.drag = {kind:"speaker-aim",speakerId,pointerId:event.pointerId};
                      event.preventDefault();
                      svg.setPointerCapture(event.pointerId);
                      return;
                    }
                    state.selectedId = speakerId || (kind === "listener" ? "listener" : null);
                    state.drag = {
                      kind: "audio", audioKind: kind, speakerId,
                      x: kind === "listener" ? state.audio.listener.x :
                        state.audio.speakers.find(speaker => speaker.id === speakerId)?.x,
                      y: kind === "listener" ? state.audio.listener.y :
                        state.audio.speakers.find(speaker => speaker.id === speakerId)?.y,
                      pointerX: point.x, pointerY: point.y, pointerId: event.pointerId
                    };
                    event.preventDefault();
                    svg.setPointerCapture(event.pointerId);
                    render();
                    return;
                  }
                  const group = event.target.closest(".room-object");
                  if (!group) {
                    state.selectedId = null;
                    render();
                    return;
                  }
                  if (group.hasAttribute("data-partition")) {
                    const wall = state.partitions.find(candidate => candidate.id === group.dataset.id);
                    if (!wall) return;
                    selectPartition(wall);
                    const point = pointInRoom(event);
                    state.drag = {kind:"partition",id:wall.id,wall:{...wall},end:event.target.dataset.wallEnd,
                      pointerX:point.x,pointerY:point.y,pointerId:event.pointerId};
                    event.preventDefault();
                    svg.setPointerCapture(event.pointerId);
                    render();
                    return;
                  }
                  const item = state.objects.find(candidate => candidate.id === group.dataset.id);
                  if (!item) return;
                  state.selectedId = item.id;
                  const point = pointInRoom(event);
                  state.drag = { kind: "object", id: item.id, x: item.x, y: item.y, pointerX: point.x, pointerY: point.y, pointerId: event.pointerId };
                  event.preventDefault();
                  svg.setPointerCapture(event.pointerId);
                  render();
                });
                svg.addEventListener("pointermove", event => {
                  if (!state.drag || state.drag.pointerId !== event.pointerId) return;
                  const point = pointInRoom(event);
                  if (state.drag.kind === "speaker-aim") {
                    const speaker = state.audio.speakers.find(entry => entry.id === state.drag.speakerId);
                    if (!speaker) return;
                    const dx = point.x-speaker.x, dy = point.y-speaker.y;
                    if (Math.hypot(dx,dy) < 0.15) return;
""",
"""
                    speaker.angle = Math.round(Math.atan2(dy,dx)*180/Math.PI);
                    render();
                    return;
                  }
                  if (state.drag.kind === "partition") {
                    const wall = selectedPartition();
                    if (!wall) return;
                    movePartition(wall,state.drag,point);
                    render();
                    return;
                  }
                  if (state.drag.kind === "audio") {
                    const marker = state.drag.audioKind === "listener" ? state.audio.listener :
                      state.audio.speakers.find(speaker => speaker.id === state.drag.speakerId);
                    if (!marker) return;
                    marker.x = clamp(state.drag.x + point.x - state.drag.pointerX, 0.25, state.width - 0.25);
                    marker.y = clamp(state.drag.y + point.y - state.drag.pointerY, 0.25, state.height - 0.25);
                    render();
                    return;
                  }
                  const item = state.objects.find(candidate => candidate.id === state.drag.id);
                  if (!item) return;
                  item.x = state.drag.x + point.x - state.drag.pointerX;
                  item.y = state.drag.y + point.y - state.drag.pointerY;
                  constrain(item);
                  render();
                });
                svg.addEventListener("pointerup", event => {
                  if (state.drag?.pointerId !== event.pointerId) return;
                  const wasPartition = state.drag.kind === "partition";
                  state.drag = null;
                  if (wasPartition) refreshWallEditor();
                  recordLayout();
                });
                svg.addEventListener("pointercancel", event => {
                  if (state.drag?.pointerId !== event.pointerId) return;
                  const wasPartition = state.drag.kind === "partition";
                  state.drag = null;
                  if (wasPartition) refreshWallEditor();
                  recordLayout();
                });
                svg.addEventListener("lostpointercapture", event => {
                  if (state.drag?.pointerId !== event.pointerId) return;
                  const wasPartition = state.drag.kind === "partition";
                  state.drag = null;
                  if (wasPartition) refreshWallEditor();
                  recordLayout();
                });
                document.getElementById("rotate-button").addEventListener("click", () => {
                  const wall = selectedPartition();
                  if (wall) {
                    transformPartition(wall,wallLength(wall),Math.PI/2);
                    refreshWallEditor();
                    return;
                  }
                  const item = state.objects.find(candidate => candidate.id === state.selectedId);
                  if (!item) return;
                  const centerX = item.x + dimensions(item).width / 2;
                  const centerY = item.y + dimensions(item).height / 2;
                  const nextRotation = (item.rotation + 90) % 360;
                  const definition = itemDefinition(item);
                  const nextSize = nextRotation % 180 === 0
                    ? { width: definition.width, height: definition.height }
                    : { width: definition.height, height: definition.width };
                  if (nextSize.width > state.width || nextSize.height > state.height) {
                    showStatus("This item cannot rotate because it would not fit in the room.", true);
                    return;
                  }
                  item.rotation = nextRotation;
                  const size = dimensions(item);
                  item.x = centerX - size.width / 2;
                  item.y = centerY - size.height / 2;
                  constrain(item);
                  render();
                });
                document.getElementById("delete-button").addEventListener("click", () => {
                  state.partitions = state.partitions.filter(wall => wall.id !== state.selectedId);
                  refreshWallEditor();
                  state.objects = state.objects.filter(item => item.id !== state.selectedId);
                  state.selectedId = null;
                  render();
                });
                document.getElementById("remove-speaker-button").addEventListener("click", () => {
                  state.audio.speakers = state.audio.speakers.filter(speaker => speaker.id !== state.selectedId);
                  state.selectedId = null;
                  render();
                });
                document.getElementById("add-speaker-button").addEventListener("click", () => addSpeaker());
                document.getElementById("add-custom-button").addEventListener("click", addCustomFurniture);
                document.getElementById("edit-name").addEventListener("change", updateFurniture);
                document.getElementById("edit-width").addEventListener("change", updateFurniture);
                document.getElementById("edit-height").addEventListener("change", updateFurniture);
                document.getElementById("edit-color").addEventListener("change", updateFurniture);
                document.getElementById("speaker-name").addEventListener("change", updateSpeaker);
                document.getElementById("speaker-sensitivity").addEventListener("change", updateSpeaker);
                document.getElementById("speaker-power").addEventListener("change", updateSpeaker);
                document.getElementById("speaker-angle").addEventListener("change", updateSpeaker);
                document.getElementById("speaker-coverage").addEventListener("change", updateSpeaker);
                document.addEventListener("keydown", event => {
                  if ((event.key === "Delete" || event.key === "Backspace") &&
                      !["INPUT", "TEXTAREA", "SELECT"].includes(document.activeElement.tagName) && state.selectedId) {
                    if (selectedPartition()) {
                      event.preventDefault();
                      byId("delete-button").click();
                      return;
                    }
                    state.objects = state.objects.filter(item => item.id !== state.selectedId);
                    state.audio.speakers = state.audio.speakers.filter(speaker => speaker.id !== state.selectedId);
                    state.selectedId = null;
                    render();
                  }
                });

                function updateRoomSize() {
                  const widthInput = document.getElementById("room-width");
                  const heightInput = document.getElementById("room-height");
                  const width = Number(widthInput.value);
                  const height = Number(heightInput.value);
                  if (!Number.isFinite(width) || !Number.isFinite(height) ||
                      width < 3 || width > 12 || height < 3 || height > 12) {
                    showStatus("Room width and depth must be between 3 m and 12 m.", true);
                    widthInput.value = state.width;
                    heightInput.value = state.height;
                    return;
                  }
                  if (state.objects.some(item => dimensions(item).width > width || dimensions(item).height > height) ||
                      state.partitions.some(wall => Math.max(wall.x1,wall.x2) > width || Math.max(wall.y1,wall.y2) > height ||
                        onBoundary(wall,width,height))) {
                    widthInput.value = state.width;
                    heightInput.value = state.height;
                    showStatus("Move or resize the furniture or partitions to fit first.", true);
                    return;
                  }
                  state.width = width;
                  state.height = height;
                  refreshWallEditor();
                  for (const item of state.objects) constrain(item);
                  constrainAudioPositions();
                  showStatus("");
                  render();
                }
                document.getElementById("room-width").addEventListener("change", updateRoomSize);
                document.getElementById("room-height").addEventListener("change", updateRoomSize);
                document.getElementById("save-button").addEventListener("click", saveLayout);
                document.getElementById("export-button").addEventListener("click", exportLayout);
                document.getElementById("start-button").addEventListener("click", () => startSoundCheck());

                const byId = id => document.getElementById(id);
const frequencies = [125, 1000, 4000];
const materials = {
  "Painted drywall": [0.23, 0.06, 0.07],
  "Sealed concrete": [0.02, 0.03, 0.05],
  "Heavy curtain": [0.08, 0.55, 0.72],
  "Acoustic panel": [0.25, 0.85, 0.92],
  "Bare brick": [0.04, 0.07, 0.08],
  "Glass": [0.18, 0.04, 0.02],
  "Wood paneling": [0.19, 0.10, 0.08],
  "Ceramic tile": [0.02, 0.02, 0.03],
  "Fabric-wrapped absorber": [0.35, 0.90, 0.95],
  "Custom": null
};
const wallNames = {north: "Top wall", east: "Right wall", south: "Bottom wall", west: "Left wall"};
const wallColors = {"Painted drywall":"#526176", "Sealed concrete":"#52525b", "Heavy curtain":"#9d347c", "Acoustic panel":"#067765", "Custom":"#6942b5","Bare brick":"#a73925","Glass":"#08788a","Wood paneling":"#825729","Ceramic tile":"#58638a","Fabric-wrapped absorber":"#5c4a77"};
const wallFinish = () => ({material:"Painted drywall", absorption:[...materials["Painted drywall"]]});
const defaultWalls = () => Object.fromEntries(Object.keys(wallNames).map(id => [id, wallFinish()]));
const defaultRoomHeight = 2.6;
const defaultElevation = 1.2;
const horizontalNames = {floor:"Floor",ceiling:"Ceiling"};
const defaultHorizontal = () => ({
  floor:{material:"Wood paneling",absorption:[...materials["Wood paneling"]]},
  ceiling:wallFinish()
});

function validateProfile(profile) {
  const goodAxis = (axis,min,max) => Array.isArray(axis) && axis.length >= 2 && axis.length <= 180 &&
    axis.every((value,i) => Number.isFinite(value) && value >= min && value <= max && (!i || value > axis[i-1]));
  if (!profile || typeof profile.name !== "string" || !profile.name.trim() || profile.name.length > 80) {
    throw new Error("Enter a profile name of 1 to 80 characters.");
  }
  const {frequenciesHz,anglesDeg,responseDb,offAxisDb} = profile;
  if (!goodAxis(frequenciesHz,20,24000) || !goodAxis(anglesDeg,0,180) || anglesDeg[0] !== 0 || anglesDeg.at(-1) !== 180) {
    throw new Error("Use increasing frequencies from 20 to 24000 Hz and angles spanning 0° to 180°.");
  }
  const goodRow = row => Array.isArray(row) && row.length === frequenciesHz.length &&
    row.every(db => Number.isFinite(db) && db >= -100 && db <= 30);
  if (!goodRow(responseDb) || !Array.isArray(offAxisDb) || offAxisDb.length !== anglesDeg.length ||
      !offAxisDb.every(goodRow) || offAxisDb[0].some(db => db !== 0)) {
    throw new Error("Provide a response value per frequency and one off-axis row per angle. Use zeros in the 0° row.");
  }
  return {name:profile.name.trim(),frequenciesHz,anglesDeg,responseDb,offAxisDb};
}

function readSpeakerExtras(speaker, height = defaultRoomHeight) {
  const top = Number.isFinite(height) && height >= 1.5 && height <= 8 ? height : defaultRoomHeight;
  const z = speaker.z ?? defaultElevation;
  if (!Number.isFinite(z) || z < 0.1 || z > top-0.09) throw new Error("A speaker height is outside the room.");
  return speaker.profile ? {z,profile:validateProfile(speaker.profile)} : {z};
}

function interpolation(axis,value) {
  let i = 0;
  while (i < axis.length-2 && axis[i+1] < value) i++;
  return [i,clamp((value-axis[i])/(axis[i+1]-axis[i]),0,1)];
}

const lerp = (a,b,t) => a+(b-a)*t;
const profileBands = new WeakMap();
function speakerBandLevel(speaker, band, angle) {
  const profile = speaker.profile;
  if (!profile) {
    const width = Math.min(360,speaker.coverage*[2.5,1,0.6][band]);
    return -speakerDirectionLoss(angle,width);
  }
  let samples = profileBands.get(profile);
  if (!samples) {
    const axis = profile.frequenciesHz.map(Math.log);
    samples = frequencies.map(frequency => {
      const [i,t] = interpolation(axis,Math.log(frequency));
      return {
        response:lerp(profile.responseDb[i],profile.responseDb[i+1],t),
        polar:profile.offAxisDb.map(row => lerp(row[i],row[i+1],t))
      };
    });
    profileBands.set(profile,samples);
  }
  const [i,t] = interpolation(profile.anglesDeg,angle);
  const {response,polar} = samples[band];
  return response+lerp(polar[i],polar[i+1],t);
}

function updateProfileEditor(speaker) {
  const panel = byId("speaker-profile");
  panel.hidden = !speaker;
  if (!speaker) return;
  byId("speaker-z").max = (state.roomHeight-0.1).toFixed(2);
  byId("speaker-z").value = speaker.z ?? defaultElevation;
  byId("profile-name").textContent = speaker.profile?.name ?? "Generic speaker";
  byId("clear-profile").hidden = !speaker.profile;
  byId("speaker-coverage").disabled = !!speaker.profile;
}

function setupProfiles() {
  byId("profile-file").addEventListener("change",async event => {
    const file = event.target.files[0];
    event.target.value = "";
    const target = state.audio.speakers.find(speaker => speaker.id === state.selectedId);
    if (!file || !target) return;
    try {
      if (file.size > 500_000) throw new Error("Choose a profile under 500 KB.");
      const profile = validateProfile(JSON.parse(await file.text()));
      if (!state.audio.speakers.includes(target)) throw new Error("The selected speaker changed. Select a speaker and try again.");
      target.profile = profile;
      render();
      byId("profile-status").textContent = "Imported "+profile.name+".";
    } catch (error) { byId("profile-status").textContent = "Could not import profile. "+error.message; }
  });
  byId("clear-profile").addEventListener("click",() => {
    const target = state.audio.speakers.find(speaker => speaker.id === state.selectedId);
    if (!target) return;
    delete target.profile;
    render();
    byId("profile-status").textContent = "Generic speaker restored.";
  });
  byId("speaker-z").addEventListener("change",event => {
    const speaker = state.audio.speakers.find(entry => entry.id === state.selectedId);
    if (!speaker) return;
    const value = event.target.valueAsNumber;
    if (!validElevation(value,state.roomHeight)) {
      event.target.value = speaker.z ?? defaultElevation;
      return showStatus("Keep the speaker height inside the room.",true);
    }
    speaker.z = value;
    render();
  });
}

const validElevation = (value,height) => Number.isFinite(value) && value >= 0.1 && value <= height-0.1+1e-9;

function horizontalBounce(speaker,ear,z) {
  const imageZ = 2*z-speaker.z;
  const t = (z-imageZ)/(ear.z-imageZ);
  return {x:lerp(speaker.x,ear.x,t),y:lerp(speaker.y,ear.y,t),z};
}

function verticalWallBounce(speaker,ear,bounce) {
  const before = Math.hypot(bounce.x-speaker.x,bounce.y-speaker.y);
  const after = Math.hypot(bounce.x-ear.x,bounce.y-ear.y);
  return {...bounce,z:lerp(speaker.z,ear.z,before/(before+after))};
}

function bandSum(paths,band) {
  return 10*Math.log10(paths.reduce((sum,path) => sum+10**(path.levels[band]/10),0));
}

let coverageKey = "";
function drawCoverage() {
  const layer = byId("coverage-layer");
  if (!byId("coverage-enabled").checked || !state.audio.speakers.length) {
    layer.replaceChildren();
    coverageKey = "";
    byId("coverage-reading").value = state.audio.speakers.length ? "" : "Add a speaker to estimate coverage.";
    return;
  }
  const band = Number(byId("coverage-band").value);
  const z = state.audio.listener.z ?? defaultElevation;
  const atListener = bandSum(currentPaths,band);
  byId("coverage-reading").value = "At the listener: "+atListener.toFixed(1)+" dB estimated.";
  const layout = {...state,audio:{...state.audio,listener:{z}}};
  const key = JSON.stringify([band,layout.width,layout.height,layout.audio,layout.walls,layout.partitions,layout.horizontal,layout.roomHeight,layout.temperature]);
  if (key === coverageKey) return;
  coverageKey = key;
  const nx=26, ny=Math.max(9,Math.round(nx*state.height/state.width));
  const sizeX=state.width/nx, sizeY=state.height/ny;
  const fragment=document.createDocumentFragment();
  for (let iy=0;iy<ny;iy++) {
    for (let ix=0;ix<nx;ix++) {
      layout.audio.listener = {x:(ix+0.5)*sizeX,y:(iy+0.5)*sizeY,z};
      const level = bandSum(acousticPaths(layout),band);
      const t = clamp((level-40)/60,0,1);
      const color = t < 0.5 ?
        [lerp(53,250,t*2),lerp(126,221,t*2),lerp(175,135,t*2)] :
        [lerp(250,215,t*2-1),lerp(221,65,t*2-1),lerp(135,39,t*2-1)];
      fragment.append(svgNode("rect",{x:ix*sizeX*SCALE,y:iy*sizeY*SCALE,
""",
"""
        width:sizeX*SCALE+0.2,height:sizeY*SCALE+0.2,fill:"rgb("+color.map(Math.round).join(",")+")","fill-opacity":0.55}));
    }
  }
  layer.replaceChildren(fragment);
}

function bassModes(layout) {
  const ear=layout.audio.listener;
  const axis = [
    ["Width","x",layout.width,layout.walls.west,layout.walls.east],
    ["Depth","y",layout.height,layout.walls.north,layout.walls.south],
    ["Height","z",layout.roomHeight,layout.horizontal.floor,layout.horizontal.ceiling]
  ];
  const speed = speedOfSound(layout);
  const modes = [];
  for (const [label,coordinate,length,first,second] of axis) {
    for (let order=1; order*speed/(2*length)<=200; order++) {
      const frequency=order*speed/(2*length);
      const at = position => Math.cos(Math.PI*order*(position[coordinate] ?? defaultElevation)/length);
      const listener = at(ear);
      const excitation = layout.audio.speakers.reduce((sum,speaker) =>
        sum+at(speaker)*10**((speaker.sensitivity-88)/20)*Math.sqrt(speaker.power),0);
      const retention = Math.max(0.05,(1-first.absorption[0])*(1-second.absorption[0]));
      const quality = clamp(2*Math.PI*frequency*(2*length/speed)/(-Math.log(retention)),5,100);
      modes.push({label,order,frequency,listener,weight:listener*excitation,quality});
    }
  }
  return modes.sort((a,b)=>a.frequency-b.frequency);
}

function drawBassModes() {
  const graph = byId("mode-graph");
  const table = byId("mode-rows");
  table.replaceChildren();
  graph.replaceChildren();
  graph.setAttribute("viewBox","0 0 650 170");
  const modes = bassModes(state);
  const x = frequency => 43+(frequency-20)*3.24;
  for (const tick of [20,50,80,110,140,170,200]) {
    graph.append(svgNode("line",{x1:x(tick),x2:x(tick),y1:14,y2:140,stroke:"#d2c6aa"}));
    graph.append(svgNode("text",{x:x(tick),y:158,"text-anchor":"middle","font-size":12,fill:"#143743"},tick+" Hz"));
  }
  for (const mode of modes) {
    const near = Math.abs(mode.listener);
    tableRow(table,[mode.frequency.toFixed(1)+" Hz",mode.label,String(mode.order),
      near < 0.22 ? "Near a null" : near > 0.82 ? "Near a peak" : "Between null and peak"]);
  }
  if (!state.audio.speakers.length || !modes.length) {
    graph.append(svgNode("text",{x:55,y:78,fill:"#143743"},"Add a speaker to estimate mode excitation."));
    return;
  }
  const magnitudes = Array.from({length:361},(_,i) => {
    const frequency=20+i/2;
    let real=0, imaginary=0;
    for (const mode of modes) {
      const ratio=frequency/mode.frequency;
      const re=1-ratio*ratio, im=ratio/mode.quality;
      const fraction=mode.weight/(re*re+im*im);
      real+=fraction*re;
      imaginary-=fraction*im;
    }
    return Math.hypot(real,imaginary);
  });
  const peak = Math.max(...magnitudes);
  if (peak <= 1e-8) {
    graph.append(svgNode("text",{x:55,y:78,fill:"#143743"},"No strong axial excitation in this setup."));
    return;
  }
  graph.append(svgNode("polyline",{points:magnitudes.map((m,i) =>
    x(20+i/2)+","+(16-2.65*clamp(20*Math.log10(m/peak),-45,0))).join(" "),
    fill:"none",stroke:"#08788a","stroke-width":2.5}));
  graph.append(svgNode("text",{x:50,y:12,"font-size":11,fill:"#143743"},"Relative to strongest modeled resonance"));
}

function setupRoomAcoustics() {
  for (const id of ["coverage-card","bass-card"]) {
    document.querySelector('.editor-links a[href="#'+id+'"]').addEventListener("click",() => { byId(id).open = true; });
  }
  for (const id of ["coverage-enabled","coverage-band"]) byId(id).addEventListener("change",drawCoverage);
  byId("ceiling-height").addEventListener("change",event => {
    const height = event.target.valueAsNumber;
    if (!Number.isFinite(height) || height < 1.5 || height > 8) {
      event.target.value = state.roomHeight;
      return showStatus("Choose a ceiling height from 1.5 to 8 m.",true);
    }
    state.roomHeight = height;
    for (const point of [...state.audio.speakers,state.audio.listener]) {
      point.z = Math.min(point.z ?? defaultElevation,height-0.1);
    }
    render();
  });
  byId("listener-height").addEventListener("change",event => {
    const height = event.target.valueAsNumber;
    if (!validElevation(height,state.roomHeight)) {
      event.target.value = state.audio.listener.z ?? defaultElevation;
      return showStatus("Keep the listener height inside the room.",true);
    }
    state.audio.listener.z = height;
    render();
  });
}

Object.assign(state, {walls:defaultWalls(), partitions:[], temperature:20, roomHeight:defaultRoomHeight,horizontal:defaultHorizontal()});
for (const point of [...state.audio.speakers,state.audio.listener]) point.z = defaultElevation;
let selectedWall = "north";
let currentPaths = [];
let acousticSettingsKey = "";
let replay = null;
let graphCursor = null;
let graphScale = null;
let history = [];
let historyIndex = -1;
let restoring = false;
const comparisons = {a: null, b: null};
const directColor = "#245bc4";
const reflectionColor = "#b64c23";

function snapshot() {
  const {width,height,objects,audio,walls,partitions,temperature,roomHeight,horizontal} = state;
  return {width,height,objects,audio,walls,partitions,temperature,roomHeight,horizontal};
}

function recordLayout() {
  if (restoring || state.drag) return;
  const next = JSON.stringify(snapshot());
  if (next === history[historyIndex]) return;
  history = history.slice(0, historyIndex + 1);
  history.push(next);
  if (history.length > 100) history.shift();
  historyIndex = history.length - 1;
  updateHistoryButtons();
}

function updateHistoryButtons() {
  byId("undo-layout").disabled = historyIndex <= 0;
  byId("redo-layout").disabled = historyIndex >= history.length - 1;
}

function applyLayout(layout) {
  if (soundCheckRunning || audioContext) stopSoundCheck(false);
  Object.assign(state, layout, {selectedId:null, drag:null});
  for (const item of state.objects) constrain(item);
  constrainAudioPositions();
  byId("room-width").value = state.width;
  byId("room-height").value = state.height;
  byId("air-temp").value = state.temperature;
  refreshWallEditor();
  render();
}

function stepHistory(delta) {
  const next = historyIndex + delta;
  if (next < 0 || next >= history.length || next === historyIndex) return;
  restoring = true;
  try {
    applyLayout(parseLayout(history[next]));
    historyIndex = next;
  } finally {
    restoring = false;
    updateHistoryButtons();
  }
  showStatus(delta < 0 ? "Layout change undone." : "Layout change redone.");
}

function readAcousticSettings(saved, width, height) {
  function finish(value) {
    if (!value || !Object.hasOwn(materials, value.material) || !Array.isArray(value.absorption) ||
        value.absorption.length !== frequencies.length ||
        value.absorption.some(a => !Number.isFinite(a) || a < 0 || a > 1)) {
      throw new Error("Invalid wall material or absorption.");
    }
    return {material:value.material, absorption: value.absorption};
  }
  const walls = defaultWalls();
  if (saved.walls) for (const id of Object.keys(walls)) walls[id] = finish(saved.walls[id]);
  const partitions = saved.partitions ?? [];
  if (!Array.isArray(partitions) || partitions.length > 12) throw new Error("Invalid partition list.");
  const ids = new Set([...Object.keys(walls),...Object.keys(horizontalNames)]);
  const checked = partitions.map(wall => {
    if (!wall || typeof wall.id !== "string" || ids.has(wall.id) ||
        ![wall.x1, wall.x2, wall.y1, wall.y2, wall.loss].every(Number.isFinite) ||
        Math.min(wall.x1, wall.x2, wall.y1, wall.y2) < 0 ||
        Math.max(wall.x1, wall.x2) > width || Math.max(wall.y1, wall.y2) > height ||
        onBoundary(wall, width, height) ||
        Math.hypot(wall.x2 - wall.x1, wall.y2 - wall.y1) < 0.1 || wall.loss < 0 || wall.loss > 80) {
      throw new Error("A partition is invalid or outside the room.");
    }
    ids.add(wall.id);
    const {id,x1,x2,y1,y2,loss} = wall;
    return {id,x1,x2,y1,y2,loss,...finish(wall)};
  });
  const temperature = saved.temperature ?? 20;
  if (!Number.isFinite(temperature) || temperature < -10 || temperature > 45) throw new Error("Invalid air temperature.");
  const roomHeight = saved.roomHeight ?? defaultRoomHeight;
  if (!Number.isFinite(roomHeight) || roomHeight < 1.5 || roomHeight > 8) throw new Error("Invalid ceiling height.");
  const horizontal = defaultHorizontal();
  if (saved.horizontal) for (const id of Object.keys(horizontal)) horizontal[id] = finish(saved.horizontal[id]);
  return {walls,partitions:checked,temperature,roomHeight,horizontal};
}

function onBoundary(wall, width, height) {
  return (wall.x1 === wall.x2 && [0,width].includes(wall.x1)) ||
    (wall.y1 === wall.y2 && [0,height].includes(wall.y1));
}

function getWalls(layout) {
  const {width:w,height:h} = layout;
  const edges = {north:[0,0,w,0], east:[w,0,w,h], south:[w,h,0,h], west:[0,h,0,0]};
  return [
    ...Object.entries(edges).map(([id,[x1,y1,x2,y2]]) => ({id,name:wallNames[id],x1,y1,x2,y2,...layout.walls[id]})),
    ...layout.partitions.map((wall,index) => ({...wall,name:"Partition " + (index + 1)}))
  ];
}

function wallSettings() {
  return state.walls[selectedWall] || state.horizontal[selectedWall] || state.partitions.find(wall => wall.id === selectedWall);
}

function numberField(parent, name, value, min, max, step, change) {
  const label = document.createElement("label");
  const field = document.createElement("input");
  Object.assign(field, {type:"number", value, min, max, step});
  label.append(name, field);
  parent.append(label);
  field.addEventListener("change", () => {
    if (field.value === "" || !Number.isFinite(field.valueAsNumber) ||
        field.valueAsNumber < min || field.valueAsNumber > max) {
      field.value = value;
      showStatus(name + " must be between " + min + " and " + max + ".", true);
      return;
    }
    change(field.valueAsNumber);
  });
}

function selectedPartition() {
  return state.partitions.find(wall => wall.id === state.selectedId);
}

function wallLength(wall) {
  return Math.hypot(wall.x2-wall.x1,wall.y2-wall.y1);
}

function fitPartition(wall) {
  const minX = Math.min(wall.x1,wall.x2), minY = Math.min(wall.y1,wall.y2);
  const spanX = Math.abs(wall.x2-wall.x1), spanY = Math.abs(wall.y2-wall.y1);
  if (spanX > state.width || spanY > state.height || wallLength(wall) < 0.1) return null;
  const insetX = spanX === 0 ? 0.01 : 0;
  const insetY = spanY === 0 ? 0.01 : 0;
  const x = clamp(minX,insetX,state.width-spanX-insetX);
  const y = clamp(minY,insetY,state.height-spanY-insetY);
  return {...wall,x1:x+wall.x1-minX,x2:x+wall.x2-minX,y1:y+wall.y1-minY,y2:y+wall.y2-minY};
}

function addPartition(x,y) {
  if (state.partitions.length >= 12) return;
  const wall = fitPartition({id:createId(),x1:x-1,y1:y,x2:x+1,y2:y,loss:30,...wallFinish()});
  state.partitions.push(wall);
  selectPartition(wall);
  render();
  showStatus("Wall added. Drag the wall to move it or drag an end handle to resize.");
}

function selectPartition(wall) {
  state.selectedId = wall.id;
  if (selectedWall !== wall.id) {
    selectedWall = wall.id;
    refreshWallEditor();
  }
}

function transformPartition(wall, length, rotation) {
  const centerX = (wall.x1+wall.x2)/2, centerY = (wall.y1+wall.y2)/2;
  const angle = Math.atan2(wall.y2-wall.y1,wall.x2-wall.x1)+rotation;
  const dx = Math.cos(angle)*length/2, dy = Math.sin(angle)*length/2;
  const x = Math.abs(dx) < 1e-8 ? 0 : dx, y = Math.abs(dy) < 1e-8 ? 0 : dy;
  const next = fitPartition({...wall,x1:centerX-x,y1:centerY-y,x2:centerX+x,y2:centerY+y});
  if (!next) {
    showStatus("This wall would extend outside the room.",true);
    return;
  }
  Object.assign(wall,next);
  render();
}

function movePartition(wall, drag, point) {
  const dx = point.x-drag.pointerX, dy = point.y-drag.pointerY;
  const start = drag.wall;
  if (drag.end) {
    const x = "x"+drag.end, y = "y"+drag.end;
    const next = {...start,[x]:clamp(start[x]+dx,0,state.width),[y]:clamp(start[y]+dy,0,state.height)};
    if (wallLength(next) >= 0.1 && !onBoundary(next,state.width,state.height)) Object.assign(wall,next);
  } else {
    const next = fitPartition({...start,x1:start.x1+dx,y1:start.y1+dy,x2:start.x2+dx,y2:start.y2+dy});
    if (next) Object.assign(wall,next);
  }
}


function refreshWallEditor() {
  if (!wallSettings()) selectedWall = "north";
  const wall = wallSettings();
  const select = byId("wall-select");
  select.replaceChildren(...getWalls(state).map(entry => new Option(entry.name,entry.id)),
    ...Object.entries(horizontalNames).map(([id,label]) => new Option(label,id)));
  select.value = selectedWall;
  byId("wall-material").value = wall.material;
  const bands = byId("wall-bands");
  bands.replaceChildren();
  frequencies.forEach((frequency,i) => {
    numberField(bands, frequency + " Hz", wall.absorption[i], 0, 1, 0.01, value => {
      wall.absorption[i] = value;
      wall.material = "Custom";
      render();
      refreshWallEditor();
    });
  });
  const endpoints = byId("wall-endpoints");
  const isPartition = state.partitions.some(entry => entry.id === selectedWall);
  endpoints.hidden = !isPartition;
  document.querySelector('[data-type="wall-partition"]').disabled = state.partitions.length >= 12;
  endpoints.replaceChildren();
  if (!isPartition) return;
  numberField(endpoints,"Length (m)",Number(wallLength(wall).toFixed(2)),0.1,Math.hypot(state.width,state.height),0.01,value => {
    transformPartition(wall,value,0);
    refreshWallEditor();
  });
  numberField(endpoints,"Transmission loss (dB)",wall.loss,0,80,1,value => {
    wall.loss = value;
    render();
    refreshWallEditor();
  });
}

function speedOfSound(layout) {
  return Math.sqrt(1.4 * 287.05 * (273.15 + layout.temperature));
}

function cross(ax,ay,bx,by) { return ax * by - ay * bx; }

function intersection(a,b,wall) {
  const dx = b.x-a.x, dy = b.y-a.y;
  const wx = wall.x2-wall.x1, wy = wall.y2-wall.y1;
  const determinant = cross(dx,dy,wx,wy);
  if (Math.abs(determinant) < 1e-9) return null;
  const qx = wall.x1-a.x, qy = wall.y1-a.y;
  const t = cross(qx,qy,wx,wy) / determinant;
  const u = cross(qx,qy,dx,dy) / determinant;
  if (t <= 1e-7 || t >= 1-1e-7 || u <= 1e-7 || u >= 1-1e-7) return null;
  return {x:a.x+t*dx,y:a.y+t*dy};
}

function mirror(point,wall) {
  const dx = wall.x2-wall.x1, dy = wall.y2-wall.y1;
  const along = ((point.x-wall.x1)*dx + (point.y-wall.y1)*dy) / (dx*dx+dy*dy);
  return {x:2*(wall.x1+along*dx)-point.x, y:2*(wall.y1+along*dy)-point.y};
}

function speakerDirectionLoss(angle, width) {
  return Math.min(36,6*(angle/(width/2))**2);
}

function acousticPaths(layout) {
  const walls = getWalls(layout);
  const partitions = layout.partitions;
  const ear = {...layout.audio.listener,z:layout.audio.listener.z ?? defaultElevation};
  const speed = speedOfSound(layout);
  const results = [];
  function add(speaker, bounce, wall) {
    const points = bounce ? [speaker,bounce,ear] : [speaker,ear];
    let distance = 0, transmission = 0;
    for (let i = 1; i < points.length; i++) {
      const a = points[i-1], b = points[i];
      distance += Math.hypot(b.x-a.x,b.y-a.y,b.z-a.z);
      for (const partition of partitions) {
        if (partition.id !== wall?.id && intersection(a,b,partition)) transmission += partition.loss;
      }
    }
    const target = points[1];
    const angle = Math.abs(normalizeAngle(Math.atan2(target.y-speaker.y,target.x-speaker.x)*180/Math.PI-speaker.angle));
    const sourceLevel = speaker.sensitivity + 10*Math.log10(speaker.power);
    const baseLevel = sourceLevel - 20*Math.log10(Math.max(distance,0.25));
    const levels = frequencies.map((_,i) => {
      const response = speakerBandLevel(speaker,i,distance < 1e-7 ? 0 : angle);
      const transmitted = 10**(-transmission/10);
      const reflection = wall ? Math.max(0,1-wall.absorption[i]-(wall.loss === undefined ? 0 : 10**(-wall.loss/10))) : 1;
      return baseLevel + response + 10*Math.log10(transmitted*reflection);
    });
    const energy = levels.reduce((sum,db) => sum + 10**(db/10),0)/frequencies.length;
    if (!energy && wall) return;
    results.push({speaker,wall,points,distance,delay:distance*1000/speed,levels,db:10*Math.log10(energy),sourceLevel,
      pan:clamp((points.at(-2).y-ear.y)/Math.max(0.25,Math.hypot(points.at(-2).x-ear.x,points.at(-2).y-ear.y)),-1,1)});
  }
  for (const entry of layout.audio.speakers) {
    const speaker = entry.z === undefined ? {...entry,z:defaultElevation} : entry;
    add(speaker);
    for (const [id,z] of [["floor",0],["ceiling",layout.roomHeight]]) {
      const surface = {id,name:horizontalNames[id],...layout.horizontal[id]};
      add(speaker,horizontalBounce(speaker,ear,z),surface);
    }
    for (const wall of walls) {
      const dx = wall.x2-wall.x1, dy = wall.y2-wall.y1;
      const side = p => cross(dx,dy,p.x-wall.x1,p.y-wall.y1);
      if (side(speaker)*side(ear) <= 0) continue;
      const bounce = intersection(mirror(speaker,wall),ear,wall);
""",
"""
      if (bounce) add(speaker,verticalWallBounce(speaker,ear,bounce),wall);
    }
  }
  return results.sort((a,b) => a.delay-b.delay);
}
function svgNode(name, attrs, text) {
  const node = document.createElementNS(svgNS,name);
  for (const [key,value] of Object.entries(attrs)) node.setAttribute(key,value);
  if (text !== undefined) node.textContent = text;
  return node;
}

function drawRoomWalls(layout = auditionLayout || state) {
  const layer = byId("room-walls");
  layer.replaceChildren();
  for (const wall of getWalls(layout)) {
    const line = svgNode("line", {
      x1:wall.x1*SCALE,y1:wall.y1*SCALE,x2:wall.x2*SCALE,y2:wall.y2*SCALE,
      stroke:wallColors[wall.material],"stroke-width":!auditionLayout && wall.id === selectedWall ? 6 : 4,
      "stroke-dasharray":wall.loss === undefined ? "none" : "8 3"
    });
    if (wall.loss === undefined) {
      layer.append(line);
      continue;
    }
    const active = wall.id === layout.selectedId;
    if (active) line.setAttribute("stroke-width",7);
    const group = svgNode("g",{"class":"room-object wall-object","data-id":wall.id,"data-partition":""});
    const target = line.cloneNode();
    target.setAttribute("stroke","transparent");
    target.setAttribute("stroke-width",18);
    target.removeAttribute("stroke-dasharray");
    group.append(line,target);
    if (active) {
      for (const end of [1,2]) group.append(svgNode("circle",{
        cx:wall["x"+end]*SCALE,cy:wall["y"+end]*SCALE,r:8,
        fill:"white",stroke:"#245bc4","stroke-width":3,"data-wall-end":end
      }));
    }
    layer.append(group);
  }
}

function tableRow(parent, values) {
  const row = parent.insertRow();
  for (const value of values) row.insertCell().textContent = value;
}

function drawArrivalGraph(paths) {
  const graph = byId("arrival-graph");
  graph.replaceChildren();
  graphCursor = null;
  const rows = byId("path-rows");
  rows.replaceChildren();
  if (!paths.length) {
    graph.setAttribute("viewBox","0 0 760 70");
    graph.append(svgNode("text",{x:18,y:39,fill:"#526176"},"Add a speaker to see arrivals."));
    byId("arrival-summary").value = "";
    return;
  }
  const speakers = [...new Map(paths.map(path => [path.speaker.id,path.speaker])).values()];
  const height = 58 + 38*speakers.length;
  const left = 145, right = 735;
  const end = Math.max(5,Math.ceil((paths.at(-1).delay + 1)/5)*5);
  graphScale = {left,right,end};
  const x = ms => left + ms/end*(right-left);
  graph.setAttribute("viewBox","0 0 760 " + height);
  const step = Math.max(5,Math.ceil(end/40)*5);
  for (let ms = 0; ms <= end; ms += step) {
    graph.append(svgNode("line",{x1:x(ms),x2:x(ms),y1:12,y2:height-30,stroke:"#dbe3ef"}));
    graph.append(svgNode("text",{x:x(ms),y:height-13,"text-anchor":"middle","font-size":12,fill:"#43516a"},ms + " ms"));
  }
  const peak = Math.max(...paths.map(path => path.db));
  speakers.forEach((speaker,i) => {
    const y = 38+i*38;
    graph.append(svgNode("text",{x:9,y:y+2,"font-size":12,fill:"#27364a"},speaker.name.length > 21 ? speaker.name.slice(0,20)+"…" : speaker.name));
    graph.append(svgNode("line",{x1:left,x2:right,y1:y,y2:y,stroke:"#aab9d1"}));
    for (const path of paths.filter(entry => entry.speaker.id === speaker.id)) {
      const strength = Number.isFinite(path.db) ? Math.max(4,26+(path.db-peak)*0.55) : 4;
      const mark = svgNode("line",{x1:x(path.delay),x2:x(path.delay),y1:y,y2:y-strength,
        stroke:path.wall ? reflectionColor : directColor,"stroke-width":path.wall ? 2 : 4,
        "stroke-dasharray":path.wall ? "3 2" : "none"});
      const type = path.wall?.name ?? "Direct";
      const relative = Number.isFinite(path.db) ? (path.db-peak).toFixed(1)+" dB" : "Inaudible";
      mark.append(svgNode("title",{},speaker.name+" / "+type+" / "+path.delay.toFixed(2)+" ms / "+relative));
      graph.append(mark);
    }
  });
  for (const path of paths) {
    tableRow(rows,[path.speaker.name,path.wall?.name ?? "Direct",path.distance.toFixed(2)+" m",
      path.delay.toFixed(2)+" ms",Number.isFinite(path.db) ? (path.db-peak).toFixed(1)+" dB" : "Inaudible"]);
  }
  graphCursor = svgNode("line",{x1:left,x2:left,y1:9,y2:height-29,stroke:"#067765","stroke-width":2,visibility:"hidden"});
  graph.append(graphCursor);
  const direct = paths.filter(path => !path.wall);
  byId("arrival-summary").value = "First direct arrival: "+direct[0].delay.toFixed(2)+" ms. Direct spread: "+
    (direct.at(-1).delay-direct[0].delay).toFixed(2)+" ms. "+(paths.length-direct.length)+" reflected arrivals.";
}

function updateAcousticTools() {
  drawRoomWalls();
  updatePositionControls();
  const {objects, ...acousticSettings} = snapshot();
  const nextKey = JSON.stringify(acousticSettings);
  recordLayout();
  if (nextKey === acousticSettingsKey) return;
  acousticSettingsKey = nextKey;
  if (soundCheckRunning || audioContext) {
    stopSoundCheck(false);
    showStatus("Acoustic layout changed. Play again to hear the update.");
  }
  currentPaths = acousticPaths(state);
  byId("speed-readout").value = speedOfSound(state).toFixed(1)+" m/s";
  byId("start-button").disabled = !currentPaths.length;
  drawArrivalGraph(currentPaths);
  byId("ceiling-height").value = state.roomHeight;
  byId("listener-height").max = (state.roomHeight-0.1).toFixed(2);
  byId("listener-height").value = state.audio.listener.z ?? defaultElevation;
  drawCoverage();
  drawBassModes();
}

const pointOnPath = (path, progress) => {
  let remaining = path.distance*progress;
  for (let i=1; i<path.points.length; i++) {
    const a=path.points[i-1], b=path.points[i];
    const length=Math.hypot(b.x-a.x,b.y-a.y,b.z-a.z);
    if (length && remaining <= length) {
      return {x:a.x+(b.x-a.x)*remaining/length,y:a.y+(b.y-a.y)*remaining/length};
    }
    remaining -= length;
  }
  return path.points.at(-1);
};

function drawWavefronts(simulated, layout = auditionLayout || state) {
  const radius = speedOfSound(layout)*simulated/1000;
  if (radius < 0.02) return;
  const diagonal = Math.hypot(layout.width,layout.height);
  const fade = Math.max(0,1-radius/diagonal);
  if (!fade) return;
  const segmentCount = 36;
  for (const speaker of layout.audio.speakers) {
    // Draw a top-down slice. Use the middle band to shade the wavefront.
    const pointAt = angle => ({
      x:speaker.x+radius*Math.cos(angle),
      y:speaker.y+radius*Math.sin(angle)
    });
    for (let i=0;i<segmentCount;i++) {
      const start = i*2*Math.PI/segmentCount;
      const end = (i+1)*2*Math.PI/segmentCount;
      const middle = (start+end)/2;
      const a = pointAt(start), b = pointAt(end), sample = pointAt(middle);
      const angle = Math.abs(normalizeAngle(middle*180/Math.PI-speaker.angle));
      const directionLoss = Math.max(0,speakerBandLevel(speaker,1,0)-speakerBandLevel(speaker,1,angle));
      const transmissionLoss = layout.partitions.reduce((loss,wall) =>
        loss+(intersection(speaker,sample,wall) ? wall.loss : 0),0);
      const opacity = 0.68*fade*10**(-(directionLoss+transmissionLoss)/20);
      if (opacity < 0.015) continue;
      waveLayer.append(svgNode("path",{
        d:`M ${a.x*SCALE} ${a.y*SCALE} A ${radius*SCALE} ${radius*SCALE} 0 0 1 ${b.x*SCALE} ${b.y*SCALE}`,
        fill:"none",stroke:directColor,"stroke-width":2,"stroke-opacity":opacity,"data-wavefront":"direct"
      }));
    }
  }
}

let auditionSide = null;
let auditionLayout = null;

function showAuditionPlan(side, paths) {
  auditionLayout = comparisons[side];
  replay = paths;
  const banner = byId("audition-plan-banner");
  byId("audition-plan-label").textContent = "Listening preview · Layout "+side.toUpperCase();
  banner.hidden = false;
  svg.classList.add("is-audition-preview");
  svg.setAttribute("aria-label","Top-down preview of captured layout "+side.toUpperCase());
  renderPlan(auditionLayout);
  drawRoomWalls(auditionLayout);
  waveLayer.replaceChildren();
  drawArrivalGraph(paths);
  runStartedAt = performance.now();
}

function restoreWorkingPlan() {
  auditionLayout = null;
  byId("audition-plan-banner").hidden = true;
  svg.classList.remove("is-audition-preview");
  svg.setAttribute("aria-label","Room floor plan. Drag items or use the position controls below.");
  renderPlan(state);
  drawRoomWalls(state);
  drawArrivalGraph(currentPaths);
}

function updateAuditionControls() {
  for (const button of document.querySelectorAll("[data-listen]")) {
    const side = button.dataset.listen;
    button.disabled = !comparisons[side]?.audio.speakers.length;
    button.setAttribute("aria-pressed",String(auditionSide === side));
  }
  const available = ["a","b"].filter(side => comparisons[side]?.audio.speakers.length);
  let message = "Capture a layout with at least one speaker to enable its Listen button.";
  if (auditionSide) {
    message = "Listening to layout "+auditionSide.toUpperCase()+". The floor plan previews this capture until you stop.";
  } else if (available.length) {
    message = trackPlayback ? "Switch layouts without restarting the song." :
      "Listen starts the MP3 you added, or plays the room test pulse.";
  }
  for (const id of ["listen-help","comparison-listen-status"]) byId(id).textContent = message;
}

function listenToComparison(side) {
  if (!comparisons[side]?.audio.speakers.length) return;
  if (soundCheckRunning && auditionSide === side) {
    stopSoundCheck();
    return;
  }
  if (!trackPlayback) {
    if (soundCheckRunning) stopSoundCheck(false);
    byId("silent").checked = false;
    void startSoundCheck(side);
    return;
  }
  const context = audioContext;
  const paths = acousticPaths(comparisons[side]);
  const previous = trackPlayback.graph;
  const next = buildMusicGraph(context,decodedTrack,paths,previous.source);
  const now = context.currentTime;
  next.output.gain.setValueAtTime(0,now);
  next.output.gain.linearRampToValueAtTime(previewVolume(),now+0.025);
  previous.output.gain.cancelScheduledValues(now);
  previous.output.gain.setTargetAtTime(0,now,0.009);
  setTimeout(previous.disconnect,75);
  trackPlayback.graph = next;
  playbackGain = next.output;
  auditionSide = side;
  showAuditionPlan(side,paths);
  updateAuditionControls();
  showStatus("Listening to layout "+side.toUpperCase()+" at the same song position.");
}


let trackFile = null;
let decodedTrack = null;
let trackPlayback = null;
let playbackGain = null;
const previewVolume = () => Number(byId("preview-level").value)/100;

function setAudioError(message = "") {
  byId("audio-error").textContent = message;
  byId("audio-error").hidden = !message;
}

function trackTimestamp(seconds) {
  const whole = Math.floor(seconds);
  return Math.floor(whole/60)+":"+String(whole%60).padStart(2,"0");
}

function renderTrackProgress() {
  if (!trackPlayback) return;
  const {start,duration} = trackPlayback;
  const elapsed = clamp(audioContext.currentTime-start,0,duration);
  byId("track-progress").value = elapsed;
  const label = trackTimestamp(elapsed)+" / "+trackTimestamp(duration);
  if (byId("track-time").textContent !== label) byId("track-time").textContent = label;
}

function buildMusicGraph(context,buffer,paths,sharedSource = null) {
  const source = sharedSource || context.createBufferSource();
  if (!sharedSource) source.buffer = buffer;
  // Feed the same mono program to every simulated speaker. The room paths create stereo output.
  const mono = context.createGain();
  mono.channelCount = 1;
  mono.channelCountMode = "explicit";
  source.connect(mono);
  const output = context.createGain();
  output.gain.value = previewVolume();
  output.connect(context.destination);
  const limiter = context.createWaveShaper();
  limiter.curve = Float32Array.from({length:2049},(_,i) => {
    const sample = i/1024-1;
    const magnitude = Math.abs(sample);
    return magnitude <= 0.28 ? sample :
      Math.sign(sample)*(0.28+0.25*Math.tanh((magnitude-0.28)/0.25));
  });
  limiter.connect(output);
  const rate = context.sampleRate;
  const frames = Math.ceil(paths.at(-1).delay*rate/1000)+2;
  const gainFor = level => 10**((level-90)/20);
  const scale = 0.36;
  const filters = [[["lowpass",350]],[["highpass",350],["lowpass",2300]],[["highpass",2300]]];
  filters.forEach((bandFilters, band) => {
    let input = mono;
    for (const [type, frequency] of bandFilters) {
      const filter = context.createBiquadFilter();
      filter.type = type;
      filter.frequency.value = frequency;
      filter.Q.value = 0.707;
      input.connect(filter);
      input = filter;
    }
    const response = context.createBuffer(2,frames,rate);
    const left = response.getChannelData(0), right = response.getChannelData(1);
    for (const path of paths) {
      const index = Math.round(path.delay*rate/1000);
      const pan = (path.pan+1)*Math.PI/4;
      const gain = scale*gainFor(path.levels[band]);
      left[index] += gain*Math.cos(pan);
      right[index] += gain*Math.sin(pan);
    }
    const room = context.createConvolver();
    room.normalize = false;
    room.buffer = response;
    input.connect(room);
    room.connect(limiter);
  });
  return {source,output,disconnect:() => {
    source.disconnect(mono);
    output.disconnect();
  }};
}

async function playTrack(context, file, run, paths) {
  if (!decodedTrack) {
    const bytes = await file.arrayBuffer();
    if (run !== soundCheckRun) return;
    const buffer = await context.decodeAudioData(bytes);
    if (run !== soundCheckRun) return;
    decodedTrack = buffer;
  }
  const graph = buildMusicGraph(context,decodedTrack,paths);
  const {source,output} = graph;
  playbackGain = output;
  const start = context.currentTime+0.04;
  trackPlayback = {start,duration:decodedTrack.duration,graph};
  updateAuditionControls();
  byId("track-progress").max = decodedTrack.duration;
  byId("track-progress-wrap").hidden = false;
  renderTrackProgress();
  source.start(start);
  const tail = Math.max(220,paths.at(-1).delay+100);
  source.onended = () => setTimeout(() => {
    if (run !== soundCheckRun) return;
    stopSoundCheck(false);
    showStatus("Track finished.");
  },tail);
  showStatus(auditionSide ? "Playing layout "+auditionSide.toUpperCase()+"." : "Playing through the room layout.");
}

function setupAudioDeck() {
  byId("mp3-file").addEventListener("change",event => {
    const file = event.target.files[0];
    if (!file) return;
    if (!/\\.mp3$/i.test(file.name) && file.type !== "audio/mpeg") {
      event.target.value = "";
      setAudioError("Choose an MP3 file.");
      return;
    }
    if (!file.size || file.size > 30_000_000) {
      event.target.value = "";
      setAudioError("Choose a nonempty MP3 under 30 MB.");
      return;
    }
    if (soundCheckRunning) stopSoundCheck(false);
    setAudioError();
    trackFile = file;
    decodedTrack = null;
    byId("audio-name").textContent = file.name;
    byId("clear-audio").hidden = false;
    byId("track-progress-wrap").hidden = true;
    showStatus("MP3 added. Use Play sound waves to listen.");
  });
  byId("clear-audio").addEventListener("click",() => {
    if (soundCheckRunning) stopSoundCheck(false);
    setAudioError();
    trackFile = null;
    decodedTrack = null;
    byId("mp3-file").value = "";
    byId("audio-name").textContent = "Room test pulse";
    byId("clear-audio").hidden = true;
    byId("track-progress-wrap").hidden = true;
  });
  byId("preview-level").addEventListener("input",() => {
    byId("preview-value").value = byId("preview-level").value+"%";
    if (playbackGain && audioContext) {
      playbackGain.gain.setTargetAtTime(previewVolume(),audioContext.currentTime,0.025);
    }
  });
}

function updatePositionControls() {
  const select = byId("position-target");
  const previous = select.value;
  const entries = [
    ["listener","Listener"],
    ...state.audio.speakers.map(speaker => [speaker.id,speaker.name]),
    ...state.objects.map(item => [item.id,itemDefinition(item).name]),
    ...state.partitions.map((wall,i) => [wall.id,"Wall partition "+(i+1)])
  ];
  select.replaceChildren(...entries.map(([value,label]) => new Option(label,value)));
  select.value = entries.some(([id]) => id === state.selectedId) ? state.selectedId :
    entries.some(([id]) => id === previous) ? previous : "listener";
}

function setupPositionControls() {
  const target = byId("position-target");
  function selectTarget() {
    const wall = state.partitions.find(item => item.id === target.value);
    if (wall) selectPartition(wall);
    else state.selectedId = target.value;
  }
  target.addEventListener("change",() => { selectTarget(); render(); });
  for (const button of document.querySelectorAll("[data-nudge]")) {
    button.addEventListener("click",() => {
      selectTarget();
      const [dx,dy] = {left:[-0.1,0],right:[0.1,0],up:[0,-0.1],down:[0,0.1]}[button.dataset.nudge];
      const wall = selectedPartition();
      const furniture = state.objects.find(item => item.id === state.selectedId);
      if (wall) {
        movePartition(wall,{wall:{...wall},pointerX:0,pointerY:0},{x:dx,y:dy});
""",
"""
      } else if (furniture) {
        furniture.x += dx;
        furniture.y += dy;
        constrain(furniture);
      } else {
        const marker = state.selectedId === "listener" ? state.audio.listener :
          state.audio.speakers.find(speaker => speaker.id === state.selectedId);
        if (!marker) return;
        marker.x += dx;
        marker.y += dy;
        constrainAudioPositions();
      }
      render();
      showStatus(target.selectedOptions[0].textContent+" moved.");
    });
  }
}


function animateWaves(now) {
  if (!replay) return;
  const elapsed = now - runStartedAt;
  renderTrackProgress();
  const musicMode = trackFile && !byId("silent").checked;
  if (!replay.length) {
    if (elapsed < 1700 || musicMode) animationFrame = requestAnimationFrame(animateWaves);
    else stopSoundCheck(false);
    return;
  }
  if (elapsed >= 1700 && musicMode && graphCursor.getAttribute("x1") === String(graphScale.right)) {
    animationFrame = requestAnimationFrame(animateWaves);
    return;
  }
  const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
  const simulated = reduced ? graphScale.end : clamp(elapsed/1700*graphScale.end,0,graphScale.end);
  const x = graphScale.left + simulated/graphScale.end*(graphScale.right-graphScale.left);
  graphCursor.setAttribute("visibility","visible");
  graphCursor.setAttribute("x1",x);
  graphCursor.setAttribute("x2",x);
  waveLayer.replaceChildren();
  drawWavefronts(simulated);
  for (const path of replay) {
    const color = path.wall ? reflectionColor : directColor;
    const progress = path.delay ? Math.min(1,simulated/path.delay) : 1;
    const point = pointOnPath(path,progress);
    waveLayer.append(svgNode("polyline",{points:path.points.map(p => p.x*SCALE+","+p.y*SCALE).join(" "),
      fill:"none",stroke:color,"stroke-width":path.wall ? 1.3 : 2.6,"stroke-opacity":0.42,
      "stroke-dasharray":path.wall ? "5 4" : "none"}));
    if (simulated <= path.delay+2) {
      waveLayer.append(svgNode("circle",{cx:point.x*SCALE,cy:point.y*SCALE,r:path.wall ? 3 : 5,fill:color}));
    }
  }
  if (elapsed < 1700 || musicMode) {
    animationFrame = requestAnimationFrame(animateWaves);
  } else {
    stopSoundCheck(false);
    showStatus("Replay complete.");
  }
}

function buildPlayback(context, paths) {
  const rate = context.sampleRate;
  const pulseLength = Math.ceil(rate*0.055);
  const samples = Math.ceil(rate*paths.at(-1).delay/1000)+pulseLength+1;
  const result = context.createBuffer(2,samples,rate);
  const left = result.getChannelData(0), right = result.getChannelData(1);
  const peakDb = Math.max(...paths.flatMap(path => path.levels));
  const pulses = frequencies.map(frequency => Float32Array.from({length:pulseLength},(_,i) =>
    Math.sin(2*Math.PI*frequency*i/rate)*Math.sin(Math.PI*i/(pulseLength-1))**2));
  for (const path of paths) {
    const index = Math.round(rate*path.delay/1000);
    const pan = (path.pan+1)*Math.PI/4;
    const leftGain = Math.cos(pan), rightGain = Math.sin(pan);
    for (let band = 0; band < frequencies.length; band++) {
      const gain = 0.018*10**((path.levels[band]-Math.max(90,peakDb))/20);
      for (let i = 0; i < pulseLength; i++) {
        const sample = pulses[band][i]*gain;
        left[index+i] += sample*leftGain;
        right[index+i] += sample*rightGain;
      }
    }
  }
  let peak = 0;
  for (let i=0;i<samples;i++) peak = Math.max(peak,Math.abs(left[i]),Math.abs(right[i]));
  if (peak > 0.075) {
    for (let i=0;i<samples;i++) { left[i] *= 0.075/peak; right[i] *= 0.075/peak; }
  }
  return result;
}

async function startSoundCheck(side = null) {
  if (soundCheckRunning) { stopSoundCheck(); return; }
  const paths = side ? acousticPaths(comparisons[side]) : currentPaths;
  if (!paths.length) return;
  replay = currentPaths;
  auditionSide = side;
  updateAuditionControls();
  const run = ++soundCheckRun;
  setAudioError();
  soundCheckRunning = true;
  byId("start-button").disabled = false;
  byId("start-button").textContent = "Stop replay";
  if (side) showAuditionPlan(side,paths);
  else drawArrivalGraph(replay);
  runStartedAt = performance.now();
  animationFrame = requestAnimationFrame(animateWaves);
  showStatus("Replaying direct and reflected arrivals.");
  if (byId("silent").checked) return;
  try {
    const AudioContextType = window.AudioContext || window.webkitAudioContext;
    if (!AudioContextType) throw new Error("Web Audio is unavailable");
    const context = new AudioContextType();
    audioContext = context;
    await context.resume();
    if (run !== soundCheckRun) return;
    if (trackFile) {
      await playTrack(context,trackFile,run,paths);
      return;
    }
    const buffer = buildPlayback(context,paths);
    playbackGain = context.createGain();
    playbackGain.gain.value = previewVolume();
    playbackGain.connect(context.destination);
    for (const offset of [0.025,0.61,1.19]) {
      const source = context.createBufferSource();
      source.buffer = buffer;
      source.connect(playbackGain);
      source.start(context.currentTime+offset);
    }
  } catch (error) {
    if (run !== soundCheckRun) return;
    if (audioContext) void audioContext.close().catch(() => {});
    audioContext = null;
    if (trackFile) {
      stopSoundCheck(false);
      setAudioError("Could not play this MP3. Try another file or use the test pulse.");
    } else {
      showStatus("Visual replay only. "+error.message,true);
    }
  }
}

function stopSoundCheck(announce = true) {
  ++soundCheckRun;
  soundCheckRunning = false;
  replay = null;
  trackPlayback = null;
  auditionSide = null;
  playbackGain = null;
  updateAuditionControls();
  if (animationFrame !== null) cancelAnimationFrame(animationFrame);
  animationFrame = null;
  waveLayer.replaceChildren();
  if (auditionLayout) restoreWorkingPlan();
  if (audioContext) void audioContext.close().catch(() => {});
  audioContext = null;
  byId("start-button").disabled = !currentPaths.length;
  byId("start-button").textContent = "Play sound waves";
  if (announce) showStatus("Replay stopped.");
}

function compareMetrics(layout) {
  const paths = acousticPaths(layout);
  const direct = paths.filter(path => !path.wall);
  const reflected = paths.filter(path => path.wall);
  const sum = entries => entries.length ? 10*Math.log10(entries.reduce((s,p) => s+10**(p.db/10),0)) : null;
  return [
    direct[0]?.delay ?? null,
    direct.length ? direct.at(-1).delay-direct[0].delay : null,
    sum(direct), reflected[0]?.delay ?? null, sum(reflected)
  ];
}

function drawComparison() {
  updateAuditionControls();
  const body = byId("comparison");
  body.replaceChildren();
  const a = comparisons.a ? compareMetrics(comparisons.a) : [];
  const b = comparisons.b ? compareMetrics(comparisons.b) : [];
  const format = (value,unit) => Number.isFinite(value) ? value.toFixed(2)+" "+unit : "—";
  [["First direct arrival","ms"],["Direct arrivals spread","ms"],["Direct level estimate","dB"],
    ["First surface reflection","ms"],["Reflected level estimate","dB"]].forEach(([label,unit],i) => {
    const delta = Number.isFinite(a[i]) && Number.isFinite(b[i]) ? b[i]-a[i] : null;
    tableRow(body,[label,format(a[i],unit),format(b[i],unit),(delta>0 ? "+" : "")+format(delta,unit)]);
  });
}

function setupAcousticTools() {
  setupAudioDeck();
  setupPositionControls();
  setupProfiles();
  setupRoomAcoustics();
  byId("wall-material").replaceChildren(...Object.keys(materials).map(name => new Option(name,name)));
  byId("wall-select").addEventListener("change",event => {
    selectedWall = event.target.value;
    const partition = state.partitions.find(wall => wall.id === selectedWall);
    state.selectedId = partition?.id ?? null;
    refreshWallEditor();
    render();
  });
  byId("wall-material").addEventListener("change",event => {
    const wall = wallSettings();
    wall.material = event.target.value;
    if (materials[wall.material]) wall.absorption = [...materials[wall.material]];
    refreshWallEditor();
    render();
  });
  byId("air-temp").addEventListener("change",event => {
    const input = event.target;
    if (!input.validity.valid || !Number.isFinite(input.valueAsNumber)) {
      input.value = state.temperature;
      showStatus("Enter an air temperature from −10 to 45 °C.",true);
      return;
    }
    state.temperature = input.valueAsNumber;
    render();
  });
  byId("silent").addEventListener("change",() => {
    if (soundCheckRunning) stopSoundCheck();
  });
  byId("undo-layout").addEventListener("click",() => stepHistory(-1));
  byId("redo-layout").addEventListener("click",() => stepHistory(1));
  document.addEventListener("keydown",event => {
    if (!(event.ctrlKey || event.metaKey) || event.altKey ||
        event.target.closest("input,select,textarea,[contenteditable]")) return;
    const key = event.key.toLowerCase();
    if (key !== "z" && key !== "y") return;
    event.preventDefault();
    stepHistory(key === "y" || event.shiftKey ? 1 : -1);
  });
  const input = byId("import-file");
  byId("import-layout").addEventListener("click",() => input.click());
  input.addEventListener("change",async () => {
    const file = input.files[0];
    input.value = "";
    if (!file) return;
    try {
      if (file.size > 2_000_000) throw new Error("Choose a layout under 2 MB.");
      const layout = parseLayout(await file.text());
      applyLayout(layout);
      showStatus("Layout imported.");
    } catch (error) {
      showStatus("Could not import layout. "+error.message,true);
    }
  });
  byId("stop-preview").addEventListener("click",() => {
    stopSoundCheck();
    byId("start-button").focus();
  });
  for (const button of document.querySelectorAll("[data-listen]")) {
    button.addEventListener("click",() => {
      listenToComparison(button.dataset.listen);
      if (auditionLayout && !button.closest(".audio-deck, .preview-banner")) {
        const banner = byId("audition-plan-banner");
        banner.focus({preventScroll:true});
        banner.scrollIntoView({block:"start"});
      }
    });
  }
  for (const side of ["a","b"]) {
    byId("capture-"+side).addEventListener("click",() => {
      comparisons[side] = JSON.parse(JSON.stringify(snapshot()));
      byId("restore-"+side).disabled = false;
      drawComparison();
      showStatus("Captured layout "+side.toUpperCase()+".");
    });
    byId("restore-"+side).addEventListener("click",() => {
      applyLayout(structuredClone(comparisons[side]));
      showStatus("Loaded layout "+side.toUpperCase()+".");
    });
  }
  refreshWallEditor();
  updateHistoryButtons();
  drawComparison();
}

                setupAcousticTools();
                loadLayout();
                constrainAudioPositions();
                render();
              </script>
            </body>
            </html>\
""");

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: java Audio2DRoom.java [port]");
        }
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", Audio2DRoom::handleRequest);
        server.start();
        System.out.println("RoomSketch is available at http://localhost:" + port);
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/health".equals(path)) {
            send(exchange, 200, "text/plain; charset=utf-8", "ok");
        } else if ("/".equals(path) || "/index.html".equals(path)) {
            send(exchange, 200, "text/html; charset=utf-8", PAGE);
        } else {
            send(exchange, 404, "text/plain; charset=utf-8", "Not found");
        }
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, response.length);
        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
