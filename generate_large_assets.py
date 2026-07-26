import os
import random
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

BASE = '/data/workspace/screen-share-app/android2/app/src/main/res'
ASSETS = '/data/workspace/screen-share-app/android2/app/src/main/assets'

BG = (13, 17, 23)
CYAN = (0, 212, 255)
DARK = (22, 27, 34)

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

def create_complex_splash(w, h, seed=42):
    """Create a detailed, complex splash that doesn't compress well."""
    random.seed(seed)
    img = Image.new('RGB', (w, h), BG)
    draw = ImageDraw.Draw(img)
    
    # Complex gradient with noise
    for y in range(h):
        ratio = y / h
        for x in range(0, w, 4):  # Draw in small strips for texture
            noise = random.randint(-3, 3)
            r = max(0, min(255, int(13 + ratio * 12 + noise)))
            g = max(0, min(255, int(17 + ratio * 10 + noise)))
            b = max(0, min(255, int(23 + ratio * 15 + noise)))
            draw.line([(x, y), (min(x + 4, w), y)], fill=(r, g, b))
    
    # Starfield-like dots
    for _ in range(200):
        x = random.randint(0, w - 1)
        y = random.randint(0, h - 1)
        brightness = random.randint(30, 80)
        draw.point((x, y), fill=(brightness, brightness + 10, brightness + 20))
    
    # Grid lines with slight noise
    for x in range(0, w, 32):
        offset = random.randint(-1, 1)
        draw.line([(x + offset, 0), (x + offset, h)], fill=(20, 25, 32), width=1)
    for y in range(0, h, 32):
        offset = random.randint(-1, 1)
        draw.line([(0, y + offset), (w, y + offset)], fill=(20, 25, 32), width=1)
    
    # Diagonal accent lines
    for i in range(-w, w * 2, 128):
        c_val = random.randint(15, 35)
        draw.line([(i, 0), (i + h, h)], fill=(c_val, c_val + 10, c_val + 20), width=1)
    
    # Decorative circles with noise
    for i in range(20):
        cx = random.randint(0, w)
        cy = random.randint(0, h)
        r = random.randint(20, min(w, h) // 4)
        c = random.randint(15, 40)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(c, c + 15, c + 30), width=2)
        # Inner ring
        r2 = r - random.randint(5, 15)
        if r2 > 5:
            draw.ellipse([cx - r2, cy - r2, cx + r2, cy + r2], outline=(c + 5, c + 20, c + 35), width=1)
    
    # Main icon glow
    icon_size = int(min(w, h) * 0.22)
    cx_icon, cy_icon = w // 2, int(h * 0.33)
    
    # Glow layers
    for i in range(20):
        glow_r = icon_size // 2 + i * 20
        c = max(10, 212 - i * 10)
        draw.ellipse([cx_icon - glow_r, cy_icon - glow_r, cx_icon + glow_r, cy_icon + glow_r],
                     outline=(0, c, min(255, c + 40)), width=2)
    
    draw_screen_icon(draw, cx_icon, cy_icon, icon_size, CYAN)
    
    # Text
    try:
        font_large = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", int(min(w, h) * 0.07))
        font_small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", int(min(w, h) * 0.03))
        font_ver = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", int(min(w, h) * 0.025))
    except:
        font_large = font_small = font_ver = ImageFont.load_default()
    
    text_y = int(h * 0.56)
    for text, font, color, dy in [
        ("ScreenShare", font_large, CYAN, 0),
        ("Wireless Display + Touch + Keyboard", font_small, (136, 136, 136), int(min(w,h)*0.09)),
        ("v3.2.0", font_ver, (100, 100, 100), int(min(w,h)*0.14)),
        ("by Jalal | @x16_96", font_ver, CYAN, int(h*0.78 - text_y)),
    ]:
        bbox = draw.textbbox((0, 0), text, font=font)
        tw = bbox[2] - bbox[0]
        draw.text(((w - tw) // 2, text_y + dy), text, fill=color, font=font)
    
    # Decorative lines
    line_w = int(w * 0.35)
    ly = int(h * 0.88)
    draw.line([(w // 2 - line_w, ly), (w // 2 + line_w, ly)], fill=CYAN, width=2)
    draw.line([(w // 2 - line_w + 20, ly + 10), (w // 2 + line_w - 20, ly + 10)], fill=(26, 80, 139), width=1)
    
    return img

def create_complex_pattern(w, h, seed=123):
    """Create a complex pattern that doesn't compress well."""
    random.seed(seed)
    img = Image.new('RGB', (w, h), BG)
    draw = ImageDraw.Draw(img)
    
    # Gradient with noise
    for y in range(h):
        ratio = y / h
        for x in range(0, w, 2):
            n = random.randint(-2, 2)
            c = tuple(max(0, min(255, int(BG[i] * (1 - ratio * 0.5) + DARK[i] * ratio * 0.5 + n))) for i in range(3))
            draw.line([(x, y), (min(x + 2, w), y)], fill=c)
    
    # Dense grid
    for x in range(0, w, 16):
        draw.line([(x, 0), (x, h)], fill=(18, 22, 28), width=1)
    for y in range(0, h, 16):
        draw.line([(0, y), (w, y)], fill=(18, 22, 28), width=1)
    
    # Diagonal lines
    for i in range(-w, w * 2, 64):
        draw.line([(i, 0), (i + h, h)], fill=(26, 80, 139), width=1)
        draw.line([(i + 32, 0), (i + 32 + h, h)], fill=(15, 40, 70), width=1)
    
    # Random geometric shapes
    for _ in range(50):
        x = random.randint(0, w)
        y = random.randint(0, h)
        size = random.randint(10, 100)
        shape = random.choice(['circle', 'rect', 'line'])
        c = random.choice([(26, 80, 139), (0, 212, 255), (13, 17, 23), (22, 27, 34)])
        if shape == 'circle':
            draw.ellipse([x - size, y - size, x + size, y + size], outline=c, width=random.randint(1, 3))
        elif shape == 'rect':
            draw.rectangle([x, y, x + size, y + size], outline=c, width=random.randint(1, 2))
        else:
            angle = random.uniform(0, 2 * math.pi)
            x2 = int(x + size * math.cos(angle))
            y2 = int(y + size * math.sin(angle))
            draw.line([(x, y), (x2, y2)], fill=c, width=random.randint(1, 3))
    
    # More random dots for texture
    for _ in range(500):
        x = random.randint(0, w - 1)
        y = random.randint(0, h - 1)
        b = random.randint(15, 50)
        draw.point((x, y), fill=(b, b + 5, b + 10))
    
    return img

# Generate large JPEG splash images (JPEG won't compress as well)
print("Generating 4K splash images...")
for name, w, h, seed in [
    ('splash_4k_land', 3840, 2160, 42),
    ('splash_4k_port', 2160, 3840, 99),
]:
    img = create_complex_splash(w, h, seed)
    path = f'{BASE}/drawable-xxxhdpi/{name}.jpg'
    img.save(path, 'JPEG', quality=95)
    fsize = os.path.getsize(path)
    print(f"  {name}: {fsize:,} bytes ({fsize/1024/1024:.1f} MB)")

# Generate large pattern backgrounds
print("Generating large patterns...")
for name, w, h, seed in [
    ('pattern_large', 3840, 3840, 123),
    ('pattern_wide', 4096, 2048, 456),
]:
    img = create_complex_pattern(w, h, seed)
    path = f'{BASE}/drawable-xxxhdpi/{name}.jpg'
    img.save(path, 'JPEG', quality=95)
    fsize = os.path.getsize(path)
    print(f"  {name}: {fsize:,} bytes ({fsize/1024/1024:.1f} MB)")

# Generate large logo PNG (PNG for transparency, but make it detailed)
print("Generating detailed logos...")
for sz in [1024, 768]:
    img = Image.new('RGBA', (sz, sz), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Complex background circle with gradient-like rings
    for i in range(sz // 4, 0, -2):
        ratio = i / (sz // 4)
        r = int(13 * ratio + 22 * (1 - ratio))
        g = int(17 * ratio + 27 * (1 - ratio))
        b = int(23 * ratio + 34 * (1 - ratio))
        draw.ellipse([sz // 2 - i, sz // 2 - i, sz // 2 + i, sz // 2 + i], fill=(r, g, b))
    
    # Multiple concentric rings
    for i in range(0, sz // 2, 8):
        c = max(0, 212 - i)
        if c > 10:
            draw.ellipse([sz // 2 - i, sz // 2 - i, sz // 2 + i, sz // 2 + i],
                        outline=(0, c, min(255, c + 40)), width=2)
    
    draw_screen_icon(draw, sz // 2, sz // 2 - sz // 12, int(sz * 0.38), CYAN)
    
    path = f'{BASE}/drawable-xxxhdpi/ic_logo_{sz}.png'
    img.save(path, 'PNG', optimize=False)
    fsize = os.path.getsize(path)
    print(f"  Logo {sz}x{sz}: {fsize:,} bytes ({fsize/1024:.1f} KB)")

# Total size check
total = 0
for root, dirs, files in os.walk(BASE):
    for f in files:
        if f.endswith(('.png', '.jpg', '.jpeg')):
            total += os.path.getsize(os.path.join(root, f))
print(f"\nTotal image resource size: {total:,} bytes ({total / 1024 / 1024:.1f} MB)")

assets_total = 0
for root, dirs, files in os.walk(ASSETS):
    for f in files:
        assets_total += os.path.getsize(os.path.join(root, f))
print(f"Total assets size: {assets_total:,} bytes ({assets_total / 1024:.1f} KB)")
print(f"Grand total: {(total + assets_total) / 1024 / 1024:.1f} MB")
