import os
import math
from PIL import Image, ImageDraw, ImageFont

BASE = '/data/workspace/screen-share-app/android2/app/src/main/res'
ASSETS = '/data/workspace/screen-share-app/android2/app/src/main/assets'

BG = (13, 17, 23)
CYAN = (0, 212, 255)
DARK = (22, 27, 34)
CYAN_DARK = (0, 168, 204)
WHITE = (255, 255, 255)

def draw_screen_icon(draw, cx, cy, size, color=CYAN):
    sw = int(size * 0.50)
    sh = int(size * 0.36)
    sx1, sy1 = cx - sw // 2, cy - sh // 2 - int(size * 0.06)
    sx2, sy2 = cx + sw // 2, cy + sh // 2 - int(size * 0.06)
    border = max(2, size // 30)
    draw.rectangle([sx1, sy1, sx2, sy2], outline=color, width=border)
    inner = border + max(2, size // 40)
    draw.rectangle([sx1 + inner, sy1 + inner, sx2 - inner, sy2 - inner], fill=DARK)
    sw2 = int(size * 0.12)
    sh2 = int(size * 0.08)
    draw.rectangle([cx - sw2 // 2, sy2, cx + sw2 // 2, sy2 + sh2], fill=color)
    bw = int(size * 0.20)
    bh = max(2, size // 28)
    draw.rectangle([cx - bw // 2, sy2 + sh2, cx + bw // 2, sy2 + sh2 + bh], fill=color)
    ay = cy - int(size * 0.04)
    asz = int(size * 0.08)
    for i in range(3):
        r = asz * (i + 1)
        draw.arc([cx - r, ay - r, cx + r, ay + r], 225, 315, fill=color, width=max(1, size // 40))
    dr = max(1, size // 40)
    draw.ellipse([cx - dr, ay - dr, cx + dr, ay + dr], fill=color)

def create_large_splash(w, h):
    img = Image.new('RGB', (w, h), BG)
    draw = ImageDraw.Draw(img)
    for y in range(h):
        ratio = y / h
        r = int(BG[0] * (1 - ratio * 0.3) + DARK[0] * ratio * 0.3)
        g = int(BG[1] * (1 - ratio * 0.3) + DARK[1] * ratio * 0.3)
        b = int(BG[2] * (1 - ratio * 0.3) + DARK[2] * ratio * 0.3)
        draw.line([(0, y), (w, y)], fill=(r, g, b))
    for i in range(8):
        x = int(w * (0.1 + 0.8 * (i % 4) / 3))
        y_pos = int(h * (0.15 + 0.7 * (i // 4)))
        r = int(min(w, h) * (0.05 + 0.03 * (i % 3)))
        draw.ellipse([x - r, y_pos - r, x + r, y_pos + r], outline=(26, 80, 139), width=2)
    icon_size = int(min(w, h) * 0.25)
    cx, cy = w // 2, int(h * 0.35)
    for i in range(5):
        glow_r = icon_size // 2 + i * 15
        draw.ellipse([cx - glow_r, cy - glow_r, cx + glow_r, cy + glow_r],
                     outline=(0, 212 // (i + 1), 255 // (i + 1)), width=3)
    draw_screen_icon(draw, cx, cy, icon_size, CYAN)
    text_y = int(h * 0.55)
    try:
        font_large = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", int(min(w, h) * 0.08))
        font_small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", int(min(w, h) * 0.035))
        font_version = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", int(min(w, h) * 0.028))
    except:
        font_large = ImageFont.load_default()
        font_small = ImageFont.load_default()
        font_version = ImageFont.load_default()
    text = "ScreenShare"
    bbox = draw.textbbox((0, 0), text, font=font_large)
    tw = bbox[2] - bbox[0]
    draw.text(((w - tw) // 2, text_y), text, fill=CYAN, font=font_large)
    sub = "Wireless Display + Touch + Keyboard"
    bbox2 = draw.textbbox((0, 0), sub, font=font_small)
    tw2 = bbox2[2] - bbox2[0]
    draw.text(((w - tw2) // 2, text_y + int(min(w, h) * 0.10)), sub, fill=(136, 136, 136), font=font_small)
    ver = "v3.2.0"
    bbox3 = draw.textbbox((0, 0), ver, font=font_version)
    tw3 = bbox3[2] - bbox3[0]
    draw.text(((w - tw3) // 2, text_y + int(min(w, h) * 0.15)), ver, fill=(100, 100, 100), font=font_version)
    dev = "by Jalal | @x16_96"
    bbox4 = draw.textbbox((0, 0), dev, font=font_version)
    tw4 = bbox4[2] - bbox4[0]
    draw.text(((w - tw4) // 2, int(h * 0.78)), dev, fill=(0, 212, 255), font=font_version)
    line_y = int(h * 0.85)
    line_w = int(w * 0.4)
    draw.line([(w // 2 - line_w // 2, line_y), (w // 2 + line_w // 2, line_y)], fill=CYAN, width=2)
    return img

def create_large_logo(size):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    margin = size // 20
    draw.ellipse([margin, margin, size - margin, size - margin], fill=BG)
    ring = size // 20
    draw.ellipse([margin, margin, size - margin, size - margin], outline=CYAN, width=ring)
    inner_margin = margin + ring + size // 30
    draw.ellipse([inner_margin, inner_margin, size - inner_margin, size - inner_margin],
                 outline=(26, 80, 139), width=max(1, size // 80))
    draw_screen_icon(draw, size // 2, size // 2 - size // 12, int(size * 0.42), CYAN)
    return img

# Generate splash backgrounds
for name, w, h in [
    ('splash_land', 1920, 1080),
    ('splash_port', 1080, 1920),
    ('splash_tablet', 2048, 1536),
]:
    print(f"Generating {name} ({w}x{h})...")
    img = create_large_splash(w, h)
    path = f'{BASE}/drawable-xxxhdpi/{name}.png'
    img.save(path, 'PNG', optimize=False)
    fsize = os.path.getsize(path)
    print(f"  Saved: {fsize:,} bytes")

# Generate large launcher icons
for sz_name, sz in [('icon512', 512), ('icon384', 384), ('icon256', 256)]:
    print(f"Generating {sz_name} ({sz}x{sz})...")
    img = create_large_logo(sz)
    path = f'{BASE}/drawable-xxxhdpi/ic_launcher_{sz_name}.png'
    img.save(path, 'PNG', optimize=False)
    fsize = os.path.getsize(path)
    print(f"  Saved: {fsize:,} bytes")

# Generate feature card backgrounds
for i, title in enumerate(['Screen Mirror', 'Touch Control', 'Full Keyboard', 'Clipboard']):
    print(f"Generating feature_{i}...")
    img = Image.new('RGB', (800, 600), DARK)
    draw = ImageDraw.Draw(img)
    for y in range(600):
        ratio = y / 600
        c = tuple(int(BG[j] * (1 - ratio * 0.5) + DARK[j] * ratio * 0.5) for j in range(3))
        draw.line([(0, y), (800, y)], fill=c)
    draw.rectangle([0, 0, 799, 599], outline=CYAN, width=3)
    corner = 75
    draw.line([(0, 0), (corner, 0)], fill=CYAN, width=5)
    draw.line([(0, 0), (0, corner)], fill=CYAN, width=5)
    draw.line([(725, 599), (799, 599)], fill=CYAN, width=5)
    draw.line([(799, 525), (799, 599)], fill=CYAN, width=5)
    path = f'{BASE}/drawable-xxxhdpi/feature_{i}.png'
    img.save(path, 'PNG', optimize=False)
    fsize = os.path.getsize(path)
    print(f"  Saved: {fsize:,} bytes")

# Generate pattern background
print("Generating pattern background...")
pattern = Image.new('RGB', (2048, 2048), BG)
draw = ImageDraw.Draw(pattern)
for x in range(0, 2048, 64):
    draw.line([(x, 0), (x, 2048)], fill=(20, 25, 32), width=1)
for y in range(0, 2048, 64):
    draw.line([(0, y), (2048, y)], fill=(20, 25, 32), width=1)
for i in range(-2048, 4096, 256):
    draw.line([(i, 0), (i + 2048, 2048)], fill=(26, 80, 139), width=1)
for i in range(12):
    x = int(2048 * (0.1 + 0.8 * (i % 4) / 3))
    y_pos = int(2048 * (0.1 + 0.8 * (i // 4)))
    r = 80 + (i % 3) * 40
    draw.ellipse([x - r, y_pos - r, x + r, y_pos + r], outline=(26, 80, 139), width=2)
path = f'{BASE}/drawable-xxxhdpi/pattern_bg.png'
pattern.save(path, 'PNG', optimize=False)
fsize = os.path.getsize(path)
print(f"  Saved: {fsize:,} bytes")

# Generate about background
print("Generating about background...")
about = Image.new('RGB', (1920, 1200), BG)
draw = ImageDraw.Draw(about)
for y in range(1200):
    ratio = y / 1200
    r = int(13 + ratio * 10)
    g = int(17 + ratio * 8)
    b = int(23 + ratio * 12)
    draw.line([(0, y), (1920, y)], fill=(r, g, b))
for i in range(6):
    cx = int(1920 * (0.15 + 0.7 * (i % 3) / 2))
    cy = int(1200 * (0.2 + 0.6 * (i // 3)))
    r = 100 + i * 30
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(26, 80, 139), width=3)
draw_screen_icon(draw, 960, 400, 400, CYAN)
path = f'{BASE}/drawable-xxxhdpi/about_bg.png'
about.save(path, 'PNG', optimize=False)
fsize = os.path.getsize(path)
print(f"  Saved: {fsize:,} bytes")

# Generate tutorial background
print("Generating tutorial background...")
tutorial = Image.new('RGB', (1920, 1400), BG)
draw = ImageDraw.Draw(tutorial)
for y in range(1400):
    ratio = y / 1400
    c = tuple(int(BG[j] * (1 - ratio * 0.4) + DARK[j] * ratio * 0.4) for j in range(3))
    draw.line([(0, y), (1920, y)], fill=c)
for i in range(15):
    x = int(1920 * (0.05 + 0.9 * (i % 5) / 4))
    y_pos = int(1400 * (0.1 + 0.8 * (i // 5)))
    r = 60 + (i % 4) * 25
    draw.ellipse([x - r, y_pos - r, x + r, y_pos + r], outline=(26, 80, 139), width=2)
path = f'{BASE}/drawable-xxxhdpi/tutorial_bg.png'
tutorial.save(path, 'PNG', optimize=False)
fsize = os.path.getsize(path)
print(f"  Saved: {fsize:,} bytes")

# Total size check
total = 0
for root, dirs, files in os.walk(BASE):
    for f in files:
        if f.endswith('.png'):
            total += os.path.getsize(os.path.join(root, f))
print(f"\nTotal PNG resource size: {total:,} bytes ({total / 1024 / 1024:.1f} MB)")

assets_total = 0
for root, dirs, files in os.walk(ASSETS):
    for f in files:
        assets_total += os.path.getsize(os.path.join(root, f))
print(f"Total assets size: {assets_total:,} bytes ({assets_total / 1024:.1f} KB)")
print(f"Grand total: {(total + assets_total) / 1024 / 1024:.1f} MB")
