//imports:
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

class Audio2DRoom {
    private static final String PAGE = """
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
              </style>
            </head>
            <body>
              <header class="topbar">
                <div class="brand"><span class="brand-mark" aria-hidden="true">⌂</span>RoomSketch</div>
                <div class="top-actions">
                  <button class="button" id="export-button" type="button">Export layout</button>
                  <button class="button primary" id="save-button" type="button">Save layout</button>
                </div>
              </header>
              <main class="layout">
                <section class="panel" aria-labelledby="furniture-heading">
                  <h1>Build your room</h1>
                  <p class="subheading">Drag a piece into the room. Move, rotate, and arrange it to fit.</p>
                  <h2>Sound positions</h2>
                  <div class="audio-legend">
                    <span><i class="legend-dot source-dot"></i>Speakers</span>
                    <span><i class="legend-dot listener-dot"></i>Listener</span>
                  </div>
                  <p class="subheading">Drag the markers to position speakers and listener.</p>
                  <h2>Speaker channels</h2>
                  <div class="palette" id="speaker-palette"></div>
                  <div class="divider"></div>
                  <h2 id="furniture-heading">Furniture &amp; fixtures</h2>
                  <div class="palette" id="palette"></div>
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
                <section class="workspace" aria-label="Room editor">
                  <div class="workspace-head">
                    <span class="workspace-title">ROOM PLAN</span>
                    <div class="room-size" aria-label="Room dimensions in meters">
                      <label for="room-width">Width</label><input id="room-width" type="number" min="3" max="12" step="0.5" value="6"><span class="unit">m</span>
                      <label for="room-height">Depth</label><input id="room-height" type="number" min="3" max="12" step="0.5" value="4"><span class="unit">m</span>
                    </div>
                  </div>
                  <div class="canvas-wrap" id="canvas-wrap">
                    <svg id="room" role="application" aria-label="Room floor plan. Drag furniture, speakers, and listener to move them.">
                      <defs>
                        <pattern id="grid" width="60" height="60" patternUnits="userSpaceOnUse">
                          <path d="M 60 0 L 0 0 0 60" fill="none" stroke="#d7dce5" stroke-width="1"/>
                          <path d="M 30 0 V 60 M 0 30 H 60" fill="none" stroke="#eceff4" stroke-width="1"/>
                        </pattern>
                        <clipPath id="room-clip"><rect x="0" y="0"/></clipPath>
                      </defs>
                      <rect id="floor" x="0" y="0" fill="#fff" stroke="#354156" stroke-width="5"/>
                      <rect id="grid-overlay" x="0" y="0" fill="url(#grid)" pointer-events="none"/>
                      <g id="waves" clip-path="url(#room-clip)" pointer-events="none"></g>
                      <g id="objects"></g>
                      <g id="audio-markers"></g>
                    </svg>
                  </div>
                  <div class="hint">Grid squares are 0.5 m · Select furniture or a speaker to edit it</div>
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
                    <div class="detail-actions" id="detail-actions" hidden>
                      <button class="button" id="rotate-button" type="button">Rotate 90°</button>
                      <button class="button" id="delete-button" type="button">Remove item</button>
                      <button class="button" id="remove-speaker-button" type="button" hidden>Remove speaker</button>
                    </div>
                  </div>
                  <div>
                    <div class="divider"></div>
                    <h2>Sound check</h2>
                    <button class="button primary" id="start-button" type="button" style="width: 100%">Start sound waves</button>
                    <div class="meter-row" aria-label="Estimated sound pressure level received">
                      <div class="meter" role="meter" aria-label="Estimated SPL at listener in decibels" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">
                        <div class="meter-fill" id="meter-fill"></div>
                      </div>
                      <div class="meter-labels" aria-hidden="true"><span>100 dB</span><span>50 dB</span><span>0 dB</span></div>
                      <div class="meter-reading">
                        <div class="meter-value" id="energy-value">— dB</div>
                        <div class="meter-caption">estimated SPL at listener</div>
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
                    </div>
                    <div class="timing" id="timing">Arrival time is estimated at 343 m/s; SPL is an estimate based on speaker sensitivity and input power.</div>
                    <div class="measurements" id="speaker-arrivals" aria-live="polite"></div>
                    <p class="acoustic-note">Estimated SPL uses nominal sensitivity, input power, distance, and a simplified direction adjustment. It is not a measurement; reflections, obstacles, room response, and real amplifier/speaker behavior are not modeled.</p>
                    <button class="button" id="add-speaker-button" type="button" style="width: 100%">Add speaker</button>
                    <div class="divider"></div>
                    <h2>Room summary</h2>
                    <div class="stat"><span>Floor area</span><strong id="area">24 m²</strong></div>
                    <div class="stat"><span>Furniture placed</span><strong id="item-count">0</strong></div>
                    <div class="status" id="status" role="status" aria-live="polite"></div>
                  </div>
                </aside>
              </main>
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
                const SOUND_SPEED = 343;
                const RUN_DURATION_MS = 4500;
                const PULSE_INTERVAL_MS = 900;
                let animationFrame = null;
                let runStartedAt = 0;
                let runTimer = null;
                let audioContext = null;
                let oscillator = null;
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

                function render() {
                  const width = state.width * SCALE;
                  const height = state.height * SCALE;
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

                  for (const item of state.objects) {
                    const definition = itemDefinition(item);
                    const size = dimensions(item);
                    const w = definition.width * SCALE;
                    const h = definition.height * SCALE;
                    const centerX = (item.x + size.width / 2) * SCALE;
                    const centerY = (item.y + size.height / 2) * SCALE;
                    const group = document.createElementNS(svgNS, "g");
                    group.setAttribute("class", `room-object${item.id === state.selectedId ? " selected" : ""}`);
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

                  for (const speaker of state.audio.speakers) {
                    const selected = state.selectedId === speaker.id;
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
                    const point = state.audio.listener;
                    const group = document.createElementNS(svgNS, "g");
                    group.setAttribute("class", `audio-marker${state.selectedId === "listener" ? " selected" : ""}`);
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

                  document.getElementById("area").textContent = `${formatNumber(state.width * state.height)} m²`;
                  document.getElementById("item-count").textContent = String(state.objects.length);
                  updateSelection();
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
                  const rotateButton = document.getElementById("rotate-button");
                  const deleteButton = document.getElementById("delete-button");
                  const removeSpeakerButton = document.getElementById("remove-speaker-button");
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
                  const measurements = state.audio.speakers.map(speaker => {
                    const dx = listener.x - speaker.x;
                    const dy = listener.y - speaker.y;
                    const distance = Math.hypot(dx, dy);
                    const bearing = Math.atan2(dy, dx) * 180 / Math.PI;
                    const difference = Math.abs(normalizeAngle(bearing - speaker.angle));
                    const outside = Math.max(0, difference - speaker.coverage / 2);
                    const directionLoss = outside / 30 * 6;
                    const referenceSpl = speaker.sensitivity + 10 * Math.log10(speaker.power) - directionLoss;
                    const spl = referenceSpl - 20 * Math.log10(Math.max(distance, 0.25));
                    return { speaker, distance, bearing, delay: distance / SOUND_SPEED * 1000, spl, referenceSpl };
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

                function animateWaves() {
                  if (!audioContext) return;
                  const elapsed = performance.now() - runStartedAt;
                  waveLayer.replaceChildren();
                  for (let pulseAt = 0; pulseAt <= elapsed; pulseAt += PULSE_INTERVAL_MS) {
                    const age = (elapsed - pulseAt) / 1000;
                    if (age > 1.3) continue;
                    for (const speaker of state.audio.speakers) {
                      const wave = document.createElementNS(svgNS, "circle");
                      wave.setAttribute("cx", speaker.x * SCALE);
                      wave.setAttribute("cy", speaker.y * SCALE);
                      wave.setAttribute("r", Math.max(1, age * Math.hypot(state.width, state.height) * SCALE / 1.3));
                      wave.setAttribute("fill", "none");
                      wave.setAttribute("stroke", "#e66848");
                      wave.setAttribute("stroke-width", "3");
                      wave.setAttribute("stroke-opacity", String(1 - age / 1.3));
                      waveLayer.append(wave);
                    }
                  }
                  animationFrame = requestAnimationFrame(animateWaves);
                }

                async function startSoundCheck() {
                  if (soundCheckRunning || audioContext) {
                    stopSoundCheck();
                    return;
                  }
                  const AudioContextType = window.AudioContext || window.webkitAudioContext;
                  if (!AudioContextType) {
                    showStatus("Audio playback is not supported by this browser.", true);
                    return;
                  }
                  const run = ++soundCheckRun;
                  try {
                    const context = new AudioContextType();
                    audioContext = context;
                    await context.resume();
                    if (run !== soundCheckRun) {
                      if (audioContext === context) {
                        audioContext = null;
                        void context.close().catch(error => showStatus(`Could not close audio: ${error.message}`, true));
                      }
                      return;
                    }
                    const tone = context.createOscillator();
                    const volume = context.createGain();
                    oscillator = tone;
                    tone.type = "sine";
                    tone.frequency.value = 440;
                    tone.connect(volume);
                    volume.connect(context.destination);
                    const now = context.currentTime;
                    volume.gain.setValueAtTime(0.0001, now);
                    for (let pulse = 0; pulse < RUN_DURATION_MS; pulse += PULSE_INTERVAL_MS) {
                      const start = now + pulse / 1000;
                      volume.gain.setValueAtTime(0.0001, start);
                      volume.gain.linearRampToValueAtTime(0.045, start + 0.025);
                      volume.gain.setValueAtTime(0.045, start + 0.16);
                      volume.gain.exponentialRampToValueAtTime(0.0001, start + 0.22);
                    }
                    tone.onended = () => {
                      if (oscillator === tone) oscillator = null;
                      if (audioContext === context) audioContext = null;
                      void context.close().catch(error => showStatus(`Could not close audio: ${error.message}`, true));
                    };
                    tone.start(now);
                    tone.stop(now + RUN_DURATION_MS / 1000);
                    soundCheckRunning = true;
                    document.getElementById("start-button").textContent = "Stop sound check";
                    showStatus("Playing a soft tone and illustrating sound waves.");
                    runStartedAt = performance.now();
                    animationFrame = requestAnimationFrame(animateWaves);
                    runTimer = setTimeout(() => stopSoundCheck(false), RUN_DURATION_MS);
                  } catch (error) {
                    if (audioContext) {
                      const failedContext = audioContext;
                      audioContext = null;
                      void failedContext.close().catch(closeError =>
                        showStatus(`Audio failed: ${error.message}; could not close audio: ${closeError.message}`, true));
                    }
                    document.getElementById("start-button").textContent = "Start sound waves";
                    showStatus(`Could not start audio: ${error.message}`, true);
                  }
                }

                function stopSoundCheck(announce = true) {
                  soundCheckRun++;
                  soundCheckRunning = false;
                  clearTimeout(runTimer);
                  if (animationFrame !== null) cancelAnimationFrame(animationFrame);
                  animationFrame = null;
                  waveLayer.replaceChildren();
                  if (oscillator) {
                    oscillator.stop();
                    oscillator = null;
                  } else if (audioContext) {
                    const context = audioContext;
                    audioContext = null;
                    void context.close().catch(error => showStatus(`Could not close audio: ${error.message}`, true));
                  }
                  document.getElementById("start-button").textContent = "Start sound waves";
                  if (announce) showStatus("Sound check stopped.");
                }

                function createId() {
                  return globalThis.crypto?.randomUUID
                    ? globalThis.crypto.randomUUID()
                    : `item-${Date.now()}-${Math.random().toString(16).slice(2)}`;
                }

                function addItem(type, x, y, custom = null) {
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
                    power: 1
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
                      angle, coverage, sensitivity, power
                    };
                  });
                  const listener = audio.listener || { x: Math.max(0.25, roomWidth - 1), y: roomHeight / 2 };
                  if (!Number.isFinite(listener.x) || !Number.isFinite(listener.y)) {
                    throw new Error("Saved listener position is invalid.");
                  }
                  return { width: roomWidth, height: roomHeight, objects, audio: { speakers: checkedSpeakers, listener } };
                }

                function saveLayout() {
                  try {
                    localStorage.setItem(STORAGE_KEY, JSON.stringify({
                      width: state.width, height: state.height, objects: state.objects, audio: state.audio
                    }));
                    showStatus("Layout saved in this browser.");
                  } catch (error) {
                    showStatus(`Could not save layout: ${error.message}`, true);
                  }
                }

                function exportLayout() {
                  const data = JSON.stringify({
                    width: state.width, height: state.height, objects: state.objects, audio: state.audio
                  }, null, 2);
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
                    const saved = parseLayout(raw);
                    state.width = saved.width;
                    state.height = saved.height;
                    state.objects = saved.objects;
                    state.audio = saved.audio;
                    for (const item of state.objects) constrain(item);
                    document.getElementById("room-width").value = state.width;
                    document.getElementById("room-height").value = state.height;
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

                for (const [type, definition] of Object.entries(catalog)) {
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
                  size.textContent = `${definition.width} × ${definition.height} m`;
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
                  const payload = event.dataTransfer.getData("text/plain");
                  const point = pointInRoom(event);
                  if (payload.startsWith("speaker:")) addSpeaker(payload.slice("speaker:".length), [point.x / state.width, point.y / state.height]);
                  else addItem(payload, point.x, point.y);
                });
                svg.addEventListener("pointerdown", event => {
                  const audioMarker = event.target.closest(".audio-marker");
                  if (audioMarker) {
                    const point = pointInRoom(event);
                    const kind = audioMarker.dataset.audio;
                    const speakerId = audioMarker.dataset.speaker;
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
                  if (state.drag?.pointerId === event.pointerId) state.drag = null;
                });
                svg.addEventListener("pointercancel", event => {
                  if (state.drag?.pointerId === event.pointerId) state.drag = null;
                });
                svg.addEventListener("lostpointercapture", event => {
                  if (state.drag?.pointerId === event.pointerId) state.drag = null;
                });
                document.getElementById("rotate-button").addEventListener("click", () => {
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
                      !["INPUT", "TEXTAREA"].includes(document.activeElement.tagName) && state.selectedId) {
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
                  state.width = width;
                  state.height = height;
                  for (const item of state.objects) constrain(item);
                  constrainAudioPositions();
                  showStatus("");
                  render();
                }
                document.getElementById("room-width").addEventListener("change", updateRoomSize);
                document.getElementById("room-height").addEventListener("change", updateRoomSize);
                document.getElementById("save-button").addEventListener("click", saveLayout);
                document.getElementById("export-button").addEventListener("click", exportLayout);
                document.getElementById("start-button").addEventListener("click", startSoundCheck);

                loadLayout();
                constrainAudioPositions();
                render();
              </script>
            </body>
            </html>
            """;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: java audio-2d-room.java [port]");
        }
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
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
