"""Concept sheet for The Usual — a diary of the cafés you keep coming back to.

Writes sheet.html next to this file: a rainy-evening café window (hero),
the notebook spread on a lamp-lit table, a cork board of sugar packets
under string lights, and three screen sketches.
"""
import math
import random
from pathlib import Path

INK = "#2a1f17"
NIGHT = "#1d1712"
PAGE = "#f1e3c4"
CREAM = "#fbf3e2"
RED = "#a8392b"
TEAL = "#3d7470"
MUSTARD = "#d39b3a"
GOLD = "#e7bf69"
AMBER = "#f2b25c"
HAND = "'Caveat', 'Segoe Print', cursive"
TYPE = "'Courier Prime', 'Courier New', monospace"
SIGN = "'Playfair Display', Georgia, serif"

DRINKS = {
    "эспрессо": "#3d2213",
    "фильтр": "#6b3b1f",
    "капучино": "#93603a",
    "флэт уайт": "#a5774b",
    "латте": "#c49c72",
    "раф": "#d4b688",
}

DEFS = f'''<defs>
  <filter id="wob" x="-10%" y="-10%" width="120%" height="120%">
    <feTurbulence type="fractalNoise" baseFrequency=".035" numOctaves="2" seed="3"/>
    <feDisplacementMap in="SourceGraphic" scale="3"/>
  </filter>
  <filter id="stain" x="-20%" y="-20%" width="140%" height="140%">
    <feTurbulence type="fractalNoise" baseFrequency=".06" numOctaves="2" seed="8"/>
    <feDisplacementMap in="SourceGraphic" scale="6"/>
  </filter>
  <filter id="blur4"><feGaussianBlur stdDeviation="4"/></filter>
  <filter id="blur10" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="10"/></filter>
  <filter id="blur24" x="-80%" y="-80%" width="260%" height="260%"><feGaussianBlur stdDeviation="24"/></filter>
  <filter id="shadow" x="-10%" y="-10%" width="130%" height="140%"><feDropShadow dx="6" dy="14" stdDeviation="12" flood-color="#000" flood-opacity=".45"/></filter>
  <filter id="soft" x="-10%" y="-10%" width="130%" height="140%"><feDropShadow dx="2" dy="5" stdDeviation="4" flood-color="#000" flood-opacity=".35"/></filter>
  <pattern id="awning" width="56" height="10" patternUnits="userSpaceOnUse">
    <rect width="28" height="10" fill="#7a2a22"/><rect x="28" width="28" height="10" fill="#e3cfa6"/>
  </pattern>
  <pattern id="awning-s" width="24" height="10" patternUnits="userSpaceOnUse">
    <rect width="12" height="10" fill="{RED}"/><rect x="12" width="12" height="10" fill="{CREAM}"/>
  </pattern>
  <pattern id="tape" width="8" height="8" patternUnits="userSpaceOnUse" patternTransform="rotate(45)">
    <rect width="8" height="8" fill="#9cc7bd"/><rect width="3" height="8" fill="#b9dbd2"/>
  </pattern>
  <pattern id="tape2" width="10" height="10" patternUnits="userSpaceOnUse">
    <rect width="10" height="10" fill="#e9b7a3"/><circle cx="5" cy="5" r="1.6" fill="#f7dccf"/>
  </pattern>
  <linearGradient id="gold" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#fbe6a6"/><stop offset=".5" stop-color="#d9a24a"/><stop offset=".55" stop-color="#b98232"/><stop offset="1" stop-color="#f1cf7c"/>
  </linearGradient>
  <radialGradient id="interior" cx=".45" cy=".25" r=".85">
    <stop offset="0" stop-color="#ffdd9c"/><stop offset=".45" stop-color="#e99a4c"/><stop offset="1" stop-color="#6e3418"/>
  </radialGradient>
  <radialGradient id="interior-off" cx=".45" cy=".3" r=".9">
    <stop offset="0" stop-color="#3e4555"/><stop offset="1" stop-color="#191b24"/>
  </radialGradient>
  <linearGradient id="fog" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#fff" stop-opacity="0"/><stop offset="1" stop-color="#fff" stop-opacity=".28"/>
  </linearGradient>
  <linearGradient id="street" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#121722"/><stop offset="1" stop-color="#231c1f"/>
  </linearGradient>
  <radialGradient id="vignette" cx=".38" cy=".3" r=".85">
    <stop offset="0" stop-color="#ffd99a" stop-opacity=".22"/><stop offset=".55" stop-color="#000" stop-opacity="0"/><stop offset="1" stop-color="#000" stop-opacity=".55"/>
  </radialGradient>
  <radialGradient id="lamp" cx=".5" cy=".5" r=".5">
    <stop offset="0" stop-color="#fff2c8" stop-opacity=".95"/><stop offset=".4" stop-color="#ffcf7a" stop-opacity=".45"/><stop offset="1" stop-color="#ffb04a" stop-opacity="0"/>
  </radialGradient>
  <radialGradient id="pagelight" cx=".35" cy=".25" r=".9">
    <stop offset="0" stop-color="#fff6dc"/><stop offset="1" stop-color="#e2cda4"/>
  </radialGradient>
  <linearGradient id="spine" x1="0" x2="1">
    <stop offset="0" stop-color="#000" stop-opacity="0"/><stop offset=".5" stop-color="#3a220f" stop-opacity=".35"/><stop offset="1" stop-color="#000" stop-opacity="0"/>
  </linearGradient>
</defs>'''


# ---------- small helpers ----------

def hand(x, y, text, size=20, color=INK, rot=0, anchor="start", weight=500, opacity=1):
    return (f'<text x="{x}" y="{y}" font-family="{HAND}" font-size="{size}" font-weight="{weight}" fill="{color}" opacity="{opacity}" '
            f'text-anchor="{anchor}" transform="rotate({rot} {x} {y})">{text}</text>')


def typed(x, y, text, size=12, color=INK, anchor="start", spacing=0, weight="normal"):
    return (f'<text x="{x}" y="{y}" font-family="{TYPE}" font-size="{size}" font-weight="{weight}" fill="{color}" '
            f'text-anchor="{anchor}" letter-spacing="{spacing}">{text}</text>')


def tape(x, y, w, h, rot, fill="url(#tape)"):
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{fill}" opacity=".82" transform="rotate({rot} {x + w / 2} {y + h / 2})"/>'


def ring(x, y, r, color, partial=False, strength=1.0):
    """Coffee-cup ring: darker rim (coffee-ring effect), faint wash inside."""
    out = (f'<circle cx="{x}" cy="{y}" r="{r}" fill="{color}" fill-opacity="{.08 * strength:.2f}" stroke="{color}" '
           f'stroke-opacity="{.6 * strength:.2f}" stroke-width="3.2"/>'
           f'<circle cx="{x + 1.5}" cy="{y - 1}" r="{r - 3}" fill="none" stroke="{color}" stroke-opacity="{.22 * strength:.2f}" stroke-width="1.4"/>')
    if partial:
        out += f'<path d="M{x - r - 4},{y + 6} A{r + 4},{r + 4} 0 0 1 {x + 6},{y - r - 4}" fill="none" stroke="{color}" stroke-opacity="{.35 * strength:.2f}" stroke-width="2"/>'
    return f'<g filter="url(#stain)">{out}</g>'


def stamp(x, y, text, rot=-8, color=RED, size=15):
    w = len(text) * size * .68 + 22
    return (f'<g transform="rotate({rot} {x} {y})" opacity=".8">'
            f'<rect x="{x - w / 2}" y="{y - size}" width="{w}" height="{size * 1.9}" rx="4" fill="none" stroke="{color}" stroke-width="2.4"/>'
            f'<text x="{x}" y="{y + size * .42}" text-anchor="middle" font-family="{TYPE}" font-weight="bold" font-size="{size}" letter-spacing="2" fill="{color}">{text}</text></g>')


def round_stamp(x, y, top, mid, rot=12, color=TEAL):
    return (f'<g transform="rotate({rot} {x} {y})" opacity=".8" filter="url(#wob)">'
            f'<circle cx="{x}" cy="{y}" r="40" fill="none" stroke="{color}" stroke-width="2.6"/>'
            f'<circle cx="{x}" cy="{y}" r="33" fill="none" stroke="{color}" stroke-width="1.2"/>'
            f'<text x="{x}" y="{y - 12}" text-anchor="middle" font-family="{TYPE}" font-size="9" letter-spacing="1.5" fill="{color}">{top}</text>'
            f'<text x="{x}" y="{y + 10}" text-anchor="middle" font-family="{TYPE}" font-weight="bold" font-size="19" letter-spacing="2" fill="{color}">{mid}</text>'
            f'<text x="{x}" y="{y + 25}" text-anchor="middle" font-family="{TYPE}" font-size="8" fill="{color}">★ ★ ★</text></g>')


def steam(x, y, h=60, opacity=.5, width=5, color="#fff"):
    return (f'<g filter="url(#blur4)" opacity="{opacity}">'
            f'<path d="M{x},{y} q-14,{-h * .25} 0,{-h * .5} q14,{-h * .25} 0,{-h * .5}" fill="none" stroke="{color}" stroke-width="{width}" stroke-linecap="round"/>'
            f'<path d="M{x + 18},{y + 6} q-10,{-h * .2} 0,{-h * .4} q10,{-h * .2} -2,{-h * .45}" fill="none" stroke="{color}" stroke-width="{width * .8}" stroke-linecap="round"/></g>')


# ---------- hero: the café window on a rainy evening ----------

def hero(uid="h", lights=True, sign=True):
    rnd = random.Random(11)
    clip = f"win{uid}"
    wx, wy, ww, wh = 130, 96, 860, 380
    inside_fill = "url(#interior)" if lights else "url(#interior-off)"
    sil = "#5a2c14" if lights else "#10121a"
    shelf = "#8d4a22" if lights else "#272b37"

    lamps = ""
    for lx in (300, 560, 820):
        glow = f'<circle cx="{lx}" cy="{wy + 92}" r="110" fill="url(#lamp)"/>' if lights else ""
        bulb = "#fff1c4" if lights else "#3a3f4c"
        lamps += (f'{glow}<path d="M{lx},{wy} L{lx},{wy + 62}" stroke="{sil}" stroke-width="2"/>'
                  f'<path d="M{lx - 26},{wy + 88} L{lx - 12},{wy + 62} L{lx + 12},{wy + 62} L{lx + 26},{wy + 88} Z" fill="{sil}"/>'
                  f'<ellipse cx="{lx}" cy="{wy + 90}" rx="12" ry="5" fill="{bulb}"/>')

    shelves = ""
    for sy in (wy + 140, wy + 196):
        shelves += f'<rect x="640" y="{sy}" width="340" height="6" fill="{shelf}"/>'
        x = 650
        while x < 970:
            kind = rnd.random()
            if kind < .4:
                h = rnd.randint(22, 36)
                shelves += f'<rect x="{x}" y="{sy - h}" width="18" height="{h}" rx="3" fill="{shelf}" opacity=".9"/>'
                x += 26
            elif kind < .75:
                shelves += f'<path d="M{x},{sy - 16} L{x + 18},{sy - 16} L{x + 15},{sy} L{x + 3},{sy} Z" fill="{shelf}"/>'
                x += 24
            else:
                shelves += f'<ellipse cx="{x + 12}" cy="{sy - 12}" rx="13" ry="12" fill="{shelf}"/>'
                x += 30

    counter = (f'<rect x="660" y="{wy + 250}" width="340" height="140" fill="{sil}" opacity=".85"/>'
               f'<rect x="660" y="{wy + 246}" width="340" height="8" fill="{shelf}"/>'
               f'<rect x="770" y="{wy + 196}" width="130" height="52" rx="6" fill="{sil}"/>'
               f'<rect x="784" y="{wy + 210}" width="20" height="14" fill="{shelf}"/><rect x="866" y="{wy + 210}" width="20" height="14" fill="{shelf}"/>')

    # someone reading by the window, back-lit
    person = f'''
  <path d="M190,{wy + 300} L470,{wy + 300}" stroke="{sil}" stroke-width="8"/>
  <path d="M220,{wy + 304} L214,{wy + 380} M440,{wy + 304} L446,{wy + 380}" stroke="{sil}" stroke-width="7"/>
  <circle cx="300" cy="{wy + 196}" r="24" fill="{sil}"/>
  <circle cx="316" cy="{wy + 176}" r="11" fill="{sil}"/>
  <path d="M258,{wy + 300} Q256,{wy + 236} 300,{wy + 226} Q344,{wy + 236} 342,{wy + 300} Z" fill="{sil}"/>
  <path d="M334,{wy + 258} Q356,{wy + 276} 356,{wy + 294}" stroke="{sil}" stroke-width="12" stroke-linecap="round" fill="none"/>
  <path d="M332,{wy + 299} Q347,{wy + 287} 364,{wy + 293} Q381,{wy + 287} 396,{wy + 299} Z" fill="{sil}"/>
  <path d="M338,{wy + 296} Q350,{wy + 290} 362,{wy + 295} M366,{wy + 295} Q378,{wy + 290} 390,{wy + 296}" stroke="{shelf}" stroke-width="1.5" fill="none" opacity=".7"/>
  <ellipse cx="432" cy="{wy + 297}" rx="20" ry="3.5" fill="{sil}"/>
  <path d="M419,{wy + 276} L445,{wy + 276} Q444,{wy + 295} 432,{wy + 295} Q420,{wy + 295} 419,{wy + 276} Z" fill="{sil}"/>
  <path d="M444,{wy + 280} q9,0 7,7 q-2,5 -9,4" stroke="{sil}" stroke-width="3.5" fill="none"/>
'''
    cup_steam = steam(428, wy + 268, 46, .55 if lights else 0, 4, "#fff6dd")
    plant = (f'<path d="M150,{wy + 380} L156,{wy + 330} L190,{wy + 330} L196,{wy + 380} Z" fill="{sil}"/>'
             + "".join(f'<path d="M173,{wy + 332} Q{173 + dx},{wy + 300 - abs(dx)} {173 + dx * 1.6},{wy + 262 - abs(dx) * .4}" stroke="{sil}" stroke-width="5" fill="none" stroke-linecap="round"/>'
                       f'<ellipse cx="{173 + dx * 1.6}" cy="{wy + 262 - abs(dx) * .4}" rx="11" ry="6" fill="{sil}" transform="rotate({dx * 2} {173 + dx * 1.6} {wy + 262 - abs(dx) * .4})"/>'
                       for dx in (-24, -10, 6, 20)))

    drops = ""
    for _ in range(90):
        x, y, r = rnd.uniform(wx, wx + ww), rnd.uniform(wy, wy + wh), rnd.uniform(1.2, 3.6)
        drops += (f'<circle cx="{x:.0f}" cy="{y:.0f}" r="{r:.1f}" fill="#fff" opacity=".22"/>'
                  f'<circle cx="{x - r * .3:.1f}" cy="{y - r * .3:.1f}" r="{r * .35:.1f}" fill="#fff" opacity=".55"/>')
    for _ in range(14):
        x, y = rnd.uniform(wx + 10, wx + ww - 10), rnd.uniform(wy + 20, wy + wh - 80)
        drops += f'<path d="M{x:.0f},{y:.0f} q2,{rnd.uniform(20, 50):.0f} -1,{rnd.uniform(40, 80):.0f}" stroke="#fff" stroke-width="2.2" opacity=".25" fill="none"/>'

    heart = (f'<path d="M820,{wy + 330} q-22,-22 -6,-34 q10,-6 16,6 q6,-12 16,-6 q16,12 -6,34 Z" fill="none" '
             f'stroke="{"#a65424" if lights else "#262a35"}" stroke-width="5" opacity=".7" stroke-linecap="round"/>')

    lettering = "" if not (lights and sign) else f'''
  <g filter="url(#soft)">
    <text x="560" y="{wy + 168}" text-anchor="middle" font-family="{SIGN}" font-style="italic" font-weight="900" font-size="108"
      fill="url(#gold)" stroke="#3a210e" stroke-width="2.5" paint-order="stroke">The Usual</text>
  </g>
  <path d="M420,{wy + 190} Q560,{wy + 206} 700,{wy + 190}" stroke="url(#gold)" stroke-width="3" fill="none"/>
  <text x="560" y="{wy + 220}" text-anchor="middle" font-family="{SIGN}" font-size="15" letter-spacing="6" fill="{GOLD}" stroke="#3a210e" stroke-width="3" paint-order="stroke">COFFEE · NOTES · REGULARS WELCOME</text>
'''
    closed_sign = "" if lights else f'''
  <path d="M520,{wy + 4} L560,{wy + 46} L600,{wy + 4}" stroke="#8b8f9c" stroke-width="2" fill="none"/>
  <g transform="rotate(-3 560 {wy + 78})"><rect x="490" y="{wy + 46}" width="140" height="62" rx="6" fill="#e9e1cf" stroke="{INK}" stroke-width="3"/>
  <text x="560" y="{wy + 88}" text-anchor="middle" font-family="{TYPE}" font-weight="bold" font-size="22" letter-spacing="3" fill="{INK}">ЗАКРЫТО</text></g>
  <text x="560" y="{wy + 168}" text-anchor="middle" font-family="{SIGN}" font-style="italic" font-weight="900" font-size="108" fill="#8a8f99" opacity="{.25 if sign else 0}">The Usual</text>
'''
    # chairs up on the tables when closed
    if not lights:
        person = (f'<path d="M190,{wy + 300} L470,{wy + 300}" stroke="{sil}" stroke-width="8"/>'
                  f'<path d="M220,{wy + 304} L214,{wy + 380} M440,{wy + 304} L446,{wy + 380}" stroke="{sil}" stroke-width="7"/>'
                  f'<path d="M250,{wy + 296} L250,{wy + 240} L300,{wy + 240} L300,{wy + 296} M250,{wy + 262} L300,{wy + 262} M250,{wy + 240} L246,{wy + 200} M300,{wy + 240} L304,{wy + 200}" stroke="{sil}" stroke-width="6" fill="none"/>'
                  f'<path d="M360,{wy + 296} L360,{wy + 240} L410,{wy + 240} L410,{wy + 296} M360,{wy + 262} L410,{wy + 262} M360,{wy + 240} L356,{wy + 200} M410,{wy + 240} L414,{wy + 200}" stroke="{sil}" stroke-width="6" fill="none"/>')

    rain = "".join(
        f'<path d="M{x:.0f},{y:.0f} l-6,22" stroke="#cfd8ea" stroke-width="1.2" opacity="{rnd.uniform(.12, .35):.2f}"/>'
        for x, y in ((rnd.uniform(0, 1120), rnd.uniform(0, 600)) for _ in range(260)))

    street_glow = "url(#lamp)"
    reflection = "" if not lights else (
        f'<g filter="url(#blur24)" opacity=".32"><rect x="{wx + 80}" y="552" width="{ww - 160}" height="44" rx="20" fill="{AMBER}"/></g>'
        + "".join(f'<path d="M{wx + rnd.uniform(0, 700):.0f},{rnd.uniform(556, 596):.0f} l{rnd.uniform(40, 140):.0f},0" stroke="#ffd79a" stroke-width="2" opacity=".35"/>' for _ in range(18)))

    cat = f'''
  <g fill="#0f0b09">
    <ellipse cx="912" cy="474" rx="26" ry="30"/>
    <circle cx="912" cy="436" r="17"/>
    <path d="M898,428 L900,410 L910,424 Z M926,428 L924,410 L914,424 Z"/>
    <path d="M936,494 Q968,496 960,470 Q956,458 964,452" stroke="#0f0b09" stroke-width="7" fill="none" stroke-linecap="round"/>
  </g>'''

    return f'''
  <rect width="1120" height="600" fill="url(#street)"/>
  <rect x="0" y="0" width="1120" height="540" fill="#2a1e18"/>
  {"".join(f'<path d="M{x},60 L{x},540" stroke="#1d1410" stroke-width="2"/>' for x in range(20, 1120, 38))}
  <circle cx="58" cy="150" r="130" fill="{street_glow}" opacity=".55"/>
  <path d="M58,600 L58,170" stroke="#121012" stroke-width="9"/>
  <path d="M40,170 L76,170 L68,148 L48,148 Z" fill="#121012"/><ellipse cx="58" cy="172" rx="14" ry="4" fill="#ffe7b0"/>
  <clipPath id="{clip}"><rect x="{wx}" y="{wy}" width="{ww}" height="{wh}"/></clipPath>
  <g clip-path="url(#{clip})">
    <rect x="{wx}" y="{wy}" width="{ww}" height="{wh}" fill="{inside_fill}"/>
    {shelves}{counter}{lamps}{plant}{person}{cup_steam}
    {lettering}{closed_sign}
    <rect x="{wx}" y="{wy + wh - 110}" width="{ww}" height="110" fill="url(#fog)"/>
    {heart}{drops}
  </g>
  <rect x="{wx - 14}" y="{wy - 14}" width="{ww + 28}" height="{wh + 28}" fill="none" stroke="#3d2a1e" stroke-width="22"/>
  <rect x="{wx}" y="{wy}" width="{ww}" height="{wh}" fill="none" stroke="#1a110c" stroke-width="3"/>
  <path d="M110,0 L1010,0 L1010,46 L110,46 Z" fill="url(#awning)"/>
  <path d="M110,46 {"".join(f"Q{110 + i * 56 + 28},70 {110 + (i + 1) * 56},46 " for i in range(16))}Z" fill="url(#awning)" stroke="#2a1510" stroke-width="2"/>
  <rect x="96" y="{wy + wh + 10}" width="928" height="16" fill="#4b3324"/>
  <rect x="96" y="{wy + wh + 24}" width="928" height="5" fill="#000" opacity=".35"/>
  {cat}
  {reflection}
  {rain}
'''


def hero_svg(lights=True, uid="h"):
    return f'<svg viewBox="0 0 1120 600" class="hero">{DEFS}{hero(uid, lights)}</svg>'


# ---------- notebook spread on a lamp-lit table ----------

def facade(x, y, s=1.0):
    """Ink sketch of the café front, like a doodle on a napkin."""
    sw = f"{2 / s:.2f}"
    L = f'stroke="{INK}" stroke-width="{sw}" fill="none" stroke-linecap="round" stroke-linejoin="round"'
    return f'''<g transform="translate({x},{y}) scale({s})" filter="url(#wob)">
  <path d="M20,40 L20,210 L260,210 L260,40" {L}/>
  <path d="M10,40 L270,40 L270,28 L10,28 Z" fill="{CREAM}" stroke="{INK}" stroke-width="{sw}"/>
  <text x="140" y="38.5" text-anchor="middle" font-family="{TYPE}" font-weight="bold" font-size="11" letter-spacing="4" fill="{INK}">ЗЕРНО и Ко</text>
  <path d="M14,52 L266,52 L262,78 {"".join(f"Q{262 - i * 22 - 11},88 {262 - (i + 1) * 22},78 " for i in range(11))}Z" fill="url(#awning-s)" stroke="{INK}" stroke-width="{sw}"/>
  <path d="M34,96 L150,96 L150,178 L34,178 Z" fill="#f6dfae" stroke="{INK}" stroke-width="{sw}"/>
  <path d="M92,96 L92,178 M34,134 L150,134" {L}/>
  <path d="M50,128 q8,-14 16,0 M56,114 a5,5 0 1 1 0.1,0" {L}/>
  <path d="M110,128 l0,-10 l16,0 l0,10 Z M126,120 q6,0 4,5 l-4,1" {L}/>
  <path d="M114,114 q-3,-5 1,-9 M120,114 q-3,-5 1,-9" {L}/>
  <path d="M182,96 L240,96 L240,210 L182,210 Z" fill="#c9a77d" stroke="{INK}" stroke-width="{sw}"/>
  <circle cx="230" cy="156" r="3" fill="{INK}"/>
  <path d="M192,108 L230,108 L230,140 L192,140 Z" {L}/>
  <path d="M40,210 L40,196 L60,196 L60,210 M50,196 q-12,-24 0,-34 q12,10 0,34 M50,190 q-14,-6 -12,-18 M50,184 q14,-4 12,-16" {L}/>
  <path d="M160,210 L160,186 M150,186 L172,186 M166,186 L166,210 M152,192 l0,18" {L}/>
  <path d="M8,214 L272,214" {L}/>
</g>'''


def lavender(x, y, rot):
    buds = "".join(f'<ellipse cx="{x + i * 1.5}" cy="{y - 40 - i * 9}" rx="4.2" ry="6" fill="#8b77a8" opacity=".9"/>'
                   f'<ellipse cx="{x + i * 1.5 + 5}" cy="{y - 44 - i * 9}" rx="3.4" ry="5" fill="#a08cbd" opacity=".9"/>' for i in range(7))
    return (f'<g transform="rotate({rot} {x} {y})"><path d="M{x},{y + 60} Q{x + 4},{y} {x + 10},{y - 100}" stroke="#7b8a5a" stroke-width="2.6" fill="none"/>'
            f'<path d="M{x + 2},{y + 10} q-16,-8 -20,-24 M{x + 4},{y - 10} q14,-6 18,-22" stroke="#7b8a5a" stroke-width="2" fill="none"/>{buds}</g>')


def receipt(x, y, rot):
    zig = "".join(f" L{x + 8 + i * 8},{y + 168 + (4 if i % 2 == 0 else 0)}" for i in range(13))
    return (f'<g transform="rotate({rot} {x + 55} {y + 80})" filter="url(#soft)">'
            f'<path d="M{x},{y} L{x + 110},{y} L{x + 110},{y + 168}{zig} L{x},{y + 168} Z" fill="#fdfaf2"/>'
            + typed(x + 55, y + 22, "ЗЕРНО и Ко", 11, INK, "middle", 1, "bold")
            + typed(x + 55, y + 38, "ул. Рылеева, 12", 8, "#6d5b45", "middle")
            + f'<path d="M{x + 10},{y + 48} L{x + 100},{y + 48}" stroke="{INK}" stroke-dasharray="2 3"/>'
            + typed(x + 10, y + 66, "флэт уайт", 9) + typed(x + 100, y + 66, "280", 9, INK, "end")
            + typed(x + 10, y + 82, "круассан", 9) + typed(x + 100, y + 82, "190", 9, INK, "end")
            + f'<path d="M{x + 10},{y + 92} L{x + 100},{y + 92}" stroke="{INK}" stroke-dasharray="2 3"/>'
            + typed(x + 10, y + 108, "ИТОГО", 9, INK, weight="bold") + typed(x + 100, y + 108, "470", 9, INK, "end", weight="bold")
            + typed(x + 55, y + 134, "спасибо, что", 8, "#6d5b45", "middle") + typed(x + 55, y + 146, "заходите :)", 8, "#6d5b45", "middle")
            + '</g>')


def notebook(rnd):
    left = f'''
  <rect x="0" y="0" width="560" height="600" fill="url(#pagelight)"/>
  <g transform="rotate(-2.5 200 160)" filter="url(#soft)">
    <rect x="46" y="44" width="320" height="250" fill="{CREAM}"/>
  </g>
  <g transform="rotate(-2.5 200 160)">
    {facade(66, 52, 1.0)}
    {hand(206, 284, "набросок с фото, 14.02", 16, "#7a6a55", 0, "middle")}
  </g>
  {tape(150, 26, 110, 26, -6)}
  {lavender(470, 300, 18)}
  {tape(440, 236, 70, 20, 30, "url(#tape2)")}
  {round_stamp(440, 104, "КОФЕЙНЯ", "СВОЯ", 14)}
  {hand(400, 172, "с 10-го визита", 18, TEAL, -4)}
  {typed(46, 340, "ЗЕРНО и Ко · ул. Рылеева, 12", 14, INK, spacing=1)}
  <path d="M46,350 L520,350" stroke="{INK}" stroke-width="1" stroke-dasharray="3 4"/>
  {hand(46, 394, "Как обычно?", 42, RED, -1, weight=700)}
  {hand(60, 434, "→ флэт уайт на овсяном, без сахара", 24)}
  {hand(60, 466, "→ место: у окна, слева от фикуса", 24)}
  {hand(60, 498, "→ бариста: Лёша (кот на руке)", 24)}
  {stamp(104, 550, "РОЗЕТКИ", -4, TEAL, 12)}
  {stamp(232, 554, "ТИХО ДО 11", 3, TEAL, 12)}
  {stamp(372, 548, "НОУТБУК ОК", -2, TEAL, 12)}
'''
    # the usual spot: rings pile up where the cup always lands
    pile = "".join(ring(860 + rnd.uniform(-46, 46), 200 + rnd.uniform(-40, 40), rnd.uniform(28, 36),
                        DRINKS["флэт уайт" if rnd.random() < .7 else rnd.choice(list(DRINKS))], rnd.random() < .4, rnd.uniform(.5, 1))
                   for _ in range(16))
    lone = [
        (650, 130, 32, "эспрессо", "12.03 · первый раз", 10, 60),
        (690, 300, 30, "капучино", "с Димой — спорили 3 часа", 50, 58),
        (1020, 330, 32, "раф", "день рождения ♡", -20, 60),
        (860, 410, 30, "латте", "Лёша нарисовал кота", 0, 60),
    ]
    rings, labels = "", ""
    for x, y, r, drink, note, dx, dy in lone:
        rings += ring(x, y, r, DRINKS[drink], partial=True)
        labels += hand(x + dx, y + dy, note, 19, "#5d4a36", rnd.uniform(-5, 4), "middle")
    right = f'''
  <rect x="560" y="0" width="560" height="600" fill="url(#pagelight)"/>
  {hand(600, 52, "27 визитов", 30, INK, -2, weight=700)}
  {pile}{rings}{labels}
  {hand(980, 286, "↖ моё место", 22, RED, -6, "middle")}
  {receipt(600, 440, -4)}
  {tape(626, 432, 60, 18, 8)}
  <g transform="rotate(10 1000 520)" filter="url(#soft)">{packet(950, 450, 104, 144, "#2f5d62", MUSTARD, "stripes", "ЗЕРНО", "и Ко")}</g>
  {tape(972, 436, 64, 20, 16, "url(#tape2)")}
  {hand(840, 572, "час у окна: 41 ч", 22, TEAL, -3, "middle")}
'''
    edges = "".join(f'<rect x="{-3 - i * 2}" y="{3 + i * 2.5}" width="{1126 + i * 4}" height="600" fill="{"#e5d3ae" if i % 2 else "#d8c39b"}"/>' for i in range(4, 0, -1))
    return f'''<g filter="url(#shadow)">{edges}</g>
  {left}{right}
  <rect x="520" y="0" width="80" height="600" fill="url(#spine)"/>'''


def wood(w, h, rnd, base="#5b3b26"):
    out = f'<rect width="{w}" height="{h}" fill="{base}"/>'
    for py in range(0, h, 130):
        shade = rnd.choice(["#5f3e28", "#56371f", "#634129", "#523420"])
        out += f'<rect x="0" y="{py}" width="{w}" height="130" fill="{shade}"/><path d="M0,{py} L{w},{py}" stroke="#2c1a0f" stroke-width="3"/>'
        for _ in range(9):
            gy = py + rnd.uniform(8, 122)
            amp = rnd.uniform(2, 6)
            d = f"M0,{gy:.0f} " + " ".join(f"Q{x + 60},{gy + (amp if (x // 120) % 2 else -amp):.0f} {x + 120},{gy:.0f}" for x in range(0, w, 120))
            out += f'<path d="{d}" stroke="#2c1a0f" stroke-width="{rnd.uniform(.6, 1.6):.1f}" opacity="{rnd.uniform(.2, .45):.2f}" fill="none"/>'
    return out


def cup(cx, cy, s=1.0):
    rosetta = "".join(f'<path d="M{cx - (26 - i * 3) * s},{cy + (14 - i * 5) * s} Q{cx},{cy + (24 - i * 5) * s} {cx + (26 - i * 3) * s},{cy + (14 - i * 5) * s}" '
                      f'stroke="#f7ecd8" stroke-width="{4 * s}" fill="none" stroke-linecap="round"/>' for i in range(7))
    return f'''<g filter="url(#shadow)">
  <circle cx="{cx}" cy="{cy}" r="{100 * s}" fill="#f4ede0"/>
  <circle cx="{cx}" cy="{cy}" r="{86 * s}" fill="none" stroke="#e1d6c2" stroke-width="{3 * s}"/>
  <rect x="{cx + 58 * s}" y="{cy - 12 * s}" width="{48 * s}" height="{24 * s}" rx="{12 * s}" fill="#efe6d6" stroke="#d6cab5" stroke-width="2"/>
  <circle cx="{cx}" cy="{cy}" r="{66 * s}" fill="#fbf7ef" stroke="#d9cdb8" stroke-width="2"/>
  <circle cx="{cx}" cy="{cy}" r="{55 * s}" fill="#b07c4c"/>
  <circle cx="{cx}" cy="{cy}" r="{55 * s}" fill="none" stroke="#7f522c" stroke-width="{5 * s}" opacity=".6"/>
  {rosetta}
  <path d="M{cx},{cy - 26 * s} L{cx},{cy + 30 * s}" stroke="#f7ecd8" stroke-width="{3 * s}" stroke-linecap="round"/>
  <path d="M{cx - 90 * s},{cy + 70 * s} L{cx - 20 * s},{cy + 20 * s}" stroke="#c9c2b6" stroke-width="{7 * s}" stroke-linecap="round"/>
  <ellipse cx="{cx - 14 * s}" cy="{cy + 15 * s}" rx="{13 * s}" ry="{9 * s}" fill="#d8d1c5" transform="rotate(-35 {cx - 14 * s} {cy + 15 * s})"/>
</g>'''


def croissant(x, y, rot):
    segs = ""
    for i, (dx, dy, rx, ry, a) in enumerate(((-58, 18, 22, 16, -40), (-32, -2, 26, 22, -20), (0, -8, 30, 26, 0), (32, -2, 26, 22, 20), (58, 18, 22, 16, 40))):
        segs += (f'<ellipse cx="{x + dx}" cy="{y + dy}" rx="{rx}" ry="{ry}" fill="{"#d18d3e" if i % 2 else "#e2a552"}" stroke="#8e5520" stroke-width="2" '
                 f'transform="rotate({a} {x + dx} {y + dy})"/>')
    crumbs = "".join(f'<circle cx="{x + dx}" cy="{y + dy}" r="{r}" fill="#d9a356"/>' for dx, dy, r in ((-80, 50, 3), (20, 46, 2.4), (70, 52, 2), (-30, 58, 2.6), (96, 30, 2)))
    return f'<g transform="rotate({rot} {x} {y})" filter="url(#shadow)">{segs}</g>{crumbs}'


def pencil(x, y, length, rot):
    return (f'<g transform="rotate({rot} {x} {y})" filter="url(#soft)">'
            f'<rect x="{x}" y="{y}" width="{length}" height="14" fill="#e3b23c"/>'
            f'<rect x="{x}" y="{y + 5}" width="{length}" height="4" fill="#cf9d2c"/>'
            f'<path d="M{x + length},{y} L{x + length + 30},{y + 7} L{x + length},{y + 14} Z" fill="#e8c9a0"/>'
            f'<path d="M{x + length + 20},{y + 4.6} L{x + length + 30},{y + 7} L{x + length + 20},{y + 9.4} Z" fill="{INK}"/>'
            f'<rect x="{x - 18}" y="{y}" width="18" height="14" fill="#b8b4ad"/><rect x="{x - 32}" y="{y}" width="14" height="14" rx="3" fill="#e38c8c"/></g>')


def table_scene():
    rnd = random.Random(7)
    sugar = "".join(f'<circle cx="{1120 + rnd.uniform(-40, 40):.0f}" cy="{720 + rnd.uniform(-14, 14):.0f}" r="1.6" fill="#fffaf0"/>' for _ in range(40))
    return f'''<svg viewBox="0 0 1240 800" class="table">{DEFS}
  {wood(1240, 800, rnd)}
  {croissant(150, 720, -12)}
  <g transform="translate(60,100) rotate(-1.2 560 300)">{notebook(rnd)}</g>
  {pencil(700, 640, 260, -8)}
  <g transform="rotate(-22 1100 690)" filter="url(#soft)">{packet(1050, 610, 96, 132, RED, CREAM, "dots", "ПЕНКА", "у моста")}</g>
  {sugar}
  {cup(1112, 128)}
  {steam(1100, 90, 120, .5, 7)}
  <rect width="1240" height="800" fill="url(#vignette)"/>
</svg>'''


# ---------- sugar packets ----------

def zigzag(x, y, w, teeth, up):
    step = w / teeth
    d = ""
    for i in range(teeth + 1):
        px = x + i * step
        d += f" L{px:.1f},{y}" + (f" L{px + step / 2:.1f},{y + (-4 if up else 4)}" if i < teeth else "")
    return d


_clip_seq = [0]


def packet(x, y, w, h, bg, accent, pattern, name, sub, city=None, closed=False, ghost=False):
    body = f"M{x},{y + 6}" + zigzag(x, y + 6, w, 11, True) + f" L{x + w},{y + h - 6}" + zigzag(x + w, y + h - 6, -w, 11, False) + " Z"
    _clip_seq[0] += 1
    clip_id = f"pk{_clip_seq[0]}"
    pat = ""
    if pattern == "stripes":
        pat = "".join(f'<rect x="{x + i * 14}" y="{y}" width="7" height="{h}" fill="{accent}" opacity=".9"/>' for i in range(0, w // 14 + 1))
    elif pattern == "dots":
        pat = "".join(f'<circle cx="{x + 10 + (i % 6) * 18 + (9 if (i // 6) % 2 else 0)}" cy="{y + 16 + (i // 6) * 18}" r="3.2" fill="{accent}"/>'
                      for i in range(6 * int(h / 18)))
    elif pattern == "chevron":
        pat = "".join(f'<path d="M{x - 10},{y + 30 + i * 22} l{w / 2 + 10},-14 l{w / 2 + 10},14" fill="none" stroke="{accent}" stroke-width="6"/>' for i in range(int(h / 22)))
    elif pattern == "band":
        pat = f'<rect x="{x}" y="{y + h * .55}" width="{w}" height="{h * .2}" fill="{accent}"/>'
    elif pattern == "sun":
        pat = "".join(f'<path d="M{x + w / 2},{y + h * .42} L{x + w / 2 + 160 * math.cos(a / 8 * math.pi):.1f},{y + h * .42 + 160 * math.sin(a / 8 * math.pi):.1f}" stroke="{accent}" stroke-width="7"/>' for a in range(16))
    pat = "" if ghost else f'<g clip-path="url(#{clip_id})">{pat}</g>'
    cx, cy = x + w / 2, y + h * .42
    label = "" if ghost else (
        f'<circle cx="{cx}" cy="{cy}" r="{w * .3}" fill="{CREAM}" stroke="{INK}" stroke-width="1.6"/>'
        f'<text x="{cx}" y="{cy + 1}" text-anchor="middle" font-family="{TYPE}" font-weight="bold" font-size="{w * .12:.0f}" fill="{INK}" letter-spacing="1">{name}</text>'
        f'<text x="{cx}" y="{cy + w * .13:.0f}" text-anchor="middle" font-family="{HAND}" font-size="{w * .14:.0f}" fill="{RED}">{sub}</text>'
        f'<rect x="{x + 10}" y="{y + h - 32}" width="{w - 20}" height="16" fill="{CREAM}" opacity=".92"/>'
        f'<text x="{cx}" y="{y + h - 20}" text-anchor="middle" font-family="{TYPE}" font-size="{w * .075:.0f}" fill="{INK}" letter-spacing="1.5">САХАР · 5 г</text>')
    ghost_txt = "" if not ghost else (
        f'<text x="{cx}" y="{cy - 4}" text-anchor="middle" font-family="{HAND}" font-size="{w * .2:.0f}" font-weight="700" fill="{CREAM}">{name}</text>'
        f'<text x="{cx}" y="{cy + 22}" text-anchor="middle" font-family="{HAND}" font-size="{w * .15:.0f}" fill="{CREAM}" opacity=".85">{sub}</text>')
    city_stamp = "" if not city else (
        f'<g transform="rotate(-14 {x + w - 22} {y + 30})" opacity=".9"><circle cx="{x + w - 22}" cy="{y + 30}" r="19" fill="{CREAM}" stroke="{RED}" stroke-width="2"/>'
        f'<text x="{x + w - 22}" y="{y + 34}" text-anchor="middle" font-family="{TYPE}" font-weight="bold" font-size="10" fill="{RED}">{city}</text></g>')
    stroke = f'stroke="{CREAM if ghost else INK}" stroke-width="{1.8 if ghost else 2}" stroke-dasharray="{"6 5" if ghost else "none"}"'
    out = (f'<clipPath id="{clip_id}"><path d="{body}"/></clipPath>'
           f'<path d="{body}" fill="{"none" if ghost else bg}" {stroke}/>{pat}{label}{ghost_txt}{city_stamp}')
    if closed:
        out = (f'<g style="filter:grayscale(1) contrast(.9)">{out}</g>'
               f'<path d="M{x - 6},{y + 40} L{x + 40},{y - 6} L{x + 58},{y - 6} L{x - 6},{y + 58} Z" fill="{INK}"/>')
    return out


PACKETS = [
    ("#2f5d62", MUSTARD, "stripes", "ЗЕРНО", "и Ко", None, False, False, "своя · 27"),
    (RED, CREAM, "dots", "ПЕНКА", "у моста", None, False, False, "8 визитов"),
    ("#4b3a6b", "#e0b5d0", "chevron", "СОВА", "до 2 ночи", None, False, False, "для работы"),
    (MUSTARD, "#f3e2a8", "sun", "ЛУЧ", "бранчи", None, False, False, "3 визита"),
    ("#1f3a5a", "#d24c36", "band", "ПРИЧАЛ", "кофе", "LIS", False, False, "Лиссабон"),
    ("#6c8c4a", "#e6efc9", "stripes", "ФИКУС", "на углу", "TBS", False, False, "Тбилиси"),
    ("#874a2b", "#f0c99a", "dots", "КОРИЦА", "и ваниль", None, True, False, "2019–2026"),
    (None, None, None, "Подвал", "от Ани", None, False, True, "хочу зайти"),
]


def cork_board():
    rnd = random.Random(4)
    W, H = 1240, 470
    cork = "".join(f'<circle cx="{rnd.uniform(0, W):.0f}" cy="{rnd.uniform(0, H):.0f}" r="{rnd.uniform(.8, 2.6):.1f}" '
                   f'fill="{rnd.choice(["#8a5c32", "#c99a66", "#7a4f2a", "#d6ab78"])}" opacity=".7"/>' for _ in range(1800))
    # string lights hanging in a gentle catenary
    wire, bulbs = "M20,24", ""
    pts = []
    for i in range(0, 25):
        x = 20 + i * 50
        y = 24 + 34 * math.sin(math.pi * ((x - 20) % 400) / 400)
        pts.append((x, y))
        wire += f" L{x},{y:.1f}"
    for i, (x, y) in enumerate(pts[1:-1]):
        c = ["#ffd27a", "#ffbe6b", "#ffe2a0"][i % 3]
        bulbs += (f'<circle cx="{x}" cy="{y + 14:.1f}" r="26" fill="{c}" opacity=".35" filter="url(#blur10)"/>'
                  f'<rect x="{x - 3}" y="{y:.1f}" width="6" height="6" fill="#2a2420"/>'
                  f'<ellipse cx="{x}" cy="{y + 12:.1f}" rx="5" ry="8" fill="{c}"/><ellipse cx="{x - 1.5}" cy="{y + 9:.1f}" rx="1.6" ry="3" fill="#fff"/>')
    items = ""
    for i, (bg, ac, pat, name, sub, city, closed, ghost, cap) in enumerate(PACKETS):
        px = 46 + i * 146
        rot = [-4, 3, -2, 5, -3, 2, -5, 3][i]
        pin = ["#c4453a", "#3d7470", "#d39b3a", "#c4453a", "#3d7470", "#d39b3a", "#555", "#c4453a"][i]
        items += (f'<g transform="rotate({rot} {px + 60} {200})" filter="url(#soft)">{packet(px, 110, 120, 170, bg, ac, pat, name, sub, city, closed, ghost)}</g>'
                  f'<circle cx="{px + 60}" cy="116" r="8" fill="{pin}" stroke="#000" stroke-opacity=".3"/><circle cx="{px + 57}" cy="113" r="2.6" fill="#fff" opacity=".7"/>'
                  f'<g transform="rotate({-rot / 2} {px + 60} 340)" filter="url(#soft)"><rect x="{px + 8}" y="314" width="104" height="38" fill="#f4e6c8"/>'
                  f'{hand(px + 60, 341, cap, 22, INK, 0, "middle")}</g>'
                  f'<circle cx="{px + 60}" cy="318" r="4.5" fill="#8a8a8a"/>')
    return f'''<svg viewBox="0 0 {W} {H}" class="board">{DEFS}
  <rect width="{W}" height="{H}" fill="#b4834f"/>{cork}
  {items}
  <path d="{wire}" stroke="#2a2420" stroke-width="2" fill="none"/>{bulbs}
  <rect width="{W}" height="{H}" fill="url(#vignette)"/>
  <rect x="0" y="0" width="{W}" height="{H}" fill="none" stroke="#3b2618" stroke-width="22"/>
</svg>'''


# ---------- screens ----------

def phone(inner, uid):
    return f'''<figure class="ph"><svg viewBox="0 0 300 600" class="phone">{DEFS}
  <rect x="2" y="2" width="296" height="596" rx="34" fill="#120d0a"/>
  <clipPath id="scr{uid}"><rect x="12" y="12" width="276" height="576" rx="26"/></clipPath>
  <g clip-path="url(#scr{uid})">{inner}</g>
  <rect x="120" y="20" width="60" height="8" rx="4" fill="#120d0a"/>
</svg></figure>'''


def qr(x, y, size, seed):
    rnd = random.Random(seed)
    n, c = 21, size / 21
    cells = "".join(f'<rect x="{x + i * c:.1f}" y="{y + j * c:.1f}" width="{c + .3:.1f}" height="{c + .3:.1f}" fill="{INK}"/>'
                    for i in range(n) for j in range(n)
                    if not ((i < 8 and j < 8) or (i > 12 and j < 8) or (i < 8 and j > 12)) and rnd.random() < .5)
    eyes = "".join(f'<rect x="{x + a * c}" y="{y + b * c}" width="{7 * c}" height="{7 * c}" fill="none" stroke="{INK}" stroke-width="{c}"/>'
                   f'<rect x="{x + (a + 2) * c}" y="{y + (b + 2) * c}" width="{3 * c}" height="{3 * c}" fill="{INK}"/>'
                   for a, b in ((0.5, 0.5), (13.5, 0.5), (0.5, 13.5)))
    return f'<rect x="{x - 8}" y="{y - 8}" width="{size + 16}" height="{size + 16}" rx="6" fill="{CREAM}"/>{cells}{eyes}'


def button(x, y, w, text, primary=True, dark=False):
    if primary:
        bg, fg, st = (AMBER, NIGHT, AMBER) if dark else (INK, CREAM, INK)
    else:
        bg, fg, st = ("none", CREAM, CREAM) if dark else ("none", INK, INK)
    return (f'<rect x="{x}" y="{y}" width="{w}" height="44" rx="22" fill="{bg}" stroke="{st}" stroke-width="2"/>'
            f'<text x="{x + w / 2}" y="{y + 28}" text-anchor="middle" font-family="{TYPE}" font-size="14" font-weight="bold" fill="{fg}">{text}</text>')


def window_crop(x, y, w, h, vb, lights, uid):
    return f'<svg x="{x}" y="{y}" width="{w}" height="{h}" viewBox="{vb}" preserveAspectRatio="xMidYMid slice">{hero(uid, lights, sign=False)}</svg>'


def screens():
    s1 = f'''
  <rect x="0" y="0" width="300" height="600" fill="{NIGHT}"/>
  {window_crop(12, 12, 276, 300, "150 90 470 510", True, "p1")}
  <rect x="12" y="240" width="276" height="80" fill="url(#fade1)"/>
  <linearGradient id="fade1" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{NIGHT}" stop-opacity="0"/><stop offset="1" stop-color="{NIGHT}"/></linearGradient>
  {typed(150, 332, "ЗЕРНО и Ко · визит 28", 12, "#c8b08a", "middle", 1)}
  {hand(150, 386, "Как обычно?", 52, AMBER, -2, "middle", 700)}
  {hand(150, 426, "флэт уайт на овсяном", 26, CREAM, 0, "middle")}
  {hand(150, 454, "у окна, слева от фикуса", 21, "#c8b08a", 0, "middle")}
  {button(36, 482, 228, "Да, как обычно", True, True)}
  {button(36, 536, 228, "Сегодня другое", False, True)}
'''
    rnd = random.Random(3)
    s2 = f'''
  <rect x="0" y="0" width="300" height="600" fill="#5b3b26"/>
  {wood(300, 600, rnd)}
  <circle cx="60" cy="40" r="200" fill="url(#lamp)" opacity=".45"/>
  {hand(150, 74, "Пакетик для Ани", 36, CREAM, -1, "middle", 700)}
  <g transform="rotate(-6 150 190)" filter="url(#soft)">{packet(92, 100, 116, 160, "#2f5d62", MUSTARD, "stripes", "ЗЕРНО", "и Ко")}</g>
  <g transform="rotate(3 150 320)" filter="url(#soft)"><rect x="34" y="284" width="232" height="78" fill="#fff3c2"/>
  {hand(46, 312, "Возьми флэт уайт и садись", 20)}{hand(46, 340, "на подоконник. Спроси Лёшу :)", 19)}</g>
  {tape(120, 274, 60, 18, -4, "url(#tape2)")}
  {qr(94, 386, 112, 5)}
  {typed(150, 530, "отсканируй — ляжет в «Хочу зайти»", 10.5, "#f1dfbd", "middle")}
  {button(36, 544, 228, "Отправить пакетик", True, True)}
  <rect width="300" height="600" fill="url(#vignette)"/>
'''
    s3 = f'''
  <rect x="0" y="0" width="300" height="600" fill="#15161c"/>
  {window_crop(12, 12, 276, 280, "170 80 760 520", False, "p3")}
  <rect x="12" y="220" width="276" height="72" fill="url(#fade3)"/>
  <linearGradient id="fade3" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#15161c" stop-opacity="0"/><stop offset="1" stop-color="#15161c"/></linearGradient>
  {hand(150, 330, "Корица и ваниль", 34, "#d9d4ca", 0, "middle", 700)}
  {hand(150, 364, "2019 — 2026", 26, "#a9a59d", 0, "middle")}
  {typed(150, 394, "143 визита · 61 ч у окна", 12, "#8d8a84", "middle", 1)}
  <g style="filter:grayscale(1)" opacity=".55">
    {ring(78, 444, 24, "#d8d0c4", True)}{ring(126, 462, 20, "#d8d0c4")}{ring(184, 440, 26, "#d8d0c4")}{ring(232, 466, 20, "#d8d0c4", True)}
  </g>
  {hand(150, 530, "Спасибо за тихие утра", 27, "#e8e1d4", -1, "middle")}
  {hand(150, 560, "и за корицу на пенке.", 27, "#e8e1d4", -1, "middle")}
'''
    return phone(s1, "a") + phone(s2, "b") + phone(s3, "c")


HTML = f'''<!doctype html>
<html lang="ru"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>The Usual — эскиз</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Caveat:wght@500;700&family=Courier+Prime:wght@400;700&family=Playfair+Display:ital,wght@0,400;1,900&display=swap" rel="stylesheet">
<style>
  :root {{ --night:{NIGHT}; --cream:{CREAM}; --amber:{AMBER}; --gold:{GOLD}; }}
  * {{ box-sizing:border-box; }}
  body {{ margin:0; background:radial-gradient(ellipse at 30% 0%, #3a2618 0%, var(--night) 60%) fixed; color:#e9dcc3;
    font-family:Georgia,serif; }}
  main {{ max-width:1240px; margin:0 auto; padding:32px 24px 72px; }}
  .hero, .table, .board {{ display:block; width:100%; height:auto; border-radius:6px; box-shadow:0 24px 60px rgba(0,0,0,.55); }}
  .intro {{ text-align:center; margin:28px auto 8px; max-width:760px; }}
  .intro h1 {{ margin:0; font-family:"Playfair Display",Georgia,serif; font-style:italic; font-weight:900; font-size:56px; color:var(--gold); }}
  .intro p {{ margin:8px 0 0; font-size:18px; line-height:1.5; opacity:.85; }}
  h2 {{ font-family:"Courier Prime","Courier New",monospace; font-size:16px; letter-spacing:4px; text-transform:uppercase; color:var(--amber); margin:56px 0 6px; }}
  .lead {{ margin:0 0 18px; opacity:.8; line-height:1.5; }}
  .phones {{ display:grid; grid-template-columns:repeat(3,1fr); gap:28px; }}
  .ph {{ margin:0; }}
  .phone {{ display:block; width:100%; max-width:320px; margin:0 auto; height:auto; filter:drop-shadow(0 18px 30px rgba(0,0,0,.6)); }}
  @media (max-width:900px) {{ .phones {{ grid-template-columns:1fr; }} }}
  @media (max-width:520px) {{ main {{ padding:16px; }} .intro h1 {{ font-size:40px; }} }}
</style></head>
<body><main>
{hero_svg()}
<div class="intro"><h1>The Usual</h1><p>Дневник кофеен, куда ты возвращаешься. Вечер, дождь за окном, твоё место у окна — и бариста, который уже знает твой заказ.</p></div>

<h2>Разворот на столе</h2>
<p class="lead">Набросок фасада, «как обычно», метки. Справа кольца: каждая чашка оставляет след, и у «моего места» они ложатся друг на друга. Чек, веточка лаванды, пакетик — всё, что хочется вклеить.</p>
{table_scene()}

<h2>Пробковая доска</h2>
<p class="lead">Каждая новая кофейня — свой сахарный пакетик. Штамп города из поездок, траурный уголок у закрывшихся, пунктир — пакетик от друга, где ты ещё не был.</p>
{cork_board()}

<h2>Экраны</h2>
<p class="lead">Вечерняя встреча у постоянной кофейни · пакетик-рекомендация другу · окно, в котором погас свет.</p>
<div class="phones">{screens()}</div>
</main></body></html>
'''

if __name__ == "__main__":
    out = Path(__file__).with_name("sheet.html")
    out.write_text(HTML, encoding="utf-8")
    print(out)
