import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

# Target Dimensions
FINAL_W = 1080
FINAL_H = 1920
SCALE = 2  # Supersampling 2x for subpixel rendering quality
W = FINAL_W * SCALE
H = FINAL_H * SCALE

# Output Directory
OUTPUT_DIR = r"C:\Users\Soumya\AndroidStudioProjects\MonthlyLekka"

# Color Tokens (Matching Monthly Lekka Theme)
C_DEEP_EMERALD = (6, 78, 59)       # #064E3B
C_MODERN_MINT  = (16, 185, 129)    # #10B981
C_LIGHT_MINT   = (236, 253, 245)   # #ECFDF5
C_SLATE_DARK   = (15, 23, 42)      # #0F172A
C_SURFACE_SLATE= (30, 41, 59)      # #1E293B
C_LIGHT_BG     = (248, 250, 252)   # #F8FAFC
C_TEAL_ACCENT  = (13, 148, 136)    # #0D9488
C_INCOME_GREEN = (16, 185, 129)    # #10B981
C_EXPENSE_CORAL= (239, 68, 68)     # #EF4444
C_CORAL_LIGHT  = (254, 226, 226)   # #FEE2E2
C_WHITE        = (255, 255, 255)
C_BORDER_GRAY  = (226, 232, 240)   # #E2E8F0
C_TEXT_DARK    = (15, 23, 42)
C_TEXT_MUTED   = (100, 116, 139)   # #64748B
C_CARD_BG      = (255, 255, 255)

# Category Colors
CAT_FOOD       = (229, 57, 53)     # Red #E53935
CAT_TRANSPORT  = (30, 136, 229)    # Blue #1E88E5
CAT_SHOPPING   = (255, 179, 0)     # Yellow #FFB300
CAT_HEALTH     = (67, 160, 71)     # Green #43A047
CAT_ENT        = (142, 36, 170)    # Purple #8E24AA
CAT_UTILITIES  = (13, 148, 136)    # Teal #0D9488

# Load Fonts
FONT_PATH_REG = "C:/Windows/Fonts/segoeui.ttf"
FONT_PATH_BOLD = "C:/Windows/Fonts/segoeuib.ttf"
FONT_PATH_LIGHT = "C:/Windows/Fonts/segoeuil.ttf"

def font(size, bold=False, light=False):
    s = int(size * SCALE)
    path = FONT_PATH_BOLD if bold else (FONT_PATH_LIGHT if light else FONT_PATH_REG)
    try:
        return ImageFont.truetype(path, s)
    except:
        return ImageFont.load_default()

def draw_rounded_rect_with_shadow(image, draw, box, radius, fill, outline=None, width=1, shadow_blur=10, shadow_offset=(0,4), shadow_color=(0,0,0,30)):
    x1, y1, x2, y2 = [b * SCALE for b in box]
    rad = radius * SCALE
    bw = width * SCALE
    sb = shadow_blur * SCALE
    ox, oy = shadow_offset[0] * SCALE, shadow_offset[1] * SCALE

    shadow_img = Image.new("RGBA", (image.width, image.height), (0,0,0,0))
    sdraw = ImageDraw.Draw(shadow_img)
    sbox = (x1 + ox, y1 + oy, x2 + ox, y2 + oy)
    sdraw.rounded_rectangle(sbox, radius=rad, fill=shadow_color)
    shadow_img = shadow_img.filter(ImageFilter.GaussianBlur(sb))

    image.alpha_composite(shadow_img)
    draw.rounded_rectangle((x1, y1, x2, y2), radius=rad, fill=fill, outline=outline, width=bw)

def draw_status_bar(draw, mode='light'):
    tc = C_TEXT_DARK if mode == 'light' else C_WHITE
    draw.text((40 * SCALE, 20 * SCALE), "09:41", font=font(18, bold=True), fill=tc)

    rx = W - 140 * SCALE
    draw.rounded_rectangle((rx, 26 * SCALE, rx + 45 * SCALE, 46 * SCALE), radius=4 * SCALE, outline=tc, width=2 * SCALE)
    draw.rounded_rectangle((rx + 4 * SCALE, 30 * SCALE, rx + 35 * SCALE, 42 * SCALE), radius=2 * SCALE, fill=tc)
    draw.rectangle((rx + 46 * SCALE, 32 * SCALE, rx + 49 * SCALE, 40 * SCALE), fill=tc)
    draw.text((rx - 60 * SCALE, 20 * SCALE), "📶", font=font(16), fill=tc)
    draw.text((rx - 110 * SCALE, 20 * SCALE), "5G", font=font(16, bold=True), fill=tc)

def draw_bottom_nav(draw, img, active_index=0):
    nav_h = 120 * SCALE
    y1 = H - nav_h
    draw_rounded_rect_with_shadow(img, draw, (0, H//SCALE - 120, W//SCALE, H//SCALE), radius=0, fill=C_WHITE, outline=C_BORDER_GRAY, shadow_blur=12, shadow_offset=(0,-4), shadow_color=(0,0,0,15))

    tabs = [
        ("🏠", "Home"),
        ("📋", "Transactions"),
        ("📊", "Analytics"),
        ("⚙️", "Settings")
    ]

    tab_w = W // len(tabs)
    for i, (icon, label) in enumerate(tabs):
        cx = i * tab_w + tab_w // 2
        cy = y1 + 30 * SCALE

        is_active = (i == active_index)

        if is_active:
            pill_w = 110 * SCALE
            pill_h = 50 * SCALE
            draw.rounded_rectangle((cx - pill_w//2, cy - 10*SCALE, cx + pill_w//2, cy + pill_h - 10*SCALE), radius=25*SCALE, fill=C_LIGHT_MINT)
            lbl_color = C_DEEP_EMERALD
            bold_flag = True
        else:
            lbl_color = C_TEXT_MUTED
            bold_flag = False

        draw.text((cx - 16 * SCALE, cy - 4 * SCALE), icon, font=font(18), fill=lbl_color)

        bbox = font(12, bold=bold_flag).getbbox(label)
        lw = bbox[2] - bbox[0]
        draw.text((cx - lw//2, cy + 44 * SCALE), label, font=font(12, bold=bold_flag), fill=lbl_color)

# SCREEN 1: WELCOME & MASTER TABLE
def render_screenshot_1():
    img = Image.new("RGBA", (W, H), C_LIGHT_BG + (255,))
    draw = ImageDraw.Draw(img)

    draw_status_bar(draw, mode='light')

    # Top Bar / Profile Header
    draw.text((40 * SCALE, 100 * SCALE), "Monthly Lekka", font=font(26, bold=True), fill=C_DEEP_EMERALD)
    draw.text((40 * SCALE, 145 * SCALE), "Track & Manage Monthly Expenses", font=font(14), fill=C_TEXT_MUTED)

    # Profile avatar
    avatar_x = W - 110 * SCALE
    draw.ellipse((avatar_x, 100 * SCALE, avatar_x + 60 * SCALE, 160 * SCALE), fill=C_DEEP_EMERALD)
    draw.text((avatar_x + 18 * SCALE, 108 * SCALE), "A", font=font(22, bold=True), fill=C_LIGHT_MINT)

    # Master Table Hero Card
    card_box = (40, 200, 500, 480)
    draw_rounded_rect_with_shadow(img, draw, card_box, radius=24, fill=C_DEEP_EMERALD, shadow_blur=16, shadow_offset=(0,8), shadow_color=(6,78,59,60))

    # Card Header
    draw.rounded_rectangle((70 * SCALE, 230 * SCALE, 260 * SCALE, 270 * SCALE), radius=12 * SCALE, fill=(16, 185, 129, 60))
    draw.text((85 * SCALE, 238 * SCALE), "OCTOBER 2024 MASTER", font=font(11, bold=True), fill=C_LIGHT_MINT)

    draw.text((70 * SCALE, 290 * SCALE), "Total Net Remaining", font=font(13), fill=(200, 230, 215))
    draw.text((70 * SCALE, 320 * SCALE), "₹ 42,650.00", font=font(34, bold=True), fill=C_WHITE)

    # Split Stats
    draw.line([(70 * SCALE, 395 * SCALE), ((FINAL_W - 70) * SCALE, 395 * SCALE)], fill=(16, 185, 129, 80), width=1*SCALE)

    draw.text((70 * SCALE, 415 * SCALE), "TOTAL BUDGET", font=font(11, bold=True), fill=(180, 220, 200))
    draw.text((70 * SCALE, 435 * SCALE), "₹ 85,000.00", font=font(18, bold=True), fill=C_WHITE)

    draw.text((310 * SCALE, 415 * SCALE), "TOTAL SPENT", font=font(11, bold=True), fill=(255, 180, 180))
    draw.text((310 * SCALE, 435 * SCALE), "₹ 42,350.00", font=font(18, bold=True), fill=C_CORAL_LIGHT)

    # Active Cycle Card
    cyc_box = (40, 505, 500, 645)
    draw_rounded_rect_with_shadow(img, draw, cyc_box, radius=20, fill=C_WHITE, outline=C_MODERN_MINT, width=2, shadow_blur=12, shadow_offset=(0,4), shadow_color=(0,0,0,12))

    draw.text((70 * SCALE, 525 * SCALE), "🔄 Active Billing Cycle", font=font(16, bold=True), fill=C_SLATE_DARK)

    # Pill badge
    draw.rounded_rectangle((350 * SCALE, 525 * SCALE, 470 * SCALE, 555 * SCALE), radius=15 * SCALE, fill=C_LIGHT_MINT)
    draw.text((365 * SCALE, 530 * SCALE), "12 Days Left", font=font(11, bold=True), fill=C_DEEP_EMERALD)

    draw.text((70 * SCALE, 560 * SCALE), "Period: Oct 01, 2024 — Oct 31, 2024", font=font(13), fill=C_TEXT_MUTED)

    # Budget progress bar
    draw.text((70 * SCALE, 590 * SCALE), "Cycle Usage (49.8% Spent)", font=font(12, bold=True), fill=C_TEXT_DARK)
    draw.rounded_rectangle((70 * SCALE, 615 * SCALE, 470 * SCALE, 630 * SCALE), radius=8 * SCALE, fill=C_BORDER_GRAY)
    draw.rounded_rectangle((70 * SCALE, 615 * SCALE, 269 * SCALE, 630 * SCALE), radius=8 * SCALE, fill=C_MODERN_MINT)

    # Quick Action Buttons Grid
    draw.text((40 * SCALE, 670 * SCALE), "Quick Actions", font=font(18, bold=True), fill=C_SLATE_DARK)

    actions = [
        ("➕ Add Expense", C_DEEP_EMERALD, C_WHITE),
        ("📊 Analytics", C_LIGHT_MINT, C_DEEP_EMERALD),
        ("📅 New Cycle", C_LIGHT_MINT, C_DEEP_EMERALD),
        ("📤 Export Data", C_LIGHT_MINT, C_DEEP_EMERALD)
    ]

    btn_w = 220
    btn_h = 50
    for idx, (label, bg, fg) in enumerate(actions):
        bx = 40 if idx % 2 == 0 else 280
        by = 710 if idx < 2 else 775
        draw_rounded_rect_with_shadow(img, draw, (bx, by, bx + btn_w, by + btn_h), radius=16, fill=bg, shadow_blur=8, shadow_offset=(0,2), shadow_color=(0,0,0,15))

        bbox = font(14, bold=True).getbbox(label)
        lw = bbox[2] - bbox[0]
        draw.text(((bx + btn_w//2) * SCALE - lw//2, (by + 14) * SCALE), label, font=font(14, bold=True), fill=fg)

    # Active Expense Tables List
    draw.text((40 * SCALE, 855 * SCALE), "Active Expense Tables", font=font(18, bold=True), fill=C_SLATE_DARK)

    tables_data = [
        ("🛒 Household & Groceries", "₹ 18,400.00 / ₹ 25,000", "73.6%", C_EXPENSE_CORAL, 0.736),
        ("🚗 Travel & Fuel", "₹ 8,200.00 / ₹ 12,000", "68.3%", C_MODERN_MINT, 0.683),
        ("📶 Subscriptions & Bills", "₹ 5,750.00 / ₹ 8,000", "71.8%", C_MODERN_MINT, 0.718)
    ]

    y_start = 895
    for name, spent_str, pct_str, bar_color, ratio in tables_data:
        box = (40, y_start, 500, y_start + 85)
        draw_rounded_rect_with_shadow(img, draw, box, radius=16, fill=C_WHITE, shadow_blur=10, shadow_offset=(0,3), shadow_color=(0,0,0,10))

        draw.text((65 * SCALE, (y_start + 14) * SCALE), name, font=font(15, bold=True), fill=C_TEXT_DARK)
        draw.text(((FINAL_W - 190) * SCALE, (y_start + 14) * SCALE), pct_str, font=font(14, bold=True), fill=bar_color)

        draw.text((65 * SCALE, (y_start + 40) * SCALE), spent_str, font=font(13), fill=C_TEXT_MUTED)

        # Mini Progress Bar
        draw.rounded_rectangle((65 * SCALE, (y_start + 65) * SCALE, 475 * SCALE, (y_start + 73) * SCALE), radius=4 * SCALE, fill=C_BORDER_GRAY)
        draw.rounded_rectangle((65 * SCALE, (y_start + 65) * SCALE, (65 + int(410 * ratio)) * SCALE, (y_start + 73) * SCALE), radius=4 * SCALE, fill=bar_color)

        y_start += 98

    draw_bottom_nav(draw, img, active_index=0)

    # Save
    out_img = img.resize((FINAL_W, FINAL_H), Image.Resampling.LANCZOS)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_1_welcome.png")
    out_img.save(out_path, "PNG")
    print(f"Saved: {out_path}")

# SCREEN 2: TABLE DETAILS & TRANSACTIONS LIST
def render_screenshot_2():
    img = Image.new("RGBA", (W, H), C_LIGHT_BG + (255,))
    draw = ImageDraw.Draw(img)

    draw_status_bar(draw, mode='light')

    # Top Bar
    draw.text((40 * SCALE, 95 * SCALE), "←", font=font(24, bold=True), fill=C_SLATE_DARK)
    draw.text((85 * SCALE, 98 * SCALE), "Household & Groceries Table", font=font(20, bold=True), fill=C_SLATE_DARK)
    draw.text((W - 120 * SCALE, 98 * SCALE), "🔍", font=font(20), fill=C_SLATE_DARK)
    draw.text((W - 65 * SCALE, 98 * SCALE), "📤", font=font(20), fill=C_SLATE_DARK)

    # Summary Banner Card
    draw_rounded_rect_with_shadow(img, draw, (40, 150, 500, 230), radius=20, fill=C_DEEP_EMERALD, shadow_blur=12, shadow_offset=(0,4), shadow_color=(6,78,59,40))
    draw.text((70 * SCALE, 168 * SCALE), "Table Total Spend", font=font(12), fill=(180, 220, 200))
    draw.text((70 * SCALE, 188 * SCALE), "₹ 18,400.00", font=font(26, bold=True), fill=C_WHITE)
    draw.text((320 * SCALE, 172 * SCALE), "24 Transactions", font=font(13, bold=True), fill=C_LIGHT_MINT)
    draw.text((320 * SCALE, 195 * SCALE), "Avg: ₹ 766 / entry", font=font(12), fill=(200, 230, 215))

    # Search & Filter Chips Bar
    draw_rounded_rect_with_shadow(img, draw, (40, 245, 500, 290), radius=14, fill=C_WHITE, outline=C_BORDER_GRAY, shadow_blur=6, shadow_offset=(0,2), shadow_color=(0,0,0,10))
    draw.text((60 * SCALE, 258 * SCALE), "🔍 Search description, amount...", font=font(13), fill=C_TEXT_MUTED)

    # Category Filter Chips
    chips = [("All", True), ("🛒 Groceries", False), ("⚡ Utilities", False), ("🍔 Dining", False)]
    cx = 40
    for label, is_sel in chips:
        bg = C_DEEP_EMERALD if is_sel else C_WHITE
        fg = C_WHITE if is_sel else C_TEXT_DARK
        border = C_DEEP_EMERALD if is_sel else C_BORDER_GRAY
        cw = int(font(12, bold=is_sel).getbbox(label)[2] / SCALE) + 24
        draw_rounded_rect_with_shadow(img, draw, (cx, 302, cx + cw, 332), radius=15, fill=bg, outline=border, shadow_blur=4, shadow_offset=(0,1), shadow_color=(0,0,0,8))
        draw.text(((cx + 12) * SCALE, 310 * SCALE), label, font=font(12, bold=is_sel), fill=fg)
        cx += cw + 10

    # 4-Column Structured Table
    # Table Header Row
    th_box = (40, 345, 500, 385)
    draw_rounded_rect_with_shadow(img, draw, th_box, radius=12, fill=C_SURFACE_SLATE, shadow_blur=6, shadow_offset=(0,2), shadow_color=(0,0,0,15))

    draw.text((55 * SCALE, 358 * SCALE), "DATE", font=font(11, bold=True), fill=C_LIGHT_MINT)
    draw.text((120 * SCALE, 358 * SCALE), "CATEGORY", font=font(11, bold=True), fill=C_LIGHT_MINT)
    draw.text((230 * SCALE, 358 * SCALE), "DESCRIPTION", font=font(11, bold=True), fill=C_LIGHT_MINT)
    draw.text((410 * SCALE, 358 * SCALE), "AMOUNT", font=font(11, bold=True), fill=C_LIGHT_MINT)

    # 10 Detailed Transaction Rows
    transactions = [
        ("Oct 18", "Groceries", "Supermarket Stock", "₹ 4,250.00", CAT_FOOD),
        ("Oct 17", "Utilities", "Electricity Bill", "₹ 2,180.00", CAT_UTILITIES),
        ("Oct 16", "Dining", "Weekend Dinner", "₹ 1,450.00", CAT_FOOD),
        ("Oct 15", "Fuel", "Shell Petrol Pump", "₹ 2,500.00", CAT_TRANSPORT),
        ("Oct 14", "Health", "Pharmacy Medicines", "₹ 890.00", CAT_HEALTH),
        ("Oct 12", "Cinema", "Movie & Snacks", "₹ 1,200.00", CAT_ENT),
        ("Oct 10", "Apparel", "Clothing Store", "₹ 3,400.00", CAT_SHOPPING),
        ("Oct 08", "Wifi", "Fiber Broadband", "₹ 999.00", CAT_UTILITIES),
        ("Oct 05", "Cafe", "Coffee & Pastry", "₹ 340.00", CAT_FOOD),
        ("Oct 02", "Home", "Laundry & Cleaning", "₹ 1,191.00", CAT_SHOPPING),
    ]

    ty = 395
    for i, (dt, cat, desc, amt, cat_col) in enumerate(transactions):
        bg_col = C_WHITE if i % 2 == 0 else (241, 245, 249)
        r_box = (40, ty, 500, ty + 50)
        draw_rounded_rect_with_shadow(img, draw, r_box, radius=10, fill=bg_col, outline=C_BORDER_GRAY, shadow_blur=4, shadow_offset=(0,1), shadow_color=(0,0,0,5))

        # Date
        draw.text((52 * SCALE, (ty + 15) * SCALE), dt, font=font(12, bold=True), fill=C_TEXT_MUTED)

        # Category Pill
        draw.rounded_rectangle((115 * SCALE, (ty + 12) * SCALE, 215 * SCALE, (ty + 38) * SCALE), radius=10 * SCALE, fill=(cat_col[0], cat_col[1], cat_col[2], 30))
        draw.text((122 * SCALE, (ty + 16) * SCALE), cat[:8], font=font(11, bold=True), fill=cat_col)

        # Description
        draw.text((225 * SCALE, (ty + 15) * SCALE), desc[:15], font=font(12), fill=C_TEXT_DARK)

        # Amount
        draw.text((405 * SCALE, (ty + 15) * SCALE), amt, font=font(12, bold=True), fill=C_EXPENSE_CORAL)

        ty += 54

    # Floating Action Button (FAB)
    fab_x, fab_y = 380, 880
    draw_rounded_rect_with_shadow(img, draw, (fab_x, fab_y, fab_x + 110, fab_y + 48), radius=24, fill=C_DEEP_EMERALD, shadow_blur=14, shadow_offset=(0,6), shadow_color=(6,78,59,60))
    draw.text(((fab_x + 16) * SCALE, (fab_y + 12) * SCALE), "➕ Add Row", font=font(13, bold=True), fill=C_WHITE)

    draw_bottom_nav(draw, img, active_index=1)

    # Save
    out_img = img.resize((FINAL_W, FINAL_H), Image.Resampling.LANCZOS)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_2_transactions.png")
    out_img.save(out_path, "PNG")
    print(f"Saved: {out_path}")

# SCREEN 3: INTERACTIVE PIE CHART & ANALYTICS
def render_screenshot_3():
    img = Image.new("RGBA", (W, H), C_LIGHT_BG + (255,))
    draw = ImageDraw.Draw(img)

    draw_status_bar(draw, mode='light')

    # Top Bar
    draw.text((40 * SCALE, 95 * SCALE), "Category Analytics", font=font(22, bold=True), fill=C_DEEP_EMERALD)

    # Month Selector Pill
    draw_rounded_rect_with_shadow(img, draw, (340, 90, 500, 130), radius=18, fill=C_WHITE, outline=C_BORDER_GRAY, shadow_blur=6, shadow_offset=(0,2), shadow_color=(0,0,0,10))
    draw.text((360 * SCALE, 102 * SCALE), "October 2024 ▾", font=font(12, bold=True), fill=C_TEXT_DARK)

    # Hero Summary Card
    draw_rounded_rect_with_shadow(img, draw, (40, 145, 500, 220), radius=20, fill=C_SURFACE_SLATE, shadow_blur=12, shadow_offset=(0,4), shadow_color=(0,0,0,30))
    draw.text((70 * SCALE, 162 * SCALE), "TOTAL MONTH EXPENSE", font=font(11, bold=True), fill=C_LIGHT_MINT)
    draw.text((70 * SCALE, 182 * SCALE), "₹ 42,350.00", font=font(28, bold=True), fill=C_WHITE)
    draw.text((330 * SCALE, 175 * SCALE), "6 Categories", font=font(12, bold=True), fill=C_MODERN_MINT)
    draw.text((330 * SCALE, 195 * SCALE), "Avg: ₹ 1,366 / day", font=font(11), fill=C_BORDER_GRAY)

    # Donut Pie Chart Rendering
    cx, cy = W // 2, 420 * SCALE
    r_outer = 160 * SCALE
    r_inner = 95 * SCALE

    slices = [
        ("Groceries", 0.40, CAT_FOOD),
        ("Housing & Bills", 0.25, CAT_UTILITIES),
        ("Transport", 0.15, CAT_TRANSPORT),
        ("Shopping", 0.10, CAT_SHOPPING),
        ("Health", 0.06, CAT_HEALTH),
        ("Entertainment", 0.04, CAT_ENT)
    ]

    start_deg = -90
    for name, pct, col in slices:
        sweep = pct * 360
        end_deg = start_deg + sweep

        # Draw Pie Slice using polygon sampling for clean anti-aliased fill
        points = []
        # Outer arc
        steps = max(10, int(sweep))
        for step in range(steps + 1):
            angle = math.radians(start_deg + (sweep * step / steps))
            points.append((cx + r_outer * math.cos(angle), cy + r_outer * math.sin(angle)))
        # Inner arc reverse
        for step in range(steps, -1, -1):
            angle = math.radians(start_deg + (sweep * step / steps))
            points.append((cx + r_inner * math.cos(angle), cy + r_inner * math.sin(angle)))

        draw.polygon(points, fill=col)
        start_deg = end_deg

    # Donut Center Circle
    draw.ellipse((cx - r_inner, cy - r_inner, cx + r_inner, cy + r_inner), fill=C_WHITE)
    draw.text((cx - 40 * SCALE, cy - 20 * SCALE), "OCTOBER", font=font(11, bold=True), fill=C_TEXT_MUTED)
    draw.text((cx - 55 * SCALE, cy + 2 * SCALE), "₹ 42,350", font=font(18, bold=True), fill=C_SLATE_DARK)

    # Category Breakdown Cards List
    draw.text((40 * SCALE, 600 * SCALE), "Category Breakdown", font=font(18, bold=True), fill=C_SLATE_DARK)

    cat_items = [
        ("🛒 Food & Groceries", "₹ 16,940.00", "40.0%", CAT_FOOD),
        ("🏠 Housing & Utilities", "₹ 10,587.50", "25.0%", CAT_UTILITIES),
        ("🚗 Transport & Fuel", "₹ 6,352.50", "15.0%", CAT_TRANSPORT),
        ("🛍️ Shopping & Apparel", "₹ 4,235.00", "10.0%", CAT_SHOPPING),
        ("💊 Health & Medical", "₹ 2,541.00", "6.0%", CAT_HEALTH),
    ]

    by = 640
    for name, amt, pct_str, col in cat_items:
        draw_rounded_rect_with_shadow(img, draw, (40, by, 500, by + 58), radius=14, fill=C_WHITE, shadow_blur=8, shadow_offset=(0,2), shadow_color=(0,0,0,8))

        # Color Dot
        draw.ellipse((60 * SCALE, (by + 20) * SCALE, 76 * SCALE, (by + 36) * SCALE), fill=col)

        draw.text((90 * SCALE, (by + 16) * SCALE), name, font=font(14, bold=True), fill=C_TEXT_DARK)
        draw.text((320 * SCALE, (by + 16) * SCALE), amt, font=font(14, bold=True), fill=C_TEXT_DARK)
        draw.text((435 * SCALE, (by + 16) * SCALE), pct_str, font=font(13, bold=True), fill=col)

        by += 66

    # Smart Insight Box
    draw_rounded_rect_with_shadow(img, draw, (40, 920, 500, 970), radius=14, fill=C_LIGHT_MINT, outline=C_MODERN_MINT, shadow_blur=6, shadow_offset=(0,2), shadow_color=(0,0,0,5))
    draw.text((55 * SCALE, 935 * SCALE), "💡 Insight: Groceries account for 40% of total spend.", font=font(12, bold=True), fill=C_DEEP_EMERALD)

    draw_bottom_nav(draw, img, active_index=2)

    # Save
    out_img = img.resize((FINAL_W, FINAL_H), Image.Resampling.LANCZOS)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_3_piechart.png")
    out_img.save(out_path, "PNG")
    print(f"Saved: {out_path}")

# SCREEN 4: CUSTOM DATE RANGE & CATEGORY FILTER
def render_screenshot_4():
    img = Image.new("RGBA", (W, H), C_LIGHT_BG + (255,))
    draw = ImageDraw.Draw(img)

    # Background - Slightly dimmed transaction screen context
    draw_status_bar(draw, mode='light')

    # Draw faint background elements
    draw.text((40 * SCALE, 100 * SCALE), "Filter & Search Transactions", font=font(20, bold=True), fill=C_SLATE_DARK)
    for i in range(5):
        draw_rounded_rect_with_shadow(img, draw, (40, 160 + i*70, 500, 215 + i*70), radius=10, fill=(240, 245, 250), shadow_blur=0)

    # Dim Overlay Backdrop
    dim_layer = Image.new("RGBA", (W, H), (0, 0, 0, 130))
    img.alpha_composite(dim_layer)
    draw = ImageDraw.Draw(img)

    # Filter Modal Sheet (Elevated Card)
    sheet_box = (30, 180, 510, 950)
    draw_rounded_rect_with_shadow(img, draw, sheet_box, radius=28, fill=C_WHITE, shadow_blur=24, shadow_offset=(0,10), shadow_color=(0,0,0,80))

    # Sheet Header
    draw.text((60 * SCALE, 215 * SCALE), "Filter Transactions", font=font(22, bold=True), fill=C_SLATE_DARK)
    draw.text((430 * SCALE, 220 * SCALE), "Reset", font=font(14, bold=True), fill=C_EXPENSE_CORAL)
    draw.line([(60 * SCALE, 265 * SCALE), (480 * SCALE, 265 * SCALE)], fill=C_BORDER_GRAY, width=1*SCALE)

    # Section 1: Date Range Options
    draw.text((60 * SCALE, 285 * SCALE), "DATE RANGE", font=font(12, bold=True), fill=C_TEXT_MUTED)

    date_opts = [("This Active Cycle", False), ("Last 30 Days", False), ("Custom Date Range", True)]
    dy = 315
    for label, is_sel in date_opts:
        # Radio circle
        r_col = C_DEEP_EMERALD if is_sel else C_BORDER_GRAY
        draw.ellipse((60 * SCALE, (dy + 2) * SCALE, 80 * SCALE, (dy + 22) * SCALE), outline=r_col, width=2*SCALE)
        if is_sel:
            draw.ellipse((66 * SCALE, (dy + 8) * SCALE, 74 * SCALE, (dy + 16) * SCALE), fill=C_DEEP_EMERALD)
        draw.text((95 * SCALE, dy * SCALE), label, font=font(14, bold=is_sel), fill=C_TEXT_DARK)
        dy += 35

    # Date Inputs (From - To)
    draw_rounded_rect_with_shadow(img, draw, (60, 425, 255, 475), radius=12, fill=C_LIGHT_BG, outline=C_MODERN_MINT, width=2)
    draw.text((75 * SCALE, 435 * SCALE), "From: Oct 01, 2024", font=font(12, bold=True), fill=C_DEEP_EMERALD)
    draw.text((225 * SCALE, 435 * SCALE), "📅", font=font(14), fill=C_DEEP_EMERALD)

    draw_rounded_rect_with_shadow(img, draw, (285, 425, 480, 475), radius=12, fill=C_LIGHT_BG, outline=C_MODERN_MINT, width=2)
    draw.text((300 * SCALE, 435 * SCALE), "To: Oct 20, 2024", font=font(12, bold=True), fill=C_DEEP_EMERALD)
    draw.text((450 * SCALE, 435 * SCALE), "📅", font=font(14), fill=C_DEEP_EMERALD)

    # Section 2: Categories Multi-select Chips
    draw.text((60 * SCALE, 500 * SCALE), "CATEGORIES", font=font(12, bold=True), fill=C_TEXT_MUTED)

    cat_chips = [
        ("✓ Groceries", True), ("✓ Utilities", True), ("✓ Transport", True),
        ("Shopping", False), ("Health", False), ("Entertainment", False)
    ]

    cx, cy = 60, 530
    for label, is_sel in cat_chips:
        bg = C_DEEP_EMERALD if is_sel else C_LIGHT_BG
        fg = C_WHITE if is_sel else C_TEXT_DARK
        border = C_DEEP_EMERALD if is_sel else C_BORDER_GRAY
        cw = int(font(12, bold=is_sel).getbbox(label)[2] / SCALE) + 24

        if cx + cw > 480:
            cx = 60
            cy += 45

        draw_rounded_rect_with_shadow(img, draw, (cx, cy, cx + cw, cy + 36), radius=18, fill=bg, outline=border)
        draw.text(((cx + 12) * SCALE, (cy + 8) * SCALE), label, font=font(12, bold=is_sel), fill=fg)
        cx += cw + 10

    # Section 3: Amount Range Slider
    draw.text((60 * SCALE, 650 * SCALE), "AMOUNT RANGE: ₹ 500 — ₹ 10,000", font=font(12, bold=True), fill=C_TEXT_MUTED)

    # Slider Track
    draw.rounded_rectangle((60 * SCALE, 685 * SCALE, 480 * SCALE, 693 * SCALE), radius=4 * SCALE, fill=C_BORDER_GRAY)
    draw.rounded_rectangle((120 * SCALE, 685 * SCALE, 380 * SCALE, 693 * SCALE), radius=4 * SCALE, fill=C_MODERN_MINT)

    # Slider Thumbs
    draw.ellipse((108 * SCALE, 674 * SCALE, 132 * SCALE, 698 * SCALE), fill=C_DEEP_EMERALD)
    draw.ellipse((368 * SCALE, 674 * SCALE, 392 * SCALE, 698 * SCALE), fill=C_DEEP_EMERALD)

    # Section 4: Sort Order Dropdown
    draw.text((60 * SCALE, 730 * SCALE), "SORT BY", font=font(12, bold=True), fill=C_TEXT_MUTED)
    draw_rounded_rect_with_shadow(img, draw, (60, 755, 480, 805), radius=12, fill=C_LIGHT_BG, outline=C_BORDER_GRAY)
    draw.text((80 * SCALE, 770 * SCALE), "Date: Newest First", font=font(13, bold=True), fill=C_TEXT_DARK)
    draw.text((440 * SCALE, 770 * SCALE), "▾", font=font(14, bold=True), fill=C_TEXT_DARK)

    # Bottom Buttons Bar
    draw_rounded_rect_with_shadow(img, draw, (60, 840, 240, 895), radius=16, fill=C_WHITE, outline=C_BORDER_GRAY)
    draw.text((115 * SCALE, 856 * SCALE), "Cancel", font=font(14, bold=True), fill=C_TEXT_DARK)

    draw_rounded_rect_with_shadow(img, draw, (260, 840, 480, 895), radius=16, fill=C_DEEP_EMERALD, shadow_blur=10, shadow_offset=(0,4), shadow_color=(6,78,59,40))
    draw.text((285 * SCALE, 856 * SCALE), "Apply Filters (24)", font=font(14, bold=True), fill=C_WHITE)

    draw_bottom_nav(draw, img, active_index=3)

    # Save
    out_img = img.resize((FINAL_W, FINAL_H), Image.Resampling.LANCZOS)
    out_path = os.path.join(OUTPUT_DIR, "screenshot_4_filter.png")
    out_img.save(out_path, "PNG")
    print(f"Saved: {out_path}")

if __name__ == "__main__":
    render_screenshot_1()
    render_screenshot_2()
    render_screenshot_3()
    render_screenshot_4()
    print("All 4 screenshots generated successfully.")
