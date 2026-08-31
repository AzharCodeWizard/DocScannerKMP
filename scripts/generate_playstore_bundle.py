import os
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ASSET_DIR = "/Users/azhar/Documents/Projects/DocScannerKMP/playstore_assets"
BRAIN_DIR = "/Users/azhar/.gemini/antigravity/brain/81fde130-f754-4350-930f-7487b6d61eb7"
os.makedirs(ASSET_DIR, exist_ok=True)

# Color Palette
BG_DARK = (11, 15, 25) # #0B0F19
BG_CARD = (19, 26, 43) # #131A2B
ACCENT_EMERALD = (0, 200, 116) # #00C874
ACCENT_MINT = (77, 245, 172)
TEXT_WHITE = (255, 255, 255)
TEXT_GRAY = (156, 163, 175)
BORDER_COLOR = (30, 41, 59)

def get_font(size, bold=False):
    # Try system fonts
    font_paths = [
        "/System/Library/Fonts/SFProText-Bold.otf" if bold else "/System/Library/Fonts/SFProText-Regular.otf",
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf" if bold else "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
        "/System/Library/Fonts/SFCompact.ttf"
    ]
    for path in font_paths:
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, size)
            except Exception:
                pass
    return ImageFont.load_default()

def create_app_icon():
    print("Generating App Icon (512x512)...")
    size = (512, 512)
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Background squircle
    margin = 16
    draw.rounded_rectangle(
        [margin, margin, size[0] - margin, size[1] - margin],
        radius=110,
        fill=BG_DARK,
        outline=ACCENT_EMERALD,
        width=8
    )

    # Inner subtle glow
    draw.rounded_rectangle(
        [margin + 12, margin + 12, size[0] - margin - 12, size[1] - margin - 12],
        radius=98,
        outline=(0, 200, 116, 40),
        width=4
    )

    # Document shape
    doc_left, doc_top = 136, 100
    doc_w, doc_h = 240, 312
    draw.rounded_rectangle(
        [doc_left, doc_top, doc_left + doc_w, doc_top + doc_h],
        radius=24,
        fill=BG_CARD,
        outline=(255, 255, 255, 180),
        width=4
    )

    # Folded corner in top-right
    corner_size = 48
    draw.polygon([
        (doc_left + doc_w - corner_size, doc_top),
        (doc_left + doc_w, doc_top + corner_size),
        (doc_left + doc_w - corner_size, doc_top + corner_size)
    ], fill=ACCENT_EMERALD)

    # Document text lines
    line_y = doc_top + 70
    for i in range(4):
        w = 160 if i % 2 == 0 else 120
        draw.rounded_rectangle(
            [doc_left + 40, line_y, doc_left + 40 + w, line_y + 12],
            radius=6,
            fill=(255, 255, 255, 140 if i > 0 else 220)
        )
        line_y += 32

    # Glowing Scanner Laser
    laser_y = 260
    draw.rectangle([doc_left - 10, laser_y, doc_left + doc_w + 10, laser_y + 6], fill=ACCENT_EMERALD)
    draw.ellipse([doc_left - 16, laser_y - 2, doc_left - 4, laser_y + 8], fill=ACCENT_MINT)
    draw.ellipse([doc_left + doc_w + 4, laser_y - 2, doc_left + doc_w + 16, laser_y + 8], fill=ACCENT_MINT)

    # Badge in bottom right (Camera / AI Spark)
    badge_x, badge_y = 310, 310
    draw.ellipse([badge_x, badge_y, badge_x + 96, badge_y + 96], fill=ACCENT_EMERALD)
    draw.ellipse([badge_x + 6, badge_y + 6, badge_x + 90, badge_y + 90], fill=BG_DARK)
    draw.ellipse([badge_x + 22, badge_y + 22, badge_x + 74, badge_y + 74], fill=ACCENT_EMERALD)

    out_path = os.path.join(ASSET_DIR, "icon_512x512.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")

def create_feature_graphic():
    print("Generating Feature Graphic (1024x500)...")
    width, height = 1024, 500
    img = Image.new("RGBA", (width, height), BG_DARK)
    draw = ImageDraw.Draw(img)

    # Subtle gradient or geometric lines
    for i in range(0, width, 40):
        draw.line([(i, 0), (i + 120, height)], fill=(20, 28, 48), width=1)

    # Left content: Title, Subtitle, Badges
    title_font = get_font(52, bold=True)
    sub_font = get_font(24, bold=False)
    badge_font = get_font(18, bold=True)

    draw.text((60, 80), "DocScanner", fill=TEXT_WHITE, font=title_font)
    draw.text((360, 96), "AI", fill=ACCENT_EMERALD, font=get_font(32, bold=True))
    draw.text((60, 150), "All-in-One Mobile Document Scanner & OCR", fill=TEXT_GRAY, font=sub_font)

    # Feature checklist pills
    features = [
        "✓ Auto-Edge Detection & Dewarping",
        "✓ GPU Magic Color & B&W Filters",
        "✓ On-Device Multi-Language OCR",
        "✓ PDF AES Lock, Watermark & E-Sign",
        "✓ 2-in-1 ID Card Stitched Scans"
    ]
    pill_y = 210
    for feat in features:
        draw.rounded_rectangle([60, pill_y, 480, pill_y + 40], radius=10, fill=BG_CARD, outline=BORDER_COLOR, width=1)
        draw.text((76, pill_y + 9), feat, fill=TEXT_WHITE, font=badge_font)
        pill_y += 50

    # Right side: App Mockup Banner Preview
    preview_box = [530, 40, 970, 460]
    draw.rounded_rectangle(preview_box, radius=24, fill=BG_CARD, outline=ACCENT_EMERALD, width=3)

    # Inner screenshot or document graphic
    src_img_path = os.path.join(BRAIN_DIR, "device_screenshot_home_with_saved_card_final.png")
    if os.path.exists(src_img_path):
        try:
            device_img = Image.open(src_img_path)
            device_img = device_img.resize((380, 800), Image.Resampling.LANCZOS)
            # Crop to fit inside preview
            cropped = device_img.crop((0, 80, 380, 460))
            img.paste(cropped, (560, 60))
        except Exception as e:
            print(f"Error loading device screenshot: {e}")

    out_path = os.path.join(ASSET_DIR, "feature_graphic_1024x500.png")
    img.save(out_path, "PNG")
    print(f"Saved: {out_path}")

def create_store_screenshot_mockup(filename, title, subtitle, source_screenshot_name):
    print(f"Generating Screenshot Mockup: {filename}...")
    width, height = 1080, 2400
    canvas = Image.new("RGBA", (width, height), BG_DARK)
    draw = ImageDraw.Draw(canvas)

    # Header Titles
    title_font = get_font(68, bold=True)
    sub_font = get_font(36, bold=False)

    # Centered Title & Subtitle
    title_w = draw.textlength(title, font=title_font)
    draw.text(((width - title_w) / 2, 140), title, fill=TEXT_WHITE, font=title_font)

    sub_w = draw.textlength(subtitle, font=sub_font)
    draw.text(((width - sub_w) / 2, 235), subtitle, fill=ACCENT_EMERALD, font=sub_font)

    # Device Mockup Frame
    frame_left = 90
    frame_top = 340
    frame_w = 900
    frame_h = 1960

    draw.rounded_rectangle(
        [frame_left - 12, frame_top - 12, frame_left + frame_w + 12, frame_top + frame_h + 12],
        radius=54,
        fill=(25, 33, 52),
        outline=ACCENT_EMERALD,
        width=4
    )

    # Embed Actual Screenshot
    src_path = os.path.join(BRAIN_DIR, source_screenshot_name)
    if os.path.exists(src_path):
        try:
            shot = Image.open(src_path).convert("RGBA")
            shot_resized = shot.resize((frame_w, frame_h), Image.Resampling.LANCZOS)
            
            # Mask with rounded corners
            mask = Image.new("L", (frame_w, frame_h), 0)
            mask_draw = ImageDraw.Draw(mask)
            mask_draw.rounded_rectangle([0, 0, frame_w, frame_h], radius=44, fill=255)

            canvas.paste(shot_resized, (frame_left, frame_top), mask)
        except Exception as e:
            print(f"Error embedding {source_screenshot_name}: {e}")
    else:
        # Fallback card
        draw.rounded_rectangle([frame_left, frame_top, frame_left + frame_w, frame_top + frame_h], radius=44, fill=BG_CARD)
        draw.text((frame_left + 100, frame_top + 400), "Screen Preview", fill=TEXT_WHITE, font=title_font)

    out_path = os.path.join(ASSET_DIR, filename)
    canvas.save(out_path, "PNG")
    print(f"Saved: {out_path}")

def generate_all():
    create_app_icon()
    create_feature_graphic()

    # 8 High-Res Play Store Screenshots
    screens = [
        ("01_home_dashboard.png", "Smart Document Manager", "Organize with Folders, Tags & Fast Search", "device_screenshot_home_with_saved_card_final.png"),
        ("02_camera_scanner.png", "AI Edge Detection", "Auto-Framing & Real-Time Laser Guidance", "device_screenshot_camera_live_capture.png"),
        ("03_adjust_crop.png", "Precision 8-Point Crop", "Perspective Dewarp & Fine Angle Adjustment", "device_screenshot_crop_review.png"),
        ("04_color_filters.png", "GPU Color Enhancement", "Magic Color, B&W & Sharp Contrast Sliders", "device_screenshot_filter_stage_verified.png"),
        ("05_pdf_tools_esign.png", "PDF Tools & Security", "AES Password Lock, Watermark & E-Sign", "device_screenshot_ocr_screen.png"),
        ("06_document_detail.png", "Multi-Page Management", "Multi-Page Strip, Reorder & Instant Share", "device_screenshot_doc_detail.png"),
        ("07_qr_barcode_studio.png", "QR & Barcode Studio", "Google Pay Style Auto-Zoom & Generator", "device_screenshot_qr_autozoom_active.png"),
        ("08_settings_customization.png", "Themes & Privacy", "AMOLED Dark Mode & 100% Offline Security", "device_screenshot_settings_screen_open_success.png")
    ]

    for filename, title, sub, src in screens:
        create_store_screenshot_mockup(filename, title, sub, src)

if __name__ == "__main__":
    generate_all()
