#!/usr/bin/env python3
"""
DocScanner KMP - Google Play Store Release Asset Generator
Renders high-resolution, visually stunning publication assets using Pillow:
1. icon_512x512.png (512x512 App Icon)
2. feature_graphic_1024x500.png (1024x500 Feature Graphic Banner)
3. 8x Play Store Screenshots (1080x2400 PNGs)
4. store_listing.md (Google Play Store Copy & Metadata)
"""

import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = "/Users/azhar/Documents/Projects/DocScannerKMP/playstore_assets"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# ---------------------------------------------------------
# COLOR PALETTE (Obsidian, Slate, Emerald & Laser Cyan)
# ---------------------------------------------------------
EMERALD = (16, 185, 129, 255)        # #10B981
EMERALD_DARK = (5, 150, 105, 255)    # #059669
EMERALD_LIGHT = (52, 211, 153, 255)  # #34D399
TEAL = (13, 148, 136, 255)          # #0D9488
CYAN = (6, 182, 212, 255)           # #06B6D4
LASER = (34, 211, 238, 255)         # #22D3EE
AMBER = (245, 158, 11, 255)         # #F59E0B
PURPLE = (139, 92, 246, 255)        # #8B5CF6
BLUE = (59, 130, 246, 255)          # #3B82F6
ROSE = (244, 63, 94, 255)           # #F43F5E

DARK_BG = (11, 15, 25, 255)         # #0B0F19
DARK_SURFACE = (19, 27, 46, 255)    # #131B2E
DARK_CARD = (30, 41, 59, 255)       # #1E293B
DARK_CARD_LIGHT = (45, 55, 72, 255)
DARK_BORDER = (51, 65, 85, 255)     # #334155
TEXT_PRIMARY = (248, 250, 252, 255) # #F8FAFC
TEXT_SECONDARY = (148, 163, 184, 255) # #94A3B8
TEXT_MUTED = (100, 116, 139, 255)   # #64748B

WHITE = (255, 255, 255, 255)
BLACK = (0, 0, 0, 255)

# ---------------------------------------------------------
# FONT HELPER
# ---------------------------------------------------------
def get_font(size, bold=False, mono=False):
    if mono:
        candidates = ['/System/Library/Fonts/Menlo.ttc', '/System/Library/Fonts/SFNSMono.ttf']
    elif bold:
        candidates = [
            '/System/Library/Fonts/Supplemental/Arial Bold.ttf',
            '/System/Library/Fonts/HelveticaNeue.ttc',
            '/System/Library/Fonts/Helvetica.ttc',
            '/System/Library/Fonts/SFNS.ttf'
        ]
    else:
        candidates = [
            '/System/Library/Fonts/Supplemental/Arial.ttf',
            '/System/Library/Fonts/HelveticaNeue.ttc',
            '/System/Library/Fonts/Helvetica.ttc',
            '/System/Library/Fonts/SFNS.ttf'
        ]
    
    for path in candidates:
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, size)
            except Exception:
                continue
    return ImageFont.load_default()

# ---------------------------------------------------------
# GRAPHIC UTILITIES
# ---------------------------------------------------------
def create_linear_gradient(size, c1, c2, vertical=True):
    w, h = size
    base = Image.new('RGBA', size)
    if vertical:
        for y in range(h):
            t = y / max(1, h - 1)
            r = int(c1[0] + (c2[0] - c1[0]) * t)
            g = int(c1[1] + (c2[1] - c1[1]) * t)
            b = int(c1[2] + (c2[2] - c1[2]) * t)
            a = int(c1[3] + (c2[3] - c1[3]) * t)
            line = Image.new('RGBA', (w, 1), (r, g, b, a))
            base.paste(line, (0, y))
    else:
        for x in range(w):
            t = x / max(1, w - 1)
            r = int(c1[0] + (c2[0] - c1[0]) * t)
            g = int(c1[1] + (c2[1] - c1[1]) * t)
            b = int(c1[2] + (c2[2] - c1[2]) * t)
            a = int(c1[3] + (c2[3] - c1[3]) * t)
            line = Image.new('RGBA', (1, h), (r, g, b, a))
            base.paste(line, (x, 0))
    return base

def create_radial_gradient(size, c_center, c_edge, center=None, radius=None):
    w, h = size
    small_w = 200
    small_h = int(200 * h / w)
    cx = (center[0] / w * small_w) if center else small_w / 2
    cy = (center[1] / h * small_h) if center else small_h / 2
    rad = (radius / w * small_w) if radius else math.hypot(small_w / 2, small_h / 2)
    
    img = Image.new('RGBA', (small_w, small_h))
    pix = img.load()
    for y in range(small_h):
        for x in range(small_w):
            d = math.hypot(x - cx, y - cy) / max(1, rad)
            d = min(1.0, max(0.0, d))
            r = int(c_center[0] + (c_edge[0] - c_center[0]) * d)
            g = int(c_center[1] + (c_edge[1] - c_center[1]) * d)
            b = int(c_center[2] + (c_edge[2] - c_center[2]) * d)
            a = int(c_center[3] + (c_edge[3] - c_center[3]) * d)
            pix[x, y] = (r, g, b, a)
    return img.resize(size, Image.Resampling.BILINEAR)

def draw_card_with_shadow(img, xy, radius=18, fill=DARK_CARD, border=DARK_BORDER, shadow_blur=16, shadow_alpha=90, offset=(0, 6)):
    x1, y1, x2, y2 = xy
    w, h = img.size
    
    # Shadow layer
    if shadow_alpha > 0:
        shadow_layer = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        s_draw = ImageDraw.Draw(shadow_layer)
        sx1, sy1 = x1 + offset[0], y1 + offset[1]
        sx2, sy2 = x2 + offset[0], y2 + offset[1]
        s_draw.rounded_rectangle([sx1, sy1, sx2, sy2], radius=radius, fill=(0, 0, 0, shadow_alpha))
        shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(shadow_blur))
        img.alpha_composite(shadow_layer)
    
    # Card layer
    card_draw = ImageDraw.Draw(img)
    card_draw.rounded_rectangle([x1, y1, x2, y2], radius=radius, fill=fill, outline=border, width=1)

def draw_glow_orb(img, center, radius, color, alpha=70):
    cx, cy = center
    w, h = img.size
    orb_layer = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(orb_layer)
    d.ellipse([cx - radius, cy - radius, cx + radius, cy + radius], fill=(color[0], color[1], color[2], alpha))
    orb_layer = orb_layer.filter(ImageFilter.GaussianBlur(radius // 2))
    img.alpha_composite(orb_layer)

def draw_pill(draw, xy, text, font, bg_color, text_color, icon_text=None, border_color=None):
    x, y, w, h = xy
    draw.rounded_rectangle([x, y, x + w, y + h], radius=h // 2, fill=bg_color, outline=border_color, width=1)
    
    full_text = f"{icon_text} {text}" if icon_text else text
    bbox = draw.textbbox((0, 0), full_text, font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    tx = x + (w - tw) // 2
    ty = y + (h - th) // 2 - bbox[1]
    draw.text((tx, ty), full_text, fill=text_color, font=font)

def draw_slider(draw, xy, width, progress, active_color=EMERALD, inactive_color=DARK_BORDER):
    x, y = xy
    h = 8
    # Background rail
    draw.rounded_rectangle([x, y, x + width, y + h], radius=4, fill=inactive_color)
    # Active rail
    p = min(1.0, max(0.0, progress))
    active_w = int(width * p)
    if active_w > 0:
        draw.rounded_rectangle([x, y, x + active_w, y + h], radius=4, fill=active_color)
    # Thumb
    thumb_x = x + active_w
    thumb_y = y + h // 2
    draw.ellipse([thumb_x - 10, thumb_y - 10, thumb_x + 10, thumb_y + 10], fill=WHITE, outline=active_color, width=3)

def draw_switch(draw, xy, is_on, active_color=EMERALD, inactive_color=DARK_BORDER):
    x, y = xy
    w, h = 48, 26
    bg = active_color if is_on else inactive_color
    draw.rounded_rectangle([x, y, x + w, y + h], radius=h // 2, fill=bg)
    thumb_x = x + w - h + 2 if is_on else x + 2
    draw.ellipse([thumb_x, y + 2, thumb_x + h - 4, y + h - 2], fill=WHITE)

def draw_status_bar(draw, w, y=14):
    font = get_font(20, bold=True)
    draw.text((36, y), "09:41", fill=TEXT_PRIMARY, font=font)
    
    # 5G, WiFi, Battery icons
    font_sub = get_font(18, bold=True)
    draw.text((w - 170, y), "5G", fill=TEXT_PRIMARY, font=font_sub)
    
    # WiFi arc
    wx = w - 120
    draw.arc([wx, y + 2, wx + 18, y + 20], start=200, end=340, fill=TEXT_PRIMARY, width=2)
    draw.arc([wx + 3, y + 6, wx + 15, y + 18], start=200, end=340, fill=TEXT_PRIMARY, width=2)
    draw.ellipse([wx + 8, y + 14, wx + 10, y + 16], fill=TEXT_PRIMARY)
    
    # Battery icon
    bx = w - 75
    draw.rounded_rectangle([bx, y + 4, bx + 34, y + 20], radius=4, outline=TEXT_PRIMARY, width=2)
    draw.rectangle([bx + 34, y + 9, bx + 37, y + 15], fill=TEXT_PRIMARY)
    draw.rounded_rectangle([bx + 3, y + 7, bx + 28, y + 17], radius=2, fill=EMERALD)

def draw_device_mockup(base_img, screen_img, phone_box):
    """
    Renders an ultra-modern smartphone bezel and embeds the screen_img inside phone_box.
    phone_box: (x, y, w, h)
    """
    px, py, pw, ph = phone_box
    
    # Outer device drop shadow
    shadow_layer = Image.new('RGBA', base_img.size, (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.rounded_rectangle([px - 14, py - 14, px + pw + 14, py + ph + 24], radius=56, fill=(0, 0, 0, 160))
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(32))
    base_img.alpha_composite(shadow_layer)
    
    # Outer metallic titanium bezel
    d = ImageDraw.Draw(base_img)
    d.rounded_rectangle([px - 12, py - 12, px + pw + 12, py + ph + 12], radius=52, fill=(28, 35, 51, 255), outline=(71, 85, 105, 255), width=3)
    d.rounded_rectangle([px - 6, py - 6, px + pw + 6, py + ph + 6], radius=46, fill=(15, 23, 42, 255), outline=(30, 41, 59, 255), width=2)
    
    # Inner screen with rounded corners
    resized_screen = screen_img.resize((pw, ph), Image.Resampling.LANCZOS)
    
    # Corner mask for screen
    mask = Image.new('L', (pw, ph), 0)
    m_draw = ImageDraw.Draw(mask)
    m_draw.rounded_rectangle([0, 0, pw, ph], radius=40, fill=255)
    
    base_img.paste(resized_screen, (px, py), mask=mask)
    
    # Front Camera Punch-hole
    cam_x = px + pw // 2
    cam_y = py + 26
    d.ellipse([cam_x - 12, cam_y - 12, cam_x + 12, cam_y + 12], fill=(5, 5, 10, 255), outline=(30, 41, 59, 255), width=2)
    d.ellipse([cam_x - 4, cam_y - 4, cam_x + 4, cam_y + 4], fill=(16, 24, 39, 255))
    d.ellipse([cam_x - 2, cam_y - 2, cam_x + 1, cam_y + 1], fill=(34, 211, 238, 180))
    
    # Bottom Home Indicator line
    home_w = 180
    home_x = px + (pw - home_w) // 2
    home_y = py + ph - 16
    d.rounded_rectangle([home_x, home_y, home_x + home_w, home_y + 5], radius=3, fill=(148, 163, 184, 180))


# ---------------------------------------------------------
# 1. APP LAUNCHER ICON (512x512)
# ---------------------------------------------------------
def generate_app_icon():
    print("Generating icon_512x512.png...")
    size = (512, 512)
    img = Image.new('RGBA', size, (0, 0, 0, 0))
    
    # Base rounded squircle container
    squircle_mask = Image.new('L', size, 0)
    s_draw = ImageDraw.Draw(squircle_mask)
    s_draw.rounded_rectangle([16, 16, 496, 496], radius=110, fill=255)
    
    # Rich Dark Obsidian to Midnight Blue Gradient
    bg_grad = create_radial_gradient(size, (20, 30, 55, 255), (10, 14, 26, 255), center=(200, 180), radius=360)
    
    # Ambient Emerald Glow
    draw_glow_orb(bg_grad, (256, 256), 180, EMERALD, alpha=80)
    draw_glow_orb(bg_grad, (340, 180), 120, CYAN, alpha=90)
    
    img.paste(bg_grad, (0, 0), mask=squircle_mask)
    
    # Drop shadow for the floating document
    d_draw = ImageDraw.Draw(img)
    
    sx1, sy1, sx2, sy2 = 110, 95, 402, 417
    fold_size = 65
    
    # Document drop shadow
    shadow_img = Image.new('RGBA', size, (0, 0, 0, 0))
    sh_draw = ImageDraw.Draw(shadow_img)
    sh_draw.rounded_rectangle([sx1, sy1 + 10, sx2, sy2 + 10], radius=28, fill=(0, 0, 0, 160))
    shadow_img = shadow_img.filter(ImageFilter.GaussianBlur(24))
    img.alpha_composite(shadow_img)
    
    # Document Body (White & Subtle Cyan Glass)
    doc_layer = Image.new('RGBA', size, (0, 0, 0, 0))
    doc_draw = ImageDraw.Draw(doc_layer)
    
    doc_points = [
        (sx1, sy1),
        (sx2 - fold_size, sy1),
        (sx2, sy1 + fold_size),
        (sx2, sy2),
        (sx1, sy2)
    ]
    doc_draw.polygon(doc_points, fill=(245, 248, 255, 255))
    
    # Folded Corner Flap
    fold_points = [
        (sx2 - fold_size, sy1),
        (sx2 - fold_size, sy1 + fold_size),
        (sx2, sy1 + fold_size)
    ]
    doc_draw.polygon(fold_points, fill=(200, 215, 235, 255))
    doc_draw.line([(sx2 - fold_size, sy1), (sx2 - fold_size, sy1 + fold_size), (sx2, sy1 + fold_size)], fill=(160, 185, 215, 255), width=2)
    
    # Document content lines
    line_color = (180, 195, 215, 255)
    doc_draw.rounded_rectangle([sx1 + 40, sy1 + 50, sx1 + 170, sy1 + 68], radius=6, fill=EMERALD)
    for i in range(5):
        ly = sy1 + 110 + (i * 38)
        lw = (sx2 - sx1 - 80) if i % 2 == 0 else (sx2 - sx1 - 130)
        doc_draw.rounded_rectangle([sx1 + 40, ly, sx1 + 40 + lw, ly + 14], radius=4, fill=line_color)
    
    # Bottom image placeholder on doc
    doc_draw.rounded_rectangle([sx1 + 40, sy1 + 290, sx2 - 40, sy2 - 35], radius=12, fill=(215, 228, 245, 255))
    
    img.alpha_composite(doc_layer)
    
    # High-Tech Glowing Laser Sweep Beam
    laser_layer = Image.new('RGBA', size, (0, 0, 0, 0))
    l_draw = ImageDraw.Draw(laser_layer)
    laser_y = 265
    
    l_draw.rectangle([sx1 - 30, laser_y - 18, sx2 + 30, laser_y + 18], fill=(34, 211, 238, 70))
    l_draw.rectangle([sx1 - 20, laser_y - 8, sx2 + 20, laser_y + 8], fill=(52, 211, 153, 140))
    l_draw.rectangle([sx1 - 15, laser_y - 3, sx2 + 15, laser_y + 3], fill=(255, 255, 255, 255))
    
    l_draw.ellipse([sx1 - 22, laser_y - 8, sx1 - 6, laser_y + 8], fill=CYAN, outline=WHITE, width=2)
    l_draw.ellipse([sx2 + 6, laser_y - 8, sx2 + 22, laser_y + 8], fill=CYAN, outline=WHITE, width=2)
    
    laser_layer = laser_layer.filter(ImageFilter.GaussianBlur(3))
    img.alpha_composite(laser_layer)
    
    # Central Camera Aperture / AI Scan Lens Emblem
    lens_x, lens_y = 256, 265
    lens_r = 52
    d_draw.ellipse([lens_x - lens_r, lens_y - lens_r, lens_x + lens_r, lens_y + lens_r], fill=(15, 23, 42, 240), outline=EMERALD_LIGHT, width=4)
    d_draw.ellipse([lens_x - 36, lens_y - 36, lens_x + 36, lens_y + 36], fill=(20, 30, 55, 255), outline=CYAN, width=3)
    d_draw.ellipse([lens_x - 20, lens_y - 20, lens_x + 20, lens_y + 20], fill=EMERALD)
    d_draw.ellipse([lens_x - 8, lens_y - 8, lens_x + 8, lens_y + 8], fill=WHITE)
    
    # Scan Viewfinder Corner Brackets
    bw, bh = 28, 6
    # TL
    d_draw.rectangle([sx1 - 10, sy1 - 10, sx1 - 10 + bw, sy1 - 10 + bh], fill=EMERALD)
    d_draw.rectangle([sx1 - 10, sy1 - 10, sx1 - 10 + bh, sy1 - 10 + bw], fill=EMERALD)
    # TR
    d_draw.rectangle([sx2 + 10 - bw, sy1 - 10, sx2 + 10, sy1 - 10 + bh], fill=EMERALD)
    d_draw.rectangle([sx2 + 10 - bh, sy1 - 10, sx2 + 10, sy1 - 10 + bw], fill=EMERALD)
    # BL
    d_draw.rectangle([sx1 - 10, sy2 + 10 - bh, sx1 - 10 + bw, sy2 + 10], fill=EMERALD)
    d_draw.rectangle([sx1 - 10, sy2 + 10 - bw, sx1 - 10 + bh, sy2 + 10], fill=EMERALD)
    # BR
    d_draw.rectangle([sx2 + 10 - bw, sy2 + 10 - bh, sx2 + 10, sy2 + 10], fill=EMERALD)
    d_draw.rectangle([sx2 + 10 - bh, sy2 + 10 - bw, sx2 + 10, sy2 + 10], fill=EMERALD)
    
    # AI Badge Chip in bottom-right
    badge_x, badge_y, badge_w, badge_h = 320, 390, 130, 48
    d_draw.rounded_rectangle([badge_x, badge_y, badge_x + badge_w, badge_y + badge_h], radius=24, fill=(16, 185, 129, 255), outline=WHITE, width=2)
    font_ai = get_font(26, bold=True)
    d_draw.text((badge_x + 24, badge_y + 9), "AI OCR", fill=(0, 0, 0, 255), font=font_ai)
    
    # Squircle border highlight
    d_draw.rounded_rectangle([16, 16, 496, 496], radius=110, outline=(71, 85, 105, 140), width=3)
    
    out_path = os.path.join(OUTPUT_DIR, "icon_512x512.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")
    return img


# ---------------------------------------------------------
# 2. FEATURE GRAPHIC (1024x500)
# ---------------------------------------------------------
def generate_feature_graphic():
    print("Generating feature_graphic_1024x500.png...")
    size = (1024, 500)
    w, h = size
    img = Image.new('RGBA', size, (11, 15, 25, 255))
    
    bg = create_radial_gradient(size, (20, 32, 58, 255), (9, 13, 22, 255), center=(300, 250), radius=600)
    img.paste(bg, (0, 0))
    
    draw_glow_orb(img, (200, 180), 280, EMERALD, alpha=50)
    draw_glow_orb(img, (850, 260), 320, CYAN, alpha=60)
    draw_glow_orb(img, (550, 400), 200, PURPLE, alpha=40)
    
    d = ImageDraw.Draw(img)
    
    # LEFT SECTION
    ix, iy = 56, 48
    d.rounded_rectangle([ix, iy, ix + 54, iy + 54], radius=14, fill=DARK_SURFACE, outline=EMERALD, width=2)
    d.rounded_rectangle([ix + 12, iy + 10, ix + 42, iy + 44], radius=6, fill=WHITE)
    d.rectangle([ix + 6, iy + 25, ix + 48, iy + 29], fill=LASER)
    
    font_brand = get_font(22, bold=True)
    d.text((ix + 68, iy + 14), "DocScanner KMP", fill=WHITE, font=font_brand)
    
    font_title = get_font(44, bold=True)
    d.text((56, 120), "Fast, Private & AI-Powered\nDocument Studio", fill=WHITE, font=font_title)
    
    font_sub = get_font(20, bold=False)
    d.text((58, 228), "100% On-Device OCR • Multi-Page PDF • 2-in-1 ID Scan", fill=EMERALD_LIGHT, font=font_sub)
    
    badges = [
        ("⚡ On-Device OCR & Parser", EMERALD),
        ("🪪 2-in-1 ID Card Stitching", CYAN),
        ("🔒 AES PDF Lock & E-Sign", AMBER),
        ("🎛️ 120 FPS GPU Color Filters", PURPLE)
    ]
    
    font_badge = get_font(17, bold=True)
    bx_start, by_start = 58, 276
    bw, bh = 250, 44
    gap_x, gap_y = 18, 14
    
    for idx, (b_text, b_col) in enumerate(badges):
        col_idx = idx % 2
        row_idx = idx // 2
        cur_x = bx_start + (col_idx * (bw + gap_x))
        cur_y = by_start + (row_idx * (bh + gap_y))
        
        d.rounded_rectangle([cur_x, cur_y, cur_x + bw, cur_y + bh], radius=12, fill=DARK_CARD, outline=DARK_BORDER, width=1)
        d.rounded_rectangle([cur_x + 8, cur_y + 8, cur_x + 12, cur_y + bh - 8], radius=2, fill=b_col)
        d.text((cur_x + 20, cur_y + 11), b_text, fill=TEXT_PRIMARY, font=font_badge)
    
    font_guard = get_font(16, bold=True)
    d.rounded_rectangle([58, 400, 576, 442], radius=10, fill=(15, 23, 42, 200), outline=EMERALD_DARK, width=1)
    d.text((74, 412), "🛡️ 100% Offline • Zero Cloud Uploads • Hardware Accelerated", fill=TEXT_PRIMARY, font=font_guard)
    
    # RIGHT SECTION
    phone_w, phone_h = 320, 440
    phone_x, phone_y = 660, 36
    
    scan_screen = Image.new('RGBA', (phone_w, phone_h), DARK_BG)
    s_draw = ImageDraw.Draw(scan_screen)
    
    s_draw.rectangle([0, 0, phone_w, phone_h], fill=(15, 20, 32, 255))
    
    doc_quad = [(50, 80), (270, 60), (290, 350), (30, 370)]
    s_draw.polygon(doc_quad, fill=(235, 240, 250, 255))
    
    for i in range(7):
        ly = 110 + i * 32
        s_draw.line([(70, ly), (250, ly - 10)], fill=(180, 195, 215, 255), width=4)
    
    s_draw.polygon(doc_quad, outline=EMERALD, width=3)
    for qx, qy in doc_quad:
        s_draw.ellipse([qx - 8, qy - 8, qx + 8, qy + 8], fill=EMERALD, outline=WHITE, width=2)
    
    l_layer = Image.new('RGBA', (phone_w, phone_h), (0, 0, 0, 0))
    ls_draw = ImageDraw.Draw(l_layer)
    ls_draw.rectangle([20, 200, 300, 220], fill=(34, 211, 238, 90))
    ls_draw.rectangle([30, 207, 290, 213], fill=(255, 255, 255, 240))
    l_layer = l_layer.filter(ImageFilter.GaussianBlur(3))
    scan_screen.alpha_composite(l_layer)
    
    draw_pill(s_draw, (85, 30, 150, 30), "AUTO CAPTURE", get_font(13, bold=True), EMERALD, BLACK)
    s_draw.ellipse([phone_w // 2 - 24, phone_h - 60, phone_w // 2 + 24, phone_h - 12], fill=EMERALD, outline=WHITE, width=3)
    
    draw_device_mockup(img, scan_screen, (phone_x, phone_y, phone_w, phone_h))
    
    out_path = os.path.join(OUTPUT_DIR, "feature_graphic_1024x500.png")
    img_rgb = Image.new("RGB", size, (11, 15, 25))
    img_rgb.paste(img, mask=img.split()[3])
    img_rgb.save(out_path, "PNG")
    print(f"Saved: {out_path}")
    return img_rgb


# ---------------------------------------------------------
# 3. 8X HIGH-RES PLAY STORE SCREENSHOTS (1080x2400)
# ---------------------------------------------------------

def create_screenshot_base(category, title, subtitle):
    size = (1080, 2400)
    w, h = size
    img = Image.new('RGBA', size, DARK_BG)
    
    bg = create_radial_gradient(size, (22, 34, 60, 255), (10, 14, 24, 255), center=(540, 400), radius=1100)
    img.paste(bg, (0, 0))
    
    draw_glow_orb(img, (200, 200), 260, EMERALD, alpha=45)
    draw_glow_orb(img, (880, 260), 280, CYAN, alpha=50)
    
    d = ImageDraw.Draw(img)
    
    font_cat = get_font(24, bold=True)
    pill_w = d.textbbox((0, 0), category, font=font_cat)[2] + 48
    draw_pill(d, ((w - pill_w) // 2, 80, pill_w, 48), category, font_cat, DARK_CARD, EMERALD_LIGHT, border_color=DARK_BORDER)
    
    font_title = get_font(56, bold=True)
    bbox_t = d.textbbox((0, 0), title, font=font_title)
    tw = bbox_t[2] - bbox_t[0]
    d.text(((w - tw) // 2, 146), title, fill=WHITE, font=font_title)
    
    font_sub = get_font(32, bold=False)
    bbox_s = d.textbbox((0, 0), subtitle, font=font_sub)
    sw = bbox_s[2] - bbox_s[0]
    d.text(((w - sw) // 2, 222), subtitle, fill=TEXT_SECONDARY, font=font_sub)
    
    phone_box = (100, 310, 880, 1980)
    return img, phone_box


# SCREEN 1: Home Dashboard & Document Organizer
def generate_screenshot_1():
    print("Generating screenshot_1_home_dashboard.png...")
    img, phone_box = create_screenshot_base(
        category="SMART DOCUMENT VAULT",
        title="All Your Documents, Organized & Secure",
        subtitle="Folders, Tags, Instant Search & Quick Studio Actions"
    )
    
    sw, sh = 880, 1980
    screen = Image.new('RGBA', (sw, sh), DARK_BG)
    sd = ImageDraw.Draw(screen)
    draw_status_bar(sd, sw)
    
    sy = 60
    font_app = get_font(38, bold=True)
    font_app_sub = get_font(20, bold=False)
    sd.text((36, sy), "DocScanner", fill=WHITE, font=font_app)
    sd.text((36, sy + 46), "AI-Powered PDF Scanner & OCR", fill=TEXT_SECONDARY, font=font_app_sub)
    
    icons = ["📷", "⇅", "▦", "⚙"]
    font_icon = get_font(26, bold=True)
    for i, ic in enumerate(icons):
        ix = sw - 220 + (i * 50)
        sd.rounded_rectangle([ix, sy + 6, ix + 42, sy + 48], radius=12, fill=DARK_SURFACE, outline=DARK_BORDER)
        sd.text((ix + 10, sy + 14), ic, fill=EMERALD_LIGHT, font=font_icon)
    
    sy += 95
    sd.rounded_rectangle([36, sy, sw - 36, sy + 64], radius=18, fill=DARK_SURFACE, outline=DARK_BORDER)
    font_search = get_font(22, bold=False)
    sd.text((60, sy + 18), "🔍  Search documents, OCR text, tags...", fill=TEXT_MUTED, font=font_search)
    
    sy += 84
    tools = [
        ("📷 Doc Scan", EMERALD_DARK, WHITE),
        ("🪪 2-in-1 ID Card", DARK_SURFACE, TEXT_PRIMARY),
        ("▦ QR & Barcode", DARK_SURFACE, TEXT_PRIMARY)
    ]
    font_tool = get_font(22, bold=True)
    tx = 36
    for t_text, bg_c, fg_c in tools:
        t_w = 250
        sd.rounded_rectangle([tx, sy, tx + t_w, sy + 58], radius=16, fill=bg_c, outline=DARK_BORDER if bg_c == DARK_SURFACE else None)
        sd.text((tx + 24, sy + 16), t_text, fill=fg_c, font=font_tool)
        tx += t_w + 18
    
    sy += 78
    folders = [
        ("All Docs (18)", True),
        ("★ Favorites (4)", False),
        ("📁 Invoices (8)", False),
        ("🪪 ID Cards (3)", False),
        ("🏥 Medical", False)
    ]
    font_f = get_font(20, bold=True)
    fx = 36
    for f_name, active in folders:
        f_bbox = sd.textbbox((0, 0), f_name, font=font_f)
        fw = f_bbox[2] - f_bbox[0] + 36
        bg_col = EMERALD if active else DARK_SURFACE
        text_col = BLACK if active else TEXT_SECONDARY
        sd.rounded_rectangle([fx, sy, fx + fw, sy + 48], radius=14, fill=bg_col, outline=None if active else DARK_BORDER)
        sd.text((fx + 18, sy + 12), f_name, fill=text_col, font=font_f)
        fx += fw + 14
    
    sy += 72
    docs = [
        ("Tax Invoice 2026", "Invoices • 4 pages", "PDF • Magic Color", "Aug 31, 2026", EMERALD),
        ("National ID Card", "ID Cards • 2 pages", "AES-256 Locked", "Aug 28, 2026", CYAN),
        ("Medical Rx Report", "Healthcare • 1 page", "OCR Extracted", "Aug 25, 2026", AMBER),
        ("Lease Agreement", "Legal • 6 pages", "Vector E-Signed", "Aug 20, 2026", PURPLE)
    ]
    
    card_w = (sw - 72 - 20) // 2
    card_h = 440
    
    for idx, (d_title, d_sub, d_tag, d_date, tag_col) in enumerate(docs):
        col = idx % 2
        row = idx // 2
        cx = 36 + col * (card_w + 20)
        cy = sy + row * (card_h + 24)
        
        draw_card_with_shadow(screen, (cx, cy, cx + card_w, cy + card_h), radius=20, fill=DARK_CARD, border=DARK_BORDER)
        
        th_h = 240
        sd.rounded_rectangle([cx + 14, cy + 14, cx + card_w - 14, cy + 14 + th_h], radius=14, fill=(240, 245, 252, 255))
        
        for line_i in range(5):
            sd.line([(cx + 34, cy + 40 + line_i * 26), (cx + card_w - 50, cy + 40 + line_i * 26)], fill=(190, 205, 225, 255), width=5)
        sd.rounded_rectangle([cx + 34, cy + 180, cx + card_w - 34, cy + 230], radius=8, fill=(215, 230, 250, 255))
        
        draw_pill(sd, (cx + 24, cy + 24, 160, 32), d_tag, get_font(14, bold=True), (15, 23, 42, 220), tag_col)
        
        font_d_title = get_font(22, bold=True)
        font_d_meta = get_font(17, bold=False)
        sd.text((cx + 18, cy + th_h + 30), d_title, fill=WHITE, font=font_d_title)
        sd.text((cx + 18, cy + th_h + 64), d_sub, fill=TEXT_SECONDARY, font=font_d_meta)
        sd.text((cx + 18, cy + th_h + 96), f"📅 {d_date}", fill=TEXT_MUTED, font=font_d_meta)
        sd.text((cx + card_w - 36, cy + th_h + 30), "⋮", fill=TEXT_SECONDARY, font=font_d_title)
    
    fab_y = sh - 200
    fab_x = sw - 120
    sd.ellipse([fab_x - 36, fab_y - 36, fab_x + 36, fab_y + 36], fill=EMERALD, outline=WHITE, width=3)
    sd.text((fab_x - 16, fab_y - 20), "📷", fill=BLACK, font=get_font(32, bold=True))
    
    draw_device_mockup(img, screen, phone_box)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_1_home_dashboard.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")


# SCREEN 2: Camera Scanner & Live Laser Edge Detection
def generate_screenshot_2():
    print("Generating screenshot_2_camera_laser.png...")
    img, phone_box = create_screenshot_base(
        category="AI AUTO-CAPTURE CAMERA",
        title="Smart Auto-Edge Detection & Auto-Capture",
        subtitle="Live Quad Edge Tracking, Real-Time Laser & Instant Shutter"
    )
    
    sw, sh = 880, 1980
    screen = Image.new('RGBA', (sw, sh), (10, 12, 18, 255))
    sd = ImageDraw.Draw(screen)
    draw_status_bar(sd, sw)
    
    sy = 60
    icons_top = [("✕", 40), ("⚡ Auto", sw // 2 - 50), ("▦ Grid", sw - 180), ("🔦", sw - 80)]
    for ic_text, ic_x in icons_top:
        sd.rounded_rectangle([ic_x, sy, ic_x + 60, sy + 48], radius=14, fill=(15, 23, 42, 180), outline=DARK_BORDER)
        sd.text((ic_x + 14, sy + 10), ic_text, fill=WHITE, font=get_font(20, bold=True))
    
    vf_y1, vf_y2 = 130, sh - 340
    sd.rectangle([0, vf_y1, sw, vf_y2], fill=(18, 24, 38, 255))
    
    quad = [(100, 320), (sw - 120, 260), (sw - 80, 1180), (70, 1240)]
    sd.polygon(quad, fill=(245, 248, 255, 255))
    
    for i in range(16):
        ly = 360 + i * 50
        sd.line([(120, ly), (sw - 140, ly - 8)], fill=(190, 205, 225, 255), width=5)
    
    sd.polygon(quad, outline=EMERALD, width=4)
    for qx, qy in quad:
        sd.ellipse([qx - 18, qy - 18, qx + 18, qy + 18], fill=EMERALD, outline=WHITE, width=3)
        sd.ellipse([qx - 6, qy - 6, qx + 6, qy + 6], fill=BLACK)
    
    laser_img = Image.new('RGBA', (sw, sh), (0, 0, 0, 0))
    ld = ImageDraw.Draw(laser_img)
    laser_y = 680
    ld.rectangle([40, laser_y - 30, sw - 40, laser_y + 30], fill=(34, 211, 238, 90))
    ld.rectangle([50, laser_y - 10, sw - 50, laser_y + 10], fill=(52, 211, 153, 160))
    ld.rectangle([60, laser_y - 3, sw - 60, laser_y + 3], fill=(255, 255, 255, 255))
    laser_img = laser_img.filter(ImageFilter.GaussianBlur(4))
    screen.alpha_composite(laser_img)
    
    draw_pill(sd, (sw // 2 - 140, 160, 280, 48), "AUTO-CAPTURE: READY", get_font(18, bold=True), EMERALD, BLACK)
    draw_pill(sd, (sw // 2 - 90, 220, 180, 36), "HOLD STEADY ✓", get_font(16, bold=True), (15, 23, 42, 200), CYAN)
    
    modes = ["DOCUMENT", "ID CARD", "BOOK", "PASSPORT", "QR CODE"]
    mx = 36
    my = sh - 310
    font_m = get_font(18, bold=True)
    for m in modes:
        active = (m == "DOCUMENT")
        col = EMERALD if active else TEXT_MUTED
        sd.text((mx, my), m, fill=col, font=font_m)
        if active:
            sd.line([(mx, my + 30), (mx + 110, my + 30)], fill=EMERALD, width=3)
        mx += 170
    
    sy_bot = sh - 200
    sd.rounded_rectangle([70, sy_bot + 10, 150, sy_bot + 90], radius=16, fill=DARK_SURFACE, outline=DARK_BORDER)
    sd.text((96, sy_bot + 36), "📄", fill=WHITE, font=get_font(26, bold=True))
    sd.ellipse([135, sy_bot, 165, sy_bot + 30], fill=EMERALD, outline=WHITE, width=2)
    sd.text((144, sy_bot + 4), "3", fill=BLACK, font=get_font(16, bold=True))
    
    shutter_x = sw // 2
    sd.ellipse([shutter_x - 52, sy_bot - 2, shutter_x + 52, sy_bot + 102], fill=(15, 23, 42, 255), outline=EMERALD, width=4)
    sd.ellipse([shutter_x - 42, sy_bot + 8, shutter_x + 42, sy_bot + 92], fill=WHITE)
    
    sd.rounded_rectangle([sw - 150, sy_bot + 10, sw - 70, sy_bot + 90], radius=16, fill=EMERALD_DARK, outline=WHITE, width=2)
    sd.text((sw - 122, sy_bot + 36), "➔", fill=WHITE, font=get_font(28, bold=True))
    
    draw_device_mockup(img, screen, phone_box)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_2_camera_laser.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")


# SCREEN 3: Perspective Dewarp & Crop
def generate_screenshot_3():
    print("Generating screenshot_3_dewarp_crop.png...")
    img, phone_box = create_screenshot_base(
        category="PRECISION PERSPECTIVE WARP",
        title="Precision 4-Point Loupe Perspective Dewarp",
        subtitle="Gaussian Direct Linear Transform & Real-Time Corner Loupe"
    )
    
    sw, sh = 880, 1980
    screen = Image.new('RGBA', (sw, sh), DARK_BG)
    sd = ImageDraw.Draw(screen)
    draw_status_bar(sd, sw)
    
    sy = 60
    sd.text((40, sy + 8), "←  Adjust & Crop", fill=WHITE, font=get_font(30, bold=True))
    draw_pill(sd, (sw - 160, sy, 120, 48), "Next", get_font(20, bold=True), EMERALD, BLACK)
    
    cy1, cy2 = 140, sh - 380
    sd.rectangle([30, cy1, sw - 30, cy2], fill=(15, 20, 32, 255), outline=DARK_BORDER)
    
    quad = [(120, 260), (sw - 100, 200), (sw - 70, 1140), (80, 1200)]
    sd.polygon(quad, fill=(245, 248, 255, 255))
    
    for i in range(14):
        ly = 320 + i * 54
        sd.line([(140, ly), (sw - 120, ly - 8)], fill=(195, 208, 226, 255), width=5)
    
    sd.polygon(quad, outline=EMERALD, width=4)
    for qx, qy in quad:
        sd.ellipse([qx - 16, qy - 16, qx + 16, qy + 16], fill=EMERALD, outline=WHITE, width=3)
    
    lx, ly = sw - 230, 320
    lr = 95
    sd.ellipse([lx - lr - 10, ly - lr - 10, lx + lr + 10, ly + lr + 10], fill=(0, 0, 0, 140))
    sd.ellipse([lx - lr, ly - lr, lx + lr, ly + lr], fill=(255, 255, 255, 255), outline=CYAN, width=5)
    sd.line([(lx - 60, ly - 30), (lx + 60, ly - 30)], fill=(80, 95, 120, 255), width=6)
    sd.line([(lx - 30, ly - 60), (lx - 30, ly + 60)], fill=(80, 95, 120, 255), width=6)
    sd.line([(lx - lr + 15, ly), (lx + lr - 15, ly)], fill=EMERALD, width=2)
    sd.line([(lx, ly - lr + 15), (lx, ly + lr - 15)], fill=EMERALD, width=2)
    sd.ellipse([lx - 12, ly - 12, lx + 12, ly + 12], fill=EMERALD, outline=WHITE, width=2)
    draw_pill(sd, (lx - 45, ly + lr + 12, 90, 28), "2.5x ZOOM", get_font(13, bold=True), (15, 23, 42, 220), WHITE)
    
    ry = sh - 340
    ratios = [("Original", False), ("A4 Doc", True), ("ID Card", False), ("1:1 Square", False), ("US Letter", False)]
    rx = 36
    font_r = get_font(18, bold=True)
    for r_name, is_sel in ratios:
        rw = 150
        bg_c = EMERALD if is_sel else DARK_SURFACE
        fg_c = BLACK if is_sel else TEXT_PRIMARY
        sd.rounded_rectangle([rx, ry, rx + rw, ry + 50], radius=14, fill=bg_c, outline=None if is_sel else DARK_BORDER)
        sd.text((rx + 24, ry + 14), r_name, fill=fg_c, font=font_r)
        rx += rw + 14
    
    by = sh - 250
    actions = [("✨ Auto-Fit", 36), ("↺ Rotate 90°", 240), ("⛶ Full Page", 460), ("🔄 Reset", 680)]
    for a_text, ax in actions:
        sd.rounded_rectangle([ax, by, ax + 180, by + 60], radius=16, fill=DARK_CARD, outline=DARK_BORDER)
        sd.text((ax + 20, by + 18), a_text, fill=TEXT_PRIMARY, font=get_font(18, bold=True))
    
    draw_device_mockup(img, screen, phone_box)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_3_dewarp_crop.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")


# SCREEN 4: GPU ColorMatrix Filters & Sliders
def generate_screenshot_4():
    print("Generating screenshot_4_gpu_filters.png...")
    img, phone_box = create_screenshot_base(
        category="120 FPS GPU COLOR ENGINE",
        title="Real-Time GPU Color Presets & Sliders",
        subtitle="Magic Color, Sharp B&W, Eco Print & 120 FPS Matrix Shaders"
    )
    
    sw, sh = 880, 1980
    screen = Image.new('RGBA', (sw, sh), DARK_BG)
    sd = ImageDraw.Draw(screen)
    draw_status_bar(sd, sw)
    
    sy = 60
    sd.text((40, sy + 8), "←  Enhance & Filter", fill=WHITE, font=get_font(30, bold=True))
    draw_pill(sd, (sw - 160, sy, 120, 48), "Done", get_font(20, bold=True), EMERALD, BLACK)
    
    doc_x1, doc_y1, doc_x2, doc_y2 = 100, 150, sw - 100, 960
    draw_card_with_shadow(screen, (doc_x1, doc_y1, doc_x2, doc_y2), radius=16, fill=(255, 255, 255, 255), border=DARK_BORDER)
    
    for i in range(12):
        ly = doc_y1 + 45 + i * 55
        sd.line([(doc_x1 + 40, ly), (doc_x2 - 40, ly)], fill=(20, 30, 45, 255), width=6)
    
    draw_pill(sd, (sw // 2 - 130, 900, 260, 44), "👁  Hold to Compare", get_font(17, bold=True), (15, 23, 42, 230), CYAN)
    
    fy = 1010
    filters = [
        ("Magic Color 1", True, EMERALD),
        ("Magic Color 2", False, CYAN),
        ("Sharp B&W", False, WHITE),
        ("Grayscale", False, TEXT_SECONDARY),
        ("Eco Print", False, AMBER)
    ]
    
    fx = 36
    for f_title, is_sel, th_col in filters:
        fw, fh = 150, 210
        border = EMERALD if is_sel else DARK_BORDER
        draw_card_with_shadow(screen, (fx, fy, fx + fw, fy + fh), radius=16, fill=DARK_CARD, border=border)
        sd.rounded_rectangle([fx + 14, fy + 14, fx + fw - 14, fy + 120], radius=10, fill=WHITE)
        sd.rounded_rectangle([fx + 24, fy + 24, fx + fw - 24, fy + 50], radius=4, fill=th_col)
        for li in range(3):
            sd.line([(fx + 24, fy + 65 + li * 16), (fx + fw - 24, fy + 65 + li * 16)], fill=(120, 140, 160, 255), width=3)
        
        sd.text((fx + 14, fy + 140), f_title, fill=WHITE if is_sel else TEXT_MUTED, font=get_font(16, bold=is_sel))
        if is_sel:
            draw_pill(sd, (fx + 24, fy + 172, fw - 48, 24), "ACTIVE", get_font(12, bold=True), EMERALD, BLACK)
        fx += fw + 18
    
    sy_sliders = 1270
    draw_card_with_shadow(screen, (36, sy_sliders, sw - 36, sh - 100), radius=20, fill=DARK_CARD, border=DARK_BORDER)
    
    sd.text((64, sy_sliders + 30), "FINE-TUNING CONTROLS", fill=TEXT_MUTED, font=get_font(18, bold=True))
    
    sliders = [
        ("Contrast", "+28%", 0.68, EMERALD),
        ("Brightness", "+15%", 0.58, CYAN),
        ("Saturation", "120%", 0.60, AMBER),
        ("Sharpness", "+45%", 0.72, PURPLE)
    ]
    
    for idx, (s_name, s_val, s_prog, s_color) in enumerate(sliders):
        cur_y = sy_sliders + 80 + idx * 110
        sd.text((64, cur_y), s_name, fill=TEXT_PRIMARY, font=get_font(20, bold=True))
        sd.text((sw - 140, cur_y), s_val, fill=s_color, font=get_font(20, bold=True))
        draw_slider(sd, (64, cur_y + 40), sw - 128, s_prog, active_color=s_color)
    
    draw_device_mockup(img, screen, phone_box)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_4_gpu_filters.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")


# SCREEN 5: PDF Tools & Vector E-Signature Pad
def generate_screenshot_5():
    print("Generating screenshot_5_pdf_tools_signature.png...")
    img, phone_box = create_screenshot_base(
        category="ENTERPRISE PDF SUITE",
        title="AES Password Lock & Vector E-Signature",
        subtitle="Hardware AES Encryption, Custom Watermark & Digital Sign"
    )
    
    sw, sh = 880, 1980
    screen = Image.new('RGBA', (sw, sh), DARK_BG)
    sd = ImageDraw.Draw(screen)
    draw_status_bar(sd, sw)
    
    sy = 60
    sd.text((40, sy + 8), "←  PDF Tools & Export", fill=WHITE, font=get_font(30, bold=True))
    
    sy += 80
    sd.text((40, sy), "PDF RESOLUTION & QUALITY", fill=TEXT_MUTED, font=get_font(18, bold=True))
    sy += 36
    qualities = [("Low", False), ("Medium", False), ("High (150 DPI)", False), ("Ultra (300 DPI)", True)]
    qx = 40
    for q_name, is_sel in qualities:
        qw = 180
        bg_c = EMERALD if is_sel else DARK_SURFACE
        fg_c = BLACK if is_sel else TEXT_PRIMARY
        sd.rounded_rectangle([qx, sy, qx + qw, sy + 48], radius=14, fill=bg_c, outline=None if is_sel else DARK_BORDER)
        sd.text((qx + 18, sy + 12), q_name, fill=fg_c, font=get_font(16, bold=True))
        qx += qw + 14
    
    sy += 75
    draw_card_with_shadow(screen, (36, sy, sw - 36, sy + 140), radius=18, fill=DARK_CARD, border=DARK_BORDER)
    sd.text((64, sy + 24), "Add Security Watermark", fill=TEXT_PRIMARY, font=get_font(22, bold=True))
    sd.text((64, sy + 56), "Visible on all exported PDF pages", fill=TEXT_MUTED, font=get_font(16, bold=False))
    draw_switch(sd, (sw - 110, sy + 28), True)
    sd.rounded_rectangle([64, sy + 84, sw - 64, sy + 124], radius=10, fill=DARK_SURFACE, outline=DARK_BORDER)
    sd.text((80, sy + 94), "CONFIDENTIAL • INTERNAL USE ONLY", fill=EMERALD_LIGHT, font=get_font(16, bold=True))
    
    sy += 164
    draw_card_with_shadow(screen, (36, sy, sw - 36, sy + 140), radius=18, fill=DARK_CARD, border=DARK_BORDER)
    sd.text((64, sy + 24), "🔒 AES-256 Password Protection", fill=TEXT_PRIMARY, font=get_font(22, bold=True))
    sd.text((64, sy + 56), "Hardware-backed military grade encryption", fill=TEXT_MUTED, font=get_font(16, bold=False))
    draw_switch(sd, (sw - 110, sy + 28), True)
    sd.rounded_rectangle([64, sy + 84, sw - 64, sy + 124], radius=10, fill=DARK_SURFACE, outline=DARK_BORDER)
    sd.text((80, sy + 94), "•••••••••••••••• (Encrypted)", fill=CYAN, font=get_font(18, bold=True))
    
    sy += 164
    draw_card_with_shadow(screen, (36, sy, sw - 36, sy + 440), radius=18, fill=DARK_CARD, border=DARK_BORDER)
    sd.text((64, sy + 24), "✍️ Vector Digital Signature", fill=TEXT_PRIMARY, font=get_font(22, bold=True))
    sd.text((sw - 180, sy + 24), "Clear / Redo", fill=ROSE, font=get_font(18, bold=True))
    
    sd.rounded_rectangle([64, sy + 70, sw - 64, sy + 380], radius=14, fill=WHITE, outline=DARK_BORDER)
    
    sig_points = [
        (120, sy + 240), (160, sy + 160), (220, sy + 280), (260, sy + 180),
        (320, sy + 220), (380, sy + 150), (450, sy + 250), (520, sy + 180),
        (580, sy + 260), (660, sy + 220), (740, sy + 200)
    ]
    for p_i in range(len(sig_points) - 1):
        sd.line([sig_points[p_i], sig_points[p_i + 1]], fill=(10, 25, 70, 255), width=5)
    
    sd.text((84, sy + 340), "Signed by: Alexander Morgan • Verified Digital Hash", fill=(100, 116, 139, 255), font=get_font(15, bold=True))
    
    sy_btn = sh - 160
    sd.rounded_rectangle([40, sy_btn, sw - 40, sy_btn + 76], radius=20, fill=EMERALD)
    sd.text((sw // 2 - 160, sy_btn + 22), "📄  Generate & Share PDF", fill=BLACK, font=get_font(24, bold=True))
    
    draw_device_mockup(img, screen, phone_box)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_5_pdf_tools_signature.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")


# SCREEN 6: 2-in-1 ID Card Double-Sided Stitching
def generate_screenshot_6():
    print("Generating screenshot_6_id_card_stitch.png...")
    img, phone_box = create_screenshot_base(
        category="DUAL-FRAME ID CAPTURE",
        title="2-in-1 ID Card Double-Sided Stitching",
        subtitle="Capture Front & Back Seamlessly onto a Single A4 Page"
    )
    
    sw, sh = 880, 1980
    screen = Image.new('RGBA', (sw, sh), DARK_BG)
    sd = ImageDraw.Draw(screen)
    draw_status_bar(sd, sw)
    
    sy = 60
    sd.text((40, sy + 8), "←  2-in-1 ID Card Scan", fill=WHITE, font=get_font(30, bold=True))
    
    sy += 80
    sd.rounded_rectangle([40, sy, sw // 2 - 10, sy + 50], radius=12, fill=EMERALD_DARK)
    sd.text((60, sy + 14), "✓ Step 1: Front Side", fill=WHITE, font=get_font(18, bold=True))
    
    sd.rounded_rectangle([sw // 2 + 10, sy, sw - 40, sy + 50], radius=12, fill=EMERALD)
    sd.text((sw // 2 + 30, sy + 14), "✓ Step 2: Back Side", fill=BLACK, font=get_font(18, bold=True))
    
    sy += 80
    a4_w, a4_h = sw - 120, 1200
    a4_x1, a4_y1 = 60, sy
    a4_x2, a4_y2 = a4_x1 + a4_w, a4_y1 + a4_h
    
    draw_card_with_shadow(screen, (a4_x1, a4_y1, a4_x2, a4_y2), radius=20, fill=WHITE, border=DARK_BORDER)
    
    sd.text((a4_x1 + 40, a4_y1 + 40), "GOVERNMENT IDENTIFICATION CARD", fill=(20, 30, 50, 255), font=get_font(22, bold=True))
    sd.line([(a4_x1 + 40, a4_y1 + 75), (a4_x2 - 40, a4_y1 + 75)], fill=(200, 210, 225, 255), width=2)
    
    # FRONT CARD
    c1_y1 = a4_y1 + 110
    c1_y2 = c1_y1 + 440
    sd.rounded_rectangle([a4_x1 + 40, c1_y1, a4_x2 - 40, c1_y2], radius=16, fill=(240, 245, 255, 255), outline=(180, 200, 230, 255), width=2)
    
    sd.rounded_rectangle([a4_x1 + 70, c1_y1 + 40, a4_x1 + 220, c1_y1 + 230], radius=12, fill=(200, 215, 240, 255))
    sd.text((a4_x1 + 115, c1_y1 + 100), "👤", fill=(100, 120, 160, 255), font=get_font(50, bold=True))
    
    font_id_h = get_font(24, bold=True)
    font_id_txt = get_font(18, bold=False)
    sd.text((a4_x1 + 250, c1_y1 + 45), "NATIONAL DRIVER LICENSE", fill=(15, 23, 42, 255), font=font_id_h)
    sd.text((a4_x1 + 250, c1_y1 + 95), "Name: ALEXANDER MORGAN", fill=(51, 65, 85, 255), font=font_id_txt)
    sd.text((a4_x1 + 250, c1_y1 + 135), "ID No: DL-984210-AZ", fill=(51, 65, 85, 255), font=font_id_txt)
    sd.text((a4_x1 + 250, c1_y1 + 175), "DOB: 14 MAY 1992", fill=(51, 65, 85, 255), font=font_id_txt)
    sd.text((a4_x1 + 250, c1_y1 + 215), "Expires: 14 MAY 2032", fill=(16, 185, 129, 255), font=get_font(18, bold=True))
    
    draw_pill(sd, (a4_x2 - 200, c1_y1 + 350, 130, 36), "FRONT SIDE", get_font(14, bold=True), (15, 23, 42, 220), CYAN)
    
    # BACK CARD
    c2_y1 = c1_y2 + 60
    c2_y2 = c2_y1 + 440
    sd.rounded_rectangle([a4_x1 + 40, c2_y1, a4_x2 - 40, c2_y2], radius=16, fill=(240, 245, 255, 255), outline=(180, 200, 230, 255), width=2)
    
    sd.rectangle([a4_x1 + 40, c2_y1 + 40, a4_x2 - 40, c2_y1 + 110], fill=(30, 41, 59, 255))
    
    for bi in range(32):
        bx = a4_x1 + 70 + bi * 18
        bw_b = 3 if bi % 3 == 0 else 7
        sd.rectangle([bx, c2_y1 + 150, bx + bw_b, c2_y1 + 240], fill=(20, 30, 50, 255))
    
    sd.text((a4_x1 + 70, c2_y1 + 270), "Address: 742 Evergreen Terrace, Springfield", fill=(51, 65, 85, 255), font=font_id_txt)
    sd.text((a4_x1 + 70, c2_y1 + 310), "Issuing Authority: Department of Motor Vehicles", fill=(100, 116, 139, 255), font=font_id_txt)
    
    draw_pill(sd, (a4_x2 - 200, c2_y1 + 350, 130, 36), "BACK SIDE", get_font(14, bold=True), (15, 23, 42, 220), AMBER)
    
    sy_act = sh - 220
    actions = [
        ("🔄 Retake", DARK_SURFACE, WHITE),
        ("💾 Save to Vault", DARK_CARD, TEXT_PRIMARY),
        ("📄 Export PDF", EMERALD, BLACK)
    ]
    ax = 40
    btn_w = (sw - 80 - 32) // 3
    for b_title, bg_c, fg_c in actions:
        sd.rounded_rectangle([ax, sy_act, ax + btn_w, sy_act + 70], radius=18, fill=bg_c, outline=DARK_BORDER if bg_c != EMERALD else None)
        sd.text((ax + 24, sy_act + 22), b_title, fill=fg_c, font=get_font(18, bold=True))
        ax += btn_w + 16
    
    draw_device_mockup(img, screen, phone_box)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_6_id_card_stitch.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")


# SCREEN 7: QR Studio & Barcode Scanner
def generate_screenshot_7():
    print("Generating screenshot_7_qr_barcode_studio.png...")
    img, phone_box = create_screenshot_base(
        category="INTELLIGENT BARCODE STUDIO",
        title="QR Studio & Google Pay Auto-Zoom Scanner",
        subtitle="Auto-Zooming Barcode Scanner & Multi-Type QR Generator"
    )
    
    sw, sh = 880, 1980
    screen = Image.new('RGBA', (sw, sh), DARK_BG)
    sd = ImageDraw.Draw(screen)
    draw_status_bar(sd, sw)
    
    sy = 60
    sd.text((40, sy + 8), "QR & Barcode Studio", fill=WHITE, font=get_font(30, bold=True))
    
    sy += 75
    sd.rounded_rectangle([40, sy, sw // 2 - 10, sy + 54], radius=14, fill=EMERALD)
    sd.text((sw // 4 - 60, sy + 14), "📷 QR Scanner", fill=BLACK, font=get_font(20, bold=True))
    
    sd.rounded_rectangle([sw // 2 + 10, sy, sw - 40, sy + 54], radius=14, fill=DARK_SURFACE, outline=DARK_BORDER)
    sd.text((3 * sw // 4 - 70, sy + 14), "▦ QR Generator", fill=TEXT_SECONDARY, font=get_font(20, bold=True))
    
    sy += 80
    vf_w, vf_h = sw - 80, 780
    vf_x1, vf_y1 = 40, sy
    vf_x2, vf_y2 = vf_x1 + vf_w, vf_y1 + vf_h
    
    sd.rounded_rectangle([vf_x1, vf_y1, vf_x2, vf_y2], radius=24, fill=(15, 20, 32, 255), outline=DARK_BORDER)
    
    ret_size = 400
    rx1 = vf_x1 + (vf_w - ret_size) // 2
    ry1 = vf_y1 + (vf_h - ret_size) // 2
    rx2, ry2 = rx1 + ret_size, ry1 + ret_size
    
    sd.rounded_rectangle([rx1 + 30, ry1 + 30, rx2 - 30, ry2 - 30], radius=16, fill=WHITE)
    for q_r in range(7):
        for q_c in range(7):
            if (q_r + q_c) % 2 == 0 or (q_r < 2 and q_c < 2) or (q_r > 4 and q_c < 2):
                sd.rectangle([rx1 + 60 + q_c * 40, ry1 + 60 + q_r * 40, rx1 + 90 + q_c * 40, ry1 + 90 + q_r * 40], fill=BLACK)
    
    cr_len = 45
    cr_w = 6
    sd.line([(rx1, ry1), (rx1 + cr_len, ry1)], fill=EMERALD, width=cr_w)
    sd.line([(rx1, ry1), (rx1, ry1 + cr_len)], fill=EMERALD, width=cr_w)
    sd.line([(rx2, ry1), (rx2 - cr_len, ry1)], fill=EMERALD, width=cr_w)
    sd.line([(rx2, ry1), (rx2, ry1 + cr_len)], fill=EMERALD, width=cr_w)
    sd.line([(rx1, ry2), (rx1 + cr_len, ry2)], fill=EMERALD, width=cr_w)
    sd.line([(rx1, ry2), (rx1, ry2 - cr_len)], fill=EMERALD, width=cr_w)
    sd.line([(rx2, ry2), (rx2 - cr_len, ry2)], fill=EMERALD, width=cr_w)
    sd.line([(rx2, ry2), (rx2, ry2 - cr_len)], fill=EMERALD, width=cr_w)
    
    sd.line([(rx1 + 10, ry1 + ret_size // 2), (rx2 - 10, ry1 + ret_size // 2)], fill=LASER, width=4)
    
    zy = vf_y2 - 65
    zoom_levels = [("1.0x", False), ("2.0x (Auto)", True), ("3.5x", False)]
    zx = vf_x1 + 180
    for z_txt, is_z in zoom_levels:
        zw = 140
        bg_z = EMERALD if is_z else (15, 23, 42, 200)
        fg_z = BLACK if is_z else TEXT_PRIMARY
        sd.rounded_rectangle([zx, zy, zx + zw, zy + 44], radius=12, fill=bg_z)
        sd.text((zx + 18, zy + 10), z_txt, fill=fg_z, font=get_font(16, bold=True))
        zx += zw + 18
    
    sy = vf_y2 + 30
    draw_card_with_shadow(screen, (40, sy, sw - 40, sy + 580), radius=22, fill=DARK_CARD, border=EMERALD)
    
    draw_pill(sd, (70, sy + 30, 200, 36), "✓ UPI PAYMENT DETECTED", get_font(14, bold=True), EMERALD, BLACK)
    sd.text((70, sy + 84), "Lufick Technologies Inc.", fill=WHITE, font=get_font(28, bold=True))
    
    sd.rounded_rectangle([70, sy + 140, sw - 70, sy + 230], radius=14, fill=DARK_SURFACE, outline=DARK_BORDER)
    sd.text((90, sy + 155), "UPI ID: merchant@okaxis", fill=CYAN, font=get_font(18, bold=True))
    sd.text((90, sy + 188), "Payload: upi://pay?pa=merchant@okaxis&pn=Lufick&am=49.99", fill=TEXT_MUTED, font=get_font(15, bold=False))
    
    sd.rounded_rectangle([70, sy + 255, sw - 70, sy + 315], radius=12, fill=(16, 185, 129, 30), outline=EMERALD_DARK)
    sd.text((90, sy + 272), "🛡️ Verified Safe Link • Zero Phishing Detected", fill=EMERALD_LIGHT, font=get_font(17, bold=True))
    
    sy_qr_act = sy + 350
    qr_actions = [
        ("📋 Copy UPI", DARK_SURFACE, WHITE),
        ("💾 Save to Vault", DARK_SURFACE, TEXT_PRIMARY),
        ("⚡ Pay / Open", EMERALD, BLACK)
    ]
    qx = 70
    q_btn_w = (sw - 140 - 28) // 3
    for q_title, bg_q, fg_q in qr_actions:
        sd.rounded_rectangle([qx, sy_qr_act, qx + q_btn_w, sy_qr_act + 70], radius=16, fill=bg_q, outline=DARK_BORDER if bg_q != EMERALD else None)
        sd.text((qx + 20, sy_qr_act + 22), q_title, fill=fg_q, font=get_font(18, bold=True))
        qx += q_btn_w + 14
    
    draw_device_mockup(img, screen, phone_box)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_7_qr_barcode_studio.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")


# SCREEN 8: ML Kit OCR & Receipt Entity Parser
def generate_screenshot_8():
    print("Generating screenshot_8_ai_ocr_extractor.png...")
    img, phone_box = create_screenshot_base(
        category="100% ON-DEVICE AI OCR",
        title="On-Device ML Kit OCR & Receipt Parser",
        subtitle="Extract Text, Dates, Invoice Numbers & Amounts Offline"
    )
    
    sw, sh = 880, 1980
    screen = Image.new('RGBA', (sw, sh), DARK_BG)
    sd = ImageDraw.Draw(screen)
    draw_status_bar(sd, sw)
    
    sy = 60
    sd.text((40, sy + 8), "←  AI OCR Text Extractor", fill=WHITE, font=get_font(30, bold=True))
    
    sy += 75
    sd.rounded_rectangle([40, sy, sw // 2 - 10, sy + 54], radius=14, fill=EMERALD)
    sd.text((sw // 4 - 80, sy + 14), "⚡ Smart Extracted Data", fill=BLACK, font=get_font(20, bold=True))
    
    sd.rounded_rectangle([sw // 2 + 10, sy, sw - 40, sy + 54], radius=14, fill=DARK_SURFACE, outline=DARK_BORDER)
    sd.text((3 * sw // 4 - 70, sy + 14), "📝 Full Text Editor", fill=TEXT_SECONDARY, font=get_font(20, bold=True))
    
    sy += 80
    entities = [
        ("🏢 Merchant", "ACME Global Cloud Services", CYAN),
        ("📄 Invoice Number", "INV-2026-8941", AMBER),
        ("📅 Invoice Date", "August 31, 2026", EMERALD_LIGHT),
        ("💳 Payment Method", "Visa ending in •••• 4291", PURPLE)
    ]
    
    for e_icon, e_val, e_col in entities:
        draw_card_with_shadow(screen, (40, sy, sw - 40, sy + 100), radius=16, fill=DARK_CARD, border=DARK_BORDER)
        sd.text((68, sy + 20), e_icon, fill=TEXT_MUTED, font=get_font(17, bold=True))
        sd.text((68, sy + 52), e_val, fill=WHITE, font=get_font(22, bold=True))
        sd.text((sw - 120, sy + 34), "Copy 📋", fill=e_col, font=get_font(16, bold=True))
        sy += 120
    
    sy += 10
    draw_card_with_shadow(screen, (40, sy, sw - 40, sy + 440), radius=20, fill=DARK_CARD, border=EMERALD)
    sd.text((68, sy + 24), "ITEMIZED CHARGES & TOTALS", fill=TEXT_MUTED, font=get_font(18, bold=True))
    
    items = [
        ("1. Enterprise KMP Engine Subscription", "$999.00"),
        ("2. Dedicated Optical GPU Processing Unit", "$241.00"),
        ("3. Subtotal", "$1,240.00"),
        ("4. Applicable VAT / Tax (9%)", "$111.60")
    ]
    for idx, (item_name, item_price) in enumerate(items):
        iy = sy + 75 + idx * 52
        sd.text((68, iy), item_name, fill=TEXT_PRIMARY if idx < 2 else TEXT_SECONDARY, font=get_font(18, bold=(idx < 2)))
        sd.text((sw - 200, iy), item_price, fill=TEXT_PRIMARY, font=get_font(18, bold=True))
    
    sd.line([(68, sy + 295), (sw - 68, sy + 295)], fill=DARK_BORDER, width=2)
    
    sd.text((68, sy + 325), "GRAND TOTAL AMOUNT", fill=EMERALD_LIGHT, font=get_font(20, bold=True))
    sd.text((sw - 220, sy + 320), "$1,351.60", fill=EMERALD, font=get_font(32, bold=True))
    
    draw_pill(sd, (68, sy + 380, 260, 36), "✓ 100% OCR CONFIDENCE", get_font(14, bold=True), (15, 23, 42, 220), CYAN)
    
    sy_ocr_act = sh - 180
    sd.rounded_rectangle([40, sy_ocr_act, sw // 2 - 10, sy_ocr_act + 72], radius=18, fill=DARK_SURFACE, outline=DARK_BORDER)
    sd.text((sw // 4 - 80, sy_ocr_act + 22), "📋 Copy All Data", fill=WHITE, font=get_font(20, bold=True))
    
    sd.rounded_rectangle([sw // 2 + 10, sy_ocr_act, sw - 40, sy_ocr_act + 72], radius=18, fill=EMERALD)
    sd.text((3 * sw // 4 - 90, sy_ocr_act + 22), "📤 Export TXT / CSV", fill=BLACK, font=get_font(20, bold=True))
    
    draw_device_mockup(img, screen, phone_box)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_8_ai_ocr_extractor.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")


# ---------------------------------------------------------
# 4. STORE LISTING METADATA (store_listing.md)
# ---------------------------------------------------------
def generate_store_listing():
    print("Generating store_listing.md...")
    content = """# Google Play Store Listing Specification & Metadata

## App Details & Title
- **App Title (28 / 30 chars)**: `DocScanner: AI PDF & ID Scan`
- **Short Description (76 / 80 chars)**: `Scan docs & ID cards, extract OCR text, create secure PDFs & scan QR codes.`
- **Default Language**: English (United States) (en-US)
- **App Category**: Productivity / Business / Tools
- **Content Rating**: Everyone (3+)

---

## Full Description (Rich Markdown)

Transform your smartphone into an ultra-fast, 100% private, AI-powered document scanner and PDF studio with **DocScanner KMP**. Digitizing paperwork, extracting invoice data, stitching 2-in-1 ID cards, signing documents, and encrypting PDFs has never been easier or more secure.

### 🛡️ 100% On-Device Privacy Guarantee
Unlike other document scanners that upload your sensitive documents to remote cloud servers, **DocScanner processes everything strictly on your device hardware**.
- Zero cloud uploads or external server transmissions
- Zero logging, tracking, or document analytics
- 100% functional in offline and Airplane mode

---

### 🌟 Key Features & Core Superpowers

#### ⚡ 1. Smart Camera Scanner & Live Edge Detection
- **Auto-Capture & Real-Time Laser**: Instant quad edge detection automatically tracks page corners and snaps crystal-clear photos the moment you hold still.
- **Multi-Page Batch Mode**: Scan dozens of receipts, contracts, books, or notes in seconds with continuous high-speed capture.
- **Multi-Document Guide Modes**: Tailored viewfinder guides for Documents, 2-in-1 ID Cards, Passports, Books, and QR codes.

#### 📐 2. Precision 4-Point Homography Perspective Dewarp
- **Direct Linear Transform Solver**: Flattens skewed angles, curled pages, and tilted camera shots with mathematical accuracy.
- **Interactive Corner Magnifying Loupe**: Zoom in 2.5x with tactile precision crosshairs for sub-pixel edge alignment.
- **Standard Aspect Ratios**: Instant one-tap crop to A4, US Letter, Legal, ID Card, or 1:1 Square formats.

#### 🎛️ 3. 120 FPS GPU ColorMatrix Filter Pipeline
- **Magic Color 1 & 2**: Proprietary enhancement shaders that eliminate shadows, whiten backgrounds, and make text pop with laser clarity.
- **Sharp B&W & Eco Print**: High-contrast black & white filters optimized for faxing, printing, and minimal toner consumption.
- **Fine-Tuning Sliders**: Full manual control over Contrast, Brightness, Saturation, and Sharpness.
- **Hold to Compare**: Instant real-time comparison with the original camera frame.

#### 🪪 4. 2-in-1 ID Card Double-Sided Stitching
- **Seamless Dual-Side Capture**: Guided frames walk you through capturing both the front and back of your Driver's License, National ID, or Passport.
- **Automatic A4 Canvas Layout**: Instantly stitches both sides with perfect alignment, official margins, and print guidelines onto a single A4 page.

#### 🔒 5. Enterprise PDF Suite, AES-256 Lock & E-Sign
- **Vector E-Signature Pad**: Draw smooth digital signatures and position them anywhere on your documents.
- **Military-Grade AES Password Encryption**: Lock sensitive PDFs with hardware-backed AES-256 passwords.
- **Custom Watermarking**: Protect your intellectual property with custom text stamps (e.g., *CONFIDENTIAL*, *COPY*).
- **Resolution Control**: Export in Ultra (300 DPI), High (150 DPI), Medium, or Compressed Low sizes.

#### 🤖 6. On-Device AI OCR Text Extractor & Invoice Parser
- **100% Offline Text Recognition**: Powered by Google ML Kit to extract text from images in milliseconds without internet access.
- **Smart Entity Heuristics**: Automatically extracts Merchant Name, Invoice Number, Transaction Date, Subtotal, Taxes, and Grand Total.
- **One-Tap Export**: Copy extracted text directly or export structured TXT and CSV records.

#### ▦ 7. QR & Barcode Studio
- **Google Pay Style Auto-Zoom**: High-speed viewfinder with dynamic optical zoom (1.0x, 2.0x, 3.5x) for distant or tiny barcodes.
- **Multi-Type QR Generator**: Create custom QR codes for URLs, WiFi networks, UPI payments, VCards, and SMS.
- **Secure QR Vault**: Store and categorize your scanned barcodes and generated codes offline.

---

### 📂 File Formats & Compatibility
- **Export Formats**: PDF (Multi-Page & Encrypted), JPEG, PNG, TXT, CSV.
- **Page Standards**: A4, US Letter, Legal, Executive, ID Card.
- **Supported Barcodes**: QR Code, Aztec, Data Matrix, UPC-A, UPC-E, EAN-8, EAN-13, Code 39, Code 128, ITF.

---

### 🔑 Targeted Keywords & Search Tags
Document Scanner, PDF Scanner, CamScanner Alternative, Adobe Scan, OCR Text Extractor, ID Card Scanner, 2-in-1 ID Scan, Scan to PDF, PDF Password, Offline Scanner, Receipt Scanner, Invoice Parser, QR Scanner, Barcode Reader, E-Signature, Watermark PDF, On-Device AI.

---

## What's New in Version 1.0.0 (Release Notes)

🎉 **Welcome to the Initial Release of DocScanner KMP v1.0.0!**
- 🚀 **Initial Production Release**: High-speed, 100% private document studio built with Kotlin Multiplatform and Compose Multiplatform.
- ⚡ **Real-Time Edge Detection**: Live laser scanning with automatic page capture.
- 📐 **Perspective Dewarp**: 4-point homography solver with magnifying corner loupe.
- 🎛️ **GPU Filters**: Magic Color, Sharp B&W, Eco Print, and real-time contrast/brightness sliders.
- 🪪 **2-in-1 ID Card Mode**: Front and back double-sided scanning onto a single A4 sheet.
- 🔒 **PDF Security Suite**: AES-256 password encryption, custom watermarking, and vector e-signatures.
- 🤖 **On-Device ML Kit OCR**: Extract text, receipts, and invoice entities offline.
- ▦ **QR Studio**: Auto-zooming barcode scanner and multi-type QR code generator.
- 🛡️ **100% On-Device Guarantee**: Zero data tracking and zero cloud uploads.
"""
    out_path = os.path.join(OUTPUT_DIR, "store_listing.md")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(content.strip() + "\n")
    print(f"Saved: {out_path}")


# ---------------------------------------------------------
# MAIN EXECUTION PIPELINE
# ---------------------------------------------------------
def main():
    print("==================================================")
    print("DocScanner KMP - Play Store Asset Generator")
    print("Output directory:", OUTPUT_DIR)
    print("==================================================")
    
    generate_app_icon()
    generate_feature_graphic()
    generate_screenshot_1()
    generate_screenshot_2()
    generate_screenshot_3()
    generate_screenshot_4()
    generate_screenshot_5()
    generate_screenshot_6()
    generate_screenshot_7()
    generate_screenshot_8()
    generate_store_listing()
    
    print("==================================================")
    print("All Play Store release assets generated successfully!")
    print("==================================================")

if __name__ == "__main__":
    main()
