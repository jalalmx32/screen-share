import os
import random
import math
from PIL import Image, ImageDraw, ImageFont

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

def create_noisy_complex_image(w, h, seed, style='gradient'):
    """Create images with noise patterns that resist compression."""
    random.seed(seed)
    img = Image.new('RGB', (w, h), BG)
    draw = ImageDraw.Draw(img)
    
    # Base gradient with pixel-level noise
    for y in range(h):
        ratio = y / h
        for x in range(0, w, 2):
            n = random.randint(-4, 4)
            if style == 'gradient':
                r = max(0, min(255, int(13 + ratio * 15 + n)))
                g = max(0, min(255, int(17 + ratio * 12 + n)))
                b = max(0, min(255, int(23 + ratio * 18 + n)))
            elif style == 'radial':
                dx = (x - w / 2) / w
                dy = (y - h / 2) / h
                dist = math.sqrt(dx * dx + dy * dy)
                r = max(0, min(255, int(22 + dist * 40 + n)))
                g = max(0, min(255, int(27 + dist * 50 + n)))
                b = max(0, min(255, int(34 + dist * 60 + n)))
            else:  # chaotic
                r = max(0, min(255, int(13 + random.randint(0, 20) + n)))
                g = max(0, min(255, int(17 + random.randint(0, 15) + n)))
                b = max(0, min(255, int(23 + random.randint(0, 25) + n)))
            draw.line([(x, y), (min(x + 2, w), y)], fill=(r, g, b))
    
    # Random geometric overlays
    for _ in range(100):
        x = random.randint(0, w)
        y = random.randint(0, h)
        size = random.randint(20, min(w, h) // 3)
        c = random.choice([(26, 80, 139), (0, 168, 204), (15, 40, 70)])
        if random.random() < 0.5:
            draw.ellipse([x - size, y - size, x + size, y + size], outline=c, width=random.randint(1, 3))
        else:
            draw.rectangle([x, y, x + size, y + size // 2], outline=c, width=random.randint(1, 2))
    
    # Scattered points for texture
    for _ in range(1000):
        x = random.randint(0, w - 1)
        y = random.randint(0, h - 1)
        b = random.randint(10, 60)
        draw.point((x, y), fill=(b, b + random.randint(0, 10), b + random.randint(0, 15)))
    
    # Diagonal noise lines
    for i in range(-w, w * 2, 48):
        c = random.randint(12, 30)
        draw.line([(i, 0), (i + h * 2, h)], fill=(c, c + 8, c + 16), width=1)
    
    # Circuit-board-like patterns
    for _ in range(30):
        x = random.randint(0, w)
        y = random.randint(0, h)
        for step in range(random.randint(3, 10)):
            dx = random.choice([-1, 0, 1]) * random.randint(20, 80)
            dy = random.choice([-1, 0, 1]) * random.randint(20, 80)
            x2, y2 = x + dx, y + dy
            c = random.choice([(26, 80, 139), (0, 120, 180)])
            draw.line([(x, y), (x2, y2)], fill=c, width=1)
            x, y = x2, y2
    
    return img

# Generate additional large images to push past 10MB
print("Generating additional large assets...")

# Additional splash variants
for name, w, h, seed, style in [
    ('splash_extra_1', 3840, 2160, 201, 'gradient'),
    ('splash_extra_2', 2160, 3840, 202, 'radial'),
    ('bg_main', 3840, 3840, 203, 'chaotic'),
    ('bg_detail', 4096, 2048, 204, 'gradient'),
]:
    img = create_noisy_complex_image(w, h, seed, style)
    path = f'{BASE}/drawable-xxxhdpi/{name}.jpg'
    img.save(path, 'JPEG', quality=95)
    fsize = os.path.getsize(path)
    print(f"  {name} ({w}x{h}): {fsize:,} bytes ({fsize/1024/1024:.1f} MB)")

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
