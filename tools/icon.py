"""Draws the README logo, the launcher icon's flat white from above on a dark table, as docs/logo.png.

python tools/icon.py
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
S = 1024
K = 4  # supersampling


def main():
    n = S * K
    u = n / 108
    im = Image.new("RGBA", (n, n), (0x3A, 0x26, 0x18, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, n, 36 * u], fill=(0x4A, 0x30, 0x20))
    d.rectangle([0, 72 * u, n, n], fill=(0x42, 0x2A, 0x1B))
    for y in (35, 71):
        d.rectangle([0, y * u, n, (y + 1.5) * u], fill=(0x2C, 0x1A, 0x0F))

    # Lamplight falling on the table.
    glow = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse([14 * u, 10 * u, 94 * u, 90 * u], fill=(0xFF, 0xD0, 0x8A, 70))
    im = Image.alpha_composite(im, glow.filter(ImageFilter.GaussianBlur(14 * u)))

    def circle(cx, cy, r, **kw):
        d.ellipse([(cx - r) * u, (cy - r) * u, (cx + r) * u, (cy + r) * u], **kw)

    shadow = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).ellipse([29 * u, 31 * u, 83 * u, 85 * u], fill=(0, 0, 0, 120))
    im = Image.alpha_composite(im, shadow.filter(ImageFilter.GaussianBlur(3 * u)))
    d = ImageDraw.Draw(im)
    circle(83, 74, 11, outline=(0xA5, 0x77, 0x4B, 160), width=int(2.2 * u))
    circle(54, 54, 26, fill=(0xF4, 0xED, 0xE0))
    d.rounded_rectangle([68 * u, 50.5 * u, 83.5 * u, 57.5 * u], radius=3.5 * u, fill=(0xEF, 0xE6, 0xD6))
    circle(54, 54, 17, fill=(0xFB, 0xF7, 0xEF))
    circle(54, 54, 14, fill=(0xB0, 0x7C, 0x4C))
    milk = (0xF7, 0xEC, 0xD8)
    for i in range(4):
        w = 6.5 - i
        y = 57.5 - i * 3
        d.arc([(54 - w) * u, (y - 3) * u, (54 + w) * u, (y + 3) * u], 20, 160, fill=milk, width=int(1.4 * u))
    d.line([54 * u, 46 * u, 54 * u, 61 * u], fill=milk, width=int(1.4 * u))

    mask = Image.new("L", (n, n), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, n - 1, n - 1], radius=int(n * 0.22), fill=255)
    out = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    out.paste(im, (0, 0), mask)
    out = out.resize((S, S), Image.LANCZOS)
    (ROOT / "docs").mkdir(exist_ok=True)
    out.save(ROOT / "docs/logo.png")


if __name__ == "__main__":
    main()
